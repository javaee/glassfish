package com.sun.enterprise.config.serverbeans.customvalidators;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import javax.validation.Constraint;
import javax.validation.Payload;

@Retention(RUNTIME)
@Target({METHOD, FIELD, TYPE})
@Documented
@Constraint(validatedBy = LDAPRealmPropertyCheckValidator.class)
public @interface LDAPRealmPropertyCheck {
    String message() default "base-dn, directory and jaas-context have to be specified for LDAPRealm";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}