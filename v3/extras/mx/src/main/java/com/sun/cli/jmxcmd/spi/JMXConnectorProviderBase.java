/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/spi/JMXConnectorProviderBase.java,v 1.3 2004/09/28 17:22:13 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/09/28 17:22:13 $
 */
 
package com.sun.cli.jmxcmd.spi;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;


/**
	The default provider which implements the jmxmp protocol.
 */
public abstract class JMXConnectorProviderBase
	implements JMXConnectorProvider
{
		public
	JMXConnectorProviderBase()
	{
	}

	protected abstract Set<String>	getSupportedProtocols();
	
	
		protected void
	requireParam( final Object value, final String name )
	{
		if ( value == null )
		{
			throw new IllegalArgumentException( "Missing parameter: " + name );
		}
	}
	
	
	
		protected String
	getHost( final Map<String,String> params )
	{
		return( (String)params.get( JMXConnectorProvider.HOST ) );
	}
		protected String
	getPort( final Map<String,String> params )
	{
		return( (String)params.get( JMXConnectorProvider.PORT ) );
	}
	
		protected String
	getProtocol( final Map<String,String> params )
	{
		return( (String)params.get( JMXConnectorProvider.PROTOCOL ) );
	}
	
		protected String
	getTruststore( final Map<String,String> params )
	{
		return( (String)params.get( JMXConnectorProvider.TRUSTSTORE_FILE ) );
	}
	
		protected String
	getTruststorePassword( final Map<String,String> params )
	{
		return( (String)params.get( JMXConnectorProvider.TRUSTSTORE_PASSWORD ) );
	}
	
		protected String
	getJNDIName( final Map<String,String> params )
	{
		return( (String)params.get( JMXConnectorProvider.JNDI_NAME ) );
	}
	
	
	
		protected int
	getPortInt( final Map<String,String> params )
	{
		final String	portParam	= getPort( params );
		
		if ( portParam == null )
		{
			requireParam( portParam, "port" );
		}
		
		return( new Integer( portParam ).intValue() );
	}
	
		protected String
	getUser( final Map<String,String> params )
	{
		return( (String)params.get( JMXConnectorProvider.USER ) );
	}
	
		protected String
	getPassword( final Map<String,String> params )
	{
		return( (String)params.get( JMXConnectorProvider.PASSWORD ) );
	}
	
		protected String[]
	getCredentials( final Map<String,String> params )
	{
		String[]	credentials	= null;
		
		final String	user			= (String)params.get( USER );
		final String	password		= (String)params.get( PASSWORD );
		if ( user != null )
		{
			credentials	= new String[] { user, password == null ? "" : password };
		}
		
		return credentials;
	}
	
		protected Map<String,Object>
	initEnv( final Map<String,String> params )
	{
		final HashMap<String,Object>	env	= new HashMap<String,Object>();
		
		final String[]	credentials	= getCredentials( params );
		if ( credentials != null )
		{
			env.put( "jmx.remote.credentials", credentials );
		}

		return( env );
	}
	
	
		public boolean
	isSupported( java.util.Map<String,String> m )
	{
		boolean 		supports	= false;
		final String	requestedProtocol	= (String)m.get( PROTOCOL );
		
		final Set<String>	protocols	= getSupportedProtocols();
		
		return( protocols.contains( requestedProtocol ) );
	}
	
		protected JMXConnector
	connect(
		final String	urlString,
		final Map<String,Object>		env )
		throws MalformedURLException, IOException
	{
		final JMXServiceURL	url			= new JMXServiceURL( urlString );
		
		final JMXConnector	conn	= JMXConnectorFactory.connect( url, env);
		
		return( conn );
	}
}




