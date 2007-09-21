package com.sun.hk2.component;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.Womb;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractWombImpl<T> implements Womb<T> {
    protected final Class<T> type;
    private final MultiMap<String,String> metadata;

    public AbstractWombImpl(Class<T> type, MultiMap<String,String> metadata) {
        this.type = type;
        this.metadata = metadata;
    }

    public final String typeName() {
        return type.getName();
    }

    public final Class<T> type() {
        return type;
    }

    public final T get() throws ComponentException {
        T o = create();
        initialize(o);
        return o;
    }

    public void initialize(T t) throws ComponentException {
        // default is no-op
    }

    public void release() {
        // Womb creates a new instance every time,
        // so there's nothing to release here.
    }

    public MultiMap<String, String> metadata() {
        return metadata;
    }
}
