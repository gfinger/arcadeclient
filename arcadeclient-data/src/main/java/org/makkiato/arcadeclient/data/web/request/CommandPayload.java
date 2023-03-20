package org.makkiato.arcadeclient.data.web.request;

import java.util.Map;

public record CommandPayload(String language, String command, Map<String, Object> params, String serializer) {
}
