package org.makkiato.arcadeclient.data.annotation;

import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Document {
    
}
