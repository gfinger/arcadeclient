package org.makkiato.arcadedb.client;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.makkiato.arcadedb.client.ArcadedbProperties.ConnectionProperties;
import org.makkiato.arcadedb.client.exception.client.ArcadeClientConfigurationException;
import org.makkiato.arcadedb.client.exception.server.*;
import org.makkiato.arcadedb.client.http.request.ServerInfoExchange;
import org.makkiato.arcadedb.client.http.response.ErrorResponse;
import org.makkiato.arcadedb.client.http.response.ServerInfoResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
public class ArcadedbClient {
    private static final String PATH_SEGMENT_API = "/api";
    private static final String PATH_SEGMENT_VERSION = "/v1";
    private static final String SCHEME_HTTP = "http";
    @Getter
    private final Map<String, ConnectionProperties> connectionPropertiesMap;
    private final int nextReplicaServerIndex = 0;

    public ArcadedbClient(Map<String, ConnectionProperties> connectionPropertiesMap) {
        this.connectionPropertiesMap = connectionPropertiesMap;
    }

    private static Mono<ClientResponse> exchangeFilterResponseProcessor(ClientResponse response) {
        var status = response.statusCode();
        if (!status.is2xxSuccessful()) {
            response.bodyToMono(ErrorResponse.class).blockOptional(Duration.ofSeconds(5)).ifPresent(error -> {
                log.error(String.format("ArcadeDB error: %s", error));
                if (error.exception().equals(ServerIsNotTheLeaderException.class.getName())) {
                    final int sep = error.detail().lastIndexOf('.');
                    throw new ServerIsNotTheLeaderException(sep > -1 ? error.detail().substring(0, sep) :
                            error.detail(), error.exceptionArgs());
                } else if (error.exception().equals(QuorumNotReachedException.class.getName())) {
                    throw new QuorumNotReachedException(error.detail());
                } else if (error.exception().equals(DuplicatedKeyException.class.getName()) && error.exceptionArgs() != null) {
                    final String[] exceptionArgsParts = error.exceptionArgs().split("\\|");
                    throw new DuplicatedKeyException(exceptionArgsParts[0], exceptionArgsParts[1],
                            exceptionArgsParts[2]);
                } else if (error.exception().equals(ConcurrentModificationException.class.getName())) {
                    throw new ConcurrentModificationException(error.detail());
                } else if (error.exception().equals(TransactionException.class.getName())) {
                    throw new TransactionException(error.detail());
                } else if (error.exception().equals(TimeoutException.class.getName())) {
                    throw new TimeoutException(error.detail());
                } else if (error.exception().equals(SchemaException.class.getName())) {
                    throw new SchemaException(error.detail());
                } else if (error.exception().equals(NoSuchElementException.class.getName())) {
                    throw new NoSuchElementException(error.detail());
                } else if (error.exception().equals(SecurityException.class.getName())) {
                    throw new SecurityException(error.detail());
                } else if (error.exception().equals("com.arcadedb.server.security.ServerSecurityException")) {
                    throw new SecurityException(error.detail());
                } else {
                    throw new RemoteException(String.format("Error on executing remote operation %s", error));
                }
            });
            return response.createError();
        }
        return Mono.just(response);
    }

    @Cacheable(value = "server-info")
    public Optional<ServerInfoResponse> serverInfo(String connectionName, String mode) {
        ConnectionProperties connectionProperties = getConnectionPropertiesFor(connectionName);
        WebClient webClient = getWebClientFor(connectionProperties);
        return new ServerInfoExchange(mode, webClient).exchange()
                .onErrorResume(WebClientRequestException.class, e -> Mono.empty())
                .blockOptional(Duration.ofSeconds(connectionProperties.getConnectionTimeoutSecs()));
    }

    public ServerSpec[] getServerSpecs(String connectionName) {
        ConnectionProperties connectionProperties = getConnectionPropertiesFor(connectionName);
        return serverInfo(connectionName, "cluster").map(serverInfo -> {
            if (serverInfo.ha() != null) {
                if (connectionProperties.getLeaderPreferred()) {
                    var leaderAddress = serverInfo.ha().leaderAddress();
                    if (leaderAddress != null) {
                        var indexOfColon = leaderAddress.indexOf(':');
                        return new ServerSpec[]{new ServerSpec(leaderAddress.substring(0, indexOfColon),
                                Integer.parseInt(leaderAddress.substring(indexOfColon + 1)))};
                    }
                }
                var replicaAddresses = serverInfo.ha().replicaAddresses();
                if (replicaAddresses != null) {
                    return Arrays.stream(replicaAddresses.split(",")).map(entry -> {
                        var splitted = entry.split(":");
                        if (splitted.length < 2) {
                            return new ServerSpec(connectionProperties.getHost(), connectionProperties.getPort());
                        } else {
                            return new ServerSpec(splitted[0], Integer.parseInt(splitted[1]));
                        }
                    }).toArray(ServerSpec[]::new);
                }
            }
            return new ServerSpec[]{new ServerSpec(connectionProperties.getHost(), connectionProperties.getPort())};
        }).orElseThrow();
    }

    public ConnectionProperties getConnectionPropertiesFor(String connectionName) {
        var connectionProperties = connectionPropertiesMap.get(connectionName);
        if (connectionProperties == null) {
            throw new ArcadeClientConfigurationException(String.format("Missing configuration for database: %s",
                    connectionName));
        }
        return connectionProperties;
    }

    public ArcadedbFactory createFactoryFor(String connectionName) {
        var connectionProperties = getConnectionPropertiesFor(connectionName);
        return new ArcadedbFactory(connectionName,
                new WebClientSupplier(getWebClientsFor(getServerSpecs(connectionName),
                        connectionProperties.getUsername(), connectionProperties.getPassword())));
    }

    @Cacheable("web-clients")
    public WebClient[] getWebClientsFor(ServerSpec[] serverSpecs, String username, String password) {
        var errorResponseFilter =
                ExchangeFilterFunction.ofResponseProcessor(ArcadedbClient::exchangeFilterResponseProcessor);
        return Arrays.stream(serverSpecs).map(spec -> {
            String baseUrl = UriComponentsBuilder.newInstance()
                    .scheme(SCHEME_HTTP)
                    .host(spec.hostname())
                    .port(spec.port())
                    .path(PATH_SEGMENT_API + PATH_SEGMENT_VERSION)
                    .toUriString();
            return WebClient.builder()
                    .baseUrl(baseUrl)
                    .filter(errorResponseFilter)
                    .filter(ExchangeFilterFunctions
                            .basicAuthentication(username, password))
                    .build();
        }).toArray(WebClient[]::new);
    }

    @Cacheable("web-client")
    public WebClient getWebClientFor(ConnectionProperties connectionProperties) {
        var errorResponseFilter =
                ExchangeFilterFunction.ofResponseProcessor(ArcadedbClient::exchangeFilterResponseProcessor);
        String baseUrl = UriComponentsBuilder.newInstance()
                .scheme(SCHEME_HTTP)
                .host(connectionProperties.getHost())
                .port(connectionProperties.getPort())
                .path(PATH_SEGMENT_API + PATH_SEGMENT_VERSION)
                .toUriString();
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(errorResponseFilter)
                .filter(ExchangeFilterFunctions.basicAuthentication(connectionProperties.getUsername(),
                        connectionProperties.getPassword()))
                .build();
    }

    record ServerSpec(String hostname, int port) {
    }

    class WebClientSupplier {
        private final WebClient[] webClients;
        private int index;

        WebClientSupplier(WebClient[] serverSpecs) {
            this.index = 0;
            this.webClients = serverSpecs;
        }

        WebClient get() {
            return webClients[index++ % webClients.length];
        }
    }
}
