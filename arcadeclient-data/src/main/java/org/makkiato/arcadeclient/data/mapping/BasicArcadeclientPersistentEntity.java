package org.makkiato.arcadeclient.data.mapping;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

import java.util.Comparator;

public class BasicArcadeclientPersistentEntity<T> extends BasicPersistentEntity<T, ArcadeclientPersistentProperty> implements ArcadeclientPersistentEntity<T> {

    /**
     * Creates a new {@link BasicPersistentEntity} from the given {@link TypeInformation}.
     *
     * @param information must not be {@literal null}.
     */
    public BasicArcadeclientPersistentEntity(TypeInformation<T> information) {
        super(information, Comparator.comparing(PersistentProperty::getName));
    }
}
