package org.makkiato.arcadeclient.data.web.request;

import java.util.Map;

import org.makkiato.arcadeclient.data.operations.GenericOperations;
import org.springframework.web.reactive.function.client.WebClient;

public class ExchangeFactory {
    public CommandExchange createCommandExchange(GenericOperations.CommandLanguage language, String command, String name, Map<String, Object> params, WebClient webClient) {
        return new CommandExchange(language, command, name, params, webClient);
    }
}
