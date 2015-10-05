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
package org.glassfish.hk2.utilities.general.test;


import java.util.LinkedList;
import java.util.Random;

import org.glassfish.hk2.utilities.cache.CacheKeyFilter;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.general.WeakHashLRU;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class WeakHashLRUTest {
    private final static String KEY = "key";
    private final static String KEY1 = "key1";
    private final static String KEY2 = "key2";
    
    private final static int ITERATIONS = 10;
    
    /**
     * Ensures that remove can remove from a single and zero length
     * lru list
     */
    private void testRemoveDeletesSingleEntry(WeakHashLRU<String> lru) {
        lru.add(KEY);
        Assert.assertEquals(1, lru.size());
        
        Assert.assertEquals(KEY, lru.remove());
        
        Assert.assertEquals(0, lru.size());
        
        Assert.assertNull(lru.remove());
        
        Assert.assertEquals(0, lru.size());
    }
    
    @Test
    public void testRemoveDeletesSingleEntryWeak() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(true);
        testRemoveDeletesSingleEntry(lru);
    }
    
    @Test
    public void testRemoveDeletesSingleEntryStrong() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(false);
        testRemoveDeletesSingleEntry(lru);
    }
    
    /**
     * Ensures that remove actually removes the LRU
     */
    private void testRemoveRemovesLRU(WeakHashLRU<String> lru) {
        lru.add(KEY);  // LRU
        lru.add(KEY1); // MRU
        Assert.assertEquals(2, lru.size());
        
        Assert.assertEquals(KEY, lru.remove());
        
        Assert.assertEquals(1, lru.size());
        
        Assert.assertEquals(KEY1, lru.remove());
        
        Assert.assertEquals(0, lru.size());
    }
    
    @Test
    public void testRemoveRemovesLRUWeak() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(true);
        testRemoveRemovesLRU(lru);
    }
    
    @Test
    public void testRemoveRemovesLRUStrong() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(false);
        testRemoveRemovesLRU(lru);
    }
    
    /**
     * Ensures that re-add of key moves it from LRU spot
     */
    private void testReAddChangesLRU(WeakHashLRU<String> lru) {
        lru.add(KEY);  // LRU
        lru.add(KEY1); // MRU
        lru.add(KEY); // now KEY1 should be LRU and KEY should be MRU again
        
        Assert.assertEquals(2, lru.size());
        
        Assert.assertEquals(KEY1, lru.remove());
        
        Assert.assertEquals(1, lru.size());
        
        Assert.assertEquals(KEY, lru.remove());
        
        Assert.assertEquals(0, lru.size());
    }
    
    @Test
    public void testReAddChangesLRUWeak() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(true);
        testReAddChangesLRU(lru);
    }
    
    @Test
    public void testReAddChangesLRUStrong() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(false);
        testReAddChangesLRU(lru);
    }
    
    /**
     * Tests that a null key throws exception
     */
    @Test
    public void testBadInputToAdd() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(true);
        
        try {
            lru.add(null);
            Assert.fail("Should have thrown exception");
        }
        catch (IllegalArgumentException iae) {
            // good
        }
    }
    
    /**
     * Tests contains
     */
    private void testContains(WeakHashLRU<String> lru) {
        lru.add(KEY);
        lru.add(KEY2);
        
        Assert.assertTrue(lru.contains(KEY));
        Assert.assertFalse(lru.contains(KEY1));
        Assert.assertTrue(lru.contains(KEY2));
        
        Assert.assertTrue(lru.remove(KEY));
        
        Assert.assertFalse(lru.contains(KEY));
        Assert.assertFalse(lru.contains(KEY1));
        Assert.assertTrue(lru.contains(KEY2));
        
        Assert.assertFalse(lru.remove(KEY1));
        
        Assert.assertFalse(lru.contains(KEY));
        Assert.assertFalse(lru.contains(KEY1));
        Assert.assertTrue(lru.contains(KEY2));
        
        Assert.assertTrue(lru.remove(KEY2));
        
        Assert.assertFalse(lru.contains(KEY));
        Assert.assertFalse(lru.contains(KEY1));
        Assert.assertFalse(lru.contains(KEY2));
        
        lru.add(KEY1);
        
        Assert.assertFalse(lru.contains(KEY));
        Assert.assertTrue(lru.contains(KEY1));
        Assert.assertFalse(lru.contains(KEY2));
        
    }
    
    @Test
    public void testContainsWeak() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(true);
        testContains(lru);
    }
    
    @Test
    public void testContainsStrong() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(false);
        testContains(lru);
    }
    
    /**
     * Tests null remove
     */
    private void testNullRemoveReturnsFalse(WeakHashLRU<String> lru) {
        lru.add(KEY);
        
        Assert.assertFalse(lru.remove(null));
    }
    
    @Test
    public void testNullRemoveReturnsFalseWeak() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(true);
        testNullRemoveReturnsFalse(lru);
    }
    
    @Test
    public void testNullRemoveReturnsFalseStrong() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(false);
        testNullRemoveReturnsFalse(lru);
    }
    
    /**
     * Tests clear
     */
    private void testClear(WeakHashLRU<String> lru) {
        lru.add(KEY);
        lru.add(KEY1);
        lru.add(KEY2);
        
        Assert.assertEquals(3, lru.size());
        
        lru.clear();
        
        Assert.assertEquals(0, lru.size());
        
        lru.clear();
        
        Assert.assertEquals(0, lru.size());
        
        lru.add(KEY1);
        
        Assert.assertEquals(1, lru.size());
        
        lru.clear();
        
        Assert.assertNull(lru.remove());
        
        lru.add(KEY2);
        lru.clear();
        
        Assert.assertFalse(lru.remove(KEY2));
    }
    
    @Test
    public void testClearWeak() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(true);
        testClear(lru);
    }
    
    @Test
    public void testClearStrong() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(false);
        testClear(lru);
    }
    
    /**
     * Tests weak reference in the middle removed
     * @throws InterruptedException 
     */
    @Test
    public void testWeakInTheMiddleIgnored() throws InterruptedException {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(true);
        
        // It is important to keep references to the keys
        // in a weak clock, otherwise they have the possibility
        // to get collected.  These arrays give a hard reference
        // to the keys and values
        String keys[] = new String[ITERATIONS];
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            keys[lcv] = KEY + lcv;
            
            lru.add(keys[lcv]);
        }
        
        Assert.assertEquals(ITERATIONS, lru.size());
        
        keys[5] = null;
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            System.gc();
            
            if (lru.size() == ITERATIONS - 1) {
                break;
            }
            
            Thread.sleep(10);
        }
        
        for (int lcv = 0; lcv < ITERATIONS - 1; lcv++) {
            if (lcv < 5) {
                Assert.assertEquals(keys[lcv], lru.remove());
            }
            else {
                Assert.assertEquals(keys[lcv+1], lru.remove());
            }
        }
        
        Assert.assertEquals(0, lru.size());
    }
    
    /**
     * Tests weak reference all over removed
     * @throws InterruptedException 
     */
    @Test
    public void testAllKeysGoWeak() throws InterruptedException {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(true);
        
        // It is important to keep references to the keys
        // in a weak clock, otherwise they have the possibility
        // to get collected.  These arrays give a hard reference
        // to the keys and values
        String keys[] = new String[ITERATIONS];
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            keys[lcv] = KEY + lcv;
            
            lru.add(keys[lcv]);
        }
        
        Assert.assertEquals(ITERATIONS, lru.size());
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            keys[lcv] = null;
        }
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            System.gc();
            
            if (lru.size() == 0) {
                break;
            }
            
            Thread.sleep(10);
        }
        
        Assert.assertNull(lru.remove());
    }
    
    /**
     * Tests only using remove to discover Weak refs
     * @throws InterruptedException
     */
    @Test
    public void testWeakOnlyRemoveUsed() throws InterruptedException {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(true);
        
        String key = new String(KEY);
        
        lru.add(key);
        
        key = null;
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            System.gc();
            
            String removed = lru.remove();
            if (removed == null) {
                // success, now check size eventually goes down as well
                for (int lcv2 = 0; lcv2 < ITERATIONS; lcv2++) {
                    if (lru.size() == 0) {
                        return;
                    }
                    
                    System.gc();
                    
                    Thread.sleep(10);
                }
                
                Assert.fail("Size never went to zero");
            }
            
            Thread.sleep(10);
        }
        
        Assert.fail("Did not remove the weak key with just remove");
        
    }
    
    /**
     * Tests weak reference in the middle removed
     * @throws InterruptedException 
     */
    @Test
    public void testClearStaleReferences() throws InterruptedException {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(true);
        
        // It is important to keep references to the keys
        // in a weak clock, otherwise they have the possibility
        // to get collected.  These arrays give a hard reference
        // to the keys and values
        String keys[] = new String[ITERATIONS];
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            keys[lcv] = KEY + lcv;
            
            lru.add(keys[lcv]);
        }
        
        Assert.assertEquals(ITERATIONS, lru.size());
        
        keys[5] = null;
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            System.gc();
            
            lru.clearStaleReferences();
            
            if (lru.size() == ITERATIONS - 1) {
                break;
            }
            
            Thread.sleep(10);
        }
        
        for (int lcv = 0; lcv < ITERATIONS - 1; lcv++) {
            if (lcv < 5) {
                Assert.assertEquals(keys[lcv], lru.remove());
            }
            else {
                Assert.assertEquals(keys[lcv+1], lru.remove());
            }
        }
        
        Assert.assertEquals(0, lru.size());
    }
    
    @Test
    public void testToString() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(false);
        
        String zeroLRU = lru.toString();
        Assert.assertTrue(zeroLRU.contains("" + System.identityHashCode(lru)));
        
        lru.add(KEY1);
        lru.add(KEY);
        lru.add(KEY2);
        
        String fullLRU = lru.toString();
        
        Assert.assertTrue(fullLRU.contains(KEY1));
        Assert.assertTrue(fullLRU.contains(KEY));
        Assert.assertTrue(fullLRU.contains(KEY2));
    }
    
    /**
     * Tests that a get that is found is found, and one not found is not found
     */
    private void testReleaseMatching(WeakHashLRU<String> lru) {
        lru.add(KEY);
        lru.add(KEY1);
        lru.add(KEY2);
        
        lru.releaseMatching(new CacheKeyFilter<String>() {

            @Override
            public boolean matches(String key) {
                if (key.equals(KEY)) return true;
                if (key.equals(KEY2)) return true;
                return false;
            }
            
        });
        
        Assert.assertEquals(1, lru.size());
        Assert.assertFalse(lru.contains(KEY));
        Assert.assertTrue(lru.contains(KEY1));
        Assert.assertFalse(lru.contains(KEY2));
        
        // Make sure doesn't bomb
        lru.releaseMatching(null);
        
        Assert.assertEquals(1, lru.size());
        Assert.assertFalse(lru.contains(KEY));
        Assert.assertTrue(lru.contains(KEY1));
        Assert.assertFalse(lru.contains(KEY2));
        
        // Make sure even if filter matches nothing we are ok
        lru.releaseMatching(new CacheKeyFilter<String>() {

            @Override
            public boolean matches(String key) {
                if (key.equals(KEY)) return true;
                if (key.equals(KEY2)) return true;
                return false;
            }
            
        });
        
        Assert.assertEquals(1, lru.size());
        Assert.assertFalse(lru.contains(KEY));
        Assert.assertTrue(lru.contains(KEY1));
        Assert.assertFalse(lru.contains(KEY2));
        
        // Ensures this remove can take us to zero
        lru.releaseMatching(new CacheKeyFilter<String>() {

            @Override
            public boolean matches(String key) {
                return true;
            }
            
        });
        
        Assert.assertEquals(0, lru.size());
        Assert.assertFalse(lru.contains(KEY));
        Assert.assertFalse(lru.contains(KEY1));
        Assert.assertFalse(lru.contains(KEY2));
    }
    
    @Test
    public void testReleaseMatchingWeak() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(true);
        testReleaseMatching(lru);
    }
    
    @Test
    public void testReleaseMatchingStrong() {
        WeakHashLRU<String> lru = GeneralUtilities.getWeakHashLRU(false);
        testReleaseMatching(lru);
    }
    
    private final static int NUM_THREADS = 20;
    
    /**
     * Tests the concurrency of the system
     * 
     * @param clock
     * @throws InterruptedException
     */
    private void testConcurrency(WeakHashLRU<Integer> lru) throws InterruptedException {
        Thread threads[] = new Thread[NUM_THREADS];
        Runner runners[] = new Runner[NUM_THREADS];
        
        for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
            runners[lcv] = new Runner(lcv, lru);
            threads[lcv] = new Thread(runners[lcv]);
            
            threads[lcv].start();
        }
        
        for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
            Assert.assertTrue(runners[lcv].waitForFinish(600 * 1000));
        }
        
        for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
            for (Throwable th : runners[lcv].errors) {
                if (th instanceof RuntimeException) {
                    throw (RuntimeException) th;
                }
                
                throw new RuntimeException(th);
            }
        }
        
    }
    
    @Test
    public void testConcurrencyWeak() throws InterruptedException {
        WeakHashLRU<Integer> lru = GeneralUtilities.getWeakHashLRU(true);
        testConcurrency(lru);
    }
    
    @Test
    public void testConcurrencyStrong() throws InterruptedException {
        WeakHashLRU<Integer> clock = GeneralUtilities.getWeakHashLRU(false);
        testConcurrency(clock);
    }
    
    private final static int CONCURRENT_ITERATIONS = 100000;
    
    private static class Runner implements Runnable {
        private final Random RANDOM;
        private final WeakHashLRU<Integer> lru;
        private final LinkedList<Throwable> errors = new LinkedList<Throwable>();
        private final LinkedList<Integer> hardenedKeys = new LinkedList<Integer>();
        private final Object lock = new Object();
        private boolean finished = false;
        
        private Runner(int randomizer, WeakHashLRU<Integer> lru) {
            RANDOM = new Random(10000L + randomizer);
            this.lru = lru;
        }

        private void runInternal() {
            for (int i = 0; i < CONCURRENT_ITERATIONS; i++) {
                int operation = RANDOM.nextInt(100);
                if (operation < 40) {
                    // Get operation, 40% of the time
                    int getMe = RANDOM.nextInt(100);
                    
                    try {
                        lru.contains(getMe);
                    }
                    catch (Throwable th) {
                        System.err.println("contains failure: " + th.getMessage());
                        th.printStackTrace();
                        
                        errors.add(th);
                    }
                }
                else if (operation < 60) {
                    // put operation, 20% of the time
                    Integer putMe = RANDOM.nextInt(100);
                    
                    try {
                        lru.add(putMe);
                        if (RANDOM.nextInt(2) == 0) {
                            // Half the keys added are hardened
                            hardenedKeys.add(putMe);
                        }
                    }
                    catch (Throwable th) {
                        System.err.println("add failure: " + th.getMessage());
                        th.printStackTrace();
                        
                        errors.add(th);
                    }
                }
                else if (operation < 75) {
                    // remove operation, 15% of the time
                    int removeMe = RANDOM.nextInt(100);
                    
                    try {
                        lru.remove(removeMe);
                    }
                    catch (Throwable th) {
                        System.err.println("removeFailure: " + th.getMessage());
                        th.printStackTrace();
                        
                        errors.add(th);
                    }
                }
                else if (operation < 90) {
                    // remove releaseMatching, 15% of the time
                    
                    try {
                        lru.releaseMatching(new CacheKeyFilter<Integer>() {

                            /**
                             * Removes even entries
                             */
                            @Override
                            public boolean matches(Integer key) {
                                int candidate = key;
                                if ((candidate % 2) == 0) return true;
                                return false;
                            }
                            
                        });
                    }
                    catch (Throwable th) {
                        System.err.println("releaseMatching failure: " + th.getMessage());
                        th.printStackTrace();
                        
                        errors.add(th);
                    }
                }
                else if (operation < 98) {
                    // size, 8% of the time
                    try {
                        lru.size();
                    }
                    catch (Throwable th) {
                        System.err.println("size failure: " + th.getMessage());
                        th.printStackTrace();
                        
                        errors.add(th);
                    }
                }
                else if (operation < 99) {
                    // clear, 1% of the time
                    try {
                        lru.clear();
                    }
                    catch (Throwable th) {
                        System.err.println("clear failure: " + th.getMessage());
                        th.printStackTrace();
                        
                        errors.add(th);
                    }
                }
                else if (operation < 100) {
                    // clearStaleReferences, 1% of the time
                    try {
                        lru.clearStaleReferences();
                    }
                    catch (Throwable th) {
                        System.err.println("clearStale failure: " + th.getMessage());
                        th.printStackTrace();
                        
                        errors.add(th);
                    }
                }
                
            }
            
        }
        
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            runInternal();
            synchronized (lock) {
                finished = true;
                lock.notifyAll();
            }
        }
        
        private boolean waitForFinish(long waitMillis) throws InterruptedException {
            synchronized (lock) {
                while (!finished && (waitMillis > 0)) {
                    long elapsedTime = System.currentTimeMillis();
                    
                    lock.wait(waitMillis);
                    
                    elapsedTime = System.currentTimeMillis() - elapsedTime;
                    waitMillis -= elapsedTime;
                }
                
                return finished;
            }
        }
    }

}
