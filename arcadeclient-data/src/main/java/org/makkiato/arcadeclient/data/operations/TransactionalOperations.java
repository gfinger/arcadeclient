package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.web.request.CommitTAExchange;
import org.makkiato.arcadeclient.data.web.request.RollbackTAExchange;
import org.makkiato.arcadeclient.data.web.response.EmptyResponse;
import reactor.core.publisher.Mono;

public interface TransactionalOperations extends GenericOperations {

    default Mono<EmptyResponse> commit() {
        return new CommitTAExchange(getDatabaseName(), getWebClient()).exchange();
    }

    default Mono<EmptyResponse> rollback() {
        return new RollbackTAExchange(getDatabaseName(), getWebClient()).exchange();
    }

}
