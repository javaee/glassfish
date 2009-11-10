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
@Constraint(validatedBy = JdbcSteadyMaxPoolSizesValidator.class)
public @interface JdbcSteadyMaxPoolSizes {
    String message() default "Max-pool-size has to be greater than or equal to steady-pool-size";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
