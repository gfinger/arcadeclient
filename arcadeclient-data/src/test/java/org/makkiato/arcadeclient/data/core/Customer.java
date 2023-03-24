package org.makkiato.arcadeclient.data.core;

import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("Customer")
public class Customer extends VertexBase {
    private String name;
    private Address address;
    private String phone;
}
