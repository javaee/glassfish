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

package com.sun.enterprise.connectors;

import com.sun.enterprise.config.ConfigException;
import java.util.logging.*;
import java.util.*;

import javax.resource.spi.*;
import javax.resource.*;
import javax.naming.NamingException;
import com.sun.enterprise.deployment.*;
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.connectors.util.*;
import com.sun.enterprise.connectors.system.*;
import com.sun.enterprise.connectors.authentication.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class is the entry point to connector backend module.
 * It exposes different API's called by external entities like admin
 * to perform various connector backend related  operations.
 * It delegates calls to various connetcor admin services and other
 * connector services which actually implement the functionality.
 * This is a delegating class.
 * @author    Binod P.G, Srikanth P and Aditya Gore
 */


public final class ConnectorRuntime implements ConnectorConstants {
    
    static final Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);     
     
    private volatile int environment = CLIENT;

    private final ConnectorAdminObjectAdminServiceImpl 
                   adminObjectAdmService;
    private final ConnectorConfigurationParserServiceImpl 
                   configParserAdmService;
    private final ConnectorConnectionPoolAdminServiceImpl 
                   ccPoolAdmService;
    private final ConnectorResourceAdminServiceImpl 
                   connectorResourceAdmService;
    private final ConnectorSecurityAdminServiceImpl 
                   connectorSecurityAdmService;
    private final ResourceAdapterAdminServiceImpl 
                   resourceAdapterAdmService;
    private final ConnectorServiceImpl connectorService;

    private boolean isSystemResourcesLoaded = false;
    
    private static final ConnectorRuntime _runtime = new ConnectorRuntime();    

    /**
     * Returns the ConnectorRuntime instance.
     * It follows singleton pattern and only one instance exists at any point
     * of time. External entities need to call this method to get
     * ConnectorRuntime instance
     * @return ConnectorRuntime instance
     */

    public static ConnectorRuntime getRuntime() {
        return _runtime;
    }
    
    /** 
     * Private constructor. It is private as it follows singleton pattern.
     */
    private ConnectorRuntime() {
        com.sun.enterprise.util.Utility.setEnvironment();
        adminObjectAdmService = (ConnectorAdminObjectAdminServiceImpl)
           ConnectorAdminServicesFactory.getService(ConnectorAdminService.AOR);
        ccPoolAdmService = ( ConnectorConnectionPoolAdminServiceImpl)
           ConnectorAdminServicesFactory.getService(ConnectorAdminService.CCP);
        connectorResourceAdmService = ( ConnectorResourceAdminServiceImpl)
           ConnectorAdminServicesFactory.getService(ConnectorAdminService.CR);
        connectorSecurityAdmService = ( ConnectorSecurityAdminServiceImpl)
           ConnectorAdminServicesFactory.getService(ConnectorAdminService.SEC);
        resourceAdapterAdmService = ( ResourceAdapterAdminServiceImpl)
           ConnectorAdminServicesFactory.getService(ConnectorAdminService.RA);
        configParserAdmService = new ConnectorConfigurationParserServiceImpl();
        connectorService = new ConnectorServiceImpl();
    }

    /** Initializes the execution environment. If the execution environment
     *  is appserv runtime it is set to ConnectorConstants.SERVER else 
     *  it is set ConnectorConstants.CLIENT
     *  @param environment set to ConnectorConstants.SERVER if execution 
     *              environment is appserv runtime else set to 
     *              ConnectorConstants.CLIENT
     */

    public void initialize(int environment) {
        this.environment = environment;
        connectorService.initialize(getEnviron());
    }

    /**
     * Returns the execution environment. 
     * @return ConnectorConstants.SERVER if execution environment is 
     *         appserv runtime 
     *         else it returns ConnectorConstants.CLIENT
     */

    public int getEnviron() {
        return environment;
    }

   /** 
     *  Destroys/deletes the Active resource adapter object from the 
     *  connector container. Active resource adapter abstracts the rar
     *  deployed. It checks whether any resources (pools and connector
     *  resources) are still present. If they are present the deletion
     *  fails and all the objects and datastructures pertaining to  
     *  to the resource adapter are left untouched.
     *  @param moduleName Name of the rarModule to destroy/delete
     *  @throws ConnectorRuntimeException if the deletion fails
     */
    
    public void destroyActiveResourceAdapter(String moduleName)
                                throws ConnectorRuntimeException 
    {
        resourceAdapterAdmService.destroyActiveResourceAdapter(moduleName);
    }

    /** 
     *  Destroys/deletes the Active resource adapter object from the 
     *  connector container. Active resource adapter abstracts the rar
     *  deployed. It checks whether any resources (pools and connector
     *  resources) are still present. If they are present and cascade option
     *  is false the deletion fails and all the objects and datastructures 
     *  pertaining to  the resource adapter are left untouched.
     *  If cascade option is true, even if resources are still present, they are
     *  also destroyed with the active resource adapter
     *  @param moduleName Name of the rarModule to destroy/delete
     *  @param cascade If true all the resources belonging to the rar are
     *                 destroyed recursively. 
     *                 If false, and if resources pertaining to resource adapter
     *                 /rar are present deletetion is failed. Then cascade 
     *                 should be set to true or all the resources have to 
     *                 deleted explicitly before destroying the rar/Active
     *                 resource adapter. 
     *  @throws ConnectorRuntimeException if the deletion fails
     */

    public void destroyActiveResourceAdapter(String moduleName,boolean cascade) 
                                throws ConnectorRuntimeException 
    {
        resourceAdapterAdmService.destroyActiveResourceAdapter(
                                  moduleName,cascade);
    }

    /** Creates Active resource Adapter which abstracts the rar module.
     *  During the creation of ActiveResourceAdapter, default pools and
     *  resources also are created. 
     *  @param connectorDescriptor object which abstracts the connector
     *         deployment descriptor i.e rar.xml and sun-ra.xml.
     *  @param moduleName Name of the module
     *  @param moduleDir Directory where rar module is exploded.
     *  @param writeSunDescriptor If true write the sun-ra.xml props to
     *         domain.xml and if false it doesnot write to domain.xml
     *  @throws ConnectorRuntimeException if creation fails.
     */ 

    public void  createActiveResourceAdapter(
            ConnectorDescriptor connectorDescriptor, 
            String moduleName, 
            String moduleDir) throws ConnectorRuntimeException 
    {
        resourceAdapterAdmService.createActiveResourceAdapter(
               connectorDescriptor,moduleName,moduleDir);

    }

    /** Creates Active resource Adapter which abstracts the rar module.
     *  During the creation of ActiveResourceAdapter, default pools and
     *  resources also are created. 
     *  @param moduleDir Directory where rar module is exploded.
     *  @param moduleName Name of the module
     *  @param writeSunDescriptor If true write the sun-ra.xml props to
     *         domain.xml and if false it doesnot write to domain.xml
     *  @throws ConnectorRuntimeException if creation fails.
     */ 

    public void  createActiveResourceAdapter(String moduleDir , 
                String moduleName) throws ConnectorRuntimeException 
    {
        resourceAdapterAdmService.createActiveResourceAdapter(
           moduleDir,moduleName); 
    }

    public ConnectionManager obtainConnectionManager(String poolName) 
                                       throws ConnectorRuntimeException
    {
        return this.obtainConnectionManager( poolName, false );
    } 

    public ConnectionManager obtainConnectionManager(String poolName, 
        boolean forceNoLazyAssoc) 
                                       throws ConnectorRuntimeException
    {
        ConnectionManager mgr = ConnectionManagerFactory.
                getAvailableConnectionManager(poolName, forceNoLazyAssoc);
        return mgr;
    } 

    /** Returns the MCF instances in scenarions where a pool has to 
     *  return multiple mcfs. Should be used only during JMS RA recovery.
     *  @param poolName Name of the pool.MCFs pertaining to this pool is
     *         created/returned.
     *  @return created MCF instances
     *  @throws ConnectorRuntimeException if creation/retrieval of MCFs fails
     */

    public ManagedConnectionFactory[]  obtainManagedConnectionFactories(
           String poolName) throws ConnectorRuntimeException
    {

        return ccPoolAdmService.obtainManagedConnectionFactories(poolName);
    }
    /** Returns the MCF instance. If the MCF is already created and
     *  present in connectorRegistry that instance is returned. Otherwise it
     *  is created explicitly and added to ConnectorRegistry.
     *  @param poolName Name of the pool.MCF pertaining to this pool is 
     *         created/returned.   
     *  @return created/already present MCF instance
     *  @throws ConnectorRuntimeException if creation/retrieval of MCF fails
     */
 
    public ManagedConnectionFactory obtainManagedConnectionFactory(
           String poolName) throws ConnectorRuntimeException
    {
        return ccPoolAdmService.obtainManagedConnectionFactory(poolName);
    }
   
    /** Creates connector connection pool in the connector container.
     *  @param ccp ConnectorConnectionPool instance to be bound to JNDI. This 
     *             object contains the pool properties.
     *  @param connectionDefinitionName Connection definition name against which
     *                                  connection pool is being created
     *  @param rarName Name of the resource adapter
     *  @param props Properties of MCF which are present in domain.xml
     *               These properties override the ones present in ra.xml
     *  @param securityMaps Array fo security maps.
     *  @throws ConnectorRuntimeException When creation of pool fails.
     */

    public void createConnectorConnectionPool(ConnectorConnectionPool ccp, 
                          String connectionDefinitionName , String rarName, 
                          com.sun.enterprise.config.serverbeans.ElementProperty[] props,
                          com.sun.enterprise.config.serverbeans.SecurityMap[] securityMaps)
                          throws ConnectorRuntimeException 
    {
        ccPoolAdmService.createConnectorConnectionPool(
                 ccp,connectionDefinitionName,rarName,props,securityMaps);
    }

    /** Creates connector connection pool in the connector container.
     *  @param ccp ConnectorConnectionPool instance to be bound to JNDI. This 
     *             object contains the pool properties.
     *  @param cdd ConnectorDescriptor obejct which abstracts the ra.xml
     *  @param rarName Name of the resource adapter
     *  @throws ConnectorRuntimeException When creation of pool fails.
     */

    public void createConnectorConnectionPool(ConnectorConnectionPool ccp, 
                    ConnectionDefDescriptor cdd, String rarName) 
                    throws ConnectorRuntimeException 
    {
        ccPoolAdmService.createConnectorConnectionPool(ccp,cdd,rarName);

    }

    /** Creates connector connection pool in the connector container.
     *  @param ccp ConnectorConnectionPool instance to be bound to JNDI. This 
     *             object contains the pool properties.
     *  @throws ConnectorRuntimeException When creation of pool fails.
     */

    public void createConnectorConnectionPool(
                       ConnectorConnectionPool connectorPoolObj ) 
                       throws ConnectorRuntimeException 
    {
        ccPoolAdmService.createConnectorConnectionPool(
                             connectorPoolObj);
    }

    /** Creates connector connection pool in the connector container.
     *  cannot be used for 1.5 rar cases
     *  @param ccp ConnectorConnectionPool instance to be bound to JNDI. This 
     *             object contains the pool properties.
     *  @param security unused
     *  @param configProperties MCF config properties  
     *  @throws ConnectorRuntimeException When creation of pool fails.
     */

    public void  createConnectorConnectionPool(
                ConnectorConnectionPool connectorPoolObj, String security, 
                Set configProperties) throws ConnectorRuntimeException 
    {
        ccPoolAdmService.createConnectorConnectionPool(
                             connectorPoolObj,security,configProperties);
    }

    /** 
     * Creates the connector resource on a given connection pool
     * @param jndiName JNDI name of the resource to be created
     * @poolName PoolName to which the connector resource belongs.
     * @resourceType Resource type Unused.
     * @throws ConnectorRuntimeException If the resouce creation fails.
     */

    public void createConnectorResource(String jndiName, String poolName, 
                    String resourceType) throws ConnectorRuntimeException
    {

        connectorResourceAdmService.createConnectorResource(
                                 jndiName,poolName,resourceType);
    }

    /** 
     * Returns the generated default poolName of JMS resources.
     * @jndiName jndi of the resources for which pool is to be created,
     * @return generated poolname
     */

    public String getDefaultPoolName(String jndiName) {
        return connectorService.getDefaultPoolName(jndiName);
    }

    public static boolean isJmsRa() {
        return ResourceAdapterAdminServiceImpl.isJmsRa();
    }

    /** 
     * Returns the generated default connection poolName for a 
     * connection definition.
     * @moduleName rar module name
     * @connectionDefName connection definition name 
     * @return generated connection poolname
     */

    public String getDefaultPoolName(String moduleName, 
                       String connectionDefName) {
        return connectorService.getDefaultPoolName(
                         moduleName,connectionDefName);
    }

    /** 
     * Returns the generated default connector resource for a 
     * connection definition.
     * @moduleName rar module name
     * @connectionDefName connection definition name 
     * @return generated default connector resource name
     */

    public String getDefaultResourceName(String moduleName, 
                       String connectionDefName) {
        return connectorService.getDefaultResourceName(
                            moduleName,connectionDefName);
    }

    public void addAdminObject (
            String appName,
            String connectorName,
            String jndiName,
            String adminObjectType,
            Properties props)
            throws ConnectorRuntimeException 
    {
        adminObjectAdmService.addAdminObject(
                  appName,connectorName,jndiName,adminObjectType,props);
    }

    public void deleteAdminObject(String jndiName) 
                           throws ConnectorRuntimeException 
    {
        adminObjectAdmService.deleteAdminObject(jndiName);

    }

    /** Deletes connector Connection pool
     *  @param poolName Name of the pool to delete
     *  @throws ConnectorRuntimeException if pool deletion operation fails
     */

    public void deleteConnectorConnectionPool(String poolName) 
                                 throws ConnectorRuntimeException
    {
        ccPoolAdmService.deleteConnectorConnectionPool(poolName);
    }

    /** Deletes connector Connection pool. 
     *  @param poolName Name of the pool to delete
     *  @param cascade If true all the resources associed with that are also
     *                 deleted from connector container 
     *                 If false and if some resources pertaining to pool
     *                 are present deletion operation fails . If no resources
     *                 are present pool is deleted.
     *  @throws ConnectorRuntimeException if pool deletion operation fails
     */

    public void deleteConnectorConnectionPool(String poolName,boolean cascade) 
                                 throws ConnectorRuntimeException
    {

        ccPoolAdmService.deleteConnectorConnectionPool(poolName,cascade);
    }

    /**
     * Deletes the connector resource.
     * @param jndiName JNDI name of the resource to delete.
     * @throws ConnectorRuntimeException if connector resource deletion fails.
     */

    public void deleteConnectorResource(String jndiName) 
                       throws ConnectorRuntimeException 
    {
        connectorResourceAdmService.deleteConnectorResource(jndiName);

    } 

    /** 
     *  Obtain the authentication service associated with rar module.
     *  Currently only the BasicPassword authentication is supported.
     *  @rarName Rar module Name
     *  @poolName Name of the pool. Used for creation of 
     *                              BasicPasswordAuthenticationService
     */

    public AuthenticationService getAuthenticationService(String rarName,
                           String poolName) {

        return connectorSecurityAdmService.getAuthenticationService(
                            rarName,poolName);
    }


    public JmsRaMapping getJmsRaMapping() {
        return resourceAdapterAdmService.getJmsRaMapping();
    }

    /**
     *  Obtains the Permission string that needs to be added to the 
     *  to the security policy files. These are the security permissions needed
     *  by the resource adapter implementation classes.
     *  These strings are obtained by parsing the ra.xml
     *  @param moduleName rar module Name
     *  @ConnectorRuntimeException If rar.xml parsing fails.
     */

    public String getSecurityPermissionSpec(String moduleName)
                         throws ConnectorRuntimeException 
    {
        return configParserAdmService.getSecurityPermissionSpec(moduleName);
    }

 
// asadmin test-connection-pool
    /**
     * This method is used to provide backend functionality for the 
     * test-connection-pool asadmin command. Briefly the design is as
     * follows:<br>
     * 1. obtainManagedConnection for the poolname<br>
     * 2. lookup ConnectorDescriptorInfo from InitialContext using poolname<br>
     * 3. from cdi get username and password<br>
     * 4. create ResourcePrincipal using default username and password<br>
     * 5. create a Subject from this (doPriveleged)<br>
     * 6. createManagedConnection using above subject<br>
     * 7. getConnection from the ManagedConnection with above subject<br>
     *
     * @return true if the connection pool is healthy. false otherwise
     * @throws ResourceException if pool is not usable 
     */
    public boolean testConnectionPool( String poolName ) 
            throws ResourceException {
        
        return ccPoolAdmService.testConnectionPool(poolName);
    }	
    
    /**
     * Pool Monitoring
     * This method returns the PoolStats for a given pool name
     * If the poolName does not exist, it returns null
     */
    /* 
    public PoolStats getPoolStats( String poolName ) {
        return ccPoolAdmService.getPoolStats(poolName);
    }
    */

    /** Checks if the rar module is already reployed.
     *  @param moduleName Rarmodule name
     *  @return true if it is already deployed.
     *          false if it is not deployed.
     */
				       
    public boolean isRarDeployed(String moduleName) {
        
        return resourceAdapterAdmService.isRarDeployed(moduleName);
    }

    /** The ActiveResourceAdapter object which abstract the rar module is 
     *  recreated in the connector container/registry. All the pools and
     *  resources are killed. But the infrastructure to create the pools and
     *  and resources is untouched. Only the actual pool is killed.
     *  @param moduleName rar module Name.
     *  @throws ConnectorRuntimeException if recreation fails.
     */
 
    public void reCreateActiveResourceAdapter(String moduleName)
                         throws ConnectorRuntimeException {
        resourceAdapterAdmService.reCreateActiveResourceAdapter(moduleName);
    }

    /**
     * Stops the resourceAdapter and removes it from connector container/
     * registry. 
     * @param moduleName Rarmodule name.
     * @return true it is successful stop and removal of ActiveResourceAdapter
     *         false it stop and removal fails.
     */

    public boolean stopAndRemoveActiveResourceAdapter(String moduleName) {

         return resourceAdapterAdmService.stopAndRemoveActiveResourceAdapter(
                                             moduleName);
    }

    /**
     *  Kills all the pools pertaining to the rar module.
     *  @moduleName Rar module Name
     */

    public void killAllPools(String moduleName) {
        ccPoolAdmService.killAllPools(moduleName);
    }

    /** 
     *  Kills a specific pool 
     *  @param poolName poolName to kill
     */

    public void killPool(String poolName) {
        ccPoolAdmService.killPool(poolName);
    }

    /** Add the resource adapter configuration to the connector registry
     *  @param rarName rarmodule
     *  @param raConfig Resource Adapter configuration object
     *  @throws ConnectorRuntimeExcetion if the addition fails.
     */

    public void addResourceAdapterConfig(String rarName, 
           ResourceAdapterConfig raConfig) throws ConnectorRuntimeException {
        resourceAdapterAdmService.addResourceAdapterConfig(rarName,raConfig);
    }
    
    /** Delete the resource adapter configuration to the connector registry
     *  @param rarName rarmodule
     */

    public void deleteResourceAdapterConfig(String rarName) {
        resourceAdapterAdmService.deleteResourceAdapterConfig(rarName);
    }
 
//Dynamic reconfig
    /**
     * Reconfigure a connection pool.
     * This method compares the passed connector connection pool with the one
     * in memory. If the pools are unequal and the MCF properties are changed
     * a pool recreate is required. However if the pools are unequal and the
     * MCF properties are not changed a recreate is not required
     *
     * @param ccp - the Updated connector connection pool object that admin
     *              hands over
     * @return true - if a pool restart is required, false otherwise
     * @throws ConnectorRuntimeException 
     */
    public boolean reconfigureConnectorConnectionPool( ConnectorConnectionPool
            ccp ) throws ConnectorRuntimeException 
    {
        return ccPoolAdmService.reconfigureConnectorConnectionPool(ccp);
    }

    /**
     * Reconfigure a connection pool.
     * This method compares the passed connector connection pool with the one
     * in memory. If the pools are unequal and the MCF properties are changed
     * a pool recreate is required. However if the pools are unequal and the
     * MCF properties are not changed a recreate is not required
     *
     * @param ccp - the Updated connector connection pool object that admin
     *              hands over
     * @param excludedProps - A set of excluded property names that we want
     *                        to be excluded in the comparison check while
     *                        comparing MCF properties
     * @return true - if a pool restart is required, false otherwise
     * @throws ConnectorRuntimeException 
     */
    public boolean reconfigureConnectorConnectionPool( ConnectorConnectionPool
            ccp, Set excludedProps ) throws ConnectorRuntimeException 
    {
        return ccPoolAdmService.reconfigureConnectorConnectionPool(
                        ccp,excludedProps);
    }
    
    /**
     * Recreate a connector connection pool. This method essentially does
     * the following things:
     * 1. Delete the said connector connection pool<br>
     * 2. Bind the pool to JNDI<br>
     * 3. Create an MCF for this pool and register with the connector registry<br>
     *
     * @param ccp - the ConnectorConnectionPool to publish
     */

    public void recreateConnectorConnectionPool( ConnectorConnectionPool ccp) 
        throws ConnectorRuntimeException
    {
        ccPoolAdmService.recreateConnectorConnectionPool(ccp);
    }

    /** Obtains all the Connection definition names of a rar
     *  @param rarName rar moduleName 
     *  @return Array of connection definition names.
     */

    public String[] getConnectionDefinitionNames(String rarName)
               throws ConnectorRuntimeException 
    {
        return configParserAdmService.getConnectionDefinitionNames(rarName);
    }
  
    /** Obtains all the Admin object interface names of a rar
     *  @param rarName rar moduleName 
     *  @return Array of admin object interface names.
     */

    public String[] getAdminObjectInterfaceNames(String rarName)
               throws ConnectorRuntimeException 
    {
        return configParserAdmService.getAdminObjectInterfaceNames(rarName);
    }
    /**
     *  Retrieves the Resource adapter javabean properties with default values.
     *  The default values will the values present in the ra.xml. If the 
     *  value is not present in ra.xxml, javabean is introspected to obtain 
     *  the default value present, if any. If intrspection fails or null is the
     *  default value, empty string is returned. 
     *  If ra.xml has only the property and no value, empty string is the value 
     *  returned.
     *  @param rarName rar module name 
     *  @return Resource adapter javabean properties with default values.
     *  @throws ConnectorRuntimeException if property retrieval fails.
     */

    public Properties getResourceAdapterConfigProps(String rarName) 
                throws ConnectorRuntimeException 
    {
        return 
	    rarName.indexOf( ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER ) == -1
            ? configParserAdmService.getResourceAdapterConfigProps(rarName)
	    : new Properties();
    }

    /**
     *  Retrieves the MCF javabean properties with default values.
     *  The default values will the values present in the ra.xml. If the 
     *  value is not present in ra.xxml, javabean is introspected to obtain 
     *  the default value present, if any. If intrspection fails or null is the
     *  default value, empty string is returned. 
     *  If ra.xml has only the property and no value, empty string is the value 
     *  returned.
     *  @param rarName rar module name 
     *  @return managed connection factory javabean properties with 
     *          default values.
     *  @throws ConnectorRuntimeException if property retrieval fails.
     */

    public Properties getMCFConfigProps(
     String rarName,String connectionDefName) throws ConnectorRuntimeException 
    {
        return 
	    rarName.indexOf( ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER ) == -1
	        ? configParserAdmService.getMCFConfigProps(
		    rarName,connectionDefName)
	        : new Properties(); 
    }

    /**
     *  Retrieves the admin object javabean properties with default values.
     *  The default values will the values present in the ra.xml. If the 
     *  value is not present in ra.xxml, javabean is introspected to obtain 
     *  the default value present, if any. If intrspection fails or null is the
     *  default value, empty string is returned. 
     *  If ra.xml has only the property and no value, empty string is the value 
     *  returned.
     *  @param rarName rar module name 
     *  @return admin object javabean properties with 
     *          default values.
     *  @throws ConnectorRuntimeException if property retrieval fails.
     */

    public Properties getAdminObjectConfigProps(
      String rarName,String adminObjectIntf) throws ConnectorRuntimeException 
    {
        return 
	    rarName.indexOf( ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER ) == -1
	        ? configParserAdmService.getAdminObjectConfigProps(
		    rarName,adminObjectIntf)
		: new Properties();    
    }

    /**
     *  Retrieves the XXX javabean properties with default values.
     *  The javabean to introspect/retrieve is specified by the type. 
     *  The default values will be the values present in the ra.xml. If the 
     *  value is not present in ra.xxml, javabean is introspected to obtain 
     *  the default value present, if any. If intrspection fails or null is the
     *  default value, empty string is returned. 
     *  If ra.xml has only the property and no value, empty string is the value 
     *  returned.
     *  @param rarName rar module name 
     *  @return admin object javabean properties with 
     *          default values.
     *  @throws ConnectorRuntimeException if property retrieval fails.
     */

    public Properties getConnectorConfigJavaBeans(String rarName,
        String connectionDefName,String type) throws ConnectorRuntimeException
    {

        return configParserAdmService.getConnectorConfigJavaBeans(
                             rarName,connectionDefName,type);
    }

    /**
     * Return the ActivationSpecClass name for given rar and messageListenerType
     * @param moduleDir The directory where rar is exploded.
     * @param messageListenerType MessageListener type
     * @throws  ConnectorRuntimeException If moduleDir is null.
     *          If corresponding rar is not deployed.
     */

    public String getActivationSpecClass( String rarName,
             String messageListenerType) throws ConnectorRuntimeException
    {
        return configParserAdmService.getActivationSpecClass(
                          rarName,messageListenerType);
    }

    /* Parses the ra.xml and returns all the Message listener types.
     *
     * @param  rarName name of the rar module.
     * @return Array of message listener types as strings.
     * @throws  ConnectorRuntimeException If moduleDir is null.
     *          If corresponding rar is not deployed.
     *
     */

    public String[] getMessageListenerTypes(String rarName)
               throws ConnectorRuntimeException
    {
        return configParserAdmService.getMessageListenerTypes( rarName);
    }

    /** Parses the ra.xml for the ActivationSpec javabean
     *  properties. The ActivationSpec to be parsed is
     *  identified by the moduleDir where ra.xml is present and the
     *  message listener type.
     *
     *  message listener type will be unique in a given ra.xml.
     *
     *  It throws ConnectorRuntimeException if either or both the
     *  parameters are null, if corresponding rar is not deployed,
     *  if message listener type mentioned as parameter is not found in ra.xml.
     *  If rar is deployed and message listener (type mentioned) is present
     *  but no properties are present for the corresponding message listener,
     *  null is returned.
     *
     *  @param  rarName name of the rar module.
     *  @param  messageListenerType message listener type.It is uniqie
     *          across all <messagelistener> sub-elements in <messageadapter>
     *          element in a given rar.
     *  @return Javabean properties with the property names and values
     *          of properties. The property values will be the values
     *          mentioned in ra.xml if present. Otherwise it will be the
     *          default values obtained by introspecting the javabean.
     *          In both the case if no value is present, empty String is
     *          returned as the value.
     *  @throws  ConnectorRuntimeException if either of the parameters are null.
     *           If corresponding rar is not deployed i.e moduleDir is invalid.
     *           If messagelistener type is not found in ra.xml
     */

    public Properties getMessageListenerConfigProps(String rarName,
         String messageListenerType)throws ConnectorRuntimeException
    {
        return 
	    rarName.indexOf( ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER ) == -1
            ? configParserAdmService.getMessageListenerConfigProps(
                        rarName,messageListenerType)
	    : new Properties();		
    }

    /** Returns the Properties object consisting of propertyname as the
     *  key and datatype as the value.
     *  @param  rarName name of the rar module.
     *  @param  messageListenerType message listener type.It is uniqie
     *          across all <messagelistener> sub-elements in <messageadapter>
     *          element in a given rar.
     *  @return Properties object with the property names(key) and datatype
     *          of property(as value).
     *  @throws  ConnectorRuntimeException if either of the parameters are null.
     *           If corresponding rar is not deployed i.e moduleDir is invalid.
     *           If messagelistener type is not found in ra.xml
     */

    public Properties getMessageListenerConfigPropTypes(String rarName,
               String messageListenerType) throws ConnectorRuntimeException
    {
        return configParserAdmService.getMessageListenerConfigPropTypes(
                        rarName,messageListenerType);
    }

    /** Checks whether the executing environment is application server
     *  @return true if execution environment is server
     *          false if it is client
     */

    public static boolean isServer() {
        return getRuntime().connectorService.isServer(); 
    }

    public void loadDeferredResourceAdapter(String rarName) 
                        throws ConnectorRuntimeException {
        connectorService.loadDeferredResourceAdapter(rarName);
    }

    public boolean checkAndLoadResource(String resName) {
        return connectorService.checkAndLoadResource(resName);
    }

    public boolean checkAccessibility(String rarName, ClassLoader loader) {
        return connectorService.checkAccessibility(rarName, loader);
    }

    /**
     * Gets the properties of the Java bean connection definition class that
     * have setter methods defined
     *
     * @param connectionDefinitionClassName  The Connection Definition Java bean class 
     *  for which overrideable properties are required.
     * @return A Set of properties that have a setter method defined in the
     *                Connection Definition class
     */
    public static Set getConnectionDefinitionProperties(String connectionDefinitionClassName) {
        return getRuntime().ccPoolAdmService.getConnectionDefinitionProperties(
            connectionDefinitionClassName);
    }
                                                                                                                                               
    /**
      * Gets the properties of the Java bean connection definition class that
      * have setter methods defined and the default values as provided by the
      * Connection Definition java bean developer.
      * This method is used to get properties of jdbc-data-source<br>
      * To get Connection definition properties for Connector Connection Pool,
      * use ConnectorRuntime.getMCFConfigProperties()<br>
      * When the connection definition class is not found, standard JDBC
      * properties (of JDBC 3.0 Specification) will be returned.<br>
      *
      * @param connectionDefinitionClassName
      *                     The Connection Definition Java bean class for which
      *                     overrideable properties are required.
      * @return Map [property, defaultValue]
      */
    public static Map getConnectionDefinitionPropertiesAndDefaults(String connectionDefinitionClassName) {
        return getRuntime().ccPoolAdmService.getConnectionDefinitionPropertiesAndDefaults(
            connectionDefinitionClassName);
    }
 
    /**
     * Gets Connector Resource Rebind Event notifier.
     * @return   ConnectorNamingEventNotifier
     */
    public ConnectorNamingEventNotifier getResourceRebindEventNotifier(){
        return connectorResourceAdmService.getResourceRebindEventNotifier();
    }

    /**
     * Causes pool to switch on the matching of connections.
     * It can be either directly on the pool or on the ConnectorConnectionPool
     * object that is bound in JNDI.
     *
     * @param rarName Name of Resource Adpater.
     * @param poolName Name of the pool.
     */
    public void switchOnMatching (String rarName, String poolName) {
        connectorService.switchOnMatching(rarName, poolName);
    }

    /**
     * Causes matching to be switched on the ConnectorConnectionPool
     * bound in JNDI
     *
     * @param poolName Name of the pool
     */
    public void switchOnMatchingInJndi (String poolName)
        throws ConnectorRuntimeException {
        ccPoolAdmService.switchOnMatching(poolName);
    }

    /** 
     * Obtains the connector Descriptor pertaining to rar. 
     * If ConnectorDescriptor is present in registry, it is obtained from
     * registry and returned. Else it is explicitly read from directory
     * where rar is exploded.
     * @param rarName Name of the rar
     * @return ConnectorDescriptor pertaining to rar.
     */
    public ConnectorDescriptor getConnectorDescriptor( String rarName ) 
        throws ConnectorRuntimeException 
    {
        return connectorService.getConnectorDescriptor( rarName );
        
    }
    
    /**
     * Calls the stop method for all J2EE Connector 1.5 spec compliant RARs
     */    
    public static void stopAllActiveResourceAdapters(){
        getRuntime().resourceAdapterAdmService.stopAllActiveResourceAdapters();
    }   
    
    /**
     * Returns the configurable ResourceAdapterBean Properties
     * for a connector module bundled as a RAR.
     * 
     * @param pathToDeployableUnit a physical,accessible location of the connector module.
     * [either a RAR for RAR-based deployments or a directory for Directory based deployments] 
     * @return A Map that is of <String RAJavaBeanPropertyName, String defaultPropertyValue>
     * An empty map is returned in the case of a 1.0 RAR 
     */
    public Map getResourceAdapterBeanProperties(String pathToDeployableUnit) throws ConnectorRuntimeException{
        return configParserAdmService.getRABeanProperties(pathToDeployableUnit);
    }

    /**
     * Initialize the monitoring listeners for connection pools, work management
     * and message end point factory related stats
     */
    public void initializeConnectorMonitoring() {
        connectorService.initializeConnectorMonitoring();
    }



    /**
     * Get a sql connection from the DataSource specified by the jdbcJndiName. 
     * This API is intended to be used in the DAS. The motivation for having this 
     * API is to provide the CMP backend a means of acquiring a connection during 
     * the codegen phase. If a user is trying to deploy an app on a remote server, 
     * without this API, a resource reference has to be present both in the DAS 
     * and the server instance. This makes the deployment more complex for the 
     * user since a resource needs to be forcibly created in the DAS Too. 
     * This API will mitigate this need.
     *
     * @param jndiName the jndi name of the resource being used to get Connection from
     *                 This resource can either be a pmf resource or a jdbc resource
     * @param user  the user used to authenticate this request
     * @param password  the password used to authenticate this request
     *
     * @return a java.sql.Connection
     * @throws java.sql.SQLException in case of errors
     */
    public Connection getConnection(String jndiName, String user, String password)
            throws SQLException
    {
             

	return ccPoolAdmService.getConnection( jndiName, user, password ); 
    }

    /**
     * Get a sql connection from the DataSource specified by the jdbcJndiName. 
     * This API is intended to be used in the DAS. The motivation for having this 
     * API is to provide the CMP backend a means of acquiring a connection during 
     * the codegen phase. If a user is trying to deploy an app on a remote server, 
     * without this API, a resource reference has to be present both in the DAS 
     * and the server instance. This makes the deployment more complex for the 
     * user since a resource needs to be forcibly created in the DAS Too. 
     * This API will mitigate this need.
     *
     * @param jndiName the jndi name of the resource being used to get Connection from
     *                 This resource can either be a pmf resource or a jdbc resource
     *
     * @return a java.sql.Connection
     * @throws java.sql.SQLException in case of errors
     */
    public Connection getConnection(String jndiName)
            throws SQLException
    {
	return ccPoolAdmService.getConnection( jndiName ); 
    }

    /**
     * Checks if a conncetor connection pool has been deployed to this server 
     * instance
     * @param poolName
     * @return
     */
    public boolean isConnectorConnectionPoolDeployed(String poolName) {
        return ccPoolAdmService.isConnectorConnectionPoolDeployed(poolName);
    }
       
    /**
     * Returns a list of <code>MQJMXConnectorInfo</code> associated with the target
     */
    public MQJMXConnectorInfo[] getMQJMXConnectorInfo(String target) throws
                                                     ConnectorRuntimeException {
       /*
        For getting a JMXConnector for each jms-host defined in jms-service:
        If EMBEDDED/LOCAL and notclustered: get default JMS host from jms-service.
        If MQRA has already been started, get from registry, introspect and get
        the valid properties to construct MQJMXConnector
        If clustered: get all hosts from JMS host information and for each of them
        construct a MQRA instance, and get properties to construct MQJMXConnector
        */
        return MQJMXConnectorHelper.getMQJMXConnectorInfo(target);
    }

    /**
     * Code that checks whether a jndi suffix is valid or not.
     */
    public boolean isValidJndiSuffix(String name) {
        return connectorResourceAdmService.isValidJndiSuffix(name);
    }

    /**
     * Does lookup of "__pm" datasource. If found, it will be returned.<br><br>
     *
     * If not found and <b>force</b> is true, this api will try to get a wrapper datasource specified
     *  by the jdbcjndi name. The motivation for having this
     * API is to provide the CMP backend/ JPA-Java2DB a means of acquiring a connection during
     * the codegen phase. If a user is trying to deploy an JPA-Java2DB app on a remote server,
     * without this API, a resource reference has to be present both in the DAS
     * and the server instance. This makes the deployment more complex for the
     * user since a resource needs to be forcibly created in the DAS Too.
     * This API will mitigate this need.
     * When the resource is not enabled, datasource wrapper provided will not be of
     * type "__pm"
     *
     * @param jndiName  jndi name of the resource
     * @param force provide the resource (in DAS)  even if it is not enabled in DAS
     * @return DataSource representing the resource.
     * @throws NamingException when not able to get the datasource.
     */
    public Object lookupPMResource(String jndiName, boolean force) throws NamingException{
        Object result ;
        try{
            result = connectorResourceAdmService.lookup(jndiName+PM_JNDI_SUFFIX);
        }catch(NamingException ne){
            if(force && ResourcesUtil.isDAS()){
                _logger.log(Level.FINE, "jdbc.unable_to_lookup_resource",new Object[] {jndiName});
                result = lookupDataSourceInDAS(jndiName);
            }else{
                throw ne;
            }
        }
        return result;
    }
    /**
     * Does lookup of non-tx-datasource. If found, it will be returned.<br><br>
     *
     * If not found and <b>force</b> is true,  this api will try to get a wrapper datasource specified
     * by the jdbcjndi name. The motivation for having this
     * API is to provide the CMP backend/ JPA-Java2DB a means of acquiring a connection during
     * the codegen phase. If a user is trying to deploy an JPA-Java2DB app on a remote server,
     * without this API, a resource reference has to be present both in the DAS
     * and the server instance. This makes the deployment more complex for the
     * user since a resource needs to be forcibly created in the DAS Too.
     * This API will mitigate this need.
     *
     * @param jndiName  jndi name of the resource
     * @param force provide the resource (in DAS)  even if it is not enabled in DAS
     * @return DataSource representing the resource.
     * @throws NamingException when not able to get the datasource.
     */
    public Object lookupNonTxResource(String jndiName, boolean force) throws NamingException{
        Object result ;
        try{
            result = connectorResourceAdmService.lookup(jndiName+NON_TX_JNDI_SUFFIX);
        }catch(NamingException ne){
            if(force && ResourcesUtil.isDAS()){
                _logger.log(Level.FINE, "jdbc.unable_to_lookup_resource",new Object[] {jndiName});
                result = lookupDataSourceInDAS(jndiName);
            }else{
                throw ne;
            }
        }
        return result;
    }


    /**
     * Get a wrapper datasource specified by the jdbcjndi name
     * This API is intended to be used in the DAS. The motivation for having this
     * API is to provide the CMP backend/ JPA-Java2DB a means of acquiring a connection during
     * the codegen phase. If a user is trying to deploy an JPA-Java2DB app on a remote server,
     * without this API, a resource reference has to be present both in the DAS
     * and the server instance. This makes the deployment more complex for the
     * user since a resource needs to be forcibly created in the DAS Too.
     * This API will mitigate this need.
     *
     * @param jndiName  jndi name of the resource
     * @return DataSource representing the resource.
     */
    private Object lookupDataSourceInDAS(String jndiName){
        return connectorResourceAdmService.lookupDataSourceInDAS(jndiName);
    }

    /**
     * Lookup the JNDI name with appropriate suffix.
     *
     * @param name : JNDI name to be looked up. This can be suffixed
     *               with a proper suffix like __pm or __nontx
     */
    public Object lookup(String name) throws NamingException {
        return connectorResourceAdmService.lookup(name);
    }

    /**
     * Returns the system RAR names that allow pool creation
     */
    public String[] getSystemConnectorsAllowingPoolCreation(){
        return new String[] {
                ConnectorConstants.DEFAULT_JMS_ADAPTER,
                ConnectorConstants.JAXR_RA_NAME};
    }
    
    /**
     * Loads all system RA resources not used till now
     * This method is used when user accesses jndi tree.
     */
    public synchronized void loadAllSystemRAResources(){
        if(!isSystemResourcesLoaded){
            ResourcesUtil.createInstance().loadSystemRAResources();
            isSystemResourcesLoaded = true;
        }
    }

}


