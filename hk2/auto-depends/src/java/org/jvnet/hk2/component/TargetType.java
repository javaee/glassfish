package org.jvnet.hk2.component;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Some HK2 contracts {@see Contract} are meant to define a behavior for certain
 * types of components. For instance, a CageBuilder {@see CageBuilder} could be
 * used for caging all instances of a certain type. Implementations of such
 * contracts should complement the @Service annotation with a @TargetType annotation
 * to identify the assignable type for which these providers apply.
 *
 * @author Jerome Dochez
 */
@Retention(RUNTIME)
@Target({ElementType.TYPE})
public @interface TargetType {
    /**
     * intented type
     */
    Class<?> value();
}
