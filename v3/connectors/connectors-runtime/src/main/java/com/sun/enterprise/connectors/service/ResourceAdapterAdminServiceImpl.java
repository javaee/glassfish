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

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.connectors.ActiveResourceAdapter;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.module.ConnectorApplication;
import com.sun.enterprise.connectors.util.ConnectorDDTransformUtils;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.config.serverbeans.ResourceAdapterConfig;

import javax.naming.NamingException;
import java.util.logging.Level;


/**
 * This is resource adapter admin service. It creates, deletes Resource adapter
 * and also the resource adapter configuration updation.
 *
 * @author Binod P.G, Srikanth P and Aditya Gore
 */

public class ResourceAdapterAdminServiceImpl extends ConnectorService {
    /**
     * Default constructor
     */
    public ResourceAdapterAdminServiceImpl() {
        super();
    }

/*
*/
/**
 * Destroys/deletes the Active resource adapter object from the connector
 * container. Active resource adapter abstracts the rar deployed. It checks
 * whether any resources (pools and connector resources) are still present.
 * If they are present the deletion fails and all the objects and
 * datastructures pertaining to to the resource adapter are left untouched.
 *
 * @param moduleName Name of the rarModule to destroy/delete
 * @throws ConnectorRuntimeException if the deletion fails
 */
/* TODO V3 use later
    public void destroyActiveResourceAdapter(String moduleName)
            throws ConnectorRuntimeException {
        destroyActiveResourceAdapter(moduleName, false);
    }
*/

    /**
     * Destroys/deletes the Active resource adapter object from the connector
     * container. Active resource adapter abstracts the rar deployed. It checks
     * whether any resources (pools and connector resources) are still present.
     * If they are present and cascade option is false the deletion fails and
     * all the objects and datastructures pertaining to the resource adapter
     * are left untouched. If cascade option is true, even if resources are
     * still present, they are also destroyed with the active resource adapter
     *
     * @param moduleName Name of the rarModule to destroy/delete
     * @param cascade    If true all the resources belonging to the rar are destroyed
     *                   recursively. If false, and if resources pertaining to
     *                   resource adapter /rar are present deletetion is failed. Then
     *                   cascade should be set to true or all the resources have to
     *                   deleted explicitly before destroying the rar/Active resource
     *                   adapter.
     * @throws ConnectorRuntimeException if the deletion fails
     */
    private void destroyActiveResourceAdapter(
            String moduleName,
            boolean cascade)
            throws ConnectorRuntimeException {

        ResourcesUtil resutil = ResourcesUtil.createInstance();
        if (resutil == null) {
            ConnectorRuntimeException cre =
                    new ConnectorRuntimeException("Failed to get ResourcesUtil object");
            _logger.log(Level.SEVERE, "rardeployment.resourcesutil_get_failure", moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }
        /* TODO V3 - handle resource deletion later
        TODO V3 refactor ?
        Object[][] resources = null;
        try {
            resources = resutil.getAllConnectorResourcesForRar(moduleName);
        } catch (ConfigException ce) {
            ConnectorRuntimeException cre =
                    new ConnectorRuntimeException("Failed to get Resources from domain.xml");
            ce.initCause(ce);
            _logger.log(
                    Level.SEVERE,
                    "rardeployment.resources_list_error",
                    cre);
            throw cre;
        }

        boolean errrorOccured = false;

        if (cascade && resources != null) {
            errrorOccured = deleteResources(resources, cascade, errrorOccured);
        } else if (
                (resources[0] != null && resources[0].length != 0)
                        || (resources[1] != null && resources[1].length != 0)
                        || (resources[2] != null && resources[2].length != 0)) { //TODO V3 need for this check ??
            _logger.log(
                    Level.SEVERE,
                    "rardeployment.pools_and_resources_exist",
                    moduleName);
            ConnectorRuntimeException cre =
                    new ConnectorRuntimeException("Error: Connector Connection Pools/resources exist.");
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }
        */

        if (!stopAndRemoveActiveResourceAdapter(moduleName)) {
            ConnectorRuntimeException cre =
                    new ConnectorRuntimeException("Failed to remove Active Resource Adapter");
            _logger.log(Level.SEVERE, "rardeployment.ra_removal_registry_failure", moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }

        try {

            String descriptorJNDIName = ConnectorAdminServiceUtils.getReservePrefixedJNDINameForDescriptor(moduleName);

            _logger.fine("ResourceAdapterAdminServiceImpl :: destroyActiveRA "
                    + moduleName + " removing descriptor " + descriptorJNDIName);

            _runtime.getNamingManager().getInitialContext().unbind(descriptorJNDIName);

        } catch (NamingException ne) {
            ConnectorRuntimeException cre = new ConnectorRuntimeException("Failed to remove connector descriptor from JNDI");
            cre.initCause(ne);
            _logger.log(Level.SEVERE, "rardeployment.connector_descriptor_jndi_removal_failure", moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }
//        com.sun.enterprise.connectors.util.ConnectorClassLoader.getInstance().removeResourceAdapter(moduleName);
        // TODO: If this is not a system standalone RAR, then
        // emit an event that ApplicationLifecycle (kernel) can listen to.
        // As part of event handling, it can either remove its classloader
        // from appropriate connector class loader.
        /* TODO V3 handle resource destroy later
        if (errrorOccured == true) {
            ConnectorRuntimeException cre =
                    new ConnectorRuntimeException("Failed to remove all connector resources/pools");
            _logger.log(
                    Level.SEVERE,
                    "rardeployment.ra_resource_removal",
                    moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }*/
    }

    /**
     * Creates Active resource Adapter which abstracts the rar module. During
     * the creation of ActiveResourceAdapter, default pools and resources also
     * are created.
     *
     * @param connectorDescriptor object which abstracts the connector deployment descriptor
     *                            i.e rar.xml and sun-ra.xml.
     * @param moduleName          Name of the module
     * @param moduleDir           Directory where rar module is exploded.
     * @throws ConnectorRuntimeException if creation fails.
     */

    public synchronized void createActiveResourceAdapter(ConnectorDescriptor connectorDescriptor,
                                                         String moduleName, String moduleDir, ClassLoader loader)
            throws ConnectorRuntimeException {

        _logger.fine("ResourceAdapterAdminServiceImpl :: createActiveRA "
                + moduleName + " at " + moduleDir);

        ActiveResourceAdapter activeResourceAdapter = _registry.getActiveResourceAdapter(moduleName);
        if (activeResourceAdapter != null) {
            _logger.log(Level.FINE, "rardeployment.resourceadapter.already.started", moduleName);
            return;
        }

/* TODO V3 not needed in v3 ?
        if (loader == null) {
            try {
                loader = connectorDescriptor.getClassLoader();
            } catch (Exception ex) {
                _logger.log(Level.FINE, "No classloader available with connector descriptor");
                loader = null;
            }
        }
*/
        ModuleDescriptor moduleDescriptor = null;
        Application application = null;
        _logger.fine("ResourceAdapterAdminServiceImpl :: createActiveRA "
                + moduleName + " at " + moduleDir + " loader :: " + loader);
        //class-loader can not be null for standalone rar as deployer should have provided one.
        //class-laoder can (may) be null for system-rars as they are not actually deployed.
        if (loader == null && ConnectorsUtil.belongsToSystemRA(moduleName)) {
            if (environment == SERVER) {
                loader = ConnectorRuntime.getRuntime().createConnectorClassLoader(moduleDir);
                if (loader == null) {
                    ConnectorRuntimeException cre =
                            new ConnectorRuntimeException("Failed to obtain the class loader");
                    _logger.log(Level.SEVERE,"rardeployment.failed_toget_classloader");
                    _logger.log(Level.SEVERE, "", cre);
                    throw cre;
                }
            }
        } else {
            connectorDescriptor.setClassLoader(null);
            moduleDescriptor = connectorDescriptor.getModuleDescriptor();
            application = connectorDescriptor.getApplication();
            connectorDescriptor.setModuleDescriptor(null);
            connectorDescriptor.setApplication(null);
        }
        try {
            activeResourceAdapter =
                    ConnectorRuntime.getRuntime().getActiveRAFactory().
                            createActiveResourceAdapter(connectorDescriptor, moduleName, loader);
            _logger.fine("ResourceAdapterAdminServiceImpl :: createActiveRA " +
                    moduleName + " at " + moduleDir +
                    " adding to registry " + activeResourceAdapter);
            _registry.addActiveResourceAdapter(moduleName, activeResourceAdapter);
            _logger.fine("ResourceAdapterAdminServiceImpl:: createActiveRA " +
                    moduleName + " at " + moduleDir
                    + " env =server ? " + (environment == SERVER));

            if (environment == SERVER) {
                activeResourceAdapter.setup();
                String descriptorJNDIName = ConnectorAdminServiceUtils.getReservePrefixedJNDINameForDescriptor(moduleName);
                _logger.fine("ResourceAdapterAdminServiceImpl :: createActiveRA "
                        + moduleName + " at " + moduleDir
                        + " publishing descriptor " + descriptorJNDIName);
                _runtime.getNamingManager().publishObject(
                        descriptorJNDIName, connectorDescriptor, true);
            }

        } catch (NullPointerException npEx) {
            ConnectorRuntimeException cre =
                    new ConnectorRuntimeException("Error in creating active RAR");
            cre.initCause(npEx);
            _logger.log( Level.SEVERE, "rardeployment.nullPointerException", moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        } catch (NamingException ne) {
            ConnectorRuntimeException cre =
                    new ConnectorRuntimeException("Error in creating active RAR");
            cre.initCause(ne);
            _logger.log(Level.SEVERE, "rardeployment.jndi_publish_failure");
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        } finally {
            if (moduleDescriptor != null) {
                connectorDescriptor.setModuleDescriptor(moduleDescriptor);
                connectorDescriptor.setApplication(application);
                connectorDescriptor.setClassLoader(loader);
            }
        }
    }

    /*
     * Updates the connector descriptor of the connector module, with the 
     * contents of a resource adapter config if specified.
     * 
     * This modified ConnectorDescriptor is then bound to JNDI so that ACC 
     * clients while configuring a non-system RAR could get the correct merged
     * configuration. Any updates to resource-adapter config while an ACC client
     * is in use is not transmitted to the client dynamically. All such changes 
     * would be visible on ACC client restart. 
     */
    /* TODO V3 handle resource-adapter-config later
    private void updateRAConfigInDescriptor(ConnectorDescriptor connectorDescriptor,
                                            String moduleName) {

        ResourceAdapterConfig raConfig =
                ConnectorRegistry.getInstance().getResourceAdapterConfig(moduleName);

        ElementProperty[] raConfigProps = null;
        if (raConfig != null) {
            raConfigProps = raConfig.getElementProperty();
        }

        _logger.fine("current RAConfig In Descriptor " + connectorDescriptor.getConfigProperties());

        if (raConfigProps != null) {
            Set mergedProps = ConnectorDDTransformUtils.mergeProps(
                    raConfigProps, connectorDescriptor.getConfigProperties());
            Set actualProps = connectorDescriptor.getConfigProperties();
            actualProps.clear();
            actualProps.addAll(mergedProps);
            _logger.fine("updated RAConfig In Descriptor " + connectorDescriptor.getConfigProperties());
        }

    }*/


    /**
     * Creates Active resource Adapter which abstracts the rar module. During
     * the creation of ActiveResourceAdapter, default pools and resources also
     * are created.
     *
     * @param moduleDir  Directory where rar module is exploded.
     * @param moduleName Name of the module
     * @throws ConnectorRuntimeException if creation fails.
     */
    public synchronized void createActiveResourceAdapter(String moduleDir, String moduleName, ClassLoader loader)
            throws ConnectorRuntimeException {

        ActiveResourceAdapter activeResourceAdapter =
                _registry.getActiveResourceAdapter(moduleName);
        if (activeResourceAdapter != null) {
            _logger.log(Level.FINE, "rardeployment.resourceadapter.already.started", moduleName);
            return;
        }

        if (ConnectorsUtil.belongsToSystemRA(moduleName)) {
            moduleDir = ConnectorsUtil.getSystemModuleLocation(moduleName);
        }

        ConnectorDescriptor connectorDescriptor = ConnectorDDTransformUtils.getConnectorDescriptor(moduleDir);

        if (connectorDescriptor == null) {
            ConnectorRuntimeException cre = new ConnectorRuntimeException("Failed to obtain the connectorDescriptor");
            _logger.log(Level.SEVERE, "rardeployment.connector_descriptor_notfound", moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }

        createActiveResourceAdapter(connectorDescriptor, moduleName, moduleDir, loader);
    }



    /**
     * Stops the resourceAdapter and removes it from connector container/
     * registry.
     *
     * @param moduleName Rarmodule name.
     * @return true it is successful stop and removal of ActiveResourceAdapter
     *         false it stop and removal fails.
     */
    private boolean stopAndRemoveActiveResourceAdapter(String moduleName) {

        ActiveResourceAdapter acr = null;
        if (moduleName != null) {
            acr = _registry.getActiveResourceAdapter(moduleName);
        }
        if (acr != null) {
            acr.destroy();
            boolean status = _registry.removeActiveResourceAdapter(moduleName);
            return status;
        }
        return false;
    }

    /**
     * Checks if the rar module is already reployed.
     *
     * @param moduleName Rarmodule name
     * @return true if it is already deployed. false if it is not deployed.
     */
    public boolean isRarDeployed(String moduleName) {

        ActiveResourceAdapter activeResourceAdapter =
                _registry.getActiveResourceAdapter(moduleName);
        if (activeResourceAdapter != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Calls the stop method for all J2EE Connector 1.5/1.0 spec compliant RARs
     */
    public void stopAllActiveResourceAdapters() {
        ActiveResourceAdapter[] resourceAdapters =
                ConnectorRegistry.getInstance().getAllActiveResourceAdapters();

        for (int i = 0; i < resourceAdapters.length; i++) {
            String raName = resourceAdapters[i].getModuleName();
            stopActiveResourceAdapter(raName, true);
        }
    }

    /**
     * stop the active resource adapter (runtime)
     * @param raName resource-adapter name
     * @param cascade if cascade is true, remove all the resources, pools
     */
    public void stopActiveResourceAdapter(String raName, boolean cascade) {
        _logger.log(Level.FINE, "Stopping RA : ", raName);
        try {
            destroyActiveResourceAdapter(raName, cascade);
        } catch (ConnectorRuntimeException cre) {
            _logger.log(Level.WARNING, "unable to stop resource adapter [ " + raName + " ]", cre.getMessage());
            _logger.log(Level.FINE, "unable to stop resource adapter [ " + raName + " ]", cre);
        }
    }

    /**
     * add the resource-adapter-config
     * @param rarName resource-adapter name
     * @param raConfig resource-adapter-config
     * @throws ConnectorRuntimeException
     */
    public void addResourceAdapterConfig(String rarName, ResourceAdapterConfig raConfig)
        throws ConnectorRuntimeException {
        if (rarName != null && raConfig != null) {
            _registry.addResourceAdapterConfig(rarName, raConfig);
            reCreateActiveResourceAdapter(rarName);
        }
    }

    /**
	 * Delete the resource adapter configuration to the connector registry
	 *
	 * @param rarName
	 */
    public void deleteResourceAdapterConfig(String rarName) {
        if (rarName != null) {
            _registry.removeResourceAdapterConfig(rarName);
        }
    }

    /**
	 * The ActiveResourceAdapter object which abstract the rar module is
	 * recreated in the connector container/registry. All the pools and
	 * resources are killed. But the infrastructure to create the pools and and
	 * resources is untouched. Only the actual pool is killed.
	 *
	 * @param moduleName
	 *                     rar module Name.
	 * @throws ConnectorRuntimeException
	 *                      if recreation fails.
	 */

    public void reCreateActiveResourceAdapter(String moduleName)
        throws ConnectorRuntimeException {
        String moduleDir= null;
        //TODO V3 is there a case where RAR is not deployed ?
        if (isRarDeployed(moduleName)) {
            ConnectorApplication app = _registry.getConnectorApplication(moduleName);
            app.undeployResources();
            stopAndRemoveActiveResourceAdapter(moduleName);
            moduleDir= ConnectorsUtil.getLocation(moduleName);
            createActiveResourceAdapter(moduleDir, moduleName, app.getClassLoader());
            _registry.getConnectorApplication(moduleName).deployResources();
        } else {
            moduleDir= ConnectorsUtil.getLocation(moduleName);
            if (moduleDir != null) {
                ConnectorApplication app = _registry.getConnectorApplication(moduleName);
                createActiveResourceAdapter(moduleDir, moduleName, app.getClassLoader());
                _registry.getConnectorApplication(moduleName).deployResources();
            }
        }
    }
}
