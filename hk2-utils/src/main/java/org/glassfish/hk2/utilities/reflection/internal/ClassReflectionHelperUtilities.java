/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2016 Oracle and/or its affiliates. All rights reserved.
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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.glassfish.hk2.utilities.reflection.MethodWrapper;
import org.glassfish.hk2.utilities.reflection.Pretty;

/**
 * @author jwells
 *
 */
public class ClassReflectionHelperUtilities {
    final static String CONVENTION_POST_CONSTRUCT = "postConstruct";
    final static String CONVENTION_PRE_DESTROY = "preDestroy";
    
    private final static Set<MethodWrapper> OBJECT_METHODS = getObjectMethods();
    private final static Set<Field> OBJECT_FIELDS = getObjectFields();
    
    private static Set<MethodWrapper> getObjectMethods() {
        return AccessController.doPrivileged(new PrivilegedAction<Set<MethodWrapper>>() {

            @Override
            public Set<MethodWrapper> run() {
                Set<MethodWrapper> retVal = new LinkedHashSet<MethodWrapper>();
                
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
                Set<Field> retVal = new LinkedHashSet<Field>();
                
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
        
        Set<MethodWrapper> retVal = new LinkedHashSet<MethodWrapper>();
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
        
        Set<Field> retVal = new LinkedHashSet<Field>();
        for (Field field : declaredFields) {
            retVal.add(field);
        }
        
        return retVal;
    }
    
    static Set<Field> getAllFieldWrappers(Class<?> clazz) {
        if (clazz == null) return Collections.emptySet();
        if (Object.class.equals(clazz)) return OBJECT_FIELDS;
        if (clazz.isInterface()) return Collections.emptySet();
        
        Set<Field> retVal = new LinkedHashSet<Field>();
        
        retVal.addAll(getDeclaredFieldWrappers(clazz));
        retVal.addAll(getAllFieldWrappers(clazz.getSuperclass()));
        
        return retVal;
    }
    
    static Set<MethodWrapper> getAllMethodWrappers(Class<?> clazz) {
        if (clazz == null) return Collections.emptySet();
        if (Object.class.equals(clazz)) return OBJECT_METHODS;
        
        Set<MethodWrapper> retVal = new LinkedHashSet<MethodWrapper>();
        
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
    
    static boolean isPostConstruct(Method m) {
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
    
    static boolean isPreDestroy(Method m) {
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

}
