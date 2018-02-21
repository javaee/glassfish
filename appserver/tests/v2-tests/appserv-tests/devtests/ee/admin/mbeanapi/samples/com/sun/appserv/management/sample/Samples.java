/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.appserv.management.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.management.Attribute;
import javax.management.Notification;
import javax.management.AttributeChangeNotification;
import javax.management.NotificationListener;
import javax.management.ListenerNotFoundException;
import javax.management.ObjectName;
import javax.management.NotificationFilter;
import javax.management.MBeanServerConnection;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerNotification;

import com.sun.appserv.management.DomainRoot;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Sample;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.Extra;
import com.sun.appserv.management.base.StdAttributesAccess;
import com.sun.appserv.management.base.QueryMgr;

import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.TLSParams;
import com.sun.appserv.management.client.HandshakeCompletedListenerImpl;

import com.sun.appserv.management.j2ee.J2EEDomain;
import com.sun.appserv.management.j2ee.J2EEServer;

import com.sun.appserv.management.deploy.DeploymentMgr;
import com.sun.appserv.management.deploy.DeploymentStatus;
import com.sun.appserv.management.deploy.DeploymentProgress;
import com.sun.appserv.management.deploy.DeploymentSupport;

import com.sun.appserv.management.monitor.MonitoringDottedNames;
import com.sun.appserv.management.monitor.JMXMonitorMgr;
import com.sun.appserv.management.monitor.AMXStringMonitor;

import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.ConfigDottedNames;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.ModuleMonitoringLevelsConfig;
import com.sun.appserv.management.config.ModuleMonitoringLevelValues;
import com.sun.appserv.management.config.PropertiesAccess;


/**
	Main class demonstrating a variety of MBean API (AMX) usages.
 */
public final class Samples
{
	private final DomainRoot				mDomainRoot;

		public
	Samples( final DomainRoot	domainRoot )
	{
		mDomainRoot	= domainRoot;
	}
	


		public final DomainRoot
	getDomainRoot()
	{
		return( mDomainRoot );
	}
	
		public DomainConfig
	getDomainConfig()
	{
		return( getDomainRoot().getDomainConfig() );
	}
	
		public QueryMgr
	getQueryMgr()
	{
		return( getDomainRoot().getQueryMgr() );
	}
	
	/**
		Print a message.
	 */
		public void
	println( Object o )
	{
		System.out.println( toString( o ) );
	}
	
	/**
		Turn an object into a useful String
	 */
		public String
	toString( Object o )
	{
		return( SampleUtil.toString( o ) );
	}
	
	/**
		Display a Map to System.out.
	 */
		private void
	displayMap(
		final String	msg,
		final Map 		m)
	{
		println( msg + ": " + toString( m.keySet() ) );
	}

	/**
		Demonstrates how to access various types of  {@link AMX} by obtaining a Map and then displaying it.
	 */
		public void
	handleList()
	{
		final DomainConfig	dcp	= getDomainConfig();
		
		// Top-level items
		println( "\n--- Top-level --- \n" );
		
		displayMap( "ConfigConfig", dcp.getConfigConfigMap() );
		
		displayMap( "ServerConfig", dcp.getServerConfigMap() );
		
		displayMap( "StandaloneServerConfig", dcp.getStandaloneServerConfigMap() );
		
		displayMap( "ClusteredServerConfig", dcp.getClusteredServerConfigMap() );
		
		displayMap( "ClusterConfig", dcp.getClusterConfigMap() );
		
		
		// deployed items
		println( "\n--- DeployedItems --- \n" );
		displayMap( "J2EEApplicationConfig", dcp.getJ2EEApplicationConfigMap() );
		displayMap( "EJBModuleConfig", dcp.getEJBModuleConfigMap() );
		displayMap( "WebModuleConfig", dcp.getWebModuleConfigMap() );
		displayMap( "RARModuleConfig", dcp.getRARModuleConfigMap() );
		displayMap( "AppClientModuleConfig", dcp.getAppClientModuleConfigMap() );
		displayMap( "LifecycleModuleConfig", dcp.getLifecycleModuleConfigMap() );
		
		
		// resources
		println( "\n--- Resources --- \n" );
		
		displayMap( "CustomResourceConfig", dcp.getCustomResourceConfigMap() );
		displayMap( "PersistenceManagerFactoryResourceConfig",
			dcp.getPersistenceManagerFactoryResourceConfigMap() );
		displayMap( "JNDIResourceConfig", dcp.getJNDIResourceConfigMap() );
		displayMap( "JMSResourceConfig", dcp.getJMSResourceConfigMap() );
		displayMap( "JDBCResourceConfig", dcp.getJDBCResourceConfigMap() );
		displayMap( "ConnectorResourceConfig", dcp.getConnectorResourceConfigMap() );
		displayMap( "JDBCConnectionPoolConfig", dcp.getJDBCConnectionPoolConfigMap() );
		displayMap( "PersistenceManagerFactoryResourceConfig",
			dcp.getPersistenceManagerFactoryResourceConfigMap() );
		displayMap( "ConnectorConnectionPoolConfig",
			dcp.getConnectorConnectionPoolConfigMap() );
		displayMap( "AdminObjectResourceConfig", dcp.getAdminObjectResourceConfigMap() );
		displayMap( "ResourceAdapterConfig", dcp.getResourceAdapterConfigMap() );
		displayMap( "MailResourceConfig", dcp.getMailResourceConfigMap() );
		
		
		// get a ConfigConfig
		final ConfigConfig	config	=
			(ConfigConfig)dcp.getConfigConfigMap().get( "server-config" );
			

		// HTTPService
		println( "\n--- HTTPService --- \n" );
		
		final HTTPServiceConfig httpService = config.getHTTPServiceConfig();
		displayMap( "HTTPListeners", httpService.getHTTPListenerConfigMap() );
		displayMap( "VirtualServers", httpService.getVirtualServerConfigMap() );
	}
	
	/**
		Return a Set of {@link AMX} whose ObjectName has the property
		 <i>property-name</i>=<i>property-value</i>.
		
		@param propertyName
		@param propertyValue
		@return Set of {@link AMX}
	*/
		public Set
	queryWild(
		final String propertyName,
		final String propertyValue)
	{
		final String[]	propNames	= new String[] { propertyName };
		final String[]	propValues	= new String[]{ propertyValue };
		
		final Set	amxs	= getQueryMgr().queryWildSet( propNames, propValues );
		
		return( amxs );
	}
	
	/**
		Call queryWild( propertyName, propertyValue ) and display the result.
		
		@param propertyName
		@param propertyValue
	*/
		public void
	displayWild(
		final String propertyName,
		final String propertyValue)
	{
		final Set	items	= queryWild( propertyName, propertyValue );
		
		println( "\n--- Queried for " + propertyName + "=" + propertyValue + " ---" );
		final Iterator	iter	= items.iterator();
		while ( iter.hasNext() )
		{
			final AMX	item	= (AMX)iter.next();
			
			println( "j2eeType=" + item.getJ2EEType() + "," + "name=" + item.getName() );
		}
	}
	
		public Set
	queryForJ2EEType( final String j2eeType )
	{
		final String	prop	= Util.makeJ2EETypeProp( j2eeType );
		final Set		items	= getQueryMgr().queryPropsSet( prop );
		
		return( items );
	}
	
		public void
	displayAvailableChildTypes( final String j2eeType )
	{
		final DomainRoot		domainRoot	= getDomainRoot();
		
	}
	
		private String
	getIndent( final int num )
	{
		final char[]	indent	= new char[ num ];
		for( int i = 0; i < num; ++i )
		{
			indent[ i ]	= ' ';
		}
		return( new String( indent ) );
	}
	
	/**
		Display the j2eeType and name (if not {@link AMX#NO_NAME})
	 */
		private void
	displayAMX(
		final AMX amx,
		final int	indentCount )
	{
		final String indent	= getIndent( indentCount );
		
		final String	j2eeType	= amx.getJ2EEType();
		final String	name		= amx.getName();
		if ( name.equals( AMX.NO_NAME ) )
		{
			println( indent + j2eeType );
		}
		else
		{
			println( indent + j2eeType + "=" + name );
		}
	}
	
		private void
	displayHierarchy(
		final Collection	amxSet,
		final int			indentCount )
	{
		final Iterator	iter	= amxSet.iterator();
		while ( iter.hasNext() )
		{
			final AMX	amx	= (AMX)iter.next();
			displayHierarchy( amx, indentCount );
		}
	}
	
	/**
		Display the hierarchy of {@link AMX} beginning with the specified one
	 */
		public void
	displayHierarchy(
		final AMX amx,
		final int	indentCount )
	{
		displayAMX( amx, indentCount );
		
		if ( amx instanceof Container )
		{
			// get Maps of all contained items
			final Map	m	= ((Container)amx).getMultiContaineeMap( null );

			// for clarity of display, separate out those that are Containers,
			// and those that are not.
			final Set	deferred	= new HashSet();
			final Iterator	mapsIter	= m.values().iterator();
			while ( mapsIter.hasNext() )
			{
				final Map	instancesMap	= (Map)mapsIter.next();
				final AMX	first	= (AMX)instancesMap.values().iterator().next();
				if ( first instanceof Container )
				{
					deferred.add( instancesMap );
				}
				else
				{
					displayHierarchy( instancesMap.values(), indentCount + 2);
				}
			}
			
			// display deferred items
			final Iterator	iter	= deferred.iterator();
			while ( iter.hasNext() )
			{
				final Map	instancesMap	= (Map)iter.next();
				displayHierarchy( instancesMap.values(), indentCount + 2);
			}
		}
		
	}
	
	/**
		Display the entire MBean hierarchy.
	 */
		public void
	displayHierarchy()
	{
		displayHierarchy( getDomainRoot(), 0);
	}
	
	/**
		Display the MBean hierarchy beginning with j2eeType.
	 */
		public void
	displayHierarchy( final String j2eeType )
	{
		final Set	items	= getQueryMgr().queryJ2EETypeSet( j2eeType );
		
		if ( items.size() == 0 )
		{
			println( "No {@link AMX} of j2eeType " + SampleUtil.quote( j2eeType ) + " found" );
		}
		else
		{
			displayHierarchy( items, 0);
		}
	}
	
	
	/**
		Display all MBeans that have j2eeType=<j2eeType>
	 */
		public void
	displayJ2EEType( final String j2eeType )
	{
		final Set		items	= queryForJ2EEType( j2eeType );
		
		println( "\n--- Queried for j2eeType=" + j2eeType + " ---" );
		
		final Iterator	iter	= items.iterator();
		while ( iter.hasNext() )
		{
			final AMX	item	= (AMX)iter.next();
			
			// they may or may not have unique names, so show ObjectNames
			println( Util.getObjectName( item ) );
		}
		println( "" );
	}
	
	
	/**
		Display all Attributes in the {@link AMX}.
	 */
		public void
	displayAllAttributes( final AMX item )
	{
		println( "\n--- Attributes for " + item.getJ2EEType() + "=" + item.getName() + " ---" );
		
		final Extra	extra	= Util.getExtra( item );
		
		final Map	attrs	= extra.getAllAttributes();
		
		final Iterator	iter	= attrs.keySet().iterator();
		while ( iter.hasNext() )
		{
			final String	name	= (String)iter.next();
			final Object	value	= attrs.get( name );
			
			println( name + "=" + toString( value ) );
		}
	}
	
	/**
		Display all Attributes in the {@link AMX}.
	 */
		public void
	displayAllAttributes( final String j2eeType )
	{
		final Set		items	= queryForJ2EEType( j2eeType );
		
		if ( items.size() == 0 )
		{
			println( "No {@link AMX} of j2eeType " + SampleUtil.quote( j2eeType ) + " found" );
		}
		else
		{
			final Iterator	iter	= items.iterator();
			while ( iter.hasNext() )
			{
				final AMX	amx	= (AMX)iter.next();
				
				displayAllAttributes( amx );
				println( "" );
			}
		}
	}
	
	
	/**
		Display all dotted names.
	 */
		public void
	displayDottedNames()
	{
		final ConfigDottedNames	configDottedNames	= getDomainRoot().getConfigDottedNames();
		Attribute[] result	= (Attribute[])configDottedNames.dottedNameGet( "*" );
		println( "--- ConfigDottedNames ---" );
		println( SampleUtil.arrayToString( result, "", "\n" ) );
		
		println( "\n--- MonitoringDottedNames ---" );
		
		final MonitoringDottedNames	monDottedNames	= getDomainRoot().getMonitoringDottedNames();
		result	= (Attribute[])monDottedNames.dottedNameGet( "*" );
		println( SampleUtil.arrayToString( result, "", "\n" ) );
	}
	
	
	/**
		Demonstrate how to use the {@link com.sun.appserv.management.base.QueryMgr} facilities.
	 */
		public void
	demoQuery()
	{
		displayWild( AMX.J2EE_TYPE_KEY, "X-*ResourceConfig" );
		displayWild( AMX.J2EE_TYPE_KEY, "X-*ServerConfig" );
		
		displayJ2EEType( XTypes.SSL_CONFIG );
		displayJ2EEType( XTypes.CLUSTER_CONFIG );
	}

	
		private Object
	uploadArchive( final File archive  )
		throws IOException
	{
		final FileInputStream	input	= new FileInputStream( archive );
		final long	length	= input.available();
		final DeploymentMgr	mgr	= getDomainRoot().getDeploymentMgr();
		final Object	uploadID	= mgr.initiateFileUpload( length );
			
		try
		{
			final int	chunkSize	= 256 * 1024;
			long remaining	= length;
			while ( remaining != 0 )
			{
				final int	actual	= remaining < chunkSize ? (int)remaining : chunkSize;
				
				final byte[]	bytes	= new byte[ actual ];
				final int	num	= input.read( bytes );
				if ( num != actual )
				{
					throw new IOException();
				}

				mgr.uploadBytes( uploadID, bytes );
				remaining	-= actual;
			}
		}
		finally
		{
			input.close();
		}
		
		return( uploadID );
	}
	
	
		private final String
	getAppName( final String archiveName )
	{
		String	result	= archiveName;
		
		final int	idx	= archiveName.lastIndexOf( "." );
		if ( idx > 1 )
		{
			result	= archiveName.substring( 0, idx );
		}
		
		return( result );
	}
	
	/**
		Deploy an archive.
		<p>
		To deploy, you will need an archive to deploy.  A recommended sample may be found at:
		<i>INSTALL_ROOT</i>/samples/ejb/stateless/apps/simple.ear
		<p>
		This sample deploys the archive to the domain, but does not create any references
		to it, so it will not actually be associated with any server.
		<p>
		To associate an application with a server, use
		{@link StandaloneServerConfig#createDeployedItemRefConfig}
		@see com.sun.appserv.management.config.StandaloneServerConfig
		@see com.sun.appserv.management.config.DeployedItemRefConfigCR
	 */
		public void
	deploy( final File archive )
		throws IOException
	{
		final Object	uploadID	= uploadArchive( archive );
		final DeploymentMgr	mgr	= getDomainRoot().getDeploymentMgr();
		
		final Object	deployID	= mgr.initDeploy( );
		final DeployNotificationListener myListener	= new DeployNotificationListener( deployID);
		mgr.addNotificationListener( myListener, null, null);
		
		try
		{
			final Map	options	= new HashMap();
			
			final String	archiveName	= archive.getName();
			final String	deployName	= getAppName( archiveName );
			SampleUtil.println( "Deploying " + archiveName + " as " + deployName );
			
			options.put( DeploymentMgr.DEPLOY_OPTION_NAME_KEY, deployName );
			options.put( DeploymentMgr.DEPLOY_OPTION_VERIFY_KEY, Boolean.TRUE.toString() );
			options.put( DeploymentMgr.DEPLOY_OPTION_DESCRIPTION_KEY, "description" );
			
			
			mgr.startDeploy( deployID, uploadID, null, options);
			
			while ( ! myListener.isCompleted() )
			{
				try
				{
					println( "deploy: waiting for deploy of " + archive);
					Thread.sleep( 1000 );
				}
				catch( InterruptedException e )
				{
				}
			}
			
			final DeploymentStatus	status	= myListener.getDeploymentStatus();
			final Map	additionalStatus	= status.getAdditionalStatus();
			final String	moduleID	=
				(String)additionalStatus.get( DeploymentStatus.MODULE_ID_KEY );
			
			SampleUtil.println( "Deployed " + quote(archiveName) + " as " + quote(deployName) +
				 ": status=" + status.getStageStatus() + ", moduleID = " + quote(moduleID) +
				 ", AdditionalStatus=" + SampleUtil.mapToString( additionalStatus, " ") );
			
			if ( ! deployName.equals( moduleID ) )
			{
				SampleUtil.println( "WARNING: requested name of " + quote(deployName) +
					" has not been used, actual name = " + quote(moduleID) +
					", see bug #6218714" );
			}
			
			if ( status.getStageThrowable() != null )
			{
				status.getStageThrowable().printStackTrace();
			}
		}
		finally
		{
			try
			{
				mgr.removeNotificationListener( myListener );
			}
			catch( Exception e )
			{
			}
		}
	}
	
		private String
	quote( final String s )
	{
		return SampleUtil.quote( s );
	}
	
	/**
		Undeploys a deployed module.
	 */
		public void
	undeploy( final String moduleName )
		throws IOException
	{
		final DeploymentMgr	mgr	= getDomainRoot().getDeploymentMgr();
		
		final Map	statusData	= mgr.undeploy( moduleName, null );
		final DeploymentStatus	status	= 
			DeploymentSupport.mapToDeploymentStatus( statusData );
			
		println( "Undeployment result: " + status.getStageStatus() );
		if ( status.getStageThrowable() != null )
		{
			status.getStageThrowable().printStackTrace();
		}
	}
	
	/**
		Get a J2EEServer by name.
	 */
		public J2EEServer
	getJ2EEServer( final String serverName )
	{
		final J2EEDomain	j2eeDomain	= getDomainRoot().getJ2EEDomain();
		final Map			servers	= j2eeDomain.getServerMap();
		final J2EEServer	server	= (J2EEServer)servers.get( serverName );
		
		if ( server == null )
		{
			throw new IllegalArgumentException( serverName );
		}
		
		return( server );
	}
	
	/**
		Create a standalone server.
		
		@param configName
	 */
		public ConfigConfig
	createConfig( final String configName )
	{
		final ConfigConfig	config	= getDomainConfig().createConfigConfig( configName, null );
		return( config );
	}
	
	/**
		Create a standalone server.
		
		@param serverName
	 */
		public StandaloneServerConfig
	createServer(
		final String	serverName,
		final String	configName )
	{
		final String	nodeAgentName	= null;
		
		final StandaloneServerConfig	server	= (StandaloneServerConfig)
			getDomainConfig().createStandaloneServerConfig( serverName, nodeAgentName, configName, null );
		
		return( server );
	}
	
		public StandaloneServerConfig
	createServer( final String	serverName )
	{
		final ConfigConfig	config	= createConfig( serverName + "-config" );
		
		final StandaloneServerConfig	server	= createServer( serverName, config.getName() );
		return( server );
	}
	
	
	/**
		Start a server.
	 */
		public void
	startServer( final String serverName )
	{
		final J2EEServer	server	= getJ2EEServer( serverName );
		
		server.start();
	}
	
	/**
		Stop a server.
	 */
		public void
	stopServer( final String serverName )
	{
		final J2EEServer	server	= getJ2EEServer( serverName );
		
		server.stop();
	}
	
	
	private static final Set	LEGAL_MON	=
		Collections.unmodifiableSet( SampleUtil.newSet( new String[]
	{
		ModuleMonitoringLevelValues.HIGH,
		ModuleMonitoringLevelValues.LOW,
		ModuleMonitoringLevelValues.OFF,
	} ));
		
	/**
		Sets the monitoring state for all available modules.
		
		@param configName	configuration element on which to operate
		@param state		one of HIGH, LOW, OFF
	 */
		public void
	setMonitoring(
		final String	configName,
		final String	 state )
	{
		if ( ! LEGAL_MON.contains( state ) )
		{
			throw new IllegalArgumentException( state );
		}
		
		final ConfigConfig	config	=
			(ConfigConfig)getDomainConfig().getConfigConfigMap().get( configName );
		
		final ModuleMonitoringLevelsConfig	mon	=
			config.getMonitoringServiceConfig().getModuleMonitoringLevelsConfig();
		
		// set all modules to the same state
		mon.setConnectorConnectionPool( state );
		mon.setThreadPool( state );
		mon.setHTTPService( state );
		mon.setJDBCConnectionPool( state );
		mon.setORB( state );
		mon.setTransactionService( state );
		mon.setWebContainer( state );
		mon.setEJBContainer( state );
	}
	
	/**
		Get a Map of <i>property-name</i>=<i>property-value</i>.
		
		@param pa	a PropertiesAccess
	 */
		public Map
	getProperties( final PropertiesAccess pa )
	{
		final HashMap	m	= new HashMap();
		
		final String[]	names	= pa.getPropertyNames();
		for( int i = 0; i < names.length; ++i )
		{
			m.put( names[ i ], pa.getPropertyValue( names[ i ] ) );
		}
		
		return( m );
	}
	
	/**
		Display all properties found on all {@link AMX}.
		
		@see #getProperties(PropertiesAccess)
		@see PropertiesAccess#getPropertyNames
	 */
		public void
	displayAllProperties( )
	{
		final Iterator	iter	= getQueryMgr().queryAllSet().iterator();
		while ( iter.hasNext() )
		{
			final AMX	amx	= (AMX)iter.next();
			
			if ( amx instanceof PropertiesAccess )
			{
				final PropertiesAccess	pa	= (PropertiesAccess)amx;
				
				final Map	props	= getProperties( pa );
				if ( props.keySet().size() != 0 )
				{
					println( "\nProperties for: " + Util.getObjectName( (AMX)pa ) );
					println( SampleUtil.mapToString( getProperties( pa ), "\n") );
				}
			}
		}
	}
	
		private void
	mySleep( final long millis )
	{
		try
		{
			Thread.sleep( millis );
		}
		catch( InterruptedException e )
		{
		}
	}
	
	
	public final static String	MBEAN_SERVER_DELEGATE	=
							"JMImplementation:type=MBeanServerDelegate";
		public static ObjectName
	getMBeanServerDelegateObjectName()
	{
		return( Util.newObjectName( MBEAN_SERVER_DELEGATE ) );
	}
	
	
		private void
	waitNumNotifs( final Map notifs, final String type, final int numRequired )
	{
		while ( true )
		{
			final List	list	= (List)notifs.get( type );
			if ( list != null && list.size() >= numRequired )
			{
				break;
			}

			mySleep( 50 );
		}
	}
	
		
	
		private void
	waitMBeanServerNotification(
		final SampleListener	listener,
		final String			type,
		final ObjectName		objectName )
	{
		List		list	= null;
		while ( (list = listener.getNotifsReceived( type )) == null )
		{
			mySleep( 50 );
		}
		
		boolean	waiting	= true;
		while ( waiting )
		{
			final Iterator	iter	= list.iterator();
			while ( iter.hasNext() )
			{
				final MBeanServerNotification	notif	= (MBeanServerNotification)iter.next();
				if ( notif.getMBeanName().equals( objectName ) )
				{
					waiting	= false;
					break;
				}
				else
				{
					SampleUtil.println( "Unexpected ObjectName: " + objectName + " != " + notif.getMBeanName() );
				}
			}
			mySleep( 100 );
		}
	}
	
	
	/**
		Demonstrates the use of a javax.management.monitor MBean
		to be notified of changes in the value of an Attribute.
	 */
		public void
	demoJMXMonitor()
		throws InstanceNotFoundException, IOException
	{
		final JMXMonitorMgr	mgr	= getDomainRoot().getJMXMonitorMgr();
		
		final String	attrName	= "SampleString";
		final String	attrValue	= "hello";
		
		// listen to the MBeanServerDelegate, too, so we can see our sample monitor
		// get registered.
		final SampleListener	sampleListener	= new SampleListener();
		final MBeanServerConnection	conn	=
			Util.getExtra( mgr ).getConnectionSource().getExistingMBeanServerConnection();
		conn.addNotificationListener(
			getMBeanServerDelegateObjectName(), sampleListener, null, null );
		
			
		final Sample	sample	= (Sample)getDomainRoot().getContainee( XTypes.SAMPLE );
		
		final String	monitorName	= "SampleStringMonitor";
		AMXStringMonitor	mon	= null;
		try
		{
			// cleanup in case it was left around by mistake...
			try { mgr.remove( monitorName ); } catch( Exception e )	{}
			
			// create a new one
			mon	= mgr.createStringMonitor( monitorName );
			// observer that we've been notified (not required)
			waitMBeanServerNotification( sampleListener,
				MBeanServerNotification.REGISTRATION_NOTIFICATION, Util.getObjectName( mon ) );
		
			// we'll modify this Attribute's value, to force a change
			sample.addAttribute( attrName, attrValue );
			
			// listen to the monitor
			mon.addNotificationListener( sampleListener, null, null );
			mon.setObservedAttribute( attrName );
			mon.setStringToCompare( attrValue );
			mon.setNotifyDiffer( true );
			mon.setNotifyMatch( true );
			
			// tell the monitor to observe sample
			mon.addObservedObject( Util.getObjectName( sample ) );
			
			// since the Attribute was added dynamically, there is no
			// getter method, so we must access the Attribute via JMX
			final StdAttributesAccess	attrs	= Util.getExtra( sample );
			attrs.setAttribute( new Attribute( attrName, "goodbye" ) );
			// set it to original value
			attrs.setAttribute( new Attribute( attrName, attrValue ) );
			
			// we added it,so we should remove it
			sample.removeAttribute( attrName );
			
			// let the Notifications arrive...
			final Map	notifs	= sampleListener.getNotifsReceived();
			waitNumNotifs( notifs, AttributeChangeNotification.ATTRIBUTE_CHANGE, 4 );
		}
		catch( Throwable t )
		{
			t.printStackTrace();
		}
		finally
		{
			try
			{
				mon.removeNotificationListener( sampleListener );
				
				// don't leave monitors around
				if ( mon != null )
				{
					mgr.remove( mon.getName() );
					// observer that we've been notified (not required)
					waitMBeanServerNotification( sampleListener,
						MBeanServerNotification.UNREGISTRATION_NOTIFICATION,
						Util.getObjectName( mon ) );
				}
				
				conn.removeNotificationListener(
					getMBeanServerDelegateObjectName(), sampleListener );
			}
			catch( ListenerNotFoundException e )
			{
			}
		}
	}
}















