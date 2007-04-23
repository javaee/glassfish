package com.sun.enterprise.module.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * {@link Iterator} implementation that works like Lisp "flatten" function.
 * @author Kohsuke Kawaguchi
 */
public class FlattenIterator<T> implements Iterator<T> {
    private Iterator<? extends T> current;
    private Iterator<? extends Iterator<? extends T>> source;

    public FlattenIterator(Iterator<? extends Iterator<? extends T>> source) {
        this.source = source;
        if (source.hasNext()) {
            current = source.next();
        } else {
            current = ((List<T>) Collections.emptyList()).iterator();
        }
    }

    public boolean hasNext() {
        while(!current.hasNext() && source.hasNext()) {
            current = source.next();
        }
        return current.hasNext();
    }

    public T next() {
        if(hasNext())
            return current.next();
        else
            throw new NoSuchElementException();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
