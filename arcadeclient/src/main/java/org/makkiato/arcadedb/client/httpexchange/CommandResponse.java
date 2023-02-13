package org.makkiato.arcadedb.client.httpexchange;

import java.util.Map;

public record CommandResponse(String user, String version, String serverName, Map<String, String>[] result) implements Response {
}
