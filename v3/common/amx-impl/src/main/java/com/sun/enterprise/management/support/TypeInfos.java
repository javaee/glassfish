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
 
/*
 */

package com.sun.enterprise.management.support;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;

import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;

import com.sun.appserv.management.util.misc.ListUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.ArrayUtil;
import com.sun.appserv.management.helper.AMXDebugHelper;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Extra;
import com.sun.appserv.management.base.Util;
import static com.sun.appserv.management.base.XTypes.*;
import com.sun.appserv.management.j2ee.J2EETypes;

import com.sun.enterprise.util.FeatureAvailability;


/**
	Maps j2eeType to the AMX interface and implementation class for all AMX
    types.
 */
public final class TypeInfos
{   
    /* volatile because a separate thread creates and assigns it */
	private static volatile TypeInfos	INSTANCE;
    
	private final	Map<String,TypeInfo>		mTypeToInfoMap;
	
    private final Map<Class,MBeanInfo> mMBeanInfos;
    
        private static java.util.logging.Logger
    getLogger()
    {
		return java.util.logging.Logger.getLogger( com.sun.logging.LogDomains.ADMIN_LOGGER );
    }
    
		private
	TypeInfos( )
	{
		mTypeToInfoMap		= new HashMap<String,TypeInfo>();

		initMap( );
        
        mMBeanInfos = new HashMap<Class, MBeanInfo>();
        populateMBeanInfos();
	}
    
        private void
	populateMBeanInfos( )
	{
        final MBeanAttributeInfo[]  extra   = getExtraAttributeInfos();
        
        // create the MBeanInfo from the interfaces
        for( final TypeInfo typeInfo: mTypeToInfoMap.values() )
        {
            final Class theInterface = typeInfo.getInterface();
            
            final MBeanInfo info =
                MBeanInfoConverter.getInstance().convert( theInterface, extra );
            
            mMBeanInfos.put( theInterface, info );
        }
	}
    
    
	private static final String[]	EXTRA_REMOVALS	= 
	{
		"ProxyFactory",
		"ConnectionSource",
		"MBeanInfo",
		"AllAttributes",
	};
    
    /**
		A design decision was to not include certain Attributes or pseuod-Attributes directly
		in AMX, so these fields are separated out into 'Extra'.  However, some of these
		are real Attributes that do need to be present in the MBeanInfo supplied by each
		MBean.
	 */
		private static MBeanAttributeInfo[]
	getExtraAttributeInfos()
	{
        final MBeanAttributeInfo[]	extraInfos	=
            JMXUtil.interfaceToMBeanInfo( Extra.class ).getAttributes();
            
        // remove items that are client-side constructs; not real Attributes
        final Map<String,MBeanAttributeInfo>	m	= JMXUtil.attributeInfosToMap( extraInfos );
        for( int i = 0; i < EXTRA_REMOVALS.length; ++i )
        {
            m.remove( EXTRA_REMOVALS[ i ] );
        }
        
        final MBeanAttributeInfo[] result	= new MBeanAttributeInfo[ m.values().size() ];
        m.values().toArray( result );
		
		return( result );
	}


        public MBeanInfo
    getMBeanInfoForInterface( final Class theInterface )
    {
        // does not need to be synchronized
        return mMBeanInfos.get( theInterface );
    }
    
		public static TypeInfos
	getInstance()
	{
        // 'INSTANCE' must be 'volatile' !
        if ( INSTANCE != null )
        {
            return INSTANCE;
        }
        
        // this is not the problematic "double null check"; 'INSTANCE' is volatile above
        synchronized( TypeInfos.class )
        {        
            if ( INSTANCE == null )
            {
                INSTANCE = new TypeInfos();
            }
        }
        
        return INSTANCE;
	}
	
		private void
	add(
		final TypeData		typeData )
		throws ClassNotFoundException
	{
		final String j2eeType	= typeData.getJ2EEType();
		
		final TypeInfo	info	= new TypeInfo( typeData );
		
		mTypeToInfoMap.put( j2eeType, info );
	}
	
	
		private void
	initData()
	{
		final Object[]	all	= DATA;

		for( int i = 0; i < all.length; ++i )
		{
			final TypeData	typeData	= (TypeData)all[ i ];
			
			assert( ! mTypeToInfoMap.containsKey( typeData.getJ2EEType() )) :
				"init(): type already exists: " + typeData.getJ2EEType();
            try
            {
                add( typeData );
            }
            catch( final ClassNotFoundException e )
            {
                // this should never occur in a release version, so need need to I18n it; it's
                // for development warning.
                getLogger().warning( e.toString() );
                getLogger().warning( "SKIPPING AMX type--missing implementation class for: " +
                    typeData.getJ2EEType() );
            }
		}
	}
	
	
	private static final class MiscChild extends TypeData
	{
		public MiscChild(
			final String	j2eeType,
			final String	parentJ2EEType )
		{
			super( j2eeType, parentJ2EEType );
		}
		
		public MiscChild(
			final String	j2eeType,
			final Set<String>		legalParentJ2EETypes )
		{
			super( j2eeType, legalParentJ2EETypes );
		}
		
		public MiscChild(
			final String	j2eeType,
			final Set<String>		legalParentJ2EETypes,
			final String	containedByJ2EEType )
		{
			super( j2eeType, legalParentJ2EETypes, containedByJ2EEType );
		}
	}
	
	private static final class Containee extends TypeData
	{
		public Containee(
			final String	j2eeType,
			final String	containerType )
		{
			super( j2eeType, null, containerType );
		}
	}
	
	private static final class MixedChild extends TypeData
	{
		public MixedChild(
			final String	j2eeType,
			final Set<String>		legalParentJ2EETypes )
		{
			super( j2eeType, legalParentJ2EETypes );
		}
	}
	
	private static final class DomainRootChild extends TypeData
	{
		public DomainRootChild( final String	j2eeType  )
		{
			super( j2eeType, null, DOMAIN_ROOT );
		}
	}
	
	private static class DomainConfigChild extends TypeData
	{
		public DomainConfigChild( final String	j2eeType  )
		{
			super( j2eeType, null, DOMAIN_CONFIG );
		}
	}
	
	private static final class ConfigConfigChild extends TypeData
	{
		public ConfigConfigChild( final String	j2eeType  )
		{
			super( j2eeType, CONFIG_CONFIG );
		}
	}
	
	private static final class ConfigResource extends TypeData
	{
		public ConfigResource( final String	j2eeType  )
		{
			super( j2eeType, null, DOMAIN_CONFIG );
		}
	}
	
	private static final class ResourceConfigMgr extends TypeData
	{
		public ResourceConfigMgr( final String	j2eeType  )
		{
			super( j2eeType, null, DOMAIN_CONFIG );
		}
	}
	
	private static final class SecurityServiceChild extends TypeData
	{
		public SecurityServiceChild( final String	j2eeType  )
		{
			super( j2eeType, SECURITY_SERVICE_CONFIG );
		}
	}
	
	private static final class HTTPServiceChild extends TypeData
	{
		public HTTPServiceChild( final String	j2eeType  )
		{
			super( j2eeType, HTTP_SERVICE_CONFIG );
		}
	}
	
	private static final class LBConfigChild extends TypeData
	{
		public LBConfigChild( final String j2eeType )
		{
			super( j2eeType, LB_CONFIG );
		}
	}
        
        private static final class IIOPServiceChild extends TypeData
	{
		public IIOPServiceChild( final String	j2eeType  )
		{
			super( j2eeType, IIOP_SERVICE_CONFIG );
		}
	}
	
	private static final class AdminServiceConfigChild extends TypeData
	{
		public AdminServiceConfigChild( final String	j2eeType  )
		{
			super( j2eeType, ADMIN_SERVICE_CONFIG );
		}
	}
	
	private static final class J2EEServerChild extends TypeData
	{
		public J2EEServerChild( final String	j2eeType  )
		{
			super( j2eeType, J2EETypes.J2EE_SERVER );
		}
	}
	
	private static final class J2EEDomainChild extends TypeData
	{
		public J2EEDomainChild( final String	j2eeType  )
		{
			super( j2eeType, null, J2EETypes.J2EE_DOMAIN );
		}
	}
	
	private static final class ServerRootMonitorChild extends TypeData
	{
		public ServerRootMonitorChild( final String	j2eeType  )
		{
			super( j2eeType, SERVER_ROOT_MONITOR );
		}
	}
	
    /*
LOAD_BALANCER_MONITORING

	private static final class LoadBalancerMonitorChild extends TypeData
	{
		public LoadBalancerMonitorChild ( final String	j2eeType  )
		{
			super( j2eeType, XTypes.LOAD_BALANCER_MONITOR );
		}
	}
        
	private static final class LoadBalancerClusterMonitorChild extends TypeData
	{
		public LoadBalancerClusterMonitorChild ( final String	j2eeType  )
		{
			super( j2eeType, XTypes.LOAD_BALANCER_CLUSTER_MONITOR );
		}
	}

	private static final class LoadBalancerServerMonitorChild extends TypeData
	{
		public LoadBalancerServerMonitorChild ( final String	j2eeType )
		{
			super( j2eeType, XTypes.LOAD_BALANCER_SERVER_MONITOR );
		}
	}

	private static final class LoadBalancerApplicationMonitorChild extends TypeData
	{
		public LoadBalancerApplicationMonitorChild ( final String j2eeType )
		{
			super( j2eeType, XTypes.LOAD_BALANCER_APPLICATION_MONITOR );
		}
	}
    */
        
    private static final class HTTPServiceMonitorChild extends TypeData
	{
		public HTTPServiceMonitorChild( final String	j2eeType  )
		{
			super( j2eeType, HTTP_SERVICE_MONITOR );
		}
	}
	
	private static final class EJBModuleChild extends TypeData
	{
		public EJBModuleChild( final String	j2eeType)
		{
			super( j2eeType, J2EETypes.EJB_MODULE );
		}
	}    

    private static final Set<String>	CLUSTER_OR_SERVER_CONFIG	=
		GSetUtil.newUnmodifiableStringSet( CLUSTER_CONFIG,
		    STANDALONE_SERVER_CONFIG, CLUSTERED_SERVER_CONFIG );
		
/**
	Data relating a j2eeType to its legal parent type(s).
 */
private static TypeData[] DATA	= new TypeData[] 
{
new MiscChild( DOMAIN_ROOT, null, null ),

new DomainRootChild( SYSTEM_INFO  ),
new DomainRootChild( UPLOAD_DOWNLOAD_MGR ),
//new DomainRootChild( DOMAIN_CONFIG ),
new DomainRootChild( NOTIFICATION_SERVICE_MGR  ),
//new DomainRootChild( DEPLOYMENT_MGR ),
new DomainRootChild( QUERY_MGR ),
//new DomainRootChild( WEB_SERVICE_MGR ),
//new DomainRootChild( UPDATE_STATUS ),
new DomainRootChild( BULK_ACCESS ),
new DomainRootChild( SAMPLE ),
//new DomainRootChild( CONFIG_DOTTED_NAMES ),
//new DomainRootChild( MONITORING_DOTTED_NAMES ),
new DomainRootChild( NOTIFICATION_EMITTER_SERVICE ),
new MiscChild( NOTIFICATION_SERVICE, NOTIFICATION_SERVICE_MGR),

/*
new DomainRootChild( LOAD_BALANCER ),

new DomainConfigChild( CONFIG_CONFIG ),
new DomainConfigChild( WEB_MODULE_CONFIG  ),
new DomainConfigChild( EJB_MODULE_CONFIG ),
new DomainConfigChild( J2EE_APPLICATION_CONFIG ),
new DomainConfigChild( RAR_MODULE_CONFIG ),
new DomainConfigChild( APP_CLIENT_MODULE_CONFIG ),

new DomainConfigChild( NODE_AGENT_CONFIG ),
new DomainConfigChild( RESOURCE_ADAPTER_CONFIG ),
new DomainConfigChild( LIFECYCLE_MODULE_CONFIG ),
new DomainConfigChild( EXTENSION_MODULE_CONFIG ),

new DomainConfigChild( LOAD_BALANCER_CONFIG ),
new DomainConfigChild( LB_CONFIG ),
new DomainConfigChild( CLUSTER_CONFIG ),
new DomainConfigChild( STANDALONE_SERVER_CONFIG ),
new DomainConfigChild( CONNECTOR_MODULE_CONFIG ),
new DomainConfigChild( CUSTOM_MBEAN_CONFIG ),

new DomainConfigChild( CLUSTERED_SERVER_CONFIG ),


new ConfigConfigChild( MANAGEMENT_RULES_CONFIG ),
new MiscChild( MANAGEMENT_RULE_CONFIG, MANAGEMENT_RULES_CONFIG),
new MiscChild( EVENT_CONFIG, MANAGEMENT_RULE_CONFIG),
new MiscChild( ACTION_CONFIG, MANAGEMENT_RULE_CONFIG),

//new ConfigConfigChild( ALERT_SERVICE_CONFIG ),
//new MiscChild( ALERT_SUBSCRIPTION_CONFIG, ALERT_SERVICE_CONFIG ),
//new MiscChild( FILTER_CONFIG, ALERT_SUBSCRIPTION_CONFIG ),
//new MiscChild( LISTENER_CONFIG, ALERT_SUBSCRIPTION_CONFIG ),

new MiscChild( SECURITY_MAP_CONFIG, CONNECTOR_CONNECTION_POOL_CONFIG ),
new MiscChild( BACKEND_PRINCIPAL_CONFIG, SECURITY_MAP_CONFIG ),

new MixedChild( WEB_SERVICE_ENDPOINT_CONFIG,
    GSetUtil.newUnmodifiableStringSet( J2EE_APPLICATION_CONFIG, EJB_MODULE_CONFIG,
        WEB_MODULE_CONFIG ) ),
new MiscChild( REGISTRY_LOCATION_CONFIG, WEB_SERVICE_ENDPOINT_CONFIG ),
new MiscChild( TRANSFORMATION_RULE_CONFIG, WEB_SERVICE_ENDPOINT_CONFIG ),


new ConfigResource( CONNECTOR_CONNECTION_POOL_CONFIG ),
new ConfigResource( CUSTOM_RESOURCE_CONFIG  ),
new ConfigResource( JDBC_RESOURCE_CONFIG  ),
new ConfigResource( JNDI_RESOURCE_CONFIG  ),
new ConfigResource( PERSISTENCE_MANAGER_FACTORY_RESOURCE_CONFIG ),
new ConfigResource( MAIL_RESOURCE_CONFIG ),
new ConfigResource( JDBC_CONNECTION_POOL_CONFIG ),
new ConfigResource( ADMIN_OBJECT_RESOURCE_CONFIG ),
new ConfigResource( CONNECTOR_RESOURCE_CONFIG ),


new AdminServiceConfigChild( DAS_CONFIG ),

new MixedChild( JMX_CONNECTOR_CONFIG,
			GSetUtil.newUnmodifiableStringSet( ADMIN_SERVICE_CONFIG,
								NODE_AGENT_CONFIG ) ),
//new AdminServiceConfigChild( JMX_CONNECTOR_CONFIG ),

new SecurityServiceChild( AUDIT_MODULE_CONFIG ),
new MixedChild( AUTH_REALM_CONFIG,
	GSetUtil.newUnmodifiableStringSet( SECURITY_SERVICE_CONFIG, NODE_AGENT_CONFIG )
	),
	
new SecurityServiceChild( JACC_PROVIDER_CONFIG ),
new SecurityServiceChild( MESSAGE_SECURITY_CONFIG ),

new ConfigConfigChild( ADMIN_SERVICE_CONFIG ),
new ConfigConfigChild( AVAILABILITY_SERVICE_CONFIG ),
new ConfigConfigChild( EJB_CONTAINER_CONFIG ),
new ConfigConfigChild( HTTP_SERVICE_CONFIG ),
new ConfigConfigChild( IIOP_SERVICE_CONFIG ),
new ConfigConfigChild( JAVA_CONFIG ),

new MixedChild( LOG_SERVICE_CONFIG,
	GSetUtil.newUnmodifiableStringSet( CONFIG_CONFIG, NODE_AGENT_CONFIG )
	),

new ConfigConfigChild( MDB_CONTAINER_CONFIG ),
new ConfigConfigChild( MONITORING_SERVICE_CONFIG ),
new ConfigConfigChild( SECURITY_SERVICE_CONFIG ),
new ConfigConfigChild( CONNECTOR_SERVICE_CONFIG ),
new ConfigConfigChild( WEB_CONTAINER_CONFIG ),
new ConfigConfigChild( TRANSACTION_SERVICE_CONFIG ),
new ConfigConfigChild( JMS_SERVICE_CONFIG ),
new ConfigConfigChild( THREAD_POOL_CONFIG ),
new ConfigConfigChild( GROUP_MANAGEMENT_SERVICE_CONFIG ),
new ConfigConfigChild( DIAGNOSTIC_SERVICE_CONFIG ),


new HTTPServiceChild( HTTP_LISTENER_CONFIG ),
new HTTPServiceChild( ACCESS_LOG_CONFIG ),
new HTTPServiceChild( KEEP_ALIVE_CONFIG ),
new HTTPServiceChild( REQUEST_PROCESSING_CONFIG ),
new HTTPServiceChild( CONNECTION_POOL_CONFIG ),
new HTTPServiceChild( HTTP_PROTOCOL_CONFIG ),
new HTTPServiceChild( HTTP_FILE_CACHE_CONFIG ),
new HTTPServiceChild( VIRTUAL_SERVER_CONFIG ), 

new IIOPServiceChild( IIOP_LISTENER_CONFIG ),
new IIOPServiceChild( ORB_CONFIG ),
new LBConfigChild(CLUSTER_REF_CONFIG),
new MiscChild( PROFILER_CONFIG, JAVA_CONFIG ),

new MiscChild( JMS_HOST_CONFIG, JMS_SERVICE_CONFIG ),  

new MiscChild( HTTP_ACCESS_LOG_CONFIG, VIRTUAL_SERVER_CONFIG ),

//new MiscChild( LB_CLUSTER_REF_CONFIG, LB_CONFIG ),
	        
new MiscChild( MODULE_LOG_LEVELS_CONFIG, LOG_SERVICE_CONFIG ),
new MiscChild( MODULE_MONITORING_LEVELS_CONFIG, MONITORING_SERVICE_CONFIG ),

new MiscChild( SESSION_CONFIG, WEB_CONTAINER_CONFIG ),
new MiscChild( SESSION_MANAGER_CONFIG, SESSION_CONFIG ),
new MiscChild( SESSION_PROPERTIES_CONFIG, SESSION_CONFIG ),

new MiscChild( MANAGER_PROPERTIES_CONFIG, SESSION_MANAGER_CONFIG ),
new MiscChild( STORE_PROPERTIES_CONFIG, SESSION_MANAGER_CONFIG ),
	
new MiscChild( EJB_TIMER_SERVICE_CONFIG, EJB_CONTAINER_CONFIG ),
new MiscChild( EJB_CONTAINER_AVAILABILITY_CONFIG, AVAILABILITY_SERVICE_CONFIG ),
new MiscChild( WEB_CONTAINER_AVAILABILITY_CONFIG, AVAILABILITY_SERVICE_CONFIG ),
new MiscChild( JMS_AVAILABILITY_CONFIG, AVAILABILITY_SERVICE_CONFIG ),

new MixedChild( DEPLOYED_ITEM_REF_CONFIG, CLUSTER_OR_SERVER_CONFIG ),
new MixedChild( RESOURCE_REF_CONFIG, CLUSTER_OR_SERVER_CONFIG ),

new MixedChild( SSL_CONFIG,
		GSetUtil.newUnmodifiableStringSet( IIOP_LISTENER_CONFIG,
                                IIOP_SERVICE_CONFIG,
                                HTTP_LISTENER_CONFIG,
                                JMX_CONNECTOR_CONFIG ) ),

new MixedChild( HEALTH_CHECKER_CONFIG,
	        GSetUtil.newUnmodifiableStringSet( SERVER_REF_CONFIG, CLUSTER_REF_CONFIG ) ),

new MixedChild( SERVER_REF_CONFIG,
                GSetUtil.newUnmodifiableStringSet( CLUSTER_CONFIG, LB_CONFIG ) ),
//new MiscChild( CLUSTER_REF_CONFIG, LB_CONFIG  ),        

        
new MiscChild( PROVIDER_CONFIG, MESSAGE_SECURITY_CONFIG ),
new MiscChild( REQUEST_POLICY_CONFIG, PROVIDER_CONFIG ),
new MiscChild( RESPONSE_POLICY_CONFIG, PROVIDER_CONFIG ),

*/


//--------------------------------------------------------------------------------
// JSR 77 types
/*
new DomainRootChild( J2EETypes.J2EE_DOMAIN ),

new J2EEDomainChild( J2EETypes.J2EE_CLUSTER ),
//new J2EEDomainChild( J2EETypes.J2EE_STANDALONE_SERVER ),
new J2EEDomainChild( J2EETypes.J2EE_SERVER ),

new J2EEServerChild( J2EETypes.JVM ),
new J2EEServerChild( J2EETypes.J2EE_APPLICATION ),
new J2EEServerChild( J2EETypes.JDBC_DRIVER ),
new J2EEServerChild( J2EETypes.JDBC_RESOURCE ),
new J2EEServerChild( J2EETypes.JMS_RESOURCE ),
new J2EEServerChild( J2EETypes.JNDI_RESOURCE ),
new J2EEServerChild( J2EETypes.JTA_RESOURCE ),
new J2EEServerChild( J2EETypes.RESOURCE_ADAPTER_MODULE ),
new J2EEServerChild( J2EETypes.RMI_IIOP_RESOURCE ),
new J2EEServerChild( J2EETypes.URL_RESOURCE ),
new J2EEServerChild( J2EETypes.JAVA_MAIL_RESOURCE ),
new J2EEServerChild( J2EETypes.JCA_RESOURCE ),

new MiscChild( J2EETypes.APP_CLIENT_MODULE, J2EETypes.J2EE_APPLICATION ),
new MiscChild( J2EETypes.EJB_MODULE, J2EETypes.J2EE_APPLICATION ),
new MiscChild( J2EETypes.WEB_MODULE, J2EETypes.J2EE_APPLICATION ),
new MiscChild( J2EETypes.RESOURCE_ADAPTER, J2EETypes.RESOURCE_ADAPTER_MODULE ),

new EJBModuleChild( J2EETypes.ENTITY_BEAN ),
new EJBModuleChild( J2EETypes.STATEFUL_SESSION_BEAN ),
new EJBModuleChild( J2EETypes.MESSAGE_DRIVEN_BEAN ),
new EJBModuleChild( J2EETypes.STATELESS_SESSION_BEAN ),

new MiscChild( J2EETypes.JCA_CONNECTION_FACTORY, J2EETypes.JCA_RESOURCE ),
new MiscChild( J2EETypes.JCA_MANAGED_CONNECTION_FACTORY, J2EETypes.JCA_RESOURCE ),
new MiscChild( J2EETypes.JDBC_DATA_SOURCE, J2EETypes.JDBC_RESOURCE ),

new MiscChild( J2EETypes.SERVLET, J2EETypes.WEB_MODULE ),

new MixedChild( J2EETypes.WEB_SERVICE_ENDPOINT, GSetUtil.newUnmodifiableStringSet(J2EETypes.WEB_MODULE
,J2EETypes.EJB_MODULE) ),

*/


// JMX monitoring
new DomainRootChild( JMX_MONITOR_MGR ), 
new DomainRootChild( JMX_COUNTER_MONITOR ), 
new DomainRootChild( JMX_GAUGE_MONITOR  ), 
new DomainRootChild( JMX_STRING_MONITOR  ), 

//-----------------
// Monitoring types
//-----------------
/*
new DomainRootChild( MONITORING_ROOT ), 
new Containee( SERVER_ROOT_MONITOR, MONITORING_ROOT ), 

new ServerRootMonitorChild( JVM_MONITOR ), 
new ServerRootMonitorChild( CALL_FLOW_MONITOR ), 
new ServerRootMonitorChild( TRANSACTION_SERVICE_MONITOR ), 
new ServerRootMonitorChild( HTTP_SERVICE_MONITOR ),  
new ServerRootMonitorChild( JDBC_CONNECTION_POOL_MONITOR ),
new ServerRootMonitorChild( APPLICATION_MONITOR ),
new ServerRootMonitorChild( LOGGING ),

//new MiscChild( WEB_MODULE_MONITOR, APPLICATION_MONITOR ),
new MiscChild( WEB_MODULE_VIRTUAL_SERVER_MONITOR, APPLICATION_MONITOR ),
new MiscChild( SERVLET_MONITOR, WEB_MODULE_VIRTUAL_SERVER_MONITOR ),
new MiscChild( WEBSERVICE_ENDPOINT_MONITOR, GSetUtil.newUnmodifiableStringSet(
WEB_MODULE_VIRTUAL_SERVER_MONITOR , EJB_MODULE_MONITOR)),

new MiscChild( EJB_MODULE_MONITOR, APPLICATION_MONITOR ), 
new MiscChild( STATELESS_SESSION_BEAN_MONITOR, EJB_MODULE_MONITOR ),
new MiscChild( STATEFUL_SESSION_BEAN_MONITOR, EJB_MODULE_MONITOR ),
new MiscChild( ENTITY_BEAN_MONITOR, EJB_MODULE_MONITOR ),
new MiscChild( MESSAGE_DRIVEN_BEAN_MONITOR, EJB_MODULE_MONITOR ),
 
new MiscChild( BEAN_POOL_MONITOR,
	GSetUtil.newUnmodifiableStringSet( ENTITY_BEAN_MONITOR, STATEFUL_SESSION_BEAN_MONITOR,
                STATELESS_SESSION_BEAN_MONITOR, MESSAGE_DRIVEN_BEAN_MONITOR) ),
	
new MiscChild( BEAN_CACHE_MONITOR,
	GSetUtil.newUnmodifiableStringSet( ENTITY_BEAN_MONITOR, MESSAGE_DRIVEN_BEAN_MONITOR, 
		STATEFUL_SESSION_BEAN_MONITOR, STATELESS_SESSION_BEAN_MONITOR) ),

new MiscChild( BEAN_METHOD_MONITOR,
	GSetUtil.newUnmodifiableStringSet( ENTITY_BEAN_MONITOR, STATEFUL_SESSION_BEAN_MONITOR,
		STATELESS_SESSION_BEAN_MONITOR, MESSAGE_DRIVEN_BEAN_MONITOR) ),
	
	        

new ServerRootMonitorChild( CONNECTOR_CONNECTION_POOL_MONITOR ),
           

new ServerRootMonitorChild( THREAD_POOL_MONITOR ),
new ServerRootMonitorChild( CONNECTION_MANAGER_MONITOR ),

new MiscChild( HTTP_SERVICE_VIRTUAL_SERVER_MONITOR,HTTP_SERVICE_MONITOR ),
new HTTPServiceMonitorChild( HTTP_LISTENER_MONITOR ),
new HTTPServiceMonitorChild( FILE_CACHE_MONITOR ),
new HTTPServiceMonitorChild( KEEP_ALIVE_MONITOR ),
new HTTPServiceMonitorChild( CONNECTION_QUEUE_MONITOR ),

*/
};

	
		private void
	initParentsForChildType( final TypeInfo	info )
	{
		if ( info.isSubType() )
		{
			final Set		possibleParents	= info.getLegalParentJ2EETypes();
			
			final Iterator	parentTypeIter	= possibleParents.iterator();
			while ( parentTypeIter.hasNext() )
			{
				final String	parentJ2EEType	= (String)parentTypeIter.next();
				final TypeInfo	parentInfo	= getInfo( parentJ2EEType );
				
				parentInfo.addChildJ2EEType( info.getJ2EEType() );
			}
		}
	}
	
		private void
	initChildAndContaineeTypes()
	{
		final Set<String>   keys		= getJ2EETypes();
		
		for( final String childJ2EEType : keys )
		{
			final TypeInfo	info	= getInfo( childJ2EEType );
		
			if ( info.isSubType() )
			{
				initParentsForChildType( info );
			}
			else	// may be contained in something
			{
				final String	containedByJ2EEType	= info.getContainedByJ2EEType();
				
				if ( containedByJ2EEType != null )
				{
					final TypeInfo	parentInfo	= getInfo( containedByJ2EEType );
					parentInfo.addContaineeJ2EEType( childJ2EEType );
				}
			}
		}
	}
	
	/**
		Initialize a mapping of j2eeTypes to TypeInfo
	 */
		private void
	initMap()
	{
		initData();		
		initChildAndContaineeTypes();
	}


		public TypeInfo
	getInfo( final String j2eeType )
	{
		if ( j2eeType == null )
		{
			throw new IllegalArgumentException( "null" );
		}
		
		final TypeInfo	info	= (TypeInfo)mTypeToInfoMap.get( j2eeType );
		
		if ( info == null )
		{
			throw new IllegalArgumentException( j2eeType );
		}
		
		return( info );
	}
	
	/**
		Return the keys by which all TypeInfos are mapped.
	 */
		public Set<String>
	getJ2EETypes()
	{
		return( mTypeToInfoMap.keySet( ) );
	}
	
		public String
	toString()
	{
		final StringBuffer	buf	= new StringBuffer();
		
		final Set<String>		keys	= getJ2EETypes();
		for( final String j2eeType : keys )
		{
			final TypeInfo	info			= getInfo( j2eeType );
			
			buf.append( info.toString() );
			buf.append( "\n" );
		}
		
		return( buf.toString() );
	}
	
	/**
		Get the contained-by chain for the specified j2eeType.  Calling
		this is only legal for j2eeTypes for which Type.isSubType() is false.
		
		@param j2eeType
	 */
		public String[]
	getContaineeByChain( final String j2eeType )
	{
		TypeInfo	info	= getInfo( j2eeType );
		if ( info.isSubType() )
		{
			throw new IllegalArgumentException( "j2eeType is a subtype: " + j2eeType );
		}
		
		final List<String>	list	= new ArrayList<String>();
		String	containedByType	= null;
		while ( (containedByType = info.getContainedByJ2EEType()) != null )
		{
			list.add( containedByType );
			info	= getInfo( containedByType );
		}
		
		Collections.reverse( list );
		return( ListUtil.toStringArray( list ) );
	}
	
	/**
		Get the j2eeType chain for the specified ObjectName.  The last element
		is the same as the j2eeType of the ObjectName supplied; preceeding
		types, if present, are its parent types.
		
		@param objectName	the ObjectName to examine
	 */
		public String[]
	getJ2EETypeChain( final ObjectName	objectName)
	{
		final String	j2eeType	= Util.getJ2EEType( objectName );
		if ( j2eeType == null )
		{
			throw new IllegalArgumentException( objectName.toString() );
		}
		
		TypeInfo	info	= getInfo( j2eeType );
		if ( info == null )
		{
			throw new IllegalArgumentException( "uknown j2eeType: " + j2eeType );
		}
		
		final List<String>	list	= new ArrayList<String>();
		list.add( j2eeType );
		while ( info.isSubType() )
		{
			final Set<String>	possibleParentTypes	= info.getLegalParentJ2EETypes();
			
			String	parentJ2EEType	= null;
			if ( possibleParentTypes.size() == 1 )
			{
				parentJ2EEType	= GSetUtil.getSingleton( possibleParentTypes );
			}
			else
			{
				parentJ2EEType	= JMXUtil.findKey( possibleParentTypes, objectName );
			}
			
			if ( parentJ2EEType == null )
			{
				throw new IllegalArgumentException(
				"MBean: " + objectName +
				" does not have any of the possible parent keys: {" +
				toString( possibleParentTypes ) + "}" );
			}
			
			list.add( parentJ2EEType );
			
			info	= getInfo( parentJ2EEType );
		}
		
		// list is in reverse order; child, parent, parent's parent, etc.  Reverse it
		// so that child is last
		Collections.reverse( list );
		
		return( ListUtil.toStringArray( list )  );
	}
	
		private String
	toString( final Object o )
	{
		return( SmartStringifier.toString( o ) );
	}
}







