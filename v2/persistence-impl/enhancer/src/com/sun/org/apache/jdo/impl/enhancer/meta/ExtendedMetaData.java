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

package com.sun.org.apache.jdo.impl.enhancer.meta;


/**
 * Provides extended JDO meta information for byte-code enhancement.
 */
public interface ExtendedMetaData
       extends EnhancerMetaData
{
    /**
     * Returns all known class names.
     * @return all known class names
     */
    String[] getKnownClasses()
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;    

    /**
     * Returns the access modifiers of a class.
     * The return value is a constant of the
     * <code>java.lang.reflect.Modifier</code> class.
     * @param className the class name
     * @return the access modifiers
     * @see java.lang.reflect.Modifier
     */
    int getClassModifiers(String className)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the name of the superclass of a class.
     * <P>
     * @param classPath the JVM-qualified name of the class 
     * @return the name of the superclass or null if there is none
     */
    String getSuperClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns all known field names of a class.
     * @param className the class name
     * @return all known field names
     */
    String[] getKnownFields(String className)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the type of a field.
     * @param className the class name
     * @param fieldName the field name
     * @return the type of the field
     */
    String getFieldType(String className, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the access modifiers of a field.
     * The return value is a constant of the
     * <code>java.lang.reflect.Modifier</code> class.
     * @param className the class name
     * @param fieldName the field name
     * @return the access modifiers
     * @see java.lang.reflect.Modifier
     */
    int getFieldModifiers(String className, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    // convenience methods

    /**
     * Returns the type of some fields.
     * @param className the class name
     * @param fieldNames the field names
     * @return the type of the fields
     */
    String[] getFieldType(String className, String[] fieldNames)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the access modifiers of some fields.
     * @param className the class name
     * @param fieldNames the field names
     * @return the access modifiers
     * @see java.lang.reflect.Modifier
     */
    int[] getFieldModifiers(String className, String[] fieldNames)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;
}
