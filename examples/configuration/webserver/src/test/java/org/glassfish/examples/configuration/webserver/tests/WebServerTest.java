/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.examples.configuration.webserver.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.glassfish.examples.configuration.webserver.SSLCertificateBean;
import org.glassfish.examples.configuration.webserver.WebServer;
import org.glassfish.examples.configuration.webserver.WebServerBean;
import org.glassfish.examples.configuration.webserver.internal.SSLCertificateService;
import org.glassfish.examples.configuration.webserver.internal.WebServerImpl;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.configuration.api.ConfigurationUtilities;
import org.glassfish.hk2.configuration.persistence.properties.PropertyFileBean;
import org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle;
import org.glassfish.hk2.configuration.persistence.properties.PropertyFileService;
import org.glassfish.hk2.configuration.persistence.properties.PropertyFileUtilities;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Ensures and demonstrates how the configuration drives the creation of the
 * Web Server and the associated certificates
 * 
 * @author jwells
 *
 */
public class WebServerTest {
    private ServiceLocator locator;
    
    @Before
    public void before() {
        locator = ServiceLocatorFactory.getInstance().create(null);
        
        // Enable HK2 service integration
        ConfigurationUtilities.enableConfigurationSystem(locator);
        
        // Enable Properties service, to get service properties from a Properties object
        PropertyFileUtilities.enablePropertyFileService(locator);
        
        // The propertyFileBean contains the mapping from type names to Java Beans
        PropertyFileBean propertyFileBean = new PropertyFileBean();
        propertyFileBean.addTypeMapping("WebServerBean", WebServerBean.class);
        propertyFileBean.addTypeMapping("SSLCertificateBean", SSLCertificateBean.class);
        
        // Add in the mapping from type name to bean classes
        PropertyFileService propertyFileService = locator.getService(PropertyFileService.class);
        propertyFileService.addPropertyFileBean(propertyFileBean);
        
        // Add the test services themselves
        ServiceLocatorUtilities.addClasses(locator,
                SSLCertificateService.class,
                WebServerImpl.class);
    }
    
    /**
     * This test demonstrates adding and the modifying the http and
     * ssl ports of the web server
     */
    @Test // @org.junit.Ignore
    public void testDemonstrateWebServerConfiguration() throws IOException {
        // Before we add a configuration there is no web server
        WebServer webServer = locator.getService(WebServer.class);
        Assert.assertNull(webServer);
        
        Properties configuration = new Properties();
        
        // Gets the URL of the configuration property file.  This
        // file contains one web server and two SSL certificate
        // configuration objects
        URL configURL = getClass().getClassLoader().getResource("config.prop");
        InputStream configStream = configURL.openConnection().getInputStream();
        try {
            // Read the property file
            configuration.load(configStream);
        }
        finally {
            configStream.close();
        }
        
        // In order to read the Properties object into HK2 we need to get a PropertyFileHandle
        PropertyFileService propertyFileService = locator.getService(PropertyFileService.class);
        PropertyFileHandle propertyFileHandle = propertyFileService.createPropertyHandleOfAnyType();
        
        // Now read the configuration into hk2
        propertyFileHandle.readProperties(configuration);
        
        // We should now have a web server!
        webServer = locator.getService(WebServer.class);
        Assert.assertNotNull(webServer);
        
        // Lets open all the ports, and check that they have the expected values
        // In this case the ports are:
        // adminPort = 7070
        // sslPort = 81
        // port = 80
        Assert.assertEquals((int) 7070, webServer.openAdminPort());
        Assert.assertEquals((int) 81, webServer.openSSLPort());
        Assert.assertEquals((int) 80, webServer.openPort());
        
        // Now lets check that we have two SSL certificates
        List<File> certs = webServer.getCertificates();
        
        // The two certificates should be Corporatex509.cert and HRx509.cert
        Assert.assertEquals(2, certs.size());
        
        HashSet<String> foundCerts = new HashSet<String>();
        for (File cert : certs) {
            foundCerts.add(cert.getName());
        }
        
        Assert.assertTrue(foundCerts.contains("Corporatex509.cert"));
        Assert.assertTrue(foundCerts.contains("HRx509.cert"));
        
        // OK, we have verified that all of the parameters of the
        // webserver are as expected.  We are now going to dynamically
        // change all the ports.  In the webserver however only
        // the ssl and http ports are dynamic, so after the change
        // only the ssl and http ports should have their new values,
        // while the admin port should remain with the old value
        
        // Change the ports so that they look like this in the properties file:
        // adminPort = 8082
        // sslPort = 8081
        // port = 8080
        configuration.put("WebServerBean.Acme.adminPort", "8082");
        configuration.put("WebServerBean.Acme.sslPort", "8081");
        configuration.put("WebServerBean.Acme.port", "8080");
        
        // Tell hk2 about the change
        propertyFileHandle.readProperties(configuration);
        
        // Now lets check the web server, make sure the ports have been modified
        
        // The adminPort is NOT dynamic in the back end service, so it did not change
        Assert.assertEquals(7070, webServer.getAdminPort());
        
        // But the SSL and HTTP ports have changed dynamically
        Assert.assertEquals(8081, webServer.getSSLPort());
        Assert.assertEquals(8080, webServer.openPort());
    }
}
