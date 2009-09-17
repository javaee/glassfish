package org.glassfish.tests.ejb;

import org.junit.Test;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.tests.ejb.sample.SimpleEjb;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.net.URL;

public class EmbeddedTest {

    @Test
    public void test() throws LifecycleException {

        Server.Builder builder = new Server.Builder("build");

        Server server = builder.build();
        server.addContainer(server.createConfig(ContainerBuilder.Type.ejb));
        EmbeddedDeployer deployer = server.getDeployer();
        server.start();

        URL source = SimpleEjb.class.getClassLoader().getResource("org/glassfish/tests/ejb/sample/SimpleEjb.class");
        String p = source.getPath().substring(0, source.getPath().length()-"org/glassfish/tests/ejb/sample/SimpleEjb.class".length());

        File path = new File(p);
        DeployCommandParameters dp = new DeployCommandParameters(path);
        dp.name="sample";
        String appName = deployer.deploy(path, dp);

        // ok now let's look up the EJB...
        try {
            InitialContext ic = new InitialContext();
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/sample/SimpleEjb");
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

        deployer.undeploy(appName, null);
        try {
            server.stop();
        } catch (LifecycleException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("..........FINISHED EmbeddedTest");
    }
}
