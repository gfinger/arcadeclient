package org.makkiato.arcadeclient.example;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.makkiato.arcadeclient.data.base.IdentifiableDocumentBase;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends IdentifiableDocumentBase {
    private String name;
    private Address address;
}
