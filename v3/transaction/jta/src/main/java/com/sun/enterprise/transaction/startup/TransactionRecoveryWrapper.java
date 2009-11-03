package com.sun.enterprise.transaction.startup;

import java.util.List;

import org.glassfish.api.Async;
import org.glassfish.api.Startup;
import org.glassfish.internal.api.*;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.transaction.api.ResourceRecoveryManager;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

/**
 * Service wrapper to only lookup the transaction recovery when there
 * are applications deployed since the actual service has ORB dependency.
 */
@Service
public class TransactionRecoveryWrapper implements PostStartup, PostConstruct {

    @Inject
    Applications applications;

    @Inject
    Habitat habitat;

    public void postConstruct() {
        final List<Application> apps = applications.getApplications();
        if (apps!=null && apps.size()>0) {
            habitat.getByContract(ResourceRecoveryManager.class);
        }
    }
}
