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

import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.AbstractLookup.Pair;

import java.lang.ref.WeakReference;

import java.util.*;


/** A special content implementation that can be passed to AbstractLookup
 * and provides methods for registration of instances and lazy instances.
 * <PRE>
 *      InstanceContent ic = new InstanceContent ();
 *      AbstractLookup al = new AbstractLookup (ic);
 *
 *      ic.add (new Object ());
 *      ic.add (new Dimension (...));
 *
 *      Dimension theDim = (Dimension)al.lookup (Dimension.class);
 * </PRE>
 *
 * @author  Jaroslav Tulach
 *
 * @since 1.25
 */
public final class InstanceContent extends AbstractLookup.Content {
    /**
     * Create a new, empty content.
     */
    public InstanceContent() {
    }

    /** The method to add instance to the lookup with.
     * @param inst instance
     */
    public final void add(Object inst) {
        addPair(new SimpleItem<Object>(inst));
    }

    /** The method to add instance to the lookup with.
     * @param inst instance
     * @param conv convertor which postponing an instantiation,
     * if <code>conv==null</code> then the instance is registered directly.
     */
    public final <T,R> void add(T inst, Convertor<T,R> conv) {
        addPair(new ConvertingItem<T,R>(inst, conv));
    }

    /** Remove instance.
     * @param inst instance
     */
    public final void remove(Object inst) {
        removePair(new SimpleItem<Object>(inst));
    }

    /** Remove instance added with a convertor.
     * @param inst instance
     * @param conv convertor, if <code>conv==null</code> it is same like
     * remove(Object)
     */
    public final <T,R> void remove(T inst, Convertor<T,R> conv) {
        removePair(new ConvertingItem<T,R>(inst, conv));
    }

    /** Changes all pairs in the lookup to new values. Converts collection of
     * instances to collection of pairs.
     * @param col the collection of (Item) objects
     * @param conv the convertor to use or null
     */
    public final <T,R> void set(Collection<T> col, Convertor<T,R> conv) {
        ArrayList<Pair<?>> l = new ArrayList<Pair<?>>(col.size());
        Iterator<T> it = col.iterator();

        if (conv == null) {
            while (it.hasNext()) {
                l.add(new SimpleItem<T>(it.next()));
            }
        } else {
            while (it.hasNext()) {
                l.add(new ConvertingItem<T,R>(it.next(), conv));
            }
        }

        setPairs(l);
    }

    /** Convertor postpones an instantiation of an object.
     * @since 1.25
     */
    public static interface Convertor<T,R> {
        /** Convert obj to other object. There is no need to implement
         * cache mechanism. It is provided by InstanceLookup.Item.getInstance().
         * Method should be called more than once because Lookup holds
         * just weak reference.
         *
         * @param obj the registered object
         * @return the object converted from this object
         */
        public R convert(T obj);

        /** Return type of converted object.
         * @param obj the registered object
         * @return the class that will be produced from this object (class or
         *      superclass of convert (obj))
         */
        public Class<? extends R> type(T obj);

        /** Computes the ID of the resulted object.
         * @param obj the registered object
         * @return the ID for the object
         */
        public String id(T obj);

        /** The human presentable name for the object.
         * @param obj the registered object
         * @return the name representing the object for the user
         */
        public String displayName(T obj);
    }

    /** Instance of one item representing an object.
     */
    final static class SimpleItem<T> extends Pair<T> {
        private T obj;

        /** Create an item.
         * @obj object to register
         */
        public SimpleItem(T obj) {
            if (obj == null) {
                throw new NullPointerException();
            }
            this.obj = obj;
        }

        /** Tests whether this item can produce object
         * of class c.
         */
        public boolean instanceOf(Class<?> c) {
            return c.isInstance(obj);
        }

        /** Get instance of registered object. If convertor is specified then
         *  method InstanceLookup.Convertor.convertor is used and weak reference
         * to converted object is saved.
         * @return the instance of the object.
         */
        public T getInstance() {
            return obj;
        }

        public boolean equals(Object o) {
            if (o instanceof SimpleItem) {
                return obj.equals(((SimpleItem) o).obj);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return obj.hashCode();
        }

        /** An identity of the item.
         * @return string representing the item, that can be used for
         *   persistance purposes to locate the same item next time
         */
        public String getId() {
            return "IL[" + obj.toString(); // NOI18N
        }

        /** Getter for display name of the item.
         */
        public String getDisplayName() {
            return obj.toString();
        }

        /** Method that can test whether an instance of a class has been created
         * by this item.
         *
         * @param obj the instance
         * @return if the item has already create an instance and it is the same
         *  as obj.
         */
        protected boolean creatorOf(Object obj) {
            return obj == this.obj;
        }

        /** The class of this item.
         * @return the correct class
         */
        @SuppressWarnings("unchecked")
        public Class<? extends T> getType() {
            return (Class<? extends T>)obj.getClass();
        }
    }
     // end of SimpleItem

    /** Instance of one item registered in the map.
     */
    final static class ConvertingItem<T,R> extends Pair<R> {
        /** registered object */
        private T obj;

        /** Reference to converted object. */
        private WeakReference<R> ref;

        /** convertor to use */
        private Convertor<? super T,R> conv;

        /** Create an item.
         * @obj object to register
         * @conv a convertor, can be <code>null</code>.
         */
        public ConvertingItem(T obj, Convertor<? super T,R> conv) {
            this.obj = obj;
            this.conv = conv;
        }

        /** Tests whether this item can produce object
         * of class c.
         */
        public boolean instanceOf(Class<?> c) {
            return c.isAssignableFrom(getType());
        }

        /** Returns converted object or null if obj has not been converted yet
         * or reference was cleared by garbage collector.
         */
        private R getConverted() {
            if (ref == null) {
                return null;
            }

            return ref.get();
        }

        /** Get instance of registered object. If convertor is specified then
         *  method InstanceLookup.Convertor.convertor is used and weak reference
         * to converted object is saved.
         * @return the instance of the object.
         */
        public synchronized R getInstance() {
            R converted = getConverted();

            if (converted == null) {
                converted = conv.convert(obj);
                ref = new WeakReference<R>(converted);
            }

            return converted;
        }

        public boolean equals(Object o) {
            if (o instanceof ConvertingItem) {
                return obj.equals(((ConvertingItem) o).obj);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return obj.hashCode();
        }

        /** An identity of the item.
         * @return string representing the item, that can be used for
         *   persistance purposes to locate the same item next time
         */
        public String getId() {
            return conv.id(obj);
        }

        /** Getter for display name of the item.
         */
        public String getDisplayName() {
            return conv.displayName(obj);
        }

        /** Method that can test whether an instance of a class has been created
         * by this item.
         *
         * @param obj the instance
         * @return if the item has already create an instance and it is the same
         *  as obj.
         */
        protected boolean creatorOf(Object obj) {
            if (conv == null) {
                return obj == this.obj;
            } else {
                return obj == getConverted();
            }
        }

        /** The class of this item.
         * @return the correct class
         */
        @SuppressWarnings("unchecked")
        public Class<? extends R> getType() {
            R converted = getConverted();

            if (converted == null) {
                return conv.type(obj);
            }

            return (Class<? extends R>)converted.getClass();
        }
    }
     // end of ConvertingItem
}
