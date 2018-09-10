/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package pe.ejb.ejb30.persistence.toplinksample.ejb;

import javax.persistence.*;

@Entity(name="OrderBean")
@Table(name="CMP3_ORDER")
@NamedQuery(
	name="findAllOrdersByItem",
	query="SELECT OBJECT(theorder) FROM OrderBean theorder WHERE theorder.item.itemId = :id"
)
public class OrderEntity implements java.io.Serializable {

	private Integer orderId;
	private int version;
	private ItemEntity item;
	private int quantity;
	private String shippingAddress;
	private CustomerEntity customer;

    public OrderEntity(){}
	
	public OrderEntity(int id,int qty) {
        this.setOrderId(new Integer(id));
        this.setQuantity(qty);
	}

	@Id
	@Column(name="ORDER_ID")
	public Integer getOrderId() { 
        return orderId; 
    }
    
	public void setOrderId(Integer id) { 
        this.orderId = id; 
    }

	@Version
	@Column(name="ORDER_VERSION")
	protected int getVersion() { 
        return version; 
    }
    
	protected void setVersion(int version) { 
        this.version = version; 
    }

	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="ITEM_ID", referencedColumnName="ITEM_ID")
	public ItemEntity getItem() { 
        return item; 
    }
    
	public void setItem(ItemEntity item) { 
        this.item = item; 
    }


    @Column(name="QTY")
	public int getQuantity() {
        return quantity; 
    }
    
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Column(name="SHIPADD")
	public String getShippingAddress() { 
        return shippingAddress; 
    }
    
	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	@ManyToOne()
	@JoinColumn(name="CUST_ID")
	public CustomerEntity getCustomer() { 
        return customer; 
    }
    
	public void setCustomer(CustomerEntity customer) { 
        this.customer = customer; 
    }

    public String toString(){
        return "ID: "+orderId+": qty :"+quantity;
    }
}
