/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */


package org.glassfish.osgijavaeebase;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This extender is responsible for detecting and deploying any Java EE OSGi bundle.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class JavaEEExtender implements Extender {
    /*
     * Implementation Note: All methods are synchronized, because we don't allow the extender to stop while it
     * is deploying or undeploying something. Similarly, while it is being stopped, we don't want it to deploy
     * or undeploy something.
     * After receiving the event, it spwans a separate thread to carry out the task so that we don't
     * spend long time in the synchronous event listener. More over, that can lead to deadlocks as observed
     * in https://glassfish.dev.java.net/issues/show_bug.cgi?id=14313.
     */

    private volatile OSGiContainer c;
    private static final Logger logger =
            Logger.getLogger(JavaEEExtender.class.getPackage().getName());
    private BundleContext context;
    private ServiceRegistration reg;
    private BundleTracker tracker;
    private ExecutorService executorService;

    public JavaEEExtender(BundleContext context) {
        this.context = context;
    }

    public synchronized void start() {
        executorService = Executors.newSingleThreadExecutor();
        c = new OSGiContainer(context);
        c.init();
        reg = context.registerService(OSGiContainer.class.getName(), c, null);
        tracker = new BundleTracker(context, Bundle.ACTIVE | Bundle.STARTING, new HybridBundleTrackerCustomizer());
        tracker.open();
    }

    public synchronized void stop() {
        if (c == null) return;
        OSGiContainer tmp = c;
        c = null;
        tmp.shutdown();
        if (tracker != null) tracker.close();
        tracker = null;
        reg.unregister();
        reg = null;
        executorService.shutdownNow();
    }

    private synchronized OSGiApplicationInfo deploy(Bundle b) {
        if (!isStarted()) return null;
        try {
            return c.deploy(b);
        }
        catch (Exception e) {
            logger.logp(Level.SEVERE, "JavaEEExtender", "deploy",
                    "Exception deploying bundle {0}",
                    new Object[]{b.getLocation()});
            logger.logp(Level.SEVERE, "JavaEEExtender", "deploy",
                    "Exception Stack Trace", e);
        }
        return null;
    }

    private synchronized void undeploy(Bundle b) {
        if (!isStarted()) return;
        try {
            if (c.isDeployed(b)) {
                c.undeploy(b);
            }
        }
        catch (Exception e) {
            logger.logp(Level.SEVERE, "JavaEEExtender", "undeploy",
                    "Exception undeploying bundle {0}",
                    new Object[]{b.getLocation()});
            logger.logp(Level.SEVERE, "JavaEEExtender", "undeploy",
                    "Exception Stack Trace", e);
        }
    }

    private boolean isStarted() {
        // This method is deliberately made non-synchronized, because it is called from tracker customizer
        return c!= null;
    }

    private class HybridBundleTrackerCustomizer implements BundleTrackerCustomizer {
        private Map<Long, Future<OSGiApplicationInfo>> deploymentTasks =
                new ConcurrentHashMap<Long, Future<OSGiApplicationInfo>>();

        public Object addingBundle(final Bundle bundle, BundleEvent event) {
            if (!isStarted()) return null;
            final int state = bundle.getState();
            if (isReady(event, state)) {
                Future<OSGiApplicationInfo> future = executorService.submit(new Callable<OSGiApplicationInfo>() {
                    @Override
                    public OSGiApplicationInfo call() throws Exception {
                        return deploy(bundle);
                    }
                });
                deploymentTasks.put(bundle.getBundleId(), future);
                return bundle;
            }
            return null;
        }

        /**
         * Bundle is ready when its state is ACTIVE or, when a lazy activation policy is used, STARTING
         * @param event
         * @param state
         * @return
         */
        private boolean isReady(BundleEvent event, int state) {
            return state == Bundle.ACTIVE ||
                    (state == Bundle.STARTING && (event != null && event.getType() == BundleEvent.LAZY_ACTIVATION));
        }

        public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
        }

        public void removedBundle(final Bundle bundle, BundleEvent event, Object object) {
            if (!isStarted()) return;
            Future<OSGiApplicationInfo> deploymentTask = deploymentTasks.remove(bundle.getBundleId());
            if (deploymentTask == null) {
                // We have never seen this bundle before. Ideally we should never get here.
                assert(false);
                return;
            }
            try {
                OSGiApplicationInfo deployedApp = deploymentTask.get();
                // It is not sufficient to check the future only, as the DeployerAddedThread currently deploys
                // without our knowledge, so we must also check isDeployed().
                if (deployedApp != null || c.isDeployed(bundle)) {
                    undeploy(bundle); // undeploy synchronously to avoid any deadlock. See GF issue #
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
            } catch (ExecutionException e) {
                logger.logp(Level.FINE, "JavaEEExtender$HybridBundleTrackerCustomizer", "removedBundle", "e = {0}", new Object[]{e});
            }
        }
    }
}
