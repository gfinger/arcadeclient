package org.makkiato.arcadeclient.data.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.io.IOException;
import java.util.ArrayList;
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
import org.makkiato.arcadeclient.data.core.ArcadedbFactory;
import org.makkiato.arcadeclient.data.exception.server.CommandExecutionException;
import org.makkiato.arcadeclient.data.exception.server.DuplicatedKeyException;
import org.makkiato.arcadeclient.data.exception.server.IllegalArgumentException;
import org.makkiato.arcadeclient.data.exception.server.ParseException;
import org.makkiato.arcadeclient.data.exception.server.SchemaException;
import org.makkiato.arcadeclient.data.exception.server.ValidationException;
import org.makkiato.arcadeclient.data.web.ArcadedbErrorResponseFilterImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringJUnitConfig(TestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArcadedbTemplateIT {
    @Autowired
    private ArcadedbFactory arcadedbFactory;

    @Autowired
    private ArcadedbTemplate template;

    @Value("classpath:schema.sql")
    private Resource sqlscript;

    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeAll
    void init() {
        arcadedbFactory.create().block();
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
    void createVertexType() {
        assertThat(template.command("create vertex type Customer").blockFirst()).contains(entry("operation",
                "create vertex type"), entry("typeName", "Customer"));
        assertThatThrownBy(() -> template.command("create vertex type Customer").blockFirst())
                .isInstanceOf(CommandExecutionException.class).hasMessageContaining("already exists");
        assertThat(logWatcher.list.stream()
                .filter(event -> event.getLevel()
                        .equals(Level.ERROR)
                        && event.getFormattedMessage().contains("already exists")))
                .hasSize(1);

        assertThat(template.command("create vertex type Kunde").blockFirst()).contains(entry("operation",
                "create vertex type"), entry("typeName", "Kunde"));
        assertThat(template.command("create document type Address").blockFirst()).contains(entry("operation",
                "create document type"), entry("typeName", "Address"));
    }

    @Test
    @Order(2)
    void wrongSqlSyntax() {
        assertThatThrownBy(() -> template.command("create new vertex type Customer").blockFirst())
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
        assertThat(template.command("create property Customer.name String (mandatory true, notnull true)")
                .blockFirst()).contains(entry("operation",
                        "create property"), entry("typeName", "Customer"));
        assertThatThrownBy(() -> template
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
        assertThat(template.command("create index on Customer (name) unique").blockFirst()).contains(entry(
                "operation",
                "create index"), entry("name", "Customer[name]"), entry("type", "LSM_TREE"),
                entry("totalIndexed", 0));
    }

    @Test
    @Order(5)
    void insert() {
        assertThat(template.command("insert into Customer set name = 'Tester'").blockFirst())
                .contains(entry("@rid", "#1:0"), entry("@type", "Customer"), entry("@cat", "v"),
                        entry("@out", 0),
                        entry("@in", 0), entry("name", "Tester"));

        assertThatThrownBy(() -> template.command("insert into Person set name = 'Tester'").blockFirst())
                .isInstanceOf(SchemaException.class).hasMessageContaining("was not found");
        assertThat(logWatcher.list.stream()
                .filter(event -> event.getLevel()
                        .equals(Level.ERROR)
                        && event.getFormattedMessage().contains("was not found")));

        assertThatThrownBy(() -> template.command("insert into Customer set name = 'Tester'").blockFirst())
                .isInstanceOf(DuplicatedKeyException.class).hasMessageContaining("Duplicated key");
        assertThat(logWatcher.list.stream()
                .filter(event -> event.getLevel()
                        .equals(Level.ERROR)
                        && event.getFormattedMessage().contains("Duplicated key")));

        assertThatThrownBy(() -> template.command("insert into Customer set name = null").blockFirst())
                .isInstanceOf(ValidationException.class).hasMessageContaining("cannot be null");
        assertThat(logWatcher.list.stream()
                .filter(event -> event.getLevel()
                        .equals(Level.ERROR)
                        && event.getFormattedMessage().contains("cannot be null")));
    }

    @Test
    @Order(6)
    void update() {
        assertThat(template.command("update Customer set age = 25 where name = 'Tester'").blockFirst())
                .containsEntry("count", 1);
    }

    @Test
    @Order(7)
    void insertWithParameters() {
        assertThat(template.command("insert into Customer set name = :name", Map.of("name", "Cookie Factory"))
                .blockFirst())
                .contains(entry("@rid", "#7:0"), entry("@type", "Customer"), entry("@cat", "v"),
                        entry("@out", 0),
                        entry("@in", 0), entry("name", "Cookie Factory"));
    }

    @Test
    @Order(8)
    void insertObject() {
        var address = Address.builder().town("Frankfurt a.M.").zipcode("60596").street("Städelstraße").build();
        var customer1 = new Customer();
        customer1.setAddress(address);
        customer1.setName("ABC Electronics");
        assertThat(template.insert("Kunde", template.convertObjectToJsonString(customer1)).block())
                .hasFieldOrPropertyWithValue("name", "ABC Electronics")
                .matches(cu -> cu.get("@cat") != null && cu.get("@rid") != null
                        && cu.get("@type").equals("Kunde"), "no valid vertex")
                .matches(cu -> cu.get("address") != null
                        && ((Map) cu.get("address")).get("street").equals("Städelstraße"));

        var customer2 = new Customer();
        customer2.setAddress(address);
        customer2.setName("XYZ Electronics");
        assertThat(template.insertDocument(customer2).block())
                .hasFieldOrPropertyWithValue("name", "XYZ Electronics")
                .matches(cu -> cu.getCat() != null && cu.getRid() != null
                        && cu.getType().equals("Kunde"), "no valid vertex")
                .matches(cu -> cu.getAddress() != null
                        && cu.getAddress().getStreet().equals("Städelstraße"));
    }

    @Test
    @Order(10)
    void updateObject() {
        var address = Address.builder().town("Frankfurt a.M.").zipcode("60596").street("Städelstraße").build();
        var customer = new Customer();
        customer.setAddress(address);
        customer.setName("123 Electronics");
        customer = template.insertDocument(customer).block();
        customer.setName("456 Electronics");
        assertThat(template.updateDocument(customer).block())
                .hasFieldOrPropertyWithValue("name", "456 Electronics");
        assertThat(template.findById(customer.getRid(), Customer.class).block())
                .hasFieldOrPropertyWithValue("name", "456 Electronics");
    }

    @Test
    @Order(11)
    void mergeObject() {
        var address = Address.builder().town("Frankfurt a.M.").zipcode("60596").street("Städelstraße").build();
        var customer = new Customer();
        customer.setAddress(address);
        customer.setName("123 BredAndBretzels");
        customer = template.mergeDocument(customer).block();
        customer.setPhone("1234567890");
        assertThat(template.mergeDocument(customer).block())
                .hasFieldOrPropertyWithValue("phone", "1234567890")
                .hasFieldOrPropertyWithValue("name", "123 BredAndBretzels")
                .hasFieldOrPropertyWithValue("address", address);
        assertThat(template.findById(customer.getRid(), Customer.class).block())
                .hasFieldOrPropertyWithValue("name", "123 BredAndBretzels")
                .hasFieldOrPropertyWithValue("phone", "1234567890");
    }

    @Test
    @Order(12)
    void selectWithCommand() {
        assertThat(template.command("select from Customer where name = 'Tester'").blockFirst())
                .contains(entry("@rid", "#1:0"), entry("@type", "Customer"), entry("@cat", "v"),
                        entry("name", "Tester"));

        assertThatThrownBy(() -> template.command("select from Person where name = 'Tester'").blockFirst())
                .isInstanceOf(SchemaException.class).hasMessageContaining("was not found");
        assertThat(logWatcher.list.stream()
                .filter(event -> event.getLevel()
                        .equals(Level.ERROR)
                        && event.getFormattedMessage().contains("was not found")));
    }

    @Test
    @Order(13)
    void selectObject() {
        assertThat(template.select("select from Customer where name = 'Tester'",
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
    @Order(14)
    void selectWithQuery() {
        assertThat(template.query("select from Customer where name = 'Tester'").blockFirst())
                .contains(entry("@rid", "#1:0"), entry("@type", "Customer"), entry("@cat", "v"),
                        entry("name", "Tester"));
        assertThatThrownBy(() -> template.query("insert into Customer set name = 'Secondo'").blockFirst())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Order(15)
    void delete() {
        assertThat(template.command("delete from Customer where name = 'Tester'").blockFirst())
                .contains(entry("count", 1));
    }

    @Test
    @Order(16)
    void drop() {
        assertThat(template.command("drop type Customer unsafe").blockFirst())
                .contains(entry("operation", "drop type"), entry("typeName", "Customer"));
        assertThatThrownBy(() -> template.command("drop type Customer").blockFirst())
                .isInstanceOf(SchemaException.class).hasMessageContaining("was not found");
        assertThat(logWatcher.list.stream()
                .filter(event -> event.getLevel()
                        .equals(Level.ERROR)
                        && event.getFormattedMessage().contains("was not found")));

        assertThat(template.command("drop type Kunde unsafe").blockFirst())
                .contains(entry("operation", "drop type"), entry("typeName", "Kunde"));
    }

    @Test
    @Order(17)
    void script() {
        var script = new String[] {
                "create vertex type Customer",
                "create property Customer.name String (mandatory true, notnull true)",
                "create index on Customer (name) unique",
                "insert into Customer set name = 'Tester'"
        };
        assertThat(template.script(script, Map.of("name", "Tester")).block())
                .isTrue();
        assertThat(template.select("select from Customer where name = 'Tester'",
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
    @Order(18)
    void sqlscript() throws IOException {
        assertThat(template.script(sqlscript).block())
                .isTrue();
        assertThat(template.query("select from Person where name = 'Josh Long'").blockFirst())
                .containsKey("@rid")
                .contains(entry("@type", "Person"), entry("@cat", "v"),
                        entry("name", "Josh Long"));

    }

    @Test
    @Order(19)
    void transactional() throws Exception {
        try (var taConnection = template.transactional()) {
            StepVerifier
                    .create(taConnection.command("create vertex type Customer if not exists")
                            .concatWith(taConnection.command("drop type Customer unsafe"))
                            .concatWith(taConnection.command("create vertex type Customer"))
                            .concatWith(taConnection.command("insert into Customer set name = 'Tester'")))
                    .expectNextMatches(result -> result.entrySet().contains(entry("operation", "create vertex type")))
                    .expectNextMatches(result -> result.entrySet().contains(entry("operation", "drop type")))
                    .expectNextMatches(result -> result.entrySet().contains(entry("operation", "create vertex type"))
                            && result.entrySet().contains(entry("typeName", "Customer")))
                    .expectNextMatches(result -> result.entrySet().contains(entry("name", "Tester"))
                            && result.entrySet().contains(entry("@type", "Customer")))
                    .verifyComplete();
        }
    }
}
