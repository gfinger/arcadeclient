package org.makkiato.arcadedb.client;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.makkiato.arcadedb.client.exception.client.ConversionException;
import org.makkiato.arcadedb.client.web.request.BeginTAExchange;
import org.makkiato.arcadedb.client.web.request.CommandExchange;
import org.makkiato.arcadedb.client.web.request.CommitTAExchange;
import org.makkiato.arcadedb.client.web.request.QueryExchange;
import org.makkiato.arcadedb.client.web.request.RollbackTAExchange;
import org.makkiato.arcadedb.client.web.request.ServerExchange;
import org.makkiato.arcadedb.client.web.response.EmptyResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    public Mono<Boolean> script(Resource resource) throws IOException {
        return script(resource, null);
    }

    public Mono<Boolean> script(Resource resource, Map<String, Object> params) throws IOException {
        var command = resource.getContentAsString(Charset.defaultCharset());
        return isClosed ? Mono.just(false)
                : new CommandExchange("sqlscript", command, databaseName, params, webClient)
                        .exchange()
                        .hasElement();
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

    @SuppressWarnings("unchecked")
    public <T> Mono<T> insertObject(String documentName, T object) {
        return isClosed ? Mono.empty()
                : command(String.format("insert into %s content %s", documentName, convertObjectToJsonString(object)))
                        .elementAt(0)
                        .map(result -> convertMapToObject((Class<T>) object.getClass(), result));
    }

    public <T extends Vertex> Mono<T> insertObject(T object) {
        return insertObject(object.getType(), object);
    }

    public <T> Mono<Map<String, Object>> updateObject(String rid, T object) {
        return isClosed ? Mono.empty()
                : command(String.format("update %s content %s", rid, convertObjectToJsonString(object)))
                        .elementAt(0);
    }

    public <T extends Vertex> Mono<Map<String, Object>> updateObject(T object) {
        return updateObject(object.getRid(), object);
    }

    public <T> Mono<Map<String, Object>> mergeObject(String rid, T object) {
        return isClosed ? Mono.empty()
                : command(String.format("update %s merge %s", rid, convertObjectToJsonString(object)))
                        .elementAt(0);
    }

    public <T extends Vertex> Mono<Map<String, Object>> mergeObject(T object) {
        return updateObject(object.getRid(), object);
    }

    public <T> Flux<T> selectObject(String command,
            Class<T> objectType) {
        return selectObject("sql", command, null, objectType, (x, y) -> convertMapToObject(x, y));
    }

    public <T> Flux<T> selectObject(String command, Map<String, Object> params,
            Class<T> objectType) {
        return selectObject("sql", command, params, objectType, (x, y) -> convertMapToObject(x, y));
    }

    public <T> Mono<T> findById(String rid, Class<T> objectType) {
        return selectObject(String.format("select from [%s]", rid), objectType).elementAt(0);
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

    private <T> String convertObjectToJsonString(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new ConversionException(String.format("cannot convert object %s", object.toString()), ex);
        }
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
