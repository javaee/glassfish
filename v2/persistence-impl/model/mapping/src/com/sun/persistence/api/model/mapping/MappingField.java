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
 * MappingField.java
 *
 */


package com.sun.persistence.api.model.mapping;

import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.jdo.JDOField;

/** 
 *
 */
public interface MappingField extends MappingMember {
    // <editor-fold desc="//==================== fetch group constants ========================">

    /**
     * Constant representing the jdo default fetch group. This is what used to
     * be mandatory for SynerJ.
     */
    public static final int GROUP_DEFAULT = 1;

    /**
     * Constant representing no fetch group.
     */
    public static final int GROUP_NONE = 0;

    /**
     * Constant representing an independent fetch group.  All independent fetch
     * groups must have a value less than or equal to this constant.
     */
    public static final int GROUP_INDEPENDENT = -1;

    // </editor-fold>

    // <editor-fold desc="//====================== read-only handling =========================">

    /**
     * Determines whether this field is read only or not.
     * @return <code>true</code> if the field is read only, <code>false</code>
     *         otherwise
     */
    public boolean isReadOnly();

    /**
     * Set whether this field is read only or not.
     * @param flag - if <code>true</code>, the field is marked as read only;
     * otherwise, it is not
     * @throws ModelException if impossible
     */
    public void setReadOnly(boolean flag) throws ModelException;

    // </editor-fold>

    // <editor-fold desc="//======================= version handling ==========================">

    /**
     * Determines whether this field is a version field or not.
     * @return <code>true</code> if the field is a version field,
     *         <code>false</code> otherwise
     */
    public boolean isVersion();

    /**
     * Set whether this field is a version field or not.
     * @param flag - if <code>true</code>, the field is marked as a version
     * field; otherwise, it is not
     * @throws ModelException if impossible
     */
    public void setVersion(boolean flag) throws ModelException;

    // </editor-fold>

    // <editor-fold desc="//===================== fetch group handling ========================">

    /**
     * Get the fetch group of this field.
     * @return the fetch group, one of {@link #GROUP_DEFAULT}, {@link
     *         #GROUP_NONE}, or anything less than or equal to {@link
     *         #GROUP_INDEPENDENT}
     */
    public int getFetchGroup();

    /**
     * Set the fetch group of this field.
     * @param group - an integer indicating the fetch group, one of: {@link
     * #GROUP_DEFAULT}, {@link #GROUP_NONE}, or anything less than or equal to
     * {@link #GROUP_INDEPENDENT}
     * @throws ModelException if impossible
     */
    public void setFetchGroup(int group) throws ModelException;

    // </editor-fold>

    // <editor-fold desc="//=================== delegation to jdo model  ======================">

    /**
     * Get the associated JDOField of this field.
     * @return the associated JDOField object.
     */
    public JDOField getJDOField();

    // </editor-fold>

    // <editor-fold desc="//==================== relationship handling ========================">
    
    /**
     * Get the associated relationship of this field.
     * @return the associated relationship object if this field represents a
     *         relationship.
     */
    public MappingRelationship getMappingRelationship();

    /**
     * Creates a new mapping relationship bound to this mapping field.
     * @return the new mapping relationship instance.
     */
    public MappingRelationship createMappingRelationship()
            throws ModelException;

    // </editor-fold>

    // <editor-fold desc="//======================= column handling ===========================">

    /**
     * Returns <code>true</code> if this is a field mapped to columns or a 
     * relationship mapped to column pairs (using reference keys), 
     * <code>false</code> otherwise.
     * @return whether this mapping field or relationship is mapped
     */
    public boolean isMapped();

    /**
     * Returns the list of column names to which this mapping field is mapped.
     * This method will throw an IllegalStateException if the field is a 
     * relationship.
     * @return the names of the columns mapped by this mapping field
     */
    public String[] getColumnNames();

    /**
     * Returns the list of columns to which this mapping field is mapped.
     * This method will throw an IllegalStateException if the field is a 
     * relationship.
     * @return the columns mapped by this mapping field
     */
    public ColumnElement[] getColumns();

    /**
     * Adds a column to the list of columns mapped by this mapping field.
     * This method will throw an exception if the field is a relationship.
     * @param column column element to be added to the mapping
     * @throws ModelException if impossible
     */
    public void addColumn(ColumnElement column) throws ModelException;

    /**
     * Removes a column from the list of columns mapped by this mapping field.
     * This method will throw an exception if the field is a relationship.
     * @param columnName the relative name of the column to be removed from the
     * mapping
     * @throws ModelException if impossible
     */
    public void removeColumn(String columnName) throws ModelException;

    // </editor-fold>
}
