package com.sun.hk2.component;

import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.Womb;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.MultiMap;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractInhabitantImpl<T> implements Inhabitant<T> {
    protected final Womb<T> womb;

    protected AbstractInhabitantImpl(Womb<T> womb) {
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
