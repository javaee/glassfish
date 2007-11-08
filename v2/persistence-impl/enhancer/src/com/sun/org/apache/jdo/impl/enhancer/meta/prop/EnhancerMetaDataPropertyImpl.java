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


package com.sun.org.apache.jdo.impl.enhancer.meta.prop;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.FileInputStream;

import java.util.Iterator;
import java.util.Properties;

import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataFatalError;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataUserException;
import com.sun.org.apache.jdo.impl.enhancer.meta.ExtendedMetaData;
import com.sun.org.apache.jdo.impl.enhancer.meta.util.EnhancerMetaDataBaseModel;

/**
 * Provides the JDO meta information based on properties.
 */
public class EnhancerMetaDataPropertyImpl
    extends EnhancerMetaDataBaseModel
    implements ExtendedMetaData
{
    /**
     * The model instance.
     */
    final private MetaDataProperties model;
    
    /**
     * Creates an instance.
     */
    public EnhancerMetaDataPropertyImpl(PrintWriter out,
                                        boolean verbose,
                                        Properties properties)
       throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        super(out, verbose);
        affirm(properties != null);
        model = new MetaDataProperties(properties);
        initModel();
        affirm(model != null);
        printMessage(getI18N("enhancer.metadata.using_properties",
                             "<unnamed>"));
    }

    /**
     *  Creates an instance.
     */
    public EnhancerMetaDataPropertyImpl(PrintWriter out,
                                        boolean verbose,
                                        String fileName)
       throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        super(out, verbose);
        affirm(fileName != null);

        InputStream stream = null;
        try {
            stream = new FileInputStream(fileName);
            final Properties properties = new Properties();
            properties.load(stream);
            model = new MetaDataProperties(properties);
            initModel();
        } catch (IOException ex) {
            final String msg
                = getI18N("enhancer.metadata.io_error", ex.getMessage());
            throw new EnhancerMetaDataFatalError(msg, ex);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    final String msg
                        = getI18N("enhancer.metadata.io_error",
                                  ex.getMessage());
                    throw new EnhancerMetaDataFatalError(msg, ex);
                }
            }
        }
        affirm(model != null);
        printMessage(getI18N("enhancer.metadata.using_properties", fileName));
    }

    // ----------------------------------------------------------------------
    
    /**
     * Initializes the model.
     */
    private void initModel()
    {
        // we'd like to have all classes (and fields) parsed and
        // cached in order to early report errors with the properties
        final String[] classNames = model.getKnownClassNames();
        affirm(classNames != null);
        for (int i = classNames.length - 1; i >= 0; i--) {
            final JDOClass clazz = getJDOClass(classNames[i]);
            affirm(clazz != null);
        }
    }

    /** 
     * Returns the JVM-qualified name of a field's declaring class.
     * @see EnhancerMetaData#getDeclaringClass(String,String)
     */
    public String getDeclaringClass(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        String declaringClass = null;
        JDOField field = getJDOField(classPath, fieldName);
        if (field != null) {
            // this class declares the filed => return classPath
            declaringClass = classPath;
        } else {
            String superclass = getSuperClass(classPath);
            if (superclass != null) {
                declaringClass = getDeclaringClass(superclass, fieldName);
            }
        }
        return declaringClass;
    }

    /**
     * Declares a field with the JDO model.
     * @see EnhancerMetaData#declareField(String,String,String)
     */
    public void declareField(String classPath,
                             String fieldName,
                             String signature)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        affirm(isPersistenceCapableClass(classPath));
        // nothing to be done: the properties-based model doesn't
        // support default calculation of persistence modifiers
    }
    
    /**
     * Tests if a class is persistence-capable.
     * @see EnhancerMetaData#isPersistenceCapableClass(String)
     */
    public boolean isPersistenceCapableClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOClass clazz = getJDOClass(classPath);
        return (clazz != null && clazz.isPersistent());
    }

    /**
     * Tests if a class implements java.io.Serializable.
     * @see EnhancerMetaData#isSerializableClass(String)
     */
    public boolean isSerializableClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOClass clazz = getJDOClass(classPath);
        return (clazz != null && clazz.isSerializable());
    }
    
    /**
     * Returns the name of the persistence-capable superclass of a class.
     * @see EnhancerMetaData#getPersistenceCapableSuperClass(String)
     */
    public String getPersistenceCapableSuperClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        for (String clazz = getSuperClass(classPath);
             clazz != null;
             clazz = getSuperClass(clazz))  {
            if (isPersistenceCapableClass(clazz)) {
                return clazz;
            }
        }
        return null;
    }

    /**
     * Returns the name of the superclass of a class.
     * @see ExtendedMetaData#getSuperClass(String)
     */
    public final String getSuperClass(String className)
    {
        final JDOClass clazz = getJDOClass(className);
        return (clazz != null ? clazz.getSuperClassName() : null);
    }

    /**
     * Returns the name of the key class of a class.
     * @see EnhancerMetaData#getKeyClass(String)
     */
    public String getKeyClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOClass clazz = getJDOClass(classPath);
        return (clazz != null ? clazz.getOidClassName() : null);
    }

    /**
     * Tests if a field is known to be non-managed.
     * @see EnhancerMetaData#isKnownNonManagedField(String,String,String)
     */
    public boolean isKnownNonManagedField(String classPath,
                                          String fieldName,
                                          String fieldSig)
    {
        final JDOClass clazz = getJDOClass(classPath);
        if (clazz == null) {
            return true;
        }
        final JDOField field = getJDOField(clazz, fieldName);
        return (field != null && field.isKnownTransient());
    }    

    /**
     * Tests if a field is managed by a JDO implementation.
     * @see EnhancerMetaData#isManagedField(String,String)
     */
    public boolean isManagedField(String classPath, String fieldName)
    {
        final JDOField field = getJDOField(classPath, fieldName);
        return (field != null
                && (field.isPersistent() || field.isTransactional()));
    }

    /**
     * Tests if a field is persistent.
     * @see EnhancerMetaData#isPersistentField(String,String)
     */
    public boolean isPersistentField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOField field = getJDOField(classPath, fieldName);
        return (field != null && field.isPersistent());
    }

    /**
     * Tests if a field is transient-transactional.
     * @see EnhancerMetaData#isTransactionalField(String,String)
     */
    public boolean isTransactionalField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOField field = getJDOField(classPath, fieldName);
        return (field != null && field.isTransactional());
    }

    /**
     * Tests if a field is a property.
     * @see EnhancerMetaData#isProperty(String,String)
     */
    public boolean isProperty(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOField field = getJDOField(classPath, fieldName);
        return (field != null && field.isProperty());
    }

    /**
     * Tests if a field has key field access annotation.
     * @see EnhancerMetaData#isKeyField(String,String)
     */
    public boolean isKeyField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOField field = getJDOField(classPath, fieldName);
        return (field != null && field.isKeyField());
    }

    /**
     * Tests if a field has default-fetch-group access annotation.
     * @see EnhancerMetaData#isDefaultFetchGroupField(String,String)
     */
    public boolean isDefaultFetchGroupField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOField field = getJDOField(classPath, fieldName);
        return (field != null && field.isDfgField());
    }

    /**
     * Returns the index of a declared, managed field.
     * @see EnhancerMetaData#getFieldNumber(String,String)
     */
    public int getFieldNumber(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOClass clazz = getJDOClass(classPath);
        return (clazz != null ? clazz.getIndexOfField(fieldName) : -1);
    }

    /**
     * Returns the names of all declared, jdo-managed fields of a class.
     * @see EnhancerMetaData#getManagedFields(String)
     */
    public String[] getManagedFields(String className)
    {
        final JDOClass clazz = getJDOClass(className);
        return (clazz != null ? clazz.getManagedFieldNames() : new String[]{});
    }

    /**
     * Returns all known classnames.
     * @see ExtendedMetaData#getKnownClasses()
     */
    public final String[] getKnownClasses()
    {
        return model.getKnownClassNames();
    }

    /**
     * Returns all known field names of a class.
     * @see ExtendedMetaData#getKnownFields(String)
     */
    public final String[] getKnownFields(String className)
    {
        final JDOClass clazz = getJDOClass(className);
        return (clazz != null ? clazz.getFieldNames() : new String[]{});
    }

    /**
     * Returns the access modifiers of a class.
     * @see ExtendedMetaData#getClassModifiers(String)
     */
    public final int getClassModifiers(String className)
    {
        final JDOClass clazz = getJDOClass(className);
        return (clazz != null ? clazz.getAccessModifiers() : 0);
    }

    /**
     * Returns the access modifiers of a field.
     * @see ExtendedMetaData#getFieldModifiers(String,String)
     */
    public final int getFieldModifiers(String className, String fieldName)
    {
        final JDOField field = getJDOField(className, fieldName);
        return (field != null ? field.getAccessModifiers() : 0);
    }

    /**
     * Returns the type of a field.
     * @see ExtendedMetaData#getFieldType(String,String)
     */
    public final String getFieldType(String className, String fieldName)
    {
        final JDOField field = getJDOField(className, fieldName);
        return (field != null ? field.getType() : null);
    }

    /**
     * Returns the type of some fields.
     * @see ExtendedMetaData#getFieldType(String,String)
     */
    public final String[] getFieldType(String className, String[] fieldNames)
    {
        final int n = (fieldNames != null ? fieldNames.length : 0);
        final String[] types = new String[n];
        for (int i = 0; i < n; i++) {
            types[i] = getFieldType(className, fieldNames[i]);
        }
        return types;
    }

    /**
     * Returns the access modifiers of some fields.
     * @see ExtendedMetaData#getFieldModifiers(String,String[])
     */
    public final int[] getFieldModifiers(String className, String[] fieldNames)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final int n = (fieldNames != null ? fieldNames.length : 0);
        final int[] mods = new int[n];
        for (int i = 0; i < n; i++) {
            mods[i] = getFieldModifiers(className, fieldNames[i]);
        }
        return mods;
    }

    // ----------------------------------------------------------------------
    
    private final JDOClass getJDOClass(String className)
        throws EnhancerMetaDataUserException
    {
        return model.getJDOClass(className);
    }

    private final JDOField getJDOField(JDOClass clazz, String fieldName)
    {
        return (clazz != null ? clazz.getField(fieldName) : null);
    }

    private final JDOField getJDOField(String className, String fieldName)
    {
        final JDOClass clazz = getJDOClass(className);
        return getJDOField(clazz, fieldName);
    }

    // ----------------------------------------------------------------------
    
    public static void main(String[] argv)
    {
        final PrintWriter out = new PrintWriter(System.out, true);
        
        if (argv.length != 1) {
            System.err.println("No property file specified.");
            return;
        }

        final Properties p = new Properties();
        try {
            java.io.InputStream in =
                new java.io.FileInputStream(new java.io.File(argv[0]));
            p.load(in);
            in.close();
            out.println("PROPERTIES: " + p);
            out.println("############");
            MetaDataProperties props = new MetaDataProperties(p);
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
        }

        final EnhancerMetaDataPropertyImpl jdo
            = new EnhancerMetaDataPropertyImpl(out, true, p);
        final String[] classes = jdo.getKnownClasses();
        for (int k = 0; k < classes.length; k++) {
            final String clazz = classes[k];
            out.println("CLAZZ: " + clazz);
            out.println("\tpersistent: "
                        + jdo.isPersistenceCapableClass(clazz));
            out.println("\tpersistent root: "
                        + jdo.isPersistenceCapableRootClass(clazz));
            out.println("\tpersistent root class: "
                        + jdo.getPersistenceCapableRootClass(clazz));
            out.println("\tpersistent super class: "
                        + jdo.getPersistenceCapableSuperClass(clazz));
            out.println("\tkey class: "
                        + jdo.getKeyClass(clazz));

            final String[] fields = jdo.getKnownFields(clazz);
            for (int j = 0; j < fields.length; j++) {
                final String field = fields[j];
                out.println("FIELD: " + field);
                out.println("\tpersistent field: "
                            + jdo.isPersistentField(clazz, field));
                out.println("\tpk field: "
                            + jdo.isKeyField(clazz, field));
                out.println("\tdfg field: "
                            + jdo.isDefaultFetchGroupField(clazz, field));
                out.println("\tnumber: "
                            + jdo.getFieldNumber(clazz, field));

                final String[] names = jdo.getManagedFields(clazz);
                final int n = (fields != null ? names.length : 0);
                out.println("managed fields: number: " + n);
                for (int i = 0; i < n; i++) {
                    final String name = names[i];
                    out.println(i + ": " + name +
                                " number: "
                                + jdo.getFieldNumber(clazz, name) +
                                " pk: "
                                + jdo.isKeyField(clazz, name) +
                                " dfg: "
                                + jdo.isDefaultFetchGroupField(clazz, name));
                }
            }
        }
    }
}
