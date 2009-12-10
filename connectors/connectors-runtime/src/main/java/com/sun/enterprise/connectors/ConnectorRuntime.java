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

import com.sun.enterprise.connectors.util.DriverLoader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URI;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;
import javax.security.auth.callback.CallbackHandler;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import com.sun.appserv.connectors.internal.api.*;
import com.sun.appserv.connectors.internal.spi.*;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.connectors.authentication.AuthenticationService;
import com.sun.enterprise.connectors.module.ConnectorApplication;
import com.sun.enterprise.connectors.naming.ConnectorNamingEventNotifier;
import com.sun.enterprise.connectors.service.*;
import com.sun.enterprise.connectors.service.ConnectorService;
import com.sun.enterprise.connectors.util.RAWriterAdapter;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.ConnectorArchivist;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactory;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.resource.pool.PoolManager;
import com.sun.enterprise.resource.pool.monitor.ConnectionPoolProbeProviderUtil;
import com.sun.enterprise.security.jmac.callback.ContainerCallbackHandler;
import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import org.jvnet.hk2.config.types.Property;
import org.glassfish.api.admin.*;
import com.sun.logging.LogDomains;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.DelegatingClassLoader;
import org.glassfish.internal.api.ConnectorClassLoaderService;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Singleton;
import com.sun.enterprise.connectors.util.ConnectorTimerProxy;
import com.sun.enterprise.connectors.util.ConnectorJavaBeanValidator;


/**
 * This class is the entry point to connector backend module.
 * It exposes different API's called by external entities like JPA, admin
 * to perform various connector backend related  operations.
 * It delegates calls to various connetcor admin services and other
 * connector services which actually implement the functionality.
 * This is a delegating class.
 *
 * @author Binod P.G, Srikanth P, Aditya Gore, Jagadish Ramu
 */
@Service
@Scoped(Singleton.class)
public class ConnectorRuntime implements com.sun.appserv.connectors.internal.api.ConnectorRuntime,
        PostConstruct, PreDestroy {

    private volatile int environment = SERVER;
    private static ConnectorRuntime _runtime;
    private Logger _logger = LogDomains.getLogger(ConnectorRuntime.class, LogDomains.RSR_LOGGER);
    private ConnectorConnectionPoolAdminServiceImpl ccPoolAdmService;
    private ConnectorResourceAdminServiceImpl connectorResourceAdmService;
    private ConnectorService connectorService;
    private ConnectorConfigurationParserServiceImpl configParserAdmService;
    private ResourceAdapterAdminServiceImpl resourceAdapterAdmService;
    private ConnectorSecurityAdminServiceImpl connectorSecurityAdmService;
    private ConnectorAdminObjectAdminServiceImpl adminObjectAdminService;
    private ConnectorRegistry connectorRegistry = ConnectorRegistry.getInstance();
    private JdbcAdminServiceImpl jdbcAdminService;

    @Inject
    private GlassfishNamingManager namingManager;

    @Inject
    private PoolManager poolManager;

    @Inject
    private InvocationManager invocationManager;

    @Inject
    private ComponentEnvManager componentEnvManager;

    @Inject
    private Habitat wmf;

    @Inject
    private Habitat allResources;

    @Inject
    private Habitat deployerHabitat;

    @Inject
    private ClassLoaderHierarchy clh;

    @Inject
    private ConnectorsClassLoaderUtil cclUtil;

    @Inject
    private ActiveRAFactory activeRAFactory;

    @Inject
    private Habitat applications;

    @Inject
    private Habitat habitat;

    @Inject
    private ProcessEnvironment processEnvironment;
    
    @Inject
    private DriverLoader driverLoader;

    @Inject
    private ConnectorJavaBeanValidator connectorBeanValidator;

    @Inject
    private ConnectorClassLoaderService connectorClassLoaderService;

    @Inject
    ServerEnvironmentImpl env;


    // performance improvement, cache the lookup of transaction manager.
    private JavaEETransactionManager transactionManager;


    /**
     * Returns the ConnectorRuntime instance.
     * It follows singleton pattern and only one instance exists at any point
     * of time. External entities need to call this method to get
     * ConnectorRuntime instance
     *
     * @return ConnectorRuntime instance
     */
    public static ConnectorRuntime getRuntime() {
        if (_runtime == null) {
            throw new RuntimeException("Connector Runtime not initialized");
        }
        return _runtime;
    }
    
    /**
     * Private constructor. It is private as it follows singleton pattern.
     */
    public ConnectorRuntime() {
        _runtime = this;
    }

    public ConnectionPoolProbeProviderUtil getProbeProviderUtil(){
        return habitat.getComponent(ConnectionPoolProbeProviderUtil.class);
    }
    
    /**
     * Returns the execution environment.
     *
     * @return ConnectorConstants.SERVER if execution environment is
     *         appserv runtime
     *         else it returns ConnectorConstants.CLIENT
     */
    public int getEnvironment() {
        return environment;
    }

    /**
     * Returns the generated default connection poolName for a
     * connection definition.
     *
     * @param moduleName        rar module name
     * @param connectionDefName connection definition name
     * @return generated connection poolname
     */
    public String getDefaultPoolName(String moduleName,
                                     String connectionDefName) {
        return connectorService.getDefaultPoolName(moduleName, connectionDefName);
    }

    /**
     * Deletes connector Connection pool
     *
     * @param poolName Name of the pool to delete
     * @throws ConnectorRuntimeException if pool deletion operation fails
     */
    public void deleteConnectorConnectionPool(String poolName)
            throws ConnectorRuntimeException {
        ccPoolAdmService.deleteConnectorConnectionPool(poolName);
    }

    /**
     * Creates connector connection pool in the connector container.
     *
     * @param connectorPoolObj ConnectorConnectionPool instance to be bound to JNDI. This
     *                         object contains the pool properties.
     * @throws ConnectorRuntimeException When creation of pool fails.
     */
    public void createConnectorConnectionPool(
            ConnectorConnectionPool connectorPoolObj)
            throws ConnectorRuntimeException {
        ccPoolAdmService.createConnectorConnectionPool(connectorPoolObj);
    }

    /**
     * Creates the connector resource on a given connection pool
     *
     * @param jndiName     JNDI name of the resource to be created
     * @param poolName     to which the connector resource belongs.
     * @param resourceType Unused.
     * @throws ConnectorRuntimeException If the resouce creation fails.
     */
    public void createConnectorResource(String jndiName, String poolName,
                                        String resourceType) throws ConnectorRuntimeException {

        connectorResourceAdmService.createConnectorResource(jndiName, poolName, resourceType);
    }

    /**
     * Returns the generated default connector resource for a
     * connection definition.
     *
     * @param moduleName        rar module name
     * @param connectionDefName connection definition name
     * @return generated default connector resource name
     */
    public String getDefaultResourceName(String moduleName,
                                         String connectionDefName) {
        return connectorService.getDefaultResourceName(
                moduleName, connectionDefName);
    }

    /**
     * Provides resource adapter log writer to be given to MCF of a resource-adapter
     *
     * @return PrintWriter
     */
    public PrintWriter getResourceAdapterLogWriter() {
        Logger logger = LogDomains.getLogger(ConnectorRuntime.class, LogDomains.RSR_LOGGER);
        RAWriterAdapter writerAdapter = new RAWriterAdapter(logger);
        return new PrintWriter(writerAdapter);
    }

    /**
     * Deletes the connector resource.
     *
     * @param jndiName JNDI name of the resource to delete.
     * @throws ConnectorRuntimeException if connector resource deletion fails.
     */
    public void deleteConnectorResource(String jndiName)
            throws ConnectorRuntimeException {
        connectorResourceAdmService.deleteConnectorResource(jndiName);
    }

    /**
     * Obtains the connector Descriptor pertaining to rar.
     * If ConnectorDescriptor is present in registry, it is obtained from
     * registry and returned. Else it is explicitly read from directory
     * where rar is exploded.
     *
     * @param rarName Name of the rar
     * @return ConnectorDescriptor pertaining to rar.
     * @throws ConnectorRuntimeException when unable to get descriptor
     */
    public ConnectorDescriptor getConnectorDescriptor(String rarName)
            throws ConnectorRuntimeException {
        return connectorService.getConnectorDescriptor(rarName);

    }

    /**
     * {@inheritDoc}
     */
    public void createActiveResourceAdapter(String moduleDir,
                                            String moduleName,
                                            ClassLoader loader) throws ConnectorRuntimeException {
        resourceAdapterAdmService.createActiveResourceAdapter(moduleDir, moduleName, loader);
    }
    /**
     * {@inheritDoc}
     */
    public void  createActiveResourceAdapter( ConnectorDescriptor connectorDescriptor, String moduleName,
            String moduleDir, ClassLoader loader) throws ConnectorRuntimeException {
        resourceAdapterAdmService.createActiveResourceAdapter(connectorDescriptor, moduleName, moduleDir, loader);
    }

    /** Creates Active resource Adapter which abstracts the rar module.
     *  During the creation of ActiveResourceAdapter, default pools and
     *  resources also are created.
     *  @param connectorDescriptor object which abstracts the connector
     *         deployment descriptor i.e rar.xml and sun-ra.xml.
     *  @param moduleName Name of the module
     *  @param moduleDir Directory where rar module is exploded.
     *  @throws ConnectorRuntimeException if creation fails.
     */

    public void  createActiveResourceAdapter( ConnectorDescriptor connectorDescriptor, String moduleName,
            String moduleDir) throws ConnectorRuntimeException {
        resourceAdapterAdmService.createActiveResourceAdapter(connectorDescriptor, moduleName, moduleDir, null);
    }

    /**
     * {@inheritDoc}
     */
    public void destroyActiveResourceAdapter(String moduleName)
            throws ConnectorRuntimeException {
        resourceAdapterAdmService.stopActiveResourceAdapter(moduleName);
    }

    /**
     * Returns the MCF instance. If the MCF is already created and
     * present in connectorRegistry that instance is returned. Otherwise it
     * is created explicitly and added to ConnectorRegistry.
     *
     * @param poolName Name of the pool.MCF pertaining to this pool is
     *                 created/returned.
     * @return created/already present MCF instance
     * @throws ConnectorRuntimeException if creation/retrieval of MCF fails
     */
    public ManagedConnectionFactory obtainManagedConnectionFactory(
            String poolName) throws ConnectorRuntimeException {
        return ccPoolAdmService.obtainManagedConnectionFactory(poolName);
    }

    /** Returns the MCF instances in scenarions where a pool has to
     *  return multiple mcfs. Should be used only during JMS RA recovery.
     *  @param poolName Name of the pool.MCFs pertaining to this pool is
     *         created/returned.
     *  @return created MCF instances
     *  @throws ConnectorRuntimeException if creation/retrieval of MCFs fails
     */

    public ManagedConnectionFactory[]  obtainManagedConnectionFactories(
           String poolName) throws ConnectorRuntimeException {
        return ccPoolAdmService.obtainManagedConnectionFactories(poolName);
    }


    /**
     * provides connection manager for a pool
     *
     * @param poolName         pool name
     * @param forceNoLazyAssoc when set to true, lazy association feature will be turned off (even if it is ON via
     *                         pool attribute)
     * @return ConnectionManager for the pool
     * @throws ConnectorRuntimeException when unable to provide a connection manager
     */
    public ConnectionManager obtainConnectionManager(String poolName,
                                                     boolean forceNoLazyAssoc)
            throws ConnectorRuntimeException {
        ConnectionManager mgr = ConnectionManagerFactory.
                getAvailableConnectionManager(poolName, forceNoLazyAssoc);
        return mgr;
    }

    /**
     * {@inheritDoc}
     */
    public Object lookupPMResource(String jndiName, boolean force) throws NamingException {
        //TODO V3 handle clustering later (  --createtables, nonDAS)
        if (force) {
            _logger.log(Level.INFO, "lookup PM Resource [ " + jndiName + " ] with force=true is not supported");
        }
        return connectorResourceAdmService.lookup(jndiName + PM_JNDI_SUFFIX);
    }

    /**
     * {@inheritDoc}
     */
    public Object lookupNonTxResource(String jndiName, boolean force) throws NamingException {
        //TODO V3 handle clustering later (  --createtables, nonDAS)
        if (force) {
            _logger.log(Level.INFO, "lookup NonTx Resource [ " + jndiName + " ] with force=true is not supported");
        }
        return connectorResourceAdmService.lookup(jndiName + NON_TX_JNDI_SUFFIX);
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
     * @param connectionDefinitionClassName The Connection Definition Java bean class for which
     *                                      overrideable properties are required.
     * @return Map<String, Object> String represents property name
     *         and Object is the defaultValue that is a primitive type or String.
     */
    public Map<String, Object> getConnectionDefinitionPropertiesAndDefaults(String connectionDefinitionClassName, String resType) {
        return ccPoolAdmService.getConnectionDefinitionPropertiesAndDefaults(
                connectionDefinitionClassName, resType);
    }

    /**
     * Provides the list of built in custom resources by
     * resource-type and factory-class-name pair.
     * @return map of resource-type & factory-class-name
     */
    public Map<String,String> getBuiltInCustomResources(){
        return ConnectorsUtil.getBuiltInCustomResources();
    }

    /**
     * Returns the system RAR names that allow pool creation
     * @return String array representing list of system-rars
     */
    public String[] getSystemConnectorsAllowingPoolCreation(){
       String[] systemRarNames = new String[systemRarsAllowingPoolCreation.size()];
       return systemRarsAllowingPoolCreation.toArray(systemRarNames);
    }


    /**
     * {@inheritDoc}
     */
    public String[] getConnectionDefinitionNames(String rarName)
               throws ConnectorRuntimeException {
        return configParserAdmService.getConnectionDefinitionNames(rarName);
    }

    /**
     * {@inheritDoc}
     */
    public String getSecurityPermissionSpec(String moduleName)
                         throws ConnectorRuntimeException {
        return configParserAdmService.getSecurityPermissionSpec(moduleName);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAdminObjectInterfaceNames(String rarName)
               throws ConnectorRuntimeException {
        return configParserAdmService.getAdminObjectInterfaceNames(rarName);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAdminObjectClassNames(String rarName, String intfName)
            throws ConnectorRuntimeException {
        return configParserAdmService.getAdminObjectClassNames(rarName, intfName);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasAdminObject(String rarName, String intfName, String className)
                throws ConnectorRuntimeException{
        return configParserAdmService.hasAdminObject(rarName, intfName, className);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String,String> getResourceAdapterConfigProps(String rarName)
                throws ConnectorRuntimeException {
        Properties properties = configParserAdmService.getResourceAdapterConfigProps(rarName);
        return ConnectorsUtil.convertPropertiesToMap(properties);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String,String> getMCFConfigProps(
     String rarName,String connectionDefName) throws ConnectorRuntimeException {
        Properties properties = configParserAdmService.getMCFConfigProps(
		    rarName,connectionDefName);

        return ConnectorsUtil.convertPropertiesToMap(properties);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String,String> getAdminObjectConfigProps(
      String rarName,String adminObjectIntf) throws ConnectorRuntimeException {
        Properties properties =
	    configParserAdmService.getAdminObjectConfigProps(rarName,adminObjectIntf);
        return ConnectorsUtil.convertPropertiesToMap(properties);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String,String> getAdminObjectConfigProps(
      String rarName,String adminObjectIntf, String adminObjectClass) throws ConnectorRuntimeException {
        Properties properties =
	     configParserAdmService.getAdminObjectConfigProps(rarName,adminObjectIntf, adminObjectClass);

        return ConnectorsUtil.convertPropertiesToMap(properties);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String,String> getConnectorConfigJavaBeans(String rarName,
        String connectionDefName,String type) throws ConnectorRuntimeException {

        Properties properties = configParserAdmService.getConnectorConfigJavaBeans(
                             rarName,connectionDefName,type);
        return ConnectorsUtil.convertPropertiesToMap(properties);
    }

    /**
     * {@inheritDoc}
     */
    public String getActivationSpecClass( String rarName,
             String messageListenerType) throws ConnectorRuntimeException {
        return configParserAdmService.getActivationSpecClass(
                          rarName,messageListenerType);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getMessageListenerTypes(String rarName)
               throws ConnectorRuntimeException  {
        return configParserAdmService.getMessageListenerTypes( rarName);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String,String> getMessageListenerConfigProps(String rarName,
         String messageListenerType)throws ConnectorRuntimeException {
        Properties properties =
        configParserAdmService.getMessageListenerConfigProps(
                        rarName,messageListenerType);
        return ConnectorsUtil.convertPropertiesToMap(properties);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String,String> getMessageListenerConfigPropTypes(String rarName,
               String messageListenerType) throws ConnectorRuntimeException {
        Properties properties =  configParserAdmService.getMessageListenerConfigPropTypes(
                        rarName,messageListenerType);
        return ConnectorsUtil.convertPropertiesToMap(properties);
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
/* TODO V3
    public Map getResourceAdapterBeanProperties(String pathToDeployableUnit) throws ConnectorRuntimeException{
        return configParserAdmService.getRABeanProperties(pathToDeployableUnit);
    }
*/

    /**
     * Causes pool to switch on the matching of connections.
     * It can be either directly on the pool or on the ConnectorConnectionPool
     * object that is bound in JNDI.
     *
     * @param rarName  Name of Resource Adpater.
     * @param poolName Name of the pool.
     */
    public void switchOnMatching(String rarName, String poolName) {
        connectorService.switchOnMatching(rarName, poolName);
    }

    /**
     * Causes matching to be switched on the ConnectorConnectionPool
     * bound in JNDI
     *
     * @param poolName Name of the pool
     * @throws ConnectorRuntimeException when unable to set matching via jndi object
     */
    public void switchOnMatchingInJndi(String poolName)
            throws ConnectorRuntimeException {
        ccPoolAdmService.switchOnMatching(poolName);
    }

    public GlassfishNamingManager getNamingManager() {
        return namingManager;
    }

    /**
     * The component has been injected with any dependency and
     * will be placed into commission by the subsystem.
     */
    public void postConstruct() {
        ccPoolAdmService = (ConnectorConnectionPoolAdminServiceImpl)
                ConnectorAdminServicesFactory.getService(ConnectorConstants.CCP);
        connectorResourceAdmService = (ConnectorResourceAdminServiceImpl)
                ConnectorAdminServicesFactory.getService(ConnectorConstants.CR);
        connectorService = new ConnectorService();
        resourceAdapterAdmService = (ResourceAdapterAdminServiceImpl)
                ConnectorAdminServicesFactory.getService(ConnectorConstants.RA);
        connectorSecurityAdmService = (ConnectorSecurityAdminServiceImpl)
                ConnectorAdminServicesFactory.getService(ConnectorConstants.SEC);
        adminObjectAdminService = (ConnectorAdminObjectAdminServiceImpl)
                ConnectorAdminServicesFactory.getService(ConnectorConstants.AOR);
        configParserAdmService = new ConnectorConfigurationParserServiceImpl();
        jdbcAdminService = (JdbcAdminServiceImpl) 
                ConnectorAdminServicesFactory.getService(ConnectorConstants.JDBC);

        initializeEnvironment(processEnvironment);
        if(isServer()) {
            getProbeProviderUtil().registerProbeProvider();
         }
    }

    /**
     * initializes the connector runtime mode to be SERVER or CLIENT
     * @param processEnvironment ProcessEnvironment
     */
    private void initializeEnvironment(ProcessEnvironment processEnvironment) {
        //TODO V3, remove ConnectorConstants.CLIENT/SERVER usage in connector-runtime, and use only
        //process environment
        if (processEnvironment.getProcessType().isServer()){
            environment = SERVER;
        }else if (processEnvironment.getProcessType().equals(ProcessEnvironment.ProcessType.ACC)) {
            environment = CLIENT;
        }else if(processEnvironment.getProcessType().equals(ProcessEnvironment.ProcessType.Other)){
            environment = NON_ACC_CLIENT;
        }
    }

    /**
     * Checks if a conncetor connection pool has been deployed to this server
     * instance
     *
     * @param poolName connection pool name
     * @return boolean indicating whether the resource is deployed or not
     */
    public boolean isConnectorConnectionPoolDeployed(String poolName) {
        return ccPoolAdmService.isConnectorConnectionPoolDeployed(poolName);
    }

    /**
     * Reconfigure a connection pool.
     * This method compares the passed connector connection pool with the one
     * in memory. If the pools are unequal and the MCF properties are changed
     * a pool recreate is required. However if the pools are unequal and the
     * MCF properties are not changed a recreate is not required
     *
     * @param ccp           - the Updated connector connection pool object that admin
     *                      hands over
     * @param excludedProps - A set of excluded property names that we want
     *                      to be excluded in the comparison check while
     *                      comparing MCF properties
     * @return true - if a pool restart is required, false otherwise
     * @throws ConnectorRuntimeException when unable to reconfigure ccp
     */
    public boolean reconfigureConnectorConnectionPool(ConnectorConnectionPool
            ccp, Set excludedProps) throws ConnectorRuntimeException {
        return ccPoolAdmService.reconfigureConnectorConnectionPool(
                ccp, excludedProps);
    }

    /**
     * Recreate a connector connection pool. This method essentially does
     * the following things:
     * 1. Delete the said connector connection pool<br>
     * 2. Bind the pool to JNDI<br>
     * 3. Create an MCF for this pool and register with the connector registry<br>
     *
     * @param ccp - the ConnectorConnectionPool to publish
     * @throws ConnectorRuntimeException when unable to recreate ccp
     */
    public void recreateConnectorConnectionPool(ConnectorConnectionPool ccp)
            throws ConnectorRuntimeException {
        ccPoolAdmService.recreateConnectorConnectionPool(ccp);
    }

    /**
     * Creates connector connection pool in the connector container.
     *
     * @param ccp                      ConnectorConnectionPool instance to be bound to JNDI. This
     *                                 object contains the pool properties.
     * @param connectionDefinitionName Connection definition name against which
     *                                 connection pool is being created
     * @param rarName                  Name of the resource adapter
     * @param props                    Properties of MCF which are present in domain.xml
     *                                 These properties override the ones present in ra.xml
     * @param securityMaps             Array fo security maps.
     * @throws ConnectorRuntimeException When creation of pool fails.
     */
    public void createConnectorConnectionPool(ConnectorConnectionPool ccp,
                                              String connectionDefinitionName, String rarName,
                                              List<Property> props,
                                              List<SecurityMap> securityMaps)
            throws ConnectorRuntimeException {
        ccPoolAdmService.createConnectorConnectionPool(ccp, connectionDefinitionName, rarName, props, securityMaps);
    }


    /**
     * {@inheritDoc}
     */
    public boolean checkAndLoadResource(Object resource, Object pool, String resourceType, String resourceName,
                                        String raName) throws ConnectorRuntimeException {
        return connectorService.checkAndLoadResource(resource, pool, resourceType, resourceName, raName);
    }

    /**
     * {@inheritDoc}
     */
    public void cleanUpResourcesAndShutdownAllActiveRAs(){
        poolManager.killFreeConnectionsInPools();
        resourceAdapterAdmService.stopAllActiveResourceAdapters();
    }
    /**
     * {@inheritDoc}
     */
    public void shutdownAllActiveResourceAdapters() {
        resourceAdapterAdmService.stopAllActiveResourceAdapters();
    }

    public PoolManager getPoolManager() {
        return poolManager;
    }

    /**
     * provides the invocationManager
     *
     * @return InvocationManager
     */
    public InvocationManager getInvocationManager() {
        return invocationManager;
    }

    public Timer getTimer() {
        return ConnectorTimerProxy.getProxy();        
    }

    /**
     * get resource reference descriptors from current component's jndi environment
     *
     * @return set of resource-refs
     */
    public Set getResourceReferenceDescriptor() {
        JndiNameEnvironment jndiEnv = componentEnvManager.getCurrentJndiNameEnvironment();
        if (jndiEnv != null) {
            return jndiEnv.getResourceReferenceDescriptors();
        } else {
            return null;
        }
    }

    /**
     * The component is about to be removed from commission
     */
    public void preDestroy() {
    }

    /**
     * Obtain the authentication service associated with rar module.
     * Currently only the BasicPassword authentication is supported.
     *
     * @param rarName  Rar module Name
     * @param poolName Name of the pool. Used for creation of
     *                 BasicPasswordAuthenticationService
     * @return AuthenticationService connector runtime's authentication service
     */
    public AuthenticationService getAuthenticationService(String rarName,
                                                          String poolName) {

        return connectorSecurityAdmService.getAuthenticationService(rarName, poolName);
    }

    /**
     * Checks whether the executing environment is application server
     * @return true if execution environment is server
     *         false if it is client
     */
    public boolean isServer() {
        return getEnvironment() == SERVER;
    }

    /**
     * Checks whether the executing environment is appclient container runtime
     * @return true if execution environment is appclient container
     *         false if it is not ACC
     */
    public boolean isAppClientRuntime() {
        return processEnvironment.getProcessType().equals(ProcessEnvironment.ProcessType.ACC);
    }

    /**
     * provides the current transaction
     * @return Transaction
     * @throws SystemException when unable to get the transaction
     */
    public Transaction getTransaction() throws SystemException {
        return getTransactionManager().getTransaction();
    }

    /**
     * provides the transactionManager
     * @return TransactionManager
     */
    public JavaEETransactionManager getTransactionManager() {
        if (transactionManager == null) {
            transactionManager = habitat.getComponent(JavaEETransactionManager.class);
        }
        return transactionManager;
    }

    /**
     * Gets Connector Resource Rebind Event notifier.
     *
     * @return ConnectorNamingEventNotifier
     */
    private ConnectorNamingEventNotifier getResourceRebindEventNotifier() {
        return connectorResourceAdmService.getResourceRebindEventNotifier();
    }

    /**
     * register the connector naming event listener
     * @param listener connector-naming-event-listener
     */
    public void registerConnectorNamingEventListener(ConnectorNamingEventListener listener){
        connectorResourceAdmService.getResourceRebindEventNotifier().addListener(listener);
    }

    /**
     * unregister the connector naming event listner
     * @param listener connector-naming-event-listener
     */
    public void unregisterConnectorNamingEventListener(ConnectorNamingEventListener listener){
        connectorResourceAdmService.getResourceRebindEventNotifier().removeListener(listener);
    }


    public ResourcePool getConnectionPoolConfig(String poolName) {
        return ConnectorsUtil.getConnectionPoolConfig(poolName, allResources.getComponent(Resources.class));
    }


    public boolean pingConnectionPool(String poolName) throws ResourceException {
        return ccPoolAdmService.testConnectionPool(poolName);
    }

    public PoolType getPoolType(String poolName) throws ConnectorRuntimeException {
        return ccPoolAdmService.getPoolType(poolName);
    }

    /**
     * provides work manager proxy that is Serializable
     *
     * @param poolId     ThreadPoolId
     * @param moduleName resource-adapter name
     * @return WorkManager
     * @throws ConnectorRuntimeException when unable to get work manager
     */
    public WorkManager getWorkManagerProxy(String poolId, String moduleName) throws ConnectorRuntimeException {
        //TODO V3 can't we make work-manager to return proxy by default ?
        return habitat.getComponent(WorkManagerFactory.class).getWorkManagerProxy(poolId, moduleName);
    }

    /**
     * provides XATerminator proxy that is Serializable
     * @param moduleName resource-adapter name
     * @return XATerminator
     */
    public XATerminator getXATerminatorProxy(String moduleName){
        XATerminator xat = getTransactionManager().getXATerminator();
        return new XATerminatorProxy(xat);
    }

    public void removeWorkManagerProxy(String moduleName) {
        habitat.getComponent(WorkManagerFactory.class).removeWorkManager(moduleName);
    }

    public void addAdminObject(String appName, String connectorName,
                               String jndiName, String adminObjectType, String adminObjectClassName, Properties props)
            throws ConnectorRuntimeException {
        adminObjectAdminService.addAdminObject(appName, connectorName, jndiName, adminObjectType, adminObjectClassName,
                props);
    }

    public void deleteAdminObject(String jndiName) throws ConnectorRuntimeException {
        adminObjectAdminService.deleteAdminObject(jndiName);
    }

    public ClassLoader getConnectorClassLoader(String rarName) throws ConnectorRuntimeException{
        return cclUtil.getSystemRARClassLoader(rarName);
    }

    /**
     * Given the module directory, creates a connector-class-finder (class-loader) for the module
     * @param moduleDirectory rar module directory for which classloader is needed
     * @param parent parent classloader<br>
     * For standalone rars, pass null, as the parent should be common-class-loader
     * that will be automatically taken care by ConnectorClassLoaderService.<br>
     * For embedded rars, parent is necessary<br>
     * @return classloader created for the module
     */
    public ClassLoader createConnectorClassLoader(String moduleDirectory, ClassLoader parent, String rarModuleName)
            throws ConnectorRuntimeException{
        List<URI> libraries = ConnectorsUtil.getInstalledLibrariesFromManifest(moduleDirectory, env);
        return cclUtil.createRARClassLoader(moduleDirectory, parent, rarModuleName, libraries);
    }

    public ResourceDeployer getResourceDeployer(Object resource){
        Collection<ResourceDeployer> deployers = deployerHabitat.getAllByContract(ResourceDeployer.class);

        for(ResourceDeployer deployer : deployers){
            if(deployer.handles(resource)){
                return deployer;
            }
        }
        return null;
    }

    /** Add the resource adapter configuration to the connector registry
     *  @param rarName rarmodule
     *  @param raConfig Resource Adapter configuration object
     *  @throws ConnectorRuntimeException if the addition fails.
     */

    public void addResourceAdapterConfig(String rarName,
           ResourceAdapterConfig raConfig) throws ConnectorRuntimeException {
        resourceAdapterAdmService.addResourceAdapterConfig(rarName,raConfig);
    }

    /** Delete the resource adapter configuration to the connector registry
     *  @param rarName rarmodule
     */

    public void deleteResourceAdapterConfig(String rarName) throws ConnectorRuntimeException {
        resourceAdapterAdmService.deleteResourceAdapterConfig(rarName);
    }

    /**
     * register the connector application with registry
     * @param rarModule resource-adapter module
     */
    public void registerConnectorApplication(ConnectorApplication rarModule){
        connectorRegistry.addConnectorApplication(rarModule);
    }

    /**
     * unregister the connector application from registry
     * @param rarName resource-adapter name
     */
    public void unregisterConnectorApplication(String rarName){
        connectorRegistry.removeConnectorApplication(rarName);
    }

    /**
     * undeploy resources of the module
     * @param rarName resource-adapter name
     */
    public void undeployResourcesOfModule(String rarName){
        ConnectorApplication app = connectorRegistry.getConnectorApplication(rarName);
        app.undeployResources();
    }

    /**
     * deploy resources of the module
     * @param rarName resource-adapter name
     */
    public void deployResourcesOfModule(String rarName){
        ConnectorApplication app = connectorRegistry.getConnectorApplication(rarName);
        app.deployResources();
    }
    public ActiveRAFactory getActiveRAFactory(){
        return activeRAFactory;
    }

    public Applications getApplications() {
        return applications.getComponent(Applications.class);
    }

    public ApplicationRegistry getAppRegistry() {
        return habitat.getComponent(ApplicationRegistry.class);
    }

    public ApplicationArchivist getApplicationArchivist(){
        return habitat.getComponent(ApplicationArchivist.class);
    }

    public FileArchive getFileArchive(){
        return habitat.getComponent(FileArchive.class);
    }

    public void createActiveResourceAdapterForEmbeddedRar(String rarModuleName) throws ConnectorRuntimeException {
        connectorService.createActiveResourceAdapterForEmbeddedRar(rarModuleName);
    }

    /**
     * Check whether ClassLoader is permitted to access this resource adapter.
     * If the RAR is deployed and is not a standalone RAR, then only the ClassLoader
     * that loaded the archive (any of its child) should be able to access it. Otherwise everybody can
     * access the RAR.
     *
     * @param rarName Resource adapter module name.
     * @param loader  <code>ClassLoader</code> to verify.
     */
    public boolean checkAccessibility(String rarName, ClassLoader loader) {
        return connectorService.checkAccessibility(rarName, loader);
    }

    public void loadDeferredResourceAdapter(String rarName)
                        throws ConnectorRuntimeException {
        connectorService.loadDeferredResourceAdapter(rarName);
    }

    public SecurityRoleMapperFactory getSecurityRoleMapperFactory() {
        return habitat.getComponent(SecurityRoleMapperFactory.class);
    }

    /**
     * {@inheritDoc}
     */
    public CallbackHandler getCallbackHandler(){
        //TODO V3 hack to make sure that SecurityServicesUtil is initialized before ContainerCallbackHander
        habitat.getComponent(SecurityServicesUtil.class);
        return habitat.getComponent(ContainerCallbackHandler.class);
    }

    //TODO V3 can this impl be moved somewhere ?
    public ConnectorArchivist getConnectorArchvist() throws ConnectorRuntimeException {
        try{
            ArchivistFactory archivistFactory = habitat.getComponent(ArchivistFactory.class);
            return (ConnectorArchivist)archivistFactory.getArchivist(XModuleType.RAR);
        }catch(IOException ioe){
            _logger.log(Level.WARNING, "unable to get Connector Archivist : ", ioe);
            ConnectorRuntimeException cre = new ConnectorRuntimeException(ioe.getMessage());
            cre.setStackTrace(ioe.getStackTrace());
            throw cre;
        }
    }

    public WorkContextHandler getWorkContextHandler(){
        return habitat.getComponent(WorkContextHandler.class);
    }


    public ComponentEnvManager getComponentEnvManager(){
        return componentEnvManager;
    }

    /**
     * {@inheritDoc}
     */
    public DelegatingClassLoader getConnectorClassLoader() {
        return clh.getConnectorClassLoader(null);
    }

    /**
     * {@inheritDoc}
     */
    public List<WorkSecurityMap> getWorkSecurityMap(String raName){
        return ConnectorsUtil.getWorkSecurityMaps(raName, allResources.getComponent(Resources.class));
    }

    /**
     * Gets the shutdown-timeout attribute configured in <code>connector-service</code>
     * @return long shutdown timeout (in milli-seconds)
     */
    public long getShutdownTimeout() {
        return ConnectorsUtil.getShutdownTimeout(
                habitat.getComponent(com.sun.enterprise.config.serverbeans.ConnectorService.class));
    }
    
    /**
     * Flush Connection pool by reinitializing the connections 
     * established in the pool.
     * @param poolName
     * @throws com.sun.appserv.connectors.internal.api.ConnectorRuntimeException
     */
    public boolean flushConnectionPool(String poolName) throws ConnectorRuntimeException {
        return ccPoolAdmService.flushConnectionPool(poolName);
    }

    /**
     * Get Validation table names list for the database that the jdbc 
     * connection pool refers to. This is used for connection validation.
     * @param poolName
     * @return all validation table names.
     * @throws javax.resource.ResourceException
     * @throws javax.naming.NamingException
     */        
    public Set<String> getValidationTableNames(String poolName) throws ResourceException {
        return jdbcAdminService.getValidationTableNames(poolName);
    }

    /**
     * Get Validation class names list for the database vendor that the jdbc 
     * connection pool refers to. This is used for custom connection validation.
     * @param dbVendor
     * @return all validation class names.
     */        
    public Set<String> getValidationClassNames(String dbVendor) {
        return jdbcAdminService.getValidationClassNames(dbVendor);
    }
    
    /**
     * Get jdbc driver implementation class names list for the dbVendor and
     * resource type supplied. 
     * @param dbVendor
     * @param resType
     * @return all jdbc driver implementation class names
     */
    public Set<String> getJdbcDriverClassNames(String dbVendor, String resType) {
        return driverLoader.getJdbcDriverClassNames(dbVendor, resType);
    }

    /**
     * returns the bean validator that can be used to validate various connector bean artifacts
     * (ResourceAdapter, ManagedConnetionFactory, AdministeredObject, ActivationSpec)
     * @return ConnectorBeanValidator
     */
    public ConnectorJavaBeanValidator getConnectorBeanValidator(){
        return connectorBeanValidator;    
    }

    /**
     * Check if ping is on during pool creation. 
     * 
     * @param poolName
     * @return
     */
    public boolean getPingDuringPoolCreation(String poolName) {
        return ConnectorsUtil.getPingDuringPoolCreation(poolName, 
                allResources.getComponent(Resources.class));
    }
    
    /**
     * Get jdbc database vendor names list.
     * @return set of common database vendor names
     */
    public Set<String> getDatabaseVendorNames() {
        return driverLoader.getDatabaseVendorNames();
    }    
}
