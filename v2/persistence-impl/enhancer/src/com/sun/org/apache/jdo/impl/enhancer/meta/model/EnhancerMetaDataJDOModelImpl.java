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

package com.sun.org.apache.jdo.impl.enhancer.meta.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataFatalError;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataUserException;
import com.sun.org.apache.jdo.impl.enhancer.meta.util.EnhancerMetaDataBaseModel;
import com.sun.org.apache.jdo.impl.enhancer.util.CombinedResourceLocator;
import com.sun.org.apache.jdo.impl.enhancer.util.ListResourceLocator;
import com.sun.org.apache.jdo.impl.enhancer.util.PathResourceLocator;
import com.sun.org.apache.jdo.impl.enhancer.util.ResourceLocator;
import com.sun.org.apache.jdo.impl.model.jdo.caching.JDOModelFactoryImplCaching;
import com.sun.org.apache.jdo.impl.model.jdo.util.TypeSupport;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.model.jdo.JDOModelFactory;
import com.sun.org.apache.jdo.model.jdo.PersistenceModifier;


/**
 * Provides the JDO meta information based on a JDO meta model.
 */
public class EnhancerMetaDataJDOModelImpl
    extends EnhancerMetaDataBaseModel
    implements EnhancerMetaData
{
    /**
     * The jdoModel instance.
     */
    private final JDOModel jdoModel;

    /**
     * The model instance.
     */
    private final EnhancerJavaModel javaModel;

    /**
     * The JavaType representation for java.io.Serializable.
     */
    private final JavaType serializableJavaType;
    
    /**
     * Creates an instance.
     */
    public EnhancerMetaDataJDOModelImpl(PrintWriter out,
                                        boolean verbose,
                                        List jdoFileNames,
                                        List jarFileNames,
                                        String sourcePath)
        throws EnhancerMetaDataFatalError
    {
        super(out, verbose);

        try {
            final List locators = new ArrayList();
            ClassLoader classLoader = null;

            // create resource locator for specified jdo files
            if (jdoFileNames != null && !jdoFileNames.isEmpty()) {
                final StringBuffer s = new StringBuffer();
                for (Iterator i = jdoFileNames.iterator(); i.hasNext();) {
                    s.append(" " + i.next());
                }
                final ResourceLocator jdos
                    = new ListResourceLocator(out, verbose, jdoFileNames);
                //printMessage(getI18N("enhancer.metadata.using_jdo_files",
                //                     s.toString()));
                locators.add(jdos);
            }

            // create resource locator for specified jar files
            if (jarFileNames != null && !jarFileNames.isEmpty()) {
                final StringBuffer s = new StringBuffer();
                final Iterator i = jarFileNames.iterator();
                s.append(i.next());
                while (i.hasNext()) {
                    s.append(File.pathSeparator + i.next());
                }
                final PathResourceLocator jars
                    = new PathResourceLocator(out, verbose, s.toString());
                //printMessage(getI18N("enhancer.metadata.using_jar_files",
                //                     s.toString()));
                locators.add(jars);
                classLoader = jars.getClassLoader();
            }

            // create resource locator for specified source path
            if (sourcePath != null && sourcePath.length() > 0) {
                final PathResourceLocator path
                    = new PathResourceLocator(out, verbose, sourcePath);
                //printMessage(getI18N("enhancer.metadata.using_source_path",
                //                     sourcePath));
                locators.add(path);
                classLoader = path.getClassLoader();
            }

            if (classLoader == null) {
                // use the current class loader as the default, if there is
                // no -s option and no archives specified.
                classLoader = EnhancerMetaDataJDOModelImpl.class.getClassLoader();
            }

            // print warning if no meta-data source specified
            if (locators.isEmpty()) {
                printWarning(getI18N("enhancer.metadata.using_no_metadata"));
            }

            // create JavaModel with combined resource locators
            final ResourceLocator locator
                = new CombinedResourceLocator(out, verbose, locators);
            //^olsen: wrap with timing jdo file locator
            //if (options.doTiming.value) {
            //    classLocator = new ResourceLocatorTimer(classLocator);
            //}
            javaModel = new EnhancerJavaModel(classLoader, locator);
            final JDOModelFactory factory = JDOModelFactoryImplCaching.getInstance();
            affirm(factory != null);
            jdoModel = factory.getJDOModel(javaModel);
            affirm(jdoModel != null);
            javaModel.setJDOModel(jdoModel);
            serializableJavaType = javaModel.getJavaType("java.io.Serializable");
        } catch (IOException ex) {
            final String msg
                = getI18N("enhancer.metadata.io_error", ex.getMessage());
            throw new EnhancerMetaDataFatalError(msg, ex);
        } catch (ModelFatalException ex) {
            final String msg
                = getI18N("enhancer.metadata.jdomodel_error", ex.getMessage());
            throw new EnhancerMetaDataFatalError(msg, ex);
        } catch (ModelException ex) {
            final String msg
                = getI18N("enhancer.metadata.jdomodel_error", ex.getMessage());
            throw new EnhancerMetaDataFatalError(msg, ex);
        }        
    }

    // ----------------------------------------------------------------------
    
    private JDOClass getJDOClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final String className = classPath.replace('/', '.');
        final JDOClass clazz = jdoModel.getJDOClass(className);
        return clazz;
    }
    
    private JDOField getJDOField(String classPath,
                                 String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOClass clazz = getJDOClass(classPath);
        if (clazz == null) {
            return null;
        }
        final JDOField field = clazz.getDeclaredField(fieldName);
        affirm(field == null || field.getDeclaringClass() == clazz,
               "field not declared in class: " + classPath + "." + fieldName);
        return field;
    }
    
    private boolean hasFieldModifier(String classPath,
                                     String fieldName,
                                     int fieldModifier)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOField field = getJDOField(classPath, fieldName);
        if (field == null) {
            return false;
        }
        final int pm = field.getPersistenceModifier();
        affirm(pm != PersistenceModifier.UNSPECIFIED,
               "field modifier 'UNSPECIFIED': " + classPath + "." + fieldName);
        return (pm & fieldModifier) != 0;
    }

    // ----------------------------------------------------------------------

    /** 
     * Returns the JVM-qualified name of a field's declaring class.
     * @see EnhancerMetaData#getDeclaringClass(String,String)
     */
    public String getDeclaringClass(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        affirm(classPath);
        affirm(fieldName);
        final String className = classPath.replace('/', '.');
        try {
            final JavaType javaType = javaModel.getJavaType(className);
            final JavaField javaField = javaType.getJavaField(fieldName);
            final JavaType declaringClass = javaField.getDeclaringClass();
            return declaringClass.getName().replace('.', '/');
        } catch (ModelFatalException ex) {
            throw new EnhancerMetaDataUserException(ex);
        }
    }

    /**
     * Declares a field with the JDO model.
     * @see EnhancerMetaData#declareField(String,String,String)
     */
    public void declareField(String classPath,
                             String fieldName,
                             String fieldSig)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {        
        affirm(classPath);
        affirm(fieldName);
        try {
            final JDOClass clazz = getJDOClass(classPath);
            JavaType javaClass = clazz.getJavaType();
            affirm(javaClass != null,
                   "cannot find class file for class: " + classPath);
            JavaField javaField = javaClass.getJavaField(fieldName);
            affirm(javaField != null,
                   "cannot find java field " + classPath + "." + fieldName);
            JavaType fieldType = javaField.getType();
            JDOField field = clazz.getField(fieldName);
            // if field not known by JDOClass (not specified in JDO XML),
            // create the field only if the model's method of default
            // calculation would yield a persistent field.  We must not
            // change the models state by newly created fields with
            // a persistence-modifier "none", because this would lead to
            // in a different annotation by isKnownNonManagedField().
            if (field == null
                && TypeSupport.isPersistenceFieldType(fieldType)) {
                field = clazz.createJDOField(fieldName);
                affirm(field != null,
                       "cannot create JDO field: "
                       + classPath + "." + fieldName);
            }
            field.setJavaField(javaField);
            affirm(fieldType == field.getType());
            affirm(field.getPersistenceModifier()
                   != PersistenceModifier.UNSPECIFIED,
                   "known, unspecified JDO field: " + classPath + "." + fieldName);
        } catch (ModelFatalException ex) {
            throw new EnhancerMetaDataUserException(ex);
        } catch (ModelException ex) {
            throw new EnhancerMetaDataUserException(ex);
        }
    }
    
    /**
     * Tests if a class is persistence-capable.
     * @see EnhancerMetaData#isPersistenceCapableClass(String)
     */
    public boolean isPersistenceCapableClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOClass clazz = getJDOClass(classPath);
        return (clazz != null);
    }

    /**
     * Tests if a class implements java.io.Serializable.
     * @see EnhancerMetaData#isSerializableClass(String)
     */
    public boolean isSerializableClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final String className = classPath.replace('/', '.');
        final JavaType javaType = javaModel.getJavaType(className);
        return javaType.isCompatibleWith(serializableJavaType);
    }

    /**
     * Returns the name of the persistence-capable superclass of a class.
     * @see EnhancerMetaData#getPersistenceCapableSuperClass(String)
     */
    public String getPersistenceCapableSuperClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOClass clazz = getJDOClass(classPath);
        if (clazz == null) {
            return null;
        }
        final String name = clazz.getPersistenceCapableSuperclassName();
        return (name != null ? name.replace('.', '/') : null);
    }

    /**
     * Returns the name of the key class of a class.
     * @see EnhancerMetaData#getKeyClass(String)
     */
    public String getKeyClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOClass clazz = getJDOClass(classPath);
        if (clazz == null) {
            return null;
        }
        final String name = clazz.getDeclaredObjectIdClassName();
        return (name != null ? name.replace('.', '/') : null);
    }

    /**
     * Returns the names of all declared, jdo-managed fields of a class.
     * @see EnhancerMetaData#getManagedFields(String)
     */
    public String[] getManagedFields(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOClass clazz = getJDOClass(classPath);
        if (clazz == null) {
            return new String[]{};
        }

        final JDOField[] fields = clazz.getDeclaredManagedFields();
        if (fields == null) {
            return new String[]{};
        }
        affirm(fields.length == clazz.getDeclaredManagedFieldCount());
        
        final int n = fields.length;
        final String[] names = new String[n];
        for (int i = 0; i < n; i++) {
            affirm(fields[i] != null);
            affirm(fields[i].getRelativeFieldNumber() == i);
            affirm(fields[i].isManaged());
            names[i] = fields[i].getName();
            affirm(names[i] != null);
        }
        return names;
    }

    /**
     * Tests if a field is known to be non-managed.
     * @see EnhancerMetaData#isKnownNonManagedField(String,String,String)
     */
    public boolean isKnownNonManagedField(String classPath,
                                          String fieldName,
                                          String fieldSig)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        affirm(classPath);
        affirm(fieldName);
        affirm(fieldSig);
        try {
            final JDOClass clazz = getJDOClass(classPath);
            if (clazz == null) {
                // class not known to be persistence-capable
                return true;
            }
            
            // check whether field is managed only if field's
            // persistence-modifier is known by the JDO model
            final JDOField field = clazz.getField(fieldName);
            if (field != null && (field.getPersistenceModifier()
                                  != PersistenceModifier.UNSPECIFIED)) {
                // only field's persistence-modifier known by model
                return !field.isManaged();
            }

            // field not known by JDOClass (not specified in JDO XML)
            // apply model's method of default calculation without
            // changing the model's state
            JavaType fieldType = javaModel.getJavaType(javaModel.getTypeName(fieldSig));
            affirm(fieldType != null, 
                   "cannot get java type for: " + fieldSig);
            return !TypeSupport.isPersistenceFieldType(fieldType);
        } catch (ModelFatalException ex) {
            throw new EnhancerMetaDataUserException(ex);
        }
    }

    /**
     * Tests if a field is managed by a JDO implementation.
     * @see EnhancerMetaData#isManagedField(String,String)
     */
    public boolean isManagedField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        return hasFieldModifier(classPath, fieldName,
                                (PersistenceModifier.PERSISTENT
                                 | PersistenceModifier.POSSIBLY_PERSISTENT
                                 | PersistenceModifier.TRANSACTIONAL));
    }

    /**
     * Tests if a field is persistent.
     * @see EnhancerMetaData#isPersistentField(String,String)
     */
    public boolean isPersistentField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        return hasFieldModifier(classPath, fieldName,
                                (PersistenceModifier.PERSISTENT
                                 | PersistenceModifier.POSSIBLY_PERSISTENT));
    }

    /**
     * Tests if a field is transient-transactional.
     * @see EnhancerMetaData#isTransactionalField(String,String)
     */
    public boolean isTransactionalField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        return hasFieldModifier(classPath, fieldName,
                                PersistenceModifier.TRANSACTIONAL);
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
        return (field != null && field.isPrimaryKey());
    }

    /**
     * Tests if a field has default-fetch-group access annotation.
     * @see EnhancerMetaData#isDefaultFetchGroupField(String,String)
     */
    public boolean isDefaultFetchGroupField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOField field = getJDOField(classPath, fieldName);
        return (field != null && field.isDefaultFetchGroup());
    }

    /**
     * Returns the index of a declared, managed field.
     * @see EnhancerMetaData#getFieldNumber(String,String)
     */
    public int getFieldNumber(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final JDOField field = getJDOField(classPath, fieldName);
        if (field == null) {
            return -1;
        }
        return field.getRelativeFieldNumber();
    }

}
