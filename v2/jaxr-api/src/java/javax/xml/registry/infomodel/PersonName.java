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
 * Represents a person's name.
 *
 * @author Farrukh S. Najmi
 */
public interface PersonName {
    /** 
	 * Gets the last name (surname) for this Person. 
	 * Default is a NULL String. 
	 * 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @return the person's last name
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public String getLastName() throws JAXRException;

    /** 
	 * Sets the last name (surname) for this Person. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param lastName	 the person's last name
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public void setLastName(String lastName) throws JAXRException;

    /** 
	 * Gets the first name for this Person. 
	 * Default is an empty String.
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @return the person's first name
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public String getFirstName() throws JAXRException;

    /** 
	 * Sets the first name for this Person. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param firstName the person's first name
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public void setFirstName(String firstName) throws JAXRException;

    /** 
	 * Gets the middle name for this Person. 
	 * Default is an empty String.
	 * 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @return the person's middle name
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public String getMiddleName() throws JAXRException;
	
	/** 
	 * Sets the middle name for this Person. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param middleName the person's middle name
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public void setMiddleName(String middleName) throws JAXRException;
	
	/** 
	 * Gets the fully formatted name for this person.
	 * Default is an empty String.
	 * 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the person's full name
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public String getFullName() throws JAXRException;

	/** 
	 * Sets the fully formatted name for this person.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param fullName	the person's full name
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public void setFullName(String fullName) throws JAXRException;
}

