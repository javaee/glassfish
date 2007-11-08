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
 * @(#) ApplicationManager.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Infolrmation and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.server;

import com.sun.enterprise.appclient.jws.AppclientJWSSupportManager;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;

import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.admin.event.ApplicationDeployEvent;
import com.sun.enterprise.admin.event.ApplicationDeployEventListener;
import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.DeployEventListenerHelper;

import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.security.acl.RoleMapper;
import com.sun.enterprise.server.pluggable.ApplicationLoaderFactory;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;

import com.sun.enterprise.deployment.Application;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.deployment.backend.Deployer;
import com.sun.enterprise.deployment.backend.DeployerFactory;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentCommand;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.autodeploy.AutoDirReDeployer;
import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapper;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactory;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactoryMgr;
import com.sun.enterprise.security.util.IASSecurityException;

import com.sun.enterprise.config.ConfigContext;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

//for jsr77
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.node.J2EEDocumentBuilder;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import java.util.Iterator;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import javax.management.MBeanException;
//import com.sun.enterprise.management.model.J2EEDeployedObjectMdl;
import com.sun.enterprise.management.StateManageable;


/**
 * Applicaton Manager manages all j2ee application for a server instance. 
 * It also acts as an listener for the deployment events. All the hot 
 * application deployment events are handled in this manager.
 *
 * @author  Mahesh Kannan
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
public class ApplicationManager extends AbstractManager
        implements ApplicationDeployEventListener {

    /** logger to log core messages */
    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    /** local string manager */
    private static StringManager localStrings = 
                        StringManager.getManager(ApplicationManager.class);

    private static final String RAR = "rar";

    /**
     * ApplicationManager is responsible for loading all applications in an 
     * instance. It creates a ApplicationLoader for each deployed application.
     * ApplicationLader is responsible for further loading EJBContainers for
     * each module in the Application.
     *
     * <p>
     * It is assumed that the next two params are set by the Startup Code.
     *
     * @param appsManager        The appsManager obtained through 
     *                           ConfigFactory.createAppsManager(ConfigCtx)
     * @param commonclassLoader  The top level class loader. For now this 
     *                           can be the MainThread's ClassLoader.
     */
    ApplicationManager(AppsManager appsManager, 
            ClassLoader connectorClassLoader) {

        super(connectorClassLoader, appsManager);

        AdminEventListenerRegistry.addApplicationDeployEventListener(this);
        
        /* Make sure the manager is alive to receive all start-up load events. */
        AppclientJWSSupportManager.getInstance();
    }

    /**
     * Returns an application loader for the given application id.
     *
     * @param    id    registration name of an application
     *
     * @return   an application loader for this manager
     */
    protected AbstractLoader getLoader(String id) {
        // get the appropriate loader
        PluggableFeatureFactory featureFactory =
            ApplicationServer.getServerContext().getPluggableFeatureFactory();
        ApplicationLoaderFactory appLoaderFactory =
            featureFactory.getApplicationLoaderFactory();
        ApplicationLoader appLoader =
            appLoaderFactory.createApplicationLoader(id,
                this.parentClassLoader,(AppsManager)this.configManager);
        _logger.log(Level.FINEST,"ApplicationLoader "+appLoader);
        return appLoader;
    }

    /**
     * Loads the given application if enabled.
     * This adds the application to the reload monitor list. 
     * This gets called when application is deployed.
     *
     * @param    appID    registration name of the application
     *
     * @return   true if application is loaded successfully
     */
    boolean applicationDeployed(String appID) {
        return applicationDeployed(false, appID, true);
    }

    /**
     * Loads the given application if enabled.
     * This adds the application to the reload monitor list. 
     * This gets called when application is deployed.
     *
     * @param    appID    registration name of the application
     * @param    jsr77    create jsr77 mBeans if it is true
     *
     * @return   true if application is loaded successfully
     */
    boolean applicationDeployed(boolean jsr77, String appID) {
        return applicationDeployed(jsr77, appID, true);
    }

    boolean applicationDeployed(boolean jsr77, String appID,
	    ConfigContext dynamicConfigContext)
    {
        return applicationDeployed(jsr77, appID, true,
	    dynamicConfigContext);
    }

    boolean applicationDeployed(boolean jsr77, String appID,
            ConfigContext dynamicConfigContext, int loadUnloadAction)
    {
        return applicationDeployed(jsr77, appID, true,
            dynamicConfigContext, loadUnloadAction);
    }

    /**
     * Loads the given application if enabled.
     *
     * @param    jsr77    create jsr77 mBeans if it is true
     * @param    appID    registration name of the application
     * @param    addToRM  if true, adds this application to reload
     *
     * @return   true if application is loaded successfully
     */
    boolean applicationDeployed(boolean jsr77, String appID, boolean addToRM) {
	return applicationDeployed(jsr77, appID, addToRM, null);
    }
    
    boolean applicationDeployed(boolean jsr77, String appID, boolean addToRM,
	    ConfigContext dynamicConfigContext)
    { 
        return applicationDeployed(jsr77, appID, addToRM, dynamicConfigContext, 
            Constants.LOAD_ALL);
    }

      
    // Depending on the value of loadUnloadAction. we will do different things
    // 1. LOAD_ALL is for loading regular application
    // 2. LOAD_RAR is for loading the rar part of the embedded rar
    // 3. LOAD_REST is for loading the rest part of the embedded rar
    boolean applicationDeployed(boolean jsr77, String appID, boolean addToRM,
            ConfigContext dynamicConfigContext, int loadUnloadAction)
    {

        boolean deployed = false;
        boolean loadJSR77 = jsr77 || loadJSR77(appID, DeployableObjectType.APP);
        AbstractLoader appLoader = null;
        try {
            if (this.configManager.isSystem(appID)) {
                    return true;
            }
        } catch (ConfigException confEx) {
             _logger.log(Level.WARNING,
                         "core.error_while_loading_app", confEx);
        }

        try {
            //set default value
            if (loadUnloadAction == Constants.LOAD_UNSET) {
                loadUnloadAction = Constants.LOAD_ALL;
            }

            boolean handlePreLoadAction = 
                (loadUnloadAction == Constants.LOAD_ALL ||
                 loadUnloadAction == Constants.LOAD_RAR); 

            boolean handlePostLoadAction = 
                (loadUnloadAction == Constants.LOAD_ALL ||
                 loadUnloadAction == Constants.LOAD_REST); 

            // adds the app to be monitored if ON
            if (handlePreLoadAction) {
                if (addToRM) {
                    addToReloadMonitor(appID);
                }
            }

            // Check to see if the app is already loaded.  If yes, do not 
            // reload. 
            if (id2loader.get(appID) != null) {
                    return true;
            }

            // create new loader for loading rars or loading non-embedded-rar 
            // application
            // and use the cached loader for the rest of the loading for 
            // embedded rar
            if (handlePreLoadAction) {
                appLoader = getLoader(appID);
                /**
                 *Add the apploader to the map now so that it can be cleaned 
                 *up later even if the load fails.
                 */
                if (loadUnloadAction == Constants.LOAD_ALL) {
                    id2loader.put(appID, appLoader);
                } else if (loadUnloadAction == Constants.LOAD_RAR) {
                    id2loader.put(appID + RAR, appLoader);
                }
            } else if (loadUnloadAction == Constants.LOAD_REST) {
                appLoader = (AbstractLoader) id2loader.remove(appID + RAR);
                if (appLoader != null) {
                    id2loader.put(appID, appLoader);
                } else {
                    return false;
                }
            }

	    appLoader.setConfigContext(dynamicConfigContext);
	    appLoader.setLoadUnloadAction(loadUnloadAction);

            // create jsr77 root Mbean for this application
            // only do this for second part of the loading
            if (handlePreLoadAction && loadJSR77) {
                try {
                    appLoader.createRootMBean();
                } catch (MBeanException mbe) {
                    _logger.log(Level.WARNING,"core.error_while_creating_jsr77_root_mbean",mbe);
                }
            }

            if (isEnabled(dynamicConfigContext, appID)) {

                _logger.log(Level.FINEST,
                    "[ApplicationManager] Application is enabled: " + appID);


                // only do this for first part of the loading
                if (handlePreLoadAction) {
                    // set jsr77 state to STARTING
                    try {
                        appLoader.setState(StateManageable.STARTING_STATE);
                    } catch (MBeanException mbe) {
                        _logger.log(Level.WARNING, 
                            "core.error_while_setting_jsr77_state",mbe);
                    }
                }

                boolean retSts = appLoader.load(jsr77);
                deployed = retSts;

                if (handlePostLoadAction && retSts) {

                    // set jsr77 state to RUNNING
                    try {
                        appLoader.setState(StateManageable.RUNNING_STATE);
                    } catch (MBeanException mbe) {
                        _logger.log(Level.WARNING, 
                        "core.error_while_setting_jsr77_state",mbe);
                    }

                					
                    // application loaded 
                    deployed = true;

                    SecurityRoleMapperFactory factory = SecurityRoleMapperFactoryMgr.getFactory();

                    if (factory==null) {
                        throw new IllegalArgumentException(localStrings.getString(
                        "enterprise.deployment.deployment.norolemapperfactorydefine",
                        "This application has no role mapper factory defined"));
                    }

                } else if (!retSts) {
                    if (handlePreLoadAction && loadJSR77) {
                        // delete leaf and root mBeans for this app
                        try {
                            appLoader.deleteLeafAndRootMBeans();
                        } catch (MBeanException mbe) {
                            _logger.log(Level.WARNING,
                            "core.error_while_deleting_jsr77_leaf_and_root_mbean",mbe);
                        }
                    } else if (!loadJSR77){
                        // set jsr77 state to FAILED
                        try {
                            appLoader.setState(StateManageable.FAILED_STATE);
                        } catch (MBeanException mbe) {
                            _logger.log(Level.WARNING, 
                            "core.error_while_setting_jsr77_state",mbe);
                        }
                    }
                    /*
                     *Release the app loader's resources so jar files can be
                     *closed.
                     */
                    try {
                        appLoader.done();
                    } catch (Throwable thr) {
                        String msg = localStrings.getString("core.application_not_loaded", appID);
                        _logger.log(Level.INFO, msg, thr);
                    }
                    _logger.log(Level.WARNING, 
                                "core.application_not_loaded", appID);
                }

            } else {
                _logger.log(Level.INFO, "core.application_disabled", appID);
                deployed = false;
            }
        } catch (ConfigException confEx) {
            _logger.log(Level.WARNING, "core.error_while_loading_app", confEx);
        } finally {
	    //To ensure that the config context is not reused...
	    if (appLoader != null) {
		appLoader.setConfigContext(null);
	    }
	}

        return deployed;
    }


    /**
     * Unloads the given application.
     * This removes the application from reload monitor list. 
     * This gets called when application is undeployed.
     *
     * @param    appID    registration name of the application
     * @return   true if application is unloaded successfully
     */
    boolean applicationUndeployed(String appID) {
        return applicationUndeployed(false, appID, true);
    }

    /**
     * Unloads the given application.
     * This removes the application from reload monitor list. 
     * This gets called when application is undeployed.
     *
     * @param    jsr77    deletes jsr77 mBeans if it is true
     * @param    appID    registration name of the application
     * @return   true if application is unloaded successfully
     */
    boolean applicationUndeployed(boolean jsr77, String appID) {
        return applicationUndeployed(jsr77, appID, true);
    }

    /**
     * Unloads the given application.
     *
     * @param    jsr77    delete jsr77 mBeans if it is true
     * @param    appID    registration name of the application
     * @param    clearRM  if true, removes this app from reload monitor
     *
     * @return   true if application is unloaded successfully
     */
    boolean applicationUndeployed(boolean jsr77, String appID, boolean clearRM) {
	    return applicationUndeployed(jsr77, appID,clearRM, false);
	}

	
    boolean applicationUndeployed(boolean jsr77, String appID, boolean clearRM, boolean cascade) {

            return applicationUndeployed(jsr77, appID,clearRM, cascade, Constants.UNLOAD_ALL);
        }

    // Depending on the value of loadUnloadAction. we will do different things
    // 1. UNLOAD_ALL is for unloading regular application
    // 2. UNLOAD_RAR is for unloading the rar part of the embedded rar
    // 3. UNLOAD_REST is for unloading the rest part of the embedded rar
    boolean applicationUndeployed(boolean jsr77, String appID, boolean clearRM, boolean cascade, int loadUnloadAction) {

        //set default value
        if (loadUnloadAction == Constants.LOAD_UNSET) {
            loadUnloadAction = Constants.UNLOAD_ALL;
        }

        boolean handlePreUnloadAction = 
            (loadUnloadAction == Constants.UNLOAD_ALL ||
             loadUnloadAction == Constants.UNLOAD_REST); 

        boolean handlePostUnloadAction = 
            (loadUnloadAction == Constants.UNLOAD_ALL ||
             loadUnloadAction == Constants.UNLOAD_RAR); 

        // removes this app from reload monitor
        if (handlePreUnloadAction && clearRM) {
            removeFromReloadMonitor(appID);
        }

        AbstractLoader appLoader = (AbstractLoader) id2loader.get(appID);

        if (handlePostUnloadAction) {
            appLoader = (AbstractLoader) id2loader.remove(appID);
        }

        // application is not in the registry
        if ((appLoader == null) || 
            (appLoader != null && appLoader.getApplication() == null)) {
            return true;
        }

        appLoader.setCascade(cascade);
        appLoader.setLoadUnloadAction(loadUnloadAction);
        Application app = appLoader.getApplication();

        if (handlePreUnloadAction) {
            // set jsr77 state to STOPPING
            try {
                appLoader.setState(StateManageable.STOPPING_STATE);
            } catch (MBeanException mbe) {
                _logger.log(Level.WARNING,
                    "core.error_while_setting_jsr77_state",mbe);
            }

            RoleMapper.removeRoleMapper(app.getRoleMapper().getName());

            if (jsr77) {
                //delete jsr77 mbean
                try {
                    appLoader.deleteLeafAndRootMBeans();
                } catch (MBeanException mbe) {
                    _logger.log(Level.WARNING,
                        "core.error_while_deleting_jsr77_leaf_and_root_mbeans",mbe);
                }
            } else {
                // set jsr77 state STOPPED
                try {
                    appLoader.setState(StateManageable.STOPPED_STATE);
                } catch (MBeanException mbe) {
                    _logger.log(Level.WARNING,
                       "core.error_while_setting_jsr77_state",mbe);
                }
            }
        
        }

        boolean undeployed = appLoader.unload(jsr77);

        if (loadUnloadAction == Constants.UNLOAD_REST) {
            return undeployed;
        }

        if (undeployed) {
            // since web modules are loaded separately, 
            // at this point we can only claim ejbs to be 
            // loaded successfully
            if (app.getEjbComponentCount() > 0) {
                _logger.log(Level.INFO, 
                        "core.application_unloaded_ejb", appID);
            }
        } else {
            _logger.log(Level.INFO, 
                        "core.application_not_unloaded", appID);
        }

        return undeployed;
    }


    private void holdRequest() { }

    private void holdRequest(String appID) { }

    // ---- START OF MonitorListener METHODS ----------------------------------

    /**
     * This is a callback from realod monitor. This is called when the 
     * thread detects a change in the $APP_ROOT/.reload file's time stamp. 
     * This method reloads the application. 
     *
     * <p> The time stamp is set when the callback is made in the entry.
     *
     * @param    entry    monitored entry with a change in time stamp
     * @return   if applicatoin was reloaded successfully
     */
    public synchronized boolean reload(MonitorableEntry entry) {

        String appName = entry.getId();
        boolean status = false;

        try {
            DeploymentRequest req = new DeploymentRequest(
                            this.configManager.getInstanceEnvironment(), 
                            DeployableObjectType.APP, 
                            DeploymentCommand.DEPLOY);

            // monitored file points to $APP_ROOT/.reload
            req.setFileSource(entry.getMonitoredFile().getParentFile());

            // application registration name
            req.setName(appName);

            // we are always trying a redeployment
            req.setForced(true);
			
			AutoDirReDeployer deployer = new AutoDirReDeployer(req);
			status = deployer.redeploy();

        } 
		catch (IASDeploymentException de) 
	{	
            _logger.log(Level.WARNING,"core.error_while_redeploying_app",de);
            return false;
        }
        return status;
    }
    // ---- END OF MonitorListener METHODS ------------------------------------


    // ---- START OF ApplicationDeployEventListener METHODS ------------------

    public synchronized void applicationDeployed(ApplicationDeployEvent event) 
            throws AdminEventListenerException {
        try {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                "[ApplicationManager] Handling event " + event.toString());
            }

            DeployEventListenerHelper.getDeployEventListenerHelper().synchronize(event);

            // refreshes the config context with the context from this event
            this.configManager.refreshConfigContext( event.getConfigContext() );

            // set jsr77 flag
            boolean jsr77 = false;
            String action = event.getAction();
            if ( (action.equals(BaseDeployEvent.DEPLOY)) ||
                 (action.equals(BaseDeployEvent.REDEPLOY)) ) {
                jsr77 = true;
            }

            String appName = event.getApplicationName();

            //If forceDeploy=true [ie a Redeploy] we need loadEmbeddedRARconfigs 
            //first and then deploy the application. After successful 
            //application Deployment, all embedded connector resources 
            //needs to be loaded
            ConnectorResourcesLoader connecorResourcesLoader  = null;
            if(event.getForceDeploy()) {
                //The undeploy event that had  been executed earlier
                //as part of the deploy --force=true action will remove the
                //application node from domain.xml.
                //Therefore refresh the ResourcesUtil singleton with
                //this event's config context so that it can 
                //find the application node while loading embedded RAR configs
                //and connector resources.
                ResourcesUtil.setEventConfigContext(event.getConfigContext());
                connecorResourcesLoader =  new ConnectorResourcesLoader();

                // do this before loading rar
                if (event.getLoadUnloadAction() == Constants.LOAD_RAR) {
                    connecorResourcesLoader.loadEmbeddedRarRAConfigs(appName);
                }
            }

            if (isEnabled(event.getConfigContext(), appName) && 
                !applicationDeployed(jsr77, appName, event.getConfigContext(), 
                 event.getLoadUnloadAction()) ) {
                    
                String msg = localStrings.getString(
                    "applicationmgr.application_deployed_failed", appName);
                registerException(event, msg);
            }
            
            // do this after rest of embedded rar is loaded
            if(event.getLoadUnloadAction() == Constants.LOAD_REST && 
                event.getForceDeploy()) {
                AbstractLoader appLoader = (AbstractLoader)id2loader.get(appName);
                if (appLoader != null) {
                    Application appDescriptor = appLoader.getApplication();
                    connecorResourcesLoader.loadEmbeddedRarResources(appName,appDescriptor);           
                }
            }
        } catch (ConfigException ce) {
            throw new AdminEventListenerException(ce.getMessage()); 
        } finally {
            ResourcesUtil.resetEventConfigContext();
        }
    }

    public synchronized void applicationUndeployed(
            ApplicationDeployEvent event) throws AdminEventListenerException  {

        // set jsr77 flag
        boolean jsr77 = false;
        String action = event.getAction();
        if ( (action.equals(BaseDeployEvent.UNDEPLOY)) ||
             (action.equals(BaseDeployEvent.REDEPLOY)) ) {
                jsr77 = true;
        }

        try {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                    "[ApplicationManager] Handling event " + event.toString());
            }

            // refreshes the config context with the context from this event
            this.configManager.refreshConfigContext(
                            event.getOldConfigContext());

            String appName = event.getApplicationName();
            //If forceDeploy = true, setcascade as true, so that RA undeployment 
            //removes all deployed connector connection pools, 
            //connector resources, RA configs
            if(event.getForceDeploy()) {
                event.setCascade(true);
            }

            boolean undeployed = applicationUndeployed(jsr77, appName, true, event.getCascade(), event.getLoadUnloadAction());

            if (!undeployed) {
                String msg = localStrings.getString(
                    "applicationmgr.application_undeployed_failed", appName);
                registerException(event, msg);
            }
        } catch(ConfigException ce) {
            throw new AdminEventListenerException(ce.getMessage()); 
        }
    }

    public synchronized void applicationRedeployed(
            ApplicationDeployEvent event) throws AdminEventListenerException {

        try {
            if (_logger.isLoggable(Level.FINEST)) {
               _logger.log(Level.FINEST,
                    "[ApplicationManager] Handling event " + event.toString());
            }

            // refreshes the config context with the context from this event
            this.configManager.refreshConfigContext( event.getConfigContext() );

            String appName = event.getApplicationName();
            boolean ok = applicationUndeployed(true, appName);
            if (ok) {
                ok = applicationDeployed(true, appName, event.getConfigContext());
            }

            if (!ok) {
                String msg = localStrings.getString(
                    "applicationmgr.application_redeployed_failed", appName);
                registerException(event, msg);
            }
        } catch(ConfigException ce) {
            throw new AdminEventListenerException(ce.getMessage()); 
        }
    }

    public synchronized void applicationEnabled(ApplicationDeployEvent event) 
            throws AdminEventListenerException {

        try {
            if (_logger.isLoggable(Level.FINEST)) {
               _logger.log(Level.FINEST,
                    "[ApplicationManager] Handling event " + event.toString());
            }

            //return if the app is not supposed to be enabled
            if (!isEnabled(event.getConfigContext(), event.getApplicationName())) {
                return;
            }

            // refreshes the config context with the context from this event
            this.configManager.refreshConfigContext( event.getConfigContext() );

            String appName = event.getApplicationName();
            boolean cascade = true;
            ConnectorResourcesLoader connecorResourcesLoader = 
                      new ConnectorResourcesLoader(); 
            ResourcesUtil.setEventConfigContext(event.getConfigContext());
 
            connecorResourcesLoader.loadEmbeddedRarRAConfigs(appName);
            boolean enabled = applicationDeployed(false, appName, event.getConfigContext());

            if (!enabled) {
                String msg = localStrings.getString(
                        "applicationmgr.application_enabled_failed", appName);
                registerException(event, msg);
            }
           
            AbstractLoader appLoader = 
                (AbstractLoader)id2loader.get(appName);
            Application appDescriptor = appLoader.getApplication();
            if (appDescriptor != null) {
                connecorResourcesLoader.loadEmbeddedRarResources(
                            appName,appDescriptor);
            } else {
                _logger.log(Level.FINE,
                "[ApplicationManager] Application descriptor is NULL.  Skip loading embedded rar resources...");
            }
        } catch(ConfigException ce) {
            throw new AdminEventListenerException(ce.getMessage()); 
        } catch(Throwable th) {
            AdminEventListenerException aele =
                           new AdminEventListenerException(th.getMessage());
            aele.initCause(th);
            throw aele;
        }finally {
            ResourcesUtil.resetEventConfigContext();
        }
    }

    public synchronized void applicationDisabled(ApplicationDeployEvent event) 
            throws AdminEventListenerException {

        try {
            if (_logger.isLoggable(Level.FINEST)) {
               _logger.log(Level.FINEST,
                    "[ApplicationManager] Handling event " + event.toString());
            }

            // refreshes the config context with the context from this event
            this.configManager.refreshConfigContext( event.getConfigContext() );

            String appName = event.getApplicationName();
            AbstractLoader appLoader = (AbstractLoader) id2loader.get(appName);
	    if(appLoader == null) {
                if (_logger.isLoggable(Level.FINEST)) {
                   _logger.log(Level.FINEST,
                        "[ApplicationManager] appLoader Null. Returning applicationDisabled");
                }
		return;

	    }
            event.setCascade(true); 
            boolean disabled = applicationUndeployed(false, appName,true,true);

            if (!disabled) {
                String msg = localStrings.getString(
                        "applicationmgr.application_disabled_failed", appName);
                registerException(event, msg);
            }
        } catch(ConfigException ce) {
            throw new AdminEventListenerException(ce.getMessage()); 
        }
    }

    
    /**
     * Invoked when an application reference is created from a
     * server instance (or cluster) to a particular application.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void applicationReferenceAdded(ApplicationDeployEvent event)
            throws AdminEventListenerException {
    }

    /**
     * Invoked when a reference is removed from a
     * server instance (or cluster) to a particular application.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void applicationReferenceRemoved(ApplicationDeployEvent event)
            throws AdminEventListenerException {
                
    }
    
    // ---- END OF ApplicationDeployEventListener METHODS --------------------
}
