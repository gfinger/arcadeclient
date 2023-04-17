package org.makkiato.arcadeclient.data.base;

import lombok.Getter;
import lombok.Setter;

public abstract class DocumentBase {
    @Getter
    @Setter
    @Type
    private String type;
    @Getter
    @Setter
    @Category
    private String cat;
}
