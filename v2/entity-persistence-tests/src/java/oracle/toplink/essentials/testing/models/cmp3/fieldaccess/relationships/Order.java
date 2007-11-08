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
package oracle.toplink.essentials.testing.models.cmp3.fieldaccess.relationships;

import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

@Entity(name="FieldAccessOrderBean")
@Table(name="CMP3_FIELDACCESS_ORDER")
@NamedQuery(
	name="findAllFieldAccessOrdersByItem",
	query="SELECT OBJECT(theorder) FROM FieldAccessOrderBean theorder WHERE theorder.item.itemId = :id"
)
public class Order implements java.io.Serializable {
	@Id
    @GeneratedValue(strategy=TABLE, generator="FIELDACCESS_ORDER_TABLE_GENERATOR")
	@TableGenerator(
        name="FIELDACCESS_ORDER_TABLE_GENERATOR", 
        table="CMP3_FIELDACCESS_CUSTOMER_SEQ", 
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT",
        pkColumnValue="ORDER_SEQ"
    )
	@Column(name="ORDER_ID")
	private Integer orderId;
	@Version
	@Column(name="ORDER_VERSION")
	private int version;
	@OneToOne(cascade=PERSIST, fetch=LAZY)
	private Item item;
	private int quantity;
	@Column(name="SHIP_ADDR")
	private String shippingAddress;
	@ManyToOne(fetch=LAZY)
	private Customer customer;
	@ManyToOne(fetch=LAZY)
    private Customer billedCustomer;
	@ManyToOne(fetch=LAZY)
    private SalesPerson salesPerson;
	
	public Order() {}

	public Integer getOrderId() { 
        return orderId; 
    }
    
	public void setOrderId(Integer id) { 
        this.orderId = id; 
    }

	protected int getVersion() { 
        return version; 
    }
    
	protected void setVersion(int version) { 
        this.version = version; 
    }

	public Item getItem() { 
        return item; 
    }
    
	public void setItem(Item item) { 
        this.item = item; 
    }

	public int getQuantity() { 
        return quantity; 
    }
    
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public String getShippingAddress() { 
        return shippingAddress; 
    }
    
	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public Customer getCustomer() { 
        return customer; 
    }
    
	public void setCustomer(Customer customer) { 
        this.customer = customer; 
    }
    
	public Customer getBilledCustomer() { 
        return billedCustomer; 
    }
    
	public void setBilledCustomer(Customer billedCustomer) { 
        this.billedCustomer = billedCustomer; 
    }
    
    public SalesPerson getSalesPerson() {
        return salesPerson;
    }
    
    public void setSalesPerson(SalesPerson salesPerson) {
        this.salesPerson = salesPerson;
    }
}
