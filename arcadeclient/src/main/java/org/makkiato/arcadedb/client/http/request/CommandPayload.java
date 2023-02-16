package org.makkiato.arcadedb.client.http.request;

public record CommandPayload(String language, String command, String serializer) {
}
