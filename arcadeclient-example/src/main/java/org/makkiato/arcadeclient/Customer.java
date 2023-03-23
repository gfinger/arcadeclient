package org.makkiato.arcadeclient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.makkiato.arcadeclient.data.core.DocumentBase;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends DocumentBase {
    private String name;
    private Address address;
}
