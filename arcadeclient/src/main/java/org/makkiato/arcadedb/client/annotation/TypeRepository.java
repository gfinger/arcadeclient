package org.makkiato.arcadedb.client.annotation;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonTypeName;

@Component
public class TypeRepository {
    public String getType(Class<?> clazz) {
        var annotatedName = clazz.getAnnotation(JsonTypeName.class);
        if (annotatedName != null) {
            return annotatedName.value();
        } else {
            return clazz.getSimpleName();
        }
    }
}
