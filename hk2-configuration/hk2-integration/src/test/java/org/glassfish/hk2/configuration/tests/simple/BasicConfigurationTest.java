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
package org.glassfish.hk2.configuration.tests.simple;

import java.beans.PropertyChangeEvent;
import java.util.List;

import javax.inject.Inject;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.configuration.api.ConfigurationUtilities;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * Tests the basic functionality of single-input configuration
 * 
 * @author jwells
 *
 */
public class BasicConfigurationTest extends HK2Runner {
    /* package */ final static String TEST_TYPE_ONE = "TestConfigurationType1";
    /* package */ final static String TEST_TYPE_TWO = "TestConfigurationType2";
    /* package */ final static String TEST_TYPE_THREE = "TestConfigurationType3";
    /* package */ final static String TEST_TYPE_FOUR = "TestConfigurationType4";
    
    private final static String DEFAULT = "default";
    private final static String FIELD1 = "field1";
    private final static String FIELD1_1 = "feild1_1";
    private final static String FIELD2 = "field2";
    private final static String CONSTRUCTOR = "constructor";
    private final static String METHOD1 = "method1";
    private final static String METHOD1_1 = "method1_1";
    private final static String METHOD2 = "method2";
    
    private final static String ALICE = "Alice";
    private final static String BOB = "Bob";
    private final static String CAROL = "Carol";
    
    @Inject
    private Hub hub;
    
    @Before
    public void before() {
        super.before();
        
        ConfigurationUtilities.enableConfigurationSystem(testLocator);
    }
    
    private ConfiguredServiceBean createBean() {
        return createBean(CONSTRUCTOR,
                FIELD1,
                FIELD2,
                METHOD1,
                METHOD2);
    }
    
    private ConfiguredServiceBean createBean(String constructorValue,
            String field1,
            String field2,
            String method1,
            String method2) {
        ConfiguredServiceBean csb = new ConfiguredServiceBean();
        
        csb.setConstructorOutput(constructorValue);
        csb.setFieldOutput1(field1);
        csb.setFieldOutput2(field2);
        csb.setMethodOutput1(method1);
        csb.setMethodOutput2(method2);
        
        return csb;
    }
    
    private void addBean(String typeName) {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType wt = wbd.findOrAddWriteableType(typeName);
        
        wt.addInstance(DEFAULT, createBean());
        
        wbd.commit();
    }
    
    private void updateBean(String typeName, ConfiguredServiceBean newBean) {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType wt = wbd.getWriteableType(typeName);
        Assert.assertNotNull(wt);
        
        wt.modifyInstance(DEFAULT, newBean);
        
        wbd.commit();
    }
    
    private void removeBean(String typeName) {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType wt = wbd.getWriteableType(typeName);
        if (wt == null) return;
        
        wt.removeInstance(DEFAULT);
        
        wbd.commit();
    }
    
    private void addNamedBean(String typeName, String name) {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType wt = wbd.findOrAddWriteableType(typeName);
        
        wt.addInstance(name, new NamedBean(name));
        
        wbd.commit();
    }
    
    private void removeNamedBean(String typeName, String name) {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        WriteableType wt = wbd.findOrAddWriteableType(typeName);
        
        wt.removeInstance(name);
        
        wbd.commit();
    }
    
    private void removeType(String typeName) {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
        wbd.removeType(typeName);
        
        wbd.commit();
    }
    
    /**
     * Tests a service that has basic configuration information
     * @throws InterruptedException 
     */
    @Test
    public void testBasicConfiguration() {
        addBean(TEST_TYPE_ONE);
        
        try {
            ActiveDescriptor<?> ad = testLocator.getBestDescriptor(BuilderHelper.createContractFilter(ConfiguredService.class.getName()));
            Assert.assertNotNull(ad);
        
            ConfiguredService cs = testLocator.getService(ConfiguredService.class);
            Assert.assertNotNull(cs);
        
            Assert.assertEquals(CONSTRUCTOR, cs.getConstructorOutput());
            Assert.assertEquals(METHOD1, cs.getMethodOutput1());
            Assert.assertEquals(METHOD2, cs.getMethodOutput2());
            Assert.assertEquals(FIELD1, cs.getFieldOutput1());
            Assert.assertEquals(FIELD2, cs.getFieldOutput2());
        }
        finally {
            removeBean(TEST_TYPE_ONE);
        }
    }
    
    /**
     * Tests a service that dynamically updates a couple fields
     * @throws InterruptedException 
     */
    @Test
    public void testBasicDynamicConfiguration() {
        addBean(TEST_TYPE_TWO);
        
        try {
            ActiveDescriptor<?> ad = testLocator.getBestDescriptor(BuilderHelper.createContractFilter(DynamicConfiguredService.class.getName()));
            Assert.assertNotNull(ad);
        
            DynamicConfiguredService cs = testLocator.getService(DynamicConfiguredService.class);
            Assert.assertNotNull(cs);
        
            Assert.assertEquals(METHOD1, cs.getMethodOutput1());
            Assert.assertEquals(FIELD1, cs.getFieldOutput1());
            
            ConfiguredServiceBean newBean = createBean(CONSTRUCTOR,
                FIELD1_1,
                FIELD2,
                METHOD1_1,
                METHOD2);
            
            updateBean(TEST_TYPE_TWO, newBean);
            
            Assert.assertEquals(METHOD1_1, cs.getMethodOutput1());
            Assert.assertEquals(FIELD1_1, cs.getFieldOutput1());
        }
        finally {
            removeBean(TEST_TYPE_TWO);
        }
    }
    
    /**
     * Tests a service that dynamically updates with a method
     * marked with {@link PreDynamicChange}
     * @throws InterruptedException 
     */
    @Test
    public void testDynamicConfigurationWithJustPreMethod() {
        addBean(TEST_TYPE_THREE);
        
        try {
            DynConJustPreMethodService service = testLocator.getService(DynConJustPreMethodService.class);
            Assert.assertNotNull(service);
        
            Assert.assertEquals(FIELD1, service.getFieldOutput1());
            Assert.assertNull(service.isPreChangeCalled());
            
            ConfiguredServiceBean newBean = createBean(CONSTRUCTOR,
                FIELD1_1,
                FIELD2,
                METHOD1,
                METHOD2);
            
            updateBean(TEST_TYPE_THREE, newBean);
            
            Assert.assertEquals(FIELD1_1, service.getFieldOutput1());
            Assert.assertEquals(FIELD1, service.isPreChangeCalled());
        }
        finally {
            removeBean(TEST_TYPE_THREE);
        }
    }
    
    /**
     * Tests a service that dynamically updates with a method
     * marked with {@link PostDynamicChange}
     * @throws InterruptedException 
     */
    @Test
    public void testDynamicConfigurationWithJustPostMethod() {
        addBean(TEST_TYPE_THREE);
        
        try {
            DynConJustPostMethodService service = testLocator.getService(DynConJustPostMethodService.class);
            Assert.assertNotNull(service);
        
            Assert.assertEquals(FIELD1, service.getFieldOutput1());
            Assert.assertNull(service.isPostChangeCalled());
            
            ConfiguredServiceBean newBean = createBean(CONSTRUCTOR,
                FIELD1_1,
                FIELD2,
                METHOD1,
                METHOD2);
            
            updateBean(TEST_TYPE_THREE, newBean);
            
            Assert.assertEquals(FIELD1_1, service.getFieldOutput1());
            Assert.assertEquals(FIELD1_1, service.isPostChangeCalled());
        }
        finally {
            removeBean(TEST_TYPE_THREE);
        }
    }
    
    /**
     * Tests a service that dynamically updates with a method
     * marked with {@link PostDynamicChange} and {@link PreDynamicChange}
     * which takes a list
     * @throws InterruptedException 
     */
    @Test
    public void testDynamicConfigurationWithPostAndPreMethods() {
        addBean(TEST_TYPE_THREE);
        
        try {
            DynConPrePostWListService service = testLocator.getService(DynConPrePostWListService.class);
            Assert.assertNotNull(service);
        
            Assert.assertEquals(FIELD1, service.getFieldOutput1());
            Assert.assertNull(service.isPostChangeCalled());
            Assert.assertNull(service.isPreChangeCalled());
            Assert.assertNull(service.getPostChangeList());
            Assert.assertNull(service.getPreChangeList());
            
            ConfiguredServiceBean newBean = createBean(CONSTRUCTOR,
                FIELD1_1,
                FIELD2,
                METHOD1,
                METHOD2);
            
            updateBean(TEST_TYPE_THREE, newBean);
            
            Assert.assertEquals(FIELD1_1, service.getFieldOutput1());
            Assert.assertEquals(FIELD1_1, service.isPostChangeCalled());
            Assert.assertEquals(FIELD1, service.isPreChangeCalled());
            
            {
                List<PropertyChangeEvent> preList = service.getPreChangeList();
                Assert.assertNotNull(preList);
                Assert.assertEquals(1, preList.size());
                
                PropertyChangeEvent pce = preList.get(0);
                Assert.assertNotNull(pce);
                
                Assert.assertEquals("fieldOutput1", pce.getPropertyName());
                Assert.assertEquals(FIELD1, pce.getOldValue());
                Assert.assertEquals(FIELD1_1, pce.getNewValue());
            }
            
            {
                List<PropertyChangeEvent> postList = service.getPostChangeList();
                Assert.assertNotNull(postList);
                Assert.assertEquals(1, postList.size());
                
                PropertyChangeEvent pce = postList.get(0);
                Assert.assertNotNull(pce);
                
                Assert.assertEquals("fieldOutput1", pce.getPropertyName());
                Assert.assertEquals(FIELD1, pce.getOldValue());
                Assert.assertEquals(FIELD1_1, pce.getNewValue());
            }
        }
        finally {
            removeBean(TEST_TYPE_THREE);
        }
    }
    
    /**
     * Tests a service that dynamically updates with a method
     * marked with {@link PreDynamicChange} that returns false
     * and so the fields should NOT get updated
     * 
     * @throws InterruptedException 
     */
    @Test
    public void testDynamicConfigurationPreMethodReturnsFalse() {
        addBean(TEST_TYPE_THREE);
        
        try {
            DynConPreFalseService service = testLocator.getService(DynConPreFalseService.class);
            Assert.assertNotNull(service);
        
            Assert.assertEquals(FIELD1, service.getFieldOutput1());
            Assert.assertNull(service.isPreChangeCalled());
            
            ConfiguredServiceBean newBean = createBean(CONSTRUCTOR,
                FIELD1_1,
                FIELD2,
                METHOD1,
                METHOD2);
            
            updateBean(TEST_TYPE_THREE, newBean);
            
            Assert.assertEquals(FIELD1, service.getFieldOutput1());  // Because the preMethod returns false
            Assert.assertEquals(FIELD1, service.isPreChangeCalled());
        }
        finally {
            removeBean(TEST_TYPE_THREE);
        }
    }
    
    /**
     * Tests a service that dynamically updates with a method
     * marked with {@link PostDynamicChange} and {@link PreDynamicChange}
     * and that is also a {@link PropertyChangeListener}
     * @throws InterruptedException 
     */
    @Test
    public void testDynamicConfigurationWithPostPreAndListener() {
        addBean(TEST_TYPE_THREE);
        
        try {
            DynConPreTrueWListenerService service = testLocator.getService(DynConPreTrueWListenerService.class);
            Assert.assertNotNull(service);
        
            Assert.assertEquals(FIELD1, service.getFieldOutput1());
            Assert.assertNull(service.isPostChangeCalled());
            Assert.assertNull(service.isPreChangeCalled());
            Assert.assertNull(service.getLastPropChangeEvent());
            Assert.assertEquals(0, service.getNumPropertyChanges());
            
            ConfiguredServiceBean newBean = createBean(CONSTRUCTOR,
                FIELD1_1,
                FIELD2,
                METHOD1,
                METHOD2);
            
            updateBean(TEST_TYPE_THREE, newBean);
            
            Assert.assertEquals(FIELD1_1, service.getFieldOutput1());
            Assert.assertEquals(FIELD1_1, service.isPostChangeCalled());
            Assert.assertEquals(FIELD1, service.isPreChangeCalled());
            Assert.assertEquals(1, service.getNumPropertyChanges());
            
            {
                PropertyChangeEvent pce = service.getLastPropChangeEvent();
                Assert.assertNotNull(pce);
                
                Assert.assertEquals("fieldOutput1", pce.getPropertyName());
                Assert.assertEquals(FIELD1, pce.getOldValue());
                Assert.assertEquals(FIELD1_1, pce.getNewValue());
            }
        }
        finally {
            removeBean(TEST_TYPE_THREE);
        }
    }
    
    /**
     * Tests that we can remove instances of services but the other
     * services are still there
     */
    public void testRemovalOfInstances() {
        addNamedBean(TEST_TYPE_FOUR, ALICE);
        addNamedBean(TEST_TYPE_FOUR, BOB);
        addNamedBean(TEST_TYPE_FOUR, CAROL);
        
        MultiService alice = null;
        MultiService carol = null;
        try {
            alice = testLocator.getService(MultiService.class, ALICE);
            MultiService bob = testLocator.getService(MultiService.class, BOB);
            carol = testLocator.getService(MultiService.class, CAROL);
            
            Assert.assertNotNull(alice);
            Assert.assertNotNull(bob);
            Assert.assertNotNull(carol);
            
            Assert.assertEquals(ALICE, alice.getName());
            Assert.assertEquals(BOB, bob.getName());
            Assert.assertEquals(CAROL, carol.getName());
            
            Assert.assertFalse(alice.isDestroyed());
            Assert.assertFalse(bob.isDestroyed());
            Assert.assertFalse(carol.isDestroyed());
            
            removeNamedBean(TEST_TYPE_FOUR, BOB);
            
            alice = testLocator.getService(MultiService.class, ALICE);
            MultiService noBob = testLocator.getService(MultiService.class, BOB);
            carol = testLocator.getService(MultiService.class, CAROL);
            
            Assert.assertNotNull(alice);
            Assert.assertNull(noBob);
            Assert.assertNotNull(carol);
            
            Assert.assertEquals(ALICE, alice.getName());
            Assert.assertEquals(CAROL, carol.getName());
            
            Assert.assertFalse(alice.isDestroyed());
            Assert.assertTrue(bob.isDestroyed());
            Assert.assertFalse(carol.isDestroyed());
        }
        finally {
            removeType(TEST_TYPE_FOUR);
        }
        
        Assert.assertTrue(alice.isDestroyed());
        Assert.assertTrue(carol.isDestroyed());
        
    }
}
