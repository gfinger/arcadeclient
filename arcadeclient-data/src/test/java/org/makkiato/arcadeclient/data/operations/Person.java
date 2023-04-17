package org.makkiato.arcadeclient.data.operations;

import lombok.*;
import org.makkiato.arcadeclient.data.base.Document;
import org.makkiato.arcadeclient.data.base.VertexBase;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Person extends VertexBase {
    private String name;
}
