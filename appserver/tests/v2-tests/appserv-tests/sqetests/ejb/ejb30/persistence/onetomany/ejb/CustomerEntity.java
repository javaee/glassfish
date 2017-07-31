//Copyright (c) 1998, 2004, Oracle Corporation. All rights reserved.
package pe.ejb.ejb30.persistence.toplinksample.ejb;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name="CMP3_CUSTOMER")

@NamedQueries({
@NamedQuery( name="findAllCustomersWithLike",
	query="SELECT OBJECT(thecust) FROM CustomerEntity thecust where thecust.name LIKE :name and thecust.city LIKE :city"),
@NamedQuery( name="findAllCustomers",
	query="SELECT OBJECT(thecust) FROM CustomerEntity thecust")
})
public class CustomerEntity implements java.io.Serializable{

	private Integer customerId;
	private int version;
	private String city;
	private String name;
	private Collection<OrderEntity> orders;

    public CustomerEntity(){}

	public CustomerEntity(int id,String name,String city) {
        this.setCustomerId(new Integer(id));
        this.setName(name);
        this.setCity(city);
	}

	@Id
	@Column(name="CUST_ID")
    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer id) {
        this.customerId = id;
    }

	@Version
	@Column(name="CUST_VERSION")
	public int getVersion() {
        return version;
    }

	protected void setVersion(int version) {
		this.version = version;
	}

	@Column(name="CITY")
	public String getCity() {
        return city;
    }

    public void setCity(String aCity) {
        this.city = aCity;
    }


	@Column(name="NAME")
    public String getName() {
        return name;
    }

    public void setName(String aName) {
        this.name = aName;
    }

	@OneToMany(cascade=CascadeType.ALL,mappedBy="customer")
//	@JoinColumn(name="CUST_ID", referencedColumnName="CUST_ID")
    public Collection<OrderEntity> getOrders() {
        System.out.println("CustomerEntity:getOrders");
        return orders;
    }

    public void setOrders(Collection<OrderEntity> newValue) {
        System.out.println("CustomerEntity:setOrders");
        this.orders = newValue;
    }

    public void addOrder(OrderEntity anOrder) {
        System.out.println("CustomerEntity: adding order");
        getOrders().add(anOrder);
		anOrder.setCustomer(this);
    }

    public void removeOrder(OrderEntity anOrder) {
        getOrders().remove(anOrder);
    }

    public String toString(){
        return "ID: "+customerId+": name :"+name+": city :"+city;
    }
}
