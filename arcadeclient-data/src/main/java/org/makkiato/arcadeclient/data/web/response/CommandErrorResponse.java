package org.makkiato.arcadeclient.data.web.response;

public record CommandErrorResponse(String error, String detail, String exception) implements Response {
}
