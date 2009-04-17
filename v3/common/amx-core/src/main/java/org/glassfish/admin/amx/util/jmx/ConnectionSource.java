/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.admin.amx.util.jmx;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.io.IOException;

/**
	A source of an MBeanServerConnection.
	@see com.sun.appserv.management.util.jmx.MBeanServerConnectionConnectionSource
	@see com.sun.appserv.management.util.jmx.MBeanServerConnectionSource
 */
public interface ConnectionSource
{
	/**
		Return a valid MBeanServerConnection, making a new connection if necessary, or
		returning an existing one if still valid.  Some implementations may choose
		to not allow creation of a new connection (when 'forceNew' is specified).
		<p>
		Should not be called frequently, as the check for validity will make a remote
		call.
		<p>
		An implementation may choose to ignore the 'forceNew' parameter and always
		return the same connection.
		
		@param forceNew		creates a new connection instead of reusing an existing one
		@return the connection, or null if a new one is not possible
	 */
	public MBeanServerConnection	getMBeanServerConnection( boolean forceNew )
										throws IOException;
	
	/**
		@return existing connection, valid or not, may be null
	 */
	public MBeanServerConnection	getExistingMBeanServerConnection();
	
	/**
		@return a JMXConnector or null if not appropriate
	 */
	public JMXConnector		getJMXConnector( boolean forceNew )
								throws IOException;
}



