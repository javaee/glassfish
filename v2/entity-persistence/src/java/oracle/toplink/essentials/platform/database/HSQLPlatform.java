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

import java.util.*;
import oracle.toplink.essentials.internal.databaseaccess.*;

/**
 * <p><b>Purpose</b>: Provides HSQL specific behaviour.
 *
 * @since TOPLink/Java 4.5
 */
public class HSQLPlatform extends DatabasePlatform {
    public HSQLPlatform() {
    }

    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping;

        fieldTypeMapping = super.buildFieldTypes();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("TINYINT", false));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("NUMERIC", 19));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("REAL", false));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("REAL", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("NUMERIC", 38));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("NUMERIC", 38).setLimits(38, -19, 19));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("NUMERIC", 38).setLimits(38, -19, 19));
        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("BINARY", false));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("LONGVARCHAR", false));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("BINARY", false));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("LONGVARCHAR", false));
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("BINARY", false));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("LONGVARCHAR", false));        

        return fieldTypeMapping;
    }

    public boolean isHSQL() {
        return true;
    }

    /**
     * INTERNAL:
     * HSQL 1.6.1 does not support the ALTER TABLE method of create foreign key constraints
     */
    public boolean supportsForeignKeyConstraints() {
        return false;
    }
}
