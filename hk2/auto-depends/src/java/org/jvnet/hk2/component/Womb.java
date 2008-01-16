package org.jvnet.hk2.component;

/**
 * Encapsulates how to create an object.
 *
 * <p>
 * Signature-wise it's the same as {@link Inhabitant}
 * but it carries an additional meaning.
 *
 * @author Kohsuke Kawaguchi
 * @see Wombs
 */
public interface Womb<T> extends Inhabitant<T> {

    /**
     * Short cut for
     *
     * <pre>
     * T o = create();
     * initialize(o);
     * return o;
     * </pre>
     */
    T get() throws ComponentException;

    /**
     * Creates a new instance.
     *
     * The caller is supposed to call the {@link #initialize(Object)}
     * right away. This 2-phase initialization allows us to handle
     * cycle references correctly.
     */
    T create() throws ComponentException;

    /**
     * Performs initialization of object, such as dependency injection.
     */
    void initialize(T t) throws ComponentException;
}
