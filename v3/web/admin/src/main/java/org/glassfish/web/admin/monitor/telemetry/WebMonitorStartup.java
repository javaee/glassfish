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
 * language governing permissions and limitations udfnder the License.
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
package org.glassfish.web.admin.monitor.telemetry;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;
/*
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.api.Startup;
 */
import org.glassfish.api.Startup.Lifecycle;
import org.glassfish.api.monitoring.TelemetryProvider;
import org.glassfish.flashlight.provider.ProbeProviderListener;
import org.glassfish.flashlight.provider.ProbeProviderEventManager;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import com.sun.enterprise.config.serverbeans.*;
import java.util.List;
import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.ConfigListener;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.glassfish.internal.api.Globals;

/**
 * Provides for bootstarpping web telemetry during startup and
 * when the monitoring level config changes
 *
 * @author Sreenivas Munnangi
 */
@Service
public class WebMonitorStartup implements //Startup, PostConstruct, 
        ProbeProviderListener, TelemetryProvider {
    private boolean requestProviderRegistered = false;
    private boolean servletProviderRegistered = false;
    private boolean jspProviderRegistered = false;
    private boolean sessionProviderRegistered = false;
    private boolean probeProviderListenerRegistered = false;
    
    // default values are set to true which can be changed thru 
    // GUI or set command
    private boolean threadpoolMonitoringEnabled = true;
    private boolean httpServiceMonitoringEnabled = true;
    private boolean jvmMonitoringEnabled = true;
    private boolean webMonitoringEnabled = true;
    private boolean isWebTreeBuilt = false;

    //@Inject
    private ProbeProviderEventManager ppem = 
            Globals.getDefaultHabitat().getComponent(ProbeProviderEventManager.class);
    //@Inject
    private ProbeClientMediator pcm = 
            Globals.getDefaultHabitat().getComponent(ProbeClientMediator.class);;

    //@Inject
    private ModuleMonitoringLevels mml =
            Globals.getDefaultHabitat().getComponent(ModuleMonitoringLevels.class);

    private boolean grizzlyModuleLoaded = false;
    private boolean webContainerLoaded = false;
    
    //@Inject
    Logger logger = Globals.getDefaultHabitat().getComponent(Logger.class);

    private WebRequestTelemetry webRequestTM = null;
    private WebModuleTelemetry moduleTM = null;
    private SessionStatsTelemetry webSessionsTM = null;
    private List<SessionStatsTelemetry> vsSessionTMs = null;
    private ServletStatsTelemetry webServletsTM = null;
    private List<ServletStatsTelemetry> vsServletTMs = null;
    private JspStatsTelemetry webJspTM = null;
    private List<JspStatsTelemetry> vsJspTMs = null;
    private ThreadPoolTelemetry threadPoolTM = null;
    private JVMMemoryStatsTelemetry jvmMemoryTM = null;
    
    //@Inject
    private static Domain domain = Globals.getDefaultHabitat().getComponent(Domain.class);
    //@Inject
    private MonitoringRuntimeDataRegistry mrdr = 
            Globals.getDefaultHabitat().getComponent(MonitoringRuntimeDataRegistry.class);
    private TreeNode serverNode;
    private TreeNode httpServiceNode;
    private TreeNode webNode;
    private TreeNode webSessionNode;
    private TreeNode webServletNode;
    private TreeNode webJspNode;
    private TreeNode webRequestNode;
    
    private Level dbgLevel = Level.FINEST;
    private Level defaultLevel;


    protected static class logger {
        public void finest(String str) {
            System.out.println(str);
        }
    }

    public void onLevelChange(String newLevel) {

        // TODO : Check that new level is ON

        // to set log level, uncomment the following 
        // remember to comment it before checkin
        // remove this once we find a proper solution
        defaultLevel = logger.getLevel();
        /*
        if ((defaultLevel == null) || (dbgLevel.intValue() < defaultLevel.intValue())) {
            logger.setLevel(dbgLevel);
        }
         */
        logger.finest("[Monitor]In the Web Monitor startup ************");

        logger.finest("[Monitor]BootstrapAdminMonitor started");

        //Load monitoring levels
        loadMonitoringLevels();

        //Build the top level monitoring tree
        buildTopLevelMonitoringTree();

        //check if the monitoring level for JVM is 'on' and enable appropriately
        buildJVMMonitoringTree();

        //check if the monitoring level for Threadpool is 'on' and 
        // if Grizzly module is loaded, then register the ProveProviderListener
        if (threadpoolMonitoringEnabled ) {
            registerProbeProviderListener();
        }

        //check if the monitoring level for web-container is 'on' and 
        // if Web Container is loaded, then register the ProveProviderListener
        if (!probeProviderListenerRegistered && webMonitoringEnabled) {
            registerProbeProviderListener();
        }

        //ProbeProviderListener already registered (as part of other container),
        // see if the providers are already registered
        buildWebMonitoringTree();
        if (!isWebTreeBuilt) {
            logger.finest("[Monitor]Web Monitoring tree is not built for some reason though the Web Container started event is fired");
            return;
        }
        if (sessionProviderRegistered)
            buildSessionTelemetry();
        if (jspProviderRegistered)
            buildJspTelemetry();
        if (servletProviderRegistered)
            buildServletTelemetry();
        if (requestProviderRegistered)
            buildWebRequestTelemetry();
    }

public Lifecycle getLifecycle() {
        // This service stays running for the life of the app server, hence SERVER.
        return Lifecycle.SERVER;
    }

    public void providerRegistered(String moduleName, String providerName, String appName) {
        try {
            
            logger.finest("[Monitor]Provider registered event received - providerName = " + 
                                providerName + " : module name = " + moduleName + 
                                " : appName = " + appName);
            if (providerName.equals("session")){
                logger.finest("[Monitor]and it is Web session");
                buildWebMonitoringTree();
                if (!isWebTreeBuilt){
                    logger.finest("[Monitor]Web Monitoring tree is not built for some reason though the provider registered event is fired");
                    sessionProviderRegistered = true;
                    return;
                }
                buildSessionTelemetry();
            }
            if (providerName.equals("servlet")){
                logger.finest("[Monitor]and it is Web servlet");
                buildWebMonitoringTree();
                if (!isWebTreeBuilt){
                    logger.finest("[Monitor]Web Monitoring tree is not built for some reason though the provider registered event is fired");
                    servletProviderRegistered = true;
                    return;
                }
                buildServletTelemetry();
            }

            if (providerName.equals("jsp")){
                logger.finest("[Monitor]and it is Web jsp");
                buildWebMonitoringTree();
                if (!isWebTreeBuilt){
                    logger.finest("[Monitor]Web Monitoring tree is not built for some reason though the provider registered event is fired");
                    jspProviderRegistered = true;
                    return;
                }
                buildJspTelemetry();
            }
            /* Need to fix module, decide where it should go
            if (providerName.equals("webmodule")){
                logger.finest("and it is Web Module");
                if (moduleTM == null) {
                    moduleTM = new WebModuleTelemetry(serverNode, webMonitoringEnabled, logger);
                    Collection<ProbeClientMethodHandle> handles = pcm.registerListener(moduleTM);
                    moduleTM.setProbeListenerHandles(handles);
                }
            }
            */
            if (providerName.equals("request")){
                logger.finest("[Monitor]and it is Web request");
                buildWebMonitoringTree();
                if (!isWebTreeBuilt){
                    logger.finest("[Monitor]Web Monitoring tree is not built for some reason though the provider registered event is fired");
                    requestProviderRegistered = true;
                    return;
                }
                buildWebRequestTelemetry();
            }
            if (providerName.equals("threadpool")){
                logger.finest("[Monitor]and it is threadpool");
                if (threadPoolTM == null) {
                    buildThreadpoolMonitoringTree();
                }
            }
             //Decide now if I need to enable or disable the nodes (for first time use)
        }catch (Exception e) {
            //Never throw an exception as the Web container startup will have a problem
            //Show warning
            logger.finest("[Monitor]WARNING: Exception in WebMonitorStartup : " + 
                                    e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void providerUnregistered(String moduleName, String providerName, String appName) {
        
    }

    private void loadMonitoringLevels() {
        if (mml != null) {
            if ("OFF".equals(mml.getWebContainer())){
                webMonitoringEnabled = false;
                logger.finest("[Monitor]Disabling webContainer");
            } else {
                webMonitoringEnabled = true;
            }
            if ("OFF".equals(mml.getJvm())){
                jvmMonitoringEnabled = false;
                logger.finest("[Monitor]Disabling jvmMonitoring");
            } else {
                jvmMonitoringEnabled = true;
            }
            if ("OFF".equals(mml.getHttpService())){
                httpServiceMonitoringEnabled = false;
                logger.finest("[Monitor]Disabling httpServiceMonitoring");
            } else {
                httpServiceMonitoringEnabled = true;
            }
            if ("OFF".equals(mml.getThreadPool())){
                threadpoolMonitoringEnabled = false;
                logger.finest("[Monitor]Disabling threadpoolMonitoring");
            } else {
                threadpoolMonitoringEnabled = true;
            }
            logger.finest("[Monitor]mml.getWebContainer() = " + mml.getWebContainer());
        }
    }

    //builds the top level tree
    private void buildTopLevelMonitoringTree() {
        // server
        Server srvr = null;
        List<Server> ls = domain.getServers().getServer();
        for (Server sr : ls) {
            if ("server".equals(sr.getName())) {
                srvr = sr;
                break;
            }
        }
        serverNode = TreeNodeFactory.createTreeNode("server", null, "server");
        mrdr.add("server", serverNode);
    }

    private void registerProbeProviderListener() {
       // ppem.registerProbeProviderListener should be called only after
        // buildWebMonitoringConfigTree is invoked because of dependency???
        ppem.registerProbeProviderListener(this);
        probeProviderListenerRegistered = true;
    }

    private void buildJspTelemetry() {
        if (webJspTM == null) {
            webJspTM = new JspStatsTelemetry(webJspNode, null, null, webMonitoringEnabled, logger);
            Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webJspTM);
            webJspTM.setProbeListenerHandles(handles);
        } else { //Make sure you turn them on
            if (!webJspTM.isEnabled())
                webJspTM.enableMonitoring(true);
        }
        if (vsJspTMs == null) {
            vsJspTMs = new ArrayList<JspStatsTelemetry>();
            TreeNode appsNode = serverNode.getNode("applications");
            Collection<TreeNode> appNodes = appsNode.getChildNodes();
            for (TreeNode appNode : appNodes){
                //Get all virtual servers
                Collection<TreeNode> vsNodes = appNode.getChildNodes();
                for (TreeNode vsNode : vsNodes) {
                    //Create sessionTM for each vsNode
                    JspStatsTelemetry vsJspTM = 
                            new JspStatsTelemetry(vsNode, 
                                appNode.getName(), vsNode.getName(), 
                                webMonitoringEnabled, logger);
                    vsJspTMs.add(vsJspTM);

                }
            }
        }else { //Make sure you turn them on
            for (JspStatsTelemetry jspTM : vsJspTMs) {
                if (!jspTM.isEnabled())
                    jspTM.enableMonitoring(true);
            }
        }
    }

    private void buildServletTelemetry() {
        if (webServletsTM == null) {
            webServletsTM = new ServletStatsTelemetry(webServletNode, null, 
                                    null, webMonitoringEnabled, logger);
            Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webServletsTM);
            webServletsTM.setProbeListenerHandles(handles);
        } else { //Make sure you turn them on
            if (webServletsTM.isEnabled())
                webServletsTM.enableMonitoring(true);
        }
        if (vsServletTMs == null) {
            vsServletTMs = new ArrayList<ServletStatsTelemetry>();
            TreeNode appsNode = serverNode.getNode("applications");
            Collection<TreeNode> appNodes = appsNode.getChildNodes();
            for (TreeNode appNode : appNodes){
                //Get all virtual servers
                Collection<TreeNode> vsNodes = appNode.getChildNodes();
                for (TreeNode vsNode : vsNodes) {
                    //Create sessionTM for each vsNode
                    ServletStatsTelemetry vsServletTM = 
                            new ServletStatsTelemetry(vsNode, 
                                appNode.getName(), vsNode.getName(), 
                                webMonitoringEnabled, logger);
                    vsServletTMs.add(vsServletTM);

                }
            }
        } else { //Make sure you turn them on
            for (ServletStatsTelemetry servletTM : vsServletTMs) {
                if (!servletTM.isEnabled())
                    servletTM.enableMonitoring(true);
            }
        }
    }

    private void buildSessionTelemetry() {
        if (webSessionsTM == null) {
            webSessionsTM = new SessionStatsTelemetry(webSessionNode, 
                            null, null, webMonitoringEnabled, logger);
            Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webSessionsTM);
            webSessionsTM.setProbeListenerHandles(handles);
        } else { //Make sure you turn them on
            if (!webSessionsTM.isEnabled())
                webSessionsTM.enableMonitoring(true);
        }
        if (vsSessionTMs == null) {
            vsSessionTMs = new ArrayList<SessionStatsTelemetry>();
            TreeNode appsNode = serverNode.getNode("applications");
            Collection<TreeNode> appNodes = appsNode.getChildNodes();
            for (TreeNode appNode : appNodes){
                //Get all virtual servers
                Collection<TreeNode> vsNodes = appNode.getChildNodes();
                for (TreeNode vsNode : vsNodes) {
                    //Create sessionTM for each vsNode
                    SessionStatsTelemetry vsSessionTM = 
                            new SessionStatsTelemetry(vsNode, 
                                appNode.getName(), vsNode.getName(), 
                                webMonitoringEnabled, logger);
                    vsSessionTMs.add(vsSessionTM);

                }
            }
        } else { //Make sure you turn them on
            for (SessionStatsTelemetry sessionTM : vsSessionTMs) {
                if (!sessionTM.isEnabled())
                    sessionTM.enableMonitoring(true);
            }
        }
    }

    private void buildWebRequestTelemetry() {
        if (webRequestTM == null) {
            webRequestTM = new WebRequestTelemetry(webRequestNode, webMonitoringEnabled, logger);
            Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webRequestTM);
            webRequestTM.setProbeListenerHandles(handles);
        } else { // Make sure you turn it on
            if (!webRequestTM.isEnabled())
                webRequestTM.enableMonitoring(true);
        }
    }

    /**
     * Handle config changes for monitoring levels
     * Add code for handling deployment changes like deploy/undeploy
     */
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        for (PropertyChangeEvent event : events) {
            String propName = event.getPropertyName();
            String mLevel = null;
            if ("http-service".equals(propName)) {
                mml = (ModuleMonitoringLevels) event.getSource();
                mLevel = event.getNewValue().toString();
                logger.finest("[TM] Config change event - propName = " + propName + " : enabled=" + httpServiceMonitoringEnabled);
                if (httpServiceMonitoringEnabled == getEnabledValue(mLevel)) {
                    //Maintain status QUO
                    return null;
                } else {
                    httpServiceMonitoringEnabled = getEnabledValue(mLevel);
                    /* TODO
                    if (httpServiceMonitoringEnabled) {//Turned from OFF to ON
                        this.buildHTTPServiceMonitoringTree();
                    } else { //Turned from ON to OFF
                        if (this.httpServiceTM.isEnabled())
                            httpServiceTM.enableMonitoring(false);
                    }
                     */
                }
            } else if ("jvm".equals(propName)) {
                mml = (ModuleMonitoringLevels) event.getSource();
                mLevel = event.getNewValue().toString();
                if (jvmMonitoringEnabled == getEnabledValue(mLevel)) {
                    //Maintain status QUO
                    return null;
                } else {
                    jvmMonitoringEnabled = getEnabledValue(mLevel);
                    if (jvmMonitoringEnabled) {//Turned from OFF to ON
                        this.buildJVMMonitoringTree();
                    } else { //Turned from ON to OFF
                        if (jvmMemoryTM.isEnabled())
                            jvmMemoryTM.enableMonitoring(false);
                    }
                }
                logger.finest("[TM] Config change event - propName = " + propName + " : enabled=" + jvmMonitoringEnabled);
                // set corresponding tree node enabled flag 
                // handle proble listener event by registering/unregistering
                //jvmMemoryTM.enableMonitoring(enabled);
            } else if ("thread-pool".equals(propName)) {
                mml = (ModuleMonitoringLevels) event.getSource();
                mLevel = event.getNewValue().toString();
                logger.finest("[TM] Config change event - propName = " + propName + " : enabled=" + threadpoolMonitoringEnabled);
                if (threadpoolMonitoringEnabled == getEnabledValue(mLevel)) {
                    //Maintain status QUO
                    return null;
                } else {
                    threadpoolMonitoringEnabled = getEnabledValue(mLevel);
                    if (threadpoolMonitoringEnabled) {//Turned from OFF to ON
                        buildThreadpoolMonitoringTree();
                    } else { //Turned from ON to OFF
                        if (threadPoolTM.isEnabled())
                            threadPoolTM.enableMonitoring(false);
                    }
                }
                // set corresponding tree node enabled flag 
                // handle proble listener event by registering/unregistering
                //threadpoolTM.enableMonitoring(enabled);
            } else if ("web-container".equals(propName)) {
                mml = (ModuleMonitoringLevels) event.getSource();
                mLevel = event.getNewValue().toString();
                if (webMonitoringEnabled == getEnabledValue(mLevel)) {
                    //Maintain status QUO
                    return null;
                } else {
                    webMonitoringEnabled = getEnabledValue(mLevel);
                    if (webMonitoringEnabled) { //Turned from OFF to ON
                        buildWebMonitoringTree();
                        if (!isWebTreeBuilt){
                            logger.finest("[Monitor]Web Monitoring tree is not built for some reason though the Web Container started event is fired");
                            return null;
                        }
                        if (sessionProviderRegistered)
                            buildSessionTelemetry();
                        if (jspProviderRegistered)
                            buildJspTelemetry();
                        if (servletProviderRegistered)
                            buildServletTelemetry();
                        if (requestProviderRegistered)
                            buildWebRequestTelemetry();
                    } else { //Turned from ON to OFF
                        disableWebMonitoring();
                    }
                        
                }
                logger.finest("[TM] Config change event - propName = " + propName + " : enabled=" + webMonitoringEnabled);
            }
        }
        return null;
    }

    private void buildJVMMonitoringTree() {
        //Construct the JVMMemoryStatsTelemetry
        if (jvmMonitoringEnabled)
            jvmMemoryTM = new JVMMemoryStatsTelemetry(serverNode, 
                                        jvmMonitoringEnabled, logger);
    }

    private void buildThreadpoolMonitoringTree() {
        //Construct the ThreadpoolStatsTelemetry
        if (threadpoolMonitoringEnabled && grizzlyModuleLoaded){
            if (threadPoolTM == null) {
                // Where do I add this? Looks like the thread pools are already created.
                // Now I need to register the listeners, but which one to register?
                threadPoolTM = new ThreadPoolTelemetry(httpServiceNode, threadpoolMonitoringEnabled, logger);
                Collection<ProbeClientMethodHandle> handles = pcm.registerListener(threadPoolTM);
                threadPoolTM.setProbeListenerHandles(handles);
            } else { //Make sure you turn them ON
                if (!threadPoolTM.isEnabled())
                    threadPoolTM.enableMonitoring(true);
            }
            
        }
    }

    private void disableWebMonitoring() {
        //Disable session telemetry
        if (webSessionsTM.isEnabled())
            webSessionsTM.enableMonitoring(false);
        for (SessionStatsTelemetry sessionTM : vsSessionTMs) {
            if (sessionTM.isEnabled())
                sessionTM.enableMonitoring(false);
        }
        
        //Disable Servlet telemetry
        if (webServletsTM.isEnabled())
            webServletsTM.enableMonitoring(false);
        for (ServletStatsTelemetry servletTM : vsServletTMs) {
            if (servletTM.isEnabled())
                servletTM.enableMonitoring(false);
        }
        
        //Disable JSP telemetry
        if (webJspTM.isEnabled())
            webJspTM.enableMonitoring(false);
        for (JspStatsTelemetry jspTM : vsJspTMs) {
            if (jspTM.isEnabled())
                jspTM.enableMonitoring(false);
        }
        
        //Disable Request telemetry
        if (webRequestTM.isEnabled())
            webRequestTM.enableMonitoring(false);
    }

    /**
     * Tree Structure, names in () are dynamic nodes
     *
     * Server
     * | web
     * | applications
     * | | web-module
     * | | application
     * | | j2ee-application
     * | | | (app-name)
     * | | | | (virtual-server)
     * | | | | | (servlet)
     **/
    private void buildWebMonitoringTree() {
        if (isWebTreeBuilt || !webMonitoringEnabled || !webContainerLoaded)
            return;
        Server srvr = null;
        List<Server> ls = domain.getServers().getServer();
        for (Server sr : ls) {
            if ("server".equals(sr.getName())) {
                srvr = sr;
                break;
            }
        }
        
        // web
        webNode = TreeNodeFactory.createTreeNode("web", null, "web");
        serverNode.addChild(webNode);
        //web->session
        webSessionNode = TreeNodeFactory.createTreeNode("session", null, "web");
        webNode.addChild(webSessionNode);
        
        //web->servlet
        webServletNode = TreeNodeFactory.createTreeNode("servlet", null, "web");
        webNode.addChild(webServletNode);
        //web->jsp
        webJspNode = TreeNodeFactory.createTreeNode("jsp", null, "web");
        webNode.addChild(webJspNode);
        //web->request
        webRequestNode = TreeNodeFactory.createTreeNode("request", this, "web");
        webNode.addChild(webRequestNode);
        
        // applications
        TreeNode applications = TreeNodeFactory.createTreeNode("applications", null, "web");
        serverNode.addChild(applications);
        // application
        List<Application> la = domain.getApplications().getModules(Application.class);
        for (Application sapp : la) {
            TreeNode app = TreeNodeFactory.createTreeNode(sapp.getName(), null, "web");
            applications.addChild(app);
            addVirtualServers(srvr, app, sapp.getName());
        }
        // j2ee application
        List<J2eeApplication> lja = domain.getApplications().getModules(J2eeApplication.class);
        for (J2eeApplication japp : lja) {
            TreeNode app = TreeNodeFactory.createTreeNode(japp.getName(), null, "web");
            applications.addChild(app);
        }
        // web modules
        List<WebModule> lm = domain.getApplications().getModules(WebModule.class);
        for (WebModule wm : lm) {
            TreeNode app = TreeNodeFactory.createTreeNode(wm.getName(), null, "web");
            applications.addChild(app);
            addVirtualServers(srvr, app, wm.getName());
        }
        isWebTreeBuilt = true;
    }
    
    private boolean getEnabledValue(String enabledStr) {
        if ("OFF".equals(enabledStr)) {
            return false;
        }
        return true;
    }


    private void addVirtualServers(Server server, TreeNode tn, String appName) {
        // get the applications refs for the server
        for (ApplicationRef ar : server.getApplicationRef()) {
            if (appName.equals(ar.getRef())) {
                String vsL = ar.getVirtualServers();
                if (vsL != null) {
                    for (String str : vsL.split(",")) {
                        TreeNode vs = TreeNodeFactory.createTreeNode(str, null, "web");
                        tn.addChild(vs);
                    }
                } else {
                    //Fix it!
                    TreeNode vs = TreeNodeFactory.createTreeNode("server", null, "web");
                    tn.addChild(vs);
                }
                return;
            }
        }
    }

    public static String getAppName(String contextRoot) {
        // first check in web modules
        List<WebModule> lm = domain.getApplications().getModules(WebModule.class);
        for (WebModule wm : lm) {
            if (contextRoot.equals(wm.getContextRoot())) {
                return (wm.getName());
            }
        }
        // then check under applications (introduced in V3 not j2ee app)
        List<Application> la = domain.getApplications().getModules(Application.class);
        for (Application sapp : la) {
            if (contextRoot.equals(sapp.getContextRoot())) {
                return (sapp.getName());
            }
        }
        return null;
    }

    /**
     * Tree Structure, names in () are dynamic nodes
     *
     * Server
     * | applications
     * | | web-module
     * | | application
     * | | j2ee-application
     * | | | (app-name)
     * | | | | (virtual-server)
     * | | | | | (servlet)
     * | web-container
     * | http-service
     * | | http-listener
     * | | connection-pool
     * | | (server-name)
     * | | | (request)
     * | thread-pools
     * | | (thread-pool)
     */
    /*
    private void buildWebMonitoringConfigTree() {

        // get server-config
        Config sConfig = null;
        List<Config> lcfg = domain.getConfigs().getConfig();
        for (Config cfg : lcfg) {
            if ("server-config".equals(cfg.getName())) {
                sConfig = cfg;
                break;
            }
        }
        // http-service
        HttpService httpS = sConfig.getHttpService();
        httpServiceNode = TreeNodeFactory.createTreeNode("http-service", null, "web");
        serverNode.addChild(httpServiceNode);
        // virtual server(s)
        for (VirtualServer vServer : httpS.getVirtualServer()) {
            TreeNode vs = TreeNodeFactory.createTreeNode(vServer.getId(), null, "web");
            httpServiceNode.addChild(vs);
            // http-listener(s)
            String httpL = vServer.getHttpListeners();
            if (httpL != null) {
                for (String str : httpL.split(",")) {
                    TreeNode httpListener = TreeNodeFactory.createTreeNode(str, null, "web");
                    vs.addChild(httpListener);
                }
            }
        }
        // http-listener
        for (HttpListener htl : httpS.getHttpListener()) {
            TreeNode httpListener = TreeNodeFactory.createTreeNode(htl.getId(), null, "web");
            httpServiceNode.addChild(httpListener);
        }
        // connection-pool
        ConnectionPool cp = httpS.getConnectionPool();
        TreeNode connectionPool = TreeNodeFactory.createTreeNode("connection-pool", null, "web");
        httpServiceNode.addChild(connectionPool);
        // web-container
        WebContainer wc = sConfig.getWebContainer();
        TreeNode webContainer = TreeNodeFactory.createTreeNode("web-container", null, "web");
        serverNode.addChild(webContainer);
        // thread-pools
        ThreadPools tps = sConfig.getThreadPools();
        TreeNode threadPools = TreeNodeFactory.createTreeNode("thread-pools", null, "web");
        serverNode.addChild(threadPools);
    }
*/
    /*
    public void postConstruct() {

        // buildWebMonitoringConfigTree should happen before
        // ppem.registerProbeProviderListener 
        buildWebMonitoringConfigTree();

        // ppem.registerProbeProviderListener should be called only after
        // buildWebMonitoringConfigTree is invoked because of dependency
        ppem.registerProbeProviderListener(this);

    }
*/
}
