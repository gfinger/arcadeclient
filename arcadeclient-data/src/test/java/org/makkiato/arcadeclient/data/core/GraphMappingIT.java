package org.makkiato.arcadeclient.data.core;

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
import org.makkiato.arcadeclient.data.web.ArcadedbErrorResponseFilterImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@SpringJUnitConfig(TestConfiguration.class)
@TestPropertySource(properties = {
        "org.makkiato.arcadedb.connections.arcadedb0.host=localhost",
        "org.makkiato.arcadedb.connections.arcadedb0.port=2480",
        "org.makkiato.arcadedb.connections.arcadedb0.database=xyz-graphql-test",
        "org.makkiato.arcadedb.connections.arcadedb0.username=root",
        "org.makkiato.arcadedb.connections.arcadedb0.password=playwithdata",
        "org.makkiato.arcadedb.connections.arcadedb0.leader-preferred=true"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GraphMappingIT {
    @Autowired
    private ArcadedbFactory arcadedbFactory;
    @Autowired
    private ArcadedbConnection connection;

    @Value("classpath:types.graphqls")
    private Resource graphqlscript;

    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeAll
    void init() {
        var person = Person.builder().name("Thomas Mann").build();
        var book = Book.builder().title("Der Zauberberg").build();

        var createDb = arcadedbFactory.create();
        var createPerson = connection.command("create vertex type Person");
        var createBook = connection.command("create vertex type Book");
        var createEdge = connection.command("create edge type AuthorOf");
        var insertPerson = connection.insertObject(person);
        var insertBook = connection.insertObject(book);

        createDb.then(
                createPerson.zipWith(createBook).then(
                        createEdge.then(
                                insertPerson.zipWith(insertBook).flatMap(
                                        tuple -> connection
                                                .command(String.format("create edge AuthorOf from %s to %s",
                                                        tuple.getT1().getRid(),
                                                        tuple.getT2().getRid()))
                                                .then()))))
                .block();
    }

    @BeforeEach
    void initEach() {
        logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(ArcadedbErrorResponseFilterImpl.class)).addAppender(logWatcher);
    }

    @AfterAll
    void tearDown() {
        arcadedbFactory.drop().block();
    }

    @Test
    @Order(1)
    void sqlscript() throws IOException {
        assertThat(connection.script("graphql", graphqlscript, null, null).block())
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
                .has(new Condition<Book>(book -> book.getRid() != null, "has @rid"))
                .has(new Condition<Book>(book -> book.getCat().equals("v"), "has @v"))
                .has(new Condition<Book>(book -> book.getType().equals("Book"),
                        "has @type"))
                .isInstanceOf(Book.class);

        assertThat(connection.selectObject("graphql", "{bookByAuthor(name:\"Thomas Mann\")}",
                Book.class).blockFirst())
                .has(new Condition<Book>(book -> book.getTitle().equals("Der Zauberberg"),
                        "has title 'Der Zauberberg'"))
                .has(new Condition<Book>(
                        book -> book.getAuthors().length == 1 && book.getAuthors()[0].getName().equals("Thomas Mann"),
                        "has author 'Thomas Mann'"))
                .has(new Condition<Book>(book -> book.getRid() != null, "has @rid"))
                .has(new Condition<Book>(book -> book.getCat().equals("v"), "has @v"))
                .has(new Condition<Book>(book -> book.getType().equals("Book"),
                        "has @type"))
                .isInstanceOf(Book.class);
    }
}
