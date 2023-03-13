package org.makkiato.arcadedb.client.web.request;

import java.util.Map;

public record CommandPayload(String language, String command, Map<String, Object> params, String serializer) {
}
