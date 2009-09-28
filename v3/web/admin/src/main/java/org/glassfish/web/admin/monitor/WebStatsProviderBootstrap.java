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
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import java.beans.PropertyChangeEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.*;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
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
public class WebStatsProviderBootstrap implements PostConstruct, ConfigListener {

    @Inject
    private Logger logger;
    @Inject
    private static Domain domain;

    private static HttpService httpService = null;
    private static NetworkConfig networkConfig = null;
    private Server server;
    private static final String APPLICATIONS = "applications";
    // Map of apps and its StatsProvider list
    private Map<String, List> statsProviderToAppMap = new HashMap();
    private ArrayList webContainerStatsProviderList = new ArrayList();

    private Map<String, Set<String>> moduleNamesMap =
        new HashMap<String, Set<String>>();

    public WebStatsProviderBootstrap() {
    }

    public void postConstruct(){
        // to set log level, uncomment the following 
        // remember to comment it before checkin
        // remove this once we find a proper solution
        Level dbgLevel = Level.FINEST;
        Level defaultLevel = logger.getLevel();
        if ((defaultLevel == null) || (dbgLevel.intValue() < defaultLevel.intValue())) {
            //logger.setLevel(dbgLevel);
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[Monitor]In the WebStatsProviderBootstrap.postConstruct ************");
        }

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

        //Register the Applications stats providers
        registerApplicationStatsProviders();
    }

    private void registerWebStatsProviders() {
        JspStatsProvider jsp = new JspStatsProvider(null, null, logger);
        RequestStatsProvider wsp = new RequestStatsProvider(null, null, logger);
        ServletStatsProvider svsp = new ServletStatsProvider(null, null, logger);
        SessionStatsProvider sssp = new SessionStatsProvider(null, null, logger);
        StatsProviderManager.register("web-container", PluginPoint.SERVER, "web/jsp", jsp);
        StatsProviderManager.register("web-container", PluginPoint.SERVER, "web/request", wsp);
        StatsProviderManager.register("web-container", PluginPoint.SERVER, "web/servlet", svsp);
        StatsProviderManager.register("web-container", PluginPoint.SERVER,  "web/session", sssp);
        webContainerStatsProviderList.add(jsp);
        webContainerStatsProviderList.add(wsp);
        webContainerStatsProviderList.add(svsp);
        webContainerStatsProviderList.add(sssp);
    }

    public void registerApplicationStatsProviders() {
        List<Application> webApps =
            domain.getApplications().getApplicationsWithSnifferType("web");
        for (Application webApp : webApps) {
            String appName = webApp.getName();
            HashSet<String> moduleNames = getModulesNames(appName);
            for (String moduleName : moduleNames) {
                addStatsForVirtualServers(appName, moduleName);
            }
        }
    }

    private void addStatsForVirtualServers(String appName,
                                           String moduleName) {
        // get the applications refs for the server
        for (ApplicationRef ar : server.getApplicationRef()) {
            if (!appName.equals(ar.getRef())) {
                continue;
            }
            String vsL = ar.getVirtualServers();
            if (vsL != null) {
                for (String str : vsL.split(",")) {
                    //create stats providers for each virtual server 'str'
                    List statspList = statsProviderToAppMap.get(moduleName);
                    if (statspList == null) {
                        statspList = new ArrayList();
                    }
                    JspStatsProvider jspStatsProvider =
                        new JspStatsProvider(moduleName, str, logger);
                    StatsProviderManager.register(
                            "web-container",
                            PluginPoint.SERVER, APPLICATIONS + "/" +
                                moduleName + "/" + str,
                            jspStatsProvider);
                    statspList.add(jspStatsProvider);
                    ServletStatsProvider servletStatsProvider =
                        new ServletStatsProvider(moduleName, str, logger);
                    StatsProviderManager.register(
                            "web-container",
                            PluginPoint.SERVER, APPLICATIONS + "/" +
                                moduleName + "/" + str,
                            servletStatsProvider);
                    statspList.add(servletStatsProvider);
                    SessionStatsProvider sessionStatsProvider =
                        new SessionStatsProvider(moduleName, str, logger);
                    StatsProviderManager.register(
                            "web-container",
                            PluginPoint.SERVER, APPLICATIONS + "/" +
                                moduleName + "/" + str,
                            sessionStatsProvider);
                    statspList.add(sessionStatsProvider);
                    RequestStatsProvider websp =
                        new RequestStatsProvider(moduleName, str, logger);
                    StatsProviderManager.register(
                            "web-container",
                            PluginPoint.SERVER, APPLICATIONS + "/" +
                                moduleName + "/" + str,
                            websp);
                    statspList.add(websp);
                    statsProviderToAppMap.put(moduleName, statspList);
                }
            }
            return;
        }
    }

    public static String getVirtualServerName(String hostName,
                                              String listenerPort) {
        try {
            if (hostName == null) {
                return null;
            }
            if (hostName.equals("localhost")) {
                hostName = InetAddress.getLocalHost().getHostName();
            }
            NetworkListener listener = null;

            for (NetworkListener hl : networkConfig.getNetworkListeners().getNetworkListener()) {
                if (hl.getPort().equals(listenerPort)) {
                    listener = hl;
                    break;
                }
            }
            VirtualServer virtualServer = null;
            for (VirtualServer vs : httpService.getVirtualServer()) {
                if (vs.getHosts().contains(hostName)
                    && vs.getNetworkListeners().contains(listener.getName())) {
                    virtualServer = vs;
                    break;
                }
            }
            return virtualServer.getId();
        } catch (UnknownHostException ex) {
            Logger.getLogger(WebStatsProviderBootstrap.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    // Handle the deploy/undeploy events
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        for (PropertyChangeEvent event : events) {
            if (event.getPropertyName().equals("application-ref")) {
                String propName = event.getPropertyName();
                Set<String> moduleNames = null;
                String appName = null;
                if (event.getNewValue() != null) {
                    // This means its a deployed event
                    appName = ((ApplicationRef)(event.getNewValue())).getRef();
                    moduleNames = getModulesNames(appName);
                    for (String moduleName : moduleNames) {
                        addStatsForVirtualServers(appName, moduleName);
                    }
                    moduleNamesMap.put(appName, moduleNames);
                } else if (event.getOldValue() != null) {
                    // This means its an undeployed event
                    appName = ((ApplicationRef)(event.getOldValue())).getRef();
                    moduleNames = moduleNamesMap.remove(appName);
                    //unregister the StatsProviders for the modules
                    for (String moduleName : moduleNames) {
                        List statsProviders = statsProviderToAppMap.remove(
                            moduleName);
                        for (Object statsProvider : statsProviders) {
                            StatsProviderManager.unregister(statsProvider);
                        }
                    }
                    if (statsProviderToAppMap.isEmpty()) {
                        for (Object statsProvider : webContainerStatsProviderList) {
                            StatsProviderManager.unregister(statsProvider);
                        }
                    }
                }
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("[Monitor] (Un)Deploy event received - name = " + propName + " : Value = " + appName);
                }
            }
        }

        return null;
    }


    /**
     * Looks up the Application with the given appName, and returns a set
     * of the names of its enclosed web modules (if any).
     *
     * If the Application with the given appName represents a standalone
     * WAR file, the returned set will contain a single name. 
     * 
     * If the Application with the given appName represents an EAR file,
     * the returned set will contain the names of all enclosed web modules,
     * using a format of appName#moduleName. 
     */    
    private HashSet<String> getModulesNames(String appName) {
        HashSet<String> moduleNames = new HashSet<String>();
        List<Application> webApps =
            domain.getApplications().getApplicationsWithSnifferType("web");
        for (Application webApp : webApps) {
            if (!appName.equals(webApp.getName())) {
                continue;
            }
            List<Module> modules = webApp.getModule();
            for (Module module : modules) {
                if (module.getEngine("web") != null) {
                    // This is a web module
                    if (webApp.isStandaloneModule()) {
                        moduleNames.add(module.getName());
                        break;
                    } else {
                        // WAR nested inside EAR. Strip off ".war" suffix
                        String nameWithSuffix = module.getName();
                        String moduleName = nameWithSuffix.substring(0,
                            nameWithSuffix.length() - 4);
                        moduleNames.add(appName + "#" + moduleName);
                    }
                }
            }
        }

        return moduleNames;
    }
}
