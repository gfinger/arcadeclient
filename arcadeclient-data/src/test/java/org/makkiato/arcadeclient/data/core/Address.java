package org.makkiato.arcadeclient.data.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address extends DocumentBase{
    private String town;
    private String zipcode;
    private String street;
}
