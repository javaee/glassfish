/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.validation;

import java.net.URL;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ValidationTest {
    private final static String VALID1_FILE = "valid1.xml";
    private final static String VALID2_FILE = "valid2.xml";
    private final static String INVALID1_FILE = "invalid1.xml";
    private final static String INVALID2_FILE = "invalid2.xml";
    private final static String INVALID3_FILE = "invalid3.xml";
    private final static String INVALID4_FILE = "invalid4.xml";
    
    private final static String E1 = "E1";
    
    /**
     * Tests that validation on a valid file works
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testValidDocument() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(VALID1_FILE);
        
        XmlRootHandle<ValidationRootBean> rootHandle = xmlService.unmarshall(url.toURI(), ValidationRootBean.class);
        
        Assert.assertFalse(rootHandle.isValidating());
        
        rootHandle.startValidating();
        
        Assert.assertTrue(rootHandle.isValidating());
        
        rootHandle.stopValidating();
        
        Assert.assertFalse(rootHandle.isValidating());
    }
    
    /**
     * Tests that validation on a valid file works
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testInvalidDocument() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(INVALID1_FILE);
        
        XmlRootHandle<ValidationRootBean> rootHandle = xmlService.unmarshall(url.toURI(), ValidationRootBean.class);
        
        ValidationRootBean validationRoot = rootHandle.getRoot();
        Assert.assertNull(validationRoot.getElementOne());
        
        try {
            rootHandle.startValidating();
            Assert.fail("Should have failed");
        }
        catch (ConstraintViolationException me) {
            // Expected
        }
    }
    
    /**
     * Tests that validation happens on a bad set
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testBadSet() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(VALID1_FILE);
        
        XmlRootHandle<ValidationRootBean> rootHandle = xmlService.unmarshall(url.toURI(), ValidationRootBean.class);
        
        rootHandle.startValidating();
        
        ValidationRootBean root = rootHandle.getRoot();
        Assert.assertEquals(E1, root.getElementOne());
        
        try {
            root.setElementOne(null);
            Assert.fail("Should not have worked because validation is on");
        }
        catch (MultiException e) {
            boolean found = false;
            for (Throwable th : e.getErrors()) {
                if (th instanceof ConstraintViolationException) {
                    found = true;
                    break;
                }
            }
            
            Assert.assertTrue("Did not find expected exception in " + e, found);
        }
        
        // Nothing should have changed
        Assert.assertEquals(E1, root.getElementOne());
        
    }
    
    /**
     * Tests that validation on a valid file works
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testValidDocumentWithChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(VALID2_FILE);
        
        XmlRootHandle<ValidationRootBean> rootHandle = xmlService.unmarshall(url.toURI(), ValidationRootBean.class);
        
        rootHandle.startValidating();
    }
    
    /**
     * Tests that validation on an invalid list child fails
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testValidDocumentWithInvalidListChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(INVALID2_FILE);
        
        XmlRootHandle<ValidationRootBean> rootHandle = xmlService.unmarshall(url.toURI(), ValidationRootBean.class);
        
        try {
            rootHandle.startValidating();
            Assert.fail("Should have failed");
        }
        catch (ConstraintViolationException me) {
            // Expected
        }
    }
    
    /**
     * Tests that validation on an invalid list child fails
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testValidDocumentWithInvalidArrayChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(INVALID3_FILE);
        
        XmlRootHandle<ValidationRootBean> rootHandle = xmlService.unmarshall(url.toURI(), ValidationRootBean.class);
        
        try {
            rootHandle.startValidating();
            Assert.fail("Should have failed");
        }
        catch (ConstraintViolationException me) {
            // Expected
        }
    }
    
    /**
     * Tests that validation on an invalid list child fails
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testValidDocumentWithInvalidDirectChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(INVALID4_FILE);
        
        XmlRootHandle<ValidationRootBean> rootHandle = xmlService.unmarshall(url.toURI(), ValidationRootBean.class);
        
        try {
            rootHandle.startValidating();
            Assert.fail("Should have failed");
        }
        catch (ConstraintViolationException me) {
            // Expected
        }
    }
    
    /**
     * Tests that validation on a valid file works
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testAddInvalidListChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(VALID2_FILE);
        
        XmlRootHandle<ValidationRootBean> rootHandle = xmlService.unmarshall(url.toURI(), ValidationRootBean.class);
        
        rootHandle.startValidating();
        
        ValidationChildBean listChild = xmlService.createBean(ValidationChildBean.class);
        
        // Do NOT fill in ElementTwo
        ValidationRootBean root = rootHandle.getRoot();
        
        try {
            root.addListChild(listChild);
            Assert.fail("Add of invalid bean should have failed");
        }
        catch (ConstraintViolationException me) {
            // Expected
            Set<ConstraintViolation<?>> violations = me.getConstraintViolations();
            for (ConstraintViolation<?> violation : violations) {
                Assert.assertEquals("may not be null", violation.getMessage());
            }
        }
    }
    
    /**
     * Tests that validation on a valid file works
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testAddInvalidArrayChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(VALID2_FILE);
        
        XmlRootHandle<ValidationRootBean> rootHandle = xmlService.unmarshall(url.toURI(), ValidationRootBean.class);
        
        rootHandle.startValidating();
        
        ValidationChildArrayBean arrayChild = xmlService.createBean(ValidationChildArrayBean.class);
        
        // Do NOT fill in ElementTwo
        ValidationRootBean root = rootHandle.getRoot();
        
        try {
            root.addArrayChild(arrayChild);
            Assert.fail("Add of invalid array child should have failed");
        }
        catch (ConstraintViolationException me) {
            // Expected
            Set<ConstraintViolation<?>> violations = me.getConstraintViolations();
            for (ConstraintViolation<?> violation : violations) {
                Assert.assertEquals("may not be null", violation.getMessage());
            }
        }
    }
    
    /**
     * Tests that validation on a valid file works
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testAddInvalidDirectChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(VALID2_FILE);
        
        XmlRootHandle<ValidationRootBean> rootHandle = xmlService.unmarshall(url.toURI(), ValidationRootBean.class);
        
        rootHandle.startValidating();
        
        ValidationChildDirectBean directChild = xmlService.createBean(ValidationChildDirectBean.class);
        
        // Do NOT fill in ElementTwo
        ValidationRootBean root = rootHandle.getRoot();
        
        // First remove the child before setting it to something bad
        root.setDirectChild(null);
        
        try {
            root.setDirectChild(directChild);
            Assert.fail("Add of invalid array child should have failed");
        }
        catch (ConstraintViolationException me) {
            // Expected
            Set<ConstraintViolation<?>> violations = me.getConstraintViolations();
            for (ConstraintViolation<?> violation : violations) {
                Assert.assertEquals("may not be null", violation.getMessage());
            }
        }
    }

}
