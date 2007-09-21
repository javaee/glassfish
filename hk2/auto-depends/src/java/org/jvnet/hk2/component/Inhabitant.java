package org.jvnet.hk2.component;

import com.sun.hk2.component.Holder;
import com.sun.hk2.component.ScopeInstance;

/**
 * @author Kohsuke Kawaguchi
 */
public interface Inhabitant<T> extends Holder<T> {
    /**
     * The short-cut for {@code type().getName()}
     * but this allows us to defer loading the actual types.
     */
    String typeName();
    Class<T> type();

    /**
     * Returns the instance of this inhabitant.
     *
     * <p>
     * Some {@link Inhabitant}s return the same instance for multiple
     * invocations (AKA singleton), but
     * the method may return different instances to invocations from different
     * context (AKA scope.) The extreme case is where the each invocation
     * returns a different object.
     */
    T get();

    /**
     * Gets the metadata associated with this inhabitant.
     *
     * <p>
     * This data is usually used by a sub-system of HK2, and not really meant to
     * be used by applications. (At least for now.)
     * The main benefit of metadata is that it's available right away
     * as soon as the {@link Habitat} is properly initialized, even before
     * component classes are loaded. In contrast, accessing annotations would require
     * classes to be loaded and resolved.
     *
     * @return
     *      can be empty but never null. The values are read-only.
     */
    MultiMap<String,String> metadata();

    /**
     * Called to orderly shutdown {@link Habitat}.
     * <p>
     * The expected behavior is for objects to get its {@link PreDestroy}
     * callback invoked, and its reference released. For singleton
     * objects, this method is expected to dispose that object.
     * <p>
     * For scoped objects, those are released when {@link ScopeInstance#release()}
     * is invoked.
     */
    void release();
}
