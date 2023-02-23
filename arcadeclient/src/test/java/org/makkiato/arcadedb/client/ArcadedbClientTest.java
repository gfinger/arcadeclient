package org.makkiato.arcadedb.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.makkiato.arcadedb.client.exception.client.ArcadeClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(ArcadedbClientITConfiguration.class)
@TestPropertySource(properties = {
        "org.makkiato.arcadedb.connections.mock.host=localhost",
        "org.makkiato.arcadedb.connections.mock.port=2480",
        "org.makkiato.arcadedb.connections.mock.username=root",
        "org.makkiato.arcadedb.connections.mock.password=playwithdata",
        "org.makkiato.arcadedb.connections.mock.leader-preferred=true"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArcadedbClientTest {
    private static MockWebServer mockWebServer;
    private static final String serverInfoResponse = """
                {
                "user": "root",
                "version": "23.2.1-SNAPSHOT (build 3d7e8e5403661e04f1482cdd78f548b285970a5b/1676623050709/main)",
                "serverName": "ArcadeDB_0",
                "ha": {
                    "clusterName": "arcadedb",
                    "leader": "ArcadeDB_0",
                    "electionStatus": "LEADER_WAITING_FOR_QUORUM",
                    "network": {
                        "current": {
                            "name": "ArcadeDB_0",
                            "address": "0481adfc6dff:2424",
                            "role": "Leader",
                            "status": "ONLINE",
                            "joinedOn": "2023-02-17 08:39:22"
                        },
                        "replicas": []
                    },
                    "databases": [
                        {
                            "name": "testdb",
                            "quorum": "MAJORITY"
                        }
                    ],
                    "leaderAddress": "0.0.0.0:2480",
                    "replicaAddresses": ""
                }
            }
            """;
    @Autowired
    private ArcadedbClient arcadedbClient;
    @Autowired
    private ArcadedbProperties properties;

    @BeforeEach
    void init() throws IOException {
        mockWebServer = new MockWebServer();

        var mockProperties = properties.getConnections().get("mock");
        mockProperties.setHost(mockWebServer.getHostName());
        mockProperties.setPort(mockWebServer.getPort());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void serverInfo() {
        mockWebServer.enqueue(new MockResponse()
                .setBody(serverInfoResponse)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .setResponseCode(HttpStatus.OK.value()));
        var serverInfo = arcadedbClient.serverInfo("mock", "cluster");
        assertTrue(serverInfo.isPresent());
    }

    @Test
    void missingServerInfo() {
        var throwable = catchThrowable(() -> arcadedbClient.serverInfo("mock1", "cluster"));
        assertThat(throwable).isInstanceOf(ArcadeClientException.class).hasMessage("Missing configuration for " +
                "database: %s", "mock1");
    }
}