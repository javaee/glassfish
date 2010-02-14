/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.enterprise.module.bootstrap.PlatformMain;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.logging.Logger;
import java.net.MalformedURLException;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Jerome.Dochez@Sun.COM
 */
public abstract class ASMainOSGi extends PlatformMain {

    // TODO(Sahoo): Stop adding derby jars to classpath

    private Properties ctx;

    protected File glassfishDir; // glassfish/

    protected File domainDir; // default is glassfish/domains/domain1

    /**
     * The class loader used to intialize the OSGi platform.
     * This will also be the parent class loader of all the OSGi bundles.
     */
    protected ClassLoader launcherCL;

    protected File fwDir; // OSGi framework directory

    //
    /**
     * The following jars in the modules directory are to be added to {@link #launcherCL}.
     * <p/>
     */
    private String[] additionalJars = {
    };

    public void start(Properties ctx) throws Exception {
        this.ctx = ctx;
        glassfishDir = StartupContextUtil.getInstallRoot(ctx);
        domainDir = StartupContextUtil.getInstanceRoot(ctx);
        setFwDir();
        setupLauncherClassLoader();
        configureEnvironment();
        launchOSGiFW();
    }

    protected abstract void setFwDir();

    /**
     * Adds the jar files of the OSGi platform to the given {@link ClassPathBuilder}
     */
    protected abstract void addFrameworkJars(ClassPathBuilder cpb) throws IOException;

    protected abstract void launchOSGiFW() throws Exception;

    protected void configureEnvironment() throws Exception
    {
        // Store the context as a system property so that GlassFish activator can
        // can retrieve it to configure the runtime.
        try {
            Writer writer = new StringWriter();
            ctx.store(writer, null);
            System.setProperty(Constants.ARGS_PROP, writer.toString());
        }
        catch (IOException e) {
            logger.info("Could not save startup parameters, will start with none");
            System.setProperty(Constants.ARGS_PROP, "");
        }

        /* Set a system property called com.sun.aas.installRootURI.
         * This property is used in felix/conf/config.properties and possibly
         * in other OSGi framework's config file to auto-start some modules.
         * We can't use com.sun.aas.installRoot,
         * because that com.sun.aas.installRoot is a directory path, where as
         * we need a URI.
         */
        System.setProperty("com.sun.aas.installRootURI", glassfishDir.toURI().toString());
        System.setProperty("com.sun.aas.instanceRootURI", domainDir.toURI().toString());

        // Set up cache directories. We have two kinds of caches: one for HK2 and another for OSGi
        // Both are kept under same directory.

        File cacheProfileDir = getCacheDir();
        System.setProperty("org.osgi.framework.storage", cacheProfileDir.getAbsolutePath());
        System.setProperty(Constants.HK2_CACHE_DIR, cacheProfileDir.getAbsolutePath()); // hk2 inhabitants cache

        configureProvisioningBundle();
    }

    protected final File getCacheDir() {return new File(domainDir, getPreferedCacheDir());}

    protected abstract String getPreferedCacheDir();

    /**
     * This is where we configure our provisioning bundle (i.e., osgi-main bundle).
     */
    protected void configureProvisioningBundle() {
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

    /**
     * This method is responsible for setting up the class loader hierarchy and
     * setting the context class loader as well.
     * Our hierarchy looks like this:
     * bootstrap class loader (a.k.a. null)
     * extension class loader (for processing contents of -Djava.ext.dirs)
     * launcher classloader (for loading jdk tools.jar, derby classes (why?) and
     * OSGi framework classes)
     */
    private void setupLauncherClassLoader() throws Exception {
        ClassLoader extCL = ClassLoader.getSystemClassLoader().getParent();
        ClassPathBuilder cpb = new ClassPathBuilder(extCL);

        try {
            addFrameworkJars(cpb);
            addJDKToolsJar(cpb);
            findDerbyClient(cpb);
            File moduleDir = new File(glassfishDir, "modules/");
            cpb.addGlob(moduleDir, additionalJars);
            this.launcherCL = cpb.create();
        } catch (IOException e) {
            throw new Error(e);
        }
        Thread.currentThread().setContextClassLoader(launcherCL);
    }

    /**
     * Adds JDK tools.jar to classpath.
     */
    private void addJDKToolsJar(ClassPathBuilder cpb) {
        try {

            File jdkToolsJar = ASMainHelper.getJDKToolsJar();
            if (jdkToolsJar.exists()) {
                cpb.addJar(jdkToolsJar);
            } else {
                // on the mac, it happens all the time
                logger.fine("JDK tools.jar does not exist at " + jdkToolsJar);
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private void findDerbyClient(ClassPathBuilder cpb) throws IOException {
        // Sahoo: Why do we have to add derby to this class loader? Find out from Jerome.
        String derbyHome = new ASMainHelper(Logger.getAnonymousLogger()).parseAsEnv(glassfishDir).getProperty("AS_DERBY_INSTALL");
        File derbyLib = null;
        if (derbyHome != null) {
            derbyLib = new File(derbyHome, "lib");
        }
        if (derbyLib == null || !derbyLib.exists()) {
            // maybe the jdk...
            if (System.getProperty("java.version").compareTo("1.6") > 0) {
                File jdkHome = new File(System.getProperty("java.home"));
                derbyLib = new File(jdkHome, "../db/lib");
            }
        }
        if (!derbyLib.exists()) {
            logger.info("Cannot find javadb client jar file, jdbc driver not available");
            return;
        }
        // Add all derby jars, as embedded driver is one jar and network driver
        // is in another.
        cpb.addGlob(derbyLib, "derby*.jar");
    }

}
