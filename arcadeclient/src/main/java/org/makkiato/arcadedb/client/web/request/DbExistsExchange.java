package org.makkiato.arcadedb.client.web.request;

import org.makkiato.arcadedb.client.web.response.BooleanResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class DbExistsExchange implements Exchange<BooleanResponse> {

    private final String name;
    private final WebClient webClient;

    public DbExistsExchange(String name, WebClient webClient) {
        this.name = name;
        this.webClient = webClient;
    }

    @Override
    public Mono<BooleanResponse> exchange() {
        return webClient.get()
                .uri(String.format("%s/%s", Exchange.BASEURL_EXISTS, name))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(BooleanResponse.class);
                    } else {
                        return response.createError();
                    }
                });
    }
}
