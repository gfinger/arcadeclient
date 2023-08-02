package org.makkiato.arcadeclient.refac.web;

import org.makkiato.arcadeclient.data.web.ArcadeclientErrorResponseFilter;
import org.makkiato.arcadeclient.refac.conf.ConnectionProperties;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

public class WebClientFactory {
    private static final String PATH_SEGMENT_API = "/api";
    private static final String PATH_SEGMENT_VERSION = "/v1";
    private static final String SCHEME_HTTP = "http";

    private final ArcadeclientErrorResponseFilter arcadedbErrorResponseFilter;
    private final ConnectionProperties connectionProperties;

    public WebClientFactory(ArcadeclientErrorResponseFilter arcadedbErrorResponseFilter,
            ConnectionProperties connectionProperties) {
        this.arcadedbErrorResponseFilter = arcadedbErrorResponseFilter;
        this.connectionProperties = connectionProperties;
    }

    public WebClient createWebClient() {
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

    public WebClient createWebClientFor(String hostname, Integer port, String username, String password) {
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
