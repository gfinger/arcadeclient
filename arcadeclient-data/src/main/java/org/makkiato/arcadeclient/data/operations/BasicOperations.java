package org.makkiato.arcadeclient.data.operations;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

public interface BasicOperations extends GenericOperations {

    static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    static final String ARCADEDB_SESSION_ID = "arcadedb-session-id";

    Mono<Map<String, Object>> insert(String documentTypeName, String jsonObject);

    Mono<Long> count(String documentTypeName);

    Mono<Void> deleteById(String id, String documentTypeName);

    Mono<Void> deleteAllById(Iterable<? extends String> ids, String documentTypeName);

    Mono<Void> deleteAll(String documentTypeName);

    Mono<Boolean> exists(String rid);
}
