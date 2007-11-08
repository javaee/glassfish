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


package oracle.toplink.essentials.testing.models.cmp3.xml.relationships;

public class Order implements java.io.Serializable {
	private Integer orderId;
	private int version;
	private Item item;
	private int quantity;
	private String shippingAddress;
	private Customer customer;
	
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
}
