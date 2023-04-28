package org.makkiato.arcadeclient.data.operations;

import java.time.Duration;
import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BasicOperations extends GenericOperations {

    static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    static final String ARCADEDB_SESSION_ID = "arcadedb-session-id";

    Mono<Map<String, Object>> insert(String documentTypeName, String jsonObject);

    Mono<Long> count(String documentTypeName);

    Mono<Void> deleteById(String id, String documentTypeName);

    Mono<Void> deleteAllById(Iterable<? extends String> ids, String documentTypeName);

    Mono<Void> deleteAll(String documentTypeName);

    Mono<Boolean> exists(String rid);

    Flux<String> outVertexIds(String fromDocumentTypeName);
    
    Flux<String> outVertexIds(String fromDocumentTypeName, String edgeDocumentName);

    Flux<String> outEdgesIds(String fromDocumentTypeName);

    Flux<String> outEdgesIds(String fromDocumentTypeName, String edgeDocumentName);
}
