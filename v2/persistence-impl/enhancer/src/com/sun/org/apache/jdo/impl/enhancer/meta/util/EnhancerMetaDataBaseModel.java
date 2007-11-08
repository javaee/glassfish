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


package com.sun.org.apache.jdo.impl.enhancer.meta.util;

import java.io.PrintWriter;

import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataFatalError;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataUserException;
import com.sun.org.apache.jdo.impl.enhancer.util.Support;


/**
 * Provides the JDO meta information based on a JDO meta model.
 */
public abstract class EnhancerMetaDataBaseModel
    extends Support
    implements EnhancerMetaData
{
    // misc
    protected boolean verbose = true;
    protected final PrintWriter out;

    // default settings
    static protected final HashSet unenhancableTypePrefixes = new HashSet();
    static
    {
        unenhancableTypePrefixes.add("java/");
        unenhancableTypePrefixes.add("javax/");
    }

    /**
     * Creates an instance.
     */
    public EnhancerMetaDataBaseModel(PrintWriter out,
                                     boolean verbose)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        affirm(out != null);
        this.out = out;
    }

    /**
     * Prints out a warning message.
     * @param msg the message
     */
    public void printWarning(String msg)
    {
        out.println(getI18N("enhancer.metadata.warning", msg));
    }

    /**
     * Prints out a verbose message.
     * @param msg the message
     */
    public void printMessage(String msg)
    {
        if (verbose) {
            out.println(getI18N("enhancer.metadata.message", msg));
        }
    }

    /**
     * Tests if a class is not to be modified by the enhancer.
     * @see EnhancerMetaData#isKnownUnenhancableClass(String)
     */
    public boolean isKnownUnenhancableClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        //check the transient prefixes
        for (Iterator i = unenhancableTypePrefixes.iterator(); i.hasNext();) {
            final String typePrefix = (String)i.next();
            if (classPath.startsWith(typePrefix))
                return true;
        }
        return false;
    }

    /**
     * Tests if a class is the least-derived, persistence-capable class.
     * @see EnhancerMetaData#isPersistenceCapableRootClass(String)
     */
    public boolean isPersistenceCapableRootClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        return (isPersistenceCapableClass(classPath)
                && (getPersistenceCapableSuperClass(classPath) == null));
    }

    /**
     * Returns the name of the least-derived, persistence-capable class of
     * a class.
     * @see EnhancerMetaData#getPersistenceCapableRootClass(String)
     */
    public String getPersistenceCapableRootClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        if (!isPersistenceCapableClass(classPath)) {
            return null;
        }

        String pcRootClass;
        String clazz = classPath;
        do {
            pcRootClass = clazz;
            clazz = getPersistenceCapableSuperClass(clazz);
        } while (clazz != null);
        return pcRootClass;
    }

    /**
     * Returns the name of the key class of the next persistence-capable
     * superclass that defines one.
     * @see EnhancerMetaData#getSuperKeyClass(String)
     */
    public String getSuperKeyClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        for (String superClass = getPersistenceCapableSuperClass(classPath);
             superClass != null;
             superClass = getPersistenceCapableSuperClass(superClass)) {
            final String superKeyClass = getKeyClass(superClass);
            if (superKeyClass != null) {
                return superKeyClass;
            }
        }
        return null;
    }

    /**
     * Tests if a field is managed by a JDO implementation.
     * @see EnhancerMetaData#isManagedField(String,String)
     */
    public boolean isManagedField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        return (isPersistentField(classPath, fieldName)
                || isTransactionalField(classPath, fieldName));
    }

    /**
     * Returns the access annotation flags for a declared, managed field.
     * @see EnhancerMetaData#getFieldFlags(String,String)
     */
    public int getFieldFlags(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        if (!isManagedField(classPath, fieldName)) {
            affirm(!isTransactionalField(classPath, fieldName));
            affirm(!isPersistentField(classPath, fieldName));
            affirm(!isProperty(classPath, fieldName));
            affirm(!isKeyField(classPath, fieldName));
            affirm(!isDefaultFetchGroupField(classPath, fieldName));
            return 0;
        }
        //affirm(isManagedField(classPath, fieldName));

        if (isTransactionalField(classPath, fieldName)) {
            affirm(!isPersistentField(classPath, fieldName));
            affirm(!isKeyField(classPath, fieldName));
            // ignore any dfg membership of transactional fields
            //affirm(!isDefaultFetchGroupField(classPath, fieldName));
            return CHECK_WRITE;
        }
        //affirm(!isTransactionalField(classPath, fieldName));

        if (isKeyField(classPath, fieldName)) {
            // ignore any dfg membership of key fields
            //affirm(!isDefaultFetchGroupField(classPath, fieldName));
            return MEDIATE_WRITE;
        }
        //affirm(!isKeyField(classPath, fieldName));
            
        if (isDefaultFetchGroupField(classPath, fieldName)) {
            return CHECK_READ | CHECK_WRITE;
        }
        //affirm(!isDefaultFetchGroupField(classPath, fieldName));

        return MEDIATE_READ | MEDIATE_WRITE;
    }

    /**
     * Returns an array of field names of all key fields of a class.
     * @see EnhancerMetaData#getKeyFields(String)
     */
    public String[] getKeyFields(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final List keys = new ArrayList();
        final String[] fieldNames = getManagedFields(classPath);
        final int n = fieldNames.length;
        for (int i = 0; i < n; i++) {
            if (isKeyField(classPath, fieldNames[i])) {
                keys.add(fieldNames[i]);
            }
        }
        return (String[])keys.toArray(new String[keys.size()]);
    }

    /**
     * Returns the access flags for some declared, managed fields.
     * @see EnhancerMetaData#getFieldFlags(String,String[])
     */
    public int[] getFieldFlags(String classPath, String[] fieldNames)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final int n = (fieldNames != null ? fieldNames.length : 0);
        final int[] flags = new int[n];
        for (int i = 0; i < n; i++) {
            flags[i] = getFieldFlags(classPath, fieldNames[i]);
        }
        return flags;
    }

    /**
     * Returns the indexes for some declared, managed fields.
     * @see EnhancerMetaData#getFieldNumber(String,String[])
     */
    public int[] getFieldNumber(String classPath, String[] fieldNames)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final int n = (fieldNames != null ? fieldNames.length : 0);
        final int[] flags = new int[n];
        for (int i = 0; i < n; i++) {
            flags[i] = getFieldNumber(classPath, fieldNames[i]);
        }
        return flags;
    }
}
