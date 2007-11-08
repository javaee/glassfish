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

import java.util.*;
import java.io.Serializable;

/**
 * Implementation of Service interface.
 *
 * @author  kwalsh
 * @author Bobby Bissett
 */
public class ServiceImpl extends RegistryEntryImpl implements Service, Serializable {

    private ArrayList serviceBindings;
    private Organization providingOrganization;
    
    /**
     * Default constructor
     */
    public ServiceImpl() {
	super();
        serviceBindings = new ArrayList();
    }
	
    /**
     * Creates new ServiceImpl with the given name
     */
    public ServiceImpl(String name) {
	this();
	this.name = new InternationalStringImpl(name);       
    }

    /**
     * Get the organization that provides this service
     */	
    public Organization getProvidingOrganization() throws JAXRException{
        return providingOrganization;
        
    }
    
    /**
     * Set the organization that provides this service
     */
    public void setProvidingOrganization(Organization org) throws JAXRException{
        providingOrganization = org;
        setIsModified(true);
    }
    
    /** 
     * Add a child ServiceBinding. Sets service on the binding.
     */
    public void addServiceBinding(ServiceBinding serviceBinding) throws JAXRException {
	if (serviceBinding != null) {
            getObject();
	    ((ServiceBindingImpl) serviceBinding).setService(this);
	    serviceBindings.add(serviceBinding);
            setIsModified(true);
	}
    }

    /** 
     * Add a Collection of ServiceBinding children. Treats null
     * param as an empty collection.
     */
    public void addServiceBindings(Collection serviceBindings) throws JAXRException {
	if (serviceBindings == null) {
	    return;
	}
        getObject();
	Iterator iter = serviceBindings.iterator();
	try {
	    while (iter.hasNext()) {
		addServiceBinding((ServiceBinding) iter.next());
	    }
	} catch (ClassCastException e) {
	    throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ServiceImpl:Objects_in_collection_must_be_ServiceBindings"), e);
	}
        setIsModified(true);
    }

    /** 
     * Remove a child ServiceBinding.
     */
    public void removeServiceBinding(ServiceBinding serviceBinding) 
	throws JAXRException {
            if (serviceBinding != null) {
                getObject();
                serviceBindings.remove(serviceBinding);
                setIsModified(true);
            }
    }

    /** 
     * Remove a Collection of children ServiceBindings. Treats
     * null param as an empty collection.
     */
    public void removeServiceBindings(Collection serviceBindings) 
	throws JAXRException {
	    if (serviceBindings != null) {
                getObject();
		this.serviceBindings.removeAll(serviceBindings);
                setIsModified(true);
	    }
    }
    
    /**
     * Get the service bindings
     */
    public Collection getServiceBindings() throws JAXRException {
        if (this.serviceBindings.size() == 0) {
	    getObject();
	}
        return (Collection) serviceBindings.clone();
    }
    
}
