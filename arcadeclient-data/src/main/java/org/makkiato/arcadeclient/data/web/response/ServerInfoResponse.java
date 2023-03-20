package org.makkiato.arcadeclient.data.web.response;

public record ServerInfoResponse(String user, String version, String serverName,
                                 HighAvailabilityInfo ha) implements Response {
}
