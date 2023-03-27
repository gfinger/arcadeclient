package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.base.IdentifiableDocumentBase;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface ArcadedbOperations extends GenericArcadedbOperations {

    Mono<Map<String, Object>> insert(String documentName, Object object);

    <T extends IdentifiableDocumentBase> Mono<T> insertDocument(T object);

    Mono<Boolean> exists(String rid);

    <T> Mono<Map<String, Object>> update(String rid, T object);

    <T extends IdentifiableDocumentBase> Mono<T> updateDocument(T document);

    <T extends IdentifiableDocumentBase> Mono<T> mergeDocument(T document);

    <T> Mono<T> findById(String rid, Class<T> objectType);

    Mono<Long> count(String documentName);

    Mono<Void> deleteById(String id, String documentName);

    <T extends IdentifiableDocumentBase> Mono<Void> deleteDocument(T document);

    Mono<Void> deleteAllById(Iterable<? extends String> ids, String documentName);

    Mono<Void> deleteAll(String documentName);
}
