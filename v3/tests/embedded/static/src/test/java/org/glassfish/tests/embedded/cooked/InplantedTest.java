package org.glassfish.tests.embedded.cooked;

import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.glassfish.api.embedded.*;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.admin.*;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.deployment.DeployCommandParameters;

import java.io.File;
import java.util.Enumeration;

/**
 * @author Jerome Dochez
 */
public class InplantedTest {

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
    public void testWeb() throws Exception {
        System.out.println("test web");
        File f = new File(System.getProperty("basedir"));
        f = new File(f, "target");
        f = new File(f, "test-classes");
        ScatteredArchive.Builder builder = new ScatteredArchive.Builder("hello", f);
        builder.addClassPath(f.toURI().toURL());
        builder.setResources(f);
        ScatteredArchive war = builder.buildWar();
        System.out.println("War content");
        Enumeration<String> contents = war.entries();
        while(contents.hasMoreElements()) {
            System.out.println(contents.nextElement());
        }
        server.createPort(8080);
        server.addContainer(server.getConfig(ContainerBuilder.Type.web));
        DeployCommandParameters dp = new DeployCommandParameters(f);
        String appName = server.getDeployer().deploy(war, dp);
        Thread.sleep(30000);
        server.getDeployer().undeploy(appName);
    }

    @Test
    public void Test() {

        Habitat habitat = server.getHabitat();
        System.out.println("Process type is " + habitat.getComponent(ProcessEnvironment.class).getProcessType());
        for (Sniffer s : habitat.getAllByContract(Sniffer.class)) {
            System.out.println("Got sniffer " + s.getModuleType());
        }
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
