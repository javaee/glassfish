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

package oracle.toplink.essentials.testing.models.cmp3.virtualattribute;

import java.util.HashMap;

import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

@Entity
@Table(name="O_O_VIRTUAL")
public class OneToOneVirtualAttributeHolder  {
    
    private HashMap attributeMap;
    
    public static final String ID_KEY = "id";
    public static final String ATTRIBUTE_KEY = "attribute";

    public OneToOneVirtualAttributeHolder() {
        attributeMap = new HashMap();
    }
    
    @Id
    @GeneratedValue(strategy=TABLE, generator="O_O_VIRTUAL_ATTRIBUTE_TABLE_GENERATOR")
	@TableGenerator(
        name="O_O_VIRTUAL_ATTRIBUTE_TABLE_GENERATOR", 
        table="CMP3_VIRTUAL_SEQ", 
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT",
        pkColumnValue="ONE_TO_ONE_VIRTUAL_ATTRIBUTE_SEQ"
    )
	@Column(name="O_O_VIRTUALID")
    public Integer getId(){
        return (Integer)attributeMap.get(ID_KEY);
    }
    
    public void setId(Integer id){
        attributeMap.remove(ID_KEY);
        attributeMap.put(ID_KEY, id);
    }
    
    @OneToOne(cascade=PERSIST, fetch=LAZY)
    @JoinColumn(name="VIRTUAL_ID")
    public VirtualAttribute getVirtualAttribute(){
        return (VirtualAttribute)attributeMap.get(ATTRIBUTE_KEY);
    }
    
    public void setVirtualAttribute(VirtualAttribute virtualAttribute){
        attributeMap.remove(ATTRIBUTE_KEY);
        attributeMap.put(ATTRIBUTE_KEY, virtualAttribute);
    }
}