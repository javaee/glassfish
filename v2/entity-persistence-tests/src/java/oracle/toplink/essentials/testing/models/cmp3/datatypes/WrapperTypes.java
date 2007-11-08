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
import java.math.*;

@Entity
@Table(name = "CMP3_WRAPPER_TYPES")
public class WrapperTypes implements java.io.Serializable {

    private int id;
    private BigDecimal bigDecimalData;
    private BigInteger bigIntegerData;
    private Boolean booleanData;
    private Byte byteData;
    private Character characterData;
    private Short shortData;
    private Integer integerData;
    private Long longData;
    private Float floatData;
    private Double doubleData;
    private String stringData;

    public WrapperTypes()
    {
    }

    public WrapperTypes(BigDecimal bigDecimalData, BigInteger bigIntegerData, Boolean booleanData, Byte byteData, Character characterData, Short shortData, Integer integerData, Long longData, Float floatData, Double doubleData, String stringData)
    {
        this.bigDecimalData = bigDecimalData;
        this.bigIntegerData =  bigIntegerData;
        this.booleanData = booleanData;
        this.byteData = byteData;
        this.characterData = characterData;
        this.shortData = shortData;
        this.integerData = integerData;
        this.longData = longData;
        this.floatData = floatData;
        this.doubleData = doubleData;
        this.stringData = stringData;
    }

    @Id
    @Column(name="WT_ID")
    @GeneratedValue(strategy=TABLE, generator="WRAPPER_TABLE_GENERATOR")
    @TableGenerator(
        name="WRAPPER_TABLE_GENERATOR",
        table="CMP3_WRAPPER_SEQ",
        pkColumnName="SEQ_NAME",
        valueColumnName="SEQ_COUNT",
        pkColumnValue="WRAPPER_SEQ"
    )
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id= id;
    }

    @Column(name = "BIGDECIMAL_DATA")
    public BigDecimal getBigDecimalData(){
        return bigDecimalData;

    }
    public void setBigDecimalData(BigDecimal bigDecimalData)
    {
        this.bigDecimalData = bigDecimalData;
    }

    @Column(name = "BIGINTEGER_DATA")
    public BigInteger getBigIntegerData(){
        return bigIntegerData;

    }
    public void setBigIntegerData(BigInteger bigIntegerData)
    {
        this.bigIntegerData = bigIntegerData;
    }

    @Column(name = "BOOLEAN_DATA")
    public Boolean getBooleanData(){
        return booleanData;

    }
    public void setBooleanData(Boolean booleanData)
    {
        this.booleanData = booleanData;
    }

    @Column(name = "BYTE_DATA")
    public Byte getByteData()
    {
        return byteData;
    }
    public void setByteData(Byte byteData)
    {
        this.byteData = byteData;
    }

    @Column(name = "CHARACTER_DATA")
    public Character getCharacterData()
    {
        return characterData;
    }
    public void setCharacterData(Character characterData)
    {
        this.characterData = characterData;
    }

    @Column(name = "SHORT_DATA")
    public Short getShortData(){
        return shortData;
    }
    public void setShortData(Short shortData)
    {
        this.shortData = shortData;
    }

    @Column(name = "INTEGER_DATA")
    public Integer getIntegerData(){
        return integerData;
    }
    public void setIntegerData(Integer integerData)
    {
        this.integerData = integerData;
    }

    @Column(name = "LONG_DATA")
    public Long getLongData(){
        return longData;
    }
    public void setLongData(Long longData)
    {
        this.longData = longData;
    }

    @Column(name = "FLOAT_DATA")
    public Float getFloatData(){
        return floatData;
    }
    public void setFloatData(Float floatData)
    {
        this.floatData = floatData;
    }

    @Column(name = "DOUBLE_DATA")
    public Double getDoubleData(){
        return doubleData;
    }
    public void setDoubleData(Double doubleData)
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
