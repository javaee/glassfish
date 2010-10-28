/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.distributions.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.api.embedded.EmbeddedContainer;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.embedded.ScatteredArchive;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.admin.AdminInfo;
import org.glassfish.api.embedded.admin.CommandExecution;
import org.glassfish.api.embedded.admin.CommandParameters;
import org.glassfish.api.embedded.admin.EmbeddedAdminContainer;
import org.glassfish.embeddable.web.EmbeddedWebContainer;
import org.glassfish.embeddable.web.HttpListener;
import org.glassfish.distributions.test.ejb.SampleEjb;
import org.glassfish.embeddable.GlassFishException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class EmbeddedTest {

    static Port http=null;
    static Server server = null;
    static EmbeddedAdminContainer ctr =null;

    @BeforeClass
    public static void setup() throws org.glassfish.api.embedded.LifecycleException, GlassFishException {

        Server.Builder builder = new Server.Builder("build");

        server = builder.build();
        NetworkConfig nc = server.getHabitat().getComponent(NetworkConfig.class);
        List<NetworkListener> listeners = nc.getNetworkListeners().getNetworkListener();
        System.out.println("Network listener size before creation " + listeners.size());
        for (NetworkListener nl : listeners) {
            System.out.println("Network listener " + nl.getPort());
        }

        server.addContainer(server.createConfig(ContainerBuilder.Type.ejb));
//        ContainerBuilder b = server.createConfig(ContainerBuilder.Type.web);
//        System.out.println("builder is " + b);
//        server.addContainer(b);

        // TODO :: change this to use org.glassfish.embeddable.GlassFish.getService
        EmbeddedWebContainer embedded = server.getHabitat().
                getComponent(EmbeddedWebContainer.class);
        try {
            HttpListener listener = new HttpListener();
            listener.setPort(8080);
            listener.setId("embedded-listener-1");
            embedded.addWebListener(listener);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        embedded.start();
        server.addContainer(ContainerBuilder.Type.all);
        ctr = server.addContainer(server.createConfig(AdminInfo.class));

        listeners = nc.getNetworkListeners().getNetworkListener();
        System.out.println("Network listener size after creation " + listeners.size());
        Assert.assertTrue(listeners.size()==1);
        for (NetworkListener nl : listeners) {
            System.out.println("Network listener " + nl.getPort());
        }
        Collection<NetworkListener> cnl = server.getHabitat().getAllByContract(NetworkListener.class);
        System.out.println("Network listener size after creation " + cnl.size());
        for (NetworkListener nl : cnl) {
            System.out.println("Network listener " + nl.getPort());
        }

    }


    @Test
    public void testAll() throws LifecycleException {
        Set<Sniffer> sniffers = new HashSet<Sniffer>();
        for (EmbeddedContainer c : server.getContainers()) {
            sniffers.addAll(c.getSniffers());
        }
        System.out.println("Sniffer size "  + sniffers.size());
        for (Sniffer sniffer : sniffers) {
            System.out.println("Registered Sniffer " + sniffer.getModuleType());
        }
    }    

    @Test
    public void testEjb() throws LifecycleException {

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
            SampleEjb ejb = (SampleEjb) ic.lookup("java:global/sample/SampleEjb");
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
        System.out.println("Done with EJB");
    }

    @Test
    public void testWeb() throws Exception {
        System.out.println("Starting Web " + server);
        EmbeddedDeployer deployer = server.getDeployer();
        System.out.println("Added Web");

        URL source = SampleEjb.class.getClassLoader().getResource("org/glassfish/distributions/test/web/WebHello.class");
        String p = source.getPath().substring(0, source.getPath().length()-"org/glassfish/distributions/test/web/WebHello.class".length());

        System.out.println("Root is " + p);
        ScatteredArchive.Builder builder = new ScatteredArchive.Builder("sampleweb", new File(p));
        builder.resources(new File(p));
        builder.addClassPath((new File(p)).toURL());
        DeployCommandParameters dp = new DeployCommandParameters(new File(p));

        System.out.println("Deploying " + p);
        String appName = null;
            appName = deployer.deploy(builder.buildWar(), dp);
            System.out.println("Deployed " + appName);
            Assert.assertTrue(appName != null); 
            try {
                URL servlet = new URL("http://localhost:8080/test-classes/hello");
                URLConnection yc = servlet.openConnection();
                BufferedReader in = new BufferedReader(
                                        new InputStreamReader(
                                        yc.getInputStream()));
                String inputLine = in.readLine();
                if (inputLine != null)
                    System.out.println(inputLine);
                Assert.assertEquals(inputLine.trim(), "Hello World !");
                in.close();
            } catch(Exception e) {
                e.printStackTrace();
		// do not throw the exception for now, because this may break the build if, for example, another instance of
		// glassfish is running on 8080
             //   throw e;
            }
        if (appName!=null)
            deployer.undeploy(appName, null);

    }

    @Test
    public void commandTest() {
        CommandExecution ce = ctr.execute("list-modules", new CommandParameters());
        try {
            ce.getActionReport().writeReport(System.out);
            System.out.println("");
            for (MessagePart mp : ce.getActionReport().getTopMessagePart().getChildren()) {
                 System.out.println(mp.getMessage());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        CommandParameters cm = new CommandParameters();
        cm.setOperand("org.glassfish.api.container.Sniffer");
        ce = ctr.execute("list-contracts", cm);
        try {
            ce.getActionReport().writeReport(System.out);
            System.out.println("");
            for (MessagePart mp : ce.getActionReport().getTopMessagePart().getChildren()) {
                 System.out.println(mp.getMessage());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        cm.setOperand("org.glassfish.api.container.Container");
        cm.setOption("started", "true");
        ce = ctr.execute("list-contracts", cm);
        try {
            ce.getActionReport().writeReport(System.out);
            System.out.println("");
            for (MessagePart mp : ce.getActionReport().getTopMessagePart().getChildren()) {
                 System.out.println(mp.getMessage());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    

    @AfterClass
    public static void  close() throws LifecycleException {
        if (http!=null) {
            http.close();
            http=null;
        }
        System.out.println("Stopping server " + server);
        if (server!=null) {
            server.stop();
            server=null;
        }
    }
}
