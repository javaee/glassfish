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
 
/*
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/spi/SunOneHttpProvider.java,v 1.3 2005/12/25 03:45:42 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:42 $
 */

package com.sun.cli.jmx.spi;

import java.util.Map;
import java.util.Iterator;
import java.io.IOException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
import com.sun.enterprise.admin.jmx.remote.SunOneHttpJmxConnectorFactory;
import com.sun.cli.jmx.spi.JMXConnectorProvider;

/**
 * Implements the provider to Generic CLI for the protocol "s1ashttp".
 * 
 * @author  kedar
 * @version 1.0
 */

public class SunOneHttpProvider implements JMXConnectorProvider {
	
	public SunOneHttpProvider() {
	}
	
	static class Info implements JMXConnectorProviderInfo
	{
		private static final String	DESCRIPTION	=
			"Implements the SunOne Appserver 8.0 PE connector.";
		private static final String	USAGE	=
			"connect --host <host> --port port --protocol s1ashttp " +
			"--user <user> --password <pass>  [connection-name]";
		
			public String
		getDescription() {
			return( DESCRIPTION );
		}
			public String
		getUsage() {
			return( USAGE );
		}
	}
	
		public static JMXConnectorProviderInfo
	getInfo() {
		return( new Info() );
	}
		
	
	public JMXConnector connect(Map m) throws IOException {
		final String user		= (String) m.get(DefaultConfiguration.ADMIN_USER_ENV_PROPERTY_NAME);
		final String password	= (String) m.get(DefaultConfiguration.ADMIN_PASSWORD_ENV_PROPERTY_NAME);
		final JMXServiceURL url	= env2JmxServiceUrl(m);
		System.out.println("User = " + user);
		System.out.println("Password = " + password);
		return ( SunOneHttpJmxConnectorFactory.connect(url, user, password) );
	}
	
	public final static String	MY_PROTOCOL	= "s1ashttp";
	
	public boolean isSupported(Map m) {
		final boolean hostPresent = m.get(JMXConnectorProvider.HOST) != null;
		final boolean portPresent = m.get(JMXConnectorProvider.PORT) != null;
		
		final String	protocol	= (String)m.get(JMXConnectorProvider.PROTOCOL);
		final boolean	protocolPresent = protocol != null;
		final boolean	userPresent = m.get(DefaultConfiguration.ADMIN_USER_ENV_PROPERTY_NAME) != null;
		final boolean	passwordPresent = m.get(DefaultConfiguration.ADMIN_PASSWORD_ENV_PROPERTY_NAME) != null;
		return ( hostPresent && portPresent && protocolPresent && protocol.equals( MY_PROTOCOL ) &&
				 userPresent && passwordPresent );
	}
	
	private JMXServiceURL env2JmxServiceUrl(Map m) throws java.net.MalformedURLException {
		final String protocol	= (String) m.get(JMXConnectorProvider.PROTOCOL);
		final String host		= (String) m.get(JMXConnectorProvider.HOST);
		final int port			= Integer.parseInt((String)m.get(JMXConnectorProvider.PORT));
		return ( new JMXServiceURL(protocol, host, port) );
	}
}
