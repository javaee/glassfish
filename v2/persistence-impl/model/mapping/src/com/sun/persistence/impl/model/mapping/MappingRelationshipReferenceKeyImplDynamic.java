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
 * MappingRelationshipReferenceKeyImplDynamic.java
 *
 * Created on July 13, 2005, 12:20 PM
 */


package com.sun.persistence.impl.model.mapping;

import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.forte4j.modules.dbmodel.ReferenceKey;
import com.sun.forte4j.modules.dbmodel.TableElement;
import com.sun.forte4j.modules.dbmodel.util.NameUtil;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelVetoException;
import com.sun.persistence.api.model.mapping.MappingClass;
import com.sun.persistence.api.model.mapping.MappingRelationship;
import com.sun.persistence.api.model.mapping.MappingTable;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of MappingReferenceKey which is used for 
 * reference keys used to define relationships.  It overrides a few
 * methods used to provide and manipulate storage of the key columns since
 * the secondary table reference keys (superclass) don't store that directly, 
 * rather they have an attached MappingTable and delegate to its keys.
 *
 * @author Michael Bouschen
 * @author Rochelle Raccah
 */
public class MappingRelationshipReferenceKeyImplDynamic 
    extends MappingReferenceKeyImplDynamic {

    // <editor-fold desc="//===================== constants & variables =======================">

    private MappingRelationship declaringRelationship = null;
    private List keyCols;       // array of column names

    // </editor-fold>

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Create new MappingReferenceKeyImplDynamic with no corresponding name or 
     * declaring relationship.
     * This constructor should only be used for cloning and archiving.
     */
    public MappingRelationshipReferenceKeyImplDynamic() {
        this((String) null, null);
    }

    /**
     * Creates new MappingReferenceKeyImplDynamic with the corresponding name
     * and declaring relationship.
     * @param name the name of the reference key
     * @param declaringRelationship the relationship to which the reference key
     * belongs
     */
    public MappingRelationshipReferenceKeyImplDynamic(String name, 
            MappingRelationship declaringRelationship) {
        super(name);
        this.declaringRelationship = declaringRelationship;
    }

    /**
     * Creates new MappingReferenceKeyImplDynamic with the corresponding 
     * declaring class and declaring relationship.
     * @param declaringClass the class to attach to
     * @param declaringRelationship the relationship to which the reference key
     * belongs
     */
    public MappingRelationshipReferenceKeyImplDynamic(
            MappingClass declaringMappingClass, 
            MappingRelationship declaringRelationship) {
        super(declaringMappingClass);
        this.declaringRelationship = declaringRelationship;
    }

    // </editor-fold>

    // <editor-fold desc="//=================== MappingMemberImpl overrides ===================">

    /**
     * Overrides MappingMemberImpl's <code>equals</code> method to compare the 
     * declaring relationship in addition to the name and declaring class name 
     * of this mapping member. The method returns <code>false</code> if obj 
     * does not have the same dynamic type and a declaring relationship and 
     * declaring class with the same name as this mapping member.
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj argument;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if ((obj != null) && super.equals(obj)) {
            MappingRelationship declaringRel = 
                getDeclaringMappingRelationship();
            MappingRelationship objDeclaringRel = 
                ((MappingRelationshipReferenceKeyImplDynamic) obj).
                    getDeclaringMappingRelationship();

            return ((declaringRel == null)
                    ? (objDeclaringRel == null)
                    : declaringRel.equals(objDeclaringRel));
        }

        return false;
    }

    /**
     * Overrides MappingMemberImpl's <code>hashCode</code> method to return the 
     * hashCode of this member's name plus the hashCode of this mapping 
     * member's declaring relationship and declaring class.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        MappingRelationship declaringRel = getDeclaringMappingRelationship();

        return (super.hashCode() + 
            ((declaringRel == null) ? 0 : declaringRel.hashCode()));
    }

    // </editor-fold>

    // <editor-fold desc="//====== MappingReferenceKey & related convenience methods ==========">
    
    // <editor-fold desc="//==================== declaring relationship =======================">

    /**
     * Get the declaring relationship of this MappingReferenceKey.
     * @return the relationship that owns this MappingReferenceKey, or 
     * <code>null</code> if the element is not attached to any relationship
     */
    private MappingRelationship getDeclaringMappingRelationship() {
        return declaringRelationship;
    }

    // </editor-fold>


    // <editor-fold desc="//======================= table handling ============================">

    /**
     * Extracts and sets the mapping table for this reference key from the 
     * declaring table of the supplied column if it is not yet set.  
     * This implementation does nothing since there is generally no mapping 
     * table instance for a reference key used in a relationship.
     * @param column column from which to extract the table.
     * @throws ModelException if impossible
     */
    protected void extractMappingTable(ColumnElement column) 
        throws ModelException {
    }

    // </editor-fold>

    // <editor-fold desc="//==================== column pair handling =========================">

    /**
     * Adds a column to the list of key columns. This method is only called 
     * privately from addColumnPairs and assumes that the column is not 
     * <code>null</code>.   Note that this implementation which is used for 
     * MappingReferenceKey objects used in relationships, stores this key 
     * column in its own key list managed by this implementation class.
     * @param column column element to be added
     * @throws ModelException if impossible
     */
    protected void addKeyColumnInternal(ColumnElement column)
            throws ModelException {
        List key = getKey();
        String columnName = NameUtil.getRelativeMemberName(
                column.getName().getFullName());

        try {
            fireVetoableChange(PROP_KEY_COLUMNS, null, null);
            key.add(columnName);
            firePropertyChange(PROP_KEY_COLUMNS, null, null);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    /**
     * Returns the list of table key column names in this reference key. This 
     * method is protected since API users should call the 
     * <code>getColumnPairNames</code> method.  Note that this implementation 
     * which is used for MappingReferenceKey objects used in relationships, 
     * provides its own key list managed by this implementation class.
     * @return the names of the key columns in this reference key
     */
    protected List getKey() {
        if (keyCols == null) {
            keyCols = new ArrayList();
        }

        return keyCols;
    }

    /**
     * Internal method which takes a reference key and determines whether or 
     * not the pairs that make up that reference key would need to be swapped 
     * in order to be used as the pairs for this reference key.  Note that this 
     * implementation which is used for MappingReferenceKey objects used in 
     * relationships, overrides the method to return <code>true</code> if the 
     * declaring mapping class of this reference key has a primary table or 
     * secondary table which is only on the referenced side of the pairs of 
     * the supplied reference key.  If both tables involved in the supplied 
     * reference key are the same, this method returns <code>false</code> if 
     * relationship for this reference key is the owning side, otherwise, it 
     * returns <code>true</code>.
     * @param rk the reference key from which to extract pairs for this 
     * reference key
     * @return whether or not to swap the pairs of the supplied reference key
     * before using them as pairs in this reference key
     */
    protected boolean needsSwap(ReferenceKey rk) {
        // check for ST in the relationships as well
        if (rk != null) {
            MappingTable[] allTables = 
                getDeclaringMappingClass().getMappingTables();
            int i, count = ((allTables != null) ? allTables.length : 0);
            TableElement declaringTable = rk.getDeclaringTable();
            TableElement referencedTable = rk.getReferencedTable();

            // check for the case of both tables being the same
            // if they are, use the extra information of the owner of the 
            // relationship to determine whether to swap or not
            if (declaringTable.equals(referencedTable)) {
                return !getDeclaringMappingRelationship().
                    getJDORelationship().isOwner();
            }

            for (i = 0; i < count; i++) {
                MappingTable testTable = allTables[i];

                if (testTable.isEqual(declaringTable))
                    return false;
                if (testTable.isEqual(referencedTable))
                    return true;
            }
        }

        return false;
    }

    // </editor-fold>

    // </editor-fold>

}
