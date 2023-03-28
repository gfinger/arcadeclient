package org.makkiato.arcadeclient.data.mapping;

import org.springframework.data.convert.EntityConverter;

public interface ArcadeclientEntityConverter extends
        EntityConverter<ArcadeclientPersistentEntity<?>, ArcadeclientPersistentProperty, Object, Object> {
}
