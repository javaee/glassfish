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
package oracle.toplink.essentials.testing.models.cmp3.datetime;

import oracle.toplink.essentials.tools.schemaframework.FieldDefinition;
import oracle.toplink.essentials.tools.schemaframework.TableDefinition;

public class DateTimeTableCreator extends oracle.toplink.essentials.tools.schemaframework.TableCreator {
    public DateTimeTableCreator() {
        setName("EJB3DateTimeProject");

        addTableDefinition(buildDateTimeTable());
    }

    public static TableDefinition buildDateTimeTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_DATE_TIME");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("DT_ID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition fieldSTREET = new FieldDefinition();
        fieldSTREET.setName("SQL_DATE");
        fieldSTREET.setTypeName("DATE");
        fieldSTREET.setIsPrimaryKey(false);
        fieldSTREET.setIsIdentity(false);
        fieldSTREET.setUnique(false);
        fieldSTREET.setShouldAllowNull(true);
        table.addField(fieldSTREET);

        FieldDefinition fieldCITY = new FieldDefinition();
        fieldCITY.setName("SQL_TIME");
        fieldCITY.setTypeName("TIME");
        fieldCITY.setIsPrimaryKey(false);
        fieldCITY.setIsIdentity(false);
        fieldCITY.setUnique(false);
        fieldCITY.setShouldAllowNull(true);
        table.addField(fieldCITY);

        FieldDefinition fieldPROVINCE = new FieldDefinition();
        fieldPROVINCE.setName("SQL_TS");
        fieldPROVINCE.setTypeName("TIMESTAMP");
        fieldPROVINCE.setIsPrimaryKey(false);
        fieldPROVINCE.setIsIdentity(false);
        fieldPROVINCE.setUnique(false);
        fieldPROVINCE.setShouldAllowNull(true);
        table.addField(fieldPROVINCE);

        FieldDefinition fieldPOSTALCODE = new FieldDefinition();
        fieldPOSTALCODE.setName("UTIL_DATE");
        fieldPOSTALCODE.setTypeName("TIMESTAMP");
        fieldPOSTALCODE.setIsPrimaryKey(false);
        fieldPOSTALCODE.setIsIdentity(false);
        fieldPOSTALCODE.setUnique(false);
        fieldPOSTALCODE.setShouldAllowNull(true);
        table.addField(fieldPOSTALCODE);

        FieldDefinition fieldCalToCal = new FieldDefinition();
        fieldCalToCal.setName("CAL");
        fieldCalToCal.setTypeName("TIMESTAMP");
        fieldCalToCal.setIsPrimaryKey(false);
        fieldCalToCal.setIsIdentity(false);
        fieldCalToCal.setUnique(false);
        fieldCalToCal.setShouldAllowNull(true);
        table.addField(fieldCalToCal);

        return table;
    }

}
