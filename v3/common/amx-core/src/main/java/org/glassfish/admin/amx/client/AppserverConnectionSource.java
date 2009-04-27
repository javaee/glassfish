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

import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.util.MapUtil;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.core.proxy.AMXBooter;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.util.jmx.ConnectionSource;


/**
	Supports connectivity to Sun Appserver 8.1 and later (not available
	prior to 8.1).
	This is the only official way
	to get a connection to the appserver.
	<p>
	If the server is running with TLS enabled, then you must use the constructor
	that includes TLSParams. Here is an example of how to connect using TLS:
<pre>
final File trustStore	= new File( "~/.keystore" );
	final char[] trustStorePassword	= "changeme".toCharArray();	// or whatever it is
final HandshakeCompletedListener listener = new HandshakeCompletedListenerImpl();
final TrustStoreTrustManager trustMgr = new TrustStoreTrustManager( trustStore, trustStorePassword);
trustMgr.setPrompt( true );

final TLSParams	tlsParams = new TLSParams( new X509TrustManager[] { trustMgr }, listener );
final AppserverConnectionSource src	=
	new AppserverConnectionSource( AppserverConnectionSource.PROTOCOL_RMI,
		"localhost", 8686, "admin", "admin123",
		tlsParams,
		null );
final DomainRoot domainRoot	= src.getDomainRootProxy();
</pre>
	If security is not an  issue, it is recommended to simply disable TLS on the
	server.  However, you can also connect using TLS whereby the 
	server certificate is blindly trusted:
<pre>
final TLSParams	tlsParams = new TLSParams( TrustAnyTrustManager.getInstanceArray(), null );
final AppserverConnectionSource src	=
	new AppserverConnectionSource( AppserverConnectionSource.PROTOCOL_RMI,
		"localhost", 8686, "admin", "admin123",
		tlsParams,
		null );
final DomainRoot domainRoot	= src.getDomainRootProxy();
</pre>
	
	
	@see org.glassfish.admin.amx.client.TrustStoreTrustManager
	@see org.glassfish.admin.amx.client.TrustAnyTrustManager
	@see org.glassfish.admin.amx.client.HandshakeCompletedListenerImpl
	@see org.glassfish.admin.amx.client.TLSParams
 */
public final class AppserverConnectionSource
	implements NotificationListener, ConnectionSource
{
	private final String			mHost;
	private final int				mPort;
	private final String			mProtocol;
	private final String			mUser;
	private final String			mPassword;
	private final TLSParams			mTLSParams;
	private final Map<String,String> mReserved;
	
	protected JMXConnector			mJMXConnector;
	
	/**
		FIXME FIXME FIXME
		This should be removed after the http connector supports the HandshakeCompletedListener
	 */
	private static final boolean DISABLE_HANDSHAKE_COMPLETED_CHECK = true;

		private boolean 
	disableHandShakeCompletedCheck()
	{
		return( DISABLE_HANDSHAKE_COMPLETED_CHECK && mProtocol.equals( PROTOCOL_HTTP ) );
	}
	
	
	/**
	    @return true if the specified protocol is supported.
	 */
		public static boolean
	isSupportedProtocol( final String protocol )
	{
		return(	protocol != null &&
				(
					protocol.equals( PROTOCOL_JMXMP ) ||
					protocol.equals( PROTOCOL_HTTP ) ||
					protocol.equals( PROTOCOL_RMI )
				)
			);
	}
	
	
	/**
		[used internally]
	 */
	public final static String	TRUST_MANAGERS_KEY	= "TRUST_MANAGER_KEY";
	
	/**
		[used internally]
	 */
	public final static String	HANDSHAKE_COMPLETED_LISTENER_KEY	= "HandshakeCompletedListener";
	
	
	private static final String	PROTOCOL_PREFIX	= "sun-as-";
	
	/**
		RMI protocol to the Appserver.
		<b>Do not use the literal value of this constant in your code;
		it is subject to change</b>
	 */
	public final static String	PROTOCOL_RMI		= PROTOCOL_PREFIX + "rmi";
	
	/**
		JMXMP protocol to the Appserver.
		<b>Do not use the literal value of this constant in your code;
		it is subject to change</b>
	 */
	public final static String	PROTOCOL_JMXMP		= PROTOCOL_PREFIX + "jmxmp";
    
	/**
		Default protocol to the Appserver eg PROTOCOL_RMI.
	 */
	public final static String	DEFAULT_PROTOCOL	= PROTOCOL_JMXMP;
	
	/**
		Internal unsupported protocol.  Not supported for external use.
		<b>Do not use the literal value of this constant in your code;
		it is subject to change</b>
	 */
	public final static String	PROTOCOL_HTTP		= PROTOCOL_PREFIX + "http";
	
	private static final String	INTERNAL_HTTP	= "s1ashttp";
	private static final String	INTERNAL_HTTPS	= "s1ashttps";
	
	/**
		Packages for http(s) JMXConnectorProvider.
	 */
	private final static String HTTP_FACTORY_PACKAGES	=
		"com.sun.enterprise.admin.jmx.remote.protocol";
	
	/**
	 	Name of the default TrustStore file, located in the client's
	 	home directory.
	 	
	  */
	public static final String	DEFAULT_TRUST_STORE_NAME	= ".asadmintruststore";
	
	/**
		The default pasword for {@link #DEFAULT_TRUST_STORE_NAME}.
	 */
	public static final String	DEFAULT_TRUST_STORE_PASSWORD	= "changeit";
	
	/**
		Create a new instance using the default protocol without TLS.
		
		@param host	hostname or IP address
		@param port	port to which to connect
		@param user username
		@param userPassword password for specified username
		@param reserved  reserved for future use
	 */
		public
	AppserverConnectionSource(
		String		host,
		int			port,
		String		user,
		String		userPassword,
		final Map<String,String>	reserved)
	{
		this( DEFAULT_PROTOCOL, host, port, user, userPassword, reserved);
	}


	/**
		Create a new instance connecting to the specified host/port with
		the specified protocol without TLS.
		<p>
		<b>Note:</b>The only supported protocol is {@link #PROTOCOL_RMI}.
		Use of any other protocol is not supported and these protocols are
		subject to change or removal at any time.
		
		@param protocol protocol to use eg PROTOCOL_RMI
		@param host	hostname or IP address
		@param port	port to which to connect
		@param user username
		@param userPassword password for specified username
		@param reserved  reserved for future use
	 */
		public
	AppserverConnectionSource(
		final String		protocol,
		final String		host,
		final int			port,
		final String		user,
		final String		userPassword,
		final Map<String,String>			reserved )
	{
		this ( protocol, host, port, user, userPassword, null, reserved );
	}
	
	/**
		Create a new instance which will use TLS for security if specified.
		
		@param protocol protocol to use eg PROTOCOL_RMI
		@param host	hostname or IP address
		@param port	port to which to connect
		@param user username
		@param userPassword password for specified username
		@param tlsParams (may be null if TLS is not desired)
		@param reserved  reserved for future use
		
		@see org.glassfish.admin.amx.client.TLSParams
	 */
		public
	AppserverConnectionSource(
		final String		protocol,
		final String		host,
		final int			port,
		final String		user,
		final String		userPassword,
		final TLSParams		tlsParams,
		final Map<String,String> reserved )
	{
		if ( reserved != null && reserved.keySet().size() != 0 )
		{
			throw new IllegalArgumentException( "No parameters may be passed in 'reserved' Map" );
		}
		
		if ( isSupportedProtocol( protocol ) )
		{
			mHost		= host;
			mPort		= port;
			mProtocol	= protocol;
			mUser		= user;
			mPassword	= userPassword;
			mTLSParams	= tlsParams;
			mReserved	= reserved;
		}
		else
		{
			throw new IllegalArgumentException( "unsupported protocol: " + protocol +
				", use either PROTOCOL_RMI or PROTOCOL_HTTP" );
		}
	}
	
		private Object
	envGet( final String key )
	{
		return( mReserved == null ? null : mReserved.get( key ) );
	}
	
		private final boolean
	useTLS()
	{
		return( mTLSParams != null  );
	}
	
		private final X509TrustManager[]
	getTrustManagers()
	{
		return( mTLSParams == null ? null : mTLSParams.getTrustManagers() );
	}
	
		private final HandshakeCompletedListener
	getSuppliedHandshakeCompletedListener()
	{
		return( mTLSParams == null ? null : mTLSParams.getHandshakeCompletedListener() );
	}
	
	
		private Map<String,Object>
	getCredentialsEnv(
		final String	user,
		final String	password )
	{
		final HashMap<String,Object>	env	= new HashMap<String,Object>();
			
		final String[]	credentials	= new String[] { mUser, mPassword };
		
		env.put( JMXConnector.CREDENTIALS, credentials );
		
		return( env );
	}
	
	private static final String	APPSERVER_JNDI_NAME	= "/management/rmi-jmx-connector";

		private void
	warning( final String msg )
	{
		System.out.println( "\n***\nWARNING: " + msg );
	}
	
		private JMXConnector
	createNew()
		throws MalformedURLException, IOException
	{
		final Map<String,Object>	env	= getCredentialsEnv( mUser, mPassword );
		
		// NetBeans fix; classloader must be more than system classloader
		env.put("jmx.remote.protocol.provider.class.loader",
			this.getClass().getClassLoader()); 

		final HandshakeCompletedListenerImpl	hcListener	=
			new HandshakeCompletedListenerImpl( getSuppliedHandshakeCompletedListener() );
		
		JMXServiceURL	url	= null;
		if ( mProtocol.equals( PROTOCOL_HTTP ) )
		{
			if ( useTLS() )
			{
				//env.put( TRUST_MANAGERS_KEY, getTrustManagers() );
                                final X509TrustManager[] tms = getTrustManagers();
                                if (tms != null && tms.length >= 1) {
                                    env.put( TRUST_MANAGERS_KEY, tms[0]);
                                }
				env.put( HANDSHAKE_COMPLETED_LISTENER_KEY, hcListener );
			}
			
			env.put( "com.sun.enterprise.as.http.auth", "BASIC" );
			env.put( "USER", mUser );
			env.put( "PASSWORD", mPassword );
			// so our JMXConnectorProvider may be found
			env.put( JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, HTTP_FACTORY_PACKAGES );
			
			final String	internalProtocol	= useTLS() ? INTERNAL_HTTPS : INTERNAL_HTTP;
			url	= new JMXServiceURL( internalProtocol, mHost, mPort);
		}
		else if ( mProtocol.equals( PROTOCOL_JMXMP) )
		{
            url = new JMXServiceURL( "jmxmp", mHost, mPort );
        }
		else if ( mProtocol.equals( PROTOCOL_RMI ) )
		{
            /*
			if ( useTLS() )
			{
				// the only way we can communicate with/control the RMI stub is through
				// this special singleton class
				final AdminRMISSLClientSocketFactoryEnvImpl	rmiEnv	=
					AdminRMISSLClientSocketFactoryEnvImpl.getInstance();
				rmiEnv.setHandshakeCompletedListener( hcListener );
				rmiEnv.setTrustManagers( getTrustManagers() );
			}
             * */
			
			final String s = "service:jmx:rmi:///jndi/rmi://" +
				mHost + ":" + mPort  + APPSERVER_JNDI_NAME;
                         
			url	= new JMXServiceURL( s );
		}
		else
		{
			assert( false );
		}
	
    System.out.println( "Connecting using JMXServiceURL: " + url );
    JMXConnector conn = null;
    try
    {
		conn	= JMXConnectorFactory.connect( url, env );
    }
    catch( Exception e )
    {
        e.printStackTrace();
        throw new RuntimeException(e);
    }
		
		/*
			If the connection was established with RMI, it could have been an insecure
			connection if a on-TLS stub was downloaded.  Verify that the
			a TLS Handshake was actually completed.
		 */
		if ( ! disableHandShakeCompletedCheck() )
		{
			if ( useTLS() && hcListener.getLastEvent() == null )
			{
				conn.close();
				throw new IOException( "Connection could not be established using TLS; server is not using TLS" );
			}
		}
		else
		{
			//warning( "HandshakeCompletedCheck is temporarily disabled for PROTOCOL_HTTP" );
			/* This has been commented out -- See CR: 6172198. HTTPS/JMX Connector Implementation
			does not have to accept HandshakeCompletedListener for 8.1*/
		}
		
		conn.addConnectionNotificationListener( this, null, conn );
		
		return( conn );
	}
	
	/**
	    Used internally as callback for {@link javax.management.NotificationListener}.
	    <b>DO NOT CALL THIS METHOD</b>.
	 */
		public void
	handleNotification(
		final Notification	notifIn, 
		final Object		handback) 
	{
		if ( notifIn instanceof JMXConnectionNotification )
		{
			final JMXConnectionNotification	notif	= (JMXConnectionNotification)notifIn;
			
			final String	type	= notif.getType();
		
			if ( type.equals( JMXConnectionNotification.FAILED) ||
				type.equals( JMXConnectionNotification.CLOSED ) )
			{
				mJMXConnector	= null;
			}
		}
	}
	
	/**
		If the connection has already been created, return the existing JMXConnector
		unless 'forceNew' is true or the connection is currently null.
		
		@param forceNew	 if a new connection should be created
		@return JMXConnector 
	 */
		public JMXConnector
	getJMXConnector( final boolean forceNew )
		throws IOException
	{
		if ( forceNew || mJMXConnector == null )
		{
			mJMXConnector	= createNew();
			final MBeanServerConnection conn = getMBeanServerConnection( false );
            
            // ensure that AMX is loaded and ready to use
            AMXBooter.bootAMX(conn);
		}
		
		return( mJMXConnector );
	}
	
	
		public MBeanServerConnection
	getExistingMBeanServerConnection( )
	{
		try
		{
			return( getJMXConnector( false ).getMBeanServerConnection() );
		}
		catch( IOException e )
		{
		}
		return( null );
	}
	
	
	/**
		@return getJMXConnector( forceNew ).getMBeanServerConnection() 
	 */
		public MBeanServerConnection
	getMBeanServerConnection( final boolean forceNew )
		throws IOException
	{
		return( getJMXConnector( forceNew ).getMBeanServerConnection() );
	}
	
    /**
        {@link DomainRoot} will be returned.  Upon return, AMX is
        ready for use.  If the server has just been started, there
        may be a slight delay until AMX is ready for use; this method
        returns only once AMX is ready for use.
       
        @return a DomainRoot
     */
		public DomainRoot
	getDomainRoot()
		throws IOException
	{
		final DomainRoot    domainRoot  =
		    ProxyFactory.getInstance( this ).getDomainRootProxy( true );
		
		return domainRoot;
	}
	
		public String
	toString()
	{
		return( 
			"protocol=" + mProtocol + 
			", host=" + mHost + 
			", port=" + mPort + 
			", user=" + mUser +
			", useTLS={" + useTLS() + "}" +
			", mReserved=" + (mReserved == null ? "null" : MapUtil.toString( mReserved )) );
	}
	

	
}

