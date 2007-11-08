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
package oracle.toplink.essentials.testing.models.cmp3.advanced;

import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import java.util.*;
import static javax.persistence.CascadeType.*;

/**
 * <p><b>Purpose</b>: Represents the mailing address on an Employee
 * <p><b>Description</b>: Held in a private 1:1 relationship from Employee
 * @see Employee
 */
@Entity
@Table(name="CMP3_ADDRESS")
@NamedNativeQueries({
@NamedNativeQuery(
    name="findAllSQLAddresses", 
    query="select * from CMP3_ADDRESS",
    resultClass=oracle.toplink.essentials.testing.models.cmp3.advanced.Address.class
),
@NamedNativeQuery(
    name="findAllSQLAddressesByCity_QuestionMark_Number", 
    query="select * from CMP3_ADDRESS where city=?1",
    resultClass=oracle.toplink.essentials.testing.models.cmp3.advanced.Address.class
),
@NamedNativeQuery(
    name="findAllSQLAddressesByCity_QuestionMark", 
    query="select * from CMP3_ADDRESS where city=?",
    resultClass=oracle.toplink.essentials.testing.models.cmp3.advanced.Address.class
),
@NamedNativeQuery(
    name="findAllSQLAddressesByCityAndCountry_QuestionMark_Number", 
    query="select * from CMP3_ADDRESS where city=?1 and country=?2",
    resultClass=oracle.toplink.essentials.testing.models.cmp3.advanced.Address.class
),
@NamedNativeQuery(
    name="findAllSQLAddressesByCityAndCountry_QuestionMark", 
    query="select * from CMP3_ADDRESS where city=? and country=?",
    resultClass=oracle.toplink.essentials.testing.models.cmp3.advanced.Address.class
)
}
)
@NamedQuery(
    name="findAllAddressesByPostalCode", 
    query="SELECT OBJECT(address) FROM Address address WHERE address.postalCode = :postalcode"
)
public class Address implements Serializable {
	private Integer id;
	private String street;
	private String city;
    private String province;
    private String postalCode;
    private String country;
	private Collection<Employee> employees;

    public Address() {
        city = "";
        province = "";
        postalCode = "";
        street = "";
        country = "";
        this.employees = new Vector<Employee>();
    }

    public Address(String street, String city, String province, String country, String postalCode) {
        this.street = street;
        this.city = city;
        this.province = province;
        this.country = country;
        this.postalCode = postalCode;
        this.employees = new Vector<Employee>();
    }

	@Id
    // BUG 5079973 - this should be a valid specification, that is the generator
    // trumps the AUTO strategy defaulting.
    // BUG 6273529 - SHOULD BE ABLE TO SKIP SEQUENCENAME IN SEQUENCEGENERATOR
    // In case sequenceName is not specified generator's name used instead.
    @GeneratedValue(generator="ADDRESS_SEQ")
    @SequenceGenerator(name="ADDRESS_SEQ", allocationSize=25)
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
    
	@OneToMany(cascade=ALL, mappedBy="address")
	public Collection<Employee> getEmployees() { 
        return employees; 
    }
    
    public void setEmployees(Collection<Employee> employees) {
		this.employees = employees;
	}
}