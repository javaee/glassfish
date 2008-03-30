package com.sun.enterprise.glassfish.bootstrap;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.MalformedURLException;
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
