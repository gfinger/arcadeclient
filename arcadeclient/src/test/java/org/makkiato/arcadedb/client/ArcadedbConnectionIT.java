package org.makkiato.arcadedb.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.makkiato.arcadedb.client.exception.server.CommandExecutionException;
import org.makkiato.arcadedb.client.exception.server.DuplicatedKeyException;
import org.makkiato.arcadedb.client.exception.server.IllegalArgumentException;
import org.makkiato.arcadedb.client.exception.server.ParseException;
import org.makkiato.arcadedb.client.exception.server.SchemaException;
import org.makkiato.arcadedb.client.exception.server.ValidationException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

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
                assertThatThrownBy(() -> connection.command("create vertex type Customer").blockFirst())
                                .isInstanceOf(CommandExecutionException.class).hasMessageContaining("already exists");
                assertThat(logWatcher.list.stream()
                                .filter(event -> event.getLevel()
                                                .equals(Level.ERROR)
                                                && event.getFormattedMessage().contains("already exists")))
                                .hasSize(1);
        }

        @Test
        @Order(2)
        void wrongSqlSyntax() {
                assertThatThrownBy(() -> connection.command("create new vertex type Customer").blockFirst())
                                .isInstanceOf(ParseException.class).hasMessageContaining("Was expecting one of");
                assertThat(logWatcher.list.stream()
                                .filter(event -> event.getLevel()
                                                .equals(Level.ERROR)
                                                && event.getFormattedMessage().contains("Was expecting one of")))
                                .hasSize(1);
        }

        @Test
        @Order(3)
        void createProperty() {
                assertThat(connection.command("create property Customer.name String (mandatory true, notnull true)")
                                .blockFirst()).contains(entry("operation",
                                                "create property"), entry("typeName", "Customer"));
                assertThatThrownBy(() -> connection
                                .command("create property Customer.name String (mandatory true, notnull " +
                                                "true)")
                                .blockFirst())
                                .isInstanceOf(CommandExecutionException.class).hasMessageContaining("already exists");
                assertThat(logWatcher.list.stream()
                                .filter(event -> event.getLevel()
                                                .equals(Level.ERROR)
                                                && event.getFormattedMessage().contains("already exists")))
                                .hasSize(1);
        }

        @Test
        @Order(4)
        void createIndex() {
                assertThat(connection.command("create index on Customer (name) unique").blockFirst()).contains(entry(
                                "operation",
                                "create index"), entry("name", "Customer[name]"), entry("type", "LSM_TREE"),
                                entry("totalIndexed", 0));
        }

        @Test
        @Order(5)
        void insert() {
                assertThat(connection.command("insert into Customer set name = 'Tester'").blockFirst())
                                .contains(entry("@rid", "#1:0"), entry("@type", "Customer"), entry("@cat", "v"),
                                                entry("@out", 0),
                                                entry("@in", 0), entry("name", "Tester"));

                assertThatThrownBy(() -> connection.command("insert into Person set name = 'Tester'").blockFirst())
                                .isInstanceOf(SchemaException.class).hasMessageContaining("was not found");
                assertThat(logWatcher.list.stream()
                                .filter(event -> event.getLevel()
                                                .equals(Level.ERROR)
                                                && event.getFormattedMessage().contains("was not found")));

                assertThatThrownBy(() -> connection.command("insert into Customer set name = 'Tester'").blockFirst())
                                .isInstanceOf(DuplicatedKeyException.class).hasMessageContaining("Duplicated key");
                assertThat(logWatcher.list.stream()
                                .filter(event -> event.getLevel()
                                                .equals(Level.ERROR)
                                                && event.getFormattedMessage().contains("Duplicated key")));

                assertThatThrownBy(() -> connection.command("insert into Customer set name = null").blockFirst())
                                .isInstanceOf(ValidationException.class).hasMessageContaining("cannot be null");
                assertThat(logWatcher.list.stream()
                                .filter(event -> event.getLevel()
                                                .equals(Level.ERROR)
                                                && event.getFormattedMessage().contains("cannot be null")));
        }

        @Test
        @Order(6)
        void update() {
                assertThat(connection.command("update Customer set age = 25 where name = 'Tester'").blockFirst())
                                .containsEntry("count", 1);
        }

        @Test
        @Order(7)
        void insertWithParameters() {
                assertThat(connection.command("insert into Customer set name = :name", Map.of("name", "Cookie Factory"))
                                .blockFirst())
                                .contains(entry("@rid", "#7:0"), entry("@type", "Customer"), entry("@cat", "v"),
                                                entry("@out", 0),
                                                entry("@in", 0), entry("name", "Cookie Factory"));
        }

        @Test
        @Order(8)
        void insertObject() {
                var customer1 = new Customer();
                customer1.setName("ABC Electronics");
                assertThat(connection.insertObject("Customer", customer1).block())
                                .hasFieldOrPropertyWithValue("name", "ABC Electronics")
                                .matches(cu -> cu.getCat() != null && cu.getRid() != null
                                                && cu.getType().equals("Customer"), "no valid vertex");

                var customer2 = new Customer();
                customer2.setName("XYZ Electronics");
                assertThat(connection.insertObject(customer2).block())
                                .hasFieldOrPropertyWithValue("name", "XYZ Electronics")
                                .matches(cu -> cu.getCat() != null && cu.getRid() != null
                                                && cu.getType().equals("Customer"), "no valid vertex");
        }

        @Test
        @Order(9)
        void selectWithCommand() {
                assertThat(connection.command("select from Customer where name = 'Tester'").blockFirst())
                                .contains(entry("@rid", "#1:0"), entry("@type", "Customer"), entry("@cat", "v"),
                                                entry("name", "Tester"));

                assertThatThrownBy(() -> connection.command("select from Person where name = 'Tester'").blockFirst())
                                .isInstanceOf(SchemaException.class).hasMessageContaining("was not found");
                assertThat(logWatcher.list.stream()
                                .filter(event -> event.getLevel()
                                                .equals(Level.ERROR)
                                                && event.getFormattedMessage().contains("was not found")));
        }

        @Test
        @Order(10)
        void selectObject() {
                assertThat(connection.selectObject("select from Customer where name = 'Tester'",
                                Customer.class).blockFirst())
                                .has(new Condition<Customer>(customer -> customer.getName().equals("Tester"),
                                                "has name 'Tester'"))
                                .has(new Condition<Customer>(customer -> customer.getRid() != null, "has @rid"))
                                .has(new Condition<Customer>(customer -> customer.getCat().equals("v"), "has @v"))
                                .has(new Condition<Customer>(customer -> customer.getType().equals("Customer"),
                                                "has @type"))
                                .isInstanceOf(Customer.class);
        }

        @Test
        @Order(11)
        void selectWithQuery() {
                assertThat(connection.query("select from Customer where name = 'Tester'").blockFirst())
                                .contains(entry("@rid", "#1:0"), entry("@type", "Customer"), entry("@cat", "v"),
                                                entry("name", "Tester"));
                assertThatThrownBy(() -> connection.query("insert into Customer set name = 'Secondo'").blockFirst())
                                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @Order(12)
        void delete() {
                assertThat(connection.command("delete from Customer where name = 'Tester'").blockFirst())
                                .contains(entry("count", 1));
        }

        @Test
        @Order(13)
        void drop() {
                assertThat(connection.command("drop type Customer unsafe").blockFirst())
                                .contains(entry("operation", "drop type"), entry("typeName", "Customer"));
                assertThatThrownBy(() -> connection.command("drop type Customer").blockFirst())
                                .isInstanceOf(SchemaException.class).hasMessageContaining("was not found");
                assertThat(logWatcher.list.stream()
                                .filter(event -> event.getLevel()
                                                .equals(Level.ERROR)
                                                && event.getFormattedMessage().contains("was not found")));
        }

        @Test
        @Order(14)
        void script() {
                var script = new String[] {
                                "create vertex type Customer",
                                "create property Customer.name String (mandatory true, notnull true)",
                                "create index on Customer (name) unique",
                                "insert into Customer set name = 'Tester'",
                                "update Customer set age = 25 where name = 'Tester'",
                                "delete from Customer where name = :name",
                                "drop type Customer unsafe"
                };
                assertThat(connection.script(script, Map.of("name", "Tester")).block())
                                .isTrue();
        }
}
