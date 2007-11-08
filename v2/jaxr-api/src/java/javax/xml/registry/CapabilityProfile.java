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

/**
 * Provides information about the capabilities of a JAXR provider.
 *
 * @author Farrukh S. Najmi
 */
public interface CapabilityProfile {

	/**
	 * Gets the JAXR specification version supported by the JAXR provider. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the specification version
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public String getVersion() throws JAXRException;

	/**
	 * Gets the capability level supported by the JAXR provider. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the capability level
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public int getCapabilityLevel() throws JAXRException;
		
}
