package org.makkiato.arcadedb.client;

import lombok.Getter;
import org.makkiato.arcadedb.client.exception.ArcadeClientConfigurationException;
import org.makkiato.arcadedb.client.exception.ArcadeConnectionException;
import org.makkiato.arcadedb.client.http.request.DbExistsExchange;
import org.makkiato.arcadedb.client.http.request.ServerExchange;
import org.makkiato.arcadedb.client.http.response.BooleanResponse;
import org.makkiato.arcadedb.client.http.response.StatusResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


public class ArcadedbFactory {
    private final WebClient webClient;
    @Getter
    private final String name;

    ArcadedbFactory(String name, WebClient webClient) {
        this.name = name;
        assert (webClient != null);
        this.webClient = webClient;
    }

    public Mono<ArcadedbConnection> open(String databaseName) throws ArcadeConnectionException {
        return new ServerExchange("sql", String.format("open database %s", databaseName), webClient)
                .exchange()
                .map(StatusResponse::result)
                .filter(result -> result.equals("ok"))
                .map(result -> new ArcadedbConnection(databaseName, webClient));
    }

    public Mono<ArcadedbConnection> create(String databaseName) {
        return new ServerExchange("sql", String.format("create database %s", databaseName), webClient)
                .exchange()
                .map(StatusResponse::result)
                .filter(response -> response.equals("ok"))
                .map(response -> new ArcadedbConnection(databaseName, webClient));
    }

    public Mono<Boolean> exists(String databaseName) throws ArcadeClientConfigurationException {
        return new DbExistsExchange(databaseName, webClient).exchange().map(BooleanResponse::result);
    }
}