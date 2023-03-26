package org.makkiato.arcadeclient.data.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.makkiato.arcadeclient.data.base.DocumentBase;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address extends DocumentBase {
    private String town;
    private String zipcode;
    private String street;
}
