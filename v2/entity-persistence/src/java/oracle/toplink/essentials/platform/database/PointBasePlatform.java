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
import oracle.toplink.essentials.expressions.ExpressionOperator;
import oracle.toplink.essentials.internal.databaseaccess.*;

/**
 * Support the Pointbase database.
 */
public class PointBasePlatform extends oracle.toplink.essentials.platform.database.DatabasePlatform {

    /**
     * INTERNAL:
     * Appends a Boolean value as true/false instead of 0/1
     */
    protected void appendBoolean(Boolean bool, Writer writer) throws IOException {
        writer.write(bool.toString());
    }

    /**
     * INTERNAL:
     * Write a Time in PointBase specific format.
     */
    protected void appendTime(java.sql.Time time, Writer writer) throws IOException {
        writer.write("TIME '" + time + "'");
    }

    /**
     * INTERNAL:
     * Write a Date in PointBase specific format.
     */
    protected void appendDate(java.sql.Date date, Writer writer) throws IOException {
        writer.write("DATE '" + date + "'");
    }

    /**
     * INTERNAL:
     * Write a TimeStamp in PointBase specific format.
     */
    protected void appendTimestamp(java.sql.Timestamp timestamp, Writer writer) throws IOException {
        writer.write("TIMESTAMP '" + timestamp + "'");
    }

    protected Hashtable buildClassTypes() {
        Hashtable classTypeMapping = super.buildClassTypes();

        classTypeMapping.put("FLOAT", Double.class);
        classTypeMapping.put("DOUBLE PRECISION", Double.class);
        classTypeMapping.put("CHARACTER", String.class);
        classTypeMapping.put("CLOB", Character[].class);
        classTypeMapping.put("BLOB", Byte[].class);
        classTypeMapping.put("BOOLEAN", Boolean.class);

        return classTypeMapping;
    }

    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping;

        fieldTypeMapping = super.buildFieldTypes();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("BOOLEAN"));

        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("NUMERIC", 19));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("NUMERIC", 5));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("NUMERIC", 3));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("NUMERIC", 19));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("REAL", false));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("DOUBLE", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("DECIMAL"));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("DECIMAL"));

        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHARACTER"));

        return fieldTypeMapping;
    }

    protected void initializePlatformOperators() {
        super.initializePlatformOperators();
        addOperator(ExpressionOperator.simpleMath(ExpressionOperator.Concat, "||"));
    }

    public boolean isPointBase() {
        return true;
    }
}
