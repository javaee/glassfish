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

package com.sun.enterprise.connectors.module;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.glassfish.javaee.services.ResourceAdaptersBinder;
import org.glassfish.javaee.services.ResourceManager;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO V3 shouldn't the Deployer be Deployer<ConnectorContainer, ConnectorApplication> ??

//TODO V3 why should it be Deployer<CC,CA> ??

/**
 * Deployer for a resource-adapter.
 *
 * @author Jagadish Ramu
 */
@Service
public class ConnectorDeployer extends JavaEEDeployer<ConnectorContainer, ConnectorApplication> implements PostConstruct {

    @Inject
    private ConnectorRuntime runtime;

    @Inject
    private ConnectorResource[] connectorResources;

    @Inject
    private ConnectorConnectionPool[] connectorConnectionPools;

    @Inject
    private ResourceAdaptersBinder binder;

    @Inject
    private ResourceManager resourceManager;

    //private long startTime;

    private static Logger _logger = LogDomains.getLogger(ConnectorDeployer.class, LogDomains.RSR_LOGGER);

    public ConnectorDeployer() {
        //startTime = System.currentTimeMillis();
    }

    /**
     * Loads the meta date associated with the application.
     *
     * @parameters type type of metadata that this deployer has declared providing.
     */
    public <T> T loadMetaData(Class<T> type, DeploymentContext context) {
        return null;
    }    

    /**
     * Loads a previously prepared application in its execution environment and
     * return a ContractProvider instance that will identify this environment in
     * future communications with the application's container runtime.
     *
     * @param container in which the application will reside
     * @param context   of the deployment
     * @return an ApplicationContainer instance identifying the running application
     */
    public ConnectorApplication load(ConnectorContainer container, DeploymentContext context) {
        File sourceDir = context.getSourceDir();
        String sourcePath = sourceDir.getAbsolutePath();
        String moduleName = sourceDir.getName();
        //TODO V3 this check is not needed ?
        if (!ConnectorsUtil.belongsToSystemRA(moduleName)) {
            try {
                runtime.createActiveResourceAdapter(sourcePath, moduleName);
            } catch (ConnectorRuntimeException cre) {
                //TODO V3 log exception
                _logger.log(Level.WARNING, " unable to load the resource-adapter [ " + moduleName + " ]", cre);
            }
        }
        Map<ConnectorResource, ConnectorConnectionPool> raResourcePoolMap =
                new HashMap<ConnectorResource, ConnectorConnectionPool>();
        Map<String, ConnectorConnectionPool> raPools = getConnectorPoolsForRA(connectorConnectionPools, moduleName);
        for (ConnectorResource resource : connectorResources) {
            if (raPools.containsKey(resource.getPoolName())) {
                raResourcePoolMap.put(resource, raPools.get(resource.getPoolName()));
            }
        }
        return new ConnectorApplication(moduleName, resourceManager, runtime);
    }

    /**
     * retrives all the connection pools of the given resource adapter.
     * @param ccp All connector connection pools
     * @param raName Resource-adapter name
     * @return connection pools of the given resource-adapter
     */
    private Map<String, ConnectorConnectionPool> getConnectorPoolsForRA(ConnectorConnectionPool[] ccp, String raName) {
        Map<String, ConnectorConnectionPool> raPools = new HashMap<String, ConnectorConnectionPool>();
        for (ConnectorConnectionPool pool : ccp) {
            if (pool.getResourceAdapterName().equalsIgnoreCase(raName)) {
                raPools.put(pool.getName(), pool);
            }
        }
        return raPools;
    }

    /**
     * Unload or stop a previously running application identified with the
     * ContractProvider instance. The container will be stop upon return from this
     * method.
     *
     * @param appContainer instance to be stopped
     * @param context      of the undeployment
     */
    //TODO V3 unload's job is to remove the app's content from class-loader or class-loader specific to this app
    //TODO V3 application.start/stop() will take care of creating/destryingARA

    public void unload(ConnectorApplication appContainer, DeploymentContext context) {
        File sourceDir = context.getSourceDir();
        String moduleName = sourceDir.getName();

        try {
            runtime.destroyActiveResourceAdapter(moduleName, true);
        } catch (ConnectorRuntimeException e) {
            //TODO V3 log exception
            _logger.log(Level.WARNING, " unable to unload the resource-adapter [ " + moduleName + " ]", e);
        }
    }

    /**
     * Clean any files and artifacts that were created during the execution
     * of the prepare method.
     *
     * @param context deployment context
     */
    public void clean(DeploymentContext context) {
    }

    protected RootDeploymentDescriptor getDefaultBundleDescriptor() {
        //TODO V3
        return null;
    }

    protected String getModuleType() {
        return "connectors";
    }

    /**
     * The component has been injected with any dependency and
     * will be placed into commission by the subsystem.
     */
    public void postConstruct() {
        //logFine("Time taken to initialize connector deployer : " + (System.currentTimeMillis() - startTime));
    }

    public void logFine(String message) {
        _logger.log(Level.FINE, message);
    }
}
