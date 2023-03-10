package org.makkiato.arcadedb.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class Vertex {
    @JsonProperty("@rid")
    private String rid;
    @JsonProperty("@type")
    private String type;
    @JsonProperty("@cat")
    private String cat;
}
