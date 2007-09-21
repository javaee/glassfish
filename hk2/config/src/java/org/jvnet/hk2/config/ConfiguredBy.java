package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Contract;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * TODO: still a work in progress
 *
 * @author Kohsuke Kawaguchi
 */
@Contract
@Retention(RUNTIME)
@Target(TYPE)
public @interface ConfiguredBy {
    /**
     * {@link Configured} class that the component will be associated with.
     */
    Class<?> value();
}
