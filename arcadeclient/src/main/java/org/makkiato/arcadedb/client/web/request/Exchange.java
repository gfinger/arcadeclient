package org.makkiato.arcadedb.client.web.request;

import org.makkiato.arcadedb.client.web.response.Response;

import reactor.core.publisher.Mono;

public interface Exchange<T extends Response> {
    String BASEURL_SERVER = "/server";
    String BASEURL_COMMAND = "/command";
    Object BASEURL_QUERY = "/query";
    String BASEURL_EXISTS = "/exists";
    String BASEURL_BEGIN = "/begin";
    String BASEURL_COMMIT = "/commit";
    String BASEURL_ROLLBACK = "/rollback";
    String SERIALIZER_JSON = "json";
    Object PARAMETER_MODE = "mode";

    Mono<T> exchange();
}