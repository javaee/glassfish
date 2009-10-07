/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.web.admin.monitor;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
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

        HttpService httpService = config.getHttpService();
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
    }

    private boolean getEnabledValue(String enabledStr) {
        if ("OFF".equals(enabledStr)) {
            return false;
        }
        return true;
    }
}
