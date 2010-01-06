/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2004-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.cli.jmxcmd.security;

import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;





public final class TLSSetup
{
	private	TLSSetup()	{}
	
	
		public static KeyStore
	loadKeystore(
		File	keyStoreFile,
		String	optionalPassword )
		throws IOException,
		KeyStoreException, NoSuchAlgorithmException, CertificateException,
		UnrecoverableKeyException, KeyManagementException
	{
		final KeyStore			ks			= KeyStore.getInstance( "JKS" );
		final FileInputStream	inputStream	= new FileInputStream( keyStoreFile );
			
		try
		{
			ks.load( inputStream,
				optionalPassword == null ? null : optionalPassword.toCharArray() );
		}
		finally
		{
			inputStream.close();
		}
		
		return( ks );
	}
	
		public static TrustManagerFactory
	getTrustManagerFactory(
		File	trustStoreFile,
		String	trustStorePassword )
		throws IOException,
		KeyStoreException, NoSuchAlgorithmException, CertificateException,
		UnrecoverableKeyException, KeyManagementException
	{
		final KeyStore ks = loadKeystore( trustStoreFile, trustStorePassword );
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
        
        return( tmf );
	}
	
		public static SSLContext
	getSSLContext(
		final KeyStore	keystore,
		final String	optionalKeyPassword )
		throws IOException,
		KeyStoreException, NoSuchAlgorithmException, CertificateException,
		UnrecoverableKeyException, KeyManagementException
	{
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init( keystore );
		
		KeyManagerFactory	keyManagerFactory	= null;
		if ( optionalKeyPassword != null )
		{
			keyManagerFactory	= KeyManagerFactory.getInstance( "SunX509" );
			keyManagerFactory.init( keystore, optionalKeyPassword.toCharArray());
		}
        
		final SSLContext	sslContext	= SSLContext.getInstance("TLSv1");
		sslContext.init( keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        
        return( sslContext );
	}
	
	
		public static SSLSocketFactory
	createSocketFactory(
		final File		keystoreFile,
		final String	optionalKeystorePassword,
		final String	optionalKeyPassword )
		throws IOException,
				KeyStoreException, NoSuchAlgorithmException, CertificateException,
				KeyManagementException, UnrecoverableKeyException
	{
		final KeyStore		keystore	= loadKeystore( keystoreFile, optionalKeystorePassword );
		final SSLContext	sslContext	= getSSLContext( keystore, null );
        
        final SSLSocketFactory	socketFactory	= sslContext.getSocketFactory();
       
       	return( socketFactory );
	}
	
		public static void
	setupTLSForJMXMP(
		final Map<String,Object>		env,
		final File		keystoreFile,
		final String	optionalKeystorePassword,
		final String	optionalKeyPassword )
		throws IOException,
				KeyStoreException, NoSuchAlgorithmException, CertificateException,
				KeyManagementException, UnrecoverableKeyException
	{
		final SSLSocketFactory	socketFactory	=
			TLSSetup.createSocketFactory( keystoreFile,
				optionalKeystorePassword, optionalKeyPassword );
       
        env.put("jmx.remote.tls.socket.factory", socketFactory);
        env.put("jmx.remote.tls.enabled.protocols", "TLSv1");
        env.put("jmx.remote.tls.enabled.cipher.suites", "SSL_RSA_WITH_RC4_128_MD5");
	}
}


