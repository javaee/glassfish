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
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.osgi.framework.Constants.ACTIVATION_LAZY;
import static org.osgi.framework.Constants.BUNDLE_ACTIVATIONPOLICY;

/**
 * This class is primarily responsbile for depoyment and undeployment of EE artifacts of an OSGi bundle.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiContainer {

    // Context in which this object is operating.
    private BundleContext context;

    protected Map<Bundle, OSGiApplicationInfo> applications =
            new HashMap<Bundle, OSGiApplicationInfo>();
    protected Map<OSGiApplicationInfo, ServiceRegistration> regs = new HashMap<OSGiApplicationInfo, ServiceRegistration>();

    private ServiceTracker deployerTracker;

    /**
     * Sorted in descending order of service ranking
     */
    private List<ServiceReference/*OSGiDeployer*/> sortedDeployerRefs = new ArrayList<ServiceReference>();

    private boolean shutdown = false;

    private static final Logger logger =
            Logger.getLogger(OSGiContainer.class.getPackage().getName());

    protected OSGiContainer(final BundleContext ctx) {
        this.context = ctx;
        deployerTracker = new OSGiDeployerTracker();
    }

    protected void init() {
        // no need to deployAll, as that will happen when tracker is notified of each deployer.
        deployerTracker.open();
    }

    protected synchronized void shutdown() {
        undeployAll();
        assert (applications.isEmpty() && regs.isEmpty());
        applications.clear();
        regs.clear();
        sortedDeployerRefs.clear();
        shutdown = true;
        deployerTracker.close();
        deployerTracker = null;
        context = null;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    private synchronized OSGiApplicationInfo redeploy(Bundle b) throws Exception {
        if (isShutdown()) return null;
        if (isDeployed(b)) {
            undeploy(b);
        }
        return deploy(b);
    }

    /**
     * Deploys an application bundle in underlying application container in GlassFish.
     * This method is synchronized because we don't know if GlassFish
     * deployment framework can handle concurrent requests or not.
     *
     * @param b Bundle to be deployed.
     */
    public synchronized OSGiApplicationInfo deploy(Bundle b) {
        if (isShutdown()) return null;
        // By the time this extender is processing the bundle, if the bundle has already changed
        // state to STOPPING, then cancel the deployment operation.
        if (b.getState() == Bundle.STOPPING) {
            logger.logp(Level.INFO, "OSGiContainer", "deploy",
                    "Bundle {0} is already moved to STOPPING state, so it won't be deployed.", new Object[]{b});
            return null;
        }
        OSGiApplicationInfo osgiAppInfo = applications.get(b);
        if (osgiAppInfo != null) {
            logger.logp(Level.WARNING, "OSGiContainer", "deploy",
                    "Bundle {0} is already deployed at {1} ", new Object[]{b,
                            osgiAppInfo.getAppInfo().getSource()});
            return null;
        }
        ServiceReference/*OSGiDeployer*/ osgiDeployerRef = selectDeployer(b);
        if (osgiDeployerRef == null) {
            // No deployer recognises this bundle, so return
            return null;
        }
        OSGiDeployer osgiDeployer = (OSGiDeployer) context.getService(osgiDeployerRef);
        if (osgiDeployer == null) {
            logger.logp(Level.WARNING, "OSGiContainer", "deploy",
                    "Bundle {0} can't be deployed because corresponding deployer {1} has vanished!!!", new Object[]{b,
                            osgiDeployer});
            return null;
        }

        // deploy the java ee artifacts
        try {
            osgiAppInfo = osgiDeployer.deploy(b);
        } catch (Exception e) {
            logger.logp(Level.WARNING, "OSGiContainer", "deploy",
                    "Failed to deploy bundle " + b, e);
            return null;
        }
        osgiAppInfo.setDeployer(osgiDeployerRef);
        applications.put(b, osgiAppInfo);
        ServiceRegistration reg = context.registerService(OSGiApplicationInfo.class.getName(), osgiAppInfo, new Properties());
        regs.put(osgiAppInfo, reg);
        logger.logp(Level.INFO, "OSGiContainer", "deploy",
                "deployed bundle {0} at {1}",
                new Object[]{osgiAppInfo.getBundle(), osgiAppInfo.getAppInfo().getSource().getURI()});
        return osgiAppInfo;
    }

    /**
     * Undeploys a Java EE application bundle.
     * This method is synchronized because we don't know if GlassFish
     * deployment framework can handle concurrent requests or not.
     *
     * @param b Bundle to be undeployed
     */
    public synchronized void undeploy(Bundle b) {
        if (isShutdown()) return;
        OSGiApplicationInfo osgiAppInfo = applications.get(b);
        if (osgiAppInfo == null) {
            throw new RuntimeException("No applications for bundle " + b);
        }
        applications.remove(b);
        regs.remove(osgiAppInfo).unregister();
        ServiceReference osgiDeployerRef = osgiAppInfo.getDeployer();
        OSGiDeployer osgiDeployer = (OSGiDeployer) context.getService(osgiDeployerRef);
        if (osgiDeployer == null) {
            logger.logp(Level.WARNING, "OSGiContainer", "undeploy",
                    "Failed to undeploy {0}, because corresponding deployer does not exist", new Object[]{b});
            return;
        }
        try {
            osgiDeployer.undeploy(osgiAppInfo);
            logger.logp(Level.INFO, "OSGiContainer", "undeploy",
                    "Undeployed bundle {0}", new Object[]{b});
        } catch (Exception e) {
            logger.logp(Level.WARNING, "OSGiContainer", "undeploy",
                    "Failed to undeploy bundle " + b, e);
            return;
        }
    }

    public synchronized void undeployAll() {
        // Take a copy of the entries as undeploy changes the underlying map.
        for (Bundle b : new HashSet<Bundle>(applications.keySet())) {
            try {
                undeploy(b);
            }
            catch (Exception e) {
                logger.logp(Level.SEVERE, "OSGiContainer", "undeployAll",
                        "Exception undeploying bundle " + b,
                        e);
            }
        }
    }

    public synchronized boolean isDeployed(Bundle bundle) {
        return applications.containsKey(bundle);
    }

    /*package*/

    boolean isReady(Bundle b) {
        final int state = b.getState();
        final boolean isActive = (state & Bundle.ACTIVE) != 0;
        final boolean isStarting = (state & Bundle.STARTING) != 0;
        final boolean isReady = isActive || (isLazy(b) && isStarting);
        return isReady;
    }

    /*package*/

    static boolean isLazy(Bundle bundle) {
        return ACTIVATION_LAZY.equals(
                bundle.getHeaders().get(BUNDLE_ACTIVATIONPOLICY));
    }

    private ServiceReference/*OSGiDeployer*/ selectDeployer(Bundle b) {
        // deployerRefs is already sorted in descending order of ranking
        for (ServiceReference deployerRef : sortedDeployerRefs) {
            OSGiDeployer deployer = OSGiDeployer.class.cast(context.getService(deployerRef));
            if (deployer != null) {
                if (deployer.handles(b)) {
                    return deployerRef;
                }
            }
        }
        return null;
    }

    public synchronized OSGiApplicationInfo[] getDeployedApps() {
        // must return a snapshot, because it is used from DeployerRemovedThread.
        return applications.values().toArray(new OSGiApplicationInfo[0]);
    }

    private class OSGiDeployerTracker extends ServiceTracker {

        public OSGiDeployerTracker() {
            super(OSGiContainer.this.context, OSGiDeployer.class.getName(), null);
        }

        @Override
        public Object addingService(ServiceReference reference) {
            deployerAdded(reference);
            return super.addingService(reference);
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            deployerRemoved(reference);
            super.removedService(reference, service);
        }

    }

    private synchronized void deployerAdded(ServiceReference/*OSGiDeployer*/ reference) {
        if (isShutdown()) return;
        sortedDeployerRefs.add(reference);
        Collections.sort(sortedDeployerRefs, Collections.reverseOrder()); // descending order
        new DeployerAddedThread(reference).start();
    }

    private void deployerRemoved(ServiceReference reference) {
        if (isShutdown()) return;
        sortedDeployerRefs.remove(reference);
        new DeployerRemovedThread(reference).start();
    }

    private class DeployerAddedThread extends Thread {

        ServiceReference newDeployerRef;

        private DeployerAddedThread(ServiceReference newDeployerRef) {
            this.newDeployerRef = newDeployerRef;
        }

        @Override
        public void run() {
            synchronized (OSGiContainer.this) {
                OSGiDeployer newDeployer = (OSGiDeployer) context.getService(newDeployerRef);
                if (newDeployer == null) return;
                for (Bundle b : context.getBundles()) {
                    if (isReady(b) && newDeployer.handles(b)) {
                        try {
                            redeploy(b);
                        } catch (Exception e) {
                            logger.logp(Level.WARNING, "OSGiContainer", "addingService", "Exception redeploying bundle " + b, e);
                        }
                    }
                }
            }
        }
    }

    private class DeployerRemovedThread extends Thread {
        ServiceReference oldDeployerRef;

        private DeployerRemovedThread(ServiceReference oldDeployerRef) {
            this.oldDeployerRef = oldDeployerRef;
        }

        @Override
        public void run() {
            synchronized (OSGiContainer.this) {
                // getDeployedApps returns a snapshot which is essential because redeploy() changes the collection.
                for (OSGiApplicationInfo osgiApplicationInfo : getDeployedApps()) {
                    if (osgiApplicationInfo.getDeployer() == oldDeployerRef) {
                        try {
                            redeploy(osgiApplicationInfo.getBundle());
                        } catch (Exception e) {
                            logger.logp(Level.WARNING, "DeployerRemovedThread", "run", "Exception redeploying bundle " + osgiApplicationInfo.getBundle(), e);
                        }
                    }
                }
            }
        }
    }
}
