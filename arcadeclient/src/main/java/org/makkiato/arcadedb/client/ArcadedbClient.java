package org.makkiato.arcadedb.client;

import lombok.Getter;
import org.makkiato.arcadedb.client.ArcadedbProperties.ConnectionProperties;
import org.makkiato.arcadedb.client.exception.ArcadeClientConfigurationException;
import org.makkiato.arcadedb.client.http.request.ServerInfoExchange;
import org.makkiato.arcadedb.client.http.response.ServerInfoResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

public class ArcadedbClient {

    @Getter
    private final Map<String, ConnectionProperties> connectionPropertiesMap;

    public ArcadedbClient(Map<String, ConnectionProperties> connectionPropertiesMap) {
        this.connectionPropertiesMap = connectionPropertiesMap;
    }

    public Optional<ServerInfoResponse> serverInfo(String connectionName, String mode) throws ArcadeClientConfigurationException {
        ConnectionProperties connectionProperties = getConnectionPropertiesFor(connectionName);
        WebClient webClient = createWebClient(connectionProperties);
        return Optional.ofNullable(new ServerInfoExchange(mode, webClient).exchange().block());
    }

    public ConnectionProperties getConnectionPropertiesFor(String connectionName) throws ArcadeClientConfigurationException {
        var connectionProperties = connectionPropertiesMap.get(connectionName);
        if (connectionProperties == null) {
            throw new ArcadeClientConfigurationException(String.format("Missing configuration for database: %s", connectionName));
        }
        return connectionProperties;
    }

    public ArcadedbConnection createConnectionFor(String connectionName) throws ArcadeClientConfigurationException {
        ConnectionProperties connectionProperties = getConnectionPropertiesFor(connectionName);
        return new ArcadedbConnection(connectionName, createWebClient(connectionProperties));
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
}
