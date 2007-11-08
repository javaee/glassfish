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


// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.xml.merge.incompletemappings.nonowning;

import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import java.util.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

/**
 *
 */
@Entity(name="XMLIncompleteAddress")
@Table(name="CMP3_XML_INC_ADDRESS")
public class Address implements Serializable {
	private Integer id;
	private String street;
	private String city;
    private String province;
    private String postalCode;
    private String country;
	private Customer customer;

    public Address() {
        city = "";
        province = "";
        postalCode = "";
        street = "";
        country = "";
    }

    public Address(String street, String city, String province, String country, String postalCode) {
        this.street = street;
        this.city = city;
        this.province = province;
        this.country = country;
        this.postalCode = postalCode;
    }

	@Id
    @GeneratedValue(strategy=SEQUENCE, generator="XML_INCOMPLETE_ADDRESS_SEQUENCE_GENERATOR")
	@SequenceGenerator(name="XML_INCOMPLETE_ADDRESS_SEQUENCE_GENERATOR", sequenceName="XML_INC_ADDRESS_SEQ", allocationSize=25)
	@Column(name="ADDRESS_ID")
	public Integer getId() { 
        return id; 
    }
    
	public void setId(Integer id) { 
        this.id = id; 
    }

	public String getStreet() { 
        return street; 
    }
    
	public void setStreet(String street) { 
        this.street = street; 
    }

	public String getCity() { 
        return city; 
    }
    
	public void setCity(String city) { 
        this.city = city; 
    }

	public String getProvince() { 
        return province; 
    }
        
	public void setProvince(String province) { 
        this.province = province; 
    }

	@Column(name="P_CODE")
	public String getPostalCode() { 
        return postalCode; 
    }
    
	public void setPostalCode(String postalCode) { 
        this.postalCode = postalCode; 
    }

	public String getCountry() { 
        return country; 
    }
    
	public void setCountry(String country) { 
        this.country = country;
    }

	@OneToOne(cascade=PERSIST, fetch=LAZY)
	@JoinColumn(name="CUST_ID", referencedColumnName="CUST_ID")
	public Customer getCustomer() { 
        return customer; 
    }
    
	public void setCustomer(Customer customer) { 
        this.customer = customer; 
    }
}
