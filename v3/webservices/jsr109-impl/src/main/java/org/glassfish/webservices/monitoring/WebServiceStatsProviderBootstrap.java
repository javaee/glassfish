package org.glassfish.webservices.monitoring;

import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.api.monitoring.TelemetryProvider;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;


/**
 * @author Jitendra Kotamraju
 */
@Service
@Scoped(Singleton.class)
public class WebServiceStatsProviderBootstrap implements TelemetryProvider, PostConstruct {

    public void onLevelChange(String newLevel) {
    }

    public void postConstruct() {
        StatsProviderManager.register(
            "WebServiceContainer",
            PluginPoint.SERVER,
            "Web Services",
            new WebServiceStatsProvider());
    }
}