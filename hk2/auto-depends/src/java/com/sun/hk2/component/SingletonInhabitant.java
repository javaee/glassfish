package com.sun.hk2.component;

import org.jvnet.hk2.component.Womb;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.component.Inhabitant;

/**
 * Specialized implementation of {@link ScopedInhabitant} for {@link Singleton}.
 * @author Kohsuke Kawaguchi
 */
public class SingletonInhabitant<T> extends AbstractWombInhabitantImpl<T> {
    private volatile T object;

    public SingletonInhabitant(Womb<T> womb) {
        super(womb);
    }

    public T get(Inhabitant onBehalfOf) {
        if(object==null) {
            synchronized(this) {
                if(object==null)
                    object =womb.get(onBehalfOf);
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
