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
 
package com.sun.enterprise.management.support;


import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.*; 
import com.sun.appserv.management.monitor.*;

import com.sun.appserv.management.base.*;

import com.sun.appserv.management.ext.wsmgmt.WebServiceMgr;
import com.sun.appserv.management.ext.update.UpdateStatus;
import com.sun.appserv.management.ext.logging.Logging;
import com.sun.appserv.management.ext.lb.LoadBalancer;
/*
import com.sun.appserv.management.monitor.LoadBalancerContextRootMonitor;
import com.sun.appserv.management.monitor.LoadBalancerClusterMonitor;
import com.sun.appserv.management.monitor.LoadBalancerServerMonitor;
import com.sun.appserv.management.monitor.LoadBalancerMonitor;
*/
import com.sun.appserv.management.deploy.DeploymentMgr;

import com.sun.appserv.management.util.misc.TimingDelta;


import com.sun.enterprise.management.support.*;
import com.sun.enterprise.management.monitor.*;
import com.sun.enterprise.management.ext.logging.LoggingImpl;

/**
	Map all types from XTypes to their respective MBean interfaces.
 */
public final class XTypesMapper extends TypesMapper
{
	private static XTypesMapper	INSTANCE	= null;
	
	/**
	    Get the (singleton) instance.
	 */
		public static synchronized XTypesMapper
	getInstance()
	{
		if ( INSTANCE == null )
		{
        final TimingDelta delta = new TimingDelta();
			INSTANCE	= new XTypesMapper();
        System.out.println( "XTypesMapper.getInstance(): " + delta.elapsedMillis() );
		}
		
		return( INSTANCE );
	}

		private
	XTypesMapper()
	{
		super( MBEAN_INTERFACES );
	}
	
	/**
		The classes for which we need mapping (all MBeans must be included)
	 */
	private static final Object[] MBEAN_INTERFACES	=
		{
		DomainRoot.J2EE_TYPE, DomainRoot.class,
		SystemInfo.J2EE_TYPE, SystemInfo.class,
		NotificationEmitterService.J2EE_TYPE, NotificationEmitterService.class,
		Logging.J2EE_TYPE, Logging.class,
		NotificationService.J2EE_TYPE, NotificationService.class,
		NotificationServiceMgr.J2EE_TYPE, NotificationServiceMgr.class,
		DomainConfig.J2EE_TYPE, DomainConfig.class,
		CustomMBeanConfig.J2EE_TYPE, CustomMBeanConfig.class,
		UploadDownloadMgr.J2EE_TYPE, UploadDownloadMgr.class,
		QueryMgr.J2EE_TYPE, QueryMgr.class,
		BulkAccess.J2EE_TYPE, BulkAccess.class,
		Sample.J2EE_TYPE, Sample.class,
		ConfigConfig.J2EE_TYPE, ConfigConfig.class,
		ClusterConfig.J2EE_TYPE, ClusterConfig.class,

		StandaloneServerConfig.J2EE_TYPE, StandaloneServerConfig.class,

		ClusteredServerConfig.J2EE_TYPE, ClusteredServerConfig.class,
		NodeAgentConfig.J2EE_TYPE, NodeAgentConfig.class,

		ConfigDottedNames.J2EE_TYPE, ConfigDottedNames.class,

		DeploymentMgr.J2EE_TYPE, DeploymentMgr.class,

		ORBConfig.J2EE_TYPE, ORBConfig.class,
		ModuleMonitoringLevelsConfig.J2EE_TYPE, ModuleMonitoringLevelsConfig.class,
		ModuleLogLevelsConfig.J2EE_TYPE, ModuleLogLevelsConfig.class,
		JavaConfig.J2EE_TYPE, JavaConfig.class,
		ProfilerConfig.J2EE_TYPE, ProfilerConfig.class,
		AppClientModuleConfig.J2EE_TYPE, AppClientModuleConfig.class,

		AdminServiceConfig.J2EE_TYPE, AdminServiceConfig.class,
		IIOPServiceConfig.J2EE_TYPE, IIOPServiceConfig.class,
		IIOPListenerConfig.J2EE_TYPE, IIOPListenerConfig.class,
		SSLConfig.J2EE_TYPE, SSLConfig.class,
		HTTPServiceConfig.J2EE_TYPE, HTTPServiceConfig.class,
		HTTPListenerConfig.J2EE_TYPE, HTTPListenerConfig.class,
		VirtualServerConfig.J2EE_TYPE, VirtualServerConfig.class,
		SecurityServiceConfig.J2EE_TYPE, SecurityServiceConfig.class,
		JACCProviderConfig.J2EE_TYPE, JACCProviderConfig.class,
		AuthRealmConfig.J2EE_TYPE, AuthRealmConfig.class,
		AuditModuleConfig.J2EE_TYPE, AuditModuleConfig.class,
		MonitoringServiceConfig.J2EE_TYPE, MonitoringServiceConfig.class,
		JMSServiceConfig.J2EE_TYPE, JMSServiceConfig.class,
		JMSHostConfig.J2EE_TYPE, JMSHostConfig.class,
		JMSAvailabilityConfig.J2EE_TYPE, JMSAvailabilityConfig.class,
		ThreadPoolConfig.J2EE_TYPE, ThreadPoolConfig.class,
		AvailabilityServiceConfig.J2EE_TYPE, AvailabilityServiceConfig.class,
		TransactionServiceConfig.J2EE_TYPE, TransactionServiceConfig.class,
		LogServiceConfig.J2EE_TYPE, LogServiceConfig.class,
		GroupManagementServiceConfig.J2EE_TYPE, GroupManagementServiceConfig.class,
		DiagnosticServiceConfig.J2EE_TYPE, DiagnosticServiceConfig.class,

		DASConfig.J2EE_TYPE, DASConfig.class,

		MailResourceConfig.J2EE_TYPE, MailResourceConfig.class,
		JNDIResourceConfig.J2EE_TYPE, JNDIResourceConfig.class,

		JDBCResourceConfig.J2EE_TYPE, JDBCResourceConfig.class,

		JDBCConnectionPoolConfig.J2EE_TYPE, JDBCConnectionPoolConfig.class,

		PersistenceManagerFactoryResourceConfig.J2EE_TYPE, PersistenceManagerFactoryResourceConfig.class,

		AdminObjectResourceConfig.J2EE_TYPE, AdminObjectResourceConfig.class,

		ResourceAdapterConfig.J2EE_TYPE, ResourceAdapterConfig.class,

		CustomResourceConfig.J2EE_TYPE, CustomResourceConfig.class,

		ConnectorConnectionPoolConfig.J2EE_TYPE, ConnectorConnectionPoolConfig.class,

		ConnectorResourceConfig.J2EE_TYPE, ConnectorResourceConfig.class,

		DeployedItemRefConfig.J2EE_TYPE, DeployedItemRefConfig.class,

		ResourceRefConfig.J2EE_TYPE, ResourceRefConfig.class,

		ServerRefConfig.J2EE_TYPE, ServerRefConfig.class,
                        
        LoadBalancer.J2EE_TYPE, LoadBalancer.class,                                          
        LoadBalancerConfig.J2EE_TYPE, LoadBalancerConfig.class,
		HealthCheckerConfig.J2EE_TYPE, HealthCheckerConfig.class,
        ClusterRefConfig.J2EE_TYPE, ClusterRefConfig.class,
		LBConfig.J2EE_TYPE, LBConfig.class,
		
        MDBContainerConfig.J2EE_TYPE, MDBContainerConfig.class,

		WebContainerConfig.J2EE_TYPE, WebContainerConfig.class,

		SessionConfig.J2EE_TYPE, SessionConfig.class,
		SessionManagerConfig.J2EE_TYPE, SessionManagerConfig.class,
		SessionPropertiesConfig.J2EE_TYPE, SessionPropertiesConfig.class,
		ManagerPropertiesConfig.J2EE_TYPE, ManagerPropertiesConfig.class,
		StorePropertiesConfig.J2EE_TYPE, StorePropertiesConfig.class,

		WebModuleConfig.J2EE_TYPE, WebModuleConfig.class,

		ConnectorModuleConfig.J2EE_TYPE, ConnectorModuleConfig.class,

		EJBContainerConfig.J2EE_TYPE, EJBContainerConfig.class,
		EJBTimerServiceConfig.J2EE_TYPE, EJBTimerServiceConfig.class,
		EJBModuleConfig.J2EE_TYPE, EJBModuleConfig.class,

		RARModuleConfig.J2EE_TYPE, RARModuleConfig.class,

		J2EEApplicationConfig.J2EE_TYPE, J2EEApplicationConfig.class,

		LifecycleModuleConfig.J2EE_TYPE, LifecycleModuleConfig.class,
		ExtensionModuleConfig.J2EE_TYPE, ExtensionModuleConfig.class,
		        
		EJBContainerAvailabilityConfig.J2EE_TYPE, EJBContainerAvailabilityConfig.class,
		WebContainerAvailabilityConfig.J2EE_TYPE, WebContainerAvailabilityConfig.class,

		AccessLogConfig.J2EE_TYPE, AccessLogConfig.class,
		RequestProcessingConfig.J2EE_TYPE, RequestProcessingConfig.class,
		HTTPProtocolConfig.J2EE_TYPE, HTTPProtocolConfig.class,
		HTTPFileCacheConfig.J2EE_TYPE, HTTPFileCacheConfig.class,
		KeepAliveConfig.J2EE_TYPE, KeepAliveConfig.class,
		ConnectionPoolConfig.J2EE_TYPE, ConnectionPoolConfig.class,

		JMXConnectorConfig.J2EE_TYPE, JMXConnectorConfig.class,

		HTTPAccessLogConfig.J2EE_TYPE, HTTPAccessLogConfig.class,

		ConnectorServiceConfig.J2EE_TYPE, ConnectorServiceConfig.class,

		RequestPolicyConfig.J2EE_TYPE, RequestPolicyConfig.class,
		ResponsePolicyConfig.J2EE_TYPE, ResponsePolicyConfig.class,
		ProviderConfig.J2EE_TYPE, ProviderConfig.class,
		MessageSecurityConfig.J2EE_TYPE, MessageSecurityConfig.class,

		/* monitoring mbean interfaces */
		MonitoringDottedNames.J2EE_TYPE, MonitoringDottedNames.class,
		MonitoringRoot.J2EE_TYPE, MonitoringRoot.class,
		JMXMonitorMgr.J2EE_TYPE, JMXMonitorMgr.class,
		AMXStringMonitor.J2EE_TYPE, AMXStringMonitor.class,
		AMXCounterMonitor.J2EE_TYPE, AMXCounterMonitor.class,
		AMXGaugeMonitor.J2EE_TYPE, AMXGaugeMonitor.class,
		
		ServerRootMonitor.J2EE_TYPE, ServerRootMonitor.class,
		
    /*
        LoadBalancerMonitor.J2EE_TYPE, LoadBalancerMonitor.class,
        LoadBalancerServerMonitor.J2EE_TYPE, LoadBalancerServerMonitor.class,
        LoadBalancerClusterMonitor.J2EE_TYPE, LoadBalancerClusterMonitor.class,
        LoadBalancerApplicationMonitor.J2EE_TYPE, LoadBalancerApplicationMonitor.class,
        LoadBalancerContextRootMonitor.J2EE_TYPE, LoadBalancerContextRootMonitor.class,
    */
        
		CallFlowMonitor.J2EE_TYPE, CallFlowMonitor.class,
                        
		JVMMonitor.J2EE_TYPE, JVMMonitor.class,            
		TransactionServiceMonitor.J2EE_TYPE, TransactionServiceMonitor.class,
		ApplicationMonitor.J2EE_TYPE, ApplicationMonitor.class,
		BeanCacheMonitor.J2EE_TYPE, BeanCacheMonitor.class,
		BeanMethodMonitor.J2EE_TYPE, BeanMethodMonitor.class,
		BeanPoolMonitor.J2EE_TYPE, BeanPoolMonitor.class, 
		ConnectionManagerMonitor.J2EE_TYPE, ConnectionManagerMonitor.class, 
		
		WebModuleVirtualServerMonitor.J2EE_TYPE, WebModuleVirtualServerMonitor.class,
		//WebModuleMonitor.J2EE_TYPE, WebModuleMonitor.class,
		ServletMonitor.J2EE_TYPE, ServletMonitor.class,
		ConnectorConnectionPoolMonitor.J2EE_TYPE, ConnectorConnectionPoolMonitor.class,
		EJBModuleMonitor.J2EE_TYPE, EJBModuleMonitor.class,               
		StatelessSessionBeanMonitor.J2EE_TYPE, StatelessSessionBeanMonitor.class,    
		StatefulSessionBeanMonitor.J2EE_TYPE, StatefulSessionBeanMonitor.class,      
		EntityBeanMonitor.J2EE_TYPE, EntityBeanMonitor.class,      
		MessageDrivenBeanMonitor.J2EE_TYPE, MessageDrivenBeanMonitor.class,               
		HTTPListenerMonitor.J2EE_TYPE, HTTPListenerMonitor.class,         
		ThreadPoolMonitor.J2EE_TYPE, ThreadPoolMonitor.class,
		HTTPServiceMonitor.J2EE_TYPE, HTTPServiceMonitor.class,   
		FileCacheMonitor.J2EE_TYPE, FileCacheMonitor.class,      
		JDBCConnectionPoolMonitor.J2EE_TYPE, JDBCConnectionPoolMonitor.class,  
		HTTPServiceVirtualServerMonitor.J2EE_TYPE, HTTPServiceVirtualServerMonitor.class,
		KeepAliveMonitor.J2EE_TYPE, KeepAliveMonitor.class,
		ConnectionQueueMonitor.J2EE_TYPE, ConnectionQueueMonitor.class,

        WebServiceMgr.J2EE_TYPE, WebServiceMgr.class,
        WebServiceEndpointMonitor.J2EE_TYPE, WebServiceEndpointMonitor.class,
        
        ManagementRuleConfig.J2EE_TYPE, ManagementRuleConfig.class,
        ManagementRulesConfig.J2EE_TYPE, ManagementRulesConfig.class,
        WebServiceEndpointConfig.J2EE_TYPE, WebServiceEndpointConfig.class,
        TransformationRuleConfig.J2EE_TYPE, TransformationRuleConfig.class,
        SecurityMapConfig.J2EE_TYPE, SecurityMapConfig.class,
        EventConfig.J2EE_TYPE, EventConfig.class,
        ActionConfig.J2EE_TYPE, ActionConfig.class,
        BackendPrincipalConfig.J2EE_TYPE, BackendPrincipalConfig.class,
        RegistryLocationConfig.J2EE_TYPE, RegistryLocationConfig.class,

        UpdateStatus.J2EE_TYPE, UpdateStatus.class,
	};
	
}
