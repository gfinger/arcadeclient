package org.makkiato.arcadedb.client;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.*;


@SpringJUnitConfig(ArcadedbClientITConfiguration.class)
@TestPropertySource(properties = {
        "org.makkiato.arcadedb.connections.arcadedb0.host=localhost",
        "org.makkiato.arcadedb.connections.arcadedb0.port=2480",
        "org.makkiato.arcadedb.connections.arcadedb0.username=root",
        "org.makkiato.arcadedb.connections.arcadedb0.password=playwithdata",
        "org.makkiato.arcadedb.connections.arcadedb0.leader-preferred=true"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArcadedbFactoryIT {
    @Autowired
    private ArcadedbClient arcadedbClient;
    private ArcadedbFactory arcadedbFactory;

    @BeforeAll
    void init() {
        arcadedbFactory = arcadedbClient.createFactoryFor("arcadedb0");
    }

    @Test
    void doesDbExist() {
        var exists = arcadedbFactory.exists("testdb").block();
        assertThat(exists).isTrue();
    }

    @Test
    void doesDbNotExist() {
        var exists = arcadedbFactory.exists("notexistentdb").block();
        assertThat(exists).isFalse();
    }
}