package org.makkiato.arcadeclient.data.core;

import org.makkiato.arcadeclient.data.exception.client.ArcadeClientConfigurationException;
import org.makkiato.arcadeclient.data.web.client.WebClientSupplier;
import org.makkiato.arcadeclient.data.web.request.DbExistsExchange;
import org.makkiato.arcadeclient.data.web.request.ServerExchange;
import org.makkiato.arcadeclient.data.web.response.BooleanResponse;
import org.makkiato.arcadeclient.data.web.response.StatusResponse;

import reactor.core.publisher.Mono;

public class Arcadeclient {
    private final WebClientSupplier webClientSupplier;

    public Arcadeclient(WebClientSupplier webClientSupplier) {
        this.webClientSupplier = webClientSupplier;
    }

    public Mono<Boolean> open(String databaseName) {
        var webClient = webClientSupplier.get();
        return new ServerExchange("sql", String.format("open database %s", databaseName), webClient)
                .exchange()
                .map(StatusResponse::result)
                .map(response -> response.equals("ok"));
    }

    public Mono<Boolean> create(String databaseName) {
        var webClient = webClientSupplier.get();
        return new ServerExchange("sql", String.format("create database %s", databaseName), webClient)
                .exchange()
                .map(StatusResponse::result)
                .map(response -> response.equals("ok"));
    }

    public Mono<Boolean> close(String databaseName) {
        var webClient = webClientSupplier.get();
        return new ServerExchange("sql", String.format("close database %s", databaseName), webClient)
                .exchange().map(response -> response.result().equalsIgnoreCase("ok"));
    }

    public Mono<Boolean> drop(String databaseName) {
        var webClient = webClientSupplier.get();
        return new ServerExchange("sql", String.format("drop database %s", databaseName), webClient)
                .exchange()
                .map(StatusResponse::result)
                .map(response -> response.equals("ok"));
    }

    public Mono<Boolean> exists(String databaseName) throws ArcadeClientConfigurationException {
        return new DbExistsExchange(databaseName, webClientSupplier.get()).exchange().map(BooleanResponse::result);
    }
}