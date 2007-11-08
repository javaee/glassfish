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
 * A simple re-usable entity class that defines attributes of a telephone number.
 *
 * @author Farrukh S. Najmi 
 */
public interface TelephoneNumber {
    /**
     * Gets the country code. 
     * Default is an empty String.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @return the country code
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public String getCountryCode() throws JAXRException;

    /**
     * Gets the area code. 
     * Default is an empty String.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @return the area code
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public String getAreaCode() throws JAXRException;

    /**
     * Gets the telephone number suffix, not including the country or area code. 
     * Default is an empty String.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the telephone number
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public String getNumber() throws JAXRException;

    /**
     * Gets the internal extension.
     * Default is an empty String.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @return the internal extension number
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public String getExtension() throws JAXRException;

    /**
     * Gets the URL that can dial this number electronically.
     * Default is a NULL String.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @return the url
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public String getUrl() throws JAXRException;

    /**
     * The type of telephone number (for example, "fax"). Any String would do.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the type for this TelephoneNumber, which is an arbitrary String
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public String getType() throws JAXRException;

    /**
     * Sets country code.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @param countryCode the country code
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public void setCountryCode(String countryCode) throws JAXRException;

    /**
     * Sets the area code. 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @param areaCode the area code
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public void setAreaCode(String areaCode) throws JAXRException;

    /**
     * Sets the telephone number suffix, not including the country or area code. 
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     * 
	 * @param number	the telephone number
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public void setNumber(String number) throws JAXRException;

    /**
     * Sets the internal extension. 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @param extension	the internal extension number
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public void setExtension(String extension) throws JAXRException;

    /**
     * Sets the URL that can dial this number electronically.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     * 
	 * @param url the URL string
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public void setUrl(String url) throws JAXRException;

    /**
     * Sets the type of telephone number (for example, "fax"). Any String will do.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @param type the type for this TelephoneNumber, which is an arbitrary String
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public void setType(String type) throws JAXRException;

}
