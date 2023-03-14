package org.makkiato.arcadedb.client.web.client;

import java.util.List;
import java.util.stream.Collectors;

import org.makkiato.arcadedb.client.exception.client.ArcadeClientConfigurationException;
import org.springframework.web.reactive.function.client.WebClient;

public class HARoundRobinWebClientSupplierStrategy implements WebClientSupplierStrategy {
    private int index = 0;

    @Override
    public WebClient apply(List<WebClientSpec> specs) {
        var replicas = specs.stream().filter(spec -> spec.replica());
        if (replicas.count() > 0) {
            return replicas.collect(Collectors.toUnmodifiableList()).get((int) (index++ % replicas.count()))
                    .webClient();
        }
        var configured = specs.stream().filter(spec -> !spec.ha()).findAny();
        return configured.orElseThrow(() -> new ArcadeClientConfigurationException("no webClient spec for replicas"))
                .webClient();
    }

}