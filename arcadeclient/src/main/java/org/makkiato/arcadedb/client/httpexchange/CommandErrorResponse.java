package org.makkiato.arcadedb.client.httpexchange;

public record CommandErrorResponse(String error, String detail, String exception) implements Response {
}
