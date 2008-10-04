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
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/comm/AuthenticationInfo.java,v 1.3 2005/12/25 04:26:30 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 04:26:30 $
*/

package com.sun.enterprise.admin.jmx.remote.comm;

/** An class that holds the user and password for the connection to the server. 
 * Instances of this class are immutable.
 * @author Kedar Mhaswade
 * @since S1AS7.0
 * @version 1.0
 */

public final class AuthenticationInfo {
	private final String  user;
	private final String  password;
	
	/** The only way to construct the instances of this class.
	 * @param user		the user name for the connection
	 * @param password  the clear text password for the connection
	 */
	public AuthenticationInfo(String user, String password) {
		this.user       = user;
		this.password   = password;
	}
	
	/** Returns the user name.
	 @return String
	 */
	public String getUser() {
		return user;
	}
	
	/** Returns the password in clear text.
	 @return String
	 */
	public String getPassword() {
		return password;
	}
}