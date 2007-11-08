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
package oracle.toplink.essentials.testing.models.cmp3.xml.merge.relationships;

import javax.persistence.*;
import java.util.Collection;

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.GenerationType.*;

@Entity(name="XMLMergePartsList")
@Table(name="CMP3_XML_MERGE_PARTSLIST")
public class PartsList implements java.io.Serializable {
	private Integer partsListId;
	private int version;
	private Collection<Item> items;

	public PartsList() {}

    
	@Id
    @GeneratedValue(strategy=TABLE, generator="XML_MERGE_PARTSLIST_TABLE_GENERATOR")
    // This table generator is overridden in the XML, therefore it should
    // not be processed. If it is processed, because the table name is so long
    // it will cause an error. No error means everyone is happy.
    @TableGenerator(
        name="XML_MERGE_PARTSLIST_TABLE_GENERATOR", 
        table="CMP3_XML_MERGE_CUSTOMER_SEQ_INCORRECT_LONG_NAME_WILL_CAUSE_ERROR", 
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT",
        pkColumnValue="PARTSLIST_SEQ"
    )
	@Column(name="ID")
    public Integer getPartsListId() { 
        return partsListId; 
    }
    
    public void setPartsListId(Integer id) { 
        this.partsListId = id; 
    }

	@Version
	@Column(name="VERSION")
	protected int getVersion() { 
        return version; 
    }
    
	protected void setVersion(int version) { 
        this.version = version; 
    }
	
	@ManyToMany(cascade=PERSIST)
    @JoinTable(
		name="CMP3_XML_MERGE_PARTSLIST_ITEM",
        joinColumns=@JoinColumn(name="PARTSLIST_ID", referencedColumnName="ID"),
		inverseJoinColumns=@JoinColumn(name="ITEM_ID", referencedColumnName="ITEM_ID")
	)
	public Collection<Item> getItems() { 
        return items; 
    }
    
	public void setItems(Collection<Item> items) {
		this.items = items;
	}
}
