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
 * ServiceBinding instances are RegistryObjects that represent technical information on a specific way to access a specific interface offered by a Service instance. A ServiceBinding may have a set of SpecificationLink instances.
 * Maps to a BindingTemplate in UDDI.
 * 
 * @see Concept
 * @author Farrukh S. Najmi
 */
public interface ServiceBinding extends RegistryObject, URIValidator {

	/** 
	 * Gets the URI that gives access to the service via this binding. 
	 * Default is a NULL String. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the URI that gives access to the service via this binding 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 * 
	 */ 
	String getAccessURI() throws JAXRException;
	
	/** 
	 * Sets the URI that gives access to the service via this binding.
	 * The accessURI is mutually exclusive from targetBinding. JAXR Provider must
	 * throw an InvalidRequestException if an accessURI is set when there is
	 * already a non-null targetBinding defined.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param uri	the URI that gives access to the service via this binding 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */ 
	void setAccessURI(String uri) throws JAXRException;
	
	/** 
	 * Gets the next ServiceBinding in case there is a redirection from
	 * one service provider to another service provider.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the next ServiceBinding in case there is a service redirection
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	ServiceBinding getTargetBinding() throws JAXRException;
	
    /** 
	 * Sets the next ServiceBinding in case there is a redirection.
     * The targetBinding is mutually exclusive from the accessURI. JAXR Provider must
     * throw an InvalidRequestExcpetion if a targetBinding is set when there is
     * already a non-null accessURI defined.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param binding the target ServiceBinding to which this object is redirected to 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void setTargetBinding(ServiceBinding binding) throws JAXRException;
    
	
	
	/** 
	 * Gets the parent service for which this is a binding. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return	the parent Service object 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */ 
	Service getService() throws JAXRException;

    /**
     * @directed
     * @label targetBinding 
     */
    /*#ServiceBinding lnkServiceBinding;*/		

    /** 
	 * Adds a child SpecificationLink. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param specificationLink	the SpecificationLink being added
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void addSpecificationLink(SpecificationLink specificationLink) throws JAXRException;

    /** 
	 * Adds a Collection of SpecificationLink children. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param specificationLinks	the Collection of SpecificationLinks being added
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void addSpecificationLinks(Collection specificationLinks) throws JAXRException;

    /** 
	 * Removes a child SpecificationLink. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param specificationLink	the SpecificationLink being removed
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void removeSpecificationLink(SpecificationLink specificationLink) throws JAXRException;

    /** 
	 * Removes a Collection of children SpecificationLinks. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param specificationLinks	the Collection of SpecificationLinks being removed
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void removeSpecificationLinks(Collection specificationLinks) throws JAXRException;


    /** 
     * Gets all children SpecificationLinks. 
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     *
     * @see javax.xml.registry.infomodel.SpecificationLink
     * @return Collection of SpecificationLink instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     *
     * @supplierCardinality 1..*
     * @associates <{javax.xml.registry.infomodel.SpecificationLink}>	 
     * @link aggregationByValue
     */
    Collection getSpecificationLinks() throws JAXRException;


    /**
     * @link aggregationByValue
     * @supplierCardinality 1..* 
     */
    /*#SpecificationLink lnkFoo;*/
}
