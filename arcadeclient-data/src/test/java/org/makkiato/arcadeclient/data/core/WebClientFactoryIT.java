package org.makkiato.arcadeclient.data.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.makkiato.arcadeclient.data.exception.client.ArcadeClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestConfiguration.class)
class WebClientFactoryIT {
    @Autowired
    private WebClientFactory arcadedbClient;
    @Autowired
    private ArcadedbProperties arcadedbProperties;

    @Test
    void serverInfo() {
        var serverInfo = arcadedbClient.serverInfo(arcadedbProperties.getConnectionPropertiesFor(null), "cluster");
        assertTrue(serverInfo.isPresent());
    }

    @Test
    void missingServerInfo() {
        var throwable = catchThrowable(
                () -> arcadedbClient.serverInfo(arcadedbProperties.getConnectionPropertiesFor("arcadedb"), "cluster"));
        assertThat(throwable).isInstanceOf(ArcadeClientException.class)
                .hasMessage("Missing configuration for database: %s", "arcadedb");
    }
}
