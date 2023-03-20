package org.makkiato.arcadedb.client;

import org.makkiato.arcadedb.client.web.request.CommitTAExchange;
import org.makkiato.arcadedb.client.web.request.RollbackTAExchange;
import org.makkiato.arcadedb.client.web.response.EmptyResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class TransactionalConnection extends ArcadedbConnection implements AutoCloseable {

    protected TransactionalConnection(String databaseName, WebClient webClient) {
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
