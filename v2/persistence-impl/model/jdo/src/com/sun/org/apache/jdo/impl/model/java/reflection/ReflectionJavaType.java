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

package com.sun.org.apache.jdo.impl.model.java.reflection;

import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.sun.org.apache.jdo.impl.model.java.PredefinedType;
import com.sun.org.apache.jdo.impl.model.java.BaseReflectionJavaType;
import com.sun.org.apache.jdo.impl.model.java.JavaPropertyImpl;
import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaMethod;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaProperty;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOField;

/**
 * A reflection based JavaType implementation used at runtime.  
 * The implementation takes <code>java.lang.Class</code> and
 * <code>java.lang.reflect.Field</code> instances to get Java related
 * metadata about types and fields. 
 *
 * @author Michael Bouschen
 * @since JDO 1.1
 * @version JDO 2.0
 */
public class ReflectionJavaType
    extends BaseReflectionJavaType
{
    /** The declaring JavaModel instance. */
    protected final ReflectionJavaModel declaringJavaModel;

    /** Flag indicating whether the superclass is checked already. */
    private boolean superclassUnchecked = true;

    /** Flag indicating whether the JDOClass info is retrieved already. */
    private boolean jdoClassUnchecked = true;

    /** The JDO metadata, if this type represents a pc class. */
    private JDOClass jdoClass;

    /** Map of JavaField instances, key is the field name. */
    protected Map declaredJavaFields = new HashMap();

    /** Map of JavaProperty instances, key is the property name. */
    protected Map declaredJavaProperties = new HashMap();

    /** Flag indicating whether thsi JavaTYpe has been introspected. */
    private boolean introspected = false;

    /** Constructor. */
    public ReflectionJavaType(Class clazz, 
        ReflectionJavaModel declaringJavaModel)
    {
        // Pass null as the superclass to the super call. This allows lazy
        // evaluation of the superclass (see getSuperclass implementation).
        super(clazz, null); 
        this.declaringJavaModel = declaringJavaModel;
    }

    /**
     * Determines if this JavaType object represents an array type.
     * @return <code>true</code> if this object represents an array type; 
     * <code>false</code> otherwise.
     */
    public boolean isArray()
    {
        return clazz.isArray();
    }

    /** 
     * Returns <code>true</code> if this JavaType represents a persistence
     * capable class.
     * <p>
     * A {@link org.apache.jdo.model.ModelFatalException} indicates a
     * problem accessing the JDO meta data for this JavaType.
     * @return <code>true</code> if this JavaType represents a persistence
     * capable class; <code>false</code> otherwise.
     * @exception ModelFatalException if there is a problem accessing the
     * JDO metadata
     */
    public boolean isPersistenceCapable()
        throws ModelFatalException
    {
        return (getJDOClass() != null);
    }

    /** 
     * Returns the JavaType representing the superclass of the entity
     * represented by this JavaType. If this JavaType represents either the 
     * Object class, an interface, a primitive type, or <code>void</code>, 
     * then <code>null</code> is returned. If this object represents an
     * array class then the JavaType instance representing the Object class
     * is returned.  
     * @return the superclass of the class represented by this JavaType.
     */
    public synchronized JavaType getSuperclass()
    {
        if (superclassUnchecked) {
            superclassUnchecked = false;
            superclass = getJavaTypeForClass(clazz.getSuperclass());
        }
        return superclass;
    }

    /**
     * Returns the JDOClass instance if this JavaType represents a
     * persistence capable class. The method returns <code>null</code>, 
     * if this JavaType does not represent a persistence capable class.
     * <p>
     * A {@link com.sun.org.apache.jdo.model.ModelFatalException} indicates a
     * problem accessing the JDO meta data for this JavaType.
     * @return the JDOClass instance if this JavaType represents a
     * persistence capable class; <code>null</code> otherwise.
     * @exception ModelFatalException if there is a problem accessing the
     * JDO metadata
     */
    public synchronized JDOClass getJDOClass()
        throws ModelFatalException
    {
        if (jdoClassUnchecked) {
            jdoClassUnchecked = false;
            jdoClass = declaringJavaModel.getJDOModel().getJDOClass(getName());
        }
        return jdoClass;
    }
 
    /** 
     * Returns the JavaType representing the component type of an array. 
     * If this JavaType does not represent an array type this method
     * returns <code>null</code>.
     * @return the JavaType representing the component type of this
     * JavaType if this class is an array; <code>null</code> otherwise. 
     */ 
    public JavaType getArrayComponentType()
    {
        JavaType componentType = null;
        if (isArray()) {
            Class componentClass = clazz.getComponentType();
            if (componentClass != null)
                componentType = getJavaTypeForClass(componentClass);
        }
        return componentType;
    }

    /**
     * Returns a JavaField instance that reflects the field with the
     * specified name of the class or interface represented by this
     * JavaType instance. The method returns <code>null</code>, if the
     * class or interface (or one of its superclasses) does not have a
     * field with that name.
     * @param fieldName the name of the field 
     * @return the JavaField instance for the specified field in this class
     * or <code>null</code> if there is no such field.
     */
    public JavaField getJavaField(String fieldName) 
    { 
        JavaField javaField = getDeclaredJavaField(fieldName);
        if (javaField == null) {
            // check superclass
            JavaType superclass = getSuperclass();
            if ((superclass != null) &&
                (superclass != PredefinedType.objectType)) {
                javaField = superclass.getJavaField(fieldName);
            }
        }
        return javaField;
    }

    /**
     * Returns an array of JavaField instances representing the declared
     * fields of the class represented by this JavaType instance. Note, this
     * method does not return JavaField instances representing inherited
     * fields. 
     * @return an array of declared JavaField instances. 
     */
    public JavaField[] getDeclaredJavaFields()
    {
        introspectClass();
        return (JavaField[]) declaredJavaFields.values().toArray(
            new JavaField[0]);
    }
    
     /**
     * Returns a JavaProperty instance that reflects the property with the
     * specified name of the class or interface represented by this
     * JavaType instance. The method returns <code>null</code>, if the
     * class or interface (or one of its superclasses) does not have a
     * field with that name.
     * @param name the name of the property 
     * @return the JavaProperty instance for the specified property in this
     * class or <code>null</code> if there is no such property.
     */
    public JavaProperty getJavaProperty(String name)
    {
        JavaProperty javaProperty = getDeclaredJavaProperty(name);
        if (javaProperty == null) {
            // check superclass
            JavaType superclass = getSuperclass();
            if ((superclass != null) &&
                (superclass != PredefinedType.objectType)) {
                javaProperty = superclass.getJavaProperty(name);
            }
        }
        return javaProperty;
    }

    /**
     * Returns an array of JavaProperty instances representing the declared
     * properties of the class represented by this JavaType instance. Note,
     * this method does not return JavaField instances representing inherited
     * properties. 
     * @return an array of declared JavaField instances. 
     */
    public JavaProperty[] getDeclaredJavaProperties()
    {
        introspectClass();
        return (JavaProperty[]) declaredJavaProperties.values().toArray(
            new JavaProperty[0]);
    }

    // ===== Methods not specified in JavaType =====

    /**
     * Returns a JavaField instance that reflects the declared field with
     * the specified name of the class or interface represented by this
     * JavaType instance. The method returns <code>null</code>, if the 
     * class or interface does not declared a field with that name. It does
     * not check whether one of its superclasses declares such a field.
     * @param fieldName the name of the field 
     * @return the JavaField instance for the specified field in this class
     */
    public synchronized JavaField getDeclaredJavaField(String fieldName)
    {
        JavaField javaField = (JavaField)declaredJavaFields.get(fieldName);
        if (javaField == null) {
            JDOClass jdoClass = getJDOClass();
            if (jdoClass != null) {
                // pc class => look for JDOField first
                if (jdoClass.getDeclaredField(fieldName) != null) {
                    // Use JDO metadata and create a JavaField skeleton to
                    // avoid unnecessary reflection access.
                    javaField = newJavaFieldInstance(fieldName, null);
                    declaredJavaFields.put(fieldName, javaField);
                }
            }
            
            // if no field info check reflection
            if (javaField == null) {
                Field field = ReflectionJavaField.getDeclaredFieldPrivileged(
                    clazz, fieldName);
                if (field != null) {
                    javaField = newJavaFieldInstance(field);
                    declaredJavaFields.put(fieldName, javaField);
                }
            }
        }
        return javaField;   
    }

    /**
     * Returns a JavaProperty instance that reflects the declared property
     * with the specified name of the class or interface represented by this
     * JavaType instance. The method returns <code>null</code>, if the 
     * class or interface does not declared a property with that name. It does
     * not check whether one of its superclasses declares such a property.
     * @param name the name of the property 
     * @return the JavaField instance for the specified property in this class
     */
    public JavaProperty getDeclaredJavaProperty(String name) 
    {
        introspectClass();
        return (JavaProperty)declaredJavaProperties.get(name);
    }

    /** 
     * Returns a JavaType instance for the specified Class object. 
     * This method provides a hook such that ReflectionJavaType subclasses can
     * implement their own mapping of Class objects to JavaType instances. 
     */
    public JavaType getJavaTypeForClass(Class clazz)
    {
        return declaringJavaModel.getDeclaringJavaModelFactory().getJavaType(clazz);
    }

    /** 
     * Creates a new JavaProperty instance and adds it to the list of
     * declared properties of this class.
     * @param name the name of the property
     * @param getter the getter method
     * @param setter the setter method
     * @param type the ytpe of the property
     * @return a new JavaProperty declared by this class
     */
    public synchronized JavaProperty createJavaProperty(
        String name, JavaMethod getter, JavaMethod setter, JavaType type)
        throws ModelFatalException
    {
        JavaProperty javaProperty = 
            newJavaPropertyInstance(name, getter, setter, type);
        declaredJavaProperties.put(name, javaProperty);
        return javaProperty;
    }

    /**
     * Creates a new JavaMethod instance.
     * @param method the java.lang.reflect.Method instance
     * @return a new JavaMethod declared by this class
     */
    public JavaMethod createJavaMethod(Method method)
    {
        return newJavaMethodInstance(method);
    }

    /**
     * Creates a new instance of the JavaField implementation class.
     * <p>
     * This implementation returns a <code>ReflectionJavaField</code>
     * instance.
     * @return a new JavaField instance.
     */
    protected JavaField newJavaFieldInstance(String fieldName, JavaType type) 
    {
        return new ReflectionJavaField(fieldName, type, this);
    }
    
    /**
     * Creates a new instance of the JavaField implementation class.
     * <p>
     * This implementation returns a <code>ReflectionJavaField</code>
     * instance.
     * @return a new JavaField instance.
     */
    protected JavaField newJavaFieldInstance(Field field) 
    {
        return new ReflectionJavaField(field, this);
    }
    
    /**
     * Creates a new instance of the JavaProperty implementation class.
     * <p>
     * This implementation returns a <code>JavaPropertyImpl</code>
     * instance.
     * @return a new JavaProperty instance.
     */
    protected JavaProperty newJavaPropertyInstance(String name, 
            JavaMethod getter, JavaMethod setter, JavaType type) 
        throws ModelFatalException
    {
        return new JavaPropertyImpl(name, getter, setter, type, this);
    }

    /**
     * Creates a new instance of the JavaMethod implementation class.
     * <p>
     * This implementation returns a <code>ReflectionJavaMethod</code>
     * instance.
     * @return a new JavaMethod instance.
     */
    protected JavaMethod newJavaMethodInstance(Method method) 
    {
        return new ReflectionJavaMethod(method, this);
    }

    /** 
     * Helper method to introspect the class and set the declared fields and
     * properties. 
     */
    protected synchronized void introspectClass() 
    {
        if (introspected)
            // has been introspected before => return;
            return;
        
        introspected = true;
        
        new ReflectionJavaTypeIntrospector().addDeclaredJavaProperties(this);

        // now get all the declared fields
        Field[] fields = ReflectionJavaField.getDeclaredFieldsPrivileged(clazz);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String fieldName = field.getName();
            if (declaredJavaFields.get(fieldName) == null) {
                JavaField javaField = newJavaFieldInstance(field);
                declaredJavaFields.put(fieldName, javaField);
            }
        }
    }
} 
