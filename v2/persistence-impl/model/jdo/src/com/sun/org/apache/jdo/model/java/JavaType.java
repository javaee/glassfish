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

package com.sun.org.apache.jdo.model.java;

import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.jdo.JDOClass;


/**
 * A JavaType instance represents a type as defined in the Java
 * language. The interface defines interrogative methods to check whether a
 * type is primitive, an interface or array, is a JDO supported collection
 * or map, is a value or trackable class, is a persistence capable class,
 * etc. Furthermore it defines methods to get detailed information about 
 * the type such as name, modifiers, superclass and the JDO meta data if
 * this type represent a persistence capable class.
 * <p>
 * Different environments (runtime, enhancer, development) will have 
 * different JavaType implementations to provide answers to the various
 * methods. 
 * 
 * @author Michael Bouschen
 * @since JDO 1.0.1
 */
public interface JavaType extends JavaElement
{
    /** 
     * Returns <code>true</code> if this JavaType represents a primitive
     * type. 
     * <p>
     * There are eight primitive types: <code>boolean</code>,
     * <code>byte</code>, <code>short</code>, <code>int</code>,
     * <code>long</code>, <code>char</code>, 
     * <code>float</code>, <code>double</code>.
     * @return <code>true</code> if this JavaType represents a primitive
     * type; <code>false</code> otherwise.
     */
    public boolean isPrimitive();

    /** 
     * Returns <code>true</code> if this JavaType represents an integral
     * type. 
     * <p>
     * There are five are integral types: <code>byte</code>, 
     * <code>short</code>, <code>int</code>, <code>long</code>, and
     * <code>char</code>.
     * @return <code>true</code> if this JavaType represents an integral
     * type; <code>false</code> otherwise.
     */
    public boolean isIntegral();
 
    /**
     * Returns <code>true</code> if this JavaType represents a floating
     * point type. 
     * <p>
     * There are two are floating point types:
     * <code>float</code> and <code>double</code>.
     * @return <code>true</code> if this JavaType represents a floating
     * point type; <code>false</code> otherwise.
     */
    public boolean isFloatingPoint();

    /** 
     * Determines if this JavaType object represents an interface type.
     * @return <code>true</code> if this object represents an interface type; 
     * <code>false</code> otherwise.
     */
    public boolean isInterface();
    
    /** 
     * Determines if this JavaType object represents an array type.
     * @return <code>true</code> if this object represents an array type; 
     * <code>false</code> otherwise.
     */
    public boolean isArray();

    /** 
     * Returns <code>true</code> if this JavaType represents a Java wrapper
     * class type. 
     * <p>
     * There are eight Java wrapper class types: 
     * <code>java.lang.Boolean</code>, <code>java.lang.Byte</code>, 
     * <code>java.lang.Short</code>, <code>java.lang.Integer</code>, 
     * <code>java.lang.Long</code>, <code>java.lang.Character</code>, 
     * <code>java.lang.Float</code>, <code>java.lang.Double</code>.
     * @return <code>true</code> if this JavaType represents a Java wrapper
     * class type; <code>false</code> otherwise.
     */
    public boolean isWrapperClass();
 
    /** 
     * Returns <code>true</code> if this JavaType represents a JDO
     * supported collection type. The JDO specification allows the
     * following collection interfaces and classes as types of persistent 
     * fields (see section 6.4.3 Persistent fields):
     * <ul>
     * <li><code>java.util.Collection</code>, <code>java.util.Set</code>, 
     * <code>java.util.List</code>
     * <li><code>java.util.HashSet</code>, <code>java.util.TreeSet</code>
     * <li><code>java.util.ArrayList</code>, <code>java.util.LinkedList</code>
     * <li><code>java.util.Vector</code>, <code>java.util.Stack</code>
     * </ul> 
     * @return <code>true</code> if this JavaType represents a JDO
     * supported collection; <code>false</code> otherwise.
     */
    public boolean isJDOSupportedCollection();
    
    /** 
     * Returns <code>true</code> if this JavaType represents a JDO
     * supported map type. The JDO specification allows the
     * following map interfaces and classes as types of persistent 
     * fields (see section 6.4.3 Persistent fields):
     * <ul>
     * <li><code>java.util.Map</code>
     * <li><code>java.util.HashMap</code>, <code>java.util.TreeMap</code>
     * <li> <code>java.util.Hashtable</code>, <code>java.util.Properties</code> 
     * </ul> 
     * @return <code>true</code> if this JavaType represents a JDO
     * supported map; <code>false</code> otherwise.
     */
    public boolean isJDOSupportedMap();

    /**
     * Returns <code>true</code> if this JavaType represents a trackable
     * Java class. A JDO implementation may replace a persistent field of
     * a trackable type with an assignment compatible instance of its own
     * implementation of this type which notifies the owning FCO of any
     * change of this field. 
     * <p>
     * The following types are trackable types:
     * <ul>
     * <li>JDO supported collection types
     * <li>JDO supported map types
     * <li><code>java.util.Date</code>, <code>java.sql.Date</code>, 
     * <code>java.sql.Time</code>, <code>java.sql.Timestamp</code>
     * <li><code>java.util.BitSet</code>
     * </ul> 
     * @return <code>true</code> if this JavaType represents a trackable
     * Java class, <code>false</code> otherwise.
     */
    public boolean isTrackable();
    
    /** 
     * Returns <code>true</code> if this JavaType represents a type whose
     * values may be treated as values rather than references during
     * storing. A value type is either a primitive type or a type a JDO
     * implementation may treat as SCO and the type is not one the
     * following types: array, JDO supported collection and JDO supported
     * map. 
     * <p>
     * The following classes are value types:
     * <ul>
     * <li>primitive types
     * <li>Java wrapper class types
     * <li><code>java.lang.Number</code>, <code>java.lang.String</code>
     * <li><code>java.util.Locale</code>
     * <li><code>java.math.BigDecimal</code>, <code>java.math.BigInteger</code>
     * <li><code>java.util.Date</code>, <code>java.sql.Date</code>, 
     * <code>java.sql.Time</code>, <code>java.sql.Timestamp</code>
     * <li><code>java.util.BitSet</code>
     * </ul> 
     * @return <code>true</code> if this JavaType represents a value type;
     * <code>false</code> otherwise.
     */
    public boolean isValue();

    /**
     * Returns <code>true</code> if this JavaType represents an orderable
     * type as specified in JDO.
     * <p>
     * The following types are orderable:
     * <ul>
     * <li>primitive types except <code>boolean</code>
     * <li>Java wrapper class types except <code>java.lang.Boolean</code>
     * <li><code>java.lang.String</code>
     * <li><code>java.math.BigDecimal</code>, <code>java.math.BigInteger</code>
     * <li><code>java.util.Date</code>, <code>java.sql.Date</code>, 
     * <code>java.sql.Time</code>, <code>java.sql.Timestamp</code>
     * </ul> 
     * Note, this method does not check whether this JavaType implements
     * the Comparable interface.
     * @return <code>true</code> if this JavaType represents an orderable
     * type; <code>false</code> otherwise.
     */
    public boolean isOrderable();

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
        throws ModelFatalException;

    /**
     * Returns true if this JavaType is compatible with the specified
     * JavaType. 
     * @param javaType the type this JavaType is checked with.
     * @return <code>true</code> if this is compatible with the specified
     * type; <code>false</code> otherwise.
     */
    public boolean isCompatibleWith(JavaType javaType);
    
    /**
     * Returns the name of the type. If this type represents a class or
     * interface, the name is fully qualified.
     * @return type name
     */
    public String getName();

    /**
     * Returns the Java language modifiers for the field represented by
     * this JavaType, as an integer. The java.lang.reflect.Modifier class
     * should be used to decode the modifiers. 
     * @return the Java language modifiers for this JavaType
     */
    public int getModifiers();

    /** 
     * Returns the JavaType representing the superclass of the entity
     * represented by this JavaType. If this JavaType represents either the 
     * Object class, an interface, a primitive type, or <code>void</code>, 
     * then <code>null</code> is returned. If this object represents an
     * array class then the JavaType instance representing the Object class
     * is returned.  
     * @return the superclass of the class represented by this JavaType.
     */
    public JavaType getSuperclass();

    /**
     * Returns the JDOClass instance if this JavaType represents a
     * persistence capable class. The method returns <code>null</code>, 
     * if this JavaType does not represent a persistence capable class.
     * <p>
     * A {@link org.apache.jdo.model.ModelFatalException} indicates a
     * problem accessing the JDO meta data for this JavaType.
     * @return the JDOClass instance if this JavaType represents a
     * persistence capable class; <code>null</code> otherwise.
     * @exception ModelFatalException if there is a problem accessing the
     * JDO metadata
     */
    public JDOClass getJDOClass()
        throws ModelFatalException;
    
    /** 
     * Returns the JavaType representing the component type of an array. 
     * If this JavaType does not represent an array type this method
     * returns <code>null</code>.
     * @return the JavaType representing the component type of this
     * JavaType if this class is an array; <code>null</code> otherwise. 
     */ 
    public JavaType getArrayComponentType();

    /**
     * Returns a JavaField instance that reflects the field with the
     * specified name of the class or interface represented by this
     * JavaType instance. The method returns <code>null</code>, if the
     * class or interface (or one of its superclasses) does not have a
     * field with that name.
     * @param name the name of the field 
     * @return the JavaField instance for the specified field in this class
     * or <code>null</code> if there is no such field.
     */
    public JavaField getJavaField(String name);

    /**
     * Returns an array of JavaField instances representing the declared
     * fields of the class represented by this JavaType instance. Note, this
     * method does not return JavaField instances representing inherited
     * fields. 
     * @return an array of declared JavaField instances. 
     */
    public JavaField[] getDeclaredJavaFields();

    /**
     * Returns a JavaProperty instance that reflects the property with the
     * specified name of the class or interface represented by this
     * JavaType instance. The method returns <code>null</code>, if the
     * class or interface (or one of its superclasses) does not have a
     * property with that name.
     * @param name the name of the property 
     * @return the JavaProperty instance for the specified property in this
     * class or <code>null</code> if there is no such property.
     */
    public JavaProperty getJavaProperty(String name);

    /**
     * Returns an array of JavaProperty instances representing the declared
     * properties of the class represented by this JavaType instance. Note,
     * this method does not return JavaProperty instances representing inherited
     * properties. 
     * @return an array of declared JavaProperty instances. 
     */
    public JavaProperty[] getDeclaredJavaProperties();
    

}
