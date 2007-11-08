/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/** Set which holds its members by using of WeakReferences.
* MT level: unsafe.
 * <p><strong>Note:</strong> as of JDK 6.0 (b51), you can instead use
 * <pre>
 * Set&lt;T&gt; s = Collections.newSetFromMap(new WeakHashMap&lt;T, Boolean&gt;());
 * </pre>
*
* @author Ales Novak
*/
public class WeakSet<E> extends AbstractSet<E> implements Cloneable, Serializable {
    static final long serialVersionUID = 3062376055928236721L;

    /** load factor */
    private float loadFactor;

    /** Number of items. */
    private int size;

    /** Modification count */
    private long modcount;

    /** Reference queue of collected weak refs */
    private transient ReferenceQueue<E> refq;

    /** Count of <tt>null</tt> in this set */
    long nullCount;

    /** An array of Entries */
    private transient Entry<E>[] entries;
    transient Entry<E> iterChain;

    /** Constructs a new set. */
    public WeakSet() {
        this(11, 0.75f);
    }

    /** Constructs a new set containing the elements in the specified collection.
    * @param c a collection to add
    */
    public WeakSet(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /** Constructs a new, empty set;
    * @param initialCapacity initial capacity
    */
    public WeakSet(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    /** Constructs a new, empty set;
    *
    * @param initialCapacity initial capacity
    * @param loadFactor load factor
    */
    public WeakSet(int initialCapacity, float loadFactor) {
        if ((initialCapacity <= 0) || (loadFactor <= 0)) {
            throw new IllegalArgumentException();
        }

        size = 0;
        modcount = 0;
        this.loadFactor = loadFactor;
        nullCount = 0;
        refq = new ReferenceQueue<E>();
        entries = Entry.createArray(initialCapacity);
        iterChain = null;
    }

    /** Adds the specified element to this set if it is not already present.
    *
    * @param o an Object to add
    */
    public boolean add(E o) {
        if (o == null) {
            size++;
            nullCount++;
            modcount++;

            return true;
        }

        Entry e = object2Entry(o);

        if (e != null) {
            return false;
        }

        modcount++;
        size++;

        int hash = hashIt(o);
        Entry<E> next = entries[hash];
        iterChain = entries[hash] = new Entry<E>(this, o, refq, next, iterChain);
        rehash();

        return true;
    }

    /** Removes all of the elements from this set. */
    public void clear() {
        for (int i = 0; i < entries.length; i++) {
            entries[i] = null;
        }

        nullCount = 0;
        modcount++;
        size = 0;
        iterChain = null;
    }

    /** Returns a shallow copy of this WeakSet instance: the elements themselves are not cloned. */
    public Object clone() {
        WeakSet<E> nws = new WeakSet<E>(1, loadFactor);
        nws.size = size;
        nws.nullCount = nullCount;

        Entry<E>[] cloned = Entry.createArray(entries.length);
        nws.entries = cloned;

        for (int i = 0; i < cloned.length; i++) {
            Object ref;

            if ((entries[i] == null) || ((ref = entries[i].get()) == null)) {
                cloned[i] = null;
            } else {
                cloned[i] = ((entries[i] == null) ? null : entries[i].clone(nws.refq));
                ref = null;
            }

            // chains into nws iterator chain
            Entry<E> entry = cloned[i];

            while (entry != null) {
                entry.chainIntoIter(nws.iterChain);
                nws.iterChain = entry;
                entry = entry.next;
            }
        }

        return nws;
    }

    /** Returns true if this set contains the specified element.
    *
    * @param o an Object to examine
    */
    public boolean contains(Object o) {
        if (o == null) {
            return nullCount > 0;
        }

        return object2Entry(o) != null;
    }

    /** Returns true if this set contains no elements.
    */
    public boolean isEmpty() {
        return ((nullCount == 0) && (size() == 0));
    }

    /** Returns an iterator over the elements in this set. */
    public Iterator<E> iterator() {
        return new WeakSetIterator();
    }

    /** Removes the given element from this set if it is present.
    *
    * @param o an Object to remove
    * @return <tt>true</tt> if and only if the Object was successfuly removed.
    */
    public boolean remove(Object o) {
        if (o == null) {
            if (nullCount > 0) {
                nullCount--;
                modcount++;
                size--;
            }

            return true;
        }

        Entry e = object2Entry(o);

        if (e != null) {
            modcount++;
            size--;
            e.remove();
            rehash();

            return true;
        }

        return false;
    }

    /** @return the number of elements in this set (its cardinality). */
    public int size() {
        checkRefQueue();

        return size;
    }

    public <T> T[] toArray(T[] array) {
        ArrayList<E> list = new ArrayList<E>(array.length);
        Iterator<E> it = iterator();

        while (it.hasNext()) {
            list.add(it.next());
        }

        return list.toArray(array);
    }

    public Object[] toArray() {
        ArrayList<E> list = new ArrayList<E>();
        Iterator<E> it = iterator();

        while (it.hasNext()) {
            list.add(it.next());
        }

        return list.toArray();
    }

    // #14772
    public String toString() {
        StringBuffer buf = new StringBuffer();
        Iterator e = iterator();
        buf.append("[");

        while (e.hasNext()) {
            buf.append(String.valueOf(e.next()));

            if (e.hasNext()) {
                buf.append(", ");
            }
        }

        buf.append("]");

        return buf.toString();
    }

    /** Checks if the queue is empty if not pending weak refs are removed. */
    void checkRefQueue() {
        for (;;) {
            Entry entry = Entry.class.cast(refq.poll());

            if (entry == null) {
                break;
            }

            entry.remove();
            size--;
        }
    }

    /** @return modcount */
    long modCount() {
        return modcount;
    }

    /** @return an index to entries array */
    int hashIt(Object o) {
        return (o.hashCode() & 0x7fffffff) % entries.length;
    }

    /** rehashes this Set */
    void rehash() {
        /*
        float currentLF = ((float) size) / ((float) entries.length);
        if (currentLF < loadFactor) {
          return;
        }
        */
    }

    /** @return an Entry with given object */
    private Entry object2Entry(Object o) {
        checkRefQueue(); // clear ref q

        int hash = hashIt(o);
        Entry e = entries[hash];

        if (e == null) {
            return null;
        }

        while ((e != null) && !e.equals(o)) {
            e = e.next;
        }

        return e;
    }

    private void writeObject(ObjectOutputStream obtos)
    throws IOException {
        obtos.defaultWriteObject();
        obtos.writeObject(toArray());
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream obtis) throws IOException, ClassNotFoundException {
        obtis.defaultReadObject();

        Object[] arr = (Object[]) obtis.readObject();
        entries = new Entry[(int) (size * 1.5)];
        refq = new ReferenceQueue<E>();

        for (int i = 0; i < arr.length; i++) {
            add((E)arr[i]);
        }
    }

    class WeakSetIterator implements Iterator<E> {
        Entry<E> current;
        Entry<E> next;
        E currentObj;
        E nextObj;
        final long myModcount;
        long myNullCount;

        WeakSetIterator() {
            myModcount = modCount();
            myNullCount = nullCount;
            current = null;
            next = null;

            Entry<E> ee = iterChain;

            if (ee == null) {
                return;
            }

            E o = ee.get();

            while (ee.isEnqueued()) {
                ee = ee.iterChainNext;

                if (ee == null) {
                    return;
                }

                o = ee.get();
            }

            nextObj = o;
            next = ee;
        }

        public boolean hasNext() {
            checkModcount();

            return ((myNullCount > 0) || (next != null));
        }

        public E next() {
            checkModcount();
            checkRefQueue();

            if (myNullCount > 0) {
                myNullCount--;

                return null;
            } else {
                if (next == null) {
                    throw new java.util.NoSuchElementException();
                }

                current = next;
                currentObj = nextObj;

                // move to next requested
                do {
                    next = next.iterChainNext;

                    if (next == null) {
                        break;
                    }

                    nextObj = next.get();
                } while (next.isEnqueued());

                return currentObj;
            }
        }

        public void remove() {
            checkModcount();

            if (current == null) {
                throw new IllegalStateException();
            }

            current.remove();
            size--;
        }

        void checkModcount() {
            if (myModcount != modCount()) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /** Entries of this set */
    static class Entry<E> extends WeakReference<E> {
        /** reference to outer WeakSet */
        private WeakSet<E> set;

        // double linked list
        Entry<E> prev;
        Entry<E> next;
        private final int hashcode;
        Entry<E> iterChainNext;
        Entry<E> iterChainPrev;

        Entry(WeakSet<E> set, E referenced, ReferenceQueue<E> q, Entry<E> next, Entry<E> nextInIter) {
            super(referenced, q);
            this.set = set;

            this.next = next;
            this.prev = null;

            if (next != null) {
                next.prev = this;
            }

            if (referenced != null) {
                hashcode = set.hashIt(referenced);
            } else {
                hashcode = 0;
            }

            chainIntoIter(nextInIter);
        }

        @SuppressWarnings("unchecked")
        static final <E> Entry<E>[] createArray(int size) {
            return new Entry[size];
        }

        void chainIntoIter(Entry<E> nextInIter) {
            iterChainNext = nextInIter;

            if (nextInIter != null) {
                nextInIter.iterChainPrev = this;

                Object ref = nextInIter.get();

                if (ref == null) {
                    nextInIter.remove();
                }
            }
        }

        /** deques itself */
        void remove() {
            if (prev != null) {
                prev.next = next;
            }

            if (next != null) {
                next.prev = prev;
            }

            if (iterChainNext != null) {
                iterChainNext.iterChainPrev = iterChainPrev;
            }

            if (iterChainPrev != null) {
                iterChainPrev.iterChainNext = iterChainNext;
            } else { // root
                set.iterChain = iterChainNext;
            }

            if (set.entries[hashcode] == this) {
                set.entries[hashcode] = next;
            }

            prev = null;
            next = null;
            iterChainNext = null;
            iterChainPrev = null;
        }

        public int hashCode() {
            return hashcode;
        }

        public boolean equals(Object o) {
            Object oo = get();

            if (oo == null) {
                return false;
            } else {
                return oo.equals(o);
            }
        }

        public Entry<E> clone(ReferenceQueue<E> q) {
            return new Entry<E>(set, get(), q, next != null ? next.clone(q) : null, null);
        }
    }
}
