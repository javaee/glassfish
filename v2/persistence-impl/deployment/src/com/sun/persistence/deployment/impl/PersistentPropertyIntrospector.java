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



package com.sun.persistence.deployment.impl;

import com.sun.persistence.api.deployment.JavaModel;
import com.sun.persistence.api.deployment.AccessType;

/**
 * JavaModel does not know which field or property is persistent capable.
 * Instead an implementation of this interface has that intelligence.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface PersistentPropertyIntrospector {
    /**
     * Return the persistence capable properties of this type. Follows rules
     * specified in section #2.1.1 of EJB 3.0 Persistence-API spec version
     * #EDR2.
     *
     * @param javaType   is the Java type whose properties will be returned
     * @param accessType whether fields or methods will be returned.
     * @return returns a list of {@link com.sun.persistence.api.deployment.JavaModel.FieldOrProperty}
     * @throws Exception if typeName could not be found in JavaModel.
     */
    JavaModel.FieldOrProperty[] getPCProperties(
            Object javaType,
            AccessType accessType)
            throws Exception;

    /**
     * Return the persistence capable property with given name in this type.
     *
     * @param javaType   is the Java Type whose property will be returned
     * @param accessType whether fields or methods will be returned.
     * @return returns a list of {@link com.sun.persistence.api.deployment.JavaModel.FieldOrProperty}
     * @throws Exception if typeName could not be found in JavaModel.
     * @see #getPCProperties(Object, AccessType)
     */
    JavaModel.FieldOrProperty getPCProperty(
            Object javaType,
            AccessType accessType,
            String fieldOrPropertyName)
            throws Exception;
}
