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


package com.sun.persistence.deployment.impl.reflection;

import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.api.deployment.AccessType;
import com.sun.persistence.api.deployment.JavaModel;
import com.sun.persistence.deployment.impl.LogHelperDeployment;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * This is an implementation of JavaModel using reflection. This is used during
 * deployment process. This class uses covariant return type in overriding
 * method signatures. It returns {@link java.lang.Class} as the JavaType where
 * as {@link JavaModel} returns Object as the JavaType.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class JavaModelImpl implements JavaModel {

    /**
     * Set of type names where their instances are handled as simple types. This
     * includes wrapper classes, java.math types, java.lang.Locale,
     * java.lang.String, java.util.Date, etc. See {@link #isBasic(Object)}
     */
    private static Set<String> basicClasses = new HashSet<String>();

    /* The class loader used to load classes. */
    private ClassLoader cl;

    private static final I18NHelper i18NHelper = I18NHelper.getInstance(
            LogHelperDeployment.class);

    /** Initializes basicClasses. */
    static {
        basicClasses.add("java.lang.Boolean"); // NOI18N
        basicClasses.add("java.lang.Byte"); // NOI18N
        basicClasses.add("java.lang.Short"); // NOI18N
        basicClasses.add("java.lang.Integer"); // NOI18N
        basicClasses.add("java.lang.Long"); // NOI18N
        basicClasses.add("java.lang.Character"); // NOI18N
        basicClasses.add("java.lang.Float"); // NOI18N
        basicClasses.add("java.lang.Double"); // NOI18N
        basicClasses.add("java.lang.String"); // NOI18N
        basicClasses.add("java.lang.Number"); // NOI18N
        basicClasses.add("java.math.BigDecimal"); // NOI18N
        basicClasses.add("java.math.BigInteger"); // NOI18N
        basicClasses.add("java.sql.Date"); // NOI18N
        basicClasses.add("java.util.Date"); // NOI18N
        basicClasses.add("java.util.Calendar"); // NOI18N
        basicClasses.add("java.sql.Time"); // NOI18N
        basicClasses.add("java.sql.TimeStamp"); // NOI18N
        basicClasses.add((new byte[0]).getClass().getName());
        basicClasses.add((new Byte[0]).getClass().getName());
        basicClasses.add((new char[0]).getClass().getName());
        basicClasses.add((new Character[0]).getClass().getName());
    }

    public JavaModelImpl(ClassLoader cl) {
        this.cl = cl;
    }

    public FieldOrPropertyImpl[] getProperties(
            String typeName,
            AccessType accessType)
            throws Exception {
        return new Introspector(getJavaType(typeName)).getProperties(
                accessType);
    }

    public FieldOrPropertyImpl getProperty(
            String typeName,
            AccessType accessType,
            String propertyName)
            throws Exception {
        return new Introspector(getJavaType(typeName)).getProperty(
                propertyName,
                accessType);
    }

    public Class getJavaType(String typeName) throws ClassNotFoundException {
        return Class.forName(typeName, false, cl);
    }

    public String getName(Object javaType) {
        return Class.class.cast(javaType).getName();
    }

    /**
     * {@inheritDoc}
     *
     * @throws ClassCastException            if the property passed to this
     *                                       method is not of type {@link
     *                                       FieldOrPropertyImpl} by an earlier
     *                                       call to this Ja
     * @throws ClassNotFoundException        if there is no JavaType corresponding
     *                                       to this component type.
     * @throws UnsupportedOperationException if fieldOrProperty is of type Map.
     */
    public Class getCollectionComponentType(FieldOrProperty property)
            throws ClassNotFoundException, ClassCastException {
        FieldOrPropertyImpl propertyImpl = FieldOrPropertyImpl.class.cast(
                property);
        if (java.util.Map.class.isAssignableFrom(propertyImpl.getJavaType())) {
            throw new UnsupportedOperationException(i18NHelper.msg(
                    "EXC_JavaModelImpl_MapNotYetSupported", // NOI18N
                    propertyImpl.getName()));
        }
        Type type = propertyImpl.getGenericType();
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = ParameterizedType.class.cast(type);
            assert(pt.getActualTypeArguments().length == 1);
            assert(pt.getActualTypeArguments()[0] instanceof Class);
            Class actualTypeArgument = Class.class.cast(
                    pt.getActualTypeArguments()[0]);
            return getJavaType(actualTypeArgument.getName());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ClassCastException if javaType is not of type java.lang.Class.
     */
    public boolean isBasic(Object javaType) throws ClassCastException {
        Class clazz = Class.class.cast(javaType);
        return (clazz.isPrimitive()) || basicClasses.contains(clazz.getName());
    }

    /**
     * {@inheritDoc}
     *
     * @throws ClassCastException if javaType is not of type java.lang.Class.
     */
    public boolean isAssignable(Object lhsType, Object rhsType) {
        return Class.class.cast(lhsType).isAssignableFrom(
                Class.class.cast(rhsType));
    }

}
