package org.makkiato.arcadeclient.data.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import static org.assertj.core.api.Assertions.*;

public class RidTest {
    @Test
    void serializeDeserializeDocument() throws NoSuchFieldException, JsonProcessingException {
        var mapper = new Jackson2ObjectMapperBuilder().propertyNamingStrategy((new PropertyNamingStrategy() {
            @Override
            public String nameForGetterMethod(MapperConfig config, AnnotatedMethod method, String defaultName) {
                if(method.hasAnnotation(Rid.class)) {
                    return method.getAnnotation(Rid.class).value();
                }
                return defaultName;
            }
            @Override
            public String nameForSetterMethod(MapperConfig config, AnnotatedMethod method, String defaultName) {
                if(method.hasAnnotation(Rid.class)) {
                    return method.getAnnotation(Rid.class).value();
                }
                return defaultName;
            }
        })).build();
        var customer = new Customer();
        customer.setRid("#1:1");
        customer.setName("Orchid Garden");
        var customerAsJson = mapper.writeValueAsString(customer);
        assertThat(customerAsJson).containsIgnoringWhitespaces("\"@rid\":\"#1:1\"");
        var customerFromJson = mapper.readValue(customerAsJson, Customer.class);
        assertThat(customerFromJson.getRid()).isEqualTo(customer.getRid());
        assertThat(customerFromJson.getName()).isEqualTo(customer.getName());
    }
}
