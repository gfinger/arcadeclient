package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.mapping.ArcadeclientEntityConverter;
import org.springframework.web.reactive.function.client.WebClient;

public class TransactionalTemplate extends ArcadedbTemplate implements TransactionalOperations, AutoCloseable{
    public TransactionalTemplate(String databaseName, WebClient webClient, ArcadeclientEntityConverter entityConverter) {
        super(databaseName, webClient, entityConverter);
    }

    @Override
    public void close() throws Exception {
        commit().onErrorResume(ex -> rollback());
    }
}
