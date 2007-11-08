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
 * @(#) ApplicationLifecycle.java
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

import java.util.Collection;
import java.util.Iterator;

import com.sun.ejb.Container;
import com.sun.ejb.ContainerFactory;
import com.sun.enterprise.Switch;
import com.sun.enterprise.log.Log;

import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.instance.InstanceFactory;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.EjbModulesManager;
import com.sun.enterprise.instance.ConnectorModulesManager;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import com.sun.enterprise.server.ServerContext;
import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.server.util.PreprocessorUtil; 

import com.sun.enterprise.resource.ResourceInstaller;
/**
 * This class implements the lifecycle methods used by the J2EE applications.  
 * It is responsible for loading and shutting down the applications.
 *
 * @author  Nazrul Islam
 * @since   JDK 1.4
 */
public class ApplicationLifecycle implements ServerLifecycle {

    /**
     * Server is initializing subsystems and setting up the runtime environment.
     * Prepare for the beginning of active use of the public methods of this
     * subsystem. This method is called before any of the public methods of 
     * this subsystem are utilized.  
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception IllegalStateException if this subsystem has already been
     *            started
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *            error that prevents this subsystem from being used
     */
    public void onInitialization(ServerContext sc) 
                        throws ServerLifecycleException {

        // set system environment variables here

        this._context = sc;

        try {
            // ---- J2EE APPLICATIONS ----------------------------------

            // instance environment of this server instance
            /*
            InstanceEnvironment iEnv = sc.getInstanceEnvironment();

            // shared class loader is used as the parent for the applications
            ClassLoader sharedCL     = sc.getSharedClassLoader();

            AppsManager appsManager = 
                InstanceFactory.createAppsManager(iEnv, false);
            */
            // manager for the j2ee applications
            //this._applicationMgr = new ApplicationManager(appsManager,sharedCL);
            this._applicationMgr = ManagerFactory.getApplicationManager();

            // ---- STAND ALONE EJB MODULES --------------------------------
            /*
            // config manager for stand alone ejb modules
            EjbModulesManager ejbModuleManager = 
                InstanceFactory.createEjbModuleManager(iEnv, false);

            // manager for stand alone ejb modules
            this._ejbMgr =
                new StandAloneEJBModulesManager(ejbModuleManager, sharedCL);
            */
            this._ejbMgr = ManagerFactory.getSAEJBModulesManager();
            // ---- STAND ALONE Web MODULES --------------------------------

            this._webMgr = ManagerFactory.getSAWebModulesManager();

            // ---- STAND ALONE RAR MODULES --------------------------------
            /*
            // config manager for stand alone connector modules
            ConnectorModulesManager connModuleManager = 
                InstanceFactory.createConnectorModuleManager(iEnv, false);
            
            // manager for stand alone connector modules
            this._connMgr = new StandAloneConnectorModulesManager(
                                                connModuleManager, sharedCL);
            */
            this._connMgr = ManagerFactory.getSAConnectorModulesManager();

            this._connectorResLoader = new ConnectorResourcesLoader();

	    // ---- STAND ALONE APPLICATION CLIENT MODULES ----------------------
	    // manager for stand alone application client modules

	    this._acMgr = ManagerFactory.getSAACModulesManager();

        } catch (ConfigException confEx) {
            _logger.log(Level.SEVERE, 
                "core.config_exception_while_app_loading", confEx);
        } catch (Throwable th) {
            _logger.log(Level.SEVERE, 
                "core.unexpected_error_occured_while_app_loading", th);
        }
    }

    /**
     * Server is starting up applications
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onStartup(ServerContext sc) throws ServerLifecycleException {

        try {

            // Loads the ResourceAdapter Configurations

            this._connectorResLoader.loadRAConfigs();

            // loads all deployed stand alone connector modules
            // stand alone connectors are loaded first since 
            // their class paths gets added to the the shared 
            // class loader. Any application acceessing these 
            // rar modules otherwise will not be able them.
            this._connMgr.load();

            // Loads connector resources belonging to standalone rars. 
            // Skip the resources belonging to embedded rars
            // Do not reload RAConfigs, they are loaded above
            this._connectorResLoader.
                    loadConnectorResources();

            // Now do recovery
            ResourceInstaller installer = 
                    Switch.getSwitch().getResourceInstaller();
            installer.recoverXAResources();

            // loads all deployed stand alone application client modules
            this._acMgr.load();

            // loads all deployed stand alone ejb modules
            this._ejbMgr.load();

            // loads all deployed stand alone web modules
            this._webMgr.load();

            // loads all deployed j2ee applications
            this._applicationMgr.load();

            // Loads connector resources belonging to embedded rars. 
            // Resources belonging to stand alone rars are already loaded
            //this._connectorResLoader.load(!standAloneRarsResources);


        } catch (Throwable th) {
            _logger.log(Level.SEVERE, 
                "core.unexpected_error_occured_while_app_loading", th);
        }
    }

    /**
     * Server has complted loading the applications and is ready to serve 
     * requests.
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onReady(ServerContext sc) throws ServerLifecycleException {
        try {
            ApplicationRegistry registry  = ApplicationRegistry.getInstance();
            Collection containers         = null;

            if (registry != null) {
                containers = registry.getAllEjbContainers();
            }

            // call onReady on all the ejb bean containers available 
            // in this server instance
            if (containers != null) {
                Iterator iter = containers.iterator();

                while (iter.hasNext()) {
                    Container container = (Container) iter.next();

                    if (container != null) {
                        container.onReady();
                    }
                }
            }

            _logger.log(Level.FINE, "core.application_onReady_complete");

        } catch (Throwable th) {
            _logger.log(Level.SEVERE, 
                "core.unexpected_error_occured_while_app_onready", th);
        }

        // Starts the dynamic reload monitor thread. This thread monitors the 
        // $APP_ROOT/.reload file of every applicatons and stand alone ejb
        // module. If the time stamp of these files are updated, this thread
        // sends a callback to the listeners for a dynamic reload.
        //
        // <p> Also starts the auto deploy monitor thread. This monitors the 
        // $INSTANCE/autodeploy directory for new archives to be deployed.

        // server configuration context
        ConfigContext configCtx = sc.getConfigContext();

        // applicatons node from server configuration
        Applications applicationsBean  = null;

        DasConfig dasConfig = null;

        // flag used to turn on/off the dynamic monitor thread
        boolean monitor                = false;

        try {
            //ROB: config changes
            //applicationsBean = 
               //(Applications)ConfigBeansFactory.getConfigBeanByXPath(configCtx,
                 //                       ServerXPathHelper.XPATH_APPLICATIONS);

            dasConfig = ServerBeansFactory.getDasConfigBean(configCtx);

            //monitor = applicationsBean.isDynamicReloadEnabled(); 
            monitor = dasConfig.isDynamicReloadEnabled();

        } catch (ConfigException ce) {
            _logger.log(Level.SEVERE, 
                "core.config_exception_while_dynamic_reloading", ce);
            monitor = false;
        }

        // starts the dynamic reload monitor thread only if it is on
        if (monitor) {
            // reload monitor is initialized in application loader
            ReloadMonitor reloadMonitor = ReloadMonitor.getInstance(2000l);
            reloadMonitor.start();
        }

        /* Nazrul: Auto Deploy will be included in a subsequent release

        // FIXME: get it from the applicationsBean
        //boolean autoDeploy = false;
        boolean autoDeploy = true;

        // start the auto deploy monitor is turned ON in server configuration
        if (autoDeploy) {
            // FIXME: get it from the applicationsBean
            long monitorIntv = 2000l;

            // monitors the auto deploy directory
            AutoDeployMonitor adm = AutoDeployMonitor.getInstance(monitorIntv);

            // instance environment for this server
            InstanceEnvironment env = this.context.getInstanceEnvironment();

            // auto deploy directory 
            File autoDeployDir         = new File(env.getAutoDeployDirPath());

            // handles auto deploy callbacks 
            AutoDeployer autoDeployer  = new AutoDeployer();

            // monitor entry for the auto deploy dir
            MonitorableEntry entry = 
                new MonitorableEntry(autoDeployDir, autoDeployer);

            adm.addMonitorableEntry(entry);

            // starts auto deploy monitor
            adm.start();
        }
        */
        
        ContainerFactory cf = Switch.getSwitch().getContainerFactory();
        try {
	        cf.restoreEJBTimers();
        } catch (Exception ex) {
	        _logger.log(Level.SEVERE, 
                "ApplicationLifeCycle.onReady():: exception when calling " +
                    "restoreEJBTimers()", ex);
        }
    }

    /**
     * Server is shutting down applications
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onShutdown() throws ServerLifecycleException {

        if (this._applicationMgr == null) {
            // not initialized
            return;
        }
        try {
            _logger.log(Level.INFO, "core.shutting_down_applications");

            // first shuts down the dynamic reload monitor if running
            this._applicationMgr.shutdown();

            // application registry for this server instance
            ApplicationRegistry registry  = ApplicationRegistry.getInstance();
            Collection containers         = null;

            if (registry != null) {
                containers = registry.getAllEjbContainers();
            }

            // shuts down all the ejb bean containers available 
            // in this server instance
            if (containers != null) {
                Iterator iter = containers.iterator();

                while (iter.hasNext()) {
                    Container container = (Container) iter.next();

                    if (container != null) {
                        container.onShutdown();
                    }
                }
            }
            
            // After the applicationmanager shutdown, call stop method
            // of all J2EE Connector 1.5 specification compliant, inbound and
            // outbound resource adapters.
            // This call to stop() of all deployed candidate connector modules
            // is implemented in a concurrent fashion and this call times-out
            // based on the shutdown-timeout-in-seconds attribute in domain.xml
            _logger.log(Level.INFO, "core.shutting_down_resource_adapters");
            this._connectorResLoader.stopActiveResourceAdapters();
            _logger.log(Level.INFO, "core.ra_shutdown_complete");

            _logger.log(Level.INFO, "core.application_shutdown_complete");

        } catch (Throwable th) {
            _logger.log(Level.SEVERE, 
                "core.unexpected_error_occured_while_app_shutdown", th);
        }
    }

    /**
     * Server is terminating the subsystems and the runtime environment.
     * Gracefully terminate the active use of the public methods of this
     * subsystem.  This method should be the last one called on a given
     * instance of this subsystem.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onTermination() throws ServerLifecycleException {

        try {
            // application registry for this server instance
            ApplicationRegistry registry  = ApplicationRegistry.getInstance();
            Collection containers         = null;

            if (registry != null) {
                containers = registry.getAllEjbContainers();
            }

            // final shut down call to all ejb containers
            if (containers != null) {
                Iterator iter = containers.iterator();

                while (iter.hasNext()) {
                    Container container = (Container) iter.next();

                    if (container != null) {
                        container.onTermination();
                    }
                }
            }
        } catch (Throwable th) {
            _logger.log(Level.SEVERE, 
                "core.unexpected_error_occured_while_app_terminate", th);
        }
    }

    // ---- VARIABLE(S) - PRIVATE ------------------------------------------
    protected ServerContext _context                      = null;
    protected ApplicationManager _applicationMgr          = null;
    protected StandAloneEJBModulesManager _ejbMgr         = null;
    protected StandAloneConnectorModulesManager _connMgr  = null; 
    protected ConnectorResourcesLoader _connectorResLoader= null;
    protected StandAloneAppClientModulesManager _acMgr    = null;
    protected DummyWebModuleManager _webMgr    = null;

    protected static Logger _logger=LogDomains.getLogger(LogDomains.CORE_LOGGER);
}
