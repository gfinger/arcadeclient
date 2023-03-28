package org.makkiato.arcadeclient.data.base;

import lombok.Getter;
import lombok.Setter;
import org.makkiato.arcadeclient.data.Type;

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
