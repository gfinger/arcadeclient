package org.makkiato.arcadeclient.data.base;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Inherited
public @interface In {
    public String value() default "@in";
}
