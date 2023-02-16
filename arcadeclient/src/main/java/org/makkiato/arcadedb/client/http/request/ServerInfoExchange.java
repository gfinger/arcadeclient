package org.makkiato.arcadedb.client.http.request;

import org.makkiato.arcadedb.client.http.response.ServerInfoResponse;
import org.makkiato.arcadedb.client.http.response.StatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class ServerInfoExchange implements Exchange<ServerInfoResponse> {

    private final String mode;
    private final WebClient webClient;

    public ServerInfoExchange(String mode, WebClient webClient) {
        this.mode = mode;
        this.webClient = webClient;
    }

    @Override
    public Mono<ServerInfoResponse> exchange() {
        return webClient.get()
                .uri(BASEURL_SERVER, Exchange.PARAMETER_MODE, mode)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(ServerInfoResponse.class);
                    } else {
                        return response.createError();
                    }
                });
    }
}
