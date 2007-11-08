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
package oracle.toplink.essentials.testing.models.cmp3.xml.relationships.unidirectional;

import java.io.Serializable;

/**
 * <p><b>Purpose</b>: Represents the mailing address on an Employee
 * <p><b>Description</b>: Held in a private 1:1 relationship from Employee
 * @see Employee
 */
public class Address implements Serializable {
	private Integer id;
	private String street;
	private String city;
    private String province;
    private String postalCode;
    private String country;

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
}