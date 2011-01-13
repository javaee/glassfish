/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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


package com.sun.enterprise.glassfish.bootstrap;

import org.glassfish.embeddable.spi.RuntimeBuilder;
import org.glassfish.embeddable.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;
import org.osgi.util.tracker.ServiceTracker;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * This {@link org.glassfish.embeddable.spi.RuntimeBuilder} is responsible for setting up a {@link GlassFishRuntime}
 * when user has a regular installation of GlassFish and they want to embed GlassFish in an existing OSGi runtime.
 *
 * It sets up the runtime like this:
 * 1. Installs all bundles located in glassfish modules directory.
 * 2. Starts the main bundle of GlassFish. Main bundle is identified by Bundle-SymbolicName
 * org.glassfish.core.kernel. This main bundle then is used to start any necessary bundle. This main bundle
 * registers a service of type GlassFishRuntime.
 * 3. It waits for GlassFishRuntime service to be available and returns when found.
 *
 * @see #build(org.glassfish.embeddable.BootstrapProperties)
 * @see #handles(org.glassfish.embeddable.BootstrapProperties)
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class EmbeddedOSGiGlassFishRuntimeBuilder implements RuntimeBuilder {

    private BundleContext bundleContext;

    private String installRoot;

    private static final String mainBundleName = "org.glassfish.core.kernel";

    /* package */
    static final String BUILDER_NAME_PROPERTY = "GlassFish.BUILDER_NAME";

    private static final String JAR_EXT = ".jar";

    public boolean handles(BootstrapProperties bsProps) {
        return EmbeddedOSGiGlassFishRuntimeBuilder.class.getName().
                equals(bsProps.getProperties().getProperty(BUILDER_NAME_PROPERTY));
    }

    public GlassFishRuntime build(BootstrapProperties bsProps) throws GlassFishException {
        this.bundleContext = getBundleContext();
        installRoot = bsProps.getInstallRoot();

        // Install all gf bundles, start the primordial bundle and wait for GlassFishRuntime service to be available
        // We install the bundles in two installments, viz:
        // 1. First we install and start the endorsed bundles,
        // 2. Then we gor for the rest.
        // This way, endorsed bundles becomes candidates for providers instead of system bundle for a matching pkg.
        installAndStartEndorsedBundles();
        installBundles();
        configureBundles();
        startBundles();
        ServiceTracker st = new ServiceTracker(bundleContext, GlassFishRuntime.class.getName(), null);
        try {
            st.open();
            return GlassFishRuntime.class.cast(st.waitForService(0));
        }catch(Exception e) {
            throw new GlassFishException(e);
        } finally {
            st.close();
        }
    }

    private void installAndStartEndorsedBundles() {
        List<Bundle> endorsedBundles = new ArrayList<Bundle>();
        File endorsedModulesDir = new File(installRoot, "modules/endorsed/");
        for (File f : endorsedModulesDir.listFiles()) {
            if (f.getName().endsWith(JAR_EXT)) {
                try {
                    endorsedBundles.add(this.bundleContext.installBundle(f.toURI().toString()));
                } catch (BundleException e) {
                    e.printStackTrace();
                }
            }
        }
        for (Bundle b : endorsedBundles) {
            try {
                b.start(Bundle.START_TRANSIENT);
            } catch (BundleException e) {
                e.printStackTrace();
            }
        }
    }

    private void configureBundles() {
        // Set this, because some stupid downstream code may be relying on this property
        System.setProperty(Constants.PLATFORM_PROPERTY_KEY, "GenericOSGi");
    }

    /**
     * Install all bundles returned by {@link #findJars}.
     */
    private void installBundles() {
        for (File jar : findJars()) {
            try {
                bundleContext.installBundle(jar.toURI().toString());
            } catch (BundleException e) {
                // continue processing after logging the exception
                // e.printStackTrace();
                System.out.println("Exception installing " + jar.toURI() + " : " + e);
            }
        }
    }

    /**
     * Returns a list of jar files found in modules directory and subdirectories under that.
     * Any file with extension .jar is considered as a jar file.
     * Exclude autostart directory, as that's taken care of by fileinstall.
     *
     * @return a list of jar files
     */
    private List<File> findJars() {
        File modulesDir = new File(installRoot, "modules/");
        final File autostartModulesDir = new File(modulesDir, "autostart/");
        final File endorsedModulesDir = new File(modulesDir, "endorsed/");
        final List<File> jars = new ArrayList<File>();
        modulesDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory() && (!pathname.equals(autostartModulesDir) && !pathname.equals(endorsedModulesDir))) {
                    pathname.listFiles(this);
                } else if (pathname.getName().endsWith(JAR_EXT)) {
                    jars.add(pathname);
                }
                return false;
            }
        });
        return jars;
    }

    /**
     * Starts GlassFish primordial bundle
     */
    private void startBundles() {
        for (Bundle b : bundleContext.getBundles()) {
            if (mainBundleName.equals(b.getSymbolicName())) {
                try {
                    b.start(Bundle.START_TRANSIENT);
                } catch (BundleException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private BundleContext getBundleContext() {
        return BundleReference.class.cast(getClass().getClassLoader()).getBundle().getBundleContext();
    }
}
