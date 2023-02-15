package org.makkiato.arcadedb.client.httpexchange;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class CommandForObjectExchange<T extends Response> implements Exchange<T> {
    private final String language;
    private final String command;
    private final String name;
    private final Class<T> objectType;
    private final WebClient webClient;

    public CommandForObjectExchange(String language, String command, String name, Class<T> objectType, WebClient webClient) {
        this.language = language;
        this.command = command;
        this.name = name;
        this.objectType = objectType;
        this.webClient = webClient;
    }

    @Override
    public Mono<T> exchange() {
        var payload = new CommandPayload(language, command, "json");
        return webClient.post()
                .uri(String.format("/%s/%s", "command", name))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(objectType);
                    } else {
                        return response.createError();
                    }
                });
    }
}
