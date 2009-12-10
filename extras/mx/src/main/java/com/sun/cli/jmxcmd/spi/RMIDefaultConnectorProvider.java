/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/spi/RMIDefaultConnectorProvider.java,v 1.14 2005/11/15 20:59:54 llc Exp $
 * $Revision: 1.14 $
 * $Date: 2005/11/15 20:59:54 $
 */
 
package com.sun.cli.jmxcmd.spi;

import java.util.Map;
import java.util.Set;

import java.io.IOException;
import java.io.File;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HandshakeCompletedListener;

import javax.management.remote.JMXConnector;

import org.glassfish.admin.amx.util.stringifier.SmartStringifier;

import com.sun.cli.jmxcmd.security.rmi.RMISSLClientSocketFactoryEnvImpl;
import com.sun.cli.jcmd.util.security.TrustAnyTrustManager;
import com.sun.cli.jcmd.util.security.TrustStoreTrustManager;
import com.sun.cli.jcmd.util.security.HandshakeCompletedListenerImpl;
import org.glassfish.admin.amx.util.SetUtil;



/**
	Supports connectivity via standard JSR 160 RMI using JNDI lookup of the client stub.
	<p>
	Supported protocols are {@link #RMI_PROTOCOL}
 */
public final class RMIDefaultConnectorProvider
	extends JMXConnectorProviderBase
{
		public
	RMIDefaultConnectorProvider()
	{
	}
	
	static final class Info implements JMXConnectorProviderInfo
	{
		private static final String	DESCRIPTION	=
			"Implements connectivity to the JSR 160 RMI jrmp connector " +
			"using host/port form with jrmp protocol";
		private static final String	USAGE	=
			"connect --host|-h <host> --port|-p port --protocol|-r rmi " +
			"--jndi-name|-j name " +
			"[--user|-u <user> --password-file|-f <path> ] "  +
			"[connection-name]";
		
			public String
		getDescription()
		{
			return( DESCRIPTION );
		}
			public String
		getUsage()
		{
			return( USAGE );
		}
	}
	
		public static JMXConnectorProviderInfo
	getInfo()
	{
		return( new Info() );
	}
	
	private final class MyTrustStoreTrustManager extends TrustStoreTrustManager
	{
			public
		MyTrustStoreTrustManager(
			final File		trustStoreFile,
			final char[]	trustStorePassword )
		{
			super( trustStoreFile, trustStorePassword ); 
		}
			protected boolean
		shouldAddToTrustStore( final Certificate c )
		{
			System.out.println( "MyTrustStoreTrustManager.shouldAddToTrustStore: returning true for " + c );
			return( true );
		}
		
			protected void
		addCertificateToTrustStore(
			final String		alias,
			final Certificate	c )
			throws IOException,
			KeyStoreException, NoSuchAlgorithmException, CertificateException
		{
			super.addCertificateToTrustStore( alias, c );
			System.out.println( "added certificate: " + c );
		}
	}
	
		
		public JMXConnector
	connect( java.util.Map<String,String> params )
		throws IOException
	{
		System.out.println( SmartStringifier.toString( params ) );
		final String	host		= (String)params.get( HOST );
		final String	port		= (String)params.get( PORT );
		final String	jndiName	= (String)params.get( JNDI_NAME );
		final String	trustStore	= (String)params.get( TRUSTSTORE_FILE );
		final String	trustStorePassword	= (String)params.get( TRUSTSTORE_PASSWORD );
		
		requireParam( host, "host" );
		requireParam( port, "port" );
		
		final Map<String,Object>	env	= initEnv( params );
		
		String urlString	= null;
		if ( jndiName != null )
		{
			urlString = "service:jmx:rmi://"+
					"/jndi/rmi://" + host + ":" + port  + jndiName;
		}
		else
		{
			requireParam( jndiName, JNDI_NAME );
		}
		
		final RMISSLClientSocketFactoryEnvImpl	factoryEnv	=
			RMISSLClientSocketFactoryEnvImpl.getInstance();
		
		X509TrustManager	trustManager	= null;
		
		if ( trustStore != null )
		{
			try
			{
				trustManager	=
					new MyTrustStoreTrustManager( new File( trustStore ), trustStorePassword.toCharArray() );
			}
			catch( Exception e )
			{
				throw new IOException( "can't load trustStore: " + trustStore );
			}
		}
		else
		{
			trustManager	= TrustAnyTrustManager.getInstance();
			System.out.println( "Warning: trusting all server certificates (no trust-store specified)." );
		}

		final TrustManager[]	trustManagers	= new TrustManager[] { trustManager };
		
		factoryEnv.setTrustManagers( trustManagers );
		
		final HandshakeCompletedListener	listener	= new HandshakeCompletedListenerImpl();
		factoryEnv.setHandshakeCompletedListener( listener );
	
		return( connect( urlString, env ) );
	}
	
		private final void
	trace( Object o )
	{
		System.out.println( "### RMIDefaultConnectorProvider: " + o.toString() );
	}
	
	
	public final static String	RMI_PROTOCOL		= "rmi";    // treat the same as "rmi_jrmp"
	public static final Set<String>	SUPPORTED_PROTOCOLS	= SetUtil.newUnmodifiableStringSet( RMI_PROTOCOL, "rmi_jrmp" );
	
		protected Set<String>
	getSupportedProtocols()
	{
		return( SUPPORTED_PROTOCOLS );
	}
}




