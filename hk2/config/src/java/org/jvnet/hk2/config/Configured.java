package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Contract;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Marks inhabitants that require configuration for instantiation.
 *
 * @author Kohsuke Kawaguchi
 */
@Contract
@Retention(RUNTIME)
@Target(TYPE)
public @interface Configured {
    /**
     * XML element name that this configured object maps to.
     *
     * See {@link Attribute#value()} for how the default value is inferred.
     *
     * @see #local()
     */
    String name() default "";

    /**
     * Designates the configuration element as local element.
     *
     * <p>
     * By default, element names for configured inhabitant are global, meaning
     * component can be referenced from anywhere that allow a valid substitution, such
     * as:
     *
     * <pre>
     * &#64;Element("*")
     * List&lt;Object> any;
     * </pre>
     *
     * <p>
     * Setting this flag to true will prevent this, and instead make this element local.
     * Such configuration inhabitants will not have any associated element name, and therefore
     * can only participate in the configuration XML when explicitly referenced from its parent.
     * For example:
     *
     * <pre>
     * &#64;Configured
     * class Foo {
     *   &#64;Element List&lt;Property> properties;
     * }
     *
     * &#64;Configured(local=true)
     * class Property {
     *   &#64;FromAttribute String name;
     *   &#64;FromAttribute String value;
     * }
     *
     * &lt;foo>
     *   &lt;property name="aaa" value="bbb" />
     * &lt;/foo>
     * </pre>
     *
     * <p>
     * This switch is mutually exclusive with {@link #name()}.
     */
    boolean local() default false;

    /**
     * Designates that a configured component creates
     * a new symbol space for given kinds of children.
     */
    Class[] symbolSpace() default {};
}
