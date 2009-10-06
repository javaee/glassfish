package org.glassfish.tests.embedded.web;

import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import java.io.File;
import java.util.logging.Level;
import java.net.URL;
import org.apache.catalina.Deployer;
import org.apache.catalina.logger.SystemOutLogger;
import org.glassfish.api.embedded.*;
import org.glassfish.api.embedded.web.WebBuilder;
import org.glassfish.web.embed.impl.Context;
import org.glassfish.web.embed.impl.EmbeddedWebContainer;
import org.glassfish.web.embed.impl.VirtualServer;
import org.glassfish.web.embed.impl.WebListener;

/**
 * @author Amy Roh
 */
public class EmbeddedWebAPIConfigTest {

    static Server server;
    static EmbeddedWebContainer embedded;
    static File f;

    @BeforeClass
    public static void setupServer() throws Exception {
        try {
            Server.Builder builder = new Server.Builder("web-api");
            server = builder.build();
            f = new File(System.getProperty("basedir"));
            System.out.println("Starting Web " + server);
            ContainerBuilder b = server.createConfig(ContainerBuilder.Type.web);
            System.out.println("builder is " + b);
            server.addContainer(b);
            System.out.println("Added Web with base directory "+f.getAbsolutePath());
            embedded = (EmbeddedWebContainer) b.create(server);
            embedded.setLogLevel(Level.INFO);
            embedded.setPath(f);
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    public void testEmbeddedWebAPIConfig() throws Exception {   
        
        System.out.println("================ Test Embedded Web API Config");
        server.createPort(8080);
        
        embedded.start();
        
        Thread.sleep(10000);
     }

    @AfterClass
    public static void shutdownServer() throws Exception {
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
