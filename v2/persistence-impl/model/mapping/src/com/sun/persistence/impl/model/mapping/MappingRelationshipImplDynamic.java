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
 * MappingRelationshipImplDynamic.java
 *
 * Created on March 3, 2000, 1:11 PM
 */


package com.sun.persistence.impl.model.mapping;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelVetoException;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDORelationship;
import com.sun.persistence.api.model.mapping.MappingClass;
import com.sun.persistence.api.model.mapping.MappingField;
import com.sun.persistence.api.model.mapping.MappingReferenceKey;
import com.sun.persistence.api.model.mapping.MappingRelationship;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Michael Bouschen
 * @author Rochelle Raccah
 */
public class MappingRelationshipImplDynamic extends MappingMemberImpl
        implements MappingRelationship {

    // <editor-fold desc="//===================== constants & variables =======================">

    private MappingField declaringField = null;

    private final Map referenceKeyMap;    // key: wrapped usage constants, value: MappingReferenceKey

    // </editor-fold>

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Create new MappingRelationshipImplDynamic with no corresponding name or
     * declaring class.  This constructor should only be used for cloning and
     * archiving.
     */
    public MappingRelationshipImplDynamic() {
        this(null, null);
    }

    /**
     * Create new MappingRelationshipImplDynamic with the corresponding name and
     * declaring field.
     * @param name the name of the relationship
     * @param declaringField the field to attach to
     */
    public MappingRelationshipImplDynamic(String name,
            MappingField declaringField) {
        super(name, ((declaringField != null) ? 
            declaringField.getDeclaringMappingClass() : null));
        this.declaringField = declaringField;
        this.referenceKeyMap = new HashMap(3);
    }

    // </editor-fold>

    // <editor-fold desc="//====== MappingRelationship & related convenience methods ==========">

    // <editor-fold desc="//=================== delegation to jdo model  ======================">

    /**
     * Get the associated JDORelationship of this relationship.
     * @return the associated JDORelationship object.
     */
    public JDORelationship getJDORelationship() {
        return getDeclaringMappingClass().getJDOClass().
                getField(getName()).getRelationship();
    }

    /**
     * Get the inverse JDORelationship of this relationship.
     * @return the inverse JDORelationship object.
     */
    private JDORelationship getInverseJDORelationship() {
        return getJDORelationship().getInverseRelationship();
    }

    // </editor-fold>

    // <editor-fold desc="//======================= declaring field ===========================">

    /**
     * Get the declaring field of this MappingRelationship.
     * @return the field that owns this MappingRelationship, or <code>null</code>
     *         if the element is not attached to any field
     */
    public MappingField getDeclaringMappingField() {
        return declaringField;
    }

    // </editor-fold>

    // <editor-fold desc="//==================== inverse relationship  ========================">

    /**
     * Get the inverse MappingRelationship of this relationship.  Note that 
     * this might return <code>null</code> even if the associated 
     * JDORelationship's inverse is set in the case that the mapping model is 
     * not yet fully populated.
     * @return the inverse MappingRelationship object.
     */
    public MappingRelationship getInverseMappingRelationship() {
        JDORelationship inverseJDORel = getInverseJDORelationship();

        if (inverseJDORel != null) {
            JDOField relatedField = inverseJDORel.getDeclaringField();
            String relatedFieldName = relatedField.getName();
            String relatedClassName = 
                relatedField.getDeclaringClass().getName();
            MappingClass relatedMappingClass = getDeclaringMappingClass().
                getDeclaringMappingModel().getMappingClass(relatedClassName);
            MappingField inverseField = ((relatedMappingClass != null) ? 
                relatedMappingClass.getMappingField(relatedFieldName) : null);
            
            return ((inverseField != null) ? 
                inverseField.getMappingRelationship() : null);
        }

        return null;
    }

    // </editor-fold>

    // <editor-fold desc="//==================== reference key handling =======================">

    /**
     * Returns the usage constant based on the corresponding JDORelationship
     * type.  This method throws an IllegalStateException in the case of
     * a JDOMap.
     * @return the usage constant that would make sense based on the 
     * JDORelationship type
     */    
    int getLogicalUsage() {
        JDORelationship jdoRelationship = getJDORelationship();

        if (jdoRelationship.isJDOMap())
            throw new IllegalStateException();
        if (jdoRelationship.isJDOReference())
            return USAGE_REFERENCE;

        return USAGE_ELEMENT;
    }

    private void checkValidUsage(int usage) {
        if ((usage != USAGE_ELEMENT) && (usage != USAGE_JOIN) && 
            (usage != USAGE_REFERENCE)) {
            throw new IllegalArgumentException();
        }
    }

    // same as below without trying for inverse
    private MappingReferenceKey getMappingReferenceKeyInternal(int usage) {
        checkValidUsage(usage);

        return (MappingReferenceKey) referenceKeyMap.get(
            new Integer(usage));
    }

    /**
     * Get the mapping reference key for the specified usage, one of: {@link
     * #USAGE_REFERENCE}, {@link #USAGE_JOIN}, or {@link #USAGE_ELEMENT}.  Note
     * that a join reference key can exist for any relationship in addition
     * to the reference or element reference key which must match the underlying
     * type of JDORelationship.
     * @param usage - an integer representing the usage of the reference key 
     * to be created, one of {@link#USAGE_REFERENCE}, {@link #USAGE_JOIN}, or 
     * {@link #USAGE_ELEMENT}.
     * @return a mapping reference used by this mapping relationship for the 
     * specified usage, or <code>null</code> if it does not exist.
     */
    public MappingReferenceKey getMappingReferenceKey(int usage) {
        MappingReferenceKey returnKey = getMappingReferenceKeyInternal(usage);

        // if the key does not exist and this is a bidirectional 
        // relationship, get the info from the other side and flip it
        if (returnKey == null) {
            returnKey = getInverseReferenceKey(usage);
        }

        return returnKey;
    }

    /**
     * Returns a new instance of the MappingReferenceKey implementation class.
     */
    protected MappingReferenceKey newMappingReferenceKeyInstance() {
        return new MappingRelationshipReferenceKeyImplDynamic(
            getDeclaringMappingClass(), this);
    }

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
            throws ModelException {
        checkValidUsage(usage);

        if ((usage != getLogicalUsage()) && (usage != USAGE_JOIN)) {
            throw new ModelException(
                    getMessageHelper().msg(
                            "mapping.usage_invalid", // NOI18N
                            getName()));
        }

        MappingReferenceKey referenceKey = 
            newMappingReferenceKeyInstance();

        try {
            fireVetoableChange(PROP_REFERENCE_KEYS, null, null);
            referenceKeyMap.put(new Integer(usage), referenceKey);
            firePropertyChange(PROP_REFERENCE_KEYS, null, null);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }

        return referenceKey;
    }

    /**
     * Removes the mapping reference key for the specified usage from 
     * this mapping relationship.
     * @param usage - an integer representing the usage of the reference key 
     * to be removed, one of {@link#USAGE_REFERENCE}, {@link #USAGE_JOIN}, or 
     * {@link #USAGE_ELEMENT}.
     * @throws ModelException if impossible
     */
    public void removeMappingReferenceKey(int usage)
            throws ModelException {
        String propertyName = PROP_COLUMNS;

        try {
            fireVetoableChange(propertyName, null, null);
            referenceKeyMap.remove(new Integer(usage));
            firePropertyChange(propertyName, null, null);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    /**
     * Determines whether this relationship is mapped using a join table or not.
     * @return <code>true</code> if the relationship uses a join reference key
     * in its mapping, <code>false</code> otherwise.
     */
    public boolean usesJoinTable() {
        return (getMappingReferenceKey(USAGE_JOIN) != null);
    }

    /**
     * Internal method which takes a usage constant and tries to find the  
     * corresponding reference key in the inverse relationship, if possible.  
     * If such a reference key is found, a new instance of MappingReferenceKey
     * is created and its pairs are set using the reference key that was found
     * (swapping the direction of the pairs if necessary).
     * The algorithm as follows:
     * 1) verify that there is a non-null inverse for this relationship, 
     * otherwise return <code>null</code>
     * 2) verify that the usage we are searching for is either USAGE_JOIN or a 
     * logical usage based on the JDORelationship type corresponding to this
     * relationship, otherwise return <code>null</code>
     * 3) if we are searching for USAGE_JOIN, verify that the inverse 
     * relationship indeed uses a join table, otherwise return <code>null</code>
     * 4) if we are searching for 
     * a) a usage other than USAGE_JOIN, and already determined that the 
     * inverse doesn't use a join table 
     * OR 
     * b) USAGE_JOIN 
     * (and steps 1-3 are okay), use the reference key found by asking the 
     * inverse relationship for its reference key for the logical usage based on 
     * its JDORelationship type
     * 5) if we are searching for a usage other than USAGE_JOIN and already 
     * determined that the inverse does use a join table (and steps 1-3 are 
     * okay), use the reference key found by asking the inverse relationship 
     * for its reference key for USAGE_JOIN
     * @param usage the usage constant for which to search for a reference key
     * @return a new reference key constructed from the inverse relationship, or 
     * <code>null</code> if there is none
     */
    private MappingReferenceKey getInverseReferenceKey(int usage) {
        MappingRelationship inverseRel = getInverseMappingRelationship();
        boolean usageIsJoin = (usage == USAGE_JOIN);

        if ((inverseRel != null) && 
                (usageIsJoin || (usage == getLogicalUsage()))) {
            MappingRelationshipImplDynamic inverseImpl = 
                (MappingRelationshipImplDynamic) inverseRel;
            boolean inverseUsesJoin = 
                (inverseImpl.getMappingReferenceKeyInternal(USAGE_JOIN) != null);
            int usageToSwap = ((!usageIsJoin && inverseUsesJoin) ?
                USAGE_JOIN : inverseImpl.getLogicalUsage());          
            MappingReferenceKey keyToSwap = ((usageIsJoin && !inverseUsesJoin) ? 
                null : inverseImpl.getMappingReferenceKeyInternal(usageToSwap));

            if (keyToSwap != null) {
                try {
                    MappingReferenceKey returnKey = 
                        newMappingReferenceKeyInstance();

                    returnKey.setColumnPairs(keyToSwap);

                    return returnKey;
                } catch (ModelException e) {
                    // do nothing, will return null below
                }
            }
        }

        return null;
    }

    // </editor-fold>

    // </editor-fold>
}
