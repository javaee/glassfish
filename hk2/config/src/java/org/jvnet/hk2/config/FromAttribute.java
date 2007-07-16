package org.jvnet.hk2.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface FromAttribute {
    /**
     * Attribute name. By default inferred from field name.
     */
    String value() default "";

    boolean reference() default false;
}
