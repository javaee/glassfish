/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
public class ReflectionHelper {
    /**
     * Given the type parameter gets the raw type represented
     * by the type, or null if this has no associated raw class
     * @param type The type to find the raw class on
     * @return The raw class associated with this type
     */
    public static Class<?> getRawClass(Type type) {
        if (type == null) return null;
        
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return (Class<?>) rawType;
            }
        }
        
        return null;
    }
    
    private static String getFactoryName(Type type) {
        if (type instanceof Class) return Object.class.getName();
        if (!(type instanceof ParameterizedType)) {
            throw new AssertionError("Unknown Factory type " + type);
        }
        
        ParameterizedType pt = (ParameterizedType) type;
        Class<?> clazz = ReflectionHelper.getRawClass(pt.getActualTypeArguments()[0]);
        if (clazz == null) {
            throw new IllegalArgumentException("A factory implmentation may not have a wildcard type or type variable as its actual type");
        }
        
        return clazz.getName();
    }
    
    private static String getFactoryName(Class<?> implClass) {
        Type types[] = implClass.getGenericInterfaces();
        
        for (Type type : types) {    
            Class<?> clazz = ReflectionHelper.getRawClass(type);
            if (clazz == null || !Factory.class.equals(clazz)) continue;
            
            // Found the factory!
            return getFactoryName(type);
        }
        
        throw new AssertionError("getFactoryName was given a non-factory " + implClass);
    }
    
    private static String getNamedName(Named named, Class<?> implClass) {
        String name = named.value();
        if (name != null && !name.equals("")) return name;
        
        String cn = implClass.getName();
            
        int index = cn.lastIndexOf(".");
        if (index < 0) return cn;
        
        return cn.substring(index + 1);
    }
    
    public static String getName(Class<?> implClass) {
        boolean isFactory = (Factory.class.isAssignableFrom(implClass));
        Named named = implClass.getAnnotation(Named.class);
        
        String factoryName = (isFactory) ? getFactoryName(implClass) : null ;
        String namedName = (named != null) ? getNamedName(named, implClass) : null ;
        
        if ((factoryName != null) && (namedName != null)) {
            if (!namedName.equals(factoryName)) {
                throw new IllegalArgumentException("The name of a factory class must be the fully qualified class name " + 
                    " of the raw type of the factory actual type argument (" + factoryName + ")." +
                    "  However, this factory had an @Named annotation with value " + namedName);
            }
        }
        
        if (factoryName != null) return factoryName;
        if (namedName != null) return namedName;
        
        return null;
    }
    
    public static Set<Type> getAdvertisedTypesFromObject(Object t) {
        Set<Type> retVal = new HashSet<Type>();
        if (t == null) return retVal;
        
        retVal.add(t.getClass());
        
        Type genericInterfaces[] = t.getClass().getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            Class<?> rawClass = getRawClass(genericInterface);
            if (rawClass == null) continue;
            
            if (rawClass.isAnnotationPresent(Contract.class)) {
                retVal.add(genericInterface);
            }
        }
        
        return retVal;
    }
    
    public static Class<? extends Annotation> getScopeFromObject(Object t) {
        if (t == null) return PerLookup.class;
        
        Class<?> oClass = t.getClass();
        for (Annotation annotation : oClass.getAnnotations()) {
            Class<? extends Annotation> annoClass = annotation.annotationType();
            
            if (annoClass.isAnnotationPresent(Scope.class)) {
                return annoClass;
            }
            
        }
        
        return PerLookup.class;
    }
    
    public static Set<Annotation> getQualifiersFromObject(Object t) {
        Set<Annotation> retVal = new HashSet<Annotation>();
        if (t == null) return retVal;
        
        Class<?> oClass = t.getClass();
        for (Annotation annotation : oClass.getAnnotations()) {
            Class<? extends Annotation> annoClass = annotation.annotationType();
            
            if (annoClass.isAnnotationPresent(Qualifier.class)) {
                retVal.add(annotation);
            }
            
        }
        
        return retVal;
    }

}
