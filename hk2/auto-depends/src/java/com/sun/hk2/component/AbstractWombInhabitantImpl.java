package com.sun.hk2.component;

import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Womb;
import org.jvnet.hk2.component.Inhabitant;

/**
 * Partial implementation of {@link Inhabitant} that delegates to {@link Womb}
 * for object creation.
 * <p>
 * Derived types are expected to implement the {@link #get()} method and
 * choose when to create an object. 
 *
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractWombInhabitantImpl<T> extends AbstractInhabitantImpl<T> {
    protected final Womb<T> womb;

    protected AbstractWombInhabitantImpl(Womb<T> womb) {
        this.womb = womb;
    }

    public final String typeName() {
        return womb.typeName();
    }

    public final Class<T> type() {
        return womb.type();
    }

    public MultiMap<String, String> metadata() {
        return womb.metadata();
    }

    protected final void dispose(T object) {
        if (object instanceof PreDestroy)
            ((PreDestroy)object).preDestroy();
    }
}
