/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/rmi/RMISSLClientSocketFactory.java,v 1.11 2005/05/19 19:34:07 llc Exp $
 * $Revision: 1.11 $
 * $Date: 2005/05/19 19:34:07 $
 */

package com.sun.cli.jmxcmd.security.rmi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

import java.lang.reflect.Method;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.HandshakeCompletedListener;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.NoSuchAlgorithmException;

import java.rmi.server.RMIClientSocketFactory;

import com.sun.cli.jcmd.util.security.TrustAnyTrustManager;


/**
	<b>Not for public use</b>
	<p>
	RMISSLClientSocketFactory which allows the configuration of security parameters
	by an RMI client; by default this is not possible.  This is the "stub" class
	that gets downloaded to the client.
	<p>
	Looks for RMISSLClientSocketFactoryEnv, and if found, uses it to get
	configuration.
 */
public class RMISSLClientSocketFactory
    implements RMIClientSocketFactory, Serializable
{
	private transient SSLSocketFactory	mFactory	= null;
	
	private transient MyEnvImpl	mEnvImpl	= null;
	
		public
	RMISSLClientSocketFactory()
	{
		mEnvImpl	= new MyEnvImpl();
	}
	
	private static final String	KEYSTORE_SPROP			= "javax.net.ssl.keyStore";
	private static final String	KEYSTORE_PASSWORD_SPROP	= "javax.net.ssl.keyStorePassword";
	private static final String	TRUSTSTORE_FILE_SPROP	= "javax.net.ssl.trustStore";
	private static final String	TRUSTSTORE_PASSWORD_SPROP= "javax.net.ssl.trustStorePassword";
	
	/**
		Non-standard!
	 */
	private static final String	KEY_PASSWORD_SPROP	= "javax.net.ssl.keyPassword";
	
	/**
		Non-standard!
	 */
	private static final String	KEY_ALIAS_SPROP	= "javax.net.ssl.keyAlias";
	
		private static TrustManager[]
	getTrustAny()
	{
		final TrustManager[]	trustManagers	= new TrustManager[ 1 ];
		trustManagers[ 0 ]	= TrustAnyTrustManager.getInstance();
		return( trustManagers );
	}
	
	/**
		If the 'env' class is not available eg a client that does not have the client jar,
		then use this dummy class.  Lack of the env class implies that the client cannot
		configure anything and must rely on system properties, which this class
		handles.
	 */
	private final class MyEnvImpl implements RMISSLClientSocketFactoryEnv
	{
		private transient boolean			mTrace	= false;
		private	MyEnvImpl()	{}
		
		  	public KeyManager[]
	    getKeyManagers( )
	    {
	    	KeyManager[]	mgrs	= null;
	    	
	    	try
	    	{
		    	final KeyManagerFactory	factory	=
		    		KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
		    	
		    	factory.init( getKeyStore(), getKeyPassord() );
		    	
		    	mgrs	= factory.getKeyManagers();
	    	}
	    	catch( Exception e )
	    	{
	    		e.printStackTrace();
	    	}

	    	return( mgrs );
	    }
	    
	   		public KeyStore
	    getKeyStore( )
	    {
			KeyStore	ks	= null;
			
			final String	prop			= System.getProperty( KEYSTORE_SPROP );
			final File		keyStoreFile	= prop == null ? null : new File( prop );
			final char[]	keyStorePassword=
				toCharArray( System.getProperty( KEYSTORE_PASSWORD_SPROP ) );
			
			if ( keyStoreFile != null &&
					keyStorePassword != null )
			{
				try
				{
					final FileInputStream	is	= new FileInputStream( keyStoreFile );
					try
					{
			        	ks = KeyStore.getInstance("JKS");
						ks.load( is, keyStorePassword );
						trace( "loaded keystore: " + keyStoreFile );
					}
					finally
					{
						ks	= null;
						is.close();
					}
				}
				catch( Exception e )
				{
					throw new RuntimeException( "can't load keystore: " + keyStoreFile, e);
				}
			}
			
			return( ks );
	    }
	    
			public String
	    getKeyAlias( )
	    {
	    	return( System.getProperty( KEY_ALIAS_SPROP ) );
	    }
	    
			public char[]
	    getKeyPassord( )
	    {
	    	String pw	= System.getProperty( KEY_PASSWORD_SPROP );
	    	if ( pw == null )
	    	{
	    		pw	= System.getProperty( KEYSTORE_PASSWORD_SPROP );
	    	}
	    	
			final char[]	keyPassword	= toCharArray( pw );
			
	    	return keyPassword;
	    }

	    
	    public TrustManager[]	getTrustManagers( )	{ return null; }
	    
	    	public File
	    getTrustStore()
	    {
			final String	prop	= System.getProperty( TRUSTSTORE_FILE_SPROP );
			final File trustStore	= prop == null ? null : new File( prop );
			return( trustStore );
	    }
	    
			public char[]
	    getTrustStorePassword()
	    {
			final char[]	pw	= toCharArray( System.getProperty( TRUSTSTORE_PASSWORD_SPROP ) );
			return( pw );
	    }
	    
	    public HandshakeCompletedListener getHandshakeCompletedListener( )	{ return null; }
	    
	    public void		setTrace( final boolean trace )	{ mTrace = trace; }
	    public boolean	getTrace(  )	{ return mTrace; }
	}
	
	/**
		The environment is useless if it gets downloaded, since it would never be 
		possible for a client to configure it!
		So instead of importing it, we must look for the class in the existing
		environment, and get an instance via reflection.
	 */
		private synchronized RMISSLClientSocketFactoryEnv
	getEnv(  )
	{
		final String	packageName	= this.getClass().getPackage().getName();
		final String	className	= packageName + ".RMISSLClientSocketFactoryEnvImpl";
		
		RMISSLClientSocketFactoryEnv	env	= null;
		
		try
		{
			final Class<?>	envClass	= Class.forName( className );
			final Method    m   = envClass.getMethod( "getInstance", (Class[])null);
			env	= (RMISSLClientSocketFactoryEnv)
				m.invoke( (Class[])null, (Object[])null );
		}
		catch (Exception e )
		{
			env	= mEnvImpl;
		}
		
		return( env );
	}
	
		private final void
	trace( Object o )
	{
		if ( getEnv().getTrace() )
		{
			System.out.println( toString() + ": " + o.toString() );
		}
	}
	
	
		private static char[]
	toCharArray( final String s )
	{
		return( s == null ? null : s.toCharArray() );
	}
	
	
		private final SSLSocketFactory
	createSocketFactory( final RMISSLClientSocketFactoryEnv env )
		throws IOException
	{
		SSLSocketFactory	factory	= null;
		
        try
        {
        	final TrustManager[]	trustManagers	= env.getTrustManagers( );
            
            final SSLContext sslContext = SSLContext.getInstance( "TLSv1" );
            sslContext.init( new MyEnvImpl().getKeyManagers(), trustManagers, null );
            factory = sslContext.getSocketFactory();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
            throw (IOException) new IOException().initCause(e);
        }
        
        return( factory );
	}
	
	/**
		Allow only these "strong" suites.
	 */
	private/*!!!!*/ static final String[]	SUITES	= new String[]
	{
		"SSL_RSA_WITH_RC4_128_MD5",
		"SSL_RSA_WITH_RC4_128_SHA",
		"TLS_RSA_WITH_AES_128_CBC_SHA",
		"TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
		"TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
		"SSL_RSA_WITH_3DES_EDE_CBC_SHA",
		"SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
		"SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
	};
		static String[]
	getSupportedCipherSuites()
	{
		return( (String[])SUITES.clone() );
	}
	
	private/*!!!!*/ static final String[]	PROTOCOLS	= new String[]
	{
		"TLSv1",
		// do not use SSLv2Hello or SSLv3
	};
		static String[]
	getSupportedProtocols()
	{
		return( (String[])PROTOCOLS.clone() );
	}
	
    	public synchronized Socket
    createSocket(
    	final String	host,
    	final int		port)
    	throws IOException
    {
    	final String	target	= host + ":" + port;
    	
		trace( "createSocket: " + target );
    	
    	final RMISSLClientSocketFactoryEnv	env	= getEnv();
    	
        if ( mFactory == null)
        {
        	mFactory = createSocketFactory( env );
			trace( "createSocket: created socket factory" );
        }
        
		//trace( "creating socket: " + target );
        final SSLSocket sslSocket	= (SSLSocket)mFactory.createSocket( host, port );
        
    	sslSocket.setEnabledCipherSuites( SUITES );
    	sslSocket.setEnabledProtocols( PROTOCOLS );
        
		final HandshakeCompletedListener	listener	= env.getHandshakeCompletedListener( );
		if ( listener != null )
		{
			trace( "createSocket: added HandshakeCompletedListener: " + listener );
			sslSocket.addHandshakeCompletedListener( listener );
		}
		
    	
		trace( "created socket: " + target );
        return( sslSocket );
    }
}











