package org.makkiato.arcadedb.client;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.makkiato.arcadedb.client.exception.client.ArcadeClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(ArcadedbAutoConfiguration.class)
@TestPropertySource(properties = {
    "org.makkiato.arcadedb.connections.default.host=localhost",
    "org.makkiato.arcadedb.connections.default.port=2480",
    "org.makkiato.arcadedb.connections.default.username=root",
    "org.makkiato.arcadedb.connections.default.password=playwithdata",
    "org.makkiato.arcadedb.connections.default.leader-preferred=true",
    "org.makkiato.arcadedb.connections.default.database=xyz-webclientfactory-test"
})
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
