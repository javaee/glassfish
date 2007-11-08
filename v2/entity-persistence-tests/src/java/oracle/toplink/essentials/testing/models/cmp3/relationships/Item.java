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
package oracle.toplink.essentials.testing.models.cmp3.relationships;

import javax.persistence.*;
import static javax.persistence.GenerationType.*;

@Entity
@Table(name="CMP3_ITEM")
@NamedQuery(
	name="findAllItemsByName",
	query="SELECT OBJECT(item) FROM Item item WHERE item.name = ?1"
)
public class Item implements java.io.Serializable {
	private Integer itemId;
	private int version;
	private String name;
	private String description;

	public Item() {}

	@Id
    @GeneratedValue(strategy=TABLE, generator="ITEM_TABLE_GENERATOR")
    @TableGenerator(
        name="ITEM_TABLE_GENERATOR", 
        table="CMP3_CUSTOMER_SEQ", 
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT",
        pkColumnValue="ITEM_SEQ"
    )
	@Column(name="ID")
    public Integer getItemId() { 
        return itemId; 
    }
    
    public void setItemId(Integer id) { 
        this.itemId = id; 
    }

	@Version
	@Column(name="ITEM_VERSION")
	protected int getVersion() { 
        return version; 
    }
    
	protected void setVersion(int version) { 
        this.version = version; 
    }

	public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String desc) { 
        this.description = desc; 
    }

    public String getName() { 
        return name; 
    }
    
    public void setName(String name) {
        this.name = name; 
    }
}
