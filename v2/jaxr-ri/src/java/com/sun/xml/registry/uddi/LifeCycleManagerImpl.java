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


package com.sun.xml.registry.uddi;

import com.sun.xml.registry.common.*;
import com.sun.xml.registry.common.util.*;
import com.sun.xml.registry.uddi.infomodel.*;
import java.util.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/**
 * Class Declaration for LifeCycleManagerImpl
 * @see
 * @author Kathy Walsh
 */
public class LifeCycleManagerImpl implements LifeCycleManager {
    RegistryServiceImpl service;
    UDDIMapper uddi;
     
    String[] names = {
        
        ASSOCIATION,
        CLASSIFICATION,
        CLASSIFICATION_SCHEME,
        CONCEPT,
        EMAIL_ADDRESS,
        EXTERNAL_IDENTIFIER,
        EXTERNAL_LINK,
        INTERNATIONAL_STRING,
	KEY,
        LOCALIZED_STRING,
        ORGANIZATION,
        PERSON_NAME,
        POSTAL_ADDRESS,
        SERVICE,
        SERVICE_BINDING,
        SLOT,
        SPECIFICATION_LINK,
        TELEPHONE_NUMBER,
        USER
    };
    
    Class[] infoModelClass = {
        com.sun.xml.registry.uddi.infomodel.AssociationImpl.class,
        com.sun.xml.registry.uddi.infomodel.ClassificationImpl.class,
        com.sun.xml.registry.uddi.infomodel.ClassificationSchemeImpl.class,
        com.sun.xml.registry.uddi.infomodel.ConceptImpl.class,
        com.sun.xml.registry.uddi.infomodel.EmailAddressImpl.class,
        com.sun.xml.registry.uddi.infomodel.ExternalIdentifierImpl.class,
        com.sun.xml.registry.uddi.infomodel.ExternalLinkImpl.class,
        com.sun.xml.registry.uddi.infomodel.InternationalStringImpl.class,
	com.sun.xml.registry.uddi.infomodel.KeyImpl.class,
        com.sun.xml.registry.uddi.infomodel.LocalizedStringImpl.class,
        com.sun.xml.registry.uddi.infomodel.OrganizationImpl.class,
        com.sun.xml.registry.uddi.infomodel.PersonNameImpl.class,
        com.sun.xml.registry.uddi.infomodel.PostalAddressImpl.class,
        com.sun.xml.registry.uddi.infomodel.ServiceImpl.class,
        com.sun.xml.registry.uddi.infomodel.ServiceBindingImpl.class,
        com.sun.xml.registry.uddi.infomodel.SlotImpl.class,
        com.sun.xml.registry.uddi.infomodel.SpecificationLinkImpl.class,
        com.sun.xml.registry.uddi.infomodel.TelephoneNumberImpl.class,
        com.sun.xml.registry.uddi.infomodel.UserImpl.class
    };
	
	   
    
    public LifeCycleManagerImpl() {
        // Fix for CRs 6520297 and 6520354
        String country = Locale.getDefault().getCountry();        
        // Default country is 'null' on Solaris.  Use 'US' as default country
        // and 'en' as default languge.
        if (country == null || country == "") {
            Locale.setDefault(Locale.US);
        }
        System.out.println("Default locale: "+Locale.getDefault().toString());
    }
    
    public LifeCycleManagerImpl(RegistryServiceImpl service){
        this();
        this.service = service;
        this.uddi = service.getUDDIMapper();
    }
    
    public RegistryService getRegistryService(){
        return service;
    }
    
    /**
     * Factory method for creating instances of information model
     * interfaces. To create an Organization use it as follows:
     * <p>
     * Organization org = lifeCycleMgr.createObject(LifeCycleManager.Organization);
     *
     * @param interfaceName Is the unqualified name of an interface in the javax.xml.registry.infomodel package
     * <p>
     * Throws InvalidRequestException if the interface is not an interface in
     * from javax.xml.registry.infomodel package.
     */
    public Object createObject(String className) throws JAXRException {
        try {
            for (int i = 0; i < names.length; i++) {
                if (className.equals(names[i])) {
                    Class infoClass = infoModelClass[i];
                    Object object = infoClass.newInstance();
                    if (object instanceof RegistryObjectImpl) {
                        RegistryObjectImpl ro = (RegistryObjectImpl) object;
                        ro.setLifeCycleManager(this);
                        ro.setRegistryService(service);
                        ro.setIsModified(true);
                    }
                    return object;
                }
            }
            
            if ((className.equals(AUDITABLE_EVENT)) ||
                (className.equals(EXTRINSIC_OBJECT)) ||
                (className.equals(REGISTRY_ENTRY)) ||
                (className.equals(VERSIONABLE)) ||
                (className.equals(REGISTRY_PACKAGE))) {
                    throw new UnsupportedCapabilityException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("LifeCycleManagerImpl:Can_not_create_object_of_type_") + className + ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("LifeCycleManagerImpl:_at_Capability_Level_0"));
            } else {
                throw new InvalidRequestException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("LifeCycleManagerImpl:Class_Name_is_not_an_interface_in_the_javax.xml.registry.infomodel_package"));
            }
        } catch (java.lang.InstantiationException ie) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("LifeCycleManagerImpl:InstantiationException_in_createObject()_"), ie);
        } catch (java.lang.IllegalAccessException iae) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("LifeCycleManagerImpl:IllegalAccessException_in_createObject()_"), iae);
        }
    }
    
  

    /**
     * Create an Association instance using the specified
     * parameters. The sourceObject is left null and will be set
     * wen the Association is added to a RegistryObject.
     *
     */
    public Association createAssociation(RegistryObject targetObject,
        Concept associationType) throws JAXRException {
            AssociationImpl association =
                new AssociationImpl(targetObject, associationType);
            association.setLifeCycleManager(this);
            association.setIsModified(true);
            return association;
    }
    
    /**
     * Create a Classification instance for an external
     * Classification using the specified name and value that identifies
     * a taxonomy element within specified ClassificationScheme.
     */
    public Classification createClassification(ClassificationScheme scheme,
        String name, String value) throws JAXRException {
            ClassificationImpl classification =
                new ClassificationImpl(scheme, name, value);
            classification.setLifeCycleManager(this);
            classification.setIsModified(true);
            return classification;
    }
    
    /**
     * Create a Classification instance for an internal
     * Classification using the specified Concept which identifies
     * a taxonomy element within an internal ClassificationScheme.
     * <p>
     * Throws InvalidRequestException if the Concept is not under
     * a ClassificationScheme.
     *
     */
    public Classification createClassification(Concept concept)
        throws JAXRException, InvalidRequestException {
            if (concept.getClassificationScheme() == null) {
                throw new InvalidRequestException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("LifeCycleManagerImpl:Concept_has_no_ClassificationScheme"));
            }
            ClassificationImpl classification = new ClassificationImpl(concept);
            classification.setLifeCycleManager(this);
            classification.setIsModified(true);
            return classification;
    }
    
    /**
     * Creates a ClassificationScheme from a Concept that has no
     * ClassificationScheme or parent Concept.
     * <p>
     * This method is
     * provided to allow for Concepts returned by the BusinessQueryManager
     * findConcepts call to be safely cast to ClassificationScheme. It
     * is up to the programer to make sure that the Concept is indeed
     * semantically a ClassificationScheme.
     * <p>
     * This method is necessary because in UDDI a tModel may serve
     * multiple purposes and there is no way to know when a tModel
     * maps to a Concept and when it maps to a ClassificationScheme.
     * UDDI leaves the determination to the programmer and consequently so does this
     * method.
     * <p>
     * Throws InvalidRequestException if the Concept has a parent Concept
     * or is under a ClassificationScheme.
     *
     */
    public ClassificationScheme createClassificationScheme(Concept concept)
        throws JAXRException, InvalidRequestException {
            if ((concept.getClassificationScheme() == null) &&
                (concept.getParentConcept() == null)) {
                    ClassificationSchemeImpl scheme =
                        new ClassificationSchemeImpl(concept);
                    scheme.setLifeCycleManager(this);
                    scheme.setIsModified(true);
                    return scheme;
            } else {
                throw new InvalidRequestException();
            }
    }
    
    
    public ClassificationScheme createClassificationScheme(String name,
        String description) throws JAXRException, InvalidRequestException {
            ClassificationSchemeImpl scheme =
                new ClassificationSchemeImpl(name, description);
            scheme.setLifeCycleManager(this);
            scheme.setIsModified(true);
            return scheme;
    }
    
    
    public Concept createConcept(RegistryObject parent, String name, String value) throws JAXRException {
        ConceptImpl concept = new ConceptImpl(parent, name, value);
        concept.setLifeCycleManager(this);
        concept.setIsModified(true);
        return concept;
    }
    
    public Concept createConcept(RegistryObject parent, InternationalString name, String value) 
        throws JAXRException {
            ConceptImpl concept = new ConceptImpl(parent, "", value);
            concept.setName(name);
            concept.setLifeCycleManager(this);
            concept.setIsModified(true);
            return concept;
    }
    
    
    public EmailAddress createEmailAddress(String address)
        throws JAXRException {
            return new EmailAddressImpl(address);
    }
    
    public EmailAddress createEmailAddress(String address, String type)
        throws JAXRException {
            return new EmailAddressImpl(address, type);
    }
    
    /**
     * Create an ExternalIdentifier instance using the specified
     * parameters.
     *
     */
    public ExternalIdentifier createExternalIdentifier(
        ClassificationScheme identificationScheme, String name, String value)
        throws JAXRException {
            ExternalIdentifierImpl identifier =
                new ExternalIdentifierImpl(identificationScheme, name, value);
            identifier.setLifeCycleManager(this);
            identifier.setIsModified(true);
            return identifier;
    }
    
    /**
     * Create an ExternalLink instance using the specified
     * parameters.
     *
     */
    public ExternalLink createExternalLink(String externalURI,
        String description) throws JAXRException {
            ExternalLinkImpl link =
                new ExternalLinkImpl(externalURI, description);
            link.setLifeCycleManager(this);
            link.setIsModified(true);
            return link;
    }
    
    /**
     * Create a Key instance using the specified
     * parameters.
     *
     */
    public Key createKey(String id) throws JAXRException {
        return new KeyImpl(id);
    }
    
    /**
     * Create a PersonName instance using the specified
     * parameters.
     *
     */
    public PersonName createPersonName(String fullName) throws JAXRException {
        return new PersonNameImpl(fullName);
    }
    
    /**
     * Create a PostalAddress instance using the specified
     * parameters.
     *
     */
    public PostalAddress createPostalAddress(String streetNumber, String street,
        String city, String stateOrProvince, String country, String postalCode,
        String type) throws JAXRException {
	    PostalAddressImpl address = new PostalAddressImpl(streetNumber, street, city,
                stateOrProvince, country, postalCode, type);
	    if (service != null) {
		address.setPostalScheme(service.getDefaultPostalScheme());
	    }
	    return address;
    }
    
    public Service createService(String name) throws JAXRException {
        ServiceImpl service = new ServiceImpl(name);
        service.setLifeCycleManager(this);
        service.setIsModified(true);
        return service;
    }
    
    public ServiceBinding createServiceBinding() throws JAXRException {
        ServiceBindingImpl binding = new ServiceBindingImpl();
        binding.setLifeCycleManager(this);
        binding.setIsModified(true);
        return binding;
    }
    
    
    /**
     * Create a Slot instance using the specified
     * parameters.
     *
     */
    public Slot createSlot(String name, String value, String slotType)
        throws JAXRException {
            return new SlotImpl(name, value, slotType);
    }
    
    /**
     * Create a Slot instance using the specified
     * parameters.
     *
     */
    public Slot createSlot(String name, Collection values, String slotType)
        throws JAXRException {
            return new SlotImpl(name, values, slotType);
    }
    
    
    public SpecificationLink createSpecificationLink() throws JAXRException {
        SpecificationLinkImpl specificationLink = new SpecificationLinkImpl();
        specificationLink.setLifeCycleManager(this);
        specificationLink.setIsModified(true);
        return specificationLink;
    }
    
    public TelephoneNumber createTelephoneNumber() throws JAXRException {
        return new TelephoneNumberImpl();
    }
    
    public User createUser() throws JAXRException {
        UserImpl user = new UserImpl();
        user.setLifeCycleManager(this);
        user.setIsModified(true);
        return user;
    }
    
    
    /**
     * Saves one or more CataloguedObjects to the registry.
     * If an object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveObjects(Collection cataloguedObjects)
        throws JAXRException {
            if (!service.getConnection().isSynchronous()) {
                BulkResponseImpl response = new BulkResponseImpl();
                response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
                response.setRequestId(Utility.getInstance().generateUUID());
                service.storeBulkResponse(response);
                FuturesRequestManager.invokeCommand(
                    new JAXRCommand.SaveObjectsCommand(service, response,
                        cataloguedObjects));
                return response;
            } else {
                return uddi.saveObjects(cataloguedObjects);
            }
    }
    
    /**
     * Approves one or more previously submitted objects specified by a
     * collection of Keys for the objects.
     *
     * Reminder for V2
     */
    //void approveObjects(Collection keys) throws JAXRException;
    
    /**
     * Checks out one or more previously submitted objects specified by a
     * collection of Keys for the objects. The objects are checked out with a lock
     * so that others cannot checkout the object while its already checked out. Attempt to
     * checkout an already checked out object results in an ObjectCheckedOutException.
     *
     * Reminder for V2 of JAXR
     */
    //Collection checkOutObjects(Collection keys) throws JAXRException, ObjectCheckedOutException;
    
    /**
     * Checks in a previously checked out object. The object must have been checked
     * out by the same user previously. Otherwise an UnexpectedObjectException is thrown.
     * Checking in an object creates a new version of the object which also generates
     * a new Key for the object. The old version is associated with the new version by a
     * an Association of type SupercededBy.
     *
     * Reminder for V2 of JAXR
     */
    //void checkInObjects(Collection keys) throws JAXRException, UnexpectedObjectException;
    
    /**
     * Deletes one or more previously submitted objects from the registry.
     *
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteObjects(Collection keys)
        throws JAXRException {
        throw new UnsupportedCapabilityException();
    }
    
    /**
     * Deletes one or more previously submitted objects from the registry.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteObjects(Collection keys, String objectType)
        throws JAXRException {
            if (!service.getConnection().isSynchronous()) {
                BulkResponseImpl response = new BulkResponseImpl();
                response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
                response.setRequestId(Utility.getInstance().generateUUID());
                service.storeBulkResponse(response);
                FuturesRequestManager.invokeCommand(
                    new JAXRCommand.DeleteObjectsCommand(service, response,
                        keys, objectType));
                return response;
            } else {
                return uddi.deleteObjects(keys, objectType);
            }
    }
    
    
    /**
     * Create a semantic equivalence between the two specified Concepts.
     * This is a convenience method to create an Association with
     * sourceObject as concept1 and targetObject as concept2 and
     * associationType as EquivalentTo.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public void createConceptEquivalence(Concept concept1, Concept concept2)
        throws JAXRException {
        throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("LifeCycleManagerImpl:UDDI_V2_functionality_is_not_supported_in_this_release"));
    }
    
    /**
     * Removes the semantic equivalence, if any, between the specified two Concepts.
     * This is a convenience method to to delete any Association
     * sourceObject as concept1 and targetObject as concept2 and
     * associationType as EquivalentTo.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public void deleteConceptEquivalence(Concept concept1, Concept concept2)
        throws JAXRException {
        throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("LifeCycleManagerImpl:UDDI_V2_functionality_is_not_supported_in_this_release"));
    }
    
    public LocalizedString createLocalizedString(Locale locale, String str) throws JAXRException {
        return new LocalizedStringImpl(locale, str);
    }
    
    public InternationalString createInternationalString(Locale locale, String str) throws JAXRException {
        return new InternationalStringImpl(locale, str);
    }
    
    public InternationalString createInternationalString() throws JAXRException {
        return new InternationalStringImpl();
    }
    
    public InternationalString createInternationalString(String str) throws JAXRException {
        return new InternationalStringImpl(str);
    }

    public Classification createClassification(ClassificationScheme classificationScheme, 
        InternationalString name, String value) throws JAXRException {
            ClassificationImpl classification =
                new ClassificationImpl(classificationScheme, "", value);
            classification.setName(name);
            classification.setLifeCycleManager(this);
            classification.setIsModified(true);
            return classification;
    }
    
    public ClassificationScheme createClassificationScheme(InternationalString name, InternationalString description) 
        throws JAXRException, InvalidRequestException {
            ClassificationSchemeImpl scheme = new ClassificationSchemeImpl();
            scheme.setName(name);
            scheme.setDescription(description);
            scheme.setLifeCycleManager(this);
            scheme.setIsModified(true);
            return scheme;
    }
    
    public LocalizedString createLocalizedString(Locale locale, String s, String charsetName) 
        throws JAXRException {
            LocalizedStringImpl lString = new LocalizedStringImpl(locale, s);
            lString.setCharsetName(charsetName);
            return lString;
    }
    
    public Organization createOrganization(InternationalString name) 
        throws JAXRException {
            OrganizationImpl organization = new OrganizationImpl();
            organization.setName(name);
            organization.setLifeCycleManager(this);
            organization.setIsModified(true);
            return organization;
    }
    
    public Organization createOrganization(String name) 
        throws JAXRException {
	    return createOrganization(createInternationalString(name));
    }
    
    public ExternalLink createExternalLink(java.lang.String externalURI, InternationalString description) 
        throws JAXRException {
            ExternalLinkImpl link = new ExternalLinkImpl(externalURI);
            link.setDescription(description);
            link.setLifeCycleManager(this);
            link.setIsModified(true);
            return link;
    }
    
    public ExternalIdentifier createExternalIdentifier(ClassificationScheme identificationScheme, InternationalString name, java.lang.String value) 
        throws JAXRException {
            ExternalIdentifierImpl exId =
                new ExternalIdentifierImpl(identificationScheme, "", value);
            exId.setName(name);
            exId.setLifeCycleManager(this);
            exId.setIsModified(true);
            return exId;
    }

    public Service createService(InternationalString name) 
        throws JAXRException {
            ServiceImpl service = new ServiceImpl();
            service.setName(name);
            service.setLifeCycleManager(this);
            service.setIsModified(true);
            return service;
    }
    
    /**
     * Level 1 method
     */
    public RegistryPackage createRegistryPackage(InternationalString internationalString) 
        throws JAXRException {
            throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public ExtrinsicObject createExtrinsicObject() throws JAXRException {
        throw new UnsupportedCapabilityException();
    }

    /**
     * Level 1 method
     */
    public PersonName createPersonName(String firstName, String middleName,
        String lastName) throws JAXRException {
            throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public RegistryPackage createRegistryPackage(String name)
        throws JAXRException {
            throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public BulkResponse deprecateObjects(Collection keys)
        throws JAXRException {
            throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public BulkResponse unDeprecateObjects(Collection keys)
        throws JAXRException {
            throw new UnsupportedCapabilityException();
    }
    
    /**
     * Level 1 method
     */
    public ExtrinsicObject createExtrinsicObject(
    javax.activation.DataHandler repositoryItem) throws JAXRException {
        throw new UnsupportedCapabilityException();
    }
    
}
