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

package com.sun.enterprise.web;

import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.web.stats.PWCRequestStatsImpl;
import com.sun.enterprise.v3.server.Globals;
import java.beans.PropertyVetoException;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.valves.RemoteAddrValve;
import org.apache.catalina.valves.RemoteHostValve;

import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpProtocol;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Property;

//import com.sun.enterprise.config.serverbeans.VirtualServerClass;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.v3.data.ApplicationInfo;
import com.sun.enterprise.v3.data.ModuleInfo;
import com.sun.enterprise.v3.data.ApplicationRegistry;
import com.sun.enterprise.web.WebDeployer;

import com.sun.enterprise.security.web.SingleSignOn;
import com.sun.enterprise.web.pluggable.WebContainerFeatureFactory;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.io.FileUtils;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
//import com.sun.enterprise.deployment.backend.DeploymentUtils;
import com.sun.logging.LogDomains;

/**
 * Standard implementation of a virtual server (aka virtual host) in
 * the iPlanet Application Server.
 */

public class VirtualServer extends StandardHost {

    public static final String ADMIN_VS = "__asadmin";

    static final String STATE = "state";
    static final String SSO_MAX_IDLE="sso-max-inactive-seconds";
    static final String SSO_REAP_INTERVAL="sso-reap-interval-seconds";
    static final String DISABLED = "disabled";
    static final String OFF = "off";
    static final String ON = "on";

    // ------------------------------------------------------------ Constructor

    /**
     * Default constructor that simply gets a handle to the web container 
     * subsystem's logger.
     */
    public VirtualServer() {

        super();

        origPipeline = pipeline;
        vsPipeline = new VirtualServerPipeline(this);
        accessLogValve = new PEAccessLogValve();

        _debug = _logger.isLoggable(Level.FINE);
    }

    // ----------------------------------------------------- Instance Variables

    /*
     * The custom pipeline of this VirtualServer, which implements the 
     * following virtual server features:
     *
     * - state (disabled/off)
     * - redirects
     */
    private VirtualServerPipeline vsPipeline;

    /*
     * The original (standard) pipeline of this VirtualServer.
     *
     * Only one (custom or original) pipeline may be active at any given time.
     * Any updates (such as adding or removing valves) to the currently
     * active pipeline are propagated to the other.
     */
    private Pipeline origPipeline;

    /**
     * The id of this virtual server as specified in the configuration.
     */
    private String _id = null;

    /**
     * The logger to use for logging ALL web container related messages.
     */
    protected static final Logger _logger
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);

    /**
     * The resource bundle containing the message strings for _logger.
     */
    protected static final ResourceBundle _rb = _logger.getResourceBundle();

    /**
     * Indicates whether the logger level is set to any one of 
     * FINE/FINER/FINEST.
     *
     * This flag is used to avoid incurring a perf penalty by making
     * logging calls for debug messages when the logger level is
     * INFO or higher.
     */
    protected boolean _debug = false;

    /**
     * The descriptive information about this implementation.
     */
    private static final String _info =
        "com.sun.enterprise.web.VirtualServer/1.0";

    /**
     * The config bean associated with this VirtualServer
     */
    private com.sun.enterprise.config.serverbeans.VirtualServer vsBean;

    /**
     * The mime mapping associated with this VirtualServer
     */
    private MimeMap mimeMap;

    /*
     * Indicates whether symbolic links from this virtual server's docroot
     * are followed. This setting is inherited by all web modules deployed on
     * this virtual server, unless overridden by a web modules allowLinking
     * property in sun-web.xml.
     */
    private boolean allowLinking = false;


     /*
     * default context.xml location
     */
    private String defaultContextXmlLocation;
    

    /*
     * default-web.xml location 
     */
    private String defaultWebXmlLocation;


    private String[] cacheControls;


    // Is this virtual server active?
    private boolean isActive;

    
    /**
     * The Stats holder used by this virtual-server.
     */
    private PWCRequestStatsImpl pwcRequestStatsImpl;


    private String authRealmName;


    /*
     * The accesslog valve of this VirtualServer.
     *
     * This valve is activated, that is, added to this virtual server's
     * pipeline, only when access logging has been enabled. When acess logging
     * has been disabled, this valve is removed from this virtual server's
     * pipeline.
     */
    private PEAccessLogValve accessLogValve;
    
    private HashMap<String, String> alternateDocBasesMap = null;


    // ------------------------------------------------------------- Properties

    /**
     * Return the virtual server identifier.
     */
    public String getID() {
        return _id;
    }

    /**
     * Set the virtual server identifier string.
     *
     * @param id New identifier for this virtual server
     */
    public void setID(String id) {
        _id = id;
    }

    /**
     * @return true if this virtual server is active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the state of this virtual server.
     *
     * @param isActive true if this virtual server is active, false otherwise
     */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
        if (isActive) {
            vsPipeline.setIsDisabled(false);
            vsPipeline.setIsOff(false);
            if (pipeline == vsPipeline && !vsPipeline.hasRedirects()) {
                // Restore original pipeline
                setPipeline(origPipeline);
            }
        }
    }

    /**
     * Gets the default-context.xml location of web modules deployed on this
     * virtual server.
     *
     * @return default-context.xml location of web modules deployed on this
     * virtual server
     */
    public String getDefaultContextXmlLocation() {
        return defaultContextXmlLocation;
    }

    /**
     * Sets the default-context.xml location for web modules deployed on this
     * virtual server.
     *
     * @param defaultWebXmlLocation default-context.xml location for web modules
     * deployed on this virtual server
     */
    public void setDefaultContextXmlLocation(String defaultContextXmlLocation) {
        this.defaultContextXmlLocation = defaultContextXmlLocation;
    }

    /**
     * Gets the default-web.xml location of web modules deployed on this
     * virtual server.
     *
     * @return default-web.xml location of web modules deployed on this
     * virtual server
     */
    public String getDefaultWebXmlLocation() {
        return defaultWebXmlLocation;
    }

    /**
     * Sets the default-web.xml location for web modules deployed on this
     * virtual server.
     *
     * @param defaultWebXmlLocation default-web.xml location for web modules
     * deployed on this virtual server
     */
    public void setDefaultWebXmlLocation(String defaultWebXmlLocation) {
        this.defaultWebXmlLocation = defaultWebXmlLocation;
    }

    /**
     * Gets the value of the allowLinking property of this virtual server.
     *
     * @return true if symbolic links from this virtual server's docroot (as
     * well as symbolic links from archives of web modules deployed on this
     * virtual server) are followed, false otherwise
     */
    public boolean getAllowLinking() {
        return allowLinking;
    }

    /**
     * Sets the allowLinking property of this virtual server, which determines
     * whether symblic links from this virtual server's docroot are followed.
     *
     * This property is inherited by all web modules deployed on this virtual
     * server, unless overridden by the allowLinking property in a web module's
     * sun-web.xml.
     *
     * @param allowLinking Value of allowLinking property
     */
    public void setAllowLinking(boolean allowLinking) {
        this.allowLinking = allowLinking;
    }

    /**
     * Gets the config bean associated with this VirtualServer.
     */
    public com.sun.enterprise.config.serverbeans.VirtualServer getBean(){
        return vsBean;
    }

    /**
     * Sets the config bean for this VirtualServer
     */
     public void setBean(com.sun.enterprise.config.serverbeans.VirtualServer vsBean){
        this.vsBean = vsBean;
     }

    /**
     * Gets the mime map associated with this VirtualServer.
     */
    public MimeMap getMimeMap(){
        return mimeMap;
    }

    /**
     * Sets the mime map for this VirtualServer
     */
    public void setMimeMap(MimeMap mimeMap){
        this.mimeMap = mimeMap;
    }

    /**
     * Gets the Cache-Control configuration of this VirtualServer.
     *
     * @return Cache-Control configuration of this VirtualServer, or null if
     * no such configuration exists for this VirtualServer
     */
    public String[] getCacheControls() {
        return cacheControls;
    }

    /**
     * Sets the Cache-Control configuration for this VirtualServer
     *
     * @param cacheControls Cache-Control configuration settings for this
     * VirtualServer
     */
    public void setCacheControls(String[] cacheControls) {
        this.cacheControls = cacheControls;
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Return descriptive information about this ContractProvider implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return _info;
    }


    // ------------------------------------------------------ Lifecycle Methods

    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public synchronized void stop() throws LifecycleException {

        super.stop();

        // Remove the descriptor bindings for all the web applications
        // in this virtual server
/*        Switch sw = Switch.getSwitch();
        ContractProvider children[] = findChildren();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                sw.removeDescriptorFor(children[i]);
            }
        }*/
    }


    /**
     * Adds the given valve to the currently active pipeline, keeping the
     * valve that is not currently active in sync.
     */
    public synchronized void addValve(Valve valve) {
        super.addValve(valve);
        if (pipeline == vsPipeline) {
            origPipeline.addValve(valve);
        } else {
            vsPipeline.addValve(valve);
        }
    }


    /**
     * Removes the given valve from the currently active pipeline, keeping the
     * valve that is not currently active in sync.
     */
    public synchronized void removeValve(Valve valve) {
        super.removeValve(valve);
        if (pipeline == vsPipeline) {
            origPipeline.removeValve(valve);
        } else {
            vsPipeline.removeValve(valve);
        }
    }


    // ------------------------------------------------------ Protected Methods

    /**
     * Return the list of enabled web-modules configured for this
     * virtual server.
     *
     * @return     The list of WebModuleConfig objects for all enabled
     *             web-modules hosted under the specified virtual server.
     *
    
    protected List getWebModules(Server serverBean, String modulesRoot) {

        List modules = new Vector();

        Domain domain = com.sun.enterprise.v3.server.Globals.getGlobals().getDefaultHabitat().getComponent(Domain.class);
        Applications appsBean = domain.getApplications();
        
        if (appsBean != null) {
            List apps = appsBean.getLifecycleModuleOrJ2EeApplicationOrEjbModuleOrWebModuleOrConnectorModuleOrAppclientModuleOrMbeanOrExtensionModule();
            for (Object app : apps) {
                if (app instanceof WebModule) {
                    WebModule wm = (WebModule) app;
                    if (isActive(wm)) {                      
                        // skips if the web module is not referenced by 
                        // this server
                        
                        ApplicationRef ref = null;
                        for (ApplicationRef ar : serverBean.getApplicationRef()) {
                            if (ar.getRef().equals(wm.getName())) {
                                ref = ar;
                                break;
                            }
                        }
                        if (ref == null) {
                            continue;
                        }                       

                        String location = wm.getLocation();
                        // If module root is relative then prefix it with the 
                        // location of where all the standalone modules for 
                        // this server instance are deployed
                        File moduleBase = new File(location);
                        try {
                        if (!moduleBase.isAbsolute()) {
                            location = modulesRoot+File.separator+location;
                            wm.setLocation(location);
                        }
                        }
                        catch (PropertyVetoException pve) {
                            // XXX
                        }
                        WebModuleConfig wmInfo = loadWebModuleConfig(wm);
                        if (wmInfo != null)
                            modules.add(wmInfo);
                    } else {
                        if (_debug) {
                            _logger.finer("Web Module [" + wm.getName() + 
                                          "] is not applicable for virtual " +
                                          " server [" + getID() + "]");
                        }
                    }
                }
            }
        }
        
        return modules;
        
    }*/

    /**
     * Gets the context root of the web module that the user/configuration
     * has designated as the default-web-module for this virtual server.
     *
     * The default-web-module for a virtual server is specified via the
     * 'default-web-module' attribute of the 'virtual-server' element in
     * server.xml. This is an optional attribute and if the configuration
     * does not specify another web module (standalone or part of a 
     * j2ee-application) that is configured at a context-root="", then
     * a default web module will be created and loaded. The value for this
     * attribute is either "${standalone-web-module-name}" or 
     * "${j2ee-app-name}:${web-module-uri}".
     *
     * @return null if the default-web-module has not been specified or
     *              if the web module specified either could not be found or
     *              is disabled or does not specify this virtual server (if
     *              it specifies a value for the virtual-servers attribute) or
     *              if there was an error loading its deployment descriptors.
     */
    protected String getDefaultContextPath(Server serverBean) {

        String contextRoot = null;
        Domain domain = com.sun.enterprise.v3.server.Globals.getDefaultHabitat().getComponent(Domain.class);
        Applications appsBean = domain.getApplications();

        String wmID = getDefaultWebModuleID();
        if (wmID != null) {
            // Check if the default-web-module is part of a
            // j2ee-application
            WebModuleConfig wmInfo = findWebModuleInJ2eeApp(appsBean, wmID);

            // Look up the list of standalone web modules
            if (wmInfo == null) {
                WebModule wm = ConfigBeansUtilities.getModule(WebModule.class, appsBean, wmID);
                if (wm != null) {
                    if (isActive(wm, false)) {
                        // Create a copy as we need to change the name
                        // and context root to indicate that this web module
                        // is to be loaded as the 'default' web module for
                        // the virtual server
                        // WebModule wmCopy = (WebModule) wm.clone();
                        contextRoot = wm.getContextRoot();
                    } else {
                        Object[] params = { wmID, getID() };
                        _logger.log(Level.SEVERE, "vs.defaultWebModuleDisabled",
                                    params);
                    }
                }
            } else {
                WebModule wm = wmInfo.getBean();
                contextRoot = wm.getContextRoot();
            }

            if (contextRoot == null) {
                Object[] params = { wmID, getID() };
                _logger.log(Level.SEVERE, "vs.defaultWebModuleNotFound",
                            params);
            }
        }

        return contextRoot;
    }

    /**
     * If a default web module has not yet been configured and added to this
     * virtual server's list of web modules then return the configuration
     * information needed in order to create a default web module for this
     * virtual server.
     *
     * This method should be invoked only after all the standalone modules
     * and the modules within j2ee-application elements have been added to
     * this virtual server's list of modules (only then will one know whether
     * the user has already configured a default web module or not).
     */
    protected WebModuleConfig createSystemDefaultWebModuleIfNecessary() {

        WebModuleConfig wmInfo = null;
        /*
        //
        // Add a default context only if one hasn't already been loaded
        // and then too only if docroot is not null
        //
        String docroot = getAppBase();
        if (getDefaultWebModuleID() == null && (findChild("") == null)
                && (docroot != null)) {
            wmInfo = new WebModuleConfig();
            WebModule wm = new WebModule();
            try {
            wm.setName(Constants.DEFAULT_WEB_MODULE_NAME);
            wm.setContextRoot("");
            wm.setLocation(docroot);
            wmInfo.setBean(wm);
            } catch (PropertyVetoException pve) {
                // XXX
            }
            WebDeployer webDeployer = Globals.getGlobals().getDefaultHabitat().
                getComponent(WebDeployer.class);
            
            wmInfo.setDescriptor(
                webDeployer.getDefaultWebXMLBundleDescriptor());
 
            WebBundleDescriptor wbd = wmInfo.getDescriptor();
            if ( wbd.getApplication() == null ) {
                Application application = new Application();
                application.setVirtual(true);
                application.setName(Constants.DEFAULT_WEB_MODULE_NAME);
                wbd.setApplication(application);
            }
        }
        */
        return wmInfo;
    }


    /**
     * Creates and returns an object that contains information about
     * the web module's configuration such as the information specified
     * in server.xml, the deployment descriptor objects etc.
     *
     * @return null if an error occured while reading/parsing the 
     *              deployment descriptors.
     */
    protected WebModuleConfig loadWebModuleConfig(WebModule wm) {

        WebModuleConfig wmInfo = new WebModuleConfig();
        wmInfo.setBean(wm);
        String wmID = wm.getName();
        String location = wm.getLocation();

        /*ApplicationInfo<WebBundleDescriptor> appInfo =
                ContainerRegistry.getRegistry().find(wmID);
        WebBundleDescriptor wbd = appInfo.getDescriptor();
        wmInfo.setDescriptor(wbd);
        */
        return wmInfo;
    }

    /**
     * Determines whether the specified web module is "active" under this
     * virtual server.
     */
    private boolean isActive(WebModule wm) {
        return isActive(wm, true);
    }

    /**
     * Determines whether the specified web module is "active" under this
     * virtual server.
     *
     * A web module is active if it meets ALL of the following conditions:
     *   - wm is not null
     *   - the enabled attribute of the web-module element is true
     * If matchVSID is true then the following additional condition must be
     * satisfied.
     *   - the virtual-servers attribute of the web-module element is either
     *     empty/not-specified or if specified then this virtual server's
     *     name/ID is one of the virtual servers specified in the list
     *
     * @param wm        The bean containing the web module configuration
     *                  information as specified in server.xml
     * @param matchVSID This is set to false when testing to see if a
     *                  web module that has been configured to be the
     *                  default-web-module for a VS is active or not. In this
     *                  case the only test is to check if the web module
     *                  is enabled or not. When set to true, the virtual
     *                  server list is examined to ensure that the web
     *                  module has been configured to run on this virtual
     *                  server.
     * @return     <code>true</code> if all the criteria are satisfied and
     *             <code>false</code> otherwise.
     */
    protected boolean isActive(WebModule wm, boolean matchVSID) {

        String vsID = getID();

        boolean active = ((vsID != null) && (vsID.length() > 0));
        active &= (wm != null);

        if (active) {
            // Check if the web module is enabled
            active &= Boolean.getBoolean(wm.getEnabled());

            //
            // Check if vsID is one of the virtual servers specified
            // in the list of virtual servers that the web module is to
            // be loaded on. If the virtual-servers attribute of the
            // <web-module> element is missing or empty then the implied
            // behaviour is that the web module is active on every virtual
            // server.
            //
            String vsIDs = getVirtualServers(wm.getName());

            //
            // fix for bug# 4913636
            // so that for PE if the vsList is null and the virtual server is
            // admin-vs then return false because we don't want to load user apps
            // on admin-vs
            //
            if (getID().equals(ADMIN_VS) && matchVSID
                    && ((vsIDs == null) || (vsIDs.length() == 0 ))) {
                return false;
            } 


            if (matchVSID && (vsIDs != null) && (vsIDs.length() > 0)) {
                List vsList = StringUtils.parseStringList(vsIDs, " ,");
                if (vsList != null)
                    active &= vsList.contains(vsID.trim());
                else
                    active &= true;
            } else
                active &= true;
        }
        return active;
    }

    /**
     * Returns the id of the default web module for this virtual server
     * as specified in the 'default-web-module' attribute of the 
     * 'virtual-server' element.
     *
     * This is an optional attribute.
     */
    protected String getDefaultWebModuleID() {
        String wmID = null;
        
        if (vsBean != null) {
            wmID = vsBean.getDefaultWebModule();
            if (wmID != null && _debug) {
                Object[] params = { wmID, _id };
                _logger.log(Level.FINE, "vs.defaultWebModule", params);
            }
        } else {
            _logger.log(Level.SEVERE, "vs.configError", _id);
        }

        return wmID;
    }

    /**
     * Finds and returns information about a web module embedded within a
     * J2EE application, which is identified by a string of the form
     * <code>a:b</code> or <code>a#b</code>, where <code>a</code> is the name
     * of the J2EE application and <code>b</code> is the name of the embedded
     * web module.
     *
     * @return null if <code>id</code> does not identify a web module embedded
     * within a J2EE application.
     */
    protected WebModuleConfig findWebModuleInJ2eeApp(Applications appsBean,
                                                   String id) {
        WebModuleConfig wmInfo = null;
        /*
        int length = id.length();
        // Check for ':' separator
        int separatorIndex = id.indexOf(Constants.NAME_SEPARATOR);
        if (separatorIndex == -1) {
            // Check for '#' separator
            separatorIndex = id.indexOf('#');
        }
        if (separatorIndex != -1) {
            String appID = id.substring(0, separatorIndex);
            String moduleID = id.substring(separatorIndex + 1);
            
            J2EeApplication j2eeApp = ConfigBeansUtilities.getModule(J2EeApplication.class, appsBean, appID);
            
            if ((j2eeApp != null) && Boolean.getBoolean(j2eeApp.getEnabled())) {
                String location = j2eeApp.getLocation();
                String moduleDir = FileUtils.makeFriendlyFilename(moduleID);
                
                ApplicationRegistry registry = com.sun.enterprise.v3.server.Globals.getGlobals().getDefaultHabitat().getComponent(ApplicationRegistry.class);
                ApplicationInfo appInfo = registry.get(appID);
                
                if (appInfo != null) {
                    Application appDesc = null;
                    for (ModuleInfo info : appInfo.getModuleInfos()) {
                        if (info.getContainerInfo().getSniffer().getModuleType().equals("web")) {
                            appDesc = (Application) info.getDescriptor();
                        }
                    }
                    if (appDesc != null) {
                        Set wbds = appDesc.getWebBundleDescriptors();
                        WebBundleDescriptor wbd = null;
                        for (Iterator itr = wbds.iterator(); itr.hasNext(); ) {
                            wbd = (WebBundleDescriptor) itr.next();
                            String webUri = wbd.getModuleDescriptor().getArchiveUri();
                            if (moduleID.equals(webUri)) {
                                StringBuffer dir = new StringBuffer(location);
                                dir.append(File.separator);
                                dir.append(moduleDir);
                                WebModule wm = new WebModule();
                                try {
                                wm.setName(moduleID);
                                wm.setContextRoot(wbd.getContextRoot());
                                wm.setLocation(dir.toString());
                                wm.setEnabled(Boolean.TRUE.toString());
                                } catch (PropertyVetoException pve) {
                                    // XXX
                                }
                                String vsList = getVirtualServers(j2eeApp.getName());
                                wmInfo = new WebModuleConfig();
                                wmInfo.setBean(wm);
                                wmInfo.setDescriptor(wbd);
//                                wmInfo.setParentLoader(appLoader);
                                wmInfo.setVirtualServers(vsList);
                                break;
                            }
                        }
                    }
                }
            } else {
                Object[] params = { id, getID() };
                _logger.log(Level.SEVERE, "vs.defaultWebModuleDisabled",
                            params);
            }
        }
         */
        return wmInfo;
    }
    
    /**
     * Virtual servers are maintained in the reference contained 
     * in Server element. First, we need to find the server
     * and then get the virtual server from the correct reference
     *
     * @param appName Name of the app to get vs
     *
     * @return virtual servers as a string (separated by space or comma)
     */
    private String getVirtualServers(String appName) {
        String ret = null;
        Server server = com.sun.enterprise.v3.server.Globals.getDefaultHabitat().getComponent(Server.class);
        for (ApplicationRef appRef : server.getApplicationRef()) {
            if (appRef.getRef().equals(appName)) {
                return appRef.getVirtualServers();
            }
        }
        
        return ret;
    }
    
    
    /**
     * Delete all aliases.
     */
    public void clearAliases(){
        aliases = new String[0];
    }


    private void setIsDisabled(boolean isDisabled) {
        vsPipeline.setIsDisabled(isDisabled);
        vsPipeline.setIsOff(false);
        if (isDisabled && pipeline != vsPipeline) {
            // Enable custom pipeline
            setPipeline(vsPipeline);
        } 
    }


    private void setIsOff(boolean isOff) {
        vsPipeline.setIsOff(isOff);
        vsPipeline.setIsDisabled(false);
        if (isOff && pipeline != vsPipeline) {
            // Enable custom pipeline
            setPipeline(vsPipeline);
        } 
    }


    /**
     * @return The properties of this virtual server, or null
     */
    List<Property> getProperties() {
        if (vsBean != null) {
            return vsBean.getProperty();
        } else {
            return null;
        }
    }


    /**
     * Set the Stat holder.
     */
    public void setPWCRequestStatsImpl(PWCRequestStatsImpl pwcRequestStatsImpl){
        this.pwcRequestStatsImpl = pwcRequestStatsImpl;
    }

    
    /**
     * Get the Stat holder.
     */
    public PWCRequestStatsImpl getPWCRequestStatsImpl(){
        return pwcRequestStatsImpl;
    }

    
    /**
     * Configures the valve_ and listener_ properties of this VirtualServer.
     */
    protected void configureCatalinaProperties(){

        List<Property> props = vsBean.getProperty();
        if (props == null) {
            return;
        }

        for (Property prop : props) {

            String propName = prop.getName();
            String propValue = prop.getValue();
            if (propName == null || propValue == null) {
                _logger.log(Level.WARNING,
                            "webcontainer.nullWebModuleProperty",
                            getName());
            }

            if (propName.startsWith("valve_")) {
                addValve(propValue);            
            } else if (propName.startsWith("listener_")) {
                addListener(propValue);   
            } else if (propName.equals("securePagesWithPragma")){
                setSecurePagesWithPragma(Boolean.valueOf(propValue));
            }
        }
    }
    

    /**
     * Configure virtual-server alias attribute.
     */    
    void configureAliases() {

        // Add each host name from the 'hosts' attribute as an alias
        List hosts = StringUtils.parseStringList(vsBean.getHosts(), ",");
        for (int i=0; i < hosts.size(); i++ ){
            String alias = hosts.get(i).toString();
            if ( !alias.equalsIgnoreCase("localhost.localdomain")){
                addAlias(alias);
            }
        }
    }

 
    /**
     * Configures this virtual server with its authentication realm.
     *
     * Checks if this virtual server specifies any authRealm property, and
     * if so, ensures that its value identifies a valid realm.
     *
     * @param securityService The security-service element from domain.xml
     */
    void configureAuthRealm(SecurityService securityService) {
        /*
        ElementProperty prop = vsBean.getElementPropertyByName("authRealm");
        if (prop != null && prop.getValue() != null) {
            if (securityService.getAuthRealmByName(prop.getValue()) != null) {
                authRealmName = prop.getValue();
            } else {
                _logger.log(Level.SEVERE, "vs.invalidAuthRealm",
                            new Object[] { getID(), prop.getValue() });
            }
        }*/
    }
     

    /**
     * Gets the value of the authRealm property of this virtual server.
     *
     * @return The value of the authRealm property of this virtual server,
     * or null of this virtual server does not have any such property
     */
    String getAuthRealmName() {
        return authRealmName;
    }


    /**
     * Adds the <code>Valve</code> with the given class name to this
     * VirtualServer.
     *
     * @param valveName The valve's fully qualified class name
     */
    protected void addValve(String valveName) {
        Valve valve = (Valve)loadInstance(valveName);  
        
        if (valve == null) return;
        
        super.addValve(valve); 
    }    
    
    
    /**
     * Adds the Catalina listener with the given class name to this
     * VirtualServer.
     * 
     * @param listenerName The fully qualified class name of the listener. 
     */
    protected void addListener(String listenerName) {
        Object listener = loadInstance(listenerName);
        
        if ( listener == null ) return;

        if (listener instanceof ContainerListener) {
            addContainerListener((ContainerListener)listener);
        } else if (listener instanceof LifecycleListener){
            addLifecycleListener((LifecycleListener)listener);            
        } else {
            _logger.log(Level.SEVERE,"webcontainer.invalidListener"
                    + listenerName);
        }     
    }
    
   
    private Object loadInstance(String className){
        try{
            Class clazz = Class.forName(className);
            return clazz.newInstance();
        } catch (Throwable ex){
            _logger.log(Level.SEVERE,"webcontainer.unableToLoadExtension",ex);        
        }
        return null;
    }


    void configureAlternateDocBases() {

        if (vsBean == null) {
            return;
        }
        List<Property> props = vsBean.getProperty();
        if (props == null) {
            return;
        }

        for (Property prop : props) {

            String propName = prop.getName();
            String propValue = prop.getValue();
            if (propName == null || propValue == null) {
                _logger.log(Level.WARNING,
                            "webcontainer.nullVirtualServerProperty",
                            getID());
            }

            if (!propName.startsWith("alternatedocroot_")) {
                continue;
            }
            
            /*
             * Validate the prop value
             */
            String urlPattern = null;
            String docBase = null;
            String[] alternateDocBaseParams = propValue.split(" ");
            for (int j=0; j<alternateDocBaseParams.length; j++) {

                if (alternateDocBaseParams[j].startsWith("from=")) {
                    urlPattern = alternateDocBaseParams[j].substring(
                        "from=".length());
                    if (!validateURLPattern(urlPattern)) {
                        _logger.log(
                            Level.SEVERE,
                            "webcontainer.alternateDocBase.illegalUrlPattern",
                            urlPattern);
                    }
                }
                if (alternateDocBaseParams[j].startsWith("dir=")) {
                    docBase = alternateDocBaseParams[j].substring(
                        "dir=".length());
                }
            }

            addAlternateDocBase(urlPattern, docBase);
        }

    }


    private boolean validateURLPattern(String urlPattern) {

        if (urlPattern == null)
            return (false);
        if (urlPattern.indexOf('\n') >= 0 || urlPattern.indexOf('\r') >= 0) {
            _logger.log(Level.WARNING,
                        "webcontainer.alternateDocBase.crlfInUrlPattern",
                        urlPattern);
        }

        if (urlPattern.startsWith("*.")) {
            if (urlPattern.indexOf('/') < 0) {
                return (true);
            } else {
                return (false);
            }
        }
        if ( (urlPattern.startsWith("/")) &&
	     (urlPattern.indexOf("*.") < 0)) {
            return (true);
        } else {
            return (false);
        }
    }


    /**
     * Adds the given mapping of url pattern to alternate doc base to this
     * VirtualServer.
     *
     * @param urlPattern The url pattern
     * @param docBase The alternate doc base
     */
    void addAlternateDocBase(String urlPattern, String docBase) {
        if (urlPattern == null || docBase == null) {
            _logger.log(
                Level.SEVERE,
                "webcontainer.alternateDocBase.missingPathOrUrlPattern");
        }

        if (alternateDocBasesMap == null) {
            alternateDocBasesMap = new HashMap<String, String>();
        }
        alternateDocBasesMap.put(urlPattern, docBase);
    }


    /**
     * Gets the mappings of url patterns to alternate doc bases of this
     * VirtualServer.
     *
     * @return The mappings of url patterns to alternate doc bases of this
     * VirtualServer
     */
    public HashMap<String, String> getAlternateDocBasesMap() {
        return alternateDocBasesMap;
    }


    /**
     * Configures this VirtualServer with its send-error properties.
     */
    void configureErrorPage() {

        ErrorPage errorPage = null;

        if (vsBean == null) {
            return;
        }

        List<Property> props = vsBean.getProperty();
        if (props == null) {
            return;
        }

        for (Property prop : props) {

            String propName = prop.getName();
            String propValue = prop.getValue();
            if (propName == null || propValue == null) {
                _logger.log(Level.WARNING,
                            "webcontainer.nullVirtualServerProperty",
                            getID());
                continue;
            }

            if (!propName.startsWith("send-error_")) {
                continue;
            }

            /* 
             * Validate the prop value
             */
            String path = null;
            String reason = null;
            String status = null;

            String[] errorParams = propValue.split(" ");
            for (int j=0; j<errorParams.length; j++) {

                if (errorParams[j].startsWith("path=")) {
                    if (path != null) {
                        _logger.log(Level.WARNING,
                                    "webcontainer.sendErrorMultipleElement",
                                    new Object[] { propValue, "path" });
                    }
                    path = errorParams[j].substring("path=".length());
                }

                if (errorParams[j].startsWith("reason=")) {
                    if (reason != null) {
                        _logger.log(Level.WARNING,
                                    "webcontainer.sendErrorMultipleElement",
                                    new Object[] { propValue, "reason" });
                    }
                    reason = errorParams[j].substring("reason=".length());
                }

                if (errorParams[j].startsWith("code=")) {
                    if (status != null) {
                        _logger.log(Level.WARNING,
                                    "webcontainer.sendErrorMultipleElement",
                                    new Object[] { propValue, "code" });
                    }
                    status = errorParams[j].substring("code=".length());
                }
            }

            if (path == null || path.length() == 0) {
                _logger.log(Level.WARNING,
                            "webcontainer.sendErrorMissingPath",
                            propValue);
            }

            errorPage = new ErrorPage();
            errorPage.setLocation(path);
            errorPage.setErrorCode(status);
            errorPage.setReason(reason);

            addErrorPage(errorPage);
        }

    }


    /**
     * Configures this VirtualServer with its redirect properties.
     */
    void configureRedirect() {

        vsPipeline.clearRedirects();

        if (vsBean == null) {
            return;
        }

        List<Property> props = vsBean.getProperty();
        if (props == null) {
            return;
        }

        for (Property prop : props) {

            String propName = prop.getName();
            String propValue = prop.getValue();
            if (propName == null || propValue == null) {
                _logger.log(Level.WARNING,
                            "webcontainer.nullVirtualServerProperty",
                            getID());
                continue;
            }

            if (!propName.startsWith("redirect_")) {
                continue;
            }

            /* 
             * Validate the prop value
             */
            String from = null;
            String url = null;
            String urlPrefix = null;
            String escape = null;

            String[] redirectParams = propValue.split(" ");
            for (int j=0; j<redirectParams.length; j++) {

                if (redirectParams[j].startsWith("from=")) {
                    if (from != null) {
                        _logger.log(Level.WARNING,
                                    "webcontainer.redirectMultipleElement",
                                    new Object[] { propValue, "from" });
                    }
                    from = redirectParams[j].substring("from=".length());
                }

                if (redirectParams[j].startsWith("url=")) {
                    if (url != null) {
                        _logger.log(Level.WARNING,
                                    "webcontainer.redirectMultipleElement",
                                    new Object[] { propValue, "url" });
                    }
                    url = redirectParams[j].substring("url=".length());
                }

                if (redirectParams[j].startsWith("url-prefix=")) {
                    if (urlPrefix != null) {
                        _logger.log(Level.WARNING,
                                    "webcontainer.redirectMultipleElement",
                                    new Object[] { propValue, "url-prefix" });
                    }
                    urlPrefix = redirectParams[j].substring(
                                                    "url-prefix=".length());
                }

                if (redirectParams[j].startsWith("escape=")) {
                    if (escape != null) {
                        _logger.log(Level.WARNING,
                                    "webcontainer.redirectMultipleElement",
                                    new Object[] { propValue, "escape" });
                    }
                    escape = redirectParams[j].substring("escape=".length());
                }
            }

            if (from == null || from.length() == 0) {
                _logger.log(Level.WARNING,
                            "webcontainer.redirectMissingFrom",
                            propValue);
            }

            // Either url or url-prefix (but not both!) must be present
            if ((url == null || url.length() == 0)
                    && (urlPrefix == null || urlPrefix.length() == 0)) {
                _logger.log(Level.WARNING,
                            "webcontainer.redirectMissingUrlOrUrlPrefix",
                            propValue);
            }
            if (url != null && url.length() > 0
                    && urlPrefix != null && urlPrefix.length() > 0) {
                _logger.log(Level.WARNING,
                            "webcontainer.redirectBothUrlAndUrlPrefix",
                            propValue);
            }

            boolean escapeURI = true;
            if (escape != null) {
                if ("yes".equalsIgnoreCase(escape)) {
                    escapeURI = true;
                } else if ("no".equalsIgnoreCase(escape)) {
                    escapeURI = false;
                } else {
                    _logger.log(Level.WARNING,
                                "webcontainer.redirectInvalidEscape",
                                propValue);
                }
            }

            vsPipeline.addRedirect(from, url, urlPrefix, escapeURI);
        }

        if (vsPipeline.hasRedirects()) {
            if (pipeline != vsPipeline) {
                // Enable custom pipeline
                setPipeline(vsPipeline);
            }
        } else if (isActive && pipeline != origPipeline) {
            setPipeline(origPipeline);
        }
    }


    /**
     * Configures the SSO valve of this VirtualServer.
     */
    void configureSSOValve(
            boolean globalSSOEnabled,
            WebContainerFeatureFactory webContainerFeatureFactory) {

        if (vsBean == null) {
            return;
        }

        if (!isSSOEnabled(globalSSOEnabled)) {
            /*
             * Disable SSO
             */
            Object[] params = {  getID() };
            _logger.log(Level.INFO, "webcontainer.ssodisabled", params);

            // Remove existing SSO valve (if any)
            Valve[] valves = getValves();
            for (int i=0; valves!=null && i<valves.length; i++) {
                if (valves[i] instanceof SingleSignOn) {
                    removeValve(valves[i]);
                    break;
                }
            }

        } else {
            /*
             * Enable SSO
             */
            try {
                SSOFactory ssoFactory = webContainerFeatureFactory.getSSOFactory();
                //SingleSignOn sso = ssoFactory.createSingleSignOnValve();
                String vsName = this.getName();
                SingleSignOn sso = ssoFactory.createSingleSignOnValve(vsName);
                
                // set max idle time if given
                Property idle = getPropertyByName(SSO_MAX_IDLE);
                if (idle != null && idle.getValue() != null) {
                    _logger.fine("SSO entry max idle time set to: " +
                                 idle.getValue());
                    int i = Integer.parseInt(idle.getValue());
                    sso.setMaxInactive(i);
                }

                // set expirer thread sleep time if given
                Property expireTime = getPropertyByName(SSO_REAP_INTERVAL);
                if (expireTime !=null && expireTime.getValue() != null) {
                    _logger.fine("SSO expire thread interval set to : " +
                                 expireTime.getValue());
                    int i = Integer.parseInt(expireTime.getValue());
                    sso.setReapInterval(i);
                }

                // Remove existing SSO valve (if any), in case of a reconfig
                Valve[] valves = getValves();
                for (int i=0; valves!=null && i<valves.length; i++) {
                    if (valves[i] instanceof SingleSignOn) {
                        removeValve(valves[i]);
                        break;
                    }
                }

                addValve(sso);

            } catch (Exception e) {
                _logger.log(Level.WARNING, "webcontainer.ssobadconfig", e);
                _logger.log(Level.WARNING, "webcontainer.ssodisabled",
                            getID());
            }
        }
    }


    /**
     * Utility method to retrieve a virtual server property by its name
     * @param the property name
     * @return the property if found
     */
    private Property getPropertyByName(String name) {
        for (Property prop : vsBean.getProperty()) {
            if (prop.getName().equals(name)) {
                return prop;
            }
        }
        return null;
    }
    
    /**
     * Configures this VirtualServer with its state (on | off | disabled).
     */
    void configureVirtualServerState(){

        String stateValue = ON;
        if (vsBean != null){
            stateValue = vsBean.getState();
        }

        if ( ( !stateValue.equalsIgnoreCase(ON) )
            && (getName().equalsIgnoreCase(ADMIN_VS) ) ){
            throw new java.lang.IllegalArgumentException(
                "virtual-server " 
                + ADMIN_VS + " state property cannot be modified");
        }
        
        if ( stateValue.equalsIgnoreCase(DISABLED) ) {
            // state="disabled"
            setIsDisabled(true);
        } else if ( !Boolean.getBoolean( stateValue ) ) {
            // state="off"
            setIsOff(true);
        } else {
            setIsActive(true);
        }
    }


    /**
     * Configures the Remote Address Filter valve of this VirtualServer.
     *
     * This valve enforces request accpetance/denial based on the string
     * representation of the remote client's IP address.
     */
    void configureRemoteAddressFilterValve() {

        RemoteAddrValve remoteAddrValve = null;

        if (vsBean == null) {
            return;
        }

        Property allow = getPropertyByName("allowRemoteAddress");
        Property deny = getPropertyByName("denyRemoteAddress");
        if ((allow != null && allow.getValue() != null)
                || (deny != null && deny.getValue() != null))  {
            remoteAddrValve = new RemoteAddrValve();
        }

        if (allow != null && allow.getValue() != null) {
            _logger.fine("Allowing access to " + getID()+ " from " +
                         allow.getValue());
            remoteAddrValve.setAllow(allow.getValue());
        }

        if (deny != null && deny.getValue() != null) {
            _logger.fine("Denying access to " + getID()+ " from " +
                         deny.getValue());
            remoteAddrValve.setDeny(deny.getValue());
        }

        if (remoteAddrValve != null) {
            // Remove existing RemoteAddrValve (if any), in case of a reconfig
            Valve[] valves = getValves();
            for (int i=0; valves!=null && i<valves.length; i++) {
                if (valves[i] instanceof RemoteAddrValve) {
                    removeValve(valves[i]);
                    break;
                }
            }
            addValve(remoteAddrValve);
        }
    }


    /**
     * Configures the Remote Host Filter valve of this VirtualServer.
     * 
     * This valve enforces request acceptance/denial based on the name of the
     * remote host from where the request originated.
     */
    void configureRemoteHostFilterValve(HttpProtocol httpProtocol) {

        RemoteHostValve remoteHostValve = null;

        if (vsBean == null) {
            return;
        }

        Property allow = getPropertyByName("allowRemoteHost");
        Property deny = getPropertyByName("denyRemoteHost");
        if ((allow != null && allow.getValue() != null)
                || (deny != null && deny.getValue() != null))  {
            remoteHostValve = new RemoteHostValve();
        }

        if (allow != null && allow.getValue() != null) {
            _logger.fine("Allowing access to " + getID()+ " from " +
                         allow.getValue());
            if (httpProtocol == null || !Boolean.getBoolean(httpProtocol.getDnsLookupEnabled())) {
                _logger.log(Level.WARNING,
                            "webcontainer.allowRemoteHost.dnsLookupDisabled",
                            getID());
            }
            remoteHostValve.setAllow(allow.getValue());
        }

        if (deny != null && deny.getValue() != null) {
            _logger.fine("Denying access to " + getID()+ " from " +
                         deny.getValue());
            if (httpProtocol == null || !Boolean.getBoolean(httpProtocol.getDnsLookupEnabled())) {
                _logger.log(Level.WARNING,
                            "webcontainer.denyRemoteHost.dnsLookupDisabled",
                            getID());
            }
            remoteHostValve.setDeny(deny.getValue());
        }

        if (remoteHostValve != null) {
            // Remove existing RemoteHostValve (if any), in case of a reconfig
            Valve[] valves = getValves();
            for (int i=0; valves!=null && i<valves.length; i++) {
                if (valves[i] instanceof RemoteHostValve) {
                    removeValve(valves[i]);
                    break;
                }
            }
            addValve(remoteHostValve);
        }
    }


    /**
     * Reconfigures the access log of this VirtualServer with its
     * updated access log related properties.
     */
    void reconfigureAccessLog(String globalAccessLogBufferSize,
                              String globalAccessLogWriteInterval,
                              org.jvnet.hk2.component.Habitat habitat,
                              Domain domain,
                              boolean globalAccessLoggingEnabled) {
        try {
            if (accessLogValve.isStarted()) {
                accessLogValve.stop();
            }
            boolean start = accessLogValve.updateVirtualServerProperties(
                vsBean.getId(), vsBean, domain, habitat,
                globalAccessLogBufferSize, globalAccessLogWriteInterval);
            if (start && isAccessLoggingEnabled(globalAccessLoggingEnabled)) {
                enableAccessLogging();
            } else {
                disableAccessLogging();
            }
        } catch (LifecycleException le) {
            _logger.log(Level.SEVERE,
                        "pewebcontainer.accesslog.reconfigure",
                        le);
        }
    }


    /**
     * Reconfigures the access log of this VirtualServer with the
     * updated attributes of the access-log element from domain.xml.
     */
    void reconfigureAccessLog(
            HttpService httpService,
            WebContainerFeatureFactory webcontainerFeatureFactory) {

        try {
            boolean restart = false;
            if (accessLogValve.isStarted()) {
                accessLogValve.stop();
                restart = true;
            }
            accessLogValve.updateAccessLogAttributes(
                httpService,
                webcontainerFeatureFactory);
            if (restart) {
                accessLogValve.start();
            }
        } catch (LifecycleException le) {
            _logger.log(Level.SEVERE,
                        "pewebcontainer.accesslog.reconfigure",
                        le);
        }
    }


    /**
     * @return the accesslog valve of this virtual server
     */
    PEAccessLogValve getAccessLogValve() {
        return accessLogValve;
    }


    /**
     * Enables access logging for this virtual server, by adding its
     * accesslog valve to its pipeline, or starting its accesslog valve
     * if it is already present in the pipeline.
     */
    void enableAccessLogging() {
        if (!isAccessLogValveActivated()) {
            addValve(accessLogValve);
        } else {
            try {
                if (accessLogValve.isStarted()) {
                    accessLogValve.stop();
                }
                accessLogValve.start();
            } catch (LifecycleException le) {
                _logger.log(Level.SEVERE,
                            "pewebcontainer.accesslog.reconfigure",
                            le);
            }
        }
    }


    /**
     * Disables access logging for this virtual server, by removing its
     * accesslog valve from its pipeline.
     */
    void disableAccessLogging() {
        removeValve(accessLogValve);
    }


    /**
     * @return true if the accesslog valve of this virtual server has been
     * activated, that is, added to this virtual server's pipeline; false
     * otherwise
     */
    private boolean isAccessLogValveActivated() {

        Pipeline p = getPipeline();
        if (p != null) {
            Valve[] valves = p.getValves();
            if (valves != null) {
                for (int i=0; i<valves.length; i++) {
                    if (valves[i] instanceof PEAccessLogValve) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    /**
     * Configures the cache control of this VirtualServer
     */
    void configureCacheControl(String cacheControl){       
        if (cacheControl != null) {
            List values = StringUtils.parseStringList(cacheControl,
                                                      ",");
            if (values != null && !values.isEmpty()) {
                String[] cacheControls = new String[values.size()];
                setCacheControls((String[]) values.toArray(cacheControls));
            }
        }
    }


    /**
     * Checks if SSO is enabled for this VirtualServer.
     *
     * @return The value of the sso-enabled property for this VirtualServer
     */
    private boolean isSSOEnabled(boolean globalSSOEnabled)
    {
        Property ssoProperty  = getPropertyByName(Constants.SSO_ENABLED);

        if (ssoProperty == null || ssoProperty.getValue() == null) {
            return globalSSOEnabled;
        } else {
            return ConfigBeansUtilities.toBoolean(ssoProperty.getValue());
        }
    }


    /**
     * Determines whether access logging is enabled for this virtual server.
     *
     * @param globalAccessLoggingEnabled The value of the 
     * accessLoggingEnabled property of the http-service element
     *
     * @return true if access logging is enabled for this virtual server,
     * false otherwise.
     */
    boolean isAccessLoggingEnabled(boolean globalAccessLoggingEnabled) {
        Property prop  = 
            ConfigBeansUtilities.getPropertyByName(vsBean, Constants.ACCESS_LOGGING_ENABLED);
        
        if (prop == null || prop.getValue() == null) {
            return globalAccessLoggingEnabled;
        } else {
            return ConfigBeansUtilities.toBoolean(prop.getValue());
        }
    }

    /**
     * Starts the children (web contexts) of this virtual server
     * concurrently.
     *
    protected void startChildren() {
     
        ArrayList<LifecycleStarter> starters
            = new ArrayList<LifecycleStarter>();

        Container children[] = findChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Lifecycle) {
                LifecycleStarter starter =
                    new LifecycleStarter(((Lifecycle) children[i]));
                starters.add(starter);
                starter.submit();
            }
        }

        for (LifecycleStarter starter : starters) {
            Throwable t = starter.waitDone();
            if (t != null) {
                Lifecycle container = starter.getContainer();
                ((Context) container).setAvailable(false);
                String msg = _rb.getString("vs.startContextError");
                msg = MessageFormat.format(msg,
                                           new Object[] { container,
                                                          getID() });
                _logger.log(Level.SEVERE, msg, t);
            }
        }
    }*/
}
