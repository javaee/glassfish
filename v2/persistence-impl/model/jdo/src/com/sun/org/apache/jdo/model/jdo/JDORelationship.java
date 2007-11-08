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

/**
 * JDORelationship is the super interface for all interfaces representing 
 * JDO relationship metadata of a managed field of a persistence-capable class.
 * 
 * @author Michael Bouschen
 */
public interface JDORelationship 
    extends JDOElement 
{
    /**
     * Constant representing the cardinality zero used for lower and upper 
     * bounds.
     */
    public static final int CARDINALITY_ZERO = 0;

    /**
     * Constant representing the cardinality one used for lower and upper bounds.
     */
    public static final int CARDINALITY_ONE = 1;

    /**
     * Constant representing the cardinality n used for lower and upper bounds.
     */
    public static final int CARDINALITY_N = java.lang.Integer.MAX_VALUE;

    /** 
     * Get the lower cardinality bound for this relationship element.
     * @return the lower cardinality bound
     */
    public int getLowerBound();

    /** 
     * Set the lower cardinality bound for this relationship element.
     * @param lowerBound an integer indicating the lower cardinality bound
     * @exception ModelException if impossible
     */
    public void setLowerBound(int lowerBound)
        throws ModelException;
    
    /** 
     * Get the upper cardinality bound for this relationship element.
     * @return the upper cardinality bound
     */
    public int getUpperBound();

    /** 
     * Set the upper cardinality bound for this relationship element.
     * @param upperBound an integer indicating the upper cardinality bound
     * @exception ModelException if impossible
     */
    public void setUpperBound(int upperBound)
        throws ModelException;

    /** 
     * Get the declaring field of this JDORelationship.
     * @return the field that owns this JDORelationship, or <code>null</code>
     * if the element is not attached to any field
     */
    public JDOField getDeclaringField();

    /** 
     * Set the declaring field of this JDORelationship.
     * @param declaringField the declaring field of this relationship element
     * @exception ModelException if impossible
     */
    public void setDeclaringField(JDOField declaringField)
        throws ModelException;

    /**
     * Get the JDOClass corresponding to the type or element of this 
     * relationship.
     * @return the related class
     */
    public JDOClass getRelatedJDOClass();

    /** 
     * Get the mappedBy relationship. If there is no mappedBy relationship
     * set, the method checks the mappedBy name as specified in the declaring
     * field and resolves the relationship. The method return
     * <code>null</code> if there is no mappedBy relationship set and there
     * is no mappedBy name specified on the declaring field.
     * @return the mappedBy relationship if available; <code>null</code>
     * otherwise.
     */
    public JDORelationship getMappedBy();

    /**
     * Set the mappedBy relationship for this relationship. This method
     * automatically updates the mappedBy name of the declaring field of this
     * relationship.
     * @param mappedBy the mappedBy relationship.
     * @exception ModelException if impossible
     */
    public void setMappedBy(JDORelationship mappedBy) throws ModelException;

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
    public String getInverseRelationshipName();

    /**
     * Get the inverse JDORelationship in the case of a two-way relationship.
     * @return the inverse relationship
     */
    public JDORelationship getInverseRelationship();

    /**
     * Set the inverse JDORelationship in the case of a two-way relationship.
     * The two relationship elements involved are set as inverses of each 
     * other and the old inverse is unset.
     * @param inverseRelationship the inverse relationship
     * @exception ModelException if impossible
     * @deprecated - call setMappedBy instead
     */
    public void setInverseRelationship(JDORelationship inverseRelationship)
        throws ModelException;

    /**
     * Determines whether this side of a two-way relationship is the
     * owning side.
     * @return <code>true</code> if this side is the owning side;
     * <code>false</code> otherwise. 
     */
    public boolean isOwner();

    /**
     * Determines whether this JDORelationship represents a reference
     * relationship or not. A return of <code>true</code> means this
     * JDORelationship is a JDOReference instance.
     * @return <code>true</code> if this JDORelationship represents a
     * reference relationship; <code>false</code> otherwise.
     */
    public boolean isJDOReference();
    
    /**
     * Determines whether this JDORelationship represents a collection
     * relationship or not. A return of <code>true</code> means this
     * JDORelationship is a JDOCollection instance.
     * @return <code>true</code> if this JDORelationship represents a
     * collection relationship; <code>false</code> otherwise.
     */
    public boolean isJDOCollection();

    /**
     * Determines whether this JDORelationship represents an array
     * relationship or not. A return of <code>true</code> means this
     * JDORelationship is a JDOArray instance.
     * @return <code>true</code> if this JDORelationship represents an 
     * array relationship; <code>false</code> otherwise.
     */
    public boolean isJDOArray();

    /**
     * Determines whether this JDORelationship represents a map 
     * relationship or not. A return of <code>true</code> means this
     * JDORelationship is a JDOMap instance.
     * @return <code>true</code> if this JDORelationship represents a
     * map relationship; <code>false</code> otherwise.
     */
    public boolean isJDOMap();

}
