package com.sun.enterprise.universal;

import java.util.*;

/**
 * This class is needed so often in so many places.  It is centralized once and
 * for all here.
 * @param K key type
 * @param V value type
 * @author bnevins
 */
public class NameValue<K, V> {
    /**
     * 
     * @param k key
     * @param v value
     */
    public NameValue(K k, V v)
    {
        if(k == null)
            throw new NullPointerException();
        
        value = v;
        key = k;
    }

    public K getName    () {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V newValue) {
        V oldValue = value;
        value = newValue;
        return oldValue;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NameValue))
            return false;
        
        NameValue nv = (NameValue) o;
        Object k1 = getName();
        Object k2 = nv.getName();
        if (k1 == k2 || k1.equals(k2)) {
            Object v1 = getValue();
            Object v2 = nv.getValue();
            
            if (v1 == v2 || (v1 != null && v1.equals(v2)))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode() ^ (value == null ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
        return getName() + "=" + getValue();
    }

    private final K key;
    private V value;
}

