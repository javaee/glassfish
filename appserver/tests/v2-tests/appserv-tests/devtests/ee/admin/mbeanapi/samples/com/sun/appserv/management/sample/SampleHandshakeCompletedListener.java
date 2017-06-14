/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.appserv.management.sample;

import javax.net.ssl.HandshakeCompletedEvent;

import com.sun.appserv.management.client.HandshakeCompletedListenerImpl;


/**
	Demonstrates how to write a HandshakeCompletedListener. Note that usually
	it is not necessary to write your own HandshakeCompletedListener since the
	event is available from {@link HandshakeCompletedListenerImpl#getLastEvent}.
	<p>
	You may wish to write a HandshakeCompletedListener if the data contained
	in the HandshakeCompletedEvent is of interest or you wish to exert more
	control over the TLS connection.
 */
public final class SampleHandshakeCompletedListener
	extends HandshakeCompletedListenerImpl
{
		public
	SampleHandshakeCompletedListener()
	{
	}
	
		public synchronized void
	handshakeCompleted( final HandshakeCompletedEvent event) 
	{
		super.handshakeCompleted( event );
		
		System.out.println( "HandshakeCompleted:\n" + event + "\n" );
	}
}






