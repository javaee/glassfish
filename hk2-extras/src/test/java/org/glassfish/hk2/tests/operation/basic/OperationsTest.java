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
package org.glassfish.hk2.tests.operation.basic;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extras.ExtrasUtilities;
import org.glassfish.hk2.extras.operation.OperationHandle;
import org.glassfish.hk2.extras.operation.OperationManager;
import org.glassfish.hk2.extras.operation.OperationState;
import org.glassfish.hk2.tests.extras.internal.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class OperationsTest {
    private final static Annotation BASIC_OPERATION_ANNOTATION = new BasicOperationScopeImpl();
    private final static Annotation SECONDARY_OPERATION_ANNOTATION = new SecondaryOperationScopeImpl();
    
    private final static String ALICE_NM = "Alice";
    private final static byte[] ALICE_PW = { 1, 2 };
    private final static String BOB_NM = "Bob";
    private final static byte[] BOB_PW = { 3, 4 };
    
    private final static OperationUser ALICE = new OperationUser() {

        @Override
        public String getName() {
            return ALICE_NM;
        }

        @Override
        public byte[] getPassword() {
            return ALICE_PW;
        }
        
    };
    
    private final static OperationUser BOB = new OperationUser() {

        @Override
        public String getName() {
            return BOB_NM;
        }

        @Override
        public byte[] getPassword() {
            return BOB_PW;
        }
        
    };
    
    private final static long FIRST_ID = 1;
    private final static long SECOND_ID = 2;
    
    private static ServiceLocator createLocator(Class<?>... clazzes) {
        ServiceLocator locator = Utilities.getUniqueLocator(clazzes);
        ExtrasUtilities.enableOperations(locator);
        
        return locator;
    }
    
    /**
     * Tests that operations can be properly swapped on a single thread
     */
    @Test // @org.junit.Ignore
    public void testChangeOperationOnSameThread() {
        ServiceLocator locator = createLocator(BasicOperationScopeContext.class,
                OperationUserFactory.class, SingletonThatUsesOperationService.class);
        
        OperationManager operationManager = locator.getService(OperationManager.class);
        
        OperationHandle aliceOperation = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        aliceOperation.setOperationData(ALICE);
        
        OperationHandle bobOperation = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        bobOperation.setOperationData(BOB);
        
        SingletonThatUsesOperationService singleton = locator.getService(SingletonThatUsesOperationService.class);
        
        // Start ALICE operation
        aliceOperation.resume();
        
        Assert.assertEquals(ALICE_NM, singleton.getCurrentUserName());
        
        // suspend ALICE and start BOB
        aliceOperation.suspend();
        bobOperation.resume();
        
        Assert.assertEquals(BOB_NM, singleton.getCurrentUserName());
        
        // Clean up
        aliceOperation.closeOperation();
        bobOperation.closeOperation();
    }
    
    /**
     * Tests that operations can be properly swapped on a single thread
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testOperationsActiveOnTwoThreads() throws InterruptedException {
        ServiceLocator locator = createLocator(BasicOperationScopeContext.class,
                OperationUserFactory.class, SingletonThatUsesOperationService.class);
        
        OperationManager operationManager = locator.getService(OperationManager.class);
        
        OperationHandle aliceOperation = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        aliceOperation.setOperationData(ALICE);
        
        OperationHandle bobOperation = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        bobOperation.setOperationData(BOB);
        
        SingletonThatUsesOperationService singleton = locator.getService(SingletonThatUsesOperationService.class);
        
        SimpleThreadedFetcher aliceFetcher = new SimpleThreadedFetcher(aliceOperation, singleton);
        Thread aliceThread = new Thread(aliceFetcher);
        
        SimpleThreadedFetcher bobFetcher = new SimpleThreadedFetcher(bobOperation, singleton);
        Thread bobThread = new Thread(bobFetcher);
        
        aliceThread.start();
        bobThread.start();
        
        // Gives both threads time for both to get inside operation
        Thread.sleep(100);
        
        aliceFetcher.go();
        bobFetcher.go();
        
        Assert.assertEquals(ALICE_NM, aliceFetcher.waitForResult());
        Assert.assertEquals(BOB_NM, bobFetcher.waitForResult());
        
        // Clean up
        aliceOperation.closeOperation();
        bobOperation.closeOperation();
    }
    
    /**
     * Tests that operations can be properly swapped on a single thread
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testCheckState() throws InterruptedException {
        ServiceLocator locator = createLocator(BasicOperationScopeContext.class,
                OperationUserFactory.class, SingletonThatUsesOperationService.class);
        
        OperationManager operationManager = locator.getService(OperationManager.class);
        
        OperationHandle operation = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        
        Assert.assertEquals(OperationState.SUSPENDED, operation.getState());
        
        Thread t1 = new Thread();
        Thread t2 = new Thread();
        
        operation.resume(t1.getId());
        
        Assert.assertEquals(OperationState.ACTIVE, operation.getState());
        
        operation.resume(t2.getId());
        
        Assert.assertEquals(OperationState.ACTIVE, operation.getState());
        
        Set<Long> activeIds = operation.getActiveThreads();
        Assert.assertEquals(2, activeIds.size());
        
        Assert.assertTrue(activeIds.contains(new Long(t1.getId())));
        Assert.assertTrue(activeIds.contains(new Long(t2.getId())));
        
        operation.suspend(t1.getId());
        
        Assert.assertEquals(OperationState.ACTIVE, operation.getState());
        
        activeIds = operation.getActiveThreads();
        Assert.assertEquals(1, activeIds.size());
        
        Assert.assertTrue(activeIds.contains(new Long(t2.getId())));
        
        // suspend t1 again, make sure nothing bad happens
        operation.suspend(t1.getId());
        
        Assert.assertEquals(OperationState.ACTIVE, operation.getState());
        
        activeIds = operation.getActiveThreads();
        Assert.assertEquals(1, activeIds.size());
        
        Assert.assertTrue(activeIds.contains(new Long(t2.getId())));
        
        // Now suspend t2, make sure we go to SUSPENDED
        operation.suspend(t2.getId());
        
        Assert.assertEquals(OperationState.SUSPENDED, operation.getState());
        
        activeIds = operation.getActiveThreads();
        Assert.assertEquals(0, activeIds.size());
        
        operation.closeOperation();
        
        Assert.assertEquals(OperationState.CLOSED, operation.getState());
        
        try {
            operation.resume(t1.getId());
            Assert.fail("Should not have been able to resume a closed operation");
        }
        catch (IllegalStateException ise) {
        }
        
        // Should do nothing
        operation.suspend(t1.getId());
        
        Assert.assertEquals(OperationState.CLOSED, operation.getState());
        
        activeIds = operation.getActiveThreads();
        Assert.assertEquals(0, activeIds.size());
    }
    
    /**
     * Tests that operations can be properly swapped on a single thread
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testDoubleResume() throws InterruptedException {
        ServiceLocator locator = createLocator(BasicOperationScopeContext.class,
                OperationUserFactory.class, SingletonThatUsesOperationService.class);
        
        OperationManager operationManager = locator.getService(OperationManager.class);
        
        OperationHandle operation = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        
        Assert.assertEquals(OperationState.SUSPENDED, operation.getState());
        
        operation.resume(Thread.currentThread().getId());
        
        Assert.assertEquals(OperationState.ACTIVE, operation.getState());
        
        operation.resume(Thread.currentThread().getId());
        
        Assert.assertEquals(OperationState.ACTIVE, operation.getState());
        
        operation.closeOperation();
        
        Assert.assertEquals(OperationState.CLOSED, operation.getState());
    }
    
    /**
     * Tests that operations can be properly swapped on a single thread
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testResumeOfSecondOperationSameThreadFails() throws InterruptedException {
        ServiceLocator locator = createLocator(BasicOperationScopeContext.class,
                OperationUserFactory.class, SingletonThatUsesOperationService.class);
        
        OperationManager operationManager = locator.getService(OperationManager.class);
        
        OperationHandle operation1 = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        OperationHandle operation2 = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        
        operation1.resume();
        
        try {
            operation2.resume();
            Assert.fail("Should not have been able to resume second operation on thread first operation already had");
        }
        catch (IllegalStateException ise) {
            // expected
        }
        
        operation1.closeOperation();
        
        // Make sure we can do it later after the first operation has gone away
        operation2.resume();
        
        operation2.closeOperation();
    }
    
    /**
     * Tests that operations can be properly swapped on a single thread
     * @throws InterruptedException 
     */
    @Test // @org.junit.Ignore
    public void testUseOperationsInHashSet() throws InterruptedException {
        ServiceLocator locator = createLocator(BasicOperationScopeContext.class,
                OperationUserFactory.class, SingletonThatUsesOperationService.class);
        
        OperationManager operationManager = locator.getService(OperationManager.class);
        
        OperationHandle operation1 = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        OperationHandle operation2 = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        
        HashSet<OperationHandle> storage = new HashSet<OperationHandle>();
        
        storage.add(operation1);
        storage.add(operation2);
        
        Assert.assertTrue(storage.contains(operation2));
        Assert.assertTrue(storage.contains(operation1));
        
        Assert.assertFalse(operation1.equals(null));
        Assert.assertFalse(operation1.equals(operationManager));
        Assert.assertFalse(operation1.equals(operation2));
        Assert.assertTrue(operation1.equals(operation1));
    }
    
    /**
     * Tests that a service in operation scope where there is
     * no operation scope on the thread fails, and that we
     * can put an operation on the thread and then it works ok
     */
    @Test // @org.junit.Ignore
    public void testNoOperationOnThread() {
        ServiceLocator locator = createLocator(BasicOperationScopeContext.class,
                OperationUserFactory.class, SingletonThatUsesOperationService.class);
        
        OperationManager operationManager = locator.getService(OperationManager.class);
        
        OperationHandle aliceOperation = operationManager.createAndStartOperation(BASIC_OPERATION_ANNOTATION);
        aliceOperation.setOperationData(ALICE);
        
        SingletonThatUsesOperationService singleton = locator.getService(SingletonThatUsesOperationService.class);
        
        Assert.assertEquals(ALICE_NM, singleton.getCurrentUserName());
        
        // suspend ALICE and start BOB
        aliceOperation.suspend();
        
        try {
            singleton.getCurrentUserName();
            Assert.fail("Should not have been able to call method as there is no operation on the thread");
        }
        catch (IllegalStateException ise) {
            // Expected
        }
        
        aliceOperation.resume();
        
        Assert.assertEquals(ALICE_NM, singleton.getCurrentUserName());
        
        // Clean up
        aliceOperation.closeOperation();
        
    }
    
    /**
     * Tests that two operations of different types can be
     * on the same thread
     */
    @Test // @org.junit.Ignore
    public void testTwoOperationsDifferentTypesOnSameThread() {
        ServiceLocator locator = createLocator(BasicOperationScopeContext.class,
                OperationUserFactory.class,
                SecondaryOperationScopeContext.class,
                SingletonThatUsesBothOperationTypes.class,
                SecondaryData.class);
        
        OperationManager operationManager = locator.getService(OperationManager.class);
        
        OperationHandle aliceOperation = operationManager.createAndStartOperation(BASIC_OPERATION_ANNOTATION);
        aliceOperation.setOperationData(ALICE);
        
        OperationHandle bobOperation = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        bobOperation.setOperationData(BOB);
        
        OperationHandle firstOperation = operationManager.createOperation(SECONDARY_OPERATION_ANNOTATION);
        OperationHandle secondOperation = operationManager.createOperation(SECONDARY_OPERATION_ANNOTATION);
        
        firstOperation.resume();
        
        SecondaryData firstData = locator.getService(SecondaryData.class);
        firstData.setId(FIRST_ID);
        
        firstOperation.suspend();
        secondOperation.resume();
        
        SecondaryData secondData = locator.getService(SecondaryData.class);
        secondData.setId(SECOND_ID);
        
        SingletonThatUsesBothOperationTypes singleton = locator.getService(SingletonThatUsesBothOperationTypes.class);
        
        Assert.assertEquals(ALICE_NM, singleton.getCurrentUserName());
        
        secondOperation.suspend();
        firstOperation.resume();
        
        Assert.assertEquals(FIRST_ID, singleton.getCurrentSecondaryId());
        
        aliceOperation.suspend();
        bobOperation.resume();
        
        Assert.assertEquals(BOB_NM, singleton.getCurrentUserName());
        
        firstOperation.suspend();
        secondOperation.resume();
        
        Assert.assertEquals(SECOND_ID, singleton.getCurrentSecondaryId());
        
        // Clean up
        aliceOperation.closeOperation();
        bobOperation.closeOperation();
        firstOperation.closeOperation();
        secondOperation.closeOperation();
        
    }
    
    /**
     * SecondaryOperationScope allows null returns, so lets test it
     */
    @Test // @org.junit.Ignore
    public void testOperationWhichAllowsNullAllowsNull() {
        ServiceLocator locator = createLocator(
                SecondaryOperationScopeContext.class,
                PerLookupThatUsesNullMeService.class,
                NullMeFactory.class);
        
        OperationManager operationManager = locator.getService(OperationManager.class);
        
        OperationHandle firstOperation = operationManager.createAndStartOperation(SECONDARY_OPERATION_ANNOTATION);
        
        firstOperation.resume();
        
        PerLookupThatUsesNullMeService usesNullMe = locator.getService(PerLookupThatUsesNullMeService.class);
        
        Assert.assertTrue(usesNullMe.isNullMeNull());
        
        // Clean up
        firstOperation.closeOperation();
    }
    
    private static class SimpleThreadedFetcher implements Runnable {
        private final OperationHandle operation;
        private final SingletonThatUsesOperationService singleton;
        private boolean go = false;
        private String retVal;
        
        private SimpleThreadedFetcher(OperationHandle operation, SingletonThatUsesOperationService singleton) {
            this.operation = operation;
            this.singleton = singleton;
        }
        
        private void go() {
            synchronized (this) {
                go = true;
                this.notifyAll();
            }
        }
        
        private void waitForGo() {
            synchronized (this) {
                long waitTime = 20 * 1000;
                while (waitTime > 0L && !go) {
                    long elapsedTime = System.currentTimeMillis();
                    try {
                        this.wait(20 * 1000);
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    elapsedTime = System.currentTimeMillis() - elapsedTime;
                    waitTime -= elapsedTime;
                }
                
                if (!go) {
                    Assert.fail("Did not get go signal within 20 seconds");
                }
            }
        }
        
        private String waitForResult() {
            synchronized (this) {
                long waitTime = 20 * 1000;
                while (waitTime > 0L && retVal == null) {
                    long elapsedTime = System.currentTimeMillis();
                    try {
                        this.wait(20 * 1000);
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    elapsedTime = System.currentTimeMillis() - elapsedTime;
                    waitTime -= elapsedTime;
                }
                
                if (retVal == null) {
                    Assert.fail("Did not get result signal within 20 seconds");
                }
                
                return retVal;
            }
        }
        
        private void setResult(String result) {
            synchronized (this) {
                retVal = result;
                this.notifyAll();
            }
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            operation.resume();
            try {
                waitForGo();
                
                setResult(singleton.getCurrentUserName());
            }
            finally {
                operation.suspend();
            }
            
        }
        
    }

}
