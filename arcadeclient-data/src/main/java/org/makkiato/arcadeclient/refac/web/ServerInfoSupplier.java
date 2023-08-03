package org.makkiato.arcadeclient.refac.web;

import org.makkiato.arcadeclient.data.web.client.WebClientSupplier;
import org.makkiato.arcadeclient.data.web.request.ServerInfoExchange;
import org.makkiato.arcadeclient.data.web.response.ServerInfoResponse;
import org.makkiato.arcadeclient.refac.conf.ConnectionProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ServerInfoSupplier {
    private final WebClientFactory webClientFactory;
    private final String MODE_CLUSTER = "cluster";

    public ServerInfoSupplier(WebClientFactory webClientFactory) {
        this.webClientFactory = webClientFactory;
    }
    public Optional<ServerInfoResponse> serverInfo(ConnectionProperties connectionProperties) {
        var webClient = webClientFactory.createWebClient();
        var timeout = connectionProperties.getConnectionTimeoutSecs();
        return new ServerInfoExchange(MODE_CLUSTER, webClient).exchange()
                .blockOptional(Duration.ofSeconds(timeout));
    }

    public List<WebClientSpec> getWebClientSpecs(ConnectionProperties connectionProperties) {
        var serverInfo = serverInfo(connectionProperties);
        var webClientSpecs = getHAServerSpecs(serverInfo.orElseThrow(), connectionProperties);
        webClientSpecs.add(getDefaultServerSpec(connectionProperties));
        return webClientSpecs;
    }

    public WebClientSpec getDefaultServerSpec(ConnectionProperties connectionProperties) {
        return new WebClientSpec(connectionProperties.getHost(), connectionProperties.getPort(), false, false, false);
    }

    public List<WebClientSpec> getHAServerSpecs(ServerInfoResponse serverInfo, ConnectionProperties connectionProperties) {
        var specs = new ArrayList<WebClientSpec>();
        if (serverInfo.ha() != null) {
            var leaderAddress = serverInfo.ha().leaderAddress();
            if (leaderAddress != null) {
                var indexOfColon = leaderAddress.indexOf(':');
                specs.add(new WebClientSpec(leaderAddress.substring(0, indexOfColon),Integer.parseInt(leaderAddress.substring(indexOfColon + 1)), true, true, false));
            }
            var replicaAddresses = serverInfo.ha().replicaAddresses();
            if (replicaAddresses != null) {
                Arrays.stream(replicaAddresses.split(",")).map(entry -> {
                    var replicaAddressComponents = entry.split(":");
                    return new WebClientSpec(replicaAddressComponents[0], Integer.parseInt(replicaAddressComponents[1]), true, false, true);
                }).forEach(specs::add);
            }
        }
        return specs;
    }
}

