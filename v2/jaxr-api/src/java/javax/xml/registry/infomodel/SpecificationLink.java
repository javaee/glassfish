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
 * A SpecificationLink provides the linkage between a ServiceBinding and one of its technical specifications that describes how to use the service using the ServiceBinding. For example, a ServiceBinding may have a SpecificationLink instance that describes how to access the service using a technical specification in the form of a WSDL document or a CORBA IDL document. 
 * It serves the same purpose as the union of the tModelInstanceInfo and instanceDetails structures in
 * UDDI.
 *
 * @see Concept
 * @author Farrukh S. Najmi
 */
public interface SpecificationLink extends RegistryObject {
    /**
     * Gets the specification object for this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
	 *
	 * @return the RegistryObject that is the specification object. 
     * 	For a UDDI provider the specification object must be a Concept with no parent.
     * 	For an ebXML provider it is likely to be an ExtrinsicObject.
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @associates <{RegistryObject}>
     * @supplierCardinality 1
     * @directed 
     */
    RegistryObject getSpecificationObject() throws JAXRException;

    /**
     * Sets the specification object for this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param obj the RegistryObject that is the specification object. 
     * 	For a UDDI provider the specification object must be a Concept with no parent.
     * 	For an ebXML provider it is likely to be an ExtrinsicObject.
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setSpecificationObject(RegistryObject obj) throws JAXRException;

    /**
     * Gets the description of usage parameters.
	 * Default is an empty String.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * 
     * @return the usage description for this object, which must not be null
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
	InternationalString getUsageDescription() throws JAXRException;

    /**
     * Sets  the description of usage parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param usageDescription the description of usage parameters for this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
	void setUsageDescription(InternationalString usageDescription) throws JAXRException;

    /**
     * Gets any usage parameters. A usage parameter is an arbitrary String
	 * that describes how to use the technical specification accessed via this
	 * SpecificationLink. Each parameter is a String.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the Collection of String instances. The Collection may be empty but not null.	 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
     */
	Collection getUsageParameters() throws JAXRException;

    /**
     * Sets any usage parameters. Each parameter is a String
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @param usageParameters the Collection of usage parameter Strings
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
	void setUsageParameters(Collection usageParameters) throws JAXRException;


	/**
	 * Gets the parent ServiceBinding for this SpecificationLink.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the parent ServiceBinding within which this object is composed
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	ServiceBinding getServiceBinding() throws JAXRException;
}