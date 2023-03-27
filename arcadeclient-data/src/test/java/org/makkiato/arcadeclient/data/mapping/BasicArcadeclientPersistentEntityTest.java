package org.makkiato.arcadeclient.data.mapping;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.TypeInformation;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.*;

@SpringJUnitConfig(TestConfiguration.class)
@TestPropertySource(properties = {
        "org.makkiato.arcadedb.connections.arcadedb0.host=localhost",
        "org.makkiato.arcadedb.connections.arcadedb0.port=2480",
        "org.makkiato.arcadedb.connections.arcadedb0.database=xyz-graphql-test",
        "org.makkiato.arcadedb.connections.arcadedb0.username=root",
        "org.makkiato.arcadedb.connections.arcadedb0.password=playwithdata",
        "org.makkiato.arcadedb.connections.arcadedb0.leader-preferred=true"
})
class BasicArcadeclientPersistentEntityTest {
    @Autowired
    ArcadeclientMappingContext mappingContext;

    @Test
    void isIdProperty() {
        var customerTypeInformation = TypeInformation.of(Customer.class);
        var customerEntity = mappingContext.getPersistentEntity(customerTypeInformation);
        assertThat(customerEntity).anyMatch(ArcadeclientPersistentProperty::isIdProperty);

        var addressTypeInformation = TypeInformation.of(Address.class);
        var addressEntity = mappingContext.getPersistentEntity(addressTypeInformation);
        assertThat(addressEntity).noneMatch(ArcadeclientPersistentProperty::isIdProperty);

    }
}