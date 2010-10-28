/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.web;

import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import java.io.*;
import java.net.*;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.apache.catalina.Deployer;
import org.apache.catalina.logger.SystemOutLogger;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.*;
import org.glassfish.embeddable.web.*;

/**
 * Test for EmbeddedWebContainer#CreateVirtualServer & EmbeddedWebContainer#deleteWebListener
 *
 * @author Amy Roh
 */
public class EmbeddedWebCreateVirtualServer {

    static Server server;
    static EmbeddedWebContainer embedded;
    static Port http;
    static File root;
    static NetworkConfig nc;
    static List<NetworkListener> listeners;


    @BeforeClass
    public static void setupServer() throws Exception {
        try {
            EmbeddedFileSystem.Builder fsBuilder = new EmbeddedFileSystem.Builder();
            String p = System.getProperty("buildDir");
            root = new File(p).getParentFile();
            root =new File(root, "glassfish");
            EmbeddedFileSystem fs = fsBuilder.instanceRoot(root).build();

            Server.Builder builder = new Server.Builder("web-api");
            builder.embeddedFileSystem(fs);
            server = builder.build();

            NetworkConfig nc = server.getHabitat().getComponent(NetworkConfig.class);
            List<NetworkListener> listeners = nc.getNetworkListeners().getNetworkListener();
            System.out.println("Network listener size before creation " + listeners.size());
            for (NetworkListener nl : listeners) {
                System.out.println("Network listener " + nl.getPort());
            }

            System.out.println("Starting Web " + server);
            ContainerBuilder b = server.createConfig(ContainerBuilder.Type.web);
            System.out.println("builder is " + b);
            server.addContainer(b);
            embedded = (EmbeddedWebContainer) b.create(server);
            embedded.setLogLevel(Level.INFO);
            embedded.setConfiguration((WebBuilder)b);
            embedded.start();

        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testEmbeddedWebAPI() throws Exception {

        System.out.println("================ Test Embedded Create Virtual Server");

        nc = server.getHabitat().getComponent(NetworkConfig.class);
        listeners = nc.getNetworkListeners().getNetworkListener();
        System.out.println("Network listener size after creation " + listeners.size());
        Assert.assertTrue(listeners.size()==1);
        for (NetworkListener nl : listeners) {
            System.out.println("Network listener " + nl.getPort());
        }

        WebListener listener = embedded.createWebListener("test-listener", WebListener.class);
        listener.setPort(9090);
        WebListener[] webListeners = new HttpListener[1];
        webListeners[0] = listener;

        embedded.createVirtualServer("test-server", root, webListeners);

        nc = server.getHabitat().getComponent(NetworkConfig.class);
        listeners = nc.getNetworkListeners().getNetworkListener();
        System.out.println("Network listener size after creation " + listeners.size());
        Assert.assertTrue(listeners.size()==2);
        for (NetworkListener nl : listeners) {
            System.out.println("Network listener " + nl.getName() +" " + nl.getPort());
        }

        EmbeddedDeployer deployer = server.getDeployer();
        String p = System.getProperty("buildDir");
        System.out.println("Root is " + p);
        ScatteredArchive.Builder builder = new ScatteredArchive.Builder("sampleweb", new File(p));
        builder.resources(new File(p));
        builder.addClassPath((new File(p)).toURL());
        DeployCommandParameters dp = new DeployCommandParameters(new File(p));

        System.out.println("Deploying " + p);
        String appName = deployer.deploy(builder.buildWar(), dp);
        Assert.assertNotNull("Deployment failed!", appName);

        URL servlet = new URL("http://localhost:9090/classes/hello");
        URLConnection yc = servlet.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            sb.append(inputLine);
        }
        in.close();
        System.out.println(inputLine);
        Assert.assertEquals("Hello World!", sb.toString());

        System.out.println("Removing web listener "+listener.getId());
        embedded.removeWebListener(listener);

        nc = server.getHabitat().getComponent(NetworkConfig.class);
        listeners = nc.getNetworkListeners().getNetworkListener();
        System.out.println("Network listener size after deletion " + listeners.size());
        Assert.assertTrue(listeners.size()==1);
        for (NetworkListener nl : listeners) {
            System.out.println("Network listener " + nl.getName() +" " + nl.getPort());
        }
        if (appName!=null)
            deployer.undeploy(appName, null);

        embedded.stop();

     }

    //@AfterClass
    public static void shutdownServer() throws Exception {
        System.out.println("shutdown initiated");
        if (server!=null) {
            try {
                server.stop();
            } catch (LifecycleException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }
    
}
