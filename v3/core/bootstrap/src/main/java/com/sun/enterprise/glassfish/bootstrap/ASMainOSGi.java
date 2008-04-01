package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;
import com.sun.enterprise.module.bootstrap.StartupContext;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.net.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class ASMainOSGi {

    private String[] args;

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

    // For the moment, we need to add the following jars to classpath as well:
    private String[] additionalJars = {
            "wstx-asl-3.2.3.jar", // needed by config module in HK2
            /* Commented this since we put javaee jar in higher class loader.
            "stax-api-1.0-2.jar", // needed by config module in HK2
             */
            "tiger-types-1.0.jar", // needed by config module in HK2
            "jmxremote_optional-1.0_01-ea.jar" // until we make this a module 
    };

    private static final String javaeeJarPath = "modules/javax.javaee-10.0-SNAPSHOT.jar";

    public ASMainOSGi(Logger logger, String... args) {
        this.logger = logger;
        this.args = args;
        findBootstrapFile();
        glassfishDir = bootstrapFile.getParentFile().getParentFile(); //glassfish/
        helper = new ASMainHelper(logger);
        context = new StartupContext(bootstrapFile, args);
        helper.parseAsEnv(glassfishDir);
        domainDir = helper.getDomainRoot(context);
        helper.verifyDomainRoot(domainDir);
    }

    public ASMainOSGi(String... args) {
        this(Logger.getAnonymousLogger());
    }

    /**
     * Returns the list of Jar files that comprise the OSGi platform
     *
     * @return
     */
    protected abstract URL[] getFWJars();

    protected abstract void launchOSGiFW(String ... args);

    public void run() {
        setupLauncherClassLoader();
        launchOSGiFW(args);
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
    private void setupLauncherClassLoader() {
        ClassLoader commonCL = createCommonClassLoader();
        ClassLoader libCL = helper.setupSharedCL(commonCL, getSharedRepos());
        List<URL> urls = new ArrayList<URL>();
        Collections.addAll(urls, getFWJars());
        try {
            File moduleDir = context.getRootDirectory().getParentFile();
            for (String jar : additionalJars) {
                URL url = new File(moduleDir, jar).toURI().toURL();
                urls.add(url);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.launcherCL = new URLClassLoader(urls.toArray(new URL[0]), libCL);
        Thread.currentThread().setContextClassLoader(launcherCL);
    }

    private ClassLoader createCommonClassLoader() {
        try {
            List<URL> urls = new ArrayList<URL>();
            File javaeeJar = new File(glassfishDir, javaeeJarPath);
            if (!javaeeJar.exists()) {
                throw new RuntimeException(javaeeJar + " does not exist.");
            }
            urls.add(javaeeJar.toURI().toURL());
            File jdkToolsJar = helper.getJDKToolsJar();
            if (jdkToolsJar.exists()) {
                urls.add(jdkToolsJar.toURI().toURL());
            } else {
                logger.warning("JDK tools.jar does not exist at " + jdkToolsJar);
            }
            ClassLoader extCL = ClassLoader.getSystemClassLoader().getParent();
            return new URLClassLoader(urls.toArray(new URL[0]), extCL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    private void findBootstrapFile() {
        String resourceName = getClass().getName().replace(".", "/") + ".class";
        URL resource = getClass().getClassLoader().getResource(resourceName);
        if (resource == null) {
            throw new RuntimeException("Cannot get bootstrap path from "
                    + resourceName + " class location, aborting");
        }
        if (resource.getProtocol().equals("jar")) {
            try {
                JarURLConnection c = (JarURLConnection) resource.openConnection();
                URL jarFile = c.getJarFileURL();
                bootstrapFile = new File(jarFile.toURI());
            } catch (IOException e) {
                throw new RuntimeException("Cannot open bootstrap jar file", e);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Incorrect bootstrap class URI", e);
            }
        } else {
            throw new RuntimeException("Don't support packaging " + resource + " , please contribute !");
        }
    }

    private List<Repository> getSharedRepos() {
        List<Repository> libs = new ArrayList<Repository>();
        // by default we add lib directory repo
        File libDir = new File(glassfishDir, "lib");
        logger.info("Path to library directory is " + libDir);
        if (libDir.exists()) {
            Repository libRepo = new DirectoryBasedRepository("lib", libDir);
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
        logger.info("Path to domain library directory is " + domainlib);
        if (domainlib.exists()) {
            Repository domainLib = new DirectoryBasedRepository("domnainlib", domainlib);
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
