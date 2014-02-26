/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.hk2.utilities.reflection;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * This is a model for a class that is somewhat more convienient from the point
 * of view of HK2 than strait reflection
 * 
 * @author jwells
 *
 */
public class ClassReflectionModel {
    private final static String CONVENTION_POST_CONSTRUCT = "postConstruct";
    private final static String CONVENTION_PRE_DESTROY = "preDestroy";
    
    private final static String USE_SOFT_REFERENCE_PROPERTY = "org.jvnet.hk2.properties.useSoftReference";
    public final static boolean USE_SOFT_REFERENCE;
    static {
        USE_SOFT_REFERENCE = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return Boolean.parseBoolean(System.getProperty(USE_SOFT_REFERENCE_PROPERTY, "true"));
            }
            
        });
    }
    
    static final Map<String, Class<?>> scalarClasses = new HashMap<String, Class<?>>();
    static {
        scalarClasses.put(boolean.class.getName(), boolean.class);
        scalarClasses.put(short.class.getName(), short.class);
        scalarClasses.put(int.class.getName(), int.class);
        scalarClasses.put(long.class.getName(), long.class);
        scalarClasses.put(float.class.getName(), float.class);
        scalarClasses.put(double.class.getName(), double.class);
        scalarClasses.put(byte.class.getName(), byte.class);
        scalarClasses.put(char.class.getName(), char.class);
    }
    
    private final Object lock = new Object();
    
    /*
     * These caches must not hold any reference to a class (or even a Field or Method which has
     * backpointers to their host classes) because in OSGi-like scenarios when a classloader
     * is removed from the system these caches will keep that classloader referenced.
     * 
     * So instead we keep Strings for class names and MemberDescriptors (which do not hold onto
     * classes) for Fields and Methods.  However, that represents a memory leak by itself, since
     * the classes that are now gone would still forever be represented in these caches.
     * 
     * In order to fix that problem we keep PhantomReferences to the classes.  When the
     * class has no more references the PhantomReference will be enqueued in the deadClasses
     * ReferenceQueue and be cleaned the next time we touch this cache
     */
    private final Map<String, LinkedHashSet<MemberKey>> methodKeyCache = new HashMap<String, LinkedHashSet<MemberKey>>();
    private final Map<String, LinkedHashSet<MemberKey>> fieldCache = new HashMap<String, LinkedHashSet<MemberKey>>();
    private final Map<String, MemberDescriptor> postConstructCache = new HashMap<String, MemberDescriptor>();
    private final Map<String, MemberDescriptor> preDestroyCache = new HashMap<String, MemberDescriptor>();
    
    private final Map<String, ClazzPhantomReference> phantoms = new HashMap<String, ClazzPhantomReference>();
    private final ReferenceQueue<Class<?>> deadClasses = new ReferenceQueue<Class<?>>();
    
    /**
     * Gets all methods, public, private etc on this class and on all
     * subclasses
     *
     * @param clazz The class to check out
     * @return A set of all methods on this class
     */
    public Set<Method> getAllMethods(Class<?> clazz) {
        LinkedHashSet<Method> retVal = new LinkedHashSet<Method>();

        LinkedHashSet<MemberKey> keys = new LinkedHashSet<MemberKey>();

        getAllMethodKeys(clazz, keys);

        Method postConstructMethod = null;
        Method preDestroyMethod = null;
        for (MemberKey key : keys) {
            retVal.add(key.getBackingMethod(clazz));
            if (key.isPostConstruct()) {
                postConstructMethod = key.getBackingMethod(clazz);
            }
            if (key.isPreDestroy()) {
                preDestroyMethod = key.getBackingMethod(clazz);
            }
        }

        synchronized (lock) {
            // It is ok for postConstructMethod to be null
            postConstructCache.put(clazz.getName(), new MemberDescriptor(this, postConstructMethod));

            // It is ok for preDestroyMethod to be null
            preDestroyCache.put(clazz.getName(), new MemberDescriptor(this, preDestroyMethod));
        }

        return retVal;
    }
    
    private void getAllMethodKeys(Class<?> clazz, LinkedHashSet<MemberKey> currentMethods) {
        if (clazz == null) return;

        Set<MemberKey> discoveredMethods;
        synchronized (lock) {
            discoveredMethods = methodKeyCache.get(clazz.getName());
        }

        if (discoveredMethods != null) {
            currentMethods.addAll(discoveredMethods);
            return;
        }

        // Do superclasses first, so that inherited methods are
        // overriden in the set
        getAllMethodKeys(clazz.getSuperclass(), currentMethods);

        for (Method method : getDeclaredMethods(clazz)) {
            boolean isPostConstruct = isPostConstruct(method);
            boolean isPreDestroy = isPreDestroy(method);

            currentMethods.add(new MemberKey(this, method, isPostConstruct, isPreDestroy));
        }

        synchronized (lock) {
            methodKeyCache.put(clazz.getName(), new LinkedHashSet<MemberKey>(currentMethods));
        }
    }
    
    private static boolean isPostConstruct(Method m) {
        if (m.isAnnotationPresent(PostConstruct.class)) return true;

        if (m.getParameterTypes().length != 0) return false;
        return CONVENTION_POST_CONSTRUCT.equals(m.getName());
    }

    private static boolean isPreDestroy(Method m) {
        if (m.isAnnotationPresent(PreDestroy.class)) return true;

        if (m.getParameterTypes().length != 0) return false;
        return CONVENTION_PRE_DESTROY.equals(m.getName());
    }
    
    /**
     * Get all fields on this class and all subclasses
     *
     * @param clazz The class to inspect
     * @return A set of all the fields on this class
     */
    public Set<Field> getAllFields(Class<?> clazz, List<Throwable> exceptions) {
        LinkedHashSet<Field> retVal = new LinkedHashSet<Field>();

        LinkedHashSet<MemberKey> keys = new LinkedHashSet<MemberKey>();

        getAllFieldKeys(clazz, keys, exceptions);

        for (MemberKey key : keys) {
            retVal.add(key.getBackingField(clazz));
        }

        return retVal;
    }
    
    private void getAllFieldKeys(Class<?> clazz, LinkedHashSet<MemberKey> currentFields, List<Throwable> exceptions) {
        if (clazz == null) return;

        Set<MemberKey> discovered;
        synchronized (lock) {
            discovered = fieldCache.get(clazz.getName());
        }

        if (discovered != null) {
            currentFields.addAll(discovered);
            return;
        }

        // Do superclasses first, so that inherited methods are
        // overriden in the set
        getAllFieldKeys(clazz.getSuperclass(), currentFields, exceptions);

        try {
            for (Field field : getDeclaredFields(clazz)) {
                currentFields.add(new MemberKey(this, field, false, false));
            }

            synchronized (lock) {
                fieldCache.put(clazz.getName(), new LinkedHashSet<MemberKey>(currentFields));
            }
        } catch (Throwable th) {
            exceptions.add(new IllegalStateException("Error while getting fields of class " + clazz.getName(), th));
        }

    }
    
    /**
     * Finds the post construct method on this class
     * @param clazz The class to search for the post construct
     * @param matchingClass The PostConstruct class, done this way to solve layering
     * @return The post construct method or null
     */
    public Method findPostConstruct(Class<?> clazz, Class<?> matchingClass) throws IllegalArgumentException {
        if (matchingClass.isAssignableFrom(clazz)) {
            // A little performance optimization
            try {
                return clazz.getMethod(ClassReflectionModel.CONVENTION_POST_CONSTRUCT, new Class<?>[0]);
            }
            catch (NoSuchMethodException e) {
                return null;
            }
        }

        boolean containsKey;
        Method retVal;
        synchronized (lock) {
            containsKey = postConstructCache.containsKey(clazz.getName());
            MemberDescriptor md = postConstructCache.get(clazz.getName());
            retVal = (md == null) ? null : md.getMethod(clazz);
        }

        if (!containsKey) {
            getAllMethods(clazz);  // Fills in the cache

            synchronized (lock) {
                MemberDescriptor md = postConstructCache.get(clazz.getName());
                retVal = (md == null) ? null : md.getMethod(clazz);
            }
        }

        if (retVal == null) return null;

        if (retVal.isAnnotationPresent(PostConstruct.class) &&
                (retVal.getParameterTypes().length != 0)) {
            throw new IllegalArgumentException("The method " + Pretty.method(retVal) +
                        " annotated with @PostConstruct must not have any arguments");
        }

        return retVal;
    }

    /**
     * Finds the pre destroy method on this class
     * @param clazz The class to search for the pre destroy method
     * @param matchingClass The PreDestroy class, done this way to solve layering
     * @return The pre destroy method or null
     */
    public Method findPreDestroy(Class<?> clazz, Class<?> matchingClass) throws IllegalArgumentException {
        if (matchingClass.isAssignableFrom(clazz)) {
            try {
                return clazz.getMethod(CONVENTION_PRE_DESTROY, new Class<?>[0]);
            }
            catch (NoSuchMethodException e) {
                return null;
            }
        }

        boolean containsKey;
        Method retVal;
        synchronized (lock) {
            containsKey = preDestroyCache.containsKey(clazz.getName());
            MemberDescriptor md = preDestroyCache.get(clazz.getName());
            retVal = (md == null) ? null : md.getMethod(clazz);
        }

        if (!containsKey) {
            getAllMethods(clazz);  // Fills in the cache

            synchronized (lock) {
                MemberDescriptor md = preDestroyCache.get(clazz.getName());
                retVal = (md == null) ? null : md.getMethod(clazz);
            }
        }

        if (retVal == null) return null;

        if (retVal.isAnnotationPresent(PreDestroy.class) &&
                (retVal.getParameterTypes().length != 0)) {
            throw new IllegalArgumentException("The method " + Pretty.method(retVal) +
                    " annotated with @PreDestroy must not have any arguments");
        }

        return retVal;
    }
    
    private static Field[] getDeclaredFields(final Class<?> clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Field[]>() {

            @Override
            public Field[] run() {
                return clazz.getDeclaredFields();
            }

        });

    }
    
    /**
     * Get the declared methods of the given class.
     *
     * @param clazz  the class
     */
    private static Method[] getDeclaredMethods(final Class<?> clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Method[]>() {

            @Override
            public Method[] run() {
                return clazz.getDeclaredMethods();
            }

        });

    }
    
    void cleanCache() {
        synchronized (lock) {
            Reference<? extends Class<?>> ref;
            while ((ref = deadClasses.poll()) != null) {
            
                ClazzPhantomReference cpr = (ClazzPhantomReference) ref;
            
                phantoms.remove(cpr.clazzName);
                postConstructCache.remove(cpr.clazzName);
                preDestroyCache.remove(cpr.clazzName);
                methodKeyCache.remove(cpr.clazzName);
                fieldCache.remove(cpr.clazzName);
            }
        }
    }
    
    private class ClazzPhantomReference extends PhantomReference<Class<?>> {
        private final String clazzName;
        
        private ClazzPhantomReference(Class<?> reference) {
            super(reference, deadClasses);
            clazzName = reference.getName();
        }
    }
}
