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
 * Defines common behavior expected of any class that validates URIs.
 *
 * 
 * @author Farrukh S. Najmi
 */
public interface URIValidator { 

	/**
	 * Sets whether to do URI validation for this object. Default is true.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param validate <code>true</code> implies JAXR provider must perform validation
	 * 	of URIs when they are set; <code>false</code> implies validation is turned off
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */  
	public void setValidateURI(boolean validate) throws JAXRException; 

	/**
	 * Gets whether to do URI validation for this object.
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return <code>true</code> implies JAXR provider must perform validation
	 * 	of URIs when they are set; <code>false</code> implies validation is turned off
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */  
	public boolean getValidateURI() throws JAXRException; 
} 
