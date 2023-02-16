package org.makkiato.arcadedb.client.http.response;

public record CommandErrorResponse(String error, String detail, String exception) implements Response {
}
