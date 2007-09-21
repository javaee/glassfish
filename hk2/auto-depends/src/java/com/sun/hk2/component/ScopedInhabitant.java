package com.sun.hk2.component;

import org.jvnet.hk2.component.Scope;
import org.jvnet.hk2.component.Womb;

/**
 * @author Kohsuke Kawaguchi
 */
public class ScopedInhabitant<T> extends AbstractInhabitantImpl<T> {
    private final Scope scope;

    public ScopedInhabitant(Womb<T> womb, Scope scope) {
        super(womb);
        this.scope = scope;
    }

    public T get() {
        ScopeInstance store = scope.current();
        // scope is extension point, so beware for the broken implementation
        assert store!=null : scope+" returned null";

        T o = store.get(this);
        if(o==null) {
            synchronized(this) {
                // to avoid creating multiple objects into the same scope, lock this object
                // verify no one else created one in the mean time
                o = store.get(this);
                if(o==null) {
                    o = womb.get();
                    store.put(this,o);
                }
            }
        }

        assert o!=null;
        return o;
    }

    public void release() {
        // noop
    }
}
