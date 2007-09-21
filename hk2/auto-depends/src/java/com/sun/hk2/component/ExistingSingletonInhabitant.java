package com.sun.hk2.component;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

/**
 * {@link Inhabitant} built around an object that already exists.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExistingSingletonInhabitant<T> implements Inhabitant<T> {
    private final T object;
    private final Class<T> type;
    private final MultiMap<String,String> metadata;

    public ExistingSingletonInhabitant(T object) {
        this((Class<T>)object.getClass(),object);
    }

    public ExistingSingletonInhabitant(Class<T> type, T object) {
        this(type,object,MultiMap.<String,String>emptyMap());
    }

    public ExistingSingletonInhabitant(Class<T> type, T object, MultiMap<String,String> metadata) {
        this.type = type;
        this.object = object;
        this.metadata = metadata;
    }

    public String typeName() {
        return type.getName();
    }

    public Class<T> type() {
        return type;
    }

    public MultiMap<String, String> metadata() {
        return metadata;
    }

    public T get() throws ComponentException {
        return object;
    }

    public void release() {
        // since we are working on the existing object,
        // we can't release its instance.
        // (technically it's possible to invoke PreDestroy,
        // not clear which is better --- you can argue both ways.)
    }
}
