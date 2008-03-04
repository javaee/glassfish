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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

/* CVS information
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/comm/GenericHttpConnectorAddress.java,v 1.3 2005/12/25 04:26:31 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 04:26:31 $
*/

package com.sun.enterprise.admin.jmx.remote.comm;

/** An interface to extend the basic ConnectorAddress to include the HTTP information.
 */
public interface GenericHttpConnectorAddress extends ConnectorAddress {
	/**
	 * Returns the host name as a String. The interpretation is to be documented
	 * by the implementation (DNS/IP address).
	 * @return String	representing the host
	 */
	String getHost();
	/**
	 * Sets the host to the given parameter.
	 * @param host		represents the host name to set to.
	 */
	void setHost(String host);
	/** Returns the port for this instance of ConnectorAddress.
	 * @return integer representing the port number
	 */
	int getPort();
	/** Sets throws port to given integer.
	 * @param port			integer indicating the port number
	 */
	void setPort(int port);
	
	/** Returns the {@link AuthenticationInfo} related to this ConnectorAddress.
	 * AuthenticationInfo is to be handled appropriately by the implementing class.
	 * @return		instance of AuthenticationInfo class
	 */
	AuthenticationInfo getAuthenticationInfo();
	/** Sets the AuthenticationInfo for this ConnectorAddress.
	 * @param authInfo		instance of AuthenticationInfo
	 */
	void setAuthenticationInfo(AuthenticationInfo authInfo);
}