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
package org.glassfish.admin.amx.client;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.X509TrustManager;
import java.io.File;

/**
	Class encapsulating parameters available for use with TLS.
	
	@see org.glassfish.admin.amx.client.TrustStoreTrustManager
	@see org.glassfish.admin.amx.client.TrustAnyTrustManager
	@see org.glassfish.admin.amx.client.HandshakeCompletedListenerImpl
 */
public final class TLSParams
{
	private final X509TrustManager[]	mTrustManagers;
	private final HandshakeCompletedListener	mHandshakeCompletedListener;

    /**
        Return a X509TrustManager[] supporting trust via
        the specified trustStore file using the specified password.
        
        @param trustStore
        @param trustStorePassword password to use for the trustStore file
        @param prompt whether to prompt via System.out/in for addition of new certificates
     */
		public static X509TrustManager[]
	getTrustManagers(
		final File 		trustStore,
		final char[]	trustStorePassword,
		final boolean	prompt )
	{
		final TrustStoreTrustManager	mgr	=
			new TrustStoreTrustManager( trustStore, trustStorePassword );
		mgr.setPrompt( prompt );
			
		final X509TrustManager[]	trustMgrs	= new X509TrustManager[] { mgr };
		
		return( trustMgrs );
	}
	
	/**
		@param trustStore
		@param trustStorePassword
		@param prompt
		@param handshakeCompletedListener (may be null)
	 */
		public
	TLSParams(
		final File 		trustStore,
		final char[]	trustStorePassword,
		final boolean	prompt,
		final HandshakeCompletedListener	handshakeCompletedListener )
	{
		this(  getTrustManagers( trustStore, trustStorePassword, prompt), handshakeCompletedListener );
		
	}
	
	/**
		@param trustManagers
		@param handshakeCompletedListener (may be null)
	 */
		public
	TLSParams(
		final X509TrustManager[]			trustManagers,
		final HandshakeCompletedListener	handshakeCompletedListener )
	{
		if ( trustManagers == null )
		{
			throw new IllegalArgumentException();
		}
		
    	mTrustManagers		= trustManagers;
    	mHandshakeCompletedListener	= handshakeCompletedListener;
	}
	
	/**
		@param trustManager
		@param handshakeCompletedListener (may be null)
	 */
		public
	TLSParams(
		final X509TrustManager				trustManager,
		final HandshakeCompletedListener	handshakeCompletedListener )
	{
		this( new X509TrustManager[] { trustManager }, handshakeCompletedListener );
	}
	
	/**
	    @return the TrustManagers in use
	 */
     	public X509TrustManager[]
    getTrustManagers( )
    {
    	return( mTrustManagers );
    }
    
	/**
	    @return the HandshakeCompletedListener in use or null if none
	 */
    	public HandshakeCompletedListener
    getHandshakeCompletedListener( )
    {
    	return( mHandshakeCompletedListener );
    }
}
