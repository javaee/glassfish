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
package org.glassfish.hk2.tests.locator.perthread;

import java.util.HashSet;

import junit.framework.Assert;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.PerThreadScopeModule;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class PerThreadTest {
    private final static String TEST_NAME = "PerThradTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new PerThreadModule());
    private final static int NUM_LOOKUPS = 10000;
    private final static int NUM_SHIRT_THREADS = 10;
    
    private final Object lock = new Object();
    private int numFinished = 0;
    private int shirtThreadsDone = 0;
    
    @Before
    public void before() {
        ServiceLocatorUtilities.enablePerThreadScope(locator);
        
        // Doing this twice ensures the idempotence of this call
        ServiceLocatorUtilities.enablePerThreadScope(locator);
    }
    
    /**
     * Tests we get different values per thread
     * 
     * @throws InterruptedException
     */
    @Test // @org.junit.Ignore
    public void testPerThread() throws InterruptedException {
        synchronized (lock) {
            numFinished = 0;
        }
        
        StoreRunner runner1 = new StoreRunner(locator);
        StoreRunner runner2 = new StoreRunner(locator);
        StoreRunner runner3 = new StoreRunner(locator);
        
        Thread thread1 = new Thread(runner1);
        Thread thread2 = new Thread(runner2);
        Thread thread3 = new Thread(runner3);
        
        thread1.start();
        thread2.start();
        thread3.start();
        
        synchronized (lock) {
            while (numFinished < 3) {
                lock.wait();
            }
        }
        
        ClothingStore store1 = runner1.store;
        ClothingStore store2 = runner2.store;
        ClothingStore store3 = runner3.store;
        
        Pants pants1 = store1.check();
        Pants pants2 = store2.check();
        Pants pants3 = store3.check();
        
        Assert.assertNotSame(pants1, pants2);
        Assert.assertNotSame(pants1, pants3);
        Assert.assertNotSame(pants2, pants3);
    }
    
    /**
     * Tests we get the same value perThread on multiple lookups
     * 
     * @throws InterruptedException
     */
    @Test // @org.junit.Ignore
    public void testSameValuePerThread() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.enablePerThreadScope(locator);
        ServiceLocatorUtilities.addClasses(locator, ShirtFactory.class);
        
        HashSet<Shirt> collector = new HashSet<Shirt>();
        
        Thread threads[] = new Thread[NUM_SHIRT_THREADS];
        for (int lcv = 0; lcv < NUM_SHIRT_THREADS; lcv++) {
            ShirtRunner runner = new ShirtRunner(locator, collector);
            
            threads[lcv] = new Thread(runner);
        }
        
        for (int lcv = 0; lcv < NUM_SHIRT_THREADS; lcv++) {
            threads[lcv].start();
        }
        
        synchronized (lock) {
            while (shirtThreadsDone < NUM_SHIRT_THREADS) {
                lock.wait();
            }
        }
        
        Assert.assertEquals(NUM_SHIRT_THREADS, collector.size());
    }
    
    /**
     * Tests we get different values per thread
     * 
     * @throws InterruptedException
     */
    @Test // @org.junit.Ignore
    public void testPerThreadWithModule() throws InterruptedException {
        synchronized (lock) {
            numFinished = 0;
        }
        
        ServiceLocator locator = ServiceLocatorUtilities.bind(new PerThreadScopeModule());
        ServiceLocatorUtilities.addClasses(locator, ClothingStore.class, Pants.class);
        
        StoreRunner runner1 = new StoreRunner(locator);
        StoreRunner runner2 = new StoreRunner(locator);
        StoreRunner runner3 = new StoreRunner(locator);
        
        Thread thread1 = new Thread(runner1);
        Thread thread2 = new Thread(runner2);
        Thread thread3 = new Thread(runner3);
        
        thread1.start();
        thread2.start();
        thread3.start();
        
        synchronized (lock) {
            while (numFinished < 3) {
                lock.wait();
            }
        }
        
        ClothingStore store1 = runner1.store;
        ClothingStore store2 = runner2.store;
        ClothingStore store3 = runner3.store;
        
        Pants pants1 = store1.check();
        Pants pants2 = store2.check();
        Pants pants3 = store3.check();
        
        Assert.assertNotSame(pants1, pants2);
        Assert.assertNotSame(pants1, pants3);
        Assert.assertNotSame(pants2, pants3);
    }
    
    private final static int NUM_MANY_THREADS = 100;
    
    /**
     * Tests a single extra thread but with a large number of children
     * service locators.  This test exhibits a memory leak in the
     * PerThreadContext since the children descriptors never leave
     * the map of the PerThreadContext
     * 
     * @throws InterruptedException
     */
    @Test // @org.junit.Ignore
    public void testManyChildLocatorsOneThread() throws InterruptedException {
        synchronized (lock) {
            numFinished = 0;
        }
        
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.bind(locator, new PerThreadScopeModule());
        
        Worker worker = new Worker();
        Thread t = new Thread(worker);
        t.start();
        
        try {
            for (int lcv = 0; lcv < NUM_MANY_THREADS; lcv++) {
                ServiceLocator child = LocatorHelper.create(locator);
                
                ServiceLocatorUtilities.enablePerThreadScope(child);
            
                ServiceLocatorUtilities.addClasses(child, Pants.class);
            
                worker.doJob(child);
            }
        }
        finally {
            worker.shutdown();
        }
        
    }
    
    /**
     * Tests a large number of threads
     * 
     * @throws InterruptedException
     */
    @Test // @org.junit.Ignore
    public void testManyThreads() throws InterruptedException {
        final ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.enablePerThreadScope(locator);
        ServiceLocatorUtilities.addClasses(locator, Pants.class);
        
        synchronized (lock) {
            numFinished = 0;
        }
        
        for (int lcv =  0; lcv < NUM_MANY_THREADS; lcv++) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    locator.getService(Pants.class);
                    synchronized (lock) {
                        numFinished++;
                        if (numFinished >= NUM_MANY_THREADS) {
                            lock.notify();
                        }
                    }
                }
            });
            
            thread.start();
        }
        
        synchronized (lock) {
            long totalWait = 20 * 1000;
            
            while (numFinished < NUM_MANY_THREADS && totalWait > 0) {
                long elapsedTime = System.currentTimeMillis();
                lock.wait();
                elapsedTime = System.currentTimeMillis() - elapsedTime;
                totalWait -= elapsedTime;
            }
            
            Assert.assertTrue(numFinished >= NUM_MANY_THREADS);
        }
    }
    
    public class StoreRunner implements Runnable {
        private final ServiceLocator locator;
        private ClothingStore store;
        
        private StoreRunner(ServiceLocator locator) {
            this.locator = locator;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            store = locator.getService(ClothingStore.class);
            
            synchronized (lock) {
                numFinished++;
                lock.notify();
            }
        }
        
    }
    
    public class ShirtRunner implements Runnable {
        private final ServiceLocator locator;
        private final HashSet<Shirt> collector;
        
        private ShirtRunner(ServiceLocator locator, HashSet<Shirt> collector) {
            this.locator = locator;
            this.collector = collector;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            for (int lcv = 0; lcv < NUM_LOOKUPS; lcv++) {
                Shirt shirt = locator.getService(Shirt.class);
                synchronized (collector) {
                    collector.add(shirt);
                }
            }
            
            synchronized (lock) {
                shirtThreadsDone++;
                lock.notify();
            }
        }
        
    }
    
    public static class Worker implements Runnable {
        private ServiceLocator nextJob;
        private boolean daylightCome = false;  // I want to go home
        private final Object lock = new Object();
        
        private void doJob(ServiceLocator job) {
            synchronized (lock) {
                while (nextJob != null) {
                    try {
                        lock.wait();
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                
                nextJob = job;
                lock.notifyAll();
            }
        }
        
        private void shutdown() {
            synchronized (lock) {
                daylightCome = true;
                lock.notifyAll();
            }
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            for (;;) {
                ServiceLocator currentJob = null;
                synchronized (lock) {
                    while (nextJob == null && !daylightCome) {
                        try {
                            lock.wait();
                        }
                        catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    
                    if (daylightCome) return;
                    
                    if (nextJob == null) continue;
                    
                    currentJob = nextJob;
                }
                
                // Now do the job
                Pants currentResult = currentJob.getService(Pants.class);
                Assert.assertNotNull(currentResult);
                
                synchronized (lock) {
                    nextJob = null;
                    lock.notifyAll();
                }
                
            }
            
        }
        
    }

}
