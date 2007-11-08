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

package oracle.toplink.essentials.testing.models.cmp3.inherited;

import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.tools.schemaframework.TableCreator;
import oracle.toplink.essentials.tools.schemaframework.TableDefinition;
import oracle.toplink.essentials.tools.schemaframework.FieldDefinition;

public class InheritedTableManager extends TableCreator {
    public static TableCreator tableCreator;

    public InheritedTableManager() {
        setName("EJB3BeerProject");

        addTableDefinition(build_ALPINE_Table());
        addTableDefinition(build_BEER_CONSUMER_Table());
        addTableDefinition(build_BEVERAGE_SEQUENCE_Table());
        addTableDefinition(build_BLUE_Table());
        addTableDefinition(build_CANADIAN_Table());
        addTableDefinition(build_CERTIFICATION_Table());
        addTableDefinition(build_SERIALNUMBER_Table());
        addTableDefinition(build_TELEPHONE_NUMBER_Table());
    }
    
    public static TableDefinition build_ALPINE_Table() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_ALPINE");
    
        FieldDefinition ID_field = new FieldDefinition();
        ID_field.setName("ID");
        ID_field.setTypeName("NUMERIC");
        ID_field.setSize(15);
        ID_field.setIsPrimaryKey(true);
        ID_field.setUnique(false);
        ID_field.setIsIdentity(false);
        ID_field.setShouldAllowNull(false);
        table.addField(ID_field);
    
        FieldDefinition ALCOHOL_CONTENT_field = new FieldDefinition();
        ALCOHOL_CONTENT_field.setName("ALCOHOL_CONTENT");
        ALCOHOL_CONTENT_field.setTypeName("DOUBLE PRECIS");
        ALCOHOL_CONTENT_field.setSize(15);
        ALCOHOL_CONTENT_field.setIsPrimaryKey(false);
        ALCOHOL_CONTENT_field.setUnique(false);
        ALCOHOL_CONTENT_field.setIsIdentity(false);
        ALCOHOL_CONTENT_field.setShouldAllowNull(true);
        table.addField(ALCOHOL_CONTENT_field);
        
        FieldDefinition BEST_BEFORE_DATE_field = new FieldDefinition();
        BEST_BEFORE_DATE_field.setName("BB_DATE");
        BEST_BEFORE_DATE_field.setTypeName("DATETIME");
        BEST_BEFORE_DATE_field.setSize(23);
        BEST_BEFORE_DATE_field.setIsPrimaryKey(false);
        BEST_BEFORE_DATE_field.setUnique(false);
        BEST_BEFORE_DATE_field.setIsIdentity(false);
        BEST_BEFORE_DATE_field.setShouldAllowNull(true);
        table.addField(BEST_BEFORE_DATE_field);
        
        FieldDefinition FLAVOR_field = new FieldDefinition();
        FLAVOR_field.setName("CLASSIFICATION");
        FLAVOR_field.setTypeName("NUMERIC");
        FLAVOR_field.setSize(15);
        FLAVOR_field.setIsPrimaryKey(false);
        FLAVOR_field.setUnique(false);
        FLAVOR_field.setIsIdentity(false);
        FLAVOR_field.setShouldAllowNull(true);
        table.addField(FLAVOR_field);
        
        FieldDefinition BEER_CONSUMER_ID_field = new FieldDefinition();
        BEER_CONSUMER_ID_field.setName("C_ID");
        BEER_CONSUMER_ID_field.setTypeName("NUMERIC");
        BEER_CONSUMER_ID_field.setSize(15);
        BEER_CONSUMER_ID_field.setIsPrimaryKey(false);
        BEER_CONSUMER_ID_field.setUnique(false);
        BEER_CONSUMER_ID_field.setIsIdentity(false);
        BEER_CONSUMER_ID_field.setShouldAllowNull(true);
        BEER_CONSUMER_ID_field.setForeignKeyFieldName("CMP3_CONSUMER.ID");
        table.addField(BEER_CONSUMER_ID_field);
        
        FieldDefinition VERSION_field = new FieldDefinition();
        VERSION_field.setName("VERSION");
        VERSION_field.setTypeName("DATETIME");
        VERSION_field.setSize(23);
        VERSION_field.setIsPrimaryKey(false);
        VERSION_field.setUnique(false);
        VERSION_field.setIsIdentity(false);
        VERSION_field.setShouldAllowNull(true);
        table.addField(VERSION_field);
        
        FieldDefinition fieldINSPECTIONDATES = new FieldDefinition();
        fieldINSPECTIONDATES.setName("I_DATES");
        fieldINSPECTIONDATES.setTypeName("LONG RAW");
        fieldINSPECTIONDATES.setSize(100);
        fieldINSPECTIONDATES.setSubSize(0);
        fieldINSPECTIONDATES.setIsPrimaryKey(false);
        fieldINSPECTIONDATES.setIsIdentity(false);
        fieldINSPECTIONDATES.setUnique(false);
        fieldINSPECTIONDATES.setShouldAllowNull(true);
        table.addField(fieldINSPECTIONDATES);

        return table;
    }
    
    public static TableDefinition build_BEER_CONSUMER_Table() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_CONSUMER");
    
        FieldDefinition ID_field = new FieldDefinition();
        ID_field.setName("ID");
        ID_field.setTypeName("NUMERIC");
        ID_field.setSize(15);
        ID_field.setIsPrimaryKey(true);
        ID_field.setUnique(false);
        ID_field.setIsIdentity(false);
        ID_field.setShouldAllowNull(false);
        table.addField(ID_field);

        FieldDefinition NAME_field = new FieldDefinition();
        NAME_field.setName("NAME");
        NAME_field.setTypeName("VARCHAR");
        NAME_field.setSize(40);
        NAME_field.setShouldAllowNull(true);
        NAME_field.setIsPrimaryKey(false);
        NAME_field.setUnique(false);
        NAME_field.setIsIdentity(false);
        table.addField(NAME_field);

        return table;
    }
    
    public static TableDefinition build_BEVERAGE_SEQUENCE_Table() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_BEVERAGE_SEQ");

        FieldDefinition SEQ_COUNT_field = new FieldDefinition();
        SEQ_COUNT_field.setName("SEQ_COUNT");
        SEQ_COUNT_field.setTypeName("NUMERIC");
        SEQ_COUNT_field.setSize(15);
        SEQ_COUNT_field.setSubSize(0);
        SEQ_COUNT_field.setIsPrimaryKey(false);
        SEQ_COUNT_field.setIsIdentity(false);
        SEQ_COUNT_field.setUnique(false);
        SEQ_COUNT_field.setShouldAllowNull(false);
        table.addField(SEQ_COUNT_field);

        FieldDefinition SEQ_NAME_field = new FieldDefinition();
        SEQ_NAME_field.setName("SEQ_NAME");
        SEQ_NAME_field.setTypeName("VARCHAR");
        SEQ_NAME_field.setSize(80);
        SEQ_NAME_field.setSubSize(0);
        SEQ_NAME_field.setIsPrimaryKey(true);
        SEQ_NAME_field.setIsIdentity(false);
        SEQ_NAME_field.setUnique(false);
        SEQ_NAME_field.setShouldAllowNull(false);
        table.addField(SEQ_NAME_field);

        return table;
    }
    
    public static TableDefinition build_BLUE_Table() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_BLUE");
    
        FieldDefinition ID_field = new FieldDefinition();
        ID_field.setName("ID");
        ID_field.setTypeName("NUMERIC");
        ID_field.setSize(15);
        ID_field.setIsPrimaryKey(true);
        ID_field.setUnique(false);
        ID_field.setIsIdentity(false);
        ID_field.setShouldAllowNull(false);
        table.addField(ID_field);
    
        FieldDefinition ALCOHOL_CONTENT_field = new FieldDefinition();
        ALCOHOL_CONTENT_field.setName("ALCOHOL_CONTENT");
        ALCOHOL_CONTENT_field.setTypeName("DOUBLE PRECIS");
        ALCOHOL_CONTENT_field.setSize(15);
        ALCOHOL_CONTENT_field.setIsPrimaryKey(false);
        ALCOHOL_CONTENT_field.setUnique(false);
        ALCOHOL_CONTENT_field.setIsIdentity(false);
        ALCOHOL_CONTENT_field.setShouldAllowNull(true);
        table.addField(ALCOHOL_CONTENT_field);
        
        FieldDefinition BEER_CONSUMER_ID_field = new FieldDefinition();
        BEER_CONSUMER_ID_field.setName("C_ID");
        BEER_CONSUMER_ID_field.setTypeName("NUMERIC");
        BEER_CONSUMER_ID_field.setSize(15);
        BEER_CONSUMER_ID_field.setIsPrimaryKey(false);
        BEER_CONSUMER_ID_field.setUnique(false);
        BEER_CONSUMER_ID_field.setIsIdentity(false);
        BEER_CONSUMER_ID_field.setShouldAllowNull(true);
        BEER_CONSUMER_ID_field.setForeignKeyFieldName("CMP3_CONSUMER.ID");
        table.addField(BEER_CONSUMER_ID_field);
        
        FieldDefinition VERSION_field = new FieldDefinition();
        VERSION_field.setName("VERSION");
        VERSION_field.setTypeName("DATETIME");
        VERSION_field.setSize(23);
        VERSION_field.setIsPrimaryKey(false);
        VERSION_field.setUnique(false);
        VERSION_field.setIsIdentity(false);
        VERSION_field.setShouldAllowNull(true);
        table.addField(VERSION_field);

        return table;
    }
    
    public static TableDefinition build_CANADIAN_Table() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_CANADIAN");
    
        FieldDefinition ID_field = new FieldDefinition();
        ID_field.setName("ID");
        ID_field.setTypeName("NUMERIC");
        ID_field.setSize(15);
        ID_field.setIsPrimaryKey(true);
        ID_field.setUnique(false);
        ID_field.setIsIdentity(false);
        ID_field.setShouldAllowNull(false);
        table.addField(ID_field);
    
        FieldDefinition ALCOHOL_CONTENT_field = new FieldDefinition();
        ALCOHOL_CONTENT_field.setName("ALCOHOL_CONTENT");
        ALCOHOL_CONTENT_field.setTypeName("DOUBLE PRECIS");
        ALCOHOL_CONTENT_field.setSize(15);
        ALCOHOL_CONTENT_field.setIsPrimaryKey(false);
        ALCOHOL_CONTENT_field.setUnique(false);
        ALCOHOL_CONTENT_field.setIsIdentity(false);
        ALCOHOL_CONTENT_field.setShouldAllowNull(true);
        table.addField(ALCOHOL_CONTENT_field);
        
        FieldDefinition BORN_ON_DATE_field = new FieldDefinition();
        BORN_ON_DATE_field.setName("BORN");
        BORN_ON_DATE_field.setTypeName("DATETIME");
        BORN_ON_DATE_field.setSize(23);
        BORN_ON_DATE_field.setIsPrimaryKey(false);
        BORN_ON_DATE_field.setUnique(false);
        BORN_ON_DATE_field.setIsIdentity(false);
        BORN_ON_DATE_field.setShouldAllowNull(true);
        table.addField(BORN_ON_DATE_field);
        
        FieldDefinition FLAVOR_field = new FieldDefinition();
        FLAVOR_field.setName("FLAVOR");
        FLAVOR_field.setTypeName("INTEGER");
        FLAVOR_field.setSize(23);
        FLAVOR_field.setIsPrimaryKey(false);
        FLAVOR_field.setUnique(false);
        FLAVOR_field.setIsIdentity(false);
        FLAVOR_field.setShouldAllowNull(true);
        table.addField(FLAVOR_field);

        FieldDefinition BEER_CONSUMER_ID_field = new FieldDefinition();
        BEER_CONSUMER_ID_field.setName("CONSUMER_ID");
        BEER_CONSUMER_ID_field.setTypeName("NUMERIC");
        BEER_CONSUMER_ID_field.setSize(15);
        BEER_CONSUMER_ID_field.setIsPrimaryKey(false);
        BEER_CONSUMER_ID_field.setUnique(false);
        BEER_CONSUMER_ID_field.setIsIdentity(false);
        BEER_CONSUMER_ID_field.setShouldAllowNull(true);
        BEER_CONSUMER_ID_field.setForeignKeyFieldName("CMP3_CONSUMER.ID");
        table.addField(BEER_CONSUMER_ID_field);
        
        FieldDefinition fieldPROPERTIES = new FieldDefinition();
        fieldPROPERTIES.setName("PROPERTIES");
        fieldPROPERTIES.setTypeName("LONG RAW");
        fieldPROPERTIES.setSize(200);
        fieldPROPERTIES.setSubSize(0);
        fieldPROPERTIES.setIsPrimaryKey(false);
        fieldPROPERTIES.setIsIdentity(false);
        fieldPROPERTIES.setUnique(false);
        fieldPROPERTIES.setShouldAllowNull(true);
        table.addField(fieldPROPERTIES);

        FieldDefinition VERSION_field = new FieldDefinition();
        VERSION_field.setName("VERSION");
        VERSION_field.setTypeName("DATETIME");
        VERSION_field.setSize(23);
        VERSION_field.setIsPrimaryKey(false);
        VERSION_field.setUnique(false);
        VERSION_field.setIsIdentity(false);
        VERSION_field.setShouldAllowNull(true);
        table.addField(VERSION_field);
        
        return table;
    }
    
    public static TableDefinition build_CERTIFICATION_Table() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_CERTIFICATION");
    
        FieldDefinition ID_field = new FieldDefinition();
        ID_field.setName("ID");
        ID_field.setTypeName("NUMERIC");
        ID_field.setSize(15);
        ID_field.setIsPrimaryKey(true);
        ID_field.setUnique(false);
        ID_field.setIsIdentity(false);
        ID_field.setShouldAllowNull(false);
        table.addField(ID_field);

        FieldDefinition DESCRIPTION_field = new FieldDefinition();
        DESCRIPTION_field.setName("DESCRIPTION");
        DESCRIPTION_field.setTypeName("VARCHAR");
        DESCRIPTION_field.setSize(40);
        DESCRIPTION_field.setShouldAllowNull(true);
        DESCRIPTION_field.setIsPrimaryKey(false);
        DESCRIPTION_field.setUnique(false);
        DESCRIPTION_field.setIsIdentity(false);
        table.addField(DESCRIPTION_field);
        
        FieldDefinition BEER_CONSUMER_ID_field = new FieldDefinition();
        BEER_CONSUMER_ID_field.setName("CONSUMER_ID");
        BEER_CONSUMER_ID_field.setTypeName("NUMERIC");
        BEER_CONSUMER_ID_field.setSize(15);
        BEER_CONSUMER_ID_field.setIsPrimaryKey(false);
        BEER_CONSUMER_ID_field.setUnique(false);
        BEER_CONSUMER_ID_field.setIsIdentity(false);
        BEER_CONSUMER_ID_field.setShouldAllowNull(true);
        BEER_CONSUMER_ID_field.setForeignKeyFieldName("CMP3_CONSUMER.ID");
        table.addField(BEER_CONSUMER_ID_field);

        return table;
    }
    
    public static TableDefinition build_SERIALNUMBER_Table() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_SERIAL_NUMBER");
    
        FieldDefinition NUMBER_field = new FieldDefinition();
        NUMBER_field.setName("S_NUMBER");
        NUMBER_field.setTypeName("NUMERIC");
        NUMBER_field.setSize(15);
        NUMBER_field.setIsPrimaryKey(true);
        NUMBER_field.setUnique(false);
        NUMBER_field.setIsIdentity(false);
        NUMBER_field.setShouldAllowNull(false);
        table.addField(NUMBER_field);
        
        return table;
    }
    
    public static TableDefinition build_TELEPHONE_NUMBER_Table() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_TELEPHONE");

        FieldDefinition ID_field = new FieldDefinition();
        ID_field.setName("CONSUMER_ID");
        ID_field.setTypeName("NUMERIC");
        ID_field.setSize(15);
        ID_field.setIsPrimaryKey(false);
        ID_field.setUnique(false);
        ID_field.setIsIdentity(false);
        ID_field.setShouldAllowNull(true);
        ID_field.setForeignKeyFieldName("CMP3_CONSUMER.ID");
        table.addField(ID_field);
    
        FieldDefinition TYPE_field = new FieldDefinition();
        TYPE_field.setName("TYPE");
        TYPE_field.setTypeName("VARCHAR");
        TYPE_field.setSize(15);
        TYPE_field.setIsPrimaryKey(true);
        TYPE_field.setUnique(false);
        TYPE_field.setIsIdentity(false);
        TYPE_field.setShouldAllowNull(false);
        table.addField(TYPE_field);
    
        FieldDefinition AREA_CODE_field = new FieldDefinition();
        AREA_CODE_field.setName("AREA_CODE");
        AREA_CODE_field.setTypeName("VARCHAR");
        AREA_CODE_field.setSize(3);
        AREA_CODE_field.setIsPrimaryKey(true);
        AREA_CODE_field.setUnique(false);
        AREA_CODE_field.setIsIdentity(false);
        AREA_CODE_field.setShouldAllowNull(false);
        table.addField(AREA_CODE_field);
    
        FieldDefinition NUMBER_field = new FieldDefinition();
        NUMBER_field.setName("TNUMBER");
        NUMBER_field.setTypeName("VARCHAR");
        NUMBER_field.setSize(8);
        NUMBER_field.setIsPrimaryKey(true);
        NUMBER_field.setUnique(false);
        NUMBER_field.setIsIdentity(false);
        NUMBER_field.setShouldAllowNull(false);
        table.addField(NUMBER_field);

        return table;
    }
    
    public static void createTables(Session session) {
        InheritedTableManager.getCreator().createTables((DatabaseSession) session);
    }
        
    public static void dropTables(Session session) {
        InheritedTableManager.getCreator().dropTables((DatabaseSession) session);
    }
        
    public static TableCreator getCreator(){
        if (InheritedTableManager.tableCreator == null) {
            InheritedTableManager.tableCreator = new InheritedTableManager();
        }
        
        return InheritedTableManager.tableCreator;
    }
}
