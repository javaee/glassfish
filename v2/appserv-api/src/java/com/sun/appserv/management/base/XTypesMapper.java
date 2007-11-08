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


import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.*; 
import com.sun.appserv.management.monitor.*;

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
			INSTANCE	= new XTypesMapper();
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
	private static final Class[] MBEAN_INTERFACES	=
		{
		DomainRoot.class,
		SystemInfo.class,
		NotificationEmitterService.class,
		Logging.class,
		NotificationService.class,
		NotificationServiceMgr.class,
		DomainConfig.class,
		CustomMBeanConfig.class,
		UploadDownloadMgr.class,
		QueryMgr.class,
		BulkAccess.class,
		Sample.class,
		ConfigConfig.class,
		ClusterConfig.class,

		StandaloneServerConfig.class,

		ClusteredServerConfig.class,
		NodeAgentConfig.class,

		ConfigDottedNames.class,

		DeploymentMgr.class,

		ORBConfig.class,
		ModuleMonitoringLevelsConfig.class,
		ModuleLogLevelsConfig.class,
		JavaConfig.class,
		ProfilerConfig.class,
		AppClientModuleConfig.class,

		AdminServiceConfig.class,
		IIOPServiceConfig.class,
		IIOPListenerConfig.class,
		SSLConfig.class,
		HTTPServiceConfig.class,
		HTTPListenerConfig.class,
		VirtualServerConfig.class,
		SecurityServiceConfig.class,
		JACCProviderConfig.class,
		AuthRealmConfig.class,
		AuditModuleConfig.class,
		MonitoringServiceConfig.class,
		JMSServiceConfig.class,
		JMSHostConfig.class,
		JMSAvailabilityConfig.class,
		ThreadPoolConfig.class,
		AvailabilityServiceConfig.class,
		TransactionServiceConfig.class,
		LogServiceConfig.class,
		GroupManagementServiceConfig.class,
		DiagnosticServiceConfig.class,

		DASConfig.class,

		MailResourceConfig.class,
		JNDIResourceConfig.class,

		JDBCResourceConfig.class,

		JDBCConnectionPoolConfig.class,

		PersistenceManagerFactoryResourceConfig.class,

		AdminObjectResourceConfig.class,

		ResourceAdapterConfig.class,

		CustomResourceConfig.class,

		ConnectorConnectionPoolConfig.class,

		ConnectorResourceConfig.class,

		DeployedItemRefConfig.class,

		ResourceRefConfig.class,

		ServerRefConfig.class,
                        
                LoadBalancer.class,                                          
                LoadBalancerConfig.class,
		HealthCheckerConfig.class,
                ClusterRefConfig.class,
		LBConfig.class,
		
                MDBContainerConfig.class,

		WebContainerConfig.class,

		SessionConfig.class,
		SessionManagerConfig.class,
		SessionPropertiesConfig.class,
		ManagerPropertiesConfig.class,
		StorePropertiesConfig.class,

		WebModuleConfig.class,

		ConnectorModuleConfig.class,

		EJBContainerConfig.class,
		EJBTimerServiceConfig.class,
		EJBModuleConfig.class,

		RARModuleConfig.class,

		J2EEApplicationConfig.class,

		LifecycleModuleConfig.class,
		        
		EJBContainerAvailabilityConfig.class,
		WebContainerAvailabilityConfig.class,

		AccessLogConfig.class,
		RequestProcessingConfig.class,
		HTTPProtocolConfig.class,
		HTTPFileCacheConfig.class,
		KeepAliveConfig.class,
		ConnectionPoolConfig.class,

		JMXConnectorConfig.class,

		HTTPAccessLogConfig.class,

		ConnectorServiceConfig.class,

		RequestPolicyConfig.class,
		ResponsePolicyConfig.class,
		ProviderConfig.class,
		MessageSecurityConfig.class,

		/* monitoring mbean interfaces */
		MonitoringDottedNames.class,
		MonitoringRoot.class,
		JMXMonitorMgr.class,
		AMXStringMonitor.class,
		AMXCounterMonitor.class,
		AMXGaugeMonitor.class,
		
		ServerRootMonitor.class,
		
    /*
        LoadBalancerMonitor.class,
        LoadBalancerServerMonitor.class,
        LoadBalancerClusterMonitor.class,
        LoadBalancerApplicationMonitor.class,
        LoadBalancerContextRootMonitor.class,
    */
        
		CallFlowMonitor.class,
                        
		JVMMonitor.class,            
		TransactionServiceMonitor.class,
		ApplicationMonitor.class,
		BeanCacheMonitor.class,
		BeanMethodMonitor.class,
		BeanPoolMonitor.class, 
		ConnectionManagerMonitor.class, 
		
		WebModuleVirtualServerMonitor.class,
		//WebModuleMonitor.class,
		ServletMonitor.class,
		ConnectorConnectionPoolMonitor.class,
		EJBModuleMonitor.class,               
		StatelessSessionBeanMonitor.class,    
		StatefulSessionBeanMonitor.class,      
		EntityBeanMonitor.class,      
		MessageDrivenBeanMonitor.class,               
		HTTPListenerMonitor.class,         
		ThreadPoolMonitor.class,
		HTTPServiceMonitor.class,   
		FileCacheMonitor.class,      
		JDBCConnectionPoolMonitor.class,  
		HTTPServiceVirtualServerMonitor.class,
		KeepAliveMonitor.class,
		ConnectionQueueMonitor.class,

        WebServiceMgr.class,
        WebServiceEndpointMonitor.class,
        
        ManagementRuleConfig.class,
        ManagementRulesConfig.class,
        WebServiceEndpointConfig.class,
        TransformationRuleConfig.class,
        SecurityMapConfig.class,
        EventConfig.class,
        ActionConfig.class,
        BackendPrincipalConfig.class,
        RegistryLocationConfig.class,

        UpdateStatus.class,
	};
	
}
