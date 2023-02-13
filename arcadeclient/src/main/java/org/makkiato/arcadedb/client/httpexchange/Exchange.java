package org.makkiato.arcadedb.client.httpexchange;

import reactor.core.publisher.Mono;

public interface Exchange<T extends Response> {
    Mono<T> exchange();
}
