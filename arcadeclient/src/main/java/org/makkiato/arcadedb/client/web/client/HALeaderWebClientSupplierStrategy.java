package org.makkiato.arcadedb.client.web.client;

import java.util.List;

import org.makkiato.arcadedb.client.exception.client.ArcadeClientConfigurationException;
import org.springframework.web.reactive.function.client.WebClient;

public class HALeaderWebClientSupplierStrategy implements WebClientSupplierStrategy {

    @Override
    public WebClient apply(List<WebClientSpec> webClientSpecs) {
        var leader = webClientSpecs.stream().filter(spec -> spec.leader()).findAny();
        var configured = webClientSpecs.stream().filter(spec -> !spec.ha()).findAny();
        return leader.or(() -> configured)
                .orElseThrow(() -> new ArcadeClientConfigurationException("no webClient spec for leader")).webClient();
    }

}