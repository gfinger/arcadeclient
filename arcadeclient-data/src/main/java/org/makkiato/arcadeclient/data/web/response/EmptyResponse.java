package org.makkiato.arcadeclient.data.web.response;

import java.util.List;
import java.util.Map;

public record EmptyResponse(Map<String, List<String>> headers) implements Response {
}
