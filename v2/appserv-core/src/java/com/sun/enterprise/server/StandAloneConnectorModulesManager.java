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

package com.sun.enterprise.server;

import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.admin.event.DeployEventListenerHelper;
import com.sun.enterprise.admin.event.ModuleDeployEvent;
import com.sun.enterprise.admin.event.ModuleDeployEventListener;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.instance.ConnectorModulesManager;
import com.sun.enterprise.management.StateManageable;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanException;


/**
 * Connector Manager  acts as an listener for the deployment events.
 *
 * @author  Orit Flint
 * @since   JDK1.4
 */
public class StandAloneConnectorModulesManager extends AbstractManager
    implements ModuleDeployEventListener {
    // ConnectorModulesManager configManager;
    //  Hashtable id2loader;

    /**
            Logger to log core messages
    */
    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    private static StringManager localStrings = StringManager.getManager(
            "com.sun.enterprise.server");

    /**
     * Connector Manager is responsible for loading connector when deploy event is invoked.
     *
     * It is assumed that the next two params are set by the Startup Code.
     *
     * @param configManager         encapsulates application related info
     */
    StandAloneConnectorModulesManager(
        ConnectorModulesManager connectorManager, ClassLoader parentClassLoader) {
        //  this.configManager=configManager;
        super(parentClassLoader, connectorManager);
        AdminEventListenerRegistry.addModuleDeployEventListener(this);
    }

    protected AbstractLoader getLoader(String moduleId) {
        return new ConnectorModuleLoader(moduleId, this.parentClassLoader,
            (ConnectorModulesManager) this.configManager);
    }


    /**
     * Loads the given stand alone connector module.
     *
     * @param    jsr77         create jsr77 mBeans if true
     * @param    moduleName    name of the stand alone connector module
     * @return   true          if module was loaded successfully
     *
     * @throws    AdminEventListenerException   if an error while reading data from config
     */
    private boolean moduleDeployed(
            boolean jsr77, ConfigContext config, String moduleName)
        throws AdminEventListenerException {

        boolean result = false;
        boolean loadJSR77 = jsr77 || loadJSR77(moduleName, DeployableObjectType.CONN);

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                "In connector moduleDeployed event,name=" + moduleName);
        }

        AbstractLoader modLoader = getLoader(moduleName);
        _logger.log(Level.FINEST, "After get loader");

        // create jsr77 mBean
        if (loadJSR77) {
            try {
                modLoader.createRootMBean();
            } catch (MBeanException mbe) {
                _logger.log(Level.WARNING,
                "core.error_while_creating_jsr77_root_mbean",mbe);
                throw new AdminEventListenerException(mbe.getMessage());
            }
        }

        if (isEnabled(config, moduleName)) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                "Loading enabled moduleName: " + moduleName);
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

                // set jsr77 state to RUNNING
                try {
                    modLoader.setState(StateManageable.RUNNING_STATE);
                } catch (MBeanException mbe) {
                    _logger.log(Level.WARNING,
                        "core.error_while_setting_jsr77_state",mbe);
                }

                result = true;

                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE,
                        "Successfully loaded moduleName: " + moduleName);
                }
            } else {
                _logger.log(Level.WARNING,
                    "core.error_while_loading_connector_module" + " " +
                    moduleName);

                // delete root and leaf mBeans if it is a deploy event
                try {
                    if (loadJSR77) {
                        modLoader.deleteLeafAndRootMBeans();
                    }
                } catch (MBeanException mbe) {
                    _logger.log(Level.WARNING,
                        "core.error_while_deleting_jsr77_leaf_and_root_mbeans",mbe);
                    throw new AdminEventListenerException(mbe.getMessage());
                }

                // set jsr77 state to FAILED
                try {
                    modLoader.setState(StateManageable.FAILED_STATE);
                } catch (MBeanException mbe) {
                    _logger.log(Level.WARNING,
                        "core.error_while_setting_jsr77_state",mbe);
                }

                String msg = localStrings.getString(
                    "connector.error_while_loading_connector_module", moduleName);
                throw new AdminEventListenerException(msg);
            }
        } else {
            //setting result to false if module is not enabled.
            result = false;
        }
        return result;
    }

    /**
     * Unloads the given stand alone connector module.
     *
     * @param    jsr77         delete jsr77 mBeans if true
     * @param    moduleName    name of the stand alone connector module
     * @return   true          if module was unloaded successfully
     *
     * @throws   ConfigException  if an error while reading data from config
     */
    private boolean moduleUndeployed(boolean jsr77, String moduleName, boolean cascade) {

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                "In connector moduleUndeployed event,name=" + moduleName);
        }

        // get loader
        AbstractLoader modLoader = (ConnectorModuleLoader) this.id2loader.remove(moduleName);

        if (modLoader == null) {
            return true;
        }
		modLoader.setCascade(cascade);

        // set jsr77 state to STOPPING
        try {
            modLoader.setState(StateManageable.STOPPING_STATE);
        } catch (MBeanException mbe) {
            _logger.log(Level.WARNING,
                "core.error_while_setting_jsr77_state",mbe);
        }

        boolean undeployed = modLoader.unload(jsr77);

        if (undeployed) {
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
        }

        return undeployed;
    }

    /**
     * Invoked when a standalone connector module is deployed.
     */
    public synchronized void moduleDeployed(ModuleDeployEvent event)
        throws AdminEventListenerException {

        if (event.getModuleType().equals(event.TYPE_CONNECTOR)) {
            DeployEventListenerHelper.getDeployEventListenerHelper().synchronize(event);
        }

        if(event.getForceDeploy() ) {
            moduleEnabled(event);
            return;
        } else {
            //As of 8.1 PE/SE/EE, RA Configs will not be deployed in the connector
            //backend when created, but would be lazily loaded while
            //the connector module is deployed in the backend.
            try {
                ConnectorResourcesLoader connectorResourcesLoader = null;
                connectorResourcesLoader= new ConnectorResourcesLoader();
                connectorResourcesLoader.loadRAConfigs(event.getModuleName());
            } catch (ConfigException e) {
                _logger.log(Level.WARNING, "" + e.getMessage());
                AdminEventListenerException aele = new AdminEventListenerException();
                aele.initCause(e);
                throw aele;
            }
            
            realDeployed(event);
        }
    }

   /**
    * Performs the deployment of a standalone connector module
    * This is method is used by moduleDeployed and moduleEnabled
    * to perform common operations
    */
   private void realDeployed(ModuleDeployEvent event) throws AdminEventListenerException {
        boolean jsr77 = false;
        

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                "In StandAloneConnectorModulesManager moduleDeployed");
            _logger.log(Level.FINEST, "ModuleType=" + event.getModuleType());
        }

        if (event.getModuleType().equals(event.TYPE_CONNECTOR)) {
            String modID = event.getModuleName();

            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "modID=" + modID);
            }

            try {
                // refreshes the config context with the context from this event
                this.configManager.refreshConfigContext(event.getConfigContext());

                // set jsr77 flag
                // which is used to signify if the event is deploy or undeploy
                // to create or delete jsr77 mBeans
                String action = event.getAction();
                if ((action.equals(BaseDeployEvent.DEPLOY)) ||
                        (action.equals(BaseDeployEvent.REDEPLOY))) {
                    jsr77 = true;
                }

                if (isEnabled(event.getConfigContext(), modID) && 
                      !moduleDeployed(jsr77, event.getConfigContext(), modID)) {
                // throw an exception is load fails
                    String msg = localStrings.getString("connector.connector_deployed_failed",
                            modID);
                    throw new AdminEventListenerException(msg);
                }
            } catch (ConfigException ce) {
                throw new AdminEventListenerException(ce.getMessage());
            }
        }
   }

    /**
     * Invoked when a standalone connector module is undeployed.
     */
    public synchronized void moduleUndeployed(ModuleDeployEvent event)
        throws AdminEventListenerException {
                //If forceDeploy is true, we need to disable module instead of undeploying it
                if(event.getForceDeploy() ) {
                    try {
                        moduleDisabled(event);
                    } catch (AdminEventListenerException aele) {
                        _logger.log(Level.FINEST, "Error while UnDeploying module: " + event.getModuleName());
                        throw aele;
                    }
                } else {
                    realUndeployed(event);
                }
    }

   /**
    * Performs the undeployment of a standalone connector module
    * This is used by moduleUndeployed and moduleDisabled and 
    * and houses all their common operations
    */
   private void realUndeployed(ModuleDeployEvent event)  throws AdminEventListenerException {
        boolean jsr77 = false;

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                "In StandAloneConnectorModulesManager moduleUndeployed");
        }

        String action = event.getAction();

        if ((action.equals(BaseDeployEvent.UNDEPLOY)) ||
                (action.equals(BaseDeployEvent.REDEPLOY))) {
            jsr77 = true;
        }

        
        try {
            
            if (event.getModuleType().equals(event.TYPE_CONNECTOR)) {
                String modID = event.getModuleName();

                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "UnDeploying module: " + modID);
                }

                boolean undeployed = false;
                try {
                    ResourcesUtil.setEventConfigContext(event.getConfigContext());
                undeployed = moduleUndeployed(jsr77, modID, event.getCascade());
                } finally {
                    ResourcesUtil.resetEventConfigContext();
                }
                
                if (!undeployed) {
                    String msg = localStrings.getString("connector.connector_undeployed_failed",
                            modID);
                    throw new AdminEventListenerException(msg);
                }

                // refreshes the config context with the context from this event
                this.configManager.refreshConfigContext(event.getConfigContext());
            }
        } catch (ConfigException ce) {
            throw new AdminEventListenerException(ce.getMessage());
        }
    }

    /**
     * Invoked when a standalone connector module is redeployed.
     */
    public synchronized void moduleRedeployed(ModuleDeployEvent event)
        throws AdminEventListenerException {
        if (event.getModuleType().equals(event.TYPE_CONNECTOR)) {
            String modID = event.getModuleName();

            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "ReDeploying module: " + modID);
            }

            moduleUndeployed(event);
            moduleDeployed(event);
        }
    }

    /**
     * Invoked when a standalone connector module is enabled.
     */
    public synchronized void moduleEnabled(ModuleDeployEvent event)
        throws AdminEventListenerException {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                "In StandAloneConnectorModulesManager moduleEnabled");
        }

        if (event.getModuleType().equals(event.TYPE_CONNECTOR)) {
            String modID = event.getModuleName();

            if (!isEnabled(event.getConfigContext(), event.getModuleName())) {
                return;
            }

            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "Module : " + modID + " enabled...");
            }
            String appName = event.getModuleName();
            ConnectorResourcesLoader connectorResourcesLoader = null;
            try {
                connectorResourcesLoader=
                      new ConnectorResourcesLoader(); 
            }catch(ConfigException ce) {
                String msg = localStrings.getString(
                      "connector.connector_enable_failed",appName);
                _logger.log(Level.SEVERE,
                      "core.failed_to_get_connectorresourcesloader");
                throw new AdminEventListenerException(msg);
            }
            
            try {
                ResourcesUtil.setEventConfigContext(event.getConfigContext());

                connectorResourcesLoader.loadRAConfigs(appName);
                realDeployed(event);
                connectorResourcesLoader.load(appName);
            } catch(AdminEventListenerException ex) {
                throw ex;
            } catch(Throwable th) {
                AdminEventListenerException aele =  
                           new AdminEventListenerException();
                aele.initCause(th);
                throw aele;
            }finally {
                ResourcesUtil.resetEventConfigContext();
            }
        }
    }

    /**
     * Invoked when a standalone connector module is disabled.
     */
    public synchronized void moduleDisabled(ModuleDeployEvent event)
        throws AdminEventListenerException {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                "In StandAloneConnectorModulesManager moduleDisabled");
        }

        if (event.getModuleType().equals(event.TYPE_CONNECTOR)) {
            String modID = event.getModuleName();

            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "Module : " + modID + " disabled...");
            }

            AbstractLoader modLoader = 
                   (ConnectorModuleLoader) this.id2loader.get(modID);
            event.setCascade(true);
            realUndeployed(event);
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


// END OF IASRI 4666602
