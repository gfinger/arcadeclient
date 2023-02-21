package org.makkiato.arcadedb.client.http.request;

import org.makkiato.arcadedb.client.http.response.EmptyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class RollbackTAExchange implements Exchange<EmptyResponse> {
    private final String name;
    private final WebClient webClient;

    public RollbackTAExchange(String name, WebClient webClient) {
        this.name = name;
        this.webClient = webClient;
    }

    @Override
    public Mono<EmptyResponse> exchange() {
        return webClient.post()
                .uri(String.format("%s/%s", Exchange.BASEURL_ROLLBACK, name))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        return Mono.just(new EmptyResponse(null));
                    } else {
                        return response.createError();
                    }
                });
    }
}
