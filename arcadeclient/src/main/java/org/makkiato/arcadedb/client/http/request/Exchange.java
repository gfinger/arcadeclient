package org.makkiato.arcadedb.client.http.request;

import org.makkiato.arcadedb.client.http.response.Response;
import reactor.core.publisher.Mono;

public interface Exchange<T extends Response> {
    String BASEURL_SERVER = "/server";
    String BASEURL_COMMAND = "/command";
    String BASEURL_EXISTS = "/exists";
    String SERIALIZER_JSON = "json";
    Object PARAMETER_MODE = "mode";

    Mono<T> exchange();
}
