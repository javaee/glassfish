/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.datatypes.arraypks;

import oracle.toplink.essentials.tools.schemaframework.TableDefinition;
import oracle.toplink.essentials.tools.schemaframework.FieldDefinition;

public class PrimitiveArraysAsPrimaryKeyTableCreator extends oracle.toplink.essentials.tools.schemaframework.TableCreator{
    public PrimitiveArraysAsPrimaryKeyTableCreator() {
        setName("EJB3PrimitiveArrayPrimaryKeyProject");

        addTableDefinition(PrimitiveArraysAsPrimaryKeyTableCreator.buildPrimitiveByteArrayTable());
    }

    /**This is Oracle specific
     * Oracle does not allow blobs/longs to be primary keys, so the RAW type needs to be used
     */
    public static TableDefinition buildPrimitiveByteArrayTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_PBYTEARRAYPK_TYPE");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("ID");
        fieldID.setTypeName("RAW");
        fieldID.setSize(16);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        return table;
    }
}

