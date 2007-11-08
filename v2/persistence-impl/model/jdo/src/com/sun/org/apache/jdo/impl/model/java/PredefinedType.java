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

import java.util.*;
import java.math.*;

import com.sun.org.apache.jdo.model.java.JavaType;


/**
 * Instances of this class represent a type that is not a persistence
 * capable class, but is known by JDO. All JavaModel implementation will
 * use this implementation to represnet predefined types. Predefined types
 * include: 
 * <ul>
 * <li> java.lang.Object
 * <li> void
 * <li> primitive types
 * <li> Java wrapper class types
 * <li> immutable and mutable value types, such as java.lang.String,
 * java.util.Date, etc.
 * <li> JDO supported collection types, including their superclasses.
 * <li> JDO supported map types, including their superclasses.
 * </ul> 
 * This class provides public static fields for all predefined types. These
 * constants are convenience for direct access of a JavaType instance
 * representing a predefined type. The class also manages a map of
 * predefined types using the type name as key. The constructors
 * automatically add the new created instance to this map. Please use
 * method  {@link #getPredefinedType(String name)} to lookup a predefined
 * type by name. Method {@link #getPredefinedTypes()} returns a view of the
 * map of predefined types. 
 *
 * @author Michael Bouschen
 * @since JDO 1.0.1
 */
public class PredefinedType
    extends BaseReflectionJavaType
{
    /** Map of all predefined types. */
    private static final Map predefinedTypes = new HashMap();

    // ===== java.lang.Object =====

    /** The JavaType instance for the class java.lang.Object. */
    public static final PredefinedType objectType = new PredefinedType(Object.class);

    // ===== void =====

    /** The JavaType instance for the type void. */
    public static final PredefinedType voidType = new PredefinedType(void.class);

    // ===== primitive types ===== 

    /** The JavaType instance for the primitive type boolean. */
    public static final PrimitiveType booleanType = new PrimitiveType(boolean.class);
    /** The JavaType instance for the integral type byte. */
    public static final IntegralType byteType = new IntegralType(byte.class);
    /** The JavaType instance for the integral type short. */
    public static final IntegralType shortType = new IntegralType(short.class);
    /** The JavaType instance for the integral type int. */
    public static final IntegralType intType = new IntegralType(int.class);
    /** The JavaType instance for the integral type long. */
    public static final IntegralType longType = new IntegralType(long.class);
    /** The JavaType instance for the integral type char. */
    public static final IntegralType charType = new IntegralType(char.class);
    /** The JavaType instance for the floating point type float. */
    public static final FloatingPointType floatType = new FloatingPointType(float.class);
    /** The JavaType instance for the floating point type double. */
    public static final FloatingPointType doubleType = new FloatingPointType(double.class);

    // ===== Value types ===== 

    /** The JavaType instance for the class java.lang.Numer. */
    public static final ValueClassType numberType = new ValueClassType(Number.class, objectType, false);
    /** The JavaType instance for the class java.lang.String. */
    public static final ValueClassType stringType = new ValueClassType(String.class, objectType, true);
    /** The JavaType instance for the class java.lang.Locale. */
    public static final ValueClassType localeType = new ValueClassType(Locale.class, objectType, false);
    /** The JavaType instance for the class java.math.BigDecimal. */
    public static final ValueClassType bigDecimalType = new ValueClassType(BigDecimal.class, numberType, true);
    /** The JavaType instance for the class java.math.BigInteger. */
    public static final ValueClassType bigIntegerType = new ValueClassType(BigInteger.class, numberType, true);

    // ===== WrapperClass types ===== 

    /** The JavaType instance for the class java.lang.Boolean. */
    public static final WrapperClassType booleanClassType = new WrapperClassType(Boolean.class, objectType, false);
    /** The JavaType instance for the class java.lang.Byte. */
    public static final WrapperClassType byteClassType = new WrapperClassType(Byte.class, numberType, true);
    /** The JavaType instance for the class java.lang.Short. */
    public static final WrapperClassType shortClassType = new WrapperClassType(Short.class, numberType, true);
    /** The JavaType instance for the class java.lang.Integer. */
    public static final WrapperClassType integerClassType = new WrapperClassType(Integer.class, numberType, true);
    /** The JavaType instance for the class java.lang.Long. */
    public static final WrapperClassType longClassType = new WrapperClassType(Long.class, numberType, true);
    /** The JavaType instance for the class java.lang.Character. */
    public static final WrapperClassType characterClassType = new WrapperClassType(Character.class, numberType, true);
    /** The JavaType instance for the class java.lang.Float. */
    public static final WrapperClassType floatClassType = new WrapperClassType(Float.class, numberType, true);
    /** The JavaType instance for the class java.lang.Double. */
    public static final WrapperClassType doubleClassType = new WrapperClassType(Double.class, numberType, true);

    // ===== Mutable value types ===== 

    /** The JavaType instance for the class java.util.Date. */
    public static final MutableValueClassType dateType = new MutableValueClassType(Date.class, objectType, true);
    /** The JavaType instance for the class java.sql.Date. */
    public static final MutableValueClassType sqlDateType = new MutableValueClassType(java.sql.Date.class, dateType, true);
    /** The JavaType instance for the class java.sql.Time. */
    public static final MutableValueClassType sqlTimeType = new MutableValueClassType(java.sql.Time.class, dateType, true);
    /** The JavaType instance for the class java.sql.Timestamp. */
    public static final MutableValueClassType sqlTimestampType = new MutableValueClassType(java.sql.Timestamp.class, dateType, true);
    /** The JavaType instance for the class java.util.BitSet. */
    public static final MutableValueClassType bitsetType = new MutableValueClassType(BitSet.class, objectType, false);

    // ===== JDOSupportedCollection types ===== 

    /** The JavaType instance for the interface java.util.Collection. */
    public static final JDOSupportedCollectionType collectionType = new JDOSupportedCollectionType(Collection.class);
    /** The JavaType instance for the interface java.util.Set. */
    public static final JDOSupportedCollectionType setType = new JDOSupportedCollectionType(Set.class);
    /** The JavaType instance for the interface java.util.List. */
    public static final JDOSupportedCollectionType listType = new JDOSupportedCollectionType(List.class);
    /** The JavaType instance for the class java.util.AbstractCollection. */
    public static final PredefinedType abstractCollectionType = new PredefinedType(AbstractCollection.class, objectType);
    /** The JavaType instance for the class java.util.AbstractSet. */
    public static final PredefinedType abstractSetType = new PredefinedType(AbstractSet.class, abstractCollectionType);
    /** The JavaType instance for the class java.util.HashSet. */
    public static final JDOSupportedCollectionType hashSetType = new JDOSupportedCollectionType(HashSet.class, abstractSetType);
    /** The JavaType instance for the class java.util.TreeSet. */
    public static final JDOSupportedCollectionType treeSetType = new JDOSupportedCollectionType(TreeSet.class, abstractSetType);
    /** The JavaType instance for the class java.util.AbstractList. */
    public static final PredefinedType abstractListType = new PredefinedType(AbstractList.class, abstractCollectionType);
    /** The JavaType instance for the class java.util.ArrayList. */
    public static final JDOSupportedCollectionType arrayListType = new JDOSupportedCollectionType(ArrayList.class, abstractListType);
    /** The JavaType instance for the class java.util.LinkedList. */
    public static final JDOSupportedCollectionType linkedListType = new JDOSupportedCollectionType(LinkedList.class, abstractListType);
    /** The JavaType instance for the class java.util.Vector. */
    public static final JDOSupportedCollectionType vectorType = new JDOSupportedCollectionType(Vector.class, abstractListType);
    /** The JavaType instance for the class java.util.Stack. */
    public static final JDOSupportedCollectionType stackType = new JDOSupportedCollectionType(Stack.class, vectorType);

    // ===== JDOSupportedMap types =====

    /** The JavaType instance for the interface java.util.Map. */
    public static final JDOSupportedMapType mapType = new JDOSupportedMapType(Map.class);
    /** The JavaType instance for the class java.util.AbstractMap. */
    public static final PredefinedType abstractMapType = new PredefinedType(AbstractMap.class, objectType);
    /** The JavaType instance for the class java.util.HashMap. */
    public static final JDOSupportedMapType hashMapType = new JDOSupportedMapType(HashMap.class, abstractMapType);
    /** The JavaType instance for the class java.util.Dictionary. */
    public static final PredefinedType dictionaryType = new PredefinedType(Dictionary.class, objectType);
    /** The JavaType instance for the class java.util.Hashtable. */
    public static final JDOSupportedMapType hashtableType = new JDOSupportedMapType(Hashtable.class, dictionaryType);
    /** The JavaType instance for the class java.util.Properties. */
    public static final JDOSupportedMapType propertiesType = new JDOSupportedMapType(Properties.class, hashtableType);
    /** The JavaType instance for the class java.util.TreeMap. */
    public static final JDOSupportedMapType treeMapType = new JDOSupportedMapType(TreeMap.class, abstractMapType);

    /** 
     * The static block sets references between the JavaType instances for
     * primitives types and the JavaType instances for the corresponding
     * wrapper class.
     */
    static
    {
        booleanType.setWrapperClassType(booleanClassType);
        booleanClassType.setWrappedPrimitiveType(booleanType);
        byteType.setWrapperClassType(byteClassType);
        byteClassType.setWrappedPrimitiveType(byteType);
        shortType.setWrapperClassType(shortClassType);
        shortClassType.setWrappedPrimitiveType(shortType);
        intType.setWrapperClassType(integerClassType);
        integerClassType.setWrappedPrimitiveType(intType);
        longType.setWrapperClassType(longClassType);
        longClassType.setWrappedPrimitiveType(longType);
        charType.setWrapperClassType(characterClassType);
        characterClassType.setWrappedPrimitiveType(charType);
        floatType.setWrapperClassType(floatClassType);
        floatClassType.setWrappedPrimitiveType(floatType);
        doubleType.setWrapperClassType(doubleClassType);
        doubleClassType.setWrappedPrimitiveType(doubleType);
    }
    
    /**
     * Constructor taking a Class instance.
     * It automatically adds a predefined type to the static map of all
     * predefined types. 
     * @param clazz the Class instance for this JavaType
     */
    protected PredefinedType(Class clazz)
    {
        this(clazz, null);
    }
    
    /** 
     * Constructor taking a Class instance and a JavaType representing the
     * superclass of the new JavaType instance. 
     * It automatically adds a predefined type to the static
     * map of all predefined types.
     * @param clazz the Class instance for this JavaType
     * @param superclass the JavaType representing the superclass or
     * <code>null</code> if there is no superclass.
     */
    protected PredefinedType(Class clazz, JavaType superclass)
    {
        super(clazz, superclass);
        predefinedTypes.put(clazz.getName(), this);
    }

    /**
     * Returns the JavaType instance for a predefined type with the
     * specified name. The method return <code>null</code> if the specified
     * name does not denote a predefined type. 
     * @param name the name of the predefined type.
     * @return the JavaType instance for the specified predefined type.
     */
    public static JavaType getPredefinedType(String name)
    {
        return (JavaType)predefinedTypes.get(name);
    }

    /** 
     * Returns an unmodifiable view of the predefined types map. This map
     * maps type names to JavaType instances.
     * @return an unmodifiable view of the predefined types map.
     */
    public static Map getPredefinedTypes()
    {
        return Collections.unmodifiableMap(predefinedTypes);
    }

}
