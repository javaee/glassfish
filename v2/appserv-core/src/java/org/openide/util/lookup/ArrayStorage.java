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
package org.openide.util.lookup;

import org.openide.util.Lookup;

import java.io.*;

import java.lang.ref.WeakReference;

import java.util.*;
import org.openide.util.lookup.AbstractLookup.Pair;


/** ArrayStorage of Pairs from AbstractLookup.
 * @author  Jaroslav Tulach
 */
final class ArrayStorage extends Object
implements AbstractLookup.Storage<ArrayStorage.Transaction> {
    /** default trashold */
    static final Integer DEFAULT_TRASH = new Integer(11);

    /** list of items */
    private Object content;

    /** linked list of refernces to results */
    private transient AbstractLookup.ReferenceToResult<?> results;

    /** Constructor
     */
    public ArrayStorage() {
        this(DEFAULT_TRASH);
    }

    /** Constructs new ArrayStorage */
    public ArrayStorage(Integer treshhold) {
        this.content = treshhold;
    }

    /** Adds an item into the tree.
    * @param item to add
    * @return true if the Item has been added for the first time or false if some other
    *    item equal to this one already existed in the lookup
    */
    public boolean add(AbstractLookup.Pair<?> item, Transaction changed) {
        Object[] arr = changed.current;

        if (changed.arr == null) {
            // just simple add of one item
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    arr[i] = item;
                    changed.add(item);

                    return true;
                }

                if (arr[i].equals(item)) {
                    // reassign the item number
                    item.setIndex(null, ((AbstractLookup.Pair) arr[i]).getIndex());

                    // already there, but update it
                    arr[i] = item;

                    return false;
                }
            }

            // cannot happen as the beginTransaction ensured we can finish 
            // correctly
            throw new IllegalStateException();
        } else {
            // doing remainAll after that, let Transaction hold the new array
            int newIndex = changed.addPair(item);

            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    changed.add(item);

                    return true;
                }

                if (arr[i].equals(item)) {
                    // already there
                    if (i != newIndex) {
                        // change in index
                        changed.add(item);

                        return false;
                    } else {
                        // no change
                        return false;
                    }
                }
            }

            // if not found in the original array
            changed.add(item);

            return true;
        }
    }

    /** Removes an item.
    */
    public void remove(AbstractLookup.Pair item, Transaction changed) {
        Object[] arr = changed.current;

        int found = -1;

        for (int i = 0; i < arr.length;) {
            if (arr[i] == null) {
                // end of task
                return;
            }

            if ((found == -1) && arr[i].equals(item)) {
                // already there
                Pair<?> p = (Pair<?>)arr[i];
                p.setIndex(null, -1);
                changed.add(p);
                found = i;
            }

            i++;

            if (found != -1) {
                if (i < arr.length) {
                    // moving the array
                    arr[i - 1] = arr[i];
                } else {
                    arr[i - 1] = null;
                }
            }
        }
    }

    /** Removes all items that are not present in the provided collection.
    * @param retain Pair -> AbstractLookup.Info map
    * @param notify set of Classes that has possibly changed
    */
    public void retainAll(Map retain, Transaction changed) {
        Object[] arr = changed.current;

        for (int from = 0; from < arr.length; from++) {
            if (!(arr[from] instanceof AbstractLookup.Pair)) {
                // end of content
                break;
            }

            AbstractLookup.Pair p = (AbstractLookup.Pair) arr[from];

            AbstractLookup.Info info = (AbstractLookup.Info) retain.get(p);

            if (info == null) {
                // was removed

                /*
                if (info != null) {
                if (info.index < arr.length) {
                    newArr[info.index] = p;
                }

                if (p.getIndex() != info.index) {
                    p.setIndex (null, info.index);
                    changed.add (p);
                }
                } else {
                // removed
                 */
                changed.add(p);
            }
        }
    }

    /** Queries for instances of given class.
    * @param clazz the class to check
    * @return enumeration of Item
    * @see #unsorted
    */
    public <T> Enumeration<Pair<T>> lookup(final Class<T> clazz) {
        class CheckEn implements org.openide.util.Enumerations.Processor<Object,Pair<T>> {
            @SuppressWarnings("unchecked")
            public Pair<T> process(Object o, Collection ignore) {
                boolean ok;

                if (o instanceof AbstractLookup.Pair) {
                    ok = (clazz == null) || ((AbstractLookup.Pair) o).instanceOf(clazz);
                } else {
                    ok = false;
                }

                return ok ? (Pair<T>)o : null;
            }
        }

        if (content instanceof Object[]) {
            Enumeration<Object> all = org.openide.util.Enumerations.array((Object[]) content);
            return org.openide.util.Enumerations.filter(all, new CheckEn());
        } else {
            return org.openide.util.Enumerations.empty();
        }
    }

    /** Associates another result with this storage.
     */
    public AbstractLookup.ReferenceToResult registerReferenceToResult(AbstractLookup.ReferenceToResult<?> newRef) {
        AbstractLookup.ReferenceToResult prev = this.results;
        this.results = newRef;

        return prev;
    }

    /** Cleanup the references
     */
    public AbstractLookup.ReferenceToResult cleanUpResult(Lookup.Template<?> templ) {
        AbstractLookup.ReferenceIterator it = new AbstractLookup.ReferenceIterator(this.results);

        while (it.next())
            ;

        return this.results = it.first();
    }

    /** We use a hash set of all modified Pair to handle the transaction */
    public Transaction beginTransaction(int ensure) {
        return new Transaction(ensure, content);
    }

    /** Extract all results.
     */
    public void endTransaction(Transaction changed, Set<AbstractLookup.R> modified) {
        AbstractLookup.ReferenceIterator it = new AbstractLookup.ReferenceIterator(this.results);

        if (changed.arr == null) {
            // either add or remove, only check the content of check HashSet
            while (it.next()) {
                AbstractLookup.ReferenceToResult ref = it.current();
                Iterator<Pair<?>> pairs = changed.iterator();

                while (pairs.hasNext()) {
                    AbstractLookup.Pair p = (AbstractLookup.Pair) pairs.next();

                    if (AbstractLookup.matches(ref.template, p, true)) {
                        modified.add(ref.getResult());
                    }
                }
            }
        } else {
            // do full check of changes
            while (it.next()) {
                AbstractLookup.ReferenceToResult ref = it.current();

                int oldIndex = -1;
                int newIndex = -1;

                for (;;) {
                    oldIndex = findMatching(ref.template, changed.current, oldIndex);
                    newIndex = findMatching(ref.template, changed.arr, newIndex);

                    if ((oldIndex == -1) && (newIndex == -1)) {
                        break;
                    }

                    if (
                        (oldIndex == -1) || (newIndex == -1) ||
                            !changed.current[oldIndex].equals(changed.arr[newIndex])
                    ) {
                        modified.add(ref.getResult());

                        break;
                    }
                }
            }
        }

        this.results = it.first();
        this.content = changed.newContent();
    }

    private static int findMatching(Lookup.Template t, Object[] arr, int from) {
        while (++from < arr.length) {
            if (arr[from] instanceof AbstractLookup.Pair) {
                if (AbstractLookup.matches(t, (AbstractLookup.Pair) arr[from], true)) {
                    return from;
                }
            }
        }

        return -1;
    }

    /** HashSet with additional field for new array which is callocated
     * in case we are doing replace to hold all new items.
     */
    static final class Transaction extends HashSet<Pair<?>> {
        /** array with current objects */
        public final Object[] current;

        /** array with new objects */
        public final Object[] arr;

        /** number of objects in the array */
        private int cnt;

        public Transaction(int ensure, Object currentContent) {
            Integer trashold;
            Object[] arr;

            if (currentContent instanceof Integer) {
                trashold = (Integer) currentContent;
                arr = null;
            } else {
                arr = (Object[]) currentContent;

                if (arr[arr.length - 1] instanceof Integer) {
                    trashold = (Integer) arr[arr.length - 1];
                } else {
                    // nowhere to grow we have reached the limit
                    trashold = null;
                }
            }

            int maxSize = (trashold == null) ? arr.length : trashold.intValue();

            if (ensure > maxSize) {
                throw new UnsupportedOperationException();
            }

            if (ensure == -1) {
                // remove => it is ok
                this.current = (Object[]) currentContent;
                this.arr = null;

                return;
            }

            if (ensure == -2) {
                // adding one
                if (arr == null) {
                    // first time add, let's allocate the array
                    arr = new Object[2];
                    arr[1] = trashold;
                } else {
                    if (arr[arr.length - 1] instanceof AbstractLookup.Pair) {
                        // we are full
                        throw new UnsupportedOperationException();
                    } else {
                        // ensure we have allocated enough space
                        if (arr.length < 2 || arr[arr.length - 2] != null) {
                            // double the array
                            int newSize = (arr.length - 1) * 2;
                            
                            if (newSize <= 1) {
                                newSize = 2;
                            }

                            if (newSize > maxSize) {
                                newSize = maxSize;

                                if (newSize <= arr.length) {
                                    // no space to get in
                                    throw new UnsupportedOperationException();
                                }

                                arr = new Object[newSize];
                            } else {
                                // still a lot of space
                                arr = new Object[newSize + 1];
                                arr[newSize] = trashold;
                            }

                            // copy content of original array without the last Integer into 
                            // the new one
                            System.arraycopy(currentContent, 0, arr, 0, ((Object[]) currentContent).length - 1);
                        }
                    }
                }

                this.current = arr;
                this.arr = null;
            } else {
                // allocate array for complete replacement
                if (ensure == maxSize) {
                    this.arr = new Object[ensure];
                } else {
                    this.arr = new Object[ensure + 1];
                    this.arr[ensure] = trashold;
                }

                this.current = (currentContent instanceof Object[]) ? (Object[]) currentContent : new Object[0];
            }
        }

        public int addPair(AbstractLookup.Pair<?> p) {
            p.setIndex(null, cnt);
            arr[cnt++] = p;

            return p.getIndex();
        }

        public Object newContent() {
            return (arr == null) ? current : arr;
        }
    }
     // end of Transaction
}
