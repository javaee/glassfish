package com.sun.hk2.component;

import org.jvnet.hk2.component.Womb;
import org.jvnet.hk2.component.Singleton;

/**
 * Specialized implementation of {@link ScopedInhabitant} for {@link Singleton}.
 * @author Kohsuke Kawaguchi
 */
public class SingletonInhabitant<T> extends AbstractInhabitantImpl<T> {
    private volatile T object;

    public SingletonInhabitant(Womb<T> womb) {
        super(womb);
    }

    public T get() {
        if(object==null) {
            synchronized(this) {
                if(object==null)
                    object =womb.get();
            }
        }
        return object;
    }

    public void release() {
        if(object!=null) {
            dispose(object);
            object=null;
        }
    }
}
