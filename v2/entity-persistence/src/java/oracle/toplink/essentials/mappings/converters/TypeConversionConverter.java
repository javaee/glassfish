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
package oracle.toplink.essentials.mappings.converters;

import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.essentials.sessions.*;
import oracle.toplink.essentials.exceptions.ConversionException;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <b>Purpose</b>: Type conversion converters are used to explicitly map a database type to a
 * Java type.
 *
 * @author James Sutherland
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class TypeConversionConverter implements Converter {
    protected DatabaseMapping mapping;

    /** Field type */
    protected Class dataClass;
    protected String dataClassName;

    /** Object type */
    protected Class objectClass;
    protected String objectClassName;

    /**
     * PUBLIC:
     * Default constructor.
     */
    public TypeConversionConverter() {
    }

    /**
     * PUBLIC:
     * Default constructor.
     */
    public TypeConversionConverter(DatabaseMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this converter to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * This method is implemented by subclasses as necessary.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        Class dataClass = null;
        Class objectClass = null;
        try{
            if (dataClassName != null){
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        dataClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(dataClassName, true, classLoader));
                    } catch (PrivilegedActionException exception) {
                        throw ValidationException.classNotFoundWhileConvertingClassNames(dataClassName, exception.getException());
                    }
                } else {
                    dataClass = oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(dataClassName, true, classLoader);
                }
                setDataClass(dataClass);
            }
        } catch (ClassNotFoundException exc){
            throw ValidationException.classNotFoundWhileConvertingClassNames(dataClassName, exc);
        }
        try {
            if (objectClassName != null){
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        objectClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(objectClassName, true, classLoader));
                    } catch (PrivilegedActionException exception) {
                        throw ValidationException.classNotFoundWhileConvertingClassNames(objectClassName, exception.getException());
                    }
                } else {
                    objectClass = oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(objectClassName, true, classLoader);
                }
                setObjectClass(objectClass);
            }
        } catch (ClassNotFoundException exc){
            throw ValidationException.classNotFoundWhileConvertingClassNames(objectClassName, exc);
        }
    };

    /**
     * INTERNAL:
     * The field value must first be converted to the field type, then the attribute type.
     */
    public Object convertDataValueToObjectValue(Object fieldValue, Session session) {
        Object attributeValue = fieldValue;
        if (attributeValue != null) {
            try {
                attributeValue = ((AbstractSession)session).getDatasourcePlatform().convertObject(attributeValue, getDataClass());
            } catch (ConversionException e) {
                throw ConversionException.couldNotBeConverted(mapping, mapping.getDescriptor(), e);
            }

            try {
                attributeValue = ((AbstractSession)session).getDatasourcePlatform().convertObject(attributeValue, getObjectClass());
            } catch (ConversionException e) {
                throw ConversionException.couldNotBeConverted(mapping, mapping.getDescriptor(), e);
            }
        }

        return attributeValue;
    }

    /**
     * PUBLIC:
     * Returns the class type of the object value.
     */
    public Class getObjectClass() {
        return objectClass;
    }

    /**
     * INTERNAL:
     * Return the name of the object type for the MW usage.
     */
    public String getObjectClassName() {
        if ((objectClassName == null) && (objectClass != null)) {
            objectClassName = objectClass.getName();
        }
        return objectClassName;
    }

    /**
     * PUBLIC:
     * Returns the class type of the data value.
     */
    public Class getDataClass() {
        return dataClass;
    }

    /**
     * INTERNAL:
     * Return the name of the data type for the MW usage.
     */
    public String getDataClassName() {
        if ((dataClassName == null) && (dataClass != null)) {
            dataClassName = dataClass.getName();
        }
        return dataClassName;
    }

    /**
     * PUBLIC:
     * Set the class type of the data value.
     */
    public void setDataClass(Class dataClass) {
        this.dataClass = dataClass;
    }

    /**
     * INTERNAL:
     * Set the name of the data type for the MW usage.
     */
    public void setDataClassName(String dataClassName) {
        this.dataClassName = dataClassName;
    }

    /**
     * PUBLIC:
     * Set the class type of the object value.
     */
    public void setObjectClass(Class objectClass) {
        this.objectClass = objectClass;
    }

    /**
     * INTERNAL:
     * Set the name of the object type for the MW usage.
     */
    public void setObjectClassName(String objectClassName) {
        this.objectClassName = objectClassName;
    }

    /**
     *  INTERNAL:
     *  Convert to the field class.
     */
    public Object convertObjectValueToDataValue(Object attributeValue, Session session) {
        try {
            return ((AbstractSession)session).getDatasourcePlatform().convertObject(attributeValue, getDataClass());
        } catch (ConversionException e) {
            throw ConversionException.couldNotBeConverted(mapping, mapping.getDescriptor(), e);
        }
    }

    /**
     * INTERNAL:
     * Set the mapping.
     */
    public void initialize(DatabaseMapping mapping, Session session) {
        this.mapping = mapping;
        // CR#... Mapping must also have the field classification.
        if (getMapping().isDirectToFieldMapping()) {
            AbstractDirectMapping directMapping = (AbstractDirectMapping)getMapping();

            // Allow user to specify field type to override computed value. (i.e. blob, nchar)
            if (directMapping.getFieldClassification() == null) {
                directMapping.setFieldClassification(getDataClass());
            }
            
            // Set the object class from the attribute, if null.
            if (getObjectClass() == null) {
                setObjectClass(directMapping.getAttributeClassification());
            }
        }
    }

    /**
     * INTERNAL:
     * Return the mapping.
     */
    protected DatabaseMapping getMapping() {
        return mapping;
    }

    /**
     * INTERNAL:
     * If the converter converts the value to a non-atomic value, i.e.
     * a value that can have its' parts changed without being replaced,
     * then it must return false, serialization can be non-atomic.
     */
    public boolean isMutable() {
        return false;
    }
}
