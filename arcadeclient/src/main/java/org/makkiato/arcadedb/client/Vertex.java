package org.makkiato.arcadedb.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class Vertex {
    @JsonProperty("@rid")
    protected String rid;
    @JsonProperty("@type")
    protected String type;
    @JsonProperty("@cat")
    protected String cat;
}
