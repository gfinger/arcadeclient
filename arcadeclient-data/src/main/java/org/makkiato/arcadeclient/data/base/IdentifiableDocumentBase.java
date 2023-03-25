package org.makkiato.arcadeclient.data.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.makkiato.arcadeclient.data.base.DocumentBase;

public abstract class IdentifiableDocumentBase extends DocumentBase {
    @Getter
    @JsonProperty("@rid")
    private String rid;
}
