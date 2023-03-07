package org.makkiato.arcadedb.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.*;
import org.makkiato.arcadedb.client.exception.server.DatabaseOperationException;
import org.makkiato.arcadedb.client.exception.server.IllegalArgumentException;
import org.slf4j.LoggerFactory;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ArcadedbFactoryIT {
    private static final String DB_NAME = "xyz-factory-test";
    private static final String DB_NAME_NOT_EXISTS = "xyz-notexists-test";
    @Autowired
    private ArcadedbClient arcadedbClient;
    private ArcadedbFactory arcadedbFactory;
    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeAll
    void init() {
        arcadedbFactory = arcadedbClient.createFactoryFor("arcadedb0");
    }

    @BeforeEach
    void initEach() {
        logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(ArcadedbClient.class)).addAppender(logWatcher);
    }

    @Test
    @Order(1)
    void doesDbNotExist() {
        assertThat(arcadedbFactory.exists(DB_NAME).block()).isFalse();
        assertThat(arcadedbFactory.exists(DB_NAME_NOT_EXISTS).block()).isFalse();
    }

    @Test
    @Order(2)
    void createDb() {
        assertThat(arcadedbFactory.create(DB_NAME).hasElement().block()).isTrue();
        assertThatThrownBy(() -> arcadedbFactory.create(DB_NAME).block())
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
        assertThat(arcadedbFactory.exists(DB_NAME).block()).isTrue();
        assertThat(arcadedbFactory.exists(DB_NAME_NOT_EXISTS).block()).isFalse();
    }

    @Test
    @Order(4)
    void openDb() {
        assertThat(arcadedbFactory.open(DB_NAME).hasElement().block()).isTrue();
        assertThatThrownBy(() -> arcadedbFactory.open(DB_NAME_NOT_EXISTS).block())
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessageContaining("does not exist");
        assertThat(logWatcher.list.stream()
                .filter(event -> event.getLevel()
                        .equals(Level.ERROR) && event.getFormattedMessage().contains("does not exist")))
                .hasSize(1);
    }

    @Test
    @Order(5)
    void dropDb() {
        assertThat(arcadedbFactory.drop(DB_NAME).block()).isEqualTo("ok");
        assertThat(arcadedbFactory.exists(DB_NAME).block()).isFalse();
        assertThatThrownBy(() -> arcadedbFactory.drop(DB_NAME_NOT_EXISTS).block())
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessageContaining("does not exist");
        assertThat(logWatcher.list.stream()
                .filter(event -> event.getLevel()
                        .equals(Level.ERROR) && event.getFormattedMessage().contains("does not exist")))
                .hasSize(1);
    }
}