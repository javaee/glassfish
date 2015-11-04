/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.tests.locator.immediate;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.FactoryDescriptors;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.hk2.api.ImmediateController;
import org.glassfish.hk2.api.ImmediateController.ImmediateServiceState;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ImmediateScopeModule;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ImmediateTest {
    /* package */ static final String EXPECTED = "Exepcted Immediate Exception";
    
    /**
     * Tests that an immediate service is started and stopped when
     * added and removed
     * @throws InterruptedException 
     */
    @Test
    public void testBasicImmediate() throws InterruptedException {
        WaitableImmediateService.clear();
        
        ServiceLocator locator = LocatorHelper.create();
        
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        List<ActiveDescriptor<?>> ims = ServiceLocatorUtilities.addClasses(locator, WaitableImmediateService.class);
        
        int numCreations = WaitableImmediateService.waitForCreationsGreaterThanZero(5 * 1000);
        Assert.assertEquals(1, numCreations);
        Assert.assertEquals(0, WaitableImmediateService.getNumDeletions());
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, ims.get(0));
        
        int numDeletions = WaitableImmediateService.waitForDeletionsGreaterThanZero(5 * 1000);
        Assert.assertEquals(1, numDeletions);
        Assert.assertEquals(1, WaitableImmediateService.getNumCreations());
    }
    
    /**
     * This test ensures that services added *prior* to the scope being added
     * will get created
     * 
     * @throws InterruptedException
     */
    @Test
    public void testImmediateAfterthought() throws InterruptedException {
        WaitableImmediateService.clear();
        
        ServiceLocator locator = LocatorHelper.create();
        
        List<ActiveDescriptor<?>> ims = ServiceLocatorUtilities.addClasses(locator, WaitableImmediateService.class);
        
        // No scope added, not created yet
        Assert.assertEquals(0, WaitableImmediateService.getNumCreations());
        Assert.assertEquals(0, WaitableImmediateService.getNumDeletions());
        
        // Scope added now
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        int numCreations = WaitableImmediateService.waitForCreationsGreaterThanZero(5 * 1000);
        Assert.assertEquals(1, numCreations);
        Assert.assertEquals(0, WaitableImmediateService.getNumDeletions());
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, ims.get(0));
        
        int numDeletions = WaitableImmediateService.waitForDeletionsGreaterThanZero(5 * 1000);
        Assert.assertEquals(1, numDeletions);
        Assert.assertEquals(1, WaitableImmediateService.getNumCreations());
    }
    
    /**
     * This test ensures that the error handler is called
     * 
     * @throws InterruptedException
     */
    @Test
    public void testImmediateFailedInConstructor() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        List<ActiveDescriptor<?>> ims = ServiceLocatorUtilities.addClasses(locator,
                ConstructorFailingImmediateService.class,
                ImmediateErrorHandlerImpl.class);
        
        ImmediateErrorHandlerImpl handler = locator.getService(ImmediateErrorHandlerImpl.class);
        
        List<ErrorData> errorDatum = handler.waitForAtLeastOneConstructionError(5 * 1000);
        Assert.assertEquals(1, errorDatum.size());
        
        Assert.assertEquals(ims.get(0), errorDatum.get(0).getDescriptor());
        Assert.assertTrue(errorDatum.get(0).getThrowable().toString().contains(EXPECTED));
    }
    
    /**
     * This test ensures that the error handler is called when post construct fails
     * 
     * @throws InterruptedException
     */
    @Test
    public void testImmediateFailedInPostConstruct() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        List<ActiveDescriptor<?>> ims = ServiceLocatorUtilities.addClasses(locator,
                PostConstructFailingImmediateService.class,
                ImmediateErrorHandlerImpl.class);
        
        ImmediateErrorHandlerImpl handler = locator.getService(ImmediateErrorHandlerImpl.class);
        
        List<ErrorData> errorDatum = handler.waitForAtLeastOneConstructionError(5 * 1000);
        Assert.assertEquals(1, errorDatum.size());
        
        Assert.assertEquals(ims.get(0), errorDatum.get(0).getDescriptor());
        Assert.assertTrue(errorDatum.get(0).getThrowable().toString().contains(EXPECTED));
    }
    
    /**
     * This test ensures that the error handler is called when pre destroy fails
     * 
     * @throws InterruptedException
     */
    @Test
    public void testImmediateFailedInPreDestroy() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        List<ActiveDescriptor<?>> ims = ServiceLocatorUtilities.addClasses(locator,
                PreDestroyFailingImmediateService.class,
                ImmediateErrorHandlerImpl.class);
        
        ImmediateErrorHandlerImpl handler = locator.getService(ImmediateErrorHandlerImpl.class);
        
        // Doing this ensures that the immediate service is registered
        Assert.assertNotNull(locator.getService(PreDestroyFailingImmediateService.class));
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, ims.get(0));
        
        List<ErrorData> errorDatum = handler.waitForAtLeastOneDestructionError(5 * 1000);
        Assert.assertEquals(1, errorDatum.size());
        
        Assert.assertEquals(ims.get(0), errorDatum.get(0).getDescriptor());
        Assert.assertTrue(errorDatum.get(0).getThrowable().toString().contains(EXPECTED));
    }
    
    /**
     * Tests that an immediate service is started and stopped when
     * added and removed and the service is created by a Factory
     * 
     * @throws InterruptedException 
     */
    @Test
    public void testFactoryImmediate() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        DynamicConfiguration cd = locator.getService(DynamicConfigurationService.class).createDynamicConfiguration();
        
        FactoryDescriptors fd = BuilderHelper.link(ImmediateServiceFactory.class.getName()).
          to(GenericImmediateService.class).
          in(Immediate.class.getName()).
          buildFactory(Singleton.class);
        
        FactoryDescriptors added = cd.bind(fd);
        
        cd.commit();
        
        ImmediateServiceFactory factory = locator.getService(ImmediateServiceFactory.class);
        
        Assert.assertTrue(factory.waitToCreate(5 * 1000));
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, added.getFactoryAsAFactory());
        
        Assert.assertTrue(factory.waitToDestroy(5 * 1000));
    }
    
    /**
     * This test is a little non-black-boxy.  In the current implementation the
     * one thread that does the work has a decay time, which means that if it has
     * no work for 20 seconds or so, it will then go away.  This is meant to keep
     * the system from spawning a lot of threads during a flurry of configuration
     * events such as what happens at the boot or shutdown of a system.
     * 
     * This test makes sure that is nominally working by adding an immediate service,
     * waiting a second, and then adding another and making sure they were
     * both created on the same thread
     * @throws InterruptedException 
     */
    @Test
    public void testThreadDecay() throws InterruptedException {
        clearTid();
        
        ServiceLocator locator = LocatorHelper.create();
        
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        ServiceLocatorUtilities.addClasses(locator,
                ImmediateTidRecorder.class);
        
        long firstTid = waitForTid(5 * 1000);
        Assert.assertTrue(firstTid > 0);
        
        clearTid();
        
        long dummyTid = waitForTid(100);
        
        // This is a good test that perhaps the first
        // service is not created twice
        Assert.assertEquals(-1, dummyTid);
        
        // Add a second, should happen on that old idling thread
        ServiceLocatorUtilities.addClasses(locator,
                ImmediateTidRecorder.class);
        
        long secondTid = waitForTid(5 * 1000);
        Assert.assertTrue(secondTid > 0);
        
        Assert.assertEquals(firstTid, secondTid);
    }
    
    /**
     * Tests that an immediate service is started and stopped when
     * added and removed and the service is created by a Factory
     * 
     * @throws InterruptedException 
     */
    @Test
    public void testDestroyedWhenLocatorShutdown() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                AnotherGetsDestroyedService.class,
                GetsDestroyedService.class,
                GetsDestroyedPerLookupService.class);
        
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        Thread.sleep(200);
        
        AnotherGetsDestroyedService agds = locator.getService(AnotherGetsDestroyedService.class);
        GetsDestroyedService gds = locator.getService(GetsDestroyedService.class);
        GetsDestroyedPerLookupService gdpls = gds.getPerLookupService();
        
        Assert.assertFalse(agds.isDestroyed());
        Assert.assertFalse(gds.isDestroyed());
        Assert.assertFalse(gdpls.isDestroyed());
        
        locator.shutdown();
        
        Assert.assertTrue(agds.isDestroyed());
        Assert.assertTrue(gds.isDestroyed());
        Assert.assertTrue(gdpls.isDestroyed());
    }
    
    /**
     * Tests that an immediate service is started and stopped when
     * added and removed using the ImmediateScopeModule to enable
     * the feature
     * 
     * @throws InterruptedException 
     */
    @Test
    public void testBasicImmediateBinder() throws InterruptedException {
        WaitableImmediateService.clear();
        
        ServiceLocator locator = ServiceLocatorUtilities.bind(new ImmediateScopeModule());
        ImmediateController controller = locator.getService(ImmediateController.class);
        controller.setImmediateState(ImmediateServiceState.RUNNING);
        
        List<ActiveDescriptor<?>> ims = ServiceLocatorUtilities.addClasses(locator, WaitableImmediateService.class);
        
        int numCreations = WaitableImmediateService.waitForCreationsGreaterThanZero(5 * 1000);
        Assert.assertEquals(1, numCreations);
        Assert.assertEquals(0, WaitableImmediateService.getNumDeletions());
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, ims.get(0));
        
        int numDeletions = WaitableImmediateService.waitForDeletionsGreaterThanZero(5 * 1000);
        Assert.assertEquals(1, numDeletions);
        Assert.assertEquals(1, WaitableImmediateService.getNumCreations());
    }
    
    /**
     * Tests that an immediate service that is already in its post construct and which
     * will take a long time to complete that post construct can have that service
     * be asked for by another thread (in other words, the service is already being
     * created at the time when another thread asks for the same service)
     * @throws InterruptedException 
     */
    @Test
    public void testImmediateServiceWithLongPostConstructAndAskedForByThisThread() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        // Start off the service creation
        ServiceLocatorUtilities.addClasses(locator, SleepInPostConstructService.class);
        
        Assert.assertTrue(SleepInPostConstructService.waitForPostConstruct(20 * 1000));
        
        // Quickly now, ask for the service
        SleepInPostConstructService service = locator.getService(SleepInPostConstructService.class);
        Assert.assertNotNull(service);
        Assert.assertEquals(1, service.getNumCreations());
    }
    
    /**
     * Tests that an immediate service that is running after its associated
     * service locator has died will not send spurious messages in the
     * output
     * 
     * @throws InterruptedException 
     */
    @Test
    public void testImmediateServiceStillGoingAfterLocatorShutdown() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                LongTimePostConstructImmediateService.class,
                EmptyImmediateServiceOne.class,
                ImmediateErrorHandlerImpl.class);
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        ImmediateErrorHandlerImpl errorHandler = locator.getService(ImmediateErrorHandlerImpl.class);
        
        LongTimePostConstructImmediateService.waitUntilRunning();
        
        locator.shutdown();
        
        LongTimePostConstructImmediateService.release();
        
        List<ErrorData> errorData = errorHandler.waitForAtLeastOneConstructionError(20 * 1000);
        Assert.assertEquals(1, errorData.size());
        
        Throwable th = errorData.get(0).getThrowable();
        Assert.assertTrue(th instanceof IllegalStateException);
        Assert.assertTrue(th.getMessage().contains(locator.getName()));
    }
    
    /**
     * Tests that when we enable Immediate scope in suspended mode
     * that it doesn't in fact start any Immediate services
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testImmediateInSuspendedIsSuspended() throws InterruptedException {
        WaitableImmediateService.clear();
        
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                WaitableImmediateService.class);
        
        ImmediateController controller = ServiceLocatorUtilities.enableImmediateScopeSuspended(locator);
        Assert.assertNotNull(controller);
        
        Assert.assertEquals(ImmediateController.ImmediateServiceState.SUSPENDED, controller.getImmediateState());
        
        // Wait a half-second to ensure the immediate service does not come on-line
        int numStarts = WaitableImmediateService.waitForCreationsGreaterThanZero(100);
        Assert.assertEquals(0, numStarts);
        
        controller.setImmediateState(ImmediateController.ImmediateServiceState.RUNNING);
        
        numStarts = WaitableImmediateService.waitForCreationsGreaterThanZero(100);
        Assert.assertEquals(1, numStarts);
        
    }
    
    private final static int NUM_LOCATORS = 4;
    
    /**
     * Creates n different ServiceLocators and uses the same Executor for all of them, then ensures
     * that all the threads used by all of the locators is the same one
     * 
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testCanSetExecutorToBeTheSameAmongstDifferentLocators() throws InterruptedException {
        ServiceLocator locators[] = new ServiceLocator[NUM_LOCATORS];
        ImmediateController controllers[] = new ImmediateController[NUM_LOCATORS];
        ImmediateThreadIdHolderService services[] = new ImmediateThreadIdHolderService[NUM_LOCATORS];
        
        Executor executor = new ThreadPoolExecutor(0, 1,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new SimpleThreadFactory());
        
        for (int lcv = 0; lcv < NUM_LOCATORS; lcv++) {
            locators[lcv] = LocatorHelper.getServiceLocator(ImmediateThreadIdHolderService.class);
            
            controllers[lcv] = ServiceLocatorUtilities.enableImmediateScopeSuspended(locators[lcv]);
            controllers[lcv].setExecutor(executor);
            controllers[lcv].setThreadInactivityTimeout(0);
        }
        
        // All set up, lets blast them off!
        for (int lcv = 0; lcv < NUM_LOCATORS; lcv++) {
            controllers[lcv].setImmediateState(ImmediateServiceState.RUNNING);
        }
        
        boolean first = true;
        long tid = -1;
        
        // Ensure the thread has plenty of time to get going
        Thread.sleep(200);
        
        for (int lcv = 0; lcv < NUM_LOCATORS; lcv++) {
            services[lcv] = locators[lcv].getService(ImmediateThreadIdHolderService.class);
            
            if (first) {
                first = false;
                tid = services[lcv].getTid(20 * 1000);
                Assert.assertTrue(tid >= 0);
            }
            else {
                long compareTid = services[lcv].getTid(20 * 1000);
                
                Assert.assertEquals(tid, compareTid);
            }
        }
        
        
        for (int lcv = 0; lcv < NUM_LOCATORS; lcv++) {
            locators[lcv].shutdown();
        }
    }
    
    /**
     * Tests that if there is no work for the Immediate system to do that it will
     * not start a thread
     * 
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testNoWorkNoThread() throws InterruptedException {
        // No immediate services of any kind
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        
        FailingThreadFactory threadFactory = new FailingThreadFactory();
        
        Executor executor = new ThreadPoolExecutor(0, 100,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory);
        
        ImmediateController controller = ServiceLocatorUtilities.enableImmediateScopeSuspended(locator);
        
        controller.setExecutor(executor);
        
        controller.setImmediateState(ImmediateServiceState.RUNNING);
        
        Assert.assertFalse(threadFactory.didTryToStartOne(100));
        
        locator.shutdown();
    }
    
    /**
     * Tests that a decay of zero truly decays the thread
     * 
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testDecayOfZeroDoesNotHoldThread() throws InterruptedException {
        clearTid();
        
        // No immediate services of any kind
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        ImmediateController controller = ServiceLocatorUtilities.enableImmediateScopeSuspended(locator);
        
        Executor executor = new ThreadPoolExecutor(0, 100,
                0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new SimpleThreadFactory());
        
        controller.setExecutor(executor);
        
        // Checks that the default is 20 seconds
        Assert.assertEquals(20 * 1000, controller.getThreadInactivityTimeout());
        
        controller.setThreadInactivityTimeout(0);
        Assert.assertEquals(0, controller.getThreadInactivityTimeout());
        
        controller.setImmediateState(ImmediateServiceState.RUNNING);
        
        ServiceLocatorUtilities.addClasses(locator, ImmediateTidRecorder.class);
        
        long tid1 = waitForTid(20 * 1000);
        clearTid();
        
        // wait for a little to allow the thread to die, since its inactivity timeout is 0
        Thread.sleep(100);
        
        ServiceLocatorUtilities.addClasses(locator, ImmediateTidRecorder.class);
        
        long tid2 = waitForTid(20 * 1000);
        
        Assert.assertNotEquals(tid1, tid2);
    }
    
    /**
     * Tests setting and getting the executor
     * 
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testSetsGetsOfExecutor() throws InterruptedException {
        // No immediate services of any kind
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        ImmediateController controller = ServiceLocatorUtilities.enableImmediateScopeSuspended(locator);
        
        Executor defaultExecutor = controller.getExecutor();
        Assert.assertNotNull(defaultExecutor);
        
        Executor executor = new ThreadPoolExecutor(0, 100,
                0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new SimpleThreadFactory());
        
        controller.setExecutor(executor);
        
        Executor setExecutor = controller.getExecutor();
        Assert.assertNotNull(setExecutor);
        
        Assert.assertNotEquals(defaultExecutor, setExecutor);
        
        controller.setExecutor(null);
        
        Assert.assertEquals(defaultExecutor, controller.getExecutor());
        
        controller.setImmediateState(ImmediateServiceState.RUNNING);
        
        try {
            controller.setExecutor(executor);
            Assert.fail("Should not be able to set this while running");
        }
        catch (IllegalStateException expected) {
            // expected exception
        }
    }
    
    /**
     * Tests setting and getting the state
     * 
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testSetState() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        ImmediateController controller = ServiceLocatorUtilities.enableImmediateScopeSuspended(locator);
        
        Assert.assertEquals(ImmediateServiceState.SUSPENDED, controller.getImmediateState());
        
        // Sets it to the same thing again (SUSPENDED)
        controller.setImmediateState(ImmediateServiceState.SUSPENDED);
        Assert.assertEquals(ImmediateServiceState.SUSPENDED, controller.getImmediateState());
        
        try {
            controller.setImmediateState(null);
            Assert.fail("Should not be able to set the state to null");
        }
        catch (IllegalArgumentException iae) {
            // expected
        }
        
        controller.setImmediateState(ImmediateServiceState.RUNNING);
        Assert.assertEquals(ImmediateServiceState.RUNNING, controller.getImmediateState());
        
        // Sets it to the same thing again (RUNNING)
        controller.setImmediateState(ImmediateServiceState.RUNNING);
        Assert.assertEquals(ImmediateServiceState.RUNNING, controller.getImmediateState());
        
        // Back to SUSPENDED, just to make sure we can
        controller.setImmediateState(ImmediateServiceState.SUSPENDED);
        Assert.assertEquals(ImmediateServiceState.SUSPENDED, controller.getImmediateState());
        
        // Now ensure the other version of enable starts it out in RUNNING state
        ServiceLocator locator2 = LocatorHelper.getServiceLocator();
        ServiceLocatorUtilities.enableImmediateScope(locator2);
        
        ImmediateController controller2 = locator2.getService(ImmediateController.class);
        Assert.assertEquals(ImmediateServiceState.RUNNING, controller2.getImmediateState());
    }
    
    /**
     * Tests setting and getting the state
     * 
     * @throws InterruptedException 
     */
    @Test(expected=IllegalArgumentException.class)
    public void testBadDecayValue() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        ImmediateController controller = ServiceLocatorUtilities.enableImmediateScopeSuspended(locator);
        
        controller.setThreadInactivityTimeout(-13);
    }
    
    private final static Object sLock = new Object();
    private static long immediateTid = -1;
    
    /* package */ static void registerTid(long tid) {
        synchronized (sLock) {
            immediateTid = tid;
            sLock.notifyAll();
        }
    }
    
    private static long waitForTid(long waitTime) throws InterruptedException {
        synchronized (sLock) {
            while (immediateTid == -1 && waitTime > 0) {
                long elapsedTime = System.currentTimeMillis();
                sLock.wait(waitTime);
                elapsedTime = System.currentTimeMillis() - elapsedTime;
                waitTime -= elapsedTime;
            }
            
            return immediateTid;
        }
        
        
    }
    
    private static void clearTid() {
        synchronized (sLock) {
            immediateTid = -1;
        }
    }
    
    private static class SimpleThreadFactory implements ThreadFactory {

        /* (non-Javadoc)
         * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
         */
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
        
    }
    
    private static class FailingThreadFactory implements ThreadFactory {
        private boolean didTryToStartOne = false;

        /* (non-Javadoc)
         * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
         */
        @Override
        public Thread newThread(Runnable r) {
            synchronized (this) {
                didTryToStartOne = true;
                this.notifyAll();
            }
            throw new AssertionError("Should never be called");
        }
        
        public boolean didTryToStartOne(long waitTime) throws InterruptedException {
            synchronized (this) {
                while (!didTryToStartOne && waitTime > 0) {
                    long elapsedTime = System.currentTimeMillis();
                    
                    this.wait(waitTime);
                    
                    elapsedTime = System.currentTimeMillis() - elapsedTime;
                    waitTime -= elapsedTime;
                }
                
                return didTryToStartOne;
            }
        }
        
    }

}
