package org.makkiato.arcadedb.client.http.request;

import org.makkiato.arcadedb.client.http.response.StatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class ServerExchange implements Exchange<StatusResponse> {

    private final String language;
    private final String command;
    private final WebClient webClient;

    public ServerExchange(String language, String command, WebClient webClient) {
        this.language = language;
        this.command = command;
        this.webClient = webClient;
    }

    @Override
    public Mono<StatusResponse> exchange() {
        var payload = new CommandPayload(language, command, null, SERIALIZER_JSON);
        return webClient.post()
                .uri(BASEURL_SERVER)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(StatusResponse.class);
                    } else {
                        return response.createError();
                    }
                });
    }
}
