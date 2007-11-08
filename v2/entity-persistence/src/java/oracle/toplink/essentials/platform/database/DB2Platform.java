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
import java.sql.*;
import java.util.*;

import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.expressions.ParameterExpression;
import oracle.toplink.essentials.internal.helper.BasicTypeHelperImpl;

/**
 * <p><b>Purpose</b>: Provides DB2 specific behaviour.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Native SQL for byte[], Date, Time, & Timestamp.
 * <li> Support for table qualified names.
 * </ul>
 *
 * @since TOPLink/Java 1.0
 */
public class DB2Platform extends oracle.toplink.essentials.platform.database.DatabasePlatform {

    /**
     * INTERNAL:
     * Append a byte[] in native DB@ format BLOB(hexString) if usesNativeSQL(),
     * otherwise use ODBC format from DatabasePLatform.
     */
    protected void appendByteArray(byte[] bytes, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("BLOB(x'");
            Helper.writeHexString(bytes, writer);
            writer.write("')");
        } else {
            super.appendByteArray(bytes, writer);
        }
    }

    /**
     * INTERNAL:
     * Appends the Date in native format if usesNativeSQL() otherwise use ODBC format from DatabasePlatform.
     * Native format: 'mm/dd/yyyy'
     */
    protected void appendDate(java.sql.Date date, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            appendDB2Date(date, writer);
        } else {
            super.appendDate(date, writer);
        }
    }

    /**
     * INTERNAL:
     * Write a timestamp in DB2 specific format (mm/dd/yyyy).
     */
    protected void appendDB2Date(java.sql.Date date, Writer writer) throws IOException {
        writer.write("'");
        // PERF: Avoid deprecated get methods, that are now very inefficient and used from toString.
        Calendar calendar = Helper.allocateCalendar();
        calendar.setTime(date);

        if ((calendar.get(Calendar.MONTH) + 1) < 10) {
            writer.write('0');
        }
        writer.write(Integer.toString(calendar.get(Calendar.MONTH) + 1));
        writer.write('/');
        if (calendar.get(Calendar.DATE) < 10) {
            writer.write('0');
        }
        writer.write(Integer.toString(calendar.get(Calendar.DATE)));
        writer.write('/');
        writer.write(Integer.toString(calendar.get(Calendar.YEAR)));
        writer.write("'");

        Helper.releaseCalendar(calendar);
    }

    /**
     * INTERNAL:
     * Write a timestamp in DB2 specific format (yyyy-mm-dd-hh.mm.ss.ffffff).
     */
    protected void appendDB2Timestamp(java.sql.Timestamp timestamp, Writer writer) throws IOException {
        // PERF: Avoid deprecated get methods, that are now very inefficient and used from toString.
        Calendar calendar = Helper.allocateCalendar();
        calendar.setTime(timestamp);

        writer.write(Helper.printDate(calendar));
        writer.write('-');
        if (calendar.get(Calendar.HOUR_OF_DAY) < 10) {
            writer.write('0');
        }
        writer.write(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
        writer.write('.');
        if (calendar.get(Calendar.MINUTE) < 10) {
            writer.write('0');
        }
        writer.write(Integer.toString(calendar.get(Calendar.MINUTE)));
        writer.write('.');
        if (calendar.get(Calendar.SECOND) < 10) {
            writer.write('0');
        }
        writer.write(Integer.toString(calendar.get(Calendar.SECOND)));
        writer.write('.');

        Helper.releaseCalendar(calendar);

        // Must truncate the nanos to six decimal places,
        // it is actually a complex algorithm...
        String nanoString = Integer.toString(timestamp.getNanos());
        int numberOfZeros = 0;
        for (int num = Math.min(9 - nanoString.length(), 6); num > 0; num--) {
            writer.write('0');
            numberOfZeros++;
        }
        if ((nanoString.length() + numberOfZeros) > 6) {
            nanoString = nanoString.substring(0, (6 - numberOfZeros));
        }
        writer.write(nanoString);
    }

    /**
     * INTERNAL:
     * Write a timestamp in DB2 specific format (yyyy-mm-dd-hh.mm.ss.ffffff).
     */
    protected void appendDB2Calendar(Calendar calendar, Writer writer) throws IOException {
        int hour;
        int minute;
        int second;
        if (!Helper.getDefaultTimeZone().equals(calendar.getTimeZone())) {
            // Must convert the calendar to the local timezone if different, as dates have no timezone (always local).
            Calendar localCalendar = Helper.allocateCalendar();
            JavaPlatform.setTimeInMillis(localCalendar, JavaPlatform.getTimeInMillis(calendar));
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
            second = calendar.get(Calendar.SECOND);
            Helper.releaseCalendar(localCalendar);
        } else {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
            second = calendar.get(Calendar.SECOND);
        }
        writer.write(Helper.printDate(calendar));
        writer.write('-');
        if (hour < 10) {
            writer.write('0');
        }
        writer.write(Integer.toString(hour));
        writer.write('.');
        if (minute < 10) {
            writer.write('0');
        }
        writer.write(Integer.toString(minute));
        writer.write('.');
        if (second < 10) {
            writer.write('0');
        }
        writer.write(Integer.toString(second));
        writer.write('.');

        // Must truncate the nanos to six decimal places,
        // it is actually a complex algorithm...
        String millisString = Integer.toString(calendar.get(Calendar.MILLISECOND));
        int numberOfZeros = 0;
        for (int num = Math.min(3 - millisString.length(), 3); num > 0; num--) {
            writer.write('0');
            numberOfZeros++;
        }
        if ((millisString.length() + numberOfZeros) > 3) {
            millisString = millisString.substring(0, (3 - numberOfZeros));
        }
        writer.write(millisString);
    }

    /**
     * INTERNAL:
     * Append the Time in Native format if usesNativeSQL() otherwise use ODBC format from DAtabasePlatform.
     * Native Format: 'hh:mm:ss'
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
     * Append the Timestamp in native format if usesNativeSQL() is true otherwise use ODBC format from DatabasePlatform.
     * Native format: 'YYYY-MM-DD-hh.mm.ss.SSSSSS'
     */
    protected void appendTimestamp(java.sql.Timestamp timestamp, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("'");
            appendDB2Timestamp(timestamp, writer);
            writer.write("'");
        } else {
            super.appendTimestamp(timestamp, writer);
        }
    }

    /**
     * INTERNAL:
     * Append the Timestamp in native format if usesNativeSQL() is true otherwise use ODBC format from DatabasePlatform.
     * Native format: 'YYYY-MM-DD-hh.mm.ss.SSSSSS'
     */
    protected void appendCalendar(Calendar calendar, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("'");
            appendDB2Calendar(calendar, writer);
            writer.write("'");
        } else {
            super.appendCalendar(calendar, writer);
        }
    }

    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping = new Hashtable();

        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("SMALLINT DEFAULT 0", false));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("FLOAT", false));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("FLOAT", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("DECIMAL", 15));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("DECIMAL", 15));

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

    /**
     * INTERNAL:
     * returns the maximum number of characters that can be used in a field
     * name on this platform.
     */
    public int getMaxFieldNameSize() {
        return 128;
    }

    /**
     * INTERNAL:
     * returns the maximum number of characters that can be used in a foreign key
     * name on this platform.
     */
    public int getMaxForeignKeyNameSize() {
        return 18;
    }

    /**
     * INTERNAL:
     * Return the catalog information through using the native SQL catalog selects.
     * This is required because many JDBC driver do not support meta-data.
     * Willcards can be passed as arguments.
     */
    public Vector getNativeTableInfo(String table, String creator, AbstractSession session) {
        String query = "SELECT * FROM SYSIBM.SYSTABLES WHERE TBCREATOR NOT IN ('SYS', 'SYSTEM')";
        if (table != null) {
            if (table.indexOf('%') != -1) {
                query = query + " AND TBNAME LIKE " + table;
            } else {
                query = query + " AND TBNAME = " + table;
            }
        }
        if (creator != null) {
            if (creator.indexOf('%') != -1) {
                query = query + " AND TBCREATOR LIKE " + creator;
            } else {
                query = query + " AND TBCREATOR = " + creator;
            }
        }
        return session.executeSelectingCall(new oracle.toplink.essentials.queryframework.SQLCall(query));
    }

    /**
     * INTERNAL:
     * Used for sp calls.
     */
    public String getProcedureCallHeader() {
        return "CALL ";
    }
    
    /**
     * INTERNAL:
     */
    public String getSelectForUpdateString() {
        return " FOR UPDATE";
    }
    
    /**
     * INTERNAL:
     * Used for stored procedure defs.
     */
    public String getProcedureEndString() {
        return "END";
    }
    
    /**
     * INTERNAL:
     * Used for stored procedure defs.
     */
    public String getProcedureBeginString() {
        return "BEGIN";
    }
    
    /**
     * INTERNAL:
     * Used for stored procedure defs.
     */
    public String getProcedureAsString() {
        return "";
    }
    
    /**
     * INTERNAL:
     * This is required in the construction of the stored procedures with
     * output parameters
     */
    public boolean shouldPrintOutputTokenAtStart() {
        return true;
    }
    
    /**
     * INTERNAL:
     * This method returns the query to select the timestamp
     * from the server for DB2.
     */
    public ValueReadQuery getTimestampQuery() {
        if (timestampQuery == null) {
            timestampQuery = new ValueReadQuery();
            timestampQuery.setSQLString("SELECT DISTINCT CURRENT TIMESTAMP FROM SYSIBM.SYSTABLES");
        }
        return timestampQuery;
    }

    /**
     * INTERNAL:
     * Initialize any platform-specific operators
     */
    protected void initializePlatformOperators() {
        super.initializePlatformOperators();

        addOperator(ExpressionOperator.simpleFunction(ExpressionOperator.ToUpperCase, "UCASE"));
        addOperator(ExpressionOperator.simpleFunction(ExpressionOperator.ToLowerCase, "LCASE"));
        addOperator(concatOperator());
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Instring, "Locate"));
        //CR#2811076 some missing DB2 functions added.
        addOperator(ExpressionOperator.simpleFunction(ExpressionOperator.ToNumber, "DECIMAL"));
        addOperator(ExpressionOperator.simpleFunction(ExpressionOperator.ToChar, "CHAR"));
        addOperator(ExpressionOperator.simpleFunction(ExpressionOperator.DateToString, "CHAR"));
        addOperator(ExpressionOperator.simpleFunction(ExpressionOperator.ToDate, "DATE"));
    }

    public boolean isDB2() {
        return true;
    }

    /**
     * INTERNAL:
     * Builds a table of maximum numeric values keyed on java class. This is used for type testing but
     * might also be useful to end users attempting to sanitize values.
     * <p><b>NOTE</b>: BigInteger & BigDecimal maximums are dependent upon their precision & Scale
     */
    public Hashtable maximumNumericValues() {
        Hashtable values = new Hashtable();

        values.put(Integer.class, new Integer(Integer.MAX_VALUE));
        values.put(Long.class, new Long((long)Integer.MAX_VALUE));
        values.put(Float.class, new Float(123456789));
        values.put(Double.class, new Double((double)Float.MAX_VALUE));
        values.put(Short.class, new Short(Short.MAX_VALUE));
        values.put(Byte.class, new Byte(Byte.MAX_VALUE));
        values.put(java.math.BigInteger.class, new java.math.BigInteger("999999999999999"));
        values.put(java.math.BigDecimal.class, new java.math.BigDecimal("0.999999999999999"));
        return values;
    }

    /**
     * INTERNAL:
     * Builds a table of minimum numeric values keyed on java class. This is used for type testing but
     * might also be useful to end users attempting to sanitize values.
     * <p><b>NOTE</b>: BigInteger & BigDecimal minimums are dependent upon their precision & Scale
     */
    public Hashtable minimumNumericValues() {
        Hashtable values = new Hashtable();

        values.put(Integer.class, new Integer(Integer.MIN_VALUE));
        values.put(Long.class, new Long((long)Integer.MIN_VALUE));
        values.put(Float.class, new Float(-123456789));
        values.put(Double.class, new Double((double)Float.MIN_VALUE));
        values.put(Short.class, new Short(Short.MIN_VALUE));
        values.put(Byte.class, new Byte(Byte.MIN_VALUE));
        values.put(java.math.BigInteger.class, new java.math.BigInteger("-999999999999999"));
        values.put(java.math.BigDecimal.class, new java.math.BigDecimal("-0.999999999999999"));
        return values;
    }

    /**
     * INTERNAL:
     * Allow for the platform to ignore exceptions.
     * This is required for DB2 which throws no-data modified as an exception.
     */
    public boolean shouldIgnoreException(SQLException exception) {
        if (exception.getMessage().equals("No data found") || exception.getMessage().equals("No row was found for FETCH, UPDATE or DELETE; or the result of a query is an empty table") || (exception.getErrorCode() == 100)) {
            return true;
        }
        return super.shouldIgnoreException(exception);
    }

    /**
     * INTERNAL:
     * JDBC defines and outer join syntax, many drivers do not support this. So we normally avoid it.
     */
    public boolean shouldUseJDBCOuterJoinSyntax() {
        return false;
    }

    /**
     * The Concat operator is of the form
     * .... VARCHAR ( <operand1> || <operand2> )
     */
    private ExpressionOperator concatOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(ExpressionOperator.FunctionOperator);
        exOperator.setSelector(ExpressionOperator.Concat);
        Vector v = new Vector(5);
        v.addElement("VARCHAR(");
        v.addElement(" || ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build the identity query for native sequencing.
     */
    public ValueReadQuery buildSelectQueryForIdentity() {
        ValueReadQuery selectQuery = new ValueReadQuery();
        StringWriter writer = new StringWriter();
        writer.write("SELECT IDENTITY_VAL_LOCAL() FROM SYSIBM.SYSDUMMY1");

        selectQuery.setSQLString(writer.toString());
        return selectQuery;
    }

    /**
     * INTERNAL:
     * Append the receiver's field 'identity' constraint clause to a writer
     */
    public void printFieldIdentityClause(Writer writer) throws ValidationException {
        try {
            writer.write(" GENERATED ALWAYS AS IDENTITY");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /**
     *  INTERNAL:
     *  Indicates whether the platform supports identity.
     *  DB2 does through AS IDENTITY field types.
     *  This method is to be used *ONLY* by sequencing classes
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
         return "DECLARE GLOBAL TEMPORARY TABLE ";
     }

    /**
     * INTERNAL:
     */
     public DatabaseTable getTempTableForTable(DatabaseTable table) {
         DatabaseTable tempTable =  super.getTempTableForTable(table);
         tempTable.setTableQualifier("session");
         return tempTable;
     }

    /**
     * INTERNAL:
     */
    protected String getCreateTempTableSqlSuffix() {
        return " ON COMMIT DELETE ROWS NOT LOGGED";
    }          

    /**
     * INTERNAL:
     */
     protected String getCreateTempTableSqlBodyForTable(DatabaseTable table) {
         return " LIKE " + table.getQualifiedName();
     }               

    /**
     * INTERNAL:
     * Override this if the platform cannot handle NULL in select clause.
     */
    public boolean isNullAllowedInSelectClause() {
        return false;
    }


    public void writeParameterMarker(Writer writer, ParameterExpression parameter) throws IOException {
        // DB2 requires cast around parameter markers if both operands of certian
        // operators are parameter markers
        // This method generates CAST for parameter markers whose type is correctly
        // identified by the query compiler
        String paramaterMarker = "?";
        Object type = parameter.getType();
        if(type != null) {
            BasicTypeHelperImpl typeHelper = BasicTypeHelperImpl.getInstance();
            String castType = null;
            if (typeHelper.isBooleanType(type) || typeHelper.isByteType(type) || typeHelper.isShortType(type)) {
                castType = "SMALLINT";
            } else if (typeHelper.isIntType(type)) {
                castType = "INTEGER";
            } else if (typeHelper.isLongType(type)) {
                castType = "BIGINT";
            } else if (typeHelper.isFloatType(type)) {
                castType = "REAL";
            } else if (typeHelper.isDoubleType(type)) {
                castType = "DOUBLE";
            } else if (typeHelper.isStringType(type)) {
                castType = "VARCHAR(32672)";
            }

            if(castType != null){
                paramaterMarker = "CAST (? AS " + castType + " )";
            }
        }
        writer.write(paramaterMarker);
    }

    /**
     * INTERNAL
     * Allows platform to choose whether to bind literals in DatabaseCalls or not.
     */
    public boolean shouldBindLiterals() {
        return false;
    }

}
