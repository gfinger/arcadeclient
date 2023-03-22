package org.makkiato.arcadeclient;

import lombok.Builder;
import lombok.Data;
import org.makkiato.arcadeclient.data.core.DocumentBase;

@Data
@Builder
public class Customer extends DocumentBase {
    private String name;
    private Address address;
}
