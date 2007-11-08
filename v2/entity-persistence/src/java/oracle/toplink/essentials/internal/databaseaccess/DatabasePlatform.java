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
package oracle.toplink.essentials.internal.databaseaccess;

import java.sql.*;
import java.util.*;
import java.math.*;
import java.io.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.sessions.SessionProfiler;
import oracle.toplink.essentials.sequencing.*;
import oracle.toplink.essentials.internal.sequencing.Sequencing;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.expressions.ParameterExpression;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.tools.schemaframework.TableDefinition;
import oracle.toplink.essentials.tools.schemaframework.FieldDefinition;

/**
 * DatabasePlatform is private to TopLink. It encapsulates behavior specific to a database platform
 * (eg. Oracle, Sybase, DBase), and provides protocol for TopLink to access this behavior. The behavior categories
 * which require platform specific handling are SQL generation and sequence behavior. While database platform
 * currently provides sequence number retrieval behaviour, this will move to a sequence manager (when it is
 * implemented).
 *
 * @see AccessPlatform
 * @see DB2Platform
 * @see DBasePlatform
 * @see OraclePlatform
 * @see SybasePlatform
 *
 * @since TOPLink/Java 1.0
 */
public class DatabasePlatform extends DatasourcePlatform {

    /** Holds a hashtable of values used to map JAVA types to database types for table creation */
    protected transient Hashtable fieldTypes;

    /** Indicates that native SQL should be used for literal values instead of ODBC esacpe format
    Only used with Oracle, Sybase & DB2 */
    protected boolean usesNativeSQL;

    /** Indicates that binding will be used for BLOB data. NOTE: does not work well with ODBC. */
    protected boolean usesByteArrayBinding;

    /** Bind all arguments to any SQL statement. */
    protected boolean shouldBindAllParameters;

    /** Cache all prepared statements, this requires full parameter binding as well. */
    protected boolean shouldCacheAllStatements;

    /** The statement cache size for prepare parameterized statements. */
    protected int statementCacheSize;

    /** Can be used if the app expects upper case but the database is not return consistent case, i.e. different databases. */
    protected boolean shouldForceFieldNamesToUpperCase;

    /** Indicates (if true) to remove blanks characters from the right of CHAR strings. */
    protected boolean shouldTrimStrings;

    /** Indicates that streams will be used to store BLOB data. NOTE: does not work with ODBC */
    protected boolean usesStreamsForBinding;

    /** Indicates the size above which strings will be bound NOTE: does not work with ODBC */
    protected int stringBindingSize;

    /** Indicates that strings will above the stringBindingSize will be bound NOTE: does not work with ODBC */
    protected boolean usesStringBinding;

    /** Allow for the code that is used for preparing cursored outs for a storedprocedure to be settable. **/
    protected int cursorCode;

    /** The transaction isolation level to be set on the connection (optional). */
    protected int transactionIsolation;

    /** Some JDBC drivers do not support AutoCommit in the way TopLink expects.  (e.g. Attunity Connect, JConnect) */
    protected boolean supportsAutoCommit;

    /**
     * Allow for driver level data conversion optimization to be disabled,
     * required because some drivers can loose precision.
     */
    protected boolean shouldOptimizeDataConversion;

    /** Stores mapping of class types to database types for schema creation. */
    protected transient Hashtable classTypes;

    /** Allow for case in field names to be ignored as some databases are not case sensitive and when using custom this can be an issue. */
    protected static boolean shouldIgnoreCaseOnFieldComparisons = false;

    public DatabasePlatform() {
        this.tableQualifier = "";
        this.usesNativeSQL = false;
        this.usesByteArrayBinding = true;
        this.usesStringBinding = false;
        this.stringBindingSize = 255;
        this.shouldTrimStrings = true;
        this.shouldBindAllParameters = false;
        this.shouldCacheAllStatements = false;
        this.shouldOptimizeDataConversion = true;
        this.statementCacheSize = 50;
        this.shouldForceFieldNamesToUpperCase = false;
        this.transactionIsolation = -1;
        this.cursorCode = -10;
        this.supportsAutoCommit = true;
    }

    /**
     * Used for sp defs.
     */
    public boolean allowsSizeInProcedureArguments() {
        return true;
    }

    /**
     * Appends a Boolean value as a number
     */
    protected void appendBoolean(Boolean bool, Writer writer) throws IOException {
        if (bool.booleanValue()) {
            writer.write("1");
        } else {
            writer.write("0");
        }
    }

    /**
     *    Append the ByteArray in ODBC literal format ({b hexString}).
     *    This limits the amount of Binary data by the length of the SQL. Binding should increase this limit.
     */
    protected void appendByteArray(byte[] bytes, Writer writer) throws IOException {
        writer.write("{b '");
        Helper.writeHexString(bytes, writer);
        writer.write("'}");
    }

    /**
     * Answer a platform correct string representation of a Date, suitable for SQL generation.
     * The date is printed in the ODBC platform independent format {d 'yyyy-mm-dd'}.
     */
    protected void appendDate(java.sql.Date date, Writer writer) throws IOException {
        writer.write("{d '");
        writer.write(Helper.printDate(date));
        writer.write("'}");
    }

    /**
     * Write number to SQL string. This is provided so that database which do not support
     * Exponential format can customize their printing.
     */
    protected void appendNumber(Number number, Writer writer) throws IOException {
        writer.write(number.toString());
    }

    /**
     * INTERNAL
     * In case shouldBindLiterals is true, instead of null value a DatabaseField
     * value may be passed (so that it's type could be used for binding null).
     */
    public void appendLiteralToCall(Call call, Writer writer, Object literal) {
        if(shouldBindLiterals()) {
            appendLiteralToCallWithBinding(call, writer, literal);
        } else {
            int nParametersToAdd = appendParameterInternal(call, writer, literal);
            for(int i=0; i < nParametersToAdd; i++ ) {
                ((DatabaseCall)call).getParameterTypes().addElement(DatabaseCall.LITERAL);
            }
        }
    }
    
    /**
     * INTERNAL
     * Override this method in case the platform needs to do something special for binding literals.
     * Note that instead of null value a DatabaseField
     * value may be passed (so that it's type could be used for binding null).
     */
    protected void appendLiteralToCallWithBinding(Call call, Writer writer, Object literal) {
        ((DatabaseCall)call).appendLiteral(writer, literal);
    }
    
    /**
     * Write a database-friendly representation of the given parameter to the writer.
     * Determine the class of the object to be written, and invoke the appropriate print method
     * for that object. The default is "toString".
     * The platform may decide to bind some types, such as byte arrays and large strings.
     * Should only be called in case binding is not used.
     */
    public void appendParameter(Call call, Writer writer, Object parameter) {
        appendParameterInternal(call, writer, parameter);
    }
    
    /**
     * Returns the number of parameters that used binding.
     * Should only be called in case binding is not used.
     */
    public int appendParameterInternal(Call call, Writer writer, Object parameter) {
        int nBoundParameters = 0;
        DatabaseCall databaseCall = (DatabaseCall)call;
        try {
            // PERF: Print Calendars directly avoiding timestamp conversion,
            // Must be before conversion as you cannot bind calendars.
            if (parameter instanceof Calendar) {
                appendCalendar((Calendar)parameter, writer);
                return nBoundParameters;
            }
            Object dbValue = convertToDatabaseType(parameter);

            if (dbValue instanceof String) {// String and number first as they are most common.
                if (usesStringBinding() && (((String)dbValue).length() >= getStringBindingSize())) {
                    databaseCall.bindParameter(writer, dbValue);
                    nBoundParameters = 1;
                } else {
                    appendString((String)dbValue, writer);
                }
            } else if (dbValue instanceof Number) {
                appendNumber((Number)dbValue, writer);
            } else if (dbValue instanceof java.sql.Time) {
                appendTime((java.sql.Time)dbValue, writer);
            } else if (dbValue instanceof java.sql.Timestamp) {
                appendTimestamp((java.sql.Timestamp)dbValue, writer);
            } else if (dbValue instanceof java.sql.Date) {
                appendDate((java.sql.Date)dbValue, writer);
            } else if (dbValue == null) {
                writer.write("NULL");
            } else if (dbValue instanceof Boolean) {
                appendBoolean((Boolean)dbValue, writer);
            } else if (dbValue instanceof byte[]) {
                if (usesByteArrayBinding()) {
                    databaseCall.bindParameter(writer, dbValue);
                    nBoundParameters = 1;
                } else {
                    appendByteArray((byte[])dbValue, writer);
                }
            } else if (dbValue instanceof Vector) {
                nBoundParameters = printValuelist((Vector)dbValue, databaseCall, writer);
            } else if ((parameter instanceof Struct) || (parameter instanceof Array) || (parameter instanceof Ref)) {
                databaseCall.bindParameter(writer, parameter);
                nBoundParameters = 1;
            } else if (oracle.toplink.essentials.internal.helper.Helper.isCollection(dbValue)) {
                nBoundParameters = printValuelist(Helper.makeVectorFromObject(dbValue), databaseCall, writer);
            } else if (dbValue.getClass() == int[].class) {
                nBoundParameters = printValuelist((int[])dbValue, databaseCall, writer);
            } else if (dbValue instanceof AppendCallCustomParameter) {
                // custom append is required (example BLOB, CLOB on Oracle8)
                ((AppendCallCustomParameter)dbValue).append(writer);
                nBoundParameters = 1;
            } else if (dbValue instanceof BindCallCustomParameter) {
                // custom binding is required, object to be bound is wrapped (example NCHAR, NVARCHAR2, NCLOB on Oracle9)
                databaseCall.bindParameter(writer, dbValue);
                nBoundParameters = 1;
            } else if ((dbValue instanceof Struct) || (dbValue instanceof Array) || (dbValue instanceof Ref)) {
                databaseCall.bindParameter(writer, dbValue);
                nBoundParameters = 1;
            } else {
                // Assume database driver primitive that knows how to print itself, this is required for drivers
                // such as Oracle JDBC, Informix JDBC and others, as well as client specific classes.
                writer.write(dbValue.toString());
            }
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
        
        return nBoundParameters;
    }

    /**
     * Write the string.  Quotes must be double quoted.
     */
    protected void appendString(String string, Writer writer) throws IOException {
        writer.write('\'');
        for (int position = 0; position < string.length(); position++) {
            if (string.charAt(position) == '\'') {
                writer.write("''");
            } else {
                writer.write(string.charAt(position));
            }
        }
        writer.write('\'');
    }

    /**
     * Answer a platform correct string representation of a Time, suitable for SQL generation.
     * The time is printed in the ODBC platform independent format {t'hh:mm:ss'}.
     */
    protected void appendTime(java.sql.Time time, Writer writer) throws IOException {
        writer.write("{t '");
        writer.write(Helper.printTime(time));
        writer.write("'}");
    }

    /**
     * Answer a platform correct string representation of a Timestamp, suitable for SQL generation.
     * The timestamp is printed in the ODBC platform independent timestamp format {ts'YYYY-MM-DD HH:MM:SS.NNNNNNNNN'}.
     */
    protected void appendTimestamp(java.sql.Timestamp timestamp, Writer writer) throws IOException {
        writer.write("{ts '");
        writer.write(Helper.printTimestamp(timestamp));
        writer.write("'}");
    }

    /**
     * Answer a platform correct string representation of a Calendar as a Timestamp, suitable for SQL generation.
     * The calendar is printed in the ODBC platform independent timestamp format {ts'YYYY-MM-DD HH:MM:SS.NNNNNNNNN'}.
     */
    protected void appendCalendar(Calendar calendar, Writer writer) throws IOException {
        writer.write("{ts '");
        writer.write(Helper.printCalendar(calendar));
        writer.write("'}");
    }

    /**
     *  Used by JDBC drivers that do not support autocommit so simulate an autocommit.
     */
    public void autoCommit(DatabaseAccessor accessor) throws SQLException {
        if (!supportsAutoCommit()) {
            accessor.getConnection().commit();
        }
    }

    /**
     *  Used for jdbc drivers which do not support autocommit to explicitly begin a transaction
     *  This method is a no-op for databases which implement autocommit as expected.
     */
    public void beginTransaction(DatabaseAccessor accessor) throws SQLException {
        if (!supportsAutoCommit()) {
            Statement statement = accessor.getConnection().createStatement();
            try {
                statement.executeUpdate("BEGIN TRANSACTION");
            } finally {
                statement.close();
            }
        }
    }

    /**
     * INTERNAL
     * Returns null unless the platform supports call with returning
     */
    public DatabaseCall buildCallWithReturning(SQLCall sqlCall, Vector returnFields) {
        throw ValidationException.platformDoesNotSupportCallWithReturning(Helper.getShortClassName(this));
    }

    /**
     * Return the mapping of class types to database types for the schema framework.
     */
    protected Hashtable buildClassTypes() {
        Hashtable classTypeMapping;

        classTypeMapping = new Hashtable();
        //Key the dictionary the other way for table creation
        classTypeMapping.put("NUMBER", java.math.BigInteger.class);
        classTypeMapping.put("DECIMAL", java.math.BigDecimal.class);
        classTypeMapping.put("INTEGER", Integer.class);
        classTypeMapping.put("INT", Integer.class);
        classTypeMapping.put("NUMERIC", Long.class);
        classTypeMapping.put("FLOAT(16)", Float.class);
        classTypeMapping.put("FLOAT(32)", Double.class);
        classTypeMapping.put("NUMBER(1) default 0", Boolean.class);
        classTypeMapping.put("SHORT", Short.class);
        classTypeMapping.put("BYTE", Byte.class);
        classTypeMapping.put("DOUBLE", Double.class);
        classTypeMapping.put("FLOAT", Float.class);
        classTypeMapping.put("SMALLINT", Short.class);

        classTypeMapping.put("BIT", Boolean.class);
        classTypeMapping.put("SMALLINT DEFAULT 0", Boolean.class);

        classTypeMapping.put("VARCHAR", String.class);
        classTypeMapping.put("CHAR", Character.class);
        classTypeMapping.put("LONGVARBINARY", Byte[].class);
        classTypeMapping.put("TEXT", Character[].class);
        classTypeMapping.put("LONGTEXT", Character[].class);
        //	classTypeMapping.put("BINARY", Byte[].class);
        classTypeMapping.put("MEMO", Character[].class);
        classTypeMapping.put("VARCHAR2", String.class);
        classTypeMapping.put("LONG RAW", Byte[].class);
        classTypeMapping.put("LONG", Character[].class);

        classTypeMapping.put("DATE", java.sql.Date.class);
        classTypeMapping.put("TIMESTAMP", java.sql.Timestamp.class);
        classTypeMapping.put("TIME", java.sql.Time.class);
        classTypeMapping.put("DATETIME", java.sql.Timestamp.class);

        classTypeMapping.put("BIGINT", java.math.BigInteger.class);
        classTypeMapping.put("DOUBLE PRECIS", Double.class);
        classTypeMapping.put("IMAGE", Byte[].class);
        classTypeMapping.put("LONGVARCHAR", Character[].class);
        classTypeMapping.put("REAL", Float.class);
        classTypeMapping.put("TINYINT", Short.class);
        //	classTypeMapping.put("VARBINARY", Byte[].class);
        
        classTypeMapping.put("BLOB", Byte[].class);
        classTypeMapping.put("CLOB", Character[].class);

        return classTypeMapping;
    }

    /**
     * Return the mapping of class types to database types for the schema framework.
     */
    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping;

        fieldTypeMapping = new Hashtable();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("NUMBER", 1));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("NUMBER", 10));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("NUMBER", 19));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("NUMBER", 12, 5).setLimits(19, 0, 19));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("NUMBER", 10, 5).setLimits(19, 0, 19));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("NUMBER", 5));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("NUMBER", 3));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("NUMBER", 19));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("NUMBER", 19, 0).setLimits(19, 0, 19));

        fieldTypeMapping.put(String.class, new FieldTypeDefinition("VARCHAR"));
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR"));

        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("BLOB"));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("CLOB"));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("BLOB"));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("CLOB"));
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("BLOB"));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("CLOB"));
        
        fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATE"));
        fieldTypeMapping.put(java.sql.Timestamp.class, new FieldTypeDefinition("TIMESTAMP"));
        fieldTypeMapping.put(java.sql.Time.class, new FieldTypeDefinition("TIME"));

        return fieldTypeMapping;
    }

    /**
     * INTERNAL
     * Indicates whether the platform can build call with returning.
     * In case this method returns true, buildCallWithReturning method
     * may be called.
     */
    public boolean canBuildCallWithReturning() {
        return false;
    }

    /**
     *  Used for jdbc drivers which do not support autocommit to explicitly commit a transaction
     *  This method is a no-op for databases which implement autocommit as expected.
     */
    public void commitTransaction(DatabaseAccessor accessor) throws SQLException {
        if (!supportsAutoCommit()) {
            accessor.getConnection().commit();
        }
    }

    /**
     * INTERNAL
     * We support more primitive than JDBC does so we must do conversion before printing or binding.
     * 2.0p22: protected->public INTERNAL
     */
    public Object convertToDatabaseType(Object value) {
        if (value == null) {
            return null;
        }
        if (value.getClass() == ClassConstants.UTILDATE) {
            return Helper.timestampFromDate((java.util.Date)value);
        } else if (value instanceof Character) {
            return ((Character)value).toString();
        } else if (value instanceof Calendar) {
            return Helper.timestampFromDate(((Calendar)value).getTime());
        } else if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger)value);
        } else if (value instanceof char[]) {
            return new String((char[])value);
        } else if (value instanceof Character[]) {
            return convertObject(value, ClassConstants.STRING);
        } else if (value instanceof Byte[]) {
            return convertObject(value, ClassConstants.APBYTE);
        }
        return value;
    }

    /**
     * Copy the state into the new platform.
     */
    public void copyInto(Platform platform) {
        super.copyInto(platform);
        if (!(platform instanceof DatabasePlatform)) {
            return;
        }
        DatabasePlatform databasePlatform = (DatabasePlatform)platform;
        databasePlatform.setShouldTrimStrings(shouldTrimStrings());
        databasePlatform.setUsesNativeSQL(usesNativeSQL());
        databasePlatform.setUsesByteArrayBinding(usesByteArrayBinding());
        databasePlatform.setUsesStringBinding(usesStringBinding());
        databasePlatform.setShouldBindAllParameters(shouldBindAllParameters());
        databasePlatform.setShouldCacheAllStatements(shouldCacheAllStatements());
        databasePlatform.setStatementCacheSize(getStatementCacheSize());
        databasePlatform.setTransactionIsolation(getTransactionIsolation());
        databasePlatform.setShouldForceFieldNamesToUpperCase(shouldForceFieldNamesToUpperCase());
        databasePlatform.setShouldOptimizeDataConversion(shouldOptimizeDataConversion());
        databasePlatform.setStringBindingSize(getStringBindingSize());
        databasePlatform.setUsesStreamsForBinding(usesStreamsForBinding());
    }

    /**
     * Used for batch writing and sp defs.
     */
    public String getBatchBeginString() {
        return "";
    }

    /**
     * Used for batch writing and sp defs.
     */
    public String getBatchDelimiterString() {
        return "; ";
    }

    /**
     * Used for batch writing and sp defs.
     */
    public String getBatchEndString() {
        return "";
    }
    
    /**
     * Used for constraint deletion.
     */
    public String getConstraintDeletionString() {
        return " DROP CONSTRAINT ";
    }
    
    /**
     * Used for view creation.
     */
    public String getCreateViewString() {
        return "CREATE VIEW ";
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
     * Used for stored procedure defs.
     */
    public String getProcedureAsString() {
        return " AS";
    }

    /**
     * Return the class type to database type mapping for the schema framework.
     */
    public Hashtable getClassTypes() {
        if (classTypes == null) {
            classTypes = buildClassTypes();
        }
        return classTypes;
    }

    /**
     * Used for stored function calls.
     */
    public String getAssignmentString() {
        return "= ";
    }

    /**
     * This method is used to print the required output parameter token for the
     * specific platform.  Used when stored procedures are created.
     */
    public String getCreationInOutputProcedureToken() {
        return getInOutputProcedureToken();
    }

    /**
     * This method is used to print the required output parameter token for the
     * specific platform.  Used when stored procedures are created.
     */
    public String getCreationOutputProcedureToken() {
        return getOutputProcedureToken();
    }

    /**
     * ADVANCED:
     * Return the code for preparing cursored output
     * parameters in a stored procedure
     */
    public int getCursorCode() {
        return cursorCode;
    }

    /**
     * Return the field type object describing this databases platform specific representation
     * of the Java primitive class name.
     */
    public FieldTypeDefinition getFieldTypeDefinition(Class javaClass) {
        return (FieldTypeDefinition)getFieldTypes().get(javaClass);
    }

    /**
     * Return the class type to database type mappings for the schema framework.
     */
    public Hashtable getFieldTypes() {
        if (fieldTypes == null) {
            fieldTypes = buildFieldTypes();
        }
        return fieldTypes;
    }

    /**
     * Used for stored function calls.
     */
    public String getFunctionCallHeader() {
        return getProcedureCallHeader() + "? " + getAssignmentString();
    }

    /**
     * This method is used to print the output parameter token when stored
     * procedures are called
     */
    public String getInOutputProcedureToken() {
        return "IN OUT";
    }

    /**
     * Returns the JDBC outer join operator for SELECT statements.
     */
    public String getJDBCOuterJoinString() {
        return "{oj ";
    }

    /**
     * Return the JDBC type for the given database field.
     */
    public int getJDBCType(DatabaseField field) {
        if (field != null) {
            // If the field has a specified JDBC type, use it,
            // otherwise compute the type from the Java class type.
            if (field.getSqlType() != -1) {
                return field.getSqlType();
            } else {
                return getJDBCType(ConversionManager.getObjectClass(field.getType()));
            }
        } else {
            return getJDBCType((Class)null);
        }
    }

    /**
     * Return the JDBC type for the Java type.
     */
    public int getJDBCType(Class javaType) {
        if (javaType == null) {
            return Types.VARCHAR;// Best guess, sometimes we cannot determine type from mapping, this may fail on some drivers, other dont care what type it is.
        } else if (javaType == ClassConstants.STRING) {
            return Types.VARCHAR;
        } else if (javaType == ClassConstants.BIGDECIMAL) {
            return Types.DECIMAL;
        } else if (javaType == ClassConstants.BIGINTEGER) {
            return Types.BIGINT;
        } else if (javaType == ClassConstants.BOOLEAN) {
            return Types.BIT;
        } else if (javaType == ClassConstants.BYTE) {
            return Types.TINYINT;
        } else if (javaType == ClassConstants.CHAR) {
            return Types.CHAR;
        } else if (javaType == ClassConstants.DOUBLE) {
            return Types.DOUBLE;
        } else if (javaType == ClassConstants.FLOAT) {
            return Types.FLOAT;
        } else if (javaType == ClassConstants.INTEGER) {
            return Types.INTEGER;
        } else if (javaType == ClassConstants.LONG) {
            return Types.INTEGER;
        } else if (javaType == ClassConstants.NUMBER) {
            return Types.DECIMAL;
        } else if (javaType == ClassConstants.SHORT ) {
            return Types.SMALLINT;
        } else if (javaType == ClassConstants.CALENDAR ) {
            return Types.TIMESTAMP;
        } else if (javaType == ClassConstants.UTILDATE ) {
            return Types.TIMESTAMP;
        } else if (javaType == ClassConstants.TIME) {
            return Types.TIME;
        } else if (javaType == ClassConstants.SQLDATE) {
            return Types.DATE;
        } else if (javaType == ClassConstants.TIMESTAMP) {
            return Types.TIMESTAMP;
        } else if (javaType == ClassConstants.ABYTE) {
            return Types.LONGVARBINARY;
        } else if (javaType == ClassConstants.APBYTE) {
            return Types.LONGVARBINARY;
        } else if (javaType == ClassConstants.BLOB) {
            return Types.BLOB;
        } else if (javaType == ClassConstants.ACHAR) {
            return Types.LONGVARCHAR;
        } else if (javaType == ClassConstants.APCHAR) {
            return Types.LONGVARCHAR;
        } else if (javaType == ClassConstants.CLOB) {
            return Types.CLOB;
        } else {
            return Types.VARCHAR;// Best guess, sometimes we cannot determine type from mapping, this may fail on some drivers, other dont care what type it is.
        }
    }

    /**
     * INTERNAL:
     * Returns the type name corresponding to the jdbc type
     */
    public String getJdbcTypeName(int jdbcType) {
        return null;
    }

    /**
     * INTERNAL:
     * returns the maximum number of characters that can be used in a field
     * name on this platform.
     */
    public int getMaxFieldNameSize() {
        return 50;
    }

    /**
     * INTERNAL:
     * returns the maximum number of characters that can be used in a foreign key
     * name on this platform.
     */
    public int getMaxForeignKeyNameSize() {
        return getMaxFieldNameSize();
    }

    /**
     * INTERNAL:
     * returns the maximum number of characters that can be used in a unique key
     * name on this platform.
     */
    public int getMaxUniqueKeyNameSize() {
        return getMaxFieldNameSize();
    }    

    /**
     * INTERNAL:
     * Get the object from the JDBC Result set.  Added to allow other platforms to
     * override.
     * @see oracle.toplink.essentials.oraclespecific.Oracle9Platform
     */
    public Object getObjectFromResultSet(ResultSet resultSet, int columnNumber, int type) throws java.sql.SQLException {
        return resultSet.getObject(columnNumber);
    }

    /**
     * This method is used to print the output parameter token when stored
     * procedures are called
     */
    public String getOutputProcedureToken() {
        return "OUT";
    }

    /**
     * Used for sp calls.
     */
    public String getProcedureArgumentSetter() {
        return " = ";
    }

    /**
     * Used for sp defs.
     */
    public String getProcedureArgumentString() {
        return "";
    }

    /**
     * Used for sp calls.
     */
    public String getProcedureCallHeader() {
        return "EXECUTE PROCEDURE ";
    }

    /**
     * Used for sp calls.
     */
    public String getProcedureCallTail() {
        return "";
    }

    public String getQualifiedSequenceTableName() {
        if (getDefaultSequence() instanceof TableSequence) {
            String sequenceTableName = ((TableSequence)getDefaultSequence()).getTableName();
            if (getTableQualifier().equals("")) {
                return sequenceTableName;
            } else {
                return getTableQualifier() + "." + sequenceTableName;
            }
        } else {
            throw ValidationException.wrongSequenceType(Helper.getShortClassName(getDefaultSequence()), "getTableName");
        }
    }

    /**
     * This syntax does no wait on the lock.
     * (i.e. In Oracle adding NOWAIT to the end will accomplish this)
     */
    public String getSelectForUpdateNoWaitString() {
        return " NOWAIT";
    }

    /**
     * For fine-grained pessimistic locking the column names can be
     * specified individually.
     */
    public String getSelectForUpdateOfString() {
        return " FOR UPDATE OF ";
    }

    /**
     * Most database support a syntax. although don't actually lock the row.
     * Some require the OF some don't like it.
     */
    public String getSelectForUpdateString() {
        return " FOR UPDATE";
    }

    public String getSequenceCounterFieldName() {
        if (getDefaultSequence() instanceof TableSequence) {
            return ((TableSequence)getDefaultSequence()).getCounterFieldName();
        } else {
            throw ValidationException.wrongSequenceType(Helper.getShortClassName(getDefaultSequence()), "getCounterFieldName");
        }
    }

    public String getSequenceNameFieldName() {
        if (getDefaultSequence() instanceof TableSequence) {
            return ((TableSequence)getDefaultSequence()).getNameFieldName();
        } else {
            throw ValidationException.wrongSequenceType(Helper.getShortClassName(getDefaultSequence()), "getNameFieldName");
        }
    }

    public int getSequencePreallocationSize() {
        return getDefaultSequence().getPreallocationSize();
    }

    public String getSequenceTableName() {
        if (getDefaultSequence() instanceof TableSequence) {
            return ((TableSequence)getDefaultSequence()).getTableName();
        } else {
            throw ValidationException.wrongSequenceType(Helper.getShortClassName(getDefaultSequence()), "getTableName");
        }
    }

    /**
     * The statement cache size for prepare parameterized statements.
     */
    public int getStatementCacheSize() {
        return statementCacheSize;
    }

    public String getStoredProcedureParameterPrefix() {
        return "";
    }

    public String getStoredProcedureTerminationToken() {
        return ";";
    }

    public int getStringBindingSize() {
        return stringBindingSize;
    }

    /**
     * Returns the transaction isolation setting for a connection.
     * Return -1 if it has not been set.
     */
    public int getTransactionIsolation() {
        return transactionIsolation;
    }

    /**
     * Some database require outer joins to be given in the where clause, others require it in the from clause.
     * Informix requires it in the from clause with no ON expression.
     */
    public boolean isInformixOuterJoin() {
        return false;
    }

    /**
     *    Builds a table of maximum numeric values keyed on java class. This is used for type testing but
     * might also be useful to end users attempting to sanitize values.
     * <p><b>NOTE</b>: BigInteger & BigDecimal maximums are dependent upon their precision & Scale
     */
    public Hashtable maximumNumericValues() {
        Hashtable values = new Hashtable();

        values.put(Integer.class, new Integer(Integer.MAX_VALUE));
        values.put(Long.class, new Long(Long.MAX_VALUE));
        values.put(Double.class, new Double(Double.MAX_VALUE));
        values.put(Short.class, new Short(Short.MAX_VALUE));
        values.put(Byte.class, new Byte(Byte.MAX_VALUE));
        values.put(Float.class, new Float(Float.MAX_VALUE));
        values.put(java.math.BigInteger.class, new java.math.BigInteger("999999999999999999999999999999999999999"));
        values.put(java.math.BigDecimal.class, new java.math.BigDecimal("99999999999999999999.9999999999999999999"));
        return values;
    }

    /**
     *    Builds a table of minimum numeric values keyed on java class. This is used for type testing but
     * might also be useful to end users attempting to sanitize values.
     * <p><b>NOTE</b>: BigInteger & BigDecimal minimums are dependent upon their precision & Scale
     */
    public Hashtable minimumNumericValues() {
        Hashtable values = new Hashtable();

        values.put(Integer.class, new Integer(Integer.MIN_VALUE));
        values.put(Long.class, new Long(Long.MIN_VALUE));
        values.put(Double.class, new Double(Double.MIN_VALUE));
        values.put(Short.class, new Short(Short.MIN_VALUE));
        values.put(Byte.class, new Byte(Byte.MIN_VALUE));
        values.put(Float.class, new Float(Float.MIN_VALUE));
        values.put(java.math.BigInteger.class, new java.math.BigInteger("-99999999999999999999999999999999999999"));
        values.put(java.math.BigDecimal.class, new java.math.BigDecimal("-9999999999999999999.9999999999999999999"));
        return values;
    }

    /**
     * Append the receiver's field 'identity' constraint clause to a writer.
     */
    public void printFieldIdentityClause(Writer writer) throws ValidationException {
        //The default is to do nothing.
    }

    /**
     * Append the receiver's field 'NOT NULL' constraint clause to a writer.
     */
    public void printFieldNotNullClause(Writer writer) throws ValidationException {
        try {
            writer.write(" NOT NULL");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /**
     * Append the receiver's field 'NULL' constraint clause to a writer.
     */
    public void printFieldNullClause(Writer writer) throws ValidationException {
        // The default is to do nothing
    }

    /**
     * Added November 7, 2000 JED
     * Prs reference: 24501
     * Tracker reference: 14111
     * Print the int array on the writer. Added to handle int[] passed as parameters to named queries
     * Returns the number of  objects using binding.
     */
    public int printValuelist(int[] theObjects, DatabaseCall call, Writer writer) throws IOException {
        int nBoundParameters = 0;
        writer.write("(");

        for (int i = 0; i < theObjects.length; i++) {
            nBoundParameters = nBoundParameters + appendParameterInternal(call, writer, new Integer(theObjects[i]));
            if (i < (theObjects.length - 1)) {
                writer.write(", ");
            }
        }

        writer.write(")");
        return nBoundParameters;
    }

    /**
     * Returns the number of  objects using binding.
     */
    public int printValuelist(Vector theObjects, DatabaseCall call, Writer writer) throws IOException {
        int nBoundParameters = 0;
        Enumeration enumtr = theObjects.elements();
        while (enumtr.hasMoreElements()) {
            nBoundParameters = nBoundParameters + appendParameterInternal(call, writer, enumtr.nextElement());
            if (enumtr.hasMoreElements()) {
                writer.write(", ");
            }
        }
        return nBoundParameters;
    }

    protected Object processResultSet(ResultSet resultSet, DatabaseCall dbCall, PreparedStatement statement, DatabaseAccessor accessor, AbstractSession session) throws SQLException {
        Object result = null;
        ResultSetMetaData metaData = resultSet.getMetaData();

        session.startOperationProfile(SessionProfiler.ROW_FETCH);
        try {
            if (dbCall.isOneRowReturned()) {
                if (resultSet.next()) {
                    result = accessor.fetchRow(dbCall.getFields(), resultSet, metaData, session);
                    if (resultSet.next()) {
                        // Raise more rows event, some apps may interpret as error or warning.
                        session.getEventManager().moreRowsDetected(dbCall);
                    }
                } else {
                    result = null;
                }
            } else {
                Vector results = new Vector(20);
                while (resultSet.next()) {
                    results.addElement(accessor.fetchRow(dbCall.getFields(), resultSet, metaData, session));
                }
                result = results;
            }
            resultSet.close();// This must be closed incase the statement is cached and not closed.
        } finally {
            session.endOperationProfile(SessionProfiler.ROW_FETCH);
        }
        return result;
    }

    /**
     * This method is used to register output parameter on Callable Statements for Stored Procedures
     * as each database seems to have a different method.
     */
    public void registerOutputParameter(CallableStatement statement, int index, int jdbcType) throws SQLException {
        statement.registerOutParameter(index, jdbcType);
    }

    /**
     * This is used as some databases create the primary key constraint differently, i.e. Access.
     */
    public boolean requiresNamedPrimaryKeyConstraints() {
        return false;
    }

    /**
     * USed for sp calls.
     */
    public boolean requiresProcedureCallBrackets() {
        return true;
    }

    /**
     * Used for sp calls.  Sybase must print output after output params.
     */
    public boolean requiresProcedureCallOuputToken() {
        return false;
    }

    /**
     * INTERNAL:
     * Indicates whether the version of CallableStatement.registerOutputParameter method
     * that takes type name should be used.
     */
    public boolean requiresTypeNameToRegisterOutputParameter() {
        return false;
    }

    /**
     *  Used for jdbc drivers which do not support autocommit to explicitly rollback a transaction
     *  This method is a no-op for databases which implement autocommit as expected.
     */
    public void rollbackTransaction(DatabaseAccessor accessor) throws SQLException {
        if (!supportsAutoCommit()) {
            accessor.getConnection().rollback();
        }
    }

    protected void setClassTypes(Hashtable classTypes) {
        this.classTypes = classTypes;
    }

    /**
     * ADVANCED:
     * Set the code for preparing cursored output
     * parameters in a stored procedure
     */
    public void setCursorCode(int cursorCode) {
        this.cursorCode = cursorCode;
    }

    protected void setFieldTypes(Hashtable theFieldTypes) {
        fieldTypes = theFieldTypes;
    }

    public void setSequenceCounterFieldName(String name) {
        if (getDefaultSequence() instanceof TableSequence) {
            ((TableSequence)getDefaultSequence()).setCounterFieldName(name);
        } else {
            if (!name.equals((new TableSequence()).getCounterFieldName())) {
                ValidationException.wrongSequenceType(Helper.getShortClassName(getDefaultSequence()), "setCounterFieldName");
            }
        }
    }

    public void setSequenceNameFieldName(String name) {
        if (getDefaultSequence() instanceof TableSequence) {
            ((TableSequence)getDefaultSequence()).setNameFieldName(name);
        } else {
            if (!name.equals((new TableSequence()).getNameFieldName())) {
                throw ValidationException.wrongSequenceType(Helper.getShortClassName(getDefaultSequence()), "setNameFieldName");
            }
        }
    }

    public void setSequenceTableName(String name) {
        if (getDefaultSequence() instanceof TableSequence) {
            ((TableSequence)getDefaultSequence()).setTableName(name);
        } else {
            if (!name.equals((new TableSequence()).getTableName())) {
                throw ValidationException.wrongSequenceType(Helper.getShortClassName(getDefaultSequence()), "setTableName");
            }
        }
    }

    /**
     * Bind all arguments to any SQL statement.
     */
    public void setShouldBindAllParameters(boolean shouldBindAllParameters) {
        this.shouldBindAllParameters = shouldBindAllParameters;
    }

    /**
     * Cache all prepared statements, this requires full parameter binding as well.
     */
    public void setShouldCacheAllStatements(boolean shouldCacheAllStatements) {
        this.shouldCacheAllStatements = shouldCacheAllStatements;
    }

    /**
     * Can be used if the app expects upper case but the database is not return consistent case, i.e. different databases.
     */
    public void setShouldForceFieldNamesToUpperCase(boolean shouldForceFieldNamesToUpperCase) {
        this.shouldForceFieldNamesToUpperCase = shouldForceFieldNamesToUpperCase;
    }

    /**
     * Allow for case in field names to be ignored as some databases are not case sensitive and when using custom this can be an issue.
     */
    public static void setShouldIgnoreCaseOnFieldComparisons(boolean newShouldIgnoreCaseOnFieldComparisons) {
        shouldIgnoreCaseOnFieldComparisons = newShouldIgnoreCaseOnFieldComparisons;
    }

    /**
     * PUBLIC:
     * Set if our driver level data conversion optimization is enabled.
     * This can be disabled as some drivers perform data conversion themselves incorrectly.
     */
    public void setShouldOptimizeDataConversion(boolean value) {
        this.shouldOptimizeDataConversion = value;
    }

    public void setShouldTrimStrings(boolean aBoolean) {
        shouldTrimStrings = aBoolean;
    }

    /**
     * The statement cache size for prepare parameterized statements.
     */
    public void setStatementCacheSize(int statementCacheSize) {
        this.statementCacheSize = statementCacheSize;
    }

    public void setStringBindingSize(int aSize) {
        stringBindingSize = aSize;
    }

    /**
     * supportsAutoCommit can be set to false for JDBC drivers which do not support autocommit
     * @return boolean
     */
    public void setSupportsAutoCommit(boolean supportsAutoCommit) {
        this.supportsAutoCommit = supportsAutoCommit;
    }

    /**
     * Set the transaction isolation setting for a connection.
     */
    public void setTransactionIsolation(int isolationLevel) {
        transactionIsolation = isolationLevel;
    }

    public void setUsesByteArrayBinding(boolean usesByteArrayBinding) {
        this.usesByteArrayBinding = usesByteArrayBinding;
    }

    public void setUsesNativeSQL(boolean usesNativeSQL) {
        this.usesNativeSQL = usesNativeSQL;
    }

    public void setUsesStreamsForBinding(boolean usesStreamsForBinding) {
        this.usesStreamsForBinding = usesStreamsForBinding;
    }

    public void setUsesStringBinding(boolean aBool) {
        usesStringBinding = aBool;
    }

    /**
     * Bind all arguments to any SQL statement.
     */
    public boolean shouldBindAllParameters() {
        return shouldBindAllParameters;
    }

    /**
     * Cache all prepared statements, this requires full parameter binding as well.
     */
    public boolean shouldCacheAllStatements() {
        return shouldCacheAllStatements;
    }

    /**
     * Can be used if the app expects upper case but the database is not return consistent case, i.e. different databases.
     */
    public boolean shouldForceFieldNamesToUpperCase() {
        return shouldForceFieldNamesToUpperCase;
    }

    /**
     * Allow for case in field names to be ignored as some databases are not case sensitive and when using custom this can be an issue.
     */
    public static boolean shouldIgnoreCaseOnFieldComparisons() {
        return shouldIgnoreCaseOnFieldComparisons;
    }

    /**
     * Allow for the platform to ignore exceptions.
     * This is required for DB2 which throws no-data modified as an exception.
     */
    public boolean shouldIgnoreException(SQLException exception) {
        // By default nothing is ignored.
        return false;
    }

    /**
     * Return if our driver level data conversion optimization is enabled.
     * This can be disabled as some drivers perform data conversion themselves incorrectly.
     */
    public boolean shouldOptimizeDataConversion() {
        return shouldOptimizeDataConversion;
    }

    /**
    * Some Platforms want the constraint name after the constraint definition.
    */
    public boolean shouldPrintConstraintNameAfter() {
        return false;
    }

    /**
     * This is required in the construction of the stored procedures with
     * output parameters
     */
    public boolean shouldPrintInOutputTokenBeforeType() {
        return true;
    }

    /**
     * Some database require outer joins to be given in the where clause, others require it in the from clause.
     */
    public boolean shouldPrintOuterJoinInWhereClause() {
        return false;
    }

    /**
     * This is required in the construction of the stored procedures with
     * output parameters
     */
    public boolean shouldPrintOutputTokenBeforeType() {
        return true;
    }
    
    /**
     * This is required in the construction of the stored procedures with
     * output parameters
     */
    public boolean shouldPrintOutputTokenAtStart() {
        return false;
    }
    
    public boolean shouldTrimStrings() {
        return shouldTrimStrings;
    }

    /**
     * JDBC defines and outer join syntax, many drivers do not support this. So we normally avoid it.
     */
    public boolean shouldUseJDBCOuterJoinSyntax() {
        return true;
    }

    /**
     * supportsAutoCommit must sometimes be set to false for JDBC drivers which do not
     * support autocommit.  Used to determine how to handle transactions properly.
     */
    public boolean supportsAutoCommit() {
        return supportsAutoCommit;
    }

    public boolean supportsForeignKeyConstraints() {
        return true;
    }

    public boolean supportsUniqueKeyConstraints() {
        return true;
    }

    public boolean supportsNativeSequenceNumbers() {
        return this.supportsSequenceObjects() || this.supportsIdentity();
    }

    public boolean supportsPrimaryKeyConstraint() {
        return true;
    }

    public boolean supportsStoredFunctions() {
        return false;
    }

    /**
     * because each platform has different requirements for accessing stored procedures and
     * the way that we can combine resultsets and output params the stored procedure call
     * is being executed on the platform
     */
    public Object executeStoredProcedure(DatabaseCall dbCall, PreparedStatement statement, DatabaseAccessor accessor, AbstractSession session) throws SQLException {
        Object result = null;
        ResultSet resultSet = null;
        if (!dbCall.getReturnsResultSet()) {// no result set is expected
            if (dbCall.isCursorOutputProcedure()) {
                result = accessor.executeNoSelect(dbCall, statement, session);
                resultSet = (ResultSet)((CallableStatement)statement).getObject(dbCall.getCursorOutIndex());
            } else {
                accessor.executeDirectNoSelect(statement, dbCall, session);
                result = accessor.buildOutputRow((CallableStatement)statement, dbCall, session);

                //ReadAllQuery may be returning just output params, or they may be executing a DataReadQuery, which also
                //assumes a vector
                if (dbCall.areManyRowsReturned()) {
                    Vector tempResult = new Vector();
                    ((Vector)tempResult).add(result);
                    result = tempResult;
                }
            }
        } else {
            // so specifically in Sybase JConnect 5.5 we must create the result vector before accessing the
            // output params in the case where the user is returning both.  this is a driver limitation
            resultSet = accessor.executeSelect(dbCall, statement, session);
        }
        if (resultSet != null) {
            dbCall.matchFieldOrder(resultSet, accessor, session);

            if (dbCall.isCursorReturned()) {
                dbCall.setStatement(statement);
                dbCall.setResult(resultSet);
                return dbCall;
            }
            result = processResultSet(resultSet, dbCall, statement, accessor, session);

        }
        return result;
    }

    /**
     *  INTERNAL
     *  Note that index (not index+1) is used in statement.setObject(index, parameter)
     *    Binding starts with a 1 not 0, so make sure that index > 0.
     */
    public void setParameterValueInDatabaseCall(Object parameter, PreparedStatement statement, int index, AbstractSession session) throws SQLException {
        // 2.0p22: Added the following conversion before binding into prepared statement
        parameter = convertToDatabaseType(parameter);
        if (! setComplexParameterValue(session, statement, index, parameter)) {
            setPrimitiveParameterValue(statement, index, parameter);
        }
    }
    
    /**
     * Set a primitive parameter.
     * Database platforms that need customised behavior would override this method
     */
    protected void setPrimitiveParameterValue(final PreparedStatement statement, final int index, 
            final Object parameter) throws SQLException {
        statement.setObject(index, parameter);
    }

    /**
     * Set a complex parameter.
     * @return true if parameter was successfully set by this method, false otherwise.
     */
    private boolean setComplexParameterValue(final AbstractSession session, final PreparedStatement statement, final int index, final Object parameter) throws SQLException {
        if (parameter == null) {
            // no DatabaseField available
            statement.setNull(index, getJDBCType((Class)null));
        } else if (parameter instanceof DatabaseField) {
            // Substituted null value for the corresponding DatabaseField.
            // Cannot bind null through set object, so we must compute to type.
            int jdbcType = getJDBCType((DatabaseField)parameter);
            statement.setNull(index, jdbcType);
        } else if ((parameter instanceof byte[]) && (usesStreamsForBinding())) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[])parameter);
            statement.setBinaryStream(index, inputStream, ((byte[])parameter).length);
        } else if ((parameter instanceof String) && usesStringBinding() && (((String)parameter).length() > getStringBindingSize())) {
            CharArrayReader reader = new CharArrayReader(((String)parameter).toCharArray());
            statement.setCharacterStream(index, reader, ((String)parameter).length());
        } else if (parameter instanceof BindCallCustomParameter) {
            ((BindCallCustomParameter)(parameter)).set(this, statement, index, session);
        } else {
            return false;
        }
        return true;
    }

    /**
     *  INTERNAL
     *    Used by SQLCall.prepareStatement(..)
     *  Note that parameterIndex corresponds to parameters vector and
     *  index corresponds to statement:
     *    statement.setObject(parameterIndex + 1, parameters.elementAt(parameterIndex))
     *    Therefore parameterIndex may be 0.
     */
    public void setParameterValueInDatabaseCall(Vector parameters, PreparedStatement statement, int parameterIndex, AbstractSession session) throws SQLException {
        setParameterValueInDatabaseCall(parameters, statement, parameterIndex, parameterIndex + 1, session);
    }

    /**
     *  INTERNAL
     *    Used by StoredProcedureCall.prepareStatement(..)
     *  Note that parameterIndex corresponds to parameters vector and
     *  index corresponds to statement:
     *    statement.setObject(index, parameters.elementAt(parameterIndex))
     *    Therefore parameterIndex may be 0, but index > 0.
     */
    public void setParameterValueInDatabaseCall(Vector parameters, PreparedStatement statement, int parameterIndex, int index, AbstractSession session) throws SQLException {
        setParameterValueInDatabaseCall(parameters.elementAt(parameterIndex), statement, index, session);
    }

    public boolean usesByteArrayBinding() {
        return usesByteArrayBinding;
    }

    public boolean usesSequenceTable() {
        return getDefaultSequence() instanceof TableSequence;
    }

    public boolean usesNativeSQL() {
        return usesNativeSQL;
    }

    public boolean usesStreamsForBinding() {
        return usesStreamsForBinding;
    }

    public boolean usesStringBinding() {
        return usesStringBinding;
    }

    /**
     * INTERNAL:
     * Write LOB value - only on Oracle8 and up
     */
    public void writeLOB(DatabaseField field, Object value, ResultSet resultSet, AbstractSession session) throws SQLException {
        // used by Oracle8Platform
    }

    /**
     *  INTERNAL
     *    Indicates whether a separate transaction is required for NativeSequence.
     *  This method is to be used *ONLY* by sequencing classes
     */
    public boolean shouldNativeSequenceUseTransaction() {
        return false;
    }

    /**
     *  INTERNAL:
     *  Indicates whether the platform supports identity.
     *  This method is to be used *ONLY* by sequencing classes
     */
    public boolean supportsIdentity() {
        return false;
    }

    /**
     * INTERNAL:
     * Returns query used to read back the value generated by Identity.
     * This method is called when identity NativeSequence is connected,
     * the returned query used until the sequence is disconnected.
     * If the platform supportsIdentity then (at least) one of buildSelectQueryForIdentity
     * methods should return non-null query.
     */
    public ValueReadQuery buildSelectQueryForIdentity() {
        return null;
    }

    /**
     * INTERNAL:
     * Returns query used to read back the value generated by Identity.
     * In case the other version of this method (taking no parameters) returns null,
     * this method is called every time identity NativeSequence reads.
     * If the platform supportsIdentity then (at least) one of buildSelectQueryForIdentity
     * methods should return non-null query.
     */
    public ValueReadQuery buildSelectQueryForIdentity(String seqName, Integer size) {
        return null;
    }

    /**
     *  INTERNAL:
     *  Indicates whether the platform supports sequence objects.
     *  This method is to be used *ONLY* by sequencing classes
     */
    public boolean supportsSequenceObjects() {
        return false;
    }

    /**
     * INTERNAL:
     * Returns query used to read value generated by sequence object (like Oracle sequence).
     * This method is called when sequence object NativeSequence is connected,
     * the returned query used until the sequence is disconnected.
     * If the platform supportsSequenceObjects then (at least) one of buildSelectQueryForSequenceObject
     * methods should return non-null query.
     */
    public ValueReadQuery buildSelectQueryForSequenceObject() {
        return null;
    }

    /**
     * INTERNAL:
     * Returns query used to read value generated by sequence object (like Oracle sequence).
     * In case the other version of this method (taking no parameters) returns null,
     * this method is called every time sequence object NativeSequence reads.
     * If the platform supportsSequenceObjects then (at least) one of buildSelectQueryForSequenceObject
     * methods should return non-null query.
     */
    public ValueReadQuery buildSelectQueryForSequenceObject(String seqName, Integer size) {
        return null;
    }

    /**
     * INTERNAL:
     * Create platform-default Sequence
     */
    protected Sequence createPlatformDefaultSequence() {
        return new TableSequence();
    }

    /**
     * INTERNAL:
     * Indicates whether the platform supports temporary tables.
     * Temporary tables may be used by UpdateAllQueries:
     * though attempt is always made to perform UpdateAll without using temporary
     * storage there are some scenarios that can't be fulfilled without it.
     * Don't override this method.
     * If the platform support temorary tables then override
     * either supportsLocalTempTables() or supportsGlobalTempTables()
     * method.
     */
     public boolean supportsTempTables() {
         return supportsLocalTempTables() || supportsGlobalTempTables();
     }

    /**
     * INTERNAL:
     * Indicates whether the platform supports local temporary tables.
     * "Local" means that several threads may create
     * temporary tables with the same name.
     * Local temporary table is created in the beginning of UpdateAllQuery 
     * execution and dropped in the end of it.
     * Override this method if the platform supports local temporary tables.
     */
     public boolean supportsLocalTempTables() {
         return false;
     }

    /**
     * INTERNAL:
     * Indicates whether the platform supports global temporary tables.
     * "Global" means that an attempt to create temporary table with the same
     * name for the second time results in exception.
     * TopLink attempts to create global temporary table in the beginning of UpdateAllQuery,
     * execution and assumes that it already exists in case SQLException results.
     * In the end of UpdateAllQuery execution all rows are removed from the temporary table -
     * it is necessary in case the same temporary table will be used by another UpdateAllQuery
     * in the same transaction.
     * Override this method if the platform supports global temporary tables.
     * Note that this method is ignored in case supportsLocalTempTables() returns true.
     */
     public boolean supportsGlobalTempTables() {
         return false;
     }
     
    /**
     * INTERNAL:
     * Override this method if the platform supports temporary tables.
     * This should contain the beginning of sql string for 
     * creating temporary table - the sql statement name, for instance:
     * "CREATE GLOBAL TEMPORARY TABLE ".
     * Don't forget to end it with a space.
     */
     protected String getCreateTempTableSqlPrefix() {
         throw ValidationException.platformDoesNotOverrideGetCreateTempTableSqlPrefix(Helper.getShortClassName(this));
     }          

    /**
     * INTERNAL:
     * May override this method if the platform support temporary tables.
     * @parameter DatabaseTable table is original table for which temp table is created.
     * @return DatabaseTable temorary table
     */
     public DatabaseTable getTempTableForTable(DatabaseTable table) {
         return new DatabaseTable("TL_" + table.getName(), table.getTableQualifier());
     }          

    /**
     * INTERNAL:
     * May override this method if the platform support temporary tables.
     * This should contain the ending of sql string for 
     * creating temporary table, for instance:
     * " ON COMMIT DELETE ROWS"
     * Don't forget to begin it with a space.
     */
     protected String getCreateTempTableSqlSuffix() {
         return "";
     }          

    /**
     * INTERNAL:
     * May override this method if the platform supports temporary tables.
     * With this method not overridden the sql string for temporary table creation
     * will include a list of database fields extracted from descriptor:
     * getCreateTempTableSqlPrefix() + getTempTableForTable(table).getQualifiedName() +
     * (list of database fields) + getCreateTempTableSqlSuffix().
     * If this method is overridden its output will be used instead of fields' list: 
     * getCreateTempTableSqlPrefix() + getTempTableForTable(table).getQualifiedName() +
     * getCreateTempTableSqlBodyForTable(table) + getCreateTempTableSqlSuffix().
     * Don't forget to begin it with a space.
     * Example: " LIKE " + table.getQualifiedName();
     * @parameter DatabaseTable table is original table for which temp table is created.
     * @result String
     */
     protected String getCreateTempTableSqlBodyForTable(DatabaseTable table) {
         return null;
     }          

    /**
     * INTERNAL:
     * Indicates whether temporary table can specify primary keys (some platforms don't allow that).
     * Used by writeCreateTempTableSql method.
     */
    protected boolean shouldTempTableSpecifyPrimaryKeys() {
        return true;
    }
    
    /**
     * INTERNAL:
     * Don't override this method.
     * Write an sql string for creation of the temporary table.
     * Note that in case of local temp table support it's possible to limit
     * the fields in the temp table to those needed for the operation it supports (usedFields) -
     * the temp table will be dropped in the end of query execution.
     * Alternatively, in global temp table case the table with a given name is created just once
     * and will be potentially used by various operations with various sets of used fields,
     * therefore global temp table should contain all mapped fields (allFields).
     * Precondition: supportsTempTables() == true.
     * Precondition: pkFields contained in usedFields contained in allFields
     * @parameter Writer writer for writing the sql
     * @parameter DatabaseTable table is original table for which temp table is created.
     * @parameter AbstractSession session.
     * @parameter Collection pkFields - primary key fields for the original table.
     * @parameter Collection usedFields - fields that will be used by operation for which temp table is created.
     * @parameter Collection allFields - all mapped fields for the original table.
     */
     public void writeCreateTempTableSql(Writer writer, DatabaseTable table, AbstractSession session, 
                                        Collection pkFields,
                                        Collection usedFields,
                                        Collection allFields) throws IOException 
    {
        String body = getCreateTempTableSqlBodyForTable(table);
        if(body == null) {
            TableDefinition tableDef = new TableDefinition();
            Collection fields;
            if(supportsLocalTempTables()) {
                fields = usedFields;
            } else {
                // supportsGlobalTempTables() == true
                fields = allFields;
            }
            Iterator itFields = fields.iterator();
            while(itFields.hasNext()) {
                DatabaseField field = (DatabaseField)itFields.next();
                FieldDefinition fieldDef;
                //bug3307, should use columnDefinition if it was defined.
                if(field.getColumnDefinition().length() == 0){
                   fieldDef = new FieldDefinition(field.getName(), ConversionManager.getObjectClass(field.getType()));
                }else{
                   fieldDef = new FieldDefinition(field.getName(), field.getColumnDefinition());
                }
                if(pkFields.contains(field) && shouldTempTableSpecifyPrimaryKeys()) {
                    fieldDef.setIsPrimaryKey(true);
                }
                tableDef.addField(fieldDef);
            }            
            tableDef.setCreationPrefix(getCreateTempTableSqlPrefix());
            tableDef.setName(getTempTableForTable(table).getQualifiedName());
            tableDef.setCreationSuffix(getCreateTempTableSqlSuffix());
            tableDef.buildCreationWriter(session, writer);
        } else {
            writer.write(getCreateTempTableSqlPrefix());
            writer.write(getTempTableForTable(table).getQualifiedName());
            writer.write(body);
            writer.write(getCreateTempTableSqlSuffix());
        }
    }          

    /**
     * INTERNAL:
     * May need to override this method if the platform supports temporary tables
     * and the generated sql doesn't work.
     * Write an sql string for insertion into the temporary table.
     * Precondition: supportsTempTables() == true.
     * @parameter Writer writer for writing the sql
     * @parameter DatabaseTable table is original table for which temp table is created.
     * @parameter Collection usedFields - fields that will be used by operation for which temp table is created.
     */
     public void writeInsertIntoTableSql(Writer writer, DatabaseTable table, Collection usedFields) throws IOException {
        writer.write("INSERT INTO ");
        writer.write(getTempTableForTable(table).getQualifiedName());

        writer.write(" (");        
        writeFieldsList(writer, usedFields);
        writer.write(") ");
    }          

    /**
     * INTERNAL:
     * Override this if the platform cannot handle NULL in select clause.
     */
    public boolean isNullAllowedInSelectClause() {
        return true;
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
        writer.write(" SET (");
        writeFieldsList(writer, assignedFields);
        writer.write(") = (SELECT ");        
        writeFieldsList(writer, assignedFields);
        writer.write(" FROM ");
        String tempTableName = getTempTableForTable(table).getQualifiedName();
        writer.write(tempTableName);
        writeAutoJoinWhereClause(writer, null, tableName, pkFields);
        writer.write(") WHERE EXISTS(SELECT ");
        writer.write(((DatabaseField)pkFields.iterator().next()).getName());
        writer.write(" FROM ");
        writer.write(tempTableName);
        writeAutoJoinWhereClause(writer, null, tableName, pkFields);
        writer.write(")");
    }          

    /**
     * INTERNAL:
     * Write an sql string for deletion from target table using temporary table.
     * At this point temporary table should contains pks for the rows that should be
     * deleted from target table.
     * Temporary tables are not required for DeleteAllQuery, however will be used if 
     * shouldAlwaysUseTempStorageForModifyAll()==true
     * May need to override this method in case it generates sql that doesn't work on the platform.
     * Precondition: supportsTempTables() == true.
     * @parameter Writer writer for writing the sql
     * @parameter DatabaseTable table is original table for which temp table is created.
     * @parameter DatabaseTable targetTable is a table from which to delete.
     * @parameter Collection pkFields - primary key fields for the original table.
     * @parameter Collection targetPkFields - primary key fields for the target table.
     * @parameter Collection assignedFields - fields to be assigned a new value.
     */
     public void writeDeleteFromTargetTableUsingTempTableSql(Writer writer, DatabaseTable table, DatabaseTable targetTable,
                                                     Collection pkFields, 
                                                     Collection targetPkFields) throws IOException 
    {
        writer.write("DELETE FROM ");
        String targetTableName = targetTable.getQualifiedName();
        writer.write(targetTableName);
        writer.write(" WHERE EXISTS(SELECT ");
        writer.write(((DatabaseField)pkFields.iterator().next()).getName());
        writer.write(" FROM ");
        String tempTableName = getTempTableForTable(table).getQualifiedName();
        writer.write(tempTableName);
        writeJoinWhereClause(writer, null, targetTableName, pkFields, targetPkFields);
        writer.write(")");
    }          

    /**
     * INTERNAL:
     * Don't override this method.
     * Write an sql string for clean up of the temporary table.
     * Drop a local temp table or delete all from a global temp table (so that it's
     * ready to be used again in the same transaction).
     * Precondition: supportsTempTables() == true.
     * @parameter Writer writer for writing the sql
     * @parameter DatabaseTable table is original table for which temp table is created.
     */
     public void writeCleanUpTempTableSql(Writer writer, DatabaseTable table) throws IOException {
        if(supportsLocalTempTables()) {
            writer.write("DROP TABLE ");
        } else {
            // supportsGlobalTempTables() == true
            writer.write("DELETE FROM ");
        }
        writer.write(getTempTableForTable(table).getQualifiedName());
    }          

    /**
     * INTERNAL:
     * That method affects UpdateAllQuery and DeleteAllQuery execution.
     * In case it returns false modify all queries would attempt to proceed
     * without using temporary storage if it is possible.
     * In case it returns true modify all queries would use temporary storage unless
     * each modify statement doesn't reference any other tables.
     * May need to override this method if the platform can't handle the sql
     * generated for modify all queries without using temporary storage.
     */
    public boolean shouldAlwaysUseTempStorageForModifyAll() {
        return false;
    }
    
   /**
    * INTERNAL:
    * May need to override this method if the sql generated for UpdateAllQuery 
    * using temp tables fails in case parameter binding is used.
    */
    public boolean dontBindUpdateAllQueryUsingTempTables() {
        return false;
    }
    
    /**
     * INTERNAL:
     * helper method, don't override.
     */
    protected static void writeFieldsList(Writer writer, Collection fields) throws IOException {
        boolean isFirst = true;
        Iterator itFields = fields.iterator();
        while(itFields.hasNext()) {
            if(isFirst) {
                isFirst = false;
            } else {
                writer.write(", ");
            }
            DatabaseField field = (DatabaseField)itFields.next();
            writer.write(field.getName());
        }
    }
    
    /**
     * INTERNAL:
     * helper method, don't override.
     */
    protected static void writeAutoAssignmentSetClause(Writer writer, String tableName1, String tableName2, Collection fields) throws IOException {
        writer.write(" SET ");
        writeFieldsAutoClause(writer, tableName1, tableName2, fields, ", ");
    }

    /**
     * INTERNAL:
     * helper method, don't override.
     */
    protected static void writeAutoJoinWhereClause(Writer writer, String tableName1, String tableName2, Collection pkFields) throws IOException {
        writer.write(" WHERE ");
        writeFieldsAutoClause(writer, tableName1, tableName2, pkFields, " AND ");
    }

    /**
     * INTERNAL:
     * helper method, don't override.
     */
    protected static void writeFieldsAutoClause(Writer writer, String tableName1, String tableName2, Collection fields, String separator) throws IOException {
        writeFields(writer, tableName1, tableName2, fields, fields, separator);
    }
    /**
     * INTERNAL:
     * helper method, don't override.
     */
    protected static void writeJoinWhereClause(Writer writer, String tableName1, String tableName2, Collection pkFields1, Collection pkFields2) throws IOException {
        writer.write(" WHERE ");
        writeFields(writer, tableName1, tableName2, pkFields1, pkFields2, " AND ");
    }

    /**
     * INTERNAL:
     * helper method, don't override.
     */
    protected static void writeFields(Writer writer, String tableName1, String tableName2, Collection fields1, Collection fields2, String separator) throws IOException {
        boolean isFirst = true;
        Iterator itFields1 = fields1.iterator();
        Iterator itFields2 = fields2.iterator();
        while(itFields1.hasNext()) {
            if(isFirst) {
                isFirst = false;
            } else {
                writer.write(separator);
            }
            if(tableName1 != null) {
                writer.write(tableName1);
                writer.write(".");
            }
            String fieldName1 = ((DatabaseField)itFields1.next()).getName();
            writer.write(fieldName1);
            writer.write(" = ");
            if(tableName2 != null) {
                writer.write(tableName2);
                writer.write(".");
            }
            String fieldName2 = ((DatabaseField)itFields2.next()).getName();
            writer.write(fieldName2);
        }
    }
    
    public boolean shouldPrintFieldIdentityClause(AbstractSession session, String qualifiedFieldName) {
        if (!supportsIdentity()) {
            return false;
        }
        if ((session.getSequencing() == null) || (session.getSequencing().whenShouldAcquireValueForAll() == Sequencing.BEFORE_INSERT)) {
            return false;
        }

        boolean shouldAcquireSequenceValueAfterInsert = false;
        DatabaseField field = new DatabaseField(qualifiedFieldName);
        Iterator descriptors = session.getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            if (!descriptor.usesSequenceNumbers()) {
                continue;
            }
            if (descriptor.getSequenceNumberField().equals(field)) {
                String seqName = descriptor.getSequenceNumberName();
                Sequence sequence = getSequence(seqName);
                shouldAcquireSequenceValueAfterInsert = sequence.shouldAcquireValueAfterInsert();
                break;
            }
        }
        return shouldAcquireSequenceValueAfterInsert;
    }  
    
    public void printFieldTypeSize(Writer writer, FieldDefinition field, 
            FieldTypeDefinition fieldType, boolean shouldPrintFieldIdentityClause) throws IOException {
        printFieldTypeSize(writer, field, fieldType);
    }
    
    protected void printFieldTypeSize(Writer writer, FieldDefinition field, 
            FieldTypeDefinition fieldType) throws IOException {
        writer.write(fieldType.getName());
        if ((fieldType.isSizeAllowed()) && ((field.getSize() != 0) || (fieldType.isSizeRequired()))) {
            writer.write("(");
            if (field.getSize() == 0) {
                writer.write(new Integer(fieldType.getDefaultSize()).toString());
            } else {
                writer.write(new Integer(field.getSize()).toString());
            }
            if (field.getSubSize() != 0) {
                writer.write(",");
                writer.write(new Integer(field.getSubSize()).toString());
            } else if (fieldType.getDefaultSubSize() != 0) {
                writer.write(",");
                writer.write(new Integer(fieldType.getDefaultSubSize()).toString());
            }
            writer.write(")");
        }
    }
    
    public void printFieldUnique(Writer writer,  boolean shouldPrintFieldIdentityClause) throws IOException {
        printFieldUnique(writer);
    }

    protected void printFieldUnique(Writer writer) throws IOException {
        if (supportsPrimaryKeyConstraint()) {
            writer.write(" UNIQUE");
        }
    }

    public void writeParameterMarker(Writer writer, ParameterExpression expression) throws IOException {
        writer.write("?");
    }

    /**
     * INTERNAL
     * Allows platform to choose whether to bind literals in DatabaseCalls or not.
     */
    public boolean shouldBindLiterals() {
        return true;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects.
     * Returns sql used to create sequence object in the database.
     */
    public Writer buildSequenceObjectCreationWriter(Writer writer, String fullSeqName, int increment, int start) throws IOException {
        return writer;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects.
     * Returns sql used to delete sequence object from the database.
     */
    public Writer buildSequenceObjectDeletionWriter(Writer writer, String fullSeqName) throws IOException {
        return writer;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects
     * and isAlterSequenceObjectSupported returns true.
     * Returns sql used to alter sequence object's increment in the database.
     */
    public Writer buildSequenceObjectAlterIncrementWriter(Writer writer, String fullSeqName, int increment) throws IOException {
        return writer;
    }
    
    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects
     * and it's possible to alter sequence object's increment in the database.
     */
    public boolean isAlterSequenceObjectSupported() {
        return false;
    }
}
