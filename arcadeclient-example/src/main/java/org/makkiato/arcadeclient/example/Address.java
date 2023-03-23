package org.makkiato.arcadeclient.example;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.makkiato.arcadeclient.data.core.DocumentBase;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address extends DocumentBase {
    private String street;
    private String zip;
    private String city;
    private String phone;

}
