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
    
    private static String getNamedName(Named named, Class<?> implClass) {
        String name = named.value();
        if (name != null && !name.equals("")) return name;
        
        String cn = implClass.getName();
            
        int index = cn.lastIndexOf(".");
        if (index < 0) return cn;
        
        return cn.substring(index + 1);
    }
    
    /**
     * Returns the name that should be associated with this class
     * 
     * @param implClass The class to evaluate
     * @return The name this class should have
     */
    public static String getName(Class<?> implClass) {
        Named named = implClass.getAnnotation(Named.class);
        
        String namedName = (named != null) ? getNamedName(named, implClass) : null ;
        
        if (namedName != null) return namedName;
        
        return null;
    }
    
    /**
     * Returns the set of types this class advertises
     * @param t the object we are analyzing
     * @return The type itself and the contracts it implements
     */
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
    
    /**
     * Gets the scope annotation from the object
     * @param t The object to analyze
     * @return The class of the scope annotation
     */
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
    
    /**
     * Gets all the qualifiers from the object
     * 
     * @param t The object to analyze
     * @return The set of qualifiers.  Will not return null but may return an empty set
     */
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
