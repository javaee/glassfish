package org.glassfish.tests.embedded.inplanted;

import org.junit.Test;
import org.junit.Assert;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedFileSystem;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.admin.*;
import org.jvnet.hk2.component.Habitat;

import java.io.File;

/**
 * @author Jerome Dochez
 */
public class InplantedTest {

    @Test
    public void Test() {

        System.out.println(System.getProperty("basedir"));
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
            efsb.setInstallRoot(f);
            Server.Builder builder = new Server.Builder("inplanted");
            builder.setEmbeddedFileSystem(efsb.build());
            Server server = builder.build();
            Habitat habitat = server.getHabitat();
            System.out.println("Process type is " + habitat.getComponent(ProcessEnvironment.class).getProcessType());
            for (Sniffer s : habitat.getAllByContract(Sniffer.class)) {
                System.out.println("Got sniffer " + s.getModuleType());
            }
            server.stop();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
