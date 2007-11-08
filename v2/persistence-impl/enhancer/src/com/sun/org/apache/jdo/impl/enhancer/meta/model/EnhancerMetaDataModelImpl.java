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

import java.io.PrintWriter;

import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataFatalError;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataUserException;
import com.sun.org.apache.jdo.impl.enhancer.meta.util.EnhancerMetaDataBaseModel;
import com.sun.org.apache.jdo.impl.model.jdo.util.PrintSupport;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.model.jdo.PersistenceModifier;


/**
 * Provides the JDO meta information based on fully populated
 * JDO and Java models.
 */
public class EnhancerMetaDataModelImpl
    extends EnhancerMetaDataBaseModel
    implements EnhancerMetaData
{
    /**
     * The JDO model instance.
     */
    private final JDOModel jdoModel;

    /**
     * The Java model instance.
     */
    private final JavaModel javaModel;

    /**
     * The JavaType representation for java.io.Serializable.
     */
    private final JavaType serializableJavaType;
    
    /**
     * Creates an instance.
     */
    public EnhancerMetaDataModelImpl(PrintWriter out,
                                     boolean verbose,
                                     JDOModel jdoModel,
                                     JavaModel javaModel)
        throws EnhancerMetaDataFatalError
    {
        super(out, verbose);

        try {
            this.javaModel = javaModel;
            this.jdoModel = jdoModel;
            PrintSupport.printJDOModel(jdoModel);
            serializableJavaType
                = javaModel.getJavaType("java.io.Serializable");
        } catch (ModelFatalException ex) {
            final String msg
                = getI18N("enhancer.metadata.jdomodel_error", ex.getMessage());
            throw new EnhancerMetaDataFatalError(msg, ex);
        }
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
        final JavaType javaType = javaModel.getJavaType(className);
        final JavaField javaField = javaType.getJavaField(fieldName);
        final JavaType declaringClass = javaField.getDeclaringClass();
        return declaringClass.getName().replace('.', '/');
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
        final JDOClass jdoClazz = getJDOClass(classPath);
        affirm(jdoClazz != null,
               "class not known by JDO model: " + classPath);
        final JavaType javaClass = jdoClazz.getJavaType();
        affirm(javaClass != null,
               "class not known by Java model: " + classPath);
        final JavaField javaField = javaClass.getJavaField(fieldName);
        affirm(javaField != null,
               "field not known by Java model: "
               + classPath + "." + fieldName);
        final String fieldTypeName = getTypeName(fieldSig);
        final JavaType fieldType = javaModel.getJavaType(fieldTypeName);
        final JavaType modelFieldType = javaField.getType();
        affirm(fieldType == modelFieldType,
               "type mismatch for field " + classPath + "." + fieldName
               + ", declared type: " + fieldType
               + ", reported type: " + modelFieldType);
        final JDOField jdoField = jdoClazz.getField(fieldName);
        affirm(jdoField == null
               || (jdoField.getPersistenceModifier()
                   != PersistenceModifier.UNSPECIFIED),
               "known but unspecified JDO field: "
               + classPath + "." + fieldName);
    }
    
    /**
     * Tests if a class is persistence-capable.
     * @see EnhancerMetaData#isPersistenceCapableClass(String)
     */
    public boolean isPersistenceCapableClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        return (getJDOClass(classPath) != null);
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
                // class not known by JDO model
                return true;
            }
            
            // check whether field is managed only if field's
            // persistence-modifier is known by the JDO model
            final JDOField field = clazz.getField(fieldName);
            if (field == null) {
                // field not known by JDO model
                return true;
            }
            affirm(field.getPersistenceModifier()
                   != PersistenceModifier.UNSPECIFIED);
            return !field.isManaged();
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

    // ----------------------------------------------------------------------
    
    private JDOClass getJDOClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        final String className = classPath.replace('/', '.');
        return jdoModel.getJDOClass(className);
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

    /** 
     * Returns the fully qualified name of the specified type representation.
     */
    static public String getTypeName(String sig)
    {
        // translates a VM type field signature into Java-format signature
        final int n = sig.length();
        affirm(n > 0, "invalid field signature: \"\"");

        // handle arrays
        if (sig.startsWith("["))
            return sig.replace('/','.');

        // parse rest of signature
        final String name;
        final char c = sig.charAt(0);
        switch (c) {
        case 'Z':
            name = "boolean";
            break;
        case 'C':
            name = "char";
            break;
        case 'B':
            name = "byte";
            break;
        case 'S':
            name = "short";
            break;
        case 'I':
            name = "int";
            break;
        case 'F':
            name = "float";
            break;
        case 'J':
            name = "long";
            break;
        case 'D':
            name = "double";
            break;
        case 'L':
            // return reference type with array dimensions
            affirm(sig.indexOf(';') == n - 1,
                   "invalid field signature: " + sig);
            name = sig.substring(1, n - 1);
            affirm(isValidName(name, '/'),
                   "invalid field signature: " + sig);
            return name.replace('/','.');
        default:
            name = "";
            affirm(false, "invalid field signature: " + sig);
        }
        return name;
    }

    static private boolean isValidName(String name, char separator) 
    {
        final int n = name.length();
        if (n == 0) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }
        for (int i = 1; i < n; i++) {
            final char c = name.charAt(i);
            if (!Character.isJavaIdentifierPart(c) && c != separator) {
                return false;
            }
        }
        return true;
    }

/*
    static protected final void affirm(boolean condition, String msg) {
        if (!condition)
            throw new InternalError("assertion failed: " + msg);
    }
*/
}

