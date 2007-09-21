package org.jvnet.hk2.annotations;

import org.jvnet.hk2.component.Habitat;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Meta-annotation indicating that the annotation designates
 * an inhabitant of a {@link Habitat}. 
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
@Documented
public @interface InhabitantAnnotation {
    /**
     * Name of the habitat.
     *
     * This determines the xxx portion of <tt>/META-INF/inhabitants/xxx</tt>
     */
    String value();
}
