package org.makkiato.arcadedb.client.http.response;

public record HighAvailabilityInfo(String clustername, String leader, String electionStatus, String leaderAddress,
                                   String replicaAddresses) {
}
