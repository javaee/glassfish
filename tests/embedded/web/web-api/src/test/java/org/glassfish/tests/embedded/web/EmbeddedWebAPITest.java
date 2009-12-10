/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.tests.embedded.web;

import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import java.io.File;
import java.util.logging.Level;
import java.net.URL;
import org.apache.catalina.Deployer;
import org.apache.catalina.logger.SystemOutLogger;
import org.glassfish.api.embedded.*;
import org.glassfish.api.embedded.web.WebBuilder;
import org.glassfish.web.embed.impl.Context;
import org.glassfish.web.embed.impl.EmbeddedWebContainer;
import org.glassfish.web.embed.impl.VirtualServer;
import org.glassfish.web.embed.impl.WebListener;

/**
 * @author Amy Roh
 */
public class EmbeddedWebAPITest {

    static Server server;
    static EmbeddedWebContainer embedded;
    static File f;

    @BeforeClass
    public static void setupServer() throws Exception {
        try {
            Server.Builder builder = new Server.Builder("web-api");
            server = builder.build();
            f = new File(System.getProperty("basedir"));
            System.out.println("Starting Web " + server);
            ContainerBuilder b = server.createConfig(ContainerBuilder.Type.web);
            System.out.println("builder is " + b);
            server.addContainer(b);
            System.out.println("Added Web with base directory "+f.getAbsolutePath());
            embedded = (EmbeddedWebContainer) b.create(server);
            embedded.setLogLevel(Level.INFO);
            embedded.setPath(f);
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    public void testEmbeddedWebAPI() throws Exception {   
        
        System.out.println("================ Test Embedded Web API");
        
        int port = 9090;
        String virtualServerId = "test-server";
        String hostName = "localhost";
        String defaultDomain = "com.sun.appserv";  
        
        server.createPort(port);
         
        VirtualServer defaultVirtualServer = (VirtualServer) 
                embedded.createVirtualServer(virtualServerId, f);
        defaultVirtualServer.addAlias(hostName);
        embedded.addVirtualServer(defaultVirtualServer);
        
        Context context = (Context) embedded.createContext(f, null);
        context.addWelcomeFile("index.html");
        System.out.println("default web xml "+context.getDefaultWebXml());
        defaultVirtualServer.addChild(context);
        
        embedded.start();
        
        Thread.sleep(10000);
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
