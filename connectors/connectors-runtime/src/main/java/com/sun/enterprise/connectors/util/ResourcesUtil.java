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

package com.sun.enterprise.connectors.util;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.connectors.DeferredResourceConfig;
import com.sun.enterprise.connectors.ConnectorRuntime;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ApplicationInfo;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.logging.LogDomains;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.io.File;
import java.net.URI;


public class ResourcesUtil {

    //The thread local ResourcesUtil is used in two cases
    //1. An event config context is to be used as in case of resource
    //   deploy/undeploy and enable/disable events.
    //2. An admin config context to be used for ConnectorRuntime.getConnection(...)
    //   request
    static ThreadLocal<ResourcesUtil> localResourcesUtil =
            new ThreadLocal<ResourcesUtil>();

    static Logger _logger = LogDomains.getLogger(ResourcesUtil.class,LogDomains.RSR_LOGGER);

    static StringManager localStrings =
            StringManager.getManager(ResourcesUtil.class);

    static ServerContext sc_ = null;

    protected Domain dom = null;

    protected Resources res = null;

    private ConnectorRuntime runtime;

    public ResourcesUtil(){
        runtime = ConnectorRuntime.getRuntime();
    }
    public static void setServerContext(ServerContext sc) {
        sc_ = sc;
    }

    private Applications getApplications(){
        return runtime.getApplications();
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

        if (localResourcesUtil.get() != null)
            return localResourcesUtil.get();

        // TODO V3 temporarily return a ResourcesUtil
        localResourcesUtil.set(new ResourcesUtil());
        return localResourcesUtil.get();
    }


    /**
     * This method takes in an admin JdbcConnectionPool and returns the RA
     * that it belongs to.
     *
     * @param pool - The pool to check
     * @return The name of the JDBC RA that provides this pool's datasource
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

            ConfigBeanProxy[] resourcesToload = new ConfigBeanProxy[]{jdbcPool, jdbcResource};
            resConfig.setResourcesToLoad(resourcesToload);

        } else if (ConnectorConstants.RES_TYPE_CR.equalsIgnoreCase(resType) ||
                ConnectorConstants.RES_TYPE_CCP.equalsIgnoreCase(resType)) {
            ConnectorConnectionPool connPool = (ConnectorConnectionPool) pool;
            ConnectorResource connResource = (ConnectorResource) resource;
            resourceAdapterName = connPool.getResourceAdapterName();

            //TODO V3 need to get AOR & RA-Config later
            resConfig = new DeferredResourceConfig(resourceAdapterName, null, connPool, connResource, null, null, null);

            ConfigBeanProxy[] resourcesToload = new ConfigBeanProxy[]{connPool, connResource};
            resConfig.setResourcesToLoad(resourcesToload);

        } else {
            //TODO V3 can other resources be lazily loaded ?
            throw new ConnectorRuntimeException("unsupported resource type : " + resType);
        }
        return resConfig;
    }

/*
    public DeferredResourceConfig getDeferredJdbcResourceConfig(JdbcResource resource, JdbcConnectionPool pool) {
        DeferredResourceConfig resConfig = null;
        */
/*TODO V3 handle later
        if (resource.isEnabled())*/
/*
        {
            String rarName = getRANameofJdbcConnectionPool(pool);
            resConfig = new DeferredResourceConfig(rarName, null, null, null, pool, resource, null);
            ConfigBeanProxy[] resourcesToload = new ConfigBeanProxy[]{pool, resource};
            resConfig.setResourcesToLoad(resourcesToload);
        }
        return resConfig;
    }
*/

    /* TODO V3 handle later
    public DeferredResourceConfig getDeferredResourceConfig(String resourceName) {
        DeferredResourceConfig resConfig = getDeferredConnectorResourceConfigs(
                resourceName);
        if (resConfig != null) {
            return resConfig;
        }

        resConfig = getDeferredJdbcResourceConfigs(resourceName);

        if (resConfig != null) {
            return resConfig;
        }

//        TODO V3 handle admin-objects later
        resConfig = getDeferredAdminObjectConfigs(
                resourceName);


        return resConfig;
    } */

    /**
     * Returns the deffered connector resource config. This can be resource of JMS RA which is lazily
     * loaded. Or for other connector RA which is not loaded at startup. The connector RA which does
     * not have any resource or admin object associated with it are not loaded at startup. They are
     * all lazily loaded.
     */
    /*
    protected DeferredResourceConfig getDeferredConnectorResourceConfigs(
            String resourceName) {

        if (resourceName == null) {
            return null;
        }
        ConfigBeanProxy[] resourcesToload = new ConfigBeanProxy[2];

        try {
            if (!isReferenced(resourceName)) {
                return null;
            }
        } catch (ConfigException e) {
            String message = localStrings.getString(
                    "error.finding.resources.references",
                    resourceName);
            _logger.log(Level.WARNING, message + e.getMessage());
            _logger.log(Level.FINE, message + e.getMessage(), e);
        }


        ConnectorResource connectorResource =
                res.getConnectorResourceByJndiName(resourceName);
        if (connectorResource == null || !connectorResource.isEnabled()) {
            return null;
        }
        String poolName = connectorResource.getPoolName();
        ConnectorConnectionPool ccPool =
                res.getConnectorConnectionPoolByName(poolName);
        if (ccPool == null) {
            return null;
        }
        String rarName = ccPool.getResourceAdapterName();
        if (rarName != null) {
            resourcesToload[0] = ccPool;
            resourcesToload[1] = connectorResource;

            ResourceAdapterConfig[] resourceAdapterConfig =
                    new ResourceAdapterConfig[1];
            // TODO V3 handle resource adapter config later
            //resourceAdapterConfig[0] =
            //        res.getResourceAdapterConfigByResourceAdapterName(
            //                rarName);

            DeferredResourceConfig resourceConfig =
                    new DeferredResourceConfig(rarName, null, ccPool,
                            connectorResource, null, null,
                            resourceAdapterConfig);
            resourceConfig.setResourcesToLoad(resourcesToload);
            return resourceConfig;
        }
        return null;
    } */

    /**
     * Returns true if the given resource is referenced by this server.
     *
     * @param resourceName the name of the resource
     * @return true if the named resource is used/referred by this server
     * @throws ConfigException if an error while parsing domain.xml
     */
    protected boolean isReferenced(String resourceName) /*throws ConfigException */{
        throw new UnsupportedOperationException();
        /* TODO V3 handle once ServerHelper is available
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("isReferenced :: " + resourceName + " - "
                    + ServerHelper.serverReferencesResource(
                    configContext_, sc_.getInstanceName(),
                    resourceName));
        }

        return ServerHelper.serverReferencesResource(configContext_,
                sc_.getInstanceName(), resourceName);
        */
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
    public boolean isEnabled(ConfigBeanProxy res) /*throws ConfigException*/ {
        _logger.fine("ResourcesUtil :: isEnabled");
        if (res == null)
            return false;
        if (res instanceof BindableResource) {
            return isEnabled((BindableResource) res);
        } else if(res instanceof ResourcePool) {
            return isEnabled((ResourcePool) res);
        }

        ResourceRef resRef = null;


        //TODO V3 handle arbitrary resource type
        //TODO V3 check whether the resource-ref is also enabled
        /*Server server = ServerBeansFactory.getServerBean(configContext_);

        //using ServerTags, otherwise have to resort to reflection or multiple instanceof/casts
        resRef =  server.getResourceRefByRef(res.getAttributeValue(ServerTags.JNDI_NAME));*/


        if (resRef == null)
            return false;

        return parseBoolean(resRef.getEnabled());
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
            //TODO V3 : need to implement isRarEnabled after dom.getApps() done.
            //enabled = isRarEnabled(raName);
        }
        return enabled;
    }

    public boolean isEnabled(BindableResource br) {
        boolean enabled = true;
        //this cannot happen? need to remove later?
        if (br == null) {
            return false;
        }
        if(br instanceof JdbcResource) {
            enabled = parseBoolean(((JdbcResource) br).getEnabled());
            //TODO V3 : handle resource-ref
            /*if(!isResourceReferenceEnabled(br.getJndiName()))
                return false;
             */
        } else if(br instanceof ConnectorResource) {
            ConnectorResource cr = (ConnectorResource) br;
            enabled = parseBoolean(cr.getEnabled());
            //TODO V3 : handle resource -ref and ccp
            /*if(!isResourceReferenceEnabled(br.getJndiName()))
                return false;
             */
//        String poolName = cr.getPoolName();
//        ConnectorConnectionPool ccp = res.getConnectorConnectionPoolByName(poolName);
//        if (ccp == null) {
//            return false;
//        }
//
//        return isEnabled(ccp);
        } else if(br instanceof MailResource) {

        } else if(br instanceof ExternalJndiResource) {

        } else if(br instanceof CustomResource) {

        } else if(br instanceof AdminObjectResource) {
            
        }
        return enabled;
    }

    /* TODO V3 enable once configBeans (dom.getApps().getConnectorModuleByName()) is implemented
    private boolean isRarEnabled(String raName) throws ConfigException {
        if (raName == null || raName.length() == 0)
            return false;
        ConnectorModule module = dom.getApplications().getConnectorModuleByName(raName);
        if (module != null) {
            if (!module.isEnabled())
                return false;
            return isApplicationReferenceEnabled(raName);
        } else if (ConnectorsUtil.belongsToSystemRA(raName)) {
            return true;
        } //  TODO V3 hande embeddedRar later
//        else {
//            return belongToEmbeddedRarAndEnabled(raName);
//        }
        return false;
    }*/


    /**
     * Checks if a resource reference is enabled
     *
     * @since SJSAS 8.1 PE/SE/EE
     */

/* TODO V3 enable once configBeans (resourceRef.isEnabled()) is implemented
    private boolean isResourceReferenceEnabled(String resourceName)
            throws ConfigException {
        ResourceRef ref = null;
//         TODO V3 handle resource-ref later  ServerHelper
//            ref = ServerHelper.getServerByName( configContext_,
//                sc_.getInstanceName()).getResourceRefByRef(resourceName);

        if (ref == null) {
            _logger.fine("ResourcesUtil :: isResourceReferenceEnabled null ref");
            if (isADeployEvent())
                return true;
            else
                return false;
        }
        _logger.fine("ResourcesUtil :: isResourceReferenceEnabled ref enabled ?" + ref.isEnabled());
        return ref.isEnabled();
    }
*/

    /**
     * Checks if a resource reference is enabled
     *
     * @since SJSAS 9.1 PE/SE/EE
     */
/* TODO V3 handle once appRefEnabled is available
    private boolean isApplicationReferenceEnabled(String appName)
            throws ConfigException {
        ApplicationRef appRef = null;
        */
/* TODO V3 handle ServerHelper later
        appRef = ServerHelper.getServerByName( configContext_,
                sc_.getInstanceName()).getApplicationRefByRef(appName);
        */
/*
        if (appRef == null) {
            _logger.fine("ResourcesUtil :: isApplicationReferenceEnabled null ref");
            if (isADeployEvent())
                return true;
            else
                return false;
        }
        _logger.fine("ResourcesUtil :: isApplicationReferenceEnabled appRef enabled ?" + appRef.isEnabled());
        return appRef.isEnabled();
    }
*/

    /**
     * Checks whether call is from a deploy event.
     * Since in case of deploy event, the localResourceUtil will be set, so check is based on that.
     */
/*
    private boolean isADeployEvent() {
        if (localResourcesUtil.get() != null)
            return true;
        return false;
    }
*/
    public String getResourceType(ConfigBeanProxy cb) {
        //TODO V3 these constants need to be taken from ResourceDeployEvent

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
}
