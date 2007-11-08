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

import java.util.*;
import java.lang.reflect.Modifier;

import com.sun.org.apache.jdo.impl.model.jdo.util.TypeSupport;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDOIdentityType;
import com.sun.org.apache.jdo.model.jdo.JDOMember;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.model.jdo.JDOPackage;
import com.sun.org.apache.jdo.model.jdo.JDOProperty;

import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.org.apache.jdo.util.StringHelper;

/**
 * An instance of this class represents the JDO metadata of a persistence 
 * capable class. This dynamic implementation only stores property
 * values explicitly set by setter method. It does not store any
 * calculated values such as list of managed or persistent fields, 
 * list of field numbers etc. 
 * <p>
 * TBD:
 * <ul>
 * <li> Property change support
 * </ul> 
 *
 * @author Michael Bouschen
 * @since 1.1
 * @version 2.0
 */
public class JDOClassImplDynamic
    extends JDOMemberImpl
    implements JDOClass
{
    /** Property shortName. It defaults to the unqualified class name. */
    protected String shortName;

    /** Property identityType. Default see {@link #getIdentityType}. */
    protected int identityType = JDOIdentityType.UNSPECIFIED;

    /** Property objectIdClass. No default. */
    protected transient JavaType objectIdClass;

    /** Property declaredObjectIdClassName. No default. */
    private String declaredObjectIdClassName;

    /** Property requiresExtent. It defaults to <code>true</code>. */
    private boolean requiresExtent = true;

    /** Property pcSuperclassName. No default. */
    private String pcSuperclassName;

    /** Relationship JDOClass<->JDOClass. */
    protected JDOClass pcSuperclass;

    /** Property javaType. No default.*/
    protected transient JavaType javaType;

    /** Relationship JDOModel<->JDOClass. Initialized during creation.*/
    private JDOModel declaringModel;

    /**
     * Relationship JDOClass<->JDOMember.
     * Map of fields declared by this JDOClass. Key is the unqualified 
     * field name, value is the JDOField instance. 
     */
    private Map declaredFields = new HashMap();

    /** 
     * Map of properties having associated JDOField instances. Key is the
     * unqualified field name, value is the JDOField instance. 
     */
    private Map associatedProperties = new HashMap();

    /**
     * Relationship JDOClass<->JDOMember.
     * Map of inner classes declared by this JDOClass. 
     * Key is the unqualified name of the inner class, 
     * value is the JDOClass instance of the inner class.
     */
    private Map declaredClasses = new HashMap();

    /** Relationship JDOClass -> JDOPackage. */
    private JDOPackage jdoPackage;

    /** Flag indicating whether XML metadata is processed already. */
    private boolean xmlMetadataLoaded = false;

    /** I18N support */
    protected final static I18NHelper msg =  
        I18NHelper.getInstance(JDOClassImplDynamic.class);

    /** Constructor. */
    protected JDOClassImplDynamic(String name) {
        super(name, null);
    }

    /** Constructor for inner classes. */
    protected JDOClassImplDynamic(String name, JDOClass declaringClass) {
        super(name, declaringClass);
    }

    /** 
     * Get the short name of this JDOClass. The short name defaults to the
     * unqualified class name, if not explicitly set by method
     * {@link #setShortName(String shortName)}.
     * @return the short name of this JDOClass.
     */
    public String getShortName() {
        if (shortName != null)
            // return short name, if explicitly set by the setter
            return shortName;

        return StringHelper.getShortClassName(getName());
    }
    
    /** 
     * Set the short name of this JDOClass.
     * @param shortName the short name.
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
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
        if (identityType != JDOIdentityType.UNSPECIFIED) {
            // return identity type, if explicitly set by the setter
            return identityType;
        }
        
        // not set => caclulate 
        JDOClass pcRoot = getPersistenceCapableRootClass();
        int result = 0;
        if (pcRoot == this) {
            // this is the least-derived pc class
            result = (pcRoot.getDeclaredObjectIdClassName() != null) ? 
                JDOIdentityType.APPLICATION : JDOIdentityType.DATASTORE; 
        }
        else {
            // get the identityType from the least-derived pc class
            result = pcRoot.getIdentityType();
        }

        return result;
    }

    /** 
     * Set the object identity type of this JDOClass.
     * @param identityType an integer indicating the JDO identity type, one of:
     * {@link JDOIdentityType#APPLICATION}, 
     * {@link JDOIdentityType#DATASTORE}, or 
     * {@link JDOIdentityType#NONDURABLE}
     */
    public void setIdentityType(int identityType) {
        this.identityType = identityType;
    }
    
    /** 
     * Get the JavaType representation of the object identity class 
     * (primary key class) for this JDOClass. 
     * @return the JavaType representation of the object identity class.
     */
    public JavaType getObjectIdClass() {
        if (objectIdClass != null) {
            // return objectIdClass if explicitly set by the setter
            return objectIdClass;
        }

        // not set => try to resolve ObjectId class 
        JavaType type = null;
        String name = getDeclaredObjectIdClassName();
        if (name != null) {
            JavaModel javaModel = getDeclaringModel().getJavaModel();
            type = javaModel.getJavaType(name);
            if (Modifier.isAbstract(type.getModifiers()))
                // do not return ObjectId class if abstract
                type = null;
        }
        else {
            JDOClass superclass = getPersistenceCapableSuperclass();
            if (superclass != null) {
                type = superclass.getObjectIdClass();
            }
        }
        return type;
    }

    /** 
     * Set the JavaType representation of the object identity class 
     * (primary key class) for this JDOClass. 
     * @param objectIdClass the JavaType representation of the 
     * object identity class
     */
    public void setObjectIdClass(JavaType objectIdClass) {
        this.objectIdClass = objectIdClass;
    }

    /** 
     * Get the fully qualified name of the object identity class 
     * (primary key class) for this JDOClass. 
     * @return the name of the object identity class.
     */
    public String getDeclaredObjectIdClassName() {
        if (declaredObjectIdClassName != null) {
            // ObjectId is declared, but it might not be qualified
            int index = declaredObjectIdClassName.indexOf('.');
            if (index == -1) {
                // not qualified => try to resolve it
                JavaType type = TypeSupport.resolveType(getDeclaringModel(), 
                    declaredObjectIdClassName, getPackagePrefix());
                if (type == null) {
                    throw new ModelFatalException(
                        msg.msg("EXC_CannotResolveObjectIdClass", //NOI18N
                                declaredObjectIdClassName, getName()));
                }
                this.declaredObjectIdClassName = type.getName();
            }
        }
        else {
            // not declared, check for single field ObjectId class
            JDOField[] declaredPKFields = getDeclaredPrimaryKeyFields();
            if ((declaredPKFields != null) && (declaredPKFields.length == 1)) {
                // there is one pk field declared by this class => 
                // check the type
                JavaType fieldType = declaredPKFields[0].getType();
                if (fieldType != null) {
                    return TypeSupport.getSingleFieldObjectIdClassName(
                        fieldType.getName());
                }
            }
        }
        return declaredObjectIdClassName;
    }
    
    /** 
     * Set the fully qualified name of the object identity class 
     * (primary key class) for this JDOClass. 
     * @param declaredObjectIdClassName the name of the object identity class
     */
    public void setDeclaredObjectIdClassName(String declaredObjectIdClassName) {
        this.declaredObjectIdClassName = declaredObjectIdClassName;
    }

    /**
     * Determines whether an extent must be managed for the 
     * persistence-capable class described by this JDOClass.
     * @return <code>true</code> if this class must manage an extent; 
     * <code>false</code> otherwise
     */
    public boolean requiresExtent() {
        return requiresExtent;
    }
    
    /**
     * Set whether an extent must be managed for the 
     * persistence-capable class described by this JDOClass.
     * @param requiresExtent <code>true</code> if this class must manage 
     * an extent; <code>false</code> otherwise
     */
    public void setRequiresExtent(boolean requiresExtent) {
        this.requiresExtent = requiresExtent;
    }

    /**
     * Get the fully qualified class name of the persistence-capable superclass 
     * of the persistence-capable class described by this JDOClass. If this 
     * class does not have a persistence-capable superclass then 
     * <code>null</code> is returned.
     * @return the fully qualified name of the persistence-capable superclass 
     * or <code>null</code> if there is no persistence-capable superclass 
     */
    public String getPersistenceCapableSuperclassName() {
        if (pcSuperclassName != null) {
            // pcSuperclassName is declared, but it might not be qualified
            int index = pcSuperclassName.indexOf('.');
            if (index == -1) {
                // not qualified => try to resolve it
                JavaType type = TypeSupport.resolveType(getDeclaringModel(),
                    pcSuperclassName, getPackagePrefix());
                if (type == null) {
                    throw new ModelFatalException(
                        msg.msg("EXC_CannotResolvePCSuperClass", //NOI18N
                                pcSuperclassName, getName()));
                }
                this.pcSuperclassName = type.getName();
            }
        }
        return pcSuperclassName;
    }
    
    /**
     * Set the fully qualified class name of the persistence-capable superclass 
     * of the persistence-capable class described by this JDOClass.
     * @param pcSuperclassName the fully qualified name of the 
     * persistence-capable superclass 
     */
    public void setPersistenceCapableSuperclassName(String pcSuperclassName) {
        this.pcSuperclassName = pcSuperclassName;
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
        if (javaType != null) {
            // return java type, if explicitly set by the setter
            return javaType;
        }
        
        // not set => calculate
        JavaModel javaModel = declaringModel.getJavaModel();
        return javaModel.getJavaType(getName());
    }

    /**
     * Set the JavaType representation corresponding to this JDOClass.
     * @param javaType the JavaType representation for this JDOClass
     */
    public void setJavaType(JavaType javaType) {
        this.javaType = javaType;
    }

    /** 
     * Determines whether the XML metadata for the class represented by this
     * JDOClass has been loaded. 
     * @return <code>true</code> if XML metadata is loaded;
     * <code>false</code> otherwise
     */
    public boolean isXMLMetadataLoaded() {
        return xmlMetadataLoaded;
    }

    /**
     * Sets the flag indicating that the class XML metadata for this
     * JDOClass is loaded to <code>true</code>.
     */
    public void setXMLMetadataLoaded() {
        this.xmlMetadataLoaded = true;
    }

    /** 
     * Remove the supplied member from the collection of members maintained by
     * this JDOClass.
     * @param member the member to be removed
     * @exception ModelException if impossible
     */
    public void removeDeclaredMember(JDOMember member) throws ModelException {
        if (member == null) {
            throw new ModelException(
                msg.msg("EXC_InvalidMember", "null")); //NOI18N
        }
        String name = member.getName();
        if (member instanceof JDOField) {
            JDOField field = (JDOField) member;
            // nullify mappedByName which removes mappedBy info 
            field.setMappedByName(null);
            // nullify relationship which updates its inverse
            field.setRelationship(null);
            if (associatedProperties.containsValue(member)) {
                associatedProperties.remove(name);
            }
            else {
                declaredFields.remove(name); 
            }

            // There might be a property with the field to be removed as
            // associated JDOField => remove the property too.
            JDOProperty prop = getAssociatedProperty(field);
            if (prop != null) {
                removeDeclaredMember(prop);
            }
        }
        else if (member instanceof JDOClass) {
            // inner class
            declaredClasses.remove(name);
        }
        else {
            throw new ModelException(
                msg.msg("EXC_InvalidMember", name)); //NOI18N
        }
    }
    
    /** 
     * Returns the collection of JDOMember instances declared by this
     * JDOClass in form of an array.
     * @return the members declared by this JDOClass
     */
    public JDOMember[] getDeclaredMembers() {
        List copy = new ArrayList(declaredFields.values());
        copy.addAll(declaredClasses.values());
        return (JDOMember[])copy.toArray(new JDOMember[copy.size()]);
    }

    /**
     * Returns the declaring JDOModel of this JDOClass.
     * @return the JDOModel that owns this JDOClass
     */
    public JDOModel getDeclaringModel() {
        return declaringModel;
    }

    /**
     * Set the declaring JDOModel for this JDOClass.
     * @param model the declaring JDOModel of this JDOClass
     */
    public void setDeclaringModel(JDOModel model) {
        this.declaringModel = model;
    }
    
    /**
     * Returns the JDOClass instance for the persistence-capable superclass 
     * of this JDOClass. If this class does not have a persistence-capable 
     * superclass then <code>null</code> is returned.
     * @return the JDClass instance of the persistence-capable superclass
     * or <code>null</code> if there is no persistence-capable superclass 
     */
    public JDOClass getPersistenceCapableSuperclass() {
        if (pcSuperclass != null) {
            // return pcSuperclass if explicitly set by the setter
            return pcSuperclass;
            
        }
        
        // not set => try to resolve persistence capable superclass
        String name = getPersistenceCapableSuperclassName();
        if (pcSuperclassName != null) {
            JavaType type = TypeSupport.resolveType(
                getDeclaringModel(), pcSuperclassName, getPackagePrefix());
            if (type == null) {
                throw new ModelFatalException(
                    msg.msg("EXC_CannotResolvePCSuperClass", //NOI18N
                            pcSuperclassName, getName()));
            }
            JDOClass jdoClass = type.getJDOClass();
            // pcSuperclassName might be unqualified
            this.pcSuperclassName = type.getName();
            return jdoClass;
        }

        return null;
    }
    
    /**
     * Set the JDOClass for the persistence-capable superclass 
     * of this JDOClass.
     * @param pcSuperclass the JDClass instance of the persistence-capable
     * superclass
     */
    public void setPersistenceCapableSuperclass(JDOClass pcSuperclass) {
        this.pcSuperclass = pcSuperclass;
        this.pcSuperclassName = 
            pcSuperclass != null ? pcSuperclass.getName() : null;
    }

    /**
     * Returns the JDOPackage instance corresponding to the package name 
     * of this JDOClass. 
     * @return the JDOPackage instance of this JDOClass.
     */
    public JDOPackage getJDOPackage() {
        return jdoPackage;
    }

    /**
     * Sets the JDOPackage instance corresponding to the package name 
     * of this JDOClass.
     * @param jdoPackage the JDOPackage of this JDOClass.
     */
    public void setJDOPackage(JDOPackage jdoPackage) {
        this.jdoPackage = jdoPackage;
    }
    
    /**
     * This method returns a JDOField instance for the field with the specified 
     * name. If this JDOClass already declares such a field, the existing 
     * JDOField instance is returned. Otherwise, it creates a new JDOField 
     * instance, sets its declaring JDOClass and returns the new instance.
     * <P> 
     * Note, if the field numbers for the managed fields of this JDOClass are 
     * calculated, this methid will fail to create a new JDOField. Any new field
     * would possibly invalidate existing field number 
     * @param name the name of the field
     * @exception ModelException if impossible
     */
    public JDOField createJDOField(String name) throws ModelException {
        // check whether there is a field with the specified name
        JDOField field = getDeclaredField(name);
        if (field == null) {
            field = newJDOFieldInstance(name);
            declaredFields.put(name, field);
        }
        else if (field instanceof JDOProperty) {
            throw new ModelException(
                msg.msg("EXC_ExistingJDOProperty", name)); //NOI18N
        }
        return field;
    }
    
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
    public JDOProperty createJDOProperty(String name) throws ModelException {
        // check whether there is a field or property with the specified name
        JDOProperty property = null;
        JDOField field = getDeclaredField(name);
        if (field == null) {
            property = newJDOPropertyInstance(name);
            declaredFields.put(name, property);
        } 
        else if (field instanceof JDOProperty) {
            property = (JDOProperty) field;
        }
        else {
            throw new ModelException(
                msg.msg("EXC_ExistingJDOField", name)); //NOI18N
        }
        return property;
    }

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
    public JDOProperty createJDOProperty(String name, 
                                         JDOField associatedJDOField)
        throws ModelException
    {
        JDOProperty property = (JDOProperty) associatedProperties.get(name);
        if (property == null) {
            property = newJDOPropertyInstance(name, associatedJDOField);
            associatedProperties.put(name, property);
        } 
        else {
            if (property.getAssociatedJDOField() != associatedJDOField) {
                throw new ModelException(
                    msg.msg("EXC_ExistingJDOAssociatedProperty", //NOI18N
                            name, associatedJDOField)); 
            }
        }
        return property;
    }
    
    /**
     * This method returns a JDOClass instance representing an inner class of 
     * this JDOClass If this JDOClass already declares such an inner class, 
     * the existing JDOClass instance is returned. Otherwise, it creates a new 
     * JDOClass instance, sets its declaring JDOClass and returns the new
     * instance.
     * @param name the name of the inner class
     * @exception ModelException if impossible
     */
    public JDOClass createJDOClass(String name) throws ModelException {
        JDOClass innerClass = (JDOClass)declaredClasses.get(name);
        if (innerClass == null) {
            innerClass = newJDOClassInstance(name);
            declaredClasses.put(name, innerClass);
        }
        return innerClass;
    }

    /**
     * Returns the collection of JDOClass instances declared by this JDOClass.  
     * @return the classes declared by this JDOClass
     */
    public JDOClass[] getDeclaredClasses() {
        return (JDOClass[])declaredClasses.values().toArray(
            new JDOClass[declaredClasses.size()]);
    }

    /**
     * Returns the collection of JDOField instances declared by this JDOClass 
     * in the form of an array. This does not include inherited fields.
     * @return the fields declared by this JDOClass
     */
    public JDOField[] getDeclaredFields() {
        Collection tmp = declaredFields.values();
        return (JDOField[])tmp.toArray(new JDOField[tmp.size()]);
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
        // Get the list of declared fields, skip the non managed fields
        // and store the remaining fields into a list
        List fieldList = new ArrayList();
        for (Iterator i = declaredFields.values().iterator(); i.hasNext();) {
            JDOField field = (JDOField)i.next();
            if (field.isManaged())
                fieldList.add(field);
        }
            
        // Sort all declared fields. JDOFieldImpl implements Comparable.
        // It uses the field name for comparison.
        Collections.sort(fieldList);
        JDOField[] fields = new JDOField[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
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
        JDOField[] fields = null;
        JDOField[] declared = getDeclaredManagedFields();
        JDOClass superclass = getPersistenceCapableSuperclass();
        if (superclass == null) {
            // no pc superclass
            fields = declared;
        }
        else {
            // pc superclass
            JDOField[] inherited = superclass.getManagedFields();
            fields = new JDOField[inherited.length+declared.length];
            System.arraycopy(inherited, 0, fields, 0, inherited.length);
            System.arraycopy(declared, 0, fields, 
                             inherited.length, declared.length);
        }

        return fields;
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
        JDOField[] fields = getManagedFields();
        JDOField[] tmp = new JDOField[fields.length];
        int length = 0;
        for (int i = 0; i < fields.length; i++) {
            JDOField field = fields[i];
            if (field.isPersistent()) {
                tmp[length++] = field;
            }
        }
        // now fill he returned array
        // the array should have the correct length
        JDOField[] result = new JDOField[length];
        System.arraycopy(tmp, 0, result, 0, length);

        return result;
    }

    /**
     * Returns the collection of identifying fields of this JDOClass in the form
     * of an array. The method returns the JDOField instances defined as 
     * primary key fields (see {@link JDOField#isPrimaryKey}).
     * @return the identifying fields of this JDOClass
     */
    public JDOField[] getPrimaryKeyFields() {
        JDOField[] fields = getManagedFields();
        JDOField[] tmp = new JDOField[fields.length];
        int length = 0;
        for (int i = 0; i < fields.length; i++) {
            JDOField field = fields[i];
            if (fields[i].isPrimaryKey()) {
                tmp[length++] = field;
            }
        }
        // now fill the returned array 
        // the array should have the correct length
        JDOField[] result = new JDOField[length];
        System.arraycopy(tmp, 0, result, 0, length);

        return result;
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
        JDOField[] fields = getPersistentFields();
        JDOField[] tmp = new JDOField[fields.length];
        int length = 0;
        for (int i = 0; i < fields.length; i++) {
            JDOField field = fields[i];
            if (field.isPersistent() && field.isRelationship()) {
                tmp[length++] = field;
            }
        }
        // now fill the returned array,
        // the arrays should have the correct length
        JDOField[] result = new JDOField[length];
        System.arraycopy(tmp, 0, result, 0, length);

        return result;
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
        JDOField[] fields = getManagedFields();
        JDOField[] tmp = new JDOField[fields.length];
        int length = 0;
        for (int i = 0; i < fields.length; i++) {
            JDOField field = fields[i];
            if (field.isDefaultFetchGroup()) {
                tmp[length++] = field;
            }
        }
        // now fill defaultFetchGroupFields
        // the arrays should have the correct length
        JDOField[] result = new JDOField[length];
        System.arraycopy(tmp, 0, result, 0, length);

        return result;
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
        JDOField[] fields = getManagedFields();
        int[] fieldNumbers = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldNumbers[i] = i;
        }

        return fieldNumbers;
    }

    /**
     * Returns an array of absolute field numbers of the persistent fields of 
     * this JDOClass. The returned array includes field numbers of inherited 
     * fields. A persistent field has the persistence-modifier 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#PERSISTENT}.
     */
    public int[] getPersistentFieldNumbers() {
        JDOField[] fields = getManagedFields();
        int[] tmp = new int[fields.length];
        int length = 0;
        for (int i = 0; i < fields.length; i++) {
            JDOField field = fields[i];
            if (field.isPersistent()) {
                tmp[length++] = i;
            }
        }
        // now fill the returned array, it should have the correct length
        int[] fieldNumbers = new int[length];
        System.arraycopy(tmp, 0, fieldNumbers, 0, length);

        return fieldNumbers;
    }
    
    /**
     * Returns an array of absolute field numbers of the identifying fields 
     * of this JDOClass. A field number is included in the returned array, 
     * iff the corresponding JDOField instance is defined as primary  key field
     * (see {@link JDOField#isPrimaryKey}).
     * @return array of numbers of the identifying fields
     */
    public int[] getPrimaryKeyFieldNumbers() {
        JDOField[] fields = getManagedFields();
        int[] tmp = new int[fields.length];
        int length = 0;
        for (int i = 0; i < fields.length; i++) {
            JDOField field = fields[i];
            if (field.isPrimaryKey()) {
                tmp[length++] = i;
            }
        }
        // now fill the returned array, it should have the correct length
        int[] fieldNumbers = new int[length];
        System.arraycopy(tmp, 0, fieldNumbers, 0, length);

        return fieldNumbers;
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
        JDOField[] fields = getManagedFields();
        int[] tmp = new int[fields.length];
        int length = 0;
        for (int i = 0; i < fields.length; i++) {
            JDOField field = fields[i];
            if (field.isPersistent() && !field.isPrimaryKey()) {
                tmp[length++] = i;
            }
        }
        // now fill the returned array, it should have the correct length
        int[] fieldNumbers = new int[length];
        System.arraycopy(tmp, 0, fieldNumbers, 0, length);

        return fieldNumbers;
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
        JDOField[] fields = getManagedFields();
        int[] tmp = new int[fields.length];
        int length = 0;
        for (int i = 0; i < fields.length; i++) {
            JDOField field = fields[i];
            if (field.isPersistent() && field.isRelationship()) {
                tmp[length++] = i;
            }
        }
        // now fill the returned array, it should have the correct length
        int[] fieldNumbers = new int[length];
        System.arraycopy(tmp, 0, fieldNumbers, 0, length);

        return fieldNumbers;
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
        JDOField[] fields = getManagedFields();
        int[] tmp = new int[fields.length];
        int length = 0;
        for (int i = 0; i < fields.length; i++) {
            JDOField field = fields[i];
            if (field.isPersistent() && field.isSerializable()) {
                tmp[length++] = i;
            }
        }
        // now fill the returned array it should have the correct length
        int[] fieldNumbers = new int[length];
        System.arraycopy(tmp, 0, fieldNumbers, 0, length);

        return fieldNumbers;
    }
    
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
    public JDOField getManagedField(String fieldName) {
        JDOField field = getField(fieldName);
        if ((field != null) && !field.isManaged())
            // return null for a non managed field
            return null;
        return field;
    }
    
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
    public JDOField getField(String fieldName) {
        // check fieldName
        if ((fieldName == null) || (fieldName.length() == 0)) {
            return null;
        }
        
        JDOField field = null;
        int index = fieldName.lastIndexOf('.');
        if (index != -1) {
            // qualified field name
            String className = fieldName.substring(0, index);
            fieldName = fieldName.substring(index + 1);
            // move to the specified class in the inheritance hierarchy,
            // starting with the current class and get the field from there
            for (JDOClassImplDynamic next = this; next != null; 
                 next = (JDOClassImplDynamic)next.getPersistenceCapableSuperclass()) {
                 if (className.equals(next.getName())) {
                     field = next.getFieldInternal(fieldName);
                 }
            }
        }
        else {
            // unqualified field name => call getFieldInternal
            field = getFieldInternal(fieldName);
        }
        
        return field;
    }
         
    /**
     * Provides metadata for a particular field specified by the absolute field 
     * number. The field number must be a valid absolute field number for this 
     * JDOClass: <code>0 <= fieldNumber < this.getManagedFields().length</code>
     * If the field number is valid the returned JDoField instance denotes a 
     * managed field, meaning the field has the persistence-modifier 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#PERSISTENT} or 
     * {@link com.sun.org.apache.jdo.model.jdo.PersistenceModifier#TRANSACTIONAL}. 
     * If the field number is not valid then the method returns
     * <code>null</code>. 
     * @param fieldNumber the number for which field metadata is needed.
     * @return JDOField metadata for the field or <code>null</code>
     * if there is no such field.
     */
    public JDOField getField(int fieldNumber) {   
        JDOField field = null;
        JDOField[] fields = getManagedFields();
        if ((0 <= fieldNumber) && (fieldNumber < fields.length))
            field = fields[fieldNumber];
        return field;
    }

    /** 
     * Returns JDOField metadata for a particular declared field for the
     * specified name. Please note, the method does not return inherited
     * fields. The field name must not be qualified by a class name. The
     * method returns <code>null</code> if the field name does not denote a
     * field declared by JDOClass.
     * @param name the unqualified name of field for which field metadata 
     * is needed.
     * @return JDOField metadata for the field or <code>null</code>
     * if there is no such field declared by this JDOClass.
     */
    public JDOField getDeclaredField(String name) {
        return (JDOField) declaredFields.get(name);
    }

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
    public JDOProperty getAssociatedProperty(String name) {
        // first check the associated properties from this class
        JDOProperty prop = (JDOProperty) associatedProperties.get(name);
        if (prop != null) {
            return prop;
        }
        
        // not in this class => check superclass
        JDOClass superclass = getPersistenceCapableSuperclass();
        if (superclass != null) {
            return superclass.getAssociatedProperty(name);
        }
        
        // not found => return null
        return null;
    }

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
    public JDOProperty getAssociatedProperty(JDOField field) {
        Collection props = associatedProperties.values();
        for (Iterator i = props.iterator(); i.hasNext();) {
            JDOProperty prop = (JDOProperty)i.next();
            if (prop.getAssociatedJDOField() == field) {
                // found property => return 
                return prop;
            }
        }

        // not found => return null
        return null;
    }

    /**
     * Returns the number of managed fields declared in the class represented
     * by this JDOClass. This does not include inherited fields.
     * @return number of declared managed fields
     */
    public int getDeclaredManagedFieldCount() {
        return getDeclaredManagedFields().length;
    }
    
    /**
     * Returns the number of inherited managed fields for the class
     * represented by this JDOClass.
     * @return number of inherited managed fields
     */
    public int getInheritedManagedFieldCount() {
        int count = 0;
        JDOClass superclass = getPersistenceCapableSuperclass();
        if (superclass != null) {
            count = 
                superclass.getInheritedManagedFieldCount() + 
                superclass.getDeclaredManagedFieldCount();
        }
    
        return count;
    }
    
    /**
     * Returns the number of managed fields for the class represented by this
     * JDOClass. The value returned by this method is equal to
     * <code>getDeclaredManagedFieldCount() +
     * getInheritedManagedFieldCount()</code>.
     * @return number of managed fields
     */
    public int getManagedFieldCount() {
        return getDeclaredManagedFieldCount() + getInheritedManagedFieldCount();
    }
    
    /**
     * Returns the package name including a terminating dot if this class has a 
     * package. The method returns the empty string if this class is in the 
     * default package.
     * @return package prefix for this class.
     */
    public String getPackagePrefix() {
        String className = getName();
        int index = className.lastIndexOf('.');
        return (index == -1) ? "" : className.substring(0, index + 1); //NOI18N
    }
    
    /**
     * Returns the least-derived (topmost) persistence-capable class in the 
     * hierarchy of this JDOClass. It returns this JDOClass if it has no 
     * persistence-capable superclass.
     * @return the topmost persistence-capable class in the hierarchy.
     */
    public JDOClass getPersistenceCapableRootClass() {
        JDOClass superclass = getPersistenceCapableSuperclass();
        if (superclass == null) {
            // no superclass => return this
            return this;
        }
        else {
            return superclass.getPersistenceCapableRootClass();
        }
    }

    //========= Internal helper methods ==========

    /**
     * Returns the JDOField definition for the specified field. 
     * The method expects unqualified field names. The method 
     * performs a bottom up lookup in the case of multiple fields 
     * with the same name in an inheritance hierarchy. So it starts
     * checking this class, then it checks its superclas, etc.
     * @param fieldName the unqualified field name
     * @return the corresponding JDOField instance if exists; 
     * <code>null</code> otherwise.
     */
    protected JDOField getFieldInternal(String fieldName) {
        // first check the declared fields
        JDOField field = (JDOField)declaredFields.get(fieldName);
        if (field != null) {
            return field;
        }
        
        // not in this class => check superclass
        JDOClassImplDynamic superclass = 
            (JDOClassImplDynamic)getPersistenceCapableSuperclass();
        if (superclass != null) {
            return superclass.getFieldInternal(fieldName);
        }
        
        // not found => return null
        return null;
    }
    
    /**
     * Returns the collection of identifying declared fields of this JDOClass
     * in the form of an array. The method returns the JDOField instances
     * declared by this JDOClass defined as primary key fields (see {@link
     * JDOField#isPrimaryKey}). 
     * @return the identifying fields of this JDOClass
     */
    protected JDOField[] getDeclaredPrimaryKeyFields() {
        JDOField[] fields = getDeclaredFields();
        JDOField[] tmp = new JDOField[fields.length];
        int length = 0;
        for (int i = 0; i < fields.length; i++) {
            JDOField field = fields[i];
            if (field.isManaged() && field.isPrimaryKey()) {
                tmp[length++] = field;
            }
        }
        // now fill the returned array 
        // the array should have the correct length
        JDOField[] result = new JDOField[length];
        System.arraycopy(tmp, 0, result, 0, length);

        return result;
    }

    /**
     * Returns a new instance of the JDOClass implementation class.
     */
    protected JDOClass newJDOClassInstance(String name) {
        return new JDOClassImplDynamic(name, this);
    }

    /**
     * Returns a new instance of the JDOField implementation class.
     */
    protected JDOField newJDOFieldInstance(String name) {
        return new JDOFieldImplDynamic(name, this);
    }

    /**
     * Returns a new instance of the JDOProperty implementation class.
     */
    protected JDOProperty newJDOPropertyInstance(String name) {
        return new JDOPropertyImplDynamic(name, this);
    }
    
    /**
     * Returns a new instance of the JDOProperty implementation class.
     */
    protected JDOProperty newJDOPropertyInstance(
        String name, JDOField associatedJDOField) throws ModelException {
        return new JDOAssociatedPropertyImplDynamic(
            name, this, associatedJDOField);
    }
    
}
