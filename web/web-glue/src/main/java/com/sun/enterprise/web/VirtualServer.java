/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.archivist.WebArchivist;
import com.sun.enterprise.security.web.GlassFishSingleSignOn;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.web.pluggable.WebContainerFeatureFactory;
import com.sun.enterprise.web.session.SessionCookieConfig;
import org.jvnet.hk2.config.types.Property;
import com.sun.logging.LogDomains;
import com.sun.web.security.RealmAdapter;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.deploy.ErrorPage;
import com.sun.enterprise.web.logger.FileLoggerHandler;
import org.apache.catalina.valves.RemoteAddrValve;
import org.apache.catalina.valves.RemoteHostValve;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.web.loader.WebappClassLoader;
import org.glassfish.web.valve.GlassFishValve;
import org.jvnet.hk2.component.Habitat;

/**
 * Standard implementation of a virtual server (aka virtual host) in
 * the iPlanet Application Server.
 */

public class VirtualServer extends StandardHost {

    private static final String STATE = "state";
    private static final String SSO_MAX_IDLE ="sso-max-inactive-seconds";
    private static final String SSO_REAP_INTERVAL ="sso-reap-interval-seconds";
    private static final String SSO_COOKIE_SECURE ="sso-cookie-secure";
    private static final String DISABLED = "disabled";
    private static final String OFF = "off";
    private static final String ON = "on";

    // ------------------------------------------------------------ Constructor

    /**
     * Default constructor that simply gets a handle to the web container
     * subsystem's logger.
     */
    public VirtualServer() {
        origPipeline = pipeline;
        vsPipeline = new VirtualServerPipeline(this);
        accessLogValve = new PEAccessLogValve();
        accessLogValve.setContainer(this);

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
    protected static final Logger _logger =
        LogDomains.getLogger(VirtualServer.class, LogDomains.WEB_LOGGER);

    /**
     * The resource bundle containing the message strings for _logger.
     */
    protected static final ResourceBundle rb = _logger.getResourceBundle();

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

    // The value of the ssoCookieSecure property
    private String ssoCookieSecure = null;

    private String defaultContextPath = null;


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
     * Gets the default-context.xml location of any web modules deployed
     * on this virtual server.
     *
     * @return default-context.xml location of any web modules deployed on
     * this virtual server
     */
    public String getDefaultContextXmlLocation() {
        return defaultContextXmlLocation;
    }

    /**
     * Sets the default-context.xml location for any web modules deployed
     * on this virtual server.
     *
     * @param defaultContextXmlLocation default-context.xml location for any
     * web modules deployed on this virtual server
     */
    public void setDefaultContextXmlLocation(String defaultContextXmlLocation) {
        this.defaultContextXmlLocation = defaultContextXmlLocation;
    }

    /**
     * Gets the default-web.xml location of any web modules deployed on this
     * virtual server.
     *
     * @return default-web.xml location of any web modules deployed on this
     * virtual server
     */
    public String getDefaultWebXmlLocation() {
        return defaultWebXmlLocation;
    }

    /**
     * Sets the default-web.xml location for any web modules deployed
     * on this virtual server.
     *
     * @param defaultWebXmlLocation default-web.xml location for any
     * web modules deployed on this virtual server
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
    public void setBean(
            com.sun.enterprise.config.serverbeans.VirtualServer vsBean){
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

    public String getInfo() {
        return _info;
    }

    public void setDefaultContextPath(String defaultContextPath) {
        this.defaultContextPath = defaultContextPath;
    }

    @Override
    public Container findChild(String contextRoot) {
        if (defaultContextPath != null && "/".equals(contextRoot)) {
            return super.findChild(defaultContextPath);
        } else {
            return super.findChild(contextRoot);
        }
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Configures the Secure attribute of the given SSO cookie.
     *
     * @param ssoCookie the SSO cookie to be configured
     * @param hreq the HttpServletRequest that has initiated the SSO session
     */
    @Override
    public void configureSingleSignOnCookieSecure(Cookie ssoCookie,
                                                  HttpServletRequest hreq) {
        super.configureSingleSignOnCookieSecure(ssoCookie, hreq);
        if (ssoCookieSecure != null &&
                !ssoCookieSecure.equals(SessionCookieConfig.DYNAMIC_SECURE)) {
            ssoCookie.setSecure(Boolean.parseBoolean(ssoCookieSecure));
        }
    }


    // ------------------------------------------------------ Lifecycle Methods

    /**
     * Adds the given valve to the currently active pipeline, keeping the
     * pipeline that is not currently active in sync.
     */
    public synchronized void addValve(GlassFishValve valve) {
        super.addValve(valve);
        if (pipeline == vsPipeline) {
            origPipeline.addValve(valve);
        } else {
            vsPipeline.addValve(valve);
        }
    }


    /**
     * Adds the given Tomcat-style valve to the currently active pipeline,
     * keeping the pipeline that is not currently active in sync.
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
    public synchronized void removeValve(GlassFishValve valve) {
        super.removeValve(valve);
        if (pipeline == vsPipeline) {
            origPipeline.removeValve(valve);
        } else {
            vsPipeline.removeValve(valve);
        }
    }

    // ------------------------------------------------------ Protected Methods

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
    protected String getDefaultContextPath(Domain domain,
            ApplicationRegistry appRegistry) {

        String contextRoot = null;
        String wmID = getDefaultWebModuleID();

        if (wmID != null) {
            // Check if the default-web-module is part of a
            // j2ee-application
            Applications appsBean = domain.getApplications();
            WebModuleConfig wmInfo = findWebModuleInJ2eeApp(appsBean, wmID,
                                                            appRegistry);
            if (wmInfo == null) {
                contextRoot = ConfigBeansUtilities.getContextRoot(wmID);
            } else {
                contextRoot = wmInfo.getContextPath();
            }

            if (contextRoot == null) {
                Object[] params = { wmID, getID() };
                _logger.log(Level.SEVERE, "vs.defaultWebModuleNotFound",
                            params);
            }
        }

        return contextRoot;
    }

    protected WebModuleConfig getDefaultWebModule(Domain domain,
            WebArchivist webArchivist, ApplicationRegistry appRegistry) {

        WebModuleConfig wmInfo = null;

        String wmID = getDefaultWebModuleID();
        if (wmID != null) {
            // Check if the default-web-module is part of a
            // j2ee-application
            Applications appsBean = domain.getApplications();
            wmInfo = findWebModuleInJ2eeApp(appsBean, wmID, appRegistry);
            if (wmInfo == null) {
                String contextRoot = ConfigBeansUtilities.getContextRoot(wmID);
                String location = ConfigBeansUtilities.getLocation(wmID);
                if (contextRoot!=null && location != null) {
                    File docroot = new File(location);
                    WebBundleDescriptor wbd = webArchivist.getDefaultWebXmlBundleDescriptor();
                    wmInfo = new WebModuleConfig();
                    wbd.setName(Constants.DEFAULT_WEB_MODULE_NAME);
                    wbd.setContextRoot(contextRoot);
                    wmInfo.setLocation(docroot);
                    wmInfo.setDescriptor(wbd);
                    wmInfo.setParentLoader(EmbeddedWebContainer.class.getClassLoader());
                    wmInfo.setAppClassLoader(new WebappClassLoader(wmInfo.getParentLoader()));
                }
            }

            if (wmInfo == null) {
                _logger.log(Level.SEVERE, "vs.defaultWebModuleNotFound",
                            new Object[] {wmID, getID()});
            }
        }

        return wmInfo;
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
    protected WebModuleConfig createSystemDefaultWebModuleIfNecessary(
            WebArchivist webArchivist) {

        WebModuleConfig wmInfo = null;

        // Add a default context only if one hasn't already been loaded
        // and then too only if docroot is not null
        //
        String docroot = getAppBase();
        if (getDefaultWebModuleID() == null && findChild("") == null
                && docroot != null) {

            WebBundleDescriptor wbd =
                webArchivist.getDefaultWebXmlBundleDescriptor();
            wmInfo = new WebModuleConfig();
            wbd.setModuleID(Constants.DEFAULT_WEB_MODULE_NAME);
            wbd.setContextRoot("");
            wmInfo.setLocation(new File(docroot));
            wmInfo.setDescriptor(wbd);
            wmInfo.setParentLoader(
                EmbeddedWebContainer.class.getClassLoader());
            WebappClassLoader loader = new WebappClassLoader(
                wmInfo.getParentLoader());
            loader.start();            
            wmInfo.setAppClassLoader(loader);
            if ( wbd.getApplication() == null ) {
                Application application = new Application(Globals.getDefaultHabitat());
                application.setVirtual(true);
                application.setName(Constants.DEFAULT_WEB_MODULE_NAME);
                wbd.setApplication(application);
            }
        }

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

        boolean active = vsID != null && vsID.length() > 0;
        active &= wm != null;

        if (active) {
            // Check if the web module is enabled
            active &= Boolean.valueOf(wm.getEnabled());

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
            if (getID().equals(org.glassfish.api.web.Constants.ADMIN_VS) &&
                        matchVSID && (vsIDs == null || vsIDs.length() == 0)) {
                return false;
            }


            if (matchVSID && vsIDs != null && vsIDs.length() > 0) {
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
     */
    protected String getDefaultWebModuleID() {
        String wmID = vsBean.getDefaultWebModule();
        if (wmID != null && wmID.equals("")) {
            wmID = null;
        }
        if (wmID != null && _debug) {
            Object[] params = { wmID, _id };
            _logger.log(Level.FINE, "vs.defaultWebModule", params);
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
            String id, ApplicationRegistry appRegistry) {

        WebModuleConfig wmInfo = null;

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

            com.sun.enterprise.config.serverbeans.Application appBean =
                appsBean.getModule(
                com.sun.enterprise.config.serverbeans.Application.class, appID);

            if ((appBean != null) && Boolean.valueOf(appBean.getEnabled())) {
                String location = appBean.getLocation();
                String moduleDir = DeploymentUtils.getRelativeEmbeddedModulePath(
                                                            location, moduleID);

                ApplicationInfo appInfo = appRegistry.get(appID);
                Application app = null;
                if (appInfo != null) {
                    app = appInfo.getMetaData(Application.class);
                } else {
                    // XXX ApplicaionInfo is NULL after restart
                    Object[] params = { id, getID() };
                    _logger.log(Level.SEVERE, "vs.defaultWebModuleDisabled",
                            params);
                    return wmInfo;
                }

                WebBundleDescriptor wbd = app.getWebBundleDescriptorByUri(moduleID);
                String webUri = wbd.getModuleDescriptor().getArchiveUri();
                String contextRoot = wbd.getModuleDescriptor().getContextRoot();
                if (moduleID.equals(webUri)) {
                    StringBuffer dir = new StringBuffer(location);
                    dir.append(File.separator);
                    dir.append(moduleDir);
                    File docroot = new File(dir.toString());
                    wmInfo = new WebModuleConfig();
                    wbd.setName(moduleID);
                    wbd.setContextRoot(contextRoot);
                    wmInfo.setDescriptor(wbd);
                    wmInfo.setLocation(docroot);
                    wmInfo.setParentLoader(EmbeddedWebContainer.class.getClassLoader());
                    wmInfo.setAppClassLoader(new WebappClassLoader(wmInfo.getParentLoader()));
                }
            } else {
                Object[] params = { id, getID() };
                _logger.log(Level.SEVERE, "vs.defaultWebModuleDisabled",
                            params);
            }
        }

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
        Server server = Globals.getDefaultHabitat().getComponent(Server.class);
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
     * @return The properties of this virtual server
     */
    List<Property> getProperties() {
        return vsBean.getProperty();
    }

    /**
     * Configures this virtual server.
     */
    public void configure(
                    String vsID,
                    com.sun.enterprise.config.serverbeans.VirtualServer vsBean,
                    String vsDocroot,
                    String vsLogFile,
                    MimeMap vsMimeMap,
                    String logServiceFile,
                    String logLevel,
                    FileLoggerHandler logHandler) {
        setDebug(debug);
        setAppBase(vsDocroot);
        setName(vsID);
        setID(vsID);
        setBean(vsBean);
        setMimeMap(vsMimeMap);

        String defaultContextXmlLocation = Constants.DEFAULT_CONTEXT_XML;
        String defaultWebXmlLocation = Constants.DEFAULT_WEB_XML;

        //Begin EE: 4920692 Make the default-web.xml be relocatable
        Property prop = vsBean.getProperty("default-web-xml");
        if (prop != null) {
            defaultWebXmlLocation = prop.getValue();
        }
        //End EE: 4920692 Make the default-web.xml be relocatable

        // allowLinking
        boolean allowLinking = false;
        prop = vsBean.getProperty("allowLinking");
        if (prop != null) {
            allowLinking = Boolean.parseBoolean(prop.getValue());
        }
        setAllowLinking(allowLinking);

        prop = vsBean.getProperty("contextXmlDefault");
        if (prop != null) {
            defaultContextXmlLocation = prop.getValue();
        }
        setDefaultWebXmlLocation(defaultWebXmlLocation);
        setDefaultContextXmlLocation(defaultContextXmlLocation);

        // Set vs state
        String state = vsBean.getState();
        if (state == null) {
            state = ON;
        }
        if (DISABLED.equalsIgnoreCase(state)) {
            setIsActive(false);
        } else {
            setIsActive(Boolean.parseBoolean(state));
        }

        if (vsLogFile != null && !vsLogFile.equals(logServiceFile)) {
            /*
             * Configure separate logger for this virtual server only if
             * 'log-file' attribute of this <virtual-server> and 'file'
             * attribute of <log-service> are different (See 6189219).
             */
            setLogFile(vsLogFile, logLevel, logHandler);
        }
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


    /*
     * Configures this virtual server with the specified log file.
     *
     * @param logFile The value of the virtual server's log-file attribute in
     * the domain.xml
     */
    void setLogFile(String logFile, String logLevel, FileLoggerHandler logHandler) {
   
        /** catalina file logger code
        String logPrefix = logFile;
        String logDir = null;
        String logSuffix = null;

        if (logPrefix == null || logPrefix.equals("")) {
            return;
        }

        int index = logPrefix.lastIndexOf(File.separatorChar);
        if (index != -1) {
            logDir = logPrefix.substring(0, index);
            logPrefix = logPrefix.substring(index+1);
        }

        index = logPrefix.indexOf('.');
        if (index != -1) {
            logSuffix = logPrefix.substring(index);
            logPrefix = logPrefix.substring(0, index);
        }

        logPrefix += "_";

        FileLogger contextLogger = new FileLogger();
        if (logDir != null) {
            contextLogger.setDirectory(logDir);
        }
        contextLogger.setPrefix(logPrefix);
        if (logSuffix != null) {
            contextLogger.setSuffix(logSuffix);
        }
        contextLogger.setTimestamp(true);
        contextLogger.setLevel(logLevel); 
         */
        
        logHandler.setLogFile(logFile);
        logHandler.setLevel(logLevel);
    }

    /**
     * Adds each host name from the 'hosts' attribute as an alias
     */
    void configureAliases() {
        List hosts = StringUtils.parseStringList(vsBean.getHosts(), ",");
        for (int i=0; i < hosts.size(); i++ ){
            String alias = hosts.get(i).toString();
            if ( !alias.equalsIgnoreCase("localhost") &&
                    !alias.equalsIgnoreCase("localhost.localdomain")){
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
        List<Property> properties = vsBean.getProperty();
        if (properties != null && properties.size() > 0) {
            for (Property p: properties) {
                if (p != null && "authRealm".equals(p.getName())) {
                    authRealmName = p.getValue();
                    if (authRealmName != null) {
                        AuthRealm realm = null;
                        List<AuthRealm> rs = securityService.getAuthRealm();
                        if (rs != null && rs.size() > 0) {
                            for (AuthRealm r : rs) {
                                if (r != null &&
                                        r.getName().equals(authRealmName)) {
                                    realm = r;
                                    break;
                                }
                            }
                        }

                        if (realm == null) {
                            _logger.log(Level.SEVERE, "vs.invalidAuthRealm",
                                new Object[] {getID(), authRealmName});
                        }
                    }
                    break;
                }
            }
        }
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
     * @param valveName The valve's fully qualified class nam
     */
    protected void addValve(String valveName) {
        Object valve = loadInstance(valveName);
        if (valve instanceof Valve) {
            addValve((Valve) valve);
        } else if (valve instanceof GlassFishValve) {
            addValve((GlassFishValve) valve);
        } else {
            _logger.log(Level.WARNING, "webcontainer.notAValve", valveName);
        }
    }

    /**
     * Adds the Catalina listener with the given class name to this
     * VirtualServer.
     *
     * @param listenerName The fully qualified class name of the listener
     */
    protected void addListener(String listenerName) {
        Object listener = loadInstance(listenerName);

        if ( listener == null ) return;

        if (listener instanceof ContainerListener) {
            addContainerListener((ContainerListener)listener);
        } else if (listener instanceof LifecycleListener){
            addLifecycleListener((LifecycleListener)listener);
        } else {
            _logger.log(Level.SEVERE, "vs.invalidListener",
                new Object[] {listenerName, getID()});
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

    /**
     * Configures this VirtualServer with its send-error properties.
     */
    void configureErrorPage() {

        ErrorPage errorPage = null;

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
                            new Object[] { propValue, getID(), "path" });
                    }
                    path = errorParams[j].substring("path=".length());
                }

                if (errorParams[j].startsWith("reason=")) {
                    if (reason != null) {
                        _logger.log(Level.WARNING,
                            "webcontainer.sendErrorMultipleElement",
                            new Object[] { propValue, getID(), "reason" });
                    }
                    reason = errorParams[j].substring("reason=".length());
                }

                if (errorParams[j].startsWith("code=")) {
                    if (status != null) {
                        _logger.log(Level.WARNING,
                            "webcontainer.sendErrorMultipleElement",
                            new Object[] { propValue, getID(), "code" });
                    }
                    status = errorParams[j].substring("code=".length());
                }
            }

            if (path == null || path.length() == 0) {
                _logger.log(Level.WARNING,
                    "webcontainer.sendErrorMissingPath",
                    new Object[] { propValue, getID() });
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
                            new Object[] { propValue, getID(), "from" });
                    }
                    from = redirectParams[j].substring("from=".length());
                }

                if (redirectParams[j].startsWith("url=")) {
                    if (url != null) {
                        _logger.log(Level.WARNING,
                            "webcontainer.redirectMultipleElement",
                            new Object[] { propValue, getID(), "url" });
                    }
                    url = redirectParams[j].substring("url=".length());
                }

                if (redirectParams[j].startsWith("url-prefix=")) {
                    if (urlPrefix != null) {
                        _logger.log(Level.WARNING,
                            "webcontainer.redirectMultipleElement",
                            new Object[] { propValue, getID(), "url-prefix" });
                    }
                    urlPrefix = redirectParams[j].substring(
                                                    "url-prefix=".length());
                }

                if (redirectParams[j].startsWith("escape=")) {
                    if (escape != null) {
                        _logger.log(Level.WARNING,
                            "webcontainer.redirectMultipleElement",
                            new Object[] { propValue, getID(), "escape" });
                    }
                    escape = redirectParams[j].substring("escape=".length());
                }
            }

            if (from == null || from.length() == 0) {
                _logger.log(Level.WARNING,
                        "webcontainer.redirectMissingFrom",
                        new Object[] { propValue, getID() });
            }

            // Either url or url-prefix (but not both!) must be present
            if ((url == null || url.length() == 0)
                    && (urlPrefix == null || urlPrefix.length() == 0)) {
                _logger.log(Level.WARNING,
                        "webcontainer.redirectMissingUrlOrUrlPrefix",
                        new Object[] { propValue, getID() });
            }
            if (url != null && url.length() > 0
                    && urlPrefix != null && urlPrefix.length() > 0) {
                _logger.log(Level.WARNING,
                    "webcontainer.redirectBothUrlAndUrlPrefix",
                    new Object[] { propValue, getID() });
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
                        new Object[] { propValue, getID() });
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
    void configureSingleSignOn(boolean globalSSOEnabled,
            WebContainerFeatureFactory webContainerFeatureFactory) {

        if (!isSSOEnabled(globalSSOEnabled)) {
            /*
             * Disable SSO
             */
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Disabling Single Sign On (SSO) " +
                    "for virtual server " + getID() +
                    ", as configured");
            }

            // Remove existing SSO valve (if any)
            GlassFishValve[] valves = getValves();
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
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Enabling Single Sign On (SSO) " +
                    "for virtual server " + getID() +
                    ", as configured");
            }

            SSOFactory ssoFactory =
                webContainerFeatureFactory.getSSOFactory();
            GlassFishSingleSignOn sso =
                ssoFactory.createSingleSignOnValve(getName());

            // set max idle time if given
            Property idle = vsBean.getProperty(SSO_MAX_IDLE);
            if (idle != null && idle.getValue() != null) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("SSO entry max idle time set to: " +
                                 idle.getValue() + " for virtual server " +
                                 getID());
                }
                sso.setMaxInactive(Integer.parseInt(idle.getValue()));
            }

            // set expirer thread sleep time if given
            Property expireTime = vsBean.getProperty(SSO_REAP_INTERVAL);
            if (expireTime !=null && expireTime.getValue() != null) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("SSO expire thread interval set to: " +
                                 expireTime.getValue() +
                                 " for virtual server " + getID());
                }
                sso.setReapInterval(Integer.parseInt(expireTime.getValue()));
            }

            // Remove existing SSO valve (if any), in case of a reconfig
            GlassFishValve[] valves = getValves();
            for (int i=0; valves!=null && i<valves.length; i++) {
                if (valves[i] instanceof SingleSignOn) {
                    removeValve(valves[i]);
                    break;
                }
            }

            addValve((GlassFishValve) sso);
            configureSingleSignOnCookieSecure();
        }
    }

    /**
     * Configures this VirtualServer with its state (on | off | disabled).
     */
    void configureState(){
        String stateValue = vsBean.getState();
        if (!stateValue.equalsIgnoreCase(ON) &&
                getName().equalsIgnoreCase(
                    org.glassfish.api.web.Constants.ADMIN_VS)){
            throw new IllegalArgumentException(
                "virtual-server " + org.glassfish.api.web.Constants.ADMIN_VS +
                " state property cannot be modified");
        }

        if (stateValue.equalsIgnoreCase(DISABLED)) {
            // state="disabled"
            setIsDisabled(true);
        } else if (!ConfigBeansUtilities.toBoolean(stateValue)) {
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
        Property allow = vsBean.getProperty("allowRemoteAddress");
        Property deny = vsBean.getProperty("denyRemoteAddress");
        if (allow != null && allow.getValue() != null
                || deny != null && deny.getValue() != null)  {
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
            GlassFishValve[] valves = getValves();
            for (int i=0; valves!=null && i<valves.length; i++) {
                if (valves[i] instanceof RemoteAddrValve) {
                    removeValve(valves[i]);
                    break;
                }
            }
            addValve((GlassFishValve) remoteAddrValve);
        }
    }

    /**
     * Configures the Remote Host Filter valve of this VirtualServer.
     *
     * This valve enforces request acceptance/denial based on the name of the
     * remote host from where the request originated.
     */
    void configureRemoteHostFilterValve() {
        RemoteHostValve remoteHostValve = null;
        Property allow = vsBean.getProperty("allowRemoteHost");
        Property deny = vsBean.getProperty("denyRemoteHost");
        if (allow != null && allow.getValue() != null
                || deny != null && deny.getValue() != null)  {
            remoteHostValve = new RemoteHostValve();
        }
        if (allow != null && allow.getValue() != null) {
            _logger.fine("Allowing access to " + getID() + " from " +
                allow.getValue());
            remoteHostValve.setAllow(allow.getValue());
        }
        if (deny != null && deny.getValue() != null) {
            _logger.fine("Denying access to " + getID() + " from " +
                deny.getValue());
            remoteHostValve.setDeny(deny.getValue());
        }
        if (remoteHostValve != null) {
            // Remove existing RemoteHostValve (if any), in case of a reconfig
            GlassFishValve[] valves = getValves();
            for (int i=0; valves!=null && i<valves.length; i++) {
                if (valves[i] instanceof RemoteHostValve) {
                    removeValve(valves[i]);
                    break;
                }
            }
            addValve((GlassFishValve) remoteHostValve);
        }
    }

    /**
     * Reconfigures the access log of this VirtualServer with its
     * updated access log related properties.
     */
    void reconfigureAccessLog(String globalAccessLogBufferSize,
                              String globalAccessLogWriteInterval,
                              Habitat habitat,
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
            addValve((GlassFishValve) accessLogValve);
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
            GlassFishValve[] valves = p.getValves();
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
            List<String> values = StringUtils.parseStringList(cacheControl, ",");
            if (values != null && !values.isEmpty()) {
                setCacheControls(values.toArray(new String[values.size()]));
            }
        }
    }

    /**
     * Checks if SSO is enabled for this VirtualServer.
     *
     * @return The value of the sso-enabled property for this VirtualServer
     */
    private boolean isSSOEnabled(boolean globalSSOEnabled) {
        String ssoEnabled = vsBean.getSsoEnabled();
        return "inherit".equals(ssoEnabled) && globalSSOEnabled
            || ConfigBeansUtilities.toBoolean(ssoEnabled); 
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
        String enabled = vsBean.getAccessLoggingEnabled();
        return "inherit".equals(enabled) && globalAccessLoggingEnabled ||
            ConfigBeansUtilities.toBoolean(enabled);
    }

    @Override
    public void setRealm(Realm realm) {
        if ((realm != null) && !(realm instanceof RealmAdapter)) {
            _logger.log(Level.SEVERE, "vs.ignoreInvalidRealm",
                    new Object[] { realm.getClass().getName(),
                        RealmAdapter.class.getName()});
        } else {
            super.setRealm(realm);
        }
    }

    /**
     * Configures the security level of the SSO cookie for this virtual
     * server, based on the value of its sso-cookie-secure attribute
     */
    private void configureSingleSignOnCookieSecure() {
        String cookieSecure = vsBean.getSsoCookieSecure();
        if (!"true".equalsIgnoreCase(cookieSecure) &&
                !"false".equalsIgnoreCase(cookieSecure) &&
                !cookieSecure.equalsIgnoreCase(
                    SessionCookieConfig.DYNAMIC_SECURE)) {
            _logger.log(Level.WARNING, "vs.invalidSsoCookieSecure",
                        new Object[] {cookieSecure, getID()});
        } else {
            ssoCookieSecure = cookieSecure;
        }
    }

    /**
     * Configures the error report valve of this VirtualServer.
     *
     * <p>The error report valve of a virtual server is specified through
     * a property with name <i>errorReportValve</i>, whose value is the
     * valve's fully qualified classname. A null or empty classname
     * disables the error report valve and therefore the container's
     * default error page mechanism for error responses.
     */
    void configureErrorReportValve() {
        Property prop = vsBean.getProperty(Constants.ERROR_REPORT_VALVE);
        if (prop != null) {
            setErrorReportValveClass(prop.getValue());
        }
    }
}
