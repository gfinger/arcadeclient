package org.makkiato.arcadeclient.data.repository;

import org.makkiato.arcadeclient.data.base.DocumentBase;
import org.springframework.data.convert.EntityConverter;

public interface ArcadeclientEntityConverter extends EntityConverter<ArcadeclientPersistentEntity<?>, ArcadeclientPersistentProperty, DocumentBase, DocumentBase>  {
}
