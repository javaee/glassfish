package org.jvnet.hk2.annotations;

import org.jvnet.hk2.component.Scope;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Indicates the scope that this managed object is tied to.
 *
 * In the absence of this annotation, singleton scope is assumed.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Scoped {
    Class<? extends Scope> value();
}
