package org.makkiato.arcadeclient.data.mapping;

import org.junit.jupiter.api.Test;
import org.makkiato.arcadeclient.data.base.Customer;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class MappingArcadeclientConverterTest {

    @Test
    void read() {
        var converter = new MappingArcadeclientConverter(null);
        var customer = new Customer();
        customer.setRid("#1:1");
        customer.setName("Orchid Garden");
        var customerAsMap = Map.of("@rid","#1:1", "name", "Orchid Garden");
        var customerFromJson = converter.read(Customer.class, customerAsMap);
        assertThat(customerFromJson.getRid()).isEqualTo(customer.getRid());
        assertThat(customerFromJson.getName()).isEqualTo(customer.getName());
    }

    @Test
    void write() {
        var converter = new MappingArcadeclientConverter(null);
        var customer = new Customer();
        customer.setRid("#1:1");
        customer.setName("Orchid Garden");
        var buffer = new StringBuffer();
        converter.write(customer, buffer);
        var customerAsJson = buffer.toString();
        assertThat(customerAsJson).containsIgnoringWhitespaces("\"@rid\":\"#1:1\"");
    }
}