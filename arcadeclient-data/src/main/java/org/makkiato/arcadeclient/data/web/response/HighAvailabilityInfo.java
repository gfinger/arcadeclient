package org.makkiato.arcadeclient.data.web.response;

public record HighAvailabilityInfo(String clustername, String leader, String electionStatus, String leaderAddress,
                                   String replicaAddresses) {
}
