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
import org.openide.util.LookupListener;

import java.util.*;


/** Allows exclusion of certain instances from lookup.
 *
 * @author Jaroslav Tulach
 */
final class ExcludingLookup extends org.openide.util.Lookup {
    /** the other lookup that we delegate to */
    private Lookup delegate;

    /** classes to exclude (Class[]) or just one class (Class) */
    private Object classes;

    /**
     * Creates new Result object with supplied instances parameter.
     * @param instances to be used to return from the lookup
     */
    ExcludingLookup(Lookup delegate, Class[] classes) {
        this.delegate = delegate;

        if (classes.length == 1) {
            this.classes = classes[0];
        } else {
            this.classes = classes;
        }
    }

    public String toString() {
        return "ExcludingLookup: " + delegate + " excludes: " + Arrays.asList(classes()); // NOI18N
    }

    public <T> Result<T> lookup(Template<T> template) {
        if (template == null) {
            throw new NullPointerException();
        }

        if (areSubclassesOfThisClassAlwaysExcluded(template.getType())) {
            // empty result
            return Lookup.EMPTY.lookup(template);
        }

        return new R<T>(template.getType(), delegate.lookup(template));
    }

    public <T> T lookup(Class<T> clazz) {
        if (areSubclassesOfThisClassAlwaysExcluded(clazz)) {
            return null;
        }

        T res = delegate.lookup(clazz);

        if (isObjectAccessible(clazz, res, 0)) {
            return res;
        } else {
            return null;
        }
    }

    public <T> Lookup.Item<T> lookupItem(Lookup.Template<T> template) {
        if (areSubclassesOfThisClassAlwaysExcluded(template.getType())) {
            return null;
        }

        Lookup.Item<T> retValue = delegate.lookupItem(template);

        if (isObjectAccessible(template.getType(), retValue, 2)) {
            return retValue;
        } else {
            return null;
        }
    }

    /** @return true if the instance of class c shall never be returned from this lookup
     */
    private boolean areSubclassesOfThisClassAlwaysExcluded(Class<?> c) {
        Class<?>[] arr = classes();

        for (int i = 0; i < arr.length; i++) {
            if (arr[i].isAssignableFrom(c)) {
                return true;
            }
        }

        return false;
    }

    /** Returns the array of classes this lookup filters.
     */
    final Class<?>[] classes() {
        if (classes instanceof Class[]) {
            return (Class[]) classes;
        } else {
            return new Class[] { (Class) classes };
        }
    }

    /** Does a check whether two classes are accessible (in the super/sub class)
     * releation ship without walking thru any of the classes mentioned in the
     * barrier.
     */
    private static boolean isAccessible(Class<?>[] barriers, Class<?> from, Class<?> to) {
        if ((to == null) || !from.isAssignableFrom(to)) {
            // no way to reach each other by walking up
            return false;
        }

        for (int i = 0; i < barriers.length; i++) {
            if (to == barriers[i]) {
                return false;
            }
        }

        if (from == to) {
            return true;
        }

        //
        // depth first search
        //
        if (isAccessible(barriers, from, to.getSuperclass())) {
            return true;
        }

        Class[] interfaces = to.getInterfaces();

        for (int i = 0; i < interfaces.length; i++) {
            if (isAccessible(barriers, from, interfaces[i])) {
                return true;
            }
        }

        return false;
    }

    /** based on type decides whether the class accepts or not anObject
     * @param from the base type of the query
     * @param to depending on value of type either Object, Class or Item
     * @param type 0,1,2 for Object, Class or Item
     * @return true if we can access the to from from by walking around the bariers
     */
    private final boolean isObjectAccessible(Class from, Object to, int type) {
        if (to == null) {
            return false;
        }

        return isObjectAccessible(classes(), from, to, type);
    }

    /** based on type decides whether the class accepts or not anObject
     * @param barriers classes to avoid when testing reachability
     * @param from the base type of the query
     * @param to depending on value of type either Object, Class or Item
     * @param type 0,1,2 for Object, Class or Item
     * @return true if we can access the to from from by walking around the bariers
     */
    static final boolean isObjectAccessible(Class[] barriers, Class from, Object to, int type) {
        if (to == null) {
            return false;
        }

        switch (type) {
        case 0:
            return isAccessible(barriers, from, to.getClass());

        case 1:
            return isAccessible(barriers, from, (Class) to);

        case 2: {
            Item item = (Item) to;

            return isAccessible(barriers, from, item.getType());
        }

        default:
            throw new IllegalStateException("Type: " + type);
        }
    }

    /** Filters collection accroding to set of given filters.
     */
    final <E, T extends Collection<E>> T filter(
        Class<?>[] arr, Class<?> from, T c, int type, T prototype
    ) {
        T ret = null;


// optimistic strategy expecting we will not need to filter
TWICE: 
        for (;;) {
            Iterator<E> it = c.iterator();
BIG: 
            while (it.hasNext()) {
                E res = it.next();

                if (!isObjectAccessible(arr, from, res, type)) {
                    if (ret == null) {
                        // we need to restart the scanning again 
                        // as there is an active filter
                        ret = prototype;
                        continue TWICE;
                    }

                    continue BIG;
                }

                if (ret != null) {
                    // if we are running the second round from TWICE
                    ret.add(res);
                }
            }

            // ok, processed
            break TWICE;
        }

        return (ret != null) ? ret : c;
    }

    /** Delegating result that filters unwanted items and instances.
     */
    private final class R<T> extends WaitableResult<T> implements LookupListener {
        private Result<T> result;
        private Object listeners;
        private Class<?> from;

        R(Class<?> from, Result<T> delegate) {
            this.from = from;
            this.result = delegate;
        }

        protected void beforeLookup(Template t) {
            if (result instanceof WaitableResult) {
                ((WaitableResult) result).beforeLookup(t);
            }
        }

        public void addLookupListener(LookupListener l) {
            boolean add;

            synchronized (this) {
                listeners = AbstractLookup.modifyListenerList(true, l, listeners);
                add = listeners != null;
            }

            if (add) {
                result.addLookupListener(this);
            }
        }

        public void removeLookupListener(LookupListener l) {
            boolean remove;

            synchronized (this) {
                listeners = AbstractLookup.modifyListenerList(false, l, listeners);
                remove = listeners == null;
            }

            if (remove) {
                result.removeLookupListener(this);
            }
        }

        public Collection<? extends T> allInstances() {
            return openCol(result.allInstances(), 0);
        }

        private <S> Collection<S> openCol(Collection<S> c, int type) {
            return filter(classes(), from, c, type, new ArrayList<S>(c.size()));
        }

        public Set<Class<? extends T>> allClasses() {
            return filter(classes(), from, result.allClasses(), 1, new HashSet<Class<? extends T>>());
        }

        public Collection<? extends Item<T>> allItems() {
            return openCol(result.allItems(), 2);
        }

        public void resultChanged(org.openide.util.LookupEvent ev) {
            if (ev.getSource() == result) {
                collectFires(null);
            }
        }

        protected void collectFires(Collection<Object> evAndListeners) {
            LookupListener[] arr;

            synchronized (this) {
                if (listeners == null) {
                    return;
                }

                if (listeners instanceof LookupListener) {
                    arr = new LookupListener[] { (LookupListener) listeners };
                } else {
                    ArrayList<?> l = (ArrayList<?>) listeners;
                    arr = l.toArray(new LookupListener[l.size()]);
                }
            }

            final LookupListener[] ll = arr;
            final org.openide.util.LookupEvent newev = new org.openide.util.LookupEvent(this);
            AbstractLookup.notifyListeners(ll, newev, evAndListeners);
        }
    }
}
