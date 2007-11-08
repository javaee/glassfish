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
import com.sun.enterprise.admin.event.DeployEventListenerHelper;
import com.sun.enterprise.admin.event.ModuleDeployEvent; 
import com.sun.enterprise.admin.event.ModuleDeployEventListener;

import com.sun.enterprise.admin.common.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext; 
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ApplicationHelper; 
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.WebModule; 
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.instance.WebModulesManager;
import com.sun.enterprise.security.acl.RoleMapper;
import com.sun.enterprise.Switch;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.web.VirtualServer;
import com.sun.enterprise.web.WebContainer;
import com.sun.enterprise.web.WebModuleConfig;
import com.sun.logging.LogDomains;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Deployer;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardHost;

/**
 * A ModuleDeployEventListener for dynamic deployment support
 * of web module.
 *
 * @author  Amy Roh
 */
 
public class WebModuleDeployEventListener extends DummyWebModuleManager 
                                          implements ModuleDeployEventListener { 


    /** logger to log core messages */
    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    /** local string manager */
    private static StringManager localStrings = 
                        StringManager.getManager(WebModuleDeployEventListener.class);
                
    private Hashtable cachedWebModules = new Hashtable();


    /**
     * Standard constructor.
     */
    public WebModuleDeployEventListener(WebModulesManager manager, ClassLoader loader) {
        super(manager, loader);
        AdminEventListenerRegistry.addModuleDeployEventListener(this);
    }
    
    
    // ----------------------------------------------------- Instance Variables


    /**
     * The WebContainer instance.
     */
    protected WebContainer webContainer = null;
    
    
    // --------------------------------------------------------- Private Methods
 
 
    /**
     * Creates and returns an object that contains information about
     * the web module's configuration such as the information specified
     * in server.xml, the deployment descriptor objects etc.
     *
     * @return null if an error occured while reading/parsing the 
     *              deployment descriptors.
     */
    private WebModuleConfig loadWebModuleConfig(WebModule wm, 
        ConfigContext config) {

        WebModuleConfig wmInfo = new WebModuleConfig();
        wmInfo.setBean(wm);
        String wmID = wm.getName();
        String location = wm.getLocation();
        try {
	    Application app = getWebModulesManager().getDescriptor(wmID, 
                location);
            WebBundleDescriptor wbd = (WebBundleDescriptor) app.getStandaloneBundleDescriptor();                
            wmInfo.setDescriptor(wbd);
            String vs = ServerBeansFactory.getVirtualServersByAppName(
                    config, wmID);
            wmInfo.setVirtualServers(vs);
        } catch (ConfigException ce) {
            wmInfo = null;
        }
        return wmInfo;
        
    } 
 
    private void moduleDeployed(ConfigContext config, String moduleName) 
            throws AdminEventListenerException {

        WebModule webModule = getWebModuleAndUpdateCache(
            config, moduleName, true); 

        //Do not deploy if enable is false
        if (!isEnabled(config, moduleName)) {
            return;
        }
        String location = webModule.getLocation();   
        // If module root is relative then prefix it with the 
        // location of where all the standalone modules for 
        // this server instance are deployed
        File moduleBase = new File(location);
        String modulesRoot = getWebContainer().getModulesRoot();
        if (!moduleBase.isAbsolute()) {
            location = modulesRoot+File.separator+location;
            webModule.setLocation(location);
        }
        WebModuleConfig wmInfo = loadWebModuleConfig(webModule, config);
        List<Throwable> throwables = 
            getWebContainer().loadWebModule(wmInfo, "null");
        if (throwables != null && !throwables.isEmpty()) {
            //we don't have the ability to return all errors.  so return the
            //first one, enough to signal the problem.
            String msg = throwables.get(0).getMessage();
            AdminEventListenerException ex = 
                new AdminEventListenerException(msg);
            ex.initCause(throwables.get(0)); 
            throw ex;
        }
    }
     
         
    private void moduleUndeployed(ConfigContext config, String moduleName) 
            throws AdminEventListenerException {

        WebModule webModule = getWebModuleAndUpdateCache(
            config, moduleName, false); 

        String appName = null;
        WebBundleDescriptor wbd;
        try {
	    Application app = getWebModulesManager().getDescriptor(
                webModule.getName(), webModule.getLocation());
            RoleMapper.removeRoleMapper(app.getRoleMapper().getName());
            wbd = (WebBundleDescriptor) app.getStandaloneBundleDescriptor();
            appName = app.getRegistrationName();
        } catch (ConfigException ce) {
            throw new AdminEventListenerException(ce);
        }            
        String contextRoot = webModule.getContextRoot();       
        String virtualServers = null;
        try {
            virtualServers = ServerBeansFactory
                                .getVirtualServersByAppName(config,moduleName);
        } catch(ConfigException ce) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, 
                "Exception getting virtual servers by app name " 
                    + moduleName, ce);
            }
        }
        try {
            getWebContainer().unloadWebModule(contextRoot, appName,
                                         virtualServers, wbd);
        } finally {
            try {
              Switch.getSwitch().getNamingManager().unbindObjects(wbd);
            } catch (javax.naming.NamingException nameEx) {
                _logger.log(Level.FINEST, "[WebModuleDeployEventListener] "
                + " Exception during namingManager.unbindObject",
                nameEx);
            }
        } 
    }
    
    // --------------------------------------------------------- Public Methods

    
    // ---- START OF ModuleDeployEventListener METHODS ------------------------

    
    /**
     * Invoked when a standalone J2EE module is deployed.     
     *
     * @param event the module deployment event
     *
     * @throws AdminEventListenerException when the listener is unable to
     * process the event.
     */
    public synchronized void moduleDeployed(ModuleDeployEvent event) 
            throws AdminEventListenerException {

        if (event.getModuleType().equals(ModuleDeployEvent.TYPE_WEBMODULE)) { 
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                    "[WebModuleDeployEventListener] Handling event " + event.toString());
            }

            DeployEventListenerHelper.getDeployEventListenerHelper().synchronize(event);

            ConfigContext config = event.getConfigContext(); 
            String moduleName = event.getModuleName(); 

            // refreshes the config context with the context from this event
            try {
                getWebModulesManager().refreshConfigContext(config);
            } catch (ConfigException ce) {
                throw new AdminEventListenerException(ce.getMessage());
            }

            // The only job of dummyLoader is to inform the ondemand framework
            // about this module. It does not actually load the app.
            AbstractLoader dummyLoader = getLoader(moduleName);
            if (dummyLoader.load(false)) {
                moduleDeployed(config, moduleName);
            }
        }
        
    }
    
    
    /**
     * Invoked when a standalone J2EE module is undeployed. 
     *
     * @param event the module deployment event
     *
     * @throws AdminEventListenerException when the listener is unable to
     * process the event.
     */
    public synchronized void moduleUndeployed(ModuleDeployEvent event) 
            throws AdminEventListenerException {

        if (event.getModuleType().equals(ModuleDeployEvent.TYPE_WEBMODULE)) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                    "[WebModuleDeployEventListener] Handling event " + event.toString());
            }
            ConfigContext config = event.getOldConfigContext(); 
            String moduleName = event.getModuleName(); 

            // refreshes the config context with the context from this event
            try {
                getWebModulesManager().refreshConfigContext(config);
            } catch (ConfigException ce) {
                throw new AdminEventListenerException(ce.getMessage());
            }

            AbstractLoader dummyLoader = getLoader(moduleName);
            dummyLoader.unload(false);
            moduleUndeployed(config, moduleName);
        }            

    }


    /**
     * Invoked when a standalone J2EE module is redeployed. 
     *
     * @param event the module deployment event
     *
     * @throws AdminEventListenerException when the listener is unable to
     * process the event.
     */
    public synchronized void moduleRedeployed(ModuleDeployEvent event) 
            throws AdminEventListenerException {

        if (event.getModuleType().equals(ModuleDeployEvent.TYPE_WEBMODULE)) {  
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                    "[WebModuleDeployEventListener] Handling event " + event.toString());
            }
            String moduleName = event.getModuleName(); 
                
            // unload from old location 
            ConfigContext oldConfig = event.getOldConfigContext();
            moduleUndeployed(oldConfig, moduleName);
               
            // load to new location
            ConfigContext config = event.getConfigContext(); 
            moduleDeployed(config, moduleName);
        }
        
    }


    /**
     * Invoked when a standalone J2EE module is enabled. 
     *
     * @param event the module deployment event
     *
     * @throws AdminEventListenerException when the listener is unable to
     * process the event.
     */
    public synchronized void moduleEnabled(ModuleDeployEvent event) 
            throws AdminEventListenerException {

        if (event.getModuleType().equals(ModuleDeployEvent.TYPE_WEBMODULE)) { 
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                    "[WebModuleDeployEventListener] Handling event " + event.toString());
            }
            ConfigContext config = event.getConfigContext(); 
            String moduleName = event.getModuleName(); 

            WebModule webModule = getWebModuleAndUpdateCache(
                config, moduleName, true); 

            //Do not deploy if enable is false
            if (!isEnabled(event.getConfigContext(), moduleName)) {
                return;
            }

            String location = webModule.getLocation();
            // If module root is relative then prefix it with the
            // location of where all the standalone modules for
            // this server instance are deployed
            File moduleBase = new File(location);
            String modulesRoot = getWebContainer().getModulesRoot();
            if (!moduleBase.isAbsolute()) {
                location = modulesRoot+File.separator+location;
                webModule.setLocation(location);
            }
            WebModuleConfig wmInfo = loadWebModuleConfig(webModule, config);
            getWebContainer().enableWebModule(wmInfo, "null");
        }
        
    }


    /**
     * Invoked when a standalone J2EE module is disabled. 
     *
     * @param event the module deployment event
     *
     * @throws AdminEventListenerException when the listener is unable to
     * process the event.
     */
    public synchronized void moduleDisabled(ModuleDeployEvent event) 
            throws AdminEventListenerException {

        if (event.getModuleType().equals(ModuleDeployEvent.TYPE_WEBMODULE)) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                    "[WebModuleDeployEventListener] Handling event " + event.toString());
            }
            ConfigContext config = event.getConfigContext(); 
            String moduleName = event.getModuleName(); 

            WebModule webModule = getWebModuleAndUpdateCache(
                config, moduleName, true); 

            String appName = null;
            WebBundleDescriptor wbd;
            try {
                Application app = getWebModulesManager().getDescriptor(
                    webModule.getName(), webModule.getLocation());
                wbd = 
                    (WebBundleDescriptor) app.getStandaloneBundleDescriptor();
                appName = app.getRegistrationName();
            } catch (ConfigException ce) {
                throw new AdminEventListenerException(ce);
            }
            String contextRoot = webModule.getContextRoot();
            String virtualServers = null;
            try {
                virtualServers = ServerBeansFactory
                                .getVirtualServersByAppName(config,moduleName);
            } catch(ConfigException ce) {
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST,
                    "Exception getting virtual servers by app name "
                    + moduleName, ce);
                }
            }
            getWebContainer().disableWebModule(contextRoot,appName,
                virtualServers);
        }  
        
    }

    private synchronized WebModule getWebModuleAndUpdateCache(
        ConfigContext config, String moduleName, boolean isAddToCache) 
        throws AdminEventListenerException {
        WebModule webModule = (WebModule)cachedWebModules.get(moduleName);
        if ( webModule  == null ) {
            try {
              webModule = (WebModule) ApplicationHelper.findApplication(
                  config, moduleName);
            } catch (ConfigException ce) {
                throw new AdminEventListenerException(ce);
            }
 
            if (webModule == null) {
                String msg = localStrings.getString(
                    "webmodule.module_not_found",
                    moduleName);
                throw new AdminEventListenerException(msg);
            }
            if (isAddToCache) {
                cachedWebModules.put(moduleName, webModule);
            }
        } else {
            if (!isAddToCache) {
                cachedWebModules.remove(moduleName);
            }
        }
        return webModule;
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
    
    
    // ---- END OF ModuleDeployEventListener METHODS --------------------------
    
    /**
     * Whether or not a web module should be
     * enabled is defined by the "enable" attribute on both the 
     * module element and the application-ref element.
     * 
     * @param config The dynamic ConfigContext
     * @param moduleName The name of the component (application or module)
     * @return boolean 
     */
    protected boolean isEnabled(ConfigContext config, String moduleName) {
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

    private WebModulesManager getWebModulesManager() throws ConfigException {
        return (WebModulesManager) this.configManager;
    }

    private WebContainer getWebContainer() {
        if (webContainer == null) {
            this.webContainer = WebContainer.getInstance();
        }
        return this.webContainer;
    }

}
