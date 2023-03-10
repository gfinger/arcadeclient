package org.makkiato.arcadedb.client.http.request;

import java.util.Map;

import org.makkiato.arcadedb.client.http.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class CommandForObjectExchange<T extends Response> implements Exchange<T> {
    private final String name;
    private final Class<T> objectType;
    private final WebClient webClient;
    private final CommandPayload payload;

    public CommandForObjectExchange(String language, String command, String name, Map<String, Object> params, Class<T> objectType, WebClient webClient) {
        this.name = name;
        this.objectType = objectType;
        this.webClient = webClient;
        this.payload = new CommandPayload(language, command, null, SERIALIZER_JSON);
    }

    @Override
    public Mono<T> exchange() {
        return webClient.post()
                .uri(String.format("%s/%s", BASEURL_COMMAND, name))
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
