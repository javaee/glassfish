package org.jvnet.hk2.annotations;

import org.jvnet.hk2.component.CageBuilder;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation indicating that additional processing is performed when
 * the component goes into a habitat.
 *
 * <p>
 * This annotation can be used either as a normal annotation on a component
 * or a meta-annotation. A common usage of this is to put this along with
 * {@link Contract} annotation so that all the implementations of a contract
 * receives some infrastructure service.
 *
 * <p>
 * If used as a normal annotation on the contract type, all the services of this
 * contract is subject to the registration hook processing &mdash; that is,
 * the specified {@link CageBuilder} is invoked whenever those services
 * are entered into the habitat, to be given an opportunity to perform
 * additional work.
 *
 * <p>
 * This can be also used as a meta-annotation.
 * Suppose this annotation is placed on annotation X, which in turn is placed
 * on class Y. In this case, {@link CageBuilder} is invoked for every Ys entered into habitat
 * (again, the common case is where X also has {@link Contract} annotation.)
 *
 * <p>
 * This is the interception point for providing additional infrastructure service
 * for certain kinds of inhabitants. 
 *
 * @author Kohsuke Kawaguchi
 * @see CageBuilder
 */
@Contract
@Documented
@Retention(RUNTIME)
@Target({ANNOTATION_TYPE,TYPE})
public @interface CagedBy {
    // this value is captured in metadata so that at runtime
    // we can check the registration hook easily.
    @InhabitantMetadata("cageBuilder")
    Class<? extends CageBuilder> value();
}
