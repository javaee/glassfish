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

/*
 * UserImpl.java
 *
 * Created on May 16, 2001, 10:39 AM
 */

package com.sun.xml.registry.uddi.infomodel;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import java.net.*;
import java.util.*;
import java.io.Serializable;

/**
 *
 * @author  kwalsh
 * @author Bobby Bissett
 */
public class UserImpl extends RegistryObjectImpl implements User, Serializable {

    // these are set after the User is created
    Organization organization;
    PersonName personName;
    String type;

    ArrayList emailAddresses;
    ArrayList postalAddresses;
    ArrayList telephoneNumbers;
    
    /**
     * Default constructor
     */
    public UserImpl() {
	super();
	emailAddresses = new ArrayList();
        postalAddresses = new ArrayList();
        telephoneNumbers = new ArrayList();
    }

    /**
     * Gets the submitting organization
     */
    public Organization getOrganization() throws JAXRException{
        return organization;
    }

    /** 
     * Name of contact person  
     */
    public PersonName getPersonName() throws JAXRException {
        return personName;
    }
    
    /**
     * Sets Name of contact person.
     */
    public void setPersonName(PersonName personName) throws JAXRException {
        this.personName = personName;
        setIsModified(true);
    }    
    
    /**
     * The postal addresses for this Contact.
     */
    public Collection getPostalAddresses() throws JAXRException {
        return (Collection) postalAddresses.clone();
    }
    
    /**
     * Sets the addresses. Treat null parameter as empty collection.
     */
    public void setPostalAddresses(Collection addresses) throws JAXRException {
        postalAddresses.clear();
        if (addresses != null) {
            postalAddresses.addAll(addresses);
	}
        setIsModified(true);
    }
    
    /** 
     * Gets the telephone numbers for this User that match the specified
     * telephone number type. If phoneType is null return all telephoneNumbers 
     */
    public Collection getTelephoneNumbers(String phoneType) throws JAXRException{
        if (phoneType == null) {
            return (Collection) telephoneNumbers.clone();
        }
        Collection numbers = new ArrayList();
        Iterator iter = telephoneNumbers.iterator();
	while (iter.hasNext()) {
	    TelephoneNumber number = (TelephoneNumber) iter.next();
	    if (number.getType().equals(phoneType)) {
		numbers.add(number);
	    }
	}
        return numbers;
    }
	
    /** 
     * Set the various telephone numbers for this user. Treat null param
     * as an empty collection.
     */
    public void setTelephoneNumbers(Collection phoneNumbers) throws JAXRException {
	telephoneNumbers.clear();
	if (phoneNumbers != null) {
	    telephoneNumbers.addAll(phoneNumbers);
	}
        setIsModified(true);
    }

    /**
     * Get the user email addresses
     */	
    public Collection getEmailAddresses() throws JAXRException {
        return (Collection) emailAddresses.clone();
    }
    
    /**
     * Set the user email addresses. Treat null param as
     * an empty collection.
     */
    public void setEmailAddresses(Collection addresses) throws JAXRException {
	emailAddresses.clear();
	if (addresses != null) {
	    emailAddresses.addAll(addresses);
	}
        setIsModified(true);
    }
    
    /**
     * Get user type
     */
    public String getType() throws JAXRException {
        return type;
    }
	
    /**
     * Set user type
     */
    public void setType(String type) throws JAXRException {
	this.type = type;
        setIsModified(true);
    }
	
    /** 
     * Level 1 method
     */
    public URL getUrl() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Sets the URL to the web page for this contact.
     */
    public void setUrl(URL url) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
     /**
     * Internal method for setting user organization
     */
    public void setOrganization(Organization org) throws JAXRException {
        this.organization = org;
    }

}
