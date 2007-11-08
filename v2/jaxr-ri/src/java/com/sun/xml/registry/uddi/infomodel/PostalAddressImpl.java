/*
* The contents of this file are subject to the terms 
* of the Common Development and Distribution License 
* (the License).  You may not use this file except in
* compliance with the License.
* 
* You can obtain a copy of the license at 
* https://glassfish.dev.java.net/public/CDDLv1.0.html or
* glassfish/bootstrap/legal/CDDLv1.0.txt.
* See the License for the specific language governing 
* permissions and limitations under the License.
* 
* When distributing Covered Code, include this CDDL 
* Header Notice in each file and include the License file 
* at glassfish/bootstrap/legal/CDDLv1.0.txt.  
* If applicable, add the following below the CDDL Header, 
* with the fields enclosed by brackets [] replaced by
* you own identifying information: 
* "Portions Copyrighted [year] [name of copyright owner]"
* 
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/

package com.sun.xml.registry.uddi.infomodel;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import java.io.Serializable;

/**
 *
 * @author  kwalsh
 * @author Bobby Bissett
 */
public class PostalAddressImpl extends ExtensibleObjectImpl implements PostalAddress, Serializable {

    private ClassificationScheme postalScheme;
    private String city;
    private String country;
    private String postalCode;
    private String stateOrProvince;
    private String street;
    private String streetNumber;
    private String addressType;
    
    /**
     * Creates new PostalAddressImpl. Sets
     * default empty strings.
     */
    public PostalAddressImpl() {
	super();
	addressType = new String();
	city = new String();
	country = new String();
	postalCode = new String();
	stateOrProvince = new String();
	street = new String();
	streetNumber = new String();
    }
	
    /** 
     * Utility constructor
     */
    public PostalAddressImpl(String streetNumber, String street, String city,
			     String stateOrProvince, String country,
			     String postalCode, String type) {
	this();
	this.streetNumber = streetNumber;
	this.street = street;
	this.city = city;
	this.stateOrProvince = stateOrProvince;
	this.country = country;
	this.postalCode = postalCode;
	addressType = type;
    }
	
   /**
     * The street address
     */
    public String getStreet() throws JAXRException {
        return street;
    }
    
    /**
     * Sets the street address
     */
    public void setStreet(String street) throws JAXRException {
	this.street = street;
    }
    
    /** 
     * The street number 
     */
    public String getStreetNumber() throws JAXRException {
	return streetNumber;
    }
	
    /**
     * Sets the street number
     */
    public void setStreetNumber(String streetNumber) throws JAXRException {
	this.streetNumber = streetNumber;
    }

    /**
     * The city
     */
    public String getCity() throws JAXRException {
        return city;
    }
    
    /**
     * Sets the city
     */
    public void setCity(String city) throws JAXRException {
        this.city = city;
    }

    /**
     * The state or province
     */
    public String getStateOrProvince() throws JAXRException {
        return stateOrProvince;
    }

    /**
     * Sets the state or province
     */
    public void setStateOrProvince(String stateOrProvince) throws JAXRException {
        this.stateOrProvince = stateOrProvince;
    }
    
    /**
     * The postal or zip code
     */
    public String getPostalCode() throws JAXRException {
        return postalCode;
    }
    
    /**
     * Sets the postal or zip code
     */
    public void setPostalCode(String postalCode) throws JAXRException {
        this.postalCode = postalCode;
    }

    /**
     * The country
     */
    public String getCountry() throws JAXRException {
        return country;
    }   

    /**
     * Sets the country
     */
    public void setCountry(String country) throws JAXRException {
        this.country = country;
    }

 
    /**
     * The type of address (e.g. "headquarters" etc.) as a String
     */
    public String getType() throws JAXRException {
        return addressType;
    }


	
    /**
     * Sets the type of address (e.g. "headquarters" etc.) as a Concept
     */
    public void setType(String type) throws JAXRException {
        addressType = type;
    }

    /** 
     * Get a user-defined postal scheme for codifying the attributes of PostalAddress
     * If none is defined for this object, then must rerturn the default value
     * returned by RegistryService#getDefaultPostalScheme()
     */ 
    public ClassificationScheme getPostalScheme() throws JAXRException {
	return postalScheme;
    }

    /** 
     * Set a user-defined postal scheme for codifying the attributes of PostalAddress
     */ 
    public void setPostalScheme(ClassificationScheme scheme) throws JAXRException {
	    postalScheme = scheme;	
    }

}
