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

import com.sun.logging.LogDomains;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.AdminObjectResource;
import com.sun.enterprise.connectors.ConnectorRuntime;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.javaee.services.ResourceManager;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a connector application, one per resource-adapter.
 * GlassFish kernel will call start/stop of connector application during start/stop of server and
 * deploy/undeploy of the resource-adapter.
 *
 * @author Jagadish Ramu
 */
public class ConnectorApplication implements ApplicationContainer {
    private static Logger _logger = LogDomains.getLogger(ConnectorApplication.class, LogDomains.RSR_LOGGER);
    private String moduleName = "";
    //indicates the "application" (ear) name if its embedded rar
    private String applicationName = null;
    private ResourceManager resourceManager;
    private ClassLoader loader;
    private ConnectorRuntime runtime;

    public ConnectorApplication(String moduleName, String appName, ResourceManager resourceManager, ClassLoader loader,
                                ConnectorRuntime runtime) {
        this.setModuleName(moduleName);
        this.resourceManager = resourceManager;
        this.loader = loader;
        this.runtime = runtime;
        this.applicationName = appName;
    }

    /**
     * Returns the deployment descriptor associated with this application
     *
     * @return deployment descriptor if they exist or null if not
     */
    public Object getDescriptor() {
        //TODO V3 implement ?
        return null;
    }

    /**
     * Starts an application container.
     * ContractProvider starting should not throw an exception but rather should
     * use their prefered Logger instance to log any issue they encounter while
     * starting. Returning false from a start mean that the container failed
     * to start
     *
     * @param startupContext the start up context
     * @return true if the container startup was successful.
     */
    public boolean start(ApplicationContext startupContext) {
        boolean started = false;

        deployResources();
        runtime.registerConnectorApplication(this);

        started = true; // TODO V3 temporary
        logFine("Resource Adapter [ " + getModuleName() + " ] started");
        return started;
    }

    /**
     * deploy all resources/pools pertaining to this resource adapter
     */
    public void deployResources() {
        Resources allResources = resourceManager.getAllResources();
        //TODO V3 needed for redeploy of module, what happens to the listeners of these resources ?
        Collection<ConnectorConnectionPool> connectionPools =
                ConnectorsUtil.getAllPoolsOfModule(moduleName, allResources);
        Collection<String> poolNames = ConnectorsUtil.getAllPoolNames(connectionPools);
        Collection<Resource> resources = ConnectorsUtil.getAllResources(poolNames, allResources);
        AdminObjectResource[] adminObjectResources =
                ConnectorsUtil.getEnabledAdminObjectResources(moduleName, allResources, null);
        for(AdminObjectResource aor : adminObjectResources){
            resources.add(aor);
        }
        resourceManager.deployResources(resources);
    }

    /**
     * undeploy all resources/pools pertaining to this resource adapter
     */
    public void undeployResources() {
        Resources allResources = resourceManager.getAllResources();
        Collection<ConnectorConnectionPool> connectionPools =
                ConnectorsUtil.getAllPoolsOfModule(moduleName, allResources);
        Collection<String> poolNames = ConnectorsUtil.getAllPoolNames(connectionPools);
        Collection<Resource> connectorResources = ConnectorsUtil.getAllResources(poolNames, allResources);
        AdminObjectResource[] adminObjectResources =
                ConnectorsUtil.getEnabledAdminObjectResources(moduleName, allResources, null);
        List<Resource> resources = new ArrayList<Resource>();
        resources.addAll(connectorResources);
        resources.addAll(connectionPools);
        for(AdminObjectResource aor : adminObjectResources){
            resources.add(aor);
        }

        resourceManager.undeployResources(resources);
    }

    /**
     * Stop the application container
     *
     * @param stopContext
     * @return true if stopping was successful.
     */
    public boolean stop(ApplicationContext stopContext) {
        boolean stopped = false;
        undeployResources();
        runtime.unregisterConnectorApplication(getModuleName());
        stopped = true;
        logFine("Resource Adapter [ " + getModuleName() + " ] stopped");
        return stopped;
    }

    /**
     * Suspends this application container.
     *
     * @return true if suspending was successful, false otherwise.
     */
    public boolean suspend() {
        // Not (yet) supported
        return false;
    }

    /**
     * Resumes this application container.
     *
     * @return true if resumption was successful, false otherwise.
     */
    public boolean resume() {
        // Not (yet) supported
        return false;
    }

    /**
     * Returns the class loader associated with this application
     *
     * @return ClassLoader for this app
     */
    public ClassLoader getClassLoader() {
        return loader;
    }

    public void logFine(String message) {
        _logger.log(Level.FINE, message);
    }

    /**
     * returns the module name
     * @return module-name
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * set the module name of the application
     * @param moduleName module-name
     */
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getApplicationName() {
        return applicationName;
    }
}
