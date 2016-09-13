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
package org.glassfish.hk2.xml.test.defaulting;

import java.net.URL;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.beans.DomainBean;
import org.glassfish.hk2.xml.test.beans.SSLManagerBean;
import org.glassfish.hk2.xml.test.beans.SSLManagerBeanCustomizer;
import org.glassfish.hk2.xml.test.beans.SecurityManagerBean;
import org.glassfish.hk2.xml.test.dynamic.merge.MergeTest;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class DefaultingTest {
    private final static String DEFAULTING_FILE = "defaulted.xml";
    
    /**
     * Tests that we can default values with JAXB 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testDefaultedValues() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(DEFAULTING_FILE);
        
        XmlRootHandle<DefaultedBean> rootHandle = xmlService.unmarshall(url.toURI(), DefaultedBean.class);
        DefaultedBean db = rootHandle.getRoot();
        
        Assert.assertEquals(13, db.getIntProp());
        Assert.assertEquals(13L, db.getLongProp());
        Assert.assertEquals((byte) 13, db.getByteProp());
        Assert.assertEquals(true, db.isBooleanProp());
        Assert.assertEquals((short) 13, db.getShortProp());
        Assert.assertEquals('f', db.getCharProp());
        Assert.assertEquals(0, Float.compare((float) 13.00, db.getFloatProp()));
        Assert.assertEquals(0, Double.compare(13.00, db.getDoubleProp()));
        Assert.assertEquals("13", db.getStringProp());
    }
    
    /**
     * Tests that we can default values with JAXB 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testDefaultDefaultedValues() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(DEFAULTING_FILE);
        
        XmlRootHandle<DefaultedBean> rootHandle = xmlService.unmarshall(url.toURI(), DefaultedBean.class);
        DefaultedBean db = rootHandle.getRoot();
        
        Assert.assertEquals(0, db.getDefaultIntProp());
        Assert.assertEquals(0L, db.getDefaultLongProp());
        Assert.assertEquals((byte) 0, db.getDefaultByteProp());
        Assert.assertEquals(false, db.isDefaultBooleanProp());
        Assert.assertEquals((short) 0, db.getDefaultShortProp());
        Assert.assertEquals((char) 0, db.getDefaultCharProp());
        Assert.assertEquals(0, Float.compare((float) 0.00, db.getDefaultFloatProp()));
        Assert.assertEquals(0, Double.compare(0.00, db.getDefaultDoubleProp()));
        Assert.assertEquals(null, db.getDefaultStringProp());
    }
    
    /**
     * Tests that defaults work in a dynamically created bean
     */
    @Test // @org.junit.Ignore
    public void testCanGetValuesFromDynamicallyCreatedBean() {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        DefaultedBean db = xmlService.createBean(DefaultedBean.class);
        
        Assert.assertEquals(13, db.getIntProp());
        Assert.assertEquals(13L, db.getLongProp());
        Assert.assertEquals((byte) 13, db.getByteProp());
        Assert.assertEquals(true, db.isBooleanProp());
        Assert.assertEquals((short) 13, db.getShortProp());
        Assert.assertEquals('f', db.getCharProp());
        Assert.assertEquals(0, Float.compare((float) 13.00, db.getFloatProp()));
        Assert.assertEquals(0, Double.compare(13.00, db.getDoubleProp()));
        Assert.assertEquals("13", db.getStringProp());
        
        Assert.assertEquals(0, db.getDefaultIntProp());
        Assert.assertEquals(0L, db.getDefaultLongProp());
        Assert.assertEquals((byte) 0, db.getDefaultByteProp());
        Assert.assertEquals(false, db.isDefaultBooleanProp());
        Assert.assertEquals((short) 0, db.getDefaultShortProp());
        Assert.assertEquals((char) 0, db.getDefaultCharProp());
        Assert.assertEquals(0, Float.compare((float) 0.00, db.getDefaultFloatProp()));
        Assert.assertEquals(0, Double.compare(0.00, db.getDefaultDoubleProp()));
        Assert.assertEquals(null, db.getDefaultStringProp());
        
    }
    
    /**
     * Ensures that we can default beans via an InstanceLifecycleListener
     * 
     * @throws Exception
     */
    @Test
    public void testDefaultingViaServiceWorks() throws Exception {
        ServiceLocator locator = Utilities.createLocator(
                SSLManagerBeanCustomizer.class,
                SecurityManagerBeanDefaulter.class);
        
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshall(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator, true);
        
        SecurityManagerBean smb = locator.getService(SecurityManagerBean.class);
        Assert.assertNotNull(smb);
        
        Assert.assertNotNull(smb.getSSLManager());
        
        Assert.assertNotNull(locator.getService(SSLManagerBean.class));
    }

}
