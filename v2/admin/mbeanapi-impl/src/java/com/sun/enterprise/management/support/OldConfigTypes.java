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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;

import javax.management.ObjectName;

import com.sun.appserv.management.base.XTypes;
import static com.sun.appserv.management.base.XTypes.*;

import com.sun.appserv.management.util.misc.GSetUtil;

 
/**
	Maps an AMX j2eeType to/from and "old" (8.0) config type.
	
	See {@link com.sun.appserv.management.base.XTypes}
 */
public final class OldConfigTypes extends OldTypesBase
{
	private static OldConfigTypes	INSTANCE	= new OldConfigTypes();
	
		private
	OldConfigTypes()
	{
		super();
	}
	
		public static OldConfigTypes
	getInstance()
	{
		return( INSTANCE );
	}
	

		 void
	initMap()
	{       
		add( DOMAIN_CONFIG, "domain");
		add( CUSTOM_MBEAN_CONFIG, "mbean");
		add( NODE_AGENT_CONFIG, "node-agent");
		
		// type, config
		add( IIOP_SERVICE_CONFIG, "iiop-service");
		add( HTTP_SERVICE_CONFIG, "http-service");
		add( SECURITY_SERVICE_CONFIG, "security-service");
		add( MONITORING_SERVICE_CONFIG, "monitoring-service");
		add( ADMIN_SERVICE_CONFIG, "admin-service");
		add( WEB_CONTAINER_CONFIG, "web-container");
		add( EJB_CONTAINER_CONFIG, "ejb-container");
		add( EJB_TIMER_SERVICE_CONFIG, "ejb-timer-service");
		add( MDB_CONTAINER_CONFIG, "mdb-container");
		add( AVAILABILITY_SERVICE_CONFIG, "availability-service");
		add( JAVA_CONFIG, "java-config");
		add( PROFILER_CONFIG, "profiler");
		add( JMS_SERVICE_CONFIG, "jms-service");
		add( LOG_SERVICE_CONFIG, "log-service");
		add( TRANSACTION_SERVICE_CONFIG, "transaction-service");
		add( MODULE_MONITORING_LEVELS_CONFIG, "module-monitoring-levels");
		add( MODULE_LOG_LEVELS_CONFIG, "module-log-levels");
		add( DAS_CONFIG, "das-config");
		add( ORB_CONFIG, "orb");
		
        add( MANAGEMENT_RULES_CONFIG, "management-rules" );
        add( MANAGEMENT_RULE_CONFIG, "management-rule" );
        add( EVENT_CONFIG, "event" );
        add( ACTION_CONFIG, "action" );
        add( REGISTRY_LOCATION_CONFIG, "registry-location" );
        
        add( SECURITY_MAP_CONFIG, "security-map" );
        add( BACKEND_PRINCIPAL_CONFIG, "backend-principal" );
		
		// type, name
		add( J2EE_APPLICATION_CONFIG, "j2ee-application");
		add( WEB_MODULE_CONFIG, "web-module");
		add( EJB_MODULE_CONFIG, "ejb-module");
		add( APP_CLIENT_MODULE_CONFIG, "appclient-module" );
		add( RAR_MODULE_CONFIG, "connector-module");
		add( LIFECYCLE_MODULE_CONFIG, "lifecycle-module");
		add( CONFIG_CONFIG, "config");
		add( STANDALONE_SERVER_CONFIG, "server");
                add( CLUSTER_CONFIG, "cluster");

		// type, resources, name
		add( JNDI_RESOURCE_CONFIG, "external-jndi-resource");
		add( JDBC_CONNECTION_POOL_CONFIG, "jdbc-connection-pool");
		add( JDBC_RESOURCE_CONFIG, "jdbc-resource");
		add( CUSTOM_RESOURCE_CONFIG, "custom-resource");
		add( ADMIN_OBJECT_RESOURCE_CONFIG, "admin-object-resource");
		add( MAIL_RESOURCE_CONFIG, "mail-resource");
		add( PERSISTENCE_MANAGER_FACTORY_RESOURCE_CONFIG, "persistence-manager-factory-resource");
		add( CONNECTOR_RESOURCE_CONFIG, "connector-resource");
		add( CONNECTOR_CONNECTION_POOL_CONFIG, "connector-connection-pool");
		add( RESOURCE_ADAPTER_CONFIG, "resource-adapter-config");
                
		// type, config, name");
		add(  AUDIT_MODULE_CONFIG, "audit-module");
		add(  AUTH_REALM_CONFIG, "auth-realm");
		add(  JACC_PROVIDER_CONFIG, "jacc-provider");
		add(  JMS_HOST_CONFIG, "jms-host");
		add(  JMS_AVAILABILITY_CONFIG, "jms-availability");
		
		add( DIAGNOSTIC_SERVICE_CONFIG, "diagnostic-service" );
		add( GROUP_MANAGEMENT_SERVICE_CONFIG, "group-management-service" );
		
		// type, config, id
		add(  HTTP_LISTENER_CONFIG, "http-listener");
		add(  IIOP_LISTENER_CONFIG, "iiop-listener");
		add(  VIRTUAL_SERVER_CONFIG, "virtual-server");
		
		// type, thread-pool-id, config
		add(  THREAD_POOL_CONFIG, "thread-pool");
		
		add(  DEPLOYED_ITEM_REF_CONFIG, "application-ref");
		add(  RESOURCE_REF_CONFIG, "resource-ref");
                add(  SERVER_REF_CONFIG, "server-ref");
                add(  CLUSTER_REF_CONFIG, "cluster-ref");
                add(  HEALTH_CHECKER_CONFIG, "health-checker");
		add(  SSL_CONFIG, "ssl");

		add(  EJB_CONTAINER_AVAILABILITY_CONFIG, "ejb-container-availability");
		add(  WEB_CONTAINER_AVAILABILITY_CONFIG, "web-container-availability");

		add(  ACCESS_LOG_CONFIG, "access-log");
		add(  KEEP_ALIVE_CONFIG, "keep-alive");
		add(  CONNECTION_POOL_CONFIG, "connection-pool");
		add(  REQUEST_PROCESSING_CONFIG, "request-processing");
		add(  HTTP_PROTOCOL_CONFIG, "http-protocol");
		add(  HTTP_FILE_CACHE_CONFIG, "http-file-cache");

		add(  JMX_CONNECTOR_CONFIG, "jmx-connector");

		add(  HTTP_ACCESS_LOG_CONFIG, "http-access-log");

		add(  CONNECTOR_SERVICE_CONFIG, "connector-service");

		add(  SESSION_CONFIG, "session-config");
		add(  SESSION_MANAGER_CONFIG, "session-manager");
		add(  SESSION_PROPERTIES_CONFIG, "session-properties");
		add(  MANAGER_PROPERTIES_CONFIG, "manager-properties");
		add(  STORE_PROPERTIES_CONFIG, "store-properties");

		add(  MESSAGE_SECURITY_CONFIG, "message-security-config");
		add(  PROVIDER_CONFIG, "provider-config");
		add(  REQUEST_POLICY_CONFIG, "request-policy");
		add(  RESPONSE_POLICY_CONFIG, "response-policy");

		add(  LB_CONFIG, "lb-config");
                add(  LOAD_BALANCER_CONFIG, "load-balancer");
		//add(  IIOP_SSL_CLIENT_CONFIG, "ssl-client-config");
		
		add(  WEB_SERVICE_ENDPOINT_CONFIG, "web-service-endpoint");
		add(  TRANSFORMATION_RULE_CONFIG, "transformation-rule");
	}

	
	/**
		Do not attempt to create corresponding "new" mbeans for these types
	 */
	private static final Set<String> IGNORE_TYPES	= Collections.unmodifiableSet(
		GSetUtil.newSet( new String[]
		{
			"applications",
			"configs",
			"resources",
			"clusters",
			"servers",
			"thread-pools",
			"node-agents",
			"synchronization",
			"transactions-recovery",	// not actually config
			"ejb-timer-management", 	// not actually config
			"transaction-service-manager", 	// not actually config
			"lb-configs",
			"load-balancers",
			"properties",
			"password-alias",
			"olbadmin",
            "status",   // not actually config
			
			// when used via config API, these are also relevant
			"element-property",
			"system-property",
		} )
	);
	
	
		public static Set<String>
	getIgnoreTypes()
	{
		return( IGNORE_TYPES );
	}
	
	
}








