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
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: Provides Microsoft Access specific behaviour.
 *
 * @since TOPLink/Java 1.0
 */
public class AccessPlatform extends oracle.toplink.essentials.platform.database.DatabasePlatform {
    protected Hashtable buildClassTypes() {
        Hashtable classTypeMapping = super.buildClassTypes();

        // In access LONG means numeric not CLOB like in Oracle
        classTypeMapping.put("LONG", Long.class);
        classTypeMapping.put("TEXT", String.class);

        return classTypeMapping;
    }

    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping;

        fieldTypeMapping = new Hashtable();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("BIT", false));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("LONG", false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("DOUBLE", false));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("DOUBLE", false));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("DOUBLE", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SHORT", false));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("BYTE", false));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("DOUBLE", false));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("DOUBLE", false));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("DOUBLE", false));

        fieldTypeMapping.put(String.class, new FieldTypeDefinition("TEXT", 255));
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("TEXT", 1));
        
        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("LONGBINARY", false));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("MEMO", false));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("LONGBINARY", false));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("MEMO", false));
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("LONGBINARY", false));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("MEMO", false));
        
        fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATETIME", false));
        fieldTypeMapping.put(java.sql.Time.class, new FieldTypeDefinition("DATETIME", false));
        fieldTypeMapping.put(java.sql.Timestamp.class, new FieldTypeDefinition("DATETIME", false));

        return fieldTypeMapping;
    }

    /**
     * INTERNAL:
     * returns the maximum number of characters that can be used in a field
     * name on this platform.
     */
    public int getMaxFieldNameSize() {
        return 64;
    }

    /**
     * INTERNAL:
     * Access do not support millisecond well, truncate the millisecond from the timestamp
     */
    public java.sql.Timestamp getTimestampFromServer(AbstractSession session, String sessionName) {
        if (getTimestampQuery() == null) {
            java.sql.Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());
            currentTime.setNanos(0);
            return currentTime;
        } else {
            getTimestampQuery().setSessionName(sessionName);
            return (java.sql.Timestamp)session.executeQuery(getTimestampQuery());
        }
    }

    /**
     * INTERNAL:
     * Initialize any platform-specific operators
     */
    protected void initializePlatformOperators() {
        super.initializePlatformOperators();

        addOperator(ExpressionOperator.simpleFunction(ExpressionOperator.ToUpperCase, "UCASE"));
        addOperator(ExpressionOperator.simpleFunction(ExpressionOperator.ToLowerCase, "LCASE"));
    }

    public boolean isAccess() {
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
        values.put(Double.class, new Double(Double.MAX_VALUE));
        values.put(Short.class, new Short(Short.MAX_VALUE));
        values.put(Byte.class, new Byte(Byte.MAX_VALUE));
        values.put(Float.class, new Float(123456789));
        values.put(java.math.BigInteger.class, new java.math.BigInteger("999999999999999"));
        values.put(java.math.BigDecimal.class, new java.math.BigDecimal("99999999999999999999.9999999999999999999"));
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
        values.put(Double.class, new Double(Double.MIN_VALUE));
        values.put(Short.class, new Short(Short.MIN_VALUE));
        values.put(Byte.class, new Byte(Byte.MIN_VALUE));
        values.put(Float.class, new Float(-123456789));
        values.put(java.math.BigInteger.class, new java.math.BigInteger("-999999999999999"));
        values.put(java.math.BigDecimal.class, new java.math.BigDecimal("-9999999999999999999.9999999999999999999"));
        return values;
    }

    /**
     * INTERNAL:
     * Append the receiver's field 'NOT NULL' constraint clause to a writer.
     */
    public void printFieldNotNullClause(Writer writer) {
        // Do nothing
    }

    /**
     * INTERNAL:
     * This is used as some databases create the primary key constraint differently, i.e. Access.
     */
    public boolean requiresNamedPrimaryKeyConstraints() {
        return true;
    }

    /**
     * INTERNAL:
     * JDBC defines and outer join syntax, many drivers do not support this. So we normally avoid it.
     */
    public boolean shouldUseJDBCOuterJoinSyntax() {
        return false;
    }
}
