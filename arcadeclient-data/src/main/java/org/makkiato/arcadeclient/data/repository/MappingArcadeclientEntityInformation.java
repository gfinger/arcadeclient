package org.makkiato.arcadeclient.data.repository;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.repository.core.support.PersistentEntityInformation;

public class MappingArcadeclientEntityInformation<T> extends PersistentEntityInformation<T, String> implements ArcadeclientEntityInformation<T> {
    public MappingArcadeclientEntityInformation(PersistentEntity<T, ? extends PersistentProperty<?>> persistentEntity) {
        super(persistentEntity);
    }
}
