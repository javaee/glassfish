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

package com.sun.org.apache.jdo.impl.model.java;

import com.sun.org.apache.jdo.model.java.JavaMember;
import com.sun.org.apache.jdo.model.java.JavaType;

/**
 * Abstract super class for JavaMember implementations. 
 * It provides getters for the name and declaringClass properties which are
 * initialized in the constructor. 
 * <p>
 * A non-abstract subclass must implement methods 
 * {@link #getModifiers()} and {@link #getType()}.
 *
 * @author Michael Bouschen
 * @since JDO 2.0
 */
abstract public class AbstractJavaMember
    implements JavaMember
{
    /** The Field name. */
    private String name;

    /** The declaring class. */
    private JavaType declaringClass;

    /** 
     * Constructor setting the name and declaringClass property.
     * @param name field name
     * @param declaringClass the JavaType of the class or interface that
     * declares this JavaMember.
     */
    public AbstractJavaMember(String name, JavaType declaringClass)
    {
        this.name = name;
        this.declaringClass = declaringClass;
    }

    // ===== Methods specified in JavaMember =====

    /**
     * Returns the name of the field. 
     * @return field name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns the JavaType instance representing the class or interface
     * that declares the field represented by this JavaMember instance.
     * @return the JavaType instance of the declaring class.
     */
    public JavaType getDeclaringClass()
    {
        return declaringClass;
    }
    
    /**
     * Returns the Java language modifiers for the member represented by
     * this JavaMember, as an integer. The java.lang.reflect.Modifier class
     * should be used to decode the modifiers. 
     * @return the Java language modifiers for this JavaMember
     * @see java.lang.reflect.Modifier
     */
    abstract public int getModifiers();

    /**
     * Returns the JavaType representation of the type of the memeber.
     * @return type of the member
     */
    abstract public JavaType getType();
    
    /**
     * Returns the JavaType representation of the component type of the type
     * of the property, if the property type is an array or collection. The
     * method returns <code>null</code>, if the property type is not an array
     * or collection.
     * @return the component type of the property type in case of an array or
     * collection.
     */
    public JavaType getComponentType()
    {
        JavaType componentType = null;
        JavaType type = getType();
        if (type.isArray())
            componentType = type.getArrayComponentType();
        else if (type.isJDOSupportedCollection())
            componentType = PredefinedType.objectType;
        return componentType;
    }

    // ===== Methods not specified in JavaMember =====

    /**
     * Indicates whether some other object is "equal to" this one.
     * @param obj the reference object with which to compare. 
     * <p>
     * This implementation matches the declaring class and the name of the
     * specified object to the declaring class and the name of this
     * JavaMember. 
     * @return <code>true</code> if this object is the same as the obj
     * argument; <code>false</code> otherwise. 
     */
    public boolean equals(Object obj)
    {
        // return true if obj is this
        if (obj == this) return  true;
        // return false if obj does not have the correct type
        if ((obj == null) || !(obj instanceof JavaMember)) return false;

        JavaMember other = (JavaMember)obj;
        // compare declaringClass and field names
        return (getDeclaringClass() == other.getDeclaringClass())
            && (getName().equals(other.getName()));
    }
    
    /**
     * Returns a hash code value for the object. 
     * <p>
     * This is computed as the exclusive-or of the hashcodes for the
     * underlying field's declaring class name and its name.
     * @return a hash code value for this object.
     */
    public int hashCode()
    {
        return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }
    
    /**
     * Returns a string representation of the object. 
     * @return a string representation of the object.
     */
    public String toString()
    {
        return getDeclaringClass().getName() + "." + getName(); //NOI18N
    }
}
