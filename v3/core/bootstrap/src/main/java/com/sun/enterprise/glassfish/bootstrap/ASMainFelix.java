package com.sun.enterprise.glassfish.bootstrap;

import java.lang.reflect.Method;
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

    protected String getFWMainClassName() {
        return "org.apache.felix.main.Main";
    }

    protected URL[] getFWJars() {
        // Calculate path to glassfish/felix/bin/felix.jar
        File felixJar = new File(glassfishDir, "felix/bin/felix.jar");
        try {
            URL felixJarURL = felixJar.toURI().toURL();
            return new URL[]{felixJarURL};
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void launchOSGiFW(String... args) {
        try {
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
            Method m = mc.getMethod("main", new Class[]{args.getClass()});
            m.invoke(null, new Object[]{args});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean deleteRecurssive(File dir) {
        for (File f : dir.listFiles()) {
            if(f.isFile()) {
                f.delete();
            } else {
                deleteRecurssive(f);
            }
        }
        return dir.delete();
    }

}
