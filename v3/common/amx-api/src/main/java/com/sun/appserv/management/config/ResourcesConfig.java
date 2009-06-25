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
    Configuration for the &lt;resources&gt; element; it is an internal "node" which
    groups all resources under itself.
    @since Glassfish V3
*/
public interface ResourcesConfig
	extends ConfigElement, Container, ConfigCreator, ConfigRemover, ConfigCollectionElement, DefaultValues, Singleton
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.RESOURCES_CONFIG;
    
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
	public JNDIResourceConfig       createJNDIResourceConfig(
                                    String    jndiName,
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
		@param	reserved		reserved for future use
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
							@ResolveTo(Boolean.class) String failAllConnections,
							@ResolveTo(Integer.class) String idleTimeoutSeconds,
							@ResolveTo(Boolean.class) String connectionValidationRequired,
							@ResolveTo(Boolean.class) String isolationLevelGuaranteed,
							String	transactionIsolationLevel,
							@ResolveTo(Integer.class) String maxPoolSize,
							@ResolveTo(Integer.class) String maxWaitTimeMillis,
							@ResolveTo(Integer.class) String poolResizeQuantity,
							String	resType,
							@ResolveTo(Boolean.class) String steadyPoolSize,
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
    @AMXCreateInfo(paramNames={"name","datasource-classname", "optional"})
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
	public MailResourceConfig	createMailResourceConfig(
                        String          jndiName,
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
    */
	public ResourceConfig				getResourceConfig( String name );

    /**
	public CustomMBeanConfig    createCustomMBeanConfig(
                        	        String name,
                        	        String implClassname,
                        	        String objectName,
                        	        @ResolveTo(Boolean.class) String enabled,
                        	        Map<String,String> reserved );
                        	        
	public void                  removeCustomMBeanConfig( String name );
	    
	
	public Map<String,CustomMBeanConfig>    getCustomMBeanConfigMap();
	 */
}







