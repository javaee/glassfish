
/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/stringifier/HandshakeCompletedEventStringifier.java,v 1.4 2005/11/08 22:39:26 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:26 $
 */
 
package org.glassfish.admin.amx.util.stringifier;

import javax.net.ssl.HandshakeCompletedEvent;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
	Stringifies a HandshakeCompletedEvent.
 */
 
public final class HandshakeCompletedEventStringifier implements Stringifier
{
	public final static HandshakeCompletedEventStringifier	DEFAULT	= new HandshakeCompletedEventStringifier();
	
		public
	HandshakeCompletedEventStringifier()
	{
	}
	
	/**
		Static variant when direct call will suffice.
	 */
		public static String
	stringify( final HandshakeCompletedEvent event )
	{
		final String	NL	= "\n";
			
		StringBuffer	buf	= new StringBuffer();
		buf.append( "Cipher suite: " + event.getCipherSuite() + NL);
		buf.append( "Address: " + event.getSocket().getRemoteSocketAddress().toString() + NL);
		
		try
		{
			buf.append( "Certificate chain:" + NL);
			final Certificate[]	certChain	= event.getPeerCertificates();
			for( int i = 0; i < certChain.length; ++i )
			{
				final X509Certificate	cert	= (X509Certificate)certChain[ i ];
				final String			certString	= X509CertificateStringifier.stringify( cert);
				
				buf.append( certString );
				buf.append( NL + "=>" + NL );
			}
		}
		catch ( javax.net.ssl.SSLPeerUnverifiedException e )
		{
			buf.append( "TLS PEER UNVERIFIED (no certificate)" );
		}
		
		return( buf.toString() );
	}
	
		public String
	stringify( Object object )
	{
		return( stringify( (X509Certificate)object ) );
	}
}

