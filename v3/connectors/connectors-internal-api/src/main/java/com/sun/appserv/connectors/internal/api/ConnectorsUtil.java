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
package com.sun.appserv.connectors.internal.api;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.logging.LogDomains;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.admin.*;
import org.glassfish.deployment.common.InstalledLibrariesResolver;
import org.glassfish.loader.util.ASClassLoaderUtil;

/**
 * Util class for connector related classes
 */
public class ConnectorsUtil {

    private static Logger _logger= LogDomains.getLogger(ConnectorsUtil.class, LogDomains.RSR_LOGGER);

    /**
     * determine whether the RAR in question is a System RAR
     * @param raName RarName
     * @return boolean
     */
    public static boolean belongsToSystemRA(String raName) {
        boolean result = false;

        for (String systemRarName : ConnectorConstants.systemRarNames) {
            if (systemRarName.equals(raName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static boolean getPingDuringPoolCreation(String poolName, Resources allResources) {
        boolean pingOn = false;
        ResourcePool pool = getConnectionPoolConfig(poolName, allResources);
        if(pool instanceof JdbcConnectionPool) {
            JdbcConnectionPool jdbcPool = (JdbcConnectionPool) pool;
            pingOn = Boolean.parseBoolean(jdbcPool.getPing());
        } else if (pool instanceof com.sun.enterprise.config.serverbeans.ConnectorConnectionPool) {
            com.sun.enterprise.config.serverbeans.ConnectorConnectionPool ccPool = 
                    (com.sun.enterprise.config.serverbeans.ConnectorConnectionPool) pool;
            pingOn = Boolean.parseBoolean(ccPool.getPing());                    
        }
        return pingOn;                        
    }

    /**
     * get the installation directory of System RARs
     * @param moduleName RARName
     * @return directory location
     */
    public static String getSystemModuleLocation(String moduleName) {
        String j2eeModuleDirName = System.getProperty(ConnectorConstants.INSTALL_ROOT) +
                File.separator + "lib" +
                File.separator + "install" +
                File.separator + "applications" +
                File.separator + moduleName;

        return j2eeModuleDirName;
    }

    public static String getLocation(String moduleName) {
        return ConfigBeansUtilities.getLocation(moduleName);
        /* TODO V3

            if(moduleName == null) {
                return null;
            }
            String location  = null;
            ConnectorModule connectorModule =
                    dom.getApplications().getConnectorModuleByName(moduleName);
            if(connectorModule != null) {
                location = RelativePathResolver.
                        resolvePath(connectorModule.getLocation());
            }
            return location;
        */


    }
    /**
     *  Return the system PM name for the JNDI name
     * @param  jndiName jndi name
     * @return String jndi name for PM resource
     **/
    public  static String getPMJndiName( String jndiName )  {
        return jndiName + ConnectorConstants.PM_JNDI_SUFFIX;
    }

    /**
     * check whether the jndi Name has connector related suffix and return if any.
     * @param name jndi name
     * @return suffix, if found
     */
    public static String getValidSuffix(String name) {
        if (name != null) {
            for (String validSuffix : ConnectorConstants.JNDI_SUFFIX_VALUES) {
                if (name.endsWith(validSuffix)) {
                    return validSuffix;
                }
            }
        }
        return null;
    }

    /**
     * If the suffix is one of the valid context return true.
     * Return false, if that is not the case.
     *
     * @param suffix __nontx / __pm
     * @return boolean whether the suffix is valid or not
     */
    public static boolean isValidJndiSuffix(String suffix) {
        if (suffix != null) {
            for (String validSuffix : ConnectorConstants.JNDI_SUFFIX_VALUES) {
                if (validSuffix.equals(suffix)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Given the name of the resource and its jndi env, derive the complete jndi name. (eg; with __PM / __nontx)
     * @param name name of the resource
     * @param env env
     * @return derived name
     */
    public static String deriveJndiName(String name, Hashtable env) {
        String suffix = (String) env.get(ConnectorConstants.JNDI_SUFFIX_PROPERTY);
        if (ConnectorsUtil.isValidJndiSuffix(suffix)) {
            return name + suffix;
        }
        return name;
    }

    public static boolean isValidEventType(Object instance) {
        return (instance instanceof JdbcConnectionPool || 
                instance instanceof JdbcResource ||
                instance instanceof ConnectorConnectionPool ||
                instance instanceof ConnectorResource ||
                instance instanceof MailResource ||
                instance instanceof ExternalJndiResource ||
                instance instanceof CustomResource ||
                instance instanceof AdminObjectResource ||
                instance instanceof WorkSecurityMap ||
                instance instanceof ResourceAdapterConfig ) ;
    }

    /**
     * given a jdbc-resource, get associated jdbc-connection-pool
     * @param resource jdbc-resource
     * @return jdbc-connection-pool
     */
    public static JdbcConnectionPool getAssociatedJdbcConnectionPool(JdbcResource resource, Resources allResources) {
        //TODO V3 need to find a generic way (instead of separate methods for jdbc/connector)
        for(Resource configuredResource : allResources.getResources()){
            if(configuredResource instanceof JdbcConnectionPool){
                JdbcConnectionPool pool = (JdbcConnectionPool)configuredResource;
                if(resource.getPoolName().equalsIgnoreCase(pool.getName())){
                    return pool;
                }
            }
        }
        return null;  //TODO V3 cannot happen ?
    }

    /**
     * given a connector-resource, get associated connector-connection-pool
     * @param resource connector-resource
     * @return connector-connection-pool
     */
    public static ConnectorConnectionPool getAssociatedConnectorConnectionPool(ConnectorResource resource,
                                                                               Resources allResources) {
        for(Resource configuredResource : allResources.getResources()){
            if(configuredResource instanceof ConnectorConnectionPool){
                ConnectorConnectionPool pool = (ConnectorConnectionPool)configuredResource;
                if(resource.getPoolName().equalsIgnoreCase(pool.getName())){
                    return pool;
                }
            }
        }
        return null;  //TODO V3 cannot happen ?
    }

    public static ResourcePool getConnectionPoolConfig(String poolName, Resources allResources){
        for(Resource configuredResource : allResources.getResources()){
            if(configuredResource instanceof ResourcePool){
                ResourcePool pool = (ResourcePool)configuredResource;
                if(pool.getName().equalsIgnoreCase(poolName)){
                    return pool;
                }
            }
        }
        return null; //TODO V3 cannot happen ?
    }

    public static Collection<Resource> getAllResources(Collection<String> poolNames, Resources allResources) {
        List<Resource> connectorResources = new ArrayList<Resource>();
        for(Resource resource : allResources.getResources()){
            if(resource instanceof ConnectorResource){
                ConnectorResource connectorResource = (ConnectorResource)resource;
                if(poolNames.contains(connectorResource.getPoolName())){
                    connectorResources.add(connectorResource);
                }
            }
        }
        return connectorResources;
    }

    /**
     * get the list of pool names
     * @param connectionPools list of pools
     * @return list of pol names
     */
    public static Collection<String> getAllPoolNames(Collection<ConnectorConnectionPool> connectionPools) {
        Set<String> poolNames = new HashSet<String>();
        for(ConnectorConnectionPool pool : connectionPools){
            poolNames.add(pool.getName());
        }
        return poolNames;
    }

    public static Collection<WorkSecurityMap> getAllWorkSecurityMaps(Resources resources, String moduleName){
        List<WorkSecurityMap> workSecurityMaps = new ArrayList<WorkSecurityMap>();
        for(WorkSecurityMap resource : resources.getResources(WorkSecurityMap.class)){
            if(resource.getResourceAdapterName().equals(moduleName)){
                workSecurityMaps.add(resource);
            }
        }
        return workSecurityMaps;
    }

    /**
     * get the pools for a particular resource-adapter
     * @param moduleName resource-adapter name
     * @return collection of connectorConnectionPool
     */
    public static Collection<ConnectorConnectionPool> getAllPoolsOfModule(String moduleName, Resources allResources) {
        List<ConnectorConnectionPool> connectorConnectionPools = new ArrayList<ConnectorConnectionPool>();
        for(Resource resource : allResources.getResources()){
            if(resource instanceof ConnectorConnectionPool){
                ConnectorConnectionPool connectorConnectionPool = (ConnectorConnectionPool)resource;
                if(connectorConnectionPool.getResourceAdapterName().equals(moduleName)){
                    connectorConnectionPools.add(connectorConnectionPool);
                }
            }
        }
        return connectorConnectionPools;
    }

    /**
     * Get all System RAR pools and resources
     * @param allResources all configured resources
     * @return Collection of system RAR pools
     */
    public static Collection<Resource> getAllSystemRAResourcesAndPools(Resources allResources) {
        //Make sure that resources are added first and then pools.
        List<Resource> resources = new ArrayList<Resource>();
        List<Resource> pools = new ArrayList<Resource>();
        for(Resource resource : allResources.getResources()){
            if(resource instanceof JdbcConnectionPool ){
                pools.add(resource);
            } else if( resource instanceof ConnectorConnectionPool){
                String raName = ((ConnectorConnectionPool)resource).getResourceAdapterName();
                if( ConnectorsUtil.belongsToSystemRA(raName) ){
                    pools.add(resource);
                }
            } else if(resource instanceof JdbcResource){
                resources.add(resource);
            } else if( resource instanceof ConnectorResource){
                String poolName = ((ConnectorResource)resource).getPoolName();
                String raName = getResourceAdapterNameOfPool(poolName, allResources);
                if( ConnectorsUtil.belongsToSystemRA(raName) ){
                    resources.add(resource);
                }
            } else if (resource instanceof AdminObjectResource){ // jms-ra
                String raName = ((AdminObjectResource)resource).getResAdapter();
                if(ConnectorsUtil.belongsToSystemRA(raName)){
                    resources.add(resource);
                }
            } //no need to list work-security-map as they are not deployable artifacts
        }
        resources.addAll(pools);
        return resources;
    }

    /**
     * Given the poolname, retrieve the resourceadapter name
     * @param poolName connection pool name
     * @param allResources resources
     * @return resource-adapter name
     */
    public static String getResourceAdapterNameOfPool(String poolName, Resources allResources) {
        String raName = ""; //TODO V3 this need not be initialized to ""
        for(Resource resource : allResources.getResources()){
            if(resource instanceof ConnectorConnectionPool){
                ConnectorConnectionPool ccp = (ConnectorConnectionPool)resource;
                String name = ccp.getName();
                if(name.equalsIgnoreCase(poolName)){
                    raName = ccp.getResourceAdapterName();
                }
            }
        }
        return raName;
    }

    public static ResourceAdapterConfig getRAConfig(String raName, Resources allResources) {
        Collection<ResourceAdapterConfig> raConfigs = allResources.getResources(ResourceAdapterConfig.class);
        for(ResourceAdapterConfig rac : raConfigs){
            if(rac.getResourceAdapterName().equals(raName)){
                return rac;
            }
        }
        return null;
    }

    /**
     * given the ra-name, returns all the configured connector-work-security-maps for the .rar
     * @param raName resource-adapter name
     * @param allResources resources
     * @return list of work-security-maps
     */
    public static List<WorkSecurityMap> getWorkSecurityMaps(String raName, Resources allResources){
        List<Resource> resourcesList = allResources.getResources();
        List<WorkSecurityMap> workSecurityMaps = new ArrayList<WorkSecurityMap>();
        for(Resource resource : resourcesList){
            if(resource instanceof WorkSecurityMap){
                WorkSecurityMap wsm = (WorkSecurityMap)resource;
                if(wsm.getResourceAdapterName().equals(raName)){
                    workSecurityMaps.add(wsm);
                }
            }
        }
        return workSecurityMaps;
    }
    public static AdminObjectResource[] getEnabledAdminObjectResources(String raName, Resources allResources,
                                                                       Server server)  {
        List resourcesList = allResources.getResources();
        int resourceCount = resourcesList.size();      //sizeAdminObjectResource();
        if(resourceCount == 0) {
            return null;
        }
        List<AdminObjectResource> adminObjectResources = new ArrayList<AdminObjectResource>();
        for(int i=0; i< resourceCount; ++i) {
             Resource resource = (Resource)resourcesList.get(i);

            if(resource == null || !(resource instanceof AdminObjectResource))
                continue;
            AdminObjectResource adminObjectResource = (AdminObjectResource)resource;
            String resourceAdapterName = adminObjectResource.getResAdapter();

            if(resourceAdapterName == null)
                continue;
            if(raName!= null && !raName.equals(resourceAdapterName))
                continue;


            // skips the admin resource if it is not referenced by the server
            if(!isEnabled(adminObjectResource, server))
                continue;
            adminObjectResources.add(adminObjectResource);
        }
        AdminObjectResource[] allAdminObjectResources =
                    new AdminObjectResource[adminObjectResources.size()];
        return adminObjectResources.toArray(allAdminObjectResources);
    }

     public static boolean isEnabled(AdminObjectResource aot, Server server) {
        if(aot == null || !Boolean.parseBoolean(aot.getEnabled()))
            return false;
        if(!isResourceReferenceEnabled(aot.getJndiName(), server))
            return false;

        String raName = aot.getResAdapter();
        return isRarEnabled(raName);
    }

     /**
     * Checks if a resource reference is enabled
     * @since SJSAS 8.1 PE/SE/EE
     */
    private static boolean isResourceReferenceEnabled(String resourceName, Server server) {

        ResourceRef ref  = null;
        //TODO V3 server should not be null.
        if(server != null){
            ref = server.getResourceRef(resourceName);
        }

        if (ref == null) {
            _logger.fine("ConnectorsUtil :: isResourceReferenceEnabled null ref");
            //todo for V3
            // if(isADeployEvent())
                return true;
            //else
              //  return false;
        }
        _logger.fine("ConnectorsUtil :: isResourceReferenceEnabled ref enabled ?" +
                Boolean.parseBoolean(ref.getEnabled()));
        return  Boolean.parseBoolean(ref.getEnabled());
    }

     private static boolean isRarEnabled(String raName) {
       //todo: for v3
       return true;
       /* if(raName == null || raName.length() == 0)
            return false;
        ConnectorModule module = dom.getApplications().getConnectorModuleByName(raName);
        if(module != null) {
            if(!module.isEnabled())
                return false;
            return isApplicationReferenceEnabled(raName);
        } else if(belongToSystemRar(raName)) {
            return true;
        } else {
            return belongToEmbeddedRarAndEnabled(raName);
        }  */
    }

    public static String getResourceType(Resource resource){
        if(resource instanceof JdbcResource){
            return ConnectorConstants.RES_TYPE_JDBC;
        } else if(resource instanceof JdbcConnectionPool){
            return ConnectorConstants.RES_TYPE_JCP;
        } else if (resource instanceof ConnectorResource){
            return ConnectorConstants.RES_TYPE_CR;
        } else if (resource instanceof ConnectorConnectionPool){
            return ConnectorConstants.RES_TYPE_CCP;
        } else if (resource instanceof MailResource){
            return ConnectorConstants.RES_TYPE_MAIL;
        } else if( resource instanceof ExternalJndiResource){
            return ConnectorConstants.RES_TYPE_EXTERNAL_JNDI;
        } else if (resource instanceof CustomResource){
            return ConnectorConstants.RES_TYPE_CUSTOM;
        } else if (resource instanceof AdminObjectResource){
            return ConnectorConstants.RES_TYPE_AOR;
        } else if (resource instanceof ResourceAdapterConfig){
            return ConnectorConstants.RES_TYPE_RAC;
        } else if (resource instanceof WorkSecurityMap){
            return ConnectorConstants.RES_TYPE_CWSM;
        } else {
            return null;
            //TODO V3 log and throw exception
        }
    }

    /**
     * load and create an object instance
     */
    public static Object loadObject(String className) {
        Object obj = null;
        Class c;

        try {
            //TODO V3 correct approach ?
            obj = Class.forName(className).newInstance();
        } catch (Exception cnf) {
            try {
                //TODO V3 not needed ?
                // c = ClassLoader.getSystemClassLoader().loadClass(className);
                //TODO V3 correct approach ?
                c = Thread.currentThread().getContextClassLoader().loadClass(className);
                obj = c.newInstance();
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, "classloader.load_class_fail", className);
                _logger.log(Level.SEVERE, "classloader.load_class_fail_excp", ex.getMessage());

            }
        }
        return obj;
    }

    /**
     * Prepares the name/value pairs for ActivationSpec. <p>
     * Rule: <p>
     * 1. The name/value pairs are the union of activation-config on
     * standard DD (message-driven) and runtime DD (mdb-resource-adapter)
     * 2. If there are duplicate property settings, the value in runtime
     * activation-config will overwrite the one in the standard
     * activation-config.
     */
    public static Set getMergedActivationConfigProperties(EjbMessageBeanDescriptor msgDesc) {

        Set mergedProps = new HashSet();
        Set runtimePropNames = new HashSet();

        Set runtimeProps = msgDesc.getRuntimeActivationConfigProperties();
        if (runtimeProps != null) {
            Iterator iter = runtimeProps.iterator();
            while (iter.hasNext()) {
                EnvironmentProperty entry = (EnvironmentProperty) iter.next();
                mergedProps.add(entry);
                String propName = (String) entry.getName();
                runtimePropNames.add(propName);
            }
        }

        Set standardProps = msgDesc.getActivationConfigProperties();
        if (standardProps != null) {
            Iterator iter = standardProps.iterator();
            while (iter.hasNext()) {
                EnvironmentProperty entry = (EnvironmentProperty) iter.next();
                String propName = (String) entry.getName();
                if (runtimePropNames.contains(propName))
                    continue;
                mergedProps.add(entry);
            }
        }

        return mergedProps;
    }

    public static boolean isJMSRA(String moduleName) {
        if(ConnectorConstants.DEFAULT_JMS_ADAPTER.equals(moduleName)){
            return true;
        }
        return false;
    }

    public static boolean parseBoolean(String enabled) {
        return Boolean.parseBoolean(enabled.toString());
    }

    /**
     * given a resource config bean, returns the resource name / jndi-name
     * @param resource
     * @return resource name / jndi-name
     */
    public static String getResourceName(Resource resource){
        if(resource instanceof BindableResource){
            return ((BindableResource)resource).getJndiName();
        }else if (resource instanceof ResourcePool){
            return ((ResourcePool)resource).getName();
        }else if (resource instanceof ResourceAdapterConfig){
            return ((ResourceAdapterConfig)resource).getName();
        }else if (resource instanceof WorkSecurityMap){
            //TODO V3 toString duckType for WorkSecurityMap config bean ?
            WorkSecurityMap wsm = (WorkSecurityMap)resource;
            return ("resource-adapter name : " + wsm.getResourceAdapterName()
                    + " : security map name : " +  wsm.getName());
        }
        return null;
    }

    /**
     * Gets the shutdown-timeout attribute from domain.xml
     * via the connector server config bean.
     * @param connectorService connector-service configuration
     * @return long shutdown timeout (in mill-seconds)
     */
    public static long getShutdownTimeout(ConnectorService connectorService)  {
        int shutdownTimeout;

        try {
            if (connectorService == null) {
                //Connector service element is not specified in
                //domain.xml and hence going with the default time-out
                shutdownTimeout =
                        ConnectorConstants.DEFAULT_RESOURCE_ADAPTER_SHUTDOWN_TIMEOUT;
                _logger.log(Level.FINE, "Shutdown timeout set to "+  shutdownTimeout + " through default");
            } else {
                shutdownTimeout = Integer.parseInt(connectorService.getShutdownTimeoutInSeconds());
                _logger.log(Level.FINE, "Shutdown timeout set to " + shutdownTimeout + " from domain.xml");
            }
        } catch (Exception e) {
            _logger.log(Level.WARNING, "error_reading_connectorservice_elt", e);
            //Going ahead with the default timeout value
            shutdownTimeout = ConnectorConstants.DEFAULT_RESOURCE_ADAPTER_SHUTDOWN_TIMEOUT;
        }
        return shutdownTimeout * 1000L;
    }

    /**
     * Provides the list of built in custom resources by
     * resource-type and factory-class-name pair.
     * @return map of resource-type & factory-class-name
     */
    public static Map<String,String> getBuiltInCustomResources(){
        Map<String, String> resourcesMap = new HashMap<String, String>();

        // user will have to provide the JavaBean Implementation class and hence we cannot list this factory
        // resourcesMap.put("JavaBean", ConnectorConstants.JAVA_BEAN_FACTORY_CLASS );

        resourcesMap.put("java.lang.Integer", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS );
        resourcesMap.put("java.lang.Long", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS );
        resourcesMap.put("java.lang.Double", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS );
        resourcesMap.put("java.lang.Float", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS );
        resourcesMap.put("java.lang.Character", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS );
        resourcesMap.put("java.lang.Short", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS );
        resourcesMap.put("java.lang.Byte", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS );
        resourcesMap.put("java.lang.Boolean", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS );
        resourcesMap.put("java.lang.String", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS );

        resourcesMap.put("java.net.URL", ConnectorConstants.URL_OBJECTS_FACTORY );

        resourcesMap.put("java.util.Properties", ConnectorConstants.PROPERTIES_FACTORY );

        return resourcesMap;
    }

    public static String getTransactionIsolationInt(int tranIsolation) {

        if(tranIsolation == Connection.TRANSACTION_READ_UNCOMMITTED){
            return "read-uncommited";
        } else if(tranIsolation == Connection.TRANSACTION_READ_COMMITTED){
            return "read-committed";
        } else if(tranIsolation == Connection.TRANSACTION_REPEATABLE_READ){
            return "repeatable-read";
        } else if(tranIsolation == Connection.TRANSACTION_SERIALIZABLE){
            return "serializable";
        } else {
            throw new RuntimeException("Invalid transaction isolation; the transaction "
                    + "isolation level can be empty or any of the following: "
                    + "read-uncommitted, read-committed, repeatable-read, serializable");
        }
    }

    public static String deriveDataSourceDefinitionResourceName(String compId, String name) {
        //String derivedName = escapeJavaName(name);
        String derivedName = name;
        return getReservePrefixedJNDINameForDataSourceDefinitionResource(compId, derivedName);
    }

    public static String deriveDataSourceDefinitionPoolName(String compId, String name) {
        //String derivedName = escapeJavaName(name);
        String derivedName = name;
        return getReservePrefixedJNDINameForDataSourceDefinitionPool(compId, derivedName);
    }

    private static String escapeJavaName(String name) {
        if (name != null) {
            //replace all 'delimiter' to double delimiter
            name = name.replace("-", "--");
            //replace '/' to 'delimiter'
            name = name.replace("/", "-");
            if (name.contains("java:") || name.contains(":")) {
                name = name.replace(":", "-");
            } else {
                name = "java-" + name;
            }
        }
        return name;
    }

    public static Map<String,String> convertPropertiesToMap(Properties properties){
        if(properties == null){
            properties = new Properties();
        }
        return new TreeMap<String, String>((Map) properties);
    }

    public static String getReservePrefixedJNDINameForDataSourceDefinitionPool(String compId, String poolName) {
        String prefix = null;
        if(compId == null || compId.equals("")){
            prefix = ConnectorConstants.POOLS_JNDINAME_PREFIX +
                ConnectorConstants.DATASOURCE_DEFINITION_JNDINAME_PREFIX ;
        }else{
            prefix = ConnectorConstants.POOLS_JNDINAME_PREFIX +
                ConnectorConstants.DATASOURCE_DEFINITION_JNDINAME_PREFIX + compId +"/";
        }
        return getReservePrefixedJNDIName(prefix, poolName);
    }

    private static String getReservePrefixedJNDIName(String prefix, String resourceName) {
        return prefix + resourceName;
    }

    public static String getReservePrefixedJNDINameForDataSourceDefinitionResource(String compId, String resourceName) {
        String prefix = null;
        if(compId == null || compId.equals("")){
            prefix = ConnectorConstants.RESOURCE_JNDINAME_PREFIX +
                ConnectorConstants.DATASOURCE_DEFINITION_JNDINAME_PREFIX ;
        }else{
            prefix = ConnectorConstants.RESOURCE_JNDINAME_PREFIX +
                ConnectorConstants.DATASOURCE_DEFINITION_JNDINAME_PREFIX + compId +"/";
        }
        return getReservePrefixedJNDIName(prefix, resourceName);
    }

    public static String getEmbeddedRarModuleName(String applicationName, String moduleName) {
        String embeddedRarName = moduleName.substring(0,
                moduleName.indexOf(ConnectorConstants.EXPLODED_EMBEDDED_RAR_EXTENSION));

        moduleName = applicationName + ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER + embeddedRarName;
        return moduleName;
    }

    public static boolean isEmbedded(DeploymentContext context) {
        ReadableArchive archive = context.getSource();
        return (archive != null && archive.getParentArchive() != null);
    }

    public static String getApplicationName(DeploymentContext context) {
        String applicationName = null;
        ReadableArchive parentArchive = context.getSource().getParentArchive();
        if (parentArchive != null) {
            applicationName = parentArchive.getName();
        }
        return applicationName;
    }

    public static List<URI> getInstalledLibrariesFromManifest(String moduleDirectory, ServerEnvironment env) 
            throws ConnectorRuntimeException {

        // this method will be called during system-rar creation.
        // Though there are code paths that will call this method for creation of rars during recovery / via
        // API exposed for GUI, they will not call this method as non-system rars are always started during server startup
        // system-rars can specify only EXTENSTION_LIST in MANIFEST.MF and do not have a way to use --libararies option.
        // So, satisfying system-rars alone as of now.
        
        List<URI> libURIs = new ArrayList<URI>();
        try{
            File module = new File(moduleDirectory);

            FileArchive fileArchive = new FileArchive();
            fileArchive.open(module.toURI());  // directory where rar is exploded
            Set<String> extensionList = InstalledLibrariesResolver.getInstalledLibraries(fileArchive);

            URL[] extensionListLibraries = ASClassLoaderUtil.getLibrariesAsURLs(extensionList, env);
            for (URL url : extensionListLibraries) {
                libURIs.add(url.toURI());
                _logger.log(Level.FINEST, "adding URL [ "+url+" ] to installedLibraries");
            }
        }catch(IOException ioe){
            ConnectorRuntimeException cre = new ConnectorRuntimeException(ioe.getMessage());
            cre.initCause(ioe);
            throw cre;
        } catch (URISyntaxException e) {
            ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
            cre.initCause(e);
            throw cre;
        }

        return libURIs;
    }

    public static String getReservePrefixedJNDINameForDescriptor(String moduleName) {
        return getReservePrefixedJNDIName(ConnectorConstants.DD_PREFIX, moduleName);
    }

    public static boolean isStandAloneRA(String moduleName){
        return ConfigBeansUtilities.getModule(moduleName)!= null;
    }

    /**
     * GlassFish (Embedded) Uber jar will have .rar bundled in it.
     * This method will extract the .rar from the uber jar into specified directory.
     * As of now, this method is only used in EMBEDDED mode
     * @param fileName rar-directory-name
     * @param rarName resource-adapter name
     * @param destDir destination directory
     * @return status indicating whether .rar is exploded successfully or not
     */
    public static boolean extractRar(String fileName, String rarName, String destDir) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(rarName);
        if (is != null) {
            FileArchive fa = new FileArchive();
            OutputStream os = null;
            try {
                os = fa.putNextEntry(fileName);

                FileUtils.copy(is, os, 0);
            } catch (IOException e) {
                Object args[] = new Object[]{rarName, e};
                _logger.log(Level.WARNING, "error.extracting.archive", args);
                return false;
            } finally {
                try {
                    if (os != null) {
                        fa.closeEntry();
                    }

                } catch (IOException ioe) {
                    if (_logger.isLoggable(Level.FINEST)) {
                        _logger.log(Level.FINEST, "Exception while closing archive [ " + fileName + " ]", ioe);
                    }
                }

                try {
                    is.close();
                } catch (IOException ioe) {
                    if (_logger.isLoggable(Level.FINEST)) {
                        _logger.log(Level.FINEST, "Exception while closing archive [ " + rarName + " ]", ioe);
                    }
                }
            }

            File file = new File(fileName);
            if (file.exists()) {
                try {
                    extractJar(file, destDir);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                _logger.log(Level.INFO, "could not find RAR [ " + rarName + " ] location [ " + fileName + " ] " +
                        "after extraction");
                return false;
            }
        } else {
            _logger.log(Level.INFO, "could not find RAR [ " + rarName + " ] in the archive, skipping .rar extraction");
            return false;
        }
    }

    private static void extractJar(File jarFile, String destDir) throws IOException {
        java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
        java.util.Enumeration enum1 = jar.entries();
        while (enum1.hasMoreElements()) {
            java.util.jar.JarEntry file = (java.util.jar.JarEntry) enum1.nextElement();
            java.io.File f = new java.io.File(destDir + java.io.File.separator + file.getName());
            if (file.isDirectory()) {
                f.mkdir();
                continue;
            }
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = jar.getInputStream(file);
                fos = new FileOutputStream(f);
                while (is.available() > 0) {
                    fos.write(is.read());
                }
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    if (_logger.isLoggable(Level.FINEST)) {
                        _logger.log(Level.FINEST, "exception while closing archive [ " + f.getName() + " ]", e);
                    }
                }

                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                    if (_logger.isLoggable(Level.FINEST)) {
                        _logger.log(Level.FINEST, "exception while closing archive [ " + file.getName() + " ]", e);
                    }
                }
            }
        }
    }
}
