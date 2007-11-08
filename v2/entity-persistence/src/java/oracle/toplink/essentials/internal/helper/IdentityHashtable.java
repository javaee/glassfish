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
 * <b>Purpose</b>: Define a {@link Hashtable} that manages key equality by
 * reference, not equals(). This is required to track objects throughout the
 * lifecycle of a {@link oracle.toplink.essentials.sessions.UnitOfWork}, regardless if the
 * domain object redefines its equals() method. Additionally, this implementation
 * does <b>not</b> permit nulls.
 */

// J2SE imports
import java.io.*;
import java.util.*;

public class IdentityHashtable extends Dictionary implements Cloneable, Serializable {
    static final long serialVersionUID = 1421746759512286392L;

    // the default initial capacity
    static final int DEFAULT_INITIAL_CAPACITY = 32;

    // the maximum capacity.
    static final int MAXIMUM_CAPACITY = 1 << 30;

    // the loadFactor used when none specified in constructor.
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /** An "enum" of Enumeration types. */
    static final int KEYS = 0;

    /** An "enum" of Enumeration types. */
    static final int ELEMENTS = 1;
    private static EmptyEnumerator emptyEnumerator = new EmptyEnumerator();
    protected transient Entry[] entries;// internal array of Entry's
    protected transient int count = 0;
    protected int threshold = 0;
    protected float loadFactor = 0;

    /**
     * Constructs a new <tt>IdentityHashtable</tt> with the given
     * initial capacity and the given loadFactor.
     *
     * @param initialCapacity the initial capacity of this
     * <tt>IdentityHashtable</tt>.
     * @param loadFactor the loadFactor of the <tt>IdentityHashtable</tt>.
     * @throws IllegalArgumentException  if the initial capacity is less
     * than zero, or if the loadFactor is nonpositive.
     */
    public IdentityHashtable(int initialCapacity, float loadFactor) {
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
     * Constructs a new <tt>IdentityHashtable</tt> with the given
     * initial capacity and a default loadFactor of <tt>0.75</tt>.
     *
     * @param initialCapacity the initial capacity of the
     * <tt>IdentityHashtable</tt>.
     * @throws <tt>IllegalArgumentException</tt> if the initial capacity is less
     * than zero.
     */
    public IdentityHashtable(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new <tt>IdentityHashtable</tt> with a default initial
     * capacity of <tt>32</tt> and a loadfactor of <tt>0.75</tt>.
     */
    public IdentityHashtable() {
        loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        entries = new Entry[DEFAULT_INITIAL_CAPACITY];
    }

    /**
     * Removes all of the mappings from this <tt>IdentityHashtable</tt>.
     */
    public synchronized void clear() {
        if (count > 0) {
            Entry[] copyOfEntries = entries;
            for (int i = copyOfEntries.length; --i >= 0;) {
                copyOfEntries[i] = null;
            }
            count = 0;
        }
    }

    /**
     * Returns a shallow copy of this <tt>IdentityHashtable</tt> (the
     * elements are not cloned).
     *
     * @return a shallow copy of this <tt>IdentityHashtable</tt>.
     */
    public synchronized Object clone() {
        try {
            Entry[] copyOfEntries = entries;
            IdentityHashtable clone = (IdentityHashtable)super.clone();
            clone.entries = new Entry[copyOfEntries.length];
            for (int i = copyOfEntries.length; i-- > 0;) {
                clone.entries[i] = (copyOfEntries[i] != null) ? (Entry)copyOfEntries[i].clone() : null;
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Returns <tt>true</tt> if this <tt>IdentityHashtable</tt> contains
     * the given object. Equality is tested by the equals() method.
     *
     * @param obj the object to find.
     * @return <tt>true</tt> if this <tt>IdentityHashtable</tt> contains
     * obj.
     * @throws <tt>NullPointerException</tt> if obj is null</tt>.
     */
    public synchronized boolean contains(Object obj) {
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
     * Returns <tt>true</tt> if this <tt>IdentityHashtable</tt> contains a
     * mapping for the given key. Equality is tested by reference.
     *
     * @param key object to be used as a key into this
     * <tt>IdentityHashtable</tt>.
     * @return <tt>true</tt> if this <tt>IdentityHashtable</tt> contains a
     * mapping for key.
     */
    public synchronized boolean containsKey(Object key) {
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

    public synchronized Enumeration elements() {
        if (count == 0) {
            return emptyEnumerator;
        } else {
            return new Enumerator(ELEMENTS);
        }
    }

    /**
     * Returns the value to which the given key is mapped in this
     * <tt>IdentityHashtable</tt>. Returns <tt>null</tt> if this
     * <tt>IdentityHashtable</tt> contains no mapping for this key.
     *
     * @return the value to which this <tt>IdentityHashtable</tt> maps the
     * given key.
     * @param key key whose associated value is to be returned.
     */
    public synchronized Object get(Object key) {
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
     * @return <tt>true</tt> if this <tt>IdentityHashtable</tt> is empty.
     */
    public boolean isEmpty() {
        return (count == 0);
    }

    public synchronized Enumeration keys() {
        if (count == 0) {
            return emptyEnumerator;
        } else {
            return new Enumerator(KEYS);
        }
    }

    /**
     * Associate the given object with the given key in this
     * <tt>IdentityHashtable</tt>, replacing any existing mapping.
     *
     * @param key key to map to given object.
     * @param obj object to be associated with key.
     * @return the previous object for key or <tt>null</tt> if this
     * <tt>IdentityHashtable</tt> did not have one.
     * @throws <tt>NullPointerException</tt> if obj is null</tt>.
     */
    public synchronized Object put(Object key, Object obj) {
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
     * INTERNAL:
     * Re-builds the internal array of Entry's with a larger capacity.
     * This method is called automatically when the number of objects in this
     * IdentityHashtable exceeds its current threshold.
     */
    private void rehash() {
        int oldCapacity = entries.length;
        Entry[] oldEntries = entries;
        int newCapacity = (oldCapacity * 2) + 1;
        Entry[] newEntries = new Entry[newCapacity];
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
     * Removes the mapping (key and its corresponding value) from this
     * <tt>IdentityHashtable</tt>, if present.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return the previous object for key or <tt>null</tt> if this
     * <tt>IdentityHashtable</tt> did not have one.
     */
    public synchronized Object remove(Object key) {
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
     * @return the size of this <tt>IdentityHashtable</tt>.
     */
    public int size() {
        return count;
    }

    /**
     * Return the string representation of this <tt>IdentityHashtable</tt>.
     *
     * @return the string representation of this <tt>IdentityHashtable</tt>.
     */
    public synchronized String toString() {
        int max = size() - 1;
        StringBuffer buf = new StringBuffer();
        Enumeration k = keys();
        Enumeration e = elements();
        buf.append("{");
        for (int i = 0; i <= max; i++) {
            String s1 = k.nextElement().toString();
            String s2 = e.nextElement().toString();
            buf.append(s1 + "=" + s2);
            if (i < max) {
                buf.append(", ");
            }
        }
        buf.append("}");
        return buf.toString();
    }

    /**
     * Serialize the state of this <tt>IdentityHashtable</tt> to a stream.
     *
     * @serialData The <i>capacity</i> of the <tt>IdentityHashtable</tt>
     * (the length of the bucket array) is emitted (int), followed by the
     * <i>size</i> of the <tt>IdentityHashtable</tt>, followed by the
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
     * Deserialize the <tt>IdentityHashtable</tt> from a stream.
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

    private static class EmptyEnumerator implements Enumeration {
        EmptyEnumerator() {
        }

        public boolean hasMoreElements() {
            return false;
        }

        public Object nextElement() {
            throw new NoSuchElementException();
        }
    }

    class Enumerator implements Enumeration {
        int enumeratorType;
        int index;
        Entry entry;

        Enumerator(int enumeratorType) {
            this.enumeratorType = enumeratorType;
            index = IdentityHashtable.this.entries.length;
        }

        public boolean hasMoreElements() {
            if (entry != null) {
                return true;
            }
            while (index-- > 0) {
                if ((entry = IdentityHashtable.this.entries[index]) != null) {
                    return true;
                }
            }
            return false;
        }

        public Object nextElement() {
            if (entry == null) {
                while ((index-- > 0) && ((entry = IdentityHashtable.this.entries[index]) == null)) {
                    ;
                }
            }
            if (entry != null) {
                Entry e = entry;
                entry = e.next;
                if (enumeratorType == KEYS) {
                    return e.key;
                } else {
                    return e.value;
                }
            }
            throw new NoSuchElementException("IdentityHashtable.Enumerator");
        }
    }

    /**
     * IdentityHashtable entry.
     */
    private static class Entry {
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
            if (!(o instanceof Entry)) {
                return false;
            }

            Entry e = (Entry)o;
            return (key == e.getKey()) && ((value == null) ? (e.getValue() == null) : value.equals(e.getValue()));
        }

        public int hashCode() {
            return hash ^ ((value == null) ? 0 : value.hashCode());
        }

        public String toString() {
            return key + "=" + value;
        }
    }
}
