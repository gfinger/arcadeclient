package org.makkiato.arcadeclient.data.web.request;

import java.util.Map;

import org.makkiato.arcadeclient.data.operations.GenericOperations.CommandLanguage;
import org.springframework.web.reactive.function.client.WebClient;

public class ExchangeFactory {
    public CommandExchange createCommandExchange(CommandLanguage language, String command, String name, Map<String, Object> params, WebClient webClient) {
        return new CommandExchange(language, command, name, params, webClient);
    }

    public QueryExchange createQueryExchange(CommandLanguage language, String query, String databaseName,
            WebClient webClient) {
        return new QueryExchange(language, query, databaseName, webClient);
    }
}
