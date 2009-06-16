/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.monitor;

import org.glassfish.admin.monitor.jvm.*;
import org.glassfish.probe.provider.StatsProviderManagerDelegate;
import org.glassfish.probe.provider.PluginPoint;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.probe.provider.StatsProviderManager;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.Startup;
import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;
/**
 *
 * @author abbagani
 */
@Service
@Scoped(Singleton.class)
public class StatsProviderManagerBootstrap implements Startup, PostConstruct {

    @Inject
    private MonitoringRuntimeDataRegistry mrdr;
    @Inject
    private Domain domain;

    public void postConstruct() {
        StatsProviderManagerDelegate spmd = new StatsProviderManagerDelegateImpl(mrdr, domain);
        StatsProviderManager.setStatsProviderManagerDelegate(spmd);
        System.out.println("StatsProviderManagerDelegate is assigned ********************");
    }

    public Lifecycle getLifecycle() {
        return Startup.Lifecycle.SERVER;
    }

}
