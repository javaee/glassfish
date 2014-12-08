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
package org.glassfish.hk2.tests.locator.inheritablethread;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import junit.framework.Assert;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.InheritableThreadScopeModule;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class InheritableThreadTest {

    private final static String TEST_NAME = "InheritableThreadTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new InheritableThreadModule());
    private final static int NUM_LOOKUPS = 10000;
    private final static int NUM_SHIRT_THREADS = 10;

    private final Object lock = new Object();
    private int numFinished = 0;
    private int shirtThreadsDone = 0;

    @Before
    public void before() {
        ServiceLocatorUtilities.enableInheritableThreadScope(locator);

        // Doing this twice ensures the idempotence of this call
        ServiceLocatorUtilities.enableInheritableThreadScope(locator);
    }

    /**
     * Tests we get different values per thread
     *
     * @throws InterruptedException
     */
    @Test // @org.junit.Ignore
    public void testInheritableThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(9);

        ParentRunner runner1 = new ParentRunner(locator, latch);
        ParentRunner runner2 = new ParentRunner(locator, latch);
        ParentRunner runner3 = new ParentRunner(locator, latch);

        Thread thread1 = new Thread(runner1);
        Thread thread2 = new Thread(runner2);
        Thread thread3 = new Thread(runner3);

        thread1.start();
        thread2.start();
        thread3.start();

        latch.await();

        BikeStore store1 = runner1.store;
        BikeStore store2 = runner2.store;
        BikeStore store3 = runner3.store;

        Bike bike1 = store1.check();
        Bike bike2 = store2.check();
        Bike bike3 = store3.check();

        Assert.assertNotSame(bike1, bike2);
        Assert.assertNotSame(bike1, bike3);
        Assert.assertNotSame(bike2, bike3);

        Bike store1Child1Bike = runner1.child1.store.check();
        Bike store1Child2Bike = runner1.child2.store.check();

        Assert.assertSame(bike1, store1Child1Bike);
        Assert.assertSame(bike1, store1Child2Bike);
        Assert.assertSame(store1Child1Bike, store1Child2Bike);

        Bike store2Child1Bike = runner2.child1.store.check();
        Bike store2Child2Bike = runner2.child2.store.check();

        Assert.assertSame(bike2, store2Child1Bike);
        Assert.assertSame(bike2, store2Child2Bike);
        Assert.assertSame(store2Child1Bike, store2Child2Bike);

        Bike store3Child1Bike = runner3.child1.store.check();
        Bike store3Child2Bike = runner3.child2.store.check();

        Assert.assertSame(bike3, store3Child1Bike);
        Assert.assertSame(bike3, store3Child2Bike);
        Assert.assertSame(store3Child1Bike, store3Child2Bike);

    }

    /**
     * Tests we get the same value inheritableThread on multiple lookups
     *
     * @throws InterruptedException
     */
    @Test // @org.junit.Ignore
    public void testSameValueInheritableThread() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.enableInheritableThreadScope(locator);
        ServiceLocatorUtilities.addClasses(locator, WheelFactory.class);

        HashSet<Wheel> collector = new HashSet<Wheel>();

        Thread threads[] = new Thread[NUM_SHIRT_THREADS];
        for (int lcv = 0; lcv < NUM_SHIRT_THREADS; lcv++) {
            WheelRunner runner = new WheelRunner(locator, collector);

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
     * Tests we get different values inheritable thread
     *
     * @throws InterruptedException
     */
    @Test // @org.junit.Ignore
    public void testInheritableThreadWithModule() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(9);

        ServiceLocator locator = ServiceLocatorUtilities.bind(new InheritableThreadScopeModule());
        ServiceLocatorUtilities.addClasses(locator, BikeStore.class, Bike.class);

        ParentRunner runner1 = new ParentRunner(locator, latch);
        ParentRunner runner2 = new ParentRunner(locator, latch);
        ParentRunner runner3 = new ParentRunner(locator, latch);

        Thread thread1 = new Thread(runner1);
        Thread thread2 = new Thread(runner2);
        Thread thread3 = new Thread(runner3);

        thread1.start();
        thread2.start();
        thread3.start();

        latch.await();

        BikeStore store1 = runner1.store;
        BikeStore store2 = runner2.store;
        BikeStore store3 = runner3.store;

        Bike bike1 = store1.check();
        Bike bike2 = store2.check();
        Bike bike3 = store3.check();

        Assert.assertNotSame(bike1, bike2);
        Assert.assertNotSame(bike1, bike3);
        Assert.assertNotSame(bike2, bike3);

        Bike store1Child1Bike = runner1.child1.store.check();
        Bike store1Child2Bike = runner1.child2.store.check();

        Assert.assertSame(bike1, store1Child1Bike);
        Assert.assertSame(bike1, store1Child2Bike);
        Assert.assertSame(store1Child1Bike, store1Child2Bike);

        Bike store2Child1Bike = runner2.child1.store.check();
        Bike store2Child2Bike = runner2.child2.store.check();

        Assert.assertSame(bike2, store2Child1Bike);
        Assert.assertSame(bike2, store2Child2Bike);
        Assert.assertSame(store2Child1Bike, store2Child2Bike);

        Bike store3Child1Bike = runner3.child1.store.check();
        Bike store3Child2Bike = runner3.child2.store.check();

        Assert.assertSame(bike3, store3Child1Bike);
        Assert.assertSame(bike3, store3Child2Bike);
        Assert.assertSame(store3Child1Bike, store3Child2Bike);
    }

    private final static int NUM_MANY_THREADS = 100;

    /**
     * Tests a single extra thread but with a large number of children service
     * locators. This test exhibits a memory leak in the
     * InheritableThreadContext since the children descriptors never leave the
     * map of the InheritableThreadContext
     *
     * @throws InterruptedException
     */
    @Test // @org.junit.Ignore
    public void testManyChildLocatorsOneThread() throws InterruptedException {
        synchronized (lock) {
            numFinished = 0;
        }

        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.bind(locator, new InheritableThreadScopeModule());

        Worker worker = new Worker();
        Thread t = new Thread(worker);
        t.start();

        try {
            for (int lcv = 0; lcv < NUM_MANY_THREADS; lcv++) {
                ServiceLocator child = LocatorHelper.create(locator);

                ServiceLocatorUtilities.enableInheritableThreadScope(child);

                ServiceLocatorUtilities.addClasses(child, Bike.class);

                worker.doJob(child);
            }
        } finally {
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
        ServiceLocatorUtilities.enableInheritableThreadScope(locator);
        ServiceLocatorUtilities.addClasses(locator, Bike.class);

        synchronized (lock) {
            numFinished = 0;
        }

        for (int lcv = 0; lcv < NUM_MANY_THREADS; lcv++) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    locator.getService(Bike.class);
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

    public class ParentRunner implements Runnable {

        private final ServiceLocator locator;
        private BikeStore store;
        private final CountDownLatch latch;
        private ChildRunnerRunner child1;
        private ChildRunnerRunner child2;

        private ParentRunner(ServiceLocator locator, CountDownLatch latch) {
            this.locator = locator;
            this.latch = latch;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            store = locator.getService(BikeStore.class);
            latch.countDown();
            child1 = new ChildRunnerRunner(locator, latch);
            child2 = new ChildRunnerRunner(locator, latch);

            new Thread(child1).start();
            new Thread(child2).start();

        }

    }

    public class ChildRunnerRunner implements Runnable {

        private final ServiceLocator locator;
        private BikeStore store;
        private final CountDownLatch latch;

        private ChildRunnerRunner(ServiceLocator locator, CountDownLatch latch) {
            this.locator = locator;
            this.latch = latch;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            store = locator.getService(BikeStore.class);
            latch.countDown();
        }

    }

    public class WheelRunner implements Runnable {

        private final ServiceLocator locator;
        private final HashSet<Wheel> collector;

        private WheelRunner(ServiceLocator locator, HashSet<Wheel> collector) {
            this.locator = locator;
            this.collector = collector;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            for (int lcv = 0; lcv < NUM_LOOKUPS; lcv++) {
                Wheel shirt = locator.getService(Wheel.class);
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
                    } catch (InterruptedException e) {
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
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (daylightCome) {
                        return;
                    }

                    if (nextJob == null) {
                        continue;
                    }

                    currentJob = nextJob;
                }

                // Now do the job
                Bike currentResult = currentJob.getService(Bike.class);
                Assert.assertNotNull(currentResult);

                synchronized (lock) {
                    nextJob = null;
                    lock.notifyAll();
                }

            }

        }

    }

}
