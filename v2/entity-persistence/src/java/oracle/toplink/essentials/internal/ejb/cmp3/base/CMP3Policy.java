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
package oracle.toplink.essentials.internal.ejb.cmp3.base;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import oracle.toplink.essentials.descriptors.*;
import oracle.toplink.essentials.internal.helper.ConversionManager;
import oracle.toplink.essentials.internal.localization.ExceptionLocalization;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.internal.descriptors.ObjectBuilder;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedGetField;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;
import oracle.toplink.essentials.internal.security.PrivilegedGetMethod;
import oracle.toplink.essentials.internal.security.PrivilegedGetValueFromField;
import oracle.toplink.essentials.internal.security.PrivilegedSetValueInField;
import oracle.toplink.essentials.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataHelper;

/**
 * <b>Description</b>: Defines primary key extraction coce,
 * and differentiates CMP3 from CMP1/2.
 *
 * @since TopLink 10.1.3
 */

// This should be refactored to have an abstract CMPPolicy, and have CMP1/2/3 subclass and define correct functionality.
public class CMP3Policy extends CMPPolicy {

    /** Stores the fields for this classes compound primary key class if required. */
    protected KeyElementAccessor[] keyClassFields;
    
    // Store the primary key class name
    protected String pkClassName;
    
    // Stores the class version of the PKClass
    protected Class pkClass;

    public CMP3Policy() {
        super();
    }
    
    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this object to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        if(getPKClassName() != null){
            try{
                Class pkClass = null;
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        pkClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(getPKClassName(), true, classLoader));
                    } catch (PrivilegedActionException exception) {
                        throw new IllegalArgumentException(ExceptionLocalization.buildMessage("pk_class_not_found", new Object[] {this.pkClassName}), exception.getException());
                        
                    }
                } else {
                    pkClass = oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(getPKClassName(), true, classLoader);
                }
                setPKClass(pkClass);
            } catch (ClassNotFoundException exc){
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("pk_class_not_found", new Object[] {this.pkClassName}), exc);
            }
        }
    }

    /**
     * INTERNAL:
     * Return if this policy is for CMP3.
     */
    public boolean isCMP3Policy() {
        return true;
    }
    
    /**
     * INTERNAL:
     */
    public void setPrimaryKeyClassName(String pkClassName) {
        this.pkClassName = pkClassName;
    }
    
    /**
     * INTERNAL:
     */
    public Class getPKClass() {
        if(this.pkClass == null && getPKClassName() == null) {
            initializePrimaryKeyFields(null);
        }
        return this.pkClass;
    }

    /**
     * ADVANCED:
     */
    public void setPKClass(Class pkClass) {
        this.pkClass = pkClass;
    }

    /**
     * INTERNAL:
     */
    public String getPKClassName() {
        return pkClassName;
    }
    
    /**
     * INTERNAL:
     */
    public Object getPKClassInstance() {
        try {
            return getPKClass().newInstance();
        } catch (Exception ex) {
            return null;
            // WIP - this should throw an exception
        }
    }
    
    /**
     * INTERNAL:
     * Use the key to create a TopLink primary key Vector.
     * If the key is simple (direct mapped) then just add it to a vector,
     * otherwise must go through the inefficient process of copying the key into the bean
     * and extracting the key from the bean.
     *
     * @param key Object the primary key to use for creating the vector
     * @return Vector
     */
    public Vector createPkVectorFromKey(Object key, AbstractSession session) {
        // If the descriptor primary key is mapped through direct-to-field mappings,
        // then no elaborate conversion is required.
        // If key is compound, add each value to the vector.
        KeyElementAccessor[] pkElementArray = this.getKeyClassFields(key.getClass());
        Vector pkVector = new Vector(pkElementArray.length);
        for (int index = 0; index < pkElementArray.length; index++) {
            DatabaseMapping mapping = this.getDescriptor().getObjectBuilder().getMappingForAttributeName(pkElementArray[index].getAttributeName());
            if (mapping == null) {
                mapping = this.getDescriptor().getObjectBuilder().getMappingForField(pkElementArray[index].getDatabaseField());
            }
            while (mapping.isAggregateObjectMapping()) {
                mapping = mapping.getReferenceDescriptor().getObjectBuilder().getMappingForAttributeName(pkElementArray[index].getAttributeName());
                if (mapping == null) {// must be aggregate
                    mapping = this.getDescriptor().getObjectBuilder().getMappingForField(pkElementArray[index].getDatabaseField());
                }
            }
            Object fieldValue = null;
            if (mapping.isDirectToFieldMapping()) {
                fieldValue = ((AbstractDirectMapping)mapping).getFieldValue(pkElementArray[index].getValue(key), (oracle.toplink.essentials.internal.sessions.AbstractSession)session);
            } else {
                fieldValue = pkElementArray[index].getValue(key);
            }
            pkVector.add(fieldValue);
        }
        return pkVector;
    }
    
    /**
     * INTERNAL:
     * Create an instance of the composite primary key class for the key object.
     */
    public Object createPrimaryKeyInstance(Object key, AbstractSession session) {
        Object keyInstance = getPKClassInstance();
        ObjectBuilder builder = getDescriptor().getObjectBuilder();
        KeyElementAccessor[] pkElementArray = this.getKeyClassFields(getPKClass());
                
        for (int index = 0; index < pkElementArray.length; index++) {
            KeyElementAccessor accessor = pkElementArray[index];
            DatabaseMapping mapping = builder.getMappingForAttributeName(accessor.getAttributeName());
            // With session validation, the mapping shouldn't be null at this 
            // point, don't bother checking.
            
            while (mapping.isAggregateObjectMapping()) {
                mapping = mapping.getReferenceDescriptor().getObjectBuilder().getMappingForAttributeName(pkElementArray[index].getAttributeName());
            
                if (mapping == null) { // must be aggregate
                    mapping = builder.getMappingForField(accessor.getDatabaseField());
                }
            }
            
            Object fieldValue = mapping.getRealAttributeValueFromObject(key, (oracle.toplink.essentials.internal.sessions.AbstractSession) session);
            accessor.setValue(keyInstance, fieldValue);
        }
        
        return keyInstance;
    }
    

    /**
     * INTERNAL:
     * Use the key to create a bean and initialize its primary key fields.
     * Note: If is a compound PK then a primary key object is being used.
     * This method should only be used for 'templates' when executing
     * queries.  The bean built will not be given an EntityContext and should
     * not be used as an actual entity bean.
     *
     * @param key Object the primary key to use for initializing the bean's
     *            corresponding pk fields
     * @return TopLinkCmpEntity
     */
    protected Object createBeanUsingKey(Object key, AbstractSession session) {
        try {
            Object bean = this.getDescriptor().getInstantiationPolicy().buildNewInstance();
            KeyElementAccessor[] keyElements = this.getKeyClassFields(key.getClass());
            for (int index = 0; index < keyElements.length; ++index) {
                Object toWriteInto = bean;
                Object keyFieldValue = keyElements[index].getValue(key);
                DatabaseField field = keyElements[index].getDatabaseField();
                DatabaseMapping mapping = this.getDescriptor().getObjectBuilder().getMappingForAttributeName(keyElements[index].getAttributeName());
                if (mapping == null) {// must be aggregate
                    mapping = this.getDescriptor().getObjectBuilder().getMappingForField(field);
                }
                while (mapping.isAggregateObjectMapping()) {
                    Object aggregate = mapping.getRealAttributeValueFromObject(toWriteInto, session);
                    if (aggregate == null) {
                        aggregate = mapping.getReferenceDescriptor().getJavaClass().newInstance();
                        mapping.setRealAttributeValueInObject(toWriteInto, aggregate);
                    }
                    mapping = mapping.getReferenceDescriptor().getObjectBuilder().getMappingForAttributeName(keyElements[index].getAttributeName());
                    if (mapping == null) {// must be aggregate
                        mapping = this.getDescriptor().getObjectBuilder().getMappingForField(field);
                    }

                    //change the object to write into to the aggregate for the next stage of the 
                    // loop or for when we exit the loop.
                    toWriteInto = aggregate;
                }
                mapping.setRealAttributeValueInObject(toWriteInto, keyFieldValue);
            }
            return bean;
        } catch (Exception e) {
            throw DescriptorException.errorUsingPrimaryKey(key, this.getDescriptor(), e);
        }
    }

    /**
     * INTERNAL:
     * Cache the bean's primary key fields so speed up creating of primary key
     * objects and initialization of beans.
     *
     * Note, we have to re-look up the fields for the bean class since
     * these fields may have been loaded with the wrong loader (thank you Kirk).
     * If the key is compound, we also have to look up the fields for the key.
     */
    protected KeyElementAccessor[] initializePrimaryKeyFields(Class keyClass) {
        KeyElementAccessor[] pkAttributes = null;
        ClassDescriptor descriptor = this.getDescriptor();

        pkAttributes = new KeyElementAccessor[descriptor.getObjectBuilder().getPrimaryKeyMappings().size()];

        Iterator attributesIter = descriptor.getPrimaryKeyFields().iterator();

        // Used fields in case it is an embedded class
        for (int i = 0; attributesIter.hasNext(); i++) {
            DatabaseField field = (DatabaseField)attributesIter.next();

            // This next section looks strange but we need to check all mappings
            // for this field, not just the writable one and instead of having 
            // multiple sections of duplicate code I will just add the writable 
            // mapping to the list.
            Vector allMappings = descriptor.getObjectBuilder().getReadOnlyMappingsForField(field);
            if (allMappings == null) {
                allMappings = new Vector(1);
            } 
            allMappings.add(descriptor.getObjectBuilder().getMappingForField(field));
            
            Exception elementIsFound = null; // use exception existence to detemine if element was found, so we can throw exception later
            for (int index = (allMappings.size() - 1); index >= 0; --index) { // start with the writable first
                DatabaseMapping mapping = (DatabaseMapping) allMappings.get(index);
                
                if (mapping.isForeignReferenceMapping()) {
                    // In EJB3 this should be a direct to field mapping (either
                    // on the entity directly or on an embeddable (aggregate).
                    continue;
                } else if (mapping.isAggregateMapping()) { // in the case of aggregates drill down.
                    ObjectBuilder builder = mapping.getReferenceDescriptor().getObjectBuilder();
                    
                    Vector aggregateMappings = builder.getReadOnlyMappingsForField(field);
                    if ((aggregateMappings != null) && (!aggregateMappings.isEmpty())) {
                        // Add all the mappings from the aggregate to be
                        // processed.
                        allMappings.addAll(aggregateMappings);
                    }
                                        
                    DatabaseMapping writableMapping = builder.getMappingForField(field);
                    
                    if (writableMapping != null) {
                        // Since it may be another aggregate mapping, add it to 
                        // the allMappings list so we can drill down on it as 
                        // well.
                        allMappings.add(writableMapping);
                    }
                    
                    // Since we added the mappings from this aggregate mapping, 
                    // we should remove this aggregate mapping from the
                    // allMappings list. Otherwise, if the mapping for the 
                    // primary key field is not found in the aggregate (or 
                    // nested aggregate) then we will hit an infinite loop when
                    // searching the aggregate and its mappings.
                    // Note: This is cautionary, since in reality, this 'should'
                    // never happen, but if it does we certainly would rather
                    // throw an exception instead of causing an infinite loop.
                    allMappings.remove(mapping);
                    
                    // Update the index to parse the next mapping.
                    index = allMappings.size();
                        
                    // We modified the allMappings list, start over!
                    continue; // for loop.
                }
                
                String fieldName = mapping.getAttributeName();
                if (keyClass == null){
                    // must be a primitive
                    pkAttributes[i] = new KeyIsElementAccessor(mapping.getAttributeName(), field);
                    setPKClass(ConversionManager.getObjectClass(mapping.getAttributeClassification()));
                    elementIsFound = null;
                } else {
                    try {
                        Field keyField = null;
                        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                            try {
                                keyField = (Field)AccessController.doPrivileged(new PrivilegedGetField(keyClass, fieldName, true));
                            } catch (PrivilegedActionException exception) {
                                throw (NoSuchFieldException)exception.getException();
                            }
                        } else {
                            keyField = PrivilegedAccessHelper.getField(keyClass, fieldName, true);
                        }
                        pkAttributes[i] = new FieldAccessor(keyField, fieldName, field);
                        elementIsFound = null;
                    } catch (NoSuchFieldException ex) {
                        //must be a property
                        StringBuffer buffer = new StringBuffer();
                        buffer.append("get");
                        buffer.append(fieldName.substring(0, 1).toUpperCase());
                        buffer.append(fieldName.substring(1));
                        try {
                            Method method = null;
                            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                                try {
                                    method = (Method)AccessController.doPrivileged(new PrivilegedGetMethod(keyClass, buffer.toString(), new Class[] {  }, true));
                                } catch (PrivilegedActionException exception) {
                                    throw (NoSuchMethodException)exception.getException();
                                }
                            } else {
                                method = PrivilegedAccessHelper.getMethod(keyClass, buffer.toString(), new Class[] {  }, true);
                            }
                            pkAttributes[i] = new PropertyAccessor(method, fieldName, field);
                            elementIsFound = null;
                        } catch (NoSuchMethodException exs) {
                            // not a field not a method 
                            if (descriptor.getObjectBuilder().getPrimaryKeyMappings().size() == 1) {
                                //must be a primitive
                                pkAttributes[i] = new KeyIsElementAccessor(mapping.getAttributeName(), field);
                                setPKClass(ConversionManager.getObjectClass(mapping.getAttributeClassification()));
                                elementIsFound = null;
                            } else {
                                elementIsFound = exs;
                            }
                        }
                    }
                }
                
                if (elementIsFound == null) {
                    break;// break out of the loop we do not need to look for any more
                }
            }
            if (elementIsFound != null) {
                throw DescriptorException.errorUsingPrimaryKey(keyClass, getDescriptor(), elementIsFound);
            }
        }
        return pkAttributes;
    }

    /**
     * INTERNAL:
     * @return Returns the keyClassFields.
     */
    protected KeyElementAccessor[] getKeyClassFields(Class clazz) {
        if (this.keyClassFields == null){
            this.keyClassFields = initializePrimaryKeyFields(this.pkClass == null? clazz : this.pkClass);
        }
        return this.keyClassFields;
    }

    /**
     * INTERNAL:
     * This is the interface used to encapsilate the the type of key class element
     */
    private interface KeyElementAccessor {
        public String getAttributeName();
        public DatabaseField getDatabaseField();
        public Object getValue(Object object);
        public void setValue(Object object, Object value);
    }

    /**
     * INTERNAL:
     * This class is used when the key class element is a property
     */
    private class PropertyAccessor implements KeyElementAccessor {
        protected Method method;
        protected String attributeName;
        protected DatabaseField databaseField;

        public PropertyAccessor(Method method, String attributeName, DatabaseField field) {
            this.method = method;
            this.attributeName = attributeName;
            this.databaseField = field;
        }

        public String getAttributeName() {
            return this.attributeName;
        }

        public DatabaseField getDatabaseField() {
            return this.databaseField;
        }
        
        public Object getValue(Object object) {
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        return AccessController.doPrivileged(new PrivilegedMethodInvoker(method, object, new Object[] {  }));
                    } catch (PrivilegedActionException exception) {
                        Exception throwableException = exception.getException();
                        if (throwableException instanceof IllegalAccessException) {
                            throw (IllegalAccessException)throwableException;
                        } else {
                            throw (java.lang.reflect.InvocationTargetException)throwableException;
                        }
                    }
                } else {
                    return PrivilegedAccessHelper.invokeMethod(method, object, new Object[] {  });
                }
            } catch (Exception ex) {
                throw DescriptorException.errorUsingPrimaryKey(object, getDescriptor(), ex);
            }
        }
        
        public void setValue(Object object, Object value) {
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        AccessController.doPrivileged(new PrivilegedMethodInvoker(MetadataHelper.getSetMethod(method, object.getClass()), object, new Object[] {value}));
                    } catch (PrivilegedActionException exception) {
                        Exception throwableException = exception.getException();
                        if (throwableException instanceof IllegalAccessException) {
                            throw (IllegalAccessException)throwableException;
                        } else {
                            throw (java.lang.reflect.InvocationTargetException)throwableException;
                        }
                    }
                } else {
                    PrivilegedAccessHelper.invokeMethod(MetadataHelper.getSetMethod(method, object.getClass()), object, new Object[] {value});
                }
            } catch (Exception ex) {
                throw DescriptorException.errorUsingPrimaryKey(object, getDescriptor(), ex);
            }
        }
    }

    /**
     * INTERNAL:
     * This class will be used when the element of the keyclass is a field
     */
    private class FieldAccessor implements KeyElementAccessor {
        protected Field field;
        protected String attributeName;
        protected DatabaseField databaseField;

        public FieldAccessor(Field field, String attributeName, DatabaseField databaseField) {
            this.field = field;
            this.attributeName = attributeName;
            this.databaseField = databaseField;
        }

        public String getAttributeName() {
            return this.attributeName;
        }

        public DatabaseField getDatabaseField() {
            return this.databaseField;
        }
        
        public Object getValue(Object object) {
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        return AccessController.doPrivileged(new PrivilegedGetValueFromField(field, object));
                    } catch (PrivilegedActionException exception) {
                        throw DescriptorException.errorUsingPrimaryKey(object, getDescriptor(), exception.getException());                    }
                } else {
                    return oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getValueFromField(field, object);
                }
            } catch (Exception ex) {
                throw DescriptorException.errorUsingPrimaryKey(object, getDescriptor(), ex);
            }
        }
        
        public void setValue(Object object, Object value) {
            try {
                Field pkField = null;
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        pkField = (Field)AccessController.doPrivileged(new PrivilegedGetField(object.getClass(), field.getName(), true));
                        AccessController.doPrivileged(new PrivilegedSetValueInField(pkField, object, value));
                    } catch (PrivilegedActionException exception) {
                        throw DescriptorException.errorUsingPrimaryKey(object, getDescriptor(), exception.getException());
                    }
                } else {
                    pkField = PrivilegedAccessHelper.getField(object.getClass(), field.getName(), true);
                    PrivilegedAccessHelper.setValueInField(pkField, object, value);
                }
            } catch (Exception ex) {
                throw DescriptorException.errorUsingPrimaryKey(object, getDescriptor(), ex);
            }
        }
    }

    /**
     * INTERNAL:
     * This class will be used when the keyClass is a primitive
     */
    private class KeyIsElementAccessor implements KeyElementAccessor {
        protected String attributeName;
        protected DatabaseField databaseField;

        public KeyIsElementAccessor(String attributeName, DatabaseField databaseField) {
            this.attributeName = attributeName;
            this.databaseField = databaseField;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public DatabaseField getDatabaseField() {
            return this.databaseField;
        }
        
        public Object getValue(Object object) {
            return object;
        }
        
        public void setValue(Object object, Object value) {
            // WIP - do nothing for now??? 
        }
    }
}
