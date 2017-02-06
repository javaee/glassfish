/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016-2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.dynamic.marshall;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.beans.DomainBean;
import org.glassfish.hk2.xml.test.beans2.RefereeBean;
import org.glassfish.hk2.xml.test.beans2.ReferencesBean;
import org.glassfish.hk2.xml.test.dynamic.merge.MergeTest;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class MarshallTest {
    private final static File OUTPUT_FILE = new File("output.xml");
    private final static String LOOK_FOR_ME = "0.255.255.255";
    public final static String REFEREES1_FILE = "Referees1.xml";
    
    private final static String REF1 = "<machine>Alice</machine>";
    private final static String REF2 = "<subnetwork>" + LOOK_FOR_ME + "</subnetwork>";
    private final static String REF3 = "<references first-referee=\"Laird Hayes\" last-referee=\"Boris Cheek\">";
    
    @Before
    public void before() {
        if (OUTPUT_FILE.exists()) {
            boolean didDelete = OUTPUT_FILE.delete();
            Assert.assertTrue(didDelete);
        }
    }
    
    @After
    public void after() {
        if (OUTPUT_FILE.exists()) {
            OUTPUT_FILE.delete();
        }
    }
    
    /**
     * Tests that the output contains nice output
     */
    @Test
    // @org.junit.Ignore
    public void testMarshallBackAfterUpdate() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshal(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
        DomainBean root = rootHandle.getRoot();
        root.setSubnetwork(LOOK_FOR_ME);
        
        FileOutputStream fos = new FileOutputStream(OUTPUT_FILE);
        try {
          rootHandle.marshal(fos);
        }
        finally {
            fos.close();
        }
        
        checkFile(REF1, REF2);
    }
    
    private void checkFile(String... strings) throws Exception {
        Map<String, Boolean> foundAll = new HashMap<String, Boolean>();
        for (String string : strings) {
            foundAll.put(string, false);
        }
        
        FileReader reader = new FileReader(OUTPUT_FILE);
        BufferedReader buffered = new BufferedReader(reader);
        
        try {
            String line;
            while ((line = buffered.readLine()) != null) {
                for (String string : foundAll.keySet()) {
                    if (line.contains(string)) {
                        foundAll.put(string, true);
                    }
                }
            }
        }
        finally {
            buffered.close();
            reader.close();
        }
        
        for (Map.Entry<String, Boolean> entry : foundAll.entrySet()) {
            String lookingFor = entry.getKey();
            boolean found = entry.getValue();
            
            Assert.assertTrue("Did not find the string " + lookingFor, found);
        }
    }
    
    /**
     * Tests that the output contains nice output
     */
    @Test
    // @org.junit.Ignore
    public void testMarshallBackAfterUpdateDom() throws Exception {
        ServiceLocator locator = Utilities.createDomLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshal(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
        DomainBean root = rootHandle.getRoot();
        root.setSubnetwork(LOOK_FOR_ME);
        
        FileOutputStream fos = new FileOutputStream(OUTPUT_FILE);
        try {
          rootHandle.marshal(fos);
        }
        finally {
            fos.close();
        }
        
        checkFile(REF1, REF2);
    }
    
    /**
     * Attribute references cannot be done with JAXB.  So this
     * file is kept separately for this purpose
     */
    @Test
    // @org.junit.Ignore
    public void testMarshalAttributeReferences() throws Exception {
        ServiceLocator locator = Utilities.createDomLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(REFEREES1_FILE);
        
        XmlRootHandle<ReferencesBean> rootHandle = xmlService.unmarshal(url.toURI(), ReferencesBean.class, false, false);
        
        ReferencesBean references = rootHandle.getRoot();
        
        RefereeBean hayes = references.getReferees().get(0);
        RefereeBean cheek = references.getReferees().get(1);
        
        Assert.assertNotNull(hayes);
        Assert.assertNotNull(cheek);
        
        Assert.assertEquals(hayes, references.getFirstReferee());
        Assert.assertEquals(cheek, references.getLastReferee());
        
        FileOutputStream fos = new FileOutputStream(OUTPUT_FILE);
        try {
          rootHandle.marshal(fos);
        }
        finally {
            fos.close();
        }
        
        checkFile(REF3);
    }
    
    private final static String A = "A";
    private final static String B = "B";
    private final static String C = "C";
    private final static String D = "D";
    private final static String E = "E";
    private final static String F = "F";
    private final static String G = "G";
    private final static String H = "H";
    private final static String I = "I";
    private final static String J = "J";
    private final static String K = "K";
    
    private static void fillInKeyedLeafBean(KeyedLeafBean klb) {
        klb.setPropertyI(I);
        klb.setPropertyH(H);
    }
    
    private static void fillInUnkeyedLeafBean(UnkeyedLeafBean ulb) {
        ulb.setPropertyJ(J);
        ulb.setPropertyK(K);
    }
    
    private static void fillInRootBean(OrderingRootBean orb, XmlService xmlService) {
        KeyedLeafBean propA = orb.addPropertyA(H);
        fillInKeyedLeafBean(propA);
        
        UnkeyedLeafBean propB = orb.addPropertyB();
        fillInUnkeyedLeafBean(propB);
        
        UnkeyedLeafBean propC = orb.addPropertyC();
        fillInUnkeyedLeafBean(propC);
        
        KeyedLeafBean propD = orb.addPropertyD(H);
        fillInKeyedLeafBean(propD);
        
        KeyedLeafBean propE = xmlService.createBean(KeyedLeafBean.class);
        fillInKeyedLeafBean(propE);
        orb.setPropertyE(propE);
        
        UnkeyedLeafBean propF = xmlService.createBean(UnkeyedLeafBean.class);
        fillInUnkeyedLeafBean(propF);
        orb.setPropertyF(propF);
        
        orb.setPropertyG(G);
    }
    
    /**
     * Attribute references cannot be done with JAXB.  So this
     * file is kept separately for this purpose
     */
    @Test
    // @org.junit.Ignore
    public void testOrderingSpecifiedWithXmlType() throws Exception {
        ServiceLocator locator = Utilities.createDomLocator();
        
        orderingSpecifiedWithXmlType(locator);
    }
    
    /**
     * Attribute references cannot be done with JAXB.  So this
     * file is kept separately for this purpose
     */
    @Test
    // @org.junit.Ignore
    public void testOrderingSpecifiedWithXmlTypeJAXB() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        orderingSpecifiedWithXmlType(locator);
    }
    
    /**
     * Attribute references cannot be done with JAXB.  So this
     * file is kept separately for this purpose
     */
    private void orderingSpecifiedWithXmlType(ServiceLocator locator) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<OrderingRootBean> rootHandle = xmlService.createEmptyHandle(OrderingRootBean.class);
        
        rootHandle.addRoot();
        OrderingRootBean root = rootHandle.getRoot();
        
        fillInRootBean(root, xmlService);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
          rootHandle.marshal(baos);
        }
        finally {
            baos.close();
        }
        
        LinkedHashMap<Integer, String> lines = new LinkedHashMap<Integer, String>();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        BufferedReader br = new BufferedReader(new InputStreamReader(bais));
        try {
            String line;
            int lcv = 0;
            while ((line = br.readLine()) != null) {
                lines.put(lcv, line);
                lcv++;
            }
            
        }
        finally {
            br.close();
        }
        
        String failureDocument = baos.toString();
        
        boolean foundF = false;
        boolean foundG = false;
        boolean foundE = false;
        boolean foundA = false;
        boolean foundC = false;
        boolean foundB = false;
        boolean foundD = false;
        for (Map.Entry<Integer, String> lineEntry : lines.entrySet()) {
            String line = lineEntry.getValue();
            int lineNumber = lineEntry.getKey();
            
            if (line.contains("<f>")) {
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundF);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundG);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundE);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundA);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundB);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundD);
                
                foundF = true;
            }
            
            if (line.contains("<g>")) {
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundF);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundG);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundE);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundA);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundC);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundB);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundD);
                
                foundG = true;
            }
            
            if (line.contains("<e>")) {
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundF);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundG);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundE);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundA);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundC);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundB);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundD);
                
                foundE = true;
            }
            
            if (line.contains("<a>")) {
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundF);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundG);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundE);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundA);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundC);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundB);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundD);
                
                foundA = true;
            }
            
            if (line.contains("<c>")) {
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundF);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundG);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundE);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundA);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundC);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundB);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundD);
                
                foundC = true;
            }
            
            if (line.contains("<b>")) {
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundF);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundG);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundE);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundA);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundC);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundB);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundD);
                
                foundB = true;
            }
            
            if (line.contains("<d>")) {
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundF);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundG);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundE);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundA);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundC);
                Assert.assertTrue("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundB);
                Assert.assertFalse("Order wrong on line " + lineNumber + " of\n" + failureDocument, foundD);
                
                foundD = true;
            }
        }
        
        Assert.assertTrue(foundD);
    }

}
