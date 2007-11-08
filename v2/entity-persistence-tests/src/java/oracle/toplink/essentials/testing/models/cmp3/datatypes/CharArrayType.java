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

@Entity
@Table(name = "CMP3_PCHARARRAY_TYPE")
public class CharArrayType implements java.io.Serializable {

    private int id;
    private char[] primitiveCharArrayData;

    public CharArrayType()
    {
    }

    public CharArrayType(char[] primitiveCharArrayData)
    {
        this.primitiveCharArrayData = primitiveCharArrayData;
    }

    @Id
    @Column(name="PCA_ID")
    @GeneratedValue(strategy=GenerationType.TABLE, generator="PCHARARRAY_TABLE_GENERATOR")
    @TableGenerator(
        name="PCHARARRAY_TABLE_GENERATOR",
        table="CMP3_PCHARARRAY_SEQ",
        pkColumnName="SEQ_NAME",
        valueColumnName="SEQ_COUNT",
        pkColumnValue="PCHARARRAY_SEQ"
    )
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id= id;
    }

    @Column(name = "PCHARARRAY_DATA")
    public char[] getPrimitiveCharArrayData()
    {
        return primitiveCharArrayData;
    }
    public void setPrimitiveCharArrayData(char[] primitiveCharArrayData)
    {
        this.primitiveCharArrayData = primitiveCharArrayData;
    }

}
