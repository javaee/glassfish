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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * An interface representing useful reflection utilities
 * 
 * @author jwells
 */
public interface ClassReflectionHelper {
    
    /**
     * Gets all methods for a class (taking class heirarchy into account)
     * 
     * @param clazz The class to analyze for all methods
     * @return The set of all methods on this class (and all subclasses)
     */
    public Set<MethodWrapper> getAllMethods(Class<?> clazz);
    
    /**
     * Creates a method wrapper from the given method
     * 
     * @param m A non-null method to create a wrapper from
     * @return A method wrapper
     */
    public MethodWrapper createMethodWrapper(Method m);
    
    /**
     * Gets all fields for a class (taking class heirarchy into account)
     * 
     * @param clazz The class to analyze for all fields
     * @return The set of all fields on this class (and all subclasses)
     */
    public Set<Field> getAllFields(Class<?> clazz);
    
    /**
     * Finds the postConstruct method on this class
     * 
     * @param clazz The class to check for the postConstruct method
     * @param matchingClass The PostConstruct interface, a small performance improvement
     * @return A matching method, or null if none can be found
     * @throws IllegalArgumentException If a method marked as postConstruct is invalid
     */
    public Method findPostConstruct(Class<?> clazz, Class<?> matchingClass) throws IllegalArgumentException;
    
    /**
     * Finds the preDestroy method on this class
     * 
     * @param clazz The class to check for the postConstruct method
     * @param matchingClass The PostConstruct interface, a small performance improvement
     * @return A matching method, or null if none can be found
     * @throws IllegalArgumentException If a method marked as postConstruct is invalid
     */
    public Method findPreDestroy(Class<?> clazz, Class<?> matchingClass) throws IllegalArgumentException;
    
    /**
     * Removes this class (and all appropriate sub-classes) from the cache
     * 
     * @param clazz The class to remove.  If null this method does nothing
     */
    public void clean(Class<?> clazz);
    
    /**
     * Releases the entire cache, though the ClassReflectionHelper is
     * still usable after calling dispose
     */
    public void dispose();
    
    /**
     * Returns an approximation of the current size of the cache
     * @return An approximation of the current size of the cache
     */
    public int size();

}
