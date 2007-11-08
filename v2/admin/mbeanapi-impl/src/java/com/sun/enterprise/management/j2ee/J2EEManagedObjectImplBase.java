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
package com.sun.enterprise.management.j2ee;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;

import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.j2ee.statistics.Stats;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;

import com.sun.enterprise.management.support.AMXNonConfigImplBase;


import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.enterprise.management.support.AMXAttributeNameMapper;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.jmx.AttributeNameManglerImpl;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.Extra;

import com.sun.appserv.management.j2ee.J2EETypes;
import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.J2EEManagedObject;
import com.sun.appserv.management.j2ee.ConfigPeer;

import com.sun.appserv.management.monitor.MonitoringStats;

import com.sun.appserv.management.config.ServerConfig;

import com.sun.enterprise.management.support.Delegate;

import com.sun.enterprise.management.support.TypeInfos;


/**
 */
public abstract class J2EEManagedObjectImplBase extends AMXNonConfigImplBase
{
	protected long	mStartTime;
	
		public
	J2EEManagedObjectImplBase()
	{
		this( null );
	}
        
		public
	J2EEManagedObjectImplBase( final Delegate	delegate )
	{
		super( delegate );
		mStartTime	= 0;
	}

		protected
	J2EEManagedObjectImplBase( String j2eeType, Delegate delegate )
	{
		super( j2eeType, delegate );
		mStartTime	= 0;
	}

    /**
        We may (or may not) want to expose some of these at some
        point, though not necessarily with the same names.
     */
	static private final Set<String>   IGNORE_MISSING =
	    GSetUtil.newUnmodifiableStringSet(
	        "EJBModule", "J2EEApplication", "J2EEServer", "j2eeType",
	        "ModuleName", "eventTypes", "hasWebServices", "endpointAddresses"
	    );
	    
	    protected void
	handleMissingOriginals( final Set<String> missingOriginals )
	{
	    missingOriginals.removeAll( IGNORE_MISSING );
	    
        super.handleMissingOriginals( missingOriginals );
	}

	
		public void
	preRegisterDone()
        throws Exception
	{
	    super.preRegisterDone();
		mStartTime	= System.currentTimeMillis();
	}
	
	     protected final void
	setstartTime(final long startTime )
	{
	     mStartTime = startTime;
	}


		public long
	getstartTime()
	{
		return( mStartTime );
	}


		protected String
	getServerName()
	{
		return( getObjectName().getKeyProperty( "J2EEServer" ) );
	}

		protected String
	getServerXType()
	{
		String	type	= null;
		
		final String	serverName	= getServerName();
		if ( serverName != null )
		{
			final Map<String,ServerConfig>	m	=
			    getDomainRoot().getDomainConfig().getServerConfigMap();
			
			final ServerConfig	serverConfig	= m.get( serverName );
			type	= serverConfig.getJ2EEType();
		}
		
		return( type );
	}
	
	/**
		Map from JSR77 j2eeType to our config j2eeType.
	 */
	private static final Map<String,String>	ToConfigMap	= MapUtil.newMap( new String[]
	{
		J2EETypes.J2EE_DOMAIN, XTypes.DOMAIN_CONFIG,
		J2EETypes.J2EE_CLUSTER, XTypes.CLUSTER_CONFIG,
		J2EETypes.J2EE_SERVER, XTypes.STANDALONE_SERVER_CONFIG,
		J2EETypes.JVM, XTypes.JAVA_CONFIG,
		
		J2EETypes.J2EE_APPLICATION, XTypes.J2EE_APPLICATION_CONFIG,
		J2EETypes.EJB_MODULE, XTypes.EJB_MODULE_CONFIG,
		J2EETypes.WEB_MODULE, XTypes.WEB_MODULE_CONFIG,
		J2EETypes.APP_CLIENT_MODULE, XTypes.APP_CLIENT_MODULE_CONFIG,
		
		J2EETypes.JAVA_MAIL_RESOURCE, XTypes.MAIL_RESOURCE_CONFIG,
		J2EETypes.JDBC_RESOURCE, XTypes.JDBC_RESOURCE_CONFIG,
		J2EETypes.JNDI_RESOURCE, XTypes.JNDI_RESOURCE_CONFIG,
        J2EETypes.WEB_SERVICE_ENDPOINT, XTypes.WEB_SERVICE_ENDPOINT_CONFIG,
	}
	);
	
		protected String
	getConfigPeerJ2EEType()
	{
		final String	configPeerJ2EEType	= (String)ToConfigMap.get( getSelfJ2EEType() );

		return( configPeerJ2EEType );
	}
	
	/**
		Maps a j2eeType to its peer monitoring j2eeType
	 */
	private static final Map<String,String>	MON_MAP	=
	    Collections.unmodifiableMap( MapUtil.newMap( new String[]
	{
		J2EETypes.J2EE_SERVER, XTypes.SERVER_ROOT_MONITOR,
		J2EETypes.J2EE_APPLICATION, XTypes.APPLICATION_MONITOR,
		
		J2EETypes.WEB_MODULE, XTypes.WEB_MODULE_VIRTUAL_SERVER_MONITOR,
		J2EETypes.SERVLET, XTypes.SERVLET_MONITOR,
		
		J2EETypes.EJB_MODULE, XTypes.EJB_MODULE_MONITOR,
		J2EETypes.STATELESS_SESSION_BEAN, XTypes.STATELESS_SESSION_BEAN_MONITOR,
		J2EETypes.STATEFUL_SESSION_BEAN, XTypes.STATEFUL_SESSION_BEAN_MONITOR,
		J2EETypes.ENTITY_BEAN, XTypes.ENTITY_BEAN_MONITOR,
		J2EETypes.MESSAGE_DRIVEN_BEAN, XTypes.MESSAGE_DRIVEN_BEAN_MONITOR,
	}));
	
		protected String
	getMonitoringPeerJ2EEType()
	{
		final String	monPeerJ2EEType	= (String)MON_MAP.get( getSelfJ2EEType() );

		return( monPeerJ2EEType );
	}
	
		protected Map<String,String>
	getMonitoringPeerProps(
		final String	monitorJ2EEType,
		final String	monitorName )
	{
		final Map<String,String>	props	= new HashMap<String,String>();
		
		props.put( AMX.J2EE_TYPE_KEY, monitorJ2EEType );
		props.put( AMX.NAME_KEY, monitorName );
		
		for( final String j2eeType : MON_MAP.keySet() )
		{
			final String	j2eeMonType	= (String)MON_MAP.get( j2eeType );
			
			final String	name	= getKeyProperty( j2eeType );
			if ( name != null )
			{
				props.put( j2eeMonType, name );
			}
		}
		
		return( props );
	}
	
		protected Map<String,String>
	getMonitoringPeerProps( )
	{
		Map<String,String>	props	= null;
		
		final String	j2eeType	= getMonitoringPeerJ2EEType();
		if ( j2eeType != null )
		{
			props	= getMonitoringPeerProps( j2eeType, getMonitoringPeerName() );
		}
		return( props );
	}
	
		protected ObjectName
	queryProps( final Map<String,String> propsMap )
	{
		ObjectName	objectName	= null;
		
		final String	props	= MapUtil.toString( propsMap, "," );
			
		final Set<ObjectName> candidates	=
		    getQueryMgr().queryPropsObjectNameSet( props );
		if ( candidates.size() == 1 )
		{
			objectName	= GSetUtil.getSingleton( candidates );
		}
		else if ( candidates.size() > 1 )
		{
			throw new RuntimeException(
				"Unexpectedly found too many candidates for query pattern " + quote( props ) +
				" found: " +  toString( candidates ) );
		}
		
		return( objectName );
	}
	
		public ObjectName
	getMonitoringPeerObjectName()
	{
		ObjectName	objectName	= null;
		
		final Map<String,String>	propsMap	= getMonitoringPeerProps();
		if ( propsMap != null && propsMap.keySet().size() != 0 )
		{
			debug( "getMonitoringPeerObjectName: my ObjectName = ", getObjectName(),
			    ", queryProps = ", MapUtil.toString( propsMap, ", " ) );
			objectName	= queryProps( propsMap );

			if ( objectName == null )
			{
				objectName	= queryMonitoringPeerFailed( propsMap );
			}
		}
		
		return( objectName );
	}
	
	/**
		The usual method of finding the monitoring peer has failed.
		This is an opportunity to try again.
	 */
		protected ObjectName
	queryMonitoringPeerFailed( final Map<String,String> propsMap )
	{
		return null;
	}
	
	
	/**
		The usual method of finding the config peer has failed.
		This is an opportunity to try again.
	 */
		protected ObjectName
	queryConfigPeerFailed( final Map<String,String> propsMap )
	{
	    debug( "queryConfigPeerFailed: " + MapUtil.toString( propsMap ) );
		return null;
	}
	
	
	/**
		JSR 77 impl
		
		@return String representation of the ObjectName
	*/
		public final String
	getobjectName()
	{
		return( getObjectName().toString() );
	}
	
	
		public boolean
	isstatisticProvider()
	{
		return( getMonitoringPeerObjectName() != null );
	}
	
		public boolean
	isstatisticsProvider()
	{
		return( isstatisticProvider() );
	}
	
		protected Stats
	getStatsGeneric()
	{
		Stats	stats	= null;
		
		if ( isstatisticProvider() )
		{
			final ObjectName	mon	= getMonitoringPeerObjectName();
			if ( mon != null )
			{
				final AMX	monStats = getProxyFactory().getProxy( mon, AMX.class);
				try
				{
					final Extra	extra	= Util.getExtra( monStats );
					stats	= (Stats)extra.getAttribute( "stats" );
					
				}
				catch( Exception e )
				{
					// OK, ignore
				}
			}
		}
		
		return( stats );
	}
		
		public final String
	getGroup()
	{
		return( AMX.GROUP_JSR77 );
	}
	
	
		protected String
	getConfigPeerName()
	{
		return( getSelfName() );
	}
	
		protected String
	getMonitoringPeerName()
	{
		return( getSelfName() );
	}
	
	/**
		If there is a config peer, return properties that uniquely identify it.
		This base implementation won't be sufficient for many MBeans, so subclasses
		should call super.getConfigPeerProps(), then tack on any additional ones.
	 */
		protected Map<String,String>
	getConfigPeerProps()
	{
		HashMap<String,String>	props	= null;
		final String	j2eeType = getConfigPeerJ2EEType();
		if ( j2eeType != null )
		{
			props	= new HashMap<String,String>();
			
			props.put( AMX.J2EE_TYPE_KEY, j2eeType );
			props.put( AMX.NAME_KEY, getConfigPeerName() );
		}
		return( props );
	}
	
	
	/**
		Get the name of a config peer.  Default behavior is to use the config peer
		j2eeType, together with the name of this MBean and GROUP_CONFIGURATION to
		locate a single MBean. A subclass not following this convention will have
		to override this method.
	 */
		public ObjectName
	getConfigPeerObjectName()
	{
		ObjectName	configPeerObjectName	= null;
		
		if (isConfigProvider() )
		{
			final Map<String,String>	propsMap	= getConfigPeerProps( );
			if ( propsMap != null )
			{
				configPeerObjectName	= queryProps( propsMap );
				if ( configPeerObjectName == null )
				{
					configPeerObjectName	= queryConfigPeerFailed( propsMap );
				}
			}
		
		}
		
		return( configPeerObjectName );
	}
	
	
		public boolean
	isConfigProvider()
	{
		return( ToConfigMap.keySet().contains( getSelfJ2EEType() ) );
	}
	
		public boolean
	iseventProvider()
	{
		return( false );
	}
	
		public boolean
	isstateManageable()
	{
		return( false );
	}
	
		protected final String[]
	getContaineeObjectNameStrings( final String j2eeType )
	{
		final Set<ObjectName>	objectNames	= getContaineeObjectNameSet( j2eeType );
		
		final String[]	names	= GSetUtil.toStringArray( objectNames );
		
		return( names );
	}
	
		public J2EEServer
	getJ2EEServer()
	{
	    return getProxyFactory().getProxy( getServerObjectName(), J2EEServer.class);
	}
	
		public ObjectName
	getServerObjectName()
	{
		final ObjectName	selfObjectName	= getObjectName();
		
		ObjectName	serverObjectName	= null;
		
		final String	serverName	= selfObjectName.getKeyProperty( J2EETypes.J2EE_SERVER );
		if ( serverName != null )
		{
			final String	props	= Util.makeRequiredProps( J2EETypes.J2EE_SERVER, serverName );
			final Set<AMX>	candidates	= getQueryMgr().queryPatternSet( selfObjectName.getDomain(), props );
			
			serverObjectName	= Util.getObjectName( GSetUtil.getSingleton( candidates ) );
		}
		return( serverObjectName );
	}
	
	
	private static final Set<String> DEPLOYED_TYPES	= GSetUtil.newUnmodifiableStringSet(
			J2EETypes.J2EE_APPLICATION,
			J2EETypes.WEB_MODULE,
			J2EETypes.EJB_MODULE,
			J2EETypes.APP_CLIENT_MODULE,
			J2EETypes.RESOURCE_ADAPTER_MODULE
		);
	
	
		public Set<ObjectName>
	getDeployedObjectsObjectNameSet()
	{
		return( getContaineeObjectNameSet( DEPLOYED_TYPES ) );
	}

		public String[]
	getdeployedObjects()
	{
		final String[]	names	= GSetUtil.toStringArray( getDeployedObjectsObjectNameSet() );
		return( names );
	}
	
	/**
		Return a list of Attribute names which should not be mapped.
	 */
		protected Set<String>
	getDontMapAttributeNames()
	{
		// all mapped by default
		return( Collections.emptySet() );
	}
	
	/*
		protected void
	addCustomMappings( final AMXAttributeNameMapper mapper )
	{
	    super.addCustomMappings( mapper );
	    
		for( final String name : getDontMapAttributeNames() )
		{
			mapper.dontMap( name );
		}
	}
	*/
	
}
