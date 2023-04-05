package org.makkiato.arcadeclient.data.base;

import lombok.Getter;
import lombok.Setter;

public abstract class EdgeBase extends IdentifiableDocumentBase {
    @Getter
    @Setter
    @In
    private String in;
    @Getter
    @Setter
    @Out
    private String out;
}
