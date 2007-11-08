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

import java.util.Arrays;
import java.util.Collections;
import org.openide.util.Lookup;

/**
 * Static factory methods for creating common lookup implementations.
 *
 * @author David Strupl
 * @since 2.21
 */
public class Lookups {

    /** static methods only */
    private Lookups() {}

    /**
     * Creates a singleton lookup. It means lookup that contains only
     * one object specified via the supplied parameter. The lookup will
     * either return the object or null if the supplied template does
     * not match the class. If the specified argument is null the method
     * will end with NullPointerException.
     * @return Fully initialized lookup object ready to use
     * @throws NullPointerException if the supplied argument is null
     * @since 2.21
     */
    public static Lookup singleton(Object objectToLookup) {
        if (objectToLookup == null) {
            throw new NullPointerException();
        }

        // performance of the resulting lookup might be further
        // improved by providing specialized singleton result (and lookup)
        // instead of using SimpleResult
        return new SimpleLookup(Collections.singleton(objectToLookup));
    }

    /**
     * Creates a lookup that contains an array of objects specified via the
     * parameter. The resulting lookup is fixed in the following sense: it
     * contains only fixed set of objects passed in by the array parameter.
     * Its contents never changes so registering listeners on such lookup
     * does not have any observable effect (the listeners are never called).
     *
     * @param objectsToLookup list of objects to include
     * @return Fully initialized lookup object ready to use
     * @throws NullPointerException if the supplied argument is null
     * @since 2.21
     *
     */
    public static Lookup fixed(Object... objectsToLookup) {
        if (objectsToLookup == null) {
            throw new NullPointerException();
        }

        return new SimpleLookup(Arrays.asList(objectsToLookup));
    }

    /**
     * Creates a lookup that contains an array of objects specified via the
     * parameter. The resulting lookup is fixed in the following sense: it
     * contains only fixed set of objects passed in by the array parameter.
     * The objects returned from this lookup are converted to real objects
     * before they are returned by the lookup.
     * Its contents never changes so registering listeners on such lookup
     * does not have any observable effect (the listeners are never called).
     *
     * @return Fully initialized lookup object ready to use
     * @throws NullPointerException if the any of the arguments is null
     * @since 2.21
     *
     */
    public static <T,R> Lookup fixed(T[] keys, InstanceContent.Convertor<? super T,R> convertor) {
        if (keys == null) {
            throw new NullPointerException();
        }

        if (convertor == null) {
            throw new NullPointerException();
        }

        return new SimpleLookup(Arrays.asList(keys), convertor);
    }

    /** Creates a lookup that delegates to another one but that one can change
     * from time to time. The returned lookup checks every time somebody calls
     * <code>lookup</code> or <code>lookupItem</code> method whether the
     * provider still returns the same lookup. If not, it updates state of
     * all <code>Lookup.Result</code>s that it created (and that still exists).
     * <P>
     * The user of this method has to implement its provider's <code>getLookup</code>
     * method (must be thread safe and fast, will be called often and from any thread)
     * pass it to this method and use the returned lookup. Whenever the user
     * changes the return value from the <code>getLookup</code> method and wants
     * to notify listeners on the lookup about that it should trigger the event
     * firing, for example by calling <code>lookup.lookup (Object.class)</code>
     * that forces check of the return value of <code>getLookup</code>.
     *
     * @param provider the provider that returns a lookup to delegate to
     * @return lookup delegating to the lookup returned by the provider
     * @since 3.9
     */
    public static Lookup proxy(Lookup.Provider provider) {
        return new SimpleProxyLookup(provider);
    }

    /** Returns a lookup that implements the JDK1.3 JAR services mechanism and delegates
     * to META-INF/services/name.of.class files.
     * <p>Note: It is not dynamic - so if you need to change the classloader or JARs,
     * wrap it in a {@link ProxyLookup} and change the delegate when necessary.
     * Existing instances will be kept if the implementation classes are unchanged,
     * so there is "stability" in doing this provided some parent loaders are the same
     * as the previous ones.
     * @since 3.35
     */
    public static Lookup metaInfServices(ClassLoader classLoader) {
        return new MetaInfServicesLookup(classLoader);
    }

    /** Creates a lookup that wraps another one and filters out instances
     * of specified classes. If you have a lookup and
     * you want to remove all instances of ActionMap you can use:
     * <pre>
     * l = Lookups.exclude(lookup, ActionMap.class);
     * </pre>
     * Then anybody who asks for <code>l.lookup(ActionMap.class)</code> or
     * subclass will get <code>null</code>. Even if the original lookup contains the
     * value.
     * To create empty lookup (well, just an example, otherwise use {@link Lookup#EMPTY}) one could use:
     * <pre>
     * Lookup.exclude(anyLookup, Object.class);
     * </pre>
     * as any instance in any lookup is of type Object and thus would be excluded.
     * <p>
     * The complete behavior can be described as <code>classes</code> being
     * a barrier. For an object not to be excluded, there has to be an inheritance
     * path between the queried class and the actual class of the instance,
     * that is not blocked by any of the excluded classes:
     * <pre>
     * interface A {}
     * interface B {}
     * class C implements A, B {}
     * Object c = new C();
     * Lookup l1 = Lookups.singleton(c);
     * Lookup l2 = Lookups.exclude(l1, A.class);
     * assertNull("A is directly excluded", l2.lookup(A.class));
     * assertEquals("Returns C as A.class is not between B and C", c, l2.lookup(B.class));
     * </pre>
     * For more info check the
     * <a href="http://www.netbeans.org/source/browse/openide/test/unit/src/org/openide/util/lookup/ExcludingLookupTest.java">
     * excluding lookup tests</a> and the discussion in issue
     * <a href="http://openide.netbeans.org/issues/show_bug.cgi?id=53058">53058</a>.
     *
     * @param lookup the original lookup that should be filtered
     * @param classes array of classes those instances should be excluded
     * @since 5.4
     */
    public static Lookup exclude(Lookup lookup, Class... classes) {
        return new ExcludingLookup(lookup, classes);
    }

    /** Creates <code>Lookup.Item</code> representing the instance passed in.
     *
     * @param instance the object for which Lookup.Item should be creted
     * @param id unique identification of the object, for details see {@link org.openide.util.Lookup.Item#getId},
     * can be <code>null</code>
     * @return lookup item representing instance
     * @since 4.8
     */
    public static <T> Lookup.Item<T> lookupItem(T instance, String id) {
        return new LookupItem<T>(instance, id);
    }

    private static class LookupItem<T> extends Lookup.Item<T> {
        private String id;
        private T instance;

        public LookupItem(T instance) {
            this(instance, null);
        }

        public LookupItem(T instance, String id) {
            this.id = id;
            this.instance = instance;
        }

        public String getDisplayName() {
            return getId();
        }

        public String getId() {
            return (id == null) ? instance.toString() : id;
        }

        public T getInstance() {
            return instance;
        }

        @SuppressWarnings("unchecked")
        public Class<? extends T> getType() {
            return (Class<? extends T>)instance.getClass();
        }

        public boolean equals(Object object) {
            if (object instanceof LookupItem) {
                return instance == ((LookupItem) object).getInstance();
            }

            return false;
        }

        public int hashCode() {
            return instance.hashCode();
        }
    }
     // End of LookupItem class
}
