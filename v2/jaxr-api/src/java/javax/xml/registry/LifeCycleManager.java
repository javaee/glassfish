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

package javax.xml.registry;

import java.util.*;
import javax.xml.registry.infomodel.*;

/**
 * The LifeCycleManager interface is the main interface in the API for managing life cycle
 * operations on objects defined by the information model.
 * <p>
 * The factory methods of this interface must throw an UnsupportedCapabilityException if the client attempts to create an instance of an infomodel interface that is not supported by the capability level of the JAXR provider.
 *
 * @author Farrukh S. Najmi
 */
public interface LifeCycleManager {
    
    /**
     * Constant representing the javax.xml.registry.infomodel.Association interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String ASSOCIATION = "Association";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.AuditableEvent interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String AUDITABLE_EVENT = "AuditableEvent";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.Classification interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String CLASSIFICATION = "Classification";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.ClassificationScheme interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String CLASSIFICATION_SCHEME = "ClassificationScheme";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.Concept interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String CONCEPT = "Concept";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.EmailAddress interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String EMAIL_ADDRESS = "EmailAddress";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.ExternalIdentifier interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String EXTERNAL_IDENTIFIER = "ExternalIdentifier";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.ExternalLink interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String EXTERNAL_LINK = "ExternalLink";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.ExternalLink interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String EXTRINSIC_OBJECT = "ExtrinsicObject";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.InternationalString interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String INTERNATIONAL_STRING = "InternationalString";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.Key interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String KEY = "Key";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.LocalizedString interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String LOCALIZED_STRING = "LocalizedString";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.Organization interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String ORGANIZATION = "Organization";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.PersonName interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String PERSON_NAME = "PersonName";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.PostalAddress interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String POSTAL_ADDRESS = "PostalAddress";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.RegistryEntry interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String REGISTRY_ENTRY = "RegistryEntry";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.RegistryPackage interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String REGISTRY_PACKAGE = "RegistryPackage";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.Service interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String SERVICE = "Service";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.ServiceBinding interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String SERVICE_BINDING = "ServiceBinding";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.Slot interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String SLOT = "Slot";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.SpecificationLink interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String SPECIFICATION_LINK = "SpecificationLink";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.TelephoneNumber interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String TELEPHONE_NUMBER = "TelephoneNumber";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.User interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String USER = "User";
    
    /**
     * Constant representing the javax.xml.registry.infomodel.Versionable interface.
     *
     * @see LifeCycleManager.createObject
     */
    public static final String VERSIONABLE = "Versionable";
    
    /**
     * Creates instances of information model
     * interfaces (factory method). To create an Organization, use this
     * method as follows:
     * <pre>
     * Organization org = (Organization)
     *    lifeCycleMgr.createObject(LifeCycleManager.ORGANIZATION);
     * </pre>
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param interfaceName the unqualified name of an interface in the javax.xml.registry.infomodel package
     *
     * @return an Object that can then be cast to an instance of the interface
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     *
     * @throws InvalidRequestException if the interface is not an interface in
     * the javax.xml.registry.infomodel package
     *
     * @throws UnsupportedCapabilityException if the client attempts to create an instance of an infomodel interface that is not supported by the capability level of the JAXR provider
     */
    public Object createObject(String interfaceName)
    throws JAXRException, InvalidRequestException, UnsupportedCapabilityException;
    
    /**
     * Creates an Association instance using the specified
     * parameters. The sourceObject is left null and will be set
     * when the Association is added to a RegistryObject.
     * <p>
     * Note that for a UDDI provider an Association may only be created
     * between Organizations.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param targetObject the target RegistryObject for the association
     * @param associationType the association type for the Association
     *
     * @return the Association instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Association createAssociation(
    RegistryObject targetObject,
    Concept associationType
    ) throws JAXRException;
    
    /**
     * Creates a Classification instance for an external
     * Classification using the specified String name and String value that identify
     * a taxonomy element within the specified ClassificationScheme.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param scheme the ClassificationScheme to be used
     * @param name the name of the taxonomy element (a String)
     * @param value the value of the taxonomy element
     *
     * @return the Classification instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Classification createClassification(
    ClassificationScheme scheme,
    String name,
    String value
    ) throws JAXRException;
    
    /**
     * Creates a Classification instance for an external
     * Classification using the specified InternationalString name and String value that identify
     * a taxonomy element within the specified ClassificationScheme.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param scheme the ClassificationScheme to be used
     * @param name the name of the taxonomy element (an InternationalString)
     * @param value the value of the taxonomy element
     *
     * @return the Classification instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Classification createClassification(
    ClassificationScheme scheme,
    InternationalString name,
    String value
    ) throws JAXRException;
    
    /**
     * Creates a Classification instance for an internal
     * Classification using the specified Concept that identifies
     * a taxonomy element within an internal ClassificationScheme.
     * <p>
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param concept the Concept that identifies the taxonomy element
     *
     * @return the Classification instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     * @throws InvalidRequestException if the Concept is not under
     * a ClassificationScheme
     */
    public Classification createClassification(
    Concept concept
    ) throws JAXRException, InvalidRequestException;
    
    /**
     * Creates a ClassificationScheme given the specified String parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param name the name of the ClassificationScheme (a String)
     * @param description a description of the ClassificationScheme (a String)
     *
     * @return the ClassificationScheme instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public ClassificationScheme createClassificationScheme(
    String name, String description
    ) throws JAXRException, InvalidRequestException;
    
    /**
     * Creates a ClassificationScheme given the specified
     * InternationalString parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param name the name of the ClassificationScheme (an InternationalString)
     * @param description a description of the ClassificationScheme (an InternationalString)
     *
     * @return the ClassificationScheme instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public ClassificationScheme createClassificationScheme(
    InternationalString name, InternationalString description
    ) throws JAXRException, InvalidRequestException;
    
    /**
     * Creates a ClassificationScheme from a Concept that has no
     * ClassificationScheme or parent Concept.
     * <p>
     * This method is a special-case method to do a type-safe conversion
     * from Concept to ClassificationScheme.
     * <p>
     * This method is
     * provided to allow for Concepts returned by the BusinessQueryManager
     * findConcepts call to be safely cast to ClassificationScheme. It
     * is up to the programmer to make sure that the Concept is indeed
     * semantically a ClassificationScheme.
     * <p>
     * This method is necessary because in the UDDI specification a tModel may serve
     * multiple purposes, and there is no way to know when a tModel
     * maps to a Concept and when it maps to a ClassificationScheme.
     * The UDDI specification leaves the determination to the programmer, and consequently so does this
     * method.
     * <p>
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param concept the Concept to be used
     *
     * @return the ClassificationScheme instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     * @throws InvalidRequestException if the Concept has a parent Concept
     * or is under a ClassificationScheme
     *
     */
    public ClassificationScheme createClassificationScheme(
    Concept concept
    ) throws JAXRException, InvalidRequestException;
    
    
    /**
     * Creates a Concept instance using the specified
     * parameters, where the name is a String.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param parent a reference either to a parent ClassificationScheme or to a Concept
     * @param name the name of the concept (a String)
     * @param value the value of the concept
     *
     * @return the Concept instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Concept createConcept(
    RegistryObject parent,
    String name,
    String value
    ) throws JAXRException;
    
    /**
     * Creates a Concept instance using the specified
     * parameters, where the name is an InternationalString.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param parent a reference either to a parent ClassificationScheme or to a Concept
     * @param name the name of the concept (an InternationalString)
     * @param value the value of the concept
     *
     * @return the Concept instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Concept createConcept(
    RegistryObject parent,
    InternationalString name,
    String value
    ) throws JAXRException;
    
    
    /**
     * Creates an EmailAddress instance using an address as the
     * parameter.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param address the email address
     *
     * @return the EmailAddress instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public EmailAddress createEmailAddress(
    String address
    ) throws JAXRException;
    
    /**
     * Creates an EmailAddress instance using both an address and a type as
     * parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param address the email address
     * @param type the type of the address
     *
     * @return the EmailAddress instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public EmailAddress createEmailAddress(
    String address,
    String type
    ) throws JAXRException;
    
    /**
     * Creates an ExternalIdentifier instance using the specified
     * parameters, where the name is a String.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param identificationScheme the ClassificationScheme used
     * @param name the name of the external identifier (a String)
     * @param value the value of the external identifier
     *
     * @return the ExternalIdentifier instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public ExternalIdentifier createExternalIdentifier(
    ClassificationScheme identificationScheme,
    String name,
    String value
    ) throws JAXRException;
    
    /**
     * Creates an ExternalIdentifier instance using the specified
     * parameters, where the name is an InternationalString.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param identificationScheme the ClassificationScheme used
     * @param name the name of the external identifier (an InternationalString)
     * @param value the value of the external identifier
     *
     * @return the ExternalIdentifier instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public ExternalIdentifier createExternalIdentifier(
    ClassificationScheme identificationScheme,
    InternationalString name,
    String value
    ) throws JAXRException;
    
    /**
     * Creates an ExternalLink instance using the specified
     * parameters, where the description is a String.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param externalURI the external URI
     * @param description a description of the link (a String)
     *
     * @return the ExternalLink instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     *
     */
    public ExternalLink createExternalLink(
    String externalURI,
    String description
    ) throws JAXRException;
    
    /**
     * Creates an ExternalLink instance using the specified
     * parameters, where the description is an InternationalString.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param externalURI the external URI
     * @param description a description of the link (an InternationalString)
     *
     * @return the ExternalLink instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     *
     */
    public ExternalLink createExternalLink(
    String externalURI,
    InternationalString description
    ) throws JAXRException;
    
    /**
     * Creates an ExtrinsicObject instance using the specified
     * parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @param repositoryItem the DataHandler for the repository item. Must not be null.
     *
     * @return the ExtrinsicObject instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     *
     */
    public ExtrinsicObject createExtrinsicObject(
    javax.activation.DataHandler repositoryItem
    ) throws JAXRException;
    
    /**
     * Creates an empty InternationalString instance.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return the InternationalString instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public InternationalString createInternationalString(
    ) throws JAXRException;
    
    /**
     * Creates an InternationalString instance using a String
     * parameter and the default Locale.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param s the String from which to create the InternationalString
     *
     * @return the InternationalString instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public InternationalString createInternationalString(
    String s
    ) throws JAXRException;
    
    /**
     * Creates an InternationalString instance using the specified
     * Locale and String parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param l the Locale in which to create the InternationalString
     * @param s the String from which to create the InternationalString
     *
     * @return the InternationalString instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public InternationalString createInternationalString(
    Locale l,
    String s
    ) throws JAXRException;
    
    /**
     * Creates a Key instance from an ID.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param id the ID string from which to create the Key
     *
     * @return the Key instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Key createKey(
    String id
    ) throws JAXRException;
    
    /**
     * Creates a LocalizedString instance using the specified
     * Locale and String parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param l the Locale in which to create the LocalizedString
     * @param s the String from which to create the LocalizedString
     *
     * @return the LocalizedString instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public LocalizedString createLocalizedString(
    Locale l,
    String s
    ) throws JAXRException;
    
    /**
     * Creates a LocalizedString instance using the specified
     * Locale, String, and character set parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param l the Locale in which to create the LocalizedString
     * @param s the String from which to create the LocalizedString
     * @param charSetName the name of the character set to use
     *
     * @return the LocalizedString instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public LocalizedString createLocalizedString(
    Locale l,
    String s,
    String charSetName
    ) throws JAXRException;
    
    /**
     * Creates an Organization instance using the specified
     * name, where the name is a String.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param name the name of the Organization
     *
     * @return the Organization instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Organization createOrganization(
    String name
    ) throws JAXRException;
    
    
    /**
     * Creates an Organization instance using the specified
     * name, where the name is an InternationalString.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param name the name of the Organization
     *
     * @return the Organization instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Organization createOrganization(
    InternationalString name
    ) throws JAXRException;
    
    
    
    /**
     * Creates a PersonName instance using the specified
     * first, middle, and last names.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @param firstName the person's first name
     * @param middleName the person's middle name
     * @param lastName the person's last name
     *
     * @return the PersonName instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public PersonName createPersonName(
    String firstName,
    String middleName,
    String lastName
    ) throws JAXRException;
    
    /**
     * Creates a PersonName instance using the specified
     * full name.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param fullName the person's full name
     *
     * @return the PersonName instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     *
     */
    public PersonName createPersonName(
    String fullName
    ) throws JAXRException;
    
    /**
     * Creates a PostalAddress instance using the specified
     * parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param streetNumber the street number
     * @param street the street name
     * @param city the city name
     * @param stateOrProvince the state or province name
     * @param country the country name
     * @param postalCode the postal code (such as a US ZIP code)
     * @param type the type of the address
     *
     * @return the PostalAddress instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public PostalAddress createPostalAddress(
    String streetNumber,
    String street,
    String city,
    String stateOrProvince,
    String country,
    String postalCode,
    String type
    ) throws JAXRException;
    
    /**
     * Creates a RegistryPackage instance using the specified
     * name, where the name is a String.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @param name the name of the registry package (a String)
     *
     * @return the RegistryPackage instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public RegistryPackage createRegistryPackage(
    String name
    ) throws JAXRException;
    
    /**
     * Creates a RegistryPackage instance using the specified
     * name, where the name is an InternationalString.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @param name the name of the registry package (an InternationalString)
     *
     * @return the RegistryPackage instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public RegistryPackage createRegistryPackage(
    InternationalString name
    ) throws JAXRException;
    
    /**
     * Creates a Service instance using the specified
     * name, where the name is a String.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param name the name of the Service (a String)
     *
     * @return the Service instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Service createService(
    String name
    ) throws JAXRException;
    
    
    /**
     * Creates a Service instance using the specified
     * name, where the name is an InternationalString.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param name the name of the Service (an InternationalString)
     *
     * @return the Service instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Service createService(
    InternationalString name
    ) throws JAXRException;
    
    /**
     * Creates an empty ServiceBinding instance.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return the ServiceBinding instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public ServiceBinding createServiceBinding(
    ) throws JAXRException;
    
    /**
     * Creates a Slot instance using the specified
     * parameters, where the value is a String.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param name the name of the Slot
     * @param value the value (a String)
     * @param slotType the slot type
     *
     * @return the Slot instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Slot createSlot(
    String name,
    String value,
    String slotType
    ) throws JAXRException;
    
    /**
     * Creates a Slot instance using the specified
     * parameters, where the value is a Collection of Strings.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param name the name of the Slot
     * @param value the value (a Collection of Strings)
     * @param slotType the slot type
     *
     * @return the Slot instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public Slot createSlot(
    String name,
    Collection values,
    String slotType
    ) throws JAXRException;
    
    
    /**
     * Creates an empty SpecificationLink instance.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return the SpecificationLink instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public SpecificationLink createSpecificationLink(
    ) throws JAXRException;
    
    /**
     * Creates an empty TelephoneNumber instance.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return the TelephoneNumber instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public TelephoneNumber createTelephoneNumber(
    ) throws JAXRException;
    
    /**
     * Creates an empty User instance.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return the User instance created
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public User createUser(
    ) throws JAXRException;
    
    /**
     * Saves one or more Objects to the registry. An object may be a RegistryObject
     * subclass instance.
     * <p>If an object is not in the registry, it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param objects a potentially heterogeneous Collection of RegistryObject instances
     *
     * @return a BulkResponse containing the Collection of keys for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    BulkResponse saveObjects(Collection objects) throws JAXRException;
    
    /**
     * Approves one or more previously submitted objects specified by a
     * collection of Keys for the objects.
     *
     * Reminder for V2
     */
    //void approveObjects(Collection keys) throws JAXRException;
    
    
    /**
     * Deprecates one or more previously submitted objects. Deprecation marks an
     * object as "soon to be deleted".
     * Once an object is deprecated, the JAXR provider must not allow any new references (e.g. new Associations, Classifications and ExternalLinks) to that object to be submitted. If a client makes an API call that results in a new reference to a deprecated object, the JAXR provider must throw a java.lang.IllegalStateException within a JAXRException. However, existing references to a deprecated object continue to function normally.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @param keys a Collection of keys for the objects to be deprecated
     *
     * @return a BulkResponse containing the Collection of keys for those objects that were
     * deprecated successfully and any JAXRException that was encountered in case of partial commit
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    BulkResponse deprecateObjects(Collection keys) throws JAXRException;
    
    /**
     * Undeprecates one or more previously deprecated objects. If an object
     * was not previously deprecated, it is not an error, and no exception
     * is thrown.
     * Once an object is undeprecated, the JAXR provider must again allow new references (e.g. new Associations, Classifications and ExternalLinks) to that object to be submitted.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @param keys a Collection of keys for the objects to be undeprecated
     *
     * @return a BulkResponse containing the Collection of keys for those objects that were
     * deprecated successfully and any JAXRException that was encountered in case of partial commit
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    BulkResponse unDeprecateObjects(Collection keys) throws JAXRException;
    
    /**
     * Deletes one or more previously submitted objects from the registry
     * using the object keys.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @param keys a Collection of keys for the objects to be deleted
     *
     * @return a BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    BulkResponse deleteObjects(Collection keys) throws JAXRException;
    
    /**
     * Deletes one or more previously submitted objects from the registry
     * using the object keys and a specified objectType attribute.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param keys a Collection of keys for the objects to be deleted
     * @param objectType the objectType attribute for the objects to be deleted
     *
     * @return a BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    BulkResponse deleteObjects(Collection keys, String objectType) throws JAXRException;
    
    /**
     * Returns the parent RegistryService that created this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return the parent RegistryService
     *
     * @throws JAXRException if the JAXR provider encounters an internal error
     *
     * @associates <{javax.xml.registry.RegistryService}>
     */
    RegistryService getRegistryService() throws JAXRException;
    
}
