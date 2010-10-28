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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.net.URLConnection;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.apache.catalina.Deployer;
import org.apache.catalina.logger.SystemOutLogger;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.*;
import org.glassfish.embeddable.web.*;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;


/**
 * Tests EmbeddedWebContainer#start correctly starts the server with default 8080 port
 * if no port is previously defined.
 *
 * @author Amy Roh
 */
public class EmbeddedWebAPIDefaultStartTest {

    static Server server;
    static EmbeddedWebContainer embedded;
    static File f;

    @BeforeClass
    public static void setupServer() throws Exception {
        try {
            Server.Builder builder = new Server.Builder("web-api");
            server = builder.build();
            f = new File(System.getProperty("basedir"));

            // TODO :: change this to use
            // org.glassfish.embeddable.GlassFish.lookupService
            embedded = server.getHabitat().
                    getComponent(EmbeddedWebContainer.class);
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    public void testDefaultStart() throws Exception {
        System.out.println("================ Test Embedded Web API Default Start ");
        System.out.println("Starting Web " + server+" "+embedded);
        embedded.setLogLevel(Level.INFO);
        embedded.start();

        NetworkConfig nc = server.getHabitat().getComponent(NetworkConfig.class);
        List<NetworkListener> listeners = nc.getNetworkListeners().getNetworkListener();
        System.out.println("Network listener size after default start " + listeners.size());
        for (NetworkListener nl : listeners) {
            System.out.println("Network listener " + nl.getPort());
        }

        List<WebListener> listenerList = new ArrayList(embedded.getWebListeners());
        Assert.assertTrue(listenerList.size()==1);
        for (WebListener listener : embedded.getWebListeners())
            System.out.println("Web listener "+listener.getId()+" "+listener.getPort());

        EmbeddedDeployer deployer = server.getDeployer();
        String p = System.getProperty("buildDir");
        System.out.println("Root is " + p);
        ScatteredArchive.Builder sa = new ScatteredArchive.Builder("sampleweb", new File(p));
        sa.resources(new File(p));
        sa.addClassPath((new File(p)).toURL());
        DeployCommandParameters dp = new DeployCommandParameters(new File(p));

        System.out.println("Deploying " + p);
        String appName = deployer.deploy(sa.buildWar(), dp);
        Assert.assertNotNull("Deployment failed!", appName);

        URL servlet = new URL("http://localhost:8080/classes/hello");
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

        Thread.sleep(1000);

        if (appName!=null)
            deployer.undeploy(appName, null);

        embedded.stop();  
    }
    

    @AfterClass
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
