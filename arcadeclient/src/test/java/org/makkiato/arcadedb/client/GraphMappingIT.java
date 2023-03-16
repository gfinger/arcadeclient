package org.makkiato.arcadedb.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.makkiato.arcadedb.client.web.ArcadedbErrorResponseFilterImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@SpringJUnitConfig(ArcadedbAutoConfiguration.class)
@TestPropertySource(properties = {
        "org.makkiato.arcadedb.connections.arcadedb0.host=localhost",
        "org.makkiato.arcadedb.connections.arcadedb0.port=2480",
        "org.makkiato.arcadedb.connections.arcadedb0.username=root",
        "org.makkiato.arcadedb.connections.arcadedb0.password=playwithdata",
        "org.makkiato.arcadedb.connections.arcadedb0.leader-preferred=true"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GraphMappingIT {
    private static final String DB_NAME = "xyz-graphql-test";
    @Autowired
    private ArcadedbFactory arcadedbFactory;

    @Value("classpath:types.graphql")
    private Resource graphqlscript;

    private ArcadedbConnection connection;
    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeAll
    void init() {
        arcadedbFactory.create(DB_NAME).block();
        connection = arcadedbFactory.open(DB_NAME).block();

        var person = Person.builder().name("Thomas Mann").build();
        var book = Book.builder().title("Der Zauberberg").build();
        connection.command("create vertex type Person").blockFirst();
        connection.command("create vertex type Book").blockFirst();
        connection.command("create edge type AuthorOf").blockFirst();
        person = connection.insertObject(person).block();
        book = connection.insertObject(book).block();
        connection.command(String.format("create edge AuthorOf from %s to %s", person.getRid(), book.getRid()))
                .blockFirst();
    }

    @BeforeEach
    void initEach() {
        logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(ArcadedbErrorResponseFilterImpl.class)).addAppender(logWatcher);
    }

    @AfterAll
    void tearDown() {
        arcadedbFactory.drop(DB_NAME).block();
    }

    @Test
    @Order(1)
    void sqlscript() throws IOException {
        assertThat(connection.script("graphql", graphqlscript, null).block())
                .isTrue();
    }

    @Test
    @Order(2)
    void selectObject() {
        assertThat(connection.selectObject("graphql", "{bookByTitle(title:\"Der Zauberberg\")}",
                Book.class).blockFirst())
                .has(new Condition<Book>(book -> book.getTitle().equals("Der Zauberberg"),
                        "has title 'Der Zauberberg'"))
                .has(new Condition<Book>(
                        book -> book.getAuthors().length == 1 && book.getAuthors()[0].getName().equals("Thomas Mann"),
                        "has author 'Thomas Mann'"))
                .isInstanceOf(Book.class);

        assertThat(connection.selectObject("graphql", "{bookByAuthor(name:\"Thomas Mann\")}",
                Book.class).blockFirst())
                .has(new Condition<Book>(book -> book.getTitle().equals("Der Zauberberg"),
                        "has title 'Der Zauberberg'"))
                .has(new Condition<Book>(
                        book -> book.getAuthors().length == 1 && book.getAuthors()[0].getName().equals("Thomas Mann"),
                        "has author 'Thomas Mann'"))
                .isInstanceOf(Book.class);
    }
}
