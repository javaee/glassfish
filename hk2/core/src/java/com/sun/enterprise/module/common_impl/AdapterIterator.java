package com.sun.enterprise.module.common_impl;

import java.util.Iterator;

/**
 * {@link Iterator} implementation that works as a filter to another iterator.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AdapterIterator<T,U> implements Iterator<T> {
    private final Iterator<? extends U> core;
    private T next;

    public AdapterIterator(Iterator<? extends U> core) {
        this.core = core;
    }

    public T next() {
        fetch();
        T r = next;
        next=null;
        return r;
    }

    public boolean hasNext() {
        fetch();
        return next!=null;
    }

    private void fetch() {
        while(next==null && core.hasNext())
            next = adapt(core.next());
    }

    public void remove() {
        core.remove();
    }

    /**
     *
     * @return
     *      null to filter out this object. Non-null object will
     *      be returned from the iterator to the caller.
     */
    protected abstract T adapt(U u);
}
