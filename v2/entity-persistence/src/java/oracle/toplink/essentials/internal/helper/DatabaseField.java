/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package oracle.toplink.essentials.internal.helper;

import java.io.*;
import oracle.toplink.essentials.internal.databaseaccess.*;

/**
 * INTERNAL:
 * <p><b>Purpose</b>:
 * Define a fully qualified field name.<p>
 * <b>Responsibilities</b>:    <ul>
 * <li> Know its name and its table.
 * </ul>
 * @see DatabaseTable
 */
public class DatabaseField implements Cloneable, Serializable {
    /** Variables used for generating DDL **/
    protected int scale;
    protected int length;
    protected int precision;
    protected boolean isUnique;
    protected boolean isNullable;
    protected boolean isUpdatable;
    protected boolean isInsertable;
    protected String columnDefinition;
    
    /** Column name of the field. */
    protected String name;

    /** Fields table (encapsulates name + creator). */
    protected DatabaseTable table;

    /** Respective Java type desired for the field's value, used to optimize performance and for binding. */
    public transient Class type;
    
    /**
     * Respective JDBC type of the field's value.
     * This overrides the class type, which the JDBC type is normally computed from.
     */
    public int sqlType;

    /** Store normal index of field in result set to optimize performance. */
    protected int index;

    public DatabaseField() {
        this("", new DatabaseTable());
    }

    public DatabaseField(String qualifiedName) {
        this.index = -1;
        this.sqlType = -1;
        int index = qualifiedName.lastIndexOf('.');

        if (index == -1) {
            this.name = qualifiedName;
            this.table = new DatabaseTable();
        } else {
            this.name = qualifiedName.substring(index + 1, qualifiedName.length());
            this.table = new DatabaseTable(qualifiedName.substring(0, index));
        }
        
        initDDLFields();
    }

    public DatabaseField(String fieldName, String tableName) {
        this(fieldName, new DatabaseTable(tableName));
    }

    public DatabaseField(String fieldName, DatabaseTable databaseTable) {
        this.index = -1;
        this.sqlType = -1;
        this.name = fieldName;
        this.table = databaseTable;
        initDDLFields();
    }
    
    /**
     * Inits the DDL generation fields. Currently equivalent to the defaults
     * from the EJB 3.0 spec.
     */
    public void initDDLFields() {
        scale = 0;
        length = 255;
        precision = 0;
        isUnique = false;
        isNullable = true;
        isUpdatable = true;
        isInsertable = true;
        columnDefinition = "";
    }

    /**
     * The table is not cloned because it is treated as an automic value.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
        }

        return null;
    }

    /**
     * Determine whether the receiver is equal to a DatabaseField.
     * Return true if the receiver and field have the same name and table.
     * Also return true if the table of the receiver or field are unspecfied,
     * ie. have no name.
     */
    public boolean equals(DatabaseField field) {
        if (this == field) {
            return true;
        }

        if (field != null) {
            if (DatabasePlatform.shouldIgnoreCaseOnFieldComparisons()) {
                if (getName().equalsIgnoreCase(field.getName())) {
                    if ((getTableName().length() == 0) || (field.getTableName().length() == 0)) {
                        return true;
                    }
                    return (getTable().equals(field.getTable()));
                }
            } else {
                if (getName().equals(field.getName())) {
                    if ((getTableName().length() == 0) || (field.getTableName().length() == 0)) {
                        return true;
                    }
                    return (getTable().equals(field.getTable()));
                }
            }
        }

        return false;
    }
    
    /**
     * Determine whether the receiver is equal to a DatabaseField.
     * Return true if the receiver and field have the same name and table.
     * Also return true if the table of the receiver or field are unspecfied,
     * ie. have no name.
     */
    public boolean equals(Object object) {
        if (!(object instanceof DatabaseField)) {
            return false;
        }

        return equals((DatabaseField)object);
    }
    
    /**
     * Get the SQL fragment that is used when generating the DDL for the column.
     */
    public String getColumnDefinition() {
        return this.columnDefinition;
    }
    
    /**
     * Return the expected index that this field will occur in the result set 
     * row. This is used to optimize performance of database row field lookups.
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Used to specify the column length when generating DDL. 
     */
    public int getLength() {
        return this.length;
    }
    
    /**
     * Return the unqualified name of the field.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the precision for a decimal column when generating DDL.
     */
    public int getPrecision() {
        return this.precision;
    }
    
    /**
     * Return the qualified name of the field.
     */
    public String getQualifiedName() {
        if (hasTableName()) {
            return getTable().getQualifiedName() + "." + getName();
        } else {
            return getName();
        }
    }
    
    /**
     * Returns the scale for a decimal column when generating DDL. 
     */
    public int getScale() {
        return this.scale;
    }
    
    /**
     * Return the JDBC type that coresponds to the field.
     * The JDBC type is normally determined from the class type,
     * but this allows it to be overriden for types that do not match directly 
     * to a Java type, such as MONEY or ARRAY, STRUCT, XMLTYPE, etc.
     * This can be used for binding or stored procedure usage.
     */
    public int getSqlType() {
        return sqlType;
    }

    /**
     * 
     */
    public DatabaseTable getTable() {
        return table;
    }

    /**
     * 
     */
    public String getTableName() {
        return getTable().getName();
    }

    /**
     * 
     */
    public Class getType() {
        return type;
    }
    
    /**
     * Return the hashcode of the name, because it is fairly unqiue.
     */
    public int hashCode() {
        return getName().hashCode();
    }
    
    public boolean hasTableName() {
        if (getTable() == null) {
            return false;
        }
        if (getTable().getName() == null) {
            return false;
        }
        return !(getTable().getName().equals(""));
    }

    /**
     * Used to specify whether the column should be included in SQL UPDATE
     * statements.
     */    
    public boolean isInsertable() {
        return this.isInsertable;
    }
    
    /**
     * Used for generatating DDL. Returns true if the database column is 
     * nullable. 
     */ 
    public boolean isNullable() {
        return this.isNullable;
    }
    
    /**
     * Used for generatating DDL. Returns true if the field is a unique key. 
     */
    public boolean isUnique() {
        return this.isUnique;
    }
    
    /**
     * Returns true is this database field should be read only.
     */
    public boolean isReadOnly() {
        return (! isUpdatable && ! isInsertable);
    }
    
    /**
     * Returns whether the column should be included in SQL INSERT
     * statements. 
     */ 
    public boolean isUpdatable() {
        return this.isUpdatable;
    }
    
    /**
     * Reset the field's name and table from the qualified name.
     */
    public void resetQualifiedName(String qualifiedName) {
        setIndex(-1);
        int index = qualifiedName.lastIndexOf('.');

        if (index == -1) {
            setName(qualifiedName);
            getTable().setName("");
            getTable().setTableQualifier("");
        } else {
            setName(qualifiedName.substring(index + 1, qualifiedName.length()));
            getTable().setPossiblyQualifiedName(qualifiedName.substring(0, index));
        }
    }
    
    /**
     * Set the SQL fragment that is used when generating the DDL for the column.
     */
    public void setColumnDefinition(String columnDefinition) {
        this.columnDefinition = columnDefinition;
    }
    
    /**
     * Set the expected index that this field will occur in the result set row.
     * This is used to optimize performance of database row field lookups.
     */
    public void setIndex(int index) {
        this.index = index;
    }
    
    /**
     * Used to specify whether the column should be included in SQL UPDATE
     * statements.
     */
    public void setInsertable(boolean isInsertable) {
        this.isInsertable = isInsertable;
    }
    
    /**
     * Used to specify the column length when generating DDL.
     */
    public void setLength(int length) {
        this.length = length;
    }
    
    /**
     * Set the unqualified name of the field.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Used for generatating DDL. Set to true if the database column is 
     * nullable. 
     */
    public void setNullable(boolean isNullable) {
        this.isNullable = isNullable;
    }
    
    /**
     * Used to specify the precision for a decimal column when generating DDL.
     */
    public void setPrecision(int precision) {
        this.precision = precision;
    }
    
    /**
     * Used to specify the scale for a decimal column when generating DDL.
     */
    public void setScale(int scale) {
        this.scale = scale;
    }
    
    /**
     * Set the JDBC type that coresponds to the field.
     * The JDBC type is normally determined from the class type,
     * but this allows it to be overriden for types that do not match directly 
     * to a Java type, such as MONEY or ARRAY, STRUCT, XMLTYPE, etc.
     * This can be used for binding or stored procedure usage.
     */
    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }
    
    /**
     * Set the table for the field.
     */
    public void setTable(DatabaseTable table) {
        this.table = table;
    }
    
    /**
     * Set the table name for this field.
     */
    public void setTableName(String tableName) {
        setTable(new DatabaseTable(tableName));
    }

    /**
     * Set the Java class type that coresponds to the field.
     * The JDBC type is determined from the class type,
     * this is used to optimize performance, and for binding.
     */
    public void setType(Class type) {
        this.type = type;
    }

    /**
     * Used for generatating DDL. Set to true if the field is a unique key. 
     */
    public void setUnique(boolean isUnique) {
        this.isUnique = isUnique;
    }
    
    /**
     * Used to specify whether the column should be included in SQL INSERT
     * statements.
     */
    public void setUpdatable(boolean isUpdatable) {
        this.isUpdatable = isUpdatable;
    }
    
    public String toString() {
        return this.getQualifiedName();
    }
}
