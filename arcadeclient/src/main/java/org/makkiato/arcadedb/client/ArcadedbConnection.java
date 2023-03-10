package org.makkiato.arcadedb.client;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.makkiato.arcadedb.client.http.request.BeginTAExchange;
import org.makkiato.arcadedb.client.http.request.CommandExchange;
import org.makkiato.arcadedb.client.http.request.CommitTAExchange;
import org.makkiato.arcadedb.client.http.request.QueryExchange;
import org.makkiato.arcadedb.client.http.request.RollbackTAExchange;
import org.makkiato.arcadedb.client.http.request.ServerExchange;
import org.makkiato.arcadedb.client.http.response.EmptyResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Represents one session on the ArcadeDB.
 * This class is not thread-safe!
 */
public class ArcadedbConnection implements AutoCloseable {
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    private static final String ARCADEDB_SESSION_ID = "arcadedb-session-id";
    @Getter
    private final String databaseName;
    private WebClient webClient;
    private boolean isClosed = false;
    private final ObjectMapper objectMapper;

    public ArcadedbConnection(String databaseName, WebClient webClient) {
        this.databaseName = databaseName;
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Mono<Boolean> script(String[] commands) {
        return script(commands, null);
    }

    public Mono<Boolean> script(String[] commands, Map<String, Object> params) {
        var command = Arrays.stream(commands).map(c -> String.format("\"%s\"", c)).collect(Collectors.joining(";"));
        return isClosed ? Mono.just(false)
                : new CommandExchange("sqlscript", command, databaseName, params, webClient)
                        .exchange()
                        .hasElement();
    }

    public Flux<Map<String, Object>> command(String command) {
        return command(command, null);
    }

    public Flux<Map<String, Object>> command(String command, Map<String, Object> params) {
        return command("sql", command, params);
    }

    public Flux<Map<String, Object>> command(String language, String command, Map<String, Object> params) {
        return isClosed ? Flux.empty()
                : new CommandExchange(language, command, databaseName, params, webClient)
                        .exchange()
                        .map(response -> response.result())
                        .flatMapMany(Flux::fromArray);
    }

    public <T> Flux<T> selectObject(String command,
            Class<T> objectType) {
        return selectObject("sql", command, null, objectType, (x, y) -> convertMapToObject(x, y));
    }

    public <T> Flux<T> selectObject(String command, Map<String, Object> params,
            Class<T> objectType) {
        return selectObject("sql", command, params, objectType, (x, y) -> convertMapToObject(x, y));
    }

    public <T> Flux<T> selectObject(String language, String command,
            Class<T> objectType) {
        return selectObject(language, command, null, objectType, (x, y) -> convertMapToObject(x, y));
    }

    public <T> Flux<T> selectObject(String language, String command, Map<String, Object> params,
            Class<T> objectType) {
        return selectObject(language, command, params, objectType, (x, y) -> convertMapToObject(x, y));
    }

    public <T> Flux<T> selectObject(String language, String command, Map<String, Object> params,
            Class<T> objectType, BiFunction<Class<T>, Map<String, Object>, T> mapper) {
        return isClosed ? Flux.empty()
                : new CommandExchange(language, command, databaseName, params, webClient)
                        .exchange()
                        .map(response -> response.result())
                        .map(resultArray -> Arrays.stream(resultArray)
                                .map(result -> mapper.apply(objectType, result))
                                .collect(Collectors.toList()))
                        .flatMapMany(list -> Flux.fromIterable(list));

    }

    private <T> T convertMapToObject(Class<T> objectType, Map<String, Object> map) {
        return objectMapper.convertValue(map, objectType);
    }

    public Flux<Map<String, Object>> query(String query) {
        return isClosed ? Flux.empty()
                : new QueryExchange("sql", query, databaseName, webClient)
                        .exchange()
                        .map(response -> response.result())
                        .flatMapMany(Flux::fromArray);
    }

    public void close() {
        this.isClosed = true;
        new ServerExchange("sql", String.format("close database %s", databaseName), webClient)
                .exchange().map(response -> response.result().equalsIgnoreCase("ok"))
                .block(CONNECTION_TIMEOUT);
    }

    public Boolean beginTransaction() {
        Optional<String> sessionId = new BeginTAExchange(databaseName, webClient).exchange()
                .map(EmptyResponse::headers)
                .filter(header -> header.containsKey(ARCADEDB_SESSION_ID))
                .map(header -> header.get(ARCADEDB_SESSION_ID))
                .filter(item -> !item.isEmpty())
                .map(item -> item.get(0))
                .blockOptional(CONNECTION_TIMEOUT);
        webClient = webClient.mutate().defaultHeader(ARCADEDB_SESSION_ID, sessionId.orElse("")).build();
        return sessionId.isPresent();
    }

    public Boolean commitTransaction() {
        Optional<EmptyResponse> response = new CommitTAExchange(databaseName, webClient).exchange()
                .blockOptional(CONNECTION_TIMEOUT);
        webClient = webClient.mutate().defaultHeader(ARCADEDB_SESSION_ID, "").build();
        return response.isPresent();
    }

    public Boolean rollbackTransaction() {
        Optional<EmptyResponse> response = new RollbackTAExchange(databaseName, webClient).exchange()
                .blockOptional(CONNECTION_TIMEOUT);
        webClient = webClient.mutate().defaultHeader(ARCADEDB_SESSION_ID, "").build();
        return response.isPresent();
    }
}
