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


/** Storages that can switch between another storages.
 * @author  Jaroslav Tulach
 */
final class DelegatingStorage<Transaction> extends Object
implements Serializable, AbstractLookup.Storage<Transaction> {
    /** object to delegate to */
    private AbstractLookup.Storage<Transaction> delegate;

    /** thread just accessing the storage */
    private Thread owner;

    public DelegatingStorage(AbstractLookup.Storage<Transaction> d) {
        this.delegate = d;
        this.owner = Thread.currentThread();
    }

    /** Never serialize yourself, always put there the delegate */
    public Object writeReplace() {
        return this.delegate;
    }

    /** Method to check whether there is not multiple access from the same thread.
     */
    public void checkForTreeModification() {
        if (Thread.currentThread() == owner) {
            throw new AbstractLookup.ISE("You are trying to modify lookup from lookup query!"); // NOI18N
        }
    }

    /** Checks whether we have simple behaviour or complex.
     */
    public static boolean isSimple(AbstractLookup.Storage s) {
        if (s instanceof DelegatingStorage) {
            return ((DelegatingStorage) s).delegate instanceof ArrayStorage;
        } else {
            return s instanceof ArrayStorage;
        }
    }

    /** Exits from the owners ship of the storage.
     */
    public AbstractLookup.Storage<Transaction> exitDelegate() {
        if (Thread.currentThread() != owner) {
            throw new IllegalStateException("Onwer: " + owner + " caller: " + Thread.currentThread()); // NOI18N
        }

        AbstractLookup.Storage<Transaction> d = delegate;
        delegate = null;

        return d;
    }

    public boolean add(AbstractLookup.Pair<?> item, Transaction transaction) {
        return delegate.add(item, transaction);
    }

    public void remove(org.openide.util.lookup.AbstractLookup.Pair item, Transaction transaction) {
        delegate.remove(item, transaction);
    }

    public void retainAll(Map retain, Transaction transaction) {
        delegate.retainAll(retain, transaction);
    }

    /** A special method to change the backing storage.
     * In fact it is not much typesafe as it changes the
     * type of Transaction but we know that nobody is currently
     * holding a transaction object, so there cannot be inconsitencies.
     */
    @SuppressWarnings("unchecked")
    private void changeDelegate(InheritanceTree st) {
        delegate = (AbstractLookup.Storage<Transaction>)st;
    }

    public Transaction beginTransaction(int ensure) {
        try {
            return delegate.beginTransaction(ensure);
        } catch (UnsupportedOperationException ex) {
            // let's convert to InheritanceTree
            ArrayStorage arr = (ArrayStorage) delegate;
            InheritanceTree inh = new InheritanceTree();
            changeDelegate(inh);

            //
            // Copy content
            //
            Enumeration<Pair<Object>> en = arr.lookup(Object.class);

            while (en.hasMoreElements()) {
                if (!inh.add(en.nextElement(), new ArrayList<Class>())) {
                    throw new IllegalStateException("All objects have to be accepted"); // NOI18N
                }
            }

            //
            // Copy listeners
            //
            AbstractLookup.ReferenceToResult<?> ref = arr.cleanUpResult(null);

            if (ref != null) {
                ref.cloneList(inh);
            }

            // we have added the current content and now we can start transaction
            return delegate.beginTransaction(ensure);
        }
    }

    public org.openide.util.lookup.AbstractLookup.ReferenceToResult cleanUpResult(
        org.openide.util.Lookup.Template templ
    ) {
        return delegate.cleanUpResult(templ);
    }

    public void endTransaction(Transaction transaction, Set<AbstractLookup.R> modified) {
        delegate.endTransaction(transaction, modified);
    }

    public <T> Enumeration<Pair<T>> lookup(Class<T> clazz) {
        return delegate.lookup(clazz);
    }

    public org.openide.util.lookup.AbstractLookup.ReferenceToResult registerReferenceToResult(
        org.openide.util.lookup.AbstractLookup.ReferenceToResult newRef
    ) {
        return delegate.registerReferenceToResult(newRef);
    }
}
