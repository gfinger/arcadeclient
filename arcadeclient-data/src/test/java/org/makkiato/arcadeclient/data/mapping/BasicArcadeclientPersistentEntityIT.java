package org.makkiato.arcadeclient.data.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.TypeInformation;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestConfiguration.class)
class BasicArcadeclientPersistentEntityIT {
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

    @Test
    void getDocumentType() {
        var customerTypeInformation = TypeInformation.of(Customer.class);
        var customerEntity = mappingContext.getPersistentEntity(customerTypeInformation);
        assertThat(customerEntity.getDocumentType()).isEqualTo("Kunde");

        var addressTypeInformation = TypeInformation.of(Address.class);
        var addressEntity = mappingContext.getPersistentEntity(addressTypeInformation);
        assertThat(addressEntity.getDocumentType()).isEqualTo("Address");

        var personTypeInformation = TypeInformation.of(Person.class);
        var personEntity = mappingContext.getPersistentEntity(personTypeInformation);
        assertThat(personEntity.getDocumentType()).isEqualTo("Person");
    }
}