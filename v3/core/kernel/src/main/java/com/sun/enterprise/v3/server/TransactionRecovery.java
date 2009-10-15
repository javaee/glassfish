package com.sun.enterprise.v3.server;

import org.jvnet.hk2.component.*;
import org.jvnet.hk2.annotations.*;
import org.glassfish.transaction.api.*;
import org.glassfish.api.*;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.hk2.component.*;

import java.util.*;

/**
 * Clumsy service to only lookup the transaction recovery when there
 * are applications deployed since that service requires the ORB.
 */
@Service
@Async
public class TransactionRecovery implements Startup, PostConstruct {

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

    public Lifecycle getLifecycle() {
        return Lifecycle.START;
    }
}
