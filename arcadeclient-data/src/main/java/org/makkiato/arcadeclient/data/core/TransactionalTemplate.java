package org.makkiato.arcadeclient.data.core;

import org.makkiato.arcadeclient.data.web.request.CommitTAExchange;
import org.makkiato.arcadeclient.data.web.request.RollbackTAExchange;
import org.makkiato.arcadeclient.data.web.response.EmptyResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class TransactionalTemplate extends ArcadedbTemplate implements AutoCloseable {

    protected TransactionalTemplate(String databaseName, WebClient webClient) {
        super(databaseName, webClient);
    }

    @Override
    public void close() throws Exception {
        commit().onErrorResume(ex -> rollback());
    }

    private Mono<EmptyResponse> commit() {
        return new CommitTAExchange(databaseName, webClient).exchange();
    }

    private Mono<EmptyResponse> rollback() {
        return new RollbackTAExchange(databaseName, webClient).exchange();
    }

}
