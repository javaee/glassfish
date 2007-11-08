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
 * MappingClassImplDynamic.java
 *
 * Created on March 3, 2000, 1:11 PM
 */


package com.sun.persistence.impl.model.mapping;

import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.forte4j.modules.dbmodel.DBIdentifier;
import com.sun.forte4j.modules.dbmodel.ForeignKeyElement;
import com.sun.forte4j.modules.dbmodel.SchemaElement;
import com.sun.forte4j.modules.dbmodel.TableElement;
import com.sun.forte4j.modules.dbmodel.UniqueKeyElement;
import com.sun.forte4j.modules.dbmodel.util.NameUtil;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelVetoException;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.persistence.api.model.mapping.MappingClass;
import com.sun.persistence.api.model.mapping.MappingField;
import com.sun.persistence.api.model.mapping.MappingModel;
import com.sun.persistence.api.model.mapping.MappingReferenceKey;
import com.sun.persistence.api.model.mapping.MappingTable;
import com.sun.persistence.utility.JavaTypeHelper;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Michael Bouschen
 * @author Rochelle Raccah
 */
public class MappingClassImplDynamic extends MappingMemberImpl
        implements MappingClass {

    // <editor-fold desc="//===================== constants & variables =======================">

    /**
     * The current version number. Note: Please increment this if there are any
     * changes in the mapping model that might cause incompatibilities to older
     * versions.
     */
    private static final int CURRENT_VERSION_NO = 1;

    /**
     * Version number of this MappingClassImplDynamic object. This number is set
     * by the initilaizer of the declaration or set by the archiver when reading
     * a mapping file.
     */
    private int versionNumber = CURRENT_VERSION_NO;

    /** */
    private MappingModel declaringModel;

    private boolean isModified;
    private MappingTable primaryTable;
    private List fields;		// array of MappingField

    /**
     * The database root for this MappingClass. The database root is the schema
     * name of of all the db elements attached to this MappingClass.
     */
    private String databaseSchemaName;

    /**
     * Consistency Level of this MappingClass.
     */
    private int consistencyLevel;

    // </editor-fold>

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Create new MappingClassImplDynamic with no corresponding declaring 
     * model or name.  (This constructor should only be used for cloning 
     * and archiving.)
     */
    public MappingClassImplDynamic() {
        this(null, null);
    }

    /**
     * Creates new MappingClassImplDynamic with the corresponding name
     * @param name the name of the mapping class
     */
    protected MappingClassImplDynamic(String name,
            MappingModel declaringMappingModel) {
        super(name, null);
        this.declaringModel = declaringMappingModel;
        this.consistencyLevel = NONE_CONSISTENCY;
    }

    // </editor-fold>

    // <editor-fold desc="//================== MappingMemberImpl overrides ====================">

    /**
     * Fires property change event.  This method overrides that of
     * MappingElementImpl to update the mapping class's modified status.
     * @param name property name
     * @param o old value
     * @param n new value
     */
    // TODO - examine this and the vetoable one because now the
    // superclass is different (member vs. element)
    protected final void firePropertyChange(String name, Object o, Object n) {
        // even though o == null and n == null will signify a change, that
        // is consistent with PropertyChangeSupport's behavior and is
        // necessary for this to work
        boolean noChange = ((o != null) && (n != null) && o.equals(n));

        super.firePropertyChange(name, o, n);

        if (!(PROP_MODIFIED.equals(name)) && !noChange) {
            setModified(true);
        }
    }

    /**
     * Fires vetoable change event.  This method overrides that of
     * MappingElementImpl to give listeners a chance to block changes on the
     * mapping class modified status.
     * @param name property name
     * @param o old value
     * @param n new value
     * @throws PropertyVetoException when the change is vetoed by a listener
     */
    protected final void fireVetoableChange(String name, Object o, Object n)
            throws PropertyVetoException {
        // even though o == null and n == null will signify a change, that
        // is consistent with PropertyChangeSupport's behavior and is
        // necessary for this to work
        boolean noChange = ((o != null) && (n != null) && o.equals(n));

        super.fireVetoableChange(name, o, n);

        if (!(PROP_MODIFIED.equals(name)) && !noChange) {
            fireVetoableChange(PROP_MODIFIED, Boolean.FALSE, Boolean.TRUE);
        }
    }

    // </editor-fold>

    // <editor-fold desc="//========== MappingClass & related convenience methods =============">

    // <editor-fold desc="//================= version and archive handling ====================">

    /**
     * Returns the version number of this MappingClass object. Please note, the
     * returned version number reflects the version number at the last save, NOT
     * the version number of the memory representation.
     * @return version number
     */
    public int getVersionNumber() {
        return versionNumber;
    }

    /**
     * Set the version number of this MappingClass.
     * @param version the new version number
     */
    private void setVersionNumber(int version) {
        versionNumber = version;
    }

    /**
     * Returns true if the version number of this MappingClass object is older
     * than the current version number of the archiving scheme.
     * @return true if it is in need of updating, false otherwise
     * @see #getVersionNumber
     */
    public boolean hasOldVersionNumber() {
        return (getVersionNumber() < CURRENT_VERSION_NO);
    }

    /**
     * This method is called after a MappingClass is unarchived from a .mapping
     * file.  This method provides a hook to do any checking (version number
     * checking) and conversion after unarchiving.
     * @throws ModelException if impossible
     */
    public void postUnarchive() throws ModelException {
        // check version number
        switch (versionNumber) {
            case MappingClassImplDynamic.CURRENT_VERSION_NO:
                // OK
                break;
            default: // version number is unknown
                throw new ModelException(
                        getMessageHelper().msg(
                                "file.incompatible_version", getName()));	//NOI18N
        }
    }

    /**
     * This method is called prior to storing a MappingClass in a .mapping file.
     *  This method provides a hook to do any conversion before archiving. Note,
     * the signature of preArchive in the interface MappingClass includes a
     * throws clause (ModelException), but the actual implementation does not
     * throw an exception.
     */
    public void preArchive() {
        // update version number
        setVersionNumber(CURRENT_VERSION_NO);
    }

    // </editor-fold>

    // <editor-fold desc="//====================== modified handling ==========================">

    /**
     * Gets the modified flag for this mapping class.
     * @return <code>true</code> if there have been (property) changes to this
     *         class, <code>false</code> otherwise.
     */
    public boolean isModified() {
        return isModified;
    }

    /**
     * Set the modified flag for this mapping class to flag.  This is usually
     * set to <code>true</code> by property changes and <code>false</code> after
     * a save.
     * @param flag if <code>true</code>, this class is marked as modified; if
     * <code>false</code>, it is marked as unmodified.
     */
    public void setModified(boolean flag) {
        boolean oldFlag = isModified();

        if (flag != oldFlag) {
            isModified = flag;
            firePropertyChange(
                    PROP_MODIFIED, JavaTypeHelper.valueOf(oldFlag),
                    JavaTypeHelper.valueOf(flag));
        }
    }

    // </editor-fold>

    // <editor-fold desc="//================== consistency level handling =====================">

    /**
     * Gets the consistency level of this mapping class.
     * @return the consistency level, one of {@link #NONE_CONSISTENCY}, {@link
     *         #CHECK_MODIFIED_AT_COMMIT_CONSISTENCY}, {@link
     *         #CHECK_ALL_AT_COMMIT_CONSISTENCY}, {@link #LOCK_WHEN_MODIFIED_CONSISTENCY},
     *         {@link #LOCK_WHEN_MODIFIED_CHECK_ALL_AT_COMMIT_CONSISTENCY},
     *         {@link #LOCK_WHEN_LOADED_CONSISTENCY}, or {@link
     *         #VERSION_CONSISTENCY}. The default is {@link #NONE_CONSISTENCY}.
     */
    public int getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * Set the consistency level of this mapping class.
     * @param level an integer indicating the consistency level, one of: {@link
     * #NONE_CONSISTENCY},{@link #CHECK_MODIFIED_AT_COMMIT_CONSISTENCY}, {@link
     * #CHECK_ALL_AT_COMMIT_CONSISTENCY}, {@link #LOCK_WHEN_MODIFIED_CONSISTENCY},
     * {@link #LOCK_WHEN_MODIFIED_CHECK_ALL_AT_COMMIT_CONSISTENCY}, {@link
     * #LOCK_WHEN_LOADED_CONSISTENCY}, or {@link #VERSION_CONSISTENCY}.
     * @throws ModelException if impossible.
     */
    public void setConsistencyLevel(int level) throws ModelException {
        Integer old = new Integer(getConsistencyLevel());
        Integer newLevel = new Integer(level);

        try {
            fireVetoableChange(PROP_CONSISTENCY, old, newLevel);
            consistencyLevel = level;
            firePropertyChange(PROP_CONSISTENCY, old, newLevel);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    // </editor-fold>

    // <editor-fold desc="//======================= declaring model ===========================">

    /**
     * Returns the declaring MappingModel of this MappingClass.
     * @return the MappingModel that owns this MappingClass
     */
    public MappingModel getDeclaringMappingModel() {
        return declaringModel;
    }

    // </editor-fold>

    // <editor-fold desc="//=================== delegation to jdo model  ======================">

    /**
     * @return JDOClass for this mapping class
     */
    public final JDOClass getJDOClass() {
        return declaringModel.getJDOModel().getJDOClass(getName());
    }

    // </editor-fold>

    // <editor-fold desc="//======================= schema handling ===========================">
    
    /**
     * Returns the name of the SchemaElement which represents the database used
     * by the tables mapped to this mapping class.
     * @return the name of the database root for this mapping class
     */
    public String getDatabaseSchemaName() {
        return databaseSchemaName;
    }

    /**
     * Returns the SchemaElement which represents the database used by the
     * tables mapped to this mapping class.
     * @return the database root for this mapping class
     */
    public SchemaElement getDatabaseSchema() {
        return SchemaElement.forName(getDatabaseSchemaName());
    }

    /**
     * Set the database root for this MappingClass. The root represents the
     * database used by the tables mapped to this mapping class.
     * @param root the new database root
     * @throws ModelException if impossible
     */
    public void setDatabaseSchema(SchemaElement root) throws ModelException {
        String old = getDatabaseSchemaName();
        String newRoot = ((root != null) ? root.getName().getFullName() : null);

        try {
            fireVetoableChange(PROP_DATABASE_ROOT, old, newRoot);
            databaseSchemaName = newRoot;
            firePropertyChange(PROP_DATABASE_ROOT, old, newRoot);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    // </editor-fold>

    // <editor-fold desc="//======================= table handling ============================">

    /**
     * Returns the list of tables (MappingTables) used by this mapping class.
     * @return the meta data tables for this mapping class
     */
    private List getMappingTablesInternal() {
        MappingTable primary = getPrimaryMappingTable();
        List tables = new ArrayList();

        if (primary != null) {
            MappingReferenceKey[] refKeys = primary.getMappingReferenceKeys();
            int i, count = ((refKeys != null) ? refKeys.length : 0);

            tables.add(primary);

            for (i = 0; i < count;i++) {
                MappingTable secondaryTable = refKeys[i].getMappingTable();

                if (secondaryTable != null)
                    tables.add(secondaryTable);
            }
        }

        return tables;
    }

    /**
     * Returns the list of tables (MappingTables) used by this mapping class.
     * Note that only the primary table is created directly on the mapping 
     * class.  The secondary tables are created on the primary table's 
     * MappingTable but are included in the list of MappingTable objects 
     * returned by this method.
     * @return the meta data tables for this mapping class
     */
    public MappingTable[] getMappingTables() {
        List tables = getMappingTablesInternal();

        return (MappingTable[]) tables.toArray(
            new MappingTable[tables.size()]);
    }

    /**
     * Scans through this mapping class looking for a table whose name matches
     * the name passed in.  Note that only the primary table is created 
     * directly on the mapping class.  The secondary tables are created on the 
     * primary table's MappingTable but are included MappingTable objects that 
     * can be returned by this method.
     * @param name name of the table to find.
     * @return the meta data table whose name matches the name parameter
     */
    public MappingTable getMappingTable(String name) {
        Iterator tableIterator = getMappingTablesInternal().iterator();

        while (tableIterator.hasNext()) {
            MappingTable table = (MappingTable) tableIterator.next();

            if (table.getName().equals(name)) {
                return table;
            }
        }

        return null;
    }

    /**
     * Returns the primary table used by this mapping class.
     * @return the meta data table for the primary table used by this mapping
     * class.
     */
    public MappingTable getPrimaryMappingTable() {
        return primaryTable;
    }

    /**
     * Returns a new instance of the MappingTable implementation class.
     */
    protected MappingTable newMappingTableInstance(TableElement table,
            MappingClass declaringMappingClass) {
        return new MappingTableImplDynamic(table, declaringMappingClass);
    }

    /**
     * Creates a mapping table for the supplied primary table. The returned 
     * MappingTable is associated with this mapping class and set as the 
     * primary table.  This mapping class' database schema will also be set 
     * using {@link #setDatabaseSchema} if it has not yet been set.  Note that 
     * secondary tables are created on the primary table's MappingTable.
     * @param table table element to be wrapped in a mapping table and 
     * used as the primary table.
     * @return a mapping table for the specified table used by this mapping 
     * class.
     * @exception ModelException if impossible
     */
    public MappingTable createPrimaryMappingTable(TableElement table)
        throws ModelException {
        MappingTable mappingTable = newMappingTableInstance(table, this);
        String currentRoot = getDatabaseSchemaName();
// TODO - reexamine whether changing from one to the other is 
// a problem here
        if (primaryTable != null) {
            throw new ModelException(
                    getMessageHelper().msg(
                            "mapping.table.primary_table_defined", // NOI18N
                            mappingTable));
        } else if (table == null) {
            throw new ModelException(
                    getMessageHelper().msg(
                            "mapping.table.schema_mismatch", // NOI18N
                            mappingTable, currentRoot));
        } else {
            UniqueKeyElement key = table.getPrimaryKey();
            SchemaElement schema = table.getDeclaringSchema();

            if (currentRoot == null) {	// set database root
                setDatabaseSchema(schema);
            } else if (!currentRoot.equals(schema.getName().getFullName())) {
                // if database root was set before, it must match
                throw new ModelException(
                        getMessageHelper().msg(
                                "mapping.table.schema_mismatch", // NOI18N
                                table.toString(), currentRoot));
            }

            try {
                fireVetoableChange(PROP_TABLES, null, null);
                primaryTable = mappingTable;
                firePropertyChange(PROP_TABLES, null, null);
            } catch (PropertyVetoException e) {
                throw new ModelVetoException(e);
            }

            //	If can't find a primary key, settle for first unique key.
            if (key == null) {
                UniqueKeyElement[] uniqueKeys = table.getUniqueKeys();

                if ((uniqueKeys != null) && (uniqueKeys.length > 0)) {
                    key = uniqueKeys[0];
                }
            }

            if (key == null) {
                //	This is a warning -- we can still use the table but we
                //	cannot perform update operations on it.  Also the user
                //	may define the key later.
            } else {
                ColumnElement[] columns = key.getColumns();
                int i, count = ((columns != null) ? columns.length : 0);

                for (i = 0; i < count; i++) {
                    mappingTable.addKeyColumn(columns[i]);
                }
            }
        }

        return mappingTable;
    }

    /**
     * Removes the reference to the supplied table as a mapped table for this
     * mapping class.  Note that while only the primary table is created 
     * directly on the mapping class, (the secondary tables are created on the 
     * primary table's MappingTable), this method call works whether the 
     * specified table is the primary table or a secondary table.
     * @param table mapping table to be removed from this mapping class.
     * @throws ModelException if impossible
     */
    public void removeMappingTable(MappingTable table) throws ModelException {
        if (table != null) {
            List tableList = getMappingTablesInternal();
            Iterator iterator = null;
            boolean found = false;

            try {
                fireVetoableChange(PROP_TABLES, null, null);
                found = tableList.remove(table);
                if (table.equals(primaryTable))
                    primaryTable = null;
                firePropertyChange(PROP_TABLES, null, null);
            } catch (PropertyVetoException e) {
                throw new ModelVetoException(e);
            }

            // remove all references to this table
            if (tableList != null) {
                iterator = tableList.iterator();
                while (iterator.hasNext()) {
                    MappingTable nextTable = (MappingTable) iterator.next();
                    MappingReferenceKey[] refKeys = 
                        nextTable.getMappingReferenceKeys();
                    int i, count = ((refKeys != null) ? refKeys.length : 0);

                    for (i = 0; i < count; i++)
                        nextTable.removeMappingReferenceKey(refKeys[i]);
                }
            }

            if (found)	// remove any fields mapped to that table
            {
                ArrayList fieldsToRemove = new ArrayList();

                iterator = getMappingFieldsInternal().iterator();
                while (iterator.hasNext()) {
                    MappingFieldImplDynamic mappingField = (MappingFieldImplDynamic) iterator.next();

                    if (mappingField.isMappedToTable(table)) {
                        fieldsToRemove.add(mappingField);
                    }
                }

                iterator = fieldsToRemove.iterator();
                while (iterator.hasNext()) {
                    MappingField mappingField = (MappingField) iterator.next();
                    boolean versionField = mappingField.isVersion();

                    removeMappingField(mappingField);

                    // if it is a version field, add back an unmapped
                    // field which retains the version flag setting
                    if (versionField) {
                        mappingField = createMappingField(
                                mappingField.getName());
                        mappingField.setVersion(true);
                    }
                }
            } else {
                throw new ModelException(
                        getMessageHelper().msg(
                                "mapping.element.element_not_removed", table));	// NOI18N
            }
        } else {
            throw new ModelException(
                    getMessageHelper().msg("mapping.element.null_argument"));				// NOI18N
        }
    }

    // </editor-fold>

    // <editor-fold desc="//======================= field handling ============================">

    /**
     * Returns the list of fields (MappingFields) in this mapping class. This
     * list includes both local and relationship fields.
     * @return the mapping fields in this mapping class
     */
    private List getMappingFieldsInternal() {
        if (fields == null) {
            fields = new ArrayList();
        }

        return fields;
    }

    /**
     * Returns the list of fields (MappingFields) in this mapping class. This
     * list includes both local and relationship fields.
     * @return the mapping fields in this mapping class
     */
    public MappingField[] getMappingFields() {
        List myFields = getMappingFieldsInternal();
        return (MappingField[]) myFields.toArray(
                new MappingField[myFields.size()]);
    }

    /**
     * Scans through this mapping class looking for a field whose name matches
     * the name passed in.
     * @param name name of the field to find.
     * @return the mapping field whose name matches the name parameter
     */
    public MappingField getMappingField(String name) {
        Iterator fieldIterator = getMappingFieldsInternal().iterator();

        while (fieldIterator.hasNext()) {
            MappingField field = (MappingField) fieldIterator.next();

            if (name.equals(field.getName())) {
                return field;
            }
        }

        return null;
    }

    /**
     * Returns a new instance of the MappingField implementation class.
     */
    protected MappingField newMappingFieldInstance(String fieldName,
            MappingClass declaringMappingClass) {
        return new MappingFieldImplDynamic(fieldName, declaringMappingClass);
    }

    /**
     * Adds a field to the list of fields in this mapping class.
     * @param field field to be added
     * @throws ModelException if impossible
     */
    public MappingField createMappingField(String name) throws ModelException {
        MappingField field = (MappingField) getMappingField(name);

        if (field == null) {
            List fieldList = getMappingFieldsInternal();

            if (!fieldList.contains(field)) {
                try {
                    fireVetoableChange(PROP_FIELDS, null, null);
                    field = newMappingFieldInstance(name, this);
                    fieldList.add(field);
                    firePropertyChange(PROP_FIELDS, null, null);
                } catch (PropertyVetoException e) {
                    throw new ModelVetoException(e);
                }
            }
        }

        return field;
    }

    /**
     * Removes a field from the list of fields in this mapping class.
     * @param field field to be removed
     * @throws ModelException if impossible
     */
    public void removeMappingField(MappingField field) throws ModelException {
        try {
            fireVetoableChange(PROP_FIELDS, null, null);

            if (!getMappingFieldsInternal().remove(field)) {
                throw new ModelException(
                        getMessageHelper().msg(
                                "mapping.element.element_not_removed", field));	// NOI18N
            }

            firePropertyChange(PROP_FIELDS, null, null);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    /**
     * Returns the list of version fields (MappingFields) in this mapping class.
     *  This list only includes fields if the consistency level is {@link
     * #VERSION_CONSISTENCY}.
     * @return the version fields in this mapping class
     */
    public MappingField[] getVersionMappingFields() {
        List versionFields = new ArrayList();

        if (VERSION_CONSISTENCY == getConsistencyLevel()) {
            Iterator iterator = getMappingFieldsInternal().iterator();

            while (iterator.hasNext()) {
                MappingField fieldCandidate = (MappingField) iterator.next();

                if (fieldCandidate.isVersion()) {
                    versionFields.add(fieldCandidate);
                }
            }
        }

        return (MappingField[]) versionFields.toArray(
            new MappingField[versionFields.size()]);
    }

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="//===================== internal helper methods =====================">

    //============= extra object support for runtime ========================

    /**
     * Accept a list of column names and return an array list containing the
     * corresponding column or column pair objects.
     * @param schemaName the database root used to find the column objects
     * @param columnNames array of column names.
     * @return a list of corresponding column objects
     * @see com.sun.forte4j.modules.dbmodel.TableElement#forName
     * @see com.sun.forte4j.modules.dbmodel.TableElement#getMember
     */
    protected static List toColumnObjects(String schemaName, List columnNames) {
        Iterator iterator = columnNames.iterator();
        List objects = new ArrayList();

        while (iterator.hasNext()) {
            String columnName = (String) iterator.next();
            String absoluteColumnName = NameUtil.getAbsoluteMemberName(
                    schemaName, columnName);
            final TableElement table = TableElement.forName(
                    NameUtil.getTableName(absoluteColumnName));

            objects.add(
                    table.getMember(DBIdentifier.create(absoluteColumnName)));
        }

        return objects;
    }

    //============= delegation to JDOClass ===========

    /**
     * Get the fully qualified name of the primary key class for this mapping
     * class.  This value is only used if <code>getObjectIdentityType</code>
     * returns <code>APPLICATION_IDENTITY</code>
     * @return the fully qualified key class name, <code>null</code> if the
     *         identity type is not managed by the application
     * @see JDOClass#setObjectIdentityType
     * @see JDOClass#APPLICATION_IDENTITY
     */
    /*public String getKeyClass() {
        return getJDOClass().getObjectIdClass().getName();
    }*/

    //=============== extra set methods needed for xml archiver ==============

    /**
     * Set the list of fields (MappingFields) in this mapping class.
     * This method should only be used internally and for cloning and
     * archiving.
     * @param fields the list of mapping fields in this mapping class
     */
    /* public void setFields(ArrayList fields) {
         this.fields = fields;
     }*/

    // </editor-fold>
}
