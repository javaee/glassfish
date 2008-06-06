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
package com.sun.appserv.management.client;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.AMXAttributes;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.client.handler.ConverterHandlerFactory;
import com.sun.appserv.management.client.handler.ConverterHandlerUtil;
import com.sun.appserv.management.client.handler.ProxyCache;
import com.sun.appserv.management.helper.AMXDebugHelper;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.jmx.MBeanProxyHandler;
import com.sun.appserv.management.util.jmx.MBeanServerConnectionConnectionSource;
import com.sun.appserv.management.util.jmx.MBeanServerConnectionSource;
import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.relation.MBeanServerNotificationFilter;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
	Factory for {@link AMX} proxies.
	Usually proxies are obtained by starting with the DomainRoot obtained via
	{@link AppserverConnectionSource#getDomainRoot}.
	
	@see com.sun.appserv.management.client.AppserverConnectionSource
 */
public final class ProxyFactory implements NotificationListener
{
	private final ProxyCache		mProxyCache;
	private final ConnectionSource	mConnectionSource;
	private final ObjectName		mDomainRootObjectName;
	private final DomainRoot		mDomainRoot;
	private final String			mMBeanServerID;
    
    private static final AMXDebugHelper mDebug  =
        new AMXDebugHelper( "com.sun.appserv.management.client.ProxyFactory" );
    private static void debug( final Object... args ) { mDebug.println( args ); }
	
	private static final Map<MBeanServerConnection,ProxyFactory> INSTANCES	=
	    Collections.synchronizedMap( new HashMap<MBeanServerConnection,ProxyFactory>() );
    
    /**
        Because ProxyFactory is used on both client and server, emitting anything to stdout
        or to the log is unacceptable in some circumstances.  Warnings remain available
        if the AMX-DEBUG system property allows it.
     */
        private static void
    warning( final Object... args )
    {
        debug( args );
    }
	
		private
	ProxyFactory( final ConnectionSource connSource )
	{
        mDebug.setEchoToStdOut( true );
        
		assert( connSource != null );
		
		mConnectionSource	= connSource;
		mProxyCache			= new ProxyCache();
		
		try
		{
			final MBeanServerConnection	conn	= getConnection();
			
			mMBeanServerID		= JMXUtil.getMBeanServerID( conn );
				
			mDomainRoot	          = AMXBooter.bootAMX(conn, true);
			mDomainRootObjectName = Util.getObjectName(mDomainRoot);
			
			// we should always be able to listen to MBeans--
			// but the http connector does not support listeners
			try
			{
				final MBeanServerNotificationFilter	filter	=
					new MBeanServerNotificationFilter();
				filter.enableAllObjectNames();
				filter.disableAllTypes();
				filter.enableType( MBeanServerNotification.UNREGISTRATION_NOTIFICATION );
				
				JMXUtil.listenToMBeanServerDelegate( conn, this, filter, null );
			}
			catch( Exception e )
			{
				warning( "ProxyFactory: connection does not support notifications: ",
                    mMBeanServerID, connSource);
			}
			
			// same idea as above, this time we want to listen to connection died
			// plus there may not be a JMXConnector involved
			final JMXConnector	connector	= connSource.getJMXConnector( false );
			if ( connector != null )
			{
				try
				{
					connector.addConnectionNotificationListener( this, null, null );
				}
				catch( Exception e )
				{
					warning("addConnectionNotificationListener failed: ",
                        mMBeanServerID, connSource, e);
				}
			}
		}
		catch( Exception e )
		{
			warning( "ProxyFactory.ProxyFactory:\n", e );
			throw new RuntimeException( e );
		}
	}
	
	
	/**
		The connection is bad.  Tell each proxy its gone and remove it.
	 */
		private void
	connectionBad()
	{
        final Set<AMX>   proxies  = new HashSet<AMX>();
        proxies.addAll( mProxyCache.values() );
        mProxyCache.clear();
        
        for( final AMX proxy : proxies )
        {
            ConverterHandlerUtil.connectionBad( proxy );
        }
	}
	
	/**
		Verify that the connection is still alive.
	 */
		public boolean
	checkConnection()
	{
		boolean	connectionGood	= true;
		
		try
		{
			getConnection().isRegistered( JMXUtil.getMBeanServerDelegateObjectName() );
			connectionGood	= true;
		}
		catch( Exception e )
		{
			connectionBad();
		}
		
		return( connectionGood );
	}
	

		void
	notifsLost()
	{
		// should probably check each proxy for validity, but not clear if it's important...
	}
	
	/**
		Listens for MBeanServerNotification.UNREGISTRATION_NOTIFICATION and
		JMXConnectionNotification and takes appropriate action.
		<br>
	    Used internally as callback for {@link javax.management.NotificationListener}.
	    <b>DO NOT CALL THIS METHOD</b>.
	 */
		public void
	handleNotification(
		final Notification	notifIn, 
		final Object		handback) 
	{
		final String	type	= notifIn.getType();
		
		if ( type.equals( MBeanServerNotification.UNREGISTRATION_NOTIFICATION)  )
		{
			final MBeanServerNotification	notif	= (MBeanServerNotification)notifIn;
			final ObjectName	objectName	= notif.getMBeanName();
			final AMX proxy	= getProxy( objectName, AMX.class, false );
			mProxyCache.remove( objectName );
			ConverterHandlerUtil.targetUnregistered(proxy);
			
			//debug( "ProxyFactory.handleNotification: UNREGISTERED: ", objectName );
		}
		else if ( notifIn instanceof JMXConnectionNotification )
		{
			if ( type.equals( JMXConnectionNotification.CLOSED ) ||
				type.equals( JMXConnectionNotification.FAILED ) )
			{
                debug( "ProxyFactory.handleNotification: connection closed or failed: ", notifIn);
				connectionBad();
			}
			else if ( type.equals( JMXConnectionNotification.NOTIFS_LOST ) )
			{
                debug( "ProxyFactory.handleNotification: notifications lost: ", notifIn);
				notifsLost();
			}
		}
		else
		{
			debug( "ProxyFactory.handleNotification: UNKNOWN notification: ", notifIn );
		}
	}
    
        	
	private final static String	DOMAIN_ROOT_KEY	= "DomainRoot";
	
		public DomainRoot
	createDomainRoot( )
		throws IOException
	{
		return( mDomainRoot );
	}
	
		public DomainRoot
	initDomainRoot( )
		throws IOException
	{
		final ObjectName	domainRootObjectName	= getDomainRootObjectName( );
		
		final DomainRoot	domainRoot	= (DomainRoot)
			newProxyInstance(domainRootObjectName, new Class[] { DomainRoot.class });
		
		return( domainRoot );
	}
	
	/**
	    Return the DomainRoot.  AMX may not yet be fully 
	    initialized; call getDomainRoot( true ) if AMX
	    must be initialized upon return.
	    
		@return the DomainRoot for this factory.
	 */
		public DomainRoot
	getDomainRoot( )
	{
		return getDomainRoot( false );
	}
	
	/**
	    If 'waitReady' is true, then upon return AMX
	    is guaranteed to be fully loaded.  Otherwise
	    AMX MBeans may continue to initialize asynchronously.
	    
	    @param waitReady
		@return the DomainRoot for this factory.
	 */
		public DomainRoot
	getDomainRoot( boolean waitReady )
	{
	    if ( waitReady )
	    {
	        mDomainRoot.waitAMXReady();
	    }
	    
		return( mDomainRoot );
	}
	
	
	/**
		@return the ConnectionSource used by this factory
	 */
		public ConnectionSource
	getConnectionSource()
	{
		return( mConnectionSource );
	}
	
	/**
		@return the JMX MBeanServerID for the MBeanServer in which MBeans reside.
	 */
		public String
	getMBeanServerID()
	{
		return( mMBeanServerID );
	}
	
	/**
		Return the ObjectName for the DomainMBean.
	 */
		public ObjectName
	getDomainRootObjectName()
		throws IOException
	{
		return( mDomainRootObjectName );
	}
	
	/**
		Get an instance of the ProxyFactory for the MBeanServer.  Generally
		not applicable for remote clients.
		
		@param server
	 */
		public static ProxyFactory
	getInstance( final MBeanServer server )
	{
		return( getInstance( new MBeanServerConnectionSource( server ), true ) );
	}
	
	/**
		Get an instance of the ProxyFactory for the MBeanServerConnection.
		Creates a ConnectionSource for it and calls getInstance( connSource, true ).
	 */
		public static ProxyFactory
	getInstance( final MBeanServerConnection conn )
	{
		return( getInstance( new MBeanServerConnectionConnectionSource( conn ), true ) );
	}
	
	/**
		Calls getInstance( connSource, true ).
	 */
		public static ProxyFactory
	getInstance( final ConnectionSource conn )
	{	
		return( getInstance( conn, true ) );
	}
	
	/**
		Get an instance.  If 'useMBeanServerID' is false, and
		the ConnectionSource is not one that has been passed before, a new ProxyFactory
		is instantiated which will not share its proxies with any previously-instantiated
		ones.  Such usage is discouraged, as it duplicates proxies.  Pass 'true' unless
		there is an excellent reason to pass 'false'.
		
		@param connSource			the ConnectionSource
		@param useMBeanServerID		use the MBeanServerID to determine if it's the same server
	 */
		public static synchronized ProxyFactory
	getInstance(
		final ConnectionSource	connSource,
		final boolean			useMBeanServerID )
	{
		ProxyFactory	instance	= findInstance( connSource );
		
		if ( instance == null )
		{
			try
			{
				// match based on the MBeanServerConnection; different
				// ConnectionSource instances could wrap the same connection
				final MBeanServerConnection	conn =
					connSource.getMBeanServerConnection( false );
				
				instance	= findInstance( conn );
				
				// if not found, match based on MBeanServerID as requested, or if this
				// is an in-process MBeanServer
				if ( instance == null &&
					( useMBeanServerID  || connSource instanceof MBeanServerConnectionSource ) )
				{
					final String	id	= JMXUtil.getMBeanServerID( conn );
					instance	= findInstanceByID( id );
				}
			
				if ( instance == null )
				{
                    debug( "Creating new ProxyFactory for ConnectionSource / conn", connSource, conn );
					instance	= new ProxyFactory( connSource );
					INSTANCES.put( conn, instance );
				}
                
                // ensure that AMX is booted and ready to go.
			}
			catch( Exception e )
			{
				warning( "ProxyFactory.getInstance: failure creating ProxyFactory: ", e );
				throw new RuntimeException( e );
			}
		}
		
		return( instance );
	}
	
	/**
		@return ProxyFactory corresponding to the ConnectionSource
	 */
		public static synchronized ProxyFactory
	findInstance( final ConnectionSource conn )
	{
		return( INSTANCES.get( conn ) );
	}
	
	/**
		@return ProxyFactory corresponding to the MBeanServerConnection
	 */
		public static synchronized ProxyFactory
	findInstance( final MBeanServerConnection conn )
	{
		ProxyFactory	instance	= null;
		
		final Collection<ProxyFactory> values	= INSTANCES.values();
		for( final ProxyFactory factory : values )
		{
			if ( factory.getConnectionSource().getExistingMBeanServerConnection( ) == conn )
			{
				instance	= factory;
				break;
			}
		}
		return( instance );
	}
	
	
	/**
		@return ProxyFactory corresponding to the MBeanServerID
	 */
		public static synchronized ProxyFactory
	findInstanceByID( final String mbeanServerID )
	{
		ProxyFactory	instance	= null;
		
		final Collection<ProxyFactory> values	= INSTANCES.values();
		for( final ProxyFactory factory : values )
		{
			if ( factory.getMBeanServerID().equals( mbeanServerID ) )
			{
				instance	= factory;
				break;
			}
		}
		
		return( instance );
	}
	
    
	/**
		@return an appropriate {@link AMX} interface for the ObjectName
		@Deprecated use versions that take a class as a parameter
	 */
		public AMX
	getProxy( final ObjectName	objectName )
	{
	    return getProxy( objectName, true );
	}
		
	/**
		Get any existing proxy, returning null if none exists and 'create' is false.
		
		@param objectName	ObjectName for which a proxy should be created
		@param create		true to create the proxy, false to return existing value
		@return an appropriate {@link AMX} interface for the ObjectName
		@Deprecated use versions that take a class as a parameter
	 */
		public AMX
	getProxy( final ObjectName	objectName, boolean create )
	{
	    return getProxy( objectName, AMX.class, create );
	}
	
	/**
	    The actual interface(s) that the proxy implements are predetermined.
	    Specifying the interface ties the return type to the interface at compile-time
	    but has no effect on the actual interfaces that are implemented by
	    the proxy.
		@return an appropriate {@link AMX} interface for the ObjectName
	 */
		public synchronized <T extends AMX> T
	getProxy(
	    final ObjectName	objectName,
	    final Class<T>      theInterface )
	{
		return getProxy( objectName, theInterface, true );
	}
	
	/**
		Get any existing proxy, returning null if none exists and 'create' is false.
		
		@param objectName	ObjectName for which a proxy should be created
		@param create		true to create the proxy, false to return existing value
		@param theClass     class of returned proxy, avoids casts and compiler warnings
		@return an appropriate {@link AMX} interface for the ObjectName
	 */
		public synchronized <T extends AMX> T
	getProxy(
	    final ObjectName	objectName,
	    Class<T>            theClass,
	    boolean             create )
	{
		AMX	proxy	= mProxyCache.getCachedProxy( objectName );
		
		if ( proxy == null && create )
		{
			proxy	= createProxy( objectName );
		}
		return theClass.cast( proxy );
	}
	
	/**
		@return MBeanServerConnection used by this factory
	 */
		protected MBeanServerConnection
	getConnection()
		throws IOException
	{
		return( getConnectionSource().getMBeanServerConnection( false ) );
	}
	
	/**
		Create a new proxy.  When a new proxy is created, its parent is
		required, which could cause a chain of proxies to be created.
		@param ObjectName
	 */
		private AMX
	createProxy( final ObjectName	objectName  )
	{
		AMX proxy				= null;
		
		try
		{
			String		proxyInterfaceName	= null;
			Class	proxyInterface		= null;
			
			proxyInterfaceName	= (String)
				getConnection().getAttribute( objectName, AMXAttributes.ATTR_INTERFACE_NAME );
			
			proxyInterface	= ClassUtil.getClassFromName( proxyInterfaceName );
					
			proxy	=  newProxyInstance( objectName, new Class[] { proxyInterface } );
		}
		catch( IllegalArgumentException e )
		{
            debug( "createProxy", e );
			throw e;
		}
		catch( Exception e )
		{
            debug( "createProxy", e );
			throw new RuntimeException( e );
		}
				
		return( proxy );
	}
	
	    private MBeanProxyHandler
	createProxyHandler( final ObjectName objectName )
	    throws IOException
	{
	    return ConverterHandlerFactory.createHandler( mConnectionSource, objectName );
	}

		
	/**
		Instantiates a new proxy using the default AttributeNameMangler and with any desired number
		of interfaces.  If you want NotificationBroadcaster as one of the interfaces, you must
		supply it in the list.
		Use of this routine is discouraged in favor of
		{@link #getProxy}
		
		@param objectName			the target MBean which will be invoked by the proxy
		@param interfaceClasses	all interfaces the proxy should implement
		
		@return the new Proxy implementing the specified interface
	 */
		public AMX
	newProxyInstance(
		final ObjectName			objectName,
		final Class<?>[]			interfaceClasses )
		throws IOException
	{
		final MBeanProxyHandler	handler	= createProxyHandler( objectName );
		
		final ClassLoader		classLoader	= interfaceClasses[ 0 ].getClassLoader();
		
		final AMX proxy	= Util.asAMX(Proxy.newProxyInstance( classLoader, interfaceClasses, handler));
		if ( proxy != null )
		{
		    mProxyCache.remove( objectName );
			mProxyCache.cacheProxy( proxy );
		}
		
		return( proxy );
	}
	
		protected static String
	toString( final Object o )
	{
		return( com.sun.appserv.management.util.stringifier.SmartStringifier.toString( o ) );
	}
    
	/**
		Convert a Set of ObjectName to a Set of AMX.
		
		@return a Set of AMX from a Set of ObjectName.
	 */
		public Set<AMX>
	toProxySet( final Set<ObjectName> objectNames )
	{
		final Set<AMX>	s	= new HashSet<AMX>();
		
		for( final ObjectName objectName : objectNames )
		{
			try
			{
				final AMX	proxy	= getProxy( objectName, AMX.class, true );
				assert( ! s.contains( proxy ) );
				s.add( proxy );
			}
			catch( final Exception e )
			{
			    debug( "ProxyFactory.toProxySet: exception for MBean ",
                    objectName, " = ", ExceptionUtil.getRootCause( e ) );
			}
		}
		
		return( s );
	}
	
	/**
		Convert a Collection of ObjectName to a List of AMX.
		
		@return a List of AMX from a List of ObjectName.
	 */
		public List<AMX>
	toProxyList( final Collection<ObjectName> objectNames )
	{
		final List<AMX>	list	= new ArrayList<AMX>();
		
		for( final ObjectName objectName : objectNames )
		{
			try
			{
				final AMX	proxy	= getProxy( objectName, AMX.class, true );
				list.add( proxy );
			}
			catch( final Exception e )
			{
			    debug( "ProxyFactory.toProxySet: exception for MBean ",
                    objectName, " = ", ExceptionUtil.getRootCause( e ) );
			}
		}
		
		return( list );
	}
	
	/**
		Convert a Map of ObjectName, and convert it to a Map
		of AMX, with the same keys.
		
		@return a Map of AMX from a Map of ObjectName.
	 */
		public Map<String,AMX>
	toProxyMap(
		final Map<String,ObjectName>	objectNameMap )
	{
		final Map<String,AMX> resultMap	= new HashMap<String,AMX>();
		
		final Set<String>   keys    = objectNameMap.keySet();
		
		for( final String key : keys )
		{
			final ObjectName	objectName	= objectNameMap.get( key );
			
			try
			{
				final AMX	proxy	= getProxy( objectName, AMX.class, true );
				resultMap.put( key, proxy );
			}
			catch( final Exception e )
			{
			    debug( "ProxyFactory.toProxySet: exception for MBean ",
                    objectName, " = ", ExceptionUtil.getRootCause( e ) );
			}
		}
		
		return( resultMap );
	}
	
}











