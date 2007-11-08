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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.enterprise.management.support;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Collections;

import java.io.IOException;

import javax.management.ObjectName;
import javax.management.MBeanRegistrationException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.AMXDebug;
import com.sun.appserv.management.base.SystemInfo;

import com.sun.appserv.management.client.ProxyFactory;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.StringUtil;

import com.sun.appserv.management.config.ClusterConfig;    	        
import com.sun.appserv.management.config.DomainConfig;    
import com.sun.appserv.management.config.ServerConfig;
import com.sun.appserv.management.config.ClusteredServerConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.ServerRefConfig;
import com.sun.appserv.management.config.RefConfig;

import com.sun.appserv.management.util.stringifier.SmartStringifier;

import com.sun.enterprise.management.support.oldconfig.OldServersMBean;
import com.sun.enterprise.management.support.oldconfig.OldConfigProxies;
import com.sun.enterprise.management.support.oldconfig.OldProps;

import com.sun.appserv.management.util.jmx.MBeanRegistrationListener;
import com.sun.appserv.management.util.jmx.JMXUtil;


/**
	Loads MBeans.
 */
class LoaderOfOldConfig extends LoaderOfOld
{
	private final boolean	mSupportsClusters;
    private final ServerRefListener mServerRefListener;
	
	LoaderOfOldConfig( final Loader loader )
	{
		super( loader );
		
		final SystemInfo	systemInfo	= getDomainRoot().getSystemInfo();
		mSupportsClusters	= systemInfo.supportsFeature( systemInfo.CLUSTERS_FEATURE );
		
		if ( NEEDS_SUPPORT.size() != 0 )
		{
			getLogger().warning(
				"Support for the following old config mbean types is not yet implemented: {" +
				SmartStringifier.toString( NEEDS_SUPPORT ) + "}" );
		}
		
		try
		{
		    mServerRefListener  = new ServerRefListener( "LoaderOfOldConfig" );
            mServerRefListener.startListening();
		}
		catch( Exception e )
		{
		    debug( "" + e );
		    throw new Error( e );
		}
	}
	
	
        private ObjectName
    getListenToServerRefConfigPattern()
    {
	    final String jmxDomain = Util.getObjectName( getDomainRoot() ).getDomain();
        final String props  = Util.makeJ2EETypeProp( XTypes.SERVER_REF_CONFIG );
        return Util.newObjectNamePattern( jmxDomain, props );
    }
    
	/**
	    Listen for creation or removal of AMX ServerRefConfig MBeans.
	 */
	private final class ServerRefListener
	    extends MBeanRegistrationListener
	{
	    public ServerRefListener( final String name )
	        throws InstanceNotFoundException, IOException
	    {
	        super( name, getMBeanServer(), getListenToServerRefConfigPattern() );
	    }
	    
	    /**
	        A ServerRefConfig has been created.  It may require converting a
	        StandaloneServerConfig to a ClusteredServerConfig.
	     */
            protected void
        mbeanRegistered( final ObjectName objectName )
        {
            debug( "mbeanRegistered: " + objectName );
            
	        final ServerRefConfig   ref =
	            ProxyFactory.getInstance( getMBeanServer() ).getProxy( objectName, ServerRefConfig.class);
	        if ( ref.getContainer() instanceof ClusterConfig )
	        {
    	        final String serverName = ref.getRef();
                debug( "mbeanRegistered: serverName = " + serverName );
    	        
    	        final DomainConfig domainConfig = getDomainRoot().getDomainConfig();
    	            
    	        if ( domainConfig.getClusteredServerConfigMap().get( serverName ) == null )
    	        {
    	            final ServerConfig  server  =
    	                domainConfig.getServerConfigMap().get( serverName );

    	            // it's referenced by a cluster, but not a ClusteredServerConfig. Fix it.
        	        final ObjectName    serverObjectName    = Util.getObjectName( server );
                    debug( "mbeanRegistered: serverConfig = " + serverObjectName );
        	        try
        	        {
        	            final Set<RefConfig> containees = new HashSet<RefConfig>();
        	            
        	            containees.addAll( server.getResourceRefConfigMap().values() );
        	            containees.addAll( server.getDeployedItemRefConfigMap().values() );
        	            
        	            mLoader.resyncAMXMBean( serverObjectName );
        	            
        	            for( final RefConfig containee : containees )
        	            {
        	                mLoader.resyncAMXMBean( Util.getObjectName( containee ) );
        	            }
        	        }
        	        catch( Throwable t )
        	        {
        	            getLogger().warning(
        	                "mbeanRegistered: can't resync with: " + serverObjectName +
        	                ": " + t);
        	        }
    	        }
    	        else
    	        {
                    debug( "mbeanRegistered: server is already Clustered (ignoring)" );
    	        }
	        }
	        else
	        {
                debug( "mbeanRegistered: ref not from a cluster (ignoring)" );
	        }
        }
        
	    /**
	        A ServerRefConfig has been removed.  It may require converting a
	        ClusteredServerConfig to a StandaloneServerConfig.
	        <p>
	        NOTE: not implemented: we don't support converting clustered
	        instances to standalone ones.
	     */
            protected void
        mbeanUnregistered( final ObjectName objectName )
        {
            debug( "mbeanUnregistered: " + objectName );
        }
	}
	
		protected Set<String>
	getIgnoreTypes()
	{
		return( OldConfigTypes.getIgnoreTypes() );
	}
	
		private Set<ObjectName>
	getOldServerConfigObjectNames()
	{
		// 8.0 put the type=server MBeans into ias:
		// see if they are present under com.sun.appserv
		//
		ObjectName	pattern		=
			Util.newObjectNamePattern( "com.sun.appserv", "type=server" );
		Set<ObjectName>	objectNames	= JMXUtil.queryNames( getMBeanServer(), pattern, null );
		
		if ( objectNames.size() == 0 )
		{
			pattern		= Util.newObjectNamePattern( "ias", "type=server" );
			
			objectNames	= JMXUtil.queryNames( getMBeanServer(), pattern, null );
		}
		return( objectNames );
	}
	
		protected Set<ObjectName>
	findAllOldCandidates()
	{
		ObjectName	pattern		= null;
		Set<ObjectName>			oldNames	= null;
		
		pattern		= Util.newObjectNamePattern( "com.sun.appserv", "category=config" );
		oldNames	= JMXUtil.queryNames( getMBeanServer(), pattern, null );
		
		oldNames.addAll( getOldServerConfigObjectNames() );
		
		debug( "LoaderOfOldConfig: found the following:" );
		debug( CollectionUtil.toString( oldNames, "\n" ) );
		return( oldNames );
	}
	

	
	/**
		These types need to be supported.
	 */
	private static final Set<String> NEEDS_SUPPORT	= GSetUtil.newUnmodifiableStringSet();
	
		protected Set<String>
	getNeedsSupport()
	{
		return( NEEDS_SUPPORT );
	}
	
	
		private boolean
	isOldConfigMBean( final ObjectName objectName )
	{
		boolean	isOldConfigMBean	= false;
		
		if ( objectName.getDomain().equals( "com.sun.appserv" ) &&
					"config".equals( objectName.getKeyProperty( "category" ) ) )
		{
			final String	type	= objectName.getKeyProperty( "type" );
			
			isOldConfigMBean	= ! OldConfigTypes.getIgnoreTypes().contains( type );
		}
		return( isOldConfigMBean );
	}
	
		protected boolean
	isOldMBean( final ObjectName objectName )
	{
		return( isOldConfigMBean( objectName ) );
	}
		
	private final static String	OLD_SERVER_TYPE	= "server";
	
	private final class MyOldTypes
		implements OldTypeToJ2EETypeMapper
	{
		private final OldConfigTypes		mOldTypes;
		private final OldServersMBean		mOldServers;
		
			public
		MyOldTypes()
		{
			mOldTypes	= OldConfigTypes.getInstance();
			mOldServers	= OldConfigProxies.getInstance( getMBeanServer() ).getOldServersMBean();
		}
		
			public String
		j2eeTypeToOldType( final String j2eeType )
		{
			return( mOldTypes.j2eeTypeToOldType( j2eeType ) );
		}
		
			private boolean
		isStandaloneServer( final String name )
		{
			boolean	isStandalone	= true;
			
			if ( mSupportsClusters )
			{
				final ObjectName[]	standaloneServerObjectNames	= 
					mOldServers.listUnclusteredServerInstances( false );
			
				isStandalone	= false;
				for( int i = 0; i < standaloneServerObjectNames.length; ++i )
				{
					if ( name.equals( Util.getName( standaloneServerObjectNames[ i ] ) ) )
					{
						isStandalone	= true;
						break;
					}
				}
			}
			
			debug( "isStandalone: " + name + " = " + isStandalone );
			
			return( isStandalone );
		}
		
			private String
		getServerJ2EEType( final String name )
		{
			assert( name != null );
			
			final String j2eeType	= isStandaloneServer( name ) ?
				XTypes.STANDALONE_SERVER_CONFIG :
				XTypes.CLUSTERED_SERVER_CONFIG;
		
			debug( "Server " + name + " is of type " + j2eeType );
			return( j2eeType );
		}
		
			public String
		oldTypeToJ2EEType( final String oldType, final ObjectName objectName )
		{
			String	j2eeType	= null;
			
			if ( oldType.equals( objectName.getKeyProperty( "type" ) ) )
			{
				j2eeType	= oldObjectNameToJ2EEType( objectName );
			}
			else if ( oldType.equals( OLD_SERVER_TYPE ) )
			{
				// the oldType is a property other than "type=server"
				final String	name	= objectName.getKeyProperty( "server" );
				assert( name != null ) : "no name in: " + objectName;
				j2eeType	= getServerJ2EEType( name );
			}
			else
			{
				j2eeType	= mOldTypes.oldTypeToJ2EEType( oldType, objectName );
			}
			return( j2eeType );
		}
	
			public String
		oldObjectNameToJ2EEType(
			final ObjectName	objectName )
		{
			String	j2eeType	= null;
			
			final String	oldType	= objectName.getKeyProperty( "type" );
			// need to special-case "server"
			if ( oldType.equals( OLD_SERVER_TYPE ) )
			{
				final String	name	= objectName.getKeyProperty( "name" );
				j2eeType	= getServerJ2EEType( name );
			}
			else
			{
				j2eeType	= mOldTypes.oldTypeToJ2EEType( oldType );
			}
			if ( j2eeType == null )
			{
                getLogger().warning(
                    "can't find j2eeType for com.sun.appserv type=" + oldType +
                    StringUtil.NEWLINE() + 
                    "This usually means that a new domain.xml element has been added which " +
                    "is not yet supported by AMX.  File a bug requesting support, being sure to " +
                    "specify the ObjectName " + StringUtil.quote( objectName ) );
			}
			
			return( j2eeType );
		}
	}
	
	/**
	 */
		protected ObjectName
	oldToNewObjectName( final ObjectName	oldObjectName )
	{
		trace( "oldToNewObjectName: " + oldObjectName );
		final OldTypeToJ2EETypeMapper	mapper	= new MyOldTypes( );
		final OldProps	oldProps	= new OldProps( oldObjectName, mapper );
		final String	domainName	= mLoader.getAMXJMXDomainName();

		String	props	= oldProps.getNewProps();
		
		// may be modified down below
		final ObjectName	newObjectName	= Util.newObjectName( domainName, props );
		
		return( newObjectName );
	}
	
	
	
}
















