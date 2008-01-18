package org.jvnet.hk2.annotations;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

/**
 * Marks the component as a companion to another "lead" component.
 *
 * <p>
 * Whenever the lead component is placed into habitat, the companion
 * will be also placed into the habitat.
 *
 * @author Kohsuke Kawaguchi
 */
@Contract
@Documented
@Retention(RUNTIME)
@Target({TYPE})
@InhabitantAnnotation("default")
public @interface CompanionOf {
    /**
     * Specifies the target of the companion.
     */
    @Index
    Class<?> value();
}
