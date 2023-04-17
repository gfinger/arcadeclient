package org.makkiato.arcadeclient.data.mapping;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ArcadeclientMappingContext extends AbstractMappingContext<ArcadeclientPersistentEntity<?>,
        ArcadeclientPersistentProperty> {
    private ConcurrentHashMap<String, ArcadeclientPersistentEntity<?>> persistentEntities = new ConcurrentHashMap();

    /**
     * Creates the concrete {@link PersistentEntity} instance.
     *
     * @param typeInformation
     * @return
     */
    @Override
    protected <T> ArcadeclientPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {
        return new BasicArcadeclientPersistentEntity(typeInformation) {
        };
    }

    /**
     * Creates the concrete instance of {@link PersistentProperty}.
     *
     * @param property
     * @param owner
     * @param simpleTypeHolder
     * @return
     */
    @Override
    protected ArcadeclientPersistentProperty createPersistentProperty(Property property,
                                                                      ArcadeclientPersistentEntity<?> owner,
                                                                      SimpleTypeHolder simpleTypeHolder) {
        return new BasicArcadeclientPersistentProperty(property, owner, simpleTypeHolder);
    }

    /**
     * Adds the given {@link TypeInformation} to the {@link MappingContext}.
     *
     * @param typeInformation must not be {@literal null}.
     * @return
     */
    protected Optional<ArcadeclientPersistentEntity<?>> addPersistentEntity(TypeInformation<?> typeInformation) {
        var persistentEntity = super.addPersistentEntity(typeInformation);
        persistentEntity.ifPresent(entity -> persistentEntities.put(entity.getDocumentType(), entity));
        return persistentEntity;
    }

    public ArcadeclientPersistentEntity<?> getPersistentEntityForDocumentType(String documentType) {
        return persistentEntities.get(documentType);
    }
}
