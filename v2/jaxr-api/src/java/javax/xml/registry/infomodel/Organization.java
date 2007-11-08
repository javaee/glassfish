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

import javax.xml.registry.*;
import java.util.*;

/**
 * Organization instances provide information on organizations such as a Submitting Organization. Each Organization instance
 * may have a reference to a parent Organization. In addition it may have a contact attribute defining the primary contact
 * within the organization. An Organization also has an address attribute.
 * 
 * @see Service
 * @author Farrukh S. Najmi
 */
public interface Organization extends RegistryObject {

    /**
     * Gets the Address for this Organization.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @return the PostalAddress for this Organization
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @supplierCardinality 1
     * @directed
     * @link aggregationByValue
     * @associates <{PostalAddress}>
     */
    PostalAddress getPostalAddress() throws JAXRException;

	/** 
	 * Sets the address for this Organization. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param address the PostalAddress for this Organization	
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void setPostalAddress(PostalAddress address) throws JAXRException;

    /**
     * Gets the primary Contact for this Organization. The primary
	 * contact is one of the Users of the Organization, as returned by the getUsers call
	 * on an Organization instance.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the User that is the primary Contact for this Organization. Must not be null
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @supplierCardinality 1
     * @directed
     * @link aggregationByValue
     * @supplierRole primaryContact
     * @associates <{User}>
     */
    User getPrimaryContact() throws JAXRException;

    /** 
	 * Sets the primary contact for this Organization. If the primary
	 * contact is not in the existing Collection of User instances for
	 * this object then it must be added to that Collection. If the primary
	 * contact is already in the existing Collection of User instances for
	 * this object, then it should not be added to the Users collection as
	 * a duplicate entry. In either case, the primary contact is a distinguished
	 * User instance within the Collection of User instances for this Object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param primaryContact	the User that is the primary Contact for this Organization
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void setPrimaryContact(User primaryContact) throws JAXRException;
	
    /** 
     * Adds a User.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @param user	the User being added to this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void addUser(User user) throws JAXRException;

    /** 
     * Adds a Collection of Users.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @param users the Collection of Users being added to this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void addUsers(Collection users) throws JAXRException;

    /** 
     * Removes a User. 
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @param user	the User being removed from this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void removeUser(User user) throws JAXRException;

    /** 
     * Removes a Collection of Users. 
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param users the Collection of Users being removed from this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void removeUsers(Collection users) throws JAXRException;

    /**
	 * Gets the Collection of Users affiliated with this Organization.
	 * One of these users is designated as the primary contact. 
	 *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @see javax.xml.registry.infomodel.User
     * @return Collection of User instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     *
     * @supplierCardinality 0..*
     * @associates <{User}>
     * @label users
     * @link aggregationByValue
     */
    Collection getUsers() throws JAXRException;
	
    /** 
     * Gets the telephone numbers for this User that match the specified telephone number type.
     * If phoneType is null return all telephoneNumbers. 
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @see javax.xml.registry.infomodel.TelephoneNumber
	 * @return Collection of TelephoneNumber instances. The Collection may be empty but not null.	 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
     * @param phoneType	a String that specifies the type of phone numbers desired
     * @supplierCardinality 1
     * @associates <{TelephoneNumber}>
     * @directed
     * @supplierRole phones
     * @clientCardinality 1..**/
    public Collection getTelephoneNumbers(String phoneType) throws JAXRException;
    
    /** 
	 * Sets the various telephone numbers for this user. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param phoneNumbers the TelephoneNumbers being set for this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public void setTelephoneNumbers(Collection phoneNumbers) throws JAXRException;
	

    /** 
	 * Adds a child Service. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param service 	the Service being added to this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void addService(Service service) throws JAXRException;

    /** 
	 * Adds a Collection of Service children. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param services 	the Collection of Services being added to this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void addServices(Collection services) throws JAXRException;

    /** 
	 * Removes a Service from this object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param service 	the Service being removed from this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void removeService(Service service) throws JAXRException;

    /** 
	 * Removes a Collection of children Services from this object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param services 	the Collection of Services being removed from this object
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void removeServices(Collection services) throws JAXRException;

    /** 
     * Gets all children Services. 
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @see javax.xml.registry.infomodel.Service
     * @return Collection of Service instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @supplierCardinality 0..*
     * @associates <{javax.xml.registry.infomodel.Service}>	 
     * @link aggregationByValue
     */
    Collection getServices() throws JAXRException;
	
    /** 
	 * Adds a child Organization. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param organization the child Organization being added
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void addChildOrganization(Organization organization) throws JAXRException;

    /** 
	 * Adds a Collection of Organization children. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param organizations the Collection of child Organizations being added
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void addChildOrganizations(Collection organizations) throws JAXRException;

    /** 
	 * Removes a child Organization. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param organization the child Organization being removed
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void removeChildOrganization(Organization organization) throws JAXRException;

    /** 
	 * Removes a Collection of children Organizations.
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param organizations the Collection of child Organizations being removed
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void removeChildOrganizations(Collection organizations) throws JAXRException;

    /** 
	 * Gets number of children. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @return the number of children Organizations
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    int getChildOrganizationCount() throws JAXRException;

    /** 
     * Gets all immediate children Organizations. 	 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
     * @see javax.xml.registry.infomodel.Organization
     * @return Collection of Organization instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    Collection getChildOrganizations() throws JAXRException;
    
    /** 
     * Gets all descendant Organizations. 	 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
     * @see javax.xml.registry.infomodel.Organization
     * @return Collection of Organization instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    Collection getDescendantOrganizations() throws JAXRException;

    /** 
	 * Gets the parent (container) organization. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @return the parent Organization or null if object has no parent Organization
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    Organization getParentOrganization()  throws JAXRException;

    /** 
	 * Gets the root organization. 
	 *
	 * 
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @return the root Organization or null if object has no parent Organization
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    Organization getRootOrganization()  throws JAXRException;
	
}
