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


package javax.xml.registry.infomodel;

import java.io.*;
import java.net.*;
import javax.xml.registry.*;
import java.util.*;

/**
 * The RegistryObject class is an abstract base class used by most classes in the model. It provides minimal metadata for registry objects. It also provides methods for accessing related objects that provide additional dynamic metadata for the registry object.
 *  
 * @see RegistryEntry
 * @author Farrukh S. Najmi
 */
public interface RegistryObject extends ExtensibleObject  {
    /** 
	 * Gets the key representing the universally unique ID (UUID) for this object. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the Key for this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    Key getKey() throws JAXRException;

    /** 
	 * Gets the textual description for this object. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the description for this object which must not be null
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    InternationalString getDescription() throws JAXRException;

    /** 
	 * Sets the context independent textual description for this object. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param description the description for this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void setDescription(InternationalString description) throws JAXRException;

    /**
     * @associates <{AccessControlPolicy}>
     * @supplierCardinality 1
     */
    //AccessControlPolicy getAccessControlPolicy() throws JAXRException;

    /** 
	 * Gets the user-friendly name of this object.
	 *  
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the name for this object which must not be null.
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    InternationalString getName() throws JAXRException;


    /** 
	 * Sets user-friendly name of object in repository. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param name	the name for this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void setName(InternationalString name) throws JAXRException;
	
	/** 
	 * Sets the key representing the universally unique ID (UUID) for this object. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param key the key for this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void setKey(Key key) throws JAXRException;
	
	/** 
	 * Returns a registry provider specific XML representation of this Object.
	 * This may be used as a last resort back door way to get to a provider specific
	 * information element that is not accessible via the API.
	 * Implementation may choose to throw a UnsupportedCapabilityException.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the String containing the XML representation for this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	String toXML() throws JAXRException;

	/**
	 * Adds specified Classification to this object.
	 * Silently replaces the classifiedObject in Classification with reference to this object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param classification	the Classification being added
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void addClassification(Classification classification) throws JAXRException;

	/**
	 * Adds specified Classifications to this object.
	 * Silently replaces the classifiedObject in Classifications with reference to this object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param classifications	the Collection of Classifications being added
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void addClassifications(Collection classifications) throws JAXRException;

	/**
	 * Removes specified Classification from this object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param classification	the Classification being removed
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void removeClassification(Classification classification) throws JAXRException;

	/**
	 * Removes specified Classifications from this object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param classifications	the Collection of Classifications being removed
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void removeClassifications(Collection classifications) throws JAXRException;

	/**
	 * Replaces all previous Classifications with specified
	 * Classifications.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param classifications	the Collection of Classifications being set
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void setClassifications(Collection classifications) throws JAXRException;

	/**
	 * Gets the Classification instances that classify this object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
     * @see javax.xml.registry.infomodel.Classification
     * @return Collection of Classification instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     *
     * @link aggregationByValue
     * @associates <{Classification}>
     * @supplierCardinality 0..*
     * @label classifications
	 *
	 */
	Collection getClassifications() throws JAXRException;
	
    /**
     * Returns the complete audit trail of all requests that effected a state change in this  object as an ordered Collection
     * of AuditableEvent objects.
     *
     * <p><DL><DT><B>Capability Level: 1 </B><DD>This method must throw UnsupportedCapabilityException in lower capability levels.</DL> 	 
     *
     * @see javax.xml.registry.infomodel.AuditableEvent
     * @return Collection of AuditableEvent instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     *
     * @link aggregationByValue
     * @associates <{AuditableEvent}>
     * @supplierCardinality 1..*
     * @label auditTrail
     * 
     */
    Collection getAuditTrail() throws JAXRException;

    /**
     * Adds specified Association to use this object as source.
	 * Silently replaces the sourceObject in Association with reference to this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param association	the Association being added
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void addAssociation(Association association) throws JAXRException;

    /**
     * Adds specified Associations to use this object as source.
     * Silently replaces the sourceObject in Associations with reference to this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param associations	the Collection of Associations being added
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void addAssociations(Collection associations) throws JAXRException;

    /**
     * Removes specified Association from this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param association	the Association being removed
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void removeAssociation(Association association) throws JAXRException;

    /**
     * Removes specified Associations from this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param associations	the Collection of Associations being removed
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void removeAssociations(Collection associations) throws JAXRException;

    /**
     * Replaces all previous Associations from this object with
	 * specified Associations.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param associations	the Collection of Associations being set
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setAssociations(Collection associations) throws JAXRException;

    /**
     * Gets all Associations where this object is source.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @see javax.xml.registry.infomodel.Association
     * @return Collection of Association instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     *
     */
    Collection getAssociations() throws JAXRException;

    /**
     * Returns the collection of RegistryObject instances associated with this object.
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B><DD>This method must throw UnsupportedCapabilityException in lower capability levels.</DL> 	 
	 *
     * @see javax.xml.registry.infomodel.RegistryObject
     * @return Collection of RegistryObject instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     *
     * @associationAsClass <{Association}>
     * @supplierCardinality 0..*
     * @clientCardinality 0..*
     * @associates <{RegistryObject}>
     * @undirected
     * @label associatedObjects
     */
    Collection getAssociatedObjects() throws JAXRException;

    /**
     * Adds specified ExternalIdentifier as an external identifier to this object.
     * Silently replaces the registryObject in ExternalIdentifier with reference to this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param externalIdentifier	the ExternalIdentifier being added
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void addExternalIdentifier(ExternalIdentifier externalIdentifier) throws JAXRException;

    /**
     * Adds specified ExternalIdentifiers as an external identifiers to this object.
     * Silently replaces the registryObject in ExternalIdentifiers with reference to this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param externalIdentifiers	the Collection of ExternalIdentifiers being added
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void addExternalIdentifiers(Collection externalIdentifiers) throws JAXRException;

    /**
     * Removes specified ExternalIdentifier as an external identifier from this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param externalIdentifier	the ExternalIdentifier being removed
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void removeExternalIdentifier(ExternalIdentifier externalIdentifier) throws JAXRException;

    /**
     * Removes specified ExternalIdentifiers as an external identifiers from this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param externalIdentifiers	the Collection of ExternalIdentifiers being removed
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void removeExternalIdentifiers(Collection externalIdentifiers) throws JAXRException;

    /**
     * Replaces all previous external identifiers with specified
     * Collection of ExternalIdentifiers as an external identifier.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param externalIdentifiers	the Collection of ExternalIdentifiers being set
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setExternalIdentifiers(Collection externalIdentifiers) throws JAXRException;

    /**
     * Returns the ExternalIdentifiers associated with this object
     * that are external identifiers for this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @see javax.xml.registry.infomodel.ExternalIdentifier
     * @return Collection of ExternalIdentifier instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     *
     * @associates <{ExternalIdentifier}>
     * @supplierCardinality 0..*
     * @undirected
     * @supplierRole externalIdentifiers
     * @link aggregationByValue
     */
    Collection getExternalIdentifiers() throws JAXRException;

    /**
     * Adds specified ExternalLink to this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param externalLinks	the ExternalLink being added
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void addExternalLink(ExternalLink externalLink) throws JAXRException;

    /**
     * Adds specified ExternalLinks to this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param externalLinks	the Collection of ExternalLinks being added
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void addExternalLinks(Collection externalLinks) throws JAXRException;

    /**
     * Removes specified ExternalLink from this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param externalLink	the ExternalLink being removed
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void removeExternalLink(ExternalLink externalLink) throws JAXRException;

    /**
     * Removes specified ExternalLinks from this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param externalLinks	the Collection of ExternalLinks being removed
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void removeExternalLinks(Collection externalLinks) throws JAXRException;

    /**
     * Replaces all previous ExternalLinks with specified
     * ExternalLinks.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param externalLink	the Collection of ExternalLinks being set
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setExternalLinks(Collection externalLinks) throws JAXRException;

    /**
     * Returns the ExternalLinks associated with this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @see javax.xml.registry.infomodel.ExternalLink
     * @return Collection of ExternalLink instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     *
     * @associationAsClass <{Association}>
     * @associates <{ExternalLink}>
     * @supplierCardinality 0..*
     * @clientCardinality 1..*
     * @undirected
     * @supplierRole externalLinks
     * @clientRole linkedObjects
     */
    Collection getExternalLinks() throws JAXRException;

    /**
     * Gets the object type that best describes the RegistryObject.
     *
     * <p><DL><DT><B>Capability Level: 1 </B><DD>This method must throw UnsupportedCapabilityException in lower capability levels.</DL> 	 
     *
     * @return the object type as a Concept within the pre-defined ClassificationScheme named ObjectType 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    Concept getObjectType() throws JAXRException;

	/**
	 * Gets the Organization that submitted this RegistryObject.
	 *
	 * @return the Organization that submitted this object to the registry
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	Organization getSubmittingOrganization() throws JAXRException;

    /**
     * Returns the Package associated with this object.
     *
     * <p><DL><DT><B>Capability Level: 1 </B><DD>This method must throw UnsupportedCapabilityException in lower capability levels.</DL> 	 
     *
     * @see javax.xml.registry.infomodel.RegistryPackage
     * @return Collection of RegistryPackage instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     *
     * @supplierCardinality 0..*
     * @clientCardinality 1..*
     * @associates <{RegistryPackage}>
     * @undirected
     * @clientRole memberObjects
     * @supplierRole packages
     * @associationAsClass <{Association}>
     */
    Collection getRegistryPackages() throws JAXRException;

	/**
	 * Returns the LifeCycleManager that created this object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the LifeCycleManager objet that created this object 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	LifeCycleManager getLifeCycleManager() throws JAXRException;

    /**
     * @supplierCardinality 0..* 
     * @supplierRole slots
     * @link aggregationByValue
     */
    /*#Slot lnkSlot;*/
	
}
