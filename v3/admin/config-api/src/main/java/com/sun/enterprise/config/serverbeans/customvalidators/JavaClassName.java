
package com.sun.enterprise.config.serverbeans.customvalidators;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import javax.validation.Constraint;
import javax.validation.ConstraintPayload; 
/**
 * User-defined constraint to check if an attribute is a valid Java class name
 * The class name can be a fully qualified classname inlcuding its package name
 * 
 * @author Nandini Ektare
 */

@Retention(RUNTIME)
@Target({FIELD, METHOD})
@Documented
@Constraint(validatedBy = JavaClassNameValidator.class)
public @interface JavaClassName {
    String message() default "must be a valid Java Class Name";
    Class<?>[] groups() default {};
    Class<? extends ConstraintPayload>[] payload() default {}; 
}