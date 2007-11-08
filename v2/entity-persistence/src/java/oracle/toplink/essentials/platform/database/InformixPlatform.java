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
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.queryframework.*;

/**
 * <p><b>Purpose</b>: Provides Informix specific behaviour.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Types for schema creation.
 * <li> Native sequencing using @@SERIAL.
 * </ul>
 *
 * @since TOPLink/Java 1.0.1
 */
public class InformixPlatform extends oracle.toplink.essentials.platform.database.DatabasePlatform {

    /**
     * INTERNAL:
     * Answer a platform correct string representation of a Date, suitable for SQL generation.
     * Native format: 'yyyy-mm-dd
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
     * Write a timestamp in Informix specific format (yyyy-mm-dd hh:mm:ss.fff).
     */
    protected void appendInformixTimestamp(java.sql.Timestamp timestamp, Writer writer) throws IOException {
        writer.write("'");
        writer.write(Helper.printTimestampWithoutNanos(timestamp));
        writer.write('.');

        // Must truncate the nanos to three decimal places,
        // it is actually a complex algorithm...
        String nanoString = Integer.toString(timestamp.getNanos());
        int numberOfZeros = 0;
        for (int num = Math.min(9 - nanoString.length(), 3); num > 0; num--) {
            writer.write('0');
            numberOfZeros++;
        }
        if ((nanoString.length() + numberOfZeros) > 3) {
            nanoString = nanoString.substring(0, (3 - numberOfZeros));
        }
        writer.write(nanoString);
        writer.write("'");
    }

    /**
     * INTERNAL:
     * Answer a platform correct string representation of a Calendar, suitable for SQL generation.
     * The date is printed in the ODBC platform independent format {d'YYYY-MM-DD'}.
     */
    protected void appendCalendar(Calendar calendar, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            appendInformixCalendar(calendar, writer);
        } else {
            super.appendCalendar(calendar, writer);
        }
    }

    /**
     * INTERNAL:
     * Write a timestamp in Informix specific format ( yyyy-mm-dd hh:mm:ss.fff)
     */
    protected void appendInformixCalendar(Calendar calendar, Writer writer) throws IOException {
        writer.write("'");
        writer.write(Helper.printCalendar(calendar));
        writer.write("'");
    }

    /**
     * INTERNAL:
     * Answer a platform correct string representation of a Time, suitable for SQL generation.
     * The time is printed in the ODBC platform independent format {t'hh:mm:ss'}.
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
     * Answer a platform correct string representation of a Timestamp, suitable for SQL generation.
     * The date is printed in the ODBC platform independent format {d'YYYY-MM-DD'}.
     */
    protected void appendTimestamp(java.sql.Timestamp timestamp, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            appendInformixTimestamp(timestamp, writer);
        } else {
            super.appendTimestamp(timestamp, writer);
        }
    }

    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping;

        fieldTypeMapping = new Hashtable();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("SMALLINT default 0", false));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("NUMERIC", 19));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("FLOAT(16)", false));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("FLOAT(32)", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("DECIMAL", 32));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("DECIMAL", 32).setLimits(32, -19, 19));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("DECIMAL", 32).setLimits(32, -19, 19));

        fieldTypeMapping.put(String.class, new FieldTypeDefinition("VARCHAR", 255));
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR", 1));
        
        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("BYTE", false));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("TEXT", false));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("BYTE", false));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("TEXT", false));
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("BYTE", false));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("TEXT", false));        

        fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATE", false));
        fieldTypeMapping.put(java.sql.Time.class, new FieldTypeDefinition("DATETIME HOUR TO SECOND", false));
        fieldTypeMapping.put(java.sql.Timestamp.class, new FieldTypeDefinition("DATETIME YEAR TO FRACTION(5)", false));

        return fieldTypeMapping;
    }

    /**
     * INTERNAL:
     * Build the identity query for native sequencing.
     */
    public ValueReadQuery buildSelectQueryForIdentity() {
        ValueReadQuery selectQuery = new ValueReadQuery();
        StringWriter writer = new StringWriter();
        writer.write("SELECT DISTINCT(DBINFO('sqlca.sqlerrd1')) FROM systables");
        selectQuery.setSQLString(writer.toString());
        return selectQuery;
    }

    /**
     * INTERNAL:
     * returns the maximum number of characters that can be used in a field
     * name on this platform.
     */
    public int getMaxFieldNameSize() {
        return 18;
    }

    /**
     * INTERNAL:
     * Informix seems to like this syntax instead of the OF * syntax.
     */
    public String getSelectForUpdateString() {
        return " FOR UPDATE";
    }

    public boolean isInformix() {
        return true;
    }

    /**
     * INTERNAL:
     * Some database require outer joins to be given in the where clause, others require it in the from clause.
     * Informix requires it in the from clause with no ON expression.
     */
    public boolean isInformixOuterJoin() {
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
        values.put(Long.class, new Long(Long.MAX_VALUE));
        values.put(Double.class, new Double((double)Float.MAX_VALUE));
        values.put(Short.class, new Short(Short.MAX_VALUE));
        values.put(Byte.class, new Byte(Byte.MAX_VALUE));
        values.put(Float.class, new Float(Float.MAX_VALUE));
        values.put(java.math.BigInteger.class, new java.math.BigInteger("99999999999999999999999999999999999999"));
        values.put(java.math.BigDecimal.class, new java.math.BigDecimal("9999999999999999999.9999999999999999999"));
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
        values.put(Long.class, new Long(Long.MIN_VALUE));
        values.put(Double.class, new Double((double)1.4012984643247149E-44));// The double values are weird. They lose precision at E-45
        values.put(Short.class, new Short(Short.MIN_VALUE));
        values.put(Byte.class, new Byte(Byte.MIN_VALUE));
        values.put(Float.class, new Float(Float.MIN_VALUE));
        values.put(java.math.BigInteger.class, new java.math.BigInteger("-99999999999999999999999999999999999999"));
        values.put(java.math.BigDecimal.class, new java.math.BigDecimal("-9999999999999999999.9999999999999999999"));
        return values;
    }

    /**
     * INTERNAL:
     * Append the receiver's field serial constraint clause to a writer.
     */
    public void printFieldIdentityClause(Writer writer) throws ValidationException {
        try {
            writer.write(" SERIAL");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /**
     * INTERNAL:
     * USed for sp calls.
     */
    public boolean requiresProcedureCallBrackets() {
        return false;
    }

    /**
     * INTERNAL:
     * Some Platforms want the constraint name after the constraint definition.
     */
    public boolean shouldPrintConstraintNameAfter() {
        return true;
    }

    /**
     * INTERNAL:
     * Some database require outer joins to be given in the where clause, others require it in the from clause.
     */
    public boolean shouldPrintOuterJoinInWhereClause() {
        return false;
    }

    /**
     *  INTERNAL:
     *  Indicates whether the platform supports identity.
     *  Informix does this through SERIAL field types.
     *  This method is to be used *ONLY* by sequencing classes
     */
    public boolean supportsIdentity() {
        return true;
    }
}
