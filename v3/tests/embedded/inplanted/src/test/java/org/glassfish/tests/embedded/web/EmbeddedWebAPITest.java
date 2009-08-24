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
import org.glassfish.web.embed.EmbeddedWebContainer;
import org.glassfish.web.embed.WebBuilder;
import org.glassfish.web.embed.impl.Context;
import org.glassfish.web.embed.impl.WebListener;
import org.glassfish.web.embed.impl.VirtualServer;

/**
 * @author Amy Roh
 */
public class EmbeddedWebAPITest {

    static Server server;

    @BeforeClass
    public static void setupServer() throws Exception {
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
        try {
            EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
            efsb.setInstallRoot(f, true);
            Server.Builder builder = new Server.Builder("inplanted");
            builder.setEmbeddedFileSystem(efsb.build());
            server = builder.build();
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    public void testEmbeddedWebAPI() throws Exception {    
        
        int port = 9090;
        String webListenerId = "test-listener";
        String virtualServerId = "test-server";
        String hostName = "localhost";
        String defaultDomain = "com.sun.appserv";
        
        System.out.println("================ Test Embedded Web API");
        
        File f = new File(System.getProperty("basedir"));
        
        server.createPort(8080);
        server.addContainer(server.getConfig(ContainerBuilder.Type.web));
        
        System.out.println("Starting Web " + server);
        ContainerBuilder b = server.getConfig(ContainerBuilder.Type.web);
        System.out.println("builder is " + b);
        server.addContainer(b);
        System.out.println("Added Web");
        
        EmbeddedWebContainer embedded = (EmbeddedWebContainer) b.create(server);
        embedded.setLogLevel(Level.INFO);
        ((WebBuilder)b).setDocRootDir(f);
        embedded.setConfiguration((WebBuilder)b);
        embedded.start();
        
        WebListener webListener = 
                embedded.createWebListener(webListenerId, WebListener.class);
        webListener.setPort(port);
        webListener.setDefaultHost(virtualServerId);
        webListener.setDomain(defaultDomain);
        WebListener[] webListeners = new WebListener[1];
        webListeners[0] = webListener;
         
        VirtualServer defaultVirtualServer = (VirtualServer) 
                embedded.createVirtualServer(virtualServerId, f);
                //embedded.createVirtualServer(virtualServerId, f, webListeners);
        defaultVirtualServer.addAlias(hostName);
        embedded.addVirtualServer(defaultVirtualServer);
        
        Context context = (Context) embedded.createContext(f, null);
        context.addWelcomeFile("index.html");
        defaultVirtualServer.addChild(context);
        
        embedded.addWebListener(webListener);
                
        VirtualServer virtualServer = (VirtualServer) 
                embedded.createVirtualServer("test-server-2", f);
        virtualServer.addChild(context);
        
        Deployer deployer = (Deployer)defaultVirtualServer;

        deployer.install("/test", new URL("file:"+f.getPath()+"/test"));
        
        System.out.println("Installed at /test ");
      
        Thread.sleep(30000);
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
