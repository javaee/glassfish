/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.jvnet.hk2.component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.utilities.general.GeneralUtilities;

/**
 * Map from a key to multiple values.
 * Order is significant among values, and null values are allowed, although null keys are not.
 *
 * @author Kohsuke Kawaguchi
 * @author Jerome Dochez
 * @param <K> The key of the multi-map
 * @param <V> The types in the multi-map
 */
public class MultiMap<K, V> implements Serializable, Cloneable {
    /**
     * For serialization
     */
    private static final long serialVersionUID = 893592003056170756L;
    
    private final Map<K, List<V>> store = new LinkedHashMap<K, List<V>>();

    /**
     * Creates an empty multi-map with default concurrency controls
     */
    public MultiMap() {
    }

    /**
     * Copy constructor.
     *
     * @param base map to copy
     */
    public MultiMap(MultiMap<K, V> base) {
        this();
        for (Map.Entry<K, List<V>> e : base.entrySet()) {
            List<V> value = newList(e.getValue());
            if (!value.isEmpty()) {
                store.put(e.getKey(), newList(e.getValue()));
            }
        }
    }

    /**
     * Creates an optionally populated list to be used as an entry in the map.
     *
     * @param initialVal
     * @return
     */
    private List<V> newList(Collection<? extends V> initialVals) {
        if (null == initialVals) {
            return new LinkedList<V>();
         }
        
         return new LinkedList<V>(initialVals);
    }
    
    /**
     * Returns the set of keys associated with this MultiMap
     * 
     * @return The set of keys currently available in this MultiMap.  Will not return null,
     * but may return a Set of lenght zero
     */
    public Set<K> keySet() {
        return store.keySet();
    }

    /**
     * Adds one more key-value pair.
     *
     * @param k key to store the entry under
     * @param v value to store in the k's values.
     */
    public final void add(K k, V v) {
        List<V> l = store.get(k);
        if (l == null) {
            l = newList(null);
            store.put(k, l);
        }
        l.add(v);
    }

    /**
     * Replaces all the existing values associated with the key
     * by the given value.  If v is empty the key k will
     * be removed from the MultiMap.
     *
     * @param k key for the values
     * @param v Can be null or empty.
     */
    public void set(K k, Collection<? extends V> v) {
        List<V> addMe = newList(v);
        if (addMe.isEmpty()) {
            store.remove(k);
        }
        else {
            store.put(k, newList(v));
        }
    }

    /**
     * Replaces all the existing values associated with the key
     * by the given single value.
     *
     * @param k key for the values
     * @param v singleton value for k key
     *          <p/>
     *          This is short for <tt>set(k,Collections.singleton(v))</tt>
     */
    public void set(K k, V v) {
        List<V> vlist = newList(null);
        vlist.add(v);
        store.put(k, vlist);
    }

    /**
     * Returns the elements indexed by the provided key
     *
     * @param k key for the values
     * @return Can be empty but never null. Read-only.
     */
    public final List<V> get(K k) {
        List<V> l = store.get(k);
        if (l == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(l);
    }
    
    /**
     * This method merges all of the keys and values from another
     * MultiMap into this MultiMap.  If a key/value pair is
     * found in both MultiMaps it is not re-added to this
     * MultiMap, but is instead discarded
     * 
     * @param another The MultiMap from which to add values
     * to this MultiMap.  If null this method does nothing
     */
    public void mergeAll(MultiMap<K, V> another) {
        if (another == null) return;
        
        for (Map.Entry<K, List<V>> entry : another.entrySet()) {
            List<V> ourList = store.get(entry.getKey());
            if (null == ourList) {
                ourList = newList(entry.getValue());
                if (!ourList.isEmpty()) {
                    store.put(entry.getKey(), ourList);
                }
            }
            else {
                for (V v : entry.getValue()) {
                    if (!ourList.contains(v)) {
                        ourList.add(v);
                    }
                }
            }
        }
    }

    /**
     * Package private (for getting the raw map for direct manipulation by the habitat)
     *
     * @param k the key
     * @return
     */
    private final List<V> _get(K k) {
        List<V> l = store.get(k);
        if (l == null) {
            return Collections.emptyList();
        }
        return l;
    }

    /**
     * Checks if the map contains the given key.
     *
     * @param k key to test
     * @return true if the map contains at least one element for this key
     */
    public boolean containsKey(K k) {
        return !get(k).isEmpty();
    }

    /**
     * Checks if the map contains the given key(s), also extending the search
     * to including the sub collection.
     *
     * @param k1 key from top collection
     * @param k2 key (value) from inner collection
     * @return true if the map contains at least one element for these keys
     */
    public boolean contains(K k1, V k2) {
        List<V> list = _get(k1);
        return list.contains(k2);
    }

    /**
     * Removes an key value from the map
     *
     * @param key key to be removed
     * @return the value stored under this key or null if there was none
     */
    public List<V> remove(K key) {
        return store.remove(key);
    }

    /**
     * Removes an key value pair from the map.  If the list of
     * entries for that key is empty after the remove
     * it will be removed from the set of keys
     *
     * @param key   key to be removed
     * @param entry the entry to be removed from the key'ed list
     * @return true if there was none that was deleted
     */
    public boolean remove(K key, V entry) {
        List<V> list = store.get(key);
        if (list == null) return false;
    
        boolean retVal = list.remove(entry);
        
        if (list.isEmpty()) {
            store.remove(key);
        }
        
        return retVal;
    }

    /**
     * Gets the first value if any, or null.
     * <p/>
     * This is useful when you know the given key only has one value and you'd like
     * to get to that value.
     *
     * @param k key for the values
     * @return null if the key has no values or it has a value but the value is null.
     */
    public V getOne(K k) {
        return getFirst(k);
    }

    private V getFirst(K k) {
        List<V> lst = store.get(k);
        if (null == lst) {
            return null;
        }

        if (lst.isEmpty()) {
            return null;
        }

        return lst.get(0);
    }

    /**
     * Lists up all entries.
     *
     * @return a {@link java.util.Set} of {@link java.util.Map.Entry} of entries
     */
    public Set<Map.Entry<K, List<V>>> entrySet() {
        return store.entrySet();
    }

    /**
     * @return the map as "key=value1,key=value2,...."
     */
    public String toCommaSeparatedString() {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<K, List<V>> e : entrySet()) {
            for (V v : e.getValue()) {
                if (buf.length() > 0) {
                    buf.append(',');
                }
                buf.append(e.getKey()).append('=').append(v);
            }
        }
        return buf.toString();
    }

    /**
     * Creates a copy of the map that contains the exact same key and value set.
     * Keys and values won't cloned.
     */
    @Override
    public MultiMap<K, V> clone() throws CloneNotSupportedException {
        super.clone();
        
        return new MultiMap<K, V>(this);
    }

    /**
     * Returns the size of the map.  This returns the numbers
     * of keys in the map, not the number of values
     *
     * @return integer or 0 if the map is empty
     */
    public int size() {
        return store.size();
    }
    
    @Override
    public int hashCode() {
        return store.hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object another) {
        if (another == null ||
                !(another instanceof MultiMap)) return false;
        
        MultiMap<K,V> other = (MultiMap<K,V>) another;
        
        return store.equals(other.store);
    }
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        final String newline = GeneralUtilities.getSystemProperty("line.separator", null);
        builder.append("{");
        for (final K key : store.keySet()) {
            builder.append(key).append(": ");
            builder.append(store.get(key));
            builder.append(newline);
        }
        builder.append("}");
        return builder.toString();
    }
}
