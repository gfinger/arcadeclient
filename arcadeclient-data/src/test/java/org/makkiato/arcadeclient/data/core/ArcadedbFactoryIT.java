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
import org.makkiato.arcadeclient.data.web.ArcadeclientErrorResponseFilterImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@SpringJUnitConfig(TestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ArcadedbFactoryIT {
    @Autowired
    private ConnectionProperties arcadedbProperties;
    @Autowired
    private WebClientSupplierFactory webClientFactory;

    private Arcadeclient arcadedbFactory;
    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeAll
    void init() {
        arcadedbFactory = new Arcadeclient(webClientFactory,
                arcadedbProperties.getConnectionPropertiesFor("default"));
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
        ((Logger) LoggerFactory.getLogger(ArcadeclientErrorResponseFilterImpl.class)).addAppender(logWatcher);
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