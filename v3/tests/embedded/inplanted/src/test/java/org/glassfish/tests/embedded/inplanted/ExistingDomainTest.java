package org.glassfish.tests.embedded.inplanted;

import org.glassfish.api.embedded.*;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.admin.*;
import org.glassfish.api.container.Sniffer;
import org.glassfish.tests.embedded.utils.EmbeddedServerUtils;
import org.junit.BeforeClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.AfterClass;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import java.io.File;
import java.util.Enumeration;
import java.util.Collection;
import java.lang.reflect.Method;

/**
 * Test embedded API with an existing domain.xml
 *
 * @author Jerome Dochez
 */
public class ExistingDomainTest {
    static Server server;

    @BeforeClass
    public static void setupServer() throws Exception {
        File serverLocation = EmbeddedServerUtils.getServerLocation();
        EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
        efsb.setInstallRoot(serverLocation);
        efsb.setInstanceRoot(EmbeddedServerUtils.getDomainLocation(serverLocation));
        server = EmbeddedServerUtils.createServer(efsb.build());
    }

    @Test
    public void Test() throws Exception {

        Habitat habitat = server.getHabitat();
        System.out.println("Process type is " + habitat.getComponent(ProcessEnvironment.class).getProcessType());
        Collection<Inhabitant<?>> listeners = habitat.getInhabitantsByType("com.sun.grizzly.config.dom.NetworkListener");
        Assert.assertTrue(listeners.size()>1);
        for (Inhabitant<?> s : listeners) {
            Object networkListener = s.get();
            Method m = networkListener.getClass().getMethod("getPort");
            Assert.assertNotNull("Object returned does not implement getPort, is it a networkListener ?", m);
            String port = (String) m.invoke(networkListener);
            System.out.println("Network Listener " + port);
            Assert.assertNotNull("Got a null networkListener port", port);
        }
    }

    @AfterClass
    public static void shutdownServer() throws Exception {
        EmbeddedServerUtils.shutdownServer(server);
    }
    
}
