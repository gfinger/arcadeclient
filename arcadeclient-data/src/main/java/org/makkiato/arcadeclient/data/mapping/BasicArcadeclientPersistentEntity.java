package org.makkiato.arcadeclient.data.mapping;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

import java.util.Comparator;

public class BasicArcadeclientPersistentEntity<T> extends BasicPersistentEntity<T, ArcadeclientPersistentProperty> implements ArcadeclientPersistentEntity<T> {
    private final String documentType;

    /**
     * Creates a new {@link BasicPersistentEntity} from the given {@link TypeInformation}.
     *
     * @param information must not be {@literal null}.
     */
    public BasicArcadeclientPersistentEntity(TypeInformation<T> information) {
        super(information, Comparator.comparing(PersistentProperty::getName));

        var annotatedName = AnnotationUtils.getAnnotation(information.getType(), JsonTypeName.class);
        this.documentType = annotatedName != null ? annotatedName.value() : this.getClass().getSimpleName();
    }
}
