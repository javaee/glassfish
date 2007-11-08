/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.helper;


/**
 * INTERNAL:
 * <p>
 * <b>Purpose</b>: Define a {@link Map} that manages key equality by reference,
 * not equals(). This is required to track objects throughout the lifecycle
 * of a {@link oracle.toplink.essentials.sessions.UnitOfWork}, regardless if the domain
 * object redefines its equals() method. Additionally, this implementation does
 * <b>not</b> permit nulls.
 *
 * @author Mike Norman (since TopLink 10.1.3)
 *
 */

// J2SE imports
import java.io.*;
import java.util.*;

public class TopLinkIdentityHashMap extends AbstractMap implements Map, Cloneable, Serializable {
    static final long serialVersionUID = -5176951017503351630L;

    // the default initial capacity
    static final int DEFAULT_INITIAL_CAPACITY = 32;

    // the maximum capacity.
    static final int MAXIMUM_CAPACITY = 1 << 30;

    // the loadFactor used when none specified in constructor.
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    protected transient Entry[] entries;// internal array of Entry's
    protected transient int count = 0;
    private transient int modCount = 0;// # of times this Map has been modified
    protected int threshold = 0;
    protected float loadFactor = 0;

    /**
     * Constructs a new <tt>TopLinkIdentityHashMap</tt> with the given
     * initial capacity and the given loadFactor.
     *
     * @param initialCapacity the initial capacity of this
     * <tt>TopLinkIdentityHashMap</tt>.
     * @param loadFactor the loadFactor of the <tt>TopLinkIdentityHashMap</tt>.
     * @throws IllegalArgumentException  if the initial capacity is less
     * than zero, or if the loadFactor is nonpositive.
     */
    public TopLinkIdentityHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initialCapacity: " + initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if ((loadFactor <= 0) || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal loadFactor: " + loadFactor);
        }

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity) {
            capacity <<= 1;
        }
        this.loadFactor = loadFactor;
        threshold = (int)(capacity * loadFactor);
        entries = new Entry[capacity];
    }

    /**
     * Constructs a new <tt>TopLinkIdentityHashMap</tt> with the given
     * initial capacity and a default loadFactor of <tt>0.75</tt>.
     *
     * @param initialCapacity the initial capacity of the
     * <tt>TopLinkIdentityHashMap</tt>.
     * @throws <tt>IllegalArgumentException</tt> if the initial capacity is less
     * than zero.
     */
    public TopLinkIdentityHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new <tt>TopLinkIdentityHashMap</tt> with a default initial
     * capacity of <tt>32</tt> and a loadfactor of <tt>0.75</tt>.
     */
    public TopLinkIdentityHashMap() {
        loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        entries = new Entry[DEFAULT_INITIAL_CAPACITY];
    }

    /**
     * Constructs a new <tt>TopLinkIdentityHashMap</tt> with the same mappings
     * as the given map.  The <tt>TopLinkIdentityHashMap</tt> is created with a
     * capacity sufficient to hold the elements of the given map.
     *
     * @param m the map whose mappings are to be placed in the
     * <tt>TopLinkIdentityHashMap</tt>.
     */
    public TopLinkIdentityHashMap(Map m) {
        this(Math.max((int)(m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAll(m);
    }

    /**
     * @return the size of this <tt>TopLinkIdentityHashMap</tt>.
     */
    public int size() {
        return count;
    }

    /**
     * @return <tt>true</tt> if this <tt>TopLinkIdentityHashMap</tt> is empty.
     */
    public boolean isEmpty() {
        return (count == 0);
    }

    /**
     * Returns <tt>true</tt> if this <tt>TopLinkIdentityHashMap</tt> contains
     * the given object. Equality is tested by the equals() method.
     *
     * @param obj the object to find.
     * @return <tt>true</tt> if this <tt>TopLinkIdentityHashMap</tt> contains
     * obj.
     * @throws <tt>NullPointerException</tt> if obj is null</tt>.
     */
    public boolean containsValue(Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }

        Entry[] copyOfEntries = entries;
        for (int i = copyOfEntries.length; i-- > 0;) {
            for (Entry e = copyOfEntries[i]; e != null; e = e.next) {
                if (e.value.equals(obj)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns <tt>true</tt> if this <tt>TopLinkIdentityHashMap</tt> contains a
     * mapping for the given key. Equality is tested by reference.
     *
     * @param key object to be used as a key into this
     * <tt>TopLinkIdentityHashMap</tt>.
     * @return <tt>true</tt> if this <tt>TopLinkIdentityHashMap</tt> contains a
     * mapping for key.
     */
    public boolean containsKey(Object key) {
        Entry[] copyOfEntries = entries;
        int hash = System.identityHashCode(key);
        int index = (hash & 0x7FFFFFFF) % copyOfEntries.length;
        for (Entry e = copyOfEntries[index]; e != null; e = e.next) {
            if (e.key == key) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value to which the given key is mapped in this
     * <tt>TopLinkIdentityHashMap</tt>. Returns <tt>null</tt> if this
     * <tt>TopLinkIdentityHashMap</tt> contains no mapping for this key.
     *
     * @return the value to which this <tt>TopLinkIdentityHashMap</tt> maps the
     * given key.
     * @param key key whose associated value is to be returned.
     */
    public Object get(Object key) {
        Entry[] copyOfEntries = entries;
        int hash = System.identityHashCode(key);
        int index = (hash & 0x7FFFFFFF) % copyOfEntries.length;
        for (Entry e = copyOfEntries[index]; e != null; e = e.next) {
            if (e.key == key) {
                return e.value;
            }
        }
        return null;
    }

    /**
     * INTERNAL:
     * Re-builds the internal array of Entry's with a larger capacity.
     * This method is called automatically when the number of objects in this
     * TopLinkIdentityHashMap exceeds its current threshold.
     */
    private void rehash() {
        int oldCapacity = entries.length;
        Entry[] oldEntries = entries;
        int newCapacity = (oldCapacity * 2) + 1;
        Entry[] newEntries = new Entry[newCapacity];
        modCount++;
        threshold = (int)(newCapacity * loadFactor);
        entries = newEntries;
        for (int i = oldCapacity; i-- > 0;) {
            for (Entry old = oldEntries[i]; old != null;) {
                Entry e = old;
                old = old.next;
                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = newEntries[index];
                newEntries[index] = e;
            }
        }
    }

    /**
     * Associate the given object with the given key in this
     * <tt>TopLinkIdentityHashMap</tt>, replacing any existing mapping.
     *
     * @param key key to map to given object.
     * @param obj object to be associated with key.
     * @return the previous object for key or <tt>null</tt> if this
     * <tt>TopLinkIdentityHashMap</tt> did not have one.
     * @throws <tt>NullPointerException</tt> if obj is null</tt>.
     */
    public Object put(Object key, Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }

        Entry[] copyOfEntries = entries;
        int hash = System.identityHashCode(key);
        int index = (hash & 0x7FFFFFFF) % copyOfEntries.length;
        for (Entry e = copyOfEntries[index]; e != null; e = e.next) {
            if (e.key == key) {
                Object old = e.value;
                e.value = obj;
                return old;
            }
        }

        modCount++;
        if (count >= threshold) {
            rehash();
            copyOfEntries = entries;
            index = (hash & 0x7FFFFFFF) % copyOfEntries.length;
        }
        Entry e = new Entry(hash, key, obj, copyOfEntries[index]);
        copyOfEntries[index] = e;
        count++;
        return null;
    }

    /**
     * Removes the mapping (key and its corresponding value) from this
     * <tt>TopLinkIdentityHashMap</tt>, if present.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return the previous object for key or <tt>null</tt> if this
     * <tt>TopLinkIdentityHashMap</tt> did not have one.
     */
    public Object remove(Object key) {
        Entry[] copyOfEntries = entries;
        int hash = System.identityHashCode(key);
        int index = (hash & 0x7FFFFFFF) % copyOfEntries.length;
        for (Entry e = copyOfEntries[index], prev = null; e != null; prev = e, e = e.next) {
            if (e.key == key) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    copyOfEntries[index] = e.next;
                }
                count--;
                return e.value;
            }
        }
        return null;
    }

    /**
     * Copies all of the mappings from the given map to this
     * <tt>TopLinkIdentityHashMap</tt>, replacing any existing mappings.
     *
     * @param m mappings to be stored in this <tt>TopLinkIdentityHashMap</tt>.
     * @throws <tt>NullPointerException</tt> if m is null.
     */
    public void putAll(Map m) {
        if (m == null) {
            throw new NullPointerException();
        }

        Iterator i = m.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            put(me.getKey(), me.getValue());
        }
    }

    /**
     * Removes all of the mappings from this <tt>TopLinkIdentityHashMap</tt>.
     */
    public void clear() {
        if (count > 0) {
            modCount++;
            Entry[] copyOfEntries = entries;
            for (int i = copyOfEntries.length; --i >= 0;) {
                copyOfEntries[i] = null;
            }
            count = 0;
        }
    }

    /**
     * Returns a shallow copy of this <tt>TopLinkIdentityHashMap</tt> (the
     * elements are not cloned).
     *
     * @return a shallow copy of this <tt>TopLinkIdentityHashMap</tt>.
     */
    public Object clone() {
        try {
            Entry[] copyOfEntries = entries;
            TopLinkIdentityHashMap clone = (TopLinkIdentityHashMap)super.clone();
            clone.entries = new Entry[copyOfEntries.length];
            for (int i = copyOfEntries.length; i-- > 0;) {
                clone.entries[i] = (copyOfEntries[i] != null) ? (Entry)copyOfEntries[i].clone() : null;
            }
            clone.keySet = null;
            clone.entrySet = null;
            clone.values = null;
            clone.modCount = 0;
            return clone;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    // Views - the following is standard 'boiler-plate' Map stuff
    private transient Set keySet = null;
    private transient Set entrySet = null;
    private transient Collection values = null;

    /**
     * Returns a set view of the keys contained in this
     * <tt>TopLinkIdentityHashMap</tt>.  The set is backed by the map, so
     * changes to the map are reflected in the set, and vice versa.  The set
     * supports element removal, which removes the corresponding mapping from
     * this map, via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the keys contained in this
     * <tt>TopLinkIdentityHashMap</tt>.
     */
    public Set keySet() {
        if (keySet == null) {
            keySet = new AbstractSet() {
                        public Iterator iterator() {
                            return getHashIterator(KEYS);
                        }

                        public int size() {
                            return count;
                        }

                        public boolean contains(Object o) {
                            return containsKey(o);
                        }

                        public boolean remove(Object o) {
                            int oldSize = count;
                            TopLinkIdentityHashMap.this.remove(o);
                            return count != oldSize;
                        }

                        public void clear() {
                            TopLinkIdentityHashMap.this.clear();
                        }
                    };
        }
        return keySet;
    }

    /**
     * Returns a collection view of the values contained in this
     * <tt>TopLinkIdentityHashMap</tt>.  The collection is backed by the map, so
     * changes to the map are reflected in the collection, and vice versa.  The
     * collection supports element removal, which removes the corresponding
     * mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this
     * <tt>TopLinkIdentityHashMap</tt>.
     */
    public Collection values() {
        if (values == null) {
            values = new AbstractCollection() {
                        public Iterator iterator() {
                            return getHashIterator(VALUES);
                        }

                        public int size() {
                            return count;
                        }

                        public boolean contains(Object o) {
                            return containsValue(o);
                        }

                        public void clear() {
                            TopLinkIdentityHashMap.this.clear();
                        }
                    };
        }
        return values;
    }

    /**
     * Returns a collection view of the mappings contained in this
     * <tt>TopLinkIdentityHashMap</tt>.  Each element in the returned collection
     * is a <tt>Map.Entry</tt>.  The collection is backed by the map, so changes
     * to the map are reflected in the collection, and vice versa.  The
     * collection supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * @return a collection view of the mappings contained in this
     * <tt>TopLinkIdentityHashMap</tt>.
     */
    public Set entrySet() {
        if (entrySet == null) {
            entrySet = new AbstractSet() {
                        public Iterator iterator() {
                            return getHashIterator(ENTRIES);
                        }

                        public boolean contains(Object o) {
                            if (!(o instanceof Map.Entry)) {
                                return false;
                            }

                            Map.Entry entry = (Map.Entry)o;
                            Object key = entry.getKey();
                            Entry[] copyOfEntries = entries;
                            int hash = System.identityHashCode(key);
                            int index = (hash & 0x7FFFFFFF) % copyOfEntries.length;
                            for (Entry e = copyOfEntries[index]; e != null; e = e.next) {
                                if ((e.hash == hash) && e.equals(entry)) {
                                    return true;
                                }
                            }
                            return false;
                        }

                        public boolean remove(Object o) {
                            if (!(o instanceof Map.Entry)) {
                                return false;
                            }

                            Map.Entry entry = (Map.Entry)o;
                            Object key = entry.getKey();
                            Entry[] copyOfEntries = entries;
                            int hash = System.identityHashCode(key);
                            int index = (hash & 0x7FFFFFFF) % copyOfEntries.length;
                            for (Entry e = copyOfEntries[index], prev = null; e != null;
                                     prev = e, e = e.next) {
                                if ((e.hash == hash) && e.equals(entry)) {
                                    modCount++;
                                    if (prev != null) {
                                        prev.next = e.next;
                                    } else {
                                        copyOfEntries[index] = e.next;
                                    }
                                    count--;
                                    e.value = null;
                                    return true;
                                }
                            }
                            return false;
                        }

                        public int size() {
                            return count;
                        }

                        public void clear() {
                            TopLinkIdentityHashMap.this.clear();
                        }
                    };
        }
        return entrySet;
    }

    private Iterator getHashIterator(int type) {
        if (count == 0) {
            return emptyHashIterator;
        } else {
            return new HashIterator(type);
        }
    }

    /**
     * TopLinkIdentityHashMap entry.
     */
    private static class Entry implements Map.Entry {
        int hash;
        Object key;
        Object value;
        Entry next;

        Entry(int hash, Object key, Object value, Entry next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        protected Object clone() {
            return new Entry(hash, key, value, ((next == null) ? null : (Entry)next.clone()));
        }

        // Map.Entry Ops
        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object value) {
            Object oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }

            Map.Entry e = (Map.Entry)o;
            return (key == e.getKey()) && ((value == null) ? (e.getValue() == null) : value.equals(e.getValue()));
        }

        public int hashCode() {
            return hash ^ ((value == null) ? 0 : value.hashCode());
        }

        public String toString() {
            return key + "=" + value;
        }
    }

    // Types of Iterators
    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;
    private static EmptyHashIterator emptyHashIterator = new EmptyHashIterator();

    private static class EmptyHashIterator implements Iterator {
        EmptyHashIterator() {
        }

        public boolean hasNext() {
            return false;
        }

        public Object next() {
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new IllegalStateException();
        }
    }

    private class HashIterator implements Iterator {
        Entry[] entries = TopLinkIdentityHashMap.this.entries;
        int index = entries.length;
        Entry entry = null;
        Entry lastReturned = null;
        int type;

        /**
         * The modCount value that the iterator believes that the backing
         * List should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        private int expectedModCount = modCount;

        HashIterator(int type) {
            this.type = type;
        }

        public boolean hasNext() {
            Entry e = entry;
            int i = index;
            Entry[] copyOfEntries = TopLinkIdentityHashMap.this.entries;
            while ((e == null) && (i > 0)) {
                e = copyOfEntries[--i];
            }
            entry = e;
            index = i;
            return e != null;
        }

        public Object next() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }

            Entry et = entry;
            int i = index;
            Entry[] copyOfEntries = TopLinkIdentityHashMap.this.entries;
            while ((et == null) && (i > 0)) {
                et = copyOfEntries[--i];
            }
            entry = et;
            index = i;
            if (et != null) {
                Entry e = lastReturned = entry;
                entry = e.next;
                return (type == KEYS) ? e.key : ((type == VALUES) ? e.value : e);
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }

            Entry[] copyOfEntries = TopLinkIdentityHashMap.this.entries;
            int index = (lastReturned.hash & 0x7FFFFFFF) % copyOfEntries.length;
            for (Entry e = copyOfEntries[index], prev = null; e != null; prev = e, e = e.next) {
                if (e == lastReturned) {
                    modCount++;
                    expectedModCount++;
                    if (prev == null) {
                        copyOfEntries[index] = e.next;
                    } else {
                        prev.next = e.next;
                    }
                    count--;
                    lastReturned = null;
                    return;
                }
            }
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Serialize the state of this <tt>TopLinkIdentityHashMap</tt> to a stream.
     *
     * @serialData The <i>capacity</i> of the <tt>TopLinkIdentityHashMap</tt>
     * (the length of the bucket array) is emitted (int), followed by the
     * <i>size</i> of the <tt>TopLinkIdentityHashMap</tt>, followed by the
     * key-value mappings (in no particular order).
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        // Write out the threshold, loadfactor (and any hidden 'magic' stuff).
        s.defaultWriteObject();

        // Write out number of buckets
        s.writeInt(entries.length);
        // Write out count
        s.writeInt(count);
        // Write out contents
        for (int i = entries.length - 1; i >= 0; i--) {
            Entry entry = entries[i];
            while (entry != null) {
                s.writeObject(entry.key);
                s.writeObject(entry.value);
                entry = entry.next;
            }
        }
    }

    /**
     * Deserialize the <tt>TopLinkIdentityHashMap</tt> from a stream.
     */
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        // Read in the threshold, loadfactor (and any hidden 'magic' stuff).
        s.defaultReadObject();

        // Read in number of buckets and allocate the bucket array;
        int numBuckets = s.readInt();
        entries = new Entry[numBuckets];
        // Read in size (count)
        int size = s.readInt();

        // Read the mappings and add to the TopLinkIdentityHashMap
        for (int i = 0; i < size; i++) {
            Object key = s.readObject();
            Object value = s.readObject();
            put(key, value);
        }
    }
}
