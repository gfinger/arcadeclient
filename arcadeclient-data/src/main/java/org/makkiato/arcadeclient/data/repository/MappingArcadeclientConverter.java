package org.makkiato.arcadeclient.data.repository;

import org.makkiato.arcadeclient.data.base.DocumentBase;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.mapping.context.MappingContext;

public class MappingArcadeclientConverter implements ArcadeclientEntityConverter {
    /**
     * Returns the underlying {@link MappingContext} used by the converter.
     *
     * @return never {@literal null}
     */
    @Override
    public MappingContext<? extends ArcadeclientPersistentEntity<?>, ArcadeclientPersistentProperty> getMappingContext() {
        return null;
    }

    /**
     * Returns the underlying {@link ConversionService} used by the converter.
     *
     * @return never {@literal null}.
     */
    @Override
    public ConversionService getConversionService() {
        return null;
    }

    /**
     * Reads the given source into the given type.
     *
     * @param type   they type to convert the given source to.
     * @param source the source to create an object of the given type from.
     * @return
     */
    @Override
    public <R extends DocumentBase> R read(Class<R> type, DocumentBase source) {
        return null;
    }

    @Override
    public void write(DocumentBase source, DocumentBase sink) {

    }
}
