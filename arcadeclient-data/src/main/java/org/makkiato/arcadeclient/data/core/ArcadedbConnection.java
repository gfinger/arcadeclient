package org.makkiato.arcadeclient.data.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.makkiato.arcadeclient.data.exception.client.ConversionException;
import org.makkiato.arcadeclient.data.web.request.*;
import org.makkiato.arcadeclient.data.web.response.EmptyResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Covenience layer on top of http-requests to the ArcadeDB.
 * This object is immutable.
 */
public class ArcadedbConnection implements ArcadedbOperations{
    protected static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    protected static final String ARCADEDB_SESSION_ID = "arcadedb-session-id";
    protected final String databaseName;
    protected final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ArcadedbConnection(String databaseName, WebClient webClient) {
        this.databaseName = databaseName;
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Mono<Boolean> script(Resource resource) throws IOException {
        return script(resource, (Map<String, Object>) null);
    }

    public Mono<Boolean> script(Resource resource, TransactionHandle transactionHandle) throws IOException {
        return script(resource, null, transactionHandle);
    }

    public Mono<Boolean> script(Resource resource, Map<String, Object> params) throws IOException {
        return script(resource, params, null);
    }

    public Mono<Boolean> script(Resource resource, Map<String, Object> params, TransactionHandle transactionHandle)
            throws IOException {
        var commands = Arrays.stream(resource.getContentAsString(Charset.defaultCharset()).split(";"))
                .map(line -> line.trim())
                .toArray(String[]::new);
        return script(commands, params, transactionHandle);
    }

    public Mono<Boolean> script(String language, Resource resource, Map<String, Object> params,
            TransactionHandle transactionHandle) throws IOException {
        var command = resource.getContentAsString(Charset.defaultCharset());
        return new CommandExchange(language, command, databaseName, params,
                transactionHandle != null ? transactionHandle.webClient() : webClient)
                .exchange()
                .hasElement();
    }

    public Mono<Boolean> script(String[] commands, TransactionHandle transactionHandle) {
        return script(commands, null, transactionHandle);
    }

    public Mono<Boolean> script(String[] commands, Map<String, Object> params) {
        return script(commands, params, null);
    }

    public Mono<Boolean> script(String[] commands, Map<String, Object> params, TransactionHandle transactionHandle) {
        var command = Arrays.stream(commands).map(c -> String.format("\"%s\"", c)).collect(Collectors.joining(";"));
        return new CommandExchange("sqlscript", command, databaseName, params,
                transactionHandle != null ? transactionHandle.webClient() : webClient)
                .exchange()
                .hasElement();
    }

    public Flux<Map<String, Object>> command(String command) {
        return command("sql", command, null, null);
    }

    public Flux<Map<String, Object>> command(String command, TransactionHandle transactionHandle) {
        return command(command, null, transactionHandle);
    }

    public Flux<Map<String, Object>> command(String command, Map<String, Object> params) {
        return command("sql", command, params, null);
    }

    public Flux<Map<String, Object>> command(String command, Map<String, Object> params,
            TransactionHandle transactionHandle) {
        return command("sql", command, params, transactionHandle);
    }

    public Flux<Map<String, Object>> command(String language, String command, Map<String, Object> params) {
        return command(language, command, params);
    }

    public Flux<Map<String, Object>> command(String language, String command, Map<String, Object> params,
            TransactionHandle transactionHandle) {
        return new CommandExchange(language, command, databaseName, params,
                transactionHandle != null ? transactionHandle.webClient() : webClient)
                .exchange()
                .map(response -> response.result())
                .flatMapMany(Flux::fromArray);
    }

    public <T> Mono<T> insertObject(String documentName, T object) {
        return insertObject(documentName, object, null);
    }

    @SuppressWarnings("unchecked")
    public <T> Mono<T> insertObject(String documentName, T object, TransactionHandle transactionHandle) {
        return command(String.format("insert into %s content %s", documentName, convertObjectToJsonString(object)),
                transactionHandle)
                .elementAt(0)
                .map(result -> convertMapToObject((Class<T>) object.getClass(), result));
    }

    public <T extends VertexParent> Mono<T> insertObject(T object) {
        return insertObject(object, null);
    }

    public <T extends VertexParent> Mono<T> insertObject(T object, TransactionHandle transactionHandle) {
        return insertObject(object.getType(), object, transactionHandle);
    }

    public <T> Mono<Map<String, Object>> updateObject(String rid, T object) {
        return updateObject(rid, object, null);
    }

    public <T> Mono<Map<String, Object>> updateObject(String rid, T object, TransactionHandle transactionHandle) {
        return command(String.format("update %s content %s", rid, convertObjectToJsonString(object)))
                .elementAt(0);
    }

    public <T extends VertexParent> Mono<Map<String, Object>> updateObject(T object) {
        return updateObject(object, null);
    }

    public <T extends VertexParent> Mono<Map<String, Object>> updateObject(T object, TransactionHandle transactionHandle) {
        return updateObject(object.getRid(), object, transactionHandle);
    }

    public <T extends VertexParent> Mono<Map<String, Object>> mergeObject(T object) {
        return updateObject(object, null);
    }

    public <T extends VertexParent> Mono<Map<String, Object>> mergeObject(T object, TransactionHandle transactionHandle) {
        return updateObject(object.getRid(), object, transactionHandle);
    }

    public <T> Mono<Map<String, Object>> mergeObject(String rid, T object) {
        return command(String.format("update %s merge %s", rid, convertObjectToJsonString(object)))
                .elementAt(0);
    }

    public <T> Flux<T> selectObject(String command,
            Class<T> objectType) {
        return selectObject(command, objectType, null);
    }

    public <T> Flux<T> selectObject(String command,
            Class<T> objectType, TransactionHandle transactionHandle) {
        return selectObject("sql", command, null, objectType, (x, y) -> convertMapToObject(x, y));
    }

    public <T> Flux<T> selectObject(String command, Map<String, Object> params,
            Class<T> objectType) {
        return selectObject(command, params, objectType, null);
    }

    public <T> Flux<T> selectObject(String command, Map<String, Object> params,
            Class<T> objectType, TransactionHandle transactionHandle) {
        return selectObject("sql", command, params, objectType, (x, y) -> convertMapToObject(x, y));
    }

    public <T> Flux<T> selectObject(String language, String command,
            Class<T> objectType) {
        return selectObject(language, command, objectType, (TransactionHandle) null);
    }

    public <T> Flux<T> selectObject(String language, String command,
            Class<T> objectType, TransactionHandle transactionHandle) {
        return selectObject(language, command, null, objectType, (x, y) -> convertMapToObject(x, y));
    }

    public <T> Flux<T> selectObject(String language, String command, Map<String, Object> params,
            Class<T> objectType) {
        return selectObject(language, command, params, objectType, (TransactionHandle) null);
    }

    public <T> Flux<T> selectObject(String language, String command, Map<String, Object> params,
            Class<T> objectType, TransactionHandle transactionHandle) {
        return selectObject(language, command, params, objectType, (x, y) -> convertMapToObject(x, y),
                transactionHandle);
    }

    public <T> Flux<T> selectObject(String language, String command, Map<String, Object> params,
            Class<T> objectType, BiFunction<Class<T>, Map<String, Object>, T> mapper) {
        return selectObject(language, command, params, objectType, mapper, null);
    }

    public <T> Flux<T> selectObject(String language, String command, Map<String, Object> params,
            Class<T> objectType, BiFunction<Class<T>, Map<String, Object>, T> mapper,
            TransactionHandle transactionHandle) {
        return new CommandExchange(language, command, databaseName, params,
                transactionHandle != null ? transactionHandle.webClient() : webClient)
                .exchange()
                .map(response -> response.result())
                .map(resultArray -> Arrays.stream(resultArray)
                        .map(result -> mapper.apply(objectType, result))
                        .collect(Collectors.toList()))
                .flatMapMany(list -> Flux.fromIterable(list));

    }

    public <T> Mono<T> findById(String rid, Class<T> objectType) {
        return findById(rid, objectType, null);
    }

    public <T> Mono<T> findById(String rid, Class<T> objectType, TransactionHandle transactionHandle) {
        return selectObject(String.format("select from [%s]", rid), objectType, transactionHandle).elementAt(0);
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
        return new QueryExchange("sql", query, databaseName, webClient)
                .exchange()
                .map(response -> response.result())
                .flatMapMany(Flux::fromArray);
    }

    public Optional<TransactionHandle> beginTransaction() {
        Optional<String> sessionId = new BeginTAExchange(databaseName, webClient).exchange()
                .map(EmptyResponse::headers)
                .filter(header -> header.containsKey(ARCADEDB_SESSION_ID))
                .map(header -> header.get(ARCADEDB_SESSION_ID))
                .filter(item -> !item.isEmpty())
                .map(item -> item.get(0))
                .blockOptional(CONNECTION_TIMEOUT);
        return sessionId.map(id -> new TransactionHandle(id,
                webClient.mutate().defaultHeader(ARCADEDB_SESSION_ID, id).build()));
    }

    public TransactionalConnection transactional() {
        return new BeginTAExchange(databaseName, webClient).exchange()
                .map(EmptyResponse::headers)
                .filter(header -> header.containsKey(ARCADEDB_SESSION_ID))
                .map(header -> header.get(ARCADEDB_SESSION_ID))
                .filter(item -> !item.isEmpty())
                .map(item -> item.get(0))
                .map(id -> new TransactionalConnection(databaseName,
                        webClient.mutate().defaultHeader(ARCADEDB_SESSION_ID, id).build()))
                .block(CONNECTION_TIMEOUT);
    }

    public Boolean commitTransaction(TransactionHandle transactionHandle) {
        Optional<EmptyResponse> response = new CommitTAExchange(databaseName, transactionHandle.webClient()).exchange()
                .blockOptional(CONNECTION_TIMEOUT);
        return response.isPresent();
    }

    public Boolean rollbackTransaction(TransactionHandle transactionHandle) {
        Optional<EmptyResponse> response = new RollbackTAExchange(databaseName, transactionHandle.webClient())
                .exchange()
                .blockOptional(CONNECTION_TIMEOUT);
        return response.isPresent();
    }
}
