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

package com.sun.org.apache.jdo.impl.model.jdo.caching;

import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDOIdentityType;
import com.sun.org.apache.jdo.model.jdo.JDOMember;
import com.sun.org.apache.jdo.model.jdo.JDOProperty;

import com.sun.org.apache.jdo.impl.model.jdo.JDOClassImplDynamic;

/**
 * An instance of this class represents the JDO metadata of a persistence 
 * capable class. This caching implementation caches any calulated
 * value to avoid re-calculating it if it is requested again.
 *
 * @author Michael Bouschen
 * @since 1.1
 * @version 2.0
 */
public class JDOClassImplCaching extends JDOClassImplDynamic
{

    /** Flag indicating whether the objectIdClass is resolved already. */
    private boolean objectIdClassResolved = false;

    /** Flag indicating whether the pcSuperclass is resolved already. */
    private boolean pcSuperclassResolved = false;

    /** Array of declared managed fields, sorted by name (see JDO spec).  */
    private JDOField[] declaredManagedFields;

    /** 
     * Array of managed fields, incluing inherited fields. The fields are 
     * sorted by name (see JDO Spec) per class in the inheritance hierarchy.
     */
    private JDOField[] managedFields;

    /** 
     * Array of persistent fields, incluing inherited fields. The fields are 
     * sorted by name (see JDO Spec) per class in the inheritance hierarchy.
     */
    private JDOField[] persistentFields;

    /** Primary key fields. */
    private JDOField[] primaryKeyFields;

    /** Persistent relationship fields. */
    private JDOField[] persistentRelationshipFields;
    
    /** Default fetch group fields. */
    private JDOField[] defaultFetchGroupFields;
    
    /** Number of inherited fields. */
    private int inheritedManagedFieldCount = -1;
    
    /** Field numbers of managed fields. */
    private int[] managedFieldNumbers;

    /** Field numbers of PERSISTENT fields. */
    private int[] persistentFieldNumbers;

    /** Field numbers of primaryKey fields. */
    private int[] primaryKeyFieldNumbers;

    /** Field numbers of managed non primaryKey fields. */
    private int[] nonPrimaryKeyFieldNumbers;

    /** Field numbers of persistent non primaryKey fields. */
    private int[] persistentNonPrimaryKeyFieldNumbers;

    /** Field numbers of persistent relationship fields. */
    private int[] persistentRelationshipFieldNumbers;

    /** Field numbers of persistent, serializable fields. */
    private int[] persistentSerializableFieldNumbers;

    /** Flag indicating wthere field numbers are calculated already. */
    private boolean fieldNumbersCalculated = false;

    /** Constructor. */
    protected JDOClassImplCaching(String name) {
        super(name);
    }

    /** Constructor for inner classes. */
    protected JDOClassImplCaching(String name, JDOClass declaringClass) {
        super(name, declaringClass);
    }

    /** 
     * Get the short name of this JDOClass. The short name defaults to the
     * unqualified class name, if not explicitly set by method
     * {@link #setShortName(String shortName)}.
     * @return the short name of this JDOClass.
     */
    public String getShortName() {
        if (shortName == null) {
            shortName = super.getShortName();
        }
        return shortName;
    }

    /** 
     * Get the JDO identity type of this JDOClass.
     * The identity type of the least-derived persistence-capable class defines
     * the identity type for all persistence-capable classes that extend it.
     * The identity type of the least-derived persistence-capable class is
     * defaulted to {@link JDOIdentityType#APPLICATION} if objectid-class is 
     * specified, and {@link JDOIdentityType#DATASTORE}, if not. 
     * @return the JDO identity type, one of 
     * {@link JDOIdentityType#APPLICATION}, 
     * {@link JDOIdentityType#DATASTORE}, or 
     * {@link JDOIdentityType#NONDURABLE}
     */
    public int getIdentityType() {
        if (identityType == JDOIdentityType.UNSPECIFIED) {
            identityType = super.getIdentityType();
        }
        return identityType;
    }

    /** 
     * Get the JavaType representation of the object identity class 
     * (primary key class) for this JDOClass. 
     * @return the JavaType representation of the object identity class.
     */
    public JavaType getObjectIdClass() {
        if (!objectIdClassResolved) {
            objectIdClassResolved = true;
            objectIdClass = super.getObjectIdClass();
        }
        return objectIdClass;
    }

    /**
     * Returns the JDOClass instance for the persistence-capable superclass 
     * of this JDOClass. If this class does not have a persistence-capable 
     * superclass then <code>null</code> is returned.
     * @return the JDClass instance of the persistence-capable superclass
     * or <code>null</code> if there is no persistence-capable superclass 
     */
    public JDOClass getPersistenceCapableSuperclass() {
        if(!pcSuperclassResolved) {
            pcSuperclass = super.getPersistenceCapableSuperclass();
        }
        return pcSuperclass;
    }

    /**
     * Provides the JavaType representaion corresponding to this JDOClass.
     * <p>
     * Note the difference between Object.getClass() and this method. The
     * former returns the class of the object in hand, this returns the class
     * of the object represented by this meta data.
     * @return the JavaType object corresponding to this JDOClass.
     */
    public JavaType getJavaType() {
        if (javaType == null) {
            javaType = super.getJavaType();
        }
        return javaType;
    }

    /** 
     * Remove the supplied member from the collection of members maintained by
     * this JDOClass.
     * @param member the member to be removed
     * @exception ModelException if impossible
     */
    public void removeDeclaredMember(JDOMember member) throws ModelException {
        if ((member instanceof JDOField) && fieldNumbersCalculated) {
            throw new ModelException(
                msg.msg("EXC_CannotRemoveJDOField")); //NOI18N
        }

        // nullify JDOField arrays storing calculated field lists
        declaredManagedFields = null;
        managedFields = null;
        persistentFields = null;
        primaryKeyFields = null;
        persistentRelationshipFields = null;
        defaultFetchGroupFields = null;

        super.removeDeclaredMember(member);
    }

    /**
     * This method returns a JDOField instance for the field with the specified 
     * name. If this JDOClass already declares such a field, the existing 
     * JDOField instance is returned. Otherwise, it creates a new JDOField 
     * instance, sets its declaringClass and returns the new instance.
     * <P> 
     * Note, if the field numbers for the managed fields of this JDOClass are 
     * calculated, this methid will fail to create a new JDOField. Any new field
     * would possibly invalidate existing field number 
     * @param name the name of the field
     * @exception ModelException if impossible
     */
    public JDOField createJDOField(String name) throws ModelException {
        if ((getDeclaredField(name) == null) && fieldNumbersCalculated) {
            throw new ModelException(
                msg.msg("EXC_CannotCreateJDOField")); //NOI18N
        }
        return super.createJDOField(name);
    }

    /**
     * Returns the collection of managed JDOField instances declared by this
     * JDOClass in the form of an array. The returned array does not include 
     * inherited fields. A field is a managed field, if it has the 
     * persistence-modifier 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#PERSISTENT} or 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#TRANSACTIONAL}. 
     * The position of the fields in the returned array equals their
     * relative field number as returned by 
     * {@link JDOField#getRelativeFieldNumber()}. The following holds
     * true for any field in the returned array: 
     * <ul>
     * <li> <code>getDeclaredManagedFields()[i].getRelativeFieldNumber() 
     * == i</code>
     * <li> <code>getDeclaredManagedFields()[field.getRelativeFieldNumber()] 
     * == field</code>
     * </ul> 
     * @return the managed fields declared by this JDOClass
     */
    public JDOField[] getDeclaredManagedFields() {
        if (declaredManagedFields == null) {
            declaredManagedFields = super.getDeclaredManagedFields();
        }
        return declaredManagedFields;
    }
            
    /**
     * Returns the collection of managed JDOField instances of this JDOClass 
     * in the form of an array. The returned array includes inherited fields.
     * A field is a managed field, if it has the persistence-modifier 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#PERSISTENT} or 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#TRANSACTIONAL}. 
     * The position of the fields in the returned array equals their
     * absolute field number as returned by 
     * {@link JDOField#getFieldNumber()}. The following holds true for
     * any field in the returned array: 
     * <ul>
     * <li> <code>getManagedFields()[i].getFieldNumber() == i</code>
     * <li> <code>getManagedFields()[field.getFieldNumber()] == field</code>
     * </ul> 
     * @return the managed fields of this JDOClass
     */
    public JDOField[] getManagedFields() {
        if (managedFields == null) {
            managedFields = super.getManagedFields();
        }
        return managedFields;
    }
    
    /**
     * Returns the collection of persistent JDOField instances of this JDOClass 
     * in the form of an array. The returned array includes inherited fields.
     * A field is a persistent field, if it has the persistence-modifier 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#PERSISTENT}.
     * Please note, the position of the fields in the returned array might not 
     * equal their absolute field number as returned by 
     * {@link JDOField#getFieldNumber()}.
     * @return the persistent fields of this JDOClass
     */
    public JDOField[] getPersistentFields() {
        if (persistentFields == null) {
            persistentFields = super.getPersistentFields();
        }
        return persistentFields;
    }

    /**
     * Returns the collection of identifying fields of this JDOClass in the form
     * of an array. The method returns the JDOField instances defined as 
     * primary key fields (see {@link JDOField#isPrimaryKey}).
     * @return the identifying fields of this JDOClass
     */
    public JDOField[] getPrimaryKeyFields() {
        if (primaryKeyFields == null) {
            primaryKeyFields = super.getPrimaryKeyFields();
        }
        return primaryKeyFields;
    }

    /**
     * Returns the collection of persistent relationship fields of this JDOClass
     * in the form of an array. The method returns the JDOField instances 
     * defined as relationship (method {@link JDOField#getRelationship} returns
     * a non null value) and having the persistence-modifier 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#PERSISTENT}.
     * @return the persistent relationship fields of this JDOClass
     */
    public JDOField[] getPersistentRelationshipFields() {
        if (persistentRelationshipFields == null) {
            persistentRelationshipFields = 
                super.getPersistentRelationshipFields();
        }
        return persistentRelationshipFields;
    }

    /**
     * Returns the collection of default fetch group fields of this JDOClass
     * in the form of an array. The method returns the JDOField instances 
     * defined as part of the default fetch group 
     * (method {@link JDOField#isDefaultFetchGroup} returns <code>true</code>.
     * @return the default fetch group fields of this JDOClass
     * @since 1.1
     */
    public JDOField[] getDefaultFetchGroupFields() {
        if (defaultFetchGroupFields == null) {
            defaultFetchGroupFields = super.getDefaultFetchGroupFields();
        }
        return defaultFetchGroupFields;
    }

    /**
     * Returns an array of absolute field numbers of the managed fields of this
     * JDOClass. The returned array includes field numbers of inherited fields.
     * A field is a managed field, if it has the persistence-modifier 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#PERSISTENT} or 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#TRANSACTIONAL}. 
     * Only managed fields have a valid field number, thus the field number in 
     * the returned array equals its index:
     * <br>
     *  <code>getManagedFields()[i] == i</code>
     */
    public int[] getManagedFieldNumbers() {
        if (managedFieldNumbers == null) {
            managedFieldNumbers = super.getManagedFieldNumbers();
        }
        return managedFieldNumbers;
    }

    /**
     * Returns an array of absolute field numbers of the persistent fields of 
     * this JDOClass. The returned array includes field numbers of inherited 
     * fields. A persistent field has the persistence-modifier 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#PERSISTENT}.
     */
    public int[] getPersistentFieldNumbers()
    {
        if (persistentFieldNumbers == null) {
            persistentFieldNumbers = super.getPersistentFieldNumbers();
        }
        return persistentFieldNumbers;
    }
    
    /**
     * Returns an array of absolute field numbers of the identifying fields 
     * of this JDOClass. A field number is included in the returned array, 
     * iff the corresponding JDOField instance is defined as primary  key field
     * (see {@link JDOField#isPrimaryKey}).
     * @return array of numbers of the identifying fields
     */
    public int[] getPrimaryKeyFieldNumbers() {
        if (primaryKeyFieldNumbers == null) {
            primaryKeyFieldNumbers = super.getPrimaryKeyFieldNumbers();
        }
        return primaryKeyFieldNumbers;
    }

    /**
     * Returns an array of absolute field numbers of the non identifying, 
     * persistent fields of this JDOClass. A field number is included in the 
     * returned array, iff the corresponding JDOField instance is persistent and 
     * not a not a primary key field (see {@link JDOField#isPrimaryKey}).
     * A field is a persistent field, if it has the persistence-modifier 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#PERSISTENT} or 
     * (see {@link JDOField#getPersistenceModifier}). 
     * @return array of numbers of the non identifying, persistent fields
     */
    public int[] getPersistentNonPrimaryKeyFieldNumbers() {
        if (persistentNonPrimaryKeyFieldNumbers == null) {
            persistentNonPrimaryKeyFieldNumbers = 
                super.getPersistentNonPrimaryKeyFieldNumbers();
        }
        return persistentNonPrimaryKeyFieldNumbers;
    }
    
    /**
     * Returns an array of absolute field numbers of persistent relationship 
     * fields of this JDOClass. A field number is included in the returned 
     * array, iff the corresponding JDOField instance is a relationship (method 
     * {@link JDOField#getRelationship} returns a non null value) and has the 
     * persistence-modifier 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#PERSISTENT}.
     * @return the field numbers of the persistent relationship fields
     */
    public int[] getPersistentRelationshipFieldNumbers() {
        if (persistentRelationshipFieldNumbers == null) {
            persistentRelationshipFieldNumbers = 
                super.getPersistentRelationshipFieldNumbers();
        }
        return persistentRelationshipFieldNumbers;
    }

    /**
     * Returns an array of absolute field numbers of persistent, serializable 
     * fields of this JDOClass. A field number is included in the returned 
     * array, iff the corresponding JDOField instance is serializable (method 
     * {@link JDOField#isSerializable} returns <code>true</code>) and has the 
     * persistence-modifier 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#PERSISTENT}.
     * @return the field numbers of serializable fields
     */
    public int[] getPersistentSerializableFieldNumbers() {
        if (persistentSerializableFieldNumbers == null) {
            persistentSerializableFieldNumbers = 
                super.getPersistentSerializableFieldNumbers();
        }
        return persistentSerializableFieldNumbers;
    }

    /**
     * Returns the number of inherited managed fields for this class.  
     * @return number of inherited fields
     */
    public synchronized int getInheritedManagedFieldCount() {
        // inheritedManagedFieldCount < 0 indicates check is not done yet.
        if (inheritedManagedFieldCount < 0) {
            inheritedManagedFieldCount = super.getInheritedManagedFieldCount();
        }
        return inheritedManagedFieldCount;
    }    

    //========= Internal helper methods ==========

    /**
     * This method calculates the relative field number of the
     * declared managed fields of this JDOClass and uddates the
     * relativeFieldNumber property of the JDOField instance.
     */
    protected void calculateFieldNumbers() {
        if (!fieldNumbersCalculated) {
            fieldNumbersCalculated = true;
            JDOField[] fields = getDeclaredManagedFields();
            // now set the relativeFieldNumber of the JDOField
            for (int i = 0; i < fields.length; i++) {
                ((JDOFieldImplCaching)fields[i]).setRelativeFieldNumber(i);
            }
        }
    }

    /**
     * Returns a new instance of the JDOClass implementation class.
     */
    protected JDOClass newJDOClassInstance(String name) {
        return new JDOClassImplCaching(name, this);
    }

    /**
     * Returns a new instance of the JDOField implementation class.
     */
    protected JDOField newJDOFieldInstance(String name) {
        return new JDOFieldImplCaching(name, this);
    }

    /**
     * Returns a new instance of the JDOProperty implementation class.
     */
    protected JDOProperty newJDOPropertyInstance(String name) {
        return new JDOPropertyImplCaching(name, this);
    }

    /**
     * Returns a new instance of the JDOProperty implementation class.
     */
    protected JDOProperty newJDOPropertyInstance(
        String name, JDOField associatedJDOField) throws ModelException {
        return new JDOAssociatedPropertyImplCaching(
            name, this, associatedJDOField);
    }
}
