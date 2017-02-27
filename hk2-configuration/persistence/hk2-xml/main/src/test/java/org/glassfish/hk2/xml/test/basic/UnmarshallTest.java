/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.xml.test.basic;

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.api.XmlServiceUtilities;
import org.glassfish.hk2.xml.spi.XmlServiceParser;
import org.glassfish.hk2.xml.test.basic.beans.Commons;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for unmarshalling xml into the hk2 hub
 * 
 * @author jwells
 */
public class UnmarshallTest {
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testInterfaceJaxbUnmarshalling() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        URL url = getClass().getClassLoader().getResource(Commons.MUSEUM1_FILE);
        URI uri = url.toURI();
        
        Commons.testInterfaceJaxbUnmarshalling(locator, uri);
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testBeanLikeMapOfInterface() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        URL url = getClass().getClassLoader().getResource(Commons.ACME1_FILE);
        URI uri = url.toURI();
        
        Commons.testBeanLikeMapOfInterface(locator, uri);
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testInterfaceJaxbUnmarshallingWithChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME1_FILE);
        URI uri = url.toURI();
        
        Commons.testInterfaceJaxbUnmarshallingWithChildren(locator, uri);
    }
    
    /**
     * Tests a more complex XML format.  This test will ensure
     * all elements are in the Hub with expected names
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testComplexUnmarshalling() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        URL url = getClass().getClassLoader().getResource(Commons.SAMPLE_CONFIG_FILE);
        URI uri = url.toURI();
        
        Commons.testComplexUnmarshalling(locator, uri);
    }
    
    /**
     * Associations has unkeyed children of type Association.  We
     * get them and make sure they have unique keys generated
     * by the system
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testUnkeyedChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        URL url = getClass().getClassLoader().getResource(Commons.SAMPLE_CONFIG_FILE);
        URI uri = url.toURI();
        
        Commons.testUnkeyedChildren(locator, uri);
    }
    
    /**
     * Foobar has two children, foo and bar, both of which are of type DataBean
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testSameClassTwoChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        URL url = getClass().getClassLoader().getResource(Commons.FOOBAR_FILE);
        URI uri = url.toURI();
        
        Commons.testSameClassTwoChildren(locator, uri);
    }
    
    /**
     * Tests that an xml hierarchy with a cycle can be unmarshalled
     * 
     * @throws Exception
     */
    @Test
    public void testBeanCycle() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        URL url = getClass().getClassLoader().getResource(Commons.CYCLE_FILE);
        URI uri = url.toURI();
        
        Commons.testBeanCycle(locator, uri);
    }
    
    /**
     * Tests every scalar type that can be read
     * 
     * @throws Exception
     */
    @Test
    public void testEveryType() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        URL url = getClass().getClassLoader().getResource(Commons.TYPE1_FILE);
        URI uri = url.toURI();
        
        Commons.testEveryType(locator, uri);
    }
    
    /**
     * Tests that the annotation is fully copied over on the method
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testAnnotationWithEverythingCopied() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME1_FILE);
        URI uri = url.toURI();
        
        Commons.testAnnotationWithEverythingCopied(locator, uri);
    }
    
    /**
     * Tests that a list child with no elements returns an empty list (not null)
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testEmptyListChildReturnsEmptyList() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME1_FILE);
        URI uri = url.toURI();
        
        Commons.testEmptyListChildReturnsEmptyList(locator, uri);
    }
    
    /**
     * Tests that a list child with no elements returns an empty array (not null)
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testEmptyArrayChildReturnsEmptyArray() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME1_FILE);
        URI uri = url.toURI();
        
        Commons.testEmptyArrayChildReturnsEmptyArray(locator, uri);
    }
    
    /**
     * Tests that a byte[] child gets properly translated
     * (into itself, for now)
     * 
     * @throws Exception
     */
    @Test
    @org.junit.Ignore
    public void testByteArrayNonChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        URL url = getClass().getClassLoader().getResource(Commons.ACME2_FILE);
        URI uri = url.toURI();
        
        Commons.testByteArrayNonChild(locator, uri);
    }
    
    /**
     * Tests that JaxB style references work.
     * These are references that use XmlID and XmlIDREF
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testJaxbStyleReference() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        URL url = getClass().getClassLoader().getResource(Commons.SAMPLE_CONFIG_FILE);
        URI uri = url.toURI();
        
        Commons.testJaxbStyleReference(locator, uri);
    }
    
    /**
     * Tests that JaxB style references work
     * even if the referenced object is AFTER the stanza
     * being referenced
     * 
     * These are references that use XmlID and XmlIDREF
     * 
     * Ignored because:  JAXB is having trouble with the
     * class being used in the XmlAttribute.  This is
     * because XmlAttribute does not have the type field
     * that XmlElement has.  It is unclear that this feature
     * can be supported because of it in the JAXB version.
     * If this feature is needed it'll have to be from the
     * XmlDom version.
     * 
     * @throws Exception
     */
    @Test
    @org.junit.Ignore
    public void testJaxbStyleForwardReference() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        
        URL url = getClass().getClassLoader().getResource(Commons.REFERENCE1_FILE);
        URI uri = url.toURI();
        
        Commons.testJaxbStyleForwardReference(locator, uri);
    }
    
    /**
     * Tests that JaxB style references work.
     * These are references that use XmlID and XmlIDREF
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testIdempotent() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null, null, Utilities.GENERATOR);
        
        XmlServiceUtilities.enableXmlService(locator);
        XmlServiceUtilities.enableXmlService(locator);
       
        List<XmlService> allXmlServices = locator.getAllServices(XmlService.class);
        Assert.assertEquals(1, allXmlServices.size());
        
        List<XmlServiceParser> allParsers = locator.getAllServices(XmlServiceParser.class);
        Assert.assertEquals(1, allParsers.size());
        
        Assert.assertNotNull(locator.getService(XmlService.class, XmlServiceParser.DEFAULT_PARSING_SERVICE));
        Assert.assertNull(locator.getService(XmlService.class, XmlServiceParser.STREAM_PARSING_SERVICE));
        
        Assert.assertNotNull(locator.getService(XmlServiceParser.class, XmlServiceParser.DEFAULT_PARSING_SERVICE));
        Assert.assertNull(locator.getService(XmlServiceParser.class, XmlServiceParser.STREAM_PARSING_SERVICE));
        
        XmlServiceUtilities.enableDomXmlService(locator);
        XmlServiceUtilities.enableDomXmlService(locator);
        
        allXmlServices = locator.getAllServices(XmlService.class);
        Assert.assertEquals(2, allXmlServices.size());
        
        allParsers = locator.getAllServices(XmlServiceParser.class);
        Assert.assertEquals(2, allParsers.size());
        
        Assert.assertNotNull(locator.getService(XmlService.class, XmlServiceParser.DEFAULT_PARSING_SERVICE));
        Assert.assertNotNull(locator.getService(XmlService.class, XmlServiceParser.STREAM_PARSING_SERVICE));
        
        Assert.assertNotNull(locator.getService(XmlServiceParser.class, XmlServiceParser.DEFAULT_PARSING_SERVICE));
        Assert.assertNotNull(locator.getService(XmlServiceParser.class, XmlServiceParser.STREAM_PARSING_SERVICE));
        
        
    }
}
