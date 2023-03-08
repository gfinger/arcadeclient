package org.makkiato.arcadedb.client;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.makkiato.arcadedb.client.ArcadedbProperties.ConnectionProperties;
import org.makkiato.arcadedb.client.exception.client.ArcadeClientConfigurationException;
import org.makkiato.arcadedb.client.exception.server.IllegalArgumentException;
import org.makkiato.arcadedb.client.exception.server.SecurityException;
import org.makkiato.arcadedb.client.exception.server.*;
import org.makkiato.arcadedb.client.http.request.ServerInfoExchange;
import org.makkiato.arcadedb.client.http.response.ErrorResponse;
import org.makkiato.arcadedb.client.http.response.ServerInfoResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ArcadedbClient {
    private static final String PATH_SEGMENT_API = "/api";
    private static final String PATH_SEGMENT_VERSION = "/v1";
    private static final String SCHEME_HTTP = "http";
    @Getter
    private final Map<String, ConnectionProperties> connectionPropertiesMap;

    public ArcadedbClient(Map<String, ConnectionProperties> connectionPropertiesMap) {
        this.connectionPropertiesMap = connectionPropertiesMap;
    }

    private static Mono<ClientResponse> exchangeFilterResponseProcessor(ClientResponse response) {
        var status = response.statusCode();
        if (!status.is2xxSuccessful()) {
            return response.bodyToMono(ErrorResponse.class).flatMap(error -> {
                log.error(String.format("ArcadeDB error: %s", error));
                return switch (error.getException()) {
                    case "com.arcadedb.network.binary.ServerIsNotTheLeaderException" ->
                            Mono.error(new ServerIsNotTheLeaderException(error.getError(), status.value()));
                    case "com.arcadedb.network.binary.QuorumNotReachedException" ->
                            Mono.error(new QuorumNotReachedException(error.getError(), status.value()));
                    case "com.arcadedb.exception.DuplicatedKeyException" ->
                            Mono.error(new DuplicatedKeyException(error.getDetail(), status.value()));
                    case "com.arcadedb.exception.ConcurrentModificationException" ->
                            Mono.error(new ConcurrentModificationException(error.getError(), status.value()));
                    case "com.arcadedb.exception.TransactionException" ->
                            Mono.error(new TransactionException(error.getError(), status.value()));
                    case "com.arcadedb.exception.TimeoutException" ->
                            Mono.error(new TimeoutException(error.getError(), status.value()));
                    case "com.arcadedb.exception.SchemaException" ->
                            Mono.error(new SchemaException(error.getDetail(), status.value()));
                    case "java.util.NoSuchElementException", "com.arcadedb.server.security.ServerSecurityException" ->
                            Mono.error(new NoSucheElementException(error.getError(), status.value()));
                    case "java.lang.SecurityException" ->
                            Mono.error(new SecurityException(error.getError(), status.value()));
                    case "com.arcadedb.query.sql.parser.ParseException" ->
                            Mono.error(new ParseException(error.getDetail(), status.value()));
                    case "com.arcadedb.exception.DatabaseOperationException" ->
                            Mono.error(new DatabaseOperationException(error.getDetail(), status.value()));
                    case "java.lang.IllegalArgumentException" ->
                            Mono.error(new IllegalArgumentException(error.getDetail(), status.value()));
                    case "com.arcadedb.exception.CommandExecutionException" ->
                            Mono.error((new CommandExecutionException(error.getDetail(), status.value())));
                    case "com.arcadedb.exception.ValidationException" ->
                            Mono.error(new ValidationException(error.getDetail(), status.value()));
                    default ->
                            Mono.error(new RemoteException(String.format("Error on executing remote operation %s",
                            error), status.value()));
                };
            });
        }
        return Mono.just(response);
    }

    @Cacheable(value = "server-info")
    public Optional<ServerInfoResponse> serverInfo(String connectionName, String mode) {
        var connectionProperties = getConnectionPropertiesFor(connectionName);
        var webClient = getWebClientFor(connectionProperties);
        return new ServerInfoExchange(mode, webClient).exchange()
                .blockOptional(Duration.ofSeconds(connectionProperties.getConnectionTimeoutSecs()));
    }

    public ServerSpec[] getServerSpecs(String connectionName) {
        var connectionProperties = getConnectionPropertiesFor(connectionName);
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
