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
import oracle.toplink.essentials.internal.databaseaccess.*;

/**
 * <p><b>Purpose</b>: Provides DBase specific behaviour.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Writing Time & Timestamp as strings since they are not supported.
 * </ul>
 *
 * @since TOPLink/Java 1.0
 */
public class DBasePlatform extends oracle.toplink.essentials.platform.database.DatabasePlatform {
    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping;

        fieldTypeMapping = new Hashtable();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("NUMBER", 1));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("NUMBER", 11));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("NUMBER", 19));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("NUMBER", 12, 5).setLimits(19, 0, 19));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("NUMBER", 10, 5).setLimits(19, 0, 19));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("NUMBER", 6));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("NUMBER", 4));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("NUMBER", 19));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("NUMBER", 19).setLimits(19, 0, 9));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("NUMBER", 19).setLimits(19, 0, 9));

        fieldTypeMapping.put(String.class, new FieldTypeDefinition("CHAR", 255));
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR", 1));

        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("BINARY"));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("MEMO"));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("BINARY"));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("MEMO"));
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("BINARY"));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("MEMO"));        

        fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATE", false));
        fieldTypeMapping.put(java.sql.Time.class, new FieldTypeDefinition("CHAR", 15));
        fieldTypeMapping.put(java.sql.Timestamp.class, new FieldTypeDefinition("CHAR", 25));

        return fieldTypeMapping;
    }

    /**
     * INTERNAL:
     * DBase does not support Time/Timestamp so we must map to strings.
     * 2.0p22: protected->public INTERNAL
     */
    public Object convertToDatabaseType(Object value) {
        Object dbValue = super.convertToDatabaseType(value);
        if ((dbValue instanceof java.sql.Time) || (dbValue instanceof java.sql.Timestamp)) {
            return dbValue.toString();
        }
        return dbValue;
    }

    /**
     * INTERNAL:
     * returns the maximum number of characters that can be used in a field
     * name on this platform.
     */
    public int getMaxFieldNameSize() {
        return 10;
    }
    
    /**
     * INTERNAL:
     */
    public String getSelectForUpdateString() {
        return " FOR UPDATE OF *";
    }

    public boolean isDBase() {
        return true;
    }

    /**
     * INTERNAL:
     * Builds a table of minimum numeric values keyed on java class. This is used for type testing but
     * might also be useful to end users attempting to sanitize values.
     * <p><b>NOTE</b>: BigInteger & BigDecimal minimums are dependent upon their precision & Scale
     */
    public Hashtable maximumNumericValues() {
        Hashtable values = new Hashtable();

        values.put(Integer.class, new Integer(Integer.MAX_VALUE));
        values.put(Long.class, Long.valueOf("922337203685478000"));
        values.put(Double.class, new Double("99999999.999999999"));
        values.put(Short.class, new Short(Short.MIN_VALUE));
        values.put(Byte.class, new Byte(Byte.MIN_VALUE));
        values.put(Float.class, new Float("99999999.999999999"));
        values.put(java.math.BigInteger.class, new java.math.BigInteger("922337203685478000"));
        values.put(java.math.BigDecimal.class, new java.math.BigDecimal("999999.999999999"));
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
        values.put(Long.class, Long.valueOf("-922337203685478000"));
        values.put(Double.class, new Double("-99999999.999999999"));
        values.put(Short.class, new Short(Short.MIN_VALUE));
        values.put(Byte.class, new Byte(Byte.MIN_VALUE));
        values.put(Float.class, new Float("-99999999.999999999"));
        values.put(java.math.BigInteger.class, new java.math.BigInteger("-922337203685478000"));
        values.put(java.math.BigDecimal.class, new java.math.BigDecimal("-999999.999999999"));
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
     * JDBC defines and outer join syntax, many drivers do not support this. So we normally avoid it.
     */
    public boolean shouldUseJDBCOuterJoinSyntax() {
        return false;
    }
    
    /**
     * INTERNAL:
     */
    public boolean supportsForeignKeyConstraints() {
        return false;
    }
    
    /**
     * INTERNAL:
     */
    public boolean supportsPrimaryKeyConstraint() {
        return false;
    }
}
