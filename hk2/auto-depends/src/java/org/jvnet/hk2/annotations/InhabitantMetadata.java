package org.jvnet.hk2.annotations;

import org.jvnet.hk2.component.Inhabitant;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Meta-annotation to make annotation values readable as {@link Inhabitant#metadata()}.
 *
 * This annotation is to be placed on properties of annotations.
 *
 * TODO: more documents!
 *
 * @author Kohsuke Kawaguchi
 */
@Contract
@Documented
@Retention(RUNTIME)
@Target({METHOD})
public @interface InhabitantMetadata {
    /**
     * Name of the metadata key. Defaults to the fully qualified class name of the annotation + '.' + property name.
     */
    String value() default "";
}
