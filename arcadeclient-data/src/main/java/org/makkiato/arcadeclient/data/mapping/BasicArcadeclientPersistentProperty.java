package org.makkiato.arcadeclient.data.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;

public class BasicArcadeclientPersistentProperty extends AnnotationBasedPersistentProperty<ArcadeclientPersistentProperty> implements ArcadeclientPersistentProperty {
    private final Boolean isIdProperty;

    /**
     * Creates a new {@link AnnotationBasedPersistentProperty}.
     *
     * @param property         must not be {@literal null}.
     * @param owner            must not be {@literal null}.
     * @param simpleTypeHolder
     */
    public BasicArcadeclientPersistentProperty(Property property,
                                               PersistentEntity<?, ArcadeclientPersistentProperty> owner,
                                               SimpleTypeHolder simpleTypeHolder) {
        super(property, owner, simpleTypeHolder);
        var jsonProperty = property.getField().orElseThrow().getAnnotation(JsonProperty.class);
        if (jsonProperty != null) {
            var propertyName = jsonProperty.value();
            isIdProperty = propertyName.equals("@rid");
        } else {
            isIdProperty = false;
        }
    }

    @Override
    protected Association<ArcadeclientPersistentProperty> createAssociation() {
        return null;
    }

    @Override
    public boolean isIdProperty() {
        return isIdProperty;
    }

    @Override
    public boolean isVersionProperty() {
        return false;
    }
}
