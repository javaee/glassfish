package org.glassfish.tests.ejb;

import org.junit.Test;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.ejb.embedded.EjbEmbeddedInfo;
import org.glassfish.ejb.embedded.EjbEmbeddedContainer;
import org.glassfish.tests.ejb.sample.SimpleEjb;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.net.URL;

public class EmbeddedTest {

    @Test
    public void test() {

        Server.Builder builder = new Server.Builder("ejb-test");

        Server server = builder.build();
        EjbEmbeddedInfo ejbInfo = server.createConfig(EjbEmbeddedInfo.class);
        EjbEmbeddedContainer ejbContainer = server.addContainer(ejbInfo);
        EmbeddedDeployer deployer = server.getDeployer();

        URL source = SimpleEjb.class.getClassLoader().getResource("org/glassfish/tests/ejb/sample/SimpleEjb.class");
        String p = source.getPath().substring(0, source.getPath().length()-"org/glassfish/tests/ejb/sample/SimpleEjb.class".length());

        File path = new File(p);
        DeployCommandParameters dp = new DeployCommandParameters(path);
        dp.name="sample";
        String appName = deployer.deploy(path, dp);

        // ok now let's look up the EJB...
        try {
            InitialContext ic = new InitialContext();
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/test-classes/SimpleEjb");
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
    }
}