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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.expressions.ExpressionOperator;
import oracle.toplink.essentials.internal.databaseaccess.FieldTypeDefinition;
import oracle.toplink.essentials.internal.expressions.RelationExpression;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.queryframework.ValueReadQuery;
import oracle.toplink.essentials.tools.schemaframework.FieldDefinition;

/**
 * <p><b>Purpose</b>: Provides Postgres specific behaviour.
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
public class PostgreSQLPlatform extends DatabasePlatform {
    
    public PostgreSQLPlatform() {
        super();
    }
    
    /**
     * Appends a Boolean value. 
     * Refer to : http://www.postgresql.org/docs/8.0/static/datatype-boolean.html
     * In PostgreSQL the following are the values that
     * are value for a boolean field
     * Valid literal values for the "true" state are: 
     *  TRUE, 't', 'true', 'y', 'yes', '1'
     * Valid literal values for the false" state are :
     *  FALSE, 'f', 'false', 'n', 'no', '0'
     *
     * To be consistent with the other data platforms we are using the values 
     * '1' and '0' for true and false states of a boolean field.
     */
    protected void appendBoolean(Boolean bool, Writer writer) throws IOException {
        if (bool.booleanValue()) {
            writer.write("\'1\'");
        } else {
            writer.write("\'0\'");
        }
    }
    
    /**
     * INTERNAL:
     * Initialize any platform-specific operators
     */
    protected void initializePlatformOperators() {
        super.initializePlatformOperators();
        addOperator(ExpressionOperator.simpleLogicalNoParens(ExpressionOperator.Concat, "||"));
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Nvl, "NULLIF"));
        addOperator(operatorLocate());
    }


    /**
     * INTERNAL:
     * This method returns the query to select the timestamp from the server
     * for Derby.
     */
    public ValueReadQuery getTimestampQuery() {
        if (timestampQuery == null) {
            timestampQuery = new ValueReadQuery();
            timestampQuery.setSQLString("SELECT NOW()");
        }
        return timestampQuery;

    }


    /**
     * This method is used to print the output parameter token when stored
     * procedures are called
     */
    public String getInOutputProcedureToken() {
        return "OUT";
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
    public boolean isPostgreSQL() {
        return true;
    }

    /**
     * INTERNAL:
     */
    protected String getCreateTempTableSqlSuffix() {
        // http://pgsqld.active-venture.com/sql-createtable.html
        return " ON COMMIT PRESERVE ROWS";
    }

    /**
     *  INTERNAL:
     *  Indicates whether the platform supports identity.
     *  This method is to be used *ONLY* by sequencing classes
     */
    public boolean supportsIdentity() {
        return true;
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
        ValueReadQuery selectQuery = new ValueReadQuery(); 
        selectQuery.setSQLString("select lastval()"); 
        return selectQuery;
    }

    /**
     *  INTERNAL:
     *  Indicates whether the platform supports sequence objects.
     *  This method is to be used *ONLY* by sequencing classes
     */
    public boolean supportsSequenceObjects() {
        return true;
    }

    /**
     * INTERNAL:
     * Returns query used to read value generated by sequence object (like Oracle sequence).
     * This method is called when sequence object NativeSequence is connected,
     * the returned query used until the sequence is disconnected.
     * If the platform supportsSequenceObjects then (at least) one of buildSelectQueryForSequenceObject
     * methods should return non-null query.
     */
    public ValueReadQuery buildSelectQueryForSequenceObject(String seqName, Integer size) {
        return new ValueReadQuery("select nextval(\'"  + getQualifiedSequenceName(seqName) + "\')");
    }

    /**
     * INTERNAL:
     *  Prepend sequence name with table qualifier (if any)
     */
    protected String getQualifiedSequenceName(String seqName) {
        if (getTableQualifier().equals("")) {
            return seqName;
        } else {
            return getTableQualifier() + "." + seqName;
        }
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
     * Append the receiver's field 'identity' constraint clause to a writer
     */
    public void printFieldIdentityClause(Writer writer) throws ValidationException {
        try {            
            writer.write(" SERIAL");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping = new Hashtable();

        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("BOOLEAN", false));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("FLOAT"));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("FLOAT", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("DECIMAL",38));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("DECIMAL",38));

        fieldTypeMapping.put(String.class, new FieldTypeDefinition("VARCHAR", 255));
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR", 1));

        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("BYTEA", false));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("TEXT"));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("BYTEA", false));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("TEXT"));
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("BYTEA"));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("TEXT"));        

        fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATE", false));
        fieldTypeMapping.put(java.sql.Time.class, new FieldTypeDefinition("TIME", false));
        fieldTypeMapping.put(java.sql.Timestamp.class, new FieldTypeDefinition("TIMESTAMP", false));

        return fieldTypeMapping;
    }  
    
    /**
     * INTERNAL:
     * Override the default locate operator
     */
    protected ExpressionOperator operatorLocate() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(ExpressionOperator.Locate);
        Vector v = new Vector(3);
        v.addElement("STRPOS(");
        v.addElement(", ");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(RelationExpression.class);
        return result;
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
        return "CREATE GLOBAL TEMPORARY TABLE ";
    }
                
    /**
     * INTERNAL:
     * returns the maximum number of characters that can be used in a field
     * name on this platform.
     */
    public int getMaxFieldNameSize() {
        // The system uses no more than NAMEDATALEN-1 characters of an identifier; longer names can be written in commands, 
        //    but they will be truncated. By default, NAMEDATALEN is 64 so the maximum identifier length is 63 (but at the time PostgreSQL 
        //    is built, NAMEDATALEN can be changed in src/include/postgres_ext.h).
        // http://www.postgresql.org/docs/7.3/interactive/sql-syntax.html#SQL-SYNTAX-IDENTIFIERS
        return 63;
    }

    // http://www.postgresql.org/docs/8.1/interactive/plpgsql-declarations.html
    /**
     * INTERNAL:
     * Used for sp calls.
     */
    public String getProcedureBeginString() {
        return "AS $$  BEGIN ";
    }

    /**
     * INTERNAL:
     * Used for sp calls.
     */
    public String getProcedureEndString() {
        return "; END ; $$ LANGUAGE plpgsql;";
    }

    /**
     * INTERNAL:
     * Used for sp calls.
     */
    public String getProcedureCallHeader() {
        return "EXECUTE ";
    }
    
    /**
     * INTERNAL
     * Used for stored function calls.
     */
    public String getAssignmentString() {
        return ":= ";
    }    
    
    public void printFieldTypeSize(Writer writer, FieldDefinition field, 
            FieldTypeDefinition fieldType, boolean shouldPrintFieldIdentityClause) throws IOException {
        if(!shouldPrintFieldIdentityClause) {
            super.printFieldTypeSize(writer, field, fieldType);
        }
    }
    
    public void printFieldUnique(Writer writer,  boolean shouldPrintFieldIdentityClause) throws IOException {
        if(!shouldPrintFieldIdentityClause) {
            super.printFieldUnique(writer);
        }
    }

    /**
     * JDBC defines and outer join syntax, many drivers do not support this. So we normally avoid it.
     */
    public boolean shouldUseJDBCOuterJoinSyntax() {
        return false;
    }    
     
    /**
     * Set a primitive parameter.
     * Postgres jdbc client driver give problem when doing a setObject() for wrapper types.
     * Ideally this code should be in the DatabasePlatform so that all platforms can use
     * this. 
     */        
    protected void setPrimitiveParameterValue(final PreparedStatement statement, final int index, 
            final Object parameter) throws SQLException {
       if (parameter instanceof Number) {
            Number number = (Number) parameter;
            if (number instanceof Integer) {
                statement.setInt(index, number.intValue());
            } else if (number instanceof Long) {
                statement.setLong(index, number.longValue());
            } else if (number instanceof Short) {
                statement.setShort(index, number.shortValue());
            } else if (number instanceof Byte) {
                statement.setByte(index, number.byteValue());
            } else if (number instanceof Double) {
                statement.setDouble(index, number.doubleValue());
            } else if (number instanceof Float) {
                statement.setFloat(index, number.floatValue());
            } else if (number instanceof BigDecimal) {
                statement.setBigDecimal(index, (BigDecimal) number);
            } else if (number instanceof BigInteger) {
                statement.setBigDecimal(index, new BigDecimal((BigInteger) number));
            } else {
                statement.setObject(index, parameter);
            }
        } else if (parameter instanceof String) {
            statement.setString(index, (String)parameter);
        } else if (parameter instanceof Boolean) {
            statement.setBoolean(index, ((Boolean) parameter).booleanValue());
        } else {
            statement.setObject(index, parameter);
        }           
    }   

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects.
     * Returns sql used to create sequence object in the database.
     */
    public Writer buildSequenceObjectCreationWriter(Writer writer, String fullSeqName, int increment, int start) throws IOException {
        writer.write("CREATE SEQUENCE ");
        writer.write(fullSeqName);
        if (increment != 1) {
            writer.write(" INCREMENT BY " + increment);
        }
        writer.write(" START WITH " + start);
        return writer;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects.
     * Returns sql used to delete sequence object from the database.
     */
    public Writer buildSequenceObjectDeletionWriter(Writer writer, String fullSeqName) throws IOException {
        writer.write("DROP SEQUENCE ");
        writer.write(fullSeqName);
        return writer;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects
     * and isAlterSequenceObjectSupported returns true.
     * Returns sql used to alter sequence object's increment in the database.
     */
    public Writer buildSequenceObjectAlterIncrementWriter(Writer writer, String fullSeqName, int increment) throws IOException {
        writer.write("ALTER SEQUENCE ");
        writer.write(fullSeqName);
        writer.write(" INCREMENT BY " + increment);
        return writer;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects
     * and it's possible to alter sequence object's increment in the database.
     */
    public boolean isAlterSequenceObjectSupported() {
        return true;
    }
}
