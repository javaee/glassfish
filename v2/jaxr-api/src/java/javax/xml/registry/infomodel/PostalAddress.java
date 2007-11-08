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
 * PostalAddress is a simple re-usable entity class that defines attributes of a postal Address.
 *
 * @author Farrukh S. Najmi
 */
public interface PostalAddress extends ExtensibleObject  {
	/** 
	 * Returns the street name. 
	 * Default is an empty String.
	 * 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the street name
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public String getStreet() throws JAXRException;
	
	/** 
	 * Sets the street name.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param street the street name
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public void setStreet(String street) throws JAXRException;
	
	/** 
	 * Returns the street number.
	 * Default is an empty String.
	 * 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the street number
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public String getStreetNumber() throws JAXRException;
	
	/** 
	 * Sets the street number. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param streetNumber the street number
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public void setStreetNumber(String streetNumber) throws JAXRException;

	/** 
	 * Returns the city. 
	 * Default is an empty String.
	 * 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the city
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public String getCity() throws JAXRException;
	
	/** 
	 * Sets the city. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param city the city
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public void setCity(String city) throws JAXRException;
	
	/** 
	 * Returns the state or province.
	 * Default is an empty String.
	 * 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the state or province
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public String getStateOrProvince() throws JAXRException;
	
	/** 
	 * Sets the state or province.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param stateOrProvince	the state or province
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public void setStateOrProvince(String stateOrProvince) throws JAXRException;
	
	/** 
	 * Returns the postal or zip code.
	 * Default is an empty String.
	 * 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the postal code (e.g. US zip code)
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public String getPostalCode() throws JAXRException;
	
	/** 
	 * Sets the postal or zip code.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param postalCode the postal code (e.g. US zip code)
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public void setPostalCode(String postalCode) throws JAXRException;
	
	/** 
	 * Returns the country. 
	 * Default is an empty String.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the country
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public String getCountry() throws JAXRException;

    /** 
	 * Sets the country.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param country	the country
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public void setCountry(String country) throws JAXRException;

    /**
     * Returns the type of address (for example, "headquarters") as a String.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @return the type for this PostalAddress. This is an arbitrary String (e.g. "Home", "Office") 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public String getType() throws JAXRException;

    /**
     * Sets the type of address (for example, "headquarters") as a String.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param type	the type for this PostalAddress. This is an arbitrary String (e.g. "Home", "Office") 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public void setType(String type) throws JAXRException;
	
    /** 
	 * Sets a user-defined postal scheme for codifying the attributes of PostalAddress.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param scheme	the user defined postal scheme.
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */ 
    public void setPostalScheme(ClassificationScheme scheme)  throws JAXRException; 	

    /** 
	 * Returns a user-defined postal scheme for codifying the attributes of PostalAddress.
	 * If none is defined for this object, then must return the default value
	 * returned by RegistryService#getDefaultPostalScheme()
	 *
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @see javax.xml.registry.RegistryService#getDefaultPostalScheme()
	 * @return the user defined postal scheme.
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */ 
    public ClassificationScheme getPostalScheme()  throws JAXRException; 
	
}
