/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.interop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.json.api.JsonUtilities;
import org.glassfish.hk2.pbuf.api.PBufUtilities;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.spi.XmlServiceParser;
import org.glassfish.hk2.xml.test1.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class InteropTest {
    private final static String STANDARD_XML = "standard.xml";
    private final static String STANDARD_PBUF = "standard.pbuf";
    
    private final static String ALICE = "Alice";
    private final static int ALICE_ADDRESS = 100;
    
    private final static String BOB = "Bob";
    private final static int BOB_ADDRESS = 200;
    
    /**
     * Reads an XML file, spits out JSON
     */
    @Test
    public void testXml2Json() throws Exception {
        ServiceLocator locator = Utilities.createInteropLocator();
        
        XmlService xmlXmlService = locator.getService(XmlService.class, XmlServiceParser.STREAM_PARSING_SERVICE);
        Assert.assertNotNull(xmlXmlService);
        
        XmlService jsonXmlService = locator.getService(XmlService.class, JsonUtilities.JSON_SERVICE_NAME);
        Assert.assertNotNull(jsonXmlService);
        
        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource(STANDARD_XML);
        
        XmlRootHandle<InteropRootBean> xmlHandle = xmlXmlService.unmarshal(url.toURI(), InteropRootBean.class);
        validateStandardBean(xmlHandle);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            jsonXmlService.marshal(baos, xmlHandle);
        }
        finally {
            baos.close();
        }
        
        byte asBytes[] = baos.toByteArray();
        String asString = new String(asBytes);
        
        Assert.assertTrue(asString.contains("\"name\":\"Alice\""));
        Assert.assertTrue(asString.contains("\"name\":\"Bob\""));
        
        Assert.assertTrue(asString.contains("\"houseNumber\":100"));
        Assert.assertTrue(asString.contains("\"houseNumber\":200"));
    }
    
    /**
     * Reads an XML file, spits out pbuf
     */
    @Test
    public void testXml2PBuf() throws Exception {
        ServiceLocator locator = Utilities.createInteropLocator();
        
        XmlService xmlXmlService = locator.getService(XmlService.class, XmlServiceParser.STREAM_PARSING_SERVICE);
        Assert.assertNotNull(xmlXmlService);
        
        XmlService pbufXmlService = locator.getService(XmlService.class, PBufUtilities.PBUF_SERVICE_NAME);
        Assert.assertNotNull(pbufXmlService);
        
        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource(STANDARD_XML);
        
        XmlRootHandle<InteropRootBean> xmlHandle = xmlXmlService.unmarshal(url.toURI(), InteropRootBean.class);
        validateStandardBean(xmlHandle);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            pbufXmlService.marshal(baos, xmlHandle);
        }
        finally {
            baos.close();
        }
        
        byte asBytes[] = baos.toByteArray();
        
        URL pbufURL = loader.getResource(STANDARD_PBUF);
        
        byte benchmark[] = readURLCompletely(pbufURL);
        
        Assert.assertTrue(Arrays.equals(asBytes, benchmark));
    }
    
    private static byte[] readURLCompletely(URL url) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            InputStream is = url.openStream();
            
            try {
                byte buf[] = new byte[1000];
                
                int received = 0;
                while((received = is.read(buf)) >= 0) {
                    baos.write(buf, 0, received);
                }
            }
            finally {
                is.close();
            }
            
        }
        finally {
            baos.close();
        }
        
        return baos.toByteArray();
    }
    
    private static void validateStandardBean(XmlRootHandle<InteropRootBean> handle) {
        Assert.assertNotNull(handle);
        
        InteropRootBean root = handle.getRoot();
        Assert.assertNotNull(root);
        
        List<InteropChildBean> children = root.getChildren();
        Assert.assertNotNull(children);
        
        Assert.assertEquals(2, children.size());
        
        {
            InteropChildBean alice = children.get(0);
            Assert.assertEquals(ALICE, alice.getName());
            Assert.assertEquals(ALICE_ADDRESS, alice.getHouseNumber());
        }
        
        {
            InteropChildBean bob = children.get(1);
            Assert.assertEquals(BOB, bob.getName());
            Assert.assertEquals(BOB_ADDRESS, bob.getHouseNumber());
        }
    }

}
