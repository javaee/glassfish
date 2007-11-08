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
package oracle.toplink.essentials.testing.models.cmp3.lob;

import oracle.toplink.essentials.tools.schemaframework.FieldDefinition;
import oracle.toplink.essentials.tools.schemaframework.TableDefinition;

public class LobTableCreator extends oracle.toplink.essentials.tools.schemaframework.TableCreator {

public LobTableCreator() {
    setName("lob");
    
    addTableDefinition(buildCLIPTable());
    addTableDefinition(buildIMAGETable());
}

public TableDefinition buildCLIPTable() {
    TableDefinition table = new TableDefinition();
    table.setName("CMP3_CLIP");
    
    FieldDefinition fieldAUDIO = new FieldDefinition();
    fieldAUDIO.setName("AUDIO");
    fieldAUDIO.setTypeName("BLOB");
    fieldAUDIO.setSize(0);
    fieldAUDIO.setSubSize(0);
    fieldAUDIO.setIsPrimaryKey(false);
    fieldAUDIO.setIsIdentity(false);
    fieldAUDIO.setUnique(false);
    fieldAUDIO.setShouldAllowNull(true);
    table.addField(fieldAUDIO);
    
    FieldDefinition fieldCOMMENTARY = new FieldDefinition();
    fieldCOMMENTARY.setName("COMMENTARY");
    fieldCOMMENTARY.setTypeName("CLOB");
    fieldCOMMENTARY.setSize(0);
    fieldCOMMENTARY.setSubSize(0);
    fieldCOMMENTARY.setIsPrimaryKey(false);
    fieldCOMMENTARY.setIsIdentity(false);
    fieldCOMMENTARY.setUnique(false);
    fieldCOMMENTARY.setShouldAllowNull(true);
    table.addField(fieldCOMMENTARY);
    
    FieldDefinition fieldID = new FieldDefinition();
    fieldID.setName("ID");
    fieldID.setTypeName("NUMBER");
    fieldID.setSize(38);
    fieldID.setSubSize(0);
    fieldID.setIsPrimaryKey(true);
    fieldID.setIsIdentity(false);
    fieldID.setUnique(false);
    fieldID.setShouldAllowNull(false);
    table.addField(fieldID);
    
    return table;
}

public TableDefinition buildIMAGETable() {
    TableDefinition table = new TableDefinition();
    table.setName("CMP3_IMAGE");
    
    FieldDefinition fieldID = new FieldDefinition();
    fieldID.setName("ID");
    fieldID.setTypeName("NUMBER");
    fieldID.setSize(20);
    fieldID.setSubSize(0);
    fieldID.setIsPrimaryKey(true);
    fieldID.setIsIdentity(false);
    fieldID.setUnique(false);
    fieldID.setShouldAllowNull(false);
    table.addField(fieldID);
    
    FieldDefinition fieldPICTURE = new FieldDefinition();
    fieldPICTURE.setName("PICTURE");
    fieldPICTURE.setTypeName("BLOB");
    fieldPICTURE.setSize(0);
    fieldPICTURE.setSubSize(0);
    fieldPICTURE.setIsPrimaryKey(false);
    fieldPICTURE.setIsIdentity(false);
    fieldPICTURE.setUnique(false);
    fieldPICTURE.setShouldAllowNull(true);
    table.addField(fieldPICTURE);
    
    FieldDefinition fieldSCRIPT = new FieldDefinition();
    fieldSCRIPT.setName("SCRIPT");
    fieldSCRIPT.setTypeName("CLOB");
    fieldSCRIPT.setSize(0);
    fieldSCRIPT.setSubSize(0);
    fieldSCRIPT.setIsPrimaryKey(false);
    fieldSCRIPT.setIsIdentity(false);
    fieldSCRIPT.setUnique(false);
    fieldSCRIPT.setShouldAllowNull(true);
    table.addField(fieldSCRIPT);
    
    FieldDefinition fieldCUSTOMATTRIBUTE1 = new FieldDefinition();
    fieldCUSTOMATTRIBUTE1.setName("CUSTOMATTRIBUTE1");
    fieldCUSTOMATTRIBUTE1.setTypeName("BLOB");
    fieldCUSTOMATTRIBUTE1.setSize(0);
    fieldCUSTOMATTRIBUTE1.setSubSize(0);
    fieldCUSTOMATTRIBUTE1.setIsPrimaryKey(false);
    fieldCUSTOMATTRIBUTE1.setIsIdentity(false);
    fieldCUSTOMATTRIBUTE1.setUnique(false);
    fieldCUSTOMATTRIBUTE1.setShouldAllowNull(true);
    table.addField(fieldCUSTOMATTRIBUTE1);
   
    FieldDefinition fieldCUSTOMATTRIBUTE2 = new FieldDefinition();
    fieldCUSTOMATTRIBUTE2.setName("CUSTOMATTRIBUTE2");
    fieldCUSTOMATTRIBUTE2.setTypeName("BLOB");
    fieldCUSTOMATTRIBUTE2.setSize(0);
    fieldCUSTOMATTRIBUTE2.setSubSize(0);
    fieldCUSTOMATTRIBUTE2.setIsPrimaryKey(false);
    fieldCUSTOMATTRIBUTE2.setIsIdentity(false);
    fieldCUSTOMATTRIBUTE2.setUnique(false);
    fieldCUSTOMATTRIBUTE2.setShouldAllowNull(true);
    table.addField(fieldCUSTOMATTRIBUTE2);
    return table;
}

}