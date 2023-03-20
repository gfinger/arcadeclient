package org.makkiato.arcadeclient.data.web.client;

import java.util.List;

import org.springframework.web.reactive.function.client.WebClient;

public class WebClientSupplier {
    private final WebClientSupplierStrategy strategy;
    private final List<WebClientSpec> specs;

    public WebClientSupplier(WebClientSupplierStrategy strategy, List<WebClientSpec> specs) {
        this.strategy = strategy;
        this.specs = specs;
    }

    public WebClient get() {
        return strategy.apply(specs);
    }
}
