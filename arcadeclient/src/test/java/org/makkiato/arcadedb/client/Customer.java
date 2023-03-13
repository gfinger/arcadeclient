package org.makkiato.arcadedb.client;

import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("Client")
public class Customer extends Vertex {
    private String name;
    private Address address;
    private String phone;
}
