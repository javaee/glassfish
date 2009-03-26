package org.glassfish.kernel.admin.monitor;

import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;
import com.sun.enterprise.config.serverbeans.*;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.Startup;
import org.glassfish.api.monitoring.TelemetryProvider;

import java.beans.PropertyChangeEvent;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

/**
 * @author Jerome Dochez
 */
@Service
@Scoped(Singleton.class)
public class TelemetryService implements Startup, PostConstruct, ConfigListener {

    @Inject(optional=true)
    ModuleMonitoringLevels config = null;

    @Inject
    Habitat habitat;
    
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }

    public void postConstruct() {
        if (config!=null) {
            if (!config.getWebContainer().equals("OFF")) {
                onLevelChange("web-container", config.getWebContainer());
            }
            if (!config.getJvm().equals("OFF")) {
                onLevelChange("jvm", config.getJvm());
            }
            if (!config.getThreadPool().equals("OFF")) {
                onLevelChange("thread-pool", config.getThreadPool());
            }
            if (!config.getHttpService().equals("OFF")) {
                onLevelChange("http-service", config.getHttpService());
            }
            if (!config.getJdbcConnectionPool().equals("OFF")) {
                onLevelChange("jdbc-connection-pool", config.getJdbcConnectionPool());
            }
        }
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
       for (PropertyChangeEvent event : propertyChangeEvents) {
           if (event.getSource() instanceof ModuleMonitoringLevels) {
                String propName = event.getPropertyName();
                String enabled = event.getNewValue().toString();
                onLevelChange(propName, enabled);
           }
       }
        return null;
    }

    private void onLevelChange(String propName, String enabled) {
        TelemetryProvider tp = habitat.getComponent(TelemetryProvider.class, propName);
        if (tp == null) {
            //System.out.println("[Monitor] Couldn't find the provider for " + propName);
        } else {
            //System.out.println("[Monitor] Telemetry Provider is being invoked - " + tp.getClass());
            tp.onLevelChange(enabled);
        }
    }
}
