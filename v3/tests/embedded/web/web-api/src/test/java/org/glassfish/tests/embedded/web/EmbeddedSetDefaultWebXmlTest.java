/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
import java.util.logging.Level;
import java.net.*;
import org.apache.catalina.Deployer;
import org.apache.catalina.logger.SystemOutLogger;
import org.glassfish.api.embedded.*; 
import org.glassfish.embeddable.web.*;
import org.glassfish.embeddable.web.config.*;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import org.glassfish.tests.webapi.HelloWeb;

/**
 * Tests EmbeddedFileSystem.configurationFile and WebBuilder.setDefaultWebXml
 * Checks if directory listings is correctly getting picked up from the default-web.xml
 * 
 * @author Amy Roh
 */
public class EmbeddedSetDefaultWebXmlTest {

    static Server server;
    static EmbeddedWebContainer embedded;
    static File root;

    @BeforeClass
    public static void setupServer() throws Exception {
        try {
            Server.Builder builder = new Server.Builder("web-api");
            server = builder.build();
            //f = new File(System.getProperty("basedir"));
            String p = System.getProperty("buildDir");
            
            /*EmbeddedFileSystem.Builder fsBuilder = new EmbeddedFileSystem.Builder();
            root = new File(p).getParentFile();
            root =new File(root, "glassfish");
            EmbeddedFileSystem fs = fsBuilder.instanceRoot(root).build();
            File domainXml = new File(p+"/org/glassfish/tests/webapi/domain.xml");
            // specify the domain.xml location
            fsBuilder.configurationFile(domainXml);
            EmbeddedFileSystem efs = fsBuilder.build();
            Server.Builder builder = new Server.Builder("dirserve");
            builder.embeddedFileSystem(efs);
            server = builder.build();
            */

            WebContainerConfig config = new WebContainerConfig();
            File defaultWebXml = new File(p+"/org/glassfish/tests/webapi/my-default-web.xml");
            config.setDefaultWebXml(defaultWebXml.toURL());
            System.out.println("Using default-web.xml "+defaultWebXml.getAbsolutePath());
            //        " domain.xml "+domainXml.getAbsolutePath());

            // TODO :: change this to use
            // org.glassfish.embeddable.GlassFish.lookupService
            embedded = server.getHabitat().
                    getComponent(EmbeddedWebContainer.class);
            embedded.start(config);
            embedded.setLogLevel(Level.INFO);
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    public void testEmbeddedWebAPIConfig() throws Exception {   
            
        System.out.println("================ EmbeddedSetDefaultWebXml Test");

        URL servlet = new URL("http://localhost:8080");
        URLConnection yc = servlet.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                yc.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            sb.append(inputLine);
        }
        in.close();
        
        Thread.sleep(100);
        
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
