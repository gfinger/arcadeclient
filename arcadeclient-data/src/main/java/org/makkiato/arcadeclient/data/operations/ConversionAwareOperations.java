package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.base.DocumentBase;
import org.makkiato.arcadeclient.data.base.EdgeBase;
import org.makkiato.arcadeclient.data.base.IdentifiableDocumentBase;
import org.makkiato.arcadeclient.data.base.VertexBase;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientEntityConverter;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientMappingContext;
import org.makkiato.arcadeclient.data.web.request.CommandExchange;
import org.makkiato.arcadeclient.data.web.response.CommandResponse;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public interface ConversionAwareOperations extends GenericOperations {

    ArcadeclientEntityConverter getEntityConverter();

    default <T extends IdentifiableDocumentBase> Mono<T> insertDocument(T entity) {
        var documentTypeName =
                getEntityConverter().getMappingContext().getRequiredPersistentEntity(entity.getClass()).getDocumentType();
        return command(String.format("insert into %s content %s", documentTypeName, convertObjectToJsonString(entity)))
                .elementAt(0)
                .map(item -> convertMapToObject((Class<T>) entity.getClass(), item));
    }

    default <T extends IdentifiableDocumentBase> Mono<Map<String, Object>> update(String rid, T entity) {
        return command(String.format("update %s content %s return after", rid, convertObjectToJsonString(entity)))
                .elementAt(0);
    }

    default <T extends IdentifiableDocumentBase> Mono<T> updateDocument(T entity) {
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
    default <T extends IdentifiableDocumentBase> Mono<T> mergeDocument(T entity) {
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

    default <T extends IdentifiableDocumentBase> Flux<T> select(String command, Class<T> entityType) {
        return select(CommandLanguage.SQL, command, null, entityType, this::convertMapToObject);
    }

    default <T extends IdentifiableDocumentBase> Flux<T> select(String command, Map<String, Object> params,
                                                                Class<T> entityType) {
        return select(CommandLanguage.SQL, command, params, entityType, this::convertMapToObject);
    }

    default <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command,
                                                                Class<T> entityType) {
        return select(language, command, null, entityType, this::convertMapToObject);
    }

    default <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command, Map<String,
            Object> params, Class<T> entityType) {
        return select(language, command, params, entityType, this::convertMapToObject);
    }

    default <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command, Map<String,
            Object> params, Class<T> entityType, BiFunction<Class<T>, Map<String, Object>, T> mapper) {
        return new CommandExchange(language, command, getDatabaseName(), params, getWebClient())
                .exchange()
                .map(CommandResponse::result)
                .map(resultArray -> Arrays.stream(resultArray)
                        .map(result -> mapper.apply(entityType, result))
                        .collect(Collectors.toList()))
                .flatMapMany(Flux::fromIterable);

    }

    default <T extends IdentifiableDocumentBase> Mono<Void> deleteDocument(T entity) {
        Assert.notNull(entity, "Document must not be empty");
        Assert.notNull(entity.getRid(), "RID of document must not be empty");
        return command(String.format("delete from %s where @rid in [%s] ", getDocumentTypeNameForEntity(entity),
                entity.getRid()))
                .elementAt(0).flatMap(r -> Mono.empty());
    }

    default <T extends IdentifiableDocumentBase> Mono<T> findById(String rid, Class<T> entityType) {
        return select(String.format("select from [%s]", rid), entityType).elementAt(0);
    }

    default <T extends IdentifiableDocumentBase> Flux<T> findAll(Class<T> entityType) {
        return select(String.format("select from %s", getDocumentTypeNameForEntityType(entityType)), entityType);
    }

    default <E extends EdgeBase, F extends VertexBase, T extends VertexBase> Mono<E> createEdge(F from, T to,
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

    default <F extends VertexBase> Flux<VertexBase> outVertices(F from) {
        return command(String.format("select out() from %s", getDocumentTypeNameForEntityType(from.getClass())))
                .flatMap(result -> Flux.fromIterable((Iterable) result.get("out()")))
                .map(item -> convertMapToObject((Map) item));
    }

    default <F extends VertexBase, E extends EdgeBase> Flux<VertexBase> outVertices(F from, Class<E> edgeType) {
        var edgeDocumentName = getDocumentTypeNameForEntityType(edgeType);
        return command(String.format("select out(%s) from %s", edgeDocumentName,
                getDocumentTypeNameForEntityType(from.getClass())))
                .flatMap(result -> Flux.fromIterable((Iterable) result.get(String.format("out(%s)", edgeDocumentName))))
                .map(item -> convertMapToObject((Map) item));
    }

    default <F extends VertexBase> Flux<EdgeBase> outEdges(F from) {
        return null;
    }

    default <F extends VertexBase, E extends EdgeBase> Flux<E> outEdges(F from, Class<E> edgeType) {
        return null;
    }

    default <T extends VertexBase> Flux<VertexBase> inVertices(T to) {
        return null;
    }

    default <T extends VertexBase, E extends EdgeBase> Flux<VertexBase> inVertices(T to, Class<E> edgeType) {
        return null;
    }

    default <T extends VertexBase> Flux<EdgeBase> inEdges(T to) {
        return null;
    }

    default <T extends VertexBase, E extends EdgeBase> Flux<E> inEdges(T to, Class<E> edgeType) {
        return null;
    }

    default <V extends VertexBase> Flux<VertexBase> bothVertices(V vertex) {
        return null;
    }

    default <V extends VertexBase, E extends EdgeBase> Flux<VertexBase> bothVertices(V vertex, Class<E> edgeType) {
        return null;
    }

    default <V extends VertexBase> Flux<EdgeBase> bothEdges(V vertex) {
        return null;
    }

    default <V extends VertexBase, E extends EdgeBase> Flux<E> bothEdges(V vertex, Class<E> edgeType) {
        return null;
    }


    default String convertObjectToJsonString(Object object) {
        var buffer = new StringBuffer();
        getEntityConverter().write(object, buffer);
        return buffer.toString();
    }

    default <T extends DocumentBase> T convertMapToObject(Class<T> entityType, Map<String, Object> item) {
        return getEntityConverter().read(entityType, item);
    }

    default <T extends DocumentBase> T convertMapToObject(Map<String, Object> item) {
        var documentTypeName = (String) ((Map) item).get("@type");
        var clazz = (Class<T>) getEntityTypeForDocumentTypeName(documentTypeName);
        return (T) convertMapToObject(clazz, (Map) item);
    }

    default <E extends DocumentBase> String getDocumentTypeNameForEntity(E entity) {
        return getDocumentTypeNameForEntityType(entity.getClass());
    }

    default <E extends DocumentBase> String getDocumentTypeNameForEntityType(Class<E> entityType) {
        return getEntityConverter().getMappingContext().getRequiredPersistentEntity(entityType).getDocumentType();
    }

    default Class<?> getEntityTypeForDocumentTypeName(String documentTypeName) {
        return ((ArcadeclientMappingContext) getEntityConverter().getMappingContext())
                .getPersistentEntityForDocumentType(documentTypeName)
                .getType();
    }
}
