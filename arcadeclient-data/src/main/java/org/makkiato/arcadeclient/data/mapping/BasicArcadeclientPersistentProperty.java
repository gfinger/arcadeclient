package org.makkiato.arcadeclient.data.mapping;

import org.makkiato.arcadeclient.data.mapping.ArcadeclientPersistentProperty;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;

public class BasicArcadeclientPersistentProperty extends AnnotationBasedPersistentProperty<ArcadeclientPersistentProperty> implements ArcadeclientPersistentProperty {
    /**
     * Creates a new {@link AnnotationBasedPersistentProperty}.
     *
     * @param property         must not be {@literal null}.
     * @param owner            must not be {@literal null}.
     * @param simpleTypeHolder
     */
    public BasicArcadeclientPersistentProperty(Property property, PersistentEntity<?, ArcadeclientPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(property, owner, simpleTypeHolder);
    }

    @Override
    protected Association<ArcadeclientPersistentProperty> createAssociation() {
        return null;
    }
}
