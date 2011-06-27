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

package org.glassfish.kernel;

import com.sun.enterprise.glassfish.bootstrap.*;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.jvnet.hk2.component.Habitat;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

import java.io.File;
import java.util.*;

import org.glassfish.embeddable.GlassFishException;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 *         <p/>
 *         This is the bundle activator responsible for starting GlassFish server process.
 *         It also starts any bundles that's necessary for glassfish to function (e.g., file install, config admin)
 */

public class GlassFishActivator implements BundleActivator, EventListener {

    private BundleContext bundleContext;
    private Events events;

    public void start(final BundleContext context) throws Exception {
        this.bundleContext = context;
        startBundles();
        registerGlassFishRuntime();
    }

    private void registerGlassFishRuntime() throws InterruptedException {
        bundleContext.registerService(GlassFishRuntime.class.getName(), new GlassFishRuntime() {

            List<GlassFish> gfs = new ArrayList<GlassFish>();

            // cache the value, because we can't use bundleContext after this bundle is stopped.
            Framework framework = (Framework) bundleContext.getBundle(0); // system bundle is the framework

            @Override
            public synchronized GlassFish newGlassFish(GlassFishProperties gfProps) throws GlassFishException {
                try {
                    // set env props before updating config, because configuration update may actually trigger
                    // some code to be executed which may be depending on the environment variable values.
                    setEnv(gfProps.getProperties());
                    final StartupContext startupContext = new StartupContext(gfProps.getProperties());
                    final ServiceTracker hk2Tracker = new ServiceTracker(bundleContext, Main.class.getName(), null);
                    hk2Tracker.open();
                    final Main main = (Main) hk2Tracker.waitForService(0);
                    hk2Tracker.close();
                    final ModulesRegistry mr = ModulesRegistry.class.cast(bundleContext.getService(bundleContext.getServiceReference(ModulesRegistry.class.getName())));
                    final Habitat habitat = main.createHabitat(mr, startupContext);
                    final ModuleStartup gfKernel = main.findStartupService(mr, habitat, null, startupContext);
                    GlassFish glassFish = new GlassFishImpl(gfKernel, habitat, gfProps.getProperties());
                    gfs.add(glassFish);
                    events = habitat.getComponent(Events.class);
                    events.register(GlassFishActivator.this);
                    // register GlassFish in service registry
                    bundleContext.registerService(GlassFish.class.getName(), glassFish, gfProps.getProperties());
                    return glassFish;
                } catch (BootException ex) {
                    throw new GlassFishException(ex);
                } catch (InterruptedException ex) {
                    throw new GlassFishException(ex);
                }
            }

            public synchronized void shutdown() throws GlassFishException {
                if (framework == null) {
                    return; // already shutdown
                }
                for (GlassFish gf : gfs) {
                    if (gf.getStatus() != GlassFish.Status.DISPOSED) {
                        try {
                            gf.dispose();
                        } catch (GlassFishException e) {
                            e.printStackTrace();
                        }
                    }
                }
                gfs.clear();
                try {
                    framework.stop();
                    framework.waitForStop(0);
                } catch (InterruptedException ex) {
                    throw new GlassFishException(ex);
                } catch (BundleException ex) {
                    throw new GlassFishException(ex);
                }
                shutdownInternal();
                framework = null;
                System.out.println("Completed shutdown of GlassFish runtime");
            }
        }, null);
    }

    private Properties dict2Properties(Dictionary dictionary) {
        Properties args = new Properties();
        Enumeration e = dictionary.keys();
        while (e.hasMoreElements()) {
            String k = e.nextElement().toString();
            String v = dictionary.get(k).toString();
            args.put(k, v);
        }
        return args;
    }

    public void stop(BundleContext context) throws Exception {
        // Stopping osgi-adapter will take care of stopping ModuleStartup service, release of habitat, etc.
        stopBundle("com.sun.enterprise.osgi-adapter");
    }

    private void setEnv(Properties properties) {
        final String installRootValue = properties.getProperty(com.sun.enterprise.glassfish.bootstrap.Constants.INSTALL_ROOT_PROP_NAME);
        if (installRootValue != null && !installRootValue.isEmpty()) {
            File installRoot = new File(installRootValue);
            System.setProperty(com.sun.enterprise.glassfish.bootstrap.Constants.INSTALL_ROOT_PROP_NAME, installRoot.getAbsolutePath());
            final Properties asenv = ASMainHelper.parseAsEnv(installRoot);
            for (String s : asenv.stringPropertyNames()) {
                System.setProperty(s, asenv.getProperty(s));
            }
            System.setProperty(com.sun.enterprise.glassfish.bootstrap.Constants.INSTALL_ROOT_URI_PROP_NAME, installRoot.toURI().toString());
        }
        final String instanceRootValue = properties.getProperty(com.sun.enterprise.glassfish.bootstrap.Constants.INSTANCE_ROOT_PROP_NAME);
        if (instanceRootValue != null && !instanceRootValue.isEmpty()) {
            File instanceRoot = new File(instanceRootValue);
            System.setProperty(com.sun.enterprise.glassfish.bootstrap.Constants.INSTANCE_ROOT_PROP_NAME, instanceRoot.getAbsolutePath());
            System.setProperty(com.sun.enterprise.glassfish.bootstrap.Constants.INSTANCE_ROOT_URI_PROP_NAME, instanceRoot.toURI().toString());
        }
    }

    private void startBundles() {
        // 1. Start cofigadmin as we depend on its service.
        startConfigAdmin();

        // 2. Start osgi-resource-locator bundle. We need to start this bundle here, because this bundle
        // may be required by other bundles like StAX and Jersey. Please note, this bundle uses
        // LAZY-ACTIVATION, so merely starting it has negligible effect on startup time.
        startBundle("org.glassfish.hk2.osgi-resource-locator");

        // 3.  Start osgi-adapter (this is a hk2 bootstrap module)
        startBundle("com.sun.enterprise.osgi-adapter");
    }

    /**
     * For various reasons, we may like to start some OSGi bundles only after server is started.
     * e.g., we set shell port as a system property in domain.xml, so to guarantee that it is set as a system
     * property in the environment, we would like to wait for server to be ready. More over, starting these
     * non-essential bundles after server is ready gives faster start up time.
     */
    private void startPostStartupBundles() {
        // a 3.0.x domain won't have the necessary gosh.args property set, so for seamless upgrade,
        // we need to set this property. See https://glassfish.dev.java.net/issues/show_bug.cgi?id=14173
        // for more details. It is just simpler to do than registering a UpgradeService to do the needful.
        final String gosh_args = "gosh.args";
        if (bundleContext.getProperty(gosh_args) == null) {
            final String gosh_args_value = "--nointeractive";
            System.setProperty(gosh_args, gosh_args_value);
        }
        String additionalOSGiBundlesToStart = bundleContext.getProperty("org.glassfish.additionalOSGiBundlesToStart");
        if (additionalOSGiBundlesToStart == null) {
            // a 3.0.x domain won't have the necessary property set, so for seamless upgrade,
            // we need to set this property. It is just simpler to do than registering a UpgradeService to do the needful.
            additionalOSGiBundlesToStart =
                    "org.apache.felix.shell, " +
                    "org.apache.felix.gogo.runtime, " +
                    "org.apache.felix.gogo.shell, " +
                    "org.apache.felix.gogo.command";
        }
        for (String bsn : additionalOSGiBundlesToStart.split(",")) {
            startBundle(bsn.trim());
        }
    }

    /**
     * Start config admin if not already started
     */
    private void startConfigAdmin() {
        ServiceReference sr = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
        if (sr == null) {
            startBundle("org.apache.felix.configadmin");
        } else {
            bundleContext.ungetService(sr);
        }
    }

    /**
     * Start a bundle if not already started
     */
    private void startBundle(String bsn) {
        Bundle b = findBundle(bsn);
        if (b != null) {
            try {
                b.start(Bundle.START_TRANSIENT);
            } catch (BundleException e) {
                System.out.println("Failed to start: " + bsn);
                e.printStackTrace();
            }
        } else {
            System.out.println("Can't locate bundle: " + bsn);
        }
    }

    /**
     * Stop a bundle if not already stopped
     */
    private void stopBundle(String bsn) {
        Bundle b = findBundle(bsn);
        if (b != null) {
            try {
                b.stop(Bundle.STOP_TRANSIENT);
            } catch (BundleException e) {
                System.out.println("Failed to stop: " + bsn);
                e.printStackTrace();
            }
        } else {
            System.out.println("Can't locate bundle: " + bsn);
        }
    }

    private Bundle findBundle(String bsn) {
        for (Bundle b : bundleContext.getBundles()) {
            if (bsn.equals(b.getSymbolicName())) return b;
        }
        return null;
    }

    public void event(Event event) {
        if (event.is(EventTypes.SERVER_READY)) {
            if (events != null) {
                events.unregister(this);
                events = null;
            }
            // This is a synchronous event. So, spawn a new thread to start all the post startup bundles
            // as we don't want to do such optional activity in the kernel main thread.
            new Thread() {
                @Override
                public void run() {
                    startPostStartupBundles();
                }
            }.start();
        }
    }
}
