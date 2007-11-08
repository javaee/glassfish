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

import com.sun.xml.registry.uddi.*;

import java.util.*;
import java.io.Serializable;

/**
 *
 * @author  kwalsh
 */
public class OrganizationImpl extends RegistryObjectImpl implements Organization, Serializable {

    private ArrayList services;
    private ArrayList telephoneNumbers;
    private ArrayList users;
    private User primaryContact;
    
    /**
     * Creates new OrganizationImpl
     */
    public OrganizationImpl() {
	super();
        services = new ArrayList();
        telephoneNumbers = new ArrayList();
        users = new ArrayList();
    }
	
    /**
     * Creates new OrganizationImpl
     */
    public OrganizationImpl(Key key) {
	this();
	this.key = key;
    }
	
    /**
     * Creates new OrganizationImpl
     */
    public OrganizationImpl(String name) throws JAXRException {
	this();
	this.name = new InternationalStringImpl(name);
    }
	
    /**
     * Creates new OrganizationImpl
     */
    public OrganizationImpl(Key key, String description, String name) throws JAXRException {
	this(key);
        this.description = new InternationalStringImpl(description);
        this.name = new InternationalStringImpl(name);
    }

    /**
     * Gets the primary contact for this Organization. Primary contact
     * should also be in the users collection.
     */
    public User getPrimaryContact() throws JAXRException {
	getObject();
        return primaryContact;
    }
    
    /**
     * Sets the primary contact. Need to add this to users
     * collection also if not already there.
     */
    public void setPrimaryContact(User primaryContact) throws JAXRException {
        this.primaryContact = primaryContact;
        getObject();
	if (!users.contains(primaryContact)) {
	    users.add(primaryContact);
	}
        setIsModified(true);
    }

    /**
     * Add a user.
     */
    public void addUser(User user) throws JAXRException {
        if (user != null) {
            getObject();
            users.add(user);
            setIsModified(true);
        }
    }
	
    /**
     * Add users. Treat null parameter as empty collection.
     */
    public void addUsers(Collection users) throws JAXRException {	
	if (users != null) {
            getObject();
	    this.users.addAll(users);
            setIsModified(true);
	}
    }

    /**
     * Remove a user
     */	
    public void removeUser(User user) throws JAXRException {
        if (user != null) {
            getObject();
            users.remove(user);
            setIsModified(true);
        }
    }
    
    /**
     * Remove multiple users. Treat null parameter
     * as an empty collection.
     */
    public void removeUsers(Collection users) throws JAXRException {
	if (users != null) {
            getObject();
	    this.users.removeAll(users);
            setIsModified(true);
	}
    }
    
    /**
     * Gets the Collection of Users affiliated with this Organization.
     * One of these users is designated as the primary contact. 
     */
    public Collection getUsers() throws JAXRException {
	if (users.size() == 0) {
	    getObject();
	}
        return (Collection) users.clone();
    }
	
    /** 
     * Gets the telephone numbers that match the type. If type
     * is null, return all phone numbers.
     */
    public Collection getTelephoneNumbers(String phoneType) throws JAXRException {
        if (telephoneNumbers.size() == 0) {
	    getObject(); // load object
	}
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
     * Set the various telephone numbers for this user.
     * Treat null param as empty collection.
     */
    public void setTelephoneNumbers(Collection phoneNumbers) throws JAXRException{
        getObject();
	telephoneNumbers.clear();
	if (phoneNumbers != null) {
	    telephoneNumbers.addAll(phoneNumbers);
	}
        setIsModified(true);
    }
    
    /**
     * Add a child Service. Set organization on the service to this.
     */
    public void addService(Service service) throws JAXRException {
        if (service != null) {
            getObject();
            ((ServiceImpl) service).setProvidingOrganization(this);
            services.add(service);
            setIsModified(true);
        }
    }
    
    /**
     * Add a Collection of service children. Set organization
     * on each to this. Treat null param as empty collection.
     */
    public void addServices(Collection services) throws JAXRException {
	if (services == null) {
	    return;
	}
        getObject();
	Iterator iter = services.iterator();
	try {
	    while (iter.hasNext()) {
		ServiceImpl service = (ServiceImpl) iter.next();
		service.setProvidingOrganization(this);				
		this.services.add(service);
	    }
	} catch (ClassCastException e) {
	    throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("OrganizationImpl:Objects_in_collection_must_be_services"), e);
	}
        setIsModified(true);
    }
    
    /**
     * Remove a child service.
     */
    public void removeService(Service service) throws JAXRException{
        if (service != null) {
            getObject();
            services.remove(service);
            setIsModified(true);
        }
    }
    
    /**
     * Remove a collection of children services. Treat null
     * param as empty collection.
     */
    public void removeServices(Collection services) throws JAXRException {
	if (services != null) {
            getObject();
	    this.services.removeAll(services);
            setIsModified(true);
	}
    }
    
    /**
     * Get all children services.
     */
    public Collection getServices()	throws JAXRException {
	    getObject();
        return (Collection) services.clone();
    }
    
    /**
     * Internal method for setting services on organization
     */
    public void setServices(Collection services) throws JAXRException {
        getObject();
	this.services.clear();
	addServices(services);
    }
    
    /**
     * Overrides behavior in RegistryObjectImpl to allow adding
     * external links. See appendix D of specification.
     */
    public void addExternalLink(ExternalLink link) throws JAXRException {
	if (link != null) {
            getObject();
	    ExternalLinkImpl externalLink = (ExternalLinkImpl) link;
	    externalLink.addLinkedObject(this);
	    externalLinks.add(externalLink);
            setIsModified(true);
	}
    }
       
    /**
     * Overrides behavior in RegistryObjectImpl to allow adding
     * external links. See appendix D of specification.
     */
    public void addExternalLinks(Collection links) throws JAXRException {
	if (links != null) {			
            getObject();
	    Iterator iter = links.iterator();
	    try {
		while (iter.hasNext()) {
		    addExternalLink((ExternalLink) iter.next());
		}	        
	    } catch (ClassCastException e) {
		throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("OrganizationImpl:Objects_in_collection_must_be_ExternalLinks"), e);
	    }
	}
    }

    /**
     * Overrides behavior in RegistryObjectImpl to allow adding
     * external links. See appendix D of specification.
     */
    public void setExternalLinks(Collection links) throws JAXRException {
        getObject();
	externalLinks.clear();
	addExternalLinks(links);
    }

    /**
     * Level 1 method
     */
    public PostalAddress getPostalAddress() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public void setPostalAddress(PostalAddress address) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public void addChildOrganization(Organization organization) throws JAXRException {
   	throw new UnsupportedCapabilityException();
    }    
    
    /**
     * Level 1 method
     */
    public void addChildOrganizations(Collection organization) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public void removeChildOrganization(Organization organization) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public void removeChildOrganizations(Collection organization) throws JAXRException {
	throw new UnsupportedCapabilityException();
		
    }
    
    /** 
     * Level 1 method
     */
    public int getChildOrganizationCount() throws JAXRException {
   	throw new UnsupportedCapabilityException();
    }
    
    /** 
     * Level 1 method
     */
    public Collection getChildOrganizations() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
	
    /** 
     * Level 1 method
     */
    public Collection getDescendantOrganizations() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public Organization getParentOrganization()  throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
	
    /**
     * Level 1 method
     */
    public Organization getRootOrganization()  throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

}
