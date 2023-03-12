package org.makkiato.arcadedb.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer extends Vertex {
    private String name;
    private Address address;
    private String phone;

    public Customer() {
        type = "Customer";
    }
}
