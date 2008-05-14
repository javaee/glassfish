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

import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.Which;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class ASMainOSGi {

    /**
     * The class loader used to intialize the OSGi platform.
     * This will also be the parent class loader of all the OSGi bundles.
     */
    protected ClassLoader launcherCL;

    protected Logger logger;

    protected ASMainHelper helper;

    protected StartupContext context;

    protected File bootstrapFile; // glassfish/modules/glassfish-$version.jar

    protected File glassfishDir; // glassfish/

    protected File domainDir; // default is glassfish/domains/domain1

    protected File fwDir; // OSGi framework directory

    //
    /**
     * The following jars in the modules directory are to be added to {@link #launcherCL}.
     *
     * These are the prefixes of the jar names (to avoid hard-coding versions),
     * and so for entry "foo", we'll find "foo*.jar"
     */
    private String[] additionalJars = {
        "wstx-asl-*.jar" // needed by config module in HK2
    };

    private static final String javaeeJarPath = "modules/javax.javaee-10.0-SNAPSHOT.jar";

    // on jdk5, without the javaee, I need this...
    private static final String staxJarPath = "modules/stax-api-1.0-2.jar";
    

    public ASMainOSGi(Logger logger, String... args) {
        this.logger = logger;
        findBootstrapFile();
        glassfishDir = bootstrapFile.getParentFile().getParentFile(); //glassfish/
        helper = new ASMainHelper(logger);
        context = new StartupContext(bootstrapFile, args);
        helper.parseAsEnv(glassfishDir);
        domainDir = helper.getDomainRoot(context);
        helper.verifyDomainRoot(domainDir);
        setFwDir();
    }

    protected abstract void setFwDir();

    public ASMainOSGi(String... args) {
        this(Logger.getAnonymousLogger(),args);
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
            setupLauncherClassLoader();
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
     * common classloader (for loading javaee API and jdk tools.jar)
     * library class loader (for glassfish/lib and domain_dir/lib)
     * framework class loader (For loading OSGi framework classes)
     */
    private void setupLauncherClassLoader() throws Exception {
        ClassLoader commonCL = createCommonClassLoader(ClassLoader.getSystemClassLoader().getParent());
        // ClassLoader libCL = helper.setupSharedCL(commonCL, getSharedRepos());

        try {
            ClassPathBuilder cpb = new ClassPathBuilder(commonCL);
            addFrameworkJars(cpb);

            File moduleDir = context.getRootDirectory().getParentFile();
            cpb.addGlob(moduleDir,additionalJars);

            this.launcherCL = cpb.createExtensible(getSharedRepos());
        } catch (IOException e) {
            throw new Error(e);
        }
        Thread.currentThread().setContextClassLoader(launcherCL);
    }

    /**
     * Creates a class loader from JavaEE API and tools.jar.
     */
    protected ClassLoader createCommonClassLoader(ClassLoader parent) {
        try {
            ClassPathBuilder cpb = new ClassPathBuilder(parent);

            File modulesDir = new File(glassfishDir, "modules");
            cpb.addGlob(modulesDir, "javax.javaee-*.jar");

            // on jdk 1.5, I need stax apis, might be duplicated if javax.javaee is present.
            if (System.getProperty("java.version").compareTo("1.6")<0)
                cpb.addGlob(modulesDir, "stax-api-*.jar");

            findDerbyClient(cpb);

            File jdkToolsJar = helper.getJDKToolsJar();
            if (jdkToolsJar.exists()) {
                cpb.addJar(jdkToolsJar);
            } else {
                // on the mac, it happens all the time
                logger.fine("JDK tools.jar does not exist at " + jdkToolsJar);
            }

            return cpb.create();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private void findDerbyClient(ClassPathBuilder cpb) throws IOException {
        String derbyHome = System.getProperty("AS_DERBY_INSTALL");
        File derbyLib=null;
        if (derbyHome!=null) {
            derbyLib = new File(derbyHome, "lib");
        }
        if (derbyLib==null || !derbyLib.exists()) {
            // maybe the jdk...
            if (System.getProperty("java.version").compareTo("1.6")>0) {
                File jdkHome = new File(System.getProperty("java.home"));
                derbyLib = new File(jdkHome, "db/lib");
            }
        } 
        if (!derbyLib.exists()) {
            logger.info("Cannot find javadb client jar file, jdbc driver not available");
            return;
        }
        cpb.addGlob(derbyLib,"derbyclient*.jar");
    }

    private void findBootstrapFile() {
        try {
            bootstrapFile = Which.jarFile(getClass());
        } catch (IOException e) {
            throw new RuntimeException("Cannot get bootstrap path from "
                    + getClass() + " class location, aborting");
        }
    }

    private List<Repository> getSharedRepos() {
        List<Repository> libs = new ArrayList<Repository>();
        // by default we add lib directory repo
        File libDir = new File(glassfishDir, "lib");
        logger.fine("Path to library directory is " + libDir);
        if (libDir.exists()) {
            // Note: we pass true as the last argument, which indicates that the
            // timer thread for this repo will be daemon thread. This is necessary
            // because we don't get any notification about server shutdown. Hence,
            // if we start a non-daemon thread, the server process won't exit atall.
            Repository libRepo = new DirectoryBasedRepository("lib", libDir, true);
            try {
                libRepo.initialize();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            libs.add(libRepo);
        } else {
            logger.info(libDir + " does not exist");
        }
        // do we have a domain lib ?
        File domainlib = new File(domainDir, "lib");
        logger.fine("Path to domain library directory is " + domainlib);
        if (domainlib.exists()) {
            // Note: we pass true as the last argument, which indicates that the
            // timer thread for this repo will be daemon thread. This is necessary
            // because we don't get any notification about server shutdown. Hence,
            // if we start a non-daemon thread, the server process won't exit atall.
            Repository domainLib = new DirectoryBasedRepository("domnainlib", domainlib, true);
            try {
                domainLib.initialize();
                libs.add(domainLib);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while initializing domain lib repository", e);
            }
        } else {
            logger.info(domainlib + " does not exist");
        }
        return libs;
    }


}
