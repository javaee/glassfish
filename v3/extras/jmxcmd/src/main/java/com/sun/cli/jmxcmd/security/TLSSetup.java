/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/TLSSetup.java,v 1.3 2004/10/14 19:06:27 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/10/14 19:06:27 $
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


