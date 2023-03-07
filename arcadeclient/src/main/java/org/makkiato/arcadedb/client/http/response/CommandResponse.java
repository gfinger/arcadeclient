package org.makkiato.arcadedb.client.http.response;

import java.util.Map;

public record CommandResponse(String user, String version, String serverName, Map<String, Object>[] result) implements Response {
}
