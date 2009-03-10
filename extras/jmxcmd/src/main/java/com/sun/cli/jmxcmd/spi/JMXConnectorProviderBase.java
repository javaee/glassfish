/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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




