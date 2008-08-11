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
package org.glassfish.web.admin.monitor.telemetry;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;
import org.glassfish.api.Startup;
import org.glassfish.api.Startup.Lifecycle;
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

/**
 * Provides for bootstarpping web telemetry during startup and
 * when the monitoring level config changes
 *
 * @author Sreenivas Munnangi
 */
@Service
@Scoped(Singleton.class)
public class WebMonitorStartup implements 
    Startup, PostConstruct, ProbeProviderListener, ConfigListener  {
    private boolean threadpoolMonitoringEnabled;
    private boolean httpServiceMonitoringEnabled;
    private boolean jvmMonitoringEnabled;
    private boolean webMonitoringEnabled;

    @Inject
    private ProbeProviderEventManager ppem;
    @Inject
    private ProbeClientMediator pcm;

    @Inject
    private ModuleMonitoringLevels mml;

    @Inject
    Logger logger;

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
    
    @Inject
    private static Domain domain;
    @Inject
    private MonitoringRuntimeDataRegistry mrdr;
    private TreeNode serverNode;
    private TreeNode httpServiceNode;
    private TreeNode webNode;
    private TreeNode webSessionNode;
    private TreeNode webServletNode;
    private TreeNode webJspNode;
    private TreeNode webRequestNode;
    
    private Level dbgLevel = Level.FINEST;
    private Level defaultLevel;
    
    public void postConstruct() {
        logger.finest("In the Web Monitor startup ************");

        // to set log level, uncomment the following 
        // remember to comment it before checkin
        // remove this once we find a proper solution
        /*
        defaultLevel = logger.getLevel();
        if (dbgLevel.intValue() < defaultLevel.intValue()) {
            logger.setLevel(dbgLevel);
        }
        */
        logger.finest("In the Web Monitor startup ************");

        ppem.registerProbeProviderListener(this);
        buildWebMonitoringConfigTree();
        //Construct the JVMMemoryStatsTelemetry
        jvmMemoryTM = 
        new JVMMemoryStatsTelemetry(serverNode, jvmMonitoringEnabled, logger);
    }

    public Lifecycle getLifecycle() {
        // This service stays running for the life of the app server, hence SERVER.
        return Lifecycle.SERVER;
    }

    public void providerRegistered(String moduleName, String providerName, String appName) {
        try {
            
            logger.finest("Provider registered event received - providerName = " + 
                                providerName + " : module name = " + moduleName + 
                                " : appName = " + appName);
            if (providerName.equals("session")){
                logger.finest("and it is Web session");
                if (webSessionsTM == null) {
                    webSessionsTM = new SessionStatsTelemetry(webSessionNode, 
                                    null, null, webMonitoringEnabled, logger);
                    Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webSessionsTM);
                    webSessionsTM.setProbeListenerHandles(handles);
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
                }
            }
            if (providerName.equals("servlet")){
                logger.finest("and it is Web servlet");
                if (webServletsTM == null) {
                    webServletsTM = new ServletStatsTelemetry(webServletNode, null, 
                                            null, webMonitoringEnabled, logger);
                    Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webServletsTM);
                    webServletsTM.setProbeListenerHandles(handles);
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
                }
            }

            if (providerName.equals("jsp")){
                logger.finest("and it is Web jsp");
                if (webJspTM == null) {
                    webJspTM = new JspStatsTelemetry(webJspNode, null, null, webMonitoringEnabled, logger);
                    Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webJspTM);
                    webJspTM.setProbeListenerHandles(handles);
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
                }
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
                logger.finest("and it is Web request");
                if (webRequestTM == null) {
                    webRequestTM = new WebRequestTelemetry(webRequestNode, webMonitoringEnabled, logger);
                    Collection<ProbeClientMethodHandle> handles = pcm.registerListener(webRequestTM);
                    webRequestTM.setProbeListenerHandles(handles);
                }
            }
            if (providerName.equals("threadpool")){
                logger.finest("and it is threadpool");
                if (threadPoolTM == null) {
                    // Where do I add this? Looks like the thread pools are already created.
                    // Now I need to register the listeners, but which one to register?
                    threadPoolTM = new ThreadPoolTelemetry(httpServiceNode, threadpoolMonitoringEnabled, logger);
                    Collection<ProbeClientMethodHandle> handles = pcm.registerListener(threadPoolTM);
                    threadPoolTM.setProbeListenerHandles(handles);
                }
            }
             //Decide now if I need to enable or disable the nodes (for first time use)
        }catch (Exception e) {
            //Never throw an exception as the Web container startup will have a problem
            //Show warning
            logger.finest("WARNING: Exception in WebMonitorStartup : " + 
                                    e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void providerUnregistered(String moduleName, String providerName, String appName) {
        
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
    private void buildWebMonitoringConfigTree() {
        /*
        if (mml != null) {
            if ("OFF".equals(mml.getWebContainer())){
                webMonitoringEnabled = false;
                logger.finest("Disabling webContainer");
            } else {
                webMonitoringEnabled = true;
            }
            if ("OFF".equals(mml.getJvm())){
                jvmMonitoringEnabled = false;
                logger.finest("Disabling jvmMonitoring");
            } else {
                jvmMonitoringEnabled = true;
            }
            if ("OFF".equals(mml.getHttpService())){
                httpServiceMonitoringEnabled = false;
                logger.finest("Disabling httpServiceMonitoring");
            } else {
                httpServiceMonitoringEnabled = true;
            }
            if ("OFF".equals(mml.getThreadPool())){
                threadpoolMonitoringEnabled = false;
                logger.finest("Disabling threadpoolMonitoring");
            } else {
                threadpoolMonitoringEnabled = true;
            }
            logger.finest("mml.getWebContainer() = " + mml.getWebContainer());
        }
         */
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
                boolean enabled = getEnabledValue(mLevel);
                logger.finest("[TM] Config change event - propName = " + propName + " : enabled=" + enabled);
                // set corresponding tree node enabled flag 
                // handle proble listener event by registering/unregistering
            } else if ("jvm".equals(propName)) {
                mml = (ModuleMonitoringLevels) event.getSource();
                mLevel = event.getNewValue().toString();
                boolean enabled = getEnabledValue(mLevel);
                logger.finest("[TM] Config change event - propName = " + propName + " : enabled=" + enabled);
                // set corresponding tree node enabled flag 
                // handle proble listener event by registering/unregistering
                //jvmMemoryTM.enableMonitoring(enabled);
            } else if ("thread-pool".equals(propName)) {
                mml = (ModuleMonitoringLevels) event.getSource();
                mLevel = event.getNewValue().toString();
                boolean enabled = getEnabledValue(mLevel);
                logger.finest("[TM] Config change event - propName = " + propName + " : enabled=" + enabled);
                // set corresponding tree node enabled flag 
                // handle proble listener event by registering/unregistering
                //threadpoolTM.enableMonitoring(enabled);
            } else if ("web-container".equals(propName)) {
                mml = (ModuleMonitoringLevels) event.getSource();
                mLevel = event.getNewValue().toString();
                boolean enabled = getEnabledValue(mLevel);
                logger.finest("[TM] Config change event - propName = " + propName + " : enabled=" + enabled);
                // set corresponding tree node enabled flag 
                // handle proble listener event by registering/unregistering
                
                //webTM.enableMonitoring(enabled);
                //webRequestTM.enableMonitoring(enabled);
                //moduleTM.enableMonitoring(enabled);
                //sessionsTM.enableMonitoring(enabled);
            }
        }
        return null;
    }
    
    private boolean getEnabledValue(String enabledStr) {
        if ("OFF".equals(enabledStr)) {
            return false;
        }
        return true;
    }
}
