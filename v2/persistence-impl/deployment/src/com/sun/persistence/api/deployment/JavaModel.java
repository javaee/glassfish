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



package com.sun.persistence.api.deployment;

/**
 * Since deployment module can be used both in deployment as well as development
 * environemnt, instead of directly using reflection, we use this class.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface JavaModel {
    /*
     * TODO Replace this by com.sun.org.apache.model.java.JavaModel
     */

    /**
     * This interface represents either a field or property(i.e. method) of a
     * JavaType.
     */
    interface FieldOrProperty {
        /**
         * @return true iff this is a field
         */
        boolean isField();

        /**
         * @return true iff this is a method
         */
        boolean isMethod();

        /**
         * @return the name of this property. Applies JavaBean rules for
         *         naming.
         */
        String getName();

        /**
         * @return the java type for a field, else java type for the return for
         *         a method
         */
        Object getJavaType();

        /**
         * Return the underlying object. In a reflection based model, for a
         * field type, this returns java.lang.reflect.Field, and for a property
         * type, this returns java.lang.reflect.Method corresponding to the
         * getter method of the property.
         *
         * @return the underlying object for this field or property.
         */
        Object getUnderlyingObject();
    }

    /**
     * Return all the properties of this type.
     *
     * @param typeName   is the name of the type
     * @param accessType whether fields or methods will be returned.
     * @return returns a list of {@link FieldOrProperty}
     * @throws Exception if typeName could not be found in JavaModel.
     */
    FieldOrProperty[] getProperties(String typeName, AccessType accessType)
            throws Exception;

    /**
     * Return the property with given name in this type.
     *
     * @param typeName   is the name of the type
     * @param accessType whether fields or methods will be returned.
     * @return returns a list of {@link FieldOrProperty}
     * @throws Exception if typeName could not be found in JavaModel.
     * @see #getProperties(String, AccessType)
     */
    FieldOrProperty getProperty(
            String typeName, AccessType accessType,
            String propertyName) throws Exception;

    /**
     * Return the JavaType with this name.
     *
     * @param name name of the JavaType
     * @return the JavaType. This can be used arguments to other methods in this
     *         class.
     * @throws Exception if there is no such type.
     */
    Object getJavaType(String name) throws Exception;

    /**
     * Return the fully qualified name of this java type.
     *
     * @param javaType the type whose name is being asked for
     * @return the name of the type. It can be passed to {@link
     *         #getJavaType(String)} method.
     */
    String getName(Object javaType);

    /**
     * Return the component type of a collection valued property which is
     * declared using using generics. e.g. for a getter method with return type
     * as {@code java.util.Set<Employee>} this method returns the JavaType
     * corresponding to Employee. For a field of type {@code
     * java.util.Map<String, Customer>}, it returns JavaType corresponding to
     * Customer.
     *
     * @param property
     * @return the JavaType correposnding to the component type of this
     *         collection.
     * @throws Exception if it encounters errors during processing, like type
     *                   not found in JavaModel.
     */
    Object getCollectionComponentType(FieldOrProperty property)
            throws Exception;

    /**
     * Evaluates if the given type is a basic type. As per section #5.1.14 of
     * EJB 3.0 EDR #2 Persistence-API spec, following types are basic types...
     * Java primitive types; java.lang.String; wrappers of the primitive types;
     * byte[], Byte[], char[], Character[]; java.math.BigInteger,
     * java.math.BigDecimal, java.util.Date, java.util.Calendar, java.sql.Date,
     * java.sql.Time, java.sql.Timestamp.
     *
     * @param javaType which will be tested
     * @return true if this is a basic type.
     */
    boolean isBasic(Object javaType);

    /**
     * This is equivalent to {@link Class#isAssignableFrom(Class)}
     *
     * @param lhsType
     * @param rhsType
     * @return return true if instance of type rhsType can be assigned to
     *         instance of type lhsType.
     */
    boolean isAssignable(Object lhsType, Object rhsType);
}
