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

import javax.persistence.*;
import static javax.persistence.GenerationType.TABLE;

@Entity
@Table(name = "CMP3_PRIMITIVE_TYPES")
public class PrimitiveTypes implements java.io.Serializable {

    private int id;
    private boolean booleanData;
    private byte byteData;
    private char charData;
    private short shortData;
    private int intData;
    private long longData;
    private float floatData;
    private double doubleData;
    private String stringData;

    public PrimitiveTypes()
    {
    }

    public PrimitiveTypes(int id, boolean booleanData, byte byteData, char charData, short shortData, int intData, long longData, float floatData, double doubleData, String stringData)
    {
        this.id = id;
        this.booleanData = booleanData;
        this.byteData = byteData;
        this.charData = charData;
        this.shortData = shortData;
        this.intData = intData;
        this.longData = longData;
        this.floatData = floatData;
        this.doubleData = doubleData;
        this.stringData = stringData;
    }

    @Id
    @Column(name="PT_ID")
    @GeneratedValue(strategy=TABLE, generator="PT_TABLE_GENERATOR")
    @TableGenerator(
        name="PT_TABLE_GENERATOR",
        table="CMP3_PT_SEQ",
        pkColumnName="SEQ_NAME",
        valueColumnName="SEQ_COUNT",
        pkColumnValue="PT_SEQ"
    )
   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id= id;
   }

    @Column(name = "BOOLEAN_DATA")
    public boolean getBooleanData(){
        return booleanData;

    }
    public void setBooleanData(boolean booleanData)
    {
        this.booleanData = booleanData;
    }

    @Column(name = "BYTE_DATA")
    public byte getByteData()
    {
        return byteData;
    }
    public void setByteData(byte byteData)
    {
        this.byteData= byteData;
    }

    @Column(name = "CHAR_DATA")
    public char getCharData()
    {
        return charData;
    }
    public void setCharData(char charData)
    {
        this.charData = charData;
    }

    @Column(name = "SHORT_DATA")
    public short getShortData(){
        return shortData;
    }
    public  void setShortData(short shortData)
    {
        this.shortData = shortData;
    }

    @Column(name = "INT_DATA")
    public int getIntData(){
        return intData;
    }
    public void setIntData(int intData)
    {
        this.intData = intData;
    }

    @Column(name = "LONG_DATA")
    public long getLongData(){
        return longData;
    }
    public void setLongData(long longData)
    {
        this.longData = longData;
    }

    @Column(name = "FLOAT_DATA")
    public float getFloatData(){
        return floatData;
    }
    public void setFloatData(float floatData)
    {
        this.floatData = floatData;
    }

    @Column(name = "DOUBLE_DATA")
    public double getDoubleData(){
        return doubleData;
    }
    public void setDoubleData(double doubleData)
    {
        this.doubleData = doubleData;
    }

    @Column(name = "STRING_DATA")
    public String getStringData(){
        return stringData;
    }
    public void setStringData(String stringData)
    {
        this.stringData = stringData;
    }

}
