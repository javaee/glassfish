/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.DispatcherType;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import org.glassfish.embeddable.*;
import org.glassfish.embeddable.web.*;
import org.glassfish.embeddable.web.config.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for Context createServlet & createFilter using class to default virtual server
 * 
 * @author Amy Roh
 */
public class EmbeddedCreateServletAndFilterTest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static File root;
    static String vsname = "test-server";
    static String contextRoot = "test";

    @BeforeClass
    public static void setupServer() throws GlassFishException {
        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);
        System.out.println("================ EmbeddedAddServletAndFilterByClassNameTest Test");
        System.out.println("Starting Web "+embedded);
        embedded.setLogLevel(Level.INFO);
        WebContainerConfig config = new WebContainerConfig();
        config.setListings(true);
        root = new File("target/classes");
        config.setDocRootDir(root);
        config.setPort(8080);
        System.out.println("Added Web with base directory "+root.getAbsolutePath());
        embedded.setConfiguration(config);
    }
    
    @Test
    public void testEmbeddedAddServletDefaultVS() throws Exception {

        VirtualServer vs = embedded.getVirtualServer("server");
        System.out.println("Default virtual server "+vs);
        Context context = (Context) embedded.createContext(root);

        Class<NewFilterServlet> servletCl = (Class<NewFilterServlet>)
            Class.forName("org.glassfish.tests.embedded.web.NewFilterServlet");
        NewFilterServlet filterServlet = context.createServlet(servletCl);
        ServletRegistration sr = context.addServlet("NewFilterServlet", filterServlet);
        sr.setInitParameter("servletInitName", "servletInitValue");
        sr.addMapping("/newFilterServlet");

        Class<NewFilter> filterCl = (Class<NewFilter>)
            Class.forName("org.glassfish.tests.embedded.web.NewFilter");
        NewFilter filter = context.createFilter(filterCl);
        FilterRegistration fr = context.addFilter("NewFilter", filter);
        fr.setInitParameter("filterInitName", "filterInitValue");
        fr.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST),
                                     true, "NewFilterServlet");

        vs.addContext(context, contextRoot);

        URL servlet = new URL("http://localhost:8080/"+contextRoot+"/newFilterServlet");
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

        vs.removeContext(context);
        
     }

    @AfterClass
    public static void shutdownServer() throws GlassFishException {
        System.out.println("Stopping server " + glassfish);
        if (glassfish != null) {
            glassfish.stop();
            glassfish.dispose();
            glassfish = null;
        }
    }
    
    
}
