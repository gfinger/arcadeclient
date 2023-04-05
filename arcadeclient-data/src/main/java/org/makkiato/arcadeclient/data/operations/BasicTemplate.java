package org.makkiato.arcadeclient.data.operations;

import org.springframework.web.reactive.function.client.WebClient;

public class BasicTemplate implements BasicOperations {
    private final String databaseName;
    private final WebClient webClient;

    public BasicTemplate(String databaseName, WebClient webClient) {
        this.databaseName = databaseName;
        this.webClient = webClient;
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
