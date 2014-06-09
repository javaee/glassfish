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
package org.glassfish.hk2.configuration.hub.xml.dom.integration.tests;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.XmlDomIntegrationUtilities;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * @author jwells
 *
 */
public class HubIntegrationTest extends HK2Runner {
    private final static String ABEAN_TAG = "a-bean";
    private final static String BBEAN_TAG = "b-bean";
    private final static String CBEAN_TAG = "b-bean/c-bean";
    
    private final static String ALICE = "alice";
    private final static String BOB = "bob";
    
    private final static String HELLO = "hello";
    
    @Inject
    private Hub hub;
    
    @Before
    public void before() {
        super.initialize("HubIntegrationTest", null, null);
    }
    
    /**
     * Tests just adding one bean
     */
    @Test // @org.junit.Ignore
    public void testAddOneBean() {
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("simple.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        ABean abean = testLocator.getService(ABean.class);
        Assert.assertNotNull(abean);
        
        Assert.assertEquals(HELLO, abean.getStringParameter());
        Assert.assertEquals(10, abean.getIntParameter());
        Assert.assertEquals(100L, abean.getLongParameter());
        
        BeanDatabase beanDatabase = hub.getCurrentDatabase();
        Object instance = beanDatabase.getInstance(ABEAN_TAG, XmlDomIntegrationUtilities.DEFAULT_INSTANCE_NAME);
        Assert.assertNotNull(instance);
        
        Assert.assertTrue(instance instanceof ABean);
        ABean abeanInstance = (ABean) instance;
        Assert.assertEquals(HELLO, abeanInstance.getStringParameter());
        Assert.assertEquals(10, abeanInstance.getIntParameter());
        Assert.assertEquals(100L, abeanInstance.getLongParameter());
    }
    
    /**
     * Tests an outer bean that has a sub-keyed bean
     */
    @Test @org.junit.Ignore
    public void testComplexKeyedBean() {
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex1.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        BBean bbean = testLocator.getService(BBean.class);
        Assert.assertNotNull(bbean);
        
        Assert.assertEquals(HELLO, bbean.getParameter());
        List<CBean> cbeans = bbean.getCBeans();
        
        Assert.assertEquals(2, cbeans.size());
        
        boolean foundAlice = false;
        boolean foundBob = false;
        for (CBean cbean : cbeans) {
            if (ALICE.equals(cbean.getName())) {
                foundAlice = true;
            }
            else if (BOB.equals(cbean.getName())) {
                foundBob = true;
            }
        }
        
        Assert.assertTrue(foundAlice);
        Assert.assertTrue(foundBob);
        
        BeanDatabase beanDatabase = hub.getCurrentDatabase();
        Object bInstance = beanDatabase.getInstance(BBEAN_TAG, XmlDomIntegrationUtilities.DEFAULT_INSTANCE_NAME);
        Assert.assertNotNull(bInstance);
        
        Object aliceInstance = beanDatabase.getInstance(CBEAN_TAG, ALICE);
        Object bobInstance = beanDatabase.getInstance(CBEAN_TAG, BOB);
        
        ServiceLocatorUtilities.dumpAllDescriptors(testLocator, System.out);
        
        Assert.assertNotNull(aliceInstance);
        Assert.assertNotNull(bobInstance);
    }

}
