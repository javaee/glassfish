package com.sun.enterprise.v3.server;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Jerome Dochez
 */
@Service(name="upgrade")
public class UpgradeStartup implements ModuleStartup {

    public void setStartupContext(StartupContext startupContext) {

    }

    // do nothing, just return, at the time the upgrade service has
    // run correctly.
    public void start() {

    }

    public void stop() {

    }
}
