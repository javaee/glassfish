package org.glassfish.distributions.test;


import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.distributions.test.ejb.SampleEjb;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class EmbeddedTest {

    static Port http=null;
    static Server server = null;

    @BeforeClass
    public static void setup() {
        Server.Builder builder = new Server.Builder("build");

        server = builder.build();
        http = server.createPort(8080);
    }

    @Test
    public void testEjb() {

        server.addContainer(server.createConfig(ContainerBuilder.Type.ejb));
        EmbeddedDeployer deployer = server.getDeployer();

        URL source = SampleEjb.class.getClassLoader().getResource("org/glassfish/distributions/test/ejb/SampleEjb.class");
        String p = source.getPath().substring(0, source.getPath().length()-"org/glassfish/distributions/test/ejb/SimpleEjb.class".length());

        File path = new File(p);
        DeployCommandParameters dp = new DeployCommandParameters(path);
        dp.name="sample";
        String appName = deployer.deploy(path, dp);

        // ok now let's look up the EJB...
        try {
            InitialContext ic = new InitialContext();
            SampleEjb ejb = (SampleEjb) ic.lookup("java:global/test-classes/SampleEjb");
            if (ejb!=null) {
                try {
                    System.out.println(ejb.saySomething());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        deployer.undeploy(appName);
        System.out.println("Done with EJB");
    }

    //@Test
    public void testWeb() throws Exception {
        System.out.println("Starting Web");
        server.addContainer(server.createConfig(ContainerBuilder.Type.web));
        EmbeddedDeployer deployer = server.getDeployer();
        System.out.println("Added Web");

        URL source = SampleEjb.class.getClassLoader().getResource("org/glassfish/distributions/test/web/WebHello.class");
        String p = source.getPath().substring(0, source.getPath().length()-"org/glassfish/distributions/test/web/WebHello.class".length());

        System.out.println("Deploying " + p);
        File path = new File(p);
        DeployCommandParameters dp = new DeployCommandParameters(path);
        dp.name="sampleweb";
        String appName = deployer.deploy(path, dp);

        try {
            URL servlet = new URL("http://localhost:8080/sampleweb/hello");
            URLConnection yc = servlet.openConnection();
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    yc.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
            in.close();
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
        deployer.undeploy(appName);

    }

    @AfterClass
    public static void  close() {
        if (http!=null) {
            http.unbind();
            http=null;
        }
        System.out.println("Stopping server");
        if (server!=null) {
            server.stop();
            server=null;
        }
    }
}