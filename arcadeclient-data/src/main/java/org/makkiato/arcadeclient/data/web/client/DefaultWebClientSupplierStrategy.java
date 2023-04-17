package org.makkiato.arcadeclient.data.web.client;

import java.util.List;

import org.makkiato.arcadeclient.data.exception.client.ArcadeClientConfigurationException;
import org.springframework.web.reactive.function.client.WebClient;

public class DefaultWebClientSupplierStrategy implements WebClientSupplierStrategy {

    @Override
    public WebClient apply(List<WebClientSpec> specs) {
        return specs.stream().filter(spec -> !spec.ha()).findAny()
                .orElseThrow(() -> new ArcadeClientConfigurationException("no webClient spec for default")).webClient();
    }

}