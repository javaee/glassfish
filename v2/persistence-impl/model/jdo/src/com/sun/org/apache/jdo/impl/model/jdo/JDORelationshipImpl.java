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
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDORelationship;

/**
 * JDORelationship is the super interface for all interfaces representing 
 * JDO relationship metadata of a managed field of a persistence capable class.
 * 
 * @author Michael Bouschen
 * @version 2.0
 */
public abstract class JDORelationshipImpl extends JDOElementImpl
    implements JDORelationship {
    
    /** Property lowerBound. No default. */
    private int lowerBound;

    /** Property upperBound. No default. */
    private int upperBound;

    /** Relationship JDOField<->JDORelationship. */
    private JDOField declaringField;

    /** Relationship JDORelationship<->JDORelationship. */
    protected JDORelationship mappedBy;

    /** Name of the field which is the inverse relationship */
    private String inverseName;

    /** Relationship JDORelationship<->JDORelationship. */
    protected JDORelationship inverse;

    /** 
     * Get the lower cardinality bound for this relationship element.
     * @return the lower cardinality bound
     */
    public int getLowerBound() {
        return lowerBound;
    }

    /** 
     * Set the lower cardinality bound for this relationship element.
     * @param lowerBound an integer indicating the lower cardinality bound
     */
    public void setLowerBound(int lowerBound) {
        this.lowerBound = lowerBound;
    }
    
    /** 
     * Get the upper cardinality bound for this relationship element.
     * @return the upper cardinality bound
     */
    public int getUpperBound() {
        return upperBound;
    }

    /** 
     * Set the upper cardinality bound for this relationship element.
     * @param upperBound an integer indicating the upper cardinality bound
     */
    public void setUpperBound(int upperBound)
    {
        this.upperBound = upperBound;
    }

    /** 
     * Get the declaring field of this JDORelationship.
     * @return the field that owns this JDORelationship, or <code>null</code>
     * if the element is not attached to any field
     */
    public JDOField getDeclaringField() {
        return declaringField;
    }

    /** 
     * Set the declaring field of this JDORelationship.
     * @param declaringField the declaring field of this relationship element
     */
    public void setDeclaringField(JDOField declaringField) {
        this.declaringField = declaringField;
    }

    /**
     * Get the JDOClass corresponding to the type or element of this 
     * relationship.
     * @return the related class
     */
    public JDOClass getRelatedJDOClass() {
        JavaType relatedType = getRelatedJavaType();

        if (relatedType != null) {
            JDOClass myClass = getDeclaringField().getDeclaringClass();
            String relatedTypeName = relatedType.getName();

            if (relatedTypeName.equals(myClass.getName()))
                return myClass;
        
            return myClass.getDeclaringModel().getJDOClass(relatedTypeName);
        }

        return null;
    }

    /** 
     * Get the mappedBy relationship. If there is no mappedBy relationship
     * set, the method checks the mappedBy name as specified in the declaring
     * field and resolves the relationship. The method return
     * <code>null</code> if there is no mappedBy relationship set and there
     * is no mappedBy name specified on the declaring field.
     * @return the mappedBy relationship if available; <code>null</code>
     * otherwise.
     */
    public JDORelationship getMappedBy() {
        if (mappedBy != null) {
            // return mappedBy relationship, if explicitly set by the setter
            return mappedBy;
        }
        
        // not set => check mappedByName of declaring field
        JDOField field = getDeclaringField();
        String mappedByName = field.getMappedByName();
        if (mappedByName != null) {
            // return a JDORelationship instance for the mappedBy field name
            // as stored in the declaring field
            return getInverseRelationship();
        }
        
        return null;
    }

    /**
     * Set the mappedBy relationship for this relationship. This method
     * automatically updates the mappedBy name of the declaring field of this
     * relationship.
     * @param mappedBy the mappedBy relationship.
     * @exception ModelException if impossible
     */
    public void setMappedBy(JDORelationship mappedBy) throws ModelException {
        this.mappedBy = mappedBy;
        String mappedByName = null;
        if (mappedBy != null) {
            JDOField declaringField = mappedBy.getDeclaringField();
            if (declaringField != null) {
                mappedByName = declaringField.getName();
            }
        }
        getDeclaringField().setMappedByName(mappedByName);
        setInverseRelationship(mappedBy);
    }

    /** 
     * Get the relative name of the inverse relationship field for this
     * relationship.  In the case of two-way relationships, the two
     * relationships involved are inverses of each other.  If this
     * relationship element does not participate in a two-way relationship,
     * this returns <code>null</code>.  Note that it is possible to have
     * this method return a value, but because of the combination of
     * related class and lookup, there may be no corresponding
     * JDORelationship which can be found.
     * @return the relative name of the inverse JDORelationship
     * @see #getInverseRelationship
     */
    public String getInverseRelationshipName() {
        if (inverseName != null) {
            // return inverseName, if explicitly set
            return inverseName;
        }
        
        JDOField declaringField = getDeclaringField();
        String mappedByName = declaringField.getMappedByName();
        if (mappedByName != null) {
            // return mappedByName, if explicitly set on the declaring field
            return mappedByName;
        }

        // try to resolve relationship info from mappedByName of the field on
        // the other side
        UnresolvedRelationshipHelper info = getUnresolvedRelationshipHelper();
        // look for an unresolved relationship entry where the name of this
        // field is used as the mappedByName
        JDOField inverseField = 
            info.resolve(declaringField.getName(), getRelatedJDOClass());
        if (inverseField != null) {
            // found inverse => update inverseName and return it
            inverseName = inverseField.getName();
            return inverseName;
        }

        // no inverse name available => return null
        return null;
    }

    /**
     * Get the inverse JDORelationship in the case of a two-way relationship.
     * @return the inverse relationship
     */
    public JDORelationship getInverseRelationship() {
        if (inverse != null) {
            // return inverse relationship, if explicitly set by the setter
            return inverse;
        }

        // not set => check inverse name 
        String fieldName = getInverseRelationshipName();
        if (fieldName != null) {
            JDOClass relatedClass = getRelatedJDOClass();
            JDOField relatedField = relatedClass.getField(fieldName);
            if (relatedField != null)
                return relatedField.getRelationship();
        }
        return null;
    }

    /**
     * Set the inverse JDORelationship in the case of a two-way relationship.
     * The two relationship elements involved are set as inverses of each 
     * other and the old inverse is unset.
     * <p>
     * Warning: this methods casts the existing and the specified inverse
     * relationship instance to JDORelationshipImpl.
     * @param inverseRelationship the inverse relationship
     */
    public void setInverseRelationship(JDORelationship inverseRelationship) 
        throws ModelException {
        
        // Skip setting the inverse, if already it is set to the specified
        // instance. Note, do not use the result of getInverseRelationship for
        // this check, since it might calculate an inverse relationship
        // instance that is identical to the specified one, although the
        // inverse is not yet set.
        if (this.inverse == inverseRelationship) {
            return;
        }

        // clear old inverse which still points to here
        JDORelationshipImpl old = 
            (JDORelationshipImpl) getInverseRelationship();
        if (old != null) {
            if (this.equals(old.getInverseRelationship()))
                old.changeInverseRelationship(null);
        }

        // link from here to new inverse
        changeInverseRelationship(inverseRelationship);

        // link from new inverse back to here
        if (inverseRelationship != null) {
            ((JDORelationshipImpl) inverseRelationship).
                changeInverseRelationship(this);
        }
    }

    /**
     * Determines whether this side of a two-way relationship is the
     * owning side.
     * @return <code>true</code> if this side is the owning side;
     * <code>false</code> otherwise. 
     */
    public boolean isOwner() {
        return getMappedBy() == null;
    }

    /**
     * Determines whether this JDORelationship represents a reference
     * relationship or not. A return of <code>true</code> means this
     * JDORelationship is a JDOReference instance.
     * @return <code>true</code> if this JDORelationship represents a
     * reference relationship; <code>false</code> otherwise.
     */
    public boolean isJDOReference() {
        return false;
    }
    
    /**
     * Determines whether this JDORelationship represents a collection
     * relationship or not. A return of <code>true</code> means this
     * JDORelationship is a JDOCollection instance.
     * @return <code>true</code> if this JDORelationship represents a
     * collection relationship; <code>false</code> otherwise.
     */
    public boolean isJDOCollection() {
        return false;
    }

    /**
     * Determines whether this JDORelationship represents an array
     * relationship or not. A return of <code>true</code> means this
     * JDORelationship is a JDOArray instance.
     * @return <code>true</code> if this JDORelationship represents an 
     * array relationship; <code>false</code> otherwise.
     */
    public boolean isJDOArray() {
        return false;
    }

    /**
     * Determines whether this JDORelationship represents a map 
     * relationship or not. A return of <code>true</code> means this
     * JDORelationship is a JDOMap instance.
     * @return <code>true</code> if this JDORelationship represents a
     * map relationship; <code>false</code> otherwise.
     */
    public boolean isJDOMap() {
        return false;
    }

    //========= Internal helper methods ==========

    /** 
     * Get the type representation of the relationship. This will be 
     * the JavaType for references, the element type for collections
     * and arrays, and the value type for maps.
     * @return the relationship type
     */
    public abstract JavaType getRelatedJavaType();

    /** Changes the inverse relationship element for this relationship 
     * element.
     * This method is invoked for both sides from
     * {@link #setInverseRelationship} and should handle setting the 
     * internal variable.
     * @param inverseRelationship - a relationship element to be used as the
     * inverse for this relationship element or <code>null</code> if this
     * relationship element does not participate in a two-way relationship.
     * @exception ModelException if impossible
     */
    private void changeInverseRelationship(JDORelationship 
        inverseRelationship) throws ModelException {
        this.inverse = inverseRelationship;
        this.inverseName = ((inverseRelationship == null) ? null :
            inverseRelationship.getDeclaringField().getName());
    }

    /** 
     * Returns the UnresolvedRelationshipHelper instance from the declaring
     * field.
     * @return the current UnresolvedRelationshipHelper
     */
    private UnresolvedRelationshipHelper getUnresolvedRelationshipHelper() {
        return ((JDOFieldImplDynamic) getDeclaringField()).
            getUnresolvedRelationshipHelper();
    }    
}
