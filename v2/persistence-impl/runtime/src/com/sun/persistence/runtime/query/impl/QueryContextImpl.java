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


package com.sun.persistence.runtime.query.impl;

import com.sun.org.apache.jdo.impl.model.java.AbstractJavaType;
import com.sun.org.apache.jdo.impl.model.java.ErrorType;
import com.sun.org.apache.jdo.impl.model.java.runtime.RuntimeJavaModelFactory;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.java.JavaModelFactory;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.persistence.runtime.query.QueryContext;
import com.sun.persistence.utility.I18NHelper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Helper class to support type info access. A type info is statically an
 * object, internally the helper uses the type name as type info. The helper
 * uses a model instance to access meta model info and uses a NameMapper to map
 * EJB names to state instance names and vice versa.
 * @author Michael Bouschen
 * @author Shing Wai Chan
 */
abstract public class QueryContextImpl implements QueryContext {
    /** Set of names of numeric types. */
    protected static final Set<JavaType> numericTypes = new HashSet<JavaType>();

    /** Set of names of numeric wrapper classes. */
    protected static final Set<JavaType> numericWrapperTypes = new HashSet<JavaType>();

    /** Set of names of date and time types.  */
    protected static final Set<JavaType> dateTimeTypes = new HashSet<JavaType>();

    /** Set of names of collection types.  */
    protected static final Set<JavaType> collectionTypes = new HashSet<JavaType>();


    /** Represents the primitive type boolean. */
    static final JavaType booleanType;

    /** Represents the primitive type byte. */
    static final JavaType byteType;

    /** Represents the primitive type short. */
    static final JavaType shortType;

    /** Represents the primitive type char. */
    static final JavaType charType;

    /** Represents the primitive type int. */
    static final JavaType intType;

    /** Represents the primitive type long. */
    static final JavaType longType;

    /** Represents the primitive type float. */
    static final JavaType floatType;

    /** Represents the primitive type double. */
    static final JavaType doubleType;

    /** Represents the wrapper class type boolean. */
    static final JavaType booleanClassType;

    /** Represents the wrapper class type byte. */
    static final JavaType byteClassType;

    /** Represents the wrapper class type short. */
    static final JavaType shortClassType;

    /** Represents the wrapper class type char. */
    static final JavaType characterClassType;

    /** Represents the wrapper class type int. */
    static final JavaType integerClassType;

    /** Represents the wrapper class type long. */
    static final JavaType longClassType;

    /** Represents the wrapper class type float. */
    static final JavaType floatClassType;

    /** Represents the wrapper class type double. */
    static final JavaType doubleClassType;

    /** Represents the type java.lang.String. */
    static final JavaType stringType;

    /** Represents the type java.math.BigDecimal. */
    static final JavaType bigDecimalType;

    /** Represents the type java.math.BigInteger. */
    static final JavaType bigIntegerType;

    /** Represents the internal error type. */
    static final JavaType errorType;

    /** Represents the internal error type. */
    static final JavaType unknownType;

    /**
     * I18N support.
     */
    protected final static ResourceBundle msgs = I18NHelper.loadBundle(
            QueryContextImpl.class);

    /** Model from which type information is obtained. */
    private static JavaModel model;

    /** Inilialize static fields numericTypes numericWrapperTypes. */
    static {

        try {
            // XXX TBD find a better class loader, or allow null to createJavaModel to
            // mean the bootstrap class loader.
            JavaModelFactory factory = 
                (JavaModelFactory) AccessController.doPrivileged(
                    new PrivilegedAction () {
                        public Object run () {
                            return RuntimeJavaModelFactory.getInstance();
                        }});
            model = factory.createJavaModel(EJBQLASTFactory.getInstance().getClass().getClassLoader());
        } catch (ModelException ex) {
            ErrorMsg.fatal(I18NHelper.getMessage(
                msgs, "EXC_CannotGetRuntimeJavaModelFactory")); // XXX FIXME msg key NOI18N
        }

        booleanType = model.getJavaType(boolean.class);
        byteType = model.getJavaType(byte.class);
        shortType = model.getJavaType(short.class);
        charType = model.getJavaType(char.class);
        intType = model.getJavaType(int.class);
        longType = model.getJavaType(long.class);
        floatType = model.getJavaType(float.class);
        doubleType = model.getJavaType(double.class);
        booleanClassType = model.getJavaType(Boolean.class);
        byteClassType = model.getJavaType(Byte.class);
        shortClassType = model.getJavaType(Short.class);
        characterClassType = model.getJavaType(Character.class);
        integerClassType = model.getJavaType(Integer.class);
        longClassType = model.getJavaType(Long.class);
        floatClassType = model.getJavaType(Float.class);
        doubleClassType = model.getJavaType(Double.class);
        stringType = model.getJavaType(String.class);
        bigDecimalType = model.getJavaType(BigDecimal.class);
        bigIntegerType = model.getJavaType(BigInteger.class);

        errorType = model.getJavaType(ErrorType.class);
        unknownType = model.getJavaType(UnknownType.class);

        numericTypes.add(byteType);
        numericTypes.add(shortType);
        numericTypes.add(charType);
        numericTypes.add(intType);
        numericTypes.add(longType);
        numericTypes.add(floatType);
        numericTypes.add(doubleType);

        numericWrapperTypes.add(byteClassType);
        numericWrapperTypes.add(shortClassType);
        numericWrapperTypes.add(characterClassType);
        numericWrapperTypes.add(integerClassType);
        numericWrapperTypes.add(longClassType);
        numericWrapperTypes.add(floatClassType);
        numericWrapperTypes.add(doubleClassType);

        dateTimeTypes.add(model.getJavaType(java.util.Date.class));
        dateTimeTypes.add(model.getJavaType(java.sql.Date.class));
        dateTimeTypes.add(model.getJavaType(java.sql.Time.class));
        dateTimeTypes.add(model.getJavaType(java.sql.Timestamp.class));

        collectionTypes.add(model.getJavaType(java.util.Collection.class));
        collectionTypes.add(model.getJavaType(java.util.AbstractCollection.class));
        collectionTypes.add(model.getJavaType(java.util.Set.class));
        collectionTypes.add(model.getJavaType(java.util.HashSet.class));
        collectionTypes.add(
            model.getJavaType(
                    com.sun.org.apache.jdo.impl.sco.HashSet.class));
    }

    protected QueryContextImpl() {
        // empty
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getBooleanType() */
    public JavaType getBooleanType() {
        return booleanType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getByteType() */
    public JavaType getByteType() {
        return byteType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getShortType() */
    public JavaType getShortType() {
        return shortType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getCharType() */
    public JavaType getCharType() {
        return charType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getIntType() */
    public JavaType getIntType() {
        return intType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getLongType() */
    public JavaType getLongType() {
        return longType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getFloatType() */
    public JavaType getFloatType() {
        return floatType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getDoubleType() */
    public JavaType getDoubleType() {
        return doubleType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getBooleanClassType() */
    public JavaType getBooleanClassType() {
        return booleanClassType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getByteClassType() */
    public JavaType getByteClassType() {
        return byteClassType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getShortClassType() */
    public JavaType getShortClassType() {
        return shortClassType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getCharacterClassType() */
    public JavaType getCharacterClassType() {
        return characterClassType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getIntegerClassType() */
    public JavaType getIntegerClassType() {
        return integerClassType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getLongClassType() */
    public JavaType getLongClassType() {
        return longClassType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getFloatClassType() */
    public JavaType getFloatClassType() {
        return floatClassType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getDoubleClassType() */
    public JavaType getDoubleClassType() {
        return doubleClassType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getStringType() */
    public JavaType getStringType() {
        return stringType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getBigDecimalType() */
    public JavaType getBigDecimalType() {
        return bigDecimalType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getBigIntegerType() */
    public JavaType getBigIntegerType() {
        return bigIntegerType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getErrorType() */
    public JavaType getErrorType() {
        return errorType;
    }

    /** @see com.sun.persistence.runtime.query.QueryContext#getErrorType() */
    public JavaType getUnknownType() {
        return unknownType;
    }
    
    /**
     * The method returns a type info by type name. If the type name denotes a
     * class the name should be fully qualified. The method uses the type name
     * as type info.
     */
    public Object getTypeInfo(Object name) {
        return name;
    }

    /**
     * The method returns a type info by type name by class object.
     */
    public Object getTypeInfo(Class clazz) {
        return getTypeInfo(clazz.getName());
    }

    /**
     * Returns <code>true</code> if type denotes the error type.
     */
    public boolean isErrorType(Object type) {
        return errorType.equals(type);
    }
    
    /**
     * Returns <code>true</code> if type denotes the unknown type.
     */
    public boolean isUnknownType(Object type) {
        return unknownType == type;
    }

    /**
     * Returns <code>true</code> if type is boolean or java.lang.Boolean
     */
    public boolean isBooleanType(Object type) {
        return booleanType.equals(type) || booleanClassType.equals(type);
    }

    /**
     * Returns <code>true</code> if type is char or java.lang.Character
     */
    public boolean isCharType(Object type) {
        return charType.equals(type) || characterClassType.equals(type);
    }

    /**
     * Returns <code>true</code> if type is int or java.lang.Integer
     */
    public boolean isIntType(Object type) {
        return intType.equals(type) || integerClassType.equals(type);
    }

    /**
     * Returns <code>true</code> if type is double or java.lang.Double.
     */
    public boolean isDoubleType(Object type) {
        return doubleType.equals(type) || doubleClassType.equals(type);
    }

    /**
     * Returns <code>true</code> if type is a primitive numeric type such as
     * byte, int etc.
     */
    public boolean isNumericType(Object type) {
        return numericTypes.contains(type);
    }

    /**
     * Returns <code>true</code> if type is a wrapper class of a primitive
     * numeric type such as java.lang.Byte, java.lang.Integer etc.
     */
    public boolean isNumericWrapperType(Object type) {
        return numericWrapperTypes.contains(type);
    }

    /**
     * Returns <code>true</code> if type is a NumerType, which means it is
     * either a numeric primitive or a numeric wrapper class.
     */
    public boolean isNumberType(Object type) {
        return isNumericType(type) || isNumericWrapperType(type)
            || bigDecimalType.equals(type) || bigIntegerType.equals(type);
    }

    /**
     * Returns <code>true</code> if type is a floating point type or wrapper
     * class of a floating point type.
     */
    public boolean isFloatingPointType(Object type) {
        return doubleType.equals(type) || doubleClassType.equals(type)
            || floatType.equals(type) || floatClassType.equals(type);
    }

    /**
     * Returns <code>true</code> if type denotes java.lang.String.
     */
    public boolean isStringType(Object type) {
        return stringType.equals(type);
    }

    /**
     * Returns <code>true</code> if type is a collection type.
     */
    public boolean isCollectionType(Object type) {
        return collectionTypes.contains(type);
    }

    /**
     * Returns <code>true</code> if type is a date or time type
     */
    public boolean isDateTimeType(Object type) {
        return dateTimeTypes.contains(type);
    }

    /**
     * Returns <code>true</code> if type is an orderable type
     */
    public boolean isOrderableType(Object type) {
        return isNumberType(type) || isDateTimeType(type)
                || isStringType(type);
    }

    /**
     * Returns the type info for a primitive type. The method returns {@link
     * #errorType} if the specified type is not a primitive type.
     */
    public Object getPrimitiveType(Object type) {
        Object result = errorType;
        if (booleanClassType.equals(type)) {
            result = booleanType;
        } else if (integerClassType.equals(type)) {
            result = intType;
        } else if (longClassType.equals(type)) {
            result = longType;
        } else if (floatClassType.equals(type)) {
            result = floatType;
        } else if (doubleClassType.equals(type)) {
            result = doubleType;
        } else if (byteClassType.equals(type)) {
            result = byteType;
        } else if (shortClassType.equals(type)) {
            result = shortType;
        } else if (characterClassType.equals(type)) {
            result = charType;
        }
        return result;
    }

    /**
     * Returns the type info for a wrapper class type. The method returns {@link
     * #errorType} if the specified type is not a wrapper class type.
     */
    public Object getWrapperType(Object type) {
        Object result = errorType;
        if (booleanType.equals(type)) {
            result = booleanClassType;
        } else if (intType.equals(type)) {
            result = integerClassType;
        } else if (longType.equals(type)) {
            result = longClassType;
        } else if (floatType.equals(type)) {
            result = floatClassType;
        } else if (doubleType.equals(type)) {
            result = doubleClassType;
        } else if (byteType.equals(type)) {
            result = byteClassType;
        } else if (shortType.equals(type)) {
            result = shortClassType;
        } else if (charType.equals(type)) {
            result = characterClassType;
        }
        return result;
    }

    /**
     * Implements binary numeric promotion as defined in the Java Language
     * Specification section 5.6.2
     */
    public Object binaryNumericPromotion(Object left, Object right) {
        if (isNumericType(left) && isNumericType(right)) {
            if (doubleType.equals(left) || doubleType.equals(right)) {
                return doubleType;
            } else if (floatType.equals(left) || floatType.equals(right)) {
                return floatType;
            } else if (longType.equals(left) || longType.equals(right)) {
                return longType;
            } else {
                return intType;
            }
        }
        return errorType;
    }

    /**
     * Implements unray numeric promotion as defined in the Java Language
     * Specification section 5.6.1
     */
    public Object unaryNumericPromotion(Object type) {
        if (isNumericType(type)) {
            if (byteType.equals(type) || shortType.equals(type)
                    || charType.equals(type)) {
                return intType;
            } else {
                return type;
            }
        }
        return errorType;
    }

    /**
     * Implements type compatibility. The method returns <code>true</code> if
     * left is compatible with right. This is equivalent to
     * rightClass.isAssignableFrom(leftClass). Note, the method does not support
     * inheritance.
     */
    abstract public boolean isCompatibleWith(Object left, Object right);

    /**
     * Returns the type name for a specified type info.
     */
    public String getTypeName(Object type) {
        String rc = null;
        try {
            JavaType t = (JavaType) type;
            rc = t.getName();
        } catch (Exception ex) {
            // empty
        }
        return rc;
    }

    /**
     * Returns the typeInfo (the ejb name) for the specified abstract schema.
     */
    abstract public Object getTypeInfoForAbstractSchema(String abstractSchema);

    /**
     * Returns the typeInfo (the ejb name) for the specified abstract schema.
     */
    abstract public String getAbstractSchemaForTypeInfo(Object typeInfo);

    /**
     * Returns the type info for the type of the given field.
     */
    abstract public Object getFieldType(Object typeInfo, String fieldName);

    /**
     * Returns the field info for the specified field of the specified type. The
     * field info is opaque for the caller. Methods {@link #isRelationship} and
     * {@link #getElementType} allow to get details for a given field info.
     */
    abstract public Object getFieldInfo(Object typeInfo, String fieldName);

    /**
     * Returns <code>true</code> if the specified field info denotes a
     * relationship field.
     */
    abstract public boolean isRelationship(Object fieldInfo);

    /**
     * Returns the type info of the element type if the specified field info
     * denotes a collection relationship. Otherwise it returns
     * <code>null</code>.
     */
    abstract public Object getElementType(Object fieldInfo);


    /**
     * Gets the name of the persistence-capable class which corresponds to the
     * specified typeInfo (assuming an ejb name). The method returs the type
     * name of the specified typeInfo, it the typeInfo does not denote an
     * ejb-name (e.g. a local or remote interface).
     */
    abstract public String getPCForTypeInfo(Object typeInfo);

    /**
     * Returns <code>true</code> if the specified type info denotes an ejb
     * name.
     */
    abstract public boolean isEjbName(Object typeInfo);

    /**
     * Returns <code>true</code> if the specified type info denotes an ejb name
     * or the name of a local interface or the name of a remote interface.
     */
    abstract public boolean isEjbOrInterfaceName(Object typeInfo);

    /**
     * Returns <code>true</code> if the specified type info denotes the remote
     * interface of the bean with the specified ejb name.
     */
    abstract public boolean isRemoteInterfaceOfEjb(Object typeInfo, String ejbName);

    /**
     * Returns <code>true</code> if the specified type info denotes the local
     * interface of the bean with the specified ejb name.
     */
    abstract public boolean isLocalInterfaceOfEjb(Object typeInfo, String ejbName);

    /**
     * Returns <code>true</code> if the specified type info denotes a remote
     * interface.
     */
    abstract public boolean isRemoteInterface(Object typeInfo);

    /**
     * Returns <code>true</code> if the specified type info denotes a local
     * interface.
     */
    abstract public boolean isLocalInterface(Object typeInfo);

    /**
     * Returns <code>true</code> if the bean with the specified ejb name has a
     * remote interface.
     */
    abstract public boolean hasRemoteInterface(Object typeInfo);

    /**
     * Returns <code>true</code> if the bean with the specified ejb name has a
     * local interface.
     */
    abstract public boolean hasLocalInterface(Object typeInfo);

    /**
     * Return return type for Sum function for a given type.
     * @param type is a number data type
     */
    public Object getSumReturnType(Object type) {
        if (isFloatingPointType(type)) {
            return doubleClassType;
        } else if (isNumericType(type) || isNumericWrapperType(type)) {
            return longClassType;
        } else {
            return type;
        }
    }

    /**
     * Return return type for Avg function for a given type.
     * @param type is a number data type
     */
    public Object getAvgReturnType(Object type) {
        if (isNumericType(type) || isNumericWrapperType(type)) {
            return doubleClassType;
        } else {
            return type;
        }
    }

    /**
     * Return return type for Min/Max function for a given type.
     * @param type is an orderable data type
     */
    public Object getMinMaxReturnType(Object type) {
        if (isFloatingPointType(type)) {
            return doubleClassType;
        } else if (isCharType(type)) {
            return characterClassType;
        } else if (isNumericType(type) || isNumericWrapperType(type)) {
            return longClassType;
        } else {
            return type;
        }
    }

    public boolean isMemberOf(int a, int b) {
        return a == b;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#getCommonOperandType(java.lang.Object, java.lang.Object)
     */
    public Object getCommonOperandType(Object left, Object right) {
        if (isNumberType(left) && isNumberType(right)) {
            boolean wrapper = false;

            // handle java.math.BigDecimal:
            if (bigDecimalType.equals(left)) {
                return left;
            }
            if (bigDecimalType.equals(right)) {
                return right;
            }

            // handle java.math.BigInteger
            if (bigIntegerType.equals(left)) {
                // if right is floating point return BigDecimal,
                // otherwise return BigInteger
                return isFloatingPointType(right) ?
                       bigDecimalType : left;
            }
            if (bigIntegerType.equals(right)) {
                // if left is floating point return BigDecimal,
                // otherwise return BigInteger
                return isFloatingPointType(left) ?
                       bigDecimalType : right;
            }

            if (isNumericWrapperType(left)) {
                left = getPrimitiveType(left);
                wrapper = true;
            }
            if (isNumericWrapperType(right)) {
                right = getPrimitiveType(right);
                wrapper = true;
            }

            // handle numeric types with arbitrary arithmetic operator
            if (isNumericType(left) && isNumericType(right)) {
                Object promotedType = binaryNumericPromotion(left, right);
                if (wrapper)
                    promotedType = getWrapperType(promotedType);
                return promotedType;
            }
        }
        else if (isBooleanType(left) && isBooleanType(right)) {
            // check for boolean wrapper class: if one of the operands has the
            // type Boolean return Boolean, otherwise return boolean.
            if (booleanClassType.equals(left) ||
                booleanClassType.equals(right))
                return booleanClassType;
            else
                return booleanType;
        }
        else if (isCompatibleWith(left, right)) {
            return right;
        }
        else if (isCompatibleWith(right, left)) {
            return left;
        }

        // not compatible types => return errorType
        return errorType;

    }
    
    /**
     * Represents a type which is "unknown".  Compatible with all other types.
     * 
     * @author Dave Bristor
     */
    static class UnknownType extends AbstractJavaType {
        /** The single UnknownType instance. */
        public static final UnknownType instance = new UnknownType();
        
        private  UnknownType() {
            // empty
        }
        
        /** 
         * @param javaType the type with which this UnknownType is compared.
         * @return <code>true</code>.
         */
        public boolean isCompatibleWith(JavaType javaType) {
            return true;
        }
        
        /** 
         * @return type name
         */
        public String getName() {
            return "<unknown type>";
        }
    }
}
