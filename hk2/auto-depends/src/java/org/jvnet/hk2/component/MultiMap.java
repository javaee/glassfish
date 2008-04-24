/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Collection;

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
     * Copy constructor.
     */
    public MultiMap(MultiMap<K,V> base) {
        this();
        for (Entry<K, List<V>> e : base.entrySet())
            store.put(e.getKey(),new ArrayList<V>(e.getValue()));
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
    public void set(K k, Collection<? extends V> v) {
        store.put(k,new ArrayList<V>(v));
    }

    /**
     * Replaces all the existing values associated wit hthe key
     * by the given single value.
     *
     * <p>
     * This is short for <tt>set(k,Collections.singleton(v))</tt>
     */
    public void set(K k, V v) {
        ArrayList<V> vlist = new ArrayList<V>(1);
        vlist.add(v);
        store.put(k, vlist);
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
        return new MultiMap<K,V>(this);
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
