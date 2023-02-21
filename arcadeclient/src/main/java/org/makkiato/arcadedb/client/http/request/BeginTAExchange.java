package org.makkiato.arcadedb.client.http.request;

import org.makkiato.arcadedb.client.http.response.EmptyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

public class BeginTAExchange implements Exchange<EmptyResponse> {
    private final String name;
    private final WebClient webClient;

    public BeginTAExchange(String name, WebClient webClient) {
        this.name = name;
        this.webClient = webClient;
    }

    @Override
    public Mono<EmptyResponse> exchange() {
        return webClient.post()
                .uri(String.format("%s/%s", Exchange.BASEURL_BEGIN, name))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                        return Mono.justOrEmpty(
                                new EmptyResponse(response.headers().asHttpHeaders()
                                        .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
                    } else {
                        return response.createError();
                    }
                });
    }
}
