package org.makkiato.arcadeclient.data.operations;

import org.makkiato.arcadeclient.data.mapping.ArcadeclientEntityConverter;
import org.springframework.web.reactive.function.client.WebClient;

public class ConversionAwareTemplate implements ConversionAwareOperations {
    private final String databaseName;
    private final WebClient webClient;
    private final ArcadeclientEntityConverter entityConverter;

    public ConversionAwareTemplate(String databaseName, WebClient webClient, ArcadeclientEntityConverter entityConverter) {
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
}
