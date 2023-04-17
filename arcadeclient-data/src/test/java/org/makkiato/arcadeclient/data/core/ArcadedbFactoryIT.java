package org.makkiato.arcadeclient.data.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.makkiato.arcadeclient.data.exception.server.IllegalArgumentException;
import org.makkiato.arcadeclient.data.web.ArcadedbErrorResponseFilterImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@SpringJUnitConfig(TestConfiguration.class)
@TestPropertySource(properties = {
        "org.makkiato.arcadedb.connections.default.host=localhost",
        "org.makkiato.arcadedb.connections.default.port=2480",
        "org.makkiato.arcadedb.connections.default.database=xyz-factory-test1",
        "org.makkiato.arcadedb.connections.default.username=root",
        "org.makkiato.arcadedb.connections.default.password=playwithdata",
        "org.makkiato.arcadedb.connections.default.leader-preferred=true",
        "org.makkiato.arcadedb.connections.arcadedb0.host=localhost",
        "org.makkiato.arcadedb.connections.arcadedb0.port=2480",
        "org.makkiato.arcadedb.connections.arcadedb0.database=xyz-factory-test2",
        "org.makkiato.arcadedb.connections.arcadedb0.username=root",
        "org.makkiato.arcadedb.connections.arcadedb0.password=playwithdata",
        "org.makkiato.arcadedb.connections.arcadedb0.leader-preferred=true"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ArcadedbFactoryIT {
    @Autowired
    private ArcadedbProperties arcadedbProperties;
    @Autowired
    private WebClientFactory webClientFactory;

    private ArcadedbFactory arcadedbFactory;
    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeAll
    void init() {
        arcadedbFactory = new ArcadedbFactory(webClientFactory,
                arcadedbProperties.getConnectionPropertiesFor("arcadedb0"));
    }

    @AfterAll
    void tearDown() {
        if (arcadedbFactory.exists().block()) {
            arcadedbFactory.drop().block();
        }
    }

    @BeforeEach
    void initEach() {
        logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(ArcadedbErrorResponseFilterImpl.class)).addAppender(logWatcher);
    }

    @Test
    @Order(1)
    void doesDbNotExist() {
        assertThat(arcadedbFactory.exists().block()).isFalse();

    }

    @Test
    @Order(2)
    void createDb() {
        assertThat(arcadedbFactory.create().hasElement().block()).isTrue();
        assertThatThrownBy(() -> arcadedbFactory.create().block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        assertThat(logWatcher.list.stream()
                .filter(event -> event.getLevel()
                        .equals(Level.ERROR) && event.getFormattedMessage().contains("already exists")))
                .hasSize(1);
    }

    @Test
    @Order(3)
    void doesDbExist() {
        assertThat(arcadedbFactory.exists().block()).isTrue();
    }

    @Test
    @Order(4)
    void openDb() {
        assertThat(arcadedbFactory.open().hasElement().block()).isTrue();
    }

    @Test
    @Order(5)
    void dropDb() {
        assertThat(arcadedbFactory.drop().block()).isTrue();
        assertThat(arcadedbFactory.exists().block()).isFalse();
    }
}