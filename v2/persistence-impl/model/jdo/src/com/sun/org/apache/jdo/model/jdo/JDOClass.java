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
import com.sun.org.apache.jdo.model.java.JavaType;


/**
 * A JDOClass instance represents the JDO metadata of a persistence-capable
 * class.
 *
 * @author Michael Bouschen
 * @version 2.0
 */
public interface JDOClass 
    extends JDOMember 
{
    /** 
     * Get the short name of this JDOClass. The short name defaults to the
     * unqualified class name, if not explicitly set by method
     * {@link #setShortName(String shortName)}.
     * @return the short name of this JDOClass.
     */
    public String getShortName();
    
    /** 
     * Set the short name of this JDOClass.
     * @param shortName the short name.
     * @exception ModelException if impossible
     */
    public void setShortName(String shortName)
        throws ModelException;

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
    public int getIdentityType();

    /** 
     * Set the object identity type of this JDOClass.
     * @param identityType an integer indicating the JDO identity type, one of:
     * {@link JDOIdentityType#APPLICATION}, 
     * {@link JDOIdentityType#DATASTORE}, or 
     * {@link JDOIdentityType#NONDURABLE}
     * @exception ModelException if impossible
     */
    public void setIdentityType(int identityType)
        throws ModelException;

    /** 
     * Get the JavaType representation of the object identity class 
     * (primary key class) for this JDOClass. 
     * @return the JavaType representation of the object identity class.
     */
    public JavaType getObjectIdClass();

    /** 
     * Set the JavaType representation of the object identity class 
     * (primary key class) for this JDOClass. 
     * @param objectIdClass the JavaType representation of the 
     * object identity class.
     * @exception ModelException if impossible
     */
    public void setObjectIdClass(JavaType objectIdClass)
        throws ModelException;

    /** 
     * Get the fully qualified name of the object identity class 
     * (primary key class) declared for this JDOClass. 
     * Please note, this method returns a non null class name, only if the 
     * JDO metadata defines an objectIdClass for exactly the pc class 
     * represented by this JDOClass. If there is no objectIdClass defines for 
     * this JDOClass, but any of the pc-superclasses defines an objectIdClass, 
     * this method returns <code>null</code>. This is different from method
     * {@link #getObjectIdClass} which returns a non-null value, if the 
     * superclass defines a objectIdClass.
     * @return the name of the object identity class.
     */
    public String getDeclaredObjectIdClassName();

    /** 
     * Set the fully qualified name of the object identity class 
     * (primary key class) declared for this JDOClass. 
     * @param declaredObjectIdClassName the name of the object identity class
     * @exception ModelException if impossible
     */
    public void setDeclaredObjectIdClassName(String declaredObjectIdClassName)
        throws ModelException;

    /**
     * Determines whether an extent must be managed for the 
     * persistence-capable class described by this JDOClass.
     * @return <code>true</true> if this class must manage an extent; 
     * <code>false</code> otherwise
     */
    public boolean requiresExtent();
    
    /**
     * Set whether an extent must be managed for the 
     * persistence-capable class described by this JDOClass.
     * @param requiresExtent <code>true</code> if this class must manage 
     * an extent; <code>false</code> otherwise
     * @exception ModelException if impossible
     */
    public void setRequiresExtent(boolean requiresExtent)
        throws ModelException;

    /**
     * Get the fully qualified class name of the persistence-capable superclass 
     * of the persistence-capable class described by this JDOClass. If this 
     * class does not have a persistence-capable superclass then 
     * <code>null</code> is returned.
     * @return the fully qualified name of the persistence-capable superclass 
     * or <code>null</code> if there is no persistence-capable superclass 
     */
    public String getPersistenceCapableSuperclassName();
    
    /**
     * Set the fully qualified class name of the persistence-capable superclass 
     * of the persistence-capable class described by this JDOClass.
     * @param pcSuperclassName the fully qualified name of the 
     * persistence-capable superclass 
     * @exception ModelException if impossible
     */
    public void setPersistenceCapableSuperclassName(String pcSuperclassName)
        throws ModelException;

    /**
     * Provides the JavaType representaion corresponding to this JDOClass.
     * <p>
     * Note the difference between Object.getClass) and this method. The
     * former returns the class of the object in hand, this returns the class
     * of the object represented by this meta data.
     * @return the JavaType object corresponding to this JDOClass.
     */
    public JavaType getJavaType();

    /**
     * Set the JavaType representation corresponding to this JDOClass.
     * @param javaType the JavaType representation for this JDOClass.
     * @exception ModelException if impossible
     */
    public void setJavaType(JavaType javaType)
        throws ModelException;

    /** 
     * Determines whether the XML metadata for the class represented by this
     * JDOClass has been loaded. 
     * @return <code>true</code> if XML metadata is loaded;
     * <code>false</code> otherwise
     */
    public boolean isXMLMetadataLoaded();

    /**
     * Sets the flag indicating that the class XML metadata for this
     * JDOClass is loaded to <code>true</code>.
     */
    public void setXMLMetadataLoaded();

    /** 
     * Remove the supplied member from the collection of members maintained by
     * this JDOClass.
     * @param member the member to be removed
     * @exception ModelException if impossible
     */
    public void removeDeclaredMember(JDOMember member)
        throws ModelException;

    /** 
     * Returns the collection of JDOMember instances declared by this JDOClass 
     * in form of an array.
     * @return the members declared by this JDOClass
     */
    public JDOMember[] getDeclaredMembers();

    /**
     * Returns the declaring JDOModel of this JDOClass.
     * @return the JDOModel that owns this JDOClass
     */
    public JDOModel getDeclaringModel();

    /**
     * Set the declaring JDOModel for this JDOClass.
     * @param model the declaring JDOModel of this JDOClass
     */
    public void setDeclaringModel(JDOModel model);
    
    /**
     * Returns the JDOClass instance for the persistence-capable superclass 
     * of this JDOClass. If this class does not have a persistence-capable 
     * superclass then <code>null</code> is returned.
     * @return the JDClass instance of the persistence-capable superclass
     * or <code>null</code> if there is no persistence-capable superclass 
     */
    public JDOClass getPersistenceCapableSuperclass();
    
    /**
     * Set the JDOClass for the persistence-capable superclass 
     * of this JDOClass.
     * @param pcSuperclass the JDClass instance of the persistence-capable
     * superclass
     * @exception ModelException if impossible
     */
    public void setPersistenceCapableSuperclass(JDOClass pcSuperclass)
        throws ModelException;

    /**
     * Returns the JDOPackage instance corresponding to the package name 
     * of this JDOClass. 
     * @return the JDOPackage instance of this JDOClass.
     */
    public JDOPackage getJDOPackage();

    /**
     * Sets the JDOPackage instance corresponding to the package name 
     * of this JDOClass.
     * @param jdoPackage the JDOPackage of this JDOClass.
     */
    public void setJDOPackage(JDOPackage jdoPackage);

    /**
     * This method returns a JDOField instance for the field with the specified
     * name. If this JDOClass already declares such a field, the existing 
     * JDOField instance is returned. Otherwise, it creates a new JDOField 
     * instance, sets its declaring JDOClass and returns the new instance.
     * @param name the name of the field
     * @return a JDOField instance for the specified field name
     * @exception ModelException if impossible
     */
    public JDOField createJDOField(String name)
        throws ModelException;

    /**
     * This method returns a JDOProperty instance for the property with the
     * specified name. If this JDOClass already declares such a property, the
     * existing JDOProperty instance is returned. Otherwise, it creates a new
     * JDOProperty instance, sets its declaring JDOClass and returns the new
     * instance.
     * @param name the name of the property
     * @return a JDOProperty instance for the specified property
     * @exception ModelException if impossible
     */
    public JDOProperty createJDOProperty(String name)
        throws ModelException;

    /**
     * This method returns a JDOProperty instance for the property with the
     * specified name and associated field. If this JDOClass already declares
     * such a property the existing JDOProperty instance is returned. If it
     * declares a property with the specified name but different associated
     * field, then a ModelException is thrown. If there is no such property,
     * the method creates a new JDOProperty instance, sets its declaring
     * JDOClass and associated field and returns the new instance.
     * @param name the name of the property
     * @param associatedField the associated JDOField 
     * @return a JDOProperty instance for the specified property
     * @exception ModelException if impossible
     */
    public JDOProperty createJDOProperty(String name, JDOField associatedField)
        throws ModelException;

    /**
     * This method returns a JDOClass instance representing an inner class of 
     * this JDOClass If this JDOClass already declares such an inner class, 
     * the existing JDOClass instance is returned. Otherwise, it creates a new 
     * JDOClass instance, sets its declaring JDOClass and returns the new
     * instance.
     * @param name the name of the inner class
     * @exception ModelException if impossible
     */
    public JDOClass createJDOClass(String name)
        throws ModelException;

    /**
     * Returns the collection of JDOClass instances declared by this JDOClass.  
     * @return the classes declared by this JDOClass
     */
    public JDOClass[] getDeclaredClasses();

    /**
     * Returns the collection of JDOField instances declared by this JDOClass 
     * in the form of an array. This does not include inherited fields.
     * @return the fields declared by this JDOClass
     */
    public JDOField[] getDeclaredFields();

    /**
     * Returns the collection of managed JDOField instances declared by this
     * JDOClass in the form of an array. The returned array does not include 
     * inherited fields. A field is a managed field, if it has the 
     * persistence-modifier {@link PersistenceModifier#PERSISTENT} or 
     * {@link PersistenceModifier#TRANSACTIONAL}. The position of the fields 
     * in the returned array equals their relative field number as returned by 
     * {@link JDOField#getRelativeFieldNumber()}. The following holds true for 
     * any field in the returned array: 
     * <ul>
     * <li> <code>getDeclaredManagedFields()[i].getRelativeFieldNumber() 
     * == i</code>
     * <li> <code>getDeclaredManagedFields()[field.getRelativeFieldNumber()] 
     * == field</code>
     * </ul> 
     * @return the managed fields declared by this JDOClass
     */
    public JDOField[] getDeclaredManagedFields();
    
    /**
     * Returns the collection of managed JDOField instances of this JDOClass 
     * in the form of an array. The returned array includes inherited fields.
     * A field is a managed field, if it has the persistence-modifier 
     * {@link PersistenceModifier#PERSISTENT} or 
     * {@link PersistenceModifier#TRANSACTIONAL}. The position of the fields 
     * in the returned array equals their absolute field number as returned by 
     * {@link JDOField#getFieldNumber()}. The following holds true for any 
     * field in the returned array: 
     * <ul>
     * <li> <code>getManagedFields()[i].getFieldNumber() == i</code>
     * <li> <code>getManagedFields()[field.getFieldNumber()] == field</code>
     * </ul> 
     * @return the managed fields of this JDOClass
     */
    public JDOField[] getManagedFields();

    /**
     * Returns the collection of persistent JDOField instances of this JDOClass 
     * in the form of an array. The returned array includes inherited fields.
     * A field is a persistent field, if it has the persistence-modifier 
     * {@link PersistenceModifier#PERSISTENT}.
     * Please note, the position of the fields in the returned array might not 
     * equal their absolute field number as returned by 
     * {@link JDOField#getFieldNumber()}.
     * @return the persistent fields of this JDOClass
     */
    public JDOField[] getPersistentFields();

    /**
     * Returns the collection of identifying fields of this JDOClass in the form
     * of an array. The method returns the JDOField instances defined as 
     * primary key fields (see {@link JDOField#isPrimaryKey}).
     * @return the identifying fields of this JDOClass
     */
    public JDOField[] getPrimaryKeyFields();

    /**
     * Returns the collection of persistent relationship fields of this JDOClass
     * in the form of an array. The method returns the JDOField instances 
     * defined as relationship (method {@link JDOField#getRelationship} returns
     * a non null value) and having the persistence-modifier 
     * {@link PersistenceModifier#PERSISTENT}.
     * @return the persistent relationship fields of this JDOClass
     */
    public JDOField[] getPersistentRelationshipFields();

    /**
     * Returns the collection of default fetch group fields of this JDOClass
     * in the form of an array. The method returns the JDOField instances 
     * defined as part of the default fetch group 
     * (method {@link JDOField#isDefaultFetchGroup} returns <code>true</code>.
     * @return the default fetch group fields of this JDOClass
     * @since 1.1
     */
    public JDOField[] getDefaultFetchGroupFields(); 

    /**
     * Returns an array of absolute field numbers of the managed fields of this
     * JDOClass. The returned array includes field numbers of inherited fields.
     * A field is a managed field, if it has the persistence-modifier 
     * {@link PersistenceModifier#PERSISTENT} or 
     * {@link PersistenceModifier#TRANSACTIONAL}. 
     * Only managed fields have a valid field number, thus the field number in 
     * the returned array equals its index:
     * <br>
     *  <code>getManagedFields()[i] == i</code>
     */
    public int[] getManagedFieldNumbers();

    /**
     * Returns an array of absolute field numbers of the persistent fields of 
     * this JDOClass. The returned array includes field numbers of inherited 
     * fields. A persistent field has the persistence-modifier 
     * {@link PersistenceModifier#PERSISTENT}.
     */
    public int[] getPersistentFieldNumbers();

    /**
     * Returns an array of absolute field numbers of the identifying fields 
     * of this JDOClass. A field number is included in the returned array, 
     * iff the corresponding JDOField instance is defined as primary  key field
     * (see {@link JDOField#isPrimaryKey}).
     * @return array of numbers of the identifying fields
     */
    public int[] getPrimaryKeyFieldNumbers();

    /**
     * Returns an array of absolute field numbers of the non identifying, 
     * persistent fields of this JDOClass. A field number is included in the 
     * returned array, iff the corresponding JDOField instance is persistent and 
     * not a not a primary key field (see {@link JDOField#isPrimaryKey}).
     * A field is a persistent field, if it has the persistence-modifier 
     * {@link PersistenceModifier#PERSISTENT} or 
     * (see {@link JDOField#getPersistenceModifier}). 
     * @return array of numbers of the non identifying, persistent fields
     */
    public int[] getPersistentNonPrimaryKeyFieldNumbers();
    
    /**
     * Returns an array of absolute field numbers of persistent relationship 
     * fields of this JDOClass. A field number is included in the returned 
     * array, iff the corresponding JDOField instance is a relationship (method 
     * {@link JDOField#getRelationship} returns a non null value) and has the 
     * persistence-modifier {@link PersistenceModifier#PERSISTENT}.
     * @return the field numbers of the persistent relationship fields
     */
    public int[] getPersistentRelationshipFieldNumbers();

    /**
     * Returns an array of absolute field numbers of persistent, serializable 
     * fields of this JDOClass. A field number is included in the returned 
     * array, iff the corresponding JDOField instance is serializable (method 
     * {@link JDOField#isSerializable} returns <code>true</code>) and has the 
     * persistence-modifier {@link PersistenceModifier#PERSISTENT}.
     * @return the field numbers of serializable fields
     */
    public int[] getPersistentSerializableFieldNumbers();

    /**
     * Returns JDOField metadata for a particular managed field specified by 
     * field name. It returns <code>null</code> if the specified name does not 
     * denote a managed field of this JDOClass. The field name may be 
     * unqualified and or qualified (see {@link #getField(String fieldName)}).
     * @param fieldName the name of the managed field for which field metadata
     * is needed.
     * @return JDOField metadata for the managed field or <code>null</code>
     * if there is no such field.
     */
    public JDOField getManagedField(String fieldName);
    
    /**
     * Returns JDOField metadata for a particular field specified by field name.
     * It returns <code>null</code> if the specified name does not denote a 
     * field of this JDOClass.
     * <p>
     * The method supports lookup by unqualified and by qualified field name. 
     * <ul>
     * <li> In the case of an unqualified field name the method starts checking 
     * this JDOClass for a field with the specified name. If this class does not
     * define such a field, it checks the inheritance hierarchy starting with 
     * its direct persistence-capable superclass. The method finds the first 
     * field with the specified name in a bootom-up lookup of the inheritance 
     * hierarchy. Hidden fields are not visible.
     * <li> In the case of a qualified field name the method assumes a fully 
     * qualified class name (called qualifier class) as the field qualifier. 
     * The qualifier class must be a either this class or a persistence-capable 
     * superclass (direct or indirect) of this class. Then the method searches 
     * the field definition in the inheritance hierarchy staring with the 
     * qualifier class. Any field declarations with the same name in subclasses
     * of the qualifier class are not considered. This form allows accessing 
     * fields hidden by subclasses. The method returns <code>null</code> if the 
     * qualifier class does not denote a valid class or if the qualifier class 
     * is not a persistence-capable superclass of this class.
     * </ul>
     * @param fieldName the unqualified or qualified name of field for which 
     * field metadata is needed.
     * @return JDOField metadata for the field or <code>null</code>
     * if there is no such field.
     */
    public JDOField getField(String fieldName);
    
    /**
     * Provides metadata for a particular field specified by the absolute field 
     * number. The field number must be a valid absolute field number for this 
     * JDOClass: <code>0 <= fieldNumber < this.getManagedFields().length</code>
     * If the field number is valid the returned JDoField instance denotes a 
     * managed field, meaning the field has the persistence-modifier 
     * {@link PersistenceModifier#PERSISTENT} or 
     * {@link PersistenceModifier#TRANSACTIONAL}. If the field number is not 
     * valid then the method returns <code>null</code>.
     * @param fieldNumber the number for which field metadata is needed.
     * @return JDOField metadata for the field or <code>null</code>
     * if there is no such field.
     */
    public JDOField getField(int fieldNumber);

    /** 
     * Returns JDOField metadata for a particular declared field specified by 
     * field name. Please note, the method does not  return inherited fields.
     * The field name must not be qualified by a class name. The method returns
     * <code>null</code> if the field name does not denote a field declared by 
     * JDOClass.
     * @param fieldName the unqualified name of field for which field metadata 
     * is needed.
     * @return JDOField metadata for the field or <code>null</code>
     * if there is no such field declared by this JDOClass.
     */
    public JDOField getDeclaredField(String fieldName);

    /**
     * Returns JDOProperty metadata for a property with the specified name
     * having an associated JDOField. The method returns <code>null</code>, if
     * the name does not denote a property with an associated JDOField of this
     * JDOClass. Please note, the method does not check for properties without
     * an associated JDOField. It will return <code>null</code> if there is
     * a property with the specified name, but this property does not have an
     * associated JDOField.
     * @param name the name of property with an associated JDOField for which
     * metadata is needed.
     * @return JDOProperty metadata for the property with an associated
     * JDOField or <code>null</code> if there is no such property.
     */
    public JDOProperty getAssociatedProperty(String name);

    /**
     * Returns JDOProperty metadata for a property having the specified
     * JDOField as associated JDOField. The method returns <code>null</code>,
     * if this JDOClass does not have a property with the specified JDOField
     * as associated JDOField.
     * @param JDOField the assoaciated JDOField of the property for which
     * metadata is needed.
     * @return JDOProperty metadata for the property the specified JDOField as
     * associated JDOField or <code>null</code> if there is no such property.
     */
    public JDOProperty getAssociatedProperty(JDOField field);

    /**
     * Returns the number of managed fields declared in the class represented
     * by this JDOClass. This does not include inherited fields.
     * @return number of declared managed fields
     */
    public int getDeclaredManagedFieldCount();

    /**
     * Returns the number of inherited managed fields for the class
     * represented by this JDOClass.
     * @return number of inherited managed fields
     */
    public int getInheritedManagedFieldCount();

    /**
     * Returns the number of managed fields for the class represented by this
     * JDOClass. The value returned by this method is equal to
     * <code>getDeclaredManagedFieldCount() +
     * getInheritedManagedFieldCount()</code>.
     * @return number of managed fields
     */
    public int getManagedFieldCount();

    /**
     * Returns the package name including a terminating dot if this class has a 
     * package. The method returns the empty string if this class is in the 
     * default package.
     * @return package prefix for this class.
     */
    public String getPackagePrefix();

    /**
     * Returns the least-derived (topmost) persistence-capable class in the 
     * hierarchy of this JDOClass. It returns this JDOClass if it has no 
     * persistence-capable superclass.
     * @return the topmost persistence-capable class in the hierarchy.
     */
    public JDOClass getPersistenceCapableRootClass();
}
