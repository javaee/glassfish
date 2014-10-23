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
package org.glassfish.hk2.configuration.hub.xml.dom.integration.writeback;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.XmlDomIntegrationUtilities;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.tests.BBean;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.tests.CBean;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.tests.common.ConfigHubIntegrationUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigParser;

/**
 * Tests writing back to the beans from a translated map copy
 * 
 * @author jwells
 */
public class WritebackTest {
    private final static String BBEAN_TAG = "/b-bean";
    private final static String BBEAN_INSTANCE_NAME = "b-bean";
    
    private final static String CBEAN_TAG = "/b-bean/c-bean";
    private final static String CAROL_INSTANCE_NAME = "b-bean.carol";
    private final static String BOB_INSTANCE_NAME = "b-bean.bob";
    
    private final static String JBEAN_TAG = "/j-bean";
    
    private final static String KBEAN_TAG = "/j-bean/k-bean";
    private final static String KBEAN_INSTANCE_NAME = "j-bean.k-bean";
    
    private final static String MBEAN_TAG = "/j-bean/k-bean/not-derivable-tag";
    private final static String DAVE_INSTANCE_NAME = "j-bean.k-bean.dave";
    private final static String EATON_INSTANCE_NAME = "j-bean.k-bean.eaton";
    private final static String GIANNA_INSTANCE_NAME = "j-bean.k-bean.gianna";
    
    private final static String LBEAN_TAG = "/j-bean/l-beans";
    private final static String FRANK_INSTANCE_NAME = "j-bean.frank";
    private final static String HELEN_INSTANCE_NAME = "j-bean.helen";
    
    private final static String NBEAN_TAG = "/j-bean/n-bean";
    private final static String IAGO_INSTANCE_NAME = "j-bean.iago";
    
    // private final static String OBEAN_TAG = "/o-bean";
    private final static String O_NBEAN_TAG = "/o-bean/n-bean";
    private final static String O_MBEAN_TAG = "/o-bean/m-bean";
    private final static String JOHN_INSTANCE_NAME = "o-bean.john";
    private final static String KAREN_INSTANCE_NAME = "o-bean.karen";
    
    private final static String HELLO = "hello";
    private final static String SAILOR = "sailor";
    
    private final static String BBEAN_PARAMETER_NAME = "parameter";
    private final static String NAME_PARAMETER_NAME = "name";
    private final static String CREATED_ON_PARAMETER_NAME = "createdOn";
    private final static String UPDATED_ON_PARAMETER_NAME = "updatedOn";
    private final static String SOME_NUMBER_PARAMETER_NAME = "someNumber";
    private final static String ANOTHER_NUMBER_PARAMETER_NAME = "someOtherNumber";
    
    private final static String BOB = "bob";
    private final static String CAROL = "carol";
    private final static String DAVE = "dave";
    private final static String EATON = "eaton";
    private final static String FRANK = "frank";
    private final static String GIANNA = "gianna";
    private final static String HELEN = "helen";
    private final static String IAGO = "iago";
    private final static String JOHN = "john";
    private final static String KAREN = "karen";
    
    private final static String AGE = "age";
    private final static String EPOCH = "epoch";
    
    private final static String PROP_A_KEY = "A";
    private final static String PROP_A_VALUE1 = "VA1";
    private final static String PROP_A_VALUE2 = "VA2";
    private final static String PROP_B_KEY = "B";
    private final static String PROP_B_VALUE = "VB";
    private final static String PROP_C_KEY = "C";
    private final static String PROP_C_VALUE = "VC";
    
    
    @SuppressWarnings("unchecked")
    @Test // @org.junit.Ignore
    public void testWritebackAnAttribute() {
        ServiceLocator testLocator = ConfigHubIntegrationUtilities.createPopulateAndConfigInit();
        XmlDomIntegrationUtilities.enableMapTranslator(testLocator);
        
        Hub hub = testLocator.getService(Hub.class);
        Assert.assertNotNull(hub);
        
        Assert.assertNull(hub.getCurrentDatabase().getType(BBEAN_TAG));
        
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex1.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        Type bbeanType = hub.getCurrentDatabase().getType(BBEAN_TAG);
        Instance bbeanInstance = bbeanType.getInstance(BBEAN_INSTANCE_NAME);
        Map<String, Object> bbeanMap = (Map<String, Object>) bbeanInstance.getBean();
        
        BBean bbean = testLocator.getService(BBean.class);
        
        // Both should now be HELLO
        Assert.assertEquals(HELLO, bbean.getParameter());
        Assert.assertEquals(HELLO, bbeanMap.get(BBEAN_PARAMETER_NAME));
        
        // Modify the map
        HashMap<String, Object> newBean = new HashMap<String, Object>(bbeanMap);
        newBean.put(BBEAN_PARAMETER_NAME, SAILOR);
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType wt = wbd.getWriteableType(BBEAN_TAG);
        wt.modifyInstance(BBEAN_INSTANCE_NAME, newBean);
        
        wbd.commit();
        
        // This is the test.  Check that the parameter got set on BBean
        Assert.assertEquals(SAILOR, bbean.getParameter());
    }
    
    /**
     * Tests we can add an instance and have it reflected back in the hk2-config beans
     */
    @Test // @org.junit.Ignore
    public void testWritebackANewChildBean() {
        ServiceLocator testLocator = ConfigHubIntegrationUtilities.createPopulateAndConfigInit();
        XmlDomIntegrationUtilities.enableMapTranslator(testLocator);
        
        Hub hub = testLocator.getService(Hub.class);
        Assert.assertNotNull(hub);
        
        Assert.assertNull(hub.getCurrentDatabase().getType(CBEAN_TAG));
        
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex1.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        // Add a Carol CBean
        HashMap<String, Object> carolBean = new HashMap<String, Object>();
        carolBean.put(NAME_PARAMETER_NAME, CAROL);
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType beanWriteableType = wbd.findOrAddWriteableType(CBEAN_TAG);
        beanWriteableType.addInstance(CAROL_INSTANCE_NAME, carolBean);
        
        wbd.commit();
        
        CBean carolService = testLocator.getService(CBean.class, CAROL);
        Assert.assertNotNull(carolService);
        
        Assert.assertEquals(CAROL, carolService.getName());
    }
    
    /**
     * Tests we can remove an instance and have it reflected back in the hk2-config beans
     */
    @Test // @org.junit.Ignore
    public void testWritebackAndRemoveAChildBean() {
        ServiceLocator testLocator = ConfigHubIntegrationUtilities.createPopulateAndConfigInit();
        XmlDomIntegrationUtilities.enableMapTranslator(testLocator);
        
        Hub hub = testLocator.getService(Hub.class);
        Assert.assertNotNull(hub);
        
        Assert.assertNull(hub.getCurrentDatabase().getType(CBEAN_TAG));
        
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex1.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        // Remove a Alice CBean
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType beanWriteableType = wbd.findOrAddWriteableType(CBEAN_TAG);
        beanWriteableType.removeInstance(BOB_INSTANCE_NAME);
        
        wbd.commit();
        
        CBean bobService = testLocator.getService(CBean.class, BOB);
        Assert.assertNull(bobService);
    }
    
    /**
     * Tests we can add a single complex child bean
     */
    @Test // @org.junit.Ignore
    public void testWritebackSingleComplexChildBean() {
        ServiceLocator testLocator = ConfigHubIntegrationUtilities.createPopulateAndConfigInit();
        XmlDomIntegrationUtilities.enableMapTranslator(testLocator);
        
        Hub hub = testLocator.getService(Hub.class);
        Assert.assertNotNull(hub);
        
        Assert.assertNull(hub.getCurrentDatabase().getType(KBEAN_TAG));
        
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex3.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        JBean jbean = testLocator.getService(JBean.class);
        Assert.assertNotNull(jbean);
        
        KBean kbean = testLocator.getService(KBean.class);
        Assert.assertNull(kbean);
        
        Map<String, Object> kbeanMap = new HashMap<String, Object>();
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType beanWriteableType = wbd.findOrAddWriteableType(KBEAN_TAG);
        beanWriteableType.addInstance(KBEAN_INSTANCE_NAME, kbeanMap);
        
        wbd.commit();
        
        kbean = testLocator.getService(KBean.class);
        Assert.assertNotNull(kbean);
        
        Assert.assertNotNull(jbean.getKBean());
    }
    
    /**
     * Tests we can remove a single complex child bean
     */
    @Test // @org.junit.Ignore
    public void testWritebackRemoveSingleComplexChildBean() {
        ServiceLocator testLocator = ConfigHubIntegrationUtilities.createPopulateAndConfigInit();
        XmlDomIntegrationUtilities.enableMapTranslator(testLocator);
        
        Hub hub = testLocator.getService(Hub.class);
        Assert.assertNotNull(hub);
        
        Assert.assertNull(hub.getCurrentDatabase().getType(KBEAN_TAG));
        
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex4.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        JBean jbean = testLocator.getService(JBean.class);
        Assert.assertNotNull(jbean);
        
        KBean kbean = testLocator.getService(KBean.class);
        Assert.assertNotNull(kbean);
        
        Assert.assertEquals(12, kbean.getEpoch());
        Assert.assertEquals(34, kbean.getAge());
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType beanWriteableType = wbd.findOrAddWriteableType(KBEAN_TAG);
        beanWriteableType.removeInstance(KBEAN_INSTANCE_NAME);
        
        wbd.commit();
        
        kbean = testLocator.getService(KBean.class);
        Assert.assertNull(kbean);
        
        Assert.assertNull(jbean.getKBean());
    }
    
    /**
     * Adds children with grand-children and non-related beans
     * as well all in on nasty database transaction
     */
    @Test // @org.junit.Ignore
    public void testMultipleBeansAddedRelatedAndNotRelated() {
        ServiceLocator testLocator = ConfigHubIntegrationUtilities.createPopulateAndConfigInit();
        XmlDomIntegrationUtilities.enableMapTranslator(testLocator);
        
        Hub hub = testLocator.getService(Hub.class);
        Assert.assertNotNull(hub);
        
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex3.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        JBean jbean = testLocator.getService(JBean.class);
        Assert.assertNotNull(jbean);
        
        Map<String, Object> kbeanMap = new HashMap<String, Object>();
        
        Map<String, Object> mbeanDaveMap = new HashMap<String, Object>();
        mbeanDaveMap.put(NAME_PARAMETER_NAME, DAVE);
        
        Map<String, Object> mbeanEatonMap = new HashMap<String, Object>();
        mbeanEatonMap.put(NAME_PARAMETER_NAME, EATON);
        
        Map<String, Object> lbeanFrankMap = new HashMap<String, Object>();
        lbeanFrankMap.put(NAME_PARAMETER_NAME, FRANK);
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType mbeanWriteableType = wbd.findOrAddWriteableType(MBEAN_TAG);
        mbeanWriteableType.addInstance(DAVE_INSTANCE_NAME, mbeanDaveMap);
        mbeanWriteableType.addInstance(EATON_INSTANCE_NAME, mbeanEatonMap);
        
        WriteableType kbeanWriteableType = wbd.findOrAddWriteableType(KBEAN_TAG);
        kbeanWriteableType.addInstance(KBEAN_INSTANCE_NAME, kbeanMap);
        
        WriteableType lbeanWriteableType = wbd.findOrAddWriteableType(LBEAN_TAG);
        lbeanWriteableType.addInstance(FRANK_INSTANCE_NAME, lbeanFrankMap);
        
        wbd.commit();
        
        KBean kbean = testLocator.getService(KBean.class);
        Assert.assertNotNull(kbean);
        
        MBean dave = testLocator.getService(MBean.class, DAVE);
        Assert.assertNotNull(dave);
        Assert.assertEquals(DAVE, dave.getName());
        
        MBean eaton = testLocator.getService(MBean.class, EATON);
        Assert.assertNotNull(eaton);
        Assert.assertEquals(EATON, eaton.getName());
        
        LBean frank = testLocator.getService(LBean.class, FRANK);
        Assert.assertNotNull(frank);
        Assert.assertEquals(FRANK, frank.getName());
    }
    
    /**
     * Removes children with grand-children and non-related beans
     * as well all in on nasty database transaction
     */
    @Test // @org.junit.Ignore
    public void testMultipleBeansRemoveRelatedAndNotRelated() {
        ServiceLocator testLocator = ConfigHubIntegrationUtilities.createPopulateAndConfigInit();
        XmlDomIntegrationUtilities.enableMapTranslator(testLocator);
        
        Hub hub = testLocator.getService(Hub.class);
        Assert.assertNotNull(hub);
        
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex5.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        JBean jbean = testLocator.getService(JBean.class);
        Assert.assertNotNull(jbean);
        
        KBean kbean = testLocator.getService(KBean.class);
        Assert.assertNotNull(kbean);
        
        MBean dave = testLocator.getService(MBean.class, DAVE);
        Assert.assertNotNull(dave);
        Assert.assertEquals(DAVE, dave.getName());
        
        MBean eaton = testLocator.getService(MBean.class, EATON);
        Assert.assertNotNull(eaton);
        Assert.assertEquals(EATON, eaton.getName());
        
        LBean frank = testLocator.getService(LBean.class, FRANK);
        Assert.assertNotNull(frank);
        Assert.assertEquals(FRANK, frank.getName());
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        wbd.removeType(KBEAN_TAG);
        wbd.removeType(MBEAN_TAG);
        wbd.removeType(JBEAN_TAG);
        wbd.removeType(LBEAN_TAG);
        
        wbd.commit();
        
        kbean = testLocator.getService(KBean.class);
        Assert.assertNull(kbean);
        
        dave = testLocator.getService(MBean.class, DAVE);
        Assert.assertNull(dave);
        
        eaton = testLocator.getService(MBean.class, EATON);
        Assert.assertNull(eaton);
        
        frank = testLocator.getService(LBean.class, FRANK);
        Assert.assertNull(frank);
        
        Assert.assertNull(testLocator.getService(JBean.class));
    }
    
    /**
     * Modifies, adds children and removes children from Kbean (and
     * does an add of l-bean just for grins)
     */
    @SuppressWarnings("unchecked")
    @Test // @org.junit.Ignore
    public void testMultipleBeansAddRemoveAndModify() {
        ServiceLocator testLocator = ConfigHubIntegrationUtilities.createPopulateAndConfigInit();
        XmlDomIntegrationUtilities.enableMapTranslator(testLocator);
        
        Hub hub = testLocator.getService(Hub.class);
        Assert.assertNotNull(hub);
        
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex5.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        JBean jbean = testLocator.getService(JBean.class);
        Assert.assertNotNull(jbean);
        
        KBean kbean = testLocator.getService(KBean.class);
        Assert.assertNotNull(kbean);
        Assert.assertEquals(12, kbean.getEpoch());
        Assert.assertEquals(34, kbean.getAge());
        
        MBean dave = testLocator.getService(MBean.class, DAVE);
        Assert.assertNotNull(dave);
        Assert.assertEquals(DAVE, dave.getName());
        
        MBean eaton = testLocator.getService(MBean.class, EATON);
        Assert.assertNotNull(eaton);
        Assert.assertEquals(EATON, eaton.getName());
        
        LBean frank = testLocator.getService(LBean.class, FRANK);
        Assert.assertNotNull(frank);
        Assert.assertEquals(FRANK, frank.getName());
        
        Map<String, Object> kbeanMap = new HashMap<String, Object>();
        Map<String, Object> existingKBeanMap = (Map<String, Object>) hub.
                getCurrentDatabase().getInstance(KBEAN_TAG, KBEAN_INSTANCE_NAME).getBean();
        kbeanMap.putAll(existingKBeanMap);
        kbeanMap.put(AGE, new Integer(33));
        kbeanMap.put(EPOCH, new Integer(13));
        
        Map<String, Object> mbeanGiannaMap = new HashMap<String, Object>();
        mbeanGiannaMap.put(NAME_PARAMETER_NAME, GIANNA);
        
        Map<String, Object> lbeanHelenMap = new HashMap<String, Object>();
        lbeanHelenMap.put(NAME_PARAMETER_NAME, HELEN);
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType mbeanWriteableType = wbd.findOrAddWriteableType(MBEAN_TAG);
        mbeanWriteableType.removeInstance(DAVE_INSTANCE_NAME);
        mbeanWriteableType.addInstance(GIANNA_INSTANCE_NAME, mbeanGiannaMap);
        
        WriteableType kbeanWriteableType = wbd.findOrAddWriteableType(KBEAN_TAG);
        kbeanWriteableType.modifyInstance(KBEAN_INSTANCE_NAME, kbeanMap);
        
        WriteableType lbeanWriteableType = wbd.findOrAddWriteableType(LBEAN_TAG);
        lbeanWriteableType.addInstance(HELEN_INSTANCE_NAME, lbeanHelenMap);
        
        wbd.commit();
        
        kbean = testLocator.getService(KBean.class);
        Assert.assertNotNull(kbean);
        Assert.assertEquals(13, kbean.getEpoch());
        Assert.assertEquals(33, kbean.getAge());
        
        dave = testLocator.getService(MBean.class, DAVE);
        Assert.assertNull(dave);
        
        eaton = testLocator.getService(MBean.class, EATON);
        Assert.assertNotNull(eaton);
        Assert.assertEquals(EATON, eaton.getName());
        
        MBean gianna = testLocator.getService(MBean.class, GIANNA);
        Assert.assertNotNull(gianna);
        Assert.assertEquals(GIANNA, gianna.getName());
        
        frank = testLocator.getService(LBean.class, FRANK);
        Assert.assertNotNull(frank);
        Assert.assertEquals(FRANK, frank.getName());
        
        LBean helen = testLocator.getService(LBean.class, HELEN);
        Assert.assertNotNull(helen);
        Assert.assertEquals(HELEN, helen.getName());
    }
    
    /**
     * Tests adding beans that have some properties that are not Strings
     */
    @Test // @org.junit.Ignore
    public void testBeansAddedWithMoreComplexProperties() {
        ServiceLocator testLocator = ConfigHubIntegrationUtilities.createPopulateAndConfigInit();
        XmlDomIntegrationUtilities.enableMapTranslator(testLocator);
        
        Hub hub = testLocator.getService(Hub.class);
        Assert.assertNotNull(hub);
        
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex3.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        JBean jbean = testLocator.getService(JBean.class);
        Assert.assertNotNull(jbean);
        
        Map<String, Object> nbeanMap = new HashMap<String, Object>();
        
        nbeanMap.put(NAME_PARAMETER_NAME, IAGO);
        nbeanMap.put(CREATED_ON_PARAMETER_NAME, "1000");
        nbeanMap.put(UPDATED_ON_PARAMETER_NAME, "1000");
        nbeanMap.put(SOME_NUMBER_PARAMETER_NAME, new Long(1000));
        nbeanMap.put(ANOTHER_NUMBER_PARAMETER_NAME, new Integer(1000));
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType nbeanWriteableType = wbd.findOrAddWriteableType(NBEAN_TAG);
        nbeanWriteableType.addInstance(IAGO_INSTANCE_NAME, nbeanMap);
        
        wbd.commit();
        
        NBean nbean = testLocator.getService(NBean.class, IAGO);
        Assert.assertNotNull(nbean);
        
        Assert.assertEquals("1000", nbean.getCreatedOn());
        Assert.assertEquals("1000", nbean.getUpdatedOn());
        Assert.assertEquals(1000, nbean.getSomeNumber());
        Assert.assertEquals(1000, nbean.getSomeOtherNumber());
        Assert.assertEquals(IAGO, nbean.getName());
    }
    
    /**
     * Tests adding beans with multiple * elements
     * 
     * This test is ignored because the base hk2-config does
     * not seem to properly support two @Element("*") fields.
     * See OBean for an example
     */
    @Test @org.junit.Ignore
    public void testBeanWithMultipleStarElements() {
        ServiceLocator testLocator = ConfigHubIntegrationUtilities.createPopulateAndConfigInit();
        XmlDomIntegrationUtilities.enableMapTranslator(testLocator);
        
        Hub hub = testLocator.getService(Hub.class);
        Assert.assertNotNull(hub);
        
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex6.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        OBean obean = testLocator.getService(OBean.class);
        Assert.assertNotNull(obean);
        
        Map<String, Object> nbeanMap = new HashMap<String, Object>();
        
        nbeanMap.put(NAME_PARAMETER_NAME, JOHN);
        
        Map<String, Object> mbeanMap = new HashMap<String, Object>();
        
        mbeanMap.put(NAME_PARAMETER_NAME, KAREN);
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType nbeanWriteableType = wbd.findOrAddWriteableType(O_NBEAN_TAG);
        nbeanWriteableType.addInstance(JOHN_INSTANCE_NAME, nbeanMap);
        
        WriteableType mbeanWriteableType = wbd.findOrAddWriteableType(O_MBEAN_TAG);
        mbeanWriteableType.addInstance(KAREN_INSTANCE_NAME, mbeanMap);
        
        wbd.commit();
        
        NBean nbean = testLocator.getService(NBean.class, JOHN);
        Assert.assertNotNull(nbean);
        
        MBean mbean = testLocator.getService(MBean.class, KAREN);
        Assert.assertNotNull(mbean);
    }
    
    /**
     * Tests that we can add properties to a bean that is a PropertyBag, and
     * that we can also modify that bean by modifying the Hub
     */
    @Test // @org.junit.Ignore
    public void testPropertyBagAddAndModify() {
        ServiceLocator testLocator = ConfigHubIntegrationUtilities.createPopulateAndConfigInit();
        XmlDomIntegrationUtilities.enableMapTranslator(testLocator);
        
        Hub hub = testLocator.getService(Hub.class);
        Assert.assertNotNull(hub);
        
        ConfigParser parser = new ConfigParser(testLocator);
        URL url = getClass().getClassLoader().getResource("complex3.xml");
        Assert.assertNotNull(url);
        
        parser.parse(url);
        
        JBean jbean = testLocator.getService(JBean.class);
        Assert.assertNotNull(jbean);
        
        {
            Map<String, Object> nbeanMap = new HashMap<String, Object>();
        
            Properties newProps = new Properties();
            newProps.put(PROP_A_KEY, PROP_A_VALUE1);
            newProps.put(PROP_B_KEY, PROP_B_VALUE);
        
            nbeanMap.put(NAME_PARAMETER_NAME, IAGO);
            nbeanMap.put(XmlDomIntegrationUtilities.PROPERTIES, newProps);
        
            WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
            WriteableType nbeanWriteableType = wbd.findOrAddWriteableType(NBEAN_TAG);
            nbeanWriteableType.addInstance(IAGO_INSTANCE_NAME, nbeanMap);
        
            wbd.commit();
        }
        
        NBean nbean = testLocator.getService(NBean.class, IAGO);
        Assert.assertNotNull(nbean);
        
        Assert.assertEquals(PROP_A_VALUE1, nbean.getPropertyValue(PROP_A_KEY));
        Assert.assertEquals(PROP_B_VALUE, nbean.getPropertyValue(PROP_B_KEY));
        Assert.assertNull(nbean.getPropertyValue(PROP_C_KEY));
        
        {
            // Now modify the properties just added!
            HashMap<String, Object> nbeanMap = new HashMap<String, Object>();
        
            Properties modProps = new Properties();
            modProps.put(PROP_A_KEY, PROP_A_VALUE2);
            // PropertyB is being removed
            modProps.put(PROP_C_KEY, PROP_C_VALUE);
        
            nbeanMap.put(NAME_PARAMETER_NAME, IAGO);
            nbeanMap.put(XmlDomIntegrationUtilities.PROPERTIES, modProps);
        
            WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
            WriteableType nbeanWriteableType = wbd.getWriteableType(NBEAN_TAG);
            nbeanWriteableType.modifyInstance(IAGO_INSTANCE_NAME, nbeanMap);
        
            wbd.commit();
        }
        
        Assert.assertEquals(PROP_A_VALUE2, nbean.getPropertyValue(PROP_A_KEY));
        Assert.assertNull(nbean.getPropertyValue(PROP_B_KEY));
        Assert.assertEquals(PROP_C_VALUE, nbean.getPropertyValue(PROP_C_KEY));
    }

}
