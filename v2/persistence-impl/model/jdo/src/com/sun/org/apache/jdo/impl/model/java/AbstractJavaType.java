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

import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaProperty;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOClass;

/**
 * Abstract super class for JavaType implementations. It provides a
 * default implementation for all methods except getName. The methods return
 * the Java default value of the return type. 
 * <p>
 * A non-abstract subclass must implement method {@link #getName()} and
 * needs to override any of the other methods where the default
 * implementation is not appropriate.
 * <p>
 * Note, the class implements methods {@link #equals(Object obj)},
 * {@link #hashCode()} and {@link #toString()}using the name of a JavaType.
 *
 * @author Michael Bouschen
 * @since JDO 1.0.1
 * @version JDO 2.0
 */
abstract public class AbstractJavaType
    implements JavaType 
{
    public Object getUnderlyingObject() { return null; }
    public boolean isPrimitive() { return false; }
    public boolean isIntegral()  { return false; }
    public boolean isFloatingPoint() { return false; }
    public boolean isInterface()  { return false; }
    public boolean isArray()      { return false; }
    public boolean isWrapperClass() { return false; }
    public boolean isJDOSupportedCollection() { return false; }
    public boolean isJDOSupportedMap() { return false; }
    public boolean isTrackable()  { return false; }
    public boolean isValue() { return false; }
    public boolean isOrderable() { return false; }
    public boolean isPersistenceCapable() { return false; }
    public boolean isCompatibleWith(JavaType javaType) { return false; }
    abstract public String getName();
    public int getModifiers() { return 0; }
    public JavaType getSuperclass() { return null; }
    public JDOClass getJDOClass() { return null; }
    public JavaType getArrayComponentType() { return null; }
    public JavaField getJavaField(String name) { return null; }
    public JavaField[] getDeclaredJavaFields() { return null; }
    public JavaProperty getJavaProperty(String name) { return null; }
    public JavaProperty[] getDeclaredJavaProperties() { return null; }
    
    // ===== Methods not defined in JavaType =====

    /**
     * Indicates whether some other object is "equal to" this one.
     * @param obj the reference object with which to compare. 
     * <p>
     * This implementation compares the name of the specified object to be
     * equal to the name of this JavaType.
     * this 
     * @return <code>true</code> if this object is the same as the obj
     * argument; <code>false</code> otherwise. 
     */
    public boolean equals(Object obj)
    {
        // return true if obj is this
        if (obj == this) return  true;
        // return false if obj does not have the correct type
        if ((obj == null) || !(obj instanceof JavaType)) return false;

        JavaType other = (JavaType)obj;
        // compare names
        String name = getName();
        if (name == null) return other.getName() == null;
        return name.equals(other.getName());
    }
    
    /**
     * Returns a hash code value for the object. 
     * <p>
     * This implementation returns the hashCode of the name of this
     * JavaType. 
     * @return a hash code value for this object.
     */
    public int hashCode()
    {
        String name = getName();
        return (name == null) ? 0 : name.hashCode();
    }
    
    /**
     * Returns a string representation of the object. 
     * @return a string representation of the object.
     */
    public String toString()
    {
        return getName();
    }
}
