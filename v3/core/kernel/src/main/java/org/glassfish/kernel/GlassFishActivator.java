/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.simpleglassfishapi.GlassFish;
import org.glassfish.simpleglassfishapi.GlassFishRuntime;
import org.jvnet.hk2.component.Habitat;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

import java.io.File;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

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
        final ServiceTracker hk2Tracker = new ServiceTracker(bundleContext, Main.class.getName(), null);
        hk2Tracker.open();
        bundleContext.registerService(GlassFishRuntime.class.getName(), new GlassFishRuntime() {
            @Override
            public GlassFish newGlassFish(Properties properties) throws Exception {
                // set env props before updating config, because configuration update may actually trigger
                // some code to be executed which may be depending on the environment variable values.
                setEnv(properties);
                final StartupContext startupContext = new StartupContext(properties);
                final Main main = (Main) hk2Tracker.waitForService(0);
                final ModulesRegistry mr = ModulesRegistry.class.cast(
                        bundleContext.getService(bundleContext.getServiceReference(ModulesRegistry.class.getName())));
                final Habitat habitat = main.createHabitat(mr, startupContext);
                final ModuleStartup gfKernel = main.findStartupService(mr, habitat, null, startupContext);
                System.out.println("gfKernel = " + gfKernel);
                GlassFish glassFish = new GlassFishImpl(gfKernel, habitat);
                events = habitat.getComponent(Events.class);
                events.register(GlassFishActivator.this);
                // register GlassFish in service registry
                bundleContext.registerService(GlassFish.class.getName(), glassFish, properties);
                return glassFish;
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
        startBundle("org.apache.felix.shell");
        startBundle("org.apache.felix.org.apache.felix.shell.remote");
        startBundle("org.apache.felix.fileinstall");
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
        if (event.is(EventTypes.SERVER_STARTUP)) {
            startPostStartupBundles();
            if (events != null) {
                events.unregister(this);
                events = null;
            }
        }
    }
}
