/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/security/TrustStoreTrustManager.java,v 1.2 2005/11/08 22:39:26 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:39:26 $
 */
package com.sun.cli.jcmd.util.security;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.Date;
import java.text.DateFormat;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.X509TrustManager;

import com.sun.cli.jcmd.util.cmd.LineReaderImpl;


/**
	This X509TrustManager implementation supports a trust-store file and allows
	adding new certificates to it.  It is designed to allow a subclass to
	override a variety of protected methods including those of TrustManager:
	<ul>
	<li>checkClientTrusted</li>
	<li>checkServerTrusted</li>
	<li>getAcceptedIssuers</li>
	</ul>
	
	as well as:
	
	<ul>
	<li>#checkCertificate</li>
	<li>#getTrustStorePassword</li>
	<li>#shouldAddToTrustStore</li>
	<li>#askShouldAddToTrustStore</li>
	<li>#getCertificateAlias</li>
	<li>#addCertificateToTrustStore</li>
	<li>#writeStore</li>
	<li>#certificateNotInTrustStore</li>
	<li>#getTrustStore</li>
	</ul>
	<p>
	For convenience, if setPrompt( true ) is called, then when a new Certificate
	is encountered, askShouldAddToTrustStore( c ) prompts the user
	via System.in as to whether to accept this new Certificate as trusted.
	Subclasses can of course override this behavior any any desired way.
 */
public class TrustStoreTrustManager
	implements X509TrustManager // do NOT make Serializable
{
	private final File		mTrustStoreFile;
	private final char[]	mTrustStorePassword;
	private final String	mKeyStoreType;
	private KeyStore		mTrustStore;
	private boolean			mPrompt;
	
	/**
		Create a new instance with the specified File and password
		The trustStoreFile must exist.
		
		@param trustStoreFile		(not required to exist)
		@param keyStoreType		keystore (truststore) type, eg "JKS"
		@param trustStorePassword (may be null)
	 */
		public
	TrustStoreTrustManager(
		final File		trustStoreFile,
		final String	keyStoreType,
		final char[]	trustStorePassword )
	{
		if ( trustStoreFile == null || keyStoreType == null )
		{
			throw new IllegalArgumentException();
		}
		
		mTrustStoreFile		= trustStoreFile;
		mKeyStoreType		= keyStoreType;
		mTrustStorePassword	= trustStorePassword;
		mTrustStore			= null;
		mPrompt				= false;
	}
	
	/**
		calls this( trustStoreFile,"JKS", trustStorePassword )
	 */
		public
	TrustStoreTrustManager(
		final File		trustStoreFile,
		final char[]	trustStorePassword )
	{
		this( trustStoreFile, "JKS", trustStorePassword );
	}
	
	/**
		If set to true, then when a new Certificate is encountered, the user
		will be prompted via System.in as to whether it should be trusted.
		
		@param prompt
	 */
		public void
	setPrompt( final boolean prompt )
	{
		mPrompt	= prompt;
	}
	
	/**
		Create an instance using the system trust-store as returned by 
		getSystemTrustStoreFile(). 
		
		@return an instance or null if not possible
	 */
		public static TrustStoreTrustManager
	getSystemInstance()
	{
    	final File		trustStore			= getSystemTrustStoreFile();
    	final char[]	trustStorePassword	= getSystemTrustStorePassword();
    	
    	TrustStoreTrustManager	mgr	= null;
    	
    	if ( trustStore != null && trustStorePassword != null )
    	{
    		return( new TrustStoreTrustManager( trustStore, trustStorePassword ) );
    	}
    	
    	return( mgr );
	}
	
		private static char[]
	toCharArray( final String s )
	{
		return( s == null ? null : s.toCharArray() );
	}
	
	
	/**
		Standard system property denoting the trust-store.
	 */
	public static final String	TRUSTSTORE_FILE_SPROP	= "javax.net.ssl.trustStore";
	
	/**
		Standard system property denoting the trust-store password.
	 */
	public static final String	TRUSTSTORE_PASSWORD_SPROP= "javax.net.ssl.trustStorePassword";
	
	/**
		Use System.getProperty( "javax.net.ssl.trustStore" ) to find a trust-store.
	 */
		public static File
	getSystemTrustStoreFile()
	{
		final String	prop	= System.getProperty( TRUSTSTORE_FILE_SPROP );
		final File trustStore	= prop == null ? null : new File( prop );
		return( trustStore );
	}
	
	/**
		Use System.getProperty( "javax.net.ssl.trustStorePassword" ) to find the
		trust-store password.
	 */
		public static char[]
	getSystemTrustStorePassword()
	{
		return( toCharArray( System.getProperty( TRUSTSTORE_PASSWORD_SPROP ) ) );
	}
    
	
	/**
		Return the trust-store that was initially passed in.
		
		@return File
	 */
		public final File
	getTrustStoreFile()
	{
		return( mTrustStoreFile );
	}
	
	/**
		Subclass may choose to override this method to get the password from any
		desired source.  Otherwise, the password used to create this instance is
		returned.
		
		@return char[]
	 */
		protected char[]
	getTrustStorePassword()
	{
		return( mTrustStorePassword );
	}
	
		public void
	checkClientTrusted( X509Certificate[] chain, String authType)
		throws CertificateException
	{
		throw new UnsupportedOperationException( "checkClientTrusted() not supported" );
	}
	
		public void
	checkServerTrusted( X509Certificate[] chain, String authType)
		throws CertificateException
	{
		if (chain == null || chain.length == 0)
		{
			throw new IllegalArgumentException();
        }
        
		checkCertificate(chain);
	}
	
	/**
		By default, no issuers are trusted. It is better to trust specific 
		Certificates explicitly.
		
		@return X509Certificate[]
	 */
		public X509Certificate[]
	getAcceptedIssuers()
	{
		// none, by default
		return( new X509Certificate[ 0 ] );
	}
	
	/**
		Prompts via System.in to ask whether the Certificate should be added.
		
		@param c
		@return true if the response is yes.
	 */
		protected boolean
	askShouldAddToTrustStore( final Certificate c )
		throws IOException
	{
		final LineReaderImpl	reader	= new LineReaderImpl( System.in );
		
		final String prompt	= c.toString() + 
			"\n\nAdd the above certificate to the truststore [y/n]?";
			
		final String result	= reader.readLine( prompt );
		
		return( result.equalsIgnoreCase( "y" ) || result.equalsIgnoreCase( "yes" ) );
	}
	
	/**
		Subclass may wish to override this routine and call defaultShouldAddToTrustStore( c );
		
		@param c
		@return true if the Certificate should be trusted and added to the trust-store
	 */
		protected boolean
	shouldAddToTrustStore( final Certificate c )
		throws IOException
	{
		return( mPrompt ? askShouldAddToTrustStore( c ) : false );
	}
	
	/**
		Return an alias for a Certificate to be added to the TrustStore.
		@param c
		@return an alias to be used for adding the Certificate to the trust-store
	 */
		protected String
	getCertificateAlias( final Certificate c )
	{
        final DateFormat f = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);     
        
		return( "cert" +  f.format( new Date() ) );
	}
	
	
	/**
		Add the Certificate with the specified alias to the trust-store.
		
		@param alias
		@param c
	 */
		protected void
	addCertificateToTrustStore(
		final String		alias,
		final Certificate	c )
		throws IOException,
		KeyStoreException, NoSuchAlgorithmException, CertificateException
	{
        mTrustStore.setCertificateEntry( alias, c );
        writeStore();
	}
	
	
	/**
		Add the Certificate to the trust-store, using the alias returned by
		getCertificateAlias( c ).
		
		@param c
	 */
		protected void
	addCertificateToTrustStore( final Certificate c )
		throws IOException,
		KeyStoreException, NoSuchAlgorithmException, CertificateException
	{
        final String aliasName = getCertificateAlias( c );
        
        addCertificateToTrustStore( aliasName, c );
	}
	
	/**
		Write the store to disk.  Results are undefined if an error occurs while
		writing the file.
	 */
		protected void
	writeStore()
		throws IOException,
		KeyStoreException, NoSuchAlgorithmException, CertificateException
	{	
		final File	f	= getTrustStoreFile();
		
		FileOutputStream	out	= new FileOutputStream( f );
    	
		try
		{
			getTrustStore().store( out, getTrustStorePassword() );
		}
		finally
		{
			out.close();
		}
    }    
    
	
	/**
		The Certificate is not found in the trust-store.
		If shouldAddToTrustStore( c ) returns false, then a CertificateException
		is thrown.  Otherwise, addCertificateToTrustStore( c ) is called.
		
		@param c
	 */
		protected void
	certificateNotInTrustStore( final Certificate c )
		throws IOException,
		KeyStoreException, NoSuchAlgorithmException, CertificateException
	{
        if ( shouldAddToTrustStore( c ) )
        {
        	addCertificateToTrustStore( c );
        }
        else
        {
            throw new CertificateException( "Certificate not trusted:\n" + c );
        }
	}
	
	/**
		Get the KeyStore containing the Certificates to be trusted.  This should
		be a KeyStore corresponding to the file that was specified.  The same
		KeyStore should be returned each time.
		
		@return KeyStore
	 */
		protected synchronized KeyStore
	getTrustStore()
		throws IOException,
			CertificateException, NoSuchAlgorithmException, KeyStoreException, FileNotFoundException
	{
		if ( mTrustStore == null )
		{
			mTrustStore	= KeyStore.getInstance( mKeyStoreType );
			final File	f	= getTrustStoreFile();
		
			final char[]	pw	= getTrustStorePassword();
			if ( f.exists() )
			{
				final FileInputStream is	= new FileInputStream( f );
				
				try
				{
					mTrustStore.load( is, pw );
				}
				finally
				{
					is.close();
				}
			}
			else
			{
				mTrustStore.load( null, pw );
			}
		}
		
		return( mTrustStore );
	}
	
	/**
     	@param chain
     	@throws RuntimeException
     	@throws CertificateException
     */    
		protected void
	checkCertificate( final X509Certificate[] chain)
		throws RuntimeException, CertificateException
    {
		try
		{
            //First ensure that the certificate is valid.
            for (int i = 0 ; i < chain.length ; i ++)
            {
                chain[i].checkValidity();   
            }
            
            mTrustStore	= getTrustStore();
            
            final Certificate	cert	= chain[ 0 ];
            
            //if the certificate already exists in the truststore, it is implicitly trusted
            if ( mTrustStore.getCertificateAlias( cert ) == null )
            {
            	certificateNotInTrustStore( cert );
            }
            else
            {
            	// System.out.println( "cert trusted: " + cert );
            }
        }
        catch (CertificateException e)
        {
            throw e;
        }
        catch (Exception e)
        {        
			throw new RuntimeException( e );
		}
	}

	
		public String
	toString()
	{
		return( "TrustStoreTrustManager--trusts certificates found in truststore: " + mTrustStore );
	}
}



