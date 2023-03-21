package org.makkiato.arcadeclient;

import lombok.Data;
import org.makkiato.arcadeclient.data.core.VertexParent;

@Data
public class Person extends VertexParent {
    private String name;
}
