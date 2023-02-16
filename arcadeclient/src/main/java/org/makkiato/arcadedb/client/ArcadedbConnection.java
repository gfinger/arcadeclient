package org.makkiato.arcadedb.client;

import lombok.Data;
import org.makkiato.arcadedb.client.exception.ArcadeClientConfigurationException;
import org.makkiato.arcadedb.client.exception.ArcadeConnectionException;
import org.makkiato.arcadedb.client.http.request.CommandExchange;
import org.makkiato.arcadedb.client.http.request.DbExistsExchange;
import org.makkiato.arcadedb.client.http.request.ServerExchange;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Data
public class ArcadedbConnection {
    private final WebClient webClient;
    private final String connectionName;
    private String databaseName;

    ArcadedbConnection(String connectionName, WebClient webClient) {
        this.connectionName = connectionName;
        assert (webClient != null);
        this.webClient = webClient;
    }

    public Boolean open(String databaseName) throws ArcadeConnectionException {
        if (getDatabaseName() != null) {
            throw new ArcadeConnectionException("Close the currently opened database first");
        }
        var result = new ServerExchange("sql", String.format("open database %s", databaseName), webClient)
                .exchange().onErrorResume(ex ->
                        ex instanceof WebClientResponseException ?
                                Mono.error(new ArcadeConnectionException(ex.getMessage())) : Mono.error(ex)).block().result();
        if (result.equalsIgnoreCase("ok")) {
            setDatabaseName(databaseName);
            return true;
        }
        return false;
    }

    public Boolean create(String databaseName) {
        if (databaseName == null) {
            return false;
        }
        var result = new ServerExchange("sql", String.format("create database %s", databaseName), webClient)
                .exchange().onErrorResume(ex ->
                        ex instanceof WebClientResponseException ?
                                Mono.error(new ArcadeConnectionException(ex.getMessage())) : Mono.error(ex)).block().result();
        if (result.equalsIgnoreCase("ok")) {
            setDatabaseName(databaseName);
            return true;
        }
        return false;
    }

    public Boolean exists(String databaseName) throws ArcadeClientConfigurationException {
        return new DbExistsExchange(databaseName, webClient).exchange().block().result();
    }

    public Boolean close() {
        if (getDatabaseName() == null) {
            return true;
        }
        var result = new ServerExchange("sql", String.format("close database %s", getDatabaseName()), webClient)
                .exchange().block().result();
        if (result.equalsIgnoreCase("ok")) {
            setDatabaseName(null);
            return true;
        }
        return false;
    }

    public Map<String, String>[] command(String command) throws ArcadeConnectionException {
        if (getDatabaseName() == null) {
            throw new ArcadeConnectionException("Open or create a database first");
        }
        return new CommandExchange("sql", command, getDatabaseName(), webClient)
                .exchange().onErrorResume(ex ->
                        ex instanceof WebClientResponseException ?
                                Mono.error(new ArcadeConnectionException(ex.getMessage())) : Mono.error(ex)).block().result();
    }
}