package org.makkiato.arcadedb.client;

import org.makkiato.arcadedb.client.ArcadedbProperties.ConnectionProperties;
import org.makkiato.arcadedb.client.exception.ArcadeClientConfigurationException;
import org.makkiato.arcadedb.client.httpexchange.DbExistsExchange;
import org.makkiato.arcadedb.client.httpexchange.ServerExchange;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

public class ArcadedbClient {

    private final Map<String, ConnectionProperties> connections;

    public ArcadedbClient(Map<String, ConnectionProperties> connections) {
        this.connections = connections;
    }

    public Optional<ArcadedbConnection> open(String name) throws ArcadeClientConfigurationException {
        ConnectionProperties connectionProperties = getConnectionProperties(name);
        WebClient webClient = createWebClient(connectionProperties);
        var result = new ServerExchange("sql", String.format("open database %s", name), webClient)
                .exchange().block().result();
        return Optional.ofNullable(result.equalsIgnoreCase("ok") ?
                createConnectionFor(name, connections.get(name)) : null);
    }

    public ArcadedbConnection create(String name) {
        return createConnectionFor(name, connections.get(name));
    }

    public Boolean exists(String name) throws ArcadeClientConfigurationException {
        ConnectionProperties connectionProperties = getConnectionProperties(name);
        WebClient webClient = createWebClient(connectionProperties);
        return new DbExistsExchange(name, webClient).exchange().block().result();
    }

    ArcadedbConnection createConnectionFor(String dbName, ConnectionProperties connectionProperties) {
        return new ArcadedbConnection(dbName, createWebClient(connectionProperties));
    }

    WebClient createWebClient(ConnectionProperties connectionProperties) {
        assert (connectionProperties != null);
        String baseUrl = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(connectionProperties.getHost())
                .port(connectionProperties.getPort())
                .path("/api/v1").toUriString();
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(ExchangeFilterFunctions.basicAuthentication(connectionProperties.getUsername(), connectionProperties.getPassword()))
                .build();
    }

    ConnectionProperties getConnectionProperties(String name) throws ArcadeClientConfigurationException {
        var connectionProperties = connections.get(name);
        if (connectionProperties == null) {
            throw new ArcadeClientConfigurationException(String.format("Missing configuration for database: %s", name));
        }
        return connectionProperties;
    }
}
