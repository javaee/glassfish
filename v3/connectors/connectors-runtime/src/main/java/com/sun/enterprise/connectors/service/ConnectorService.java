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

package com.sun.enterprise.connectors.service;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.DeferredResourceConfig;
import com.sun.enterprise.connectors.util.ConnectorDDTransformUtils;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.spi.ResourceDeployer;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.resource.pool.PoolManager;
import com.sun.enterprise.resource.deployer.ResourceDeployerFactory;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.logging.LogDomains;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collection;
import java.util.List;


/**
 * This is the base class for all the connector services. It defines the
 * enviroment of execution (client or server), and holds the reference to
 * connector runtime for inter service method invocations.
 *
 * @author Srikanth P
 */
//TODO V3 can this be a service ? (will help to eliminate need of resource-deployer-factory)
public class ConnectorService implements ConnectorConstants {
    protected static final Logger _logger = LogDomains.getLogger(ConnectorService.class, LogDomains.RSR_LOGGER);

    protected static final ConnectorRegistry _registry =
            ConnectorRegistry.getInstance();

    private boolean debug = true;
    protected static int environment = SERVER;
    protected ConnectorRuntime _runtime;
    protected ResourceDeployerFactory factory = new ResourceDeployerFactory();

    /**
     * Default Constructor
     */
    public ConnectorService() {
        _runtime = ConnectorRuntime.getRuntime();
    }

    /**
     * Initializes the execution environment. If the execution environment
     * is appserv runtime it is set to ConnectorConstants.SERVER else
     * it is set ConnectorConstants.CLIENT
     *
     * @param environ set to ConnectorConstants.SERVER if execution
     *                environment is appserv runtime else set to
     *                ConnectorConstants.CLIENT
     */
    public static void initialize(int environ) {
        environment = environ;
    }

    /**
     * Returns the execution environment.
     *
     * @return ConnectorConstants.SERVER if execution environment is
     *         appserv runtime
     *         else it returns ConnectorConstants.CLIENT
     */
    public static int getEnviron() {
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
    // TODO V3 can the default pool name generation be fully done by connector-admin-service-utils ?
    public String getDefaultPoolName(String moduleName,
                                     String connectionDefName) {
        return moduleName + POOLNAME_APPENDER + connectionDefName;
    }

    /**
     * Returns the generated default connector resource for a
     * connection definition.
     *
     * @param moduleName        resource-adapter name
     * @param connectionDefName connection definition name
     * @return generated default connector resource name
     */
    // TODO V3 can the default resource name generation be fully done by connector-admin-service-utils ?
    public String getDefaultResourceName(String moduleName,
                                         String connectionDefName) {
        //Construct the default resource name as
        // <JNDIName_of_RA>#<connectionDefnName>
        String resourceJNDIName = ConnectorAdminServiceUtils.getReservePrefixedJNDINameForResource(moduleName);
        return resourceJNDIName + RESOURCENAME_APPENDER + connectionDefName;
    }

    /**
     * Checks whether the executing environment is application server
     *
     * @return true if execution environment is server
     *         false if it is client
     */

    public static boolean isServer() {
        if (getEnviron() == SERVER) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkAndLoadResource(Object resource, Object pool, String resourceType, String resourceName,
                                        String raName)
            throws ConnectorRuntimeException {
        String resname = ConnectorAdminServiceUtils.getOriginalResourceName(resourceName);
        _logger.fine("ConnectorService :: checkAndLoadResource resolved to load " + resname);

        ResourcesUtil resUtil = ResourcesUtil.createInstance();
        DeferredResourceConfig defResConfig = resUtil.getDeferredResourceConfig(resource, pool, resourceType, raName);
        return loadResourcesAndItsRar(defResConfig);
    }

    public boolean loadResourcesAndItsRar(DeferredResourceConfig defResConfig) {
        if (defResConfig != null) {
            try {
                loadDeferredResources(defResConfig.getResourceAdapterConfig());
                String rarName = defResConfig.getRarName();
                loadDeferredResourceAdapter(rarName);
                final ConfigBeanProxy[] resToLoad = defResConfig.getResourcesToLoad();
                AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        try {
                            loadDeferredResources(resToLoad);
                        } catch (Exception ex) {
                            _logger.log(Level.SEVERE, "failed to load resources/ResourceAdapter");
                            _logger.log(Level.SEVERE, "", ex);
                        }
                        return null;
                    }
                });
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, "failed to load resources/ResourceAdapter");
                _logger.log(Level.SEVERE, "", ex);
                return false;
            }
            return true;
        }
        return false;
    }


    public void loadDeferredResourceAdapter(String rarName)
            throws ConnectorRuntimeException {
        try {
            ConnectorRuntime cr = ConnectorRuntime.getRuntime();
            //Do this only for System RA
            if (ConnectorsUtil.belongsToSystemRA(rarName)) {
                cr.createActiveResourceAdapter(ConnectorsUtil.getSystemModuleLocation(rarName), rarName, null);
            }
        } catch (Exception e) {
            ConnectorRuntimeException ce =
                    new ConnectorRuntimeException(e.getMessage());
            ce.initCause(e);
            throw ce;
        }
    }


    public void loadDeferredResources(ConfigBeanProxy[] resourcesToLoad)
            throws Exception {
        if (resourcesToLoad == null || resourcesToLoad.length == 0) {
            return;
        }
        String resourceType = null;
        ResourceDeployer deployer = null;
        ResourcesUtil resourceUtil = ResourcesUtil.createInstance();
        for (ConfigBeanProxy resource : resourcesToLoad) {
            if (resource == null) {
                continue;
            } else /* TODO V3 handle later once configBeans (resource.isEnabled()) is available
                    if (resourceUtil.isEnabled(resource))*/ {
                resourceType = resourceUtil.getResourceType(resource);

                deployer = factory.getResourceDeployer(resourceType);
                if (deployer != null) {
                    deployer.deployResource(resource);
                }
            }
        }
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

        if (rarName == null) {
            return null;
        }
        ConnectorDescriptor desc = null;
        desc = _registry.getDescriptor(rarName);
        if (desc != null) {
            return desc;
        }
        String moduleDir;
        ResourcesUtil resUtil = ResourcesUtil.createInstance();

        if (ConnectorsUtil.belongsToSystemRA(rarName)) {
            moduleDir = ConnectorsUtil.getSystemModuleLocation(rarName);
        } else {
            moduleDir = ConnectorsUtil.getLocation(rarName);
        }
        if (moduleDir != null) {
            desc = ConnectorDDTransformUtils.getConnectorDescriptor(moduleDir);
        } else {
            _logger.log(Level.SEVERE,
                    "rardeployment.no_module_deployed", rarName);
        }
        return desc;
    }


    /**
     * Matching will be switched off in the pool, by default. This will be
     * switched on if the connections with different resource principals reach the pool.
     *
     * @param poolName Name of the pool to switchOn matching.
     * @param rarName  Name of the resource adater.
     */
    public void switchOnMatching(String rarName, String poolName) {
        // At present it is applicable to only JDBC resource adapters
        // Later other resource adapters also become applicable.
        if (rarName.equals(ConnectorRuntime.JDBCDATASOURCE_RA_NAME)
                || rarName.equals(ConnectorRuntime.JDBCCONNECTIONPOOLDATASOURCE_RA_NAME) ||
                rarName.equals(ConnectorRuntime.JDBCXA_RA_NAME)) {

            PoolManager poolMgr = _runtime.getPoolManager();
            boolean result = poolMgr.switchOnMatching(poolName);
            if (!result) {
                try {
                    getRuntime().switchOnMatchingInJndi(poolName);
                } catch (ConnectorRuntimeException cre) {
                    // This will never happen.
                }
            }
        }
    }

    protected ConnectorRuntime getRuntime() {
        return ConnectorRuntime.getRuntime();
    }

    private void deleteResource(Object resource) throws ConnectorRuntimeException {
        try {
            factory.getResourceDeployer(resource).undeployResource(resource);
        } catch (Exception e) {
            ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
            cre.initCause(e);
            throw cre;
        }
    }

    private void destroyConnectionPools(Collection pools) {
        for (Object pool : pools) {
            try {
                deleteResource(pool);
            } catch (ConnectorRuntimeException cre) {
                cre.printStackTrace();
                //TODO V3 handle / log exceptions
            }
        }
    }

    private void destroyResources(Collection resources) {
        for (Object resource : resources) {
            try {
                deleteResource(resource);
            } catch (ConnectorRuntimeException cre) {
                cre.printStackTrace();
                //TODO V3 handle / log exceptions
            }
        }
    }

    /**
     * Destroy all resources and pools.
     * Delete resources followed by the pools from the list
     * @param allResources resources and pools
     */
    public void destroyResourcesAndPools(Collection allResources) {
        Collection pools = getPools(allResources);
        Collection resources = getResources(allResources);

        destroyResources(resources);
        destroyConnectionPools(pools);
    }

    public boolean checkAndLoadPool(String poolName) {
        boolean status = false;
        try {
            ResourcesUtil resutil = ResourcesUtil.createInstance();
            ResourcePool pool = _runtime.getConnectionPoolConfig(poolName);
            DeferredResourceConfig defResConfig =
                    resutil.getDeferredResourceConfig(null, pool, ConnectorsUtil.getResourceType(pool), null);

            status = loadResourcesAndItsRar(defResConfig);
        } catch (ConnectorRuntimeException cre) {
            _logger.log(Level.WARNING, "unable to load Jdbc Connection Pool [ " + poolName + " ]", cre);
        }
        return status;
    }

/*
    public boolean checkAndLoadJdbcPool(String poolName) {
        boolean status = false;
        try {
            ResourcesUtil resutil = ResourcesUtil.createInstance();
            JdbcConnectionPool pool = _runtime.getJdbcConnectionPoolConfig(poolName);
            DeferredResourceConfig defResConfig =
                    resutil.getDeferredResourceConfig(null, pool, ConnectorConstants.RES_TYPE_JDBC, null);

            status = loadResourcesAndItsRar(defResConfig);
        } catch (ConnectorRuntimeException cre) {
            _logger.log(Level.WARNING, "unable to load Jdbc Connection Pool [ " + poolName + " ]", cre);
        }
        return status;
    }
*/

    /**
     * Redeploy the resource into the server's runtime naming context
     *
     * @param instance a resource object
     * @throws Exception thrown if fail
     */
/*
    public void redeployResource(Object instance) throws Exception {
        ResourceDeployer deployer = factory.getResourceDeployer(instance);
        if(deployer != null) {
            deployer.redeployResource(instance);
        }
    }
*/

    //TODO V3 change exception type ?
/*
    public void deployResource(Object resource) throws Exception {
        ResourceDeployer deployer = factory.getResourceDeployer(resource);
        if(deployer != null){
            deployer.deployResource(resource);
        }
    }
*/

/*
    public void undeployResource(Object resource) throws Exception {
        ResourceDeployer deployer = factory.getResourceDeployer(resource);
        if(deployer != null){
            deployer.undeployResource(resource);
        }
    }
*/
    private Collection getPools(Collection allResources) {
        List pools = new ArrayList();
        for (Object resource : allResources) {
            if (resource instanceof JdbcConnectionPool) {
                JdbcConnectionPool pool = (JdbcConnectionPool) resource;
                pools.add(pool);
            } else if (resource instanceof ConnectorConnectionPool) {
                ConnectorConnectionPool pool = (ConnectorConnectionPool) resource;
                pools.add(pool);
            }
        }
        return pools;
    }

    private Collection getResources(Collection allResources) {
        List resources = new ArrayList();
        for (Object resource : allResources) {
            if (resource instanceof JdbcResource) {
                JdbcResource res = (JdbcResource) resource;
                resources.add(res);
            } else if (resource instanceof ConnectorResource) {
                ConnectorResource res = (ConnectorResource) resource;
                resources.add(res);
            }
        }
        return resources;
    }

    public void ifSystemRarLoad(String rarName)
                           throws ConnectorRuntimeException {
        if(ConnectorsUtil.belongsToSystemRA(rarName)){
            loadDeferredResourceAdapter(rarName);
        }
    }


/*
    */
/**
 * Find if a resource is either a JDBC Connection pool or a
 * Connector Connection pool.
 * @param resource
 * @return boolean
 */
/*
    private boolean isPool(Object resource) {
        return (resource instanceof JdbcConnectionPool || 
                resource instanceof ConnectorConnectionPool);
    }

    */
/**
 * Find is a resource is either a JDBC Resource or a
 * Connector Resource.
 * @param resource
 * @return boolean
 */
/*
    private boolean isResource(Object resource) {
        return (resource instanceof JdbcResource || 
                resource instanceof ConnectorResource);
    }
*/
}
