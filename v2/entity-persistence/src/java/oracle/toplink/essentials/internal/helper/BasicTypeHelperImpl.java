/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package oracle.toplink.essentials.internal.helper;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * INTERNAL
 * This class is a helper class providing type information.
 * Its implementation uses Java reflection to calculate the type information.
 */
public class BasicTypeHelperImpl {

    /** Set of intergral types and its wrapper classes. */
    private static Set integralTypes = new HashSet();
    /** Set of floating point types and its wrapper classes. */
    private static Set floatingPointTypes = new HashSet();
    /** Set of date classes. */
    private static Set dateClasses = new HashSet();
    /** Maps primtives types to their wrapper classes. */
    private static Map primitiveToWrapper = new HashMap();
    /** Maps wrapper classes to their primitive types. */
    private static Map wrapperToPrimitive = new HashMap();

    static {
        // Inilialize set of integral types plus their wrapper classes
        integralTypes.add(byte.class);
        integralTypes.add(Byte.class);
        integralTypes.add(short.class);
        integralTypes.add(Short.class);
        integralTypes.add(char.class);
        integralTypes.add(Character.class);
        integralTypes.add(int.class);
        integralTypes.add(Integer.class);
        integralTypes.add(long.class);
        integralTypes.add(Long.class);
        
        // Inilialize set of floating point types plus their wrapper classes
        floatingPointTypes.add(float.class);
        floatingPointTypes.add(Float.class);
        floatingPointTypes.add(double.class);
        floatingPointTypes.add(Double.class);

        // Inilialize set of floating point types plus their wrapper classes
        dateClasses.add(java.util.Date.class);
        dateClasses.add(java.util.Calendar.class);
        dateClasses.add(java.sql.Date.class);
        dateClasses.add(java.sql.Time.class);
        dateClasses.add(java.sql.Timestamp.class);

        // Inilialize mapping primitives to their wrapper classes
        primitiveToWrapper.put(boolean.class, Boolean.class);
        primitiveToWrapper.put(byte.class, Byte.class);
        primitiveToWrapper.put(short.class, Short.class);
        primitiveToWrapper.put(char.class, Character.class);
        primitiveToWrapper.put(int.class, Integer.class);
        primitiveToWrapper.put(long.class, Long.class);
        primitiveToWrapper.put(float.class, Float.class);
        primitiveToWrapper.put(double.class, Double.class);

        // Inilialize mapping wrapper classes to their primitives
        wrapperToPrimitive.put(Boolean.class, boolean.class);
        wrapperToPrimitive.put(Byte.class, byte.class);
        wrapperToPrimitive.put(Short.class, short.class);
        wrapperToPrimitive.put(Character.class, char.class);
        wrapperToPrimitive.put(Integer.class, int.class);
        wrapperToPrimitive.put(Long.class, long.class);
        wrapperToPrimitive.put(Float.class, float.class);
        wrapperToPrimitive.put(Double.class, double.class);
    }

    /** A singleton for this class */
    private static final BasicTypeHelperImpl singleton = new BasicTypeHelperImpl();

    /** Gets instance of this class */
    public static BasicTypeHelperImpl getInstance() {
        return singleton;
    }

    /** Returns the name of the specified type. */
    public String getTypeName(Object type) {
        Class clazz = getJavaClass(type);
        return (clazz == null) ? null : clazz.getName();
    }

    /** Returns the class object of the specified type. */
    public Class getJavaClass(Object type) {
        Class clazz = null;
        if (type instanceof Class) {
            clazz = (Class)type;
        } else if (type instanceof ClassDescriptor) {
            clazz = ((ClassDescriptor)type).getJavaClass();
        }
        return clazz;
    }

    /** Returns the Object type representation.*/
    public Object getObjectType() {
        return Object.class;
    }

    /** Returns the boolean type representation.*/
    public Object getBooleanType() {
        return boolean.class;
    }

    /** Returns the Boolean class representation.*/
    public Object getBooleanClassType() {
        return Boolean.class;
    }

    /** Returns the char type representation.*/
    public Object getCharType() {
        return char.class;
    }

    /** Returns the Character class representation.*/
    public Object getCharacterClassType() {
        return Character.class;
    }

    /** Returns the byte type representation.*/
    public Object getByteType() {
        return byte.class;
    }

    /** Returns the Byte class representation.*/
    public Object getByteClassType() {
        return Byte.class;
    }

    /** Returns the short type representation.*/
    public Object getShortType() {
        return short.class;
    }

    /** Returns the Short class representation.*/
    public Object getShortClassType() {
        return Short.class;
    }

    /** Returns the int type representation.*/
    public Object getIntType() {
        return int.class;
    }

    /** Returns the Inter class representation.*/
    public Object getIntegerClassType() {
        return Integer.class;
    }

    /** Returns the long type representation.*/
    public Object getLongType() {
        return long.class;
    }

    /** Returns the type representation of class Long.*/
    public Object getLongClassType()  {
        return Long.class;
    }

    /** Returns the float type representation.*/
    public Object getFloatType() {
        return float.class;
    }

    /** Returns the type representation of class Float.*/
    public Object getFloatClassType()  {
        return Float.class;
    }

    /** Returns the double type representation.*/
    public Object getDoubleType() {
        return double.class;
    }

    /** Returns the type representation of class Double.*/
    public Object getDoubleClassType()  {
        return Double.class;
    }

    /** Returns the String type representation.*/
    public Object getStringType() {
        return String.class;
    }

    /** Returns the BigInteger type representation.*/
    public Object getBigIntegerType() {
        return BigInteger.class;
    }

    /** Returns the BigDecimal type representation.*/
    public Object getBigDecimalType() {
        return BigDecimal.class;
    }

    /** Returns the java.util.Date type representation.*/
    public Object getDateType() {
        return Date.class;
    }

    /** */
    public boolean isEnumType(Object type) {
        Class clazz = getJavaClass(type);
        return (clazz != null) && (clazz.isEnum());
    }

    /** */
    public boolean isNumericType(Object type) {
        return isIntegralType(type) || isFloatingPointType(type) ||
            isBigIntegerType(type) || isBigDecimalType(type);
    }

    /**
     * Returns true if the specified type represents an
     * integral type or a wrapper class of an integral type. 
     */
    public boolean isIntegralType(Object type) {
        return integralTypes.contains(type);
    }
    
    /**
     * Returns true if the specified type represents an
     * floating point type or a wrapper class of an floating point type. 
     */
    public boolean isFloatingPointType(Object type) {
        return floatingPointTypes.contains(type);
    }

    /** Returns true if the specified type is a wrapper class. */
    public boolean isWrapperClass(Object type) {
        return wrapperToPrimitive.containsKey(type);
    }

    /**
     * Returns true if type is the boolean primitive type or the Boolean wrapper class
     */
    public boolean isBooleanType(Object type) {
        return (type == getBooleanType()) || (type == getBooleanClassType());
    }

    /**
     * Returns true if type is the char primitive type or the Character wrapper class
     */
    public boolean isCharacterType(Object type) {
        return (type == getCharType()) || (type == getCharacterClassType());
    }

    /**
     * Returns true if type is the byte primitive type or the Byte wrapper class
     */
    public boolean isByteType(Object type) {
        return (type == getByteType()) || (type == getByteClassType());
    }

    /**
     * Returns true if type is the short primitive type or the Short wrapper class
     */
    public boolean isShortType(Object type) {
        return (type == getShortType()) || (type == getShortClassType());
    }

    /**
     * Returns true if type is the int primitive type or the Integer wrapper class
     */
    public boolean isIntType(Object type) {
        return (type == getIntType()) || (type == getIntegerClassType());
    }

    /**
     * Returns true if type is the long primitive type or the Long wrapper class
     */
    public boolean isLongType(Object type) {
        return (type == getLongType()) || (type == getLongClassType());
    }

    /**
     * Returns true if type is the float primitive type or the Float wrapper class
     */
    public boolean isFloatType(Object type) {
        return (type == getFloatType()) || (type == getFloatClassType());
    }

    /**
     * Returns true if type is the double primitive type or the Double wrapper class
     */
    public boolean isDoubleType(Object type) {
        return (type == getDoubleType()) || (type == getDoubleClassType());
    }

    /** Returns true if the specified type represents java.lang.String. */
    public boolean isStringType(Object type) {
        return type == getStringType();
    }

    /** */
    public boolean isDateClass(Object type) {
        return dateClasses.contains(type);
    }

    /** */
    public boolean isBigIntegerType(Object type) {
        return type == getBigIntegerType();
    }

    /** */
    public boolean isBigDecimalType(Object type) {
        return type == getBigDecimalType();
    }

    /** Returns true if the specified type denotes an orable type */
    public boolean isOrderableType(Object type) {
        return isNumericType(type) || isStringType(type) || 
            isDateClass(type) || isEnumType(type);
    }

    /** */
    public boolean isAssignableFrom(Object left, Object right) {
        if ((left == null) || (right == null)) {
            return false;
        }
        // chec for identical types
        if (left == right) {
            return true;
        }
        // numeric types are compatible
        Object promoted = extendedBinaryNumericPromotion(left, right);
        if (promoted != null) {
            return true;
        }
        // date types are compatible
        if (isDateClass(left) && isDateClass(right))  {
            return true;
        }
        // handle boolean and Boolean
        if (isBooleanType(left) && isBooleanType(right)) {
            return true;
        }
        // check for inheritance and implements
        return getJavaClass(left).isAssignableFrom(getJavaClass(right));
    }

    /** Implements binary numeric promotion as defined in JLS extended by
     * wrapper classes, BigDecimal and BigInteger.  */
    public Object extendedBinaryNumericPromotion(Object left, Object right) {
        if ((left == null) || (right == null) || 
            !isNumericType(left) || !isNumericType(right)) {
            return null;
        }

        // handle BigDecimal
        if (isBigDecimalType(left) || isBigDecimalType(right)) {
            return getBigDecimalType();
        }
        
        // handle BigInteger
        if (isBigIntegerType(left)) {
            return isFloatingPointType(right) ? right : getBigIntegerType();
        }
        if (isBigIntegerType(right)) {
            return isFloatingPointType(left) ? left : getBigIntegerType();
        }

        // check wrapper classes
        boolean wrapper = false;
        if (isWrapperClass(left)) {
            wrapper = true;
            left = getPrimitiveType(left);
        }
        if (isWrapperClass(right)) {
            wrapper = true;
            right = getPrimitiveType(right);
        }
        
        Object promoted = binaryNumericPromotion(left, right);
        if (wrapper && promoted != null) {
            promoted = getWrapperClass(promoted);
        }
        return promoted;
    }
    
    // Helper methods

    /** Returns the primitive for the specified wrapper class. */
    protected Object getPrimitiveType(Object wrapper) {
        return wrapperToPrimitive.get(wrapper);
    }
    
    /** Returns the wrapper class for the specified primitive. */
    protected Object getWrapperClass(Object primitive) {
        return primitiveToWrapper.get(primitive);
    }

    /** Implements binary numeric promotion as defined in JLS. */
    protected Object binaryNumericPromotion(Object left, Object right) {
        if ((left == null) || (right == null)) {
            return null;
        }
        Object type = null;
        
        if (left == getDoubleType() || right == getDoubleType()) {
            type = getDoubleType();
        } else if (left == getFloatType() || right == getFloatType()) {
            type = getFloatType();
        } else if (left == getLongType() || right == getLongType()) {
            type = getLongType();
        } else if (isIntegralType(left) && isIntegralType(right)) {
            type = getIntType();
        }
        return type;
    }

}

