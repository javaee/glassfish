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
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

import java.util.*;


/**
 * Simple proxy lookup. Keeps reference to a lookup it delegates to and
 * forwards all requests.
 *
 * @author Jaroslav Tulach
 */
final class SimpleProxyLookup extends org.openide.util.Lookup {
    /** the provider to check for the status */
    private Provider provider;

    /** the lookup we currently delegate to */
    private Lookup delegate;

    /** map of all templates to Reference (results) associated to this lookup */
    private WeakHashMap<Template<?>,Reference<ProxyResult<?>>> results;

    /**
     * @param provider provider to delegate to
     */
    SimpleProxyLookup(Provider provider) {
        this.provider = provider;
    }

    /** Checks whether we still delegate to the same lookup */
    private Lookup checkLookup() {
        Lookup l = provider.getLookup();

        // iterator over Reference (ProxyResult)
        Iterator<Reference<ProxyResult<?>>> toCheck = null;

        synchronized (this) {
            if (l != delegate) {
                this.delegate = l;

                if (results != null) {
                    toCheck = new ArrayList<Reference<ProxyResult<?>>>(results.values()).iterator();
                }
            }
        }

        if (toCheck != null) {
            // update
            ArrayList<Object> evAndListeners = new ArrayList<Object>();
            for (Iterator<Reference<ProxyResult<?>>> it = toCheck; it.hasNext(); ) {
                java.lang.ref.Reference<ProxyResult<?>> ref = it.next();
                if (ref == null) {
                    continue;
                }

                ProxyResult<?> p = ref.get();

                if (p != null && p.updateLookup(l)) {
                    p.collectFires(evAndListeners);
                }
            }
            
            for (Iterator it = evAndListeners.iterator(); it.hasNext(); ) {
                LookupEvent ev = (LookupEvent)it.next();
                LookupListener ll = (LookupListener)it.next();
                ll.resultChanged(ev);
            }
        }

        return delegate;
    }

    @SuppressWarnings("unchecked")
    private static <T> ProxyResult<T> cast(ProxyResult<?> p) {
        return (ProxyResult<T>)p;
    }

    public <T> Result<T> lookup(Template<T> template) {
        synchronized (this) {
            if (results == null) {
                results = new WeakHashMap<Template<?>,Reference<ProxyResult<?>>>();
            } else {
                Reference<ProxyResult<?>> ref = results.get(template);

                if (ref != null) {
                    ProxyResult<?> p = ref.get();

                    if (p != null) {
                        return cast(p);
                    }
                }
            }

            ProxyResult<T> p = new ProxyResult<T>(template);
            Reference<ProxyResult<?>> ref = new WeakReference<ProxyResult<?>>(p);
            results.put(template, ref);

            return p;
        }
    }

    public <T> T lookup(Class<T> clazz) {
        if (clazz == null) {
            checkLookup();
            return null;
        }
        return checkLookup().lookup(clazz);
    }

    public <T> Item<T> lookupItem(Template<T> template) {
        return checkLookup().lookupItem(template);
    }

    /**
     * Result used in SimpleLookup. It holds a reference to the collection
     * passed in constructor. As the contents of this lookup result never
     * changes the addLookupListener and removeLookupListener are empty.
     */
    private final class ProxyResult<T> extends WaitableResult<T> implements LookupListener {
        /** Template used for this result. It is never null.*/
        private Template<T> template;

        /** result to delegate to */
        private Lookup.Result<T> delegate;

        /** listeners set */
        private javax.swing.event.EventListenerList listeners;
        private LookupListener lastListener;

        /** Just remembers the supplied argument in variable template.*/
        ProxyResult(Template<T> template) {
            this.template = template;
        }

        /** Checks state of the result
         */
        private Result<T> checkResult() {
            updateLookup(checkLookup());

            return this.delegate;
        }

        /** Updates the state of the lookup.
         * @return true if the lookup really changed
         */
        public boolean updateLookup(Lookup l) {
            Collection<? extends Item<T>> oldPairs = (delegate != null) ? delegate.allItems() : null;

            LookupListener removedListener;

            synchronized (this) {
                if ((delegate != null) && (lastListener != null)) {
                    removedListener = lastListener;
                    delegate.removeLookupListener(lastListener);
                } else {
                    removedListener = null;
                }
            }

            // cannot call to foreign code 
            Lookup.Result<T> res = l.lookup(template);

            synchronized (this) {
                if (removedListener == lastListener) {
                    delegate = res;
                    lastListener = new WeakResult<T>(this, delegate);
                    delegate.addLookupListener(lastListener);
                }
            }

            if (oldPairs == null) {
                // nobody knows about a change
                return false;
            }

            Collection<? extends Item<T>> newPairs = delegate.allItems();

            // See #34961 for explanation.
            if (!(oldPairs instanceof List)) {
                if (oldPairs == Collections.EMPTY_SET) {
                    // avoid allocation
                    oldPairs = Collections.emptyList();
                } else {
                    oldPairs = new ArrayList<Item<T>>(oldPairs);
                }
            }

            if (!(newPairs instanceof List)) {
                newPairs = new ArrayList<Item<T>>(newPairs);
            }

            return !oldPairs.equals(newPairs);
        }

        public synchronized void addLookupListener(LookupListener l) {
            if (listeners == null) {
                listeners = new javax.swing.event.EventListenerList();
            }

            listeners.add(LookupListener.class, l);
        }

        public synchronized void removeLookupListener(LookupListener l) {
            if (listeners != null) {
                listeners.remove(LookupListener.class, l);
            }
        }

        public java.util.Collection<? extends T> allInstances() {
            return checkResult().allInstances();
        }

        public Set<Class<? extends T>> allClasses() {
            return checkResult().allClasses();
        }

        public Collection<? extends Item<T>> allItems() {
            return checkResult().allItems();
        }

        protected void beforeLookup(Lookup.Template t) {
            Lookup.Result r = checkResult();

            if (r instanceof WaitableResult) {
                ((WaitableResult) r).beforeLookup(t);
            }
        }

        /** A change in lookup occured.
         * @param ev event describing the change
         *
         */
        public void resultChanged(LookupEvent anEvent) {
            collectFires(null);
        } 
        
        protected void collectFires(Collection<Object> evAndListeners) {
            javax.swing.event.EventListenerList l = this.listeners;

            if (l == null) {
                return;
            }

            Object[] listeners = l.getListenerList();

            if (listeners.length == 0) {
                return;
            }

            LookupEvent ev = new LookupEvent(this);
            AbstractLookup.notifyListeners(listeners, ev, evAndListeners);
        }
    }
     // end of ProxyResult
    private final class WeakResult<T> extends WaitableResult<T> implements LookupListener {
        private Lookup.Result source;
        private Reference<ProxyResult<T>> result;
        
        public WeakResult(ProxyResult<T> r, Lookup.Result<T> s) {
            this.result = new WeakReference<ProxyResult<T>>(r);
            this.source = s;
        }
        
        protected void beforeLookup(Lookup.Template t) {
            ProxyResult r = (ProxyResult)result.get();
            if (r != null) {
                r.beforeLookup(t);
            } else {
                source.removeLookupListener(this);
            }
        }

        protected void collectFires(Collection<Object> evAndListeners) {
            ProxyResult<T> r = result.get();
            if (r != null) {
                r.collectFires(evAndListeners);
            } else {
                source.removeLookupListener(this);
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
            ProxyResult r = (ProxyResult)result.get();
            if (r != null) {
                r.resultChanged(ev);
            } else {
                source.removeLookupListener(this);
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
