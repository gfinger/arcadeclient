package org.makkiato.arcadedb.client.web.request;

import org.makkiato.arcadedb.client.web.response.EmptyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class CommitTAExchange implements Exchange<EmptyResponse> {
    private final String name;
    private final WebClient webClient;

    public CommitTAExchange(String name, WebClient webClient) {
        this.name = name;
        this.webClient = webClient;
    }

    @Override
    public Mono<EmptyResponse> exchange() {
        return webClient.post()
                .uri(String.format("%s/%s", Exchange.BASEURL_BEGIN, name))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        return Mono.just(new EmptyResponse(null));
                    } else {
                        return response.createError();
                    }
                });
    }
}
