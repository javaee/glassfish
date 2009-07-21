package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.bootstrap.Which;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.PlatformMain;

import java.io.*;
import java.util.logging.Logger;
import java.net.URI;

/**
 * Top level abstract main class
 *
 * @author Jerome Dochez
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class AbstractMain extends PlatformMain {

    final File bootstrapFile;

    protected ASMainHelper helper;

    final protected File glassfishDir; // glassfish/

    protected File domainDir; // default is glassfish/domains/domain1    

    abstract Logger getLogger();

    protected abstract String getPreferedCacheDir();

    AbstractMain() {
        this.bootstrapFile = findBootstrapFile();
        System.setProperty(StartupContext.ROOT_PROP, bootstrapFile.getParent());
        glassfishDir = bootstrapFile.getParentFile().getParentFile(); //glassfish/
        System.setProperty("com.sun.aas.installRoot",glassfishDir.getAbsolutePath());
    }

    public void start(String[] args) throws Exception {
        helper = new ASMainHelper(logger);
        helper.parseAsEnv(glassfishDir);
        run(logger, args);
    }

    protected void run(Logger logger, String... args) throws Exception {
        this.logger = logger;
        StartupContext sc = getContext(StartupContext.class);
        if (sc!=null) {
            domainDir = sc.getUserDirectory();
        }
        if (domainDir==null) {
            domainDir = helper.getDomainRoot(new StartupContext(bootstrapFile, args));
            helper.verifyAndSetDomainRoot(domainDir);
        }

        File cacheProfileDir = new File(domainDir, getPreferedCacheDir());
        // This is where inhabitants cache is located
        System.setProperty("com.sun.enterprise.hk2.cacheDir", cacheProfileDir.getAbsolutePath());
        setUpCache(bootstrapFile.getParentFile(), cacheProfileDir);
    }

    protected void setSystemProperties() throws Exception {
       /* Set a system property called com.sun.aas.installRootURI.
         * This property is used in felix/conf/config.properties and possibly
         * in other OSGi framework's config file to auto-start some modules.
         * We can't use com.sun.aas.installRoot,
         * because that com.sun.aas.installRoot is a directory path, where as
         * we need a URI.
         */
        String installRoot = System.getProperty("com.sun.aas.installRoot");
        URI installRootURI = new File(installRoot).toURI();
        System.setProperty("com.sun.aas.installRootURI", installRootURI.toString());
        String instanceRoot = System.getProperty("com.sun.aas.instanceRoot");
        URI instanceRootURI = new File(instanceRoot).toURI();
        System.setProperty("com.sun.aas.instanceRootURI", instanceRootURI.toString());        
    }

    protected abstract void setUpCache(File sourceDir, File cacheDir) throws IOException;

    protected File findBootstrapFile() {
        try {
            return Which.jarFile(getClass());
        } catch (IOException e) {
            throw new RuntimeException("Cannot get bootstrap path from "
                    + getClass() + " class location, aborting");
        }
    }


}