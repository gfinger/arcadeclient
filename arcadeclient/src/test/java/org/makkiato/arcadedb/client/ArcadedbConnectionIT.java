package org.makkiato.arcadedb.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.*;
import org.makkiato.arcadedb.client.exception.server.ParseException;
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
public class ArcadedbConnectionIT {
    private static final String DB_NAME = "xyz-connection-test";
    @Autowired
    private ArcadedbClient arcadedbClient;
    private ArcadedbFactory arcadedbFactory;
    private ArcadedbConnection connection;
    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeAll
    void init() {
        arcadedbFactory = arcadedbClient.createFactoryFor("arcadedb0");
        arcadedbFactory.create(DB_NAME).block();
    }

    @BeforeEach
    void initEach() {
        connection = arcadedbFactory.open(DB_NAME).block();
        logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(ArcadedbClient.class)).addAppender(logWatcher);
    }

    @AfterAll
    void tearDown() {
        arcadedbFactory.drop(DB_NAME).block();
    }

    @Test
    @Order(1)
    void createVertexType() {
        assertThat(connection.command("create vertex type Customer").blockFirst()).contains(entry("operation",
                "create vertex type"), entry("typeName", "Customer"));
    }

    @Test
    @Order(2)
    void wrongSqlSyntax() {
        assertThatThrownBy(() -> connection.command("create new vertex type Customer").blockFirst())
                .isInstanceOf(ParseException.class).hasMessageContaining("Was expecting one of");
        assertThat(logWatcher.list.stream()
                .filter(event -> event.getLevel()
                        .equals(Level.ERROR) && event.getFormattedMessage().contains("Was expecting one of")))
                .hasSize(1);
    }

}
