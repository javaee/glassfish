package org.glassfish.admin.runtime.infrastructure.management;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for operation of runtime mBean
 *
 * @author Sreenivas Munnangi
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ManagedOperation {
}
