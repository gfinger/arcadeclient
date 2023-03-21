package org.makkiato.arcadeclient;

import lombok.Data;
import org.makkiato.arcadeclient.data.core.DocumentBase;

@Data
public class Person extends DocumentBase {
    private String name;
}
