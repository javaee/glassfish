/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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


package com.sun.enterprise.glassfish.bootstrap;

import org.glassfish.experimentalgfapi.GlassFishRuntime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.util.tracker.ServiceTracker;

import java.io.File;
import java.net.URI;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * This class is responsible for
 * a) setting up OSGi framework,
 * b) installing glassfish bundles,
 * c) starting the primordial GlassFish bundle,
 * d) obtaining a reference to GlassFishRuntime OSGi service.
 *
 * It is the responsibility of the caller to pass in a properly populated properties object.
 *
 * <p/>
 * This class is registered as a provider of RuntimeBuilder.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiGlassFishRuntimeBuilder implements GlassFishRuntime.RuntimeBuilder {
    private Framework framework;
    private URI installRoot;
    private Properties properties;

    /**
     * Default constructor needed for meta-inf/service lookup to work
     */
    public OSGiGlassFishRuntimeBuilder() {}

    public GlassFishRuntime build(Properties properties) throws Exception {
        this.properties = properties;
        installRoot = URI.create(Util.getPropertyOrSystemProperty(properties, Constants.INSTALL_ROOT_URI_PROP_NAME));
        launchOSGiFrameWork();
        setupBundles();
        return getRuntime();
    }

    public boolean handles(org.glassfish.experimentalgfapi.Constants.Platform platform) {
        // TODO(Sahoo): Add support for generic OSGi platform
        switch (platform) {
            case Felix:
            case Equinox:
            case Knopflerfish:
                return true;
        }
        return false;
    }

    public void destroy() throws Exception {
        framework.stop();
        framework.waitForStop(0);
    }

    private GlassFishRuntime getRuntime() throws Exception {
        final BundleContext context = framework.getBundleContext();
//        ServiceReference[] refs = context.getAllServiceReferences(GlassFishRuntime.class.getName(), null);
//        if (refs.length == 1) {
//            return (GlassFishRuntime) context.getService(refs[0]);
//        }
//        throw new RuntimeException("Not able to get hold of GlassFishRuntime OSGi service");
        ServiceTracker tracker = new ServiceTracker(context, GlassFishRuntime.class.getName(), null);
        tracker.open(true);
        return GlassFishRuntime.class.cast(tracker.waitForService(0));
    }

    protected void setupBundles() throws Exception {
        BundleInstaller bi = getBundleInstaller();
        bi.install();
    }

    /**
     * Extension point for plugging in different types of Provisioning System
     * Use properties object as input to decide.
     *
     * @return BundleInstaller appropriate for this environment
     */
    private BundleInstaller getBundleInstaller() {
        BundleInstaller bi = new OSGiMainBundleInstaller();
        return bi;
    }

    protected void launchOSGiFrameWork() throws Exception {
        if (!isOSGiEnv()) {
            // Start an OSGi framework
            ServiceLoader<FrameworkFactory> frameworkFactories = ServiceLoader.load(FrameworkFactory.class,

                    getClass().getClassLoader());
            for (FrameworkFactory ff : frameworkFactories) {
                framework = ff.newFramework(properties);
            }
            if (framework == null) {
                throw new RuntimeException("No OSGi framework in classpath");
            } else {
                framework.init();
                debug("Initialized + " + framework);
            }
            framework.start();
        }
    }

    /**
     * Determine if we we are operating in OSGi env. We do this by checking what class loader is used to
     * this class.
     *
     * @return false if we are already called in the context of OSGi framework, else true.
     */
    private boolean isOSGiEnv() {
        return (getClass().getClassLoader() instanceof BundleReference);
    }

    /**
     * Abstraction to install and start GlassFish bundles.
     * Different implementations can exist, viz: one can use our osgi-main.jar to do the work,
     * some other implementation can use OBR and something else can use another custom implementation
     * to install bundles from JarFile entries.
     */
    abstract class BundleInstaller {
        abstract void install() throws Exception;
    }

    /**
     * Uses osgi-,main.jar to install GlassFish bundles and start
     */
    class OSGiMainBundleInstaller extends BundleInstaller {
        File glassfishDir;

        public void install() throws Exception {
            glassfishDir = new File(installRoot);
            if (!glassfishDir.isDirectory()) {
                throw new RuntimeException("OSGiMainBundleInstaller can only install from a regular glassfish installtion");
            }
            URI osgiMain = installRoot.resolve("modules/osgi-main.jar");
            Bundle b = framework.getBundleContext().installBundle(osgiMain.toString());
            b.update(); // in case it has changed since last time
            configureProvisioningBundle();
            b.start(Bundle.START_TRANSIENT);
        }

        /**
         * This is where we configure our provisioning bundle (i.e., osgi-main bundle).
         */
        private void configureProvisioningBundle() {
            // Set the modules dir. This is used by our main bundle to locate all
            // bundles and install them
            System.setProperty("org.jvnet.hk2.osgimain.bundlesDir",
                    new File(glassfishDir, "modules/").getAbsolutePath());

            // Set the excluded dir list property so that osgi-main does not
            // install bundles from modules/autostart.
            System.setProperty("org.jvnet.hk2.osgimain.excludedSubDirs", "autostart/");

            // Set the autostart bundle list. This is used by bootstrap bundle to
            // locate the bundles and start them. The path is relative to bundles dir
            if (System.getProperty("org.jvnet.hk2.osgimain.autostartBundles") == null) {
                final String bundlePaths = "glassfish.jar";
                System.setProperty("org.jvnet.hk2.osgimain.autostartBundles", bundlePaths);
            }
        }
    }

    private static void debug(String s) {
        System.out.println("OSGiGlassFishRuntime: " + s);
    }

}
