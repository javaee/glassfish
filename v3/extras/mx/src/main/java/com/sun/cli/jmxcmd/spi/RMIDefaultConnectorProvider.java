/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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




