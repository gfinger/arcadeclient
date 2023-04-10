package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.base.DocumentBase;
import org.makkiato.arcadeclient.data.base.EdgeBase;
import org.makkiato.arcadeclient.data.base.IdentifiableDocumentBase;
import org.makkiato.arcadeclient.data.base.VertexBase;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientEntityConverter;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientMappingContext;
import org.makkiato.arcadeclient.data.web.request.*;
import org.makkiato.arcadeclient.data.web.response.CommandResponse;
import org.makkiato.arcadeclient.data.web.response.EmptyResponse;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ArcadedbTemplate implements GenericOperations, BasicOperations, ConversionAwareOperations, TransactionalOperations {
    private final String databaseName;
    private final WebClient webClient;
    private final ArcadeclientEntityConverter entityConverter;

    public ArcadedbTemplate(String databaseName, WebClient webClient, ArcadeclientEntityConverter entityConverter) {
        this.databaseName = databaseName;
        this.webClient = webClient;
        this.entityConverter = entityConverter;
    }

    @Override
    public ArcadeclientEntityConverter getEntityConverter() {
        return entityConverter;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public WebClient getWebClient() {
        return webClient;
    }


    public TransactionalOperations transactional() {
        return new BeginTAExchange(getDatabaseName(), getWebClient()).exchange()
                .map(EmptyResponse::headers)
                .filter(header -> header.containsKey(ARCADEDB_SESSION_ID))
                .map(header -> header.get(ARCADEDB_SESSION_ID))
                .filter(item -> !item.isEmpty())
                .map(item -> item.get(0))
                .map(id -> new ArcadedbTemplate(getDatabaseName(),
                        getWebClient().mutate().defaultHeader(ARCADEDB_SESSION_ID, id).build(), entityConverter))
                .block(CONNECTION_TIMEOUT);
    }

    /* GenericOperations */

    public Mono<Boolean> script(Resource resource) throws IOException {
        return script(resource, null);
    }

    public Mono<Boolean> script(Resource resource, Map<String, Object> params)
            throws IOException {
        var commands = Arrays.stream(resource.getContentAsString(Charset.defaultCharset()).split(";"))
                .map(String::trim)
                .toArray(String[]::new);
        return script(commands, params);
    }

    public Mono<Boolean> script(CommandLanguage language, Resource resource, Map<String, Object> params)
            throws IOException {
        var command = resource.getContentAsString(Charset.defaultCharset());
        return new CommandExchange(language, command, getDatabaseName(), params, getWebClient())
                .exchange()
                .hasElement();
    }

    public Mono<Boolean> script(String[] commands) {
        return script(commands, null);
    }

    public Mono<Boolean> script(String[] commands, Map<String, Object> params) {
        var command = Arrays.stream(commands).map(c -> String.format("%s", c)).collect(Collectors.joining(";"));
        return new CommandExchange(CommandLanguage.SQLSCRIPT, command, getDatabaseName(), params, getWebClient())
                .exchange()
                .hasElement();
    }

    public Flux<Map<String, Object>> command(String command) {
        return command(CommandLanguage.SQL, command, null);
    }

    public Flux<Map<String, Object>> command(String command, Map<String, Object> params) {
        return command(CommandLanguage.SQL, command, params);
    }

    public Flux<Map<String, Object>> command(CommandLanguage language, String command, Map<String, Object> params) {
        return new CommandExchange(language, command, getDatabaseName(), params, getWebClient())
                .exchange()
                .map(CommandResponse::result)
                .flatMapMany(Flux::fromArray);
    }

    public Flux<Map<String, Object>> query(String query) {
        return new QueryExchange(CommandLanguage.SQL, query, getDatabaseName(), getWebClient())
                .exchange()
                .map(CommandResponse::result)
                .flatMapMany(Flux::fromArray);
    }


    /* BasicOperations */

    public Mono<Map<String, Object>> insert(String documentTypeName, String jsonObject) {
        return command(String.format("insert into %s content %s", documentTypeName, jsonObject))
                .elementAt(0);
    }

    public Mono<Long> count(String documentTypeName) {
        Assert.notNull(documentTypeName, "Document name must not be empty");
        return command(String.format("select count() as c from %s", documentTypeName))
                .elementAt(0)
                .map(result -> result.get("c"))
                .cast(Long.class);
    }

    public Mono<Void> deleteById(String id, String documentTypeName) {
        Assert.notNull(id, "RID of document must not be empty");
        Assert.notNull(documentTypeName, "Document name must not be empty");
        return command(String.format("delete from %s where @rid in [%s] ", documentTypeName, id))
                .elementAt(0).flatMap(r -> Mono.empty());
    }

    public Mono<Void> deleteAllById(Iterable<? extends String> ids, String documentTypeName) {
        Assert.notNull(ids, "RID of document must not be empty");
        Assert.notNull(documentTypeName, "Document name must not be empty");
        return command(String.format("delete from %s where @rid in [%s]", documentTypeName,
                StreamSupport.stream(ids.spliterator(), false).collect(Collectors.joining())))
                .elementAt(0).flatMap(r -> Mono.empty());
    }

    public Mono<Void> deleteAll(String documentTypeName) {
        Assert.notNull(documentTypeName, "Document name must not be empty");
        return command(String.format("delete from %s", documentTypeName))
                .elementAt(0).map(result -> result.get("count")).log().then(Mono.empty());
    }

    public Mono<Boolean> exists(String rid) {
        return command(String.format("select from [%s]", rid)).hasElements();
    }


    /* ConversionAwareOperations */

    public <T extends IdentifiableDocumentBase> Mono<T> insertDocument(T entity) {
        var documentTypeName =
                getEntityConverter().getMappingContext().getRequiredPersistentEntity(entity.getClass()).getDocumentType();
        return command(String.format("insert into %s content %s", documentTypeName, convertObjectToJsonString(entity)))
                .elementAt(0)
                .map(item -> convertMapToObject((Class<T>) entity.getClass(), item));
    }

    public <T extends IdentifiableDocumentBase> Mono<Map<String, Object>> update(String rid, T entity) {
        return command(String.format("update %s content %s return after", rid, convertObjectToJsonString(entity)))
                .elementAt(0);
    }

    public <T extends IdentifiableDocumentBase> Mono<T> updateDocument(T entity) {
        Assert.notNull(entity, "Document must not be empty");
        Assert.notNull(entity.getRid(), "RID of document must not be empty");
        return command(String.format("update %s content %s return after", entity.getRid(),
                convertObjectToJsonString(entity)))
                .elementAt(0)
                .map(result -> convertMapToObject((Class<T>) entity.getClass(), result));
    }

    /**
     * Merges an instance of {@link org.makkiato.arcadeclient.data.base.Document} into the corresponding Document
     * in the database if it exists, or creates a new one, if not.
     * Whether a document is merged or inserted depends on whether there is a RID in the document or not.
     * The returned value is the document after being updated.
     *
     * @param entity the document to be updated (must not be null)
     * @param <T>    Document type
     * @return the updated object
     */
    public <T extends IdentifiableDocumentBase> Mono<T> mergeDocument(T entity) {
        Assert.notNull(entity, "Document must not be empty");
        if (entity.getRid() != null) {
            return command(String.format("update %s merge %s upsert return after", entity.getRid(),
                    convertObjectToJsonString(entity)))
                    .elementAt(0)
                    .map(result -> convertMapToObject((Class<T>) entity.getClass(), result));
        } else {
            return insertDocument(entity);
        }
    }

    public <T extends IdentifiableDocumentBase> Flux<T> select(String command, Class<T> entityType) {
        return select(CommandLanguage.SQL, command, null, entityType, this::convertMapToObject);
    }

    public <T extends IdentifiableDocumentBase> Flux<T> select(String command, Map<String, Object> params,
                                                               Class<T> entityType) {
        return select(CommandLanguage.SQL, command, params, entityType, this::convertMapToObject);
    }

    public <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command,
                                                               Class<T> entityType) {
        return select(language, command, null, entityType, this::convertMapToObject);
    }

    public <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command, Map<String,
            Object> params, Class<T> entityType) {
        return select(language, command, params, entityType, this::convertMapToObject);
    }

    public <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command, Map<String,
            Object> params, Class<T> entityType, BiFunction<Class<T>, Map<String, Object>, T> mapper) {
        return new CommandExchange(language, command, getDatabaseName(), params, getWebClient())
                .exchange()
                .map(CommandResponse::result)
                .map(resultArray -> Arrays.stream(resultArray)
                        .map(result -> mapper.apply(entityType, result))
                        .collect(Collectors.toList()))
                .flatMapMany(Flux::fromIterable);

    }

    public <T extends IdentifiableDocumentBase> Mono<Void> deleteDocument(T entity) {
        Assert.notNull(entity, "Document must not be empty");
        Assert.notNull(entity.getRid(), "RID of document must not be empty");
        return command(String.format("delete from %s where @rid in [%s] ", getDocumentTypeNameForEntity(entity),
                entity.getRid()))
                .elementAt(0).flatMap(r -> Mono.empty());
    }

    public <T extends IdentifiableDocumentBase> Mono<T> findById(String rid, Class<T> entityType) {
        return select(String.format("select from [%s]", rid), entityType).elementAt(0);
    }

    public <T extends IdentifiableDocumentBase> Flux<T> findAll(Class<T> entityType) {
        return select(String.format("select from %s", getDocumentTypeNameForEntityType(entityType)), entityType);
    }

    public <E extends EdgeBase, F extends VertexBase, T extends VertexBase> Mono<E> createEdge(F from, T to,
                                                                                               Class<E> edge) {
        Assert.notNull(from, "From vertex must not be empty");
        Assert.notNull(from.getRid(), "From vertex must have a non-empty @rid");
        Assert.notNull(to, "To vertex must not be empty");
        Assert.notNull(to.getRid(), "To vertex must have a non-empty @rid");
        Assert.notNull(edge, "Edge must not be empty");
        return command(String.format("create edge %s from %s to %s", getDocumentTypeNameForEntityType(edge),
                from.getRid(), to.getRid()))
                .elementAt(0)
                .map(item -> convertMapToObject(edge, item));
    }

    public <F extends VertexBase> Flux<VertexBase> outVertices(F from) {
        return command(String.format("select out() from %s", getDocumentTypeNameForEntityType(from.getClass())))
                .flatMap(result -> Flux.fromIterable((Iterable) result.get("out()")))
                .map(item -> convertMapToObject((Map) item));
    }

    public <F extends VertexBase, E extends EdgeBase> Flux<VertexBase> outVertices(F from, Class<E> edgeType) {
        var edgeDocumentName = getDocumentTypeNameForEntityType(edgeType);
        return command(String.format("select out(%s) from %s", edgeDocumentName,
                getDocumentTypeNameForEntityType(from.getClass())))
                .flatMap(result -> Flux.fromIterable((Iterable) result.get(String.format("out(%s)", edgeDocumentName))))
                .map(item -> convertMapToObject((Map) item));
    }

    public <F extends VertexBase> Flux<EdgeBase> outEdges(F from) {
        return null;
    }

    public <F extends VertexBase, E extends EdgeBase> Flux<E> outEdges(F from, Class<E> edgeType) {
        return null;
    }

    public <T extends VertexBase> Flux<VertexBase> inVertices(T to) {
        return null;
    }

    public <T extends VertexBase, E extends EdgeBase> Flux<VertexBase> inVertices(T to, Class<E> edgeType) {
        return null;
    }

    public <T extends VertexBase> Flux<EdgeBase> inEdges(T to) {
        return null;
    }

    public <T extends VertexBase, E extends EdgeBase> Flux<E> inEdges(T to, Class<E> edgeType) {
        return null;
    }

    public <V extends VertexBase> Flux<VertexBase> bothVertices(V vertex) {
        return null;
    }

    public <V extends VertexBase, E extends EdgeBase> Flux<VertexBase> bothVertices(V vertex, Class<E> edgeType) {
        return null;
    }

    public <V extends VertexBase> Flux<EdgeBase> bothEdges(V vertex) {
        return null;
    }

    public <V extends VertexBase, E extends EdgeBase> Flux<E> bothEdges(V vertex, Class<E> edgeType) {
        return null;
    }


    public String convertObjectToJsonString(Object object) {
        var buffer = new StringBuffer();
        getEntityConverter().write(object, buffer);
        return buffer.toString();
    }

    public <T extends DocumentBase> T convertMapToObject(Class<T> entityType, Map<String, Object> item) {
        return getEntityConverter().read(entityType, item);
    }

    public <T extends DocumentBase> T convertMapToObject(Map<String, Object> item) {
        var documentTypeName = (String) ((Map) item).get("@type");
        var clazz = (Class<T>) getEntityTypeForDocumentTypeName(documentTypeName);
        return (T) convertMapToObject(clazz, (Map) item);
    }

    public <E extends DocumentBase> String getDocumentTypeNameForEntity(E entity) {
        return getDocumentTypeNameForEntityType(entity.getClass());
    }

    public <E extends DocumentBase> String getDocumentTypeNameForEntityType(Class<E> entityType) {
        return getEntityConverter().getMappingContext().getRequiredPersistentEntity(entityType).getDocumentType();
    }

    public Class<?> getEntityTypeForDocumentTypeName(String documentTypeName) {
        return ((ArcadeclientMappingContext) getEntityConverter().getMappingContext())
                .getPersistentEntityForDocumentType(documentTypeName)
                .getType();
    }

    /* TransactionalOperations */

    public Mono<EmptyResponse> commit() {
        return new CommitTAExchange(getDatabaseName(), getWebClient()).exchange();
    }

    public Mono<EmptyResponse> rollback() {
        return new RollbackTAExchange(getDatabaseName(), getWebClient()).exchange();
    }

    public void close() throws Exception {
        commit().onErrorResume(ex -> rollback());
    }
}
