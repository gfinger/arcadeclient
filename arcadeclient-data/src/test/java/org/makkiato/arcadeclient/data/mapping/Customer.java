package org.makkiato.arcadeclient.data.mapping;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import org.makkiato.arcadeclient.data.base.VertexBase;
import org.makkiato.arcadeclient.data.repository.Address;

@Getter
@Setter
@JsonTypeName("Customer")
public class Customer extends VertexBase {
    private String name;
    private Address address;
    private String phone;
}
