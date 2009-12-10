/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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


import java.util.*;
import org.junit.Test;
import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import com.sun.grizzly.config.dom.NetworkConfig;
import com.sun.grizzly.config.dom.NetworkListener;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.embedded.EmbeddedContainer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.embedded.ScatteredArchive;
import org.glassfish.api.embedded.ScatteredArchive.Builder;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.api.embedded.admin.AdminInfo;
import org.glassfish.api.embedded.admin.EmbeddedAdminContainer;
import org.glassfish.api.embedded.admin.CommandExecution;
import org.glassfish.api.embedded.admin.CommandParameters;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.container.Sniffer;
import org.glassfish.distributions.test.ejb.SampleEjb;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class EmbeddedTest {

    static Port http=null;
    static Server server = null;
    static EmbeddedAdminContainer ctr =null;

    @BeforeClass
    public static void setup() {
        Server.Builder builder = new Server.Builder("build");

        server = builder.build();
        NetworkConfig nc = server.getHabitat().getComponent(NetworkConfig.class);
        List<NetworkListener> listeners = nc.getNetworkListeners().getNetworkListener();
        System.out.println("Network listener size before creation " + listeners.size());
        for (NetworkListener nl : listeners) {
            System.out.println("Network listener " + nl.getPort());
        }
        try {
            http = server.createPort(8080);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
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
        server.addContainer(server.createConfig(ContainerBuilder.Type.ejb));
        server.addContainer(ContainerBuilder.Type.all);
        ctr = server.addContainer(server.createConfig(AdminInfo.class));

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
        try {
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
                throw e;
            }
        } catch(Exception e) {
            // mask exceptions for now
            // e.printStackTrace();
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