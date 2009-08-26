package org.glassfish.tests.embedded.utils;

import org.junit.Assert;
import org.glassfish.api.embedded.EmbeddedFileSystem;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.LifecycleException;

import java.io.File;

public class EmbeddedServerUtils {

    public static File getServerLocation() {
        System.out.println("setup started with gf installation " + System.getProperty("basedir"));
        File f = new File(System.getProperty("basedir"));
        f = new File(f, "target");
        f = new File(f, "dependency");
        f = new File(f, "glassfishv3");
        f = new File(f, "glassfish");
        if (f.exists()) {
            System.out.println("Using gf at " + f.getAbsolutePath());
        } else {
            System.out.println("GlassFish not found at " + f.getAbsolutePath());
            Assert.assertTrue(f.exists());
        }
        return f;
    }
    public static File getDomainLocation(File serverLocation) {
        return getDomainLocation(serverLocation, "domain1");
    }
    
    public static File getDomainLocation(File serverLocation, String domainName) {

       // find the domain root.
        File f = new File(serverLocation,"domains");
        f = new File(f, domainName);
        Assert.assertTrue(f.exists());
        return f;
    }

    public static Server createServer(EmbeddedFileSystem fileSystem) throws Exception {
        try {
            Server.Builder builder = new Server.Builder("inplanted");
            builder.setEmbeddedFileSystem(fileSystem);
            return builder.build();
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void shutdownServer(Server server) throws Exception {
        System.out.println("shutdown initiated");
        if (server!=null) {
            try {
                server.stop();
            } catch (LifecycleException e) {
                e.printStackTrace();
                throw e;
            }
        }


    }

}