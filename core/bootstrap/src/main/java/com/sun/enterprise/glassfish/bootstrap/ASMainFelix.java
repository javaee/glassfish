package com.sun.enterprise.glassfish.bootstrap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;
import java.io.File;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ASMainFelix extends ASMainOSGi {

    public ASMainFelix(Logger logger, String[] args) {
        super(logger, args);
    }

    protected void setFwDir() {
        String fwPath = System.getenv("FELIX_HOME");
        if (fwPath == null) {
            fwPath = new File(glassfishDir, "felix").getAbsolutePath();
        }
        fwDir = new File(fwPath);
        if (!fwDir.exists()) {
            throw new RuntimeException("Can't locate Felix at " + fwPath);
        }
    }

    protected URL[] getFWJars() throws Exception {
        // Calculate path to glassfish/felix/bin/felix.jar
        File felixJar = new File(glassfishDir, "felix/bin/felix.jar");
        URL felixJarURL = felixJar.toURI().toURL();
        return new URL[]{felixJarURL};
    }

    protected void launchOSGiFW() throws Exception {
        /* Set a system property called com.sun.aas.installRootURI.
         * This property is used in felix/conf/config.properties to
         * to auto-start some modules. We can't use com.sun.aas.installRoot
         * because that com.sun.aas.installRoot is a directory path, where as
         * we need a URI.
         */
        String installRoot = System.getProperty("com.sun.aas.installRoot");
        URI installRootURI = new File(installRoot).toURI();
        System.setProperty("com.sun.aas.installRootURI", installRootURI.toString());
        String sysFileURL = new File(glassfishDir, "felix/conf/system.properties").toURI().toURL().toString();
        System.setProperty("felix.system.properties", sysFileURL);
        String confFileURL = new File(glassfishDir, "felix/conf/config.properties").toURI().toURL().toString();
        System.setProperty("felix.config.properties", confFileURL);
        File cacheProfileDir = new File(domainDir, ".felix/gf/");
        if (cacheProfileDir.exists() && cacheProfileDir.isDirectory()) {
            // remove this
            logger.info("Removing Felix cache profile dir " + cacheProfileDir+ " left from a previous run");
            boolean deleted = deleteRecurssive(cacheProfileDir);
            if (!deleted) {
                logger.warning("Not able to delete " + cacheProfileDir);
            }
        }
        cacheProfileDir.mkdirs();
        cacheProfileDir.deleteOnExit();
        System.setProperty("felix.cache.profiledir", cacheProfileDir.getCanonicalPath());
        Class mc = launcherCL.loadClass(getFWMainClassName());
        final String[] args = new String[0];
        final Method m = mc.getMethod("main", new Class[]{args.getClass()});
        // Call Felix on a daemon Thread so that the thread created by
        // Felix EventDispatcher also inherits the daemon status.
        Thread launcherThread = new Thread(new Runnable(){
            public void run() {
                try {
                    m.invoke(null, new Object[]{args});
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e); // TODO: Proper Exception Handling
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e); // TODO: Proper Exception Handling
                }
            }
        },"OSGi Framework Launcher");

        // The EventDispatcher thread in Felix inherits the daemon status of the thread
        // that starts Felix. So, it is very important to start Felix on a daemon thread.
        // Otherwise, the server process would not exit even when all the server specific
        // non-daemon threads are stopped.
        launcherThread.setDaemon(true);
        launcherThread.start();

        // Wait for framework to be started, otherwise the VM would exit since there is no
        // non-daemon thread started yet. The first non-daemon thread is started
        // when our hk2 osgi-adapter is started.
        launcherThread.join();
        logger.info("Framework successfully started");
    }

    private String getFWMainClassName() {
        return "org.apache.felix.main.Main";
    }

}
