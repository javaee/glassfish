/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.runlevel.tests.async;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.tests.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hk2.annotations.Service;

/**
 * @author jwells
 *
 */
public class AsyncRLSTest {
    public static final int ZERO = 0;
    
    public static final int FIVE = 5;
    public static final String LEVEL_FIVE_1 = "5.1";
    public static final String LEVEL_FIVE_2 = "5.2";
    
    public static final int TEN = 10;
    public static final String LEVEL_TEN_1 = "10.1";
    
    public static final int FIFTEEN = 15;
    public static final String LEVEL_FIFTEEN_1 = "15.1";
    public static final String LEVEL_FIFTEEN_2 = "15.2";
    
    public static final int TWENTY = 20;
    public static final String LEVEL_TWENTY_1 = "20.1";
    
    public static final int ONE = 1;
    public static final String ASYNC_ONE = "Async.1";
    
    public static final String SERVICE_A = "ServiceA";
    public static final String DEPENDS_ON_SERVICE_A = "DependsOnServiceA";
    
    /**
     * Tests your basic up and down with no listeners
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    public void testUpToTwentyAndDownToZero() throws
        ExecutionException, InterruptedException, TimeoutException {
        ServiceLocator basicLocator = Utilities.getServiceLocator("AsyncRLSTest.basic1",
                UpRecorder.class,
                DownRecorder.class,
                LevelFiveService_1.class,
                LevelFiveService_2.class,
                LevelTenService_1.class,
                LevelFifteenService_1.class,
                LevelFifteenService_2.class,
                LevelTwentyService_1.class);
        
        UpRecorder upRecorder = basicLocator.getService(UpRecorder.class);
        Assert.assertNotNull(upRecorder);
        upRecorder.getRecordsAndPurge();
        
        DownRecorder downRecorder = basicLocator.getService(DownRecorder.class);
        Assert.assertNotNull(downRecorder);
        downRecorder.getRecordsAndPurge();
        
        RunLevelController controller = basicLocator.getService(RunLevelController.class);
        
        RunLevelFuture future = controller.proceedToAsync(TWENTY);
        future.get(20, TimeUnit.SECONDS);
        
        List<String> records = upRecorder.getRecordsAndPurge();
        Assert.assertEquals(6, records.size());
        
        String recordA = records.get(0);
        String recordB = records.get(1);
        
        Assert.assertNotSame(recordA, recordB);
        Assert.assertTrue(recordA.equals(LEVEL_FIVE_1) || recordA.equals(LEVEL_FIVE_2));
        Assert.assertTrue(recordB.equals(LEVEL_FIVE_1) || recordB.equals(LEVEL_FIVE_2));
        
        recordA = records.get(2);
        
        Assert.assertEquals(LEVEL_TEN_1, recordA);
        
        recordA = records.get(3);
        recordB = records.get(4);
        
        Assert.assertNotSame(recordA, recordB);
        Assert.assertTrue(recordA.equals(LEVEL_FIFTEEN_1) || recordA.equals(LEVEL_FIFTEEN_2));
        Assert.assertTrue(recordB.equals(LEVEL_FIFTEEN_1) || recordB.equals(LEVEL_FIFTEEN_2));
        
        recordA = records.get(5);
        Assert.assertEquals(LEVEL_TWENTY_1, recordA);
        
        future = controller.proceedToAsync(ZERO);
        future.get(20, TimeUnit.SECONDS);
        
        List<String> recordsDown = downRecorder.getRecordsAndPurge();
        Assert.assertNotNull(recordsDown);
        Assert.assertEquals(6, recordsDown.size());
        
        // Down is ORDERED (unlike up) and must happen in the opposite of the up order
        Assert.assertEquals(records.get(5), recordsDown.get(0));
        if (records.get(4).equals(recordsDown.get(1))) {
        	Assert.assertEquals(records.get(3), recordsDown.get(2));
        }
        else if (records.get(4).equals(recordsDown.get(2))) {
        	Assert.assertEquals(records.get(3), recordsDown.get(1));
        }
        else {
        	Assert.fail("records.get(4)=" + records.get(4) +
        			" recordsDown.get(1)=" + recordsDown.get(1) +
        			" recordsDown.get(2)=" + recordsDown.get(2));
        }
        Assert.assertEquals(records.get(2), recordsDown.get(3));
        
        if (records.get(1).equals(recordsDown.get(4))) {
        	Assert.assertEquals(records.get(0), recordsDown.get(5));
        }
        else if (records.get(1).equals(recordsDown.get(5))) {
        	Assert.assertEquals(records.get(0), recordsDown.get(4));
        }
        else {
        	Assert.fail("records.get(1)=" + records.get(1) +
        			" recordsDown.get(4)=" + recordsDown.get(4) +
        			" recordsDown.get(5)=" + recordsDown.get(5));
        }
    }
    
    /**
     * Tests going up and down part way and up part way then
     * all the way down and then back up etc
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    public void testYoYo() throws
        ExecutionException, InterruptedException, TimeoutException {
        ServiceLocator basicLocator = Utilities.getServiceLocator("AsyncRLSTest.basicYoYo",
                UpRecorder.class,
                DownRecorder.class,
                LevelFiveService_1.class,
                LevelFiveService_2.class,
                LevelTenService_1.class,
                LevelFifteenService_1.class,
                LevelFifteenService_2.class,
                LevelTwentyService_1.class);
        
        UpRecorder upRecorder = basicLocator.getService(UpRecorder.class);
        Assert.assertNotNull(upRecorder);
        upRecorder.getRecordsAndPurge();
        
        DownRecorder downRecorder = basicLocator.getService(DownRecorder.class);
        Assert.assertNotNull(downRecorder);
        downRecorder.getRecordsAndPurge();
        
        RunLevelController controller = basicLocator.getService(RunLevelController.class);
        
        RunLevelFuture future = controller.proceedToAsync(TWENTY);
        future.get(20, TimeUnit.SECONDS);
        
        List<String> records = upRecorder.getRecordsAndPurge();
        Assert.assertEquals(6, records.size());
        
        String recordA = records.get(0);
        String recordB = records.get(1);
        
        Assert.assertNotSame(recordA, recordB);
        Assert.assertTrue(recordA.equals(LEVEL_FIVE_1) || recordA.equals(LEVEL_FIVE_2));
        Assert.assertTrue(recordB.equals(LEVEL_FIVE_1) || recordB.equals(LEVEL_FIVE_2));
        
        recordA = records.get(2);
        
        Assert.assertEquals(LEVEL_TEN_1, recordA);
        
        recordA = records.get(3);
        recordB = records.get(4);
        
        Assert.assertNotSame(recordA, recordB);
        Assert.assertTrue(recordA.equals(LEVEL_FIFTEEN_1) || recordA.equals(LEVEL_FIFTEEN_2));
        Assert.assertTrue(recordB.equals(LEVEL_FIFTEEN_1) || recordB.equals(LEVEL_FIFTEEN_2));
        
        recordA = records.get(5);
        Assert.assertEquals(LEVEL_TWENTY_1, recordA);
        
        future = controller.proceedToAsync(TEN);
        future.get(20, TimeUnit.SECONDS);
        
        List<String> recordsDown = downRecorder.getRecordsAndPurge();
        Assert.assertNotNull(recordsDown);
        Assert.assertEquals(3, recordsDown.size());
        
        // Down is ORDERED for related services, but can be unordered for services not related at all
        Assert.assertEquals(records.get(5), recordsDown.get(0));
        if (records.get(4).equals(recordsDown.get(1))) {
            Assert.assertEquals(records.get(3),recordsDown.get(2));
        }
        else if (records.get(4).equals(recordsDown.get(2))) {
            Assert.assertEquals(records.get(3), recordsDown.get(1));
        }
        else {
            Assert.fail("records(4) must be match either " + recordsDown.get(1) + " or " + recordsDown.get(2));
        }
        
        future = controller.proceedToAsync(FIFTEEN);
        future.get(20, TimeUnit.SECONDS);
        
        records = upRecorder.getRecordsAndPurge();
        Assert.assertEquals(2, records.size());
        
        recordA = records.get(0);
        recordB = records.get(1);
        
        Assert.assertNotSame(recordA, recordB);
        Assert.assertTrue(recordA.equals(LEVEL_FIFTEEN_1) || recordA.equals(LEVEL_FIFTEEN_2));
        Assert.assertTrue(recordB.equals(LEVEL_FIFTEEN_1) || recordB.equals(LEVEL_FIFTEEN_2));
        
        future = controller.proceedToAsync(FIVE);
        future.get(20, TimeUnit.SECONDS);
        
        recordsDown = downRecorder.getRecordsAndPurge();
        Assert.assertNotNull(recordsDown);
        Assert.assertEquals(3, recordsDown.size());
        
        future = controller.proceedToAsync(ZERO);
        future.get(20, TimeUnit.SECONDS);
        
        recordsDown = downRecorder.getRecordsAndPurge();
        Assert.assertNotNull(recordsDown);
        Assert.assertEquals(2, recordsDown.size());
    }
    
    /**
     * Tests that listeners work in the success case
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    public void testUpToTwentyAndDownToZeroWithListeners() throws
        ExecutionException, InterruptedException, TimeoutException {
        ServiceLocator basicLocator = Utilities.getServiceLocator("AsyncRLSTest.basic2",
                UpRecorder.class,
                DownRecorder.class,
                LevelFiveService_1.class,
                LevelFiveService_2.class,
                LevelTenService_1.class,
                LevelFifteenService_1.class,
                LevelFifteenService_2.class,
                LevelTwentyService_1.class,
                ListenerA.class,
                ListenerB.class);
        
        ListenerA listenerA = basicLocator.getService(ListenerA.class);
        ListenerB listenerB = basicLocator.getService(ListenerB.class);
        
        listenerA.getAndPurgeProgressedLevels();
        listenerB.getAndPurgeProgressedLevels();
        
        RunLevelController controller = basicLocator.getService(RunLevelController.class);
        
        RunLevelFuture future = controller.proceedToAsync(TWENTY);
        future.get(20, TimeUnit.SECONDS);
        
        List<Integer> proceedsA = listenerA.getAndPurgeProgressedLevels();
        List<Integer> proceedsB = listenerB.getAndPurgeProgressedLevels();
        
        Assert.assertEquals(22, proceedsA.size());
        Assert.assertEquals(22, proceedsB.size());
        
        for (int lcv = -1; lcv < 21; lcv++) {
            Assert.assertEquals(proceedsA.get(lcv + 1).intValue(), lcv);
        }
        
        for (int lcv = -1; lcv < 21; lcv++) {
            Assert.assertEquals(proceedsB.get(lcv + 1).intValue(), lcv);
        }
        
        future = controller.proceedToAsync(ZERO);
        future.get(20, TimeUnit.SECONDS);
        
        proceedsA = listenerA.getAndPurgeProgressedLevels();
        proceedsB = listenerB.getAndPurgeProgressedLevels();
        
        Assert.assertEquals(proceedsA.size(), 20);
        Assert.assertEquals(proceedsB.size(), 20);
        
        for (int lcv = 0; lcv < TWENTY; lcv++) {
            Assert.assertEquals(proceedsA.get(lcv).intValue(), (TWENTY - 1) - lcv);
        }
        
        for (int lcv = 0; lcv < TWENTY; lcv++) {
            Assert.assertEquals(proceedsB.get(lcv).intValue(), (TWENTY - 1) - lcv);
        }
    }
    
    /**
     * These services will not proceed until they are all in their
     * postConstructs
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    public void testUpAndDownWithAsyncServices() throws
        ExecutionException, InterruptedException, TimeoutException {
        ServiceLocator basicLocator = Utilities.getServiceLocator("AsyncRLSTest.basic3",
                UpRecorder.class,
                DownRecorder.class,
                AsyncService1_1.class,
                AsyncService1_2.class,
                AsyncService1_3.class,
                AsyncService1_4.class,
                AsyncService1_5.class);
                
        UpRecorder upRecorder = basicLocator.getService(UpRecorder.class);
        Assert.assertNotNull(upRecorder);
        upRecorder.getRecordsAndPurge();
        
        DownRecorder downRecorder = basicLocator.getService(DownRecorder.class);
        Assert.assertNotNull(downRecorder);
        downRecorder.getRecordsAndPurge();
        
        RunLevelController controller = basicLocator.getService(RunLevelController.class);
        
        RunLevelFuture future = controller.proceedToAsync(ONE);
        future.get(20, TimeUnit.SECONDS);
        
        List<String> records = upRecorder.getRecordsAndPurge();
        Assert.assertEquals(5, records.size());
        
        for (String record : records) {
            Assert.assertEquals(record, ASYNC_ONE);
        }
        
        future = controller.proceedToAsync(ZERO);
        future.get(20, TimeUnit.SECONDS);
        
        List<String> recordsDown = downRecorder.getRecordsAndPurge();
        Assert.assertNotNull(recordsDown);
        Assert.assertEquals(5, recordsDown.size());
        
        for (String record : records) {
            Assert.assertEquals(record, ASYNC_ONE);
        }
    }
    
    /**
     * These services can come up async but have a specific
     * dependeny order, so MUST come up in that order
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    public void testUpAndDownWithAsyncDependentServices() throws
        ExecutionException, InterruptedException, TimeoutException {
        ServiceLocator basicLocator = Utilities.getServiceLocator("AsyncRLSTest.basic4",
                UpRecorder.class,
                DownRecorder.class,
                DependsOnServiceA.class,
                ServiceA.class);
                
        UpRecorder upRecorder = basicLocator.getService(UpRecorder.class);
        Assert.assertNotNull(upRecorder);
        upRecorder.getRecordsAndPurge();
        
        DownRecorder downRecorder = basicLocator.getService(DownRecorder.class);
        Assert.assertNotNull(downRecorder);
        downRecorder.getRecordsAndPurge();
        
        RunLevelController controller = basicLocator.getService(RunLevelController.class);
        
        RunLevelFuture future = controller.proceedToAsync(ONE);
        future.get(20, TimeUnit.SECONDS);
        
        List<String> records = upRecorder.getRecordsAndPurge();
        Assert.assertEquals(2, records.size());
        
        Assert.assertEquals(SERVICE_A, records.get(0));
        Assert.assertEquals(DEPENDS_ON_SERVICE_A, records.get(1));
        
        ServiceA serviceA = basicLocator.getService(ServiceA.class);
        Assert.assertEquals(1, serviceA.getNumTimesConstructed());
        
        future = controller.proceedToAsync(ZERO);
        future.get(20, TimeUnit.SECONDS);
        
        List<String> recordsDown = downRecorder.getRecordsAndPurge();
        Assert.assertNotNull(recordsDown);
        Assert.assertEquals(2, recordsDown.size());
        
        Assert.assertEquals(DEPENDS_ON_SERVICE_A, recordsDown.get(0));
        Assert.assertEquals(SERVICE_A, recordsDown.get(1));
    }
    
    @Service @RunLevel(ONE) @Rank(100)
    public static class DependsOnServiceA extends AbstractRunLevelService {
        @SuppressWarnings("unused")
        @Inject
        private ServiceA serviceA;

        @Override
        public String getServiceName() {
            return DEPENDS_ON_SERVICE_A;
        }
    }
    
    @Service @RunLevel(ONE)
    public static class ServiceA extends AbstractRunLevelService {
        private int numTimesConstructed = 0;
        
        @PostConstruct
        public void postConstruct() {
            try {
                Thread.sleep(20);
            }
            catch (InterruptedException e) {
                throw new AssertionError(e);
            }
            
            synchronized (this) {
                numTimesConstructed++;
            }
            
            super.postConstruct();
        }
        
        public synchronized int getNumTimesConstructed() {
            return numTimesConstructed;
        }

        @Override
        public String getServiceName() {
            return SERVICE_A;
        }
        
    }
    
    @Service @RunLevel(ONE)
    public static class AsyncService1_1 extends LevelOneAsyncService {
    }
    
    @Service @RunLevel(ONE)
    public static class AsyncService1_2 extends LevelOneAsyncService {
    }
    
    @Service @RunLevel(ONE)
    public static class AsyncService1_3 extends LevelOneAsyncService {
    }
    
    @Service @RunLevel(ONE)
    public static class AsyncService1_4 extends LevelOneAsyncService {
    }
    
    @Service @RunLevel(ONE)
    public static class AsyncService1_5 extends LevelOneAsyncService {
    }
    
    public static class LevelOneAsyncService extends AbstractRunLevelService {
        private static final Object lock = new Object();
        private static int numServices = 0;
        
        @PostConstruct
        public void postConstruct() {
            synchronized (lock) {
                numServices++;
                
                if (numServices == FIVE) {
                    lock.notifyAll();
                }
                
                while (numServices < FIVE) {
                    try {
                        lock.wait();
                    }
                    catch (InterruptedException ie) {
                        throw new AssertionError(ie);
                    }
                }
            }
            
            super.postConstruct();
        }

        @Override
        public String getServiceName() {
            return ASYNC_ONE;
        }
    }
    

    @Service @RunLevel(FIVE)
    public static class LevelFiveService_1 extends AbstractRunLevelService {

        @Override
        public String getServiceName() {
            return LEVEL_FIVE_1;
        }
        
    }
    
    @Service @RunLevel(FIVE)
    public static class LevelFiveService_2 extends AbstractRunLevelService {

        @Override
        public String getServiceName() {
            return LEVEL_FIVE_2;
        }
        
    }
    
    @Service @RunLevel(TEN)
    public static class LevelTenService_1 extends AbstractRunLevelService {

        @Override
        public String getServiceName() {
            return LEVEL_TEN_1;
        }
        
    }
    
    @Service @RunLevel(FIFTEEN)
    public static class LevelFifteenService_1 extends AbstractRunLevelService {

        @Override
        public String getServiceName() {
            return LEVEL_FIFTEEN_1;
        }
        
    }
    
    @Service @RunLevel(FIFTEEN)
    public static class LevelFifteenService_2 extends AbstractRunLevelService {

        @Override
        public String getServiceName() {
            return LEVEL_FIFTEEN_2;
        }
        
    }
    
    @Service @RunLevel(TWENTY)
    public static class LevelTwentyService_1 extends AbstractRunLevelService {

        @Override
        public String getServiceName() {
            return LEVEL_TWENTY_1;
        }
        
        /**
         * This guy waits just a little bit to ensure that the waiting for the
         *  down side properly waits
         */
        @PreDestroy
        @Override
        public void preDestroy() {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                throw new AssertionError(e);
            }
            
            super.preDestroy();
        }
        
    }
}
