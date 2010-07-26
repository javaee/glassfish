/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.connectors.util;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.connectors.DeferredResourceConfig;
import com.sun.enterprise.connectors.ConnectorRuntime;
import org.glassfish.internal.api.ServerContext;
import com.sun.enterprise.util.i18n.StringManager;
import org.glassfish.internal.api.RelativePathResolver;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.logging.LogDomains;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.net.URI;


public class ResourcesUtil {

/*
    //The thread local ResourcesUtil is used in two cases
    //1. An event config context is to be used as in case of resource
    //   deploy/undeploy and enable/disable events.
    //2. An admin config context to be used for ConnectorRuntime.getConnection(...)
    //   request
    static ThreadLocal<ResourcesUtil> localResourcesUtil =
            new ThreadLocal<ResourcesUtil>();

*/
    static Logger _logger = LogDomains.getLogger(ResourcesUtil.class,LogDomains.RSR_LOGGER);

    static StringManager localStrings =
            StringManager.getManager(ResourcesUtil.class);

    static ServerContext sc_ = null;

    protected Domain domain = null;

    protected Resources resources = null;

    private ConnectorRuntime runtime;

    private Server server;

    private static ResourcesUtil resourcesUtil;

    private ResourcesUtil(){
    }

    private Resources getResources() {
        if(resources == null){
            resources = getRuntime().getResources();
        }
        return resources;
    }

    private Domain getDomain(){
        if(domain == null){
            domain = getRuntime().getDomain();
        }
        return domain;
    }

    private ConnectorRuntime getRuntime(){
        if(runtime == null){
            runtime = ConnectorRuntime.getRuntime();
        }
        return runtime;
    }

    private Server getServer(){
        if(server == null){
            server = getDomain().getServerNamed(getRuntime().getServerEnvironment().getInstanceName());
        }
        return server;
    }
/*
    public static void setServerContext(ServerContext sc) {
        sc_ = sc;
    }
*/

    private Applications getApplications(){
        return getRuntime().getApplications();
    }

    private ConnectorModule getConnectorModuleByName(String name){
        ConnectorModule module = null;
        List<ConnectorModule> modules = getApplications().getModules(ConnectorModule.class);
        for(ConnectorModule connectorModule : modules){
            if(connectorModule.getName().equals(name)){
                module = connectorModule;
                break;
            }
        }
        return module;
    }


    private Application getApplicationByName(String name){
        Application application = null;
        List<Application> apps = getApplications().getApplications();
        for(Application app : apps){
            if(app.getName().equals(name)){
                application = app;
                break;
            }
        }
        return application;
    }
    /**
     * Gets the deployment location for a J2EE application.
     * @param appName application name
     * @return application deploy location
     */
    public String getApplicationDeployLocation(String appName) {
        String location = null;
        Application app = getApplicationByName(appName);
        if(app != null){
            //TODO V3 with annotations, is this right location ?
            location = RelativePathResolver.resolvePath(app.getLocation());
        }
        return location;
    }


    public boolean belongToStandAloneRar(String resourceAdapterName) {
        ConnectorModule connectorModule = getConnectorModuleByName(resourceAdapterName);
        return connectorModule != null;
    }

    public static ResourcesUtil createInstance() {
        //stateless, no synchronization needed
        if(resourcesUtil == null){
            resourcesUtil = new ResourcesUtil();
        }
        return resourcesUtil;
    }


    /**
     * This method takes in an admin JdbcConnectionPool and returns the RA
     * that it belongs to.
     *
     * @param pool - The pool to check
     * @return The name of the JDBC RA that provides this pool's data-source
     */

    public String getRANameofJdbcConnectionPool(JdbcConnectionPool pool) {
        String dsRAName = ConnectorConstants.JDBCDATASOURCE_RA_NAME;

        Class dsClass = null;

        if(pool.getDatasourceClassname() != null) {
            try {
                dsClass = ClassLoadingUtility.loadClass(pool.getDatasourceClassname());
            } catch (ClassNotFoundException cnfe) {
                return dsRAName;
            }
        } else if(pool.getDriverClassname() != null) {
            try {
                dsClass = ClassLoadingUtility.loadClass(pool.getDriverClassname());
            } catch (ClassNotFoundException cnfe) {
                return dsRAName;
            }            
        }

        //check if its XA
        if ("javax.sql.XADataSource".equals(pool.getResType())) {
            if (javax.sql.XADataSource.class.isAssignableFrom(dsClass)) {
                return ConnectorConstants.JDBCXA_RA_NAME;
            }
        }

        //check if its CP
        if ("javax.sql.ConnectionPoolDataSource".equals(pool.getResType())) {
            if (javax.sql.ConnectionPoolDataSource.class.isAssignableFrom(
                    dsClass)) {
                return ConnectorConstants.JDBCCONNECTIONPOOLDATASOURCE_RA_NAME;
            }
        }
        
        //check if its DM
        if("java.sql.Driver".equals(pool.getResType())) {
            if(java.sql.Driver.class.isAssignableFrom(dsClass)) {
                return ConnectorConstants.JDBCDRIVER_RA_NAME;
            }
        }
        
        //default to __ds
        return dsRAName;
    }

    public DeferredResourceConfig getDeferredResourceConfig(String resourceName) {
        DeferredResourceConfig resConfig = getDeferredConnectorResourceConfigs(
                resourceName);
        if(resConfig != null) {
            return resConfig;
        }

        resConfig = getDeferredJdbcResourceConfigs(
                resourceName);

        if(resConfig != null) {
            return resConfig;
        }

        resConfig = getDeferredAdminObjectConfigs(
                resourceName);

        return resConfig;
    }

    public DeferredResourceConfig getDeferredPoolConfig(String poolName) {

        DeferredResourceConfig resConfig = getDeferredConnectorPoolConfigs(
                poolName);
        if(resConfig != null) {
            return resConfig;
        }

        if(poolName == null){
            return null;
        }

        resConfig = getDeferredJdbcPoolConfigs(poolName);

        return resConfig;
    }

    public DeferredResourceConfig getDeferredResourceConfig(Object resource, Object pool, String resType, String raName)
            throws ConnectorRuntimeException {
        String resourceAdapterName = raName;
        DeferredResourceConfig resConfig = null;
        //TODO V3 there should not be res-type related check, refactor deferred-ra-config
        //TODO V3 (not to hold specific resource types)
        if (ConnectorConstants.RES_TYPE_JDBC.equalsIgnoreCase(resType) ||
                ConnectorConstants.RES_TYPE_JCP.equalsIgnoreCase(resType)) {

            JdbcConnectionPool jdbcPool = (JdbcConnectionPool) pool;
            JdbcResource jdbcResource = (JdbcResource) resource;

            resourceAdapterName = getRANameofJdbcConnectionPool((JdbcConnectionPool) pool);

            resConfig = new DeferredResourceConfig(resourceAdapterName, null, null, null, jdbcPool, jdbcResource, null);

            Resource[] resourcesToload = new Resource[]{jdbcPool, jdbcResource};
            resConfig.setResourcesToLoad(resourcesToload);

        } else if (ConnectorConstants.RES_TYPE_CR.equalsIgnoreCase(resType) ||
                ConnectorConstants.RES_TYPE_CCP.equalsIgnoreCase(resType)) {
            ConnectorConnectionPool connPool = (ConnectorConnectionPool) pool;
            ConnectorResource connResource = (ConnectorResource) resource;
            resourceAdapterName = connPool.getResourceAdapterName();

            //TODO V3 need to get AOR & RA-Config later
            resConfig = new DeferredResourceConfig(resourceAdapterName, null, connPool, connResource, null, null, null);

            Resource[] resourcesToload = new Resource[]{connPool, connResource};
            resConfig.setResourcesToLoad(resourcesToload);

        } else {
            throw new ConnectorRuntimeException("unsupported resource type : " + resType);
        }
        return resConfig;
    }

    public DeferredResourceConfig getDeferredJdbcResourceConfig(JdbcResource resource, JdbcConnectionPool pool) {
        DeferredResourceConfig resConfig = null;

        if (parseBoolean(resource.getEnabled())){
            String rarName = getRANameofJdbcConnectionPool(pool);
            resConfig = new DeferredResourceConfig(rarName, null, null, null, pool, resource, null);
            Resource[] resourcesToload = new Resource[]{pool, resource};
            resConfig.setResourcesToLoad(resourcesToload);
        }
        return resConfig;
    }


    /**
     * Returns the deferred connector resource config. This can be resource of JMS RA which is lazily
     * loaded. Or for other connector RA which is not loaded at start-up. The connector RA which does
     * not have any resource or admin object associated with it are not loaded at start-up. They are
     * all lazily loaded.
     */
    protected DeferredResourceConfig getDeferredConnectorResourceConfigs(
            String resourceName) {

        if (resourceName == null) {
            return null;
        }
        Resource[] resourcesToload = new Resource[2];

        try {
            if (!isReferenced(resourceName)) {
                return null;
            }
        } catch (Exception e) {
            String message = localStrings.getString(
                    "error.finding.resources.references",
                    resourceName);
            _logger.log(Level.WARNING, message + e.getMessage());
            _logger.log(Level.FINE, message + e.getMessage(), e);
        }


        ConnectorResource connectorResource = (ConnectorResource)
                getResources().getResourceByName(ConnectorResource.class, resourceName);
        if (connectorResource == null || !ConnectorsUtil.parseBoolean(connectorResource.getEnabled())) {
            return null;
        }
        String poolName = connectorResource.getPoolName();
        ConnectorConnectionPool ccPool = (ConnectorConnectionPool)
                getResources().getResourceByName(ConnectorConnectionPool.class, poolName);
        if (ccPool == null) {
            return null;
        }
        String rarName = ccPool.getResourceAdapterName();
        if (rarName != null) {
            resourcesToload[0] = ccPool;
            resourcesToload[1] = connectorResource;

            ResourceAdapterConfig[] resourceAdapterConfig = new ResourceAdapterConfig[1];
            resourceAdapterConfig[0] = (ResourceAdapterConfig)
                    getResources().getResourceByName(ResourceAdapterConfig.class, rarName);

            DeferredResourceConfig resourceConfig =
                    new DeferredResourceConfig(rarName, null, ccPool, connectorResource, null, null,
                            resourceAdapterConfig);
            resourceConfig.setResourcesToLoad(resourcesToload);
            return resourceConfig;
        }
        return null;
    }

    protected DeferredResourceConfig getDeferredJdbcResourceConfigs(String resourceName) {

        Resource[] resourcesToload = new Resource[2];
        if(resourceName == null) {
            return null;
        }

        try {
            //__pm does not have a domain.xml entry and hence will not
            //be referenced
            if(!(resourceName.endsWith("__pm"))){
                if(!isReferenced(resourceName)){
                    return null;
                }
            }
        } catch (Exception e) {
            String message = localStrings.getString(
                    "error.finding.resources.references",
                    resourceName);
            _logger.log(Level.WARNING, message + e.getMessage());
            _logger.log(Level.FINE,message + e.getMessage(), e);
        }

        JdbcResource jdbcResource = (JdbcResource) getResources().getResourceByName(JdbcResource.class, resourceName);
        if(jdbcResource == null || !ConnectorsUtil.parseBoolean(jdbcResource.getEnabled())) {
            String cmpResourceName = getCorrespondingCmpResourceName(resourceName);
            jdbcResource = (JdbcResource) getResources().getResourceByName(JdbcResource.class, cmpResourceName);
            if(jdbcResource == null) {
                return null;
            }
        }
        JdbcConnectionPool jdbcPool = (JdbcConnectionPool)
                getResources().getResourceByName(JdbcConnectionPool.class, jdbcResource.getPoolName());
        if(jdbcPool == null) {
            return null;
        }
        String rarName = getRANameofJdbcConnectionPool(jdbcPool);
        if(rarName != null && ConnectorsUtil.belongsToSystemRA(rarName)) {
            resourcesToload[0] = jdbcPool;
            resourcesToload[1] = jdbcResource;
            DeferredResourceConfig resourceConfig =
                    new DeferredResourceConfig(rarName,null,null,
                    null,jdbcPool,jdbcResource,null);
            resourceConfig.setResourcesToLoad(resourcesToload);
            return resourceConfig;
        }
        return null;
    }

    /**
     * Returns the deferred admin object config. This can be admin object of JMS RA which is lazily
     * loaded. Or for other connector RA which is not loaded at start-up. The connector RA which does
     * not have any resource or admin object associated with it are not loaded at start-up. They are
     * all lazily loaded.
     */
    protected DeferredResourceConfig getDeferredAdminObjectConfigs(String resourceName) {

        if(resourceName == null) {
            return null;
        }
        Resource[] resourcesToload = new Resource[1];

        try {
            if(!isReferenced(resourceName)){
                return null;
            }
        } catch (Exception e) {
            String message = localStrings.getString(
                    "error.finding.resources.references",
                    resourceName);
            _logger.log(Level.WARNING, message + e.getMessage());
            _logger.log(Level.FINE,message + e.getMessage(), e);
        }

        AdminObjectResource adminObjectResource = (AdminObjectResource)
                getResources().getResourceByName(AdminObjectResource.class, resourceName);
        if(adminObjectResource == null || !ConnectorsUtil.parseBoolean(adminObjectResource.getEnabled())) {
            return null;
        }
        String rarName = adminObjectResource.getResAdapter();
        if(rarName != null){
            resourcesToload[0] = adminObjectResource;
            ResourceAdapterConfig[] resourceAdapterConfig =
                    new ResourceAdapterConfig[1];
            resourceAdapterConfig[0] = (ResourceAdapterConfig)
                    getResources().getResourceByName(ResourceAdapterConfig.class, rarName);
            DeferredResourceConfig resourceConfig =
                    new DeferredResourceConfig(rarName,adminObjectResource,
                    null,null,null,null,resourceAdapterConfig);
            resourceConfig.setResourcesToLoad(resourcesToload);
            return resourceConfig;
        }
        return null;
    }

    protected String getCorrespondingCmpResourceName(String resourceName) {

        int index = resourceName.lastIndexOf("__pm");
        if(index != -1) {
            return resourceName.substring(0,index);
        }
        return null;
    }

    /**
     * Returns the deffered connector connection pool config. This can be pool of JMS RA which is lazily
     * loaded. Or for other connector RA which is not loaded at startup. The connector RA which does
     * not have any resource or admin object associated with it are not loaded at startup. They are
     * all lazily loaded.
     */
    protected DeferredResourceConfig getDeferredConnectorPoolConfigs(String poolName) {

        Resource[] resourcesToload = new Resource[1];
        if(poolName == null) {
            return null;
        }


        ConnectorConnectionPool ccPool = (ConnectorConnectionPool)
                getResources().getResourceByName(ConnectorConnectionPool.class, poolName);
        if(ccPool == null) {
            return null;
        }

        String rarName = ccPool.getResourceAdapterName();

        if(rarName != null){
            resourcesToload[0] = ccPool;
            ResourceAdapterConfig[] resourceAdapterConfig =
                    new ResourceAdapterConfig[1];
            resourceAdapterConfig[0] = (ResourceAdapterConfig)
                    getResources().getResourceByName(ResourceAdapterConfig.class, rarName);
            DeferredResourceConfig resourceConfig =
                    new DeferredResourceConfig(rarName,null,ccPool,
                    null,null,null,resourceAdapterConfig);
            resourceConfig.setResourcesToLoad(resourcesToload);
            return resourceConfig;
        }
        return null;
    }

    protected DeferredResourceConfig getDeferredJdbcPoolConfigs(String poolName) {

        Resource[] resourcesToload = new Resource[1];
        if(poolName == null) {
            return null;
        }

        JdbcConnectionPool jdbcPool = (JdbcConnectionPool)
                getResources().getResourceByName(JdbcConnectionPool.class, poolName);
        if(jdbcPool == null) {
            return null;
        }
        String rarName = getRANameofJdbcConnectionPool(jdbcPool);

        if(rarName != null && ConnectorsUtil.belongsToSystemRA(rarName)) {
            resourcesToload[0] = jdbcPool;
            DeferredResourceConfig resourceConfig =
                    new DeferredResourceConfig(rarName,null,null,
                    null,jdbcPool,null,null);
            resourceConfig.setResourcesToLoad(resourcesToload);
            return resourceConfig;
        }
        return null;
    }

    public boolean poolBelongsToSystemRar(String poolName) {
        ConnectorConnectionPool ccPool = (ConnectorConnectionPool)
                getResources().getResourceByName(ConnectorConnectionPool.class, poolName);
        if(ccPool != null){
            return ConnectorsUtil.belongsToSystemRA(ccPool.getResourceAdapterName());
        } else {
            JdbcConnectionPool jdbcPool = (JdbcConnectionPool)
                    getResources().getResourceByName(JdbcConnectionPool.class, poolName);
            if(jdbcPool != null) {
                return true;
            }
        }
        return false;
    }

    public boolean adminObjectBelongsToSystemRar(String adminObject) {
        AdminObjectResource aor = (AdminObjectResource)
                getResources().getResourceByName(AdminObjectResource.class, adminObject);
        if(aor != null) {
            return ConnectorsUtil.belongsToSystemRA(aor.getResAdapter());
        }
        return false;
    }

    public boolean resourceBelongsToSystemRar(String resourceName) {
        ConnectorResource connectorResource = (ConnectorResource)
                getResources().getResourceByName(ConnectorResource.class, resourceName);
        if(connectorResource != null){
            return poolBelongsToSystemRar(connectorResource.getPoolName());
        } else {
            JdbcResource jdbcResource = (JdbcResource)
                    getResources().getResourceByName(JdbcResource.class, resourceName);
            if(jdbcResource != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given resource is referenced by this server.
     *
     * @param resourceName the name of the resource
     * @return true if the named resource is used/referred by this server
     */
    protected boolean isReferenced(String resourceName) {
        boolean refExists = getServer().isResourceRefExists(resourceName);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("isReferenced :: " + resourceName + " - "+ refExists);
        }
        return refExists;
    }

    /**
     * Checks if a Resource is enabled.
     * <p/>
     * Since 8.1 PE/SE/EE, A resource [except resource adapter configs, connector and
     * JDBC connection pools which are global and hence enabled always] is enabled
     * only when the resource is enabled and there exists a resource ref to this
     * resource in this server instance and that resource ref is enabled.
     * <p/>
     * Before a resource is loaded or deployed, it is checked to see if it is
     * enabled.
     *
     * @since 8.1 PE/SE/EE
     */
    public boolean isEnabled(Resource resource) {
        _logger.fine("ResourcesUtil :: isEnabled");
        if (resource == null){
            return false;
        }else if (resource instanceof BindableResource) {
            ResourceRef resRef = getServer().getResourceRef(
                    ((BindableResource) resource).getJndiName());
            return isEnabled((BindableResource) resource) &&
                    ((resRef != null) && parseBoolean(resRef.getEnabled()));
        } else if(resource instanceof ResourcePool) {
            return isEnabled((ResourcePool) resource);
        }else if(resource instanceof WorkSecurityMap || resource instanceof ResourceAdapterConfig){
            return true;
        }else{
            return false;
        }
    }


    public boolean isEnabled(ResourcePool pool) {
        boolean enabled = true;
        if(pool == null) {
            return false;
        }
        if(pool instanceof JdbcConnectionPool) {
            //JDBC RA is system RA and is always enabled
            enabled = true;
        } else if(pool instanceof ConnectorConnectionPool) {
            ConnectorConnectionPool ccpool = (ConnectorConnectionPool) pool;
            String raName = ccpool.getResourceAdapterName();
            enabled = isRarEnabled(raName);
        }
        return enabled;
    }

    public boolean isEnabled(BindableResource br) {
        boolean enabled = false;
        //this cannot happen? need to remove later?
        if (br == null) {
            return false;
        }
        boolean resourceEnabled = ConnectorsUtil.parseBoolean(br.getEnabled());
        boolean refEnabled = isResourceReferenceEnabled(br.getJndiName());

        if((br instanceof JdbcResource) ||
                (br instanceof MailResource) ||
                (br instanceof ExternalJndiResource) ||
                (br instanceof CustomResource)) {
            if(resourceEnabled && refEnabled){
                enabled = true;
            }
        } else if(br instanceof ConnectorResource) {
            ConnectorResource cr = (ConnectorResource) br;
            String poolName = cr.getPoolName();
            ConnectorConnectionPool ccp = (ConnectorConnectionPool)
                    getResources().getResourceByName(ConnectorConnectionPool.class, poolName);
            if (ccp == null) {
                return false;
            }
            boolean poolEnabled = isEnabled(ccp);
            enabled  = poolEnabled && resourceEnabled && refEnabled ;
        } else if(br instanceof AdminObjectResource) {
            AdminObjectResource aor = (AdminObjectResource) br;
            String raName = aor.getResAdapter();
            boolean isRarEnabled = isRarEnabled(raName);
            if(/* TODO isRarEnabled &&*/ resourceEnabled && refEnabled){
                enabled = true;
            }
        }
        return enabled;
    }

    private boolean isRarEnabled(String raName) {
        if(raName == null || raName.length() == 0)
            return false;
        Application application = getDomain().getApplications().getApplication(raName);
        if(application != null) {
            return isApplicationReferenceEnabled(raName);
        } else if(ConnectorsUtil.belongsToSystemRA(raName)) {
            return true;
        } else {
            return belongToEmbeddedRarAndEnabled(raName);
        }
    }

    /**
     * Checks if the application reference is enabled
     * @param appName application-name
     * @since SJSAS 9.1 PE/SE/EE
     * @return boolean indicating the status
     */
    private boolean isApplicationReferenceEnabled(String appName) {
        ApplicationRef appRef = getServer().getApplicationRef(appName);
        if (appRef == null) {
            _logger.fine("ResourcesUtil :: isApplicationReferenceEnabled null ref");
/*  TODO revisit
            if(isADeployEvent()){
                return true;
            }else{
*/
                return false;
//            }
        }
        _logger.fine("ResourcesUtil :: isApplicationReferenceEnabled appRef enabled ?" + appRef.getEnabled());
        return ConnectorsUtil.parseBoolean(appRef.getEnabled());
    }

    //TODO can be made generic
    //TODO probably, DuckTyped for resources
    public AdminObjectResource[] getEnabledAdminObjectResources(String raName)  {
        List resourcesList = getResources().getResources();
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
            if(!isEnabled(adminObjectResource))
                continue;
            adminObjectResources.add(adminObjectResource);
        }
        AdminObjectResource[] allAdminObjectResources =
                    new AdminObjectResource[adminObjectResources.size()];
        return adminObjectResources.toArray(allAdminObjectResources);
    }



/*
    */
/**
     * Checks whether call is from a deploy event.
     * Since in case of deploy event, the localResourceUtil will be set, so check is based on that.
     * @return boolean indicating the status
     *//*


    private boolean isADeployEvent(){
        if(localResourcesUtil.get() != null)
            return true;
        return false;
    }
*/


    private boolean belongToEmbeddedRarAndEnabled(String resourceAdapterName)  {
        String appName = getAppNameToken(resourceAdapterName);
        if(appName==null)
            return false;
        Applications apps = getDomain().getApplications();
        Application app = apps.getApplication(appName);
        if(app == null || !ConnectorsUtil.parseBoolean(app.getEnabled()))
            return false;
        return isApplicationReferenceEnabled(appName);
    }

    private String getAppNameToken(String rarName) {
        if(rarName == null) {
            return null;
        }
        int index = rarName.indexOf(
                ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER);
        if(index != -1) {
            return rarName.substring(0,index);
        } else {
            return null;
        }
    }

    /**
     * Checks if a resource reference is enabled
     * @param resourceName resource-name
     * @since SJSAS 8.1 PE/SE/EE
     * @return boolean indicating whether the resource-ref is enabled.
     */
    private boolean isResourceReferenceEnabled(String resourceName) {
        ResourceRef ref = getServer().getResourceRef(resourceName);
        if (ref == null) {
            _logger.fine("ResourcesUtil :: isResourceReferenceEnabled null ref");
/* TODO revisit
            if(isADeployEvent())
                return true;
            else
*/
                return false;
        }
        _logger.fine("ResourcesUtil :: isResourceReferenceEnabled ref enabled ?" + ref.getEnabled());
        return ConnectorsUtil.parseBoolean(ref.getEnabled());
    }

    /**
     * Gets a JDBC resource on the basis of its jndi name
     * @param jndiName the jndi name of the JDBC resource to lookup
     * @param checkReference if true, returns this JDBC resource only if it is referenced in
     *                       this server. If false, returns the JDBC resource irrespective of
     *                       whether it is referenced or not.
     * @return JdbcResource resource
     */
    public JdbcResource getJdbcResourceByJndiName( String jndiName, boolean checkReference) {

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ResourcesUtil :: looking up jdbc resource, jndiName is : " + jndiName );
        }

        JdbcResource jdbcResource = (JdbcResource) getResources().getResourceByName(JdbcResource.class, jndiName);

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ResourcesUtil :: looked up jdbc resource:" + jdbcResource );
        }

        //does the isReferenced method throw NPE for null value? Better be safe
        if (jdbcResource == null) {
            return null;
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ResourcesUtil :: looked up jdbc resource name:" + jdbcResource.getJndiName() );
        }

        if(checkReference){
            return isReferenced( jndiName ) ? jdbcResource : null;
        }else{
            return jdbcResource;
        }
    }

    public String getResourceType(ConfigBeanProxy cb) {
        if (cb instanceof ConnectorConnectionPool) {
            return ConnectorConstants.RES_TYPE_CCP;
        } else if (cb instanceof ConnectorResource) {
            return ConnectorConstants.RES_TYPE_CR;
        }
        if (cb instanceof JdbcConnectionPool) {
            return ConnectorConstants.RES_TYPE_JCP;
        } else if (cb instanceof JdbcResource) {
            return ConnectorConstants.RES_TYPE_JDBC;
        }
        return null;
    }

    private boolean parseBoolean(String enabled) {
        return Boolean.parseBoolean(enabled);
    }

    public ConnectorDescriptor getConnectorDescriptorFromUri(String rarName, String raLoc) {
        try {
            String appName = rarName.substring(0, rarName.indexOf(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER));
            //String actualRarName = rarName.substring(rarName.indexOf(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER) + 1);
            String appDeployLocation = ResourcesUtil.createInstance().getApplicationDeployLocation(appName);

            FileArchive in = ConnectorRuntime.getRuntime().getFileArchive();
            in.open(new URI(appDeployLocation));
            ApplicationArchivist archivist = ConnectorRuntime.getRuntime().getApplicationArchivist();
            com.sun.enterprise.deployment.Application application = archivist.open(in);
            return application.getRarDescriptorByUri(raLoc);
        } catch (Exception e) {
            Object params[] = new Object[]{rarName, e};
            _logger.log(Level.WARNING, "error.getting.connector.descriptor", params);
        }
        return null;
    }

    /**
     * Determines if a connector connection pool is referred in a
     * server-instance via resource-refs
     * @param poolName pool-name
     * @return boolean true if pool is referred in this server instance, false
     * otherwise
     */
    public boolean isPoolReferredInServerInstance(String poolName) {

        Collection<ConnectorResource> connectorResources = getRuntime().getResources().getResources(ConnectorResource.class);
        for (ConnectorResource resource : connectorResources) {
            _logger.fine("poolname " + resource.getPoolName() + "resource " + resource.getJndiName());
            if ((resource.getPoolName().equalsIgnoreCase(poolName)) && isReferenced(resource.getJndiName())){
                _logger.fine("Connector resource "  + resource.getJndiName() + "refers "
                        + poolName + "in this server instance");
                return true;
            }
        }
        _logger.fine("No Connector resource refers [ " + poolName + " ] in this server instance");
        return false;
    }

    /**
     * Determines if a JDBC connection pool is referred in a
     * server-instance via resource-refs
     * @param poolName pool-name
     * @return boolean true if pool is referred in this server instance, false
     * otherwise
     */
    public boolean isJdbcPoolReferredInServerInstance(String poolName) {

        Collection<JdbcResource> jdbcResources = getRuntime().getResources().getResources(JdbcResource.class);

        for (JdbcResource resource : jdbcResources) {
            _logger.fine("poolname " + resource.getPoolName() + "resource " + resource.getJndiName()
            + " referred " + isReferenced(resource.getJndiName()));
            //Have to check isReferenced here!
            if ((resource.getPoolName().equalsIgnoreCase(poolName)) && isReferenced(resource.getJndiName())){
                _logger.fine("JDBC resource "  + resource.getJndiName() + "refers " + poolName +
                        "in this server instance");
                return true;
            }
        }
        _logger.fine("No JDBC resource refers [ " + poolName + " ] in this server instance");
        return false;
    }
}
