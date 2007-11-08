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

import java.io.*;
import java.util.*;

import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.expressions.ExpressionOperator;
import oracle.toplink.essentials.internal.databaseaccess.FieldTypeDefinition;
import oracle.toplink.essentials.internal.expressions.FunctionExpression;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.queryframework.ValueReadQuery;

/**
 * <p><b>Purpose</b>: Provides MySQL specific behaviour.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Native SQL for Date, Time, & Timestamp.
 * <li> Native sequencing.
 * <li> Mapping of class types to database types for the schema framework.
 * <li> Pessimistic locking.
 * <li> Platform specific operators.
 * </ul>
 *
 * @since OracleAS TopLink 10<i>g</i> (10.1.3)
 */
public class MySQL4Platform extends DatabasePlatform {

    /**
     * INTERNAL:
     * Appends an MySQL specific date if usesNativeSQL is true otherwise use the ODBC format.
     * Native FORMAT: 'YYYY-MM-DD'
     */
    protected void appendDate(java.sql.Date date, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("'");
            writer.write(Helper.printDate(date));
            writer.write("'");
        } else {
            super.appendDate(date, writer);
        }
    }

    /**
     * INTERNAL:
     * Appends an MySQL specific time if usesNativeSQL is true otherwise use the ODBC format.
     * Native FORMAT: 'HH:MM:SS'.
     */
    protected void appendTime(java.sql.Time time, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("'");
            writer.write(Helper.printTime(time));
            writer.write("'");
        } else {
            super.appendTime(time, writer);
        }
    }

    /**
     * INTERNAL:
     * Appends an MySQL specific Timestamp, if usesNativeSQL is true otherwise use the ODBC format.
     * Native Format: 'YYYY-MM-DD HH:MM:SS' 
     */
    protected void appendTimestamp(java.sql.Timestamp timestamp, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("'");
            writer.write(Helper.printTimestampWithoutNanos(timestamp));
            writer.write("'");
        } else {
            super.appendTimestamp(timestamp, writer);
        }
    }

    /**
     * INTERNAL:
     * Appends an MySQL specific Timestamp, if usesNativeSQL is true otherwise use the ODBC format.
     * Native Format: 'YYYY-MM-DD HH:MM:SS'
     */
    protected void appendCalendar(Calendar calendar, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("'");
            writer.write(Helper.printCalendarWithoutNanos(calendar));
            writer.write("'");
        } else {
            super.appendCalendar(calendar, writer);
        }
    }

    /**
     * INTERNAL:
     * Return the mapping of class types to database types for the schema framework.
     */
    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping;

        fieldTypeMapping = new Hashtable();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("TINYINT(1) default 0", false));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("FLOAT", false));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("DOUBLE", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("TINYINT", false));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("DECIMAL",38));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("DECIMAL",38));

        fieldTypeMapping.put(String.class, new FieldTypeDefinition("VARCHAR", 255));
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR", 1));

        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("BLOB", 64000));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("TEXT", 64000));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("BLOB", 64000));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("TEXT", 64000));
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("BLOB", 64000));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("TEXT", 64000));
        
        fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATE", false));
        fieldTypeMapping.put(java.sql.Time.class, new FieldTypeDefinition("TIME", false));
        fieldTypeMapping.put(java.sql.Timestamp.class, new FieldTypeDefinition("DATETIME", false));

        return fieldTypeMapping;
    }

    /**
     * INTERNAL:
     * Build the identity query for native sequencing.
     */
    public ValueReadQuery buildSelectQueryForIdentity() {
        ValueReadQuery selectQuery = new ValueReadQuery();
        StringWriter writer = new StringWriter();
        writer.write("SELECT LAST_INSERT_ID()");
        selectQuery.setSQLString(writer.toString());
        return selectQuery;
    }

    /**
     * INTERNAL:
     * Used for constraint deletion.
     */
    public String getConstraintDeletionString() {
        return " DROP FOREIGN KEY ";
    }
    
    /**
     * INTERNAL:
     * Used for pessimistic locking.
     */
    public String getSelectForUpdateString() {
        return " FOR UPDATE";
    }
    
    /**
     * INTERNAL:
     * This method returns the query to select the timestamp
     * from the server for MySQL.
     */
    public ValueReadQuery getTimestampQuery() {
        if (timestampQuery == null) {
            timestampQuery = new ValueReadQuery();
            timestampQuery.setSQLString("SELECT NOW()");
        }
        return timestampQuery;
    }

    /**
     * Answers whether platform is MySQL
     */
    public boolean isMySQL() {
        return true;
    }

    /**
     * INTERNAL:
     * Initialize any platform-specific operators
     */
    protected void initializePlatformOperators() {
        super.initializePlatformOperators();
        addOperator(logOperator());
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Atan2, "ATAN2"));
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Concat, "CONCAT"));
        addOperator(toNumberOperator());
        addOperator(toCharOperator());
        addOperator(toDateOperator());
        addOperator(dateToStringOperator());
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Nvl, "IFNULL"));
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Trunc, "TRUNCATE"));
        addOperator(leftTrim2());
        addOperator(rightTrim2());
    }

    /**
     * INTERNAL:
     * Create the 10 based log operator for this platform
     */
    protected ExpressionOperator logOperator() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(ExpressionOperator.Log);
        Vector v = new Vector(2);
        v.addElement("LOG(");
        v.addElement(", 10)");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(FunctionExpression.class);
        return result;

    }

    /**
     * INTERNAL:
     * Build MySQL equivalent to TO_NUMBER.
     */
    protected ExpressionOperator toNumberOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(ExpressionOperator.FunctionOperator);
        exOperator.setSelector(ExpressionOperator.ToNumber);
        Vector v = new Vector(2);
        v.addElement("CONVERT(");
        v.addElement(", SIGNED)");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build MySQL equivalent to TO_DATE.
     */
    protected ExpressionOperator toDateOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(ExpressionOperator.FunctionOperator);
        exOperator.setSelector(ExpressionOperator.ToDate);
        Vector v = new Vector(2);
        v.addElement("CONVERT(");
        v.addElement(", DATETIME)");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build MySQL equivalent to TO_CHAR.
     */
    protected ExpressionOperator toCharOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(ExpressionOperator.FunctionOperator);
        exOperator.setSelector(ExpressionOperator.ToChar);
        Vector v = new Vector(2);
        v.addElement("CONVERT(");
        v.addElement(", CHAR)");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build MySQL equivalent to TO_CHAR.
     */
    protected ExpressionOperator dateToStringOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(ExpressionOperator.FunctionOperator);
        exOperator.setSelector(ExpressionOperator.DateToString);
        Vector v = new Vector(2);
        v.addElement("CONVERT(");
        v.addElement(", CHAR)");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }
    
    /**
     * INTERNAL:
     * Build MySQL equivalent to LTRIM(string_exp, character).
     * MySQL: TRIM(LEADING character FROM string_exp)
     */
    protected ExpressionOperator leftTrim2() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(ExpressionOperator.FunctionOperator);
        exOperator.setSelector(ExpressionOperator.LeftTrim2);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(5);
        v.addElement("TRIM(LEADING ");
        v.addElement(" FROM ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        int[] indices = {1, 0};
        exOperator.setArgumentIndices(indices);
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build MySQL equivalent to RTRIM(string_exp, character).
     * MySQL: TRIM(TRAILING character FROM string_exp)
     */
    protected ExpressionOperator rightTrim2() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(ExpressionOperator.FunctionOperator);
        exOperator.setSelector(ExpressionOperator.RightTrim2);
        Vector v = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(5);
        v.addElement("TRIM(TRAILING ");
        v.addElement(" FROM ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        int[] indices = {1, 0};
        exOperator.setArgumentIndices(indices);
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Append the receiver's field 'identity' constraint clause to a writer
     */
    public void printFieldIdentityClause(Writer writer) throws ValidationException {
        try {
            writer.write(" AUTO_INCREMENT");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /**
     * INTERNAL:
     * JDBC defines an outer join syntax which many drivers do not support. So we normally avoid it.
     */
    public boolean shouldUseJDBCOuterJoinSyntax() {
        return false;
    }

    /**
     * INTERNAL:
     * Indicates whether the platform supports identity.
     * MySQL supports native sequencing through AUTO_INCREMENT field types.
     */
    public boolean supportsIdentity() {
        return true;
    }

    /**
     * INTERNAL:
     */
    public boolean supportsGlobalTempTables() {
        return true;
    }

    /**
     * INTERNAL:
     */
    protected String getCreateTempTableSqlPrefix() {
        return "CREATE TEMPORARY TABLE IF NOT EXISTS ";
    }

    /**
     * INTERNAL:
     */
    public boolean shouldAlwaysUseTempStorageForModifyAll() {
        return true;
    }
    
    /**
     * INTERNAL:
     */
    public void writeUpdateOriginalFromTempTableSql(Writer writer, DatabaseTable table,
                                                    Collection pkFields,
                                                    Collection assignedFields) throws IOException 
    {
        writer.write("UPDATE ");
        String tableName = table.getQualifiedName();
        writer.write(tableName);
        writer.write(", ");
        String tempTableName = getTempTableForTable(table).getQualifiedName();
        writer.write(tempTableName);
        writeAutoAssignmentSetClause(writer, tableName, tempTableName, assignedFields);
        writeAutoJoinWhereClause(writer, tableName, tempTableName, pkFields);
    }          

    /**
     * INTERNAL:
     */
    public void writeDeleteFromTargetTableUsingTempTableSql(Writer writer, DatabaseTable table, DatabaseTable targetTable,
                                                        Collection pkFields, 
                                                        Collection targetPkFields) throws IOException 
    {
        writer.write("DELETE FROM ");
        String targetTableName = targetTable.getQualifiedName();
        writer.write(targetTableName);
        writer.write(" USING ");
        writer.write(targetTableName);
        writer.write(", ");
        String tempTableName = getTempTableForTable(table).getQualifiedName();
        writer.write(tempTableName);
        writeJoinWhereClause(writer, targetTableName, tempTableName, targetPkFields, pkFields);
    }          
}
