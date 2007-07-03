package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Contract;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * @author Kohsuke Kawaguchi
 */
@Contract
@Retention(RUNTIME)
@Target({TYPE})
public @interface Configured {
    String name() default "";
}
