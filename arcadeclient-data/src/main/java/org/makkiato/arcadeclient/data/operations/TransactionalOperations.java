package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.web.response.EmptyResponse;
import reactor.core.publisher.Mono;

public interface TransactionalOperations extends BasicOperations, AutoCloseable {

    Mono<EmptyResponse> commit();

    Mono<EmptyResponse> rollback();

}
