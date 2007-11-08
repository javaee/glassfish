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
 * MappingTable.java
 *
 */


package com.sun.persistence.api.model.mapping;

import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.forte4j.modules.dbmodel.TableElement;
import com.sun.org.apache.jdo.model.ModelException;

/**
 * This is an object which represents a database table. It exists (separately
 * from TableElement in the database model) to allow the runtime to use a
 * description of the underlying table that differs from the actual database.
 * For example, mapping table contains a key which can be thought of as a "fake
 * primary key" and designates the columns which the runtime will use to
 * identify rows.  It is analagous to the primary key of the underlying database
 * table and is typically the same, however the important point is that it is
 * not a requirement.  The table in the database may have a different primary
 * key or may have no primary key at all.  Similarly, the mapping table contains
 * a list of reference keys which can be thought of as "fake foreign key"
 * objects and designate the column pairs used to join the primary table with a
 * secondary table.  These are analagous to foreign keys and may in fact contain
 * identical pairs as the foreign key, but again, this is not a requirement.
 * The foreign key may define a different set of pairs or may not exist at all.
 * Although any set of pairs is legal, the user should be careful to define
 * pairs which represent a logical relationship between the two tables. Any
 * mapping table objects which are designated as primary tables have their key
 * set up automatically.  Any mapping table objects which are designated as
 * secondary tables should not have their keys set up directly; the setup is
 * automatically part of the pair definition which makes up the reference key.
 */
public interface MappingTable extends MappingMember {
    // <editor-fold desc="//======================= table handling ============================">

    /**
     * Returns the name of the table element used by this mapping table.
     * @return the table name for this mapping table
     */
    public String getTableName();

    /**
     * Returns the table element used by this mapping table.
     * @return the table name for this mapping table
     */
    public TableElement getTable();

    /**
     * Set the table element for this mapping table to the supplied table.
     * @param table table element to be used by the mapping table.
     * @throws ModelException if impossible
     */
    public void setTable(TableElement table) throws ModelException;

    /**
     * Returns true if the table element used by this mapping table is equal to
     * the supplied table.
     * @return <code>true</code> if table elements are equal, <code>false</code>
     *         otherwise.
     */
    public boolean isEqual(TableElement table);

    // </editor-fold>

    // <editor-fold desc="//==================== primary key handling =========================">

    /**
     * Returns the list of column names in the primary key for this mapping
     * table.
     * @return the names of the columns in the primary key for this mapping
     *         table
     */
    public String[] getKeyColumnNames();

    /**
     * Returns the list of column elements in the primary key for this mapping
     * table.
     * @return the column elements in the primary key for this mapping table
     */
    public ColumnElement[] getKeyColumns();

    /**
     * Adds a column to the primary key of columns in this mapping table. This
     * method should only be used to manipulate the key columns of the primary
     * table.  The secondary table key columns should be manipulated using
     * MappingReferenceKey methods for pairs.
     * @param column column element to be added
     * @throws ModelException if impossible
     */
    public void addKeyColumn(ColumnElement column) throws ModelException;

    /**
     * Removes a column from the primary key of columns in this mapping table.
     * This method should only be used to manipulate the key columns of the
     * primary table.  The secondary table key columns should be manipulated
     * using MappingReferenceKey methods for pairs.
     * @param columnName the relative name of the column to be removed
     * @throws ModelException if impossible
     */
    public void removeKeyColumn(String columnName) throws ModelException;

    // </editor-fold>

    // <editor-fold desc="//=================== reference key handling ========================">

    /**
     * Returns the list of keys (MappingReferenceKeys) for this mapping table.
     * There will be keys for foreign keys and "fake" foreign keys.
     * @return the reference key objects for this mapping table
     */
    public MappingReferenceKey[] getMappingReferenceKeys();

    /**
     * Scans through this mapping table's list of keys (MappingReferenceKeys) 
     * looking for a secondary table which matches the specified table.
     * @param the secondary table to find
     * @return the reference key object whose reference table matches
     * the specified table
     */
    public MappingReferenceKey getMappingReferenceKey(
            MappingTable secondaryTable);

    /**
     * Creates a mapping reference key. The returned MappingReferenceKey
     * is associated with this mapping table, but does not have any pairs. set.
     * @return a mapping reference used by this mapping table.
     */
    public MappingReferenceKey createMappingReferenceKey()
            throws ModelException;

    /**
     * Removes the specified mapping reference key from list of keys in
     * this mapping table.
     * @param key mapping reference key to be removed
     * @throws ModelException if impossible
     */
    public void removeMappingReferenceKey(MappingReferenceKey key)
        throws ModelException;

    // </editor-fold>
}
