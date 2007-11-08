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
package oracle.toplink.essentials.testing.models.cmp3.datatypes;

import oracle.toplink.essentials.tools.schemaframework.TableDefinition;
import oracle.toplink.essentials.tools.schemaframework.FieldDefinition;

public class DataTypesTableCreator extends oracle.toplink.essentials.tools.schemaframework.TableCreator {
    public DataTypesTableCreator() {
        setName("EJB3DataTypesProject");

        addTableDefinition(DataTypesTableCreator.buildPrimitiveTypesTable());
        addTableDefinition(DataTypesTableCreator.buildWrapperTypesTable());
        addTableDefinition(DataTypesTableCreator.buildByteArrayTable());
        addTableDefinition(DataTypesTableCreator.buildPrimitiveByteArrayTable());
        addTableDefinition(DataTypesTableCreator.buildCharacterArrayTable());
        addTableDefinition(DataTypesTableCreator.buildCharArrayTable());
    }

    public static TableDefinition buildPrimitiveTypesTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_PRIMITIVE_TYPES");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("PT_ID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition fieldBOOLEANDATA = new FieldDefinition();
        fieldBOOLEANDATA.setName("BOOLEAN_DATA");
        fieldBOOLEANDATA.setTypeName("BIT");
        fieldBOOLEANDATA.setIsPrimaryKey(false);
        fieldBOOLEANDATA.setIsIdentity(false);
        fieldBOOLEANDATA.setUnique(false);
        fieldBOOLEANDATA.setShouldAllowNull(false);
        table.addField(fieldBOOLEANDATA);

        FieldDefinition fieldBYTEDATA = new FieldDefinition();
        fieldBYTEDATA.setName("BYTE_DATA");
        fieldBYTEDATA.setTypeName("TINYINT");
        fieldBYTEDATA.setIsPrimaryKey(false);
        fieldBYTEDATA.setIsIdentity(false);
        fieldBYTEDATA.setUnique(false);
        fieldBYTEDATA.setShouldAllowNull(false);
        table.addField(fieldBYTEDATA);

        FieldDefinition fieldCHARDATA = new FieldDefinition();
        fieldCHARDATA.setName("CHAR_DATA");
        fieldCHARDATA.setTypeName("CHAR");
        fieldCHARDATA.setIsPrimaryKey(false);
        fieldCHARDATA.setIsIdentity(false);
        fieldCHARDATA.setUnique(false);
        fieldCHARDATA.setShouldAllowNull(false);
        table.addField(fieldCHARDATA);

        FieldDefinition fieldSHORTDATA = new FieldDefinition();
        fieldSHORTDATA.setName("SHORT_DATA");
        fieldSHORTDATA.setTypeName("SMALLINT");
        fieldSHORTDATA.setIsPrimaryKey(false);
        fieldSHORTDATA.setIsIdentity(false);
        fieldSHORTDATA.setUnique(false);
        fieldSHORTDATA.setShouldAllowNull(false);
        table.addField(fieldSHORTDATA);

        FieldDefinition fieldINTDATA = new FieldDefinition();
        fieldINTDATA.setName("INT_DATA");
        fieldINTDATA.setTypeName("NUMERIC");
        fieldINTDATA.setSize(15);
        fieldINTDATA.setIsPrimaryKey(false);
        fieldINTDATA.setIsIdentity(false);
        fieldINTDATA.setUnique(false);
        fieldINTDATA.setShouldAllowNull(false);
        table.addField(fieldINTDATA);

        FieldDefinition fieldLONGDATA = new FieldDefinition();
        fieldLONGDATA.setName("LONG_DATA");
        fieldLONGDATA.setTypeName("NUMERIC");
        fieldLONGDATA.setSize(19);
        fieldLONGDATA.setIsPrimaryKey(false);
        fieldLONGDATA.setIsIdentity(false);
        fieldLONGDATA.setUnique(false);
        fieldLONGDATA.setShouldAllowNull(false);
        table.addField(fieldLONGDATA);

        FieldDefinition fieldFLOATDATA = new FieldDefinition();
        fieldFLOATDATA.setName("FLOAT_DATA");
        fieldFLOATDATA.setTypeName("FLOAT");
        fieldFLOATDATA.setIsPrimaryKey(false);
        fieldFLOATDATA.setIsIdentity(false);
        fieldFLOATDATA.setUnique(false);
        fieldFLOATDATA.setShouldAllowNull(false);
        table.addField(fieldFLOATDATA);

        FieldDefinition fieldDOUBLEDATA = new FieldDefinition();
        fieldDOUBLEDATA.setName("DOUBLE_DATA");
        fieldDOUBLEDATA.setTypeName("DOUBLE");
        fieldDOUBLEDATA.setIsPrimaryKey(false);
        fieldDOUBLEDATA.setIsIdentity(false);
        fieldDOUBLEDATA.setUnique(false);
        fieldDOUBLEDATA.setShouldAllowNull(false);
        table.addField(fieldDOUBLEDATA);

        FieldDefinition fieldSTRINGDATA = new FieldDefinition();
        fieldSTRINGDATA.setName("STRING_DATA");
        fieldSTRINGDATA.setTypeName("VARCHAR");
        fieldSTRINGDATA.setSize(30);
        fieldSTRINGDATA.setIsPrimaryKey(false);
        fieldSTRINGDATA.setIsIdentity(false);
        fieldSTRINGDATA.setUnique(false);
        fieldSTRINGDATA.setShouldAllowNull(true);
        table.addField(fieldSTRINGDATA);

        return table;
    }

    public static TableDefinition buildWrapperTypesTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_WRAPPER_TYPES");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("WT_ID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition fieldBIGDECIMALDATA = new FieldDefinition();
        fieldBIGDECIMALDATA.setName("BIGDECIMAL_DATA");
        fieldBIGDECIMALDATA.setTypeName("NUMERIC");
        fieldBIGDECIMALDATA.setSize(38);
        fieldBIGDECIMALDATA.setSubSize(0);
        fieldBIGDECIMALDATA.setIsPrimaryKey(false);
        fieldBIGDECIMALDATA.setIsIdentity(false);
        fieldBIGDECIMALDATA.setUnique(false);
        fieldBIGDECIMALDATA.setShouldAllowNull(true);
        table.addField(fieldBIGDECIMALDATA);

        FieldDefinition fieldBIGINTEGERDATA = new FieldDefinition();
        fieldBIGINTEGERDATA.setName("BIGINTEGER_DATA");
        fieldBIGINTEGERDATA.setTypeName("NUMERIC");
        fieldBIGINTEGERDATA.setSize(38);
        fieldBIGINTEGERDATA.setSubSize(0);
        fieldBIGINTEGERDATA.setIsPrimaryKey(false);
        fieldBIGINTEGERDATA.setIsIdentity(false);
        fieldBIGINTEGERDATA.setUnique(false);
        fieldBIGINTEGERDATA.setShouldAllowNull(true);
        table.addField(fieldBIGINTEGERDATA);

        FieldDefinition fieldBOOLEANDATA = new FieldDefinition();
        fieldBOOLEANDATA.setName("BOOLEAN_DATA");
        fieldBOOLEANDATA.setTypeName("BIT");
        fieldBOOLEANDATA.setIsPrimaryKey(false);
        fieldBOOLEANDATA.setIsIdentity(false);
        fieldBOOLEANDATA.setUnique(false);
        fieldBOOLEANDATA.setShouldAllowNull(true);
        table.addField(fieldBOOLEANDATA);

        FieldDefinition fieldBYTEDATA = new FieldDefinition();
        fieldBYTEDATA.setName("BYTE_DATA");
        fieldBYTEDATA.setTypeName("TINYINT");
        fieldBYTEDATA.setIsPrimaryKey(false);
        fieldBYTEDATA.setIsIdentity(false);
        fieldBYTEDATA.setUnique(false);
        fieldBYTEDATA.setShouldAllowNull(true);
        table.addField(fieldBYTEDATA);

        FieldDefinition fieldCHARDATA = new FieldDefinition();
        fieldCHARDATA.setName("CHARACTER_DATA");
        fieldCHARDATA.setTypeName("CHAR");
        fieldCHARDATA.setIsPrimaryKey(false);
        fieldCHARDATA.setIsIdentity(false);
        fieldCHARDATA.setUnique(false);
        fieldCHARDATA.setShouldAllowNull(true);
        table.addField(fieldCHARDATA);

        FieldDefinition fieldSHORTDATA = new FieldDefinition();
        fieldSHORTDATA.setName("SHORT_DATA");
        fieldSHORTDATA.setTypeName("SMALLINT");
        fieldSHORTDATA.setIsPrimaryKey(false);
        fieldSHORTDATA.setIsIdentity(false);
        fieldSHORTDATA.setUnique(false);
        fieldSHORTDATA.setShouldAllowNull(true);
        table.addField(fieldSHORTDATA);

        FieldDefinition fieldINTDATA = new FieldDefinition();
        fieldINTDATA.setName("INTEGER_DATA");
        fieldINTDATA.setTypeName("NUMERIC");
        fieldINTDATA.setSize(15);
        fieldINTDATA.setIsPrimaryKey(false);
        fieldINTDATA.setIsIdentity(false);
        fieldINTDATA.setUnique(false);
        fieldINTDATA.setShouldAllowNull(true);
        table.addField(fieldINTDATA);

        FieldDefinition fieldLONGDATA = new FieldDefinition();
        fieldLONGDATA.setName("LONG_DATA");
        fieldLONGDATA.setTypeName("NUMERIC");
        fieldLONGDATA.setSize(19);
        fieldLONGDATA.setIsPrimaryKey(false);
        fieldLONGDATA.setIsIdentity(false);
        fieldLONGDATA.setUnique(false);
        fieldLONGDATA.setShouldAllowNull(true);
        table.addField(fieldLONGDATA);

        FieldDefinition fieldFLOATDATA = new FieldDefinition();
        fieldFLOATDATA.setName("FLOAT_DATA");
        fieldFLOATDATA.setTypeName("FLOAT");
        fieldFLOATDATA.setIsPrimaryKey(false);
        fieldFLOATDATA.setIsIdentity(false);
        fieldFLOATDATA.setUnique(false);
        fieldFLOATDATA.setShouldAllowNull(true);
        table.addField(fieldFLOATDATA);

        FieldDefinition fieldDOUBLEDATA = new FieldDefinition();
        fieldDOUBLEDATA.setName("DOUBLE_DATA");
        fieldDOUBLEDATA.setTypeName("DOUBLE");
        fieldDOUBLEDATA.setIsPrimaryKey(false);
        fieldDOUBLEDATA.setIsIdentity(false);
        fieldDOUBLEDATA.setUnique(false);
        fieldDOUBLEDATA.setShouldAllowNull(true);
        table.addField(fieldDOUBLEDATA);

        FieldDefinition fieldSTRINGDATA = new FieldDefinition();
        fieldSTRINGDATA.setName("STRING_DATA");
        fieldSTRINGDATA.setTypeName("VARCHAR");
        fieldSTRINGDATA.setSize(30);
        fieldSTRINGDATA.setIsPrimaryKey(false);
        fieldSTRINGDATA.setIsIdentity(false);
        fieldSTRINGDATA.setUnique(false);
        fieldSTRINGDATA.setShouldAllowNull(true);
        table.addField(fieldSTRINGDATA);

        return table;
    }

    public static TableDefinition buildByteArrayTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_BYTEARRAY_TYPE");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("BA_ID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition fieldBYTEARRAYDATA = new FieldDefinition();
        fieldBYTEARRAYDATA.setName("BYTEARRAY_DATA");
        fieldBYTEARRAYDATA.setTypeName("LONGVARBINARY");
        fieldBYTEARRAYDATA.setIsPrimaryKey(false);
        fieldBYTEARRAYDATA.setIsIdentity(false);
        fieldBYTEARRAYDATA.setUnique(false);
        fieldBYTEARRAYDATA.setShouldAllowNull(true);
        table.addField(fieldBYTEARRAYDATA);

        return table;
    }

    public static TableDefinition buildPrimitiveByteArrayTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_PBYTEARRAY_TYPE");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("PBA_ID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition fieldPRIMITIVEBYTEARRAYDATA = new FieldDefinition();
        fieldPRIMITIVEBYTEARRAYDATA.setName("PBYTEARRAY_DATA");
        fieldPRIMITIVEBYTEARRAYDATA.setTypeName("LONGVARBINARY");
        fieldPRIMITIVEBYTEARRAYDATA.setIsPrimaryKey(false);
        fieldPRIMITIVEBYTEARRAYDATA.setIsIdentity(false);
        fieldPRIMITIVEBYTEARRAYDATA.setUnique(false);
        fieldPRIMITIVEBYTEARRAYDATA.setShouldAllowNull(true);
        table.addField(fieldPRIMITIVEBYTEARRAYDATA);

        return table;
    }

    public static TableDefinition buildCharacterArrayTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_CHARACTERARRAY_TYPE");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("CA_ID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition fieldCHARACTERARRAYDATA = new FieldDefinition();
        fieldCHARACTERARRAYDATA.setName("CHARACTERARRAY_DATA");
        fieldCHARACTERARRAYDATA.setTypeName("LONGVARCHAR");
        fieldCHARACTERARRAYDATA.setIsPrimaryKey(false);
        fieldCHARACTERARRAYDATA.setIsIdentity(false);
        fieldCHARACTERARRAYDATA.setUnique(false);
        fieldCHARACTERARRAYDATA.setShouldAllowNull(true);
        table.addField(fieldCHARACTERARRAYDATA);

        return table;
    }

    public static TableDefinition buildCharArrayTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CMP3_PCHARARRAY_TYPE");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("PCA_ID");
        fieldID.setTypeName("NUMERIC");
        fieldID.setSize(15);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(true);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition fieldCHARARRAYDATA = new FieldDefinition();
        fieldCHARARRAYDATA.setName("PCHARARRAY_DATA");
        fieldCHARARRAYDATA.setTypeName("LONGVARCHAR");
        fieldCHARARRAYDATA.setIsPrimaryKey(false);
        fieldCHARARRAYDATA.setIsIdentity(false);
        fieldCHARARRAYDATA.setUnique(false);
        fieldCHARARRAYDATA.setShouldAllowNull(true);
        table.addField(fieldCHARARRAYDATA);

        return table;
    }

}
