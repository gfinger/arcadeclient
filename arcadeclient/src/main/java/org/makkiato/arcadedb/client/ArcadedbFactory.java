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
    private final String databaseName;

    public ArcadedbFactory(WebClientFactory webClientFactory, ConnectionProperties connectionProperties) {
        this.webClientSupplier = webClientFactory.getWebClientSupplierFor(connectionProperties);
        this.databaseName = connectionProperties.getDatabase();
    }

    public Mono<Boolean> open() {
        var webClient = webClientSupplier.get();
        return new ServerExchange("sql", String.format("open database %s", databaseName), webClient)
                .exchange()
                .map(StatusResponse::result)
                .map(response -> response.equals("ok"));
    }

    public Mono<Boolean> create() {
        var webClient = webClientSupplier.get();
        return new ServerExchange("sql", String.format("create database %s", databaseName), webClient)
                .exchange()
                .map(StatusResponse::result)
                .map(response -> response.equals("ok"));
    }

    public Mono<Boolean> close() {
        var webClient = webClientSupplier.get();
        return new ServerExchange("sql", String.format("close database %s", databaseName), webClient)
                .exchange().map(response -> response.result().equalsIgnoreCase("ok"));
    }

    public Mono<Boolean> drop() {
        var webClient = webClientSupplier.get();
        return new ServerExchange("sql", String.format("drop database %s", databaseName), webClient)
                .exchange()
                .map(StatusResponse::result)
                .map(response -> response.equals("ok"));
    }

    public Mono<Boolean> exists() throws ArcadeClientConfigurationException {
        return new DbExistsExchange(databaseName, webClientSupplier.get()).exchange().map(BooleanResponse::result);
    }
}