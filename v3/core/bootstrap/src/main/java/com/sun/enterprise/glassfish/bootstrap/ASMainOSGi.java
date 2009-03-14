/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.enterprise.module.bootstrap.ArgumentManager;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.Which;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.MalformedURLException;
import java.net.URI;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class ASMainOSGi extends AbstractMain {

    /**
     * The class loader used to intialize the OSGi platform.
     * This will also be the parent class loader of all the OSGi bundles.
     */
    protected ClassLoader launcherCL;

    protected Logger logger;

    protected ASMainHelper helper;

    protected StartupContext context;

    protected File glassfishDir; // glassfish/

    protected File domainDir; // default is glassfish/domains/domain1

    protected File fwDir; // OSGi framework directory

    //
    /**
     * The following jars in the modules directory are to be added to {@link #launcherCL}.
     * <p/>
     */
    private String[] additionalJars = {
    };


    public ASMainOSGi(Logger logger, String... args) {
        this.logger = logger;
        glassfishDir = bootstrapFile.getParentFile().getParentFile(); //glassfish/
        helper = new ASMainHelper(logger);

        context = new StartupContext(bootstrapFile, args);
        // we need to save the startup context as there is no easy way to pass it through felix
        // to our bundles.

        Properties properties = ArgumentManager.argsToMap(args);
        properties.put(StartupContext.TIME_ZERO_NAME, (new Long(System.currentTimeMillis())).toString());
        String lineformat = null;
        try {
            // when we switch to jdk6 and up as minimum platform, use following apis.
            //Writer writer = new StringWriter();
            //properties.store(writer, null);
            //lineformat = writer.toString();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            properties.store(os, null);
            lineformat = os.toString();
        } catch (IOException e) {
            logger.info("Could not save startup parameters, will start with none");
        }
        if (lineformat!=null) {
            System.setProperty("glassfish.startup.context", lineformat); // NO I18N                            
        }

        helper.parseAsEnv(glassfishDir);
        domainDir = helper.getDomainRoot(context);
        helper.verifyDomainRoot(domainDir);
        setFwDir();
    }

    protected abstract void setFwDir();

    public ASMainOSGi(String... args) {
        this(Logger.getAnonymousLogger(), args);
    }

    /**
     * Adds the jar files of the OSGi platform to the given {@link ClassPathBuilder}
     */
    protected abstract void addFrameworkJars(ClassPathBuilder cpb) throws IOException;

    protected abstract void launchOSGiFW() throws Exception;

    public void run() {
        try {
            System.setProperty("org.jvnet.hk2.osgiadapter.contextrootdir",
                    new File(glassfishDir, "modules").getAbsolutePath());

            /* Set a system property called com.sun.aas.installRootURI.
             * This property is used in felix/conf/config.properties to
             * to auto-start some modules. We can't use com.sun.aas.installRoot
             * because that com.sun.aas.installRoot is a directory path, where as
             * we need a URI.
             */
            String installRoot = System.getProperty("com.sun.aas.installRoot");
            URI installRootURI = new File(installRoot).toURI();
            System.setProperty("com.sun.aas.installRootURI", installRootURI.toString());
            String instanceRoot = System.getProperty("com.sun.aas.instanceRoot");
            URI instanceRootURI = new File(instanceRoot).toURI();
            System.setProperty("com.sun.aas.instanceRootURI", instanceRootURI.toString());            setupLauncherClassLoader();
            launchOSGiFW();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            File moduleDir = context.getRootDirectory().getParentFile();
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
    protected void addJDKToolsJar(ClassPathBuilder cpb) {
        try {

            File jdkToolsJar = helper.getJDKToolsJar();
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
        String derbyHome = System.getProperty("AS_DERBY_INSTALL");
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

    @Override
    long getSettingsLastModification() {
        return getLastModified(fwDir, 0);
    }

    @Override
    Logger getLogger() {
        return logger;
    }

    @Override
    boolean createCache(File cacheDir) throws IOException {
        return cacheDir.mkdirs();
    }

}
