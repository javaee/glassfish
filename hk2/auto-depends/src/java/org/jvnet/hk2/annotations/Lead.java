package org.jvnet.hk2.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Used on a companion object to request an injection of the corresponding lead object.
 *
 * TODO: expand doc
 *
 * @author Kohsuke Kawaguchi
 * @see CompanionOf
 */
@Retention(RUNTIME)
@Target({METHOD,FIELD})
public @interface Lead {
}
