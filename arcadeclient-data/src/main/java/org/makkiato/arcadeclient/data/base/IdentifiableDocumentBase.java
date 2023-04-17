package org.makkiato.arcadeclient.data.base;

import lombok.Getter;
import lombok.Setter;

public abstract class IdentifiableDocumentBase extends DocumentBase {
    @Getter
    @Setter
    @Rid
    private String rid;
}
