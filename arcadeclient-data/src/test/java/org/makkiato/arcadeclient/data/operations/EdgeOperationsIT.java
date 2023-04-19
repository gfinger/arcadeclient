package org.makkiato.arcadeclient.data.operations;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.makkiato.arcadeclient.data.core.ArcadedbFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import reactor.test.StepVerifier;

@SpringJUnitConfig(TestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EdgeOperationsIT {
    @Autowired
    private ArcadedbFactory arcadedbFactory;
    @Autowired
    private ArcadedbTemplate template;

    @BeforeAll
    void init() {
        arcadedbFactory.create().block();
        template.command("create vertex type Kunde").blockFirst();
        template.command("create vertex type Person").blockFirst();
        template.command("create edge type IsContactOf if not exists").blockFirst();
        var customerRid = (String) template.command("insert into Kunde set name = 'Flower Power'").blockFirst().get(
                "@rid");
        var personRid = (String) template.command("insert into Person set name = 'Clint'").blockFirst().get("@rid");
        template.findById(personRid, Person.class).zipWith(template.findById(customerRid, Customer.class))
                .flatMap(tuple -> template.createEdge(tuple.getT1(), tuple.getT2(), IsContactOf.class)).block();
    }

    @Test
    void outVertices() {
        StepVerifier.create(template.findAll(Person.class).flatMap(person -> template.outVertices(person)).collectList())
                .expectNextMatches(customers -> customers.stream()
                        .allMatch(customer -> customer.getRid() != null && customer.getType().equals("Kunde") && customer instanceof Customer))
                .verifyComplete();
    }

    @Test
    void outVerticesWithEdgeType() {
        StepVerifier.create(template.findAll(Person.class).flatMap(person -> template.outVertices(person, IsContactOf.class)).collectList())
                .expectNextMatches(customers -> customers.stream()
                        .allMatch(customer -> customer.getRid() != null && customer.getType().equals("Kunde") && customer instanceof Customer))
                .verifyComplete();
    }

    @AfterAll
    void tearDown() {
        arcadedbFactory.drop().block();
    }

}
