package org.glassfish.tests.embedded.cooked;

import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedFileSystem;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hk2.component.Habitat;

import java.io.File;
import java.util.Collection;

import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.grizzly.config.dom.NetworkListener;

/**
 * @author Jerome Dochez
 */
public class ExistingConfigurationTest {

    @Test
    public void setupServer() throws Exception {

        Server server=null;

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
            efsb.installRoot(f, false);
            // find the domain root.
            f = new File(f,"domains");
            f = new File(f, "domain1");
            f = new File(f, "config");
            f = new File(f, "domain.xml");
            Assert.assertTrue(f.exists());
            efsb.configurationFile(f);

            Server.Builder builder = new Server.Builder("inplanted");
            builder.embeddedFileSystem(efsb.build());
            server = builder.build();

            Habitat habitat = server.getHabitat();
            Collection<VirtualServer> vss = habitat.getAllByContract(VirtualServer.class);
            Assert.assertTrue(vss.size()>0);
            for (VirtualServer vs : vss ) {
                System.out.println("Virtual Server " + vs.getId());
            }
            Collection<NetworkListener> nls = habitat.getAllByContract(NetworkListener.class);
            for (NetworkListener nl : nls) {
                System.out.println("Network listener " + nl.getPort());
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (server!=null) {
                server.stop();
            }
        }
    }
}
