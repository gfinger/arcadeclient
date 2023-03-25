package org.makkiato.arcadeclient.data.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

public abstract class DocumentBase {

    private final String documentName;
    @JsonProperty("@type")
    private String type;
    @Getter
    @JsonProperty("@cat")
    private String cat;

    public DocumentBase() {
        var annotatedName = AnnotationUtils.getAnnotation(this.getClass(), JsonTypeName.class);
        documentName = annotatedName != null ? annotatedName.value() : this.getClass().getSimpleName();
    }

    public String getType() {
        return documentName;
    }

    public void setType(String type) {
        Assert.isTrue(type.equals(documentName), String.format("Type name %s does not equal the static Document name " +
                "%s", type, documentName));
    }
}
