/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.examples.configuration.xml.webserver.tests;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.examples.configuration.xml.webserver.ApplicationBean;
import org.glassfish.examples.configuration.xml.webserver.WebServerBean;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.api.XmlServiceUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * These are the test cases for the HK2 XML Example WebServer
 * 
 * @author jwells
 */
public class WebServerXmlTest extends HK2Runner {
    private final static String EXAMPLE1_FILENAME = "webserverExample1.xml";
    
    @Inject
    private Provider<XmlService> xmlServiceProvider;
    
    @Before
    public void before() {
        initialize();
        
        XmlServiceUtilities.enableXmlService(testLocator);
    }
    
    /**
     * Tests that a file can be read and used directly from
     * the XmlService
     * 
     * @throws Exception
     */
    @Test
    public void testParseWebServerXmlFile() throws Exception {
        XmlService xmlService = xmlServiceProvider.get();
        
        URI webserverFile = getClass().getClassLoader().getResource(EXAMPLE1_FILENAME).toURI();
        
        XmlRootHandle<ApplicationBean> applicationRootHandle =
                xmlService.unmarshal(webserverFile, ApplicationBean.class);
        
        ApplicationBean root = applicationRootHandle.getRoot();
        WebServerBean webservers[] = root.getWebServers();
        
        Assert.assertEquals(3, webservers.length);
        
        {
            WebServerBean developmentServer = webservers[0];
            Assert.assertEquals("Development Server", developmentServer.getName());
            Assert.assertEquals(8001, developmentServer.getAdminPort());
            Assert.assertEquals(8002, developmentServer.getPort());
            Assert.assertEquals(8003, developmentServer.getSSLPort());
        }
        
        {
            WebServerBean qaServer = webservers[1];
            Assert.assertEquals("QA Server", qaServer.getName());
            Assert.assertEquals(9001, qaServer.getAdminPort());
            Assert.assertEquals(9002, qaServer.getPort());
            Assert.assertEquals(9003, qaServer.getSSLPort());
        }
        
        {
            WebServerBean externalServer = webservers[2];
            Assert.assertEquals("External Server", externalServer.getName());
            Assert.assertEquals(10001, externalServer.getAdminPort());
            Assert.assertEquals(80, externalServer.getPort());
            Assert.assertEquals(81, externalServer.getSSLPort());
        }
    }
    
    
}
