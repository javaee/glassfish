/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.web.admin.monitor;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.logging.LogDomains;
import java.beans.PropertyChangeEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.*;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ApplicationInfo;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 *
 * @author PRASHANTH ABBAGANI
 */
@Service(name = "web")
@Scoped(Singleton.class)
public class WebStatsProviderBootstrap implements PostConstruct {

    private static final Logger logger = LogDomains.getLogger(
        WebStatsProviderBootstrap.class, LogDomains.WEB_LOGGER);

    private static final String NODE_SEPARATOR = "/";

    @Inject
    private static Domain domain;

    private static HttpService httpService = null;
    private static NetworkConfig networkConfig = null;

    private Server server;

    // Map of apps and its StatsProvider list
    private ConcurrentMap<String, ConcurrentMap<String, Queue>> vsNameToStatsProviderMap =
            new ConcurrentHashMap<String, ConcurrentMap<String, Queue>>();
    private Queue webContainerStatsProviderQueue = new ConcurrentLinkedQueue();
    private AtomicBoolean isWebStatsProvidersRegistered = new AtomicBoolean(false);

    public WebStatsProviderBootstrap() {
    }

    public void postConstruct(){
        List<Config> lc = domain.getConfigs().getConfig();
        Config config = null;
        for (Config cf : lc) {
            if (cf.getName().equals("server-config")) {
                config = cf;
                break;
            }
        }
        httpService = config.getHttpService();
        networkConfig = config.getNetworkConfig();

        server = null;
        List<Server> ls = domain.getServers().getServer();
        for (Server sr : ls) {
            if ("server".equals(sr.getName())) {
                server = sr;
                break;
            }
        }

        //Register the Web stats providers
        registerWebStatsProviders();
    }

    private synchronized void registerWebStatsProviders() {
        if (isWebStatsProvidersRegistered.get()) {
            return;
        }

        JspStatsProvider jsp = new JspStatsProvider(null, null);
        RequestStatsProvider wsp = new RequestStatsProvider(null, null);
        ServletStatsProvider svsp = new ServletStatsProvider(null, null);
        SessionStatsProvider sssp = new SessionStatsProvider(null, null);
        StatsProviderManager.register("web-container", PluginPoint.SERVER,
            "web/jsp", jsp);
        StatsProviderManager.register("web-container", PluginPoint.SERVER,
            "web/request", wsp);
        StatsProviderManager.register("web-container", PluginPoint.SERVER,
            "web/servlet", svsp);
        StatsProviderManager.register("web-container", PluginPoint.SERVER,
            "web/session", sssp);
        webContainerStatsProviderQueue.add(jsp);
        webContainerStatsProviderQueue.add(wsp);
        webContainerStatsProviderQueue.add(svsp);
        webContainerStatsProviderQueue.add(sssp);

        isWebStatsProvidersRegistered.set(true);
    }

    public void registerApplicationStatsProviders(String monitoringName,
            String vsName, List<String> servletNames) {

        // try register again as it may be unregistered
        registerWebStatsProviders();

        //create stats providers for each virtual server 'vsName'
        String node = getNodeString(monitoringName, vsName);
        ConcurrentMap<String, Queue> statsProviderMap = vsNameToStatsProviderMap.get(vsName);
        Queue statspList = null;
        if (statsProviderMap == null) {
            statsProviderMap = new ConcurrentHashMap<String, Queue>();
            ConcurrentMap<String, Queue> anotherMap =
                    vsNameToStatsProviderMap.putIfAbsent(vsName, statsProviderMap);
            if (anotherMap != null) {
                statsProviderMap = anotherMap;
            }
        } else {
            statspList = statsProviderMap.get(monitoringName);
        }
        if (statspList == null) {
            statspList = new ConcurrentLinkedQueue();
            Queue anotherQueue = statsProviderMap.putIfAbsent(monitoringName, statspList);
            if (anotherQueue != null) {
                statspList = anotherQueue;
            }
        }

        JspStatsProvider jspStatsProvider =
                new JspStatsProvider(monitoringName, vsName);
        StatsProviderManager.register(
                "web-container", PluginPoint.APPLICATIONS, node,
                jspStatsProvider);
        statspList.add(jspStatsProvider);
        ServletStatsProvider servletStatsProvider =
                new ServletStatsProvider(monitoringName, vsName);
        StatsProviderManager.register(
                "web-container", PluginPoint.APPLICATIONS, node,
                servletStatsProvider);
        statspList.add(servletStatsProvider);
        SessionStatsProvider sessionStatsProvider =
                new SessionStatsProvider(monitoringName, vsName);
        StatsProviderManager.register(
                "web-container", PluginPoint.APPLICATIONS, node,
                sessionStatsProvider);
        statspList.add(sessionStatsProvider);
        RequestStatsProvider websp =
                new RequestStatsProvider(monitoringName, vsName);
        StatsProviderManager.register(
                "web-container", PluginPoint.APPLICATIONS, node,
                websp);

        for (String servletName : servletNames) {
             ServletInstanceStatsProvider servletInstanceStatsProvider = 
                 new ServletInstanceStatsProvider(servletName,
                     monitoringName, vsName, servletStatsProvider);
             StatsProviderManager.register(
                     "web-container", PluginPoint.APPLICATIONS,
                     getNodeString(monitoringName, vsName, servletName),
                     servletInstanceStatsProvider);
             statspList.add(servletInstanceStatsProvider);
        }

        statspList.add(websp);
    }

    public void unregisterApplicationStatsProviders(String monitoringName,
            String vsName) {

        Map<String, Queue> statsProviderMap = vsNameToStatsProviderMap.get(vsName); 
        // remove stats providers for a given monitoringName and vs
        Queue statsProviders = statsProviderMap.remove(monitoringName);
        for (Object statsProvider : statsProviders) {
            StatsProviderManager.unregister(statsProvider);
        }

        if (statsProviderMap.isEmpty()) {
            vsNameToStatsProviderMap.remove(vsName);
        }

        // remove web stats provider if it is empty (for all vs)
        if (vsNameToStatsProviderMap.isEmpty()) {
            for (Object statsProvider : webContainerStatsProviderQueue) {
                StatsProviderManager.unregister(statsProvider);
            }
            webContainerStatsProviderQueue.clear();
            isWebStatsProvidersRegistered.set(false);
        }
    }

    private String getNodeString(String moduleName, String... others) {
        StringBuffer sb = new StringBuffer(moduleName);
        for (String other: others) {
            sb.append(NODE_SEPARATOR).append(other);
        }
        return sb.toString();
    }
}
