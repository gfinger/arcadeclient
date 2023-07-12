package org.makkiato.arcadeclient.data.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import org.makkiato.arcadeclient.data.base.Type;
import org.makkiato.arcadeclient.data.base.Category;
import org.makkiato.arcadeclient.data.base.Rid;
import org.makkiato.arcadeclient.data.exception.client.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Map;

public class MappingArcadeclientConverter implements ArcadeclientEntityConverter {
    private final ArcadeclientMappingContext arcadeclientMappingContext;
    private final ObjectMapper mapper;

    public MappingArcadeclientConverter(ArcadeclientMappingContext arcadeclientMappingContext) {
        this.arcadeclientMappingContext = arcadeclientMappingContext;
        this.mapper = new Jackson2ObjectMapperBuilder()
                .propertyNamingStrategy((new ArcadeclientPropertyNamingStrategy()))
                .build();
    }

    /**
     * Returns the underlying {@link MappingContext} used by the converter.
     *
     * @return never {@literal null}
     */
    @Override
    public MappingContext<? extends ArcadeclientPersistentEntity<?>, ArcadeclientPersistentProperty> getMappingContext() {
        return arcadeclientMappingContext;
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
    public <R> R read(Class<R> type, Object source) {
            if(source instanceof Map<?, ?> map) {
                return mapper.convertValue(map, type);
            }
        throw new ConversionException(String.format("cannot convert object %s", source.toString()));
    }

    /**
     * Interface to write objects into store specific sinks.
     *
     * @param sink   the entity type the converter can handle
     * @param source the store specific sink the converter is able to write to
     */
    @Override
    public void write(Object source, Object sink) {
        try {
            if (sink instanceof StringBuffer buffer) {
                buffer.delete(0, buffer.length());
                buffer.append(mapper.writeValueAsString(source));
            }
        } catch (JsonProcessingException ex) {
            throw new ConversionException(String.format("cannot convert object %s", source.toString()), ex);
        }
    }

    private static class ArcadeclientPropertyNamingStrategy extends PropertyNamingStrategy {
        @Override
        public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            if (method.hasAnnotation(Rid.class)) {
                return method.getAnnotation(Rid.class).value();
            }
            if (method.hasAnnotation(Category.class)) {
                return method.getAnnotation(Category.class).value();
            }
            if (method.hasAnnotation(Type.class)) {
                return method.getAnnotation(Type.class).value();
            }
            return defaultName;
        }

        @Override
        public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            return nameForGetterMethod(config, method, defaultName);
        }
    }
}
