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
package oracle.toplink.essentials.platform.database;

import oracle.toplink.essentials.internal.databaseaccess.FieldTypeDefinition;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.helper.DatabaseTable;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.queryframework.ValueReadQuery;

import java.util.Vector;
import java.io.Writer;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Iterator;
import oracle.toplink.essentials.internal.helper.DatabaseField;

/**
 * <p><b>Purpose</b>: Provides Derby DBMS specific behaviour.
 *
 * @since TOPLink Essentials 1.0
 */
public class DerbyPlatform extends DB2Platform {

    /**
     * INTERNAL:
     * TODO: Need to find out how can byte arrays be inlined in Derby
     */
    protected void appendByteArray(byte[] bytes, Writer writer) throws IOException {
            super.appendByteArray(bytes, writer);
    }

    /**
     * INTERNAL:
     * This method returns the query to select the timestamp from the server
     * for Derby.
     */
    public ValueReadQuery getTimestampQuery() {
        if (timestampQuery == null) {
            timestampQuery = new ValueReadQuery();
            timestampQuery.setSQLString("VALUES CURRENT_TIMESTAMP");
        }
        return timestampQuery;

    }

    //TODO: check with reviewer. This method should be made private in DB2platform
    public Vector getNativeTableInfo(String table, String creator, AbstractSession session) {
        throw new RuntimeException("Should never reach here");
    }

    /**
     * Used for stored procedure defs.
     */
    public String getProcedureEndString() {
        return getBatchEndString();
    }

    /**
     * Used for stored procedure defs.
     */
    public String getProcedureBeginString() {
        return getBatchBeginString();
    }

    /**
     * This method is used to print the output parameter token when stored
     * procedures are called
     */
    public String getInOutputProcedureToken() {
        return "INOUT";
    }

    /**
     * This is required in the construction of the stored procedures with
     * output parameters
     */
    public boolean shouldPrintOutputTokenAtStart() {
        //TODO: Check with the reviewer where this is used
        return false;
    }

    /**
     * INTERNAL:
     * Answers whether platform is Derby
     */
    public boolean isDerby() {
        return true;
    }

    public boolean isDB2() {
        //This class inhertits from DB2. But it is not DB2
        return false;
    }

    /**
     * Allow for the platform to ignore exceptions.
     */
    public boolean shouldIgnoreException(SQLException exception) {
        // Nothing is ignored.
        return false;
    }


    /**
     * INTERNAL:
     */
    protected String getCreateTempTableSqlSuffix() {
        return " ON COMMIT DELETE ROWS NOT LOGGED";
    }

    /**
     * INTERNAL:
     * Build the identity query for native sequencing.
     */
    public ValueReadQuery buildSelectQueryForIdentity() {
        ValueReadQuery selectQuery = new ValueReadQuery();
        selectQuery.setSQLString("values IDENTITY_VAL_LOCAL()");
        return selectQuery;
    }

    /**
     * INTERNAL:
     * Indicates whether temporary table can specify primary keys (some platforms don't allow that).
     * Used by writeCreateTempTableSql method.
     */
    protected boolean shouldTempTableSpecifyPrimaryKeys() {
        return false;
    }
    
    /**
     * INTERNAL:
     */
     protected String getCreateTempTableSqlBodyForTable(DatabaseTable table) {
        // returning null includes fields of the table in body
        // see javadoc of DatabasePlatform#getCreateTempTableSqlBodyForTable(DataBaseTable)
        // for details
        return null;
     }

    /**
     * INTERNAL:
     * May need to override this method if the platform supports temporary tables
     * and the generated sql doesn't work.
     * Write an sql string for updating the original table from the temporary table.
     * Precondition: supportsTempTables() == true.
     * Precondition: pkFields and assignFields don't intersect.
     * @parameter Writer writer for writing the sql
     * @parameter DatabaseTable table is original table for which temp table is created.
     * @parameter Collection pkFields - primary key fields for the original table.
     * @parameter Collection assignedFields - fields to be assigned a new value.
     */
     public void writeUpdateOriginalFromTempTableSql(Writer writer, DatabaseTable table,
                                                     Collection pkFields,
                                                     Collection assignedFields) throws IOException 
    {
        writer.write("UPDATE ");
        String tableName = table.getQualifiedName();
        writer.write(tableName);
        writer.write(" SET ");
        
        String tempTableName = getTempTableForTable(table).getQualifiedName();
        boolean isFirst = true;
        Iterator itFields = assignedFields.iterator();
        while(itFields.hasNext()) {
            if(isFirst) {
                isFirst = false;
            } else {
                writer.write(", ");
            }
            DatabaseField field = (DatabaseField)itFields.next();
            String fieldName = field.getName();
            writer.write(fieldName);
            writer.write(" = (SELECT ");
            writer.write(fieldName);
            writer.write(" FROM ");
            writer.write(tempTableName);
            writeAutoJoinWhereClause(writer, null, tableName, pkFields);
            writer.write(")");
        }
        
        writer.write(" WHERE EXISTS(SELECT ");
        writer.write(((DatabaseField)pkFields.iterator().next()).getName());
        writer.write(" FROM ");
        writer.write(tempTableName);
        writeAutoJoinWhereClause(writer, null, tableName, pkFields);
        writer.write(")");
    }          

    /**
     * INTERNAL:
     * Append the receiver's field 'identity' constraint clause to a writer.
     */
    public void printFieldIdentityClause(Writer writer) throws ValidationException {
        try {
            writer.write(" GENERATED ALWAYS AS IDENTITY");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }
    
    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping = new Hashtable();

        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("SMALLINT DEFAULT 0", false));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("FLOAT"));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("FLOAT", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("DECIMAL"));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("DECIMAL"));

        fieldTypeMapping.put(String.class, new FieldTypeDefinition("VARCHAR", 255));
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR", 1));
        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("BLOB", 64000));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("CLOB", 64000));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("BLOB", 64000));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("CLOB", 64000));
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("BLOB", 64000));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("CLOB", 64000));        
        
        fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATE", false));
        fieldTypeMapping.put(java.sql.Time.class, new FieldTypeDefinition("TIME", false));
        fieldTypeMapping.put(java.sql.Timestamp.class, new FieldTypeDefinition("TIMESTAMP", false));

        return fieldTypeMapping;
    }

}
