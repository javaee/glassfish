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
 */
 
package com.sun.appserv.management.base;


/**
	These are the types possible as values for 'j2eeType' in an ObjectName.
	Each of these types is an adjunct to JSR 77, dealing specifically with our notion
	of configuration and/or monitoring.
	
	@see com.sun.appserv.management.j2ee.J2EETypes
 */
public final class XTypes
{
	private static final String	P	=  "X-";	// keep it short in this file
	private static final String	M	=  "Mgr";	// keep it short in this file
	
	/**
		Common prefix for all types in XTypes
	 */
	public static final String	PREFIX	=  P;
	
	/**
		Common suffix for all manager types.
	 */
	//public static final String	MGR_SUFFIX	=  M;
	
	/**
		ID for {@link com.sun.appserv.management.DomainRoot}
	 */
	public final static String	DOMAIN_ROOT				= P + "DomainRoot";
	
	/**
		ID for {@link com.sun.appserv.management.base.SystemInfo}
	 */
	public final static String	SYSTEM_INFO				=  P + "SystemInfo";
	
	/**
		ID for {@link com.sun.appserv.management.base.SystemInfo}
	 */
	public final static String	SYSTEM_STATUS				=  P + "SystemStatus";
	
	/**
		ID for {@link com.sun.appserv.management.base.KitchenSink}
	 */
	public final static String	KITCHEN_SINK				=  P + "KitchenSink";
	
	/**
		ID for {@link com.sun.appserv.management.ext.update.UpdateStatus}
	 */
	public final static String	UPDATE_STATUS				=  P + "UpdateStatus";
	
	/**
		ID for {@link com.sun.appserv.management.base.NotificationService}
	 */
	public final static String	NOTIFICATION_SERVICE		=  P + "NotificationService";
	
	/**
		ID for {@link com.sun.appserv.management.base.NotificationServiceMgr}
	 */
	public final static String	NOTIFICATION_SERVICE_MGR	=  P + "NotificationService" + M;
	
	/**
		ID for {@link com.sun.appserv.management.base.NotificationEmitterService}
	 */
	public final static String	NOTIFICATION_EMITTER_SERVICE		=  P + "NotificationEmitterService";
	
	/**
		ID for {@link com.sun.appserv.management.ext.logging.Logging}
	 */
	public final static String	LOGGING		=  P + "Logging";
	
	/**
		ID for {@link com.sun.appserv.management.monitor.CallFlowMonitor}
	 */
	public final static String	CALL_FLOW_MONITOR		=  P + "CallFlowMonitor";
	
	/**
		ID for {@link com.sun.appserv.management.base.UploadDownloadMgr}
	 */
	public final static String	UPLOAD_DOWNLOAD_MGR				=  P + "UploadDownload" + M;
	
	/**
		ID for {@link com.sun.appserv.management.config.DomainConfig}
	 */
	public final static String	DOMAIN_CONFIG				=  P + "DomainConfig";
	
	/**
		@deprecated  no longer available in GlassFish V3
	 */
	public final static String	CUSTOM_MBEAN_CONFIG				=  P + "CustomMBeanConfig";
	
	/**
		ID for {@link com.sun.appserv.management.base.QueryMgr}
	 */
	public final static String	QUERY_MGR					=  P + "Query" + M;
	
	/**
		ID for {@link com.sun.appserv.management.base.BulkAccess}
	 */
	public final static String	BULK_ACCESS					=  P + "BulkAccess";
	
	/**
		ID for {@link com.sun.appserv.management.base.Sample}
	 */
	public final static String	SAMPLE					=  P + "Sample";
	
	/**
		ID for {@link com.sun.appserv.management.config.ConfigsConfig}
	 */
	public final static String	CONFIGS_CONFIG				=  P + "ConfigsConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ConfigConfig}
	 */
	public final static String	CONFIG_CONFIG				=  P + "ConfigConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ServerRefConfig}
	 */
	public final static String	SERVER_REF_CONFIG	=   P + "ServerRefConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ClusterRefConfig}
	 */
	public final static String	CLUSTER_REF_CONFIG	=   P + "ClusterRefConfig";

        /**
		ID for {@link com.sun.appserv.management.config.ClustersConfig}
	 */
	public final static String	CLUSTERS_CONFIG				=  P + "ClustersConfig";

        /**
		ID for {@link com.sun.appserv.management.config.ClusterConfig}
	 */
	public final static String	CLUSTER_CONFIG				=  P + "ClusterConfig";

	/**
		ID for {@link com.sun.appserv.management.config.ServersConfig}
	 */
	public final static String	SERVERS_CONFIG	=  P + "ServersConfig";

	/**
		ID for {@link com.sun.appserv.management.config.StandaloneServerConfig}
	 */
	public final static String	STANDALONE_SERVER_CONFIG	=  P + "StandaloneServerConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ClusteredServerConfig}
	 */
	public final static String	CLUSTERED_SERVER_CONFIG		=  P + "ClusteredServerConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.NodeAgentsConfig}
	 */
	public final static String	NODE_AGENTS_CONFIG			=  P + "NodeAgentsConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.NodeAgentConfig}
	 */
	public final static String	NODE_AGENT_CONFIG			=  P + "NodeAgentConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.PropertyConfig}
	 */
	public final static String	PROPERTY_CONFIG       =  P + "PropertyConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.SystemPropertyConfig}
	 */
	public final static String	SYSTEM_PROPERTY_CONFIG       =  P + "SystemPropertyConfig";
    
    
    
	/**
		ID for {@link com.sun.appserv.management.config.TransformationRuleConfig}
	 */
	public final static String	TRANSFORMATION_RULE_CONFIG	=  P + "TransformationRuleConfig";
		
	/**
		ID for {@link com.sun.appserv.management.config.WebServiceEndpointConfig}
	 */
	public final static String	WEB_SERVICE_ENDPOINT_CONFIG	=  P + "WebServiceEndpointConfig";
	
	
	
	/* other */
	
	/**
		ID for {@link com.sun.appserv.management.deploy.DeploymentMgr}
	 *
	public final static String	DEPLOYMENT_MGR				=  P + "Deployment" + M;
    */
	
	
	/* within a config */
	
	/**
		ID for {@link com.sun.appserv.management.config.ORBConfig}
	 */
	public final static String	ORB_CONFIG					=  P + "ORBConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ModuleMonitoringLevelsConfig}
	 */
	public final static String	MODULE_MONITORING_LEVELS_CONFIG	=  P + "ModuleMonitoringLevelsConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ModuleLogLevelsConfig}
	 */
	public final static String	MODULE_LOG_LEVELS_CONFIG	=  P + "ModuleLogLevelsConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.JavaConfig}
	 */
	public final static String	JAVA_CONFIG					=  P + "JavaConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ProfilerConfig}
	 */
	public final static String	PROFILER_CONFIG				=  P + "ProfilerConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.JACCProviderConfig}
	 */
	public final static String	APP_CLIENT_MODULE_CONFIG		=  P + "AppClientModuleConfig";
	
	
	/* services  */
	
	/**
		ID for {@link com.sun.appserv.management.config.AdminServiceConfig}
	 */
	public final static String	ADMIN_SERVICE_CONFIG		=  P + "AdminServiceConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.IIOPServiceConfig}
	 */
	public final static String	IIOP_SERVICE_CONFIG			=  P + "IIOPServiceConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.IIOPListenerConfig}
	 */
	public final static String	IIOP_LISTENER_CONFIG		=  P + "IIOPListenerConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.SSLConfig}
	 */
	public final static String	SSL_CONFIG	=  P + "SSLConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.HTTPServiceConfig}
	 */
	public final static String	HTTP_SERVICE_CONFIG			=  P + "HTTPServiceConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.HTTPListenerConfig}
	 */
	public final static String	HTTP_LISTENER_CONFIG		=  P + "HTTPListenerConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.VirtualServerConfig}
	 */
	public final static String	VIRTUAL_SERVER_CONFIG		=  P + "VirtualServerConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.SecurityServiceConfig}
	 */
	public final static String	SECURITY_SERVICE_CONFIG		=  P + "SecurityServiceConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.MonitoringServiceConfig}
	 */
	public final static String	MONITORING_SERVICE_CONFIG	=  P + "MonitoringServiceConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.JMSServiceConfig}
	 */
	public final static String	JMS_SERVICE_CONFIG			=  P + "JMSServiceConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.JMSHostConfig}
	 */
	public final static String	JMS_HOST_CONFIG				=  P + "JMSHostConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.JMSAvailabilityConfig}
	 */
	public final static String	JMS_AVAILABILITY_CONFIG				=  P + "JMSAvailabilityConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ThreadPoolsConfig}
	 */
	public final static String	THREAD_POOLS_CONFIG			=  P + "ThreadPoolsConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ThreadPoolConfig}
	 */
	public final static String	THREAD_POOL_CONFIG			=  P + "ThreadPoolConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.AvailabilityServiceConfig}
	 */
	public final static String	AVAILABILITY_SERVICE_CONFIG	=  P + "AvailabilityServiceConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.TransactionServiceConfig}
	 */
	public final static String	TRANSACTION_SERVICE_CONFIG	=  P + "TransactionServiceConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.LogServiceConfig}
	 */
	public final static String	LOG_SERVICE_CONFIG			=  P + "LogServiceConfig";
	
	
	
	/**
		ID for {@link com.sun.appserv.management.config.DASConfig}
	 */
	public final static String	DAS_CONFIG					=  P + "DASConfig";
	
	/* resources */
	/**
		ID for {@link com.sun.appserv.management.config.ResourceConfig}
	 */
	public final static String	RESOURCES_CONFIG		=  P + "ResourcesConfig";

	
	/**
		ID for {@link com.sun.appserv.management.config.MailResourceConfig}
	 */
	public final static String	MAIL_RESOURCE_CONFIG		=  P + "MailResourceConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.JNDIResourceConfig}
	 */
	public final static String	JNDI_RESOURCE_CONFIG		=  P + "JNDIResourceConfig";
	
	
	/**
		ID for {@link com.sun.appserv.management.config.JDBCResourceConfig}
	 */
	public final static String	JDBC_RESOURCE_CONFIG		=  P + "JDBCResourceConfig";
	
	
	/**
		ID for {@link com.sun.appserv.management.config.JDBCConnectionPoolConfig}
	 */
	public final static String	JDBC_CONNECTION_POOL_CONFIG	=  P + "JDBCConnectionPoolConfig";
	
	
	/**
		ID for {@link com.sun.appserv.management.config.PersistenceManagerFactoryResourceConfig}
	 */
	public final static String	PERSISTENCE_MANAGER_FACTORY_RESOURCE_CONFIG	=
									 P + "PersistenceManagerFactoryResourceConfig";
	
	
	/**
		ID for {@link com.sun.appserv.management.config.AdminObjectResourceConfig}
	 */
	public final static String	ADMIN_OBJECT_RESOURCE_CONFIG		=  P + "AdminObjectResourceConfig";
	
	
	/**
		ID for {@link com.sun.appserv.management.config.ResourceAdapterConfig}
	 */
	public final static String	RESOURCE_ADAPTER_CONFIG		=  P + "ResourceAdapterConfig";
	
	
	/**
		ID for {@link com.sun.appserv.management.config.CustomResourceConfig}
	 */
	public final static String	CUSTOM_RESOURCE_CONFIG		=  P + "CustomResourceConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ConnectorConnectionPoolConfig}
	 */
	public final static String	CONNECTOR_CONNECTION_POOL_CONFIG		=  P + "ConnectorConnectionPoolConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ConnectorResourceConfig}
	 */
	public final static String	CONNECTOR_RESOURCE_CONFIG		=  P + "ConnectorResourceConfig";
	
	
	/**
		ID for {@link com.sun.appserv.management.config.DeployedItemRefConfig}
	 */
	public final static String	DEPLOYED_ITEM_REF_CONFIG	=  P + "DeployedItemRefConfig";
		
	/**
		ID for {@link com.sun.appserv.management.config.ResourceRefConfig}
	 */
	public final static String	RESOURCE_REF_CONFIG	=  P + "ResourceRefConfig";
	
	
	
	/* j2ee */
	
	
	/**
		ID for {@link com.sun.appserv.management.config.MDBContainerConfig}
	 */
	public final static String	MDB_CONTAINER_CONFIG		=  P + "MDBContainerConfig";
	
	
	/**
		ID for {@link com.sun.appserv.management.config.WebContainerConfig}
	 */
	public final static String	WEB_CONTAINER_CONFIG		=  P + "WebContainerConfig";
	
	
	/**
		ID for {@link com.sun.appserv.management.config.SessionConfig}
	 */
	public final static String	SESSION_CONFIG		=  P + "SessionConfig";
	
    /**
            ID for {@link com.sun.appserv.management.config.ApplicationsConfig}
     */
    public final static String      APPLICATIONS_CONFIG                       =  P + "ApplicationsConfig";
	
    /**
            ID for {@link com.sun.appserv.management.config.ApplicationConfig}
     */
    public final static String      APPLICATION_CONFIG                       =  P + "ApplicationConfig";


    /**
            ID for {@link com.sun.appserv.management.config.ModuleConfig}
     */
    public final static String      MODULE_CONFIG                       =  P + "ModuleConfig";


    /**
            ID for {@link com.sun.appserv.management.config.EngineConfig}
     */
    public final static String      ENGINE_CONFIG                       =  P + "EngineConfig";

	
	/**
		@deprecated  no longer available in GlassFish V3
	 */
	public final static String	WEB_MODULE_CONFIG			=  P + "WebModuleConfig";
	
	
	/**
		@deprecated  no longer available in GlassFish V3
	 */
	public final static String	CONNECTOR_MODULE_CONFIG		=  P + "ConnectorModuleConfig";
	
	
	/**
		ID for {@link com.sun.appserv.management.config.EJBContainerConfig}
	 */
	public final static String	EJB_CONTAINER_CONFIG		=  P + "EJBContainerConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.EJBTimerServiceConfig}
	 */
	public final static String	EJB_TIMER_SERVICE_CONFIG		=  P + "EJBTimerServiceConfig";
	
	/**
		@deprecated  no longer available in GlassFish V3
	 */
	public final static String	EJB_MODULE_CONFIG			=  P + "EJBModuleConfig";
	
	
	/**
		@deprecated  no longer available in GlassFish V3
	 */
	public final static String	RAR_MODULE_CONFIG			=  P + "RARModuleConfig";
	
	
	/**
		@deprecated  no longer available in GlassFish V3
	 */
	public final static String	J2EE_APPLICATION_CONFIG		=  P + "J2EEApplicationConfig";
	
	
	/**
		@deprecated  no longer available in GlassFish V3
	 */
	public final static String	LIFECYCLE_MODULE_CONFIG		=  P + "LifecycleModuleConfig";
	
	
	/**
		@deprecated  no longer available in GlassFish V3
	 */
	public final static String	EXTENSION_MODULE_CONFIG		=  P + "ExtensionModuleConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.AuthRealmConfig}
	 */
	public final static String	AUTH_REALM_CONFIG			=  P + "AuthRealmConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.JACCProviderConfig}
	 */
	public final static String	JACC_PROVIDER_CONFIG		=  P + "JACCProviderConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.AuditModuleConfig}
	 */
	public final static String	AUDIT_MODULE_CONFIG			=  P + "AuditModuleConfig";

	/*
		ID for {@link com.sun.appserv.management.config.LBConfigsConfig}
	 */
	public final static String	LB_CONFIGS_CONFIG	=  P + "LBConfigs";

	/*
		ID for {@link com.sun.appserv.management.config.LBConfig}
	 */
	public final static String	LB_CONFIG	=  P + "LBConfig";

    /**
            ID for {@link com.sun.appserv.management.config.LoadBalancersConfig}
    */
    public final static String      LOAD_BALANCERS_CONFIG        =  P + "LoadBalancersConfig";

    /**
            ID for {@link com.sun.appserv.management.config.LoadBalancerConfig}
    */
    public final static String      LOAD_BALANCER_CONFIG        =  P + "LoadBalancerConfig";

	/**
		ID for {@link com.sun.appserv.management.ext.lb.LoadBalancer}
	 */
	public final static String	LOAD_BALANCER		=  P + "LoadBalancer";

	/*
		ID for {@link com.sun.appserv.management.config.HealthCheckerConfig}
	 */
	public final static String	HEALTH_CHECKER_CONFIG =  P + "HealthCheckerConfig";

	/*
		ID for {@link com.sun.appserv.management.config.LBClusterRefConfig}
	 */
	public final static String	LB_CLUSTER_REF_CONFIG =  P + "LBClusterRefConfig";
        
	/**
		ID for {@link com.sun.appserv.management.config.EJBContainerAvailabilityConfig}
	 */
	public final static String	EJB_CONTAINER_AVAILABILITY_CONFIG =  P + "EJBContainerAvailabilityConfig";

	/**
		ID for {@link com.sun.appserv.management.config.WebContainerAvailabilityConfig}
	 */
	public final static String	WEB_CONTAINER_AVAILABILITY_CONFIG =  P + "WebContainerAvailabilityConfig";

	/**
		ID for {@link com.sun.appserv.management.config.AccessLogConfig}
	 */
	public final static String	ACCESS_LOG_CONFIG =  P + "AccessLogConfig";

	/**
		ID for {@link com.sun.appserv.management.config.ConnectionPoolConfig}
	 */
	public final static String	CONNECTION_POOL_CONFIG =  P + "ConnectionPoolConfig";

	/**
		ID for {@link com.sun.appserv.management.config.RequestProcessingConfig}
	 */
	public final static String	REQUEST_PROCESSING_CONFIG =  P + "RequestProcessingConfig";

	/**
		ID for {@link com.sun.appserv.management.config.HTTPProtocolConfig}
	 */
	public final static String	HTTP_PROTOCOL_CONFIG =  P + "HTTPProtocolConfig";

	/**
		ID for {@link com.sun.appserv.management.config.HTTPFileCacheConfig}
	 */
	public final static String	HTTP_FILE_CACHE_CONFIG =  P + "HTTPFileCacheConfig";

	/**
		ID for {@link com.sun.appserv.management.config.KeepAliveConfig}
	 */
	public final static String	KEEP_ALIVE_CONFIG =  P + "KeepAliveConfig";

	/**
		ID for {@link com.sun.appserv.management.config.JMXConnectorConfig}
	 */
	public final static String	JMX_CONNECTOR_CONFIG =  P + "JMXConnectorConfig";

	/**
		ID for {@link com.sun.appserv.management.config.HTTPAccessLogConfig}
	 */
	public final static String	HTTP_ACCESS_LOG_CONFIG =  P + "HTTPAccessLogConfig";

	/**
		ID for {@link com.sun.appserv.management.config.ConnectorServiceConfig}
	 */
	public final static String	CONNECTOR_SERVICE_CONFIG =  P + "ConnectorServiceConfig";

	/**
		ID for {@link com.sun.appserv.management.config.SessionManagerConfig}
	 */
	public final static String	SESSION_MANAGER_CONFIG		=  P + "SessionManagerConfig";

	/**
		ID for {@link com.sun.appserv.management.config.SessionPropertiesConfig}
	 */
	public final static String	SESSION_PROPERTIES_CONFIG		=  P + "SessionPropertiesConfig";

	/**
		ID for {@link com.sun.appserv.management.config.ManagerPropertiesConfig}
	 */
	public final static String	MANAGER_PROPERTIES_CONFIG		=  P + "ManagerPropertiesConfig";

	/**
		ID for {@link com.sun.appserv.management.config.StorePropertiesConfig}
	 */
	public final static String	STORE_PROPERTIES_CONFIG		=  P + "StorePropertiesConfig";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.MonitoringRoot}
	 */
	public final static String	MONITORING_ROOT		=  P + "MonitoringRoot";
        
    /**
		ID for {@link com.sun.appserv.management.monitor.AMXGaugeMonitor}
	 */
	public final static String	JMX_GAUGE_MONITOR		=  P + "AMXGaugeMonitor";
        
    /**
		ID for {@link com.sun.appserv.management.monitor.AMXStringMonitor}
	 */
	public final static String	JMX_STRING_MONITOR		=  P + "AMXStringMonitor";
        
    /**
		ID for {@link com.sun.appserv.management.monitor.AMXCounterMonitor}
	 */
	public final static String	JMX_COUNTER_MONITOR		=  P + "AMXCounterMonitor";
	
	
        
	/**
		ID for {@link com.sun.appserv.management.monitor.ServerRootMonitor}
	 */
	public final static String	SERVER_ROOT_MONITOR		=  P + "ServerRootMonitor";
        

	/**
-------------------------------------------------------------------------------
LOAD_BALANCER_MONITORING
		ID for {@link com.sun.appserv.management.monitor.ServerRootMonitor}
	 *
	public final static String	LOAD_BALANCER_MONITOR		=  P + "LoadBalancerMonitor";

        
	/**
		ID for {@link com.sun.appserv.management.monitor.LoadBalancerServerMonitor}
	 *
	public final static String	LOAD_BALANCER_SERVER_MONITOR		=  P + "LoadBalancerServerMonitor";

        
	/**
		ID for {@link com.sun.appserv.management.monitor.LoadBalancerClusterMonitor}
	 *
	public final static String	LOAD_BALANCER_CLUSTER_MONITOR		=  P + "LoadBalancerClusterMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.LoadBalancerApplicationMonitor}
	 *
	public final static String	LOAD_BALANCER_APPLICATION_MONITOR	=  P + "LoadBalancerApplicationMonitor";
        
   /**
		ID for {@link com.sun.appserv.management.monitor.LoadBalancerContextRootMonitor}
	 *
	public final static String	LOAD_BALANCER_CONTEXT_ROOT_MONITOR	=  P + "LoadBalancerContextRootMonitor";
-------------------------------------------------------------------------------
    */
        
    /**
		ID for {@link com.sun.appserv.management.monitor.ApplicationMonitor}
	 */
	public final static String	APPLICATION_MONITOR	=  P + "ApplicationMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.EJBModuleMonitor}
	 */
	public final static String	EJB_MODULE_MONITOR		=  P + "EJBModuleMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.StatelessSessionBeanMonitor}
	 */
	public final static String	STATELESS_SESSION_BEAN_MONITOR		=  P + "StatelessSessionBeanMonitor";
	
	/**
		ID for {@link com.sun.appserv.management.monitor.StatefulSessionBeanMonitor}
	 */
	public final static String	STATEFUL_SESSION_BEAN_MONITOR		=  P + "StatefulSessionBeanMonitor";
	
	/**
		ID for {@link com.sun.appserv.management.monitor.EntityBeanMonitor}
	 */
	public final static String	ENTITY_BEAN_MONITOR		=  P + "EntityBeanMonitor";
	
	/**
		ID for {@link com.sun.appserv.management.monitor.MessageDrivenBeanMonitor}
	 */
	public final static String	MESSAGE_DRIVEN_BEAN_MONITOR		=  P + "MessageDrivenBeanMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.BeanPoolMonitor}
	 */
	public final static String	BEAN_POOL_MONITOR		=  P + "BeanPoolMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.BeanCacheMonitor}
	 */
	public final static String	BEAN_CACHE_MONITOR		=  P + "BeanCacheMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.BeanMethodMonitor}
	 */
	public final static String	BEAN_METHOD_MONITOR		=  P + "BeanMethodMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.ServletMonitor}
	 */
	public final static String	SERVLET_MONITOR                 =  P + "ServletMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.HTTPServiceMonitor}
	 */
	public final static String	HTTP_SERVICE_MONITOR	=  P + "HTTPServiceMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.FileCacheMonitor}
	 */
	public final static String	FILE_CACHE_MONITOR	=  P + "FileCacheMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.HTTPListenerMonitor}
	 */
	public final static String	HTTP_LISTENER_MONITOR		=  P + "HTTPListenerMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.HTTPServiceVirtualServerMonitor}
	 */
	public final static String	HTTP_SERVICE_VIRTUAL_SERVER_MONITOR	=  P + "HTTPServiceVirtualServerMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.WebModuleMonitor}
	 */
	//public final static String	WEB_MODULE_MONITOR	=  P + "WebModuleMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.WebModuleVirtualServerMonitor}
	 */
	public final static String	WEB_MODULE_VIRTUAL_SERVER_MONITOR	=  P + "WebModuleVirtualServerMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.JVMMonitor}
	 */
	public final static String	JVM_MONITOR                     =  P + "JVMMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.TransactionServiceMonitor}
	 */
	public final static String	TRANSACTION_SERVICE_MONITOR	=  P + "TransactionServiceMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.ThreadPoolMonitor}
	 */
	public final static String	THREAD_POOL_MONITOR     	=  P + "ThreadPoolMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.ConnectionManagerMonitor}
	 */
	public final static String	CONNECTION_MANAGER_MONITOR		=  P + "ConnectionManagerMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.JDBCConnectionPoolMonitor}
	 */
	public final static String	JDBC_CONNECTION_POOL_MONITOR		=  P + "JDBCConnectionPoolMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.ConnectorConnectionPoolMonitor}
	 */
	public final static String	CONNECTOR_CONNECTION_POOL_MONITOR	=  P + "ConnectorConnectionPoolMonitor";
	/**
		Deprecated, use {@link #CONNECTOR_CONNECTION_POOL_MONITOR} instead.
		@deprecated
	 */
	public final static String	CONNNECTOR_CONNECTION_POOL_MONITOR	=  CONNECTOR_CONNECTION_POOL_MONITOR;
        
	/**
		ID for {@link com.sun.appserv.management.monitor.ConnectionQueueMonitor}
	 */
	public final static String	CONNECTION_QUEUE_MONITOR       =  P + "ConnectionQueueMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.monitor.KeepAliveMonitor}
	 */
	public final static String	KEEP_ALIVE_MONITOR       =  P + "KeepAliveMonitor";
        
	/**
		ID for {@link com.sun.appserv.management.config.MessageSecurityConfig}
	 */
	public final static String	MESSAGE_SECURITY_CONFIG       =  P + "MessageSecurityConfig";

	/**
		ID for {@link com.sun.appserv.management.config.ProviderConfig}
	 */
	public final static String	PROVIDER_CONFIG       =  P + "ProviderConfig";

	/**
		ID for {@link com.sun.appserv.management.config.RequestPolicyConfig}
	 */
	public final static String	REQUEST_POLICY_CONFIG       =  P + "RequestPolicyConfig";

	/**
		ID for {@link com.sun.appserv.management.config.ResponsePolicyConfig}
	 */
	public final static String	RESPONSE_POLICY_CONFIG       =  P + "ResponsePolicyConfig";
	
	
	/**
		ID for {@link com.sun.appserv.management.monitor.JMXMonitorMgr}
	 */
	public final static String	JMX_MONITOR_MGR   =  P + "JMXMonitor" + M;

	/**
		ID for {@link com.sun.appserv.management.config.DiagnosticServiceConfig}
                @since AppServer 9.0
	 */
	public final static String	DIAGNOSTIC_SERVICE_CONFIG   =  P + "DiagnosticServiceConfig";
	/**
		ID for {@link com.sun.appserv.management.config.GroupManagementServiceConfig}
                @since AppServer 9.0
	 */
	public final static String	GROUP_MANAGEMENT_SERVICE_CONFIG   =  P + "GroupManagementServiceConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ManagementRuleConfig}
                @since AppServer 9.0
	 */
	public final static String	MANAGEMENT_RULE_CONFIG   =  P + "ManagementRuleConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.ManagementRulesConfig}
                @since AppServer 9.0
	 */
	public final static String	MANAGEMENT_RULES_CONFIG   =  P + "ManagementRulesConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.EventConfig}
                @since AppServer 9.0
	 */
	public final static String	EVENT_CONFIG   =  P + "EventConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.EventConfig}
                @since AppServer 9.0
	 */
	public final static String	ACTION_CONFIG   =  P + "ActionConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.FilterConfig}
                @since AppServer 9.0
	public final static String	FILTER_CONFIG   =  P + "FilterConfig";
	 */
	
	/**
		ID for {@link com.sun.appserv.management.config.AlertSubscriptionConfig}
                @since AppServer 9.0
	public final static String	ALERT_SUBSCRIPTION_CONFIG   =  P + "AlertSubscriptionConfig";
	 */
	
	/**
		ID for {@link com.sun.appserv.management.config.ListenerConfig}
                @since AppServer 9.0
	public final static String	LISTENER_CONFIG   =  P + "ListenerConfig";
	 */
	
	/**
		ID for {@link com.sun.appserv.management.config.RegistryLocationConfig}
                @since AppServer 9.0
	 */
	public final static String	REGISTRY_LOCATION_CONFIG   =  P + "RegistryLocationConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.SecurityMapConfig}
                @since AppServer 9.0
	 */
	public final static String	SECURITY_MAP_CONFIG   =  P + "SecurityMapConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.UserGroupConfig}
                @since AppServer 9.0
	public final static String	USER_GROUP_CONFIG   =  P + "UserGroupConfig";
	 */
	
	/**
		ID for {@link com.sun.appserv.management.config.BackendPrincipalConfig}
                @since AppServer 9.0
	 */
	public final static String	BACKEND_PRINCIPAL_CONFIG   =  P + "BackendPrincipalConfig";
	
	/**
		ID for {@link com.sun.appserv.management.config.PrincipalConfig}
                @since AppServer 9.0
	public final static String	PRINCIPAL_CONFIG   =  P + "PrincipalConfig";
	 */


	/**
		ID for {@link com.sun.appserv.management.ext.wsmgmt.WebServiceMgr}
	 */
	public final static String	WEB_SERVICE_MGR   =  P + "WebService" + M;

	/**
		ID for {@link com.sun.appserv.management.monitor.WebServiceEndpointMonitor}
	 */
	public final static String	WEBSERVICE_ENDPOINT_MONITOR       =  P + "WebServiceEndpointMonitor";

        
}
