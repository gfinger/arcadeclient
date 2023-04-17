package org.makkiato.arcadeclient.data.core;

import org.makkiato.arcadeclient.data.core.ArcadedbProperties.ConnectionProperties;
import org.makkiato.arcadeclient.data.web.ArcadedbErrorResponseFilter;
import org.makkiato.arcadeclient.data.web.client.WebClientSpec;
import org.makkiato.arcadeclient.data.web.client.WebClientSupplier;
import org.makkiato.arcadeclient.data.web.client.WebClientSupplierStrategy;
import org.makkiato.arcadeclient.data.web.request.ServerInfoExchange;
import org.makkiato.arcadeclient.data.web.response.ServerInfoResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WebClientFactory {
    private static final String PATH_SEGMENT_API = "/api";
    private static final String PATH_SEGMENT_VERSION = "/v1";
    private static final String SCHEME_HTTP = "http";
    private final ArcadedbErrorResponseFilter arcadedbErrorResponseFilter;
    private final WebClientSupplierStrategy webClientSupplierStrategy;

    public WebClientFactory(ArcadedbErrorResponseFilter arcadedbErrorResponseFilter, WebClientSupplierStrategy webClientSupplierStrategy) {
        this.arcadedbErrorResponseFilter = arcadedbErrorResponseFilter;
        this.webClientSupplierStrategy = webClientSupplierStrategy;
    }

    @Cacheable(value = "server-info")
    public Optional<ServerInfoResponse> serverInfo(ConnectionProperties connectionProperties, String mode) {
        var webClient = createWebClientFor(connectionProperties.getHost(), connectionProperties.getPort(),
                connectionProperties.getUsername(), connectionProperties.getPassword());
        var timeout = connectionProperties.getConnectionTimeoutSecs();
        return new ServerInfoExchange(mode, webClient).exchange()
                .blockOptional(Duration.ofSeconds(timeout));
    }

    public WebClientSupplier getWebClientSupplierFor(ConnectionProperties connectionProperties) {
        var serverInfo = serverInfo(connectionProperties, "cluster");
        var webClientSpecs = getHAServerSpecs(serverInfo.get(), connectionProperties.getUsername(),
                connectionProperties.getPassword());
        webClientSpecs.add(new WebClientSpec(createWebClientFor(connectionProperties.getHost(),
                connectionProperties.getPort(), connectionProperties.getUsername(),
                connectionProperties.getPassword()), false, false, false));
        return new WebClientSupplier(webClientSupplierStrategy, webClientSpecs);
    }

    private List<WebClientSpec> getHAServerSpecs(ServerInfoResponse serverInfo, String username, String password) {
        var specs = new ArrayList<WebClientSpec>();
        if (serverInfo.ha() != null) {
            var leaderAddress = serverInfo.ha().leaderAddress();
            if (leaderAddress != null) {
                var indexOfColon = leaderAddress.indexOf(':');
                var webClient = createWebClientFor(leaderAddress.substring(0, indexOfColon),
                        Integer.parseInt(leaderAddress.substring(indexOfColon + 1)), username, password);
                specs.add(new WebClientSpec(webClient, true, true, false));
            }
            var replicaAddresses = serverInfo.ha().replicaAddresses();
            if (replicaAddresses != null) {
                Arrays.stream(replicaAddresses.split(",")).map(entry -> {
                    var splitted = entry.split(":");
                    var webClient = createWebClientFor(splitted[0], Integer.parseInt(splitted[1]), username, password);
                    return new WebClientSpec(webClient, true, false, true);
                }).forEach(spec -> specs.add(spec));
            }
        }
        return specs;
    }

    private WebClient createWebClientFor(String hostname, Integer port, String username, String password) {
        String baseUrl = UriComponentsBuilder.newInstance()
                .scheme(SCHEME_HTTP)
                .host(hostname)
                .port(port)
                .path(PATH_SEGMENT_API + PATH_SEGMENT_VERSION)
                .toUriString();
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(arcadedbErrorResponseFilter)
                .filter(ExchangeFilterFunctions.basicAuthentication(username, password))
                .build();
    }
}

