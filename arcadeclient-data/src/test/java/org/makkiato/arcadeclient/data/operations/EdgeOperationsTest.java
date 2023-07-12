package org.makkiato.arcadeclient.data.operations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.makkiato.arcadeclient.data.web.request.CommandExchange;
import org.makkiato.arcadeclient.data.web.request.ExchangeFactory;
import org.makkiato.arcadeclient.data.web.response.CommandResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringJUnitConfig(OperationsTestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EdgeOperationsTest {
    static final String VERTEX_RESULT_STRING = """
            [
                {
                    "item": [
                        {
                            "address": {
                                "street": "Flower Road",
                                "@cat": "d",
                                "@type": "Address"
                            },
                            "name": "Happy Garden",
                            "@cat": "v",
                            "@type": "Kunde",
                            "@rid": "#21:0"
                        }
                    ]
                },
                {
                    "item": [
                        {
                            "address": {
                                "street": "Flower Road",
                                "@cat": "d",
                                "@type": "Address"
                            },
                            "name": "Happy Garden",
                            "@cat": "v",
                            "@type": "Kunde",
                            "@rid": "#21:0"
                        }
                    ]
                }
            ]
                """;

    static final String ID_RESULT_STRING = """
            [
                {
                    "item": "#9:0"
                },
                {
                    "item": "#9:0"
                }
            ]
                """;

    static final String EDGE_RESULT_STRING = """
            [
                {
                    "item": [
                        {
                            "@cat": "e",
                            "@type": "IsContactOf",
                            "@rid": "#57:0",
                            "@in": "#9:0",
                            "@out": "#33:0"
                        }
                    ]
                },
                {
                    "item": [
                        {
                            "@cat": "e",
                            "@type": "IsContactOf",
                            "@rid": "#58:0",
                            "@in": "#9:0",
                            "@out": "#36:0"
                        }
                    ]
                }
            ]
                """;

    @MockBean
    CommandExchange commandExchange;

    @MockBean
    ExchangeFactory exchangeFactory;

    @Autowired
    ArcadedbOperations operations;

    ObjectMapper mapper;

    @BeforeAll
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class VertexTests {
        @BeforeEach
        void setUpVertexTests() throws JsonMappingException, JsonProcessingException {
            var outArray = mapper.readValue(VERTEX_RESULT_STRING, Map[].class);
            var response = Mono.just(new CommandResponse(null, null, null, outArray));
            when(commandExchange.exchange()).thenReturn(response);
            when(exchangeFactory.createCommandExchange(any(), any(), any(), any(), any())).thenReturn(commandExchange);
        }

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
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class IdTests {
        @BeforeEach
        void setUpIdTests() throws JsonMappingException, JsonProcessingException {
            var outArray = mapper.readValue(ID_RESULT_STRING, Map[].class);
            var response = Mono.just(new CommandResponse(null, null, null, outArray));
            when(commandExchange.exchange()).thenReturn(response);
            when(exchangeFactory.createCommandExchange(any(), any(), any(), any(), any())).thenReturn(commandExchange);
        }

        @Test
        void outVertexIds() {
            StepVerifier.create(operations.outVertexIds("Kunde"))
                    .expectNextMatches(id -> id != null && id.length() > 0)
                    .expectNextMatches(id -> id != null && id.length() > 0)
                    .verifyComplete();
        }

        @Test
        void outVertexIdsWithEdgeType() {
            StepVerifier.create(operations.outVertexIds("Kunde", "IsContactOf"))
                    .expectNextMatches(id -> id != null && id.length() > 0)
                    .expectNextMatches(id -> id != null && id.length() > 0)
                    .verifyComplete();
        }

        @Test
        void outEdgesIds() {
            ;
            StepVerifier.create(operations.outEdgesIds("Kunde"))
                    .expectNextMatches(id -> id != null && id.length() > 0)
                    .expectNextMatches(id -> id != null && id.length() > 0)
                    .verifyComplete();
        }

        @Test
        void outEdgesIdsWithEdgeType() {
            StepVerifier.create(operations.outEdgesIds("Kunde", "IsContactOf"))
                    .expectNextMatches(id -> id != null && id.length() > 0)
                    .expectNextMatches(id -> id != null && id.length() > 0)
                    .verifyComplete();
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class EdgeTests {
        @BeforeEach
        void setUpEdgeTests() throws JsonMappingException, JsonProcessingException {
            var outArray = mapper.readValue(EDGE_RESULT_STRING, Map[].class);
            var response = Mono.just(new CommandResponse(null, null, null, outArray));
            when(commandExchange.exchange()).thenReturn(response);
            when(exchangeFactory.createCommandExchange(any(), any(), any(), any(), any())).thenReturn(commandExchange);
        }

        @Test
        void outEdges() throws JsonMappingException, JsonProcessingException {
            StepVerifier.create(operations.outEdges(new Person()))
                    .expectNextMatches(
                            edge -> edge instanceof IsContactOf && ((IsContactOf) edge).getRid().equals("#57:0"))
                    .expectNextMatches(
                            edge -> edge instanceof IsContactOf && ((IsContactOf) edge).getRid().equals("#58:0"))
                    .verifyComplete();
        }

        @Test
        void outEdgesWithEdgeType() {
            StepVerifier.create(operations.outEdges(new Person(), IsContactOf.class))
                    .expectNextMatches(
                            edge -> edge instanceof IsContactOf && ((IsContactOf) edge).getRid().equals("#57:0"))
                    .expectNextMatches(
                            edge -> edge instanceof IsContactOf && ((IsContactOf) edge).getRid().equals("#58:0"))
                    .verifyComplete();
        }
    }
}
