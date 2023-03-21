package org.makkiato.arcadeclient.data.web.request;

import org.makkiato.arcadeclient.data.core.CommandLanguage;
import org.makkiato.arcadeclient.data.web.response.CommandResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

public class CommandExchange implements Exchange<CommandResponse> {
    private final String name;
    private final WebClient webClient;
    private final CommandPayload payload;

    public CommandExchange(CommandLanguage language, String command, String name, Map<String, Object> params, WebClient webClient) {
        this.name = name;
        this.webClient = webClient;
        this.payload = new CommandPayload(language.key, command, params, SERIALIZER_JSON);
    }

    @Override
    public Mono<CommandResponse> exchange() {
        return webClient.post()
                .uri(String.format("%s/%s", Exchange.BASEURL_COMMAND, name))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(CommandResponse.class);
                    } else {
                        return response.createError();
                    }
                });
    }
}
