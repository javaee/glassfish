/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/security/TrustAnyTrustManager.java,v 1.2 2005/11/08 22:39:26 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:39:26 $
 */
package com.sun.cli.jcmd.util.security;

import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.io.IOException;

import javax.net.ssl.X509TrustManager;

//import org.glassfish.admin.amx.util.stringifier.SmartStringifier;

/**
	This TrustManager applies no logic as to whether its peer
	certificate is trusted; it trusts all peers.
	This is a security risk, and should be used only for convenience
	in testing or where security is explicitly not an issue.
 */
public final class TrustAnyTrustManager implements X509TrustManager
{
	private static TrustAnyTrustManager		INSTANCE	= null;
	private static TrustAnyTrustManager[]	INSTANCE_ARRAY	= null;
	
	private	TrustAnyTrustManager()	{}
	
	/**
		Get an instance; only one is ever created.
	 */
		public static synchronized TrustAnyTrustManager
	getInstance()
	{
		if ( INSTANCE == null )
		{
			INSTANCE		= new TrustAnyTrustManager();
			INSTANCE_ARRAY	= new TrustAnyTrustManager[] { INSTANCE };
		}
		return( INSTANCE );
	}
	
	/**
		Calls getInstance() and returns an array containing it.
	 */
		public static TrustAnyTrustManager[]
	getInstanceArray()
	{
		getInstance();
		return( INSTANCE_ARRAY );
	}
	
		public void
	checkClientTrusted( X509Certificate[] chain, String authType)
		throws CertificateException
	{
		//trace( "checkClientTrusted, authType = " + authType +
		//	", " + SmartStringifier.toString( chain ) );
	}
	
		public void
	checkServerTrusted( X509Certificate[] chain, String authType)
		throws CertificateException
	{
		//trace( "checkServerTrusted, authType = " + authType +
		//	", " + SmartStringifier.toString( chain ) );
	}
	
		public X509Certificate[]
	getAcceptedIssuers()
	{
		return( new X509Certificate[ 0 ] );
	}
	
	
		private final void
	trace( Object o )
	{
		System.out.println( "### TrustAnyTrustManager: " + o.toString() );
	}
	
		public String
	toString()
	{
		return( "TrustAnyTrustManager--trusts all certificates with no check whatsoever" );
	}
}