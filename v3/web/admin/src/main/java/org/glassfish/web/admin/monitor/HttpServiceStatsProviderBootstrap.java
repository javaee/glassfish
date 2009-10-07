/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.web.admin.monitor;

import java.util.List;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;

/**
 *
 * @author PRASHANTH ABBAGANI
 */
@Service(name = "http-service")
@Scoped(Singleton.class)
public class HttpServiceStatsProviderBootstrap implements PostConstruct {

    @Inject
    private static Domain domain;
    
    public void postConstruct() {
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
            StatsProviderManager.register(
                    "http-service",
                    PluginPoint.SERVER,
                    "http-service/" + vs.getId(),
                    new VirtualServerInfoStatsProvider(vs));
            StatsProviderManager.register(
                    "http-service",
                    PluginPoint.SERVER,
                    "http-service/" + vs.getId() + "/request",
                    new HttpServiceStatsProvider(vs.getId()));
        }
    }
}
