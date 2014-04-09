/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.locator.classanalysis;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ClassAnalyzer;
import org.glassfish.hk2.api.MultiException;

/**
 * This method picks the constructor that takes a double,
 * the fields that are doubles and the postConstruct
 * the methods that are doubles and the postConstruct
 * method must also be named "doublePostConstruct" and
 * "doublePreDestroy"
 * 
 * @author jwells
 *
 */
@Singleton @Named(DoubleClassAnalyzer.DOUBLE_ANALYZER)
public class DoubleClassAnalyzer implements ClassAnalyzer {
    public final static String DOUBLE_ANALYZER = "DoubleStrategy";
    
    private final static String POST_NAME = "doublePostConstruct";
    private final static String PRE_NAME = "doublePreDestroy";

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getConstructor(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Constructor<T> getConstructor(Class<T> clazz)
            throws MultiException {
        
        Constructor<?> allConstructors[] = clazz.getConstructors();
        for (Constructor<?> aConstructor : allConstructors) {
            Class<?> params[] = aConstructor.getParameterTypes();
            if (params.length != 1) continue;
            
            if (!params[0].equals(Double.class)) continue;
            
            return (Constructor<T>) aConstructor;
        }
        
        throw new MultiException(new AssertionError("Could not find a constructor that takes a Double parameter"));
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getInitializerMethods(java.lang.Class)
     */
    @Override
    public <T> Set<Method> getInitializerMethods(Class<T> clazz)
            throws MultiException {
        HashSet<Method> retVal = new HashSet<Method>();
        
        Method allMethods[] = clazz.getMethods();
        for (Method aMethod : allMethods) {
            Class<?> params[] = aMethod.getParameterTypes();
            if (params.length < 1) continue;
            
            if (!params[0].equals(Double.class)) continue;
            
            retVal.add(aMethod);
        }
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getFields(java.lang.Class)
     */
    @Override
    public <T> Set<Field> getFields(Class<T> clazz) throws MultiException {
        HashSet<Field> retVal = new HashSet<Field>();
        
        Field allFields[] = clazz.getFields();
        for (Field aField : allFields) {
            Class<?> fieldType = aField.getType();
            
            if (!fieldType.equals(Double.class)) continue;
            
            retVal.add(aField);
        }
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getPostConstructMethod(java.lang.Class)
     */
    @Override
    public <T> Method getPostConstructMethod(Class<T> clazz)
            throws MultiException {
        try {
            return clazz.getMethod(POST_NAME);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getPreDestroyMethod(java.lang.Class)
     */
    @Override
    public <T> Method getPreDestroyMethod(Class<T> clazz) throws MultiException {
        try {
            return clazz.getMethod(PRE_NAME);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }

}
