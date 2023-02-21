package org.makkiato.arcadedb.client;

import lombok.Getter;
import org.makkiato.arcadedb.client.ArcadedbProperties.ConnectionProperties;
import org.makkiato.arcadedb.client.exception.ArcadeClientConfigurationException;
import org.makkiato.arcadedb.client.http.request.ServerInfoExchange;
import org.makkiato.arcadedb.client.http.response.ServerInfoResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

public class ArcadedbClient {
    private static final String PATH_SEGMENT_API = "/api";
    private static final String PATH_SEGMENT_VERSION = "/v1";
    private static final String SCHEME_HTTP = "http";
    private int nextReplicaServerIndex = 0;
    private Map<String, ServerInfoResponse> serverInfoMap = new HashMap();

    @Getter
    private final Map<String, ConnectionProperties> connectionPropertiesMap;

    public ArcadedbClient(Map<String, ConnectionProperties> connectionPropertiesMap) {
        this.connectionPropertiesMap = connectionPropertiesMap;
    }

    public ServerInfoResponse serverInfo(String connectionName, String mode) throws ArcadeClientConfigurationException {
        ConnectionProperties connectionProperties = getConnectionPropertiesFor(connectionName);
        WebClient webClient = createWebClient(connectionProperties);
        return new ServerInfoExchange(mode, webClient).exchange().block();
    }

    public ConnectionProperties getPreferredConnectionPropertiesFor(String connectionName, Boolean reload) throws ArcadeClientConfigurationException {
        var connectionProperties = getConnectionPropertiesFor(connectionName);
        var serverInfo = serverInfoMap.get(connectionName);
        if(reload || serverInfoMap.get(connectionName)== null) {
            serverInfo = serverInfo(connectionName, "cluster");
            serverInfoMap.put(connectionName, serverInfo);
        }
        if(serverInfo == null || serverInfo.ha() == null) {
            return connectionProperties;
        }
        if(connectionProperties.getLeaderPreferred()) {
            var leaderAddress = serverInfo.ha().leaderAddress();
            var indexOfColon = leaderAddress.indexOf(':');
            connectionProperties.setHost(leaderAddress.substring(0,indexOfColon));
            connectionProperties.setPort(Integer.parseInt(leaderAddress.substring(indexOfColon+1)));
            return connectionProperties;
        }
        var replicaAddresses = serverInfo.ha().replicaAddresses();
        if(replicaAddresses == null || replicaAddresses.isEmpty()) {
            return connectionProperties;
        } else {
            var serverEntries = replicaAddresses.split(",");
            var selectedServer = serverEntries[nextReplicaServerIndex];
            nextReplicaServerIndex = (nextReplicaServerIndex + 1) % serverEntries.length;
            var indexOfColon = selectedServer.indexOf(':');
            connectionProperties.setHost(selectedServer.substring(0, indexOfColon));
            connectionProperties.setPort(Integer.parseInt(selectedServer.substring(indexOfColon + 1)));
            return connectionProperties;
        }
    }

    public ConnectionProperties getConnectionPropertiesFor(String connectionName) throws ArcadeClientConfigurationException {
        var connectionProperties = connectionPropertiesMap.get(connectionName);
        if (connectionProperties == null) {
            throw new ArcadeClientConfigurationException(String.format("Missing configuration for database: %s", connectionName));
        }
        return connectionProperties;
    }

    public ArcadedbFactory createFactoryFor(String connectionName) throws ArcadeClientConfigurationException {
        ConnectionProperties connectionProperties = getPreferredConnectionPropertiesFor(connectionName, true);
        return new ArcadedbFactory(connectionName, createWebClient(connectionProperties));
    }

    WebClient createWebClient(ConnectionProperties connectionProperties) {
        assert (connectionProperties != null);
        String baseUrl = UriComponentsBuilder.newInstance()
                .scheme(SCHEME_HTTP)
                .host(connectionProperties.getHost())
                .port(connectionProperties.getPort())
                .path(PATH_SEGMENT_API + PATH_SEGMENT_VERSION).toUriString();
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(ExchangeFilterFunctions.basicAuthentication(connectionProperties.getUsername(), connectionProperties.getPassword()))
                .build();
    }
}
