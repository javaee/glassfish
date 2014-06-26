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
package org.glassfish.hk2.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import org.jvnet.hk2.annotations.Contract;

/**
 * When HK2 automatically analyzes a class to find the constructor, fields,
 * initializer methods and postConstruct and preDestroy methods it uses this
 * service to analyze the class.  This analyzer is only used for descriptors
 * that are not pre-reified and which are not provided by factories.
 * <p>
 * HK2 will provide a default implementation of this service (with the name
 * &quot;default&quot;).  However, individual descriptors may choose a different class
 * analyzer should they so choose.  All user supplied implementations of this
 * service must have a name.  Implementations of this service must not be ClassAnalyzers
 * for themselves.
 * <p>
 * The method {@link ServiceLocator#setDefaultClassAnalyzerName(String)} can be used
 * to set the global ClassAnalyzer name that will be the name of the ClassAnalyzer used
 * when the method {@link Descriptor#getClassAnalysisName()} returns null
 * <p>
 * Implementations of ClassAnalyzer will be instantiated as soon as
 * they are added to HK2 in order to avoid deadlocks and circular references.
 * Therefore it is recommended that implementations of ClassAnalyzer
 * make liberal use of {@link javax.inject.Provider} or {@link IterableProvider}
 * when injecting dependent services so that these services are not instantiated
 * when the ClassAnalyzer is created
 * 
 * @author jwells
 *
 */
@Contract
public interface ClassAnalyzer {
    /** The name of the default ClassAnalyzer service */
    public final static String DEFAULT_IMPLEMENTATION_NAME = "default";
    
    /**
     * Will return the constructor that it to be used when constructing this
     * service
     * <p>
     * The default implementation will use the zero-arg constructor if no single
     * constructor with Inject is found
     * <p>
     * 
     * @param clazz the non-null class to analyze
     * @return The non-null constructor to use for creating this service
     * @throws MultiException on an error when analyzing the class
     * @throws NoSuchMethodException if there was no available constructor
     */
    public <T> Constructor<T> getConstructor(Class<T> clazz) throws MultiException,
        NoSuchMethodException;
    
    /**
     * Will return the set of initializer method to be used when initializing
     * this service
     * <p>
     * The default implementation will return all methods marked with Inject
     * 
     * @param clazz the non-null class to analyze
     * @return A non-null but possibly empty set of initialization methods
     * @throws MultiException on an error when analyzing the class
     */
    public <T> Set<Method> getInitializerMethods(Class<T> clazz) throws MultiException;

    /**
     * Will return the set of initializer fields to be used when initializing
     * this service
     * <p>
     * The default implementation will return all fields marked with Inject
     * 
     * @param clazz the non-null class to analyze
     * @return A non-null but possibly empty set of initialization fields
     * @throws MultiException on an error when analyzing the class
     */
    public <T> Set<Field> getFields(Class<T> clazz) throws MultiException;
    
    /**
     * Will return the postConstruct method of the class
     * <p>
     * The default implementation will return the {@link PostConstruct#postConstruct()}
     * method or the method annotated with PostConstruct
     * 
     * @param clazz the non-null class to analyze
     * @return A possibly null method representing the postConstruct method to call
     * @throws MultiException on an error when analyzing the class
     */
    public <T> Method getPostConstructMethod(Class<T> clazz) throws MultiException;
    
    /**
     * Will return the preDestroy method of the class
     * <p>
     * The default implementation will return the {@link PreDestroy#preDestroy()}
     * method or the method annotated with PreDestroy
     * 
     * @param clazz the non-null class to analyze
     * @return A possibly null method representing the preDestroy method to call
     * @throws MultiException on an error when analyzing the class
     */
    public <T> Method getPreDestroyMethod(Class<T> clazz) throws MultiException;
}
