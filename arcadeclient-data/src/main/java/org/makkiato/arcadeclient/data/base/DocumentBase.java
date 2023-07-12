package org.makkiato.arcadeclient.data.base;

import lombok.Data;
@Data
public abstract class DocumentBase {
    @Type
    private String type;
    @Category
    private String cat;
}
