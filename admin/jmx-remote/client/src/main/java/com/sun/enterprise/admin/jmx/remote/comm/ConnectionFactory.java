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
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/comm/ConnectionFactory.java,v 1.3 2005/12/25 04:26:31 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 04:26:31 $
*/

package com.sun.enterprise.admin.jmx.remote.comm;

/** A factory class to create new instances of {@link IConnection} 
 * interface.
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.1
 */
public class ConnectionFactory {
	private ConnectionFactory() {
	}
	
	/** Returns the newly created connection (instance of {@link IConnection} 
	 * to the servlet. Note that the Servlet has to be up and running. If the
	 * server/servlet does not respond, IOException results. Note that this is
	 * by default a connection to request a resource on server side. It is not
	 * guaranteed that the connection is kept alive after it is used to send the data.
	 * Clients are expected to create the instances of IConnection as and when
	 * required, by calling this method.
	 * @param h		an instance of {@link HttpConnectorAddress} that encapsulates
	 *				the data required to create the connection
	 * @return an instance of IConnection interface
	 * @throws an instance of IOException if the attempt to connect fails
	 */
	public static IConnection createConnection(HttpConnectorAddress h) throws 
		java.io.IOException {
		return new ServletConnection(h);
	}
}