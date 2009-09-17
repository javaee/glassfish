package org.glassfish.tests.embedded.inplanted;

import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.glassfish.api.embedded.*;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.admin.*;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.tests.embedded.utils.EmbeddedServerUtils;

import java.io.File;
import java.util.Enumeration;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Jerome Dochez
 */
public class InplantedTest {

    static Server server;

    @BeforeClass
    public static void setupServer() throws Exception {
        EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
        efsb.installRoot(EmbeddedServerUtils.getServerLocation());
        server = EmbeddedServerUtils.createServer(efsb.build());
    }

    @Test
    public void testWeb() throws Exception {
        System.out.println("test web");
        File f = new File(System.getProperty("basedir"));
        f = new File(f, "target");
        f = new File(f, "test-classes");
        ScatteredArchive.Builder builder = new ScatteredArchive.Builder("hello", f);
        builder.addClassPath(f.toURI().toURL());
        builder.resources(f);
        ScatteredArchive war = builder.buildWar();
        System.out.println("War content");
        Enumeration<String> contents = war.entries();
        while(contents.hasMoreElements()) {
            System.out.println(contents.nextElement());
        }
        server.createPort(8080);
        server.addContainer(server.createConfig(ContainerBuilder.Type.web));
        DeployCommandParameters dp = new DeployCommandParameters(f);
        String appName = server.getDeployer().deploy(war, dp);
        WebClient webClient = new WebClient();
        Page page =  webClient.getPage("http://localhost:8080/test-classes/hello");
        System.out.println("Got response " + page.getWebResponse().getContentAsString());
        Assert.assertTrue("Servlet returned wrong content", page.getWebResponse().getContentAsString().startsWith("Hello World"));
        server.getDeployer().undeploy(appName, null);
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
        EmbeddedServerUtils.shutdownServer(server);                
    }
}
