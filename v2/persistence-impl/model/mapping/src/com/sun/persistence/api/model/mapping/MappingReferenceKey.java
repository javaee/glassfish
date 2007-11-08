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
 * MappingReferenceKey.java
 *
 */


package com.sun.persistence.api.model.mapping;

import com.sun.forte4j.modules.dbmodel.*;
import com.sun.org.apache.jdo.model.ModelException;

/**
 * This is an object which represents a relationship between two tables.  It 
 * can be used both in the definition of secondary tables and relationships.
 * It can be thought of as a "fake foreign key" meaning it designates the 
 * column pairs used to define the join between the two tables.  It is 
 * analagous to a foreign key and may in fact contain identical pairs as the 
 * foreign key, but this is not a requirement.  The foreign key may define a 
 * different set of pairs or may not exist at all.  Although any set of pairs 
 * is legal, the user should be careful to define pairs which represent a 
 * logical relationship between the two tables.
 */
public interface MappingReferenceKey extends MappingMember, ReferenceKey {

    // <editor-fold desc="//======================= table handling ============================">

    /**
     * Returns the mapping table for this reference key.
     * @return the meta data table for this reference key
     */
    public MappingTable getMappingTable();

    /**
     * Set the mapping table for this reference key to the supplied table.
     * @param table mapping table to be used with this key.
     * @throws ModelException if impossible
     */
    public void setMappingTable(MappingTable table) throws ModelException;

    // </editor-fold>

    // <editor-fold desc="//==================== column pair handling =========================">

    /**
     * Returns the list of relative column pair names in this reference key.
     * @return the names of the column pairs in this reference key
     */
    public String[] getColumnPairNames();

    /**
     * Remove a column pair from the holder.  This method can be used to remove
     * a pair by name when it cannot be resolved to an actual pair.
     * @param pairName the relative name of the column pair to remove
     * @throws ModelException if impossible
     */
    public void removeColumnPair(String pairName) throws ModelException;

    /**
     * Remove some column pairs from the holder.  This method can be used to
     * remove pairs by name when they cannot be resolved to actual pairs.
     * @param pairNames the relative names of the column pairs to remove
     * @throws ModelException if impossible
     */
    public void removeColumnPairs(String[] pairNames) throws ModelException;

    /**
     * Set the column pairs for this holder. Previous column pairs are removed.
     * This method will automatically swap the direction of the pairs if 
     * necessary.
     * @param rk the reference key from which to extract the new column pairs
     * @throws ModelException if impossible
     */
    public void setColumnPairs(ReferenceKey rk) throws ModelException;

    // <editor-fold desc="//==================== ReferenceKey redefines =======================">

    //==== redefined from ReferenceKey to narrow Exception->ModelException ===

    /**
     * Add a new column pair to the holder.
     * @param pair the pair to add
     * @throws ModelException if impossible
     */
    public void addColumnPair(ColumnPairElement pair) throws ModelException;

    /**
     * Add some new column pairs to the holder.
     * @param pairs the column pairs to add
     * @throws ModelException if impossible
     */
    public void addColumnPairs(ColumnPairElement[] pairs)
            throws ModelException;

    /**
     * Remove a column pair from the holder.
     * @param pair the column pair to remove
     * @throws ModelException if impossible
     */
    public void removeColumnPair(ColumnPairElement pair) throws ModelException;

    /**
     * Remove some column pairs from the holder.
     * @param pairs the column pairs to remove
     * @throws ModelException if impossible
     */
    public void removeColumnPairs(ColumnPairElement[] pairs)
            throws ModelException;

    /**
     * Set the column pairs for this holder. Previous column pairs are removed.
     * @param pairs the new column pairs
     * @throws ModelException if impossible
     */
    public void setColumnPairs(ColumnPairElement[] pairs)
            throws ModelException;

    // </editor-fold>

    // </editor-fold>
}
