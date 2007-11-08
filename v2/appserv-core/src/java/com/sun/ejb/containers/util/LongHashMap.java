/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.ejb.containers.util;

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.security.*;


public class LongHashMap {

    protected static final int			DEFAULT_CAPACITY = 128;
    protected static final float		DEFAULT_LOAD_FACTOR = 0.75f;
    protected static final int			MAXIMUM_CAPACITY = 1 << 30;

    protected static boolean 			debug = false;

    protected transient Entry[]			table;
    protected float			        loadFactor;
    protected transient int			size;
    protected int				bucketmask;
    protected int				capacity;
    protected int			        threshold;

    public LongHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public LongHashMap(int initialCapacity) {
       this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public LongHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: "
                                               + loadFactor);
        }

        // Find a power of 2 >= initialCapacity
        capacity = 1;
        
        while (capacity < initialCapacity) {
            capacity <<= 1;
        }
        this.loadFactor = loadFactor;
        this.threshold = (int)(capacity * loadFactor);
        this.table = new Entry[capacity];
        this.bucketmask = capacity - 1;
    }
    

    public boolean contains(long key) {
        int index = ((int) key) & bucketmask;
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.key == key) {
                return true;
            }
        }
        return false;
    }
    
    public Object get(long key) {
        int index = ((int) key) & bucketmask;
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.key == key) {
                return e.value;
            }
        }
        return null;
    }
    
    public Object put(long key, Object value) {
        int index = ((int) key) & bucketmask;
        
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.key == key) {
                Object oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        
        table[index] = new Entry(key, value, table[index]);
        if (size++ >= threshold) {
            int newCapacity = 2 * capacity;
            Entry[] newTable = new Entry[newCapacity];
            transfer(newTable);
            table = newTable;
            capacity = newCapacity;
            threshold = (int)(newCapacity * loadFactor);
            bucketmask = capacity-1;
        }
        return null;
    }
    
    /** 
     * Transfer all entries from current table to newTable.
     */
    private void transfer(Entry[] newTable) {
        Entry[] src = table;
        int newCapacity = newTable.length;
        int bucketmask = newCapacity-1;
        for (int j = 0; j < src.length; j++) {
            Entry e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    Entry next = e.next;
                    int index = ((int) e.key) & bucketmask;
                    e.next = newTable[index];
                    newTable[index] = e;
                    e = next;
                } while (e != null);
            }
        }
    }
    
    public Object remove(long key) {
        int index = ((int) key) & bucketmask;
        Entry prev = table[index];
        Entry e = prev;
        
        while (e != null) {
            Entry next = e.next;
            if (e.key == key) {
                size--;
                if (prev == e)  {
                    table[index] = next;
                } else {
                    prev.next = next;
                }
                return e.value;
            }
            prev = e;
            e = next;
        }
        
        return null;
    }
    
    public Enumeration elements() {
        Vector keyList = new Vector();
        for (int index=0; index<capacity; index++) {
            for (Entry e = table[index]; e != null; e = e.next) {
                keyList.addElement(e.value);                   
            }
        }
        
        return keyList.elements();
    }
    
    public Iterator values() {
        ArrayList keyList = new ArrayList();
        for (int index=0; index<capacity; index++) {
            for (Entry e = table[index]; e != null; e = e.next) {
                keyList.add(e.value);                   
            }
        }
        
        return keyList.iterator();
    }
    
    public Iterator keys() {
        ArrayList keyList = new ArrayList();
        for (int index=0; index<capacity; index++) {
            for (Entry e = table[index]; e != null; e = e.next) {
                keyList.add(new Long(e.key));
            }
        }
        
        return keyList.iterator();
    }
    
    static class Entry {
        final long key;
        Object value;
        Entry next;
        
        /**
         * Create new entry.
         */
        Entry(long k, Object v, Entry n) { 
            key = k;
            value = v; 
            next = n;
        }
        
        public int hashCode() {
            return ((int) key);
        }
        
        public String toString() {
            return key + "=" + value;
        }
    }
}
