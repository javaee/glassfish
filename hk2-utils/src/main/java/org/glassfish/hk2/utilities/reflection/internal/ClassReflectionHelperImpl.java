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
package org.glassfish.hk2.utilities.reflection.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.glassfish.hk2.utilities.cache.Computable;
import org.glassfish.hk2.utilities.cache.HybridCacheEntry;
import org.glassfish.hk2.utilities.cache.LRUHybridCache;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.MethodWrapper;
import org.glassfish.hk2.utilities.reflection.Pretty;

/**
 * @author jwells
 *
 */
public class ClassReflectionHelperImpl implements ClassReflectionHelper {
    private final int MAX_CACHE_SIZE = 200;
    
    private final static String CONVENTION_POST_CONSTRUCT = "postConstruct";
    private final static String CONVENTION_PRE_DESTROY = "preDestroy";
    
    private final static Set<MethodWrapper> OBJECT_METHODS = getObjectMethods();
    private final static Set<Field> OBJECT_FIELDS = getObjectFields();
    
    private final LRUHybridCache<LifecycleKey, Method> postConstructCache =
            new LRUHybridCache<LifecycleKey, Method>(MAX_CACHE_SIZE, new Computable<LifecycleKey, HybridCacheEntry<Method>>() {

                @Override
                public HybridCacheEntry<Method> compute(LifecycleKey key) {
                    return postConstructCache.createCacheEntry(key, getPostConstructMethod(key.clazz, key.matchingClass), false);
                }
                
            });
    
    private final LRUHybridCache<LifecycleKey, Method> preDestroyCache =
            new LRUHybridCache<LifecycleKey, Method>(MAX_CACHE_SIZE, new Computable<LifecycleKey, HybridCacheEntry<Method>>() {

                @Override
                public HybridCacheEntry<Method> compute(LifecycleKey key) {
                    return preDestroyCache.createCacheEntry(key, getPreDestroyMethod(key.clazz, key.matchingClass), false);
                }
                
            });
    
    private final LRUHybridCache<Class<?>, Set<MethodWrapper>> methodCache =
            new LRUHybridCache<Class<?>, Set<MethodWrapper>>(MAX_CACHE_SIZE, new Computable<Class<?>, HybridCacheEntry<Set<MethodWrapper>>>() {

                @Override
                public HybridCacheEntry<Set<MethodWrapper>> compute(Class<?> key) {
                    return methodCache.createCacheEntry(key, getAllMethodWrappers(key), false);
                }
                
            });
    
    
    private final LRUHybridCache<Class<?>, Set<Field>> fieldCache =
            new LRUHybridCache<Class<?>, Set<Field>>(MAX_CACHE_SIZE, new Computable<Class<?>, HybridCacheEntry<Set<Field>>>() {

                @Override
                public HybridCacheEntry<Set<Field>> compute(Class<?> key) {
                    return fieldCache.createCacheEntry(key, getAllFieldWrappers(key), false);
                }
                
            });
    
    private static Set<MethodWrapper> getObjectMethods() {
        return AccessController.doPrivileged(new PrivilegedAction<Set<MethodWrapper>>() {

            @Override
            public Set<MethodWrapper> run() {
                Set<MethodWrapper> retVal = new HashSet<MethodWrapper>();
                
                for (Method method : Object.class.getDeclaredMethods()) {
                    retVal.add(new MethodWrapperImpl(method));                   
                }
                
                return retVal;
            }
            
        });
        
    }
    
    private static Set<Field> getObjectFields() {
        return AccessController.doPrivileged(new PrivilegedAction<Set<Field>>() {

            @Override
            public Set<Field> run() {
                Set<Field> retVal = new HashSet<Field>();
                
                for (Field field : Object.class.getDeclaredFields()) {
                    retVal.add(field);                   
                }
                
                return retVal;
            }
            
        });
        
    }
    
    private static Method[] secureGetDeclaredMethods(final Class<?> clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Method[]>() {

            @Override
            public Method[] run() {
                return clazz.getDeclaredMethods();
            }
            
        });
    }
    
    private static Field[] secureGetDeclaredFields(final Class<?> clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Field[]>() {

            @Override
            public Field[] run() {
                return clazz.getDeclaredFields();
            }
            
        });
    }
    
    /**
     * Gets the EXACT set of MethodWrappers on this class only.  No subclasses.  So
     * this set should be considered RAW and has not taken into account any subclasses
     * 
     * @param clazz The class to examine
     * @return
     */
    private static Set<MethodWrapper> getDeclaredMethodWrappers(final Class<?> clazz) {
        Method declaredMethods[] = secureGetDeclaredMethods(clazz);
        
        Set<MethodWrapper> retVal = new HashSet<MethodWrapper>();
        for (Method method : declaredMethods) {
            retVal.add(new MethodWrapperImpl(method));
        }
        
        return retVal;
    }
    
    /**
     * Gets the EXACT set of FieldWrappers on this class only.  No subclasses.  So
     * this set should be considered RAW and has not taken into account any subclasses
     * 
     * @param clazz The class to examine
     * @return
     */
    private static Set<Field> getDeclaredFieldWrappers(final Class<?> clazz) {
        Field declaredFields[] = secureGetDeclaredFields(clazz);
        
        Set<Field> retVal = new HashSet<Field>();
        for (Field field : declaredFields) {
            retVal.add(field);
        }
        
        return retVal;
    }
    
    public static Set<Field> getAllFieldWrappers(Class<?> clazz) {
        if (clazz == null) return Collections.emptySet();
        if (Object.class.equals(clazz)) return OBJECT_FIELDS;
        if (clazz.isInterface()) return Collections.emptySet();
        
        Set<Field> retVal = new HashSet<Field>();
        
        retVal.addAll(getDeclaredFieldWrappers(clazz));
        retVal.addAll(getAllFieldWrappers(clazz.getSuperclass()));
        
        return retVal;
    }
    
    public static Set<MethodWrapper> getAllMethodWrappers(Class<?> clazz) {
        if (clazz == null) return Collections.emptySet();
        if (Object.class.equals(clazz)) return OBJECT_METHODS;
        
        Set<MethodWrapper> retVal = new HashSet<MethodWrapper>();
        
        if (clazz.isInterface()) {
            for (Method m : clazz.getDeclaredMethods()) {
                MethodWrapper wrapper = new MethodWrapperImpl(m);
                
                retVal.add(wrapper);
            }
            
            for (Class<?> extendee : clazz.getInterfaces()) {
                retVal.addAll(getAllMethodWrappers(extendee));
            }
        }
        else {
            retVal.addAll(getDeclaredMethodWrappers(clazz));
            retVal.addAll(getAllMethodWrappers(clazz.getSuperclass()));
        }
        
        return retVal;
    }
    
    private static boolean isPostConstruct(Method m) {
        if (m.isAnnotationPresent(PostConstruct.class)) {
            if (m.getParameterTypes().length != 0) {
                throw new IllegalArgumentException("The method " + Pretty.method(m) +
                        " annotated with @PostConstruct must not have any arguments");
            }
            return true;
        }

        if (m.getParameterTypes().length != 0) return false;
        return CONVENTION_POST_CONSTRUCT.equals(m.getName());
    }
    
    private static boolean isPreDestroy(Method m) {
        if (m.isAnnotationPresent(PreDestroy.class)) {
            if (m.getParameterTypes().length != 0) {
                throw new IllegalArgumentException("The method " + Pretty.method(m) +
                    " annotated with @PreDestroy must not have any arguments");
            }
            
            return true;
        }

        if (m.getParameterTypes().length != 0) return false;
        return CONVENTION_PRE_DESTROY.equals(m.getName());
    }
    
    private Method getPostConstructMethod(Class<?> clazz, Class<?> matchingClass) {
        if (clazz == null || Object.class.equals(clazz)) return null;
        
        if (matchingClass.isAssignableFrom(clazz)) {
            // A little performance optimization
            Method retVal;
            
            try {
                retVal = clazz.getMethod(CONVENTION_POST_CONSTRUCT, new Class<?>[0]);
            }
            catch (NoSuchMethodException e) {
                retVal = null;
            }
            
            return retVal;
        }
        
        for (MethodWrapper wrapper : getAllMethods(clazz)) {
            Method m = wrapper.getMethod();
            if (isPostConstruct(m)) return m;
        }
        
        return null;
    }
    
    private Method getPreDestroyMethod(Class<?> clazz, Class<?> matchingClass) {
        if (clazz == null || Object.class.equals(clazz)) return null;
        
        if (matchingClass.isAssignableFrom(clazz)) {
            // A little performance optimization
            Method retVal;
            
            try {
                retVal = clazz.getMethod(CONVENTION_PRE_DESTROY, new Class<?>[0]);
            }
            catch (NoSuchMethodException e) {
                retVal = null;
            }
            
            return retVal;
        }
        
        for (MethodWrapper wrapper : getAllMethods(clazz)) {
            Method m = wrapper.getMethod();
            if (isPreDestroy(m)) return m;
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.reflection.ClassReflectionHelper#getAllMethods(java.lang.Class)
     */
    @Override
    public Set<MethodWrapper> getAllMethods(final Class<?> clazz) {
        return methodCache.compute(clazz).getValue();
    }
    
    @Override
    public Set<Field> getAllFields(final Class<?> clazz) {
        return fieldCache.compute(clazz).getValue();
    }
    
    @Override
    public Method findPostConstruct(final Class<?> clazz, Class<?> matchingClass)
            throws IllegalArgumentException {
        return postConstructCache.compute(new LifecycleKey(clazz, matchingClass)).getValue();
    }

    @Override
    public Method findPreDestroy(final Class<?> clazz, Class<?> matchingClass)
            throws IllegalArgumentException {
        return preDestroyCache.compute(new LifecycleKey(clazz, matchingClass)).getValue();
    }
    
    @Override
    public void clean(Class<?> clazz) {
        while ((clazz != null) && !Object.class.equals(clazz)) {
            postConstructCache.remove(new LifecycleKey(clazz, null));
            preDestroyCache.remove(new LifecycleKey(clazz, null));
            methodCache.remove(clazz);
            fieldCache.remove(clazz);
            
            clazz = clazz.getSuperclass();
        }
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.reflection.ClassReflectionHelper#createMethodWrapper(java.lang.reflect.Method)
     */
    @Override
    public MethodWrapper createMethodWrapper(Method m) {
        return new MethodWrapperImpl(m);
    }

    @Override
    public void dispose() {
        postConstructCache.clear();
        preDestroyCache.clear();
        methodCache.clear();
        fieldCache.clear();
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.reflection.ClassReflectionHelper#size()
     */
    @Override
    public int size() {
        return postConstructCache.size() +
                preDestroyCache.size() +
                methodCache.size() +
                fieldCache.size();
    }
    
    @Override
    public String toString() {
        return "ClassReflectionHelperImpl(" + System.identityHashCode(this) + ")";
    }
    
    private final static class LifecycleKey {
        private final Class<?> clazz;
        private final Class<?> matchingClass;
        private final int hash;
        
        private LifecycleKey(Class<?> clazz, Class<?> matchingClass) {
            this.clazz = clazz;
            this.matchingClass = matchingClass;
            hash = clazz.hashCode();
        }
        
        @Override
        public int hashCode() { return hash; }
        
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof LifecycleKey)) return false;
            return clazz.equals(((LifecycleKey) o).clazz);
        }
    }
}
