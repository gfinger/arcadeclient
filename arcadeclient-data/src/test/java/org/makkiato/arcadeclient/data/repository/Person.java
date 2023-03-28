package org.makkiato.arcadeclient.data.repository;

import lombok.*;
import org.makkiato.arcadeclient.data.base.Document;
import org.makkiato.arcadeclient.data.base.VertexBase;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("Person")
public class Person extends VertexBase {
    private String name;
}
