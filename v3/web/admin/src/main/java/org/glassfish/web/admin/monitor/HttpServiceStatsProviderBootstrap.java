/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.web.admin.monitor;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;

/**
 *
 * @author PRASHANTH ABBAGANI
 */
@Service(name = "http-service")
@Scoped(Singleton.class)
public class HttpServiceStatsProviderBootstrap implements PostConstruct {

    @Inject
    private Logger logger;
    @Inject
    private static Domain domain;
    
    private boolean httpServiceMonitoringEnabled = false;
    
    private static HttpService httpService = null;
    private static NetworkConfig networkConfig = null;

    public HttpServiceStatsProviderBootstrap() {
    }

    public void postConstruct() {
        // to set log level, uncomment the following 
        // remember to comment it before checkin
        // remove this once we find a proper solution
        Level dbgLevel = Level.FINEST;
        Level defaultLevel = logger.getLevel();
        if ((defaultLevel == null) || (dbgLevel.intValue() < defaultLevel.intValue())) {
            //logger.setLevel(dbgLevel);
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("****** [Monitor]In the HttpServiceStatsProvider bootstrap ************");
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

        for (VirtualServer vs : httpService.getVirtualServer()) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("**** Registering a new StatsProvider");
            }
            StatsProviderManager.register(
                    "http-service",
                    PluginPoint.SERVER,
                    "http-service/" + vs.getId() + "/request",
                    new HttpServiceStatsProvider(vs.getId()));
        }

        /*for (ThreadPool tp : config.getThreadPools().getThreadPool()) {
            StatsProviderManager.register(
                    "http-service",
                    PluginPoint.SERVER,
                    "http-service/thread-pool" + tp.getName()),
                    new ThreadPoolStatsProvider());
        }*/

    }

    private boolean getEnabledValue(String enabledStr) {
        if ("OFF".equals(enabledStr)) {
            return false;
        }
        return true;
    }

    public static String getVirtualServer(String hostName, String listenerPort) {
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
                if (vs.getHosts().contains(hostName) && vs.getNetworkListeners().contains(listener.getName())) {
                    virtualServer = vs;
                }
            }
            return virtualServer.getId();
        } catch (UnknownHostException ex) {
            Logger.getLogger(HttpServiceStatsProviderBootstrap.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
