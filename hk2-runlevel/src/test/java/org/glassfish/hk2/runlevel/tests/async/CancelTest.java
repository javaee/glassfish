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

package org.glassfish.hk2.runlevel.tests.async;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.tests.utilities.Utilities;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hk2.annotations.Service;

/**
 * Tests various cancel operations
 * 
 * @author jwells
 *
 */
public class CancelTest {
    private final static int ZERO = 0;
    
    private final static int ONE = 1;
    private final static String SERVICE_ONE = "Service1";
    private final static String SERVICE_ONE_DOWN = "Service1_Down";
    
    private final static int TWO = 2;
    private final static String SERVICE_TWO_ONE = "Service2_1";
    private final static String SERVICE_TWO_TWO = "Service2_2";
    private final static String SERVICE_TWO_THREE = "Service2_3";
    
    private final static String SERVICE_TWO_ONE_DOWN = "Service2_1_Down";
    private final static String SERVICE_TWO_TWO_DOWN = "Service2_2_Down";
    private final static String SERVICE_TWO_THREE_DOWN = "Service2_3_Down";
    
    private final static int THREE = 3;
    private final static String SERVICE_THREE = "Service3";
    private final static String SERVICE_THREE_DOWN = "Service3_Down";
    
    private final static Object lock = new Object();
    private static boolean pleaseCancelNow = false;
    private static boolean haveCancelled = false;
    
    /**
     * Test basic cancellation going up
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Test
    public void testCancelUp() throws InterruptedException, ExecutionException, TimeoutException {
        pleaseCancelNow = false;
        haveCancelled = false;
        
        ServiceLocator basicLocator = Utilities.getServiceLocator(
                UpRecorder.class,
                DownRecorder.class,
                Service1.class,
                Service2_1.class,
                Service2_2.class,
                Service2_3.class,
                Service3.class);
        
        UpRecorder upRecorder = basicLocator.getService(UpRecorder.class);
        Assert.assertNotNull(upRecorder);
        upRecorder.getRecordsAndPurge();
        
        DownRecorder downRecorder = basicLocator.getService(DownRecorder.class);
        Assert.assertNotNull(downRecorder);
        downRecorder.getRecordsAndPurge();
        
        RunLevelController controller = basicLocator.getService(RunLevelController.class);
        
        // With only two threads Service2_3 will not come up prior to be cancelled
        controller.setMaximumUseableThreads(1);
        
        RunLevelFuture future = controller.proceedToAsync(THREE);
        
        synchronized (lock) {
            while (!pleaseCancelNow) {
                lock.wait();
            }
        }
        
        future.cancel(false);
        
        synchronized (lock) {
            haveCancelled = true;
            lock.notify();
        }
        
        future.get(20, TimeUnit.SECONDS);
        
        Assert.assertEquals(1, controller.getCurrentRunLevel());
        Assert.assertTrue(future.isDone());
        Assert.assertTrue(future.isCancelled());
        
        // Now lets check the services that came up
        List<String> upRecords = upRecorder.getRecordsAndPurge();
        List<String> downRecords = downRecorder.getRecordsAndPurge();
        
        Assert.assertEquals(3, upRecords.size());
        
        Assert.assertEquals(SERVICE_ONE, upRecords.get(0));
        Assert.assertEquals(SERVICE_TWO_ONE, upRecords.get(1));
        Assert.assertEquals(SERVICE_TWO_TWO, upRecords.get(2));
        
        Assert.assertEquals(2, downRecords.size());
        
        Assert.assertEquals(SERVICE_TWO_TWO, downRecords.get(0));
        Assert.assertEquals(SERVICE_TWO_ONE, downRecords.get(1));
    }
    
    /**
     * Tests basic cancellation in the down direction, and also tests a listener
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Test
    public void testCancelDown() throws InterruptedException, ExecutionException, TimeoutException {
        pleaseCancelNow = false;
        haveCancelled = false;
        
        ServiceLocator basicLocator = Utilities.getServiceLocator(
                UpRecorder.class,
                DownRecorder.class,
                Service1_Down.class,
                Service2_1_Down.class,
                Service2_2_Down.class,
                Service2_3_Down.class,
                Service3_Down.class,
                CancelledListener.class);
        
        CancelledListener cancelledListener = basicLocator.getService(CancelledListener.class);
        Assert.assertNotNull(cancelledListener);
        cancelledListener.getAndPurgeCancelledLevels();
        
        UpRecorder upRecorder = basicLocator.getService(UpRecorder.class);
        Assert.assertNotNull(upRecorder);
        upRecorder.getRecordsAndPurge();
        
        DownRecorder downRecorder = basicLocator.getService(DownRecorder.class);
        Assert.assertNotNull(downRecorder);
        downRecorder.getRecordsAndPurge();
        
        RunLevelController controller = basicLocator.getService(RunLevelController.class);
        
        RunLevelFuture future = controller.proceedToAsync(THREE);
        future.get();  // Assumption is that up works fine
        
        Assert.assertTrue(future.isDone());
        
        future = controller.proceedToAsync(ZERO);
        
        synchronized (lock) {
            while (!pleaseCancelNow) {
                lock.wait();
            }
        }
        
        future.cancel(false);
        
        synchronized (lock) {
            haveCancelled = true;
            lock.notify();
        }
        
        future.get(60, TimeUnit.SECONDS);
        
        Assert.assertEquals(1, controller.getCurrentRunLevel());
        Assert.assertTrue(future.isDone());
        Assert.assertTrue(future.isCancelled());
        
        // Now lets check the services that came up
        List<String> upRecords = upRecorder.getRecordsAndPurge();
        List<String> downRecords = downRecorder.getRecordsAndPurge();
        
        Assert.assertEquals(5, upRecords.size());
        
        Assert.assertEquals(SERVICE_ONE_DOWN, upRecords.get(0));
        Assert.assertEquals(SERVICE_TWO_ONE_DOWN, upRecords.get(1));
        Assert.assertEquals(SERVICE_TWO_TWO_DOWN, upRecords.get(2));
        Assert.assertEquals(SERVICE_TWO_THREE_DOWN, upRecords.get(3));
        Assert.assertEquals(SERVICE_THREE_DOWN, upRecords.get(4));
        
        Assert.assertEquals(4, downRecords.size());
        
        Assert.assertEquals(SERVICE_THREE_DOWN, downRecords.get(0));
        Assert.assertEquals(SERVICE_TWO_THREE_DOWN, downRecords.get(1));
        Assert.assertEquals(SERVICE_TWO_TWO_DOWN, downRecords.get(2));
        Assert.assertEquals(SERVICE_TWO_ONE_DOWN, downRecords.get(3));
        
        List<Integer> cancelledLevels = cancelledListener.getAndPurgeCancelledLevels();
        Assert.assertNotNull(cancelledLevels);
        Assert.assertEquals(1, cancelledLevels.size());
        Assert.assertEquals(1, cancelledLevels.get(0).intValue());
    }
    
    /**
     * Test basic cancellation going up is properly notified to the listeners
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Test
    public void testCancelUpListener() throws InterruptedException, ExecutionException, TimeoutException {
        pleaseCancelNow = false;
        haveCancelled = false;
        
        ServiceLocator basicLocator = Utilities.getServiceLocator(
                UpRecorder.class,
                DownRecorder.class,
                Service1.class,
                Service2_1.class,
                Service2_2.class,
                Service2_3.class,
                Service3.class,
                CancelledListener.class);
        
        CancelledListener cancelledListener = basicLocator.getService(CancelledListener.class);
        Assert.assertNotNull(cancelledListener);
        cancelledListener.getAndPurgeCancelledLevels();
        
        RunLevelController controller = basicLocator.getService(RunLevelController.class);
        
        RunLevelFuture future = controller.proceedToAsync(THREE);
        
        synchronized (lock) {
            while (!pleaseCancelNow) {
                lock.wait();
            }
        }
        
        future.cancel(false);
        
        synchronized (lock) {
            haveCancelled = true;
            lock.notify();
        }
        
        future.get(20, TimeUnit.SECONDS);
        
        Assert.assertEquals(1, controller.getCurrentRunLevel());
        Assert.assertTrue(future.isDone());
        Assert.assertTrue(future.isCancelled());
        
        List<Integer> cancelled = cancelledListener.getAndPurgeCancelledLevels();
        Assert.assertEquals(1, cancelled.size());
        
        Assert.assertEquals(1, cancelled.get(0).intValue());
    }
    
    @RunLevel(ONE) @Service
    public static class Service1 extends AbstractRunLevelService {

        @Override
        public String getServiceName() {
            return SERVICE_ONE;
        }
        
    }
    
    @RunLevel(TWO) @Service
    public static class Service2_1 extends AbstractRunLevelService {

        @Override
        public String getServiceName() {
            return SERVICE_TWO_ONE;
        }
        
    }
    
    @RunLevel(TWO) @Service
    public static class Service2_2 extends AbstractRunLevelService {
        @SuppressWarnings("unused")
        @Inject
        private Service2_1 dependency;
        
        @PostConstruct
        public void postConstruct() {
            synchronized (lock) {
                pleaseCancelNow = true;
                lock.notify();
                
                while (!haveCancelled) {
                    try {
                        lock.wait();
                    }
                    catch (InterruptedException e) {
                        throw new AssertionError(e);
                    }
                }
            }
            
            super.postConstruct();
        }

        @Override
        public String getServiceName() {
            return SERVICE_TWO_TWO;
        }
        
    }
    
    @RunLevel(TWO) @Service
    public static class Service2_3 extends AbstractRunLevelService {
        @SuppressWarnings("unused")
        @Inject
        private Service2_2 dependency;

        @Override
        public String getServiceName() {
            return SERVICE_TWO_THREE;
        }
        
    }
    
    @RunLevel(THREE) @Service
    public static class Service3 extends AbstractRunLevelService {

        @Override
        public String getServiceName() {
            return SERVICE_THREE;
        }
        
    }
    
    @Service
    public static class CancelledListener extends AbstractRunLevelListener {
        
    }
    
    @RunLevel(ONE) @Service
    public static class Service1_Down extends AbstractRunLevelService {

        @Override
        public String getServiceName() {
            return SERVICE_ONE_DOWN;
        }
        
    }
    
    @RunLevel(TWO) @Service
    public static class Service2_1_Down extends AbstractRunLevelService {

        @Override
        public String getServiceName() {
            return SERVICE_TWO_ONE_DOWN;
        }
        
    }
    
    @RunLevel(TWO) @Service
    public static class Service2_2_Down extends AbstractRunLevelService {
        @SuppressWarnings("unused")
        @Inject
        private Service2_1_Down dependency;
        
        @PreDestroy
        public void preDestroy() {
            synchronized (lock) {
                pleaseCancelNow = true;
                lock.notify();
                
                while (!haveCancelled) {
                    try {
                        lock.wait();
                    }
                    catch (InterruptedException e) {
                        throw new AssertionError(e);
                    }
                }
            }
            
            super.preDestroy();
        }

        @Override
        public String getServiceName() {
            return SERVICE_TWO_TWO_DOWN;
        }
        
    }
    
    @RunLevel(TWO) @Service
    public static class Service2_3_Down extends AbstractRunLevelService {
        @SuppressWarnings("unused")
        @Inject
        private Service2_2_Down dependency;

        @Override
        public String getServiceName() {
            return SERVICE_TWO_THREE_DOWN;
        }
        
    }
    
    @RunLevel(THREE) @Service
    public static class Service3_Down extends AbstractRunLevelService {

        @Override
        public String getServiceName() {
            return SERVICE_THREE_DOWN;
        }
        
    }

}
