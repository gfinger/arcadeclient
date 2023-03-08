package org.makkiato.arcadedb.client;

import org.springframework.web.reactive.function.client.WebClient;

public interface WebClientSupplier {
    WebClient get();
}
