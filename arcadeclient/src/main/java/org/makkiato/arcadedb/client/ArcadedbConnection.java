package org.makkiato.arcadedb.client;

import lombok.Getter;
import org.makkiato.arcadedb.client.http.request.*;
import org.makkiato.arcadedb.client.http.response.EmptyResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public ArcadedbConnection(String databaseName, WebClient webClient) {
        this.databaseName = databaseName;
        this.webClient = webClient;
    }

    public Flux<Map<String, Object>> script(String[] commands) {
        var command = Arrays.stream(commands).map(c -> String.format("\"%s\"", c)).collect(Collectors.joining(";"));
        return command("sqlscript", command, null);
    }

    public Flux<Map<String, Object>> script(String[] commands, Map<String, Object> params) {
        var command = Arrays.stream(commands).map(c -> String.format("\"%s\"", c)).collect(Collectors.joining(";"));
        return command("sqlscript", command, params);
    }


    public Flux<Map<String, Object>> command(String command) {
        return command(command, null);
    }

    public Flux<Map<String, Object>> command(String command, Map<String, Object> params) {
        return command("sql", command, params);
    }

    public Flux<Map<String, Object>> command(String language, String command, Map<String, Object> params) {
        return isClosed ? Flux.empty() : new CommandExchange(language, command, databaseName, params, webClient)
                .exchange()
                .map(response -> response.result())
                .flatMapMany(Flux::fromArray);
    }

    public Flux<Map<String, Object>> query(String query) {
        return isClosed ? Flux.empty() : new QueryExchange("sql", query, databaseName, webClient)
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
        Optional<EmptyResponse> response = new CommitTAExchange(databaseName, webClient).exchange().blockOptional(CONNECTION_TIMEOUT);
        webClient = webClient.mutate().defaultHeader(ARCADEDB_SESSION_ID, "").build();
        return response.isPresent();
    }

    public Boolean rollbackTransaction() {
        Optional<EmptyResponse> response = new RollbackTAExchange(databaseName, webClient).exchange().blockOptional(CONNECTION_TIMEOUT);
        webClient = webClient.mutate().defaultHeader(ARCADEDB_SESSION_ID, "").build();
        return response.isPresent();
    }
}
