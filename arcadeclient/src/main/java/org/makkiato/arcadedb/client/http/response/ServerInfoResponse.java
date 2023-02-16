package org.makkiato.arcadedb.client.http.response;

public record ServerInfoResponse(String user, String version, String serverName,
                                 HighAvailabilityInfo ha) implements Response {
}
