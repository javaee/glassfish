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

import java.util.Vector;
import java.util.HashSet;
import java.util.Collection;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;

@Entity
@Table(name="CMP3_CUSTOMER")
@NamedQuery(
	name="findAllCustomers",
	query="SELECT OBJECT(thecust) FROM Customer thecust"
)
@NamedNativeQueries(value={
@NamedNativeQuery(name="findAllSQLCustomers",
    query="select * from CMP3_CUSTOMER"),
@NamedNativeQuery(name="insertCustomer1111SQL",
    query="INSERT INTO CMP3_CUSTOMER (CUST_ID, NAME, CITY, CUST_VERSION) VALUES (1111, NULL, NULL, 1)"),
@NamedNativeQuery(name="deleteCustomer1111SQL",
    query="DELETE FROM CMP3_CUSTOMER WHERE (CUST_ID=1111)")})
public class Customer implements java.io.Serializable{
	private Integer customerId;
	private int version;
	private String city;
	private String name;
	private Collection<Order> orders = new HashSet<Order>();
    private Collection<Customer> controlledCustomers = new HashSet<Customer>();

	public Customer() {}

    @Id
    @GeneratedValue(strategy=TABLE, generator="CUSTOMER_TABLE_GENERATOR")
	@TableGenerator(
        name="CUSTOMER_GENERATOR_TABLE", 
        table="CMP3_CUSTOMER_SEQ", 
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT",
        pkColumnValue="CUST_SEQ"
    )
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

	public String getCity() {
        return city; 
    }
    
    public void setCity(String aCity) { 
        this.city = aCity; 
    }

    public String getName() { 
        return name; 
    }
    
    public void setName(String aName) { 
        this.name = aName; 
    }

    @OneToMany(targetEntity=Order.class, cascade=ALL, mappedBy="customer")
    public Collection getOrders() { 
        return orders; 
    }
    
    public void setOrders(Collection<Order> newValue) { 
        this.orders = newValue; 
    }

    public void addOrder(Order anOrder) {
        getOrders().add(anOrder);
		anOrder.setCustomer(this);
    }

    public void removeOrder(Order anOrder) {
        getOrders().remove(anOrder);
    }
    
    @ManyToMany
    @JoinTable(name="CMP3_CUSTOMER_CUSTOMER")
    public Collection<Customer> getCCustomers() {
        if (controlledCustomers == null) {
            return new Vector();
        }
        
        return controlledCustomers;
    }
    
    public void setCCustomers(Collection<Customer> controlledCustomers) {
        this.controlledCustomers = controlledCustomers;
    }
    
    public void addCCustomer(Customer controlledCustomer) {
        getCCustomers().add(controlledCustomer);
    }
}
