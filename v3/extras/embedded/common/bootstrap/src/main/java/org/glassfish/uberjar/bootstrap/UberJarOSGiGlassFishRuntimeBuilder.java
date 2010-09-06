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

package org.glassfish.uberjar.bootstrap;

import com.sun.enterprise.glassfish.bootstrap.OSGiFrameworkLauncher;
import org.glassfish.simpleglassfishapi.Constants;
import org.glassfish.simpleglassfishapi.GlassFishRuntime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.launch.Framework;
import org.osgi.util.tracker.ServiceTracker;

import java.io.File;
import java.net.URI;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bhavanishankar@dev.java.net
 */

public class UberJarOSGiGlassFishRuntimeBuilder implements GlassFishRuntime.RuntimeBuilder {

    private Framework framework;

    public boolean handles(Properties props) {
        // default is Felix
        Constants.Platform platform =
                Constants.Platform.valueOf(props.getProperty(
                        Constants.PLATFORM_PROPERTY_KEY, Constants.Platform.Felix.name()));
        logger.info("platform = " + platform);
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
        if (framework != null) {
            framework.stop();
            framework.waitForStop(0);
            logger.info("EmbeddedOSGIRuntimeBuilder.destroy, stopped framework " + framework);
        } else {
            logger.info("EmbeddedOSGIRuntimeBuilder.destroy called");
        }
    }

    private static Logger logger = Logger.getLogger("embedded-glassfish");

    public static final String AUTO_START_BUNDLES_PROP =
            "org.glassfish.embedded.osgimain.autostartBundles";


    private static final String UBER_JAR_URI = "org.glassfish.embedded.osgimain.jarURI";

    public GlassFishRuntime build(Properties props) throws Exception {

        String uberJarURI = props.getProperty(UBER_JAR_URI);

        logger.info("EmbeddedOSGIRuntimeBuilder.build, uberJarUri = " + uberJarURI);

        URI jar = uberJarURI != null ? new URI(uberJarURI) : Util.whichJar(GlassFishRuntime.class);

        String instanceRoot = props.getProperty(Constants.INSTALL_ROOT_PROP_NAME);
        String installRoot = props.getProperty(Constants.INSTALL_ROOT_PROP_NAME);

        if (installRoot == null) {
            installRoot = getDefaultInstallRoot();
            props.setProperty(Constants.INSTALL_ROOT_PROP_NAME, installRoot);
            props.setProperty(Constants.INSTALL_ROOT_URI_PROP_NAME,
                    new File(installRoot).toURI().toString());
        }

        if (instanceRoot == null) {
            instanceRoot = getDefaultInstanceRoot();
            props.setProperty(Constants.INSTANCE_ROOT_PROP_NAME, instanceRoot);
            props.setProperty(Constants.INSTANCE_ROOT_URI_PROP_NAME,
                    new File(instanceRoot).toURI().toString());

        }

        String platform = props.getProperty(Constants.PLATFORM_PROPERTY_KEY);
        if (platform == null) {
            platform = Constants.Platform.Felix.toString();
            props.setProperty(Constants.PLATFORM_PROPERTY_KEY, platform);
        }

//        readConfigProperties(installRoot, props);

        System.setProperty(UBER_JAR_URI, jar.toString()); // embedded-osgi-main module will need this to extract the modules.

        String osgiMainModule = "jar:" + jar.toString() + "!/uber-osgi-main.jar";
        props.setProperty("glassfish.auto.start", osgiMainModule);

        String autoStartBundleLocation = "jar:" + jar.toString() + "!/modules/installroot-builder_jar/," +
                "jar:" + jar.toString() + "!/modules/instanceroot-builder_jar/," +
                "jar:" + jar.toString() + "!/modules/kernel_jar/"; // TODO :: was modules/glassfish_jar

        if (isOSGiEnv()) {
            autoStartBundleLocation = autoStartBundleLocation +
                    ",jar:" + jar.toString() + "!/modules/osgi-modules-uninstaller_jar/";
        }

        props.setProperty(AUTO_START_BUNDLES_PROP, autoStartBundleLocation);
        System.setProperty(AUTO_START_BUNDLES_PROP, autoStartBundleLocation);

        System.setProperty(Constants.INSTALL_ROOT_PROP_NAME, installRoot);
        System.setProperty(Constants.INSTANCE_ROOT_PROP_NAME, instanceRoot);

        props.setProperty("org.osgi.framework.system.packages.extra",
                "org.glassfish.simpleglassfishapi; version=3.1");

//        props.setProperty(org.osgi.framework.Constants.FRAMEWORK_BUNDLE_PARENT,
//                org.osgi.framework.Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
//        props.setProperty("org.osgi.framework.bootdelegation", "org.jvnet.hk2.component, " +
//                "org.jvnet.hk2.component.*," +
//                "org.jvnet.hk2.annotations," +
//                "org.jvnet.hk2.annotations.*");
//        props.setProperty("org.osgi.framework.bootdelegation", "*");

        props.setProperty("org.osgi.framework.storage", instanceRoot + "/osgi-cache/Felix");
//        }

        logger.logp(Level.INFO, "EmbeddedOSGIRuntimeBuilder", "build",
                "Building file system {0}", props);

        try {
            if (!isOSGiEnv()) {
                final OSGiFrameworkLauncher fwLauncher = new OSGiFrameworkLauncher(props);
                framework = fwLauncher.launchOSGiFrameWork();
                return fwLauncher.getService(GlassFishRuntime.class);
            } else {
                BundleContext context = ((BundleReference) (getClass().getClassLoader())).
                        getBundle().getBundleContext();
                Bundle autostartBundle = context.installBundle(props.getProperty("glassfish.auto.start"));
                autostartBundle.start(Bundle.START_TRANSIENT);
                logger.info("Started autostartBundle " + autostartBundle);
                return getService(GlassFishRuntime.class, context);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception(t);
//            return null;
        }
    }

    private String getDefaultInstallRoot() {
        String userDir = System.getProperty("user.home");
        return new File(userDir, ".glassfishv3-embedded").getAbsolutePath();
    }

    private String getDefaultInstanceRoot() {
        String userDir = System.getProperty("user.home");
        String fs = File.separator;
        return new File(userDir, ".glassfishv3-embedded" + fs + "domains" + fs + "domain1").getAbsolutePath();
    }

    private boolean isOSGiEnv() {
        return (getClass().getClassLoader() instanceof BundleReference);
    }

    public <T> T getService(Class<T> type, BundleContext context) throws Exception {
        ServiceTracker tracker = new ServiceTracker(context, type.getName(), null);
        try {
            tracker.open(true);
            return type.cast(tracker.waitForService(0));
        } finally {
            tracker.close(); // no need to track further
        }
    }

}
