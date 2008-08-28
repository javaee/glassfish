package org.glassfish.kernel.admin.monitor;

import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.Startup;
import org.glassfish.api.monitoring.TelemetryProvider;

import java.beans.PropertyChangeEvent;

/**
 * @author Jerome Dochez
 */
public class TelemetryService implements Startup, PostConstruct, ConfigListener {

    @Inject(optional=true)
    ModuleMonitoringLevels config=null;

    @Inject
    Habitat habitat;
    
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }

    public void postConstruct() {
        if (config!=null) {
            resetConfig();
        }
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
        resetConfig();
        return null;
    }

    private void resetConfig() {

        if (!config.getWebContainer().equals("OFF")) {
            habitat.getInhabitant(TelemetryProvider.class, "web").get().onLevelChange(config.getWebContainer());
        }
    }
}
