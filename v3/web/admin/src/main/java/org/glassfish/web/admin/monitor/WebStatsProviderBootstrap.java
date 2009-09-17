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
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import java.beans.PropertyChangeEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    Logger logger;
    @Inject
    private static Domain domain;

    private static HttpService httpService = null;
    private static NetworkConfig networkConfig = null;
    private Server server;
    private static final String APPLICATIONS = "applications";
    // Map of apps and its StatsProvider list
    Map<String, List> statsProviderToAppMap = new HashMap();


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
        logger.finest("[Monitor]In the WebStatsProviderBootstrap.postConstruct ************");

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
        StatsProviderManager.register("web-container", PluginPoint.SERVER,
                            "web/jsp", new JspStatsProvider(null, null, logger));
        StatsProviderManager.register("web-container", PluginPoint.SERVER,
                            "web/request", new WebRequestStatsProvider(null, null, logger));
        StatsProviderManager.register("web-container", PluginPoint.SERVER,
                            "web/servlet", new ServletStatsProvider(null, null, logger));
        StatsProviderManager.register("web-container", PluginPoint.SERVER,
                            "web/session", new SessionStatsProvider(null, null, logger));
    }

    public void registerApplicationStatsProviders() {

        List<Application> la = domain.getApplications().getModules(Application.class);
        for (Application sapp : la) {
            addStatsForVirtualServers(sapp.getName());
        }
        // j2ee application
        List<J2eeApplication> lja = domain.getApplications().getModules(J2eeApplication.class);
        for (J2eeApplication japp : lja) {
            addStatsForVirtualServers(japp.getName());
        }
        // web modules
        List<WebModule> lm = domain.getApplications().getModules(WebModule.class);
        for (WebModule wm : lm) {
            addStatsForVirtualServers(wm.getName());
        }

    }

    private void addStatsForVirtualServers(String appName) {
        // get the applications refs for the server
        for (ApplicationRef ar : server.getApplicationRef()) {
            if (appName.equals(ar.getRef())) {
                String vsL = ar.getVirtualServers();
                if (vsL != null) {
                    for (String str : vsL.split(",")) {
                        //create stats providers for each virtual server 'str'
                        List statspList = statsProviderToAppMap.get(appName);
                        if (statspList == null) {
                            statspList = new ArrayList();
                        }
                        JspStatsProvider jspStatsProvider = new JspStatsProvider(appName, str, logger);
                        StatsProviderManager.register(
                                "web-container",
                                PluginPoint.SERVER, APPLICATIONS + "/" + appName + "/" + str,
                                jspStatsProvider);
                        statspList.add(jspStatsProvider);
                        ServletStatsProvider servletStatsProvider = new ServletStatsProvider(appName, str, logger);
                        StatsProviderManager.register(
                                "web-container",
                                PluginPoint.SERVER, APPLICATIONS + "/" + appName + "/" + str,
                                servletStatsProvider);
                        statspList.add(servletStatsProvider);
                        SessionStatsProvider sessionStatsProvider = new SessionStatsProvider(appName, str, logger);
                        StatsProviderManager.register(
                                "web-container",
                                PluginPoint.SERVER, APPLICATIONS + "/" + appName + "/" + str,
                                sessionStatsProvider);
                        statspList.add(sessionStatsProvider);
                        WebRequestStatsProvider websp = new WebRequestStatsProvider(appName, str, logger);
                        StatsProviderManager.register(
                                "web-container",
                                PluginPoint.SERVER, APPLICATIONS + "/" + appName + "/" + str,
                                websp);
                        statspList.add(websp);

                        statsProviderToAppMap.put(appName, statspList);
                    }
                } else {
                    //When the app is deployed without virtual-servers mentioned,
                    // then it is implicitly associated to all the user vitual-servers
                    addStatsForUserVirtualServers(appName);
                }
                return;
            }
        }
    }

    private void addStatsForUserVirtualServers(String appName) {

        for (VirtualServer vs : httpService.getVirtualServer()) {
            if (!vs.getId().equals("__asadmin")) {
                // create stats providers under vs.getId()
                        List statspList = statsProviderToAppMap.get(appName);
                        if (statspList == null) {
                            statspList = new ArrayList();
                        }
                        JspStatsProvider jspStatsProvider = new JspStatsProvider(appName, vs.getId(), logger);
                        StatsProviderManager.register(
                                "web-container",
                                PluginPoint.SERVER, APPLICATIONS + "/" + appName + "/" + vs.getId(),
                                jspStatsProvider);
                        statspList.add(jspStatsProvider);
                        ServletStatsProvider servletStatsProvider = new ServletStatsProvider(appName, vs.getId(), logger);
                        StatsProviderManager.register(
                                "web-container",
                                PluginPoint.SERVER, APPLICATIONS + "/" + appName + "/" + vs.getId(),
                                servletStatsProvider);
                        statspList.add(servletStatsProvider);
                        SessionStatsProvider sessionStatsProvider = new SessionStatsProvider(appName, vs.getId(), logger);
                        StatsProviderManager.register(
                                "web-container",
                                PluginPoint.SERVER, APPLICATIONS + "/" + appName + "/" + vs.getId(),
                                sessionStatsProvider);
                        statspList.add(sessionStatsProvider);
                        WebRequestStatsProvider websp = new WebRequestStatsProvider(appName, vs.getId(), logger);
                        StatsProviderManager.register(
                                "web-container",
                                PluginPoint.SERVER, APPLICATIONS + "/" + appName + "/" + vs.getId(),
                                websp);
                        statspList.add(websp);

                        statsProviderToAppMap.put(appName, statspList);
            }
        }
    }

    public static String getVirtualServerName(String hostName, String listenerPort) {
        try {
            //
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
           //if (event.getSource() instanceof ApplicationRef) {
            if (event.getPropertyName().equals("application-ref")) {
                String propName = event.getPropertyName();
                String appName = null;
                if (event.getNewValue() != null) {
                    //This means its a deployed event
                    appName = ((ApplicationRef)(event.getNewValue())).getRef();
                    addStatsForVirtualServers(appName);
                } else if (event.getOldValue() != null) {
                    //This means its an undeployed event
                    appName = ((ApplicationRef)(event.getOldValue())).getRef();
                    //unregister the StatsProviders for the App
                    List statsProviders = statsProviderToAppMap.get(appName);
                    for (Object statsProvider : statsProviders) {
                        StatsProviderManager.unregister(statsProvider);
                    }
                    statsProviderToAppMap.remove(appName);
                }
                logger.finest("[Monitor] (Un)Deploy event received - name = " + propName + " : Value = " + appName);
           }
       }

        return null;
    }

}
