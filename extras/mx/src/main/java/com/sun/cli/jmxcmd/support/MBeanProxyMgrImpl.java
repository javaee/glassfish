/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/MBeanProxyMgrImpl.java,v 1.12 2004/09/09 20:04:43 llc Exp $
 * $Revision: 1.12 $
 * $Date: 2004/09/09 20:04:43 $
 */
 

package com.sun.cli.jmxcmd.support;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;
import java.util.regex.Pattern;

import javax.management.ObjectName;
import javax.management.MBeanRegistration;
import javax.management.StandardMBean;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.MBeanServerNotification;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationListener;
import javax.management.MBeanServer;


import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.management.MalformedObjectNameException;

import org.glassfish.admin.amx.util.jmx.JMXUtil;
import com.sun.cli.jmxcmd.util.ConnectionSource;

import org.glassfish.admin.amx.util.RegexUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;


/**
	Within the scope of the MBeanServer it lives in, and the remote server
	from which it proxies MBeans, a ProxyMgr is a singleton.
 */
public final class MBeanProxyMgrImpl extends StandardMBean
	implements
	MBeanProxyMgrMBean, MBeanRegistration, NotificationListener
{
	final ConnectionSource		mConnectionSource;
	final Map<ObjectName,MBeanProxyInfo>		mProxyObjectNameToProxyInfo;
	final Map<ObjectName,ObjectName>	mTargetObjectNameToProxyObjectName;
	MBeanServer					mSelfMBeanServer;
	ConnectionListener	mListener;
	final String				mDefaultDomain;
	
	/**
		An ordered list (first added is first in the list) of information
		about desired proxies.
	 */
	final List<ProxySetInfo>					mProxySetInfos;
	

		public
	MBeanProxyMgrImpl( ConnectionSource connSource )
		throws NotCompliantMBeanException, IOException
	{
		super( MBeanProxyMgrMBean.class );
		
		mConnectionSource	= connSource;
		
		mSelfMBeanServer					= null;
		mProxyObjectNameToProxyInfo			= Collections.synchronizedMap( new HashMap<ObjectName,MBeanProxyInfo>() );
		mTargetObjectNameToProxyObjectName	= Collections.synchronizedMap( new HashMap<ObjectName,ObjectName>() );
		
		mProxySetInfos	= Collections.synchronizedList( new ArrayList<ProxySetInfo>() );
		
		final MBeanServerConnection	conn	= connSource.getMBeanServerConnection( false );
		try
		{
			mListener = new ConnectionListener( conn );
		}
		catch( Exception e )
		{
			mListener	= null;
			System.err.println( "\n----------\n" +
			"WARNING: registration of MBeanServerDelegate listener on remote server failed " +
			"(non-compliant JMX Connector?), class = " + conn.getClass().getName() +
			"\n----------\n"
			);
			//ExceptionUtil.getRootCause( e ).printStackTrace();
		}
		
		mDefaultDomain	= conn.getDefaultDomain();
	}
	
		private MBeanServerConnection
	getMBeanServerConnection()
		throws IOException
	{
		return( mConnectionSource.getMBeanServerConnection( false ) );
	}
	
							
	private final class ConnectionListener
		implements NotificationListener
	{
		private final MBeanServerConnection		mConn;
		
		ConnectionListener( final MBeanServerConnection conn )
			throws IOException
		{
			mConn	= conn;
			
			final ObjectName	delegateObjectName	=
				JMXUtil.getMBeanServerDelegateObjectName();
			
			try
			{
				mConn.addNotificationListener( delegateObjectName, this, null, null );
			}
			catch( InstanceNotFoundException e )
			{
				throw new RuntimeException( e );
			}
		}
			
			public void
		cleanup()
			throws IOException
		{
			final ObjectName	delegateObjectName	=
				JMXUtil.getMBeanServerDelegateObjectName();
				
			try
			{
				mConn.removeNotificationListener( delegateObjectName, this, null, null );
			}
			catch( InstanceNotFoundException e )
			{
				throw new RuntimeException( e );
			}
			catch( ListenerNotFoundException e )
			{
				throw new RuntimeException( e );
			}
		}
		
			public void
		handleNotification( Notification notif, Object handback )
		{
			MBeanProxyMgrImpl.this.handleNotification( notif, mConn );
		}
	}
		
		private void
	checkUnregisterTarget( final ObjectName	targetObjectName )
		throws InstanceNotFoundException, MBeanRegistrationException
	{
		// dm( "MBeanProxyMgrImpl.checkUnregisterTarget: " + targetObjectName );
			
		final ObjectName	proxyObjectName	= (ObjectName)
			mTargetObjectNameToProxyObjectName.get( targetObjectName );
		
		// dm( "MBeanProxyMgrImpl.checkUnregisterTarget: proxy = " + proxyObjectName );
		if ( mSelfMBeanServer.isRegistered( proxyObjectName ) )
		{
		// dm( "MBeanProxyMgrImpl.checkUnregisterTarget: unregistering: " + proxyObjectName );
		
			mSelfMBeanServer.unregisterMBean( proxyObjectName );
			
			mTargetObjectNameToProxyObjectName.remove( targetObjectName );
			
			mProxyObjectNameToProxyInfo.remove( proxyObjectName );
		}
	}
	
		private boolean
	domainMatches(
		final ObjectName	pattern,
		final ObjectName	candidate )
		throws IOException
	{
		boolean	matches	= false;
		
		final String	candidateDomain	= candidate.getDomain();
		if ( pattern.isDomainPattern() )
		{
			final String	regex	=
				RegexUtil.wildcardToJavaRegex( pattern.getDomain() );
			
			matches	= Pattern.matches( regex, candidateDomain);
		}
		else
		{	
			// domain is not a pattern
			
			String	patternDomain	= pattern.getDomain();
			if ( patternDomain.length() == 0 )
			{
				patternDomain	= mDefaultDomain;
			}
			
			matches	= patternDomain.equals( candidateDomain );
		}
		
		//dm( "MBeanProxyMgrImpl.domainMatches: " + matches + " " + pattern + " vs " + candidate );
		
		return( matches );
	}
	
		private boolean
	matchesPattern(
		final ObjectName	pattern,
		final ObjectName	candidate )
		throws IOException
	{
		boolean	matches	= false;
		
		if ( domainMatches( pattern, candidate ) )
		{
			final String	patternProps	= pattern.getCanonicalKeyPropertyListString();
			final String	candidateProps	= candidate.getCanonicalKeyPropertyListString();
			assert(  patternProps.indexOf( "*" ) < 0 );
			assert(  candidateProps.indexOf( "*" ) < 0 );
			
			// Since we used canonical form any match means the pattern props String
			// must be a substring of candidateProps
			if ( candidateProps.indexOf( patternProps ) >= 0 )
			{
				matches	= true;
			}
		}
		
		return( matches );
	}
	
		private boolean
	matches(
		final Set			namesAndPatterns,
		final ObjectName	candidate )
		throws IOException
	{
		final Iterator	iter	= namesAndPatterns.iterator();
		
		boolean	matches	= false;
		
		final MBeanServerConnection	conn	= getMBeanServerConnection();
		while ( iter.hasNext() )
		{
			final ObjectName	testee	= (ObjectName)iter.next();
			
			if ( testee.isPattern() )
			{
				matches	= matchesPattern( testee, candidate );
			}
			else
			{
				matches	= testee.equals( candidate );
			}
			
			if ( matches )
			{
				break;
			}
		}

		return( matches );
	}
	
	/**
		See if the targetObjectName matches any of our ProxySetInfos.
	 */
		private void
	checkRegisterTarget( final ObjectName targetObjectName )
		throws JMException, IOException
	{
		//dm( "MBeanProxyMgrImpl.checkRegisterTarget: " + targetObjectName );
		ProxySetInfo	proxySetContaining	= null;
		
        for( final ProxySetInfo proxySet : mProxySetInfos )
		{
			if ( matches( proxySet.getNamesAndPatterns(), targetObjectName ) )
			{
				proxySetContaining	= proxySet;
				break;
			}
		}
		
		if ( proxySetContaining != null )
		{
			//dm( "MBeanProxyMgrImpl.checkRegisterTarget: match: " + targetObjectName );
			addProxy( targetObjectName,
				proxySetContaining.getUseNewNames(),
				proxySetContaining.getMBeanInfoRefreshMillis(),
				proxySetContaining.getAttributeRefreshMillis() );
		}
		else
		{
			//dm( "MBeanProxyMgrImpl.checkRegisterTarget: no match: " + targetObjectName );
		}
	}
	
		private void
	connectionFailed()
	{
		dm( "CONNECTION FAILED" );
	}
	
		public synchronized void
	handleNotification( 
		final Notification			notifIn,
		final MBeanServerConnection	conn )
	{
		final String	type	= notifIn.getType();
		
		//dm( "\nMBeanProxyMgrImpl.handleNotification: " + type );
		
		if ( notifIn instanceof MBeanServerNotification )
		{
			final MBeanServerNotification	notif	= (MBeanServerNotification)notifIn;
			final ObjectName				objectName	= notif.getMBeanName();
			
			//dm( "MBeanProxyMgrImpl.handleNotification:\n" + SmartStringifier.toString( notifIn ) + "\n");
			
			if ( type.equals( MBeanServerNotification.REGISTRATION_NOTIFICATION ) )
			{
				try
				{
					checkRegisterTarget( objectName );
				}
				catch( JMException e )
				{
					dm( "Failure registering proxy: " + e );
					throw new RuntimeException( e );
				}
				catch( IOException e )
				{
					connectionFailed();
					throw new RuntimeException( e );
				}
			}
			else if ( type.equals( MBeanServerNotification.UNREGISTRATION_NOTIFICATION ) )
			{
				try
				{
					checkUnregisterTarget( objectName );
				}
				catch( JMException e )
				{
					dm( "Failure unregistering proxy: " + e );
					throw new RuntimeException( e );
				}
			}
		}
	}
	
		private static void
	dm(	Object msg	)
	{
		System.out.println( SmartStringifier.toString( msg ) );
	}
	

		protected ObjectName
	addProxy(
		final ObjectName	targetObjectName,
		final ObjectName	proxyObjectName,
		final int			cachedMBeanInfoRefreshMillis,
		final int			cachedAttributeRefreshMillis )
		throws IOException, MBeanRegistrationException,
		InstanceNotFoundException, ReflectionException,IntrospectionException,
		InstanceAlreadyExistsException
	{
		final MBeanServerConnection	conn	= getMBeanServerConnection();
		
		final MBeanProxyMBean	proxy	=
			new MBeanProxyMBean( conn, targetObjectName,
				cachedMBeanInfoRefreshMillis,
				cachedAttributeRefreshMillis );
		
		try
		{
			mSelfMBeanServer.registerMBean( proxy, proxyObjectName );
		}
		catch( NotCompliantMBeanException e )
		{
			assert( false );
			throw new RuntimeException( e );
		}
		
		final MBeanProxyInfo	info	=
			new MBeanProxyInfo( proxyObjectName, targetObjectName, conn );
		
		mProxyObjectNameToProxyInfo.put( proxyObjectName, info );
		mTargetObjectNameToProxyObjectName.put( targetObjectName, proxyObjectName );
		
		return( proxyObjectName );
	}
	
		protected ObjectName
	addProxy(
		final ObjectName	targetMBean,
		final boolean		newNames,
		final int			cachedMBeanInfoRefreshMillis,
		final int			cachedAttributeRefreshMillis )
		throws IOException, MBeanRegistrationException,
		InstanceNotFoundException, ReflectionException,IntrospectionException,
		InstanceAlreadyExistsException, MalformedObjectNameException
	{
		final ObjectName	proxyName	= createProxyName( targetMBean, newNames );
		final ObjectName	name		=
			addProxy( targetMBean, proxyName,
				cachedMBeanInfoRefreshMillis, cachedAttributeRefreshMillis);
	
		return( proxyName );
	}
	
	

		public MBeanProxyInfo
	getProxyInfo( final ObjectName proxyObjectName )
	{
		return( (MBeanProxyInfo)mProxyObjectNameToProxyInfo.get( proxyObjectName ) );
	}
	
		public MBeanServerConnection
	getProxyMBeanServerConnection( final ObjectName proxyObjectName )
	{
		final MBeanProxyInfo	info	= getProxyInfo( proxyObjectName );
		
		return( info.mConn );
	}
	
		public ObjectName
	getProxyTarget( final ObjectName proxyObjectName )
	{
		final MBeanProxyInfo	info	= getProxyInfo( proxyObjectName );
		
		return( info.mTarget );
	}
	
		
		ObjectName
	createProxyName(
		final ObjectName	srcName,
		final boolean		newNames )
	{
		ObjectName	newObjectName	= srcName;
		
		if ( newNames )
		{
			final int	temp	= (srcName.toString() + System.currentTimeMillis()).hashCode();
			
			newObjectName	= JMXUtil.newObjectName( srcName.toString() + ",jmxcmd.proxyid=" + temp );
		}

		return( newObjectName );
	}
	
		public Set<ObjectName>
	getProxyObjectNames()
	{
		return( Collections.unmodifiableSet( mProxyObjectNameToProxyInfo.keySet() ) );
	}
	
	/**
	 */
	private static final class ProxySetInfo
	{
		private final Set<ObjectName>		mNamesAndPatterns;
		private final boolean	mUseNewNames;
		private final int		mCachedMBeanInfoRefreshMillis;
		private final int		mCachedAttributeRefreshMillis;
		
			public
		ProxySetInfo(
			final Set<ObjectName>		namesAndPatterns,
			final boolean	useNewNames,
			final int		cachedMBeanInfoRefreshMillis,
			final int		cachedAttributeRefreshMillis )
		{
			mNamesAndPatterns				= Collections.unmodifiableSet( namesAndPatterns );
			mUseNewNames					= useNewNames;
			mCachedMBeanInfoRefreshMillis	= cachedMBeanInfoRefreshMillis;
			mCachedAttributeRefreshMillis	= cachedAttributeRefreshMillis;
		}
		
		public Set<ObjectName>		getNamesAndPatterns()	{ return( mNamesAndPatterns ); }
		public boolean	getUseNewNames()		{ return( mUseNewNames ); }
		public int	getMBeanInfoRefreshMillis()	{ return( mCachedMBeanInfoRefreshMillis ); }
		public int	getAttributeRefreshMillis()	{ return( mCachedAttributeRefreshMillis ); }
	}
	
		protected Set<ObjectName>
	resolve( final Set<ObjectName> namesAndPatterns )
		throws IOException
	{
		final MBeanServerConnection	conn	= getMBeanServerConnection();
		
		final Iterator<ObjectName>	iter	= namesAndPatterns.iterator();
		final Set<ObjectName>		resolvedObjectNames	= new HashSet<ObjectName>();
		
		while ( iter.hasNext() )
		{
			final ObjectName	nameOrPattern	= iter.next();
			
			final Set<ObjectName>	results	= conn.queryNames( nameOrPattern, null );
			
			resolvedObjectNames.addAll( results );
		}
		
		return( resolvedObjectNames );
	}
	
	/**
		Add, and maintain live, proxies to all MBeans matching the specified pattern.
		
	 */	
		public void
	refreshProxies(
		final ProxySetInfo	info )
		throws IOException
	{
		final Set<ObjectName>	targets	= resolve( info.getNamesAndPatterns() );
		final Set<ObjectName>	proxies	= new HashSet<ObjectName>();
		
		final Iterator<ObjectName> iter	= targets.iterator();
		while( iter.hasNext() )
		{
			final ObjectName	targetObjectName	= iter.next();
			final ObjectName	proxyObjectName		=
				createProxyName( targetObjectName, info.getUseNewNames() );
			
			final boolean	missing	= mProxyObjectNameToProxyInfo.get( proxyObjectName ) == null;
			
			if ( missing )
			{
				try
				{
					addProxy( targetObjectName,
						proxyObjectName,
						info.getMBeanInfoRefreshMillis(),
						info.getAttributeRefreshMillis() );
					proxies.add( proxyObjectName );
				}
				catch( JMException e )
				{
					// OK
				}
			}
			
		}
	}
	
	
	/**
		Add, and maintain live, proxies to all MBeans matching the specified pattern.
		
	 */	
		public void
	addProxies(
		final ObjectName[]	namesAndPatterns,
		final boolean		useNewNames,
		final int			cachedMBeanInfoRefreshMillis,
		final int			cachedAttributesRefreshMillis
		)
		throws IOException
	{
		final ProxySetInfo	proxySet	=
			new ProxySetInfo( SetUtil.newSet( namesAndPatterns ),
					useNewNames, cachedMBeanInfoRefreshMillis,
						cachedAttributesRefreshMillis ) ;
		
		mProxySetInfos.add( proxySet );
		
		refreshProxies( proxySet );
	}
	
	
	
		public ObjectName
	preRegister(MBeanServer server, ObjectName name)
	{
		mSelfMBeanServer		= server;
		
		return( name );
	}
	
		public void
	postRegister( Boolean registrationDone )
	{
	}
	
		public void
	preDeregister()
	{
	}
		public void
	postDeregister(  )
	{
		try
		{
			// we don't want the listener hanging around!
			mListener.cleanup();
		}
		catch( IOException e )
		{
		}
	}
	
		public void
	handleNotification(
		Notification	notification,
		Object			handback) 
	{
	}
}









