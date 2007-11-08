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

package oracle.toplink.essentials.testing.models.cmp3.virtualattribute;

import oracle.toplink.essentials.tools.schemaframework.*;

public class VirtualAttributeTableCreator extends oracle.toplink.essentials.tools.schemaframework.TableCreator {

    public VirtualAttributeTableCreator() {
        setName("VirtualAttributeProject");

        addTableDefinition(buildVIRTUALATTRIBUTETable());
        addTableDefinition(buildOOVIRTUALATTRIBUTETable());
        addTableDefinition(buildVIRTUAL_SEQTable());
    }
    
    public static TableDefinition buildVIRTUALATTRIBUTETable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_VIRTUAL");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("CMP3_VIRTUALID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition fieldDESC = new FieldDefinition();
        fieldDESC.setName("DESCRIPTION");
        fieldDESC.setTypeName("VARCHAR2");
        fieldDESC.setSize(60);
        fieldDESC.setSubSize(0);
        fieldDESC.setIsPrimaryKey(false);
        fieldDESC.setIsIdentity(false);
        fieldDESC.setUnique(false);
        fieldDESC.setShouldAllowNull(true);
        table.addField(fieldDESC);

        return table;
    }

    public static TableDefinition buildOOVIRTUALATTRIBUTETable() {
        TableDefinition table = new TableDefinition();
        // SECTION: TABLE
        table.setName("O_O_VIRTUAL");
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field.setName("O_O_VIRTUALID");
        field.setTypeName("NUMERIC");
        field.setSize(15);
        field.setShouldAllowNull(false );
        field.setIsPrimaryKey(true );
        field.setUnique(false );
        field.setIsIdentity(true );
        table.addField(field);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field8 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field8.setName("VIRTUAL_ID");
        field8.setTypeName("NUMERIC");
        field8.setSize(15);
        field8.setShouldAllowNull(true );
        field8.setIsPrimaryKey(false );
        field8.setUnique(false );
        field8.setIsIdentity(false );
        field8.setForeignKeyFieldName("CMP3_VIRTUAL.CMP3_VIRTUALID");
        table.addField(field8);
        
        return table;
    }
    
    public static TableDefinition buildVIRTUAL_SEQTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_VIRTUAL_SEQ");

        FieldDefinition fieldSEQ_COUNT = new FieldDefinition();
        fieldSEQ_COUNT.setName("SEQ_COUNT");
        fieldSEQ_COUNT.setTypeName("NUMBER");
        fieldSEQ_COUNT.setSize(15);
        fieldSEQ_COUNT.setSubSize(0);
        fieldSEQ_COUNT.setIsPrimaryKey(false);
        fieldSEQ_COUNT.setIsIdentity(false);
        fieldSEQ_COUNT.setUnique(false);
        fieldSEQ_COUNT.setShouldAllowNull(false);
        table.addField(fieldSEQ_COUNT);

        FieldDefinition fieldSEQ_NAME = new FieldDefinition();
        fieldSEQ_NAME.setName("SEQ_NAME");
        fieldSEQ_NAME.setTypeName("VARCHAR2");
        fieldSEQ_NAME.setSize(80);
        fieldSEQ_NAME.setSubSize(0);
        fieldSEQ_NAME.setIsPrimaryKey(true);
        fieldSEQ_NAME.setIsIdentity(false);
        fieldSEQ_NAME.setUnique(false);
        fieldSEQ_NAME.setShouldAllowNull(false);
        table.addField(fieldSEQ_NAME);

        return table;
    }
    
}