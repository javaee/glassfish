package com.sun.enterprise.module.common_impl;

import java.util.*;

/**
 * Equivalent of FlattenIterator for enumeration
 * @author Jerome Dochez
 */
public class FlattenEnumeration<T> implements Enumeration<T> {

    private Enumeration<? extends T> current;
    private Enumeration<? extends Enumeration<? extends T>> sources;

    public FlattenEnumeration(Enumeration<Enumeration<T>> sources) {
        this.sources = sources;
        if (sources.hasMoreElements()) {
            current = sources.nextElement();
        }  else {
            current = Collections.enumeration((List<T>) Collections.emptyList());
        }
    }
    public boolean hasMoreElements() {
        while(!current.hasMoreElements() && sources.hasMoreElements()) {
             current = sources.nextElement();
         }
         return current.hasMoreElements();

    }

    public T nextElement() {
        if(hasMoreElements())
            return current.nextElement();
        else
            throw new NoSuchElementException();
    }
}
