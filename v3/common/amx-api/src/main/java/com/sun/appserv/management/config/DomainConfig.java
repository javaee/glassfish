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


package com.sun.appserv.management.config;

import java.util.Map;
import java.util.List;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.DomainRoot;


/**
	 Configuration for the &lt;domain&gt; element.
	<p>
	All configuration resides in a tree rooted at this .
*/

public interface DomainConfig
	extends PropertiesAccess, SystemPropertiesAccess,
	ConfigElement, Container, ConfigRemover
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.DOMAIN_CONFIG;

    /**
        Create a new &lt;server&gt; given an existing config and node-agent.
        These are required parameters for the server instance to be created.

        @param name the name of the server to create
        @param nodeAgentName the node agent that the server will reference
        @param configName the config that the server will reference
        @param optional properties for this new server
          This is a Map object consisting of key/value for a given property, that can be applied to
          this server instance. The Map may also contain additional properties that can be applied to this server instance.
          <p>Note that Properties that relate to ports of listeners are stored as system-properties and have
          specific key names and must be specified to override values defined in the config to any of the relevant
          ports - this is particularly required when the instance being created is on the same machine as other
          instances in the domain.
          <p>Legal property keys are those found in {@link ServerConfigKeys}.

        @return A proxy to the StandaloneServerConfig MBean that manages the newly created server
     */
    public StandaloneServerConfig createStandaloneServerConfig(String name, String nodeAgentName,
            String configName, Map<String,String> optional);


	

	/**
		Calls Container.getContaineeMap( XTypes.NODE_AGENT_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,NodeAgentConfig>		getNodeAgentConfigMap();
	
	/**
		Calls Container.getContaineeMap( XTypes.CONFIG_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,ConfigConfig>		getConfigConfigMap();
	
	/**
		Create a new &lt;config&gt; element. Legal keys in the optional Map include:
		<ul>
		<li>{@link ConfigConfigKeys#DYNAMIC_RECONFIGURATION_ENABLED_KEY}</li>
		<li>{@link ConfigConfigKeys#SRC_CONFIG_NAME_KEY}</li>
		</ul>
		<p>
		A new config is created by copying an existing one.  Unless a specific
		config is specified via {@link ConfigConfigKeys#SRC_CONFIG_NAME_KEY},
		the default config as given by {@link ConfigConfigKeys#DEFAULT_SRC_CONFIG_NAME}
		will be copied.
		<p>
		Depending on the context in which the config is to be used, it may or may 
		not conflict with values found in other configs.

		@param name		name of the &lt;config>
		@param optional		optional attributes for config creation
		@return Returns a proxy to the ConfigConfig MBean.
		@see ConfigConfigKeys
	*/
	public ConfigConfig	createConfigConfig( String name, Map<String,String> optional );

	/**
		Remove a config.  This will fail if any <server> or <cluster>
		refers to it.

		@param name The config name.
	*/
	public void		removeConfigConfig( String name );
	
	/**
		Calls Container.getContaineeMap( XTypes.STANDALONE_SERVER_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,StandaloneServerConfig>		getStandaloneServerConfigMap();
	
	/**
		Calls Container.getContaineeMap( XTypes.CLUSTERED_SERVER_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,ClusteredServerConfig>		getClusteredServerConfigMap();
	
	public void		removeStandaloneServerConfig( String name );
	public void		removeClusteredServerConfig( String name );
	               
	/**
	 * Creates a new &lt;server&gt; that belongs to a cluster.

     @param name			Name of the server.
     @param nodeAgentName	Name of the node agent that should manage this instance
     @param clusterName	    Name of the cluster to which this server should belong.
        <p>Note that it is prefereable to pass in an existing nodeagent's name. A non-existent nodeagent name can be
        passed in but this nodeagent's hostname attribute will be marked as "localhost" as an assumption is made that the
        nodeagent is local. The nodeagent should be created through the create-node-agent command
        using the Command Line Interface(CLI) on the machine where this instance is intended to reside after this create()
        operation. Prior to starting this instance, that nodeagent will have to be started using the CLI command
        start-node-agent.
     @param optional Attributes and properties for this new server.
       <p>Note that Properties that relate to ports of listeners are stored as system-properties and have
       specific key names and must be specified to override values defined in the config to any of the relevant
       ports - this is particularly required when the instance being created is on the same machine as other
       instances in the domain.
       <p>Legal keys are those defined in {@link ServerConfigKeys}.

     @return	A proxy to the ClusteredServerConfig MBean.
	 */
	public ClusteredServerConfig createClusteredServerConfig(String name, 
            String clusterName, String nodeAgentName,
            java.util.Map<String,String> optional);
	
	/**
		Combines the results of getStandaloneServerConfigMap() and
		getClusteredServerConfigMap().
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,ServerConfig>		getServerConfigMap();

	/**
		Calls Container.getContaineeMap( XTypes.CLUSTER_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,ClusterConfig>	getClusterConfigMap();
	
        /**
		Create a new ClusterConfig.  The 'referencedConfigName' must be non-null
		and must not be "default-config" or "server-config".  If it is desired
		to create a new ClusterConfig which uses a copy of default-config,
		use the createClusterConfig( name, reserved ) form.

		@param name the name of the cluster to create.
		@param referencedConfigName the non-null name of the config to reference.
		@param optional	optional values, properties only

		@return a ClusterConfig
	 */
	public ClusterConfig createClusterConfig(String name, String referencedConfigName,
                Map<String,String> optional);
                
	/**
            Create a new ClusterConfig which refers to a copy of the default-config.

            @param name the name of the cluster to create.
            @param optional	optional values, properties only

            @return a ClusterConfig
	 */ 
	public ClusterConfig createClusterConfig(String name, Map<String,String> optional);

	/**
          Remove an existing &lt;cluster&gt;.
          @param name the name of the cluster to remove.
	 */
	public void removeClusterConfig(String name);
	
	/**
          Calls Container.getContaineeMap( XTypes.LOAD_BALANCER_CONFIG).
          @return Map of items, keyed by name.
          @see com.sun.appserv.management.base.Container#getContaineeMap
	*/
        public Map<String,LoadBalancerConfig> getLoadBalancerConfigMap();

        /**
          Create a new LoadBalancer.  The 'lbConfigName' and 'name' must be non-null.
          @param name the name of the load balancer to create
          @param lbConfigName the non-null name of the lb config to reference.
          @param autoApplyEnabled flag to indicate if the LB changes are pushed 
                 immediately to the physical load balancer. Defaults to false
          @param optional optional values, properties only
            <b> The known properties are </b>
              <ul>
              <li>
                property.device-host - Host name or IP address for the device
              </li>
            <li>
                property.device-admin-port - Device administration port number
                </li>
            <li>
                property.ssl-proxy-host - proxy host used for outbound HTTP
                </li>
            <li>
                property.ssl-proxy-port - proxy port used for outbound HTTP
                </li>
            </ul>
          @return a LoadBalancer

          @see LoadBalancerConfig
	*/
	public LoadBalancerConfig createLoadBalancerConfig(String name, String lbConfigName, 
                boolean autoApplyEnabled, Map<String,String> optional);

        /**
          Remove an existing &lt;LoadBalancerConfig&gt;.
          @param name the name of the load-balancer to remove.
	*/
	public void removeLoadBalancerConfig(String name);

        /**
          Calls Container.getContaineeMap( XTypes.LB_CONFIG ).
          @return Map of items, keyed by name.
          @see com.sun.appserv.management.base.Container#getContaineeMap
        */
        public Map<String,LBConfig> getLBConfigMap();

        /**
          Creates a new lb-config.  Legal options include:
          <ul>
             <li>{@link LBConfigKeys#RESPONSE_TIMEOUT_IN_SECONDS_KEY}</li>
             <li>{@link LBConfigKeys#HTTPS_ROUTING_KEY}</li>
             <li>{@link LBConfigKeys#RELOAD_POLL_INTERVAL_IN_SECONDS_KEY}</li>
             <li>{@link LBConfigKeys#MONITORING_ENABLED_KEY}</li>
             <li>{@link LBConfigKeys#ROUTE_COOKIE_ENABLED_KEY}</li>
          </ul>
          @param name The name of the load balancer configuration.
          @param params Remaining attributes for creation of a new lb-config.
          @return A proxy to the LBConfig MBean that manages the newly created lb-config.
          @see LBConfigKeys
         */
        public LBConfig createLBConfig(String name, Map<String,String> params);

        /**
          Removes a lb-config.
          @param name The name of the load balancer configuration.
         */
        public void removeLBConfig(String name);

	
	public String	getApplicationRoot();
	public void		setApplicationRoot( final String value );

	public String	getLocale();
	public void		setLocale( final String value );

	public String	getLogRoot();
	public void		setLogRoot( final String value );
	
	
	//---------------------------------------------------------------------------------------
	/**
		Calls Container.getContaineeMap( XTypes.CUSTOM_RESOURCE_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	 
	public Map<String,CustomResourceConfig>	getCustomResourceConfigMap();
	

	/**
		Creates a new custom resource.	
		<ul>
		<li>{@link ResourceConfigKeys#ENABLED_KEY}</li>
		</ul>

		@param jndiName
		@param resType
		@param factoryClass
		@param optional optional Attributes (may be null )
		@return A proxy to the  CustomResourceConfig MBean.
	 */
	public CustomResourceConfig createCustomResourceConfig( String jndiName,
	                                            String resType,
	                                            String factoryClass,
	                                            Map<String,String> optional );


	/**
		Removes a custom resource.

		@param name The name of the custom resource. 
	 */
	public void	removeCustomResourceConfig( String name );


	
	/**
		Calls Container.getContaineeMap( XTypes.JNDI_RESOURCE_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,JNDIResourceConfig>	getJNDIResourceConfigMap();
	
	/**
		Creates a new &lt;external-jndi-resource&gt;. Optional values include:
		<ul>
		<li>{@link ResourceConfigKeys#ENABLED_KEY}</li>
		</ul>

		@param jndiName
		@param jndiLookupName
		@param resType
		@param factoryClass
		@param optional optional Attributes (may be null )
		@return a JNDIResourceConfig
	*/
	public JNDIResourceConfig       createJNDIResourceConfig( String    jndiName,
	                                String    jndiLookupName, 
	                                String    resType,
	                                String    factoryClass,
	                                Map<String,String> optional);

	/**
		Remove the &lt;external-jndi-resource&gt;.

		@param jndiName The jndi name of the external jndi resource
		to be removed.
	*/
	public void		removeJNDIResourceConfig( String jndiName );

	
	/**
		Calls Container.getContaineeMap( XTypes.PERSISTENCE_MANAGER_FACTORY_RESOURCE_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,PersistenceManagerFactoryResourceConfig>
	    getPersistenceManagerFactoryResourceConfigMap();
	
	
	/**
		Create a new persistence manager factory resource. Optional values include:
		<ul>
		<li>{@link PersistenceManagerFactoryResourceConfigKeys#FACTORY_CLASS_KEY}</li>
		<li>{@link PersistenceManagerFactoryResourceConfigKeys#JDBC_RESOURCE_JNDI_NAME_KEY}</li>
		<li>{@link ResourceConfigKeys#ENABLED_KEY}</li>
		</ul>

		@param jndiName
		@param optional optional Attributes (may be null )
		@return A proxy to the PersistenceManagerFactoryResourceConfig MBean that
		manages the newly created resource.
		@see PersistenceManagerFactoryResourceConfigKeys
	*/
	public PersistenceManagerFactoryResourceConfig
		createPersistenceManagerFactoryResourceConfig( String jndiName, Map<String,String> optional);


	/**
		Removes a persistence manager factory resource.
		 
		@param jndiName
	*/
	public void		removePersistenceManagerFactoryResourceConfig( String jndiName );

	
	/**
		Calls Container.getContaineeMap( XTypes.JDBC_RESOURCE_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,JDBCResourceConfig>	getJDBCResourceConfigMap();
	
	/**
		Create a new &lt;jdbc-resource&gt;  Optional parameters include:
		<ul>
		<li>{@link ResourceConfigKeys#ENABLED_KEY}</li>
		</ul>

		@param jndiName
		@param poolName
		@param optional optional Attributes (may be null )
		@return	A proxy to the JDBCResourceConfig MBean that manages the newly
		created jdbc-resource.
	*/
	public JDBCResourceConfig	createJDBCResourceConfig( String jndiName,
                                String poolName,
                                Map<String,String> optional );
        
	/**     
		Removes a jdbc resource.

		@param jndiName
	 */
	public void             removeJDBCResourceConfig( String jndiName );
	
	
	/**
		Calls Container.getContaineeMap( XTypes.JDBC_CONNECTION_POOL_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,JDBCConnectionPoolConfig>	getJDBCConnectionPoolConfigMap();
	
		/**
		Create a new &lt;jdbc-connection-pool&gt;.
		
		@param name								name of the &lt;jdbc-connection-pool>
		@param	connectionValidationMethod
		@param	datasourceClassname
		@param	failAllConnections
		@param	idleTimeoutSeconds
		@param	connectionValidationRequired
		@param	isolationLevelGuaranteed
		@param	transactionIsolationLevel
		@param	maxPoolSize
		@param	maxWaitTimeMillis
		@param	poolResizeQuantity
		@param	resType
		@param	steadyPoolSize
		@param	databaseName
		@param	databaseUserName
		@param	databasePassword
		@param	reservedForFutureUse		reserved for future use
		@return a JDBCConnectionPoolConfig
	 */
    @AMXCreateInfo(paramNames={
        "name", 
        "connectionValidationMethod",
        "datasource-clasname",
        "failAllConnections",
        "idleTimeoutSeconds",
        "connectionValidationRequired",
        "isolationLevelGuaranteed",
        "transactionIsolationLevel",
        "maxPoolSize",
        "maxWaitTimeMillis",
        "poolResizeQuantity",
        "resType",
        "steadyPoolSize",
        "databaseName",
        "databaseUserName",
        "databasePassword",
        "reserved"})
	public JDBCConnectionPoolConfig	createJDBCConnectionPoolConfig(
                            String name,
							String connectionValidationMethod,
							String datasourceClassname,
							boolean	failAllConnections,
							int		idleTimeoutSeconds,
							boolean	connectionValidationRequired,
							boolean	isolationLevelGuaranteed,
							String	transactionIsolationLevel,
							int		maxPoolSize,
							int		maxWaitTimeMillis,
							int		poolResizeQuantity,
							String	resType,
							int		steadyPoolSize,
							String	databaseName,
							String	databaseUserName,
							String	databasePassword,
							Map<String,String> reserved );
	    
	/**
		Create a new &lt;jdbc-connection-pool>.  Legal optional attributes include:
		
	<ul>
	<li>{@link JDBCConnectionPoolConfigKeys#CONNECTION_VALIDATION_METHOD_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#VALIDATION_TABLE_NAME_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#FAIL_ALL_CONNECTIONS_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#IDLE_TIMEOUT_IN_SECONDS_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#IS_CONNECTION_VALIDATION_REQUIRED_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#IS_ISOLATION_LEVEL_GUARANTEED_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#TRANSACTION_ISOLATION_LEVEL_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#MAX_POOL_SIZE_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#MAX_WAIT_TIME_MILLIS_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#POOL_RESIZE_QUANTITY_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#RES_TYPE_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#STEADY_POOL_SIZE_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#DATABASE_NAME_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#DATABASE_USER_KEY}</li>
	<li>{@link JDBCConnectionPoolConfigKeys#DATABASE_PASSWORD_KEY}</li>
	</ul>

		@param name					name of the &lt;jdbc-connection-pool>
		@param datasourceClassname
		@param optional				optional parameters
		@return a JDBCConnectionPoolConfig
	*/
    @AMXCreateInfo(paramNames={"name","datasource-clasname", "optional"})
	public JDBCConnectionPoolConfig       createJDBCConnectionPoolConfig(  String name, 
	                            String datasourceClassname, Map<String,String> optional);

	/**
		Remove the &lt;jdbc-connection-pool&gt;.
		
		@param jdbcConnectionPoolName 
	 */
	public void		removeJDBCConnectionPoolConfig( String jdbcConnectionPoolName );
	
	
	/**
		Calls Container.getContaineeMap( XTypes.CONNECTOR_RESOURCE_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,ConnectorResourceConfig>	getConnectorResourceConfigMap();
	
	/**
		Creates a new &lt;connector-resource&gt;
		Legal optional keys include:
		<ul>
		<li>ResourceConfigKeys#ENABLED_KEY</li>
		</ul>

		@param jndiName
		@param poolName
		@param optional	optional parameters (may be null).
	*/
	public ConnectorResourceConfig createConnectorResourceConfig( String   jndiName,
                           String   poolName,
                           Map<String,String> optional );
	
        
	/**
	Removes a connector resource.

	@param jndiName
	*/
	public void removeConnectorResourceConfig( String jndiName );
	
	
	/**
		Calls Container.getContaineeMap( XTypes.CONNECTOR_CONNECTION_POOL_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,ConnectorConnectionPoolConfig>	getConnectorConnectionPoolConfigMap();
	
	/**
	Creates a new &lt;connector-connection-pool&gt;
	Valid keys in optional map include:
	<ul>
	<li>{@link ConnectorConnectionPoolConfigKeys#STEADY_POOL_SIZE_KEY}</li>
	<li>{@link ConnectorConnectionPoolConfigKeys#MAX_POOL_SIZE_KEY}</li>
	<li>{@link ConnectorConnectionPoolConfigKeys#MAX_WAIT_TIME_IN_MILLIS_KEY}</li>
	<li>{@link ConnectorConnectionPoolConfigKeys#POOL_RESIZE_QUANTITY_KEY}</li>
	<li>{@link ConnectorConnectionPoolConfigKeys#IDLE_TIMEOUT_IN_SECONDS_KEY}</li>
	<li>{@link ConnectorConnectionPoolConfigKeys#FAIL_ALL_CONNECTIONS_KEY}</li>
	<li>{@link ConnectorConnectionPoolConfigKeys#TRANSACTION_SUPPORT_KEY}</li>
	<li>{@link ConnectorConnectionPoolConfigKeys #TRANSACTION_SUPPORT_KEY}</li>
	</ul>

	@param name						pool name.
	@param resourceAdapterName			
	@param connectionDefinitionName	unique name, identifying one 
	connection-definition in a Resource Adapter.
	@param optional
	@return A proxy to the ConnectorConnectionPoolConfig MBean
	that manages the newly created connector-connection-pool element.
	@see ConnectorConnectionPoolConfigKeys
	*/
	public ConnectorConnectionPoolConfig createConnectorConnectionPoolConfig( String	name,
	         String	resourceAdapterName,
	         String	connectionDefinitionName,
	         Map<String,String> optional );

	/**
		Removes a connector connection pool.

		@param name	pool name.
	*/
	public void removeConnectorConnectionPoolConfig( String name );
	
	/**
		Calls Container.getContaineeMap( XTypes.ADMIN_OBJECT_RESOURCE_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,AdminObjectResourceConfig>	getAdminObjectResourceConfigMap();
	
	/**
		Creates a new &lt;admin-object-resource&gt;.
		<ul>
		<li>{@link ResourceConfigKeys#ENABLED_KEY}</li>
		</ul>

		@param jndiName	JNDI name of the resource.
		@param resType		
		@param resAdapter	Name of the inbound resource adapter.
		@param optional optional Attributes (may be null )
		@return			A proxy to the AdminObjectResourceConfig MBean.
	 */
	public AdminObjectResourceConfig	createAdminObjectResourceConfig( String jndiName,
                               String resType,
                               String resAdapter,
                               Map<String,String> optional );
	
	/**
		Removes an admin object resource.

		@param jndiName	JNDI name of the resource.
	 */
	public void             removeAdminObjectResourceConfig( String jndiName );
	
	
	/**
		Calls Container.getContaineeMap( XTypes.RESOURCE_ADAPTER_CONFIG ).
		@return Map of AMXs, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,ResourceAdapterConfig>	getResourceAdapterConfigMap();
	
	/**
		Legal options include:
		<ul>
		<li>{@link ResourceAdapterConfigKeys#THREAD_POOL_IDS_KEY}</li>
		</ul>
	 */
	public ResourceAdapterConfig	createResourceAdapterConfig( String resourceAdapterName, Map<String,String> optional );
	public void			removeResourceAdapterConfig( String resourceAdapterName );
	
	
	/**
		Calls Container.getContaineeMap( XTypes.MAIL_RESOURCE_CONFIG ).
		@return Map of s, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,MailResourceConfig>	getMailResourceConfigMap();
	
	/**
		Create a new &lt;mail-resource>. Optional keys are:
		<ul>
		<li>{@link MailResourceConfigKeys#STORE_PROTOCOL_KEY}</li>
		<li>{@link MailResourceConfigKeys#STORE_PROTOCOL_CLASS_KEY}</li>
		<li>{@link MailResourceConfigKeys#TRANSPORT_PROTOCOL_KEY}</li>
		<li>{@link MailResourceConfigKeys#TRANSPORT_PROTOCOL_CLASS_KEY}</li>
		<li>{@link MailResourceConfigKeys#DEBUG_KEY}</li>
		</ul>


		@param jndiName			
		@param host
		@param user
		@param from
		@param optional
		@return A  MailResourceConfig.
		@see MailResourceConfigKeys
	*/
	public MailResourceConfig	createMailResourceConfig( String          jndiName,
	                    String          host,
	                    String          user,
	                    String          from,
	                    Map<String,String> optional);

	/**
		Removes a mail resource.

		@param jndiName
	*/                
	public void             removeMailResourceConfig( String jndiName );
	
	/**
        Glassfish V3 prefers {@link #getApplicationConfigMap}.
        
		@return Map, keyed by name of {@link J2EEApplicationConfig}
		@see #getEJBModuleConfigMap
		@see #getWebModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
        @deprecated
	 */
	public Map<String,J2EEApplicationConfig>			getJ2EEApplicationConfigMap();
    
    
	/**
		@return Map, keyed by name of {@link J2EEApplicationConfig}
		@see #getEJBModuleConfigMap
		@see #getWebModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
		@see #getJ2EEApplicationConfigMap
        @since Appserver V3
	 */
	public Map<String,ApplicationConfig>    getApplicationConfigMap();
	
	
	/**
		@return Map, keyed by name of {@link EJBModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
	 */
	public Map<String,EJBModuleConfig>			getEJBModuleConfigMap( );
	
	/**
		@return Map, keyed by name of {@link WebModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getEJBModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
	 */
	public Map<String,WebModuleConfig>			getWebModuleConfigMap( );
	
	/**
		@return Map, keyed by name of {@link RARModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getEJBModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
	 */
	public Map<String,RARModuleConfig>			getRARModuleConfigMap();
	
	/**
		@return Map, keyed by name of {@link AppClientModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getEJBModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
	 */
	public Map<String,AppClientModuleConfig>			getAppClientModuleConfigMap();
	
	/**
		@return Map, keyed by name of {@link LifecycleModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getEJBModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
	 */
	public Map<String,LifecycleModuleConfig>			getLifecycleModuleConfigMap();
	
	/**
		@return Map, keyed by name of {@link LifecycleModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getEJBModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getConnectorModuleConfigMap
	 */
	public Map<String,ExtensionModuleConfig>			getExtensionModuleConfigMap();
    
	/**
		@return Map, keyed by name of {@link ExtensionModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getEJBModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
	    @since AppServer 9.1.1
	 */
	public DiagnosticServiceConfig getDiagnosticServiceConfig();
    
	
	/**
		@return Map, keyed by name of {@link J2EEApplicationConfig}
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getEJBModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getExtensionModuleConfigMap
	 */
	public Map<String,ConnectorModuleConfig> getConnectorModuleConfigMap();
	
	/**
		Create a new lifecycle module.  A Lifecycle Module must
		implement the interface
		<code>com.sun.appserv.server.LifecycleListener</code>,
		which is outside the scope of AMX, see the product
		documentation.
		<p>
		The 'loadOrder' parameter must be a positive integer value
		(eg >= 1) can be used to force the order in which    
        deployed lifecycle modules are loaded at server start up.     
        Smaller numbered modules get loaded sooner. Order is          
        unspecified if two or more lifecycle modules have the same    
        load-order value.
		<p>                                  
        If 'isFailureFatal' is true,server startup will fail when
        this module does not load properly.                                                   

		@param name the name for the new lifecycle module
		@param description optional description
		@param classname the classname associated with this lifecycle module
		@param classpath optional additioinal classpath
		@param loadOrder integer value to force loading order of LifecycleModules
		@param isFailureFatal if true, server startup will fail when
		        this module does not load properly.   
		@param enabled  whether to load the module at startup
		@return a LifecycleModuleConfig
	*/
	public LifecycleModuleConfig	createLifecycleModuleConfig( String name,
	                            String description,
	                            String classname,
	                            String classpath, 
	                            String loadOrder,
	                            boolean	isFailureFatal,
	                            boolean enabled,
	                            Map<String,String>		reserved );

	/**
		Removes an existing lifecycle module.
		    
		@param name the name of the lifecycle module to be removed.
	*/
	public void			removeLifecycleModuleConfig( String name );
	

	/**
		Get a ResourceConfig of any kind.
		@param name
	 */
	public ResourceConfig				getResourceConfig( String name );

    /**
		Create a new {@link CustomMBeanConfig}.
		The 'implClassname' must specify a valid classname. If invalid,
		the CustomMBeanConfig will still be created, but of course the MBean
		will not be loaded.
		<p>
		Any number of properties may be included by adding them to the
		Map 'optional'. See {@link PropertiesAccess} for details.
		<p>
		See {@link CustomMBeanConfig} for details on valid values
		for the 'objectNameProperties' parameter, and for details on
		the ObjectName with which the MBean will be registered.
		<p>
        <b>Questions</b>
        <ul>
        <li>
            Where do you put the jar file for the mbean so that it can
            be loaded?
        </li>
        <li>
            What is the behavior if the user
            creates a CustomMBeanConfig specifying 'objectName' with the following:<br>
            "", "user:", "name=foo", "amx:name=foo", "name=foo,type=bar",":".
            <p>
            What are the resulting ObjectNames produced by the above, and how 
            are they obtained?
        </li>
        </ul>

		@param name the display name, will be the name used in the ObjectName 'name' property
		@param implClassname    the implementing class
		@param objectName the partial ObjectName used when registering the MBean
		new module
		@param enabled whether the MBean should load
		@param reserved reserved
	*/
	public CustomMBeanConfig    createCustomMBeanConfig(
                        	        String name,
                        	        String implClassname,
                        	        String objectName,
                        	        boolean enabled,
                        	        Map<String,String> reserved );
                        	        
    /** 
        Remove a CustomMBeanConfig.  All references to it are also removed.
        <p>
        <b>Questions</b>
        <ul>
        <li>Are running MBeans first stopped?</li>
        </ul>
        @param name    name as returned by {@link CustomMBeanConfig#getName}
     */
	public void                  removeCustomMBeanConfig( String name );
	    
	
	/**
		@return Map, keyed by name of {@link CustomMBeanConfig}.
	 */
	public Map<String,CustomMBeanConfig>    getCustomMBeanConfigMap();
	
	/**
        @param j2eeType the j2eeType of any {@link AMXConfig} (all items having a group of
        {@link AMX#GROUP_CONFIG}. See {@link XTypes}.
        
		@return a Map whose keys are the <b>domain.xml</b> names,
          and whose values are the default value for that attribute.  Not all Attributes
          have default values available.
	 */
	public Map<String,String>	getDefaultAttributeValues( final String j2eeType );
}







