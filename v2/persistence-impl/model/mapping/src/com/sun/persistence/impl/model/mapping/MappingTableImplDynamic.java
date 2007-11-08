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
 * MappingTableImplDynamic.java
 *
 * Created on March 3, 2000, 1:11 PM
 */


package com.sun.persistence.impl.model.mapping;

import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.forte4j.modules.dbmodel.TableElement;
import com.sun.forte4j.modules.dbmodel.util.NameUtil;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelVetoException;
import com.sun.persistence.api.model.mapping.MappingClass;
import com.sun.persistence.api.model.mapping.MappingReferenceKey;
import com.sun.persistence.api.model.mapping.MappingTable;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Michael Bouschen
 * @author Rochelle Raccah
 */
public class MappingTableImplDynamic extends MappingMemberImpl
        implements MappingTable {

    // <editor-fold desc="//===================== constants & variables =======================">

    private List keyColumnNames;	// array of column names
    //@olsen: made transient to prevent from serializing into mapping files
    private transient List keyColumns;	// array of ColumnElement (for runtime)
    private List referenceKeys;	// array of MappingReferenceKey
    private String tableName;
    //@olsen: made transient to prevent from serializing into mapping files
    private transient TableElement table;	// for runtime

    // </editor-fold>

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Create new MappingTableImplDynamic with no corresponding name or declaring
     * class.  This constructor should only be used for cloning and archiving.
     */
    public MappingTableImplDynamic() {
        this((String) null, null);
    }

    /**
     * Create new MappingTableImplDynamic with the corresponding name and declaring
     * class.
     * @param name the name of the element
     * @param declaringClass the class to attach to
     */
    protected MappingTableImplDynamic(String name, MappingClass declaringClass) {
        super(name, declaringClass);
    }

    /**
     * Creates new MappingTableImplDynamic with a corresponding table and declaring
     * class.
     * @param table table element to be used by the mapping table.
     * @param declaringClass the class to attach to
     */
    protected MappingTableImplDynamic(TableElement table, MappingClass declaringClass) {
        this(table.toString(), declaringClass);

        // don't use setTable so as not to fire property change events
        tableName = getName();
    }

    // </editor-fold>

    // <editor-fold desc="//================== MappingMemberImpl overrides ====================">

    /**
     * Override method in MappingElementImpl to set the tableName variable if
     * necessary (used for unarchiving).
     * @param name the name
     * @throws ModelException if impossible
     */
    public void setName(String name) throws ModelException {
        super.setName(name);

        if (getTable() == null) {
            tableName = name;
        }
    }

    // </editor-fold>

    // <editor-fold desc="//========= MappingTable & related convenience methods ==============">

    // <editor-fold desc="//======================= table handling ============================">

    /**
     * Returns the name of the table element used by this mapping table.
     * @return the table name for this mapping table
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Returns the table element used by this mapping table.
     * @return the table name for this mapping table
     */
    public TableElement getTable() {
        return getTableObject();
    }

    /**
     * Set the table element for this mapping table to the supplied table.
     * @param table table element to be used by the mapping table.
     * @throws ModelException if impossible
     */
    public void setTable(TableElement table) throws ModelException {
        String old = getTableName();
        String newName = table.toString();

        try {
            fireVetoableChange(PROP_TABLE, old, newName);
            tableName = newName;
            firePropertyChange(PROP_TABLE, old, newName);
            setName(tableName);

            // sync up runtime's object too: force next
            // access to getTableObject to recompute it
            table = null;
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    /**
     * Returns true if the table element used by this mapping table is equal to
     * the supplied table.
     * @return <code>true</code> if table elements are equal, <code>false</code>
     * otherwise.
     */
    public boolean isEqual(TableElement table) {
        return ((table != null) ? getTable().equals(table) : false);
    }

    //============= extra object support for runtime ========================

    /**
     * Returns the table element (TableElement) used by this mapping table. This
     * method should only be used by the runtime.
     * @return the table element for this mapping table
     */
    private TableElement getTableObject() {
        if (table == null) {
            String absoluteTableName = NameUtil.getAbsoluteTableName(
                    getDeclaringMappingClass().getDatabaseSchemaName(),
                    tableName);

            table = TableElement.forName(absoluteTableName);
        }

        return table;
    }

    //=============== extra set methods needed for xml archiver ==============

    /**
     * Set the name of the table element used by this mapping table.  This
     * method should only be used internally and for cloning and archiving.
     * @param table the table name for this mapping table
     */
    public void setTable(String table) {
        tableName = table;
    }

    // </editor-fold>

    // <editor-fold desc="//==================== primary key handling =========================">

    /**
     * Returns the list of column names in the primary key for this mapping
     * table.
     * @return the names of the columns in the primary key for this mapping
     *         table
     */
    List getKeyColumnNamesInternal() {
        if (keyColumnNames == null) {
            keyColumnNames = new ArrayList();
        }

        return keyColumnNames;
    }

    /**
     * Returns the list of column names in the primary key for this mapping
     * table.
     * @return the names of the columns in the primary key for this mapping
     *         table
     */
    public String[] getKeyColumnNames() {
        List keyNames = getKeyColumnNamesInternal();

        return (String[]) keyNames.toArray(new String[keyNames.size()]);
    }

    /**
     * Returns the list of column elements in the primary key for this mapping
     * table.
     * @return the column elements in the primary key for this mapping table
     */
    public ColumnElement[] getKeyColumns() {
        List keyCols = getKeyObjects();

        return (ColumnElement[]) keyCols.toArray(
            new ColumnElement[keyCols.size()]);
    }

    /**
     * Adds a column to the primary key of columns in this mapping table. This
     * method should only be used to manipulate the key columns of the primary
     * table.  The secondary table key columns should be manipulated using
     * MappingReferenceKey methods for pairs.
     * @param column column element to be added
     * @throws ModelException if impossible
     */
    public void addKeyColumn(ColumnElement column) throws ModelException {
        if (column != null) {
            String columnName = NameUtil.getRelativeMemberName(
                    column.getName().getFullName());

            if (!getKeyColumnNamesInternal().contains(columnName)) {
                addKeyColumnInternal(column);
            } else {
                // this part was blank -- do we want an error or skip here?
            }
        } else {
            throw new ModelException(
                    getMessageHelper().msg("mapping.element.null_argument"));				// NOI18N
        }
    }

    /**
     * Adds a column to the primary key of columns in this mapping table. This
     * method is used internally to manipulate primary key columns that have
     * passed the null and duplicate tests in addKeyColumn and secondary table
     * key columns when pairs are being set up and ignoring duplicates is done
     * at the pair level.
     * @param column column element to be added
     * @throws ModelException if impossible
     */
    protected void addKeyColumnInternal(ColumnElement column)
            throws ModelException {
        List key = getKeyColumnNamesInternal();
        String columnName = NameUtil.getRelativeMemberName(
                column.getName().getFullName());

        try {
            fireVetoableChange(PROP_KEY_COLUMNS, null, null);
            key.add(columnName);
            firePropertyChange(PROP_KEY_COLUMNS, null, null);

            // sync up runtime's object list too
            //@olsen: rather clear objects instead of maintaining them
            //getKeyObjects().add(column);
            keyColumns = null;
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    /**
     * Removes a column from the primary key of columns in this mapping table.
     * This method should only be used to manipulate the key columns of the
     * primary table.  The secondary table key columns should be manipulated
     * using MappingReferenceKey methods for pairs.
     * @param columnName the relative name of the column to be removed
     * @throws ModelException if impossible
     */
    public void removeKeyColumn(String columnName) throws ModelException {
        if (columnName != null) {
            try {
                fireVetoableChange(PROP_KEY_COLUMNS, null, null);

                if (!getKeyColumnNamesInternal().remove(columnName)) {
                    throw new ModelException(
                            getMessageHelper().msg(
                                    "mapping.element.element_not_removed", // NOI18N
                                    columnName));
                }

                firePropertyChange(PROP_KEY_COLUMNS, null, null);

                // sync up runtime's object list too
                //@olsen: rather clear objects instead of maintaining them
                //getKeyObjects().remove(column);
                keyColumns = null;
            } catch (PropertyVetoException e) {
                throw new ModelVetoException(e);
            }
        }
    }

    /**
     * Returns the list of columns (ColumnElements) in the primary key for this
     * mapping table.  This method should only be used by the runtime.
     * @return the column elements in the primary key for this mapping table
     */
    private List getKeyObjects() {
        if (keyColumns == null) {
            //@olsen: calculate the key objects based on
            //        the key names as stored in keyColumnNames
            //keyColumns = new ArrayList();
            keyColumns = MappingClassImplDynamic.toColumnObjects(
                    getDeclaringMappingClass().getDatabaseSchemaName(),
                    getKeyColumnNamesInternal());
        }

        return keyColumns;
    }

    /**
     * Set the list of column names in the primary key for this mapping table.
     * This method should only be used internally and for cloning and
     * archiving.
     * @param key the list of names of the columns in the primary key for this
     * mapping table
     */
    public void setKey(List key) {
        keyColumnNames = key;
    }

    // </editor-fold>

    // <editor-fold desc="//=================== reference key handling ========================">

    /**
     * Returns the list of keys (MappingReferenceKeys) for this mapping table.
     * There will be keys for foreign keys and "fake" foreign keys.
     * @return the reference key elements for this mapping table
     */
    private List getMappingReferenceKeysInternal() {
        if (referenceKeys == null) {
            referenceKeys = new ArrayList();
        }

        return referenceKeys;
    }

    /**
     * Returns the list of keys (MappingReferenceKeys) for this mapping table.
     * There will be keys for foreign keys and "fake" foreign keys.
     * @return the reference key objects for this mapping table
     */
    public MappingReferenceKey[] getMappingReferenceKeys() {
        List refKeys = getMappingReferenceKeysInternal();

        return (MappingReferenceKey[]) refKeys.toArray(
                new MappingReferenceKey[refKeys.size()]);
    }

    /**
     * Scans through this mapping table's list of keys (MappingReferenceKeys) 
     * looking for a secondary table which matches the specified table.
     * @param the secondary table to find
     * @return the reference key object whose reference table matches
     * the specified table
     */
    public MappingReferenceKey getMappingReferenceKey(
            MappingTable secondaryTable) {
        Iterator keyIterator = 
                 getMappingReferenceKeysInternal().iterator();

        while (keyIterator.hasNext()) {
            MappingReferenceKey key = 
                    (MappingReferenceKey) keyIterator.next();

            if (key.getMappingTable().equals(secondaryTable)) {
                return key;
            }
        }

        return null;       
    }

    /**
     * Returns a new instance of the MappingReferenceKey implementation class.
     */
    protected MappingReferenceKey newMappingReferenceKeyInstance() {
        return new MappingReferenceKeyImplDynamic(getDeclaringMappingClass());
    }

    /**
     * Creates a mapping reference key. The returned MappingReferenceKey
     * is associated with this mapping table, but does not have any pairs. set.
     * @return a mapping reference used by this mapping table.
     */
    public MappingReferenceKey createMappingReferenceKey()
            throws ModelException {
        MappingReferenceKey referenceKey = 
            newMappingReferenceKeyInstance();

        try {
            fireVetoableChange(PROP_REFERENCE_KEYS, null, null);
            getMappingReferenceKeysInternal().add(referenceKey);
            firePropertyChange(PROP_REFERENCE_KEYS, null, null);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }

        return referenceKey;
    }

    /**
     * Removes the specified mapping reference key from list of keys in
     * this mapping table.
     * @param key mapping reference key to be removed
     * @throws ModelException if impossible
     */
    public void removeMappingReferenceKey(MappingReferenceKey key)
        throws ModelException {
        if (key != null) {
            try {
                fireVetoableChange(PROP_REFERENCE_KEYS, null, null);
                getMappingReferenceKeysInternal().remove(key);
                firePropertyChange(PROP_REFERENCE_KEYS, null, null);
            } catch (PropertyVetoException e) {
                throw new ModelVetoException(e);
            }
        } else {
            throw new ModelException(
                    getMessageHelper().msg("mapping.element.null_argument"));					// NOI18N
        }
        
    }

    /**
     * Set the list of keys (MappingReferenceKeys) for this mapping table.  This
     * method should only be used internally and for cloning and archiving.
     * @param referenceKeys the list of reference keys for this mapping table
     */
    public void setMappingReferenceKeys(List referenceKeys) {
        this.referenceKeys = referenceKeys;
    }

    // </editor-fold>

    // </editor-fold>
}
