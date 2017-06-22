//Copyright (c) 1998, 2004, Oracle Corporation. All rights reserved.
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
