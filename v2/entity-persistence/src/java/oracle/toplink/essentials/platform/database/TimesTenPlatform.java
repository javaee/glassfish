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
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import oracle.toplink.essentials.expressions.ExpressionOperator;
import oracle.toplink.essentials.internal.databaseaccess.FieldTypeDefinition;
import oracle.toplink.essentials.internal.expressions.RelationExpression;
import oracle.toplink.essentials.internal.helper.Helper;
import oracle.toplink.essentials.queryframework.ValueReadQuery;

public class TimesTenPlatform extends DatabasePlatform {

    //supportsForeignKeyConstraints is settable because TimesTen does not support circular referencing/self referencing
    private boolean supportsForeignKeyConstraints;
    
    public TimesTenPlatform() {
        supportsForeignKeyConstraints = true;
    }

    /**
     *    If using native SQL then print a byte[] literally as a hex string otherwise use ODBC format
     *    as provided in DatabasePlatform.
     */
    protected void appendByteArray(byte[] bytes, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("Ox");
            Helper.writeHexString(bytes, writer);
        } else {
            super.appendByteArray(bytes, writer);
        }
    }

    /**
     * Appends an MySQL specific date if usesNativeSQL is true otherwise use the ODBC format.
     * Native FORMAT: 'YYYY-MM-DD'
     */
    protected void appendDate(java.sql.Date date, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("DATE '");
            writer.write(Helper.printDate(date));
            writer.write("'");
        } else {
            super.appendDate(date, writer);
        }
    }

    /**
     * Appends an MySQL specific time if usesNativeSQL is true otherwise use the ODBC format.
     * Native FORMAT: 'HH:MM:SS'.
     */
    protected void appendTime(java.sql.Time time, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("TIME '");
            writer.write(Helper.printTime(time));
            writer.write("'");
        } else {
            super.appendTime(time, writer);
        }
    }

    /**
     * Appends an MySQL specific Timestamp, if usesNativeSQL is true otherwise use the ODBC format.
     * Native Format: 'YYYY-MM-DD HH:MM:SS' 
     */
    protected void appendTimestamp(java.sql.Timestamp timestamp, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("TIMESTAMP '");
            writer.write(Helper.printTimestampWithoutNanos(timestamp));
            writer.write("'");
        } else {
            super.appendTimestamp(timestamp, writer);
        }
    }

    /**
     * Appends an MySQL specific Timestamp, if usesNativeSQL is true otherwise use the ODBC format.
     * Native Format: 'YYYY-MM-DD HH:MM:SS'
     */
    protected void appendCalendar(Calendar calendar, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("TIMESTAMP '");
            writer.write(Helper.printCalendarWithoutNanos(calendar));
            writer.write("'");
        } else {
            super.appendCalendar(calendar, writer);
        }
    }

    /**
     * Return the mapping of class types to database types for the schema framework.
     */
    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping;

        fieldTypeMapping = new Hashtable();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("TINYINT", false));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("FLOAT", false));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("DOUBLE", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("TINYINT", false));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("DECIMAL(38)", false));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("DECIMAL(38)", false));

        fieldTypeMapping.put(String.class, new FieldTypeDefinition("VARCHAR", 255));
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR", 1));

        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("VARBINARY", 64000));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("VARCHAR", 64000));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("VARBINARY", 64000));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("VARCHAR", 64000));
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("VARBINARY", 64000));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("VARCHAR", 64000));        
        
        fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATE", false));
        fieldTypeMapping.put(java.sql.Time.class, new FieldTypeDefinition("TIME", false));
        fieldTypeMapping.put(java.sql.Timestamp.class, new FieldTypeDefinition("TIMESTAMP", false));

        return fieldTypeMapping;
    }

    /**
     * INTERNAL:
     *    Produce a DataReadQuery which updates(!) the sequence number in the db
     *  and returns it. 
     *    @param sequenceName        Name known by TimesTen to be a defined sequence
     */
    public ValueReadQuery buildSelectQueryForSequenceObject(String seqName, Integer size) {
        return new ValueReadQuery("SELECT " + getQualifiedSequenceName(seqName) + ".NEXTVAL FROM DUAL");
    }

    /**
     * INTERNAL:
     * Used for view creation.
     */
    public String getCreateViewString() {
        return "CREATE MATERIALIZED VIEW ";
    }
    
    /**
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
     * Used for pessimistic locking.
     */
    public String getSelectForUpdateString() {
        return " FOR UPDATE";
    }
    
    /**
     * PUBLIC:
     * This method returns the query to select the timestamp
     * from the server for TimesTen.
     */
    public ValueReadQuery getTimestampQuery() {
        if (timestampQuery == null) {
            timestampQuery = new ValueReadQuery();
            timestampQuery.setSQLString("SELECT SYSDATE FROM DUAL");
        }
        return timestampQuery;
    }

    /**
     * Initialize any platform-specific operators
     */
    protected void initializePlatformOperators() {
        super.initializePlatformOperators();
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Concat, "CONCAT"));
        addOperator(operatorOuterJoin());
        addOperator(ExpressionOperator.ifNull());
    }

    /**
     * Answers whether platform is TimesTen
     */
    public boolean isTimesTen() {
        return true;
    }

    /**
     *  Create the outer join operator for this platform
     */
    protected ExpressionOperator operatorOuterJoin() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(ExpressionOperator.EqualOuterJoin);
        Vector v = new Vector(2);
        v.addElement(" (+) = ");
        result.printsAs(v);
        result.bePostfix();
        result.setNodeClass(RelationExpression.class);
        return result;
    }

    /**
     * Some database require outer joins to be given in the where clause, others require it in the from clause.
     */
    public boolean shouldPrintOuterJoinInWhereClause() {
        return true;
    }

    /**
     *  INTERNAL:
     *  Indicates whether the platform supports sequence objects.
     *  This method is to be used *ONLY* by sequencing classes
     */
    public boolean supportsSequenceObjects() {
        return true;
    }

    public boolean supportsForeignKeyConstraints() {
        return supportsForeignKeyConstraints;
    }

    public void setSupportsForeignKeyConstraints(boolean supportsForeignKeyConstraints) {
        this.supportsForeignKeyConstraints = supportsForeignKeyConstraints;
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
}
