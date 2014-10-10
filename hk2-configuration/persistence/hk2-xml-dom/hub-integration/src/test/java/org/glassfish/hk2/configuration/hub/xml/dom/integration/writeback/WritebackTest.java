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
    
    // private final static String JBEAN_TAG = "/j-bean";
    
    private final static String KBEAN_TAG = "/j-bean/k-bean";
    private final static String KBEAN_INSTANCE_NAME = "j-bean.k-bean";
    
    private final static String HELLO = "hello";
    private final static String SAILOR = "sailor";
    
    private final static String BBEAN_PARAMETER_NAME = "parameter";
    private final static String NAME_PARAMETER_NAME = "name";
    
    private final static String BOB = "bob";
    private final static String CAROL = "carol";
    
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
        
        Map<String, Object> jbeanMap = new HashMap<String, Object>();
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType beanWriteableType = wbd.findOrAddWriteableType(KBEAN_TAG);
        beanWriteableType.addInstance(KBEAN_INSTANCE_NAME, jbeanMap);
        
        wbd.commit();
        
        kbean = testLocator.getService(KBean.class);
        Assert.assertNotNull(kbean);
        
        Assert.assertNotNull(jbean.getKBean());
    }

}
