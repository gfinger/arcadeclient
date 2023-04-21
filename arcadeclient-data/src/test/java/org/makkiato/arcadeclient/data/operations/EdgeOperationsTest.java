package org.makkiato.arcadeclient.data.operations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.makkiato.arcadeclient.data.base.VertexBase;
import org.makkiato.arcadeclient.data.core.ArcadedbProperties;
import org.makkiato.arcadeclient.data.core.WebClientFactory;
import org.makkiato.arcadeclient.data.mapping.MappingArcadeclientConverter;
import org.makkiato.arcadeclient.data.web.client.WebClientSupplier;
import org.makkiato.arcadeclient.data.web.request.CommandExchange;
import org.makkiato.arcadeclient.data.web.request.ExchangeFactory;
import org.makkiato.arcadeclient.data.web.response.CommandResponse;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringJUnitConfig(OperationsTestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EdgeOperationsTest {
    static final String OUT_RESULT_STRING = """
            [
                {
                    "out()": [
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
                    "out()": [
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

    @MockBean
    CommandExchange commandExchange;

    @MockBean
    ExchangeFactory exchangeFactory;

    @Autowired
    ArcadedbOperations operations;

    ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void outVertices() throws JsonMappingException, JsonProcessingException {
        var outArray = mapper.readValue(OUT_RESULT_STRING, Map[].class);
        var response = Mono.just(new CommandResponse(null, null, null, outArray));
        when(commandExchange.exchange()).thenReturn(response);
        when(exchangeFactory.createCommandExchange(any(), any(), any(), any(), any())).thenReturn(commandExchange);
        StepVerifier.create(operations.outVertices(new Customer()))
                .expectNextMatches(vertex -> vertex instanceof VertexBase)
                .expectNextMatches(vertex -> vertex instanceof VertexBase)
                .verifyComplete();

    }
}
