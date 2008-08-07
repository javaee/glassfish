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
import java.util.Collection;
import java.util.List;
import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;
import java.beans.PropertyChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.ConfigListener;

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

    @Inject
    private ProbeProviderEventManager ppem;
    @Inject
    private ProbeClientMediator pcm;

    @Inject
    private ModuleMonitoringLevels mml;
    
    private WebTelemetry webTM = null;
    private SessionStatsTelemetry sessionsTM = null;
    private WebRequestTelemetry webRequestTM = null;
    private ThreadPoolTelemetry threadPoolTM = null;
    private WebModuleTelemetry moduleTM = null;
    private JVMMemoryStatsTelemetry jvmMemoryTM;
    
    private Collection<ProbeClientMethodHandle> handles;
    @Inject
    private Domain domain;
    @Inject
    private MonitoringRuntimeDataRegistry mrdr;
    private TreeNode server;
    private TreeNode httpService;
    
    public void postConstruct() {
        System.out.println("In the Web Monitor startup ************");
        ppem.registerProbeProviderListener(this);
        buildWebMonitoringConfigTree();
        //Construct the JVMMemoryStatsTelemetry
        jvmMemoryTM = new JVMMemoryStatsTelemetry(server);
    }

    public Lifecycle getLifecycle() {
        // This service stays running for the life of the app server, hence SERVER.
        return Lifecycle.SERVER;
    }

    public void providerRegistered(String moduleName, String providerName, String appName) {
        try {
            
            System.out.println("Provider registered event received - providerName = " + 
                                providerName + " : module name = " + moduleName + 
                                " : appName = " + appName);
            if (providerName.equals("session")){
                System.out.println("and it is Web session");
                if (webTM == null) {
                    webTM = new WebTelemetry(server);
                    handles = pcm.registerListener(webTM);
                }
                if (sessionsTM == null) {
                    sessionsTM = new SessionStatsTelemetry(webTM.getTreeNode());
                    handles = pcm.registerListener(sessionsTM);
                }
            }
            if (providerName.equals("servlet")){
                System.out.println("and it is Web servlet");
                if (webTM == null) {
                    webTM = new WebTelemetry(server);
                    handles = pcm.registerListener(webTM);
                }
            }

            if (providerName.equals("webmodule")){
                System.out.println("and it is Web Module");
                if (moduleTM == null) {
                    moduleTM = new WebModuleTelemetry(server);
                    handles = pcm.registerListener(moduleTM);
                }
            }
            
            if (providerName.equals("request")){
                System.out.println("and it is Web request");
                if (webRequestTM == null) {
                    webRequestTM = new WebRequestTelemetry(server);
                    handles = pcm.registerListener(webTM);
                }
            }
            if (providerName.equals("threadpool")){
                System.out.println("and it is threadpool");
                if (threadPoolTM == null) {
                    // Where do I add this? Looks like the thread pools are already created.
                    // Now I need to register the listeners, but which one to register?
                    //threadPoolTM = new ThreadPoolTelemetry(httpService);
                    //handles = pcm.registerListener(threadPoolTM);
                }
            }
             //Decide now if I need to enable or disable the nodes (for first time use)
        }catch (Exception e) {
            //Never throw an exception as the Web container startup will have a problem
            //Show warning
            System.out.println("WARNING: Exception in WebMonitorStartup : " + 
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
        // server
        Server srvr = null;
        List<Server> ls = domain.getServers().getServer();
        for (Server sr : ls) {
            if ("server".equals(sr.getName())) {
                srvr = sr;
                break;
            }
        }
        server = TreeNodeFactory.createTreeNode("server", null, "server");
        mrdr.add("server", server);
        // applications
        TreeNode applications = TreeNodeFactory.createTreeNode("applications", null, "web");
        server.addChild(applications);
        // application
        List<Application> la = domain.getApplications().getModules(Application.class);
        for (Application sapp : la) {
            TreeNode app = TreeNodeFactory.createTreeNode(sapp.getName(), null, "web");
            applications.addChild(app);
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
        httpService = TreeNodeFactory.createTreeNode("http-service", null, "web");
        server.addChild(httpService);
        // http-listener
        for (HttpListener htl : httpS.getHttpListener()) {
            TreeNode httpListener = TreeNodeFactory.createTreeNode(htl.getId(), null, "web");
            httpService.addChild(httpListener);
        }
        // connection-pool
        ConnectionPool cp = httpS.getConnectionPool();
        TreeNode connectionPool = TreeNodeFactory.createTreeNode("connection-pool", null, "web");
        httpService.addChild(connectionPool);
        // web-container
        WebContainer wc = sConfig.getWebContainer();
        TreeNode webContainer = TreeNodeFactory.createTreeNode("web-container", null, "web");
        server.addChild(webContainer);
        // thread-pools
        ThreadPools tps = sConfig.getThreadPools();
        TreeNode threadPools = TreeNodeFactory.createTreeNode("thread-pools", null, "web");
        server.addChild(threadPools);
    }

    /**
     * Handle config changes for monitoring levels
     * Add code for handling deployment changes like deploy/undeploy
     */
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        System.out.println("WebMonitorStartup: UnprocessedChangeEvents: " + events[0].getPropertyName());
        for (PropertyChangeEvent event : events) {
            String propName = event.getPropertyName();
            String enabled = null;
            if ("http-service".equals(propName)) {
                mml = (ModuleMonitoringLevels) event.getSource();
                enabled = event.getNewValue().toString();
                // set corresponding tree node enabled flag 
                // handle proble listener event by registering/unregistering
            } else if ("jvm".equals(propName)) {
                mml = (ModuleMonitoringLevels) event.getSource();
                enabled = event.getNewValue().toString();
                // set corresponding tree node enabled flag 
                // handle proble listener event by registering/unregistering
            } else if ("thread-pool".equals(propName)) {
                mml = (ModuleMonitoringLevels) event.getSource();
                enabled = event.getNewValue().toString();
                // set corresponding tree node enabled flag 
                // handle proble listener event by registering/unregistering
            } else if ("web-container".equals(propName)) {
                mml = (ModuleMonitoringLevels) event.getSource();
                enabled = event.getNewValue().toString();
                // set corresponding tree node enabled flag 
                // handle proble listener event by registering/unregistering
            }
        }
        return null;
    }
}
