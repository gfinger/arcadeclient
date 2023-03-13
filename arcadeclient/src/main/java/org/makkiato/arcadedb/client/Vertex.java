package org.makkiato.arcadedb.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Getter;

@Getter
public class Vertex {
    @JsonProperty("@rid")
    private String rid;
    @JsonProperty("@type")
    private String type;
    @JsonProperty("@cat")
    private String cat;

    public String getType() {
        if (type == null) {
            var annotatedName = this.getClass().getAnnotation(JsonTypeName.class);
            if (annotatedName != null) {
                return annotatedName.value();
            } else {
                return this.getClass().getSimpleName();
            }
        } else {
            return type;
        }
    }
}
