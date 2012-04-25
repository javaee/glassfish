package org.jvnet.hk2.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The {@link RunLevel} that indicates immediate activation
 * for the default run level scope.
 *
 * @author Jeff Trent
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@RunLevel(RunLevel.KERNEL_RUNLEVEL)
public @interface Immediate {}
