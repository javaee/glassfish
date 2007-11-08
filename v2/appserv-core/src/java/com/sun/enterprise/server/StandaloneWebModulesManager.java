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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.admin.server.core.AdminService;

import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentCommand;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.deployment.autodeploy.AutoDirReDeployer;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import com.sun.enterprise.instance.WebModulesManager;
import com.sun.enterprise.instance.InstanceFactory;

/**
 * Implements the reload callbacks for standalone web modules.
 *
 * When dynamic reloading is enabled, this object adds the list of enabled
 * standalone web modules to the reload monitor thread, so that web module
 * reload via 'touch .reload' works.
 *
 * This class lives in the com.sun.enterprise.server package and not the
 * com.sun.enterprise.web package because of the scope of the interface/methods
 * in - ReloadMonitor, MonitorableEntry and MonitorListener.
 */
public final class StandaloneWebModulesManager implements MonitorListener {
    

    ReloadMonitor reloadMonitor;

    
    // ------------------------------------------------------------ Constructor
    
    /**
     * Standard constructor.
     *
     * @param id           The web container object identifier
     * @param modulesRoot  The top level directory under which all standalone
     *                     modules are deployed at.
     * @param pollInterval The interval at which dynamic reloading is performed
     */
    public StandaloneWebModulesManager(String id, String modulesRoot,
                                       long pollInterval) {
        _id = id;
        _modulesRoot = modulesRoot;
        start(pollInterval);
    }
    
    
    // ----------------------------------------------------- Instance Variables
    
    /**
     * The id of this object (this is the same as that of the id of the
     * associated web container object).
     */
    private String _id = null;
    
    /**
     * Absolute path for location where all the deployed
     * standalone modules are stored for this Server Instance.
     */
    private String _modulesRoot = null;
    
    /**
     * List of web module ids registered with the reload monitor thread.
     */
    private ArrayList _reloadables = new ArrayList();
    
    
    // --------------------------------------------------------- Public Methods
    
    /**
     * Enable dynamic reloading (via the .reload file) for all the
     * standalone web-modules that are marked as enabled
     *
     * This method is invoked be WebContainer.start() only when
     * dynamic reloading has been enabled in server.xml.
     */
    public void start(long pollInterval) {
       
        reloadMonitor = ReloadMonitor.getInstance(pollInterval * 1000);
    }
    
    /**
     * Remove the modules (that were previously registered by start()) from
     * the reload monitor thread.
     *
     * This method is invoked be WebContainer.stop() only when
     * dynamic reloading has been enabled in server.xml.
     */
    public void stop() {
        ReloadMonitor reloadMonitor = ReloadMonitor.getInstance(1);
        for (int i = 0; i < _reloadables.size(); i++) {
            String id = (String) _reloadables.get(i);
            reloadMonitor.removeMonitoredEntry(id);
        }
        _reloadables.clear();
        _reloadables = null;
    }
    
    // ------------------------------------------------ MonitorListener Methods
    
    /**
     * Callback from the reload monitor thread for a web module.
     *
     * This is done when the user updates the $MODULE_ROOT/$MODULE/.reload
     * file indicating the server runtime for a dynamic reload.
     *
     * @param    entry    entry thats being monitored
     *
     * @return   true if application was reloaded successfully
     */
    public boolean reload(MonitorableEntry entry) {
        // The actual reload is done by the NSAPI reconfiguration logic which
        // the reload monitor thread invokes (at the end), so simply return
        // true here.
        
        InstanceEnvironment ienv = ApplicationServer.getServerContext().getInstanceEnvironment();
        
        //4926513 work-around --- use displayname
        // for "foo", the id will be "foo[0]"
        //String moduleName = entry.getId();
        String moduleName = entry.getDisplayName();
        boolean status = false;
        
        try {
            DeploymentRequest req = new DeploymentRequest(ienv, DeployableObjectType.WEB, DeploymentCommand.DEPLOY);
            
            // monitored file points to $APP_ROOT/.reload
            req.setFileSource(entry.getMonitoredFile().getParentFile());
            
            // application registration name
            req.setName(moduleName);
            
            // we are always trying a redeployment
            req.setForced(true);
                        
            AutoDirReDeployer deployer = new AutoDirReDeployer(req);
            status = deployer.redeploy();
            
        } catch (IASDeploymentException de) {
            _logger.log(Level.WARNING,"core.error_in_reload_war_module",de);
            return false;
        }
        return status;
    }
    
    /**
     * Callback from the auto deploy monitor when a new archive is detected.
     *
     * @param    entry    entry thats being monitored
     * @param    archive  newly detected archive under the auto deploy directory
     *
     * @return   true if archive was deployed successfully
     */
    public boolean deploy(MonitorableEntry entry, File archive) {
        // auto-deploy has not been implemented in S1AS7
        return false;
    }

    /**
     * Adds the given WebModule to the list of monitorable entries for 
     * dynamic reloading.
     *
     * @param wm The WebModule to add to the list of monitorable entries for
     * dynamic reloading
     */
    public void addWebModule(WebModule wm) {
        if (wm != null && isEnabled(wm.getConfigContext(), wm.getName()))  {
            String name = wm.getName();
            String id = name + "[" + _id + "]";
            String fileName = getReloadFilename(wm);
            MonitorableEntry entry = new MonitorableEntry(id, name,
                                                          new File(fileName),
                                                          this);
            _reloadables.add(id);
            reloadMonitor.addMonitorableEntry(entry);
        }
    }

    /**
     * Whether or not a component (either an application or a module) should be
     * enabled is defined by the "enable" attribute on both the
     * application/module element and the application-ref element.
     *
     * @param config The dynamic ConfigContext
     * @param moduleName The name of the component
     * @return boolean
     */
    protected boolean isEnabled (ConfigContext config, String moduleName) {
        try {
            if (config == null) {
                config = AdminService.getAdminService().getAdminContext().getAdminConfigContext();
            }
            ConfigBean app = ApplicationHelper.findApplication(config,
                moduleName);
            Server server = ServerBeansFactory.getServerBean(config);
            ApplicationRef appRef = server.getApplicationRefByRef(moduleName);

            return ((app != null && app.isEnabled()) &&
                        (appRef != null && appRef.isEnabled()));
        } catch (ConfigException e) {
            _logger.log(Level.FINE, "Error in finding " + moduleName, e);

            //If there is anything wrong, do not enable the module
            return false;
        }
    }


    /**
     * Adds the given WebModule instances to the list of monitorable entries
     * for dynamic reloading.
     * 
     * @param modules The array of WebModule instances to add to the list of
     * monitorable entries for dynamic reloading
     */
    public void addWebModules(WebModule[] modules) {
        if (modules != null && modules.length > 0) {
            for (int i = 0; i < modules.length; i++) {
                addWebModule(modules[i]);
            }
        }
    }
       
    /**
     * Returns the absolute pathname to the .reload file in the
     * specified web module's directory.
     */
    private String getReloadFilename(WebModule wm) {
        String path = wm.getLocation();
        File dir = new File(path);
        if (!dir.isAbsolute())
            path = _modulesRoot + "/" + path;
        return  path + "/" + ReloadMonitor.RELOAD_FILE;
    }
    
    /** logger to log core messages */
    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    
    /** local string manager */
    private static StringManager localStrings =
    StringManager.getManager(StandAloneEJBModulesManager.class);
    
}
