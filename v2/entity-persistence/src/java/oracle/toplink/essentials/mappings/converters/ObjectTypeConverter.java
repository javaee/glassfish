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

import java.util.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.essentials.sessions.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <b>Purpose</b>: Object type converter is used to match a fixed number of database data values
 * to Java object value. It can be used when the values on the database and in the Java differ.
 * To create an object type converter, simply specify the set of conversion value pairs.
 * A default value and one-way conversion are also supported for legacy data situations.
 *
 * @author James Sutherland
 * @since Toplink 10
 */
public class ObjectTypeConverter implements Converter {
    protected DatabaseMapping mapping;
    protected transient Map fieldToAttributeValues;
    protected Map attributeToFieldValues;
    protected transient Object defaultAttributeValue;
    protected transient Class fieldClassification;
    protected transient String fieldClassificationName;

    /**
     * PUBLIC:
     * Default constructor.
     */
    public ObjectTypeConverter() {
        this.attributeToFieldValues = new HashMap(10);
        this.fieldToAttributeValues = new HashMap(10);
    }

    /**
     * PUBLIC:
     * Default constructor.
     */
    public ObjectTypeConverter(DatabaseMapping mapping) {
        this();
        this.mapping = mapping;
    }

    /**
     * PUBLIC:
     * A type conversion value is a two-way mapping from the database to the object.
     * The database value will be substituted for the object value when read,
     * and the object value will be substituted for database value when written.
     * Note that each field/attribute value must have one and only one attribute/field value to maintain a two-way mapping.
     */
    public void addConversionValue(Object fieldValue, Object attributeValue) {
        if (fieldValue == null) {
            fieldValue = Helper.getNullWrapper();
        }

        if (attributeValue == null) {
            attributeValue = Helper.getNullWrapper();
        }

        getFieldToAttributeValues().put(fieldValue, attributeValue);
        getAttributeToFieldValues().put(attributeValue, fieldValue);
    }

    /**
     * PUBLIC:
     * An attribute only conversion value is a one-way mapping from the database to the object.
     * This can be used if multiple database values are desired to be mapped to the same object value.
     * Note that when written only the default value will be used for the attribute, not this value.
     */
    public void addToAttributeOnlyConversionValue(Object fieldValue, Object attributeValue) {
        if (fieldValue == null) {
            fieldValue = Helper.getNullWrapper();
        }

        if (attributeValue == null) {
            attributeValue = Helper.getNullWrapper();
        }

        getFieldToAttributeValues().put(fieldValue, attributeValue);
    }

    /**
     * INTERNAL:
     * Get the attribute to field mapping.
     */
    public Map getAttributeToFieldValues() {
        return attributeToFieldValues;
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this converter to actual 
     * class-based settings. This method is used when converting a project 
     * that has been built with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        // Does nothing right now but was implemented since EnumTypeConverter
        // is dependent on this method but we need to avoid JDK 1.5 
        // dependencies. AbstractDirectMapping will call this method.
    }
    
    /**
     * INTERNAL:
     * Returns the corresponding attribute value for the specified field value.
     */
    public Object convertDataValueToObjectValue(Object fieldValue, Session session) {
        Object attributeValue = null;

        if (fieldValue == null) {
            attributeValue = getFieldToAttributeValues().get(Helper.getNullWrapper());
        } else {
            try {
                fieldValue = ((AbstractSession)session).getDatasourcePlatform().getConversionManager().convertObject(fieldValue, getFieldClassification());
            } catch (ConversionException e) {
                throw ConversionException.couldNotBeConverted(mapping, mapping.getDescriptor(), e);
            }

            attributeValue = getFieldToAttributeValues().get(fieldValue);
            if (attributeValue == null) {
                if (getDefaultAttributeValue() != null) {
                    attributeValue = getDefaultAttributeValue();
                } else {
                    // CR#3779
                    throw DescriptorException.noFieldValueConversionToAttributeValueProvided(fieldValue, getMapping().getField(), getMapping());
                }
            }
        }
        return attributeValue;
    }

    /**
     * PUBLIC:
     * The default value can be used if the database can possibly store additional values then those that
     * have been mapped.  Any value retreived from the database that is not mapped will be substitued for the default value.
     */
    public Object getDefaultAttributeValue() {
        return defaultAttributeValue;
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
     * Set the mapping.
     */
    protected void setMapping(DatabaseMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * INTERNAL:
     * Get the type of the field value to allow conversion from the database.
     */
    public Class getFieldClassification() {
        return fieldClassification;
    }

    public String getFieldClassificationName() {
        if ((fieldClassificationName == null) && (fieldClassification != null)) {
            fieldClassificationName = fieldClassification.getName();
        }
        return fieldClassificationName;
    }

    /**
     * INTERNAL:
     * Return the classifiction for the field contained in the mapping.
     * This is used to convert the row value to a consistent java value.
     * By default this is null which means unknown.
     */
    public Class getFieldClassification(DatabaseField fieldToClassify) {
        return getFieldClassification();
    }

    /**
     * INTERNAL:
     * Get the field to attribute mapping.
     */
    public Map getFieldToAttributeValues() {
        return fieldToAttributeValues;
    }

    /**
     *  INTERNAL:
     *  Convert to the data value.
     */
    public Object convertObjectValueToDataValue(Object attributeValue, Session session) {
        Object fieldValue;
        if (attributeValue == null) {
            fieldValue = getAttributeToFieldValues().get(Helper.getNullWrapper());
        } else {
            fieldValue = getAttributeToFieldValues().get(attributeValue);
            if (fieldValue == null) {
                throw DescriptorException.noAttributeValueConversionToFieldValueProvided(attributeValue, getMapping());
            }
        }
        return fieldValue;
    }

    /**
     * INTERNAL:
     */
    public boolean isObjectTypeMapping() {
        return true;
    }

    /**
     * PUBLIC:
     * This is a very specific protocol which maps fieldValues "T" and "F"
     * to true and false respectively.
     */
    public void mapBooleans() {
        addConversionValue("F", new Boolean(false));
        addConversionValue("T", new Boolean(true));
    }

    /**
     * PUBLIC:
     * This is a very specific protocol which maps fieldValues "F" and "M"
     * to "Female" and "Male" respectively.
     */
    public void mapGenders() {
        addConversionValue("F", "Female");
        addConversionValue("M", "Male");
    }

    /**
     * PUBLIC:
     * This is a very specific protocol which maps fieldValues "Y" and "N"
     * to "Yes" and "No" respectively.
     */
    public void mapResponses() {
        addConversionValue("Y", "Yes");
        addConversionValue("N", "No");
    }

    /**
     * INTERNAL:
     * Set the field classification through searching the fields hashtable.
     */
    public void initializeFieldClassification(Session session) throws DescriptorException {
        if (getFieldToAttributeValues().isEmpty()) {
            return;
        }
        Class type = null;
        Iterator fieldValuesEnum = getFieldToAttributeValues().keySet().iterator();
        while (fieldValuesEnum.hasNext() && (type == null)) {
            Object value = fieldValuesEnum.next();
            if (value != Helper.getNullWrapper()) {
                type = value.getClass();
            }
        }

        setFieldClassification(type);
        // CR#... Mapping must also have the field classification.
        if (getMapping().isDirectToFieldMapping()) {
            AbstractDirectMapping directMapping = (AbstractDirectMapping)getMapping();

            // Allow user to specify field type to override computed value. (i.e. blob, nchar)
            if (directMapping.getFieldClassification() == null) {
                directMapping.setFieldClassification(type);
            }
        }
    }

    /**
     * INTERNAL:
     * Set the mapping.
     */
    public void initialize(DatabaseMapping mapping, Session session) {
        this.mapping = mapping;
        initializeFieldClassification(session);
    }

    /**
     * INTERNAL:
     * Set the attribute to field mapping.
     */
    public void setAttributeToFieldValues(Map attributeToFieldValues) {
        this.attributeToFieldValues = attributeToFieldValues;
    }

    /**
     * PUBLIC:
     * The default value can be used if the database can possibly store additional values then those that
     * have been mapped.  Any value retreived from the database that is not mapped will be substitued for the default value.
     */
    public void setDefaultAttributeValue(Object defaultAttributeValue) {
        this.defaultAttributeValue = defaultAttributeValue;
    }

    /**
     * INTERNAL:
     * Set the type of the field value to allow conversion from the database.
     */
    public void setFieldClassification(Class fieldClassification) {
        this.fieldClassification = fieldClassification;
    }

    public void setFieldClassificationName(String fieldClassificationName) {
        this.fieldClassificationName = fieldClassificationName;
    }

    /**
     * INTERNAL:
     * Set a collection of the field to attribute value associations.
     */
    public void setFieldToAttributeValueAssociations(Vector fieldToAttributeValueAssociations) {
        setFieldToAttributeValues(new Hashtable(fieldToAttributeValueAssociations.size() + 1));
        setAttributeToFieldValues(new Hashtable(fieldToAttributeValueAssociations.size() + 1));
        for (Enumeration associationsEnum = fieldToAttributeValueAssociations.elements();
                 associationsEnum.hasMoreElements();) {
            Association association = (Association)associationsEnum.nextElement();
            addConversionValue(association.getKey(), association.getValue());
        }
    }

    /**
     * INTERNAL:
     * Set the field to attribute mapping.
     */
    public void setFieldToAttributeValues(Map fieldToAttributeValues) {
        this.fieldToAttributeValues = fieldToAttributeValues;
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
