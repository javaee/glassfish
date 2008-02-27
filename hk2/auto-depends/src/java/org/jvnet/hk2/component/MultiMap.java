package org.jvnet.hk2.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Map from a key to multiple values.
 * Order is significant among values, and null values are allowed, although null keys are not.
 * 
 * @author Kohsuke Kawaguchi
 */
public final class MultiMap<K,V> {
    private final Map<K,List<V>> store;

    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        
        final String newline = System.getProperty( "line.separator" );
        builder.append( "{" );
        for ( final K key : store.keySet() ) {
            builder.append( key  + ": {" );
            for( final V value : store.get(key) ) {
                builder.append( value.toString() + "," );
            }
            // trailing comma is OK
            builder.append( "}" + newline );
        }
        builder.append( "}" );
        return builder.toString();
    }
    
    /**
     * Creates an empty multi-map.
     */
    public MultiMap() {
        store = new HashMap<K, List<V>>();
    }

    /**
     * Creates a multi-map backed by the given store.
     */
    private MultiMap(Map<K,List<V>> store) {
        this.store = store;
    }

    /**
     * Adds one more value.
     */
    public final void add(K k,V v) {
        List<V> l = store.get(k);
        if(l==null) {
            l = new ArrayList<V>();
            store.put(k,l);
        }
        l.add(v);
    }

    /**
     * Replaces all the existing values associated with the key
     * by the given value.
     *
     * @param v
     *      Can be null or empty.
     */
    public void set(K k, List<V> v) {
        store.put(k,new ArrayList<V>(v));
    }

    /**
     * @return
     *      Can be empty but never null. Read-only.
     */
    public final List<V> get(K k) {
        List<V> l = store.get(k);
        if(l==null) return Collections.emptyList();
        return l;
    }

    /**
     * Checks if the map contains the given key.
     */
    public boolean containsKey(K key) {
        return !get(key).isEmpty();
    }

    /**
     * Gets the first value if any, or null.
     * <p>
     * This is useful when you know the given key only has one value and you'd like
     * to get to that value.
     *
     * @return
     *      null if the key has no values or it has a value but the value is null.
     */
    public final V getOne(K k) {
        List<V> lst = store.get(k);
        if(lst ==null)  return null;
        if(lst.isEmpty())   return null;
        return lst.get(0);
    }

    /**
     * Lists up all entries.
     */
    public Set<Entry<K,List<V>>> entrySet() {
        return store.entrySet();
    }

    /**
     * Format the map as "key=value1,key=value2,...."
     */
    public String toCommaSeparatedString() {
        StringBuilder buf = new StringBuilder();
        for (Entry<K,List<V>> e : entrySet()) {
            for (V v : e.getValue()) {
                if(buf.length()>0)  buf.append(',');
                buf.append(e.getKey()).append('=').append(v);
            }
        }
        return buf.toString();
    }

    /**
     * Creates a copy of the map that contains the exact same key and value set.
     * Keys and values won't cloned.
     */
    public MultiMap<K,V> clone() {
        MultiMap<K,V> m = new MultiMap<K,V>();
        for (Entry<K, List<V>> e : store.entrySet())
            m.store.put(e.getKey(),new ArrayList<V>(e.getValue()));
        return m;
    }

    private static final MultiMap EMPTY = new MultiMap(Collections.emptyMap());

    /**
     * Gets the singleton read-only empty multi-map.
     *
     * @see Collections#emptyMap()
     */
    public static <K,V> MultiMap<K,V> emptyMap() {
        return EMPTY;
    }
}
