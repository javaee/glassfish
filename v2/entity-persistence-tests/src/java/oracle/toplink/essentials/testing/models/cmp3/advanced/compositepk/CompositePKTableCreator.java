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

package oracle.toplink.essentials.testing.models.cmp3.advanced.compositepk;

import oracle.toplink.essentials.tools.schemaframework.*;

public class CompositePKTableCreator extends TableCreator {
    public CompositePKTableCreator() {
        setName("EJB3CompositePKProject");

        addTableDefinition(buildSCIENTISTTable());
        addTableDefinition(buildDEPARTMENTTable());
        addTableDefinition(buildCUBICLETable());
    }

    public static TableDefinition buildSCIENTISTTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_SCIENTIST");
    
        FieldDefinition ID_NUMBER_field = new FieldDefinition();
        ID_NUMBER_field.setName("ID_NUMBER");
        ID_NUMBER_field.setTypeName("NUMERIC");
        ID_NUMBER_field.setSize(15);
        ID_NUMBER_field.setShouldAllowNull(false);
        ID_NUMBER_field.setIsPrimaryKey(true);
        ID_NUMBER_field.setUnique(false);
        ID_NUMBER_field.setIsIdentity(true);
        table.addField(ID_NUMBER_field);
    
        FieldDefinition F_NAME_field = new FieldDefinition();
        F_NAME_field.setName("F_NAME");
        F_NAME_field.setTypeName("VARCHAR");
        F_NAME_field.setSize(40);
        F_NAME_field.setShouldAllowNull(false);
        F_NAME_field.setIsPrimaryKey(true);
        F_NAME_field.setUnique(false);
        F_NAME_field.setIsIdentity(true);
        table.addField(F_NAME_field);
    
        FieldDefinition L_NAME_Field = new FieldDefinition();
        L_NAME_Field.setName("L_NAME");
        L_NAME_Field.setTypeName("VARCHAR");
        L_NAME_Field.setSize(40);
        L_NAME_Field.setShouldAllowNull(false);
        L_NAME_Field.setIsPrimaryKey(true);
        L_NAME_Field.setUnique(false);
        L_NAME_Field.setIsIdentity(true);
        table.addField(L_NAME_Field);
    
        FieldDefinition CUBE_ID_field = new FieldDefinition();
        CUBE_ID_field.setName("CUBE_ID");
        CUBE_ID_field.setTypeName("NUMERIC");
        CUBE_ID_field.setSize(15);
        CUBE_ID_field.setShouldAllowNull(true);
        CUBE_ID_field.setIsPrimaryKey(false);
        CUBE_ID_field.setUnique(false);
        CUBE_ID_field.setIsIdentity(false);
        table.addField(CUBE_ID_field);
    
        FieldDefinition CUBE_CODE_field = new FieldDefinition();
        CUBE_CODE_field.setName("CUBE_CODE");
        CUBE_CODE_field.setTypeName("VARCHAR");
        CUBE_CODE_field.setSize(1);
        CUBE_CODE_field.setShouldAllowNull(true);
        CUBE_CODE_field.setIsPrimaryKey(false);
        CUBE_CODE_field.setUnique(false);
        CUBE_CODE_field.setIsIdentity(false);
        table.addField(CUBE_CODE_field);
    
        FieldDefinition DEPT_NAME_field = new FieldDefinition();
        DEPT_NAME_field.setName("DEPT_NAME");
        DEPT_NAME_field.setTypeName("VARCHAR");
        DEPT_NAME_field.setSize(40);
        DEPT_NAME_field.setShouldAllowNull(true);
        DEPT_NAME_field.setIsPrimaryKey(false);
        DEPT_NAME_field.setUnique(false);
        DEPT_NAME_field.setIsIdentity(false);
        table.addField(DEPT_NAME_field);
    
        FieldDefinition DEPT_ROLE_field = new FieldDefinition();
        DEPT_ROLE_field.setName("DEPT_ROLE");
        DEPT_ROLE_field.setTypeName("VARCHAR");
        DEPT_ROLE_field.setSize(40);
        DEPT_ROLE_field.setShouldAllowNull(true);
        DEPT_ROLE_field.setIsPrimaryKey(false);
        DEPT_ROLE_field.setUnique(false);
        DEPT_ROLE_field.setIsIdentity(false);
        table.addField(DEPT_ROLE_field);
    
        FieldDefinition DEPT_LOCATION_field = new FieldDefinition();
        DEPT_LOCATION_field.setName("DEPT_LOCATION");
        DEPT_LOCATION_field.setTypeName("VARCHAR");
        DEPT_LOCATION_field.setSize(40);
        DEPT_LOCATION_field.setShouldAllowNull(true);
        DEPT_LOCATION_field.setIsPrimaryKey(false);
        DEPT_LOCATION_field.setUnique(false);
        DEPT_LOCATION_field.setIsIdentity(false);
        table.addField(DEPT_LOCATION_field);
        
        FieldDefinition fieldDTYPE = new FieldDefinition();
        fieldDTYPE.setName("DTYPE");
        fieldDTYPE.setTypeName("VARCHAR2");
        fieldDTYPE.setSize(15);
        fieldDTYPE.setSubSize(0);
        fieldDTYPE.setIsPrimaryKey(false);
        fieldDTYPE.setIsIdentity(false);
        fieldDTYPE.setUnique(false);
        fieldDTYPE.setShouldAllowNull(true);
        table.addField(fieldDTYPE);
       
        return table;
    }
    
    public static TableDefinition buildDEPARTMENTTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_DEPARTMENT");

        FieldDefinition NAME_field = new FieldDefinition();
        NAME_field.setName("NAME");
        NAME_field.setTypeName("VARCHAR");
        NAME_field.setSize(40);
        NAME_field.setShouldAllowNull(false);
        NAME_field.setIsPrimaryKey(true);
        NAME_field.setUnique(false);
        NAME_field.setIsIdentity(true);
        table.addField(NAME_field);
    
        FieldDefinition ROLE_field = new FieldDefinition();
        ROLE_field.setName("ROLE");
        ROLE_field.setTypeName("VARCHAR");
        ROLE_field.setSize(40);
        ROLE_field.setShouldAllowNull(false);
        ROLE_field.setIsPrimaryKey(true);
        ROLE_field.setUnique(false);
        ROLE_field.setIsIdentity(true);
        table.addField(ROLE_field);
    
        FieldDefinition LOCATION_field = new FieldDefinition();
        LOCATION_field.setName("LOCATION");
        LOCATION_field.setTypeName("VARCHAR");
        LOCATION_field.setSize(40);
        LOCATION_field.setShouldAllowNull(false);
        LOCATION_field.setIsPrimaryKey(true);
        LOCATION_field.setUnique(false);
        LOCATION_field.setIsIdentity(true);
        table.addField(LOCATION_field);

        return table;
    }

    public static TableDefinition buildCUBICLETable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_CUBICLE");

        FieldDefinition ID_field = new FieldDefinition();
        ID_field.setName("ID");
        ID_field.setTypeName("NUMERIC");
        ID_field.setSize(15);
        ID_field.setShouldAllowNull(false);
        ID_field.setIsPrimaryKey(true);
        ID_field.setUnique(false);
        ID_field.setIsIdentity(true);
        table.addField(ID_field);
    
        FieldDefinition CODE_field = new FieldDefinition();
        CODE_field.setName("CODE");
        CODE_field.setTypeName("VARCHAR");
        CODE_field.setSize(1);
        CODE_field.setShouldAllowNull(false);
        CODE_field.setIsPrimaryKey(true);
        CODE_field.setUnique(false);
        CODE_field.setIsIdentity(true);
        table.addField(CODE_field);
    
        return table;
    }
}
