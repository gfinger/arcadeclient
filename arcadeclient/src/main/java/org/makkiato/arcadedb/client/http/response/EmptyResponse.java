package org.makkiato.arcadedb.client.http.response;

import java.util.List;
import java.util.Map;

public record EmptyResponse(Map<String, List<String>> headers) implements Response {
}
