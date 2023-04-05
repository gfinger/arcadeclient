package org.makkiato.arcadeclient.data.operations;

import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface BasicOperations extends GenericOperations{

    static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    static final String ARCADEDB_SESSION_ID = "arcadedb-session-id";

    default Mono<Map<String, Object>> insert(String documentTypeName, String jsonObject) {
        return command(String.format("insert into %s content %s", documentTypeName, jsonObject))
                .elementAt(0);
    }

    default Mono<Long> count(String documentTypeName) {
        Assert.notNull(documentTypeName, "Document name must not be empty");
        return command(String.format("select count() as c from %s", documentTypeName))
                .elementAt(0)
                .map(result -> result.get("c"))
                .cast(Long.class);
    }

    default Mono<Void> deleteById(String id, String documentTypeName) {
        Assert.notNull(id, "RID of document must not be empty");
        Assert.notNull(documentTypeName, "Document name must not be empty");
        return command(String.format("delete from %s where @rid in [%s] ", documentTypeName, id))
                .elementAt(0).flatMap(r -> Mono.empty());
    }

    default Mono<Void> deleteAllById(Iterable<? extends String> ids, String documentTypeName) {
        Assert.notNull(ids, "RID of document must not be empty");
        Assert.notNull(documentTypeName, "Document name must not be empty");
        return command(String.format("delete from %s where @rid in [%s]", documentTypeName,
                StreamSupport.stream(ids.spliterator(), false).collect(Collectors.joining())))
                .elementAt(0).flatMap(r -> Mono.empty());
    }

    default
    public Mono<Void> deleteAll(String documentTypeName) {
        Assert.notNull(documentTypeName, "Document name must not be empty");
        return command(String.format("delete from %s", documentTypeName))
                .elementAt(0).map(result -> result.get("count")).log().then(Mono.empty());
    }

    default Mono<Boolean> exists(String rid) {
        return command(String.format("select from [%s]", rid)).hasElements();
    }
}
