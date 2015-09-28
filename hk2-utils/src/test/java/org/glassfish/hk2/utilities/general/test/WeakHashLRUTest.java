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

}
