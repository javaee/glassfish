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

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaProperty;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOArray;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOCollection;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDOMap;
import com.sun.org.apache.jdo.model.jdo.JDOProperty;
import com.sun.org.apache.jdo.model.jdo.JDOReference;
import com.sun.org.apache.jdo.model.jdo.JDORelationship;
import com.sun.org.apache.jdo.util.I18NHelper;

/**
 * An instance of this class represents the JDO metadata of a managed property
 * of a persistence capable class. This JDOProperty implementation is used for
 * persistent properties with an associated JDOField. All JDOField getter
 * methods delegate to the associated JDOField, except methods getName,
 * getDeclaringClass and getJavaField. All JDOField setter method throw a
 * ModelException to avoid changing the associated JDOField through this
 * JDOProperty instance. This dynamic implementation only stores values
 * explicitly set by setter method. 
 *
 * @author Michael Bouschen
 * @since 2.0
 * @version 2.0
 */
public class JDOAssociatedPropertyImplDynamic
    extends JDOMemberImpl 
    implements JDOProperty
{
    /** The associated JDOField instance. */
    private final JDOField associatedJDOField;

    /** The corresponding JavaProperty instance. */
    protected JavaProperty javaProperty;

   /** I18N support. */
    protected final static I18NHelper msg =  
        I18NHelper.getInstance(JDOAssociatedPropertyImplDynamic.class);
    
    /** Constrcutor. */
    protected JDOAssociatedPropertyImplDynamic(
        String name, JDOClass declaringClass, JDOField associatedJDOField)
        throws ModelException {
        super(name, declaringClass);
        this.associatedJDOField = associatedJDOField;
        if (associatedJDOField == null) {
            throw new ModelException(
                msg.msg("EXC_InvalidNullAssociatedJDOField")); //NOI18N
        }
    }
    
    // ===== Methods specified in JDOField =====

    /**
     * Get the corresponding JavaProperty representation for this JDOProperty.
     * @return the corresponding JavaProperty representation
     */
    public JavaField getJavaField() {
        if (javaProperty != null) {
            // return javaProperty, if explicitly set by the setter
            return javaProperty;
        }
        
        // not set => calculate
        JavaType javaType = getDeclaringClass().getJavaType();
        return javaType.getJavaProperty(getName());
    }

    /**
     * Sets the corresponding JavaProperty representation for this JDOProperty.
     * @param javaField the corresponding JavaProperty representation
     * @throws ModelException if impossible
     */
    public void setJavaField(JavaField javaField) throws ModelException {
        
        if (javaField instanceof JavaProperty) {
            this.javaProperty = (JavaProperty)javaField;
        }
        else {
            throw new ModelException(msg.msg(
                "EXC_InvalidJavaFieldForJDOProperty", javaField)); //NOI18N
        }
    }

    /**
     * Convenience method to check whether this field represents a property.
     * @return <code>true</code> if this field represents a property; 
     * <code>false</code> otherwise
     */
    public boolean isProperty() {
        return true;
    }

    // ===== Methods specified in JDOField delegate to associatedJDOField =====

    /** Deletegate to associatedJDOField. */
    public int getPersistenceModifier() {
        return associatedJDOField.getPersistenceModifier();
    }

    /** Throws ModelException. */
    public void setPersistenceModifier (int persistenceModifier)
        throws ModelException {
        throw new ModelException(
            msg.msg("EXC_CannotModifyJDOProperty")); //NOI18N
    }
    
    /** Deletegate to associatedJDOField. */
    public boolean isPrimaryKey() {
        return associatedJDOField.isPrimaryKey();
    }
    
    /** Throws ModelException. */
    public void setPrimaryKey(boolean primaryKey)
        throws ModelException {
        throw new ModelException(
            msg.msg("EXC_CannotModifyJDOProperty")); //NOI18N
    }
    
    /** Deletegate to associatedJDOField. */
    public int getNullValueTreatment() {
        return associatedJDOField.getNullValueTreatment();
    }
    
    /** Throws ModelException. */
    public void setNullValueTreatment(int nullValueTreament)
        throws ModelException {
        throw new ModelException(
            msg.msg("EXC_CannotModifyJDOProperty")); //NOI18N
    }
    
    /** Deletegate to associatedJDOField. */
    public boolean isDefaultFetchGroup() {
        return associatedJDOField.isDefaultFetchGroup();
    }

    /** Throws ModelException. */
    public void setDefaultFetchGroup(boolean defaultFetchGroup)
        throws ModelException {
        throw new ModelException(
            msg.msg("EXC_CannotModifyJDOProperty")); //NOI18N
    }

    /** Deletegate to associatedJDOField. */
    public boolean isEmbedded() {
        return associatedJDOField.isEmbedded();
    }
    
    /** Throws ModelException. */
    public void setEmbedded(boolean embedded)
        throws ModelException {
        throw new ModelException(
            msg.msg("EXC_CannotModifyJDOProperty")); //NOI18N
    }

    /** Deletegate to associatedJDOField. */
    public boolean isSerializable() {
        return associatedJDOField.isSerializable();
    }

    /** Throws ModelException. */
    public void setSerializable(boolean serializable)
        throws ModelException {
        throw new ModelException(
            msg.msg("EXC_CannotModifyJDOProperty")); //NOI18N
    }

    /** Deletegate to associatedJDOField. */
    public String getMappedByName() {
        return associatedJDOField.getMappedByName();
    }

    /** Throws ModelException. */
    public void setMappedByName(String mappedByName)
        throws ModelException {
        throw new ModelException (
            msg.msg("EXC_CannotModifyJDOProperty")); //NOI18N
    }

    /** Deletegate to associatedJDOField. */
    public JDORelationship getRelationship() {
        return associatedJDOField.getRelationship();
    }

    /** Throws ModelException. */
    public void setRelationship(JDORelationship relationship)
        throws ModelException {
        throw new ModelException(
            msg.msg("EXC_CannotModifyJDOProperty")); //NOI18N
    }

    /** Throws ModelException. */
    public JDOReference createJDOReference()
        throws ModelException {
        throw new ModelException(
            msg.msg("EXC_CannotModifyJDOProperty")); //NOI18N
    }

    /** Throws ModelException. */
    public JDOCollection createJDOCollection()
        throws ModelException {
        throw new ModelException(
            msg.msg("EXC_CannotModifyJDOProperty")); //NOI18N
    }

    /** Throws ModelException. */
    public JDOArray createJDOArray()
        throws ModelException {
        throw new ModelException(
            msg.msg("EXC_CannotModifyJDOProperty")); //NOI18N
    }
    
    /** Throws ModelException. */
    public JDOMap createJDOMap()
        throws ModelException {
        throw new ModelException(
            msg.msg("EXC_CannotModifyJDOProperty")); //NOI18N
    }
    
    /** Deletegate to associatedJDOField. */
    public boolean isPersistent() {
        return associatedJDOField.isPersistent();
    }
    
    /** Deletegate to associatedJDOField. */
    public boolean isTransactional() {
        return associatedJDOField.isTransactional();
    }

    /** Deletegate to associatedJDOField. */
    public boolean isManaged() {
        return associatedJDOField.isManaged();
    }

    /** Deletegate to associatedJDOField. */
    public boolean isRelationship() {
        return associatedJDOField.isRelationship();
    }

    /** Deletegate to associatedJDOField. */
    public JavaType getType() {
        return associatedJDOField.getType();
    }

    /** Deletegate to associatedJDOField. */
    public int getFieldNumber() {
        return associatedJDOField.getFieldNumber();
    }

    /** Deletegate to associatedJDOField. */
    public int getRelativeFieldNumber() {
        return associatedJDOField.getRelativeFieldNumber();
    }

    // ===== Methods specified in JDOProperty =====

    /** 
     * Return the JDOField instance associated with this property, if
     * available. If there is no JDOField instance associated, then the method
     * returns <code>null</code>.
     * @return associated JDOField instance or <code>null</code> if not
     * available.
     */
    public JDOField getAssociatedJDOField() {
        return associatedJDOField;
    }
}
