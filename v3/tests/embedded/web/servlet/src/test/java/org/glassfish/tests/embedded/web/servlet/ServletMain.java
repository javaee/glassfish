package org.glassfish.tests.embedded.web.servlet;

import org.junit.*;
import org.junit.Assert;
import org.glassfish.api.embedded.*;
import org.glassfish.api.deployment.*;

import javax.naming.*;
import java.io.*;
import java.util.*;

import com.gargoylesoftware.htmlunit.*;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Nov 4, 2009
 * Time: 1:44:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServletMain {

    public static void main(String[] args) {
        ServletMain test = new ServletMain();
        System.setProperty("basedir", System.getProperty("user.dir"));
        try {
            test.test();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    
    @Test
    public void test() throws Exception {

        Server server = new Server.Builder("web").build();
        try {
            File f = new File(System.getProperty("basedir"));
            f = new File(f, "target");
            f = new File(f, "classes");
            ScatteredArchive.Builder builder = new ScatteredArchive.Builder("hello", f);
            builder.addClassPath(f.toURI().toURL());
            builder.resources(f);
            ScatteredArchive war = builder.buildWar();
            System.out.println("War content");
            Enumeration<String> contents = war.entries();
            while(contents.hasMoreElements()) {
                System.out.println(contents.nextElement());
            }
            Port port = server.createPort(8080);
            server.addContainer(server.createConfig(ContainerBuilder.Type.web));
            DeployCommandParameters dp = new DeployCommandParameters(f);
            String appName = server.getDeployer().deploy(war, dp);
            WebClient webClient = new WebClient();
            try {
                Page page =  webClient.getPage("http://localhost:8080/classes/hello");
                System.out.println("Got response " + page.getWebResponse().getContentAsString());
                Assert.assertTrue("Servlet returned wrong content", page.getWebResponse().getContentAsString().startsWith("Hello World"));
            } finally {
                System.out.println("Undeploying");
                server.getDeployer().undeploy(appName, null);
                port.close();
            }

        } finally {
            System.out.println("Stopping the server !");
            try {
                server.stop();
            } catch (LifecycleException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
