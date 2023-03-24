package org.makkiato.arcadeclient.data.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public abstract class IdentifiableDocumentBase extends DocumentBase {
    @Getter
    @JsonProperty("@rid")
    private String rid;
}
