package org.makkiato.arcadeclient.data.operations;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.makkiato.arcadeclient.data.core.Arcadeclient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringJUnitConfig(TestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(properties = {
        "org.makkiato.arcadedb.connection.database=test-edge-operations"
})
public class EdgeOperationsIT {
    @Autowired
    private Arcadeclient arcadedbFactory;
    @Autowired
    private ArcadedbOperations operations;

    @BeforeAll
    void init() {
        Flux.concat(
                arcadedbFactory.exists().flatMap(exists -> {
                    if (exists) {
                        return arcadedbFactory.drop();
                    }
                    return Mono.just(true);
                }).flatMap(ok -> {
                    if (ok) {
                        return arcadedbFactory.create();
                    }
                    return Mono.just(false);
                }),
                Flux.merge(
                        operations.command("create vertex type Kunde"),
                        operations.command("create vertex type Person"),
                        operations.command("create document type Address"),
                        operations.command("create edge type IsContactOf")),
                Flux.zip(
                        operations.command("""
                                create vertex Kunde content
                                    {
                                        "name": "Happy Garden",
                                        "address": {
                                            "street": "Flower Road",
                                            "@type": "Address"
                                        }
                                    }
                                """)
                                .map(customer -> customer.get("@rid")).cast(String.class),
                        operations.command("insert into Person set name = 'Clint'")
                                .map(person -> person.get("@rid")).cast(String.class),
                        operations.command("insert into Person set name = 'Robert'")
                                .map(person -> person.get("@rid")).cast(String.class))
                        .flatMap(tuple -> {
                            return Flux.concat(
                                    operations.command(String.format("create edge IsContactOf from %s to %s",
                                            tuple.getT2(), tuple.getT1())),
                                    operations.command(String.format("create edge IsContactOf from %s to %s",
                                            tuple.getT3(), tuple.getT1())));
                        }))
                .blockLast();
    }

    @Nested
    class VertexTests {
        @Test
        void outVertices() {
            StepVerifier
                    .create(operations.findAll(Person.class)
                            .flatMap(person -> operations.outVertices(person)).collectList())
                    .expectNextMatches(customers -> customers.stream()
                            .allMatch(customer -> customer.getRid() != null && customer.getType().equals("Kunde")
                                    && customer instanceof Customer
                                    && ((Customer) customer).getName().equals("Happy Garden")))
                    .verifyComplete();
        }

        @Test
        void outVerticesWithEdgeType() {
            StepVerifier
                    .create(operations.findAll(Person.class)
                            .flatMap(person -> operations.outVertices(person, IsContactOf.class)).collectList())
                    .expectNextMatches(customers -> customers.stream()
                            .allMatch(customer -> customer.getRid() != null && customer.getType().equals("Kunde")
                                    && customer instanceof Customer
                                    && ((Customer) customer).getName().equals("Happy Garden")))
                    .verifyComplete();
        }
    }

    @Nested
    class IdTests {
        @Test
        void outVertexIds() {
            StepVerifier.create(operations.outVertexIds("Person").collectList())
                    .expectNextMatches(ids -> ids.stream()
                            .allMatch(id -> id != null))
                    .verifyComplete();
        }
    }

    @AfterAll
    void tearDown() {
        arcadedbFactory.drop().block();
    }

}
