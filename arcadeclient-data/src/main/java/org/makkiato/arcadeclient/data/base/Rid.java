package org.makkiato.arcadeclient.data.base;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Inherited
public @interface Rid {
    public String value() default "@rid";
}
