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
