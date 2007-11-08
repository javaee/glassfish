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
 * Represents an email address.
 *
 * @see User
 * @author Farrukh S. Najmi
 */
public interface EmailAddress {

	/**
	 * Returns the email address for this object.
	 * Default is a NULL String. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the actual email address (e.g. john.doe@acme.com)
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public String getAddress() throws JAXRException;
	
	/**
	 * Sets the email address for this object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param address	the actual email address (e.g. john.doe@acme.com)
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public void setAddress(String address) throws JAXRException;
	
    /**
     * Gets the type for this object.
     * Default is a NULL String. 
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return	the usage type for this object which is an arbitrary value (e.g. "Home" or "Office")
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public String getType() throws JAXRException;

    /**
     * Sets the type for this object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @param	type	the usage type for this object which is an arbitrary value (e.g. "Home" or "Office")
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    public void setType(String type) throws JAXRException;
}
