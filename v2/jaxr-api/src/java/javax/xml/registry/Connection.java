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

import javax.xml.registry.infomodel.*;
import java.util.*;



/**
 * This class represents a connection between a JAXR client and a
 * JAXR provider.
 *
 * @see ConnectionFactory
 * @author Farrukh S. Najmi
 */
public interface Connection {

    /**
     * Gets the RegistryService interface associated with the Connection.
	 * If a Connection property (e.g. credentials) is set after the client calls getRegistryService
	 * then the newly set Connection property is visible to the RegistryService
	 * previously returned by this call.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the RegistryService associated with this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @associates <{RegistryService}>
     * @see RegistryService
     */
	RegistryService getRegistryService() throws JAXRException;

    /**
	 * Closes a Connection when it is no longer needed.
     * Since a provider typically allocates significant resources outside 
     * the JVM on behalf of a Connection, clients should close them when
     * they are not needed.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void close() throws JAXRException;
	
	/**
	 * Indicated whether this Connection has been closed or not.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return <code>true</code> if Connection is closed; <code>false</code> otherwise
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	boolean isClosed() throws JAXRException;

	/**
	 * Indicates whether a client uses synchronous communication with JAXR provider or not.
	 * A JAXR provider must support both modes
	 * of communication. A JAXR client can choose which mode it wants to use.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return <code>true</code> if Connection is synchronous (default); <code>false</code> otherwise
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public boolean isSynchronous() throws JAXRException;

	/**
	 * Sets whether the client uses synchronous communication or not. 
	 * A JAXR client may dynamically change its communication style
	 * preference. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param sync	<code>true</code> if Connection is desired to be synchronous; <code>false</code> otherwise
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public void setSynchronous(boolean sync) throws JAXRException;

	/**
	 * Sets the Credentials associated with this client. The credentials is used to authenticate the client with the JAXR provider.
	 * A JAXR client may dynamically change its identity by changing
	 * the credentials associated with it.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 *
	 * @param credentials	a Collection oj java.lang.Objects which provide identity related information for the caller.
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public void setCredentials(Set credentials) throws JAXRException;

	/**
	 * Gets the credentials associated with this client.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL>
	 * 	 
	 * @return Set of java.lang.Object instances. The Collection may be empty but not null.	 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 *
	 */
	public Set getCredentials() throws JAXRException;
	

}