package org.jvnet.hk2.annotations;

import org.jvnet.hk2.component.Scope;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Indicates the scope that this managed object is tied to.
 *
 * In the absence of this annotation, singleton scope is assumed.
 *
 * <p>
 * A scope can be placed on the same type as {@link Contract} does,
 * in which case it is used to force the use of a specific scope
 * for all {@link Service services}.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({TYPE,ANNOTATION_TYPE})
public @interface Scoped {
    Class<? extends Scope> value();
}
