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

package com.sun.org.apache.jdo.model.jdo;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaType;


/**
 * A JDOField instance represents the JDO metadata of a managed field 
 * of a persistence-capable class.
 *
 * @author Michael Bouschen
 */
public interface JDOField 
    extends JDOMember 
{
    /**
     * Get the persistence modifier of this JDOField.
     * @return the persistence modifier, one of 
     * {@link PersistenceModifier#UNSPECIFIED}, 
     * {@link PersistenceModifier#NONE}, 
     * {@link PersistenceModifier#PERSISTENT},
     * {@link PersistenceModifier#TRANSACTIONAL},
     * {@link PersistenceModifier#POSSIBLY_PERSISTENT}.
     */
    public int getPersistenceModifier();

    /** 
     * Set the persistence modifier for this JDOField.
     * @param persistenceModifier an integer indicating the persistence 
     * modifier, one of: {@link PersistenceModifier#UNSPECIFIED}, 
     * {@link PersistenceModifier#NONE}, 
     * {@link PersistenceModifier#PERSISTENT},
     * {@link PersistenceModifier#TRANSACTIONAL},
     * {@link PersistenceModifier#POSSIBLY_PERSISTENT}.
     * @exception ModelException if impossible
     */
    public void setPersistenceModifier (int persistenceModifier)
        throws ModelException;
    
    /** 
     * Determines whether this JDOField is a key field or not.  
     * @return <code>true</code> if the field is a key field, 
     * <code>false</code> otherwise
     */
    public boolean isPrimaryKey();

    /** 
     * Set whether this JDOField is a key field or not.
     * @param primaryKey if <code>true</code>, the JDOField is marked 
     * as a key field; otherwise, it is not
     * @exception ModelException if impossible
     */
    public void setPrimaryKey(boolean primaryKey)
        throws ModelException;

    /**
     * Gets the null value treatment indicator of this JDOField.
     * @return the null value treatment of this JDOField, one of 
     * {@link NullValueTreatment#NONE}, {@link NullValueTreatment#EXCEPTION} or
     * {@link NullValueTreatment#DEFAULT}
     */
    public int getNullValueTreatment();

    /**
     * Sets the null value treatment indicator for this JDOField.
     * @param nullValueTreament an integer indicating the null value treatment, 
     * one of: {@link NullValueTreatment#NONE}, 
     * {@link NullValueTreatment#EXCEPTION} or 
     * {@link NullValueTreatment#DEFAULT}
     * @exception ModelException if impossible
     */
    public void setNullValueTreatment(int nullValueTreament)
        throws ModelException;

    /**
     * Determines whether this JDOField is part of the default fetch group or 
     * not.
     * @return <code>true</code> if the field is part of the default fetch 
     * group, <code>false</code> otherwise
     */
    public boolean isDefaultFetchGroup();

    /**
     * Set whether this JDOField is part of the default fetch group or not.
     * @param defaultFetchGroup if <code>true</code>, the JDOField is marked  
     * as beeing part of the default fetch group; otherwise, it is not
     * @exception ModelException if impossible
     */
    public void setDefaultFetchGroup(boolean defaultFetchGroup)
        throws ModelException;

    /**
     * Determines whether the field should be stored if possible as part of
     * the instance instead of as its own instance in the datastore.
     * @return <code>true</code> if the field is stored as part of the instance;
     * <code>false</code> otherwise
     */
    public boolean isEmbedded();

    /**
     * Set whether the field should be stored if possible as part of
     * the instance instead of as its own instance in the datastore.
     * @param embedded <code>true</code> if the field is stored as part of the 
     * instance; <code>false</code> otherwise
     * @exception ModelException if impossible
     */
    public void setEmbedded(boolean embedded)
        throws ModelException;
    
    /**
     * Determines whether this JDOField is serializable or not.  
     * @return <code>true</code> if the field is serializable,
     * <code>false</code> otherwise
     */
    public boolean isSerializable();

    /** 
     * Set whether this JDOField is serializable or not.
     * @param serializable if <code>true</code>, the JDOField is serializable;
     * otherwise, it is not
     * @exception ModelException if impossible
     */
    public void setSerializable(boolean serializable)
        throws ModelException;

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
    public String getMappedByName();

    /**
     * Set the name of the field specified in a mappedBy attribute in the
     * metadata.  Note that this can be provided at the field level to 
     * help population of the model, but should only be specified on a 
     * field that has a corresponding relationship.
     * @param mappedByName the mappedBy field name.
     * @exception ModelException if impossible
     */
    public void setMappedByName(String mappedByName) throws ModelException;

    /**
     * Get the corresponding Java field representation for this JDOField.
     * @return the corresponding Java field representation
     */
    public JavaField getJavaField();

    /**
     * Sets the corresponding Java field representation for this JDOField.
     * @param javaField the corresponding Java field representation
     * @exception ModelException if impossible
     */
    public void setJavaField (JavaField javaField)
        throws ModelException;

    /**
     * Get the relationship information for this JDOField. The method 
     * returns null if the field is not part of a relationship 
     * (e.g. it is a primitive type field).
     * @return relationship info of this JDOField or <code>null</code> if 
     * this JDOField is not a relationship
     */
    public JDORelationship getRelationship();

    /**
     * Set the relationship information for this JDOField.
     * @param relationship the JDORelationship instance
     * @exception ModelException if impossible
     */
    public void setRelationship(JDORelationship relationship)
        throws ModelException;

    /**
     * Creates and returns a new JDOReference instance. 
     * This method automatically binds the new JDOReference to this JDOField. 
     * It throws a ModelException, if this JDOField is already bound to 
     * another JDORelationship instance. Otherwise the following holds true:
     * <ul>
     * <li> Method {@link #getRelationship} returns the new created instance
     * <li> <code>this.getRelationship().getDeclaringField() == this</code>
     * </ul> 
     * @return a new JDOReference instance bound to this JDOField
     * @exception ModelException if impossible
     */
    public JDOReference createJDOReference()
        throws ModelException;

    /**
     * Creates and returns a new JDOCollection instance. 
     * This method automatically binds the new JDOCollection to this JDOField. 
     * It throws a ModelException, if this JDOField is already bound to 
     * another JDORelationship instance. Otherwise the following holds true:
     * <ul>
     * <li> Method {@link #getRelationship} returns the new created instance
     * <li> <code>this.getRelationship().getDeclaringField() == this</code>
     * </ul> 
     * @return a new JDOCollection instance bound to this JDOField
     * @exception ModelException if impossible
     */
    public JDOCollection createJDOCollection()
        throws ModelException;

    /**
     * Creates and returns a new JDOArray instance. 
     * This method automatically binds the new JDOArray to this JDOField. 
     * It throws a ModelException, if this JDOField is already bound to 
     * another JDORelationship instance. Otherwise the following holds true:
     * <ul>
     * <li> Method {@link #getRelationship} returns the new created instance
     * <li> <code>this.getRelationship().getDeclaringField() == this</code>
     * </ul> 
     * @return a new JDOArray instance bound to this JDOField
     * @exception ModelException if impossible
     */
    public JDOArray createJDOArray()
        throws ModelException;

    /**
     * Creates and returns a new JDOMap instance. 
     * This method automatically binds the new JDOMap to this JDOField. 
     * It throws a ModelException, if this JDOField is already bound to 
     * another JDORelationship instance. Otherwise the following holds true:
     * <ul>
     * <li> Method {@link #getRelationship} returns the new created instance
     * <li> <code>this.getRelationship().getDeclaringField() == this</code>
     * </ul> 
     * @return a new JDOMap instance bound to this JDOField
     * @exception ModelException if impossible
     */
    public JDOMap createJDOMap()
        throws ModelException;

    /**
     * Convenience method to check the persistence modifier from this JDOField.
     * @return <code>true</code> if this field has the  
     * {@link PersistenceModifier#PERSISTENT} modifier; <code>false</code> 
     * otherwise
     */
    public boolean isPersistent();

    /**
     * Convenience method to check the persistence modifier from this JDOField.
     * @return <code>true</code> if this field has the  
     * {@link PersistenceModifier#TRANSACTIONAL} modifier; <code>false</code> 
     * otherwise
     */
    public boolean isTransactional();

    /**
     * Convenience method to check the persistence modifier from this JDOField.
     * A field is a managed field, if it has the persistence-modifier 
     * {@link PersistenceModifier#PERSISTENT} or
     * {@link PersistenceModifier#TRANSACTIONAL}.
     * @return <code>true</code> if this field is a managed field; 
     * <code>false</code> otherwise     
     */
    public boolean isManaged();

    /**
     * Convenience method to check whether this field is a relationship field.
     * @return <code>true</code> if this field is a relationship; 
     * <code>false</code> otherwise
     */
    public boolean isRelationship();

    /**
     * Convenience method to check whether this field represents a property.
     * @return <code>true</code> if this field represents a property; 
     * <code>false</code> otherwise
     */
    public boolean isProperty();

    /**
     * Get the JavaType representation of the type of the field.
     * @return JavaType representation of the type of this field.
     */
    public JavaType getType();

    /**
     * Returns the absolute field number of this JDOField.
     * @return the absolute field number
     */
    public int getFieldNumber();

    /**
     * Returns the relative field number of this JDOField.
     * @return the relative field number
     */
    public int getRelativeFieldNumber();

}
