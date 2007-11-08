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
@Table(name = "CMP3_CHARACTERARRAY_TYPE")
public class CharacterArrayType implements java.io.Serializable {

    private int id;
    private Character[] characterArrayData;

    public CharacterArrayType()
    {
    }

    public CharacterArrayType(Character[] characterArrayData)
    {
        this.characterArrayData = characterArrayData;
    }

    @Id
    @Column(name="CA_ID")
    @GeneratedValue(strategy=GenerationType.TABLE, generator="CHARACTERARRAY_TABLE_GENERATOR")
    @TableGenerator(
        name="CHARACTERARRAY_TABLE_GENERATOR",
        table="CMP3_CHARACTERARRAY_SEQ",
        pkColumnName="SEQ_NAME",
        valueColumnName="SEQ_COUNT",
        pkColumnValue="CHARACTERARRAY_SEQ"
    )
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id= id;
    }

    @Column(name = "CHARACTERARRAY_DATA")
    public Character[] getCharacterArrayData()
    {
        return characterArrayData;
    }
    public void setCharacterArrayData(Character[] characterArrayData)
    {
        this.characterArrayData = characterArrayData;
    }

}
