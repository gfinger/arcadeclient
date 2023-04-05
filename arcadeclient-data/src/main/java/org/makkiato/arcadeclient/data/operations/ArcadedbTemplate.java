package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.mapping.ArcadeclientEntityConverter;
import org.makkiato.arcadeclient.data.web.request.BeginTAExchange;
import org.makkiato.arcadeclient.data.web.response.EmptyResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class ArcadedbTemplate implements BasicOperations, ConversionAwareOperations {
    private final String databaseName;
    private final WebClient webClient;
    private final ArcadeclientEntityConverter entityConverter;

    public ArcadedbTemplate(String databaseName, WebClient webClient, ArcadeclientEntityConverter entityConverter) {
        this.databaseName = databaseName;
        this.webClient = webClient;
        this.entityConverter = entityConverter;
    }

    @Override
    public ArcadeclientEntityConverter getEntityConverter() {
        return entityConverter;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public WebClient getWebClient() {
        return webClient;
    }


    public TransactionalTemplate transactional() {
        return new BeginTAExchange(getDatabaseName(), getWebClient()).exchange()
                .map(EmptyResponse::headers)
                .filter(header -> header.containsKey(ARCADEDB_SESSION_ID))
                .map(header -> header.get(ARCADEDB_SESSION_ID))
                .filter(item -> !item.isEmpty())
                .map(item -> item.get(0))
                .map(id -> new TransactionalTemplate(getDatabaseName(),
                        getWebClient().mutate().defaultHeader(ARCADEDB_SESSION_ID, id).build(), entityConverter))
                .block(CONNECTION_TIMEOUT);
    }
}
