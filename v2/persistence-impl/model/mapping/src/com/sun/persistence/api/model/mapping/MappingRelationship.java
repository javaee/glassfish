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
 * MappingRelationship.java
 *
 */


package com.sun.persistence.api.model.mapping;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.jdo.JDORelationship;

/**
 * This is an object which represents a relationship between two classes.  The 
 * mapping portion should be set up as follows: When mapping a non-join table 
 * relationship, call the {@link #createMappingReferenceKey} method with 
 * USAGE_REFERENCE or USAGE_ELEMENT as appropriate, then 
 * {@link MappingReferenceKey#addColumnPair} once for each pair of columns 
 * between the local table and the foreign table.  When mapping a join table 
 * relationship, first call the {@link #createMappingReferenceKey} method with 
 * USAGE_JOIN, then {@link MappingReferenceKey#addColumnPair} once for each 
 * pair of columns between the local table and the join table.  Next, call the 
 * {@link #createMappingReferenceKey} again with USAGE_REFERENCE or 
 * USAGE_ELEMENT as appropriate, then {@link MappingReferenceKey#addColumnPair} 
 * once for each pair of columns between the join table and the foreign table. 
 * Note that the number of pairs in each reference key may differ and that the 
 * order of adding the two reference keys is not important.
 */
public interface MappingRelationship extends MappingMember {
    // <editor-fold desc="//================= reference key usage constants ====================">

    /**
     * Constant representing the usage of a reference key for a reference
     * relationship.
     */
    public static final int USAGE_REFERENCE = 0;

    /**
     * Constant representing the usage of a reference key for a relationship
     * which uses a join table.
     */
    public static final int USAGE_JOIN = 1;

    /**
     * Constant representing the usage of a reference key for a collection
     * relationship.
     */
    public static final int USAGE_ELEMENT = 2;

    // </editor-fold>

    // <editor-fold desc="//=================== delegation to jdo model  ======================">

    /**
     * Get the associated JDORelationship of this relationship.
     * @return the associated JDORelationship object.
     */
    public JDORelationship getJDORelationship();

    // </editor-fold>

    // <editor-fold desc="//======================= declaring field ===========================">

    /** 
     * Get the declaring field of this MappingRelationship.
     * @return the field that owns this MappingRelationship, or <code>null</code>
     * if the element is not attached to any field
     */
    public MappingField getDeclaringMappingField();

    // </editor-fold>

    // <editor-fold desc="//==================== inverse relationship  ========================">

    /**
     * Get the inverse MappingRelationship of this relationship.  Note that 
     * this might return <code>null</code> even if the associated 
     * JDORelationship's inverse is set in the case that the mapping model is 
     * not yet fully populated.
     * @return the inverse MappingRelationship object.
     */
    public MappingRelationship getInverseMappingRelationship();

    // </editor-fold>

    // <editor-fold desc="//=================== reference key handling ========================">

    /**
     * Get the mapping reference key for the specified usage, one of: {@link
     * #USAGE_REFERENCE}, {@link #USAGE_JOIN}, or {@link #USAGE_ELEMENT}.  Note
     * that a join reference key can exist for any relationship in addition
     * to the reference or element reference key which must match the underlying
     * type of JDORelationship.
     * @param usage - an integer representing the usage of the reference key 
     * to be returned, one of {@link#USAGE_REFERENCE}, {@link #USAGE_JOIN}, or 
     * {@link #USAGE_ELEMENT}.
     * @return a mapping reference used by this mapping relationship for the 
     * specified usage, or <code>null</code> if it does not exist.
     */
    public MappingReferenceKey getMappingReferenceKey(int usage);

    /**
     * Creates a mapping reference key for the specified usage, one of: {@link
     * #USAGE_REFERENCE}, {@link #USAGE_JOIN}, or {@link #USAGE_ELEMENT}.  Note
     * that a join reference key can be created for any relationship in addition
     * to the reference or element reference key which must match the underlying
     * type of JDORelationship.  The returned MappingReferenceKey is associated 
     * with this mapping relationship, but does not have any pairs. set.
     * @param usage - an integer representing the usage of the reference key 
     * to be created, one of {@link#USAGE_REFERENCE}, {@link #USAGE_JOIN}, or 
     * {@link #USAGE_ELEMENT}.
     * @throws ModelException if impossible or the usage doesn't match the 
     * underlying JDORelationship.
     * @return a mapping reference used by this mapping relationship for the 
     * specified usage.
     */
    public MappingReferenceKey createMappingReferenceKey(int usage)
            throws ModelException;

    /**
     * Removes the mapping reference key for the specified usage from 
     * this mapping relationship.
     * @param usage - an integer representing the usage of the reference key 
     * to be removed, one of {@link#USAGE_REFERENCE}, {@link #USAGE_JOIN}, or 
     * {@link #USAGE_ELEMENT}.
     * @throws ModelException if impossible
     */
    public void removeMappingReferenceKey(int usage)
        throws ModelException;

    /**
     * Determines whether this relationship is mapped using a join table or not.
     * @return <code>true</code> if the relationship uses a join reference key
     * in its mapping, <code>false</code> otherwise.
     */
    public boolean usesJoinTable();
 
    // </editor-fold>
}
