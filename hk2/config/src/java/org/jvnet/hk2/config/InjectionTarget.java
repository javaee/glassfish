package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Index;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import java.lang.annotation.Target;

/**
 * Used on {@link ConfigInjector} to indicate
 * the target class of the injection.
 * @author Kohsuke Kawaguchi
 */
@Contract
@Retention(SOURCE)
@Target(TYPE)
public @interface InjectionTarget {
    @Index Class<?> value();
}
