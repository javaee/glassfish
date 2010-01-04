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

package com.sun.cli.jmxcmd.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.io.File;
import java.io.IOException;


import javax.management.remote.JMXConnector;


import java.security.cert.CertificateException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.KeyManagementException;
import java.security.UnrecoverableKeyException;

import com.sun.cli.jmxcmd.security.sasl.SaslClientSetup;

import com.sun.cli.jmxcmd.security.TLSSetup;
import org.glassfish.admin.amx.util.SetUtil;

/**
	Supports connectivity to JSR 160 jmxmp servers.
	<p>
	Supported protocols are {@link #JMXMP_PROTOCOL
}
 */
public final class JMXMPDefaultConnectorProvider
	extends JMXConnectorProviderBase
{
		public
	JMXMPDefaultConnectorProvider()
	{
	}
	
	static class Info implements JMXConnectorProviderInfo
	{
		private static final String	DESCRIPTION	=
			"Implements the standard JSR 160 connector.";
		private static final String	USAGE	=
			"connect [--host <host>] --port port --protocol jmxmp " +
			"[--user <user> --password-file <path> ] "  +
			"[--trust-store <path>] " + "[connection-name]";
		
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
	
	
	
	
		public JMXConnector
	connect( Map<String,String> m )
		throws IOException,
		KeyStoreException, NoSuchAlgorithmException, CertificateException,
		KeyManagementException, UnrecoverableKeyException
	{
		final String	host		= (String)m.get( HOST );
		final String	port		= (String)m.get( PORT );
		
		requireParam( host, "host" );
		requireParam( port, "port" );
		
		final String	user			= (String)m.get( USER );
		final String	password		= (String)m.get( PASSWORD );
		final String	trustStoreFile	= (String)m.get( TRUSTSTORE_FILE );
		final String	trustStorePassword	= (String)m.get( TRUSTSTORE_PASSWORD );
		final String	sasl			= (String)m.get( SASL );
		
        final boolean	useTLS	= trustStoreFile != null;
		final HashMap<String,Object>	env	= new HashMap<String,Object>();
		
		final SaslClientSetup	setup	= new SaslClientSetup( env, useTLS );
		if ( useTLS )
		{
			TLSSetup.setupTLSForJMXMP( env, new File( trustStoreFile ), trustStorePassword, null);
		}
           
        setup.setupProfiles( sasl );
		setup.setupClientCallback( user, password );
		
		final String		urlString	= "service:jmx:jmxmp://" + host + ":" + port;
		
		return( connect( urlString, env ) );
	}
	
	public final static String	JMXMP_PROTOCOL	= "jmxmp";
	public static final Set<String>	SUPPORTED_PROTOCOLS	=
		SetUtil.newUnmodifiableStringSet( JMXMP_PROTOCOL );
	
		protected Set<String>
	getSupportedProtocols()
	{
		return( SUPPORTED_PROTOCOLS );
	}
}




