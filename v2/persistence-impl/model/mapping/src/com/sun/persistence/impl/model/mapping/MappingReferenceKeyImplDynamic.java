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
 * MappingReferenceKeyImplDynamic.java
 *
 * Created on March 3, 2000, 1:11 PM
 */


package com.sun.persistence.impl.model.mapping;

import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.forte4j.modules.dbmodel.ColumnPairElement;
import com.sun.forte4j.modules.dbmodel.DBIdentifier;
import com.sun.forte4j.modules.dbmodel.ReferenceKey;
import com.sun.forte4j.modules.dbmodel.TableElement;
import com.sun.forte4j.modules.dbmodel.util.NameUtil;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelVetoException;
import com.sun.persistence.api.model.mapping.MappingClass;
import com.sun.persistence.api.model.mapping.MappingReferenceKey;
import com.sun.persistence.api.model.mapping.MappingTable;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of MappingReferenceKey whose direct instances are
 * used for reference keys used to define secondary tables.  A subclass is used
 * to define relationship reference keys.
 *
 * @author Michael Bouschen
 * @author Rochelle Raccah
 */
public class MappingReferenceKeyImplDynamic extends MappingMemberImpl
        implements MappingReferenceKey {

    // <editor-fold desc="//===================== constants & variables =======================">

    private static final String COLUMN_PAIR_SEPARATOR = ";";    // NOI18N
    private static final String ARROW_CONSTANT = "->";  // NOI18N
    private List referenceKey;	// array of column names
    private MappingTable table;

    // </editor-fold>

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Create new MappingReferenceKeyImplDynamic with no corresponding name.
     * This constructor should only be used for cloning and archiving.
     */
    public MappingReferenceKeyImplDynamic() {
        this((String) null);
    }

    /**
     * Creates new MappingReferenceKeyImplDynamic with the corresponding name
     * @param name the name of the reference key
     */
    public MappingReferenceKeyImplDynamic(String name) {
        super(name, null);
    }

    /**
     * Creates new MappingReferenceKeyImplDynamic with the corresponding 
     * declaring class
     * @param declaringClass the class to attach to
     */
    public MappingReferenceKeyImplDynamic(MappingClass declaringMappingClass) {
        super(null, declaringMappingClass);
    }

    /**
     * Creates new MappingReferenceKeyImplDynamic with a corresponding mapping
     * table.
     * @param table mapping table to be used with this key.
     */
    public MappingReferenceKeyImplDynamic(MappingTable mappingTable)
            throws ModelException {
        super(mappingTable.getName(), mappingTable.getDeclaringMappingClass());
        setTableInternal(mappingTable);
    }

    // </editor-fold>

    // <editor-fold desc="//=================== MappingMemberImpl overrides ===================">

    /**
     * Overrides MappingMemberImpl's <code>toString</code> method to return the 
     * a string that mentions there are no pairs if the name of this member is 
     * not yet set.
     * @return a string representation of the object
     */
    public String toString() {
        String returnString = super.toString();

        if (returnString == null) {
            returnString = getMessageHelper().
                msg("mapping.reference_key.no_pairs");   // NOI18N
        }

        return returnString;
    }

    // </editor-fold>

    // <editor-fold desc="//====== MappingReferenceKey & related convenience methods ==========">

    // <editor-fold desc="//======================= table handling ============================">

    // <editor-fold desc="//====================== ReferenceKey methods =======================">

    /**
     * Get the declaring table.  This method is provided as part of the
     * implementation of the ReferenceKey interface but should only be used when
     * a ReferenceKey object is used or by the runtime.
     * @return the table that owns this reference key, or
     *         <code>null</code> if the key is not attached to any table
     */
    public TableElement getDeclaringTable() {
        List locals = getReferenceKey();

        if ((locals != null) && (locals.size() > 0)) {
            String absoluteName = NameUtil.getAbsoluteMemberName(
                    getDeclaringMappingClass().getDatabaseSchemaName(),
                    locals.get(0).toString());

            return TableElement.forName(NameUtil.getTableName(absoluteName));
        }

        return null;
    }

    /**
     * Set the mapping table for this reference key to the mapping table based
     * on the name of the supplied table.  This method is provided as part of
     * the implementation of the ReferenceKey interface but should only be used
     * when a ReferenceKey object is used or by the runtime.
     * @param table table element to be used with this key.
     */
    public void setDeclaringTable(TableElement tableElement) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the referenced table of the reference key.  This method is provided
     * as part of the implementation of the ReferenceKey interface but should
     * only be used when a ReferenceKey object is used or by the runtime.
     * @return the referenced table
     */
    public TableElement getReferencedTable() {
        ColumnPairElement[] columnPairs = getColumnPairs();

        if ((columnPairs != null) && (columnPairs.length > 0)) {
            return columnPairs[0].getReferencedColumn().getDeclaringTable();
        }

        return null;
    }

    // </editor-fold>

    /**
     * Returns the mapping table for this reference key.
     * @return the meta data table for this reference key
     */
    public MappingTable getMappingTable() {
        return table;
    }

    /**
     * Set the mapping table for this reference key to the supplied table.
     * @param table mapping table to be used with this key.
     * @throws ModelException if impossible
     */
    public void setMappingTable(MappingTable mappingTable) throws ModelException {
        MappingTable old = getMappingTable();

        try {
            fireVetoableChange(PROP_TABLE, old, mappingTable);
            setTableInternal(mappingTable);
            firePropertyChange(PROP_TABLE, old, mappingTable);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    /**
     * Returns a new instance of the MappingTable implementation class.
     */
    protected MappingTable newMappingTableInstance(TableElement table,
            MappingClass declaringMappingClass) {
        return new MappingTableImplDynamic(table, declaringMappingClass);
    }
 
    /**
     * Set the mapping table for this reference key to the supplied table
     * without firing any property change events.
     * @param table mapping table to be used with this key.
     * @throws ModelException if impossible
     */
    private void setTableInternal(MappingTable table)
            throws ModelException {
        if (table == null) {
            throw new ModelException(
                getMessageHelper().msg("mapping.element.null_argument")); // NOI18N
        }

        this.table = table;

        if (null == getDeclaringMappingClass()) {
            setDeclaringMappingClass(table.getDeclaringMappingClass());
        }

        if (null == getName()) {
            setNameInternal(table.getName());
        }
    }

    /**
     * Extracts and sets the mapping table for this reference key from the 
     * declaring table of the supplied column if it is not yet set.  Since the 
     * mapping table is implicitly set via the secondary table pairs, it can be 
     * null at this point.
     * @param column column from which to extract the table.
     * @throws ModelException if impossible
     */
    protected void extractMappingTable(ColumnElement column) 
        throws ModelException {
        if (getMappingTable() == null) {
            setTableInternal(newMappingTableInstance(
                column.getDeclaringTable(), 
                getDeclaringMappingClass()));
        }
    }

    // </editor-fold>

    // <editor-fold desc="//==================== column pair handling =========================">

    /**
     * Adds a column to the list of key columns. This method is only called 
     * privately from addColumnPairs and assumes that the column is not 
     * <code>null</code>.   Note that this implementation which is used for 
     * MappingReferenceKey objects used as secondary tables, delegates this key 
     * column to the list managed by the MappingTable.
     * @param column column element to be added
     * @throws ModelException if impossible
     */
    protected void addKeyColumnInternal(ColumnElement column)
            throws ModelException {
       ((MappingTableImplDynamic) getMappingTable()).
                addKeyColumnInternal(column);
    }

    /**
     * Adds a column to the list of key columns in this reference key. This
     * method is only called privately from addColumnPairs and assumes that the
     * column is not <code>null</code>.
     * @param column column element to be added
     * @throws ModelException if impossible
     */
    private void addKeyColumn(ColumnElement column) throws ModelException {
        List referenceKeyInternal = getReferenceKey();
        String columnName = NameUtil.getRelativeMemberName(
                column.getName().getFullName());

        try {
            fireVetoableChange(PROP_KEY_COLUMNS, null, null);
            referenceKeyInternal.add(columnName);
            firePropertyChange(PROP_KEY_COLUMNS, null, null);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    /**
     * Returns the list of key column names in this reference key. This method
     * is private since API users should call the <code>getColumnPairNames</code>
     * method.
     * @return the names of the columns in this reference key
     */
    private List getReferenceKey() {
        if (referenceKey == null) {
            referenceKey = new ArrayList();
        }

        return referenceKey;
    }

    /**
     * Returns the list of table key column names in this reference key. This 
     * method is protected since API users should call the 
     * <code>getColumnPairNames</code> method.  Note that this implementation 
     * which is used for MappingReferenceKey objects used as secondary tables, 
     * gets this key list from the one managed by the MappingTable.
     * @return the names of the key columns in this reference key
     */
    protected List getKey() {
        return ((MappingTableImplDynamic) getMappingTable()).
            getKeyColumnNamesInternal();
    }

    /**
     * Returns the list of relative column pair names in this reference key.
     * @return the names of the column pairs in this reference key
     */
    private List getColumnPairNamesInternal() {
        List locals = getReferenceKey();
        List foreigns = getKey();
        int i, count = ((locals != null) ? locals.size() : 0);
        ArrayList pairs = new ArrayList();

        for (i = 0; i < count; i++) {
            pairs.add(locals.get(i) + COLUMN_PAIR_SEPARATOR + foreigns.get(i));
        }

        return pairs;
    }

    /**
     * Convenience method which takes a pair and returns its index.
     * @param searchPairName the relative name of the column pair for which to
     * look
     * @return the index of the column pair or -1 if not found
     */
    private int getIndexOfColumnPair(String searchPairName) {
        List myPairs = getColumnPairNamesInternal();
        int count = ((myPairs != null) ? myPairs.size() : 0);

        if (count > 0) {
            int i;

            for (i = 0; i < count; i++) {
                if (myPairs.get(i).equals(searchPairName)) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Returns the list of relative column pair names in this reference key.
     * @return the names of the column pairs in this reference key
     */
    public String[] getColumnPairNames() {
        List pairNames = getColumnPairNamesInternal();

        return (String[]) pairNames.toArray(new String[pairNames.size()]);
    }

    /**
     * Remove a column pair from the holder.  This method can be used to remove
     * a pair by name when it cannot be resolved to an actual pair.
     * @param pairName the relative name of the column pair to remove
     * @throws ModelException if impossible
     */
    public void removeColumnPair(String pairName) throws ModelException {
        removeColumnPairs(new String[]{pairName});
    }

    /**
     * Remove some column pairs from the holder.  This method can be used to
     * remove pairs by name when they cannot be resolved to actual pairs.
     * @param pairNames the relative names of the column pairs to remove
     * @throws ModelException if impossible
     */
    public void removeColumnPairs(String[] pairNames) throws ModelException {
        List refKey = getReferenceKey();
        List key = getKey();
        int i, count = ((pairNames != null) ? pairNames.length : 0);

        for (i = 0; i < count; i++) {
            String pairName = pairNames[i];
            int index = getIndexOfColumnPair(pairName);

            if (pairName != null) {
                try {
                    Object remove1 = null, remove2 = null;

                    fireVetoableChange(PROP_KEY_COLUMNS, null, null);

                    remove1 = key.remove(index);
                    remove2 = refKey.remove(index);

                    if ((remove1 == null) || (remove2 == null)) {
                        // if only 1 failed, put the other one back
                        if (remove1 != null) {
                            key.add(index, remove1);
                        } else if (remove2 != null) {
                            refKey.add(index, remove2);
                        }

                        throw new ModelException(getMessageHelper().msg(
                            "mapping.element.element_not_removed", // NOI18N
                                pairName));
                    }

                    firePropertyChange(PROP_KEY_COLUMNS, null, null);
                } catch (PropertyVetoException e) {
                    throw new ModelVetoException(e);
                }
            }
        }
    }

    /**
     * Internal method which takes a reference key and determines whether or 
     * not the pairs that make up that reference key would need to be swapped 
     * in order to be used as the pairs for this reference key.  This 
     * implementation returns <code>true</code> if the declaring mapping class
     * of this reference key has a primary table which is only on the 
     * referenced side of the pairs of the supplied reference key.  There is no
     * need to deal with the case of both tables in the supplied reference 
     * key being the same for secondary tables.
     * @param rk the reference key from which to extract pairs for this 
     * reference key
     * @return whether or not to swap the pairs of the supplied reference key
     * before using them as pairs in this reference key
     */
    protected boolean needsSwap(ReferenceKey rk) {
        if (rk != null) {
            MappingTable primaryTable = 
                getDeclaringMappingClass().getPrimaryMappingTable();
            
            if (primaryTable != null) {
                if (primaryTable.isEqual(rk.getDeclaringTable()))
                    return false;
                if (primaryTable.isEqual(rk.getReferencedTable()))
                    return true;
            }
        }

        return false;
    }

    /**
     * Set the column pairs for this holder. Previous column pairs are removed.
     * This method will automatically swap the direction of the pairs if 
     * necessary.
     * @param rk the reference key from which to extract the new column pairs
     * @throws ModelException if impossible
     */
    public void setColumnPairs(ReferenceKey rk) throws ModelException {
        if (rk != null) {
            ColumnPairElement[] pairs = rk.getColumnPairs();

            setColumnPairs((needsSwap(rk) ? getInversePairs(pairs) : pairs));
        }
    }

    /**
     * Internal method which takes an array of ColumnPairElements and swaps the 
     * local and referenced columns in each entry.
     * @param originalPairs the array of pairs to be swapped
     * @return an array containing swapped pairs
     */
    private ColumnPairElement[] getInversePairs(
            ColumnPairElement[] originalPairs) {
        int i, count = ((originalPairs != null) ? originalPairs.length : 0);
        ColumnPairElement[] inversePairs = new ColumnPairElement[count];

        for (i = 0; i < count; i++) {
            inversePairs[i] = getInversePair(originalPairs[i]);
        }

        return inversePairs;
    }

    /**
     * Internal method which takes a ColumnPairElement and swaps the 
     * local and referenced columns.
     * @param originalPairs the pair to be swapped
     * @return a swapped pair
     */
    private ColumnPairElement getInversePair(ColumnPairElement originalPair) {
        if (originalPair != null) {
            ColumnElement local = originalPair.getLocalColumn();
            ColumnElement foreign = originalPair.getReferencedColumn();
            TableElement newLocalTable = foreign.getDeclaringTable();

            // we don't need to explicitly handle the case where local or 
            // foreign is null because the explicitly set side will retain
            // the invalid strings, but when computing the inverse, we don't
            // want to consider the invalid strings as possible pairs
            if ((local != null) && (foreign != null)) {
                return newLocalTable.getColumnPair(DBIdentifier.create(
                    foreign.getName().getFullName() + COLUMN_PAIR_SEPARATOR + 
                    local.getName().getFullName()));
            }
        }

        return null;
    }

    // <editor-fold desc="//=============== ColumnPairElementHolder methods ===================">

    /**
     * Add a new column pair to the holder.
     * @param pair the pair to add
     * @throws ModelException if impossible
     */
    public void addColumnPair(ColumnPairElement pair) throws ModelException {
        addColumnPairs(new ColumnPairElement[]{pair});
    }

    /**
     * Add some new column pairs to the holder.
     * @param pairs the column pairs to add
     * @throws ModelException if impossible
     */
    public void addColumnPairs(ColumnPairElement[] pairs)
            throws ModelException {
        int i, count = ((pairs != null) ? pairs.length : 0);

        for (i = 0; i < count; i++) {
            ColumnPairElement pair = (ColumnPairElement) pairs[i];

            if (pair != null) {
                ColumnElement refColumn = pair.getReferencedColumn();

                // check if entire pair matches
                // OK only add it if it has not been added before.
                if (getIndexOfColumnPair(
                        NameUtil.getRelativeMemberName(
                                pair.getName().getFullName())) == -1) {

                    ColumnElement localColumn = pair.getLocalColumn();
                    
                    // set the name of this reference key to be based on the 
                    // two tables involved
                    if (i == 0) {
                        extractMappingTable(refColumn);
                        setNameInternal(localColumn.getDeclaringTable() + 
                            ARROW_CONSTANT + refColumn.getDeclaringTable());
                    }

                    addKeyColumnInternal(refColumn);
                    addKeyColumn(localColumn);
                } else {
                    // this part was blank -- do we want an error or skip here?
                }
            } else {
                throw new ModelException(
                         getMessageHelper().msg("mapping.element.null_argument"));  // NOI18N
            }
        }
    }

    /**
     * Remove a column pair from the holder.
     * @param pair the column pair to remove
     * @throws ModelException if impossible
     */
    public void removeColumnPair(ColumnPairElement pair) throws ModelException {
        removeColumnPairs(new ColumnPairElement[]{pair});
    }

    /**
     * Remove some column pairs from the holder.
     * @param pairs the column pairs to remove
     * @throws ModelException if impossible
     */
    public void removeColumnPairs(ColumnPairElement[] pairs)
            throws ModelException {
        int i, count = ((pairs != null) ? pairs.length : 0);
        String[] pairNames = new String[count];

        for (i = 0; i < count; i++) {
            ColumnPairElement pair = (ColumnPairElement) pairs[i];

            pairNames[i] =
                    NameUtil.getRelativeMemberName(
                            pair.getName().getFullName());
        }

        removeColumnPairs(pairNames);
    }

    /**
     * Set the column pairs for this holder. Previous column pairs are removed.
     * @param pairs the new column pairs
     * @throws ModelException if impossible
     */
    public void setColumnPairs(ColumnPairElement[] pairs)
            throws ModelException {
        removeColumnPairs(getColumnPairNames());	// remove the old ones
        addColumnPairs(pairs);				// add the new ones
    }

    /**
     * Get all column pairs in this holder.
     * @return the column pairs
     */
    public ColumnPairElement[] getColumnPairs() {
        String[] pairNames = getColumnPairNames();
        TableElement tableElement = getDeclaringTable();
        int i, count = ((pairNames != null) ? pairNames.length : 0);
        ColumnPairElement[] pairs = new ColumnPairElement[count];
        String databaseRoot = getDeclaringMappingClass().getDatabaseSchemaName();

        for (i = 0; i < count; i++) {
            String absoluteName = NameUtil.getAbsoluteMemberName(
                    databaseRoot, pairNames[i]);

            pairs[i] = (ColumnPairElement) tableElement.getMember(
                    DBIdentifier.create(absoluteName));
        }

        return pairs;
    }

    /**
     * Find a column pair by name.
     * @param name the name of the column pair for which to look
     * @return the column pair or <code>null</code> if not found
     */
    public ColumnPairElement getColumnPair(DBIdentifier name) {
        ColumnPairElement[] myPairs = getColumnPairs();
        int count = ((myPairs != null) ? myPairs.length : 0);
        String databaseRoot = getDeclaringMappingClass().getDatabaseSchemaName();

        if (count > 0) {
            String absoluteTableName = NameUtil.getAbsoluteTableName(
                    databaseRoot, getReferencedTable().toString());
            ColumnPairElement searchPair = 
                    (ColumnPairElement) TableElement.forName(absoluteTableName)
                    .getMember(name);
            int i;

            for (i = 0; i < count; i++) {
                if (myPairs[i].equals(searchPair)) {
                    return searchPair;
                }
            }
        }

        return null;
    }

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="//================== other ReferenceKey methods =====================">

    /**
     * Get the name of this reference key.
     * @return the name
     */
    public String getKeyName() {
        return getName();
    }

    /**
     * Set the name of this reference key.
     * @param name the name
     * @throws ModelException if impossible
     */
    public void setKeyName(String name) throws ModelException {
        setName(name.toString());
    }

    /**
     * Get all local columns in this reference key.  This method is provided as
     * part of the implementation of the ReferenceKey interface but should only
     * be used when a ReferenceKey object is used or by the runtime.
     * @return the columns
     */
    public ColumnElement[] getLocalColumns() {
        ColumnPairElement[] columnPairs = getColumnPairs();
        int i, count = ((columnPairs != null) ? columnPairs.length : 0);
        ColumnElement[] columns = new ColumnElement[count];

        for (i = 0; i < count; i++) {
            columns[i] = columnPairs[i].getLocalColumn();
        }

        return columns;
    }

    /**
     * Get all referenced columns in this reference key.  This method is
     * provided as part of the implementation of the ReferenceKey interface but
     * should only be used when a ReferenceKey object is used or by the
     * runtime.
     * @return the columns
     */
    public ColumnElement[] getReferencedColumns() {
        ColumnPairElement[] columnPairs = getColumnPairs();
        int i, count = ((columnPairs != null) ? columnPairs.length : 0);
        ColumnElement[] columns = new ColumnElement[count];

        for (i = 0; i < count; i++) {
            columns[i] = columnPairs[i].getReferencedColumn();
        }

        return columns;
    }

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="//===================== internal helper methods =====================">

    //=============== extra set methods needed for xml archiver ==============

    /**
     * Set the list of of key column names in this reference key.  This method
     * should only be used internally and for cloning and archiving.
     * @param referenceKey the list of names of the columns in this
     * reference key
     */
    public void setReferenceKey(List referenceKey) {
        this.referenceKey = referenceKey;
    }

    // </editor-fold>

}
