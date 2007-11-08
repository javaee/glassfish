/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.model.jdo;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;


/**
 * A JDOModel instance bundles a number of JDOClass instances used by an 
 * application. It provides factory methods to create and retrieve JDOClass 
 * instances. A fully qualified class name must be unique within a JDOModel 
 * instance. The model supports multiple classes having the same fully qualified 
 * name by different JDOModel instances.
 *
 * @author Michael Bouschen
 * @version 2.0
 */
public interface JDOModel
    extends JDOElement
{
    /** 
     * The method returns a JDOClass instance for the specified package name.
     * If this JDOModel contains the corresponding JDOPackage instance,
     * the existing instance is returned. Otherwise, it creates a new JDOPackage
     * instance and returns the new instance.
     * @param packageName the name of the JDOPackage instance 
     * to be returned
     * @return a JDOPackage instance for the specified package name
     * @exception ModelException if impossible
     */
    public JDOPackage createJDOPackage(String packageName)
        throws ModelException;

    /** 
     * The method returns the JDOPackage instance for the specified package 
     * name, if present. The method returns <code>null</code> if it cannot 
     * find a JDOPackage instance for the specified name. 
     * @param packageName the name of the JDOPackage instance 
     * to be returned
     * @return a JDOPackage instance for the specified package name 
     * or <code>null</code> if not present
     */
    public JDOPackage getJDOPackage(String packageName);

    /**
     * Returns the collection of JDOPackage instances declared by this JDOModel 
     * in the format of an array.
     * @return the packages declared by this JDOModel
     */
    public JDOPackage[] getDeclaredPackages();

    /**
     * The method returns a JDOClass instance for the specified fully qualified
     * class name. If this JDOModel contains the corresponding JDOClass instance,
     * the existing instance is returned. Otherwise, it creates a new JDOClass 
     * instance, sets its declaringModel and returns the new instance.
     * <p>
     * Whether this method reads XML metatdata or not is determined at
     * JDOModel creation time (see flag <code>loadXMLMetadataDefault</code> 
     * in {@link JDOModelFactory#getJDOModel(JavaModel javaModel, boolean
     * loadXMLMetadataDefault)}). Invoking this method is method is equivalent
     * to <code>createJDOClass(className, loadXMLMetadataDefault)</code>.
     * @param className the fully qualified class name of the JDOClass
     * instance to be returned
     * @return a JDOClass instance for the specified class name
     * @exception ModelException if impossible
     */
    public JDOClass createJDOClass(String className)
        throws ModelException;

    /**
     * The method returns a JDOClass instance for the specified fully qualified
     * class name. If this JDOModel contains the corresponding JDOClass instance,
     * the existing instance is returned. Otherwise, if the flag loadXMLMetadata
     * is set to <code>true</code> the method tries to find the JDOClass 
     * instance by reading the XML metadata. If it could not be found the method
     * creates a new JDOClass instance, sets its declaringModel and returns the 
     * instance.
     * @param className the fully qualified class name of the JDOClass instance 
     * to be returned
     * @param loadXMLMetadata indicates whether to read XML metatdata or not
     * @return a JDOClass instance for the specified class name
     * @exception ModelException if impossible
     */
    public JDOClass createJDOClass(String className, boolean loadXMLMetadata)
        throws ModelException;

    /**
     * The method returns the JDOClass instance for the specified fully 
     * qualified class name if present. The method returns <code>null</code> 
     * if it cannot find a JDOClass instance for the specified name. 
     * <p>
     * Whether this method reads XML metatdata or not is determined at
     * JDOModel creation time (see flag <code>loadXMLMetadataDefault</code> 
     * in {@link JDOModelFactory#getJDOModel(JavaModel javaModel, boolean
     * loadXMLMetadataDefault)}). Invoking this method is method is equivalent
     * to <code>createJDOClass(className, loadXMLMetadataDefault)</code>.
     * @param className the fully qualified class name of the JDOClass
     * instance to be returned
     * @return a JDOClass instance for the specified class name 
     * or <code>null</code> if not present
     */
    public JDOClass getJDOClass(String className);

    /**
     * The method returns the JDOClass instance for the specified fully 
     * qualified class name if present. If the flag loadXMLMetadata is set 
     * to <code>true</code> the method tries to find the JDOClass instance by 
     * reading the XML metadata. The method returns null if it cannot find a 
     * JDOClass instance for the specified name.
     * @param className the fully qualified class name of the JDOClass instance 
     * to be returned
     * @param loadXMLMetadata indicates whether to read XML metatdata or not
     * @return a JDOClass instance for the specified class name
     * or <code>null</code> if not present
     */
    public JDOClass getJDOClass(String className, boolean loadXMLMetadata);

    /**
     * The method returns the JDOClass instance for the specified short name
     * (see {@link JDOClass#getShortName()}) or <code>null</code> if it cannot
     * find a JDOClass instance with the specified short name. 
     * <p>
     * The method searches the list of JDOClasses currently managed by this
     * JDOModel instance. It does not attempt to load any metadata if it
     * cannot find a JDOClass instance with the specified short name. The
     * metadata for a JDOClass returned by this method must have been loaded
     * before by any of the methods
     * {@link #createJDOClass(String className)},
     * {@link #createJDOClass(String className, boolean loadXMLMetadataDefault)},
     * {@link #getJDOClass(String className)}, or
     * {@link #getJDOClass(String className, boolean loadXMLMetadataDefault)}.
     * @param shortName the short name of the JDOClass instance to be returned
     * @return a JDOClass instance for the specified short name 
     * or <code>null</code> if not present
     */
    public JDOClass getJDOClassForShortName(String shortName);

    /**
     * Returns the collection of JDOClass instances declared by this JDOModel 
     * in the format of an array.
     * @return the classes declared by this JDOModel
     */
    public JDOClass[] getDeclaredClasses();

    /**
     * Returns the JavaModel bound to this JDOModel instance.
     * @return the JavaModel
     */
    public JavaModel getJavaModel();
    
    /**
     * Sets the JavaModel for this JDOModel instance.
     * @param javaModel the JavaModel
     */
    public void setJavaModel(JavaModel javaModel);

    /**
     * Returns the parent JDOModel instance of this JDOModel.
     * @return the parent JDOModel
     */
    public JDOModel getParent();

    /**
     * This method returns the JDOClass instance that defines the specified type
     * as its objectId class. In the case of an inheritance hierarchy it returns 
     * the top most persistence-capable class of the hierarchy (see 
     * {@link JDOClass#getPersistenceCapableSuperclass}).
     * @param objectIdClass the type representation of the ObjectId class
     * @return the JDOClass defining the specified class as ObjectId class
     */
    public JDOClass getJDOClassForObjectIdClass(JavaType objectIdClass);
}
