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

package com.sun.enterprise.management.support.oldconfig;

import java.util.Set;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.base.Util;

import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.util.i18n.StringManager;

/**
	Provides support for getting proxies to "old" (8.0) config MBeans.
 */
public class OldConfigProxies 
{
        final MBeanServer	mServer;
        private static final	StringManager	_strMgr = 
                StringManager.getManager(OldConfigProxies.class);
	
		private
	OldConfigProxies( final MBeanServer server )
	{
		mServer	= server;
	}
	
	private final String	OLD_DOMAIN				= "com.sun.appserv";
	private final String	CONFIGS_IN_OLD_DOMAIN	= "com.sun.appserv:category=config";
	
	private static OldConfigProxies	INSTANCE	= null;
	
		public static synchronized OldConfigProxies
	getInstance( final MBeanServer server )
	{
		if ( INSTANCE == null )
		{
			INSTANCE	= new OldConfigProxies( server );
		}
		else
		{
			assert( INSTANCE.getMBeanServer() == server );
		}
		return( INSTANCE );
	}
	
		private MBeanServer
	getMBeanServer()
	{
		return( mServer );
	}
	
	/**
		@param target			the target MBean ObjectName
		@param interfaceClass	the interface class to use
	 */
		public <T> T
	newProxy(
		final ObjectName	target,
		final Class<T>		interfaceClass )
	{
		return interfaceClass.cast( MBeanServerInvocationHandler.newProxyInstance(
					mServer, target, interfaceClass, false ) );
	}
	
	/**
		@param props			properties for use in an ObjectName query
		@param interfaceClass	the interface class to use
	 */
		public <T> T
	newProxy(
		final String	props,
		final Class<T>	interfaceClass )
	{
		final ObjectName	objectName	= queryOldConfigObjectName( props );
		
		return newProxy( objectName, interfaceClass );
	}
	
		private ObjectName
	queryOldConfigObjectName( String props )
	{
		final ObjectName pattern	=
			Util.newObjectNamePattern( OLD_DOMAIN,
				Util.concatenateProps( props, "category=config" ) );
		
		final Set<ObjectName>	candidates	= JMXUtil.queryNames( mServer, pattern, null );
		
		ObjectName	oldConfigObjectName	= null;
		
		if ( candidates.size() == 1 )
		{
			oldConfigObjectName	= GSetUtil.getSingleton( candidates );
		}
		else if ( candidates.size() == 0 )
		{
			oldConfigObjectName	= null;
		}
		else
		{
                        final String msg = _strMgr.getString("FoundMoreThanOneMatch", props);
			throw new IllegalArgumentException( msg );
		}
		
		return( oldConfigObjectName );
		
	}

		public OldProperties
	getOldProperties( final ObjectName	objectName )
	{
		return( newProxy( objectName, OldProperties.class ) );
	}
	
		public OldConfigsMBean
	getOldConfigsMBean()
	{
		return( newProxy( "type=configs", OldConfigsMBean.class ) );
	}
	
		public OldConfig
	getOldConfig( final String name )
	{
		return( newProxy( "type=config,name=" + name, OldConfig.class ) );
	}
                
        public OldClustersMBean
	getOldClustersMBean()
	{
		return( newProxy( "type=clusters", OldClustersMBean.class ) );
	}
                
		public OldServersMBean
	getOldServersMBean()
	{
		return( newProxy( "type=servers", OldServersMBean.class ) );
	}
	
		public OldResourcesMBean
	getOldResourcesMBean()
	{
		return( newProxy( "type=resources", OldResourcesMBean.class ) );
	}
                
                public OldApplicationsConfigMBean
	getOldApplicationsConfigMBean()
	{
		return( newProxy( 
				"type=applications", OldApplicationsConfigMBean.class ) );
	}
                
		public OldDomainMBean
	getOldDomainMBean()
	{
		return( newProxy( "type=domain", OldDomainMBean.class ) );
	}
	
        public OldClusterMBean
	getOldClusterMBean( final String clusterName )
	{
		return( newProxy(
				"type=cluster,name=" + clusterName, OldClusterMBean.class ) );
	}
	
		public OldServerMBean
	getOldServerMBean( final String serverName )
	{
		return( newProxy(
				"type=server,name=" + serverName, OldServerMBean.class ) );
	}
	
		public OldAdminServiceMBean
	getOldAdminServiceMBean( final String configName )
	{
		return( newProxy(
				"type=admin-service,config=" + configName, OldAdminServiceMBean.class ) );
	}
	
		public OldHTTPServiceMBean
	getOldHTTPServiceMBean( final String configName )
	{
		return( newProxy(
				"type=http-service,config=" + configName, OldHTTPServiceMBean.class ) );
	}
	
		public OldVirtualServerMBean
	getOldVirtualServerMBean(
	    final String    configName,
	    final String    virtualServer )
	{
		return( newProxy(
				"type=virtual-server,config=" + configName +
				",id=" + virtualServer, OldVirtualServerMBean.class ) );
	}
	
		public OldIIOPServiceMBean
	getOldIIOPServiceMBean( final String configName )
	{
		return( newProxy( 
				"type=iiop-service,config=" + configName, OldIIOPServiceMBean.class ) );
	}
	
	
		public OldSecurityServiceMBean
	getOldSecurityServiceMBean( final String configName )
	{
		return( newProxy( 
				"type=security-service,config=" + configName, OldSecurityServiceMBean.class ) );
	}
	
	
		public OldTransactionServiceMBean
	getOldTransactionServiceMBean( final String configName )
	{
		return( newProxy( 
				"type=transaction-service,config=" + configName, OldTransactionServiceMBean.class ) );
	}
	
	
		public OldLogServiceMBean
	getOldLogServiceMBean( final String configName )
	{
		return( newProxy( 
				"type=log-service,config=" + configName, OldLogServiceMBean.class ) );
	}
	
	
		public OldMonitoringServiceMBean
	getOldMonitoringServiceMBean( final String configName )
	{
		return( newProxy( 
				"type=monitoring-service,config=" + configName, OldMonitoringServiceMBean.class ) );
	}
	
		public OldSessionConfigMBean
	getOldSessionConfigMBean( final String configName )
	{
		return( newProxy( 
				"type=session,config=" + configName, OldSessionConfigMBean.class ) );
	}
	
		public OldSessionManagerMBean
	getOldSessionManagerMBean( final String configName )
	{
		return( newProxy( 
				"type=session-manager,config=" + configName, OldSessionManagerMBean.class ) );
	}
	
	
		public OldEJBContainerConfigMBean
	getOldEJBContainerConfigMBean( final String configName )
	{
		return( newProxy( 
				"type=ejb-container,config=" + configName, OldEJBContainerConfigMBean.class ) );
	}
	
	
		public OldWebContainerConfigMBean
	getOldWebContainerConfigMBean( final String configName )
	{
		return( newProxy( 
				"type=web-container,config=" + configName, OldWebContainerConfigMBean.class ) );
	}
	
		public OldAvailabilityServiceMBean
	getOldAvailabilityServiceMBean( final String configName )
	{
		return( newProxy( 
				"type=availability,config=" + configName, OldAvailabilityServiceMBean.class ) );
	}
	
	
		public OldJavaConfigMBean
	getOldJavaConfigMBean( final String configName )
	{
		return( newProxy( 
				"type=java-config,config=" + configName, OldJavaConfigMBean.class ) );
	}
	
	
		public OldModuleLogLevelsMBean
	getOldModuleLogLevelsMBean( final String configName )
	{
		return( newProxy( 
				"type=module-log-levels,config=" + configName, OldModuleLogLevelsMBean.class ) );
	}
	
	
		public OldModuleMonitoringLevelsMBean
	getOldModuleMonitoringLevelsMBean( final String configName )
	{
		return( newProxy( 
				"type=module-monitoring-levels,config=" + configName, OldModuleMonitoringLevelsMBean.class ) );
	}
	
	
		public OldThreadPoolsConfigMBean
	getOldThreadPoolsConfigMBean( final String configName )
	{
		return( newProxy( 
				"type=thread-pools,config=" + configName, OldThreadPoolsConfigMBean.class ) );
	}
	
	
		public OldDASConfigMBean
	getOldDASConfigMBean( final String configName )
	{
		return( newProxy( 
				"type=das-config,config=" + configName, OldDASConfigMBean.class ) );
	}
	
        public OldSystemServicesMBean
	getOldSystemServicesMBean()
	{
		return( newProxy( ObjectNames.kDefaultIASDomainName + ":type=system-services,server=server", 
                        OldSystemServicesMBean.class ) );
	}

		public OldJMSServiceMBean
	getOldJMSServiceMBean( final String configName )
	{
		return( newProxy( 
				"type=jms-service,config=" + configName, OldJMSServiceMBean.class ) );
	}
                
         public OldMessageSecurityConfigMBean
	getOldMessageSecurityConfigMBean( final String name )
	{
		return( newProxy( 
                    "type=message-security-config,name=" + name, 
                        OldMessageSecurityConfigMBean.class ) );
	}


		public OldLbConfigs
	getOldLbConfigs( )
	{
		return( newProxy( "type=lb-configs", OldLbConfigs.class ) );
	}
	
		public OldLbConfig
	getOldLbConfig( final String name)
	{
            return( (OldLbConfig)newProxy( 
                "type=lb-config,name="+name, OldLbConfig.class ) );
	}
                
                public OldServerRefMBean
	getOldServerRefMBean(
            final String name,
            final String props )
        {
            return( ( OldServerRefMBean)newProxy(
                   props + ",type=server-ref,ref=" + name, OldServerRefMBean.class));
        }
	
		public OldClusterRefMBean
	getOldClusterRefMBean(
            final String name,
            final String props )
    	{
            return( ( OldClusterRefMBean)newProxy(
                   props + ",type=cluster-ref,ref=" + name, OldClusterRefMBean.class));
	}

                public OldLoadBalancers
	getOldLoadBalancers( )
	{
		return( newProxy( "type=load-balancers", OldLoadBalancers.class ) );
	}
	
	    private static String
	getManagementRulesProps( final String configName )
	{
	    return "type=management-rules,config=" + configName;
	}
	
	    public ObjectName
	createOldManagementRules( final String configName )
	{
	    final String    props = getManagementRulesProps( configName );
	    
	    ObjectName  objectName  =
	        queryOldConfigObjectName( props );
	        
	    if ( objectName == null )
	    {
	        // null seems to be OK
	        getOldConfig( configName ).createManagementRules( null );
	        
	        objectName  = queryOldConfigObjectName( props );
	    }
	    return objectName;
	}
	
	
		public OldManagementRules
	getOldManagementRules( final String configName )
	{
		return( 
		    newProxy( getManagementRulesProps( configName ), OldManagementRules.class ) );
	}
	
	
		public OldConnectorConnectionPoolMBean
	getOldConnectorConnectionPool( final String name )
	{
		return( 
		    newProxy( "type=connector-connection-pool,name=" + name, OldConnectorConnectionPoolMBean.class ) );
	}
	
	
		public ObjectName
	getOldSecurityMapObjectName(
	    final String connectorConnectionPoolName,
	    final String name )
	{
	    return getOldConnectorConnectionPool( connectorConnectionPoolName ).getSecurityMapByName( name );
	}
	
		public OldSecurityMap
	getOldSecurityMap(
	    final String connectorConnectionPoolName,
	    final String name )
	{
	    final ObjectName securityMapObjectName =
	        getOldSecurityMapObjectName( connectorConnectionPoolName, name );
	    
		return newProxy( securityMapObjectName, OldSecurityMap.class );
	}

        public OldWebServiceEndpointConfigMBean
	getOldWebServiceEndpointConfigMBean( final String name)
	{
            return( ( OldWebServiceEndpointConfigMBean)newProxy(
               "type=web-service-endpoint,name=" + name,
               OldWebServiceEndpointConfigMBean.class));
	}

}











