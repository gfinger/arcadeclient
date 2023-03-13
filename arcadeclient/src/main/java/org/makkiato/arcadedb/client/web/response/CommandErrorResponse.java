package org.makkiato.arcadedb.client.web.response;

public record CommandErrorResponse(String error, String detail, String exception) implements Response {
}
