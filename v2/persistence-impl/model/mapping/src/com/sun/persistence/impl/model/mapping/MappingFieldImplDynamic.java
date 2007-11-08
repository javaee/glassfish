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
 * MappingFieldImplDynamic.java
 *
 * Created on March 3, 2000, 1:11 PM
 */


package com.sun.persistence.impl.model.mapping;

import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.forte4j.modules.dbmodel.util.NameUtil;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelVetoException;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.persistence.api.model.mapping.MappingClass;
import com.sun.persistence.api.model.mapping.MappingField;
import com.sun.persistence.api.model.mapping.MappingReferenceKey;
import com.sun.persistence.api.model.mapping.MappingRelationship;
import com.sun.persistence.api.model.mapping.MappingTable;
import com.sun.persistence.utility.JavaTypeHelper;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Michael Bouschen
 * @author Rochelle Raccah
 */
public class MappingFieldImplDynamic extends MappingMemberImpl
        implements MappingField {

    // <editor-fold desc="//===================== constants & variables =======================">

    private MappingRelationship mappingRelationship;

    private List columnNames;	// array of member names (columnNames or pairs)

    //@olsen: made transient to prevent from serializing into mapping files
    private transient List columns; // array of ColumnElement (for runtime)

    /** */
    private int fetchGroup;

    /** */
    private boolean isReadOnly;

    /**
     * Version field flag of the mapping field.
     */
    private boolean isVersion;

    // </editor-fold>

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Create new MappingFieldImplDynamic with no corresponding name or
     * declaring class.  This constructor should only be used for cloning and
     * archiving.
     */
    public MappingFieldImplDynamic() {
        this(null, null);
    }

    /**
     * Create new MappingFieldImplDynamic with the corresponding name and
     * declaring class.
     * @param name the name of the field
     * @param declaringClass the class to attach to
     */
    protected MappingFieldImplDynamic(String name,
            MappingClass declaringMappingClass) {
        super(name, declaringMappingClass);
        setFetchGroupInternal(GROUP_DEFAULT);
    }

    // </editor-fold>

    // <editor-fold desc="//========= MappingField & related convenience methods ==============">

    // <editor-fold desc="//====================== read-only handling =========================">

    /**
     * Determines whether this field is read only or not.
     * @return <code>true</code> if the field is read only, <code>false</code>
     *         otherwise
     */
    public boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     * Set whether this field is read only or not.
     * @param flag - if <code>true</code>, the field is marked as read only;
     * otherwise, it is not
     * @throws ModelException if impossible
     */
    public void setReadOnly(boolean flag) throws ModelException {
        Boolean old = JavaTypeHelper.valueOf(isReadOnly());
        Boolean newFlag = JavaTypeHelper.valueOf(flag);

        try {
            fireVetoableChange(PROP_READ_ONLY, old, newFlag);
            this.isReadOnly = flag;
            firePropertyChange(PROP_READ_ONLY, old, newFlag);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    // </editor-fold>

    // <editor-fold desc="//======================= version handling ==========================">

    /**
     * Determines whether this field is a version field or not.
     * @return <code>true</code> if the field is a version field,
     *         <code>false</code> otherwise
     */
    public boolean isVersion() {
        return isVersion;
    }

    /**
     * Set whether this field is a version field or not.
     * @param flag - if <code>true</code>, the field is marked as a version
     * field; otherwise, it is not
     * @throws ModelException if impossible
     */
    public void setVersion(boolean flag) throws ModelException {
        Boolean old = JavaTypeHelper.valueOf(isVersion());
        Boolean newFlag = JavaTypeHelper.valueOf(flag);

        try {
            fireVetoableChange(PROP_VERSION_FIELD, old, newFlag);
            isVersion = flag;
            firePropertyChange(PROP_VERSION_FIELD, old, newFlag);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    // </editor-fold>

    // <editor-fold desc="//===================== fetch group handling ========================">

    /**
     * Get the fetch group of this field.
     * @return the fetch group, one of {@link #GROUP_DEFAULT}, {@link
     *         #GROUP_NONE}, or anything less than or equal to {@link
     *         #GROUP_INDEPENDENT}
     */
    public int getFetchGroup() {
        return fetchGroup;
    }

    /**
     * Set the fetch group of this field.
     * @param group - an integer indicating the fetch group, one of: {@link
     * #GROUP_DEFAULT}, {@link #GROUP_NONE}, or anything less than or equal to
     * {@link #GROUP_INDEPENDENT}
     * @throws ModelException if impossible
     */
    public void setFetchGroup(int group) throws ModelException {
        Integer old = new Integer(getFetchGroup());
        Integer newGroup = new Integer(group);

        try {
            fireVetoableChange(PROP_FETCH_GROUP, old, newGroup);
            setFetchGroupInternal(group);
            firePropertyChange(PROP_FETCH_GROUP, old, newGroup);
        } catch (PropertyVetoException e) {
            throw new ModelVetoException(e);
        }
    }

    /**
     * Set the fetch group of this field.  Meant to be used in the constructor
     * and by subclasses when there should be no exceptions and no property
     * change events fired.
     * @param group - an integer indicating the fetch group, one of: {@link
     * #GROUP_DEFAULT}, {@link #GROUP_NONE}, or anything less than or equal to
     * {@link #GROUP_INDEPENDENT}
     */
    protected void setFetchGroupInternal(int group) {
        fetchGroup = group;
    }

    // </editor-fold>

    // <editor-fold desc="//=================== delegation to jdo model  ======================">

    /**
     * Get the associated JDOField of this field.
     * @return the associated JDOField object.
     */
    public JDOField getJDOField() {
        return getDeclaringMappingClass().getJDOClass().getField(getName());
    }

    // </editor-fold>

    // <editor-fold desc="//==================== relationship handling ========================">
    
    /**
     * Get the associated relationship of this field.
     * @return the associated relationship object if this field represents a
     *         relationship.
     */
    public MappingRelationship getMappingRelationship() {
        return mappingRelationship;
    }

    /**
     * Returns a new instance of the MappingRelationship implementation class.
     */
    protected MappingRelationship newMappingRelationshipInstance(
            String relationshipName, MappingField declaringMappingField) {
        return new MappingRelationshipImplDynamic(relationshipName, 
                declaringMappingField);
    }

    /**
     * Creates a new mapping relationship bound to this mapping field, 
     * replacing the existing one (if there is one).
     * @return the new mapping relationship instance.
     */
    public MappingRelationship createMappingRelationship()
            throws ModelException {
        mappingRelationship = newMappingRelationshipInstance(getName(), this);

        // set the fetch group for this field to NONE as is done for 
        // relationships
        setFetchGroupInternal(MappingField.GROUP_NONE);
        
        return mappingRelationship;
    }

    // </editor-fold>

    // <editor-fold desc="//======================= column handling ===========================">

    private boolean isMappingRelationship() {
        return (getMappingRelationship() != null);
    }

    /**
     * Returns <code>true</code> if this is a field mapped to columns or a 
     * relationship mapped to column pairs (using reference keys), 
     * <code>false</code> otherwise.
     * @return whether this mapping field or relationship is mapped
     */
    public boolean isMapped() {
        MappingRelationship rel = getMappingRelationship();

        if (null == rel) {
            return !getColumnNamesInternal().isEmpty();
        } else {
            MappingReferenceKey refKey = rel.getMappingReferenceKey(
                ((MappingRelationshipImplDynamic) rel).getLogicalUsage());

            return ((refKey != null) && 
                (refKey.getColumnPairNames().length > 0));
        }
    }

    /**
     * Returns the list of column names to which this mapping field is mapped.
     * @return the names of the columnNames mapped by this mapping field
     */
    private List getColumnNamesInternal() {
        if (columnNames == null) {
            columnNames = new ArrayList();
        }

        return columnNames;
    }

    /**
     * Returns the list of column names to which this mapping field is mapped.
     * This method will throw an IllegalStateException if the field is a 
     * relationship.
     * @return the names of the columnNames mapped by this mapping field
     */
    public String[] getColumnNames() {
        if (!isMappingRelationship()) {
            List colNames = getColumnNamesInternal();

            return (String[]) colNames.toArray(new String[colNames.size()]);
        }

        throw new IllegalStateException();
    }

    /**
     * Returns the list of columns to which this mapping field is mapped.
     * This method will throw an IllegalStateException if the field is a 
     * relationship.
     * @return the ColumnElements mapped by this mapping field
     */
    public ColumnElement[] getColumns() {
        if (!isMappingRelationship()) {
            List cols = getColumnObjects();
        
            return (ColumnElement[]) cols.toArray(
                new ColumnElement[cols.size()]);
        }

        throw new IllegalStateException();
    }

    /**
     * Adds a column to the list of columnNames mapped by this mapping field.
     * This method will throw an exception if the field is a relationship.
     * @param column column element to be added to the mapping
     * @throws ModelException if impossible
     */
    public void addColumn(ColumnElement column) throws ModelException {
        if (column != null) {
            if (isMappingRelationship()) {
                throw new ModelException(
                    getMessageHelper().msg("mapping.column.column_relationship", // NOI18N
                        column));
            } else {
                List columnList = getColumnNamesInternal();
                String columnName = NameUtil.getRelativeMemberName(
                        column.getName().getFullName());

                if (!columnList.contains(columnName)) {
                    try {
                        fireVetoableChange(PROP_COLUMNS, null, null);
                        columnList.add(columnName);
                        firePropertyChange(PROP_COLUMNS, null, null);

                        // sync up runtime's object list too
                        columns = null;
                    } catch (PropertyVetoException e) {
                        throw new ModelVetoException(e);
                    }
                } else {
                    // this part was blank -- do we want an error or skip here?
                }
            }
        } else {
            throw new ModelException(
                    getMessageHelper().msg("mapping.element.null_argument")); // NOI18N
        }
    }

    /**
     * Removes a column from the list of columns mapped by this mapping field.
     * This method will throw an exception if the field is a relationship.
     * @param columnName the relative name of the column to be removed from the
     * mapping
     * @throws ModelException if impossible
     */
    public void removeColumn(String columnName) throws ModelException {
        if (isMappingRelationship()) {
            throw new ModelException(
                getMessageHelper().msg("mapping.column.column_relationship", // NOI18N
                    columnName));
        }

        if (columnName != null) {
            try {
                fireVetoableChange(PROP_COLUMNS, null, null);

                if (!getColumnNamesInternal().remove(columnName)) {
                    throw new ModelException(
                            getMessageHelper().msg(
                                    "mapping.element.element_not_removed", // NOI18N
                                    columnName));
                }

                firePropertyChange(PROP_COLUMNS, null, null);

                // sync up runtime's object list too
                columns = null;
            } catch (PropertyVetoException e) {
                throw new ModelVetoException(e);
            }
        }
    }

    protected boolean isMappedToTable(MappingTable table) {
        String tableName = table.getName();
        Iterator iterator = getColumnNamesInternal().iterator();

        while (iterator.hasNext()) {
            String columnName = iterator.next().toString();

            if (NameUtil.getTableName(columnName).equals(tableName)) {
                return true;
            }
        }

        return false;
    }

    //============= extra object support for runtime ========================

    /**
     * Returns the list of columnNames (ColumnElements) to which this mapping
     * field is mapped.  This method should only be used by the runtime.
     * @return the columnNames mapped by this mapping field
     */
    private List getColumnObjects() {
        //@olsen: compute objects on access
        if (columns == null) {
            //@olsen: calculate the column objects based on
            //        the column names as stored in columnNames
            //columns = new ArrayList();
            columns = MappingClassImplDynamic.toColumnObjects(
                    getDeclaringMappingClass().getDatabaseSchemaName(),
                    getColumnNamesInternal());
        }

        return columns;
    }

    // </editor-fold>

    // </editor-fold>
}
