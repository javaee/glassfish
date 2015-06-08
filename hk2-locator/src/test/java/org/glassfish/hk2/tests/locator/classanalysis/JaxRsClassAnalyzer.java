/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ClassAnalyzer;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.reflection.Logger;

/**
 * Implementation of the ClassAnalyzer that prefers the
 * largest number of parameters in the constructor over
 * the smallest.  Other than that it is exactly the
 * same as the default analyzer
 * 
 * @author jwells
 *
 */
@Singleton @Named(JaxRsClassAnalyzer.PREFER_LARGEST_CONSTRUCTOR)
public class JaxRsClassAnalyzer implements ClassAnalyzer {
    public final static String PREFER_LARGEST_CONSTRUCTOR = "PreferLargestConstructor";
    
    @Inject @Named(ClassAnalyzer.DEFAULT_IMPLEMENTATION_NAME)
    private ClassAnalyzer defaultAnalyzer;

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getConstructor(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Constructor<T> getConstructor(Class<T> clazz)
            throws MultiException, NoSuchMethodException {
        Constructor<T> retVal = null;
        try {
            retVal = defaultAnalyzer.getConstructor(clazz);
            
            Class<?> args[] = retVal.getParameterTypes();
            if (args.length != 0) return retVal;
            
            // Is zero length, but is it specifically marked?
            Inject i = retVal.getAnnotation(Inject.class);
            if (i != null) return retVal;
            
            // In this case, the default chose a zero-arg constructor since it could find no other
        }
        catch (NoSuchMethodException nsme) {
           // In this case, the default failed because it found no constructor it could use
        }
        
        // At this point, we simply need to find the constructor with the largest number of parameters
        Constructor<?> allCs[] = clazz.getDeclaredConstructors();
        List<Constructor<?>> allMaximums = new LinkedList<Constructor<?>>();
        int currentBestSize = -1;
        
        for (Constructor<?> candidate : allCs) {
            Class<?> params[] = candidate.getParameterTypes();
            if (params.length > currentBestSize) {
                currentBestSize = params.length;
                allMaximums.clear();
                
                allMaximums.add(candidate);
            }
            else if (params.length == currentBestSize) {
                allMaximums.add(candidate);
            }
        }
        
        if (allMaximums.isEmpty()) {
            // Is it possible to get here?
            throw new NoSuchMethodException("Could not find any constructors on " + clazz.getName());
        }
        
        return (Constructor<T>) allMaximums.get(0);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getInitializerMethods(java.lang.Class)
     */
    @Override
    public <T> Set<Method> getInitializerMethods(Class<T> clazz)
            throws MultiException {
        return defaultAnalyzer.getInitializerMethods(clazz);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getFields(java.lang.Class)
     */
    @Override
    public <T> Set<Field> getFields(Class<T> clazz) throws MultiException {
        return defaultAnalyzer.getFields(clazz);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getPostConstructMethod(java.lang.Class)
     */
    @Override
    public <T> Method getPostConstructMethod(Class<T> clazz)
            throws MultiException {
        return defaultAnalyzer.getPostConstructMethod(clazz);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ClassAnalyzer#getPreDestroyMethod(java.lang.Class)
     */
    @Override
    public <T> Method getPreDestroyMethod(Class<T> clazz) throws MultiException {
        return defaultAnalyzer.getPreDestroyMethod(clazz);
    }

}
