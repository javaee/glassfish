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


package oracle.toplink.essentials.testing.models.cmp3.advanced;

import oracle.toplink.essentials.tools.schemaframework.*;

public class AdvancedTableCreator extends oracle.toplink.essentials.tools.schemaframework.TableCreator {
    public AdvancedTableCreator() {
        setName("EJB3EmployeeProject");

        addTableDefinition(buildADDRESSTable());
        addTableDefinition(buildEMPLOYEETable());
//        addTableDefinition(buildEMPLOYEE_SEQTable());
        addTableDefinition(buildLARGEPROJECTTable());
        addTableDefinition(buildMANTable());
        addTableDefinition(buildWOMANTable());
        addTableDefinition(buildPARTNERLINKTable());
        addTableDefinition(buildPHONENUMBERTable());
        addTableDefinition(buildPROJECTTable());
        addTableDefinition(buildPROJECT_EMPTable());
        addTableDefinition(buildSALARYTable());
        addTableDefinition(buildPLATINUMBUYERTable());
        addTableDefinition(buildBUYERTable());
        addTableDefinition(buildDEPTTable());
        addTableDefinition(buildDEPT_EMPTable());
        addTableDefinition(buildGOFLERTable());
        addTableDefinition(buildWORLDRANKTable());
        addTableDefinition(buildVEGETABLETable());
    }
    
    public static TableDefinition buildADDRESSTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_ADDRESS");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("ADDRESS_ID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition fieldSTREET = new FieldDefinition();
        fieldSTREET.setName("STREET");
        fieldSTREET.setTypeName("VARCHAR2");
        fieldSTREET.setSize(60);
        fieldSTREET.setSubSize(0);
        fieldSTREET.setIsPrimaryKey(false);
        fieldSTREET.setIsIdentity(false);
        fieldSTREET.setUnique(false);
        fieldSTREET.setShouldAllowNull(true);
        table.addField(fieldSTREET);

        FieldDefinition fieldCITY = new FieldDefinition();
        fieldCITY.setName("CITY");
        fieldCITY.setTypeName("VARCHAR2");
        fieldCITY.setSize(60);
        fieldCITY.setSubSize(0);
        fieldCITY.setIsPrimaryKey(false);
        fieldCITY.setIsIdentity(false);
        fieldCITY.setUnique(false);
        fieldCITY.setShouldAllowNull(true);
        table.addField(fieldCITY);

        FieldDefinition fieldPROVINCE = new FieldDefinition();
        fieldPROVINCE.setName("PROVINCE");
        fieldPROVINCE.setTypeName("VARCHAR2");
        fieldPROVINCE.setSize(60);
        fieldPROVINCE.setSubSize(0);
        fieldPROVINCE.setIsPrimaryKey(false);
        fieldPROVINCE.setIsIdentity(false);
        fieldPROVINCE.setUnique(false);
        fieldPROVINCE.setShouldAllowNull(true);
        table.addField(fieldPROVINCE);

        FieldDefinition fieldPOSTALCODE = new FieldDefinition();
        fieldPOSTALCODE.setName("P_CODE");
        fieldPOSTALCODE.setTypeName("VARCHAR2");
        fieldPOSTALCODE.setSize(67);
        fieldPOSTALCODE.setSubSize(0);
        fieldPOSTALCODE.setIsPrimaryKey(false);
        fieldPOSTALCODE.setIsIdentity(false);
        fieldPOSTALCODE.setUnique(false);
        fieldPOSTALCODE.setShouldAllowNull(true);
        table.addField(fieldPOSTALCODE);

        FieldDefinition fieldCOUNTRY = new FieldDefinition();
        fieldCOUNTRY.setName("COUNTRY");
        fieldCOUNTRY.setTypeName("VARCHAR2");
        fieldCOUNTRY.setSize(60);
        fieldCOUNTRY.setSubSize(0);
        fieldCOUNTRY.setIsPrimaryKey(false);
        fieldCOUNTRY.setIsIdentity(false);
        fieldCOUNTRY.setUnique(false);
        fieldCOUNTRY.setShouldAllowNull(true);
        table.addField(fieldCOUNTRY);

        return table;
    }

    public static TableDefinition buildEMPLOYEETable() {
        TableDefinition table = new TableDefinition();
        // SECTION: TABLE
        table.setName("CMP3_EMPLOYEE");
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field.setName("EMP_ID");
        field.setTypeName("NUMERIC");
        field.setSize(15);
        field.setShouldAllowNull(false );
        field.setIsPrimaryKey(true );
        field.setUnique(false );
        field.setIsIdentity(true );
        table.addField(field);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field1 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field1.setName("F_NAME");
        field1.setTypeName("VARCHAR");
        field1.setSize(40);
        field1.setShouldAllowNull(true );
        field1.setIsPrimaryKey(false );
        field1.setUnique(false );
        field1.setIsIdentity(false );
        table.addField(field1);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field2 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field2.setName("L_NAME");
        field2.setTypeName("VARCHAR");
        field2.setSize(40);
        field2.setShouldAllowNull(true );
        field2.setIsPrimaryKey(false );
        field2.setUnique(false );
        field2.setIsIdentity(false );
        table.addField(field2);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field3 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field3.setName("START_DATE");
        field3.setTypeName("DATE");
        field3.setSize(23);
        field3.setShouldAllowNull(true );
        field3.setIsPrimaryKey(false );
        field3.setUnique(false );
        field3.setIsIdentity(false );
        table.addField(field3);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field4 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field4.setName("END_DATE");
        field4.setTypeName("DATE");
        field4.setSize(23);
        field4.setShouldAllowNull(true );
        field4.setIsPrimaryKey(false );
        field4.setUnique(false );
        field4.setIsIdentity(false );
        table.addField(field4);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field8 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field8.setName("ADDR_ID");
        field8.setTypeName("NUMERIC");
        field8.setSize(15);
        field8.setShouldAllowNull(true );
        field8.setIsPrimaryKey(false );
        field8.setUnique(false );
        field8.setIsIdentity(false );
        field8.setForeignKeyFieldName("CMP3_ADDRESS.ADDRESS_ID");
        table.addField(field8);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field9 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field9.setName("MANAGER_EMP_ID");
        field9.setTypeName("NUMERIC");
        field9.setSize(15);
        field9.setShouldAllowNull(true );
        field9.setIsPrimaryKey(false );
        field9.setUnique(false );
        field9.setIsIdentity(false );
        field9.setForeignKeyFieldName("CMP3_EMPLOYEE.EMP_ID");
        table.addField(field9);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field10 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field10.setName("VERSION");
        field10.setTypeName("NUMERIC");
        field10.setSize(15);
        field10.setShouldAllowNull(true );
        field10.setIsPrimaryKey(false );
        field10.setUnique(false );
        field10.setIsIdentity(false );
        table.addField(field10);
        
        // SECTION: FIELD
        FieldDefinition fieldDEPT = new FieldDefinition();
        fieldDEPT.setName("DEPT_ID");
        fieldDEPT.setTypeName("NUMERIC");
        fieldDEPT.setSize(15);
        fieldDEPT.setShouldAllowNull(true);
        fieldDEPT.setIsPrimaryKey(false);
        fieldDEPT.setUnique(false);
        fieldDEPT.setIsIdentity(false);
        fieldDEPT.setForeignKeyFieldName("CMP3_DEPT.ID");
        table.addField(fieldDEPT);

        // SECTION: FIELD
        FieldDefinition fieldStatus = new FieldDefinition();
        fieldStatus.setName("STATUS");
        fieldStatus.setTypeName("NUMERIC");
        fieldStatus.setSize(15);
        fieldStatus.setIsPrimaryKey(false);
        fieldStatus.setUnique(false);
        fieldStatus.setIsIdentity(false);
        fieldStatus.setShouldAllowNull(true);
        table.addField(fieldStatus);
        
        // SECTION: FIELD
        FieldDefinition fieldPayScale = new FieldDefinition();
        fieldPayScale.setName("PAY_SCALE");
        fieldPayScale.setTypeName("VARCHAR");
        fieldPayScale.setSize(40);
        fieldPayScale.setIsPrimaryKey(false);
        fieldPayScale.setUnique(false);
        fieldPayScale.setIsIdentity(false);
        fieldPayScale.setShouldAllowNull(true);
        table.addField(fieldPayScale);
        
        // SECTION: FIELD
        FieldDefinition fieldRoomNumber = new FieldDefinition();
        fieldRoomNumber.setName("ROOM_NUM");
        fieldRoomNumber.setTypeName("NUMBER");
        fieldRoomNumber.setSize(15);
        fieldRoomNumber.setSubSize(0);
        fieldRoomNumber.setIsPrimaryKey(false);
        fieldRoomNumber.setIsIdentity(false);
        fieldRoomNumber.setUnique(false);
        fieldRoomNumber.setShouldAllowNull(true);
        table.addField(fieldRoomNumber);
        
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition fieldFormerCompany = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        fieldFormerCompany.setName("FORMER_COMPANY");
        fieldFormerCompany.setTypeName("VARCHAR");
        fieldFormerCompany.setSize(40);
        fieldFormerCompany.setShouldAllowNull(true );
        fieldFormerCompany.setIsPrimaryKey(false );
        fieldFormerCompany.setUnique(false );
        fieldFormerCompany.setIsIdentity(false );
        table.addField(fieldFormerCompany);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition fieldFormerStartDate = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        fieldFormerStartDate.setName("FORMER_START_DATE");
        fieldFormerStartDate.setTypeName("DATE");
        fieldFormerStartDate.setSize(23);
        fieldFormerStartDate.setShouldAllowNull(true );
        fieldFormerStartDate.setIsPrimaryKey(false );
        fieldFormerStartDate.setUnique(false );
        fieldFormerStartDate.setIsIdentity(false );
        table.addField(fieldFormerStartDate);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition fieldFormerEndDate = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        fieldFormerEndDate.setName("FORMER_END_DATE");
        fieldFormerEndDate.setTypeName("DATE");
        fieldFormerEndDate.setSize(23);
        fieldFormerEndDate.setShouldAllowNull(true );
        fieldFormerEndDate.setIsPrimaryKey(false );
        fieldFormerEndDate.setUnique(false );
        fieldFormerEndDate.setIsIdentity(false );
        table.addField(fieldFormerEndDate);

/*        ForeignKeyConstraint foreignKeyEMPLOYEE_ADDRESS = new ForeignKeyConstraint();
        foreignKeyEMPLOYEE_ADDRESS.setName("EMPLOYEE_ADDRESS");
        foreignKeyEMPLOYEE_ADDRESS.setTargetTable("ADDRESS");
        foreignKeyEMPLOYEE_ADDRESS.addSourceField("ADDR_ID");
        foreignKeyEMPLOYEE_ADDRESS.addTargetField("ID");
        table.addForeignKeyConstraint(foreignKeyEMPLOYEE_ADDRESS);

        ForeignKeyConstraint foreignKeyEMPLOYEE_MANAGER = new ForeignKeyConstraint();
        foreignKeyEMPLOYEE_MANAGER.setName("EMPLOYEE_MANAGER");
        foreignKeyEMPLOYEE_MANAGER.setTargetTable("CMP3_EMPLOYEE");
        foreignKeyEMPLOYEE_MANAGER.addSourceField("MANAGER_ID");
        foreignKeyEMPLOYEE_MANAGER.addTargetField("EMP_ID");
        table.addForeignKeyConstraint(foreignKeyEMPLOYEE_MANAGER);

 */       return table;
    }

    public static TableDefinition buildGOFLERTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_GOLFER");
        
        FieldDefinition IDfield = new FieldDefinition();
        IDfield.setName("ID");
        IDfield.setTypeName("NUMERIC");
        IDfield.setSize(15);
        IDfield.setShouldAllowNull(false);
        IDfield.setIsPrimaryKey(true);
        IDfield.setUnique(false);
        IDfield.setIsIdentity(true);
        table.addField(IDfield);
        
        return table;
    }

    public static TableDefinition buildEMPLOYEE_SEQTable() {
        TableDefinition table = new TableDefinition();
//        table.setName("SEQUENCE");
        table.setName("CMP3_EMPLOYEE_SEQ");

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
    public static TableDefinition buildLARGEPROJECTTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_LPROJECT");

        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field.setName("PROJ_ID");
        field.setTypeName("NUMERIC");
        field.setSize(15);
        field.setShouldAllowNull(false );
        field.setIsPrimaryKey(true );
        field.setUnique(false );
        field.setIsIdentity(false );
        field.setForeignKeyFieldName("CMP3_PROJECT.PROJ_ID");
        table.addField(field);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field1 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field1.setName("BUDGET");
        field1.setTypeName("DOUBLE PRECIS");
        field1.setSize(32);
        field1.setShouldAllowNull(true );
        field1.setIsPrimaryKey(false );
        field1.setUnique(false );
        field1.setIsIdentity(false );
        table.addField(field1);
    
        return table;
    }
    
    public static TableDefinition buildMANTable() {
        TableDefinition table = new TableDefinition();
        table.setName("MAN");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("ID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition field1 = new FieldDefinition();
        field1.setName("F_NAME");
        field1.setTypeName("VARCHAR");
        field1.setSize(40);
        field1.setShouldAllowNull(true);
        field1.setIsPrimaryKey(false);
        field1.setUnique(false);
        field1.setIsIdentity(false);
        table.addField(field1);
    
        FieldDefinition field2 = new FieldDefinition();
        field2.setName("L_NAME");
        field2.setTypeName("VARCHAR");
        field2.setSize(40);
        field2.setShouldAllowNull(true);
        field2.setIsPrimaryKey(false);
        field2.setUnique(false);
        field2.setIsIdentity(false);
        table.addField(field2);        

        return table;
    }
    
    public static TableDefinition buildPARTNERLINKTable() {
        TableDefinition table = new TableDefinition();
        table.setName("MW");

        FieldDefinition fieldMID = new FieldDefinition();
        fieldMID.setName("M");
        fieldMID.setTypeName("NUMERIC");
        fieldMID.setSize(15);
        fieldMID.setSubSize(0);
        fieldMID.setIsPrimaryKey(false);
        fieldMID.setIsIdentity(false);
        fieldMID.setUnique(false);
        fieldMID.setShouldAllowNull(true);
        fieldMID.setForeignKeyFieldName("MAN.ID");
        table.addField(fieldMID);
        
        FieldDefinition fieldWID = new FieldDefinition();
        fieldWID.setName("W");
        fieldWID.setTypeName("NUMERIC");
        fieldWID.setSize(15);
        fieldWID.setSubSize(0);
        fieldWID.setIsPrimaryKey(false);
        fieldWID.setIsIdentity(false);
        fieldWID.setUnique(false);
        fieldWID.setShouldAllowNull(true);
        fieldWID.setForeignKeyFieldName("WOMAN.ID");
        table.addField(fieldWID);
        
        return table;
    }

    public static TableDefinition buildPHONENUMBERTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_PHONENUMBER");

        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field.setName("OWNER_ID");
        field.setTypeName("NUMERIC");
        field.setSize(15);
        field.setShouldAllowNull(false );
        field.setIsPrimaryKey(true );
        field.setUnique(false );
        field.setIsIdentity(false );
        field.setForeignKeyFieldName("CMP3_EMPLOYEE.EMP_ID");
        table.addField(field);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field1 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field1.setName("TYPE");
        field1.setTypeName("VARCHAR");
        field1.setSize(15);
        field1.setShouldAllowNull(false );
        field1.setIsPrimaryKey(true );
        field1.setUnique(false );
        field1.setIsIdentity(false );
        table.addField(field1);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field2 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field2.setName("AREA_CODE");
        field2.setTypeName("VARCHAR");
        field2.setSize(3);
        field2.setShouldAllowNull(true );
        field2.setIsPrimaryKey(false );
        field2.setUnique(false );
        field2.setIsIdentity(false );
        table.addField(field2);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field3 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field3.setName("NUMB");
        field3.setTypeName("VARCHAR");
        field3.setSize(8);
        field3.setShouldAllowNull(true );
        field3.setIsPrimaryKey(false );
        field3.setUnique(false );
        field3.setIsIdentity(false );
        table.addField(field3);

 /*       ForeignKeyConstraint foreignKeyPHONE_OWNER = new ForeignKeyConstraint();
        foreignKeyPHONE_OWNER.setName("PHONE_OWNER");
        foreignKeyPHONE_OWNER.setTargetTable("CMP3_EMPLOYEE");
        foreignKeyPHONE_OWNER.addSourceField("EMP_ID");
        foreignKeyPHONE_OWNER.addTargetField("EMP_ID");
        table.addForeignKeyConstraint(foreignKeyPHONE_OWNER);
*/
        return table;
    }

    public static TableDefinition buildPROJECTTable() {
        TableDefinition table = new TableDefinition();

        table.setName("CMP3_PROJECT");

        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field.setName("PROJ_ID");
        field.setTypeName("NUMERIC");
        field.setSize(15);
        field.setShouldAllowNull(false );
        field.setIsPrimaryKey(true );
        field.setUnique(false );
        field.setIsIdentity(true );
        table.addField(field);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field1 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field1.setName("PROJ_TYPE");
        field1.setTypeName("VARCHAR");
        field1.setSize(1);
        field1.setShouldAllowNull(true );
        field1.setIsPrimaryKey(false );
        field1.setUnique(false );
        field1.setIsIdentity(false );
        table.addField(field1);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field2 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field2.setName("PROJ_NAME");
        field2.setTypeName("VARCHAR");
        field2.setSize(30);
        field2.setShouldAllowNull(true );
        field2.setIsPrimaryKey(false );
        field2.setUnique(false );
        field2.setIsIdentity(false );
        table.addField(field2);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field3 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field3.setName("DESCRIP");
        field3.setTypeName("VARCHAR");
        field3.setSize(200);
        field3.setShouldAllowNull(true );
        field3.setIsPrimaryKey(false );
        field3.setUnique(false );
        field3.setIsIdentity(false );
        table.addField(field3);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field4 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field4.setName("LEADER_ID");
        field4.setTypeName("NUMERIC");
        field4.setSize(15);
        field4.setShouldAllowNull(true );
        field4.setIsPrimaryKey(false );
        field4.setUnique(false );
        field4.setIsIdentity(false );
        field4.setForeignKeyFieldName("CMP3_EMPLOYEE.EMP_ID");
        table.addField(field4);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field5 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field5.setName("VERSION");
        field5.setTypeName("NUMERIC");
        field5.setSize(15);
        field5.setShouldAllowNull(true );
        field5.setIsPrimaryKey(false );
        field5.setUnique(false );
        field5.setIsIdentity(false );
        table.addField(field5);

/*        ForeignKeyConstraint foreignKeyPROJECT_LEADER = new ForeignKeyConstraint();
        foreignKeyPROJECT_LEADER.setName("PROJECT_LEADER");
        foreignKeyPROJECT_LEADER.setTargetTable("CMP3_EMPLOYEE");
        foreignKeyPROJECT_LEADER.addSourceField("LEADER_ID");
        foreignKeyPROJECT_LEADER.addTargetField("EMP_ID");
        table.addForeignKeyConstraint(foreignKeyPROJECT_LEADER);
*/
        return table;
    }

    public static TableDefinition buildPROJECT_EMPTable() {
        TableDefinition table = new TableDefinition();

        table.setName("CMP3_EMP_PROJ");

        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field.setName("EMPLOYEES_EMP_ID");
        field.setTypeName("NUMERIC");
        field.setSize(15);
        field.setShouldAllowNull(false );
        field.setIsPrimaryKey(true );
        field.setUnique(false );
        field.setIsIdentity(false );
        field.setForeignKeyFieldName("CMP3_EMPLOYEE.EMP_ID");
        table.addField(field);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field1 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field1.setName("projects_PROJ_ID");
        field1.setTypeName("NUMERIC");
        field1.setSize(15);
        field1.setShouldAllowNull(false );
        field1.setIsPrimaryKey(true );
        field1.setUnique(false );
        field1.setIsIdentity(false );
        field1.setForeignKeyFieldName("CMP3_PROJECT.PROJ_ID");
        table.addField(field1);

/*        ForeignKeyConstraint foreignKeyPROJECT_EMPLOYEE = new ForeignKeyConstraint();
        foreignKeyPROJECT_EMPLOYEE.setName("PROJECT_EMPLOYEE");
        foreignKeyPROJECT_EMPLOYEE.setTargetTable("CMP3_EMPLOYEE");
        foreignKeyPROJECT_EMPLOYEE.addSourceField("EMP_ID");
        foreignKeyPROJECT_EMPLOYEE.addTargetField("EMP_ID");
        table.addForeignKeyConstraint(foreignKeyPROJECT_EMPLOYEE);

        ForeignKeyConstraint foreignKeyEMPLOYEE_PROJECT = new ForeignKeyConstraint();
        foreignKeyEMPLOYEE_PROJECT.setName("EMPLOYEE_PROJECT");
        foreignKeyEMPLOYEE_PROJECT.setTargetTable("CMP3_PROJECT");
        foreignKeyEMPLOYEE_PROJECT.addSourceField("EMP_ID");
        foreignKeyEMPLOYEE_PROJECT.addTargetField("EMP_ID");
        table.addForeignKeyConstraint(foreignKeyEMPLOYEE_PROJECT);
*/
        return table;
    }

    public static TableDefinition buildSALARYTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_SALARY");

        FieldDefinition fieldEMP_ID = new FieldDefinition();
        fieldEMP_ID.setName("EMP_ID");
        fieldEMP_ID.setTypeName("NUMERIC");
        fieldEMP_ID.setSize(15);
        fieldEMP_ID.setSubSize(0);
        fieldEMP_ID.setIsPrimaryKey(true);
        fieldEMP_ID.setIsIdentity(false);
        fieldEMP_ID.setUnique(false);
        fieldEMP_ID.setShouldAllowNull(false);
        fieldEMP_ID.setForeignKeyFieldName("CMP3_EMPLOYEE.EMP_ID");
        table.addField(fieldEMP_ID);

        FieldDefinition fieldSALARY = new FieldDefinition();
        fieldSALARY.setName("SALARY");
        fieldSALARY.setTypeName("NUMBER");
        fieldSALARY.setSize(15);
        fieldSALARY.setSubSize(0);
        fieldSALARY.setIsPrimaryKey(false);
        fieldSALARY.setIsIdentity(false);
        fieldSALARY.setUnique(false);
        fieldSALARY.setShouldAllowNull(true);
        table.addField(fieldSALARY);

        return table;
    }

    public static TableDefinition buildPLATINUMBUYERTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_PBUYER");

        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field.setName("BUYER_ID");
        field.setTypeName("NUMERIC");
        field.setSize(15);
        field.setShouldAllowNull(false );
        field.setIsPrimaryKey(true );
        field.setUnique(false );
        field.setIsIdentity(false );
        field.setForeignKeyFieldName("CMP3_BUYER.BUYER_ID");
        table.addField(field);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field1 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field1.setName("PURCHASES");
        field1.setTypeName("DOUBLE PRECIS");
        field1.setSize(32);
        field1.setShouldAllowNull(true );
        field1.setIsPrimaryKey(false );
        field1.setUnique(false );
        field1.setIsIdentity(false );
        table.addField(field1);
    
        return table;
    }

    public static TableDefinition buildBUYERTable() {
        TableDefinition table = new TableDefinition();

        table.setName("CMP3_BUYER");
        
        String[] unq1 = {"BUYER_ID", "BUYER_NAME"};
        String[] unq2 = {"BUYER_ID", "DESCRIP"};
        table.addUniqueKeyConstraint("UNQ_CMP3_BUYER_1", unq1);
        table.addUniqueKeyConstraint("UNQ_CMP3_BUYER_2", unq2);
 
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field.setName("BUYER_ID");
        field.setTypeName("NUMERIC");
        field.setSize(15);
        field.setShouldAllowNull(false );
        field.setIsPrimaryKey(true );
        field.setUnique(false );
        field.setIsIdentity(true );
        table.addField(field);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field2 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field2.setName("BUYER_NAME");
        field2.setTypeName("VARCHAR");
        field2.setSize(30);
        field2.setShouldAllowNull(false);
        field2.setIsPrimaryKey(false );
        field2.setUnique(false );
        field2.setIsIdentity(false );
        table.addField(field2);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field3 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field3.setName("DESCRIP");
        field3.setTypeName("VARCHAR");
        field3.setSize(200);
        field3.setShouldAllowNull(false);
        field3.setIsPrimaryKey(false );
        field3.setUnique(false );
        field3.setIsIdentity(false );
        table.addField(field3);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field4 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field4.setName("DTYPE");
        field4.setTypeName("VARCHAR");
        field4.setSize(200);
        field4.setShouldAllowNull(true );
        field4.setIsPrimaryKey(false );
        field4.setUnique(false );
        field4.setIsIdentity(false );
        table.addField(field4);
    
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition field5 = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        field5.setName("VERSION");
        field5.setTypeName("NUMERIC");
        field5.setSize(15);
        field5.setShouldAllowNull(true );
        field5.setIsPrimaryKey(false );
        field5.setUnique(false );
        field5.setIsIdentity(false );
        table.addField(field5);

        return table;
    }

    public static TableDefinition buildVEGETABLETable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_VEGETABLE");

        FieldDefinition fieldNAME = new FieldDefinition();
        fieldNAME.setName("VEGETABLE_NAME");
        fieldNAME.setTypeName("VARCHAR");
        fieldNAME.setSize(30);
        fieldNAME.setIsPrimaryKey(true);
        table.addField(fieldNAME);
        
        FieldDefinition fieldCOLOR = new FieldDefinition();
        fieldCOLOR.setName("VEGETABLE_COLOR");
        fieldCOLOR.setTypeName("VARCHAR");
        fieldCOLOR.setSize(30);
        fieldCOLOR.setIsPrimaryKey(true);
        table.addField(fieldCOLOR);
        
        FieldDefinition fieldCOST = new FieldDefinition();
        fieldCOST.setName("COST");
        fieldCOST.setTypeName("DOUBLE PRECIS");
        fieldCOST.setSize(32);
        table.addField(fieldCOST);
        
        FieldDefinition fieldTAGS = new FieldDefinition();
        fieldTAGS.setName("TAGS");
        fieldTAGS.setTypeName("BLOB");
        table.addField(fieldTAGS);
        return table;
    }
    
    public static TableDefinition buildWOMANTable() {
        TableDefinition table = new TableDefinition();
        table.setName("WOMAN");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("ID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition field1 = new FieldDefinition();
        field1.setName("F_NAME");
        field1.setTypeName("VARCHAR");
        field1.setSize(40);
        field1.setShouldAllowNull(true);
        field1.setIsPrimaryKey(false);
        field1.setUnique(false);
        field1.setIsIdentity(false);
        table.addField(field1);
    
        FieldDefinition field2 = new FieldDefinition();
        field2.setName("L_NAME");
        field2.setTypeName("VARCHAR");
        field2.setSize(40);
        field2.setShouldAllowNull(true);
        field2.setIsPrimaryKey(false);
        field2.setUnique(false);
        field2.setIsIdentity(false);
        table.addField(field2);        
        
        return table;
    }
    
    public static TableDefinition buildWORLDRANKTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_WORLDRANK");
        
        FieldDefinition IDfield = new FieldDefinition();
        IDfield.setName("ID");
        IDfield.setTypeName("NUMERIC");
        IDfield.setSize(15);
        IDfield.setShouldAllowNull(false);
        IDfield.setIsPrimaryKey(true);
        IDfield.setUnique(false );
        IDfield.setIsIdentity(true);
        table.addField(IDfield);
        
        return table;
    }
    
    public static TableDefinition buildDEPTTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_DEPT");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("ID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition fieldNAME = new FieldDefinition();
        fieldNAME.setName("NAME");
        fieldNAME.setTypeName("VARCHAR2");
        fieldNAME.setSize(60);
        fieldNAME.setSubSize(0);
        fieldNAME.setIsPrimaryKey(false);
        fieldNAME.setIsIdentity(false);
        fieldNAME.setUnique(false);
        fieldNAME.setShouldAllowNull(true);
        table.addField(fieldNAME);

        return table;
    }
    
    public static TableDefinition buildDEPT_EMPTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_DEPT_CMP3_EMPLOYEE");

        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition fieldID = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        fieldID.setName("ADV_DEPT_ID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setShouldAllowNull(false);
        fieldID.setIsPrimaryKey(true);
        fieldID.setUnique(false);
        fieldID.setIsIdentity(false);
        fieldID.setForeignKeyFieldName("CMP3_DEPT.ID");
        table.addField(fieldID);
        
        // SECTION: FIELD
        oracle.toplink.essentials.tools.schemaframework.FieldDefinition fieldEMP = new oracle.toplink.essentials.tools.schemaframework.FieldDefinition();
        fieldEMP.setName("MANAGERS_EMP_ID");
        fieldEMP.setTypeName("NUMERIC");
        fieldEMP.setSize(15);
        fieldEMP.setShouldAllowNull(false);
        fieldEMP.setIsPrimaryKey(true);
        fieldEMP.setUnique(false);
        fieldEMP.setIsIdentity(false);
        fieldEMP.setForeignKeyFieldName("CMP3_EMPLOYEE.EMP_ID");
        table.addField(fieldEMP);
        
        return table;   
    }

}
