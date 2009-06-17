/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.web.admin.monitor;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.probe.provider.PluginPoint;
import org.glassfish.probe.provider.StatsProviderManager;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

/**
 *
 * @author PRASHANTH ABBAGANI
 */
//@Service
public class WebStatsProviderBootstrap implements PostConstruct {

    @Inject
    Logger logger;
    @Inject
    private static Domain domain;
            
    private TreeNode serverNode;
    private static HttpService httpService = null;
    private static NetworkConfig networkConfig = null;

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

        StatsProviderManager.register("web-container", PluginPoint.SERVER, "web/jsp", new JspStatsProvider(null, null, logger));
        StatsProviderManager.register("web-container", PluginPoint.SERVER, "web/request", new WebRequestStatsProvider(null, null, logger));
        StatsProviderManager.register("web-container", PluginPoint.SERVER, "web/servlet", new ServletStatsProvider(null, null, logger));
        StatsProviderManager.register("web-container", PluginPoint.SERVER, "web/session", new SessionStatsProvider(null, null, logger));

        TreeNode appsNode = serverNode.getNode("applications");
        Collection<TreeNode> appNodes = appsNode.getChildNodes();
        for (TreeNode appNode : appNodes) {
            Collection<TreeNode> vsNodes = appNode.getChildNodes();
            for (TreeNode vsNode : vsNodes) {
                StatsProviderManager.register(
                        "web-container",
                        PluginPoint.APPLICATIONS,
                        appNode.getName() + "/" + vsNode.getName(),
                        new JspStatsProvider(appNode.getName(), vsNode.getName(), logger));
                StatsProviderManager.register(
                        "web-container",
                        PluginPoint.APPLICATIONS,
                        appNode.getName() + "/" + vsNode.getName(),
                        new ServletStatsProvider(appNode.getName(), vsNode.getName(), logger));
                StatsProviderManager.register(
                        "web-container",
                        PluginPoint.APPLICATIONS,
                        appNode.getName() + "/" + vsNode.getName(),
                        new SessionStatsProvider(appNode.getName(), vsNode.getName(), logger));
                StatsProviderManager.register(
                        "web-container",
                        PluginPoint.APPLICATIONS,
                        appNode.getName() + "/" + vsNode.getName(),
                        new WebRequestStatsProvider(appNode.getName(), vsNode.getName(), logger));
            }
        }
    }

    public static String getAppName(String contextRoot) {
        if (contextRoot == null)
            return null;
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
}
