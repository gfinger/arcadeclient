package org.makkiato.arcadedb.client;

import org.makkiato.arcadedb.client.ArcadedbProperties.ConnectionProperties;
import org.makkiato.arcadedb.client.exception.client.ArcadeClientConfigurationException;
import org.makkiato.arcadedb.client.web.client.WebClientSupplier;
import org.makkiato.arcadedb.client.web.request.DbExistsExchange;
import org.makkiato.arcadedb.client.web.request.ServerExchange;
import org.makkiato.arcadedb.client.web.response.BooleanResponse;
import org.makkiato.arcadedb.client.web.response.StatusResponse;

import reactor.core.publisher.Mono;

public class ArcadedbFactory {
    private final WebClientSupplier webClientSupplier;

    public ArcadedbFactory(WebClientFactory arcadedbClient, ConnectionProperties connectionProperties) {
        this.webClientSupplier = arcadedbClient.getWebClientSupplierFor(connectionProperties);
    }

    public Mono<ArcadedbConnection> open(String databaseName) {
        var webClient = webClientSupplier.get();
        return new ServerExchange("sql", String.format("open database %s", databaseName), webClient)
                .exchange()
                .map(StatusResponse::result)
                .filter(result -> result.equals("ok"))
                .map(result -> new ArcadedbConnection(databaseName, webClient));
    }

    public Mono<ArcadedbConnection> create(String databaseName) {
        var webClient = webClientSupplier.get();
        return new ServerExchange("sql", String.format("create database %s", databaseName), webClient)
                .exchange()
                .map(StatusResponse::result)
                .filter(response -> response.equals("ok"))
                .map(response -> new ArcadedbConnection(databaseName, webClient));
    }

    public Mono<String> drop(String databaseName) {
        var webClient = webClientSupplier.get();
        return new ServerExchange("sql", String.format("drop database %s", databaseName), webClient)
                .exchange()
                .map(StatusResponse::result);
    }

    public Mono<Boolean> exists(String databaseName) throws ArcadeClientConfigurationException {
        return new DbExistsExchange(databaseName, webClientSupplier.get()).exchange().map(BooleanResponse::result);
    }
}