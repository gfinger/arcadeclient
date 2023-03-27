package org.makkiato.arcadeclient.data.web.request;

import org.makkiato.arcadeclient.data.operations.GenericArcadedbOperations;
import org.makkiato.arcadeclient.data.web.response.CommandResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class QueryExchange implements Exchange<CommandResponse> {
    private final String name;
    private final WebClient webClient;
    private final String queryPathSegment;
    private final GenericArcadedbOperations.CommandLanguage language;

    public QueryExchange(GenericArcadedbOperations.CommandLanguage language, String query, String name, WebClient webClient) {
        this.name = name;
        this.webClient = webClient;
        this.queryPathSegment = query;
        this.language = language;
    }

    @Override
    public Mono<CommandResponse> exchange() {
        return webClient.get()
                .uri(String.format("%s/%s/%s/%s", BASEURL_QUERY, name, language.key, queryPathSegment))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(CommandResponse.class);
                    } else {
                        return response.createError();
                    }
                });
    }
}
