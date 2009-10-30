package org.glassfish.tests.embedded.ejb.test;

import org.junit.Test;
import org.junit.Assert;
import org.glassfish.tests.embedded.ejb.SampleEjb;
import org.glassfish.api.embedded.*;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.*;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

/**
 * this test will use the ejb API testing.
 *
 * @author Jerome Dochez
 */
public class EmbeddedTest {

    @Test
    public void test() throws Exception {
        Server.Builder builder = new Server.Builder("simple");
        Server server = builder.build();
        File f = new File(System.getProperty("basedir"), "target");
        f = new File(f, "classes");

        ScatteredArchive archive = new ScatteredArchive.Builder("simple",f).buildJar();
        server.addContainer(ContainerBuilder.Type.ejb);
        try {
            server.start();
            String appName = null;
            try {
                appName = server.getDeployer().deploy(archive, null);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            assert(appName!=null);
            try {
                System.out.println("Looking up EJB...");
                SampleEjb ejb = (SampleEjb) (new InitialContext()).lookup("java:global/simple/SampleEjb");
                if (ejb!=null) {
                    System.out.println("Invoking EJB...");
                    System.out.println(ejb.saySomething());
                    Assert.assertEquals(ejb.saySomething(), "Hello World");
                }
            } catch (Exception e) {
                System.out.println("ERROR calling EJB:");
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            server.getDeployer().undeploy(appName, null);
        } finally {
            server.stop();
        }
    }
}
