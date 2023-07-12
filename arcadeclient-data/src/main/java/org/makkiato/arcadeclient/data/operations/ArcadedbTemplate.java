package org.makkiato.arcadeclient.data.operations;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.makkiato.arcadeclient.data.base.DocumentBase;
import org.makkiato.arcadeclient.data.base.EdgeBase;
import org.makkiato.arcadeclient.data.base.IdentifiableDocumentBase;
import org.makkiato.arcadeclient.data.base.VertexBase;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientEntityConverter;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientMappingContext;
import org.makkiato.arcadeclient.data.web.request.BeginTAExchange;
import org.makkiato.arcadeclient.data.web.request.ExchangeFactory;
import org.makkiato.arcadeclient.data.web.response.CommandResponse;
import org.makkiato.arcadeclient.data.web.response.EmptyResponse;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ArcadedbTemplate implements ArcadedbOperations {
    private final String databaseName;
    private final WebClient webClient;
    private final ArcadeclientEntityConverter entityConverter;
    private final ExchangeFactory exchangeFactory;

    public ArcadedbTemplate(String databaseName, WebClient webClient, ArcadeclientEntityConverter entityConverter,
            ExchangeFactory exchangeFactory) {
        this.databaseName = databaseName;
        this.webClient = webClient;
        this.entityConverter = entityConverter;
        this.exchangeFactory = exchangeFactory;
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

    @Override
    public TransactionalOperations transactional() {
        return new BeginTAExchange(getDatabaseName(), getWebClient()).exchange()
                .map(EmptyResponse::headers)
                .filter(header -> header.containsKey(ARCADEDB_SESSION_ID))
                .map(header -> header.get(ARCADEDB_SESSION_ID))
                .filter(item -> !item.isEmpty())
                .map(item -> item.get(0))
                .map(id -> new TransactionalTemplate(getDatabaseName(),
                        getWebClient().mutate().defaultHeader(ARCADEDB_SESSION_ID, id).build(), entityConverter,
                        exchangeFactory))
                .block(CONNECTION_TIMEOUT);
    }

    /* GenericOperations */

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
    public Mono<Boolean> script(CommandLanguage language, Resource resource, Map<String, Object> params)
            throws IOException {
        var command = resource.getContentAsString(Charset.defaultCharset());
        return exchangeFactory.createCommandExchange(language, command, getDatabaseName(), params, getWebClient())
                .exchange()
                .hasElement();
    }

    @Override
    public Mono<Boolean> script(String[] commands) {
        return script(commands, null);
    }

    @Override
    public Mono<Boolean> script(String[] commands, Map<String, Object> params) {
        var command = Arrays.stream(commands).map(c -> String.format("%s", c)).collect(Collectors.joining(";"));
        return exchangeFactory
                .createCommandExchange(CommandLanguage.SQLSCRIPT, command, getDatabaseName(), params, getWebClient())
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
        return exchangeFactory.createCommandExchange(language, command, getDatabaseName(), params, getWebClient())
                .exchange()
                .map(CommandResponse::result)
                .flatMapMany(Flux::fromArray);
    }

    @Override
    public Flux<Map<String, Object>> query(String query) {
        return exchangeFactory.createQueryExchange(CommandLanguage.SQL, query, getDatabaseName(), getWebClient())
                .exchange()
                .map(CommandResponse::result)
                .flatMapMany(Flux::fromArray);
    }

    /* BasicOperations */

    @Override
    public Mono<Map<String, Object>> insert(String documentTypeName, String jsonObject) {
        return command(String.format("insert into %s content %s", documentTypeName, jsonObject))
                .elementAt(0);
    }

    @Override
    public Mono<Long> count(String documentTypeName) {
        Assert.notNull(documentTypeName, "Document name must not be empty");
        return command(String.format("select count() as c from %s", documentTypeName))
                .elementAt(0)
                .map(result -> result.get("c"))
                .cast(Long.class);
    }

    @Override
    public Mono<Void> deleteById(String id, String documentTypeName) {
        Assert.notNull(id, "RID of document must not be empty");
        Assert.notNull(documentTypeName, "Document name must not be empty");
        return command(String.format("delete from %s where @rid in [%s] ", documentTypeName, id))
                .elementAt(0).flatMap(r -> Mono.empty());
    }

    @Override
    public Mono<Void> deleteAllById(Iterable<? extends String> ids, String documentTypeName) {
        Assert.notNull(ids, "RID of document must not be empty");
        Assert.notNull(documentTypeName, "Document name must not be empty");
        return command(String.format("delete from %s where @rid in [%s]", documentTypeName,
                StreamSupport.stream(ids.spliterator(), false).collect(Collectors.joining())))
                .elementAt(0).flatMap(r -> Mono.empty());
    }

    @Override
    public Mono<Void> deleteAll(String documentTypeName) {
        Assert.notNull(documentTypeName, "Document name must not be empty");
        return command(String.format("delete from %s", documentTypeName))
                .elementAt(0).map(result -> result.get("count")).log().then(Mono.empty());
    }

    @Override
    public Mono<Boolean> exists(String rid) {
        return command(String.format("select from [%s]", rid)).hasElements();
    }

    /* ConversionAwareOperations */

    @Override
    public <T extends IdentifiableDocumentBase> Mono<T> insertDocument(T entity) {
        var documentTypeName = getEntityConverter().getMappingContext().getRequiredPersistentEntity(entity.getClass())
                .getDocumentType();
        return command(String.format("insert into %s content %s", documentTypeName, convertObjectToJsonString(entity)))
                .elementAt(0)
                .map(item -> (T) convertMapToObject(entity.getClass(), item));
    }

    @Override
    public <T extends IdentifiableDocumentBase> Mono<Map<String, Object>> update(String rid, T entity) {
        return command(String.format("update %s content %s return after", rid, convertObjectToJsonString(entity)))
                .elementAt(0);
    }

    @Override
    public <T extends IdentifiableDocumentBase> Mono<T> updateDocument(T entity) {
        Assert.notNull(entity, "Document must not be empty");
        Assert.notNull(entity.getRid(), "RID of document must not be empty");
        return command(String.format("update %s content %s return after", entity.getRid(),
                convertObjectToJsonString(entity)))
                .elementAt(0)
                .map(result -> (T) convertMapToObject(entity.getClass(), result));
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

    @Override
    public <T extends IdentifiableDocumentBase> Mono<T> mergeDocument(T entity) {
        Assert.notNull(entity, "Document must not be empty");
        if (entity.getRid() != null) {
            return command(String.format("update %s merge %s upsert return after", entity.getRid(),
                    convertObjectToJsonString(entity)))
                    .elementAt(0)
                    .map(result -> (T) convertMapToObject(entity.getClass(), result));
        } else {
            return insertDocument(entity);
        }
    }

    @Override
    public <T extends IdentifiableDocumentBase> Flux<T> select(String command, Class<T> entityType) {
        return select(CommandLanguage.SQL, command, null, entityType, this::convertMapToObject);
    }

    @Override
    public <T extends IdentifiableDocumentBase> Flux<T> select(String command, Map<String, Object> params,
            Class<T> entityType) {
        return select(CommandLanguage.SQL, command, params, entityType, this::convertMapToObject);
    }

    @Override
    public <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command,
            Class<T> entityType) {
        return select(language, command, null, entityType, this::convertMapToObject);
    }

    @Override
    public <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command,
            Map<String, Object> params, Class<T> entityType) {
        return select(language, command, params, entityType, this::convertMapToObject);
    }

    @Override
    public <T> Flux<T> select(CommandLanguage language, String command, Map<String, Object> params, Class<T> entityType,
            BiFunction<Class<T>, Map<String, Object>, T> mapper) {
        return exchangeFactory.createCommandExchange(language, command, getDatabaseName(), params, getWebClient())
                .exchange()
                .map(CommandResponse::result)
                .map(resultArray -> Arrays.stream(resultArray)
                        .map(result -> mapper.apply(entityType, result))
                        .collect(Collectors.toList()))
                .flatMapMany(Flux::fromIterable);

    }

    @Override
    public <T extends IdentifiableDocumentBase> Mono<Void> deleteDocument(T entity) {
        Assert.notNull(entity, "Document must not be empty");
        Assert.notNull(entity.getRid(), "RID of document must not be empty");
        return command(String.format("delete from %s where @rid in [%s] ", getDocumentTypeNameForEntity(entity),
                entity.getRid()))
                .elementAt(0).flatMap(r -> Mono.empty());
    }

    @Override
    public <T extends IdentifiableDocumentBase> Mono<T> findById(String rid, Class<T> entityType) {
        return select(String.format("select from [%s]", rid), entityType).elementAt(0);
    }

    @Override
    public <T extends IdentifiableDocumentBase> Flux<T> findAll(Class<T> entityType) {
        return select(String.format("select from %s", getDocumentTypeNameForEntityType(entityType)), entityType);
    }

    @Override
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

    @Override
    public <F extends VertexBase> Flux<VertexBase> outVertices(F from) {
        return convertFluxOfMapToFluxOfVertex(
                command(String.format("select out() as item from %s",
                        getDocumentTypeNameForEntityType(from.getClass()))));
    }

    @Override
    public Flux<String> outVertexIds(String fromDocumentTypeName) {
        return convertFluxOfMapToFluxOfRid(
                command(String.format("select out().@rid as item from %s unwind item", fromDocumentTypeName)));
    }

    @Override
    public <F extends VertexBase, E extends EdgeBase> Flux<VertexBase> outVertices(F from, Class<E> edgeType) {
        var edgeDocumentName = getDocumentTypeNameForEntityType(edgeType);
        return convertFluxOfMapToFluxOfVertex(
                command(String.format("select out(%s) as item from %s", edgeDocumentName,
                        getDocumentTypeNameForEntityType(from.getClass()))));
    }

    @Override
    public Flux<String> outVertexIds(String fromDocumentTypeName, String edgeDocumentName) {
        return convertFluxOfMapToFluxOfRid(
                command(String.format("select out(%s).@rid as item from %s unwind item", edgeDocumentName, fromDocumentTypeName)));
    }

    @Override
    public <F extends VertexBase> Flux<EdgeBase> outEdges(F from) {
        return convertFluxOfMapToFluxOfEdge(
                command(String.format("select outE() as item from %s",
                        getDocumentTypeNameForEntityType(from.getClass()))));
    }

    @Override
    public Flux<String> outEdgesIds(String fromDocumentTypeName) {
        return convertFluxOfMapToFluxOfRid(
                command(String.format("select outE().@rid as item from %s unwind item",
                        fromDocumentTypeName)));
    }

    @Override
    public <F extends VertexBase, E extends EdgeBase> Flux<EdgeBase> outEdges(F from, Class<E> edgeType) {
        return convertFluxOfMapToFluxOfEdge(
                command(String.format("select outE() as item from %s",
                        getDocumentTypeNameForEntityType(from.getClass()))));
    }

    @Override
    public Flux<String> outEdgesIds(String fromDocumentTypeName, String edgeDocumentName) {
        return convertFluxOfMapToFluxOfRid(
                command(String.format("select outE(%s).@rid as item from %s unwind item",
                        edgeDocumentName, fromDocumentTypeName)));
    }

    @Override
    public <T extends VertexBase> Flux<VertexBase> inVertices(T to) {
        return null;
    }

    @Override
    public <T extends VertexBase, E extends EdgeBase> Flux<VertexBase> inVertices(T to, Class<E> edgeType) {
        return null;
    }

    @Override
    public <T extends VertexBase> Flux<EdgeBase> inEdges(T to) {
        return null;
    }

    @Override
    public <T extends VertexBase, E extends EdgeBase> Flux<EdgeBase> inEdges(T to, Class<E> edgeType) {
        return null;
    }

    @Override
    public <V extends VertexBase> Flux<VertexBase> bothVertices(V vertex) {
        return null;
    }

    @Override
    public <V extends VertexBase, E extends EdgeBase> Flux<VertexBase> bothVertices(V vertex, Class<E> edgeType) {
        return null;
    }

    @Override
    public <V extends VertexBase> Flux<EdgeBase> bothEdges(V vertex) {
        return null;
    }

    @Override
    public <V extends VertexBase, E extends EdgeBase> Flux<EdgeBase> bothEdges(V vertex, Class<E> edgeType) {
        return null;
    }

    @Override
    public String convertObjectToJsonString(Object object) {
        var buffer = new StringBuffer();
        getEntityConverter().write(object, buffer);
        return buffer.toString();
    }

    @Override
    public <T> T convertMapToObject(Class<T> entityType, Map<String, Object> item) {
        return getEntityConverter().read(entityType, item);
    }

    @Override
    public <T extends DocumentBase> T convertMapToObject(Map<String, Object> item) {
        var documentTypeName = (String) (item).get("@type");
        var clazz = getEntityTypeForDocumentTypeName(documentTypeName);
        return (T) convertMapToObject(clazz, item);
    }

    @Override
    public <E extends DocumentBase> String getDocumentTypeNameForEntity(E entity) {
        return getDocumentTypeNameForEntityType(entity.getClass());
    }

    @Override
    public <E extends DocumentBase> String getDocumentTypeNameForEntityType(Class<E> entityType) {
        return getEntityConverter().getMappingContext().getRequiredPersistentEntity(entityType).getDocumentType();
    }

    @Override
    public Class<?> getEntityTypeForDocumentTypeName(String documentTypeName) {
        return ((ArcadeclientMappingContext) getEntityConverter().getMappingContext())
                .getPersistentEntityForDocumentType(documentTypeName)
                .getType();
    }

    private <F extends VertexBase, E extends EdgeBase> Flux<VertexBase> convertFluxOfMapToFluxOfVertex(
            Flux<Map<String, Object>> flux) {
        return flux.flatMap(result -> {
            var out = result.get("item");
            if (out instanceof Iterable) {
                return Flux.fromIterable((Iterable<?>) out);
            }
            return Flux.empty();
        })
                .map(item -> {
                    if (item instanceof Map<?,?>) {
                        var convertedItem = convertMapToObject((Map<String, Object>) item);
                        if (convertedItem instanceof VertexBase) {
                            return (VertexBase) convertedItem;
                        }
                        return null;
                    } else {
                        return null;
                    }
                });
    }

    private <F extends VertexBase, E extends EdgeBase> Flux<EdgeBase> convertFluxOfMapToFluxOfEdge(
        Flux<Map<String, Object>> flux) {
    return flux.flatMap(result -> {
        var out = result.get("item");
        if (out instanceof Iterable) {
            return Flux.fromIterable((Iterable<?>) out);
        }
        return Flux.empty();
    })
            .map(item -> {
                if (item instanceof Map) {
                    var convertedItem = convertMapToObject((Map<String, Object>) item);
                    if (convertedItem instanceof EdgeBase) {
                        return (EdgeBase) convertedItem;
                    }
                    return null;
                } else {
                    return null;
                }
            });
}

    private Flux<String> convertFluxOfMapToFluxOfRid(Flux<Map<String, Object>> flux) {
        return flux.map(item -> {
            if (item instanceof Map<?,?>) {
                return (String) item.get("item");
            }
            return null;
        });
    }
}
