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
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.MethodWrapper;
import org.glassfish.hk2.utilities.reflection.Pretty;

/**
 * @author jwells
 *
 */
public class ClassReflectionHelperImpl implements ClassReflectionHelper {
    private final static String CONVENTION_POST_CONSTRUCT = "postConstruct";
    private final static String CONVENTION_PRE_DESTROY = "preDestroy";
    
    private final static Set<MethodWrapper> OBJECT_METHODS = getObjectMethods();
    private final static Set<Field> OBJECT_FIELDS = getObjectFields();
    
    private final ConcurrentHashMap<Class<?>, MethodPresentValue> postConstructCache =
            new ConcurrentHashMap<Class<?>, MethodPresentValue>();
    private final ConcurrentHashMap<Class<?>, MethodPresentValue> preDestroyCache =
            new ConcurrentHashMap<Class<?>, MethodPresentValue>();
    private final ConcurrentHashMap<Class<?>, Set<MethodWrapper>> methodCache =
            new ConcurrentHashMap<Class<?>, Set<MethodWrapper>>();
    private final ConcurrentHashMap<Class<?>, Set<Field>> fieldCache =
            new ConcurrentHashMap<Class<?>, Set<Field>>();
    
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
    
    /**
     * Gets the EXACT set of MethodWrappers on this class only.  No subclasses.  So
     * this set should be considered RAW and has not taken into account any subclasses
     * 
     * @param clazz The class to examine
     * @return
     */
    private Set<MethodWrapper> getDeclaredMethodWrappers(final Class<?> clazz) {
        Method declaredMethods[] = clazz.getDeclaredMethods();
        
        Set<MethodWrapper> retVal = new HashSet<MethodWrapper>();
        for (Method method : declaredMethods) {
            retVal.add(new MethodWrapperImpl(method));
            
            // Since postConstruct and preDestroy are top down it is fine to fill in the cache here.
            // In other words if this class has a direct postConstruct/preDestroy then it is the method
            // that should win.  Hence we can often pre-populate the cache here
            if ((isPostConstruct(method) || isPreDestroy(method)) && (method.getParameterTypes().length == 0)) {
                if (isPostConstruct(method) && !postConstructCache.containsKey(clazz)) {
                    postConstructCache.putIfAbsent(clazz, new MethodPresentValue(method));
                }
                if (isPreDestroy(method) && !preDestroyCache.containsKey(clazz)) {
                    preDestroyCache.putIfAbsent(clazz, new MethodPresentValue(method));
                }
            }
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
        Field declaredFields[] = clazz.getDeclaredFields();
        
        Set<Field> retVal = new HashSet<Field>();
        for (Field field : declaredFields) {
            retVal.add(field);
        }
        
        return retVal;
    }
    
    public Set<Field> getAllFieldWrappers(Class<?> clazz) {
        if (clazz == null) return Collections.emptySet();
        if (Object.class.equals(clazz)) return OBJECT_FIELDS;
        
        Set<Field> retVal = fieldCache.get(clazz);
        if (retVal != null) {
            return retVal;
        }
        
        retVal = new HashSet<Field>();
        
        retVal.addAll(getDeclaredFieldWrappers(clazz));
        retVal.addAll(getAllFieldWrappers(clazz.getSuperclass()));
        
        fieldCache.putIfAbsent(clazz, retVal);
        
        return retVal;
    }
    
    public Set<MethodWrapper> getAllMethodWrappers(Class<?> clazz) {
        if (clazz == null) return Collections.emptySet();
        if (Object.class.equals(clazz)) return OBJECT_METHODS;
        
        Set<MethodWrapper> retVal = methodCache.get(clazz);
        if (retVal != null) {
            return retVal;
        }
        
        retVal = new HashSet<MethodWrapper>();
        
        retVal.addAll(getDeclaredMethodWrappers(clazz));
        retVal.addAll(getAllMethodWrappers(clazz.getSuperclass()));
        
        methodCache.putIfAbsent(clazz, retVal);
        
        return retVal;
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

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.reflection.ClassReflectionHelper#getAllMethods(java.lang.Class)
     */
    @Override
    public Set<MethodWrapper> getAllMethods(final Class<?> clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Set<MethodWrapper>>() {

            @Override
            public Set<MethodWrapper> run() {
                return getAllMethodWrappers(clazz);
            }
            
        });
    }
    
    @Override
    public Set<Field> getAllFields(final Class<?> clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Set<Field>>() {

            @Override
            public Set<Field> run() {
                return getAllFieldWrappers(clazz);
            }
            
        });
    }
    
    private Method getPostConstructMethod(Class<?> clazz) {
        if (clazz == null || Object.class.equals(clazz)) return null;
        
        MethodPresentValue cachedValue = postConstructCache.get(clazz);
        if (cachedValue != null) {
            if (!cachedValue.isPresent()) {
                return null;
            }
            
            Method retVal = cachedValue.getMethod();
            if (retVal != null) {
                return retVal;
            }
        }
        
        Method retVal = null;
        for (Method m : clazz.getDeclaredMethods()) {
            if (isPostConstruct(m)) {
                retVal = m;
                break;
            }
        }
        
        if (retVal == null) {
            retVal = getPostConstructMethod(clazz.getSuperclass());
        }
        
        if (retVal != null && retVal.getParameterTypes().length != 0) {
            // We do not cache fails
            throw new IllegalArgumentException("The method " + Pretty.method(retVal) +
                    " annotated with @PostConstruct must not have any arguments");
        }
        
        postConstructCache.putIfAbsent(clazz, new MethodPresentValue(retVal));
        
        return retVal;
    }
    
    private Method getPreDestroyMethod(Class<?> clazz) {
        if (clazz == null || Object.class.equals(clazz)) return null;
        
        MethodPresentValue cachedValue = preDestroyCache.get(clazz);
         if (cachedValue != null) {
            if (!cachedValue.isPresent()) {
                return null;
            }
            
            Method retVal = cachedValue.getMethod();
            if (retVal != null) {
                return retVal;
            }
        }
        
        Method retVal = null;
        for (Method m : clazz.getDeclaredMethods()) {
            if (isPreDestroy(m)) {
                retVal = m;
                break;
            }
        }
        
        if (retVal == null) {
            retVal = getPreDestroyMethod(clazz.getSuperclass());
        }
        
        if (retVal != null && retVal.getParameterTypes().length != 0) {
            // We do not cache fails
            throw new IllegalArgumentException("The method " + Pretty.method(retVal) +
                    " annotated with @PreDestroy must not have any arguments");
        }
        
        preDestroyCache.put(clazz, new MethodPresentValue(retVal));
        
        return retVal;
    }
    
    @Override
    public Method findPostConstruct(final Class<?> clazz, Class<?> matchingClass)
            throws IllegalArgumentException {
        MethodPresentValue cachedValue = postConstructCache.get(clazz);
        if (cachedValue != null) {
            if (!cachedValue.isPresent()) {
                return null;
            }
            
            Method retVal = cachedValue.getMethod();
            if (retVal != null) {
                return retVal;
            }
        }
        
        if (matchingClass.isAssignableFrom(clazz)) {
            // A little performance optimization
            Method retVal;
            
            try {
                retVal = clazz.getMethod(CONVENTION_POST_CONSTRUCT, new Class<?>[0]);
            }
            catch (NoSuchMethodException e) {
                retVal = null;
            }
            
            postConstructCache.put(clazz, new MethodPresentValue(retVal));
        }
        
        return AccessController.doPrivileged(new PrivilegedAction<Method>() {

            @Override
            public Method run() {
                return getPostConstructMethod(clazz);
            }
            
        });
    }

    @Override
    public Method findPreDestroy(final Class<?> clazz, Class<?> matchingClass)
            throws IllegalArgumentException {
        MethodPresentValue cachedValue = preDestroyCache.get(clazz);
        if (cachedValue != null) {
            if (!cachedValue.isPresent()) {
                return null;
            }
            
            Method retVal = cachedValue.getMethod();
            if (retVal != null) {
                return retVal;
            }
        }
        
        if (matchingClass.isAssignableFrom(clazz)) {
            // A little performance optimization
            Method retVal;
            
            try {
                retVal = clazz.getMethod(CONVENTION_PRE_DESTROY, new Class<?>[0]);
            }
            catch (NoSuchMethodException e) {
                retVal = null;
            }
            
            preDestroyCache.put(clazz, new MethodPresentValue(retVal));
        }
        
        return AccessController.doPrivileged(new PrivilegedAction<Method>() {

            @Override
            public Method run() {
                return getPreDestroyMethod(clazz);
            }
            
        });
    }
    
    private static class MethodPresentValue {
        private final Method method;
        
        private MethodPresentValue(Method method) {
            this.method = method;
        }
        
        private boolean isPresent() {
            return method != null;
        }
        
        private Method getMethod() {
            return method;
        }
    }
    
    @Override
    public void clean(Class<?> clazz) {
        while ((clazz != null) && !Object.class.equals(clazz)) {
            postConstructCache.remove(clazz);
            preDestroyCache.remove(clazz);
            methodCache.remove(clazz);
            fieldCache.remove(clazz);
            
            clazz = clazz.getSuperclass();
        }
    }

    @Override
    public void dispose() {
        postConstructCache.clear();
        preDestroyCache.clear();
        methodCache.clear();
        fieldCache.clear();
    }
    
    @Override
    public String toString() {
        return "ClassReflectionHelperImpl(" + System.identityHashCode(this) + ")";
    }

    

    

}
