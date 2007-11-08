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
 * @(#) AbstractManager.java
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

import com.sun.appserv.management.util.misc.RunnableBase;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext; 
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ApplicationHelper; 
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.management.StateManageable;
import com.sun.enterprise.server.ondemand.OnDemandServer;
import com.sun.logging.LogDomains;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Base manager class for J2ee applications. A  manager manages all
 * j2ee application and stand alone modules for a server instance. 
 * It also acts as a listener for the deployment events.
 *
 * <p> This manager object loads all the deployed applications 
 * or stand alone modules.
 *
 * <xmp>
 *   Code Example:
 *     AbstractManager mgr = new SubClassOfAbstractManager(loader, baseMgr);
 *
 *     // loads the deployed apps returned by the base mgr
 *     mgr.load(); 
 * </xmp>
 *
 * @author  Mahesh Kannan
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
public abstract class AbstractManager implements MonitorListener { 

    /** parent class loader for the j2ee applications */
    protected ClassLoader parentClassLoader;

    /** application registry */
    protected ApplicationRegistry registry;

    /** encapsulates all application related info */
    protected BaseManager configManager;

    /** application id vs application loaders */
    protected Hashtable id2loader;

    /** monitors the time stamp of the RELOAD_FILE */
    protected ReloadMonitor reloadMonitor = null;

    /** Indicates whether system apps of this manager are already loaded */
    protected boolean systemAppsLoaded = false;
	
    /** logger to log core messages */
    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    /**
     * Inintializes the variables and constructs the application registry.
     *
     * @param    parentClassLoader    parent class loader for this manager
     * @param    configMgr            encapsulates all application related info
     */
    AbstractManager(ClassLoader parentClassLoader, BaseManager configMgr)
    {

        this.id2loader          = new Hashtable();
        this.parentClassLoader  = parentClassLoader;
        this.configManager      = configMgr;

        // initializes the application registry
        this.registry           = ApplicationRegistry.getInstance();

        // initializes the reload monitor if dynamic reload is enabled
        boolean monitor         = this.configManager.isDynamicReloadEnabled();
        if (monitor) {
            this.reloadMonitor  = ReloadMonitor.getInstance(
                            this.configManager.getReloadPollIntervalInMillis());
        }
    }

    /**
     * Loads the deployed and enabled applications and/or stand alone modules.
     * The list and the type (application vs stand alone module) is determined
     * from the associated config manager object.
     */
    void load() {
        
        // Set the thread context classloader 
        final ClassLoader connCL = this.parentClassLoader;
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    Thread.currentThread().setContextClassLoader(connCL);
                    return null;
                }
            }
        );
        
        //loadSystem();

        // deployed application list from config
        List appList = null;
        try {
            appList = this.configManager.listIds();
        } catch (ConfigException confEx) {
            _logger.log(Level.WARNING,
                        "core.error_while_getting_deployed_applist", confEx);
            return;
        }

        // set jsr77 to true
        boolean jsr77 = true;

        // loads all the deployed applications
        for (int i=0; i<appList.size(); i++) {

            // registration name of this app
            String id = (String) appList.get(i);

            try {
	    	if (this.configManager.isSystem(id)) {
			continue;
	    	}
            } catch (ConfigException confEx) {
                _logger.log(Level.WARNING,
                            "core.error_while_loading_app", confEx);
	    }

            try {
                // loader for this app
                AbstractLoader loader = getLoader(id);

                // create jsr77 root mBean
                loader.createRootMBean();
            
                // if app is enabled
                ConfigContext ctx = configManager.getConfigContext();
                if (isEnabled(ctx, id)) {

                    // set jsr77 root mBean state to STARTING
                    try {
                        loader.setState(StateManageable.STARTING_STATE);
                    } catch (MBeanException mbe) {
                        _logger.log(Level.WARNING,
                            "core.error_while_setting_jsr77_state",mbe);
                    }

                    /**
                     *Add the loader to the map so it can be cleaned up later
                     *even if the load fails.
                     */
                    id2loader.put(id, loader);
                    
                    // if loader was able to load successfully
                    if (loader.load(jsr77) == true) {
                        // set jsr77 root mBean state to RUNNING
                        try {
                            loader.setState(StateManageable.RUNNING_STATE);
                        } catch (MBeanException mbe) {
                            _logger.log(Level.WARNING,
                            "core.error_while_setting_jsr77_state",mbe);
                        }

                    } else {
                        // set jsr77 root mBean state to FAILED
                        try {
                            loader.setState(StateManageable.FAILED_STATE);
                        } catch (MBeanException mbe) {
                            _logger.log(Level.WARNING,
                            "core.error_while_setting_jsr77_state",mbe);
                        }

                        _logger.log(Level.WARNING, 
                                    "core.application_not_loaded", id);
                    }
                    // adds this app to be monitored if ON
                    addToReloadMonitor(id);
                } else if (! isEnabled(ctx, id)) {
                    // if app is not enabled
                    // set jsr77 root mBean state to STOPPED
                    try {
                        loader.setState(StateManageable.STOPPED_STATE);
                        loader.createLeafMBeans();
                    } catch (MBeanException mbe) {
                        _logger.log(Level.WARNING,
                            "core.error_while_setting_jsr77_state",mbe);
                    }

                }
            } catch (ConfigException confEx) {
                _logger.log(Level.WARNING,
                            "core.error_while_loading_app", confEx);
            } catch (Throwable t) {  
                _logger.log(Level.WARNING,
                    "core.unexpected_error_occured_while_loading_app", t);
            }
        }
    }
    /**
     * Loads the deployed and enabled system applications and/or stand alone modules.
     * The list and the type (application vs stand alone module) is determined
     * from the associated config manager object.
     */
    void loadSystem() {
        
        // set jsr77 to true
        boolean jsr77 = true;

        if(systemAppsLoaded)
            return;

        // deployed application list from config
        List appList = null;
        try {
            appList = this.configManager.listIds();
        } catch (ConfigException confEx) {
            _logger.log(Level.WARNING,
                        "core.error_while_getting_deployed_applist", confEx);
            return;
        }

        // loads all the deployed applications
        ArrayList<SystemAppStarter> systemAppStarters = new ArrayList<SystemAppStarter>();
        
        long systemAppLoadStartTime = System.currentTimeMillis();
        boolean isSystemAppLoadLoggingOn = _logger.isLoggable(Level.FINE);
        for (int i=0; i<appList.size(); i++) {

            // registration name of this app
            String id = (String) appList.get(i);

            /**
             * The system app controlled by on-demand framework should not be
             * loaded from this path. All appserver system apps are managed by
             * On-demand system app loader. Any system app inserted by addons 
             * will be loaded in this path.
             */
            if (OnDemandServer.isOnDemandOff() || 
               !OnDemandServer.getSystemAppLoader().isOnDemandSystemApp(id)) {
                /*
                 *For each system app of this manager's type create a starter
                 *object and submit it for concurrent execution.  Keep
                 *track of all such objects to wait for their completion and
                 *to query their success.
                 */
                try {
                    if (configManager.isSystem(id)) {
                        SystemAppStarter starter = new SystemAppStarter(id, jsr77);
                        systemAppStarters.add(starter); //loadOneSystemApp(id, jsr77);
                        starter.submit();
                        if (isSystemAppLoadLoggingOn) {
                            _logger.fine(getClass().getName() + " submitted system app load request for " + id);
                        }
                    }
                } catch (ConfigException ce) {
                    // Ignore this - it would come from the isSystem invocation.
                }
            } 
        }
        
        /*
         * Wait for the system apps to complete their start-up.  Errors will be
         * thrown, and therefore logged, from the thread doing the work.  Wait for
         * all to complete before aborting the startup if any failed.
         */
        boolean systemAppsStartedOK = true;
        for (SystemAppStarter starter : systemAppStarters) {
            if (isSystemAppLoadLoggingOn) {
                _logger.fine("About to wait for completion of load request for " + starter.getID());
            }
            boolean ok = starter.waitDone() == null; 
            systemAppsStartedOK &= ok;
            if (isSystemAppLoadLoggingOn) {
                _logger.fine("Completion received for " + starter.getID() + " reports " + (ok ? "success" : "failure"));
            }
        }
        
        if ( ! systemAppsStartedOK) {
            throw new RuntimeException(
                    _logger.getResourceBundle().getString("core.unexpected_error_occured_while_loading_app"));
        } else if (isSystemAppLoadLoggingOn) {
            _logger.fine(getClass().getName() + " has started its system apps successfully after " + 
                    (System.currentTimeMillis() - systemAppLoadStartTime) + " ms");
        }
        systemAppsLoaded = true;
    }


    public void loadOneSystemApp(String id, boolean jsr77) {
     // Set the thread context classloader 
        final ClassLoader connCL = this.parentClassLoader;
        final ClassLoader cl = (ClassLoader) 
            java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    ClassLoader cloader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(connCL);
                    return cloader;
                }
            }
        );

        try {
            // if app is enabled
            ConfigContext ctx = configManager.getConfigContext();
            if (ResourcesUtil.createInstance().belongToSystemRar(id) ||
                    (this.configManager.isSystem(id) 
                     && isEnabled(ctx, id))) {

                // loader for this app
                AbstractLoader loader = getLoader(id);

                // create jsr77 root mBean
                loader.createRootMBean();
                
                /*
                 *Add the loader to the map so it can be cleaned up later 
                 *even if the load fails.
                 */
                id2loader.put(id, loader);
                
                // if loader was able to load successfully
                if ( ! loader.load(jsr77) ) {
                    _logger.log(Level.WARNING,
                                "core.application_not_loaded", id);
                }
            }

        } catch (ConfigException confEx) {
            _logger.log(Level.SEVERE,
                        "core.error_while_loading_system_app", confEx);
        } catch (Throwable t) {
            _logger.log(Level.SEVERE,
               "core.unexpected_error_occured_while_loading_system_app", t);
        }

        if (cl != connCL) { 
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        Thread.currentThread().setContextClassLoader(cl);
                        return null;
                    }
               }
            );
        }

    }


    /**
     * Shutsdown the reload monitor. The bean container shutdown
     * activity is handled from the ApplicationLifeCycle module.
     */
    void shutdown() { 
        
        try {
            // shuts down the dynamic reload monitor
            if (this.reloadMonitor != null) {
                this.reloadMonitor.stop();
            }
        } catch (Throwable t) {  
            _logger.log(Level.WARNING,
                "core.unexpected_error_occured_while_app_shutdown", t);
        }
    }

    /**
     * Adds the given application or stand alone module to reload monitor.
     *
     * @param    id    registration name of the application
     *
     * @throws  ConfigException  if an error while parsing server configuration
     */
    protected void addToReloadMonitor(String id) throws ConfigException {

        // adds the reload file to monitor the time stamp 
        if (this.reloadMonitor != null && !(this.configManager.isSystem(id))) {
            String mPath = this.configManager.getLocation(id) 
                         + File.separator + ReloadMonitor.RELOAD_FILE;
            MonitorableEntry entry = 
                new MonitorableEntry(id, new File(mPath), this);
            this.reloadMonitor.addMonitorableEntry(entry);
        }
    }

    /**
     * Removes the given application or stand alone module from reload monitor.
     *
     * @param    id    registration name of the application
     */
    protected void removeFromReloadMonitor(String id) {
		// 4925582  bnevins 10-1-03
		// If the user makes some tiny error and does a touch .reload -- they couldn't fix
		// it and try again.  Now -- we don't remove it from the monitor list.  So they can try again and
		// a regular first-time deployment will be attempted.  They can keep trying until a server restart.
		// After a restart, they'll have to deploy from scratch again.
		
        /*
		 if (this.reloadMonitor != null) {
            //this.reloadMonitor.removeMonitoredEntry(id);
        }
		 */ 
    }

    // ---- START OF MonitorListener METHOD -----------------------------------

    /**
     * No-op. Subclasses should implement this methods if they want to handle 
     * the dynamic reload callbacks.
     *
     * @param    entry    monitored entry with a change in time stamp
     *
     * @return   no-op; returns false
     */
    public boolean reload(MonitorableEntry entry) { 
        return false;
    }

    /**
     * No-op. Subclasses should implement this methods if they want to handle 
     * the auto deploy callbacks.
     *
     * @param    entry    entry thats being monitored
     * @param    archive  newly detected archive under the auto deploy directory
     *
     * @return   no-op; returns false
     */
    public boolean deploy(MonitorableEntry entry, File archive) {
        return false;
    }

    // ---- END OF MonitorListener METHOD -------------------------------------

    /**
     * Returns the loader for this type of manager and the given id.
     *
     * @param    id    registration name of an application or stand alone module
     *
     * @return   the correct loader for this manager 
     */
    protected abstract AbstractLoader getLoader(String id);

    /**
     * Returns the parent class loader (of ejb modules) used by this manager.
     *
     * @return    the parent class loader used by this manager
     */
    ClassLoader getParentClassLoader() {
        return this.parentClassLoader;
    }

    /**
     * This method is used to determine if an app or a module is already
     * registered.
     * @return true if the app is registered
     **/
    public boolean isRegistered(String id) {
        return this.configManager.isRegistered(id);
    }

    /**
     * @return Whether the JSR77 MBean for a specific app exists
     */
    protected boolean loadJSR77(String appName, DeployableObjectType type) {
	ObjectName jsr77mBeanObjectName = null;
	try {
            String domainName = null;
            AdminService adminService = AdminService.getAdminService();
            if (adminService != null) {
                domainName = adminService.getAdminContext().getDomainName();
            } else {
                return false;
            }

            //We do not load JSR77 MBeans for web modules
            //web modules are loaded by the Tomcat container
            String j2eeType = null;
            if (type.isAPP()) {
                j2eeType = "J2EEApplication";
            } else if (type.isEJB()) {
                j2eeType = "EJBModule";
            } else if (type.isCONN()) {
                j2eeType = "ResourceAdapterModule";
            } else if (type.isCAR()) {
                j2eeType = "AppClientModule";
            } else if (type.isWEB()) {
                return false; 
            }

            String instanceName = 
                ApplicationServer.getServerContext().getInstanceName();

            //form query
            StringBuffer sb = new StringBuffer("");
            sb.append(domainName + ":");
            sb.append("j2eeType=" + j2eeType + ",");
            sb.append("name=" + appName + ",");
            if (!type.isAPP()) {
                sb.append("J2EEApplication=null" + ",");
            }
            sb.append("J2EEServer=" + instanceName + "," + "*");

            // perform query
            MBeanServer mbs = adminService.getAdminContext().getMBeanServer();

            ObjectName objNamePattern = new ObjectName(sb.toString());
            java.util.Set s = mbs.queryNames(objNamePattern, null);
            if (s != null && s.size()>0) {
                ObjectName [] objNameArr = 
                    (ObjectName[]) s.toArray( new ObjectName[s.size()]);
                if (objNameArr.length>0) {
                    jsr77mBeanObjectName = objNameArr[0];
                }
            }
        } catch (Exception ex) {
            //Ignore on purpose.
        }

	return jsr77mBeanObjectName == null;
    }

    /**
     * Whether or not a component (either an application or a module) should be
     * enabled is defined by the "enable" attribute on both the 
     * application/module element and the application-ref element.
     * 
     * @param config The dynamic ConfigContext
     * @param moduleName The name of the component (application or module)
     * @return boolean 
     */
    protected boolean isEnabled (ConfigContext config, String moduleName) {
        try {
            ConfigBean app = ApplicationHelper.findApplication(config, moduleName);

            Server server = ServerBeansFactory.getServerBean(config);
            ApplicationRef appRef = server.getApplicationRefByRef(moduleName);
            return ((app != null && app.isEnabled()) && 
                        (appRef != null && appRef.isEnabled()));
        } catch (ConfigException e) {
            AdminEventListenerException ex = new AdminEventListenerException();
            ex.initCause(e);
            _logger.log(Level.FINE, "Error in finding " + moduleName, e);

            //If there is anything wrong, do not enable the module
            return false;
        }
    }

    protected void registerException(BaseDeployEvent event, String msg) {
        AdminEventResult result = AdminEventResult.getAdminEventResult(event);
        result.setResultCode(AdminEventResult.SUCCESS);
        result.addException(event.getEffectiveDestination(), 
                            new AdminEventListenerException(msg));
    }
    
    /**
     *Represents the start-up of a system app, typically run concurrently with
     *other system-app start up work.
     */
    private class SystemAppStarter extends RunnableBase {
        
        private final String id;
        private final boolean jsr77;
        
        public SystemAppStarter(String id, boolean jsr77) {
            this.id = id;
            this.jsr77 = jsr77;
        }

        protected void doRun() throws Exception {
            loadOneSystemApp(id, jsr77);
        }
        
        public String getID() {
            return id;
        }
    }
}
