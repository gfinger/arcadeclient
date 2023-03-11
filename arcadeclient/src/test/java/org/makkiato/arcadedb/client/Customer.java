package org.makkiato.arcadedb.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer extends Vertex {
    private String name;

    public Customer() {
        type = "Customer";
    }
}
