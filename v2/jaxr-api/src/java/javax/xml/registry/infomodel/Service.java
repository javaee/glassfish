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
 * Service instances are RegistryObjects that provide information on services 
 * (for example, web services) offered by an Organization. A Service may have a set of ServiceBinding instances. 
 * Maps to a BusinessService in UDDI.
 *
 * @see ServiceBinding
 * @author Farrukh S. Najmi
 */
public interface Service extends RegistryEntry {

	/** 
	 * Gets the Organization that provides this service.
	 * Providing Organization may be null. The providing
	 * Organization may be different from the Submitting Organization
	 * as defined by RegistryObject#getSubmittingOrganization.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL>
	 *
	 * @see RegistryObject#getSubmittingOrganization()
	 * @return the Organization that provides this service
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 *
	 */
	Organization getProvidingOrganization() throws JAXRException;

	/** 
	 * Sets the Organization that provides this service. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param providingOrganization	the Organization that provides this service
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void setProvidingOrganization(Organization providingOrganization) throws JAXRException;

	/** 
	 * Adds a child ServiceBinding.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param serviceBinding	the ServiceBinding being added
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void addServiceBinding(ServiceBinding serviceBinding) throws JAXRException;

	/** 
	 * Adds a Collection of ServiceBinding children.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param serviceBindings	the Collection of ServiceBindings being added
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void addServiceBindings(Collection serviceBindings) throws JAXRException;

	/** 
	 * Removes a child ServiceBinding.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param serviceBinding	the ServiceBinding being removed
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void removeServiceBinding(ServiceBinding serviceBinding) throws JAXRException;

	/** 
	 * Removes a Collection of children ServiceBindings.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param serviceBindings	the Collection of ServiceBindings being removed
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	void removeServiceBindings(Collection serviceBindings) throws JAXRException;

	/** 
	 * Gets all children ServiceBindings. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 
	 *
	 * @see javax.xml.registry.infomodel.ServiceBinding
	 * @return Collection of ServiceBinding instances. The Collection may be empty but not null.	 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 *
	 * @supplierCardinality 1..*
	 * @associates <{javax.xml.registry.infomodel.ServiceBinding}>	 
	 * @link aggregationByValue
	 */
	Collection getServiceBindings() throws JAXRException;

}