package org.makkiato.arcadeclient.data.mapping;

import lombok.Getter;
import lombok.Setter;
import org.makkiato.arcadeclient.data.base.Document;
import org.makkiato.arcadeclient.data.base.VertexBase;

@Getter
@Setter
@Document("Customer")
public class Customer extends VertexBase {
    private String name;
    private Address address;
    private String phone;
}
