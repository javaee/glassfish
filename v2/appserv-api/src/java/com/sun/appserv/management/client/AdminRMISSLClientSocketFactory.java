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
 
/*
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/client/AdminRMISSLClientSocketFactory.java,v 1.1 2006/12/02 06:02:52 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2006/12/02 06:02:52 $
 */
package com.sun.appserv.management.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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

import com.sun.appserv.management.client.TrustAnyTrustManager;


/**
	<b>Not for public use</b>
	<p>
	RMISSLClientSocketFactory which allows the configuration of security parameters
	by an RMI client; by default is not possible to configure these parameters.
	This is the "stub" class that gets downloaded to the client.
	<p>
	Looks for RMISSLClientSocketFactoryEnv, and if found, uses it to get
	configuration.
 */
public class AdminRMISSLClientSocketFactory
    implements RMIClientSocketFactory, Serializable
{
	static final long serialVersionUID = 5096547838871926785L;
	
	private transient SSLSocketFactory	mFactory	= null;
	
	private transient MyEnvImpl	mEnvImpl	= null;
	
		public
	AdminRMISSLClientSocketFactory()
	{
		mEnvImpl	= new MyEnvImpl();
	}
	
		private static TrustManager[]
	getTrustAny()
	{
		final TrustManager[]	trustManagers	= new TrustManager[ 1 ];
		trustManagers[ 0 ]	= TrustAnyTrustManager.getInstance();
		return( trustManagers );
	}
	
	/**
		If the 'env' class is not available eg a client that does not have the client jar,
		then use this implementation.  Lack of the env class implies that the client cannot
		configure anything and must rely on system properties, which this class
		handles.
	 */
	private final class MyEnvImpl
		implements AdminRMISSLClientSocketFactoryEnv
	{
		private transient boolean			mTrace	= false;
		private	MyEnvImpl()	{}
    
	    	public TrustManager[]
	    getTrustManagers( )
	    {
	    	final TrustStoreTrustManager	mgr	= TrustStoreTrustManager.getSystemInstance();
	    	
	    	return new TrustManager[] { mgr };
	    }
	    
	    public HandshakeCompletedListener getHandshakeCompletedListener( )	{ return null; }
	    
	    public void		setTrace( final boolean trace )	{ mTrace = trace; }
	    public boolean	getTrace()	{ return mTrace; }
	    
	    public Object	getValue( final String key )	{ return null; }
	}
    
    
	/**
		Note that the environment is useless if it gets downloaded, since it would never be 
		possible for a client to configure it! However, that is OK as default 
		behavior will be used.
	 */
		private synchronized AdminRMISSLClientSocketFactoryEnv
	getEnv()
	{
		return( AdminRMISSLClientSocketFactoryEnvImpl.getInstance() );
	}
	
		private final void
	trace( Object o )
	{
		if ( getEnv().getTrace() )
		{
			final String	name	= this.getClass().getName();
			Logger.getLogger( name ).info( toString() + ": " + o.toString() );
		}
	}
	
		private static char[]
	toCharArray( final String s )
	{
		return( s == null ? null : s.toCharArray() );
	}
	
	
		private final SSLSocketFactory
	createSocketFactory( final AdminRMISSLClientSocketFactoryEnv env )
		throws IOException
	{
		SSLSocketFactory	factory	= null;
		
        try
        {
        	final TrustManager[]	trustManagers	= env.getTrustManagers( );
            
            final SSLContext sslContext = SSLContext.getInstance( "TLSv1" );
            sslContext.init( null, trustManagers, null );
            factory = sslContext.getSocketFactory();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
            throw (IOException) new IOException().initCause(e);
        }
        
        return( factory );
	}
	
	
	
    	public synchronized Socket
    createSocket(
    	final String	host,
    	final int		port)
    	throws IOException
    {
    	final String	target	= host + ":" + port;
    	
		trace( "createSocket: " + target );
    	
    	final AdminRMISSLClientSocketFactoryEnv	env	= getEnv();
    	
        if ( mFactory == null)
        {
        	mFactory = createSocketFactory( env );
			trace( "createSocket: created socket factory" );
        }
        
		//trace( "creating socket: " + target );
        final SSLSocket sslSocket	= (SSLSocket)mFactory.createSocket( host, port );
        
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











