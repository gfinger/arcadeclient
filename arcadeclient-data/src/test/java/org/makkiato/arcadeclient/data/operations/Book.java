package org.makkiato.arcadeclient.data.operations;

import lombok.*;
import org.makkiato.arcadeclient.data.base.Document;
import org.makkiato.arcadeclient.data.base.VertexBase;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("Book")
public class Book extends VertexBase {
    private String title;
    private Person[] authors;
}
