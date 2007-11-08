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

package com.sun.persistence.runtime.query;

import com.sun.org.apache.jdo.model.java.JavaType;

/**
 * 
 * @author Dave Bristor
 */
public interface QueryContext {
    /** @return the model of the primitive type boolean. */
    public JavaType getBooleanType();

    /** @return the model of the primitive type byte. */
    public JavaType getByteType();

    /** @return the model of the primitive type short. */
    public JavaType getShortType();

    /** @return the model of the primitive type char. */
    public JavaType getCharType();

    /** @return the model of the primitive type int. */
    public JavaType getIntType();

    /** @return the model of the primitive type long. */
    public JavaType getLongType();

    /** @return the model of the primitive type float. */
    public JavaType getFloatType();

    /** @return the model of the primitive type double. */
    public JavaType getDoubleType();

    /** @return the model of the wrapper class type boolean. */
    public JavaType getBooleanClassType();

    /** @return the model of the wrapper class type byte. */
    public JavaType getByteClassType();

    /** @return the model of the wrapper class type short. */
    public JavaType getShortClassType();

    /** @return the model of the wrapper class type char. */
    public JavaType getCharacterClassType();

    /** @return the model of the wrapper class type int. */
    public JavaType getIntegerClassType();

    /** @return the model of the wrapper class type long. */
    public JavaType getLongClassType();

    /** @return the model of the wrapper class type float. */
    public JavaType getFloatClassType();

    /** @return the model of the wrapper class type double. */
    public JavaType getDoubleClassType();

    /** @return the model of the type java.lang.String. */
    public JavaType getStringType();

    /** @return the model of the type java.math.BigDecimal. */
    public JavaType getBigDecimalType();

    /** @return the model of the type java.math.BigInteger. */
    public JavaType getBigIntegerType();

    /** @return the model of the internal error type. */
    public JavaType getErrorType();

    /** @return the model of the internal "unknown" type. */
    public JavaType getUnknownType();

    /**
     * The method returns a type info by type name. If the type name denotes a
     * class the name should be fully qualified. The method uses the type name
     * as type info.
     */
    public Object getTypeInfo(Object name);

    /**
     * The method returns a type info by type name by class object.
     */
    public Object getTypeInfo(Class clazz);

    /**
     * Returns <code>true</code> if type denotes the error type.
     */
    public boolean isErrorType(Object type);

    /**
     * Returns <code>true</code> if type denotes the unknown type.
     */
    public boolean isUnknownType(Object type);

    /**
     * Returns <code>true</code> if type is boolean or java.lang.Boolean
     */
    public boolean isBooleanType(Object type);

    /**
     * Returns <code>true</code> if type is char or java.lang.Character
     */
    public boolean isCharType(Object type);

    /**
     * Returns <code>true</code> if type is int or java.lang.Integer
     */
    public boolean isIntType(Object type);

    /**
     * Returns <code>true</code> if type is double or java.lang.Double.
     */
    public boolean isDoubleType(Object type);

    /**
     * Returns <code>true</code> if type is a primitive numeric type such as
     * byte, int etc.
     */
    public boolean isNumericType(Object type);

    /**
     * Returns <code>true</code> if type is a wrapper class of a primitive
     * numeric type such as java.lang.Byte, java.lang.Integer etc.
     */
    public boolean isNumericWrapperType(Object type);

    /**
     * Returns <code>true</code> if type is a NumerType, which means it is
     * either a numeric primitive or a numeric wrapper class.
     */
    public boolean isNumberType(Object type);

    /**
     * Returns <code>true</code> if type is a floating point type or wrapper
     * class of a floating point type.
     */
    public boolean isFloatingPointType(Object type);

    /**
     * Returns <code>true</code> if type denotes java.lang.String.
     */
    public boolean isStringType(Object type);

    /**
     * Returns <code>true</code> if type is a collection type.
     */
    public boolean isCollectionType(Object type);

    /**
     * Returns <code>true</code> if type is a date or time type
     */
    public boolean isDateTimeType(Object type);

    /**
     * Returns <code>true</code> if type is an orderable type
     */
    public boolean isOrderableType(Object type);

    /**
     * Returns the type info for a primitive type. The method returns {@link
     * #errorType} if the specified type is not a primitive type.
     */
    public Object getPrimitiveType(Object type);

    /**
     * Returns the type info for a wrapper class type. The method returns {@link
     * #errorType} if the specified type is not a wrapper class type.
     */
    public Object getWrapperType(Object type);

    /**
     * Implements binary numeric promotion as defined in the Java Language
     * Specification section 5.6.2
     */
    public Object binaryNumericPromotion(Object left, Object right);

    /**
     * Implements unray numeric promotion as defined in the Java Language
     * Specification section 5.6.1
     */
    public Object unaryNumericPromotion(Object type);

    /**
     * Implements type compatibility. The method returns <code>true</code> if
     * left is compatible with right. This is equivalent to
     * rightClass.isAssignableFrom(leftClass). Note, the method does not support
     * inheritance.
     */
    public boolean isCompatibleWith(Object left, Object right);

    /**
     * Returns the type name for a specified type info.
     */
    public String getTypeName(Object type);

    /**
     * Returns the typeInfo (the ejb name) for the specified abstract schema.
     */
    public Object getTypeInfoForAbstractSchema(String abstractSchema);

    /**
     * Returns the typeInfo (the ejb name) for the specified abstract schema.
     */
    public String getAbstractSchemaForTypeInfo(Object typeInfo);

    /**
     * Returns the type info for the type of the given field.
     */
    public Object getFieldType(Object typeInfo, String fieldName);

    /**
     * Returns the field info for the specified field of the specified type. The
     * field info is opaque for the caller. Methods {@link #isRelationship} and
     * {@link #getElementType} allow to get details for a given field info.
     */
    public Object getFieldInfo(Object typeInfo, String fieldName);

    /**
     * Returns <code>true</code> if the specified field info denotes a
     * relationship field.
     */
    public boolean isRelationship(Object fieldInfo);

    /**
     * Returns the type info of the element type if the specified field info
     * denotes a collection relationship. Otherwise it returns
     * <code>null</code>.
     */
    public Object getElementType(Object fieldInfo);

    /**
     * Gets the name of the persistence-capable class which corresponds to the
     * specified typeInfo (assuming an ejb name). The method returs the type
     * name of the specified typeInfo, it the typeInfo does not denote an
     * ejb-name (e.g. a local or remote interface).
     */
    public String getPCForTypeInfo(Object typeInfo);

    /**
     * Returns <code>true</code> if the specified type info denotes an ejb
     * name.
     */
    public boolean isEjbName(Object typeInfo);

    /**
     * Returns <code>true</code> if the specified type info denotes an ejb name
     * or the name of a local interface or the name of a remote interface.
     */
    public boolean isEjbOrInterfaceName(Object typeInfo);

    /**
     * Returns <code>true</code> if the specified type info denotes the remote
     * interface of the bean with the specified ejb name.
     */
    public boolean isRemoteInterfaceOfEjb(Object typeInfo, String ejbName);

    /**
     * Returns <code>true</code> if the specified type info denotes the local
     * interface of the bean with the specified ejb name.
     */
    public boolean isLocalInterfaceOfEjb(Object typeInfo, String ejbName);

    /**
     * Returns <code>true</code> if the specified type info denotes a remote
     * interface.
     */
    public boolean isRemoteInterface(Object typeInfo);

    /**
     * Returns <code>true</code> if the specified type info denotes a local
     * interface.
     */
    public boolean isLocalInterface(Object typeInfo);

    /**
     * Returns <code>true</code> if the bean with the specified ejb name has a
     * remote interface.
     */
    public boolean hasRemoteInterface(Object typeInfo);

    /**
     * Returns <code>true</code> if the bean with the specified ejb name has a
     * local interface.
     */
    public boolean hasLocalInterface(Object typeInfo);

    /**
     * Return return type for Sum function for a given type.
     * @param type is a number data type
     */
    public Object getSumReturnType(Object type);

    /**
     * Return return type for Avg function for a given type.
     * @param type is a number data type
     */
    public Object getAvgReturnType(Object type);

    /**
     * Return return type for Min/Max function for a given type.
     * @param type is an orderable data type
     */
    public Object getMinMaxReturnType(Object type);

    public boolean isMemberOf(int a, int b);
    
    /**
     * Returns the common type info for the specified operand types. 
     * This includes binary numeric promotion as specified in Java.
     * @param left type info of left operand 
     * @param right type info of right operand
     * @return the type info of the operator
     */
    public Object getCommonOperandType(Object left, Object right);
}
