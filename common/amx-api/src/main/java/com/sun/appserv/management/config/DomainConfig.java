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

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Singleton;
import com.sun.appserv.management.base.XTypes;

import java.util.Map;


/**
	 Configuration for the &lt;domain&gt; element.
	<p>
	All configuration resides in a tree rooted at this .
*/

public interface DomainConfig
	extends PropertiesAccess, SystemPropertiesAccess,
	ConfigElement, Container, ConfigRemover, DefaultValues, Singleton
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.DOMAIN_CONFIG;
	

	/**
		Calls Container.getContaineeMap( XTypes.NODE_AGENT_CONFIG ).
		@return Map of items, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.NODE_AGENTS_CONFIG )
	public Map<String,NodeAgentConfig>		getNodeAgentConfigMap();
    
	/** @since Glassfish V3 */
	public ResourcesConfig          getResourcesConfig();
    
	/** @since Glassfish V3 */
	public ConfigsConfig            getConfigsConfig();
    
	/** @since Glassfish V3 */
	public ApplicationsConfig       getApplicationsConfig();
    
	/** @since Glassfish V3 */
	public ServersConfig            getServersConfig();
    
	/** @since Glassfish V3 */
	public ClustersConfig           getClustersConfig();
    
	/** @since Glassfish V3 */
	public LoadBalancersConfig		getLoadBalancersConfig();
    
	/** @since Glassfish V3 */
	public LBConfigsConfig          getLBConfigsConfig();
    
	/** @since Glassfish V3 */
	public NodeAgentsConfig         getNodeAgentsConfig();
	
	/**
        @deprecated use {@link ConfigsConfig#getConfigConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.CONFIGS_CONFIG )
	public Map<String,ConfigConfig>		getConfigConfigMap();
	
	/**
        @deprecated use {@link ConfigsConfig#getConfigConfigMap}
    */
    @AMXForwardTo( containeeJ2EEType=XTypes.CONFIGS_CONFIG )
	public ConfigConfig	createConfigConfig( String name, Map<String,String> optional );

	/**
        @deprecated use {@link ConfigsConfig#getConfigConfigMap}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.CONFIGS_CONFIG )
	public void		removeConfigConfig( String name );

    /**
        @deprecated use {@link ServersConfig#createStandaloneServerConfig}
     */
    @AMXForwardTo( containeeJ2EEType=XTypes.SERVERS_CONFIG )
    public StandaloneServerConfig createStandaloneServerConfig(String name, String nodeAgentName,
            String configName, Map<String,String> optional);


	/**
        @deprecated use {@link ServersConfig#getStandaloneServerConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.SERVERS_CONFIG )
	public Map<String,StandaloneServerConfig>		getStandaloneServerConfigMap();
	
	/**
        @deprecated use {@link ServersConfig#getClusteredServerConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.SERVERS_CONFIG )
	public Map<String,ClusteredServerConfig>		getClusteredServerConfigMap();
	
	/**
        @deprecated use {@link ServersConfig#removeStandaloneServerConfig}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.SERVERS_CONFIG )
	public void		removeStandaloneServerConfig( String name );
    
	/**
        @deprecated use {@link ServersConfig#removeClusteredServerConfig}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.SERVERS_CONFIG )
	public void		removeClusteredServerConfig( String name );
	               
	/**
        @deprecated use {@link ServersConfig#createClusteredServerConfig}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.SERVERS_CONFIG )
	public ClusteredServerConfig createClusteredServerConfig(String name, 
            String clusterName, String nodeAgentName,
            java.util.Map<String,String> optional);
	
	/**
        @deprecated use {@link ServersConfig#getServerConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.SERVERS_CONFIG )
	public Map<String,ServerConfig>		getServerConfigMap();

	/**
        @deprecated use {@link ClustersConfig#getClusterConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.CLUSTERS_CONFIG )
	public Map<String,ClusterConfig>	getClusterConfigMap();
	
    /**
        @deprecated use {@link ClustersConfig#createClusterConfig}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.CLUSTERS_CONFIG )
	public ClusterConfig createClusterConfig(String name, String referencedConfigName, Map<String,String> optional);
                
	/**
        @deprecated use {@link ClustersConfig#createClusterConfig}
	 */ 
    @AMXForwardTo( containeeJ2EEType=XTypes.CLUSTERS_CONFIG )
	public ClusterConfig createClusterConfig(String name, Map<String,String> optional);

	/**
        @deprecated use {@link ClustersConfig#removeClusterConfig}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.CLUSTERS_CONFIG )
	public void removeClusterConfig(String name);
	
	/**
        @deprecated use {@link LoadBalancerConfigs#getLoadBalancerConfigMap}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.LOAD_BALANCERS_CONFIG )
    public Map<String,LoadBalancerConfig> getLoadBalancerConfigMap();

    /**
        @deprecated use {@link LoadBalancerConfigs#createLoadBalancerConfig}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.LOAD_BALANCERS_CONFIG )
	public LoadBalancerConfig createLoadBalancerConfig(String name, String lbConfigName, 
                boolean autoApplyEnabled, Map<String,String> optional);

    /**
        @deprecated use {@link LoadBalancerConfigs#removeLoadBalancerConfig}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.LOAD_BALANCERS_CONFIG )
	public void removeLoadBalancerConfig(String name);

    /**
        @deprecated use {@link LBConfigsConfig#getLBConfigMap}
    */
    @AMXForwardTo( containeeJ2EEType=XTypes.LB_CONFIGS_CONFIG )
    public Map<String,LBConfig> getLBConfigMap();

    /**
        @deprecated use {@link LBConfigsConfig#createLBConfig}
    */
    @AMXForwardTo( containeeJ2EEType=XTypes.LB_CONFIGS_CONFIG )
    public LBConfig createLBConfig(String name, Map<String,String> params);

    /**
        @deprecated use {@link LBConfigsConfig#removeLBConfig}
     */
    @AMXForwardTo( containeeJ2EEType=XTypes.LB_CONFIGS_CONFIG )
    public void removeLBConfig(String name);

	//---------------------------------------------------------------------------------------
	
	public String	getApplicationRoot();
	public void		setApplicationRoot( final String value );

	public String	getLocale();
	public void		setLocale( final String value );

	public String	getLogRoot();
	public void		setLogRoot( final String value );
	
	
	//---------------------------------------------------------------------------------------
    


	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public Map<String,CustomResourceConfig>	getCustomResourceConfigMap();
	

	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public CustomResourceConfig createCustomResourceConfig( String jndiName,
	                                            String resType,
	                                            String factoryClass,
	                                            Map<String,String> optional );


	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public void	removeCustomResourceConfig( String name );


	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public Map<String,JNDIResourceConfig>	getJNDIResourceConfigMap();
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public JNDIResourceConfig       createJNDIResourceConfig( String    jndiName,
	                                String    jndiLookupName, 
	                                String    resType,
	                                String    factoryClass,
	                                Map<String,String> optional);

	/**
        @deprecated use {@link Resources#METHOD_NAME}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public void		removeJNDIResourceConfig( String jndiName );

	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public Map<String,PersistenceManagerFactoryResourceConfig>
	    getPersistenceManagerFactoryResourceConfigMap();
	
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public PersistenceManagerFactoryResourceConfig
		createPersistenceManagerFactoryResourceConfig( String jndiName, Map<String,String> optional);


	/**
        @deprecated use {@link Resources#METHOD_NAME}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public void		removePersistenceManagerFactoryResourceConfig( String jndiName );

	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public Map<String,JDBCResourceConfig>	getJDBCResourceConfigMap();
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public JDBCResourceConfig	createJDBCResourceConfig( String jndiName,
                                String poolName,
                                Map<String,String> optional );
        
	/**     
		Removes a jdbc resource.

		@param jndiName
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public void             removeJDBCResourceConfig( String jndiName );
	
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public Map<String,JDBCConnectionPoolConfig>	getJDBCConnectionPoolConfigMap();
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
    */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
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
        @deprecated use {@link Resources#METHOD_NAME}
    */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public JDBCConnectionPoolConfig       createJDBCConnectionPoolConfig(  String name, 
	                            String datasourceClassname, Map<String,String> optional);

	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public void		removeJDBCConnectionPoolConfig( String jdbcConnectionPoolName );
	
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public Map<String,ConnectorResourceConfig>	getConnectorResourceConfigMap();
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public ConnectorResourceConfig createConnectorResourceConfig( String   jndiName,
                           String   poolName,
                           Map<String,String> optional );
	
        
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public void removeConnectorResourceConfig( String jndiName );
	
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public Map<String,ConnectorConnectionPoolConfig>	getConnectorConnectionPoolConfigMap();
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public ConnectorConnectionPoolConfig createConnectorConnectionPoolConfig( String	name,
	         String	resourceAdapterName,
	         String	connectionDefinitionName,
	         Map<String,String> optional );

	/**
        @deprecated use {@link Resources#METHOD_NAME}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public void removeConnectorConnectionPoolConfig( String name );
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public Map<String,AdminObjectResourceConfig>	getAdminObjectResourceConfigMap();
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public AdminObjectResourceConfig	createAdminObjectResourceConfig( String jndiName,
                               String resType,
                               String resAdapter,
                               Map<String,String> optional );
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public void             removeAdminObjectResourceConfig( String jndiName );
	
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public Map<String,ResourceAdapterConfig>	getResourceAdapterConfigMap();
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
     */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public ResourceAdapterConfig	createResourceAdapterConfig( String resourceAdapterName, Map<String,String> optional );
    
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public void			removeResourceAdapterConfig( String resourceAdapterName );
	
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public Map<String,MailResourceConfig>	getMailResourceConfigMap();
	
	/**
        @deprecated use {@link Resources#METHOD_NAME}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public MailResourceConfig	createMailResourceConfig( String          jndiName,
	                    String          host,
	                    String          user,
	                    String          from,
	                    Map<String,String> optional);

	/**
        @deprecated use {@link Resources#METHOD_NAME}
	*/                
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public void             removeMailResourceConfig( String jndiName );
    

//-------------------------------------------------------------------------------------------
	
	/**
        @deprecated use {@link ApplicationsConfig#getJ2EEApplicationConfigMap}
        
     */
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public Map<String,J2EEApplicationConfig>			getJ2EEApplicationConfigMap();
    
    
	/**
        @deprecated use {@link ApplicationsConfig#getApplicationConfigMap}
    */
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public Map<String,ApplicationConfig>    getApplicationConfigMap();
	
	
	/**
        @deprecated use {@link ApplicationsConfig#getEJBModuleConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public Map<String,EJBModuleConfig>			getEJBModuleConfigMap( );
	
	/**
        @deprecated use {@link ApplicationsConfig#getWebModuleConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public Map<String,WebModuleConfig>			getWebModuleConfigMap( );
	
	/**
        @deprecated use {@link ApplicationsConfig#getRARModuleConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public Map<String,RARModuleConfig>			getRARModuleConfigMap();
	
	/**
        @deprecated use {@link ApplicationsConfig#getAppClientModuleConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public Map<String,AppClientModuleConfig>			getAppClientModuleConfigMap();
	
	/**
        @deprecated use {@link ApplicationsConfig#getLifecycleModuleConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public Map<String,LifecycleModuleConfig>			getLifecycleModuleConfigMap();
	
	/**
        @deprecated use {@link ApplicationsConfig#getExtensionModuleConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public Map<String,ExtensionModuleConfig>			getExtensionModuleConfigMap();
    
	
	/**
        @deprecated use {@link ApplicationsConfig#getConnectorModuleConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public Map<String,ConnectorModuleConfig> getConnectorModuleConfigMap();
	
	/**
        @deprecated use {@link ApplicationsConfig#createLifecycleModuleConfig}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public LifecycleModuleConfig	createLifecycleModuleConfig( String name,
	                            String description,
	                            String classname,
	                            String classpath, 
	                            String loadOrder,
	                            boolean	isFailureFatal,
	                            boolean enabled,
	                            Map<String,String>		reserved );

	/**
        @deprecated use {@link ApplicationsConfig#removeLifecycleModuleConfig}
	*/
	public void			removeLifecycleModuleConfig( String name );
	

	/**
        @deprecated use {@link ResourcesConfig#getResourceConfig}
    */
    @AMXForwardTo( containeeJ2EEType=XTypes.RESOURCES_CONFIG )
	public ResourceConfig				getResourceConfig( String name );

    /**
        @deprecated use {@link ApplicationsConfig#createCustomMBeanConfig}
	*/
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public CustomMBeanConfig    createCustomMBeanConfig(
                        	        String name,
                        	        String implClassname,
                        	        String objectName,
                        	        boolean enabled,
                        	        Map<String,String> reserved );
                        	        
    /** 
        @deprecated use {@link ApplicationsConfig#removeCustomMBeanConfig}
     */
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public void                  removeCustomMBeanConfig( String name );
	    
	
	/**
        @deprecated use {@link ApplicationsConfig#getCustomMBeanConfigMap}
	 */
    @AMXForwardTo( containeeJ2EEType=XTypes.APPLICATIONS_CONFIG )
	public Map<String,CustomMBeanConfig>    getCustomMBeanConfigMap();
}







