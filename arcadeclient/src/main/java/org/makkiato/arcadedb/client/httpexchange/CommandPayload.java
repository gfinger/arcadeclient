package org.makkiato.arcadedb.client.httpexchange;

public record CommandPayload(String language, String command, String serializer) {
}
