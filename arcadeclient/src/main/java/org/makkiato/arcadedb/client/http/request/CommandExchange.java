package org.makkiato.arcadedb.client.http.request;

import org.makkiato.arcadedb.client.http.response.CommandResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class CommandExchange implements Exchange<CommandResponse> {
    private final String language;
    private final String command;
    private final String name;
    private final WebClient webClient;

    public CommandExchange(String language, String command, String name, WebClient webClient) {
        this.language = language;
        this.command = command;
        this.name = name;
        this.webClient = webClient;
    }

    @Override
    public Mono<CommandResponse> exchange() {
        var payload = new CommandPayload(language, command, SERIALIZER_JSON);
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
