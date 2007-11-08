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

/*
 * @(#) StandAloneEJBModulesManager.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.server;

import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.admin.event.DeployEventListenerHelper;
import com.sun.enterprise.admin.event.ModuleDeployEvent;
import com.sun.enterprise.admin.event.ModuleDeployEventListener;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.instance.EjbModulesManager;

import com.sun.enterprise.security.acl.RoleMapper;
import com.sun.enterprise.deployment.Application;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.deployment.backend.Deployer;
import com.sun.enterprise.deployment.backend.DeployerFactory;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentCommand;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.config.ConfigContext;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

//for jsr77
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.EjbDeploymentDescriptorFile;
import com.sun.enterprise.deployment.node.J2EEDocumentBuilder;
import java.util.Iterator;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import javax.management.MBeanException;
import com.sun.enterprise.management.StateManageable;
import com.sun.enterprise.deployment.autodeploy.AutoDirReDeployer;

/**
 * Manages all stand alone EJB modules. It also acts as an listener for
 * the deployment events.
 *
 * @author  Mahesh Kannan
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
public class StandAloneEJBModulesManager extends AbstractManager
        implements ModuleDeployEventListener { 

    /** logger to log core messages */
    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    /** local string manager */
    private static StringManager localStrings = 
                StringManager.getManager(StandAloneEJBModulesManager.class);

    /**
     * This manager is responsible for loading all stand alone ejb modules 
     * in an instance. It creates EJBModuleLoader for each deployed ejb module.
     * EJBModuleLoader is responsible for further loading ejb containers for
     * each module.
     *
     * <p>
     * It is assumed that the next two params are set by the startup Code.
     *
     * @param ejbManager         encapsulates application related info
     * @param commonclassLoader  the top level class loader. For now this 
     *                           can be the MainThread's ClassLoader.
     */
    StandAloneEJBModulesManager(EjbModulesManager ejbManager,
            ClassLoader parentClassLoader) {

        super(parentClassLoader, ejbManager);

        AdminEventListenerRegistry.addModuleDeployEventListener(this);
    }

    /**
     * Returns a loader for stand alone ejb module. 
     *
     * @param    moduleId    registration name of the ejb module
     * 
     * @return   a loader for the stand alone ejb module
     */
    protected AbstractLoader getLoader(String moduleId) {
        return new EJBModuleLoader(moduleId, this.parentClassLoader, 
            (EjbModulesManager)this.configManager);
    }

    private void holdRequest() { }

    private void holdRequest(String appID) { }

    // ---- START OF MonitorListener METHODS ----------------------------------

    /**
     * This is a callback from reload monitor when a change in time stamp 
     * for a stand alone ejb module is detected. 
     *
     * @param    entry    monitored entry with a change in time stamp
     * @return   if stand alone ejb module was reloaded successfully
     */
    public synchronized boolean reload(MonitorableEntry entry) {

		String moduleName = entry.getId();
        boolean status = false;

        // redeploys the stand alone ejb module
        try {

            DeploymentRequest req = new DeploymentRequest(
                            this.configManager.getInstanceEnvironment(), 
                            DeployableObjectType.EJB, 
                            DeploymentCommand.DEPLOY);

            // monitored file points to $APP_ROOT/.reload
            req.setFileSource(entry.getMonitoredFile().getParentFile());

            // application registration name
            req.setName(moduleName);

            // we are always trying a redeployment
            req.setForced(true);

	   AutoDirReDeployer deployer = new AutoDirReDeployer(req);
	   status =  deployer.redeploy();

        } catch (IASDeploymentException de) {
            _logger.log(Level.WARNING,"core.error_in_reload_ejb_module",de);
            return false;
        }
        return status;
    }
    // ---- END OF MonitorListener METHODS ------------------------------------

    private boolean moduleDeployed(boolean jsr77, String moduleName,
		ConfigContext dynamicConfigContext)
    {
	return moduleDeployed(jsr77, moduleName, true, dynamicConfigContext);
    }

    private boolean moduleDeployed(boolean jsr77, String moduleName,
		boolean addToRM, ConfigContext dynamicConfigContext)
    {
        boolean result = false;
        boolean loadJSR77 = jsr77 || loadJSR77(moduleName, DeployableObjectType.EJB);

        AbstractLoader modLoader = null;
        try {

            modLoader = getLoader(moduleName);
	    modLoader.setConfigContext(dynamicConfigContext);

            // create root mBean for this module
            if (loadJSR77) {
                try {
                    modLoader.createRootMBean();
                } catch (MBeanException mbe) {
                    _logger.log(Level.WARNING,
                        "core.error_while_creating_jsr77_root_mbean",mbe);
                }
            }

            if (isEnabled(dynamicConfigContext, moduleName)) {

                if (_logger.isLoggable(Level.FINEST)) {
                   _logger.log(Level.FINEST,
                           "Loading enabled ejb module: " + moduleName);
                }

                //Check to see if the app is already loaded.  If yes, do not reload.
                if (id2loader.get(moduleName) != null) {
                    return true;
                }

                // set jsr77 state STARTING 
                try {
                    modLoader.setState(StateManageable.STARTING_STATE);
                } catch (MBeanException mbe) {
                    _logger.log(Level.WARNING,
                            "core.error_while_setting_jsr77_state",mbe);
                }

                boolean retSts = modLoader.load(jsr77);

                if (retSts) {
                    this.id2loader.put(moduleName, modLoader);

                    // adds the ejb module to be monitored if ON
                    if (addToRM) {
                        addToReloadMonitor(moduleName);
                    }

                    // set jsr77 state to RUNNING
                    try {
                        modLoader.setState(StateManageable.RUNNING_STATE);
                    } catch (MBeanException mbe) {
                        _logger.log(Level.WARNING,
                            "core.error_while_setting_jsr77_state",mbe);
                    }

                    result = true;
                } else {
                    if (loadJSR77) {
                        // delete root and leaf mBeans for this module
                        try {
                            modLoader.deleteLeafAndRootMBeans();
                        } catch (MBeanException mbe) {
                            _logger.log(Level.WARNING,
                            "core.error_while_deleting_jsr77_root_and_leaf_mbeans",mbe);
                        }
                    } else {
                        // set jsr77 state FAILED 
                        try {
                            modLoader.setState(StateManageable.FAILED_STATE);
                        } catch (MBeanException mbe) {
                            _logger.log(Level.WARNING,
                                "core.error_while_setting_jsr77_state",mbe);
                        }
                    }

                    _logger.log(Level.WARNING,
                                "core.error_while_loading_ejb_module");
                }
            } else {
                _logger.log(Level.INFO, "core.ejb_module_disabled", moduleName);
                //setting result to false since module was disabled
                result = false;
            }
        } catch (ConfigException ce) {
                        _logger.log(Level.WARNING,"core.error_while_loading_ejb_module",ce);
            result = false;
        } finally {
	    //To ensure that the config context is not reused...
	    if (modLoader != null) {
		modLoader.setConfigContext(null);
	    }
	}
        return result;
    }

    /**
     * Unloads the given stand alone ejb module.
     * This removes this ejb module from reload monitor list.
     * This gets called when the ejb module is undeployed.
     *
     * @param    moduleName    name of the stand alone ejb module
     * @return   true if module was unloaded successfully
     *
     * @throws   ConfigException  if an error while reading data from config
     */
    private boolean moduleUnDeployed(String moduleName) {
        return moduleUnDeployed(false, moduleName);
    }

    /**
     * Unloads the given stand alone ejb module.
     * This removes this ejb module from reload monitor list.
     * This gets called when the ejb module is undeployed.
     *
     * @param    jsr77         delete jsr77 mBeans if true
     * @param    moduleName    name of the stand alone ejb module
     * @return   true          if module was unloaded successfully
     *
     * @throws   ConfigException  if an error while reading data from config
     */
    private boolean moduleUnDeployed(boolean jsr77, String moduleName) {
        return moduleUnDeployed(jsr77, moduleName, true);
    }

    /**
     * Unloads the given ejb module.
     *
     * @param    jsr77         delete jsr77 mBeans if true
     * @param    moduleName    name of the stand alone ejb module
     * @param    clearRM       reloadMonitor, reload if true
     * @return   true          if module was unloaded successfully
     *
     * @throws   ConfigException  if an error while reading data from config
     */
    private boolean moduleUnDeployed(boolean jsr77, String moduleName, boolean clearRM) {

        EJBModuleLoader modLoader = 
            (EJBModuleLoader) this.id2loader.remove(moduleName);

        // removes this ejb module from reload monitor
        if (clearRM) {
            removeFromReloadMonitor(moduleName);
        }

        // module is not in the registry - must have been undeployed 
        if (modLoader == null) {
            return true;
        }

        // set jsr77 state to STOPPING
        try {
            modLoader.setState(StateManageable.STOPPING_STATE);
        } catch (MBeanException mbe) {
            _logger.log(Level.WARNING,
                "core.error_while_setting_jsr77_state",mbe);
        }

        Application app = modLoader.getApplication();
        RoleMapper.removeRoleMapper(app.getRoleMapper().getName());

        if (jsr77) {
            // delete jsr77 mBean for this ejbModule
            try {
                modLoader.deleteRootMBean();
            } catch (MBeanException mbe) {
                _logger.log(Level.WARNING,
                        "core.error_while_deleting_jsr77_root_mbean",mbe);
            }
        } else {
            // set jsr77 state to STOPPED
            try {
                modLoader.setState(StateManageable.STOPPED_STATE);
            } catch (MBeanException mbe) {
                _logger.log(Level.WARNING,
                        "core.error_while_setting_jsr77_state",mbe);
            }
        }

        boolean undeployed = modLoader.unload(jsr77);

        if (undeployed) {
            _logger.log(Level.INFO, 
                        "core.ejb_module_unload_successful", moduleName);
        } else {
            _logger.log(Level.INFO, 
                        "core.ejb_module_not_unloaded", moduleName);
        }

        return undeployed;
    }

    // ---- START OF ModuleDeployEventListener METHODS ------------------------

    /**
     * Invoked when a standalone J2EE module is deployed.
     */
	    public synchronized void moduleDeployed(ModuleDeployEvent event) 
            throws AdminEventListenerException {

        boolean jsr77 = false;

        if (event.getModuleTypeCode() == event.TYPE_EJBMODULE_CODE) {
            
            DeployEventListenerHelper.getDeployEventListenerHelper().synchronize(event);

            String modID = event.getModuleName();

            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "Deploying EJB Module: " + modID);
            }

            try {
                // refreshes the config context with the context from this event
                this.configManager.refreshConfigContext(
                                        event.getConfigContext() );

                // set jsr77 flag
                String action = event.getAction();
                if ((action.equals(BaseDeployEvent.DEPLOY)) ||
                     (action.equals(BaseDeployEvent.REDEPLOY))) {
                    jsr77 = true;
                }

                if (isEnabled(event.getConfigContext(), modID) && 
                      !moduleDeployed(jsr77, modID, event.getConfigContext())) {
                          
                    String msg = localStrings.getString(
                        "standaloneejbmodulesmgr.ejbmodule_deployed_failed", 
                        modID);
                    registerException(event, msg);
                }
            } catch (ConfigException ce) {
                throw new AdminEventListenerException(ce.getMessage()); 
            }
        }
    }

   /**
    * Invoked when a standalone J2EE module is undeployed.
    */
    public synchronized void moduleUndeployed(ModuleDeployEvent event) 
            throws AdminEventListenerException {

        boolean jsr77 = false;

        String action = event.getAction();
        if ((action.equals(BaseDeployEvent.UNDEPLOY)) ||
            (action.equals(BaseDeployEvent.REDEPLOY))) {
            jsr77 = true;
        }

        try {
            if (event.getModuleTypeCode() == event.TYPE_EJBMODULE_CODE) {

                String modID = event.getModuleName();

                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST,
                                "Undeploying EJB Module: " + modID);
                }

                // refreshes the config context with the context from this event
                this.configManager.refreshConfigContext(
                                event.getOldConfigContext() );

                boolean undeployed = moduleUnDeployed(jsr77, modID);

                if (!undeployed) {
                    String msg = localStrings.getString(
                        "standaloneejbmodulesmgr.ejbmodule_undeployed_failed", 
                        modID);
                    registerException(event, msg);
                }
            }
        } catch (ConfigException ce) {
            throw new AdminEventListenerException(ce.getMessage()); 
        }
    }

   /**
    * Invoked when a standalone J2EE module is redeployed.
    */
    public synchronized void moduleRedeployed(ModuleDeployEvent event) 
            throws AdminEventListenerException {

        if (event.getModuleTypeCode() == event.TYPE_EJBMODULE_CODE) {

            String modID = event.getModuleName();

                        if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST,
                            "Redeploying EJB Module: " + modID);
            }

            moduleUndeployed(event);
            moduleDeployed(event);
        }
    }

   /**
    * Invoked when a standalone J2EE module is enabled.
    */
    public synchronized void moduleEnabled(ModuleDeployEvent event) 
            throws AdminEventListenerException {

        if (event.getModuleTypeCode() == event.TYPE_EJBMODULE_CODE) {

            String modID = event.getModuleName();

            if (!isEnabled(event.getConfigContext(), modID)) {
                return;
            }

            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "Enabling EJB Module: " + modID);
            }

            moduleDeployed(event);
        }
    }

   /**
    * Invoked when a standalone J2EE module is disabled.
    */
    public synchronized void moduleDisabled(ModuleDeployEvent event) 
            throws AdminEventListenerException {

        if (event.getModuleTypeCode() == event.TYPE_EJBMODULE_CODE) {

            String modID = event.getModuleName();

                        if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST,
                            "Disabling EJB Module: " + modID);
            }

            moduleUndeployed(event);
        }
    }
    
    
/**
     * Invoked when a  reference is created from a
     * server instance (or cluster) to a particular module.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void moduleReferenceAdded(ModuleDeployEvent event)
            throws AdminEventListenerException {
                
}

    /**
     * Invoked when a reference is removed from a
     * server instance (or cluster) to a particular module.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void moduleReferenceRemoved(ModuleDeployEvent event)
            throws AdminEventListenerException {
                
    }
    
    

    // ---- END OF ModuleDeployEventListener METHODS ------------------------


}
