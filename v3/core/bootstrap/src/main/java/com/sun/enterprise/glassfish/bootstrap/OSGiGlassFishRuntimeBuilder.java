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
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.util.tracker.ServiceTracker;

import java.util.Properties;
import java.util.ServiceLoader;

/**
 * This class is responsible for
 * a) setting up OSGi framework,
 * b) installing glassfish bundles,
 * c) starting the primordial GlassFish bundle,
 * d) obtaining a reference to GlassFishRuntime OSGi service.
 * <p/>
 * Steps #b & #c are handled via {@link AutoProcessor}.
 * We specify our provisioning bundle details in the properties object that's used to boostrap
 * the system. AutoProcessor installs and starts such bundles, The provisioning bundle is also configured
 * via the same properties object.
 * <p/>
 * It is the responsibility of the caller to pass in a properly populated properties object.
 * <p/>
 * <p/>
 * This class is registered as a provider of RuntimeBuilder using META-INF/services file.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiGlassFishRuntimeBuilder implements GlassFishRuntime.RuntimeBuilder {
    private Framework framework;
    private Properties properties;

    /**
     * Default constructor needed for meta-inf/service lookup to work
     */
    public OSGiGlassFishRuntimeBuilder() {}

    public GlassFishRuntime build(Properties properties) throws Exception {
        this.properties = properties;
        launchOSGiFrameWork();
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
        ServiceTracker tracker = new ServiceTracker(context, GlassFishRuntime.class.getName(), null);
        tracker.open(true);
        return GlassFishRuntime.class.cast(tracker.waitForService(0));
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

            // Call auto-processor - this is where our provisioning bundle is installed and started.
            AutoProcessor.process(properties, framework.getBundleContext());
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

    private static void debug(String s) {
        System.out.println("OSGiGlassFishRuntime: " + s);
    }

}
