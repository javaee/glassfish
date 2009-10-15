/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/security/HandshakeCompletedListenerImpl.java,v 1.2 2005/11/08 22:39:25 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:39:25 $
 */
package com.sun.cli.jcmd.util.security;

import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

import java.security.cert.Certificate;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.SSLPeerUnverifiedException;

	
/**
	A default HandshakeCompletedListener which remembers the
	last HandshakeCompletedEvent that occured.
 */
public class HandshakeCompletedListenerImpl implements HandshakeCompletedListener
{
	private HandshakeCompletedEvent	mEvent;
	
	public	HandshakeCompletedListenerImpl()
	{
		mEvent	= null;
	}
	
		public synchronized void
	handshakeCompleted( HandshakeCompletedEvent event) 
	{
		//trace( "handshakeCompleted: " + this + ":\n" +
			//HandshakeCompletedEventStringifier.stringify( event ) );
		mEvent	= event;
	}
	
	/**
		Get the last HandshakeCompletedEvent which occurred, possibly null.
	 */
		public synchronized HandshakeCompletedEvent
	getLastEvent()
	{
		return( mEvent );
	}
	
		private final void
	trace( Object o )
	{
		System.out.println( "### HandshakeCompletedListenerImpl: " + o.toString() );
	}
		
}