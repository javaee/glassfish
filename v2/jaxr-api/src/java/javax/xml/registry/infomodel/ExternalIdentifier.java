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

/**
 * ExternalIdentifier instances provide the additional identifier information 
 * to RegistryObjects such as DUNS number, Social Security Number, or an alias 
 * name of the organization.  The attribute name inherited from RegistryObject is 
 * used to contain the identification scheme ("DUNS" "Social Security Number", etc.), 
 * and the attribute value contains the actual information (e.g. the actual DUNS number). 
 * Each RegistryObject may have 0 or more ExternalIdentifiers.
 *
 * @see RegistryObject
 * @author Farrukh S. Najmi   
 */
public interface ExternalIdentifier extends RegistryObject {

	/** 
	 * Gets the parent RegistryObject for this ExternalIdentifier.
	 * To set the registryObject call addExternalIdentifier on a RegistryObject.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 
	 *
	 * @see RegistryObject#addExternalIdentifier(ExternalIdentifier ei)
	 * @return	the RegistryObject that this object identifies 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	RegistryObject getRegistryObject() throws JAXRException;

	/** 
	 * Gets the value of an ExternalIdentifier.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return	the identification value defined by this object (e.g. a company's DUNS number)
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public String getValue() throws JAXRException;
	
	/** 
	 * Sets the value of an ExternalIdentifier.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param value	the identification value defined by this object (e.g. a company's DUNS number)
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public void setValue(String value) throws JAXRException;

    /**
     * Gets the ClassificationScheme that is used as the identification scheme
     * for identifying this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the ClassificationScheme that is used as the identification scheme (e.g. "DUNS")
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @supplierCardinality 0..*
     * @clientCardinality 0..*
     * @associationAsClass Classification
     */
    ClassificationScheme getIdentificationScheme() throws JAXRException;

    /** 
     * Sets the ClassificationScheme that is used as the identification scheme
	 * for identifying this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @param identificationScheme	the ClassificationScheme that is used as the identification scheme (e.g. "DUNS")
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setIdentificationScheme(ClassificationScheme identificationScheme) throws JAXRException;
}
