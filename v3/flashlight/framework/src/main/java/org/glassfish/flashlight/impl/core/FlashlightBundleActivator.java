package org.glassfish.flashlight.impl.core;

import org.glassfish.flashlight.impl.client.FlashlightProbeClientMediator;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.internal.api.Globals;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.jvnet.hk2.component.Habitat;

/**
 * @author Mahesh Kannan
 *         Date: Jul 15, 2008
 */
public class FlashlightBundleActivator
    implements BundleActivator {

    BundleContext myBundleContext;

    public void start(BundleContext bCtx) {
        this.myBundleContext = bCtx;
    }

    public void stop(BundleContext bCtx) {
        
    }


}
