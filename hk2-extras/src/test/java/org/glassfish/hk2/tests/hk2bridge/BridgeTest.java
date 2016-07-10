/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.hk2bridge;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.AnnotationLiteral;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ProxyCtl;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extras.ExtrasUtilities;
import org.glassfish.hk2.extras.operation.OperationHandle;
import org.glassfish.hk2.extras.operation.OperationManager;
import org.glassfish.hk2.tests.extras.internal.Utilities;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class BridgeTest {
    private static final PerRequest PER_REQUEST = new PerRequestImpl();
    
    /**
     * Tests the hk2 to hk2 bridging feature
     */
    @Test // @org.junit.Ignore
    public void testBasicOneWayBridge() {
        ServiceLocator into = Utilities.getUniqueLocator();
        ServiceLocator from = Utilities.getUniqueLocator(SimpleService.class);
        
        Assert.assertNull(into.getService(SimpleService.class));
        ExtrasUtilities.bridgeServiceLocator(into, from);
        
        Assert.assertNotNull(into.getService(SimpleService.class));
    }
    
    /**
     * Tests that dynamic changes are captured in the from locator
     */
    @Test // @org.junit.Ignore
    public void testDynamicallyAddAndRemove() {
        ServiceLocator into = Utilities.getUniqueLocator();
        ServiceLocator from = Utilities.getUniqueLocator(SimpleService.class);
        
        ExtrasUtilities.bridgeServiceLocator(into, from);
        
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNull(into.getService(SimpleService2.class));
        Assert.assertNotNull(from.getService(SimpleService.class));
        Assert.assertNull(from.getService(SimpleService2.class));
        
        DynamicConfigurationService dcs = from.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addActiveDescriptor(SimpleService2.class);
        config.addUnbindFilter(BuilderHelper.createContractFilter(SimpleService.class.getName()));
        
        config.commit();
        
        Assert.assertNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
        Assert.assertNull(from.getService(SimpleService.class));
        Assert.assertNotNull(from.getService(SimpleService2.class));
    }
    
    /**
     * Tests that bridged services are represented in lists
     */
    @Test // @org.junit.Ignore
    public void testAllServicesGetsBoth() {
        ServiceLocator into = Utilities.getUniqueLocator(SimpleService.class);
        ServiceLocator from = Utilities.getUniqueLocator(SimpleService.class);
        
        Assert.assertEquals(1, into.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, from.getAllServices(SimpleService.class).size());
        
        ExtrasUtilities.bridgeServiceLocator(into, from);
        
        Assert.assertEquals(2, into.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, from.getAllServices(SimpleService.class).size());
    }
    
    /**
     * Tests that cycles can be done
     */
    @Test // @org.junit.Ignore
    public void testCycle() {
        ServiceLocator into = Utilities.getUniqueLocator(SimpleService.class, SimpleService3.class);
        ServiceLocator from = Utilities.getUniqueLocator(SimpleService2.class, SimpleService3.class);
        
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNull(into.getService(SimpleService2.class));
        Assert.assertNull(from.getService(SimpleService.class));
        Assert.assertNotNull(from.getService(SimpleService2.class));
        Assert.assertNotNull(into.getService(SimpleService3.class));
        Assert.assertNotNull(from.getService(SimpleService3.class));
        
        ExtrasUtilities.bridgeServiceLocator(into, from);
        ExtrasUtilities.bridgeServiceLocator(from, into);
        
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
        Assert.assertNotNull(from.getService(SimpleService.class));
        Assert.assertNotNull(from.getService(SimpleService2.class));
        Assert.assertNotNull(into.getService(SimpleService3.class));
        Assert.assertNotNull(from.getService(SimpleService3.class));
        
        Assert.assertEquals(1, into.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, from.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, into.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, from.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(2, into.getAllServices(SimpleService3.class).size());
        Assert.assertEquals(2, from.getAllServices(SimpleService3.class).size());
        
    }
    
    /**
     * Tests that two locators can both bridge into a single other locator
     */
    @Test // @org.junit.Ignore
    public void testTwoBridges() {
        ServiceLocator into = Utilities.getUniqueLocator();
        ServiceLocator from1 = Utilities.getUniqueLocator(SimpleService.class);
        ServiceLocator from2 = Utilities.getUniqueLocator(SimpleService2.class);
        
        ExtrasUtilities.bridgeServiceLocator(into, from1);
        ExtrasUtilities.bridgeServiceLocator(into, from2);
        
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
    }
    
    /**
     * Tests that locators can chain
     */
    @Test // @org.junit.Ignore
    public void testBridgeChain() {
        ServiceLocator into = Utilities.getUniqueLocator(SimpleService.class);
        ServiceLocator from1 = Utilities.getUniqueLocator(SimpleService2.class);
        ServiceLocator from2 = Utilities.getUniqueLocator(SimpleService3.class);
        
        ExtrasUtilities.bridgeServiceLocator(into, from1);
        ExtrasUtilities.bridgeServiceLocator(from1, from2);
        
        // The into locator should now have all services
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
        Assert.assertNotNull(into.getService(SimpleService3.class));
        
        // The from1 locator should have Services 2 and 3
        Assert.assertNull(from1.getService(SimpleService.class));
        Assert.assertNotNull(from1.getService(SimpleService2.class));
        Assert.assertNotNull(from1.getService(SimpleService3.class));
        
        // The from2 locator should have Service3 only
        Assert.assertNull(from2.getService(SimpleService.class));
        Assert.assertNull(from2.getService(SimpleService2.class));
        Assert.assertNotNull(from2.getService(SimpleService3.class));
    }
    
    /**
     * Tests that locators can chain with a cycle
     */
    @Test // @org.junit.Ignore
    public void testBridgeChainWithCycle() {
        ServiceLocator into = Utilities.getUniqueLocator(SimpleService.class);
        ServiceLocator from1 = Utilities.getUniqueLocator(SimpleService2.class);
        ServiceLocator from2 = Utilities.getUniqueLocator(SimpleService3.class);
        
        ExtrasUtilities.bridgeServiceLocator(into, from1);
        ExtrasUtilities.bridgeServiceLocator(from1, from2);
        ExtrasUtilities.bridgeServiceLocator(from2, into);
        
        // The into locator should now have all services
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
        Assert.assertNotNull(into.getService(SimpleService3.class));
        
        // The from1 locator should have all services
        Assert.assertNotNull(from1.getService(SimpleService.class));
        Assert.assertNotNull(from1.getService(SimpleService2.class));
        Assert.assertNotNull(from1.getService(SimpleService3.class));
        
        // The from2 locator should have all services
        Assert.assertNotNull(from2.getService(SimpleService.class));
        Assert.assertNotNull(from2.getService(SimpleService2.class));
        Assert.assertNotNull(from2.getService(SimpleService3.class));
        
        // Everybody should have one of each
        Assert.assertEquals(1, into.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, into.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, into.getAllServices(SimpleService3.class).size());
        
        Assert.assertEquals(1, from1.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, from1.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, from1.getAllServices(SimpleService3.class).size());
        
        Assert.assertEquals(1, from2.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, from2.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, from2.getAllServices(SimpleService3.class).size());
        
    }
    
    /**
     * This type of cycle could cause problems:
     * a -> b -> c -> b
     */
    @Test // @org.junit.Ignore
    public void testBridgeChainWithInnerCycle() {
        ServiceLocator into = Utilities.getUniqueLocator(SimpleService.class);
        ServiceLocator from1 = Utilities.getUniqueLocator(SimpleService2.class);
        ServiceLocator from2 = Utilities.getUniqueLocator(SimpleService3.class);
        
        ExtrasUtilities.bridgeServiceLocator(into, from1);
        ExtrasUtilities.bridgeServiceLocator(from1, from2);
        ExtrasUtilities.bridgeServiceLocator(from1, into);
        
        // The into locator should now have all services
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
        Assert.assertNotNull(into.getService(SimpleService3.class));
        
        // The from1 locator should have all services
        Assert.assertNotNull(from1.getService(SimpleService.class));
        Assert.assertNotNull(from1.getService(SimpleService2.class));
        Assert.assertNotNull(from1.getService(SimpleService3.class));
        
        // The from2 locator should have Service3 only
        Assert.assertNull(from2.getService(SimpleService.class));
        Assert.assertNull(from2.getService(SimpleService2.class));
        Assert.assertNotNull(from2.getService(SimpleService3.class));
        
        // Should be one of each in the into locator
        Assert.assertEquals(1, into.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, into.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, into.getAllServices(SimpleService3.class).size());
        
        // Should be one of each in the from1 locator (which is the test, basically)
        Assert.assertEquals(1, from1.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, from1.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, from1.getAllServices(SimpleService3.class).size());
        
        // Should only be one Service2 in from2
        Assert.assertEquals(0, from2.getAllServices(SimpleService.class).size());
        Assert.assertEquals(0, from2.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, from2.getAllServices(SimpleService3.class).size());
    }
    
    /**
     * Ensure chained removals works
     */
    @Test // @org.junit.Ignore
    public void testChainedRemovals() {
        ServiceLocator into = Utilities.getUniqueLocator();
        ServiceLocator from1 = Utilities.getUniqueLocator();
        ServiceLocator from2 = Utilities.getUniqueLocator(SimpleService.class, SimpleService2.class);
        
        ExtrasUtilities.bridgeServiceLocator(into, from1);
        ExtrasUtilities.bridgeServiceLocator(from1, from2);
        
        // The into locator should now have all services
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
        
        ServiceLocatorUtilities.removeFilter(from2, BuilderHelper.createContractFilter(SimpleService.class.getName()));
        
        // The into locator should now only have SimpleService2
        Assert.assertNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
    }
    
    /**
     * Tests that we will not bridge parents or selfies
     */
    @Test // @org.junit.Ignore
    public void testNoParentedBridges() {
        ServiceLocator grandparent = Utilities.FACTORY.create("NoParentedBridges_Grandparent");
        ServiceLocator parent = Utilities.FACTORY.create("NoParentedBridges_Parent", grandparent);
        ServiceLocator child = Utilities.FACTORY.create("NoParentedBridges_Child", parent);
        
        try {
            ExtrasUtilities.bridgeServiceLocator(grandparent, child);
            Assert.fail("Bridging into parents is not allowed");
        }
        catch (IllegalStateException ise) {
            // Good
        }
        
        try {
            ExtrasUtilities.bridgeServiceLocator(child, grandparent);
            Assert.fail("Bridging into children is not allowed");
        }
        catch (IllegalStateException ise) {
            // Good
        }
        
        try {
            ExtrasUtilities.bridgeServiceLocator(parent, parent);
            Assert.fail("Bridging into self is not allowed");
        }
        catch (IllegalStateException ise) {
            // Good
        }
    }
    
    /**
     * Tests unbridging two locators
     */
    @Test // @org.junit.Ignore
    public void testUnbridging() {
        ServiceLocator into = Utilities.getUniqueLocator();
        ServiceLocator from = Utilities.getUniqueLocator(SimpleService.class);
        
        Assert.assertNull(into.getService(SimpleService.class));
        ExtrasUtilities.bridgeServiceLocator(into, from);
        
        Assert.assertNotNull(into.getService(SimpleService.class));
        
        ExtrasUtilities.unbridgeServiceLocator(into, from);
        
        Assert.assertNull(into.getService(SimpleService.class));
        
        ServiceLocatorUtilities.addClasses(from, SimpleService2.class);
        
        // Ensures no longer tracking services
        Assert.assertNull(into.getService(SimpleService2.class));
    }
    
    /**
     * Tests unbridging two locators
     */
    @Test // @org.junit.Ignore
    public void testShutdown() {
        ServiceLocator into = Utilities.getUniqueLocator();
        ServiceLocator from = Utilities.getUniqueLocator(SimpleService.class);
        
        Assert.assertNull(into.getService(SimpleService.class));
        ExtrasUtilities.bridgeServiceLocator(into, from);
        
        Assert.assertNotNull(into.getService(SimpleService.class));
        
        from.shutdown();
        
        Assert.assertNull(into.getService(SimpleService.class));
    }
    
    /**
     * Tests bridge with un-reified descriptors
     */
    @Test // @org.junit.Ignore
    public void testBridgeUnreifiedDescriptor() {
        ServiceLocator into = Utilities.getUniqueLocator();
        ServiceLocator from = Utilities.getUniqueLocator();
        
        Descriptor d = BuilderHelper.link(SimpleService.class.getName()).build();
        ActiveDescriptor<?> fromD = ServiceLocatorUtilities.addOneDescriptor(from, d);
        
        Descriptor perLookupD = BuilderHelper.link(PerLookupService.class.getName()).
                in(PerLookup.class.getName()).
                build();
        ActiveDescriptor<?> fromPerLookupD = ServiceLocatorUtilities.addOneDescriptor(from, perLookupD);
        
        Descriptor singletonD = BuilderHelper.link(SingletonService.class.getName()).
                in(Singleton.class.getName()).
                build();
        ActiveDescriptor<?> fromSingletonD = ServiceLocatorUtilities.addOneDescriptor(from, singletonD);
        
        Assert.assertNull(into.getService(SimpleService.class));
        ExtrasUtilities.bridgeServiceLocator(into, from);
        
        {
            ActiveDescriptor<?> intoD = into.getBestDescriptor(BuilderHelper.createContractFilter(SimpleService.class.getName()));
        
            Assert.assertFalse(fromD.isReified());
            Assert.assertTrue(intoD.isReified());
        
            Assert.assertNull(intoD.getScope());
        }
        
        {
            ActiveDescriptor<?> intoPerLookupD = into.getBestDescriptor(BuilderHelper.createContractFilter(PerLookupService.class.getName()));
            
            Assert.assertFalse(fromPerLookupD.isReified());
            Assert.assertTrue(intoPerLookupD.isReified());
        
            Assert.assertEquals(PerLookup.class.getName(), intoPerLookupD.getScope());
            
        }
        
        {
            ActiveDescriptor<?> intoSingletonD = into.getBestDescriptor(BuilderHelper.createContractFilter(SingletonService.class.getName()));
            
            Assert.assertFalse(fromSingletonD.isReified());
            Assert.assertTrue(intoSingletonD.isReified());
        
            Assert.assertEquals(Singleton.class.getName(), intoSingletonD.getScope());
        }
        
        
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(PerLookupService.class));
        Assert.assertNotNull(into.getService(SingletonService.class));
    }
    
    /**
     * Makes sure a singleton is only started once when using the bridge
     */
    @Test // @org.junit.Ignore
    public void testSingletonOnlyStartedOnce() {
        ServiceLocator locator1 = Utilities.getCleanLocator("SingletonOnlyStartedOnce-1",
                ConstructorCountingSingletonService.class);
        ServiceLocator locator2 = Utilities.getCleanLocator("SingletonOnlyStartedOnce-2");
        
        try {
            ConstructorCountingSingletonService.reset();
        
            ExtrasUtilities.bridgeServiceLocator(locator2, locator1);
        
            ConstructorCountingSingletonService s1 = locator1.getService(ConstructorCountingSingletonService.class);
            ConstructorCountingSingletonService s2 = locator2.getService(ConstructorCountingSingletonService.class);
        
            Assert.assertEquals(1, ConstructorCountingSingletonService.getNumConstructorCalls());
            Assert.assertEquals(s1, s2);
        }
        finally {
            locator2.shutdown();
            locator1.shutdown();
        }
        
    }
    
    /**
     * Makes sure a context defined in one locator works properly
     * from the other locator
     */
    @Test // @org.junit.Ignore
    public void testContextFromOneLocatorWorksInOtherlocator() {
        ServiceLocator locator1 = Utilities.getCleanLocator("testContextFromOneLocatorWorksInOtherlocator-1",
                PerRequestOperationContext.class,
                PerRequestService.class);
        ServiceLocator locator2 = Utilities.getCleanLocator("testContextFromOneLocatorWorksInOtherlocator-2",
                SingletonInjectsPerRequest.class,
                AnotherPerRequestService.class);
        
        ExtrasUtilities.enableOperations(locator1);
        
        PerRequestService.reset();
        
        OperationHandle<PerRequest> handle = null;
        try {
            ExtrasUtilities.bridgeServiceLocator(locator2, locator1);
        
            OperationManager operationManager = locator1.getService(OperationManager.class);
            handle = operationManager.createAndStartOperation(PER_REQUEST);
            
            Assert.assertEquals(0, PerRequestService.getNumInitializations());
            
            SingletonInjectsPerRequest sipr = locator2.getService(SingletonInjectsPerRequest.class);
            PerRequestService prs = locator2.getService(PerRequestService.class);
            AnotherPerRequestService aprs = locator2.getService(AnotherPerRequestService.class);
            
            prs.invoke();
            
            // Just here to ensure only one initialization no matter from two locators
            locator1.getService(PerRequestService.class);
            
            Assert.assertEquals(1, PerRequestService.getNumInitializations());
            
            PerRequestService originalPRS = (PerRequestService) ((ProxyCtl) prs).__make();
            
            Assert.assertTrue(sipr.getUnderlyingService() == originalPRS);
            Assert.assertTrue(aprs.returnNotProxied() == originalPRS);
            
            Assert.assertEquals(1, PerRequestService.getNumInitializations());
            
            handle.closeOperation();
            handle = operationManager.createAndStartOperation(PER_REQUEST);
            
            Assert.assertFalse(sipr.getUnderlyingService() == originalPRS);
            Assert.assertFalse(aprs.returnNotProxied() == originalPRS);
            
            originalPRS.invoke();
            
            Assert.assertEquals(2, PerRequestService.getNumInitializations());
        }
        finally {
            if (handle != null) {
                handle.closeOperation();
            }
            
            locator2.shutdown();
            locator1.shutdown();
        }
        
    }
    
    private static class PerRequestImpl extends AnnotationLiteral<PerRequest> implements PerRequest {
    }

}
