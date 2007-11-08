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

package com.sun.org.apache.jdo.impl.model.jdo;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import com.sun.org.apache.jdo.impl.model.jdo.util.TypeSupport;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOArray;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOCollection;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDOMap;
import com.sun.org.apache.jdo.model.jdo.JDOReference;
import com.sun.org.apache.jdo.model.jdo.JDORelationship;
import com.sun.org.apache.jdo.model.jdo.NullValueTreatment;
import com.sun.org.apache.jdo.model.jdo.PersistenceModifier;
import com.sun.org.apache.jdo.util.I18NHelper;

/**
 * An instance of this class represents the JDO metadata of a managed field 
 * of a persistence capable class. This dynamic implementation only
 * stores property values explicitly set by setter method. 
 * <p>
 * Please note, you cannot rely on the Java identity of the
 * JDORelationship instance returned by {@link #getRelationship}.  
 * The getter will always return a new Java Instance, unless the
 * relationship is explicitly set by the setter 
 * {@link #setRelationship(JDORelationship relationship)}.
 * <p>
 * TBD:
 * <ul>
 * <li> Change usage of POSSIBLY_PERSISTENT persistence-modifier as soon as 
 * the enhancer fully supports it.
 * <li> Property change support
 * </ul> 
 *
 * @author Michael Bouschen
 * @since 1.1
 * @version 2.0
 */
public class JDOFieldImplDynamic
    extends JDOMemberImpl 
    implements JDOField
{
    /** 
     * Property persistenceModifier. 
     * Default see {@link #getPersistenceModifier}. 
     */
    protected int persistenceModifier = PersistenceModifier.UNSPECIFIED;
    
    /** Property primaryKey. Defaults to <code>false</code>. */
    private boolean primaryKey = false;
    
    /** Property nullValueTreatment. Defaults to none. */
    private int nullValueTreatment = NullValueTreatment.NONE;

    /** Property defaultFetchGroup. Default see {@link #isDefaultFetchGroup}. */
    protected Boolean defaultFetchGroup;

    /** Property embedded. Default see {@link #isEmbedded}. */
    protected Boolean embedded;

    /** Property javaField. No default. */
    protected transient JavaField javaField;

    /** Property serializable. Defaults to <code>false</code>. */
    private boolean serializable = false;

    /** Property mappedByName. Defaults to <code>null</code>. */
    private String mappedByName = null;

    /** Relationship JDOField<->JDORelationship. */
    protected JDORelationship relationship;
    
    /** I18N support */
    protected final static I18NHelper msg =  
        I18NHelper.getInstance(JDOFieldImplDynamic.class);

    /** Constructor. */
    protected JDOFieldImplDynamic(String name, JDOClass declaringClass) {
        super(name, declaringClass);
    }

    /**
     * Get the persistence modifier of this JDOField.
     * @return the persistence modifier, one of 
     * {@link PersistenceModifier#NONE}, 
     * {@link PersistenceModifier#PERSISTENT},
     * {@link PersistenceModifier#TRANSACTIONAL}, or
     * {@link PersistenceModifier#POSSIBLY_PERSISTENT}.
     */
    public int getPersistenceModifier() {
        if (persistenceModifier != PersistenceModifier.UNSPECIFIED) {
            // return persistenceModifier, if explicitly set by the setter
            return persistenceModifier;
        }
        
        // not set => calculate
        int result = PersistenceModifier.UNSPECIFIED;
        JavaType type = getType();
        if (nameHasJDOPrefix()) {
            result = PersistenceModifier.NONE;
        }
        else if (type != null) {
            result = TypeSupport.isPersistenceFieldType(type) ?
                PersistenceModifier.POSSIBLY_PERSISTENT : 
                PersistenceModifier.NONE;
        }

        return result;
    }

    /** 
     * Set the persistence modifier for this JDOField.
     * @param persistenceModifier an integer indicating the persistence 
     * modifier, one of: {@link PersistenceModifier#UNSPECIFIED}, 
     * {@link PersistenceModifier#NONE}, 
     * {@link PersistenceModifier#PERSISTENT},
     * {@link PersistenceModifier#TRANSACTIONAL}, or
     * {@link PersistenceModifier#POSSIBLY_PERSISTENT}.
     */
    public void setPersistenceModifier (int persistenceModifier)
        throws ModelException {
        if (nameHasJDOPrefix() && 
            (persistenceModifier == PersistenceModifier.PERSISTENT ||
             persistenceModifier == PersistenceModifier.TRANSACTIONAL)) {
            throw new ModelException(
                msg.msg("EXC_IllegalJDOPrefix", getName())); //NOI18N
        }
        this.persistenceModifier = persistenceModifier;
    }
    
    /** 
     * Determines whether this JDOField is a key field or not.  
     * @return <code>true</code> if the field is a key field, 
     * <code>false</code> otherwise
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    /** 
     * Set whether this JDOField is a key field or not.
     * @param primaryKey if <code>true</code>, the JDOField is marked 
     * as a key field; otherwise, it is not
     */
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * Gets the null value treatment indicator of this JDOField.
     * @return the null value treatment of this JDOField, one of 
     * {@link NullValueTreatment#NONE}, {@link NullValueTreatment#EXCEPTION} or
     * {@link NullValueTreatment#DEFAULT}
     */
    public int getNullValueTreatment() {
        return nullValueTreatment;
    }

    /**
     * Sets the null value treatment indicator for this JDOField.
     * @param nullValueTreatment an integer indicating the null 
     * value treatment, one of: {@link NullValueTreatment#NONE}, 
     * {@link NullValueTreatment#EXCEPTION} or 
     * {@link NullValueTreatment#DEFAULT}
     */
    public void setNullValueTreatment(int nullValueTreatment) {
        this.nullValueTreatment = nullValueTreatment;
    }

    /**
     * Determines whether this JDOField is part of the default fetch group or 
     * not.
     * @return <code>true</code> if the field is part of the default fetch 
     * group, <code>false</code> otherwise
     */
    public boolean isDefaultFetchGroup() {
        if (defaultFetchGroup != null) {
            // return dfg, if explicitly set by the setter
            return defaultFetchGroup.booleanValue();
        }
        
        // not set => calculate
        boolean dfg = false;
        if (isPrimaryKey()) {
            dfg = false;
        }
        else {
            JavaType type = getType();
            if ((type != null) && type.isValue()) {
                dfg = true;
            }
        }
        
        return dfg;
    }

    /**
     * Set whether this JDOField is part of the default fetch group or not.
     * @param defaultFetchGroup if <code>true</code>, the JDOField is marked  
     * as beeing part of the default fetch group; otherwise, it is not
     */
    public void setDefaultFetchGroup(boolean defaultFetchGroup) {
        this.defaultFetchGroup = 
            defaultFetchGroup ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Determines whether the field should be stored if possible as part of
     * the instance instead of as its own instance in the datastore.
     * @return <code>true</code> if the field is stored as part of the instance;
     * <code>false</code> otherwise
     */
    public boolean isEmbedded() {
        if (embedded != null) {
            // return embedded, if explicitly set by the setter
            return embedded.booleanValue();
        }
        
        // not set => calculate
        boolean result = false;
        JavaType type = getType();
        if (type != null) {
            result = TypeSupport.isEmbeddedFieldType(type);
        }
        return result;
    }

    /**
     * Set whether the field should be stored if possible as part of
     * the instance instead of as its own instance in the datastore.
     * @param embedded <code>true</code> if the field is stored as part of the 
     * instance; <code>false</code> otherwise
     */
    public void setEmbedded(boolean embedded) {
        this.embedded = (embedded ? Boolean.TRUE : Boolean.FALSE);
    }
    
    /**
     * Get the corresponding JavaField representation for this JDOField.
     * @return the corresponding JavaField representation
     */
    public JavaField getJavaField() {
        if (javaField != null) {
            // return java field, if explicitly set by the setter
            return javaField;
        }
        
        // not set => calculate
        JavaType javaType = getDeclaringClass().getJavaType();
        return javaType.getJavaField(getName());
    }

    /**
     * Sets the corresponding Java field representation for this JDOField.
     * @param javaField the corresponding Java field representation
     */
    public void setJavaField (JavaField javaField) throws ModelException {
        this.javaField = javaField;
    }
    
    /**
     * Determines whether this JDOField is serializable or not.  
     * @return <code>true</code> if the field is serializable,
     * <code>false</code> otherwise
     */
    public boolean isSerializable() {
        return serializable;
    }

    /** 
     * Set whether this JDOField is serializable or not.
     * @param serializable if <code>true</code>, the JDOField is serializable;
     * otherwise, it is not
     * @exception ModelException if impossible
     */
    public void setSerializable(boolean serializable) throws ModelException {
        this.serializable = serializable;
    }

    /** 
     * Get the name of the field specified in a mappedBy attribute in the
     * metadata. The method returns <code>null</code> if the metadata for this
     * field does not specify the mappedBy attribute.  Note that this 
     * can be provided at the field level to help population of the model, 
     * but should only be specified on a field that has a corresponding
     * relationship.
     * @return the mappedBy field name if available; <code>null</code>
     * otherwise.
     */
    public String getMappedByName() {
        return mappedByName;
    }

    /**
     * Set the name of the field specified in a mappedBy attribute in the
     * metadata.  Note that this can be provided at the field level to 
     * help population of the model, but should only be specified on a 
     * field that has a corresponding relationship.
     * @param mappedByName the mappedBy field name.
     * @exception ModelException if impossible
     */
    public void setMappedByName(String mappedByName) throws ModelException {
        String oldMappedByName = this.mappedByName;
        this.mappedByName = mappedByName;
        UnresolvedRelationshipHelper info = getUnresolvedRelationshipHelper();
        if (oldMappedByName != null) {
            // remove old mappedByName from unresolved relationship helper
            info.remove(oldMappedByName, this);
        }
        if (mappedByName != null) {
            // update unresolved relationship helper
            info.register(mappedByName, this);
        }
    }

    /**
     * Get the relationship information for this JDOField. The method 
     * returns null if the field is not part of a relationship 
     * (e.g. it is a primitive type field).
     * @return relationship info of this JDOField or <code>null</code> if 
     * this JDOField is not a relationship
     */
    public JDORelationship getRelationship() {
        if (relationship != null) {
            // return relationship, if explicitly set by the setter
            return relationship;
        }
        
        // not set => calculate

        if (getPersistenceModifier() == PersistenceModifier.NONE)
            // field has persistence modifier none => cannot be a relationship
            return null;
                            
        // check the type if available
        JDORelationship rel = null;
        JavaType type = getType();
        if (type != null) {
            if (type.isValue() || TypeSupport.isValueArrayType(type)) {
                // no relationship
                rel = null;
            }
            else if (type.isJDOSupportedCollection()) {
                rel = createJDOCollectionInternal();
            }
            else if (type.isJDOSupportedMap()) {
                rel = createJDOMapInternal();
            }
            else if (type.isArray()) {
                rel = createJDOArrayInternal();
            }
            else {
                rel = createJDOReferenceInternal();
            }
        }
        return rel;
    }

    /**
     * Set the relationship information for this JDOField.
     * @param relationship the JDORelationship instance
     */
    public void setRelationship(JDORelationship relationship) 
        throws ModelException {
        JDORelationship old = this.relationship;
        if (old != null) {
            old.setInverseRelationship(null);
        }
        this.relationship = relationship;
    }

    /**
     * Creates and returns a new JDOReference instance. 
     * This method automatically binds the new JDOReference to this JDOField. 
     * The following holds true:
     * <ul>
     * <li> Method {@link #getRelationship} returns the new created instance
     * <li> <code>this.getRelationship().getDeclaringField() == this</code>
     * </ul> 
     * @return a new JDOReference instance bound to this JDOField
     * @exception ModelException if impossible
     */
    public JDOReference createJDOReference() throws ModelException {
        JDOReference ref = createJDOReferenceInternal();
        setRelationship(ref);
        return ref;
    }

    /**
     * Creates and returns a new JDOCollection instance. 
     * This method automatically binds the new JDOCollection to this JDOField. 
     * The following holds true:
     * <ul>
     * <li> Method {@link #getRelationship} returns the new created instance
     * <li> <code>this.getRelationship().getDeclaringField() == this</code>
     * </ul> 
     * @return a new JDOCollection instance bound to this JDOField
     * @exception ModelException if impossible
     */
    public JDOCollection createJDOCollection() throws ModelException {
        JDOCollection col = createJDOCollectionInternal();
        setRelationship(col);
        return col;
    }

    /**
     * Creates and returns a new JDOArray instance. 
     * This method automatically binds the new JDOArray to this JDOField. 
     * The following holds true:
     * <ul>
     * <li> Method {@link #getRelationship} returns the new created instance
     * <li> <code>this.getRelationship().getDeclaringField() == this</code>
     * </ul> 
     * @return a new JDOArray instance bound to this JDOField
     * @exception ModelException if impossible
     */
    public JDOArray createJDOArray() throws ModelException {
        JDOArray array = createJDOArrayInternal();
        setRelationship(array);
        return array;
    }

    /**
     * Creates and returns a new JDOMap instance. 
     * This method automatically binds the new JDOMap to this JDOField. 
     * The following holds true:
     * <ul>
     * <li> Method {@link #getRelationship} returns the new created instance
     * <li> <code>this.getRelationship().getDeclaringField() == this</code>
     * </ul> 
     * @return a new JDOMap instance bound to this JDOField
     * @exception ModelException if impossible
     */
    public JDOMap createJDOMap() throws ModelException {
        JDOMap map = createJDOMapInternal();
        setRelationship(map);
        return map;
    }

    /**
     * Convenience method to check the persistence modifier from this JDOField.
     * @return <code>true</code> if this field has the  
     * {@link PersistenceModifier#PERSISTENT} modifier; <code>false</code> 
     * otherwise
     */
    public boolean isPersistent() {
        switch (getPersistenceModifier()) {
        case PersistenceModifier.PERSISTENT:
            return true;
        case PersistenceModifier.POSSIBLY_PERSISTENT:
            // Enable assertion as soon as the enhancer sets the java modifier.
            //Assertion.affirm(javaModifier, 
            //                 msg.msg("ERR_MissingJavaModifier", 
            //                 getDeclaringClass().getName() + "." + getName()));
            int mod = getJavaField().getModifiers();
            return !(Modifier.isStatic(mod) || Modifier.isFinal(mod) || 
                     Modifier.isTransient(mod));
        }
        return false;
    }

    /**
     * Convenience method to check the persistence modifier from this JDOField.
     * @return <code>true</code> if this field has the  
     * {@link PersistenceModifier#TRANSACTIONAL} modifier; <code>false</code> 
     * otherwise
     */
    public boolean isTransactional() {
        return (getPersistenceModifier() == PersistenceModifier.TRANSACTIONAL);
    }
    
    /**
     * Convenience method to check the persistence modifier from this JDOField.
     * A field is a managed field, if it has the persistence-modifier 
     * {@link PersistenceModifier#PERSISTENT} or 
     * {@link PersistenceModifier#TRANSACTIONAL}.
     * @return <code>true</code> if this field is a managed field; 
     * <code>false</code> otherwise     
     */
    public boolean isManaged() {
        // For now treat POSSIBLY_PERSISTENT as PERSISTENT. This will be removed 
        // as soon as the enhancer fully supports POSSIBLY_PERSISTENT
        int persistenceModifier = getPersistenceModifier();
        return (persistenceModifier == PersistenceModifier.PERSISTENT) ||
               (persistenceModifier == PersistenceModifier.POSSIBLY_PERSISTENT) || 
               (persistenceModifier == PersistenceModifier.TRANSACTIONAL);
    }

    /**
     * Convenience method to check whether this field is a relationship field.
     * @return <code>true</code> if this field is a relationship;
     * <code>false</code> otherwise
     */
    public boolean isRelationship() {
        return getRelationship() != null;
    }

    /**
     * Convenience method to check whether this field represents a property.
     * @return <code>true</code> if this field represents a property; 
     * <code>false</code> otherwise
     */
    public boolean isProperty() {
        return false;
    }

    /**
     * Get the JavaType representation of the type of the field.
     * @return JavaType representation of the type of this field.
     */
    public JavaType getType() {
        JavaField field = getJavaField();
        return (field == null) ? null : field.getType();
    }
    
    /**
     * Returns the absolute field number of this JDOField.
     * @return the absolute field number
     */
    public int getFieldNumber() {
        int fieldNumber = getRelativeFieldNumber();
        if (fieldNumber > -1) {
            // >-1 denotes a managed field
            fieldNumber += getDeclaringClass().getInheritedManagedFieldCount();
        }
        return fieldNumber;
    }

    /**
     * Returns the relative field number of this JDOField.
     * @return the relative field number
     */
    public int getRelativeFieldNumber() {
        JDOField[] fields = getDeclaringClass().getDeclaredManagedFields();
        List fieldList = Arrays.asList(fields);
        return fieldList.indexOf(this);
    }

    //========= Internal helper methods ==========

    /**
     * Creates and returns a new JDOReference instance. 
     * This method automatically sets this JDOField as the declaring field of 
     * the returned instance.
     * @return a new JDOReference instance bound to this JDOField
     */
    protected JDOReference createJDOReferenceInternal() {
        JDOReferenceImplDynamic ref = new JDOReferenceImplDynamic();
        // update relationship JDORelationship->JDOField
        ref.setDeclaringField(this);
        return ref;
    }

    /**
     * Creates and returns a new JDOCollection instance. 
     * This method automatically this JDOField as the declaring field of 
     * the returned instance.
     * @return a new JDOCollection instance bound to this JDOField
     */
    protected JDOCollection createJDOCollectionInternal() {
        JDOCollectionImplDynamic collection = new JDOCollectionImplDynamic();
        // update relationship JDORelationship->JDOField
        collection.setDeclaringField(this);
        return collection;
    }

    /**
     * Creates and returns a new JDOArray instance. 
     * This method automatically this JDOField as the declaring field of 
     * the returned instance.
     * @return a new JDOArray instance bound to this JDOField
     */
    protected JDOArray createJDOArrayInternal() {
        JDOArrayImplDynamic array = new JDOArrayImplDynamic();
        // update relationship JDORelationship->JDOField
        array.setDeclaringField(this);
        return array;
    }

    /**
     * Creates and returns a new JDOMap instance. 
     * This method automatically this JDOField as the declaring field of 
     * the returned instance.
     * @return a new JDOMap instance bound to this JDOField
     */
    protected JDOMap createJDOMapInternal() {
        JDOMapImplDynamic map = new JDOMapImplDynamic();
        // update relationship JDORelationship->JDOField
        map.setDeclaringField(this);
        return map;
    }

    /**
     * Returns <code>true</code> if the name of this JDOField has the
     * prefix jdo. 
     * @return <code>true</code> if the name of this JDOField has the
     * prefix jdo; <code>false</code> otherwise.
     */
    private boolean nameHasJDOPrefix() {
        String name = getName();
        return (name != null) && name.startsWith("jdo"); //NOI18N
    }

    /** 
     * Returns the UnresolvedRelationshipHelper instance from the declaring
     * JDOModel instacne of the declaring JDOClass.
     * @return the current UnresolvedRelationshipHelper
     */
    UnresolvedRelationshipHelper getUnresolvedRelationshipHelper() {
        return ((JDOModelImplDynamic) getDeclaringClass().getDeclaringModel()).
            getUnresolvedRelationshipHelper();
    }
    
}
