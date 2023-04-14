package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.base.DocumentBase;
import org.makkiato.arcadeclient.data.base.EdgeBase;
import org.makkiato.arcadeclient.data.base.IdentifiableDocumentBase;
import org.makkiato.arcadeclient.data.base.VertexBase;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientEntityConverter;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.BiFunction;

public interface ConversionAwareOperations extends GenericOperations {

    ArcadeclientEntityConverter getEntityConverter();

    <T extends IdentifiableDocumentBase> Mono<T> insertDocument(T entity);

    default <T extends IdentifiableDocumentBase> Mono<Map<String, Object>> update(String rid, T entity) {
        return command(String.format("update %s content %s return after", rid, convertObjectToJsonString(entity)))
                .elementAt(0);
    }

    <T extends IdentifiableDocumentBase> Mono<T> updateDocument(T entity);

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
    <T extends IdentifiableDocumentBase> Mono<T> mergeDocument(T entity);

    <T extends IdentifiableDocumentBase> Flux<T> select(String command, Class<T> entityType);

    <T extends IdentifiableDocumentBase> Flux<T> select(String command, Map<String, Object> params,
                                                        Class<T> entityType);

    <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command,
                                                        Class<T> entityType);

    <T extends IdentifiableDocumentBase> Flux<T> select(CommandLanguage language, String command, Map<String,
            Object> params, Class<T> entityType);

    <T> Flux<T> select(CommandLanguage language, String command, Map<String,
            Object> params, Class<T> entityType, BiFunction<Class<T>, Map<String, Object>, T> mapper);

    default <T extends IdentifiableDocumentBase> Mono<Void> deleteDocument(T entity) {
        Assert.notNull(entity, "Document must not be empty");
        Assert.notNull(entity.getRid(), "RID of document must not be empty");
        return command(String.format("delete from %s where @rid in [%s] ", getDocumentTypeNameForEntity(entity),
                entity.getRid()))
                .elementAt(0).flatMap(r -> Mono.empty());
    }

    <T extends IdentifiableDocumentBase> Mono<T> findById(String rid, Class<T> entityType);

    <T extends IdentifiableDocumentBase> Flux<T> findAll(Class<T> entityType);

    <E extends EdgeBase, F extends VertexBase, T extends VertexBase> Mono<E> createEdge(F from, T to,
                                                                                        Class<E> edge);

    <F extends VertexBase> Flux<VertexBase> outVertices(F from);

    <F extends VertexBase, E extends EdgeBase> Flux<VertexBase> outVertices(F from, Class<E> edgeType);

    <F extends VertexBase> Flux<EdgeBase> outEdges(F from);

    <F extends VertexBase, E extends EdgeBase> Flux<E> outEdges(F from, Class<E> edgeType);

    <T extends VertexBase> Flux<VertexBase> inVertices(T to);

    <T extends VertexBase, E extends EdgeBase> Flux<VertexBase> inVertices(T to, Class<E> edgeType);

    <T extends VertexBase> Flux<EdgeBase> inEdges(T to);

    <T extends VertexBase, E extends EdgeBase> Flux<E> inEdges(T to, Class<E> edgeType);

    <V extends VertexBase> Flux<VertexBase> bothVertices(V vertex);

    <V extends VertexBase, E extends EdgeBase> Flux<VertexBase> bothVertices(V vertex, Class<E> edgeType);

    <V extends VertexBase> Flux<EdgeBase> bothEdges(V vertex);

    <V extends VertexBase, E extends EdgeBase> Flux<E> bothEdges(V vertex, Class<E> edgeType);


    String convertObjectToJsonString(Object object);

    <T> T convertMapToObject(Class<T> entityType, Map<String, Object> item);

    <T extends DocumentBase> T convertMapToObject(Map<String, Object> item);

    <E extends DocumentBase> String getDocumentTypeNameForEntity(E entity);

    <E extends DocumentBase> String getDocumentTypeNameForEntityType(Class<E> entityType);

    Class<?> getEntityTypeForDocumentTypeName(String documentTypeName);
}
