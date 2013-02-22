/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.locator.dynamicconfig;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.FactoryDescriptors;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ValidationService;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class DynamicConfigTest {
    private final static String TEST_NAME = "DynamicConfigTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, null);
    
    /** The name of the named service */
    public final static String SERVICE_NAME = "Fred";
    
    public final static String METADATA_KEY1 = "key1";
    public final static String METADATA_KEY2 = "key2";
    
    /**
     * Tests that things can be dynamically added to the system
     */
    @Test
    public void testDynamicallyAddService() {
        LateService ls = locator.getService(LateService.class);
        Assert.assertNull(ls);
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        DynamicConfiguration dc = dcs.createDynamicConfiguration();
        Assert.assertNotNull(dc);
        
        dc.bind(BuilderHelper.link(LateService.class).build());
        
        // Not committed yet, should still not be there
        ls = locator.getService(LateService.class);
        Assert.assertNull(ls);
        
        dc.commit();
        
        ls = locator.getService(LateService.class);
        Assert.assertNotNull(ls);
    }
    
    /**
     * Tests that things can be dynamically added to the system
     */
    @Test
    public void testUnbindAService() {
        SimpleService ss = locator.getService(SimpleService.class);
        Assert.assertNull(ss);
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration dc = dcs.createDynamicConfiguration();
        
        dc.bind(BuilderHelper.link(SimpleService.class).build());
        
        dc.commit();
        
        ss = locator.getService(SimpleService.class);
        Assert.assertNotNull(ss);
        
        dc = dcs.createDynamicConfiguration();
        
        dc.addUnbindFilter(BuilderHelper.createContractFilter(SimpleService.class.getName()));
        
        // Should still be there prior to update
        ss = locator.getService(SimpleService.class);
        Assert.assertNotNull(ss);
        
        dc.commit();
        
        ss = locator.getService(SimpleService.class);
        Assert.assertNull(ss);
    }
    
    private final static String KEY = "ID";
    private final static String ONE = "One";
    private final static String TWO = "Two";
    
    private static void checkDescriptor(ActiveDescriptor<?> checkMe, String expectedValue) {
        Map<String, List<String>> metadata = checkMe.getMetadata();
        
        List<String> values = metadata.get(KEY);
        Assert.assertNotNull(values);
        
        Assert.assertTrue("Expected " + expectedValue + " in the metadata, but did not get it", values.contains(expectedValue));
        
    }
    
    /**
     * Tests that things can be dynamically added to the system
     */
    @Test
    public void testRebindAService() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration dc = dcs.createDynamicConfiguration();
        
        dc.bind(BuilderHelper.link(SimpleService2.class).has(KEY, ONE).build());
        
        dc.commit();
        
        Filter filter = BuilderHelper.createContractFilter(SimpleService2.class.getName());
        
        List<ActiveDescriptor<?>> ss2Descriptors = locator.getDescriptors(filter);
        Assert.assertEquals(1, ss2Descriptors.size());
        
        for (ActiveDescriptor<?> ss2Descriptor : ss2Descriptors) {
            checkDescriptor(ss2Descriptor, ONE);
        }
        
        dc = dcs.createDynamicConfiguration();
        
        dc.addUnbindFilter(filter);
        dc.bind(BuilderHelper.link(SimpleService2.class).has(KEY, TWO).build());
        
        // Does both a bind and an unbind in the same commit
        dc.commit();
        
        ss2Descriptors = locator.getDescriptors(filter);
        Assert.assertEquals(1, ss2Descriptors.size());
        
        for (ActiveDescriptor<?> ss2Descriptor : ss2Descriptors) {
            checkDescriptor(ss2Descriptor, TWO);
        }
    }
    
    /**
     * Tests that things can be dynamically added to the system
     */
    @Test
    public void testUnbindANamedService() {
        NamedService ns = locator.getService(NamedService.class);
        Assert.assertNull(ns);
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration dc = dcs.createDynamicConfiguration();
        
        dc.bind(BuilderHelper.link(NamedService.class).named(SERVICE_NAME).build());
        
        dc.commit();
        
        ns = locator.getService(NamedService.class, SERVICE_NAME);
        Assert.assertNotNull(ns);
        
        dc = dcs.createDynamicConfiguration();
        
        dc.addUnbindFilter(BuilderHelper.createNameFilter(SERVICE_NAME));
        
        // Should still be there prior to update
        ns = locator.getService(NamedService.class, SERVICE_NAME);
        Assert.assertNotNull(ns);
        
        dc.commit();
        
        ns = locator.getService(NamedService.class, SERVICE_NAME);
        Assert.assertNull(ns);
    }
    
    /**
     * Tests that things can be dynamically added to the system
     */
    @Test
    public void testUnbindAValidatorService() {
        ValidationServiceImpl vsi = locator.getService(ValidationServiceImpl.class);
        Assert.assertNull(vsi);
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration dc = dcs.createDynamicConfiguration();
        
        dc.bind(BuilderHelper.link(ValidationServiceImpl.class).
                to(ValidationService.class).
                in(Singleton.class.getName()).build());
        
        dc.commit();
        
        vsi = locator.getService(ValidationServiceImpl.class);
        Assert.assertNotNull(vsi);
        
        dc = dcs.createDynamicConfiguration();
        
        dc.addUnbindFilter(BuilderHelper.createContractFilter(ValidationServiceImpl.class.getName()));
        
        // Should still be there prior to update
        vsi = locator.getService(ValidationServiceImpl.class);
        Assert.assertNotNull(vsi);
        
        dc.commit();
        
        vsi = locator.getService(ValidationServiceImpl.class);
        Assert.assertNull(vsi);
    }
    
    /**
     * Tests that things can be dynamically added to the system
     */
    @Test
    public void testCheckFactoryBindResults() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration dc = dcs.createDynamicConfiguration();
        
        FactoryDescriptors result = dc.bind(BuilderHelper.link(SimpleService3Factory.class).
                to(SimpleService3.class).
                in(Singleton.class.getName()).
                buildFactory(Singleton.class.getName()));
        Assert.assertNotNull(result);
        
        Descriptor asFactory = result.getFactoryAsAFactory();
        Descriptor asService = result.getFactoryAsAService();
        
        Assert.assertNotNull(asFactory);
        Assert.assertNotNull(asService);
        
        Assert.assertNotNull(asFactory.getServiceId());
        Assert.assertNotNull(asService.getServiceId());
        
        Assert.assertNotSame(asService.getServiceId(), asFactory.getServiceId());
        
        Assert.assertEquals(locator.getLocatorId(), asFactory.getLocatorId().longValue());
        Assert.assertEquals(locator.getLocatorId(), asService.getLocatorId().longValue());
        
        dc.commit();
        
        SimpleService3 ss3 = locator.getService(SimpleService3.class);
        Assert.assertNotNull(ss3);
    }
    
    /**
     * helps code coverage by invoking the toString
     */
    @Test
    public void testConfigToString() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        Assert.assertTrue(config.toString(), config.toString().contains("DynamicConfiguration"));
    }
    
    /**
     * Tests that a complex hierarchy of classes and interfaces gets all of the proper
     * types and contracts
     */
    @Test
    public void testProperSetOfTypes() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        ActiveDescriptor<ComplexObject> d = config.addActiveDescriptor(ComplexObject.class);
        
        {
            Set<String> contracts = d.getAdvertisedContracts();
        
            Assert.assertTrue(contracts.contains(ComplexObject.class.getName()));
            Assert.assertTrue(contracts.contains(IsAClassContract.class.getName()));
            Assert.assertTrue(contracts.contains(IsAContract.class.getName()));
            Assert.assertTrue(contracts.contains(ParameterizedObject.class.getName()));
            Assert.assertTrue(contracts.contains(ParameterizedInterface.class.getName()));
        
            Assert.assertEquals(5, contracts.size());
        }
        
        {
            Set<Type> contracts = d.getContractTypes();
            
            // This also tests (kind of) that the returned set is ordered
            // The ordering is classes first, interfaces second (in the order declared)
            int lcv = 0;
            for (Type contract : contracts) {
                switch (lcv) {
                case 0:
                    Assert.assertEquals(ComplexObject.class, contract);
                    break;
                case 1:
                    Assert.assertEquals(IsAClassContract.class, contract);
                    break;
                case 2:
                    Assert.assertEquals(ParameterizedObject.class, contract);
                    break;
                case 3:
                    Assert.assertEquals(IsAContract.class, contract);
                    break;
                case 4: {
                    ParameterizedType pt = (ParameterizedType) contract;
                    
                    Assert.assertEquals(ParameterizedInterface.class, pt.getRawType());
                    Assert.assertEquals(String.class, pt.getActualTypeArguments()[0]);
                    break;  
                } 
                default:
                    Assert.fail("Unknown type: " + contract);
                }
                
                lcv++;
            }
        }
        
    }
    
    /**
     * This test ensures that metadata is properly added to a descriptor
     * from a direct class
     */
    @Test
    public void testAutoMetadata() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        ActiveDescriptor<ServiceWithMetadata> ad =
                config.addActiveDescriptor(ServiceWithMetadata.class);
        
        Map<String, List<String>> metadata = ad.getMetadata();
        
        Assert.assertEquals(metadata.toString(), 2, metadata.size());
        
        {
            List<String> values1 = metadata.get(METADATA_KEY1);
            Assert.assertEquals(values1.toString(), 1, values1.size());
            
            Assert.assertTrue(values1.contains(DynamicConfigTest.class.getName()));
        }
        
        {
            List<String> values2 = metadata.get(METADATA_KEY2);
            Assert.assertEquals(values2.toString(), 1, values2.size());
            
            Assert.assertTrue(values2.contains(""));
        }
        
        Assert.assertNotNull(ad.isProxiable());
        Assert.assertEquals(true, ad.isProxiable().booleanValue());
        
        Assert.assertEquals(DescriptorVisibility.LOCAL, ad.getDescriptorVisibility());
        
    }
}
