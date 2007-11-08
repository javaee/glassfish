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
 * MappingClass.java
 *
 */


package com.sun.persistence.api.model.mapping;

import com.sun.forte4j.modules.dbmodel.SchemaElement;
import com.sun.forte4j.modules.dbmodel.TableElement;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.jdo.JDOClass;

/**
 * This is an object which represents the mapping metadata for a persistent
 * class.  The primary table should be set up on this object as follows:
 * myPrimaryMappingTable = myMappingClass.createPrimaryMappingTable(myPrimaryTableElement);
 * One or more secondary tables can be set up on the primary mapping table 
 * as follows:
 * myRefKey = myPrimaryMappingTable.createMappingReferenceKey();
 * Then set up the pairs on myRefKey.
 */
public interface MappingClass extends MappingMember {

    // <editor-fold desc="//================= consistency level constants =====================">

    /**
     * Constant representing Consistency level. NONE_CONSISTENCY implies that no
     * consistency semantics are enforced.
     */
    public static final int NONE_CONSISTENCY = 0x0;

    /**
     * Constant representing Consistency level. CHECK_MODIFIED_AT_COMMIT_CONSISTENCY
     * implies that at commit, consistency check is enforced for all fetched
     * fields of modified objects.
     */
    public static final int CHECK_MODIFIED_AT_COMMIT_CONSISTENCY = 0x1;

    /**
     * Constant representing Consistency level. CHECK_ALL_AT_COMMIT_CONSISTENCY
     * implies that at commit, consistency check is enforced for all the fields
     * of objects at this consistency level. Please note that this level is not
     * supported in the current release.
     */
    public static final int CHECK_ALL_AT_COMMIT_CONSISTENCY = 0x2;

    /**
     * Constant representing Consistency level. LOCK_WHEN_MODIFIED_CONSISTENCY
     * implies exclusive lock is obtained for data corresponding to this object
     * when an attempt to modify the object is made. Please note that this level
     * is not supported in the current release.
     */
    public static final int LOCK_WHEN_MODIFIED_CONSISTENCY = 0x4;

    /**
     * Constant representing Consistency level. 
     * LOCK_WHEN_MODIFIED_CHECK_ALL_AT_COMMIT_CONSISTENCY implies exclusive 
     * lock is obtained for data corresponding to this object when an attempt 
     * to modify the object is made.  Also at commit, consistency check is 
     * enforced for all the fields of objects at this consistency level. Please 
     * note that this level is not supported in the current release.
     */
    public static final int LOCK_WHEN_MODIFIED_CHECK_ALL_AT_COMMIT_CONSISTENCY = 
        CHECK_ALL_AT_COMMIT_CONSISTENCY | LOCK_WHEN_MODIFIED_CONSISTENCY;

    /**
     * Constant representing Consistency level. LOCK_WHEN_LOADED_CONSISTENCY
     * implies that exclusive lock is obtained for data corresponding to this
     * object before accessing it.
     */
    public static final int LOCK_WHEN_LOADED_CONSISTENCY = 0x8;

    /**
     * Constant representing Consistency level. VERSION_CONSISTENCY implies that
     * no lock is obtained for data corresponding to this object until it will
     * be updated.
     */
    public static final int VERSION_CONSISTENCY = 0x10;

    // </editor-fold>

    // <editor-fold desc="//================= version and archive handling ====================">

    /**
     * Returns the version number of this MappingClass object. Please note, the
     * returned version number reflects the version number at the last save, NOT
     * the version number of the memory representation.
     * @return version number
     */
    public int getVersionNumber();

    /**
     * Returns true if the version number of this MappingClass object is older
     * than the current version number of the archiving scheme.
     * @return true if it is in need of updating, false otherwise
     * @see #getVersionNumber
     */
    public boolean hasOldVersionNumber();

    /**
     * This method is called after a MappingClass is unarchived from a .mapping
     * file.  This method provides a hook to do any checking (version number
     * checking) and conversion after unarchiving.
     * @throws ModelException if impossible
     */
    public void postUnarchive() throws ModelException;

    /**
     * This method is called prior to storing a MappingClass in a .mapping file.
     * This method provides a hook to do any conversion before archiving.
     * @throws ModelException if impossible
     */
    public void preArchive() throws ModelException;

    // </editor-fold>

    // <editor-fold desc="//====================== modified handling ==========================">

    /**
     * Gets the modified flag for this mapping class.
     * @return <code>true</code> if there have been (property) changes to this
     *         class, <code>false</code> otherwise.
     */
    public boolean isModified();

    /**
     * Set the modified flag for this mapping class to flag.  This is usually
     * set to <code>true</code> by property changes and <code>false</code> after
     * a save.
     * @param flag if <code>true</code>, this class is marked as modified; if
     * <code>false</code>, it is marked as unmodified.
     */
    public void setModified(boolean flag);

    // </editor-fold>

    // <editor-fold desc="//================== consistency level handling =====================">

    /**
     * Gets the consistency level of this mapping class.
     * @return the consistency level, one of {@link #NONE_CONSISTENCY}, {@link
     *         #CHECK_MODIFIED_AT_COMMIT_CONSISTENCY}, {@link
     *         #CHECK_ALL_AT_COMMIT_CONSISTENCY}, {@link #LOCK_WHEN_MODIFIED_CONSISTENCY},
     *         {@link #LOCK_WHEN_MODIFIED_CHECK_ALL_AT_COMMIT_CONSISTENCY},
     *         {@link #LOCK_WHEN_LOADED_CONSISTENCY}, or {@link
     *         #VERSION_CONSISTENCY}.
     */
    public int getConsistencyLevel();

    /**
     * Set the consistency level of this mapping class.
     * @param level an integer indicating the consistency level, one of: {@link
     * #NONE_CONSISTENCY},{@link #CHECK_MODIFIED_AT_COMMIT_CONSISTENCY}, {@link
     * #CHECK_ALL_AT_COMMIT_CONSISTENCY}, {@link #LOCK_WHEN_MODIFIED_CONSISTENCY},
     * {@link #LOCK_WHEN_MODIFIED_CHECK_ALL_AT_COMMIT_CONSISTENCY} or {@link
     * #LOCK_WHEN_LOADED_CONSISTENCY}, or {@link #VERSION_CONSISTENCY}.
     * @throws ModelException if impossible.
     */
    public void setConsistencyLevel(int level) throws ModelException;

    // </editor-fold>

    // <editor-fold desc="//======================= declaring model ===========================">

    /**
     * Returns the declaring MappingModel of this MappingClass.
     * @return the MappingModel that owns this MappingClass
     */
    public MappingModel getDeclaringMappingModel();

    // </editor-fold>

    // <editor-fold desc="//=================== delegation to jdo model  ======================">

    /**
     * Provides the JDO class representation corresponding to this meta data.
     * @return the JDO class object corresponding to this meta data.
     */
    public JDOClass getJDOClass();

    // </editor-fold>

    // <editor-fold desc="//======================= schema handling ===========================">

    /**
     * Returns the name of the SchemaElement which represents the database used
     * by the tables mapped to this mapping class.
     * @return the name of the database root for this mapping class
     */
    public String getDatabaseSchemaName();

    /**
     * Returns the SchemaElement which represents the database used by the
     * tables mapped to this mapping class.
     * @return the database root for this mapping class
     */
    public SchemaElement getDatabaseSchema();

    /**
     * Set the database root for this MappingClass. The root represents the
     * database used by the tables mapped to this mapping class.
     * @param root the new database root
     * @throws ModelException if impossible
     */
    public void setDatabaseSchema(SchemaElement root) throws ModelException;

    // </editor-fold>

    // <editor-fold desc="//======================= table handling ============================">

    /**
     * Returns the list of tables (MappingTables) used by this mapping class.  
     * Note that only the primary table is created directly on the mapping 
     * class.  The secondary tables are created on the primary table's 
     * MappingTable but are included in the list of MappingTable objects 
     * returned by this method.
     * @return the meta data tables for this mapping class
     */
    public MappingTable[] getMappingTables();

    /**
     * Scans through this mapping class looking for a table whose name matches
     * the name passed in.  Note that only the primary table is created 
     * directly on the mapping class.  The secondary tables are created on the 
     * primary table's MappingTable but are included MappingTable objects that 
     * can be returned by this method.
     * @param name name of the table to find.
     * @return the meta data table whose name matches the name parameter
     */
    public MappingTable getMappingTable(String name);

    /**
     * Returns the primary table used by this mapping class.
     * @return the meta data table for the primary table used by this mapping
     * class.
     */
    public MappingTable getPrimaryMappingTable();

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
        throws ModelException;

    /**
     * Removes the reference to the supplied table as a mapped table for this
     * mapping class.  Note that while only the primary table is created 
     * directly on the mapping class, (the secondary tables are created on the 
     * primary table's MappingTable), this method call works whether the 
     * specified table is the primary table or a secondary table.
     * @param table mapping table to be removed from this mapping class.
     * @throws ModelException if impossible
     */
    public void removeMappingTable(MappingTable table) throws ModelException;

    // </editor-fold>

    // <editor-fold desc="//======================= field handling ============================">

    /**
     * Returns the list of fields (MappingFields) in this mapping class.  This
     * list includes both local and relationship fields.
     * @return the mapping fields in this mapping class
     */
    public MappingField[] getMappingFields();

    /**
     * Scans through this mapping class looking for a field whose name matches
     * the name passed in.
     * @param name name of the field to find.
     * @return the mapping field whose name matches the name parameter
     */
    public MappingField getMappingField(String name);

    /**
     * This method returns a mapping field for the field with the secified name.
     * If this mapping class already declares such a field, the existing mapping
     * field is returned. Otherwise, it creates a new mapping field, sets its
     * declaringClass and returns the new instance.
     * @param name the name of the field
     * @return an existing mapping field if it exists already, a new mapping
     *         field otherwise
     * @throws ModelException if impossible
     */
    public MappingField createMappingField(String name) throws ModelException;

    /**
     * Removes a field from the list of fields in this mapping class.
     * @param field field object to be removed
     * @throws ModelException if impossible
     */
    public void removeMappingField(MappingField field) throws ModelException;

    /**
     * Returns the list of version fields in this mapping class. This list only
     * includes fields if the consistency level is {@link
     * #VERSION_CONSISTENCY}.
     * @return the version fields in this mapping class
     */
    public MappingField[] getVersionMappingFields();

    // </editor-fold>
}
