package org.makkiato.arcadeclient.data.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.makkiato.arcadeclient.data.exception.client.ConversionException;
import org.makkiato.arcadeclient.data.web.request.BeginTAExchange;
import org.makkiato.arcadeclient.data.web.request.CommandExchange;
import org.makkiato.arcadeclient.data.web.request.QueryExchange;
import org.makkiato.arcadeclient.data.web.response.CommandResponse;
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
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Convenience layer on top of http-requests to the ArcadeDB.
 * This object is immutable.
 */
public class ArcadedbTemplate implements ArcadedbOperations {
    protected static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    protected static final String ARCADEDB_SESSION_ID = "arcadedb-session-id";
    protected final String databaseName;
    protected final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ArcadedbTemplate(String databaseName, WebClient webClient) {
        this.databaseName = databaseName;
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Mono<Boolean> script(Resource resource) throws IOException {
        return script(resource, null);
    }

    @Override
    public Mono<Boolean> script(Resource resource, Map<String, Object> params)
            throws IOException {
        var commands = Arrays.stream(resource.getContentAsString(Charset.defaultCharset()).split(";"))
                .map(String::trim)
                .toArray(String[]::new);
        return script(commands, params);
    }

    @Override
    public Mono<Boolean> script(CommandLanguage language, Resource resource, Map<String, Object> params) throws IOException {
        var command = resource.getContentAsString(Charset.defaultCharset());
        return new CommandExchange(language, command, databaseName, params, webClient)
                .exchange()
                .hasElement();
    }

    @Override
    public Mono<Boolean> script(String[] commands) {
        return script(commands, null);
    }

    @Override
    public Mono<Boolean> script(String[] commands, Map<String, Object> params) {
        var command = Arrays.stream(commands).map(c -> String.format("\"%s\"", c)).collect(Collectors.joining(";"));
        return new CommandExchange(CommandLanguage.SQLSCRIPT, command, databaseName, params, webClient)
                .exchange()
                .hasElement();
    }

    @Override
    public Flux<Map<String, Object>> command(String command) {
        return command(CommandLanguage.SQL, command, null);
    }

    @Override
    public Flux<Map<String, Object>> command(String command, Map<String, Object> params) {
        return command(CommandLanguage.SQL, command, params);
    }

    @Override
    public Flux<Map<String, Object>> command(CommandLanguage language, String command, Map<String, Object> params) {
        return new CommandExchange(language, command, databaseName, params, webClient)
                .exchange()
                .map(CommandResponse::result)
                .flatMapMany(Flux::fromArray);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> insertDocument(String documentName, T object) {
        return command(String.format("insert into %s content %s", documentName, convertObjectToJsonString(object)))
                .elementAt(0)
                .map(result -> convertMapToObject((Class<T>) object.getClass(), result));
    }

    @Override
    public <T extends DocumentBase> Mono<T> insertDocument(T object) {
        return insertDocument(object.getType(), object);
    }

    @Override
    public <T> Mono<Map<String, Object>> updateDocument(String rid, T object) {
        return command(String.format("update %s content %s", rid, convertObjectToJsonString(object)))
                .elementAt(0);
    }

    @Override
    public <T extends DocumentBase> Mono<Map<String, Object>> updateDocument(T object) {
        return updateDocument(object.getRid(), object);
    }

    /**
     * @param document the document to be updated
     * @param <T>
     * @return the updated object
     */
    @Override
    public <T extends DocumentBase> Mono<T> mergeDocument(T document) {
        return command(String.format("update %s merge %s upsert return after", document.getRid() == null ?
                document.getType() : document.getRid(), convertObjectToJsonString(document)))
                .elementAt(0)
                .map(result -> convertMapToObject((Class<T>) document.getClass(), result));
    }

    @Override
    public <T> Flux<T> selectDocument(String command, Class<T> objectType) {
        return selectDocument(CommandLanguage.SQL, command, null, objectType, this::convertMapToObject);
    }

    @Override
    public <T> Flux<T> selectDocument(String command, Map<String, Object> params, Class<T> objectType) {
        return selectDocument(CommandLanguage.SQL, command, params, objectType, this::convertMapToObject);
    }

    @Override
    public <T> Flux<T> selectDocument(CommandLanguage language, String command, Class<T> objectType) {
        return selectDocument(language, command, null, objectType, this::convertMapToObject);
    }

    @Override
    public <T> Flux<T> selectDocument(CommandLanguage language, String command, Map<String, Object> params,
                                      Class<T> objectType) {
        return selectDocument(language, command, params, objectType, this::convertMapToObject);
    }

    @Override
    public <T> Flux<T> selectDocument(CommandLanguage language, String command, Map<String, Object> params,
                                      Class<T> objectType, BiFunction<Class<T>, Map<String, Object>, T> mapper) {
        return new CommandExchange(language, command, databaseName, params, webClient)
                .exchange()
                .map(CommandResponse::result)
                .map(resultArray -> Arrays.stream(resultArray)
                        .map(result -> mapper.apply(objectType, result))
                        .collect(Collectors.toList()))
                .flatMapMany(Flux::fromIterable);

    }

    @Override
    public Flux<Map<String, Object>> query(String query) {
        return new QueryExchange(CommandLanguage.SQL, query, databaseName, webClient)
                .exchange()
                .map(CommandResponse::result)
                .flatMapMany(Flux::fromArray);
    }

    @Override
    public <T> Mono<T> findById(String rid, Class<T> objectType) {
        return selectDocument(String.format("select from [%s]", rid), objectType).elementAt(0);
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

    public TransactionalTemplate transactional() {
        return new BeginTAExchange(databaseName, webClient).exchange()
                .map(EmptyResponse::headers)
                .filter(header -> header.containsKey(ARCADEDB_SESSION_ID))
                .map(header -> header.get(ARCADEDB_SESSION_ID))
                .filter(item -> !item.isEmpty())
                .map(item -> item.get(0))
                .map(id -> new TransactionalTemplate(databaseName,
                        webClient.mutate().defaultHeader(ARCADEDB_SESSION_ID, id).build()))
                .block(CONNECTION_TIMEOUT);
    }
}
