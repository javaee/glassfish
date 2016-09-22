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
package org.glassfish.hk2.tests.locator.twophaseresources;

import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TwoPhaseTransactionData;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class TwoPhaseResourceTest {
    /**
     * Tests that all two phase resource prepares
     * and commits are called and that the added
     * and removed lists are accurate
     */
    @Test
    // @org.junit.Ignore
    public void testAllResourcesSuccess() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(SimpleService2.class);
        DynamicConfiguration dc = getDC(locator);
        
        ServiceHandle<SimpleService2> ss2Handle = locator.getServiceHandle(SimpleService2.class);
        ActiveDescriptor<SimpleService2> removed = ss2Handle.getActiveDescriptor();
        
        Assert.assertNotNull(locator.getService(SimpleService2.class));
        Assert.assertNotNull(removed);
        
        ActiveDescriptor<SimpleService> added = dc.addActiveDescriptor(SimpleService.class);
        dc.addUnbindFilter(BuilderHelper.createContractFilter(SimpleService2.class.getName()));
        
        RecordingResource rr1 = new RecordingResource(false, false, false);
        RecordingResource rr2 = new RecordingResource(false, false, false);
        
        dc.registerTwoPhaseResources(rr1);
        dc.registerTwoPhaseResources(rr2);
        
        dc.commit();
        
        {
            List<TwoPhaseTransactionData> rr1p = rr1.getPrepares();
            Assert.assertEquals(1, rr1p.size());
            
            checkAddsList(rr1p.get(0), added);
            checkRemovedList(rr1p.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr2p = rr2.getPrepares();
            Assert.assertEquals(1, rr2p.size());
            
            checkAddsList(rr2p.get(0), added);
            checkRemovedList(rr2p.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr1c = rr1.getCommits();
            Assert.assertEquals(1, rr1c.size());
            
            checkAddsList(rr1c.get(0), added);
            checkRemovedList(rr1c.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr2c = rr2.getCommits();
            Assert.assertEquals(1, rr2c.size());
            
            checkAddsList(rr2c.get(0), added);
            checkRemovedList(rr2c.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr1r = rr1.getRollbacks();
            Assert.assertEquals(0, rr1r.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr2r = rr2.getRollbacks();
            Assert.assertEquals(0, rr2r.size());
        }
        
        {
            Assert.assertNotNull(locator.getService(SimpleService.class));
            Assert.assertNull(locator.getService(SimpleService2.class));
        }
    }
    
    /**
     * Tests that when the first resource fails the second is not called
     */
    @Test
    // @org.junit.Ignore
    public void testFirstResourcePrepareFail() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(SimpleService2.class);
        DynamicConfiguration dc = getDC(locator);
        
        ServiceHandle<SimpleService2> ss2Handle = locator.getServiceHandle(SimpleService2.class);
        ActiveDescriptor<SimpleService2> removed = ss2Handle.getActiveDescriptor();
        
        Assert.assertNotNull(locator.getService(SimpleService2.class));
        Assert.assertNotNull(removed);
        
        ActiveDescriptor<SimpleService> added = dc.addActiveDescriptor(SimpleService.class);
        dc.addUnbindFilter(BuilderHelper.createContractFilter(SimpleService2.class.getName()));
        
        RecordingResource rr1 = new RecordingResource(true, false, false);
        RecordingResource rr2 = new RecordingResource(false, false, false);
        
        dc.registerTwoPhaseResources(rr1);
        dc.registerTwoPhaseResources(rr2);
        
        try {
            dc.commit();
            Assert.fail("Should have failed due to prepare failure");
        }
        catch (MultiException me) {
            // Expected
        }
        
        {
            List<TwoPhaseTransactionData> rr1p = rr1.getPrepares();
            Assert.assertEquals(1, rr1p.size());
            
            checkAddsList(rr1p.get(0), added);
            checkRemovedList(rr1p.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr2p = rr2.getPrepares();
            Assert.assertEquals(0, rr2p.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr1c = rr1.getCommits();
            Assert.assertEquals(0, rr1c.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr2c = rr2.getCommits();
            Assert.assertEquals(0, rr2c.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr1r = rr1.getRollbacks();
            Assert.assertEquals(0, rr1r.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr2r = rr2.getRollbacks();
            Assert.assertEquals(0, rr2r.size());
        }
        
        {
            Assert.assertNull(locator.getService(SimpleService.class));
            Assert.assertNotNull(locator.getService(SimpleService2.class));
        }
    }
    
    /**
     * Tests that when the second resource fails the rollback
     * is called on the first
     */
    @Test
    // @org.junit.Ignore
    public void testSecondResourcePrepareFail() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(SimpleService2.class);
        DynamicConfiguration dc = getDC(locator);
        
        ServiceHandle<SimpleService2> ss2Handle = locator.getServiceHandle(SimpleService2.class);
        ActiveDescriptor<SimpleService2> removed = ss2Handle.getActiveDescriptor();
        
        Assert.assertNotNull(locator.getService(SimpleService2.class));
        Assert.assertNotNull(removed);
        
        ActiveDescriptor<SimpleService> added = dc.addActiveDescriptor(SimpleService.class);
        dc.addUnbindFilter(BuilderHelper.createContractFilter(SimpleService2.class.getName()));
        
        RecordingResource rr1 = new RecordingResource(false, false, false);
        RecordingResource rr2 = new RecordingResource(true, false, false);
        
        dc.registerTwoPhaseResources(rr1);
        dc.registerTwoPhaseResources(rr2);
        
        try {
            dc.commit();
            Assert.fail("Should have failed due to prepare failure");
        }
        catch (MultiException me) {
            // Expected
        }
        
        {
            List<TwoPhaseTransactionData> rr1p = rr1.getPrepares();
            Assert.assertEquals(1, rr1p.size());
            
            checkAddsList(rr1p.get(0), added);
            checkRemovedList(rr1p.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr2p = rr2.getPrepares();
            Assert.assertEquals(1, rr2p.size());
            
            checkAddsList(rr2p.get(0), added);
            checkRemovedList(rr2p.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr1c = rr1.getCommits();
            Assert.assertEquals(0, rr1c.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr2c = rr2.getCommits();
            Assert.assertEquals(0, rr2c.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr1r = rr1.getRollbacks();
            Assert.assertEquals(1, rr1r.size());
            
            checkAddsList(rr1r.get(0), added);
            checkRemovedList(rr1r.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr2r = rr2.getRollbacks();
            Assert.assertEquals(0, rr2r.size());
        }
        
        {
            Assert.assertNull(locator.getService(SimpleService.class));
            Assert.assertNotNull(locator.getService(SimpleService2.class));
        }
    }
    
    /**
     * Tests that when the resources fail in activate the
     * transaction is successful
     */
    @Test
    // @org.junit.Ignore
    public void testBothResourcesFailInActivateOk() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(SimpleService2.class);
        DynamicConfiguration dc = getDC(locator);
        
        ServiceHandle<SimpleService2> ss2Handle = locator.getServiceHandle(SimpleService2.class);
        ActiveDescriptor<SimpleService2> removed = ss2Handle.getActiveDescriptor();
        
        Assert.assertNotNull(locator.getService(SimpleService2.class));
        Assert.assertNotNull(removed);
        
        ActiveDescriptor<SimpleService> added = dc.addActiveDescriptor(SimpleService.class);
        dc.addUnbindFilter(BuilderHelper.createContractFilter(SimpleService2.class.getName()));
        
        RecordingResource rr1 = new RecordingResource(false, true, false);
        RecordingResource rr2 = new RecordingResource(false, true, false);
        
        dc.registerTwoPhaseResources(rr1);
        dc.registerTwoPhaseResources(rr2);
        
        dc.commit();
        
        {
            List<TwoPhaseTransactionData> rr1p = rr1.getPrepares();
            Assert.assertEquals(1, rr1p.size());
            
            checkAddsList(rr1p.get(0), added);
            checkRemovedList(rr1p.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr2p = rr2.getPrepares();
            Assert.assertEquals(1, rr2p.size());
            
            checkAddsList(rr2p.get(0), added);
            checkRemovedList(rr2p.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr1c = rr1.getCommits();
            Assert.assertEquals(1, rr1c.size());
            
            checkAddsList(rr1c.get(0), added);
            checkRemovedList(rr1c.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr2c = rr2.getCommits();
            Assert.assertEquals(1, rr2c.size());
            
            checkAddsList(rr2c.get(0), added);
            checkRemovedList(rr2c.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr1r = rr1.getRollbacks();
            Assert.assertEquals(0, rr1r.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr2r = rr2.getRollbacks();
            Assert.assertEquals(0, rr2r.size());
        }
        
        {
            Assert.assertNotNull(locator.getService(SimpleService.class));
            Assert.assertNull(locator.getService(SimpleService2.class));
        }
    }
    
    /**
     * Tests that when the resources fail in rollback the
     * transaction is successful
     */
    @Test
    // @org.junit.Ignore
    public void testResourceFailsInRollbackOk() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(SimpleService2.class);
        DynamicConfiguration dc = getDC(locator);
        
        ServiceHandle<SimpleService2> ss2Handle = locator.getServiceHandle(SimpleService2.class);
        ActiveDescriptor<SimpleService2> removed = ss2Handle.getActiveDescriptor();
        
        Assert.assertNotNull(locator.getService(SimpleService2.class));
        Assert.assertNotNull(removed);
        
        ActiveDescriptor<SimpleService> added = dc.addActiveDescriptor(SimpleService.class);
        dc.addUnbindFilter(BuilderHelper.createContractFilter(SimpleService2.class.getName()));
        
        RecordingResource rr1 = new RecordingResource(false, false, true);
        RecordingResource rr2 = new RecordingResource(false, false, true);
        RecordingResource rr3 = new RecordingResource(true, false, false);
        
        dc.registerTwoPhaseResources(rr1);
        dc.registerTwoPhaseResources(rr2, rr3);
        
        try {
            dc.commit();
            Assert.fail("Should have failed");
        }
        catch (MultiException me) {
            // Expected
        }
        
        {
            List<TwoPhaseTransactionData> rr1p = rr1.getPrepares();
            Assert.assertEquals(1, rr1p.size());
            
            checkAddsList(rr1p.get(0), added);
            checkRemovedList(rr1p.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr2p = rr2.getPrepares();
            Assert.assertEquals(1, rr2p.size());
            
            checkAddsList(rr2p.get(0), added);
            checkRemovedList(rr2p.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr3p = rr3.getPrepares();
            Assert.assertEquals(1, rr3p.size());
            
            checkAddsList(rr3p.get(0), added);
            checkRemovedList(rr3p.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr1c = rr1.getCommits();
            Assert.assertEquals(0, rr1c.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr2c = rr2.getCommits();
            Assert.assertEquals(0, rr2c.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr3c = rr3.getCommits();
            Assert.assertEquals(0, rr3c.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr1r = rr1.getRollbacks();
            Assert.assertEquals(1, rr1r.size());
            
            checkAddsList(rr1r.get(0), added);
            checkRemovedList(rr1r.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr2r = rr2.getRollbacks();
            Assert.assertEquals(1, rr2r.size());
            
            checkAddsList(rr2r.get(0), added);
            checkRemovedList(rr2r.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr3r = rr3.getRollbacks();
            Assert.assertEquals(0, rr3r.size());
        }
        
        {
            Assert.assertNull(locator.getService(SimpleService.class));
            Assert.assertNotNull(locator.getService(SimpleService2.class));
        }
    }
    
    /**
     * Tests that all two phase resource prepares
     * and commits are called and that the added
     * and removed lists are accurate
     */
    @Test
    // @org.junit.Ignore
    public void testOnlyAddSuccess() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(SimpleService2.class);
        DynamicConfiguration dc = getDC(locator);
        
        ServiceHandle<SimpleService2> ss2Handle = locator.getServiceHandle(SimpleService2.class);
        ActiveDescriptor<SimpleService2> removed = ss2Handle.getActiveDescriptor();
        
        Assert.assertNotNull(locator.getService(SimpleService2.class));
        Assert.assertNotNull(removed);
        
        ActiveDescriptor<SimpleService> added = dc.addActiveDescriptor(SimpleService.class);
        
        RecordingResource rr1 = new RecordingResource(false, false, false);
        RecordingResource rr2 = new RecordingResource(false, false, false);
        
        dc.registerTwoPhaseResources(rr1);
        dc.registerTwoPhaseResources(rr2);
        
        dc.commit();
        
        {
            List<TwoPhaseTransactionData> rr1p = rr1.getPrepares();
            Assert.assertEquals(1, rr1p.size());
            
            checkAddsList(rr1p.get(0), added);
            checkRemovedList(rr1p.get(0));
        }
        
        {
            List<TwoPhaseTransactionData> rr2p = rr2.getPrepares();
            Assert.assertEquals(1, rr2p.size());
            
            checkAddsList(rr2p.get(0), added);
            checkRemovedList(rr2p.get(0));
        }
        
        {
            List<TwoPhaseTransactionData> rr1c = rr1.getCommits();
            Assert.assertEquals(1, rr1c.size());
            
            checkAddsList(rr1c.get(0), added);
            checkRemovedList(rr1c.get(0));
        }
        
        {
            List<TwoPhaseTransactionData> rr2c = rr2.getCommits();
            Assert.assertEquals(1, rr2c.size());
            
            checkAddsList(rr2c.get(0), added);
            checkRemovedList(rr2c.get(0));
        }
        
        {
            List<TwoPhaseTransactionData> rr1r = rr1.getRollbacks();
            Assert.assertEquals(0, rr1r.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr2r = rr2.getRollbacks();
            Assert.assertEquals(0, rr2r.size());
        }
        
        {
            Assert.assertNotNull(locator.getService(SimpleService.class));
            Assert.assertNotNull(locator.getService(SimpleService2.class));
        }
    }
    
    /**
     * Tests that all two phase resource prepares
     * and commits are called and that the added
     * and removed lists are accurate
     */
    @Test
    // @org.junit.Ignore
    public void testOnlyRemoveSuccess() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(SimpleService2.class);
        DynamicConfiguration dc = getDC(locator);
        
        ServiceHandle<SimpleService2> ss2Handle = locator.getServiceHandle(SimpleService2.class);
        ActiveDescriptor<SimpleService2> removed = ss2Handle.getActiveDescriptor();
        
        Assert.assertNotNull(locator.getService(SimpleService2.class));
        Assert.assertNotNull(removed);
        
        dc.addUnbindFilter(BuilderHelper.createContractFilter(SimpleService2.class.getName()));
        
        RecordingResource rr1 = new RecordingResource(false, false, false);
        RecordingResource rr2 = new RecordingResource(false, false, false);
        
        dc.registerTwoPhaseResources(rr1);
        dc.registerTwoPhaseResources(rr2);
        
        dc.commit();
        
        {
            List<TwoPhaseTransactionData> rr1p = rr1.getPrepares();
            Assert.assertEquals(1, rr1p.size());
            
            checkAddsList(rr1p.get(0));
            checkRemovedList(rr1p.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr2p = rr2.getPrepares();
            Assert.assertEquals(1, rr2p.size());
            
            checkAddsList(rr2p.get(0));
            checkRemovedList(rr2p.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr1c = rr1.getCommits();
            Assert.assertEquals(1, rr1c.size());
            
            checkAddsList(rr1c.get(0));
            checkRemovedList(rr1c.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr2c = rr2.getCommits();
            Assert.assertEquals(1, rr2c.size());
            
            checkAddsList(rr2c.get(0));
            checkRemovedList(rr2c.get(0), removed);
        }
        
        {
            List<TwoPhaseTransactionData> rr1r = rr1.getRollbacks();
            Assert.assertEquals(0, rr1r.size());
        }
        
        {
            List<TwoPhaseTransactionData> rr2r = rr2.getRollbacks();
            Assert.assertEquals(0, rr2r.size());
        }
        
        {
            Assert.assertNull(locator.getService(SimpleService.class));
            Assert.assertNull(locator.getService(SimpleService2.class));
        }
    }
    
    private static DynamicConfiguration getDC(ServiceLocator locator) {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        DynamicConfiguration cd = dcs.createDynamicConfiguration();
        Assert.assertNotNull(cd);
        return cd;
    }
    
    private static void checkAddsList(TwoPhaseTransactionData transactionData, ActiveDescriptor<?>... addedDescriptors) {
        Assert.assertEquals(transactionData.getAllAddedDescriptors().size(), addedDescriptors.length);
        
        for (int lcv = 0; lcv < addedDescriptors.length; lcv++) {
            ActiveDescriptor<?> transactionDataAdd = transactionData.getAllAddedDescriptors().get(lcv);
            
            Assert.assertEquals(addedDescriptors[lcv], transactionDataAdd);
        }
    }
    
    private static void checkRemovedList(TwoPhaseTransactionData transactionData, ActiveDescriptor<?>... removedDescriptors) {
        Assert.assertEquals(transactionData.getAllRemovedDescriptors().size(), removedDescriptors.length);
        
        for (int lcv = 0; lcv < removedDescriptors.length; lcv++) {
            ActiveDescriptor<?> transactionDataAdd = transactionData.getAllRemovedDescriptors().get(lcv);
            
            Assert.assertEquals(removedDescriptors[lcv], transactionDataAdd);
        }
    }

}
