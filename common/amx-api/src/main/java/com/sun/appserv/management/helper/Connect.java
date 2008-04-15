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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/helper/Connect.java,v 1.2 2007/05/05 05:30:50 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2007/05/05 05:30:50 $
 */
package com.sun.appserv.management.helper;

import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.HandshakeCompletedListenerImpl;
import com.sun.appserv.management.client.TLSParams;
import com.sun.appserv.management.client.TrustStoreTrustManager;

import javax.net.ssl.HandshakeCompletedListener;
import java.io.File;
import java.io.IOException;

/**
	Miscellaneous helper routines for connecting to the Appserver.
	@since AppServer 9.0
 */
public class Connect
{
	private	Connect()	{}
	
	/**
		Connect to an Appserver using TLS (SSL) using the default TrustStore and 
		default TrustStore password.
		<p>
		If the server certificate is unknown, prompting will occur via System.out if
		'promptForNewCertificate' is true; otherwise the connection will be rejected.
		<p>
		If a new server certificate is found, and the user enters "yes", then it
		is added to the default truststore.
		
		@param host	hostname or IP address
		@param port	port to which to connect
		@param user admin user name
		@param userPassword password for specified user
		@param promptForUnknownCertificate	prompts via System.out if the server
			certificate is unrecognized, otherwise rejects the connection
	 */
		public static AppserverConnectionSource
	connectTLS(
		final String host,
		final int	 port,
		final String user,
		final String userPassword,
		final boolean promptForUnknownCertificate )
		throws IOException
	{
		final TLSParams	tlsParams	= createTLSParams( null, promptForUnknownCertificate );
		
		return new AppserverConnectionSource( AppserverConnectionSource.PROTOCOL_RMI,
				host, port, user, userPassword, tlsParams, null);
	}
	
		
		public static AppserverConnectionSource
	connectNoTLS(
		final String host,
		final int	 port,
		final String user,
		final String userPassword)
		throws IOException
	{
		return new AppserverConnectionSource(
				host, port, user, userPassword, null);
	}
	
	
	/**
		The File will use the name
		{@link AppserverConnectionSource#DEFAULT_TRUST_STORE_NAME}
		in the user's home directory.
		
		@return a File for the default truststore.  May not yet exist.
	 */
		public static File
	getDefaultTrustStore()
	{
		final String homeDir = System.getProperty( "user.home" );
		final String sep     = System.getProperty( "file.separator" );
		
		return new File ( homeDir + sep + AppserverConnectionSource.DEFAULT_TRUST_STORE_NAME );
	}
	
	/**
		Get TLSParams for the default truststore, assuming the default password.
		
		@param trustStorePassword a truststore, or null for the default one
		@param promptForNewCertificate
	 */
		public static TLSParams
	createTLSParams(
	    final String trustStorePassword,
	    final boolean promptForNewCertificate)
	{
		return createTLSParams( null, trustStorePassword , promptForNewCertificate);
	}
	

	/**
		Get TLSParams for the specified truststore and password.
		<p>
		A {@link HandshakeCompletedListener} is installed which may be obtained
		by calling {@link TLSParams#getHandshakeCompletedListener}.
		<p>
		If a new server certificate is found, and the user enters "yes" in response
		to prompting, then the certificate is added to the truststore.
		
		@param trustStore a truststore, or null for the default one
		@param trustStorePasswordIn the truststore password, if null the default one will be used
		@param promptForUnknownCertificate	prompts via System.out if the server
			certificate is unrecognized
	 */
		public static TLSParams
	createTLSParams(
		final File		trustStore,
		final String	trustStorePasswordIn,
		final boolean	promptForUnknownCertificate )
	{
		final File		trustStoreFile	= (trustStore == null) ? getDefaultTrustStore() : trustStore;
		final char[]	trustStorePassword	= ((trustStorePasswordIn == null) ?
			AppserverConnectionSource.DEFAULT_TRUST_STORE_PASSWORD : trustStorePasswordIn).toCharArray();
					
		final HandshakeCompletedListener	handshakeCompletedListener	=
			new HandshakeCompletedListenerImpl();
			
		final TrustStoreTrustManager trustMgr =
			new TrustStoreTrustManager( trustStoreFile, trustStorePassword);
			
		trustMgr.setPrompt( promptForUnknownCertificate );

		final TLSParams	tlsParams = new TLSParams( trustMgr, handshakeCompletedListener );

		return( tlsParams );
	}
}







