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
 * This is the principal interface implemented by a JAXR provider.  
 * A registry client can get this interface from a Connection to a registry. 
 * It provides the methods that are used by the client to discover various capability 
 * specific interfaces implemented by the JAXR provider.
 *
 * @see Connection
 * @author Farrukh S. Najmi
 */
public interface RegistryService {

    /**
     * Returns the CapabilityProfile for the JAXR provider.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the CapabilityProfile for a JAXR provider 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @associates <{javax.xml.registry.CapabilityProfile}>
     * @see LifeCycleManager
     */
    CapabilityProfile getCapabilityProfile() throws JAXRException;


    /**
     * Returns the BusinessLifeCycleManager object implemented by the JAXR provider.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the BusinessLifeCycleManager
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @associates <{javax.xml.registry.BusinessLifeCycleManager}>
     * @see LifeCycleManager
     */
    BusinessLifeCycleManager getBusinessLifeCycleManager() throws JAXRException;

    /**
     * Returns the BusinessQueryManager object implemented by the JAXR provider.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the BusinessQueryManaer
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @associates <{BusinessQueryManager}>
     * @directed
     */
    BusinessQueryManager getBusinessQueryManager() throws JAXRException;

    /**
     * Returns the DeclarativeQueryManager object implemented by the JAXR provider.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @return the DeclarativeQueryManager
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @associates <{DeclarativeQueryManager}>
     * @directed
     */
    DeclarativeQueryManager getDeclarativeQueryManager() throws JAXRException, UnsupportedCapabilityException;

	/**
	 * Returns the BulkResponse associated with specified requestId.
	 * Once a client retrieves a BulkResponse for a particular requestId
	 * any subsequent calls to retrieve the Bulkresponse for the same requestId
	 * should result in an InvalidRequestException.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param requestId	the id for a previous asynchronous request
	 * @return the BulkResponse that contains the result for the specified request
	 * @throws InvalidRequestException	if no responses exist for specified requestId
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	BulkResponse getBulkResponse(String requestId) throws InvalidRequestException, JAXRException;


    /** 
     * Gets the default user-defined postal scheme for codifying the attributes of PostalAddress.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the ClassificationScheme that is the default postal scheme
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */ 
    public ClassificationScheme getDefaultPostalScheme()  throws JAXRException; 	

	/** 
	 * Takes a String that is an XML request in a registry-specific 
	 * format, sends the request to the registry, and returns a String that is 
	 * the registry-specific XML response. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param request	the registry-specific request in a String representation
	 * @return the String that is the XML response in a registry-specific manner
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */ 
	public String makeRegistrySpecificRequest(String request) throws JAXRException; 
}
