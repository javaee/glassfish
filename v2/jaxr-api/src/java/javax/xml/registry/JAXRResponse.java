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
 * A JAXR requests' response.
 *
 * @see JAXRException
 * @author Farrukh S. Najmi  
 */
public interface JAXRResponse {

	/**
	 * Status indicating a successful response.
	 */
	public static final int STATUS_SUCCESS=0;

	/**
	 * Status indicating a successful response that included at least one warning.
	 */
	public static final int STATUS_WARNING=1;

	/**
	 * Status indicating a failure response.
	 */
	public static final int STATUS_FAILURE=2;

	/**
	 * Status indicating that the results are currently unavailable.
	 */
	public static final int STATUS_UNAVAILABLE=3;
	
	/**
	 * Returns the unique id for the request that generated this response.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the request id
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	String getRequestId() throws JAXRException;
	
	
	/**
	 * Returns the status for this response.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @see JAXRResponse#STATUS_SUCCESS
	 * @return the status which is an integer enumerated value
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public int getStatus() throws JAXRException;
	
	/**
	 * Returns true if a response is available, false otherwise.
	 * This is a polling method and must not block.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return <code>true</code> if the response is available; <code>false</code> otherwise
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public boolean isAvailable() throws JAXRException;
}
