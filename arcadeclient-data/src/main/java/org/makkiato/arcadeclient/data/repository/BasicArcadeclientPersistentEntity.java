package org.makkiato.arcadeclient.data.repository;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

import java.util.Comparator;

public class BasicArcadeclientPersistentEntity<T> extends BasicPersistentEntity<T, ArcadeclientPersistentProperty> implements ArcadeclientPersistentEntity<T>{
    /**
     * Creates a new {@link BasicPersistentEntity} from the given {@link TypeInformation}.
     *
     * @param information must not be {@literal null}.
     */
    public BasicArcadeclientPersistentEntity(TypeInformation<T> information) {
        super(information);
    }

    /**
     * Creates a new {@link BasicPersistentEntity} for the given {@link TypeInformation} and {@link Comparator}. The
     * given
     * {@link Comparator} will be used to define the order of the {@link PersistentProperty} instances added to the
     * entity.
     *
     * @param information must not be {@literal null}.
     * @param comparator  can be {@literal null}.
     */
    public BasicArcadeclientPersistentEntity(TypeInformation<T> information, Comparator<ArcadeclientPersistentProperty> comparator) {
        super(information, comparator);
    }
}
