package org.makkiato.arcadedb.client;

import org.junit.jupiter.api.Test;
import org.makkiato.arcadedb.client.exception.client.ArcadeClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(ArcadedbClientITConfiguration.class)
@TestPropertySource(properties = {
        "org.makkiato.arcadedb.connections.arcadedb0.host=localhost",
        "org.makkiato.arcadedb.connections.arcadedb0.port=2480",
        "org.makkiato.arcadedb.connections.arcadedb0.username=root",
        "org.makkiato.arcadedb.connections.arcadedb0.password=playwithdata",
        "org.makkiato.arcadedb.connections.arcadedb0.leader-preferred=true"
})
class ArcadedbClientIT {
    @Autowired
    private ArcadedbClient arcadedbClient;

    @Test
    void serverInfo() {
        var serverInfo = arcadedbClient.serverInfo("arcadedb0", "cluster");
        assertTrue(serverInfo.isPresent());
    }

    @Test
    void missingServerInfo() {
        var throwable = catchThrowable(() -> arcadedbClient.serverInfo("arcadedb1", "cluster"));
        assertThat(throwable).isInstanceOf(ArcadeClientException.class).hasMessage("Missing configuration for database: %s", "arcadedb1");
    }
}
