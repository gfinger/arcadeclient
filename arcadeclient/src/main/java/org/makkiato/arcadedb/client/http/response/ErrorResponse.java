package org.makkiato.arcadedb.client.http.response;

public record ErrorResponse(String error, String detail, String exception, String exceptionArgs) {
}
