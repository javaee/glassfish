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
 * Implementation of RegistryObject interface
 *
 * @author Kathy Walsh
 * @author Bobby Bissett		  
 */
public abstract class RegistryObjectImpl extends ExtensibleObjectImpl implements RegistryObject, Serializable {
    
    ArrayList associations;
    ArrayList classifications;
    ArrayList externalIdentifiers;
    ArrayList externalLinks;
    InternationalString description;
    InternationalString name;
    Key key;
    Organization submittingOrganization;
    LifeCycleManager lifeCycleManager;

    transient boolean isRetrieved = false;
    transient boolean isLoaded = false;
    transient boolean isNew = true;
    transient boolean isModified = false;
    transient boolean isDeleted = false;
    transient String serviceId;
    RegistryService registryService;
    transient boolean areAssociationsLoaded = false;
    UDDIObjectCache objectManager;

    /**
     * Default constructor
     */
    public RegistryObjectImpl() {
        super();
        associations = new ArrayList();
        classifications = new ArrayList();
        externalIdentifiers = new ArrayList();
        externalLinks = new ArrayList();
        
        // these can not be null
        description = new InternationalStringImpl();
        name = new InternationalStringImpl();
    }
	
    /**
     * Utility constructor used when key is known
     */
    public RegistryObjectImpl(Key key) {
	this();
	this.key = key;
    }
    
    /**
     * Utility constructor used when key, name,
     * and description are known
     */
    public RegistryObjectImpl(Key key, String description, String name) {
        this(key);
        this.description = new InternationalStringImpl(description);
        this.name = new InternationalStringImpl(name);
    }
    
    /**
     * Override superclass to set isModified
     */
    public void addSlot(Slot slot) throws JAXRException {
        super.addSlot(slot);
        setIsModified(true);
    }

    /**
     * Override superclass to set isModified
     */
    public void addSlots(Collection slots) throws JAXRException {
        super.addSlots(slots);
        setIsModified(true);
    }

    /**
     * Override superclass to set isModified
     */
    public void removeSlot(String slotName) throws JAXRException {
        super.removeSlot(slotName);
        setIsModified(true);
    }

    /**
     * Override superclass to set isModified
     */
    public void removeSlots(Collection slotNames) throws JAXRException {
        super.removeSlots(slotNames);
        setIsModified(true);
    }

    /**
     * Adds specified Association to use this object as source.
     */
    public void addAssociation(Association association) throws JAXRException {
	if (association == null) {
	    return;
	}
	association.setSourceObject(this);
	associations.add(association);
        setIsModified(true);
    }

    /**
     * Adds specified Associations to use this object as source. Treat
     * null parameter as empty collection.
     */
    public void addAssociations(Collection associations) throws JAXRException {
        if (associations == null) {
	    return;
	}
	Iterator iter = associations.iterator();
	try {
	    while (iter.hasNext()) {
		addAssociation((Association) iter.next());
	    }
	} catch (ClassCastException e) {
	    throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("RegistryObjectImpl:Objects_in_collection_must_be_Associations"), e);
	}
        setIsModified(true);
    }

    /**
     * Removes specified Association from this object.
     */
    public void removeAssociation(Association association) throws JAXRException {
	if (association != null) {
	    associations.remove(association);
            setIsModified(true);
	}
    }

    /**
     * Removes specified Associations from this object. Treat null
     * parameter as empty collection.
     */
    public void removeAssociations(Collection associations) throws JAXRException {
	if (associations != null) {
	    this.associations.removeAll(associations);
            setIsModified(true);
	}
    }

    /**
     * Gets all Associations where this object is source.
     */
    public Collection getAssociations() throws JAXRException {
        if (this.associations.isEmpty()) {
            if (this instanceof Organization) {
                getOrganizationAssociations();
            } else {
                getObject();
            }
	}
	return (Collection) associations.clone();
    }
	
    /**
     * Replaces all previous Associations from this object with
     * specified Associations. Treat null param as empty collection.
     */
    public void setAssociations(Collection associations) throws JAXRException {
	this.associations.clear();
	addAssociations(associations);
        setIsModified(true);
    }

    /**
     * Internal method to set if associations are loaded
     */
    public void setAssociationsLoaded(boolean loaded) {
        areAssociationsLoaded = loaded;
    }
    
    /**
     * Internal method to check if associations are loaded
     */
    public boolean areAssociationsLoaded() {
        return areAssociationsLoaded;
    }
    
    /**
     * Adds specified Classification to this object.
     */
    public void addClassification(Classification classification) throws JAXRException {
        if (classification == null) {
	    return;
	}
        getObject();
	classification.setClassifiedObject(this);
	classifications.add(classification);
        setIsModified(true);
    }

    /**
     * Adds specified Classifications to this object. Treat null
     * parameter as empty collection.
     */
    public void addClassifications(Collection classifications) throws JAXRException {
        if (classifications == null) {
	    return;
        }
	Iterator iter = classifications.iterator();
	try {
	    while (iter.hasNext()) {
		addClassification((Classification) iter.next());
	    }
	} catch (ClassCastException e) {
	    throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("RegistryObjectImpl:Objects_in_collection_must_be_Classifiations"), e);
	}
        setIsModified(true);
    }
    
    /**
     * Removes specified Classification from this object.
     */
    public void removeClassification(Classification classification) throws JAXRException {
        if (classification != null) {
            getObject();
            classifications.remove(classification);
            setIsModified(true);
        }
    }

    /**
     * Removes specified Classifications from this object. Treat null
     * parameter as empty collection.
     */
    public void removeClassifications(Collection classifications) throws JAXRException {
        if (classifications != null) {
            getObject();
	    this.classifications.removeAll(classifications);
            setIsModified(true);
        }
    }

    /**
     * Gets the Classification that classify this object.
     */
    public Collection getClassifications() throws JAXRException {
	if (classifications.isEmpty()) {
	    getObject();
	}
	return (Collection) classifications.clone();
    }

    /**
     * Replaces all previous Classifications with specified
     * Classififications. Treat null param as empty collection.
     */
    public void setClassifications(Collection classifications) throws JAXRException {
        getObject();
	this.classifications.clear();
	addClassifications(classifications);
        setIsModified(true);
    }

    /**
     * Adds specified ExternalIdentifier to this object.
     */
    public void addExternalIdentifier(ExternalIdentifier identifier) 
	throws JAXRException {
	    if (identifier == null) {
		return;
	    }
            getObject();
	    ((ExternalIdentifierImpl) identifier).setRegistryObject(this);
	    externalIdentifiers.add(identifier);
            setIsModified(true);
    }

    /**
     * Adds specified ExternalIdentifiers to this object. Treat null
     * parameter as empty collection.
     */
    public void addExternalIdentifiers(Collection identifiers) 
	throws JAXRException {
	    if (identifiers == null) {
		return;
	    }
	    Iterator iter = identifiers.iterator();
	    try {
		while (iter.hasNext()) {
		    addExternalIdentifier((ExternalIdentifier) iter.next());
		}	   
	    } catch (ClassCastException e) {
		throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("RegistryObjectImpl:Objects_in_collection_must_be_ExternalIdentifers"), e);
	    }
            setIsModified(true);
    }
	
    /**
     * Removes specified ExternalIdentifier from this object.
     */
    public void removeExternalIdentifier(ExternalIdentifier externalIdentifier) 
	throws JAXRException {
	    if (externalIdentifier != null) {
                getObject();
		externalIdentifiers.remove(externalIdentifier);
                setIsModified(true);
	    }
    }

    /**
     * Removes specified ExternalIdentifiers from this object. Treat
     * null parameter as empty collection.
     */
    public void removeExternalIdentifiers(Collection externalIdentifiers) 
	throws JAXRException {
	    if (externalIdentifiers != null) {
                getObject();
		this.externalIdentifiers.removeAll(externalIdentifiers);
                setIsModified(true);
	    }			
    }

    /**
     * Returns the ExternalIdentifiers associated with this object.
     */
    public Collection getExternalIdentifiers() throws JAXRException {
	if (externalIdentifiers.isEmpty()) {
	    getObject();
	}
        return (Collection) externalIdentifiers.clone();
    }

    /**
     * Replaces all previous ExternalIdentifiers with specified
     * ExternalIdentifiers. Treat null param as empty collection.
     */
    public void setExternalIdentifiers(Collection externalIdentifiers) 	
	throws JAXRException {
            getObject();
	    this.externalIdentifiers.clear();
	    addExternalIdentifiers(externalIdentifiers);
            setIsModified(true);
    }
	
    /**
     * Adds specified ExternalLink to this object. Can only be added
     * to Organization, Concept, ClassificationScheme, and SpecificationLink.
     */
    public void addExternalLink(ExternalLink link) throws JAXRException {
	throw new UnsupportedCapabilityException(
	    ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("RegistryObjectImpl:ExternalLinks_may_be_added_only_to_Organization,_Concept,_ClassificationScheme,_and_SpecificationLink"));
    }
       
    /**
     * Adds specified ExternalLinks to this object. Can only be added
     * to Organization, Concept, ClassificationScheme, and SpecificationLink.
     */
    public void addExternalLinks(Collection links) throws JAXRException {
	throw new UnsupportedCapabilityException(
	    ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("RegistryObjectImpl:ExternalLinks_may_be_added_only_to_Organization,_Concept,_ClassificationScheme,_and_SpecificationLink"));
    }
   
    /**
     * Removes specified ExternalLink from this object.
     */
    public void removeExternalLink(ExternalLink externalLink) throws JAXRException {
	if (externalLink != null) {
            getObject();
	    externalLinks.remove(externalLink);
            setIsModified(true);
	}
    }

    /**
     * Removes specified ExternalLinks from this object. Treat null parameter
     * as empty collection.
     */
    public void removeExternalLinks(Collection externalLinks) throws JAXRException {
	if (externalLinks != null) {
            getObject();
	    this.externalLinks.removeAll(externalLinks);
            setIsModified(true);
	}   
    }

    /**
     * Returns the ExternalLinks associated with this object.
     */
    public Collection getExternalLinks() throws JAXRException {
        if (externalLinks.isEmpty()) {
            getObject();
	}
        return (Collection) externalLinks.clone();
    }
	
    /**
     * Sets specified ExternalLinks to this object. Can only be added
     * to Organization, Concept, ClassificationScheme, and SpecificationLink.
     */
    public void setExternalLinks(Collection links) throws JAXRException {
	throw new UnsupportedCapabilityException(
	    ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("RegistryObjectImpl:ExternalLinks_may_be_added_only_to_Organization,_Concept,_ClassificationScheme,_and_SpecificationLink"));
    }

    /**
     * Gets the context independent textual description for this object.
     * Description will not be null.
     */
    public InternationalString getDescription() throws JAXRException {
        String value = description.getValue();
        if ((value == null) || (value.equals(""))) {
            getObject();
        }
        return description;
    }

    /**
     * Set the desciption for the object
     */
    public void setDescription(InternationalString description) throws JAXRException {
	if (description != null) {
            getObject();
	    this.description = description;
	} else {
	    this.description = new InternationalStringImpl();
	}
        setIsModified(true);
    }        

    /**
     * Gets user friendly context independent name of object in repository.
     */
    public InternationalString getName() throws JAXRException {
        String value = name.getValue();
	if ((value == null) || (value.equals(""))) {
	    getObject();
	}
	return name;
    }

    /**
     * Set the name for the object
     */
    public void setName(InternationalString name) throws JAXRException {
	if (name != null) {
            getObject();
	    this.name = name;
	} else {
	    this.name = new InternationalStringImpl();
	}
        setIsModified(true);
    }

    /**
     * Get registry key identifying this object
     */
    public Key getKey() throws JAXRException {
	return key;
    }
    
    /**
     * Set registry key on this object
     */
    public void setKey(Key key) throws JAXRException {
	this.key = key;
        setIsModified(true);
    }

    public Organization getSubmittingOrganization() throws JAXRException{
	if (submittingOrganization == null) {
	    getObject();
	}
        return submittingOrganization;
    }

    /**
     * Internal method for setting submitting organization
     */
    public void setSubmittingOrganization(Organization org) throws JAXRException {
        submittingOrganization = org;
    }
    
    /**
     * Returns the Connection associated with this object.
     */
    public Connection getConnection() throws JAXRException {
	
	if (lifeCycleManager != null) {	
	RegistryServiceImpl rService =
	    (RegistryServiceImpl) lifeCycleManager.getRegistryService();
            return (rService != null ? rService.getConnection() : null);
        }
        return null;
    }

    public LifeCycleManager getLifeCycleManager() {
        return lifeCycleManager;
    }
    
    /**
     * Internal method for setting manager when object
     * is created
     */
    public void setLifeCycleManager(LifeCycleManager manager) {
        lifeCycleManager = manager;
    }
    
    public void setServiceId(String serviceId) {
	this.serviceId = serviceId;
    }
	
    public String getServiceId() {
	return this.serviceId;
    }
    
    public void setRegistryService(RegistryService service) {
        this.registryService = service;
    }
    
    public RegistryService getRegistryService() {
        return this.registryService;
    }
    
    public synchronized void setStatusFlags(boolean retrieved, boolean loaded, 
					    boolean isNew) {
	this.isRetrieved = retrieved;
	this.isLoaded = loaded;
	this.isNew = isNew;	
    }
      
    public synchronized boolean isLoaded(){
	return isLoaded;
    }
	
    public synchronized void setIsLoaded(boolean loaded){
	isLoaded = loaded;
    }
	
    public synchronized boolean isRetrieved(){
	return isRetrieved;
    }
	
    public synchronized void setIsRetrieved(boolean retrieved){
	isDeleted = false;
	isRetrieved = retrieved;
    }
	
    public synchronized boolean isNew(){
	return isNew;
    }
	
    public synchronized void setIsNew(boolean isNew){	
	this.isNew = isNew;
    }
	
    public boolean isModified(){
	return isModified;
    }
	
    public void setIsModified(boolean modified){
	isModified = modified;
    }
	
    public synchronized boolean isDeleted(){
	return isDeleted;
    }
	
    public synchronized void setIsDeleted(boolean deleted){
	isDeleted = deleted;
    }

    /**
     * Internal method
     */
    void getObject() throws JAXRException {
        
	if (isDeleted()) {
	    return;
	}
		
	if (isRetrieved() && !isLoaded())  {
            if (objectManager == null) 
            objectManager = getObjectManager();
            
            if (objectManager == null)
                throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("RegistryObjectImpl:Can_not_obtain_Object_detail"));	
	    objectManager.fetchObject(this, serviceId);
	}		
    }
    
    UDDIObjectCache getObjectManager() throws JAXRException {
        if (registryService == null) {
            if (lifeCycleManager != null)
             registryService = 
                lifeCycleManager.getRegistryService();
        }
        if (registryService != null){
            objectManager = 
               ((RegistryServiceImpl)registryService).getObjectManager(); 
        }
         return objectManager;
    }
              
    /**
     * Internal method
     */
    void getOrganizationAssociations() throws JAXRException {
      
        if (isDeleted() || isNew())             
            return;
       
        if (isRetrieved() && !areAssociationsLoaded()) {            
            if (objectManager == null) 
                objectManager = getObjectManager();
            if (objectManager != null)           
            objectManager.fetchAssociations(this, serviceId);
        }
    }

    /*
     * Method to allow child ExternalLinks to get a
     * sequenceId used in key generation
     */
    int getSequenceId(ExternalLink link) {
	return externalLinks.indexOf(link);
    }

    /** 
     * Implementation may choose to throw an
     * UnsupportedCapabilityException. Currently
     * not implemented.
     */
    public String toXML() throws JAXRException{
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public Collection getAssociatedObjects() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public Concept getObjectType() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
	
    /**
     * Level 1 method
     */
    public void setObjectType(Concept objectType) throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public Collection getAuditTrail() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }
   
    /**
     * Level 1 method
     */
    public Collection getRegistryPackages() throws JAXRException {
	throw new UnsupportedCapabilityException();
    }

}
