/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/rmi/RMISSLClientSocketFactoryEnvImpl.java,v 1.2 2004/07/26 23:03:48 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2004/07/26 23:03:48 $
 */
package com.sun.cli.jmxcmd.security.rmi;

import java.io.File;
import java.io.Serializable;

import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import javax.net.ssl.HandshakeCompletedListener;

/**
	<b>Not for public use</b>
	<p>
	Class which encapsulates knowledge of how to exchange data between the
	RMISSLClientSocketFactory stub and the JVM into which it gets downloaded.
	<p>
	The client code configures this class so the RMI client stub may obtain
	any environmental overrides, thus enabling the client to control how the RMI
	stub behaves.
	<p>
	This class is global for all outgoing connections for RMI.
 */
public final class RMISSLClientSocketFactoryEnvImpl // do NOT implement Serializable
	implements RMISSLClientSocketFactoryEnv
{
	private static RMISSLClientSocketFactoryEnvImpl	INSTANCE	= null;
	
	private transient File				mTrustStore	= null;
	private transient char[]			mTrustStorePassword	= null;
	private transient TrustManager[]	mTrustManagers	= null;
	private transient HandshakeCompletedListener	mHandshakeCompletedListener	= null;
	
	private transient KeyManager[]		mKeyManagers	= null;
	
	private transient boolean			mTrace	= false;
	
		public static synchronized RMISSLClientSocketFactoryEnvImpl
	getInstance()
	{
		// these should only ever be a single instance
		if ( INSTANCE == null )
		{
			INSTANCE	= new RMISSLClientSocketFactoryEnvImpl();
		}
		return( INSTANCE );
	}

    	private
    RMISSLClientSocketFactoryEnvImpl( )
    {
    	// important for server side to have these in place, or they
    	// will look for a truststore
    	mTrustManagers	= new TrustManager[0];
    }
    
    /**
    	Set the TrustManagers.  Removes any existing trust-store and trust-store password
    	as the TrustManagers will be used instead.
    	
    	@param trustManagers
     */
    	public void
    setTrustManagers( final TrustManager[] trustManagers )
    {
    	mTrustStore			= null;
    	mTrustStorePassword	= null;
    	mTrustManagers		= trustManagers;
    }

     	public TrustManager[]
    getTrustManagers( )
    {
    	return( mTrustManagers );
    }
    
        public KeyManager[]
    getKeyManagers( )
    {
    	return( mKeyManagers );
    }
    
    /**
    	Optional, for client auth.
    	
    	@param keyManagers
    */
    	public void
    setKeyManagers( final KeyManager[] keyManagers )
    {
    	mKeyManagers	= keyManagers;
    }
    
    /**
    	Set the trust-store and its password.  Removes any existing TrustManagers
    	as the trust-store will be used instead.
    	
    	@param trustStore
    	@param trustStorePassword
     */
    	public void
    setTrustStore( final File trustStore, final char[] trustStorePassword )
    {
    	// must both be null or non-null
    	if ( trustStore == null || trustStorePassword == null )
    	{
    		// one of them is null, so both are required to be null
    		if ( trustStore != null || trustStorePassword != null)
    		{
    			throw new IllegalArgumentException();
    		}
    	}
    	
    	mTrustStore			= trustStore;
    	mTrustStorePassword	= trustStorePassword;
    	mTrustManagers		= null;
    }
    
     	public File
    getTrustStore( )
    {
    	return( mTrustStore );
    }
    
     	public char[]
    getTrustStorePassword( )
    {
    	return( mTrustStorePassword );
    }
    
    /**
    	Set a HandshakeCompletedListener (optional).
    	
    	@param listener
     */
    	public void
    setHandshakeCompletedListener( final HandshakeCompletedListener listener )
    {
    	mHandshakeCompletedListener	= listener;
    }
    
    	public HandshakeCompletedListener
    getHandshakeCompletedListener( )
    {
    	return( mHandshakeCompletedListener );
    }
    
    /**
    	Set tracing on or off.
    	
    	@param trace
     */
    	public void
    setTrace( final boolean trace )
    {
    	mTrace	= trace;
    }
    	public boolean
    getTrace(  )
    {
    	return( mTrace );
    }
	
}
