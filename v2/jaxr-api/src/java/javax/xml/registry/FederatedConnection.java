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
 * Represents a single logical connection to a federation or group of
 * registry providers. This interface is used in support of the distributed
 * query feature of the JAXR API.
 *
 * 
 * @author Farrukh S. Najmi
 */
public interface FederatedConnection extends Connection {
    /**
     * Gets the RegistryService interface associated with the Connection.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @associates <{RegistryService}>
     * @see RegistryService
     */
	/*#RegistryService getRegistryService() throws JAXRException;*/  
}
