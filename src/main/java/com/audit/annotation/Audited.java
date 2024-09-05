package com.audit.annotation;

import com.audit.enums.Identifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
        int index();
        boolean shouldStoreAll();
        String[] fieldsToAudit() default {};
        Identifier identifier() default Identifier.NONE;
        String identifierKey() default "";
}
