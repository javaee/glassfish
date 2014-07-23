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

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.Hub;
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
    private final static String ABEAN_TAG = "/a-bean";
    private final static String BBEAN_TAG = "/b-bean";
    private final static String CBEAN_TAG = "/b-bean/c-bean";
    private final static String DBEAN_TAG = "/h-bean/d-bean";
    private final static String EBEAN_TAG = "/h-bean/d-bean/e-bean";
    private final static String FBEAN_TAG = "/h-bean/d-bean/e-bean/f-bean";
    private final static String G0BEAN_TAG = "/h-bean/d-bean/e-bean/f-bean/g-bean";
    private final static String G1BEAN_TAG = "/h-bean/d-bean/e-bean/f-bean/g-bean/g-bean";
    
    private final static String ABEAN_INSTANCE_NAME = "a-bean";
    private final static String BBEAN_INSTANCE_NAME = "b-bean";
    private final static String DBEAN_ALICE_INSTANCE_NAME = "h-bean.alice";
    private final static String DBEAN_BOB_INSTANCE_NAME = "h-bean.bob";
    private final static String DBEAN_HOLYOKE_INSTANCE_NAME = "h-bean.holyoke";
    
    private final static String ALICE = "alice";
    private final static String BOB = "bob";
    private final static String CAROL = "carol";
    private final static String DAVE = "dave";
    private final static String ENGLEBERT = "englebert";
    private final static String FRED = "fred";
    private final static String GARY = "gary";
    private final static String HOLYOKE = "holyoke";
    private final static String ALICE_INSTANCE_NAME = "b-bean.alice";
    private final static String BOB_INSTANCE_NAME = "b-bean.bob";
    
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
        Object instance = beanDatabase.getInstance(ABEAN_TAG, ABEAN_INSTANCE_NAME);
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
    @Test // @org.junit.Ignore
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
        Object bInstance = beanDatabase.getInstance(BBEAN_TAG, BBEAN_INSTANCE_NAME);
        Assert.assertNotNull(bInstance);
        
        Object aliceInstance = beanDatabase.getInstance(CBEAN_TAG, ALICE_INSTANCE_NAME);
        Object bobInstance = beanDatabase.getInstance(CBEAN_TAG, BOB_INSTANCE_NAME);
        
        Assert.assertNotNull(aliceInstance);
        Assert.assertNotNull(bobInstance);
        
        Assert.assertEquals(ALICE, ((CBean) aliceInstance).getName());
        Assert.assertEquals(BOB, ((CBean) bobInstance).getName());
    }
    
    /**
     * Both cbeans have the same structure internally
     * 
     * @param cbean
     */
    private void checkDBean(DBean dbean) {
        String dbeanName = dbean.getName();
        
        EBean ebean = dbean.getEBean();
        Assert.assertNotNull(ebean);
        
        String eBeanInstanceName = "h-bean." + dbeanName + ".e-bean";
        
        Object ebeanInstance = hub.getCurrentDatabase().getInstance(EBEAN_TAG, eBeanInstanceName);
        Assert.assertNotNull(ebeanInstance);
        
        List<FBean> fbeans = ((EBean) ebeanInstance).getFBeans();
        Assert.assertNotNull(fbeans);
        Assert.assertEquals(2, fbeans.size());
        
        for (FBean fbean : fbeans) {
            String fbeanName = fbean.getName();
            
            String fbeanInstanceName = eBeanInstanceName + "." + fbeanName;
            Object fbeanInstance = hub.getCurrentDatabase().getInstance(FBEAN_TAG, fbeanInstanceName);
            Assert.assertNotNull(fbeanInstance);
            
            if (CAROL.equals(fbeanName)) {
                GBean daveGBean = ((FBean) fbeanInstance).getGBeans().get(0);
                Assert.assertNotNull(daveGBean);
                
                Assert.assertEquals(DAVE, daveGBean.getName());
                
                String daveGBeanInstanceName = fbeanInstanceName + "." + DAVE;
                
                Object daveGBeanInstance = hub.getCurrentDatabase().getInstance(G0BEAN_TAG, daveGBeanInstanceName);
                Assert.assertNotNull(daveGBeanInstance);
                Assert.assertEquals(DAVE, ((GBean) daveGBeanInstance).getName());
                
                GBean englebertGBean = daveGBean.getExtensions().get(0);
                Assert.assertNotNull(englebertGBean);
                
                Assert.assertEquals(ENGLEBERT, englebertGBean.getName());
                
                String englebertGBeanInstanceName = daveGBeanInstanceName + "." + ENGLEBERT;
                
                Object englebertGBeanInstance = hub.getCurrentDatabase().getInstance(G1BEAN_TAG, englebertGBeanInstanceName);
                Assert.assertNotNull(englebertGBeanInstance);
                Assert.assertEquals(ENGLEBERT, ((GBean) englebertGBeanInstance).getName());
            }
            else if (FRED.equals(fbeanName)) {
                GBean garyGBean = ((FBean) fbeanInstance).getGBeans().get(0);
                Assert.assertNotNull(garyGBean);
                
                Assert.assertEquals(GARY, garyGBean.getName());
                
                String garyGBeanInstanceName = fbeanInstanceName + "." + GARY;
                
                Object garyGBeanInstance = hub.getCurrentDatabase().getInstance(G0BEAN_TAG, garyGBeanInstanceName);
                Assert.assertNotNull(garyGBeanInstance);
                Assert.assertEquals(GARY, ((GBean) garyGBeanInstance).getName());
            }
            else {
                Assert.fail("Unkown fbean with name " + fbean.getName());
            }
            
        }
        
        
    }
    
    /**
     * Tests an outer bean with an inner bean that is
     * singleton but which itself has keyed beans, one of
     * which recurses on itself (fun!)
     */
    @Test // @org.junit.Ignore
    public void testUberComplexKeyedBean() {
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex2.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        BeanDatabase beanDatabase = hub.getCurrentDatabase();
        
        {
            DBean alice = testLocator.getService(DBean.class, ALICE);
            Assert.assertNotNull(alice);
        
            Object aliceInstance = beanDatabase.getInstance(DBEAN_TAG, DBEAN_ALICE_INSTANCE_NAME);
            Assert.assertNotNull(aliceInstance);
            
            Assert.assertEquals(ALICE, ((DBean) aliceInstance).getName());
            
            checkDBean((DBean) aliceInstance);
        }
        
        {
            DBean bob = testLocator.getService(DBean.class, BOB);
            Assert.assertNotNull(bob);
        
            Object bobInstance = beanDatabase.getInstance(DBEAN_TAG, DBEAN_BOB_INSTANCE_NAME);
            Assert.assertNotNull(bobInstance);
            
            Assert.assertEquals(BOB, ((DBean) bobInstance).getName());
            
            checkDBean((DBean) bobInstance);
        }
    }
    
    /**
     * Tests that adding a bean in fact adds an entry in the hub, and that
     * removing a bean in fact removes a bean from the hub
     */
    @Test @org.junit.Ignore
    public void testAddAndRemoveDBeanToHBean() {
        ServiceLocator testLocator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
        
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex2.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        Hub hub = testLocator.getService(Hub.class);
        BeanDatabase beanDatabase = hub.getCurrentDatabase();
        
        DBean holyoke = (DBean) beanDatabase.getInstance(DBEAN_TAG, DBEAN_HOLYOKE_INSTANCE_NAME);
        Assert.assertNull(holyoke);
        Assert.assertNull(testLocator.getService(DBean.class, HOLYOKE));
        
        HBean hbean = testLocator.getService(HBean.class);
        Assert.assertNotNull(hbean);
        
        hbean.createDBean(HOLYOKE);
        
        Assert.assertNotNull(testLocator.getService(DBean.class, HOLYOKE));
        
        beanDatabase = hub.getCurrentDatabase();
        
        // beanDatabase.dumpDatabase();
        
        holyoke = (DBean) beanDatabase.getInstance(DBEAN_TAG, DBEAN_HOLYOKE_INSTANCE_NAME);
        Assert.assertNotNull(holyoke);
        Assert.assertEquals(HOLYOKE, holyoke.getName());
    }

}
