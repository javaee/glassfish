package org.jvnet.hk2.annotations;

import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

/**
 * Marks the index property of the {@link Contract} annotations.
 *
 * <p>
 * This annotation goes to one of the annotation elements of the annotation type
 * that's annotated with {@link Contract}.
 *
 * @author Kohsuke Kawaguchi
 * @see Contract
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface Index {}
