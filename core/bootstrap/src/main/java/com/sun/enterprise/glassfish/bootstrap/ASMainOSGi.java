package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.Which;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        this(Logger.getAnonymousLogger(),args);
    }

    /**
     * Adds the jar files of the OSGi platform to the given {@link ClassPathBuilder}
     */
    protected abstract void addFrameworkJars(ClassPathBuilder cpb) throws IOException;

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
        ClassLoader commonCL = createCommonClassLoader(ClassLoader.getSystemClassLoader().getParent());
        ClassLoader libCL = helper.setupSharedCL(commonCL, getSharedRepos());

        try {
            ClassPathBuilder cpb = new ClassPathBuilder(libCL);
            addFrameworkJars(cpb);

            File moduleDir = context.getRootDirectory().getParentFile();
            for (String jar : additionalJars)
                cpb.addJar(new File(moduleDir, jar));

            this.launcherCL = cpb.create();
        } catch (IOException e) {
            throw new IOError(e);
        }
        Thread.currentThread().setContextClassLoader(launcherCL);
    }

    /**
     * Creates a class loader from JavaEE API and tools.jar.
     */
    protected ClassLoader createCommonClassLoader(ClassLoader parent) {
        try {
            ClassPathBuilder cpb = new ClassPathBuilder(parent);

            cpb.addJar(new File(glassfishDir, javaeeJarPath));
            File jdkToolsJar = helper.getJDKToolsJar();
            if (jdkToolsJar.exists()) {
                cpb.addJar(jdkToolsJar);
            } else {
                logger.warning("JDK tools.jar does not exist at " + jdkToolsJar);
            }
            
            return cpb.create();
        } catch (IOException e) {
            throw new IOError(e);
        }
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
