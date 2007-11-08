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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.EventListenerList;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/** Implementation of lookup that can delegate to others.
 *
 * @author  Jaroslav Tulach
 * @since 1.9
 */
public class ProxyLookup extends Lookup {
    /** empty array of lookups for potential use */
    private static final Lookup[] EMPTY_ARR = new Lookup[0];

    /** lookups to delegate to (either Lookup or array of Lookups) */
    private Object lookups;

    /** map of templates to currently active results */
    private HashMap<Template<?>,Reference<R>> results;

    /** Create a proxy to some other lookups.
     * @param lookups the initial delegates
     */
    public ProxyLookup(Lookup... lookups) {
        this.setLookupsNoFire(lookups);
    }

    /**
     * Create a lookup initially proxying to no others.
     * Permits serializable subclasses.
     * @since 3.27
     */
    protected ProxyLookup() {
        this(EMPTY_ARR);
    }

    public String toString() {
        return "ProxyLookup(class=" + getClass() + ")->" + Arrays.asList(getLookups(false)); // NOI18N
    }

    /** Getter for the delegates.
    * @return the array of lookups we delegate to
    * @since 1.19
    */
    protected final Lookup[] getLookups() {
        return getLookups(true);
    }

    /** getter for the delegates, that can but need not do a clone.
     * @param clone true if clone of internal array is requested
     */
    private final Lookup[] getLookups(boolean clone) {
        Object l = this.lookups;
        if (l instanceof Lookup) {
            return new Lookup[] { (Lookup)l };
        } else {
            Lookup[] arr = (Lookup[])l;
            if (clone) {
                arr = arr.clone();
            }
            return arr;
        }
    }
    
    /** Called from setLookups and constructor. 
     * @param lookups the lookups to setup
     */
    private void setLookupsNoFire(Lookup[] lookups) {
        if (lookups.length == 1) {
            this.lookups = lookups[0];
            assert this.lookups != null : "Cannot assign null delegate";
        } else {
            if (lookups.length == 0) {
                this.lookups = EMPTY_ARR;
            } else {
                this.lookups = lookups.clone();
            }
        }
    }

    /**
     * Changes the delegates.
     *
     * @param lookups the new lookups to delegate to
     * @since 1.19 protected
     */
    protected final void setLookups(Lookup... lookups) {
        Collection<Reference<R>> arr;
        HashSet<Lookup> newL;
        HashSet<Lookup> current;
        Lookup[] old;

        synchronized (this) {
            old = getLookups(false);
            current = new HashSet<Lookup>(Arrays.asList(old));
            newL = new HashSet<Lookup>(Arrays.asList(lookups));

            setLookupsNoFire(lookups);
            
            if ((results == null) || results.isEmpty()) {
                // no affected results => exit
                return;
            }

            arr = new ArrayList<Reference<R>>(results.values());

            HashSet<Lookup> removed = new HashSet<Lookup>(current);
            removed.removeAll(newL); // current contains just those lookups that have disappeared
            newL.removeAll(current); // really new lookups

            if (removed.isEmpty() && newL.isEmpty()) {
                // no need to notify changes
                return;
            }

            for (Reference<R> ref : arr) {
                R r = ref.get();
                if (r != null) {
                    r.lookupChange(newL, removed, old, lookups);
                }
            }
        }

        // this cannot be done from the synchronized block
        ArrayList<Object> evAndListeners = new ArrayList<Object>();
        for (Reference<R> ref : arr) {
            R<?> r = ref.get();
            if (r != null) {
                r.collectFires(evAndListeners);
            }
        }
        
        {
            Iterator it = evAndListeners.iterator();
            while (it.hasNext()) {
                LookupEvent ev = (LookupEvent)it.next();
                LookupListener l = (LookupListener)it.next();
                l.resultChanged(ev);
            }
        }
    }

    /** Notifies subclasses that a query is about to be processed.
     * Subclasses can update its state before the actual processing
     * begins. It is allowed to call <code>setLookups</code> method
     * to change/update the set of objects the proxy delegates to.
     *
     * @param template the template of the query
     * @since 1.31
     */
    protected void beforeLookup(Template<?> template) {
    }

    public final <T> T lookup(Class<T> clazz) {
        beforeLookup(new Template<T>(clazz));

        Lookup[] lookups = this.getLookups(false);

        for (int i = 0; i < lookups.length; i++) {
            T o = lookups[i].lookup(clazz);

            if (o != null) {
                return o;
            }
        }

        return null;
    }

    public final <T> Item<T> lookupItem(Template<T> template) {
        beforeLookup(template);

        Lookup[] lookups = this.getLookups(false);

        for (int i = 0; i < lookups.length; i++) {
            Item<T> o = lookups[i].lookupItem(template);

            if (o != null) {
                return o;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> R<T> convertResult(R r) {
        return (R<T>)r;
    }

    public final synchronized <T> Result<T> lookup(Lookup.Template<T> template) {
        if (results != null) {
            Reference<R> ref = results.get(template);
            R r = (ref == null) ? null : ref.get();

            if (r != null) {
                return convertResult(r);
            }
        } else {
            results = new HashMap<Template<?>,Reference<R>>();
        }

        R<T> newR = new R<T>(template);
        results.put(template, new java.lang.ref.SoftReference<R>(newR));

        return newR;
    }

    /** Unregisters a template from the has map.
     */
    private final synchronized void unregisterTemplate(Template<?> template) {
        if (results == null) {
            return;
        }

        Reference<R> ref = results.remove(template);

        if ((ref != null) && (ref.get() != null)) {
            // seems like there is a reference to a result for this template
            // thta is still alive
            results.put(template, ref);
        }
    }

    /** Result of a lookup request. Allows access to single object
     * that was found (not too useful) and also to all objects found
     * (more useful).
     */
    private final class R<T> extends WaitableResult<T> {
        /** list of listeners added */
        private javax.swing.event.EventListenerList listeners;

        /** template for this result */
        private Lookup.Template<T> template;

        /** collection of Objects */
        private Collection[] cache;

        /** weak listener & result */
        private WeakResult<T> weakL;

        /** Constructor.
         */
        public R(Lookup.Template<T> t) {
            template = t;
            weakL = new WeakResult<T>(this);
        }

        /** When garbage collected, remove the template from the has map.
         */
        protected void finalize() {
            unregisterTemplate(template);
        }

        @SuppressWarnings("unchecked")
        private Result<T>[] newResults(int len) {
            return new Result[len];
        }

        /** initializes the results
         */
        private Result<T>[] initResults() {
            synchronized (this) {
                if (weakL.results != null) {
                    return weakL.results;
                }
            }

            Lookup[] myLkps = getLookups(false);
            Result<T>[] arr = newResults(myLkps.length);

            for (int i = 0; i < arr.length; i++) {
                arr[i] = myLkps[i].lookup(template);
            }

            synchronized (this) {
                // some other thread might compute the result mean while. 
                // if not finish the computation yourself
                if (weakL.results != null) {
                    return weakL.results;
                }

                for (int i = 0; i < arr.length; i++) {
                    arr[i].addLookupListener(weakL);
                }

                weakL.results = arr;

                return arr;
            }
        }

        /** Called when there is a change in the list of proxied lookups.
         * @param added set of added lookups
         * @param remove set of removed lookups
         * @param current array of current lookups
         */
        protected void lookupChange(Set added, Set removed, Lookup[] old, Lookup[] current) {
            synchronized (this) {
                if (weakL.results == null) {
                    // not computed yet, do not need to do anything
                    return;
                }

                // map (Lookup, Lookup.Result)
                HashMap<Lookup,Result<T>> map = new HashMap<Lookup,Result<T>>(old.length * 2);

                for (int i = 0; i < old.length; i++) {
                    if (removed.contains(old[i])) {
                        // removed lookup
                        weakL.results[i].removeLookupListener(weakL);
                    } else {
                        // remember the association
                        map.put(old[i], weakL.results[i]);
                    }
                }

                Lookup.Result<T>[] arr = newResults(current.length);

                for (int i = 0; i < current.length; i++) {
                    if (added.contains(current[i])) {
                        // new lookup
                        arr[i] = current[i].lookup(template);
                        arr[i].addLookupListener(weakL);
                    } else {
                        // old lookup
                        arr[i] = map.get(current[i]);

                        if (arr[i] == null) {
                            // assert
                            throw new IllegalStateException();
                        }
                    }
                }

                // remember the new results
                weakL.results = arr;
            }
        }

        /** Just delegates.
         */
        public void addLookupListener(LookupListener l) {
            if (listeners == null) {
                synchronized (this) {
                    if (listeners == null) {
                        listeners = new EventListenerList();
                    }
                }
            }

            listeners.add(LookupListener.class, l);
        }

        /** Just delegates.
         */
        public void removeLookupListener(LookupListener l) {
            if (listeners != null) {
                listeners.remove(LookupListener.class, l);
            }
        }

        /** Access to all instances in the result.
         * @return collection of all instances
         */
        @SuppressWarnings("unchecked")
        public java.util.Collection<T> allInstances() {
            return computeResult(0);
        }

        /** Classes of all results. Set of the most concreate classes
         * that are registered in the system.
         * @return set of Class objects
         */
        @SuppressWarnings("unchecked")
        public java.util.Set<Class<? extends T>> allClasses() {
            return (java.util.Set<Class<? extends T>>) computeResult(1);
        }

        /** All registered items. The collection of all pairs of
         * ii and their classes.
         * @return collection of Lookup.Item
         */
        @SuppressWarnings("unchecked")
        public java.util.Collection<? extends Item<T>> allItems() {
            return computeResult(2);
        }

        /** Computes results from proxied lookups.
         * @param indexToCache 0 = allInstances, 1 = allClasses, 2 = allItems
         * @return the collection or set of the objects
         */
        private java.util.Collection computeResult(int indexToCache) {
            // results to use
            Lookup.Result<T>[] arr = myBeforeLookup();

            // if the call to beforeLookup resulted in deletion of caches
            synchronized (this) {
                if (cache != null) {
                    Collection result = cache[indexToCache];
                    if (result != null) {
                        return result;
                    }
                }
            }

            // initialize the collection to hold result
            Collection<Object> compute;
            Collection<Object> ret;

            if (indexToCache == 1) {
                HashSet<Object> s = new HashSet<Object>();
                compute = s;
                ret = Collections.unmodifiableSet(s);
            } else {
                List<Object> l = new ArrayList<Object>(arr.length * 2);
                compute = l;
                ret = Collections.unmodifiableList(l);
            }

            // fill the collection
            for (int i = 0; i < arr.length; i++) {
                switch (indexToCache) {
                case 0:
                    compute.addAll(arr[i].allInstances());
                    break;
                case 1:
                    compute.addAll(arr[i].allClasses());
                    break;
                case 2:
                    compute.addAll(arr[i].allItems());
                    break;
                default:
                    assert false : "Wrong index: " + indexToCache;
                }
            }
            
            

            synchronized (this) {
                if (cache == null) {
                    // initialize the cache to indicate this result is in use
                    cache = new Collection[3];
                }
                
                if (arr == weakL.results) {
                    // updates the results, if the results have not been
                    // changed during the computation of allInstances
                    cache[indexToCache] = ret;
                }
            }

            return ret;
        }

        /** When the result changes, fire the event.
         */
        public void resultChanged(LookupEvent ev) {
            collectFires(null);
        }
        
        protected void collectFires(Collection<Object> evAndListeners) {
            // clear cached instances
            Collection oldItems;
            Collection oldInstances;
            synchronized (this) {
                if (cache == null) {
                    // nobody queried the result yet
                    return;
                }
                oldInstances = cache[0];
                oldItems = cache[2];
                

                if (listeners == null || listeners.getListenerCount() == 0) {
                    // clear the cache
                    cache = new Collection[3];
                    return;
                }
                
                // ignore events if they arrive as a result of call to allItems
                // or allInstances, bellow...
                cache = null;
            }

            boolean modified = true;

            if (oldItems != null) {
                Collection newItems = allItems();
                if (oldItems.equals(newItems)) {
                    modified = false;
                }
            } else {
                if (oldInstances != null) {
                    Collection newInstances = allInstances();
                    if (oldInstances.equals(newInstances)) {
                        modified = false;
                    }
                } else {
                    synchronized (this) {
                        if (cache == null) {
                            // we have to initialize the cache
                            // to show that the result has been initialized
                            cache = new Collection[3];
                        }
                    }
                }
            }
            
            assert cache != null;

            if (modified) {
                LookupEvent ev = new LookupEvent(this);
                AbstractLookup.notifyListeners(listeners.getListenerList(), ev, evAndListeners);
            }
        }

        /** Implementation of my before lookup.
         * @return results to work on.
         */
        private Lookup.Result<T>[] myBeforeLookup() {
            ProxyLookup.this.beforeLookup(template);

            Lookup.Result<T>[] arr = initResults();

            // invoke update on the results
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] instanceof WaitableResult) {
                    WaitableResult w = (WaitableResult) arr[i];
                    w.beforeLookup(template);
                }
            }

            return arr;
        }

        /** Used by proxy results to synchronize before lookup.
         */
        protected void beforeLookup(Lookup.Template t) {
            if (t.getType() == template.getType()) {
                myBeforeLookup();
            }
        }
    }
    private static final class WeakResult<T> extends WaitableResult<T> implements LookupListener {
        /** all results */
        private Lookup.Result<T>[] results;

        private Reference<R> result;
        
        public WeakResult(R r) {
            this.result = new WeakReference<R>(r);
        }
        
        protected void beforeLookup(Lookup.Template t) {
            R r = result.get();
            if (r != null) {
                r.beforeLookup(t);
            } else {
                removeListeners();
            }
        }

        private void removeListeners() {
            Lookup.Result<T>[] arr = this.results;
            if (arr == null) {
                return;
            }

            for(int i = 0; i < arr.length; i++) {
                arr[i].removeLookupListener(this);
            }
        }

        protected void collectFires(Collection<Object> evAndListeners) {
            R<?> r = result.get();
            if (r != null) {
                r.collectFires(evAndListeners);
            } else {
                removeListeners();
            }
        }

        public void addLookupListener(LookupListener l) {
            assert false;
        }

        public void removeLookupListener(LookupListener l) {
            assert false;
        }

        public Collection<T> allInstances() {
            assert false;
            return null;
        }

        public void resultChanged(LookupEvent ev) {
            R r = result.get();
            if (r != null) {
                r.resultChanged(ev);
            } else {
                removeListeners();
            }
        }

        public Collection<? extends Item<T>> allItems() {
            assert false;
            return null;
        }

        public Set<Class<? extends T>> allClasses() {
            assert false;
            return null;
        }
    } // end of WeakResult
}
