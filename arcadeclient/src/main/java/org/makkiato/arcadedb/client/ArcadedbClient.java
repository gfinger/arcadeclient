package org.makkiato.arcadedb.client;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.makkiato.arcadedb.client.ArcadedbProperties.ConnectionProperties;
import org.makkiato.arcadedb.client.web.ArcadedbErrorResponseFilter;
import org.makkiato.arcadedb.client.web.request.ServerInfoExchange;
import org.makkiato.arcadedb.client.web.response.ServerInfoResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

public class ArcadedbClient {
    private static final String PATH_SEGMENT_API = "/api";
    private static final String PATH_SEGMENT_VERSION = "/v1";
    private static final String SCHEME_HTTP = "http";
    private ArcadedbErrorResponseFilter arcadedbErrorResponseFilter;

    public ArcadedbClient(ArcadedbErrorResponseFilter arcadedbErrorResponseFilter) {
        this.arcadedbErrorResponseFilter = arcadedbErrorResponseFilter;
    }

    @Cacheable(value = "server-info")
    public Optional<ServerInfoResponse> serverInfo(ConnectionProperties connectionProperties, String mode) {
        var webClient = getConfiguredWebClientFor(connectionProperties);
        var timeout = connectionProperties.getConnectionTimeoutSecs();
        return new ServerInfoExchange(mode, webClient).exchange()
                .blockOptional(Duration.ofSeconds(timeout));
    }

    private ServerSpec[] getServerSpecs(ConnectionProperties connectionProperties) {
        return serverInfo(connectionProperties, "cluster").map(serverInfo -> {
            if (serverInfo.ha() != null) {
                if (connectionProperties.getLeaderPreferred()) {
                    var leaderAddress = serverInfo.ha().leaderAddress();
                    if (leaderAddress != null) {
                        var indexOfColon = leaderAddress.indexOf(':');
                        return new ServerSpec[] { new ServerSpec(
                                leaderAddress.substring(0, indexOfColon),
                                Integer.parseInt(leaderAddress
                                        .substring(indexOfColon + 1))) };
                    }
                }
                var replicaAddresses = serverInfo.ha().replicaAddresses();
                if (replicaAddresses != null) {
                    return Arrays.stream(replicaAddresses.split(",")).map(entry -> {
                        var splitted = entry.split(":");
                        if (splitted.length < 2) {
                            return new ServerSpec(connectionProperties.getHost(),
                                    connectionProperties.getPort());
                        } else {
                            return new ServerSpec(splitted[0],
                                    Integer.parseInt(splitted[1]));
                        }
                    }).toArray(ServerSpec[]::new);
                }
            }
            return new ServerSpec[] { new ServerSpec(connectionProperties.getHost(),
                    connectionProperties.getPort()) };
        }).orElseThrow();
    }

    public WebClient[] getHAWebClientsFor(ConnectionProperties connectionProperties) {
        var serverSpecs = getServerSpecs(connectionProperties);
        return Arrays.stream(serverSpecs).map(spec -> {
            String baseUrl = UriComponentsBuilder.newInstance()
                    .scheme(SCHEME_HTTP)
                    .host(spec.hostname())
                    .port(spec.port())
                    .path(PATH_SEGMENT_API + PATH_SEGMENT_VERSION)
                    .toUriString();
            return WebClient.builder()
                    .baseUrl(baseUrl)
                    .filter(arcadedbErrorResponseFilter)
                    .filter(ExchangeFilterFunctions
                            .basicAuthentication(connectionProperties.getUsername(),
                                    connectionProperties.getPassword()))
                    .build();
        }).toArray(WebClient[]::new);
    }

    public WebClient getConfiguredWebClientFor(ConnectionProperties connectionProperties) {
        String baseUrl = UriComponentsBuilder.newInstance()
                .scheme(SCHEME_HTTP)
                .host(connectionProperties.getHost())
                .port(connectionProperties.getPort())
                .path(PATH_SEGMENT_API + PATH_SEGMENT_VERSION)
                .toUriString();
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(arcadedbErrorResponseFilter)
                .filter(ExchangeFilterFunctions.basicAuthentication(connectionProperties.getUsername(),
                        connectionProperties.getPassword()))
                .build();
    }

    public WebClientSupplier getWebClientSupplierFor(ConnectionProperties connectionProperties) {
        return new WebClientSupplier(getHAWebClientsFor(connectionProperties));
    }

    record ServerSpec(String hostname, int port) {
    }

    class WebClientSupplier {
        private final WebClient[] webClients;
        private int index;

        WebClientSupplier(WebClient[] webClients) {
            this.index = 0;
            this.webClients = webClients;
        }

        WebClient get() {
            return webClients[index++ % webClients.length];
        }
    }
}
