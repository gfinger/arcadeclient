package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.mapping.ArcadeclientEntityConverter;
import org.makkiato.arcadeclient.data.web.request.CommitTAExchange;
import org.makkiato.arcadeclient.data.web.request.ExchangeFactory;
import org.makkiato.arcadeclient.data.web.request.RollbackTAExchange;
import org.makkiato.arcadeclient.data.web.response.EmptyResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class TransactionalTemplate extends ArcadedbTemplate implements TransactionalOperations {
        public TransactionalTemplate(String databaseName, WebClient webClient, ArcadeclientEntityConverter entityConverter,
            ExchangeFactory exchangeFactory) {
        super(databaseName, webClient, entityConverter, exchangeFactory);
    }

        public Mono<EmptyResponse> commit() {
            return new CommitTAExchange(getDatabaseName(), getWebClient()).exchange();
        }
    
        public Mono<EmptyResponse> rollback() {
            return new RollbackTAExchange(getDatabaseName(), getWebClient()).exchange();
        }
    
        public void close() throws Exception {
            commit().onErrorResume(ex -> rollback());
        }
}
