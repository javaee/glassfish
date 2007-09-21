package org.jvnet.hk2.config;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that this property or the field value must be injected from
 * an XML element in a configuration file.
 *
 * @author Kohsuke Kawaguchi
 * @see Attribute
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface Element {
    /**
     * Element name.
     *
     * See {@link Attribute#value()} for how the default value is inferred.
     */
    String value() default "";

    /**
     * Indicates that this property becomes the name of the component.
     * There can be only one key on a class.
     */
    boolean key() default false;

    /**
     * Indicates that this element is required.
     *
     * <p>
     * To specify the default value, simply use the field initializer
     * to set it to a certain value. The field/method values are only
     * set when the value is present.
     */
    boolean required() default false;

    /**
     * Indicates that this property is a reference to another
     * configured inhabitant.
     *
     * <p>
     * On XML, this is represented as a string value that points
     * to the {@link #key() value of the key property} of the target
     * inhabitant. See the following example:
     *
     * <pre>
     * &#x40;Configured
     * class VirtualHost {
     *   &#x40;Attribute(key=true)
     *   String name;
     * }
     *
     * &#x40;Configured
     * class HttpListener {
     *   &#x40;Attribute(reference=true)
     *   VirtualHost host;
     * }
     * </pre>
     *
     * <pre><xmp>
     * <virtual-host name="foo" />
     * <http-listener host="foo" />
     * </xmp></pre>
     */
    boolean reference() default false;

    /**
     * Indicates that the variable expansion should be performed on this proeprty.
     *
     * <p>
     * The configuration mechanism supports the Ant/Maven like {@link VariableResolver variable expansion}
     * in the configuration XML out of the box. Normally this happens transparently to objects in modules,
     * hence this property is set to true by default.
     *
     * <p>
     * However, in a rare circumstance you might want to get values injected before the variables
     * are expanded, in which case you can set this property to false to indicate so. Note that such
     * property must be of type {@link String} (or its collection/array.)
     *
     * <p>
     * Also note the inhabitants can always access the XML infoset by talking to {@link Dom} directly.
     */
    boolean variableExpansion() default true;
}
