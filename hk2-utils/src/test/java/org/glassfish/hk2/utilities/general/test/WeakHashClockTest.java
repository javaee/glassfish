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

import java.util.Map;

import org.glassfish.hk2.utilities.cache.CacheKeyFilter;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.general.WeakHashClock;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class WeakHashClockTest {
    private final static String KEY = "key";
    private final static String VALUE = "value";
    private final static String KEY1 = "key1";
    private final static String VALUE1 = "value1";
    private final static String KEY2 = "key2";
    private final static String VALUE2 = "value2";
    
    private final static int ITERATIONS = 10;
    
    /**
     * Ensures that next goes forever and keeps returning the same element
     */
    private void testNextKeepsGoingWithOneEntry(WeakHashClock<String, String> clock) {
        clock.put(KEY, VALUE);
        Assert.assertEquals(1, clock.size());
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            Map.Entry<String, String> next = clock.next();
            
            Assert.assertEquals(KEY, next.getKey());
            Assert.assertEquals(VALUE, next.getValue());
        }
        
        Assert.assertEquals(1, clock.size());
    }
    
    @Test
    public void testNextKeepsGoingWithOneEntryWeak() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        
        testNextKeepsGoingWithOneEntry(clock);
    }
    
    @Test
    public void testNextKeepsGoingWithOneEntryStrong() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(false);
        
        testNextKeepsGoingWithOneEntry(clock);
    }
    
    /**
     * Ensures that next returns null when no entries
     */
    private void testNextReturnsNullWithZeroEntries(WeakHashClock<String, String> clock) {
        Assert.assertEquals(0, clock.size());
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            Map.Entry<String, String> next = clock.next();
            
            Assert.assertNull(next);
        }
        
        Assert.assertEquals(0, clock.size());
    }
    
    @Test
    public void testNextReturnsNullWithZeroEntriesWeak() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        testNextReturnsNullWithZeroEntries(clock);
    }
    
    @Test
    public void testNextReturnsNullWithZeroEntriesStrong() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(false);
        testNextReturnsNullWithZeroEntries(clock);
    }
    
    /**
     * Ensures that next goes forever returning the two elements again and again
     */
    private void testNextKeepsGoingWithTwoEntries(WeakHashClock<String, String> clock) {
        clock.put(KEY, VALUE);
        clock.put(KEY1, VALUE1);
        Assert.assertEquals(2, clock.size());
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            Map.Entry<String, String> next = clock.next();
            
            Assert.assertEquals(KEY, next.getKey());
            Assert.assertEquals(VALUE, next.getValue());
            
            next = clock.next();
            
            Assert.assertEquals(KEY1, next.getKey());
            Assert.assertEquals(VALUE1, next.getValue());
        }
        
        Assert.assertEquals(2, clock.size());
        
    }
    
    /**
     * Ensures that next goes forever returning the two elements again and again
     */
    @Test
    public void testNextKeepsGoingWithTwoEntriesWeak() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        testNextKeepsGoingWithTwoEntries(clock);
    }
    
    @Test
    public void testNextKeepsGoingWithTwoEntriesStrong() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(false);
        testNextKeepsGoingWithTwoEntries(clock);
    }
    
    /**
     * Ensures that next goes forever returning the two elements again and again
     */
    private void testNextKeepsGoingWithTenEntries(WeakHashClock<String, String> clock) {
        // It is important to keep references to the keys
        // in a weak clock, otherwise they have the possibility
        // to get collected.  These arrays give a hard reference
        // to the keys and values
        String keys[] = new String[ITERATIONS];
        String values[] = new String[ITERATIONS];
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            keys[lcv] = KEY + lcv;
            values[lcv] = VALUE + lcv;
            
            clock.put(keys[lcv], values[lcv]);
        }
        
        Assert.assertEquals(ITERATIONS, clock.size());
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            for (int lcv2 = 0; lcv2 < ITERATIONS; lcv2++) {
                Map.Entry<String, String> next = clock.next();
            
                Assert.assertEquals(keys[lcv2], next.getKey());
                Assert.assertEquals(values[lcv2], next.getValue());
            }
        }
        
        Assert.assertEquals(ITERATIONS, clock.size());
    }
    
    /**
     * Ensures that next goes forever returning the two elements again and again
     */
    @Test
    public void testNextKeepsGoingWithTenEntriesWeak() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        testNextKeepsGoingWithTenEntries(clock);
    }
    
    @Test
    public void testNextKeepsGoingWithTenEntriesString() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(false);
        testNextKeepsGoingWithTenEntries(clock);
    }
    
    /**
     * Ensures that nulls to put throws exceptions
     */
    @Test
    public void testNullsToPutThrowsExceptions() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        
        try {
            clock.put(KEY, null);
            Assert.fail("null value should fail");
        }
        catch (IllegalArgumentException iae) {
            // expected
        }
        
        try {
            clock.put(null, VALUE);
            Assert.fail("null key should fail");
        }
        catch (IllegalArgumentException iae) {
            // expected
        }
        
        try {
            clock.put(null, null);
            Assert.fail("null key and value should fail");
        }
        catch (IllegalArgumentException iae) {
            // expected
        }
    }
    
    /**
     * Ensures that a null as the key to get returns null
     */
    private void testNullGet(WeakHashClock<String, String> clock) {
        Assert.assertNull(clock.get(null));
        
        clock.put(KEY, VALUE);
        
        Assert.assertNull(clock.get(null));
    }
    
    /**
     * Ensures that a null as the key to get returns null
     */
    @Test
    public void testNullGetWeak() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        testNullGet(clock);
    }
    
    /**
     * Ensures that a null as the key to get returns null
     */
    @Test
    public void testNullGetStrong() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(false);
        testNullGet(clock);
    }
    
    /**
     * Tests that a get that is found is found, and one not found is not found
     */
    private void testFoundGet(WeakHashClock<String, String> clock) {
        Assert.assertNull(clock.get(KEY));
        
        clock.put(KEY, VALUE);
        
        Assert.assertEquals(VALUE, clock.get(KEY));
        
        Assert.assertEquals(VALUE, clock.remove(KEY));
        
        Assert.assertNull(clock.get(KEY));
    }
    
    @Test
    public void testFoundGetWeak() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        testFoundGet(clock);
    }
    
    @Test
    public void testFoundGetStrong() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(false);
        testFoundGet(clock);
    }
    
    /**
     * Tests that a get that is found is found, and one not found is not found
     */
    private void testNullNextWorksAfterRemovingLastItem(WeakHashClock<String, String> clock) {
        // It is important to keep references to the keys
        // in a weak clock, otherwise they have the possibility
        // to get collected.  These arrays give a hard reference
        // to the keys and values
        String keys[] = new String[ITERATIONS];
        String values[] = new String[ITERATIONS];
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            keys[lcv] = KEY + lcv;
            values[lcv] = VALUE + lcv;
            
            clock.put(keys[lcv], values[lcv]);
        }
        
        Assert.assertEquals(ITERATIONS, clock.size());
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            Assert.assertEquals(values[lcv], clock.remove(KEY + lcv));
        }
        
        Assert.assertEquals(0, clock.size());
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            Map.Entry<String, String> next = clock.next();
            
            Assert.assertNull(next);
        }
        
        Assert.assertEquals(0, clock.size());
    }
    
    @Test
    public void testNullNextWorksAfterRemovingLastItemWeak() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        testNullNextWorksAfterRemovingLastItem(clock);
    }
    
    @Test
    public void testNullNextWorksAfterRemovingLastItemStrong() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(false);
        testNullNextWorksAfterRemovingLastItem(clock);
    }
    
    /**
     * Tests that a weak key/value is removed after a GC
     * @throws InterruptedException 
     */
    @Test
    public void testWeakKeyValueGoneAfterGC() throws InterruptedException {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        
        // It is important to keep references to the keys
        // in a weak clock, otherwise they have the possibility
        // to get collected.  These arrays give a hard reference
        // to the keys and values
        String keys[] = new String[ITERATIONS];
        String values[] = new String[ITERATIONS];
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            keys[lcv] = KEY + lcv;
            values[lcv] = VALUE + lcv;
            
            clock.put(keys[lcv], values[lcv]);
        }
        
        Assert.assertEquals(ITERATIONS, clock.size());
        
        // Remove key5
        keys[5] = null;
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            System.gc();
            
            clock.clearStaleReferences();
            
            if (clock.size() != ITERATIONS - 1) {
                Thread.sleep(10);
            }
            else {
                break;
            }
        }
        
        Assert.assertEquals(ITERATIONS - 1, clock.size());
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            Map.Entry<String, String> next = clock.next();
            if (lcv >= 5) {
                if (lcv == ITERATIONS - 1) {
                    Assert.assertEquals(keys[0], next.getKey());
                    Assert.assertEquals(values[0], next.getValue());
                }
                else {
                    Assert.assertEquals(keys[lcv+1], next.getKey());
                    Assert.assertEquals(values[lcv+1], next.getValue());
                }
                
            }
            else {
                Assert.assertEquals(keys[lcv], next.getKey());
                Assert.assertEquals(values[lcv], next.getValue());
            }
        }
        
        Assert.assertEquals(ITERATIONS - 1, clock.size());
    }
    
    /**
     * Tests removal of null key returns null
     * 
     * @throws InterruptedException
     */
    private void testRemovalOfNullReturnsNull(WeakHashClock<String, String> clock) throws InterruptedException {
        Assert.assertNull(clock.remove(null));
        
        clock.put(KEY, VALUE);
        
        Assert.assertNull(clock.remove(null));
    }
    
    @Test
    public void testRemovalOfNullReturnsNullWeak() throws InterruptedException {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        testRemovalOfNullReturnsNull(clock);
    }
    
    @Test
    public void testRemovalOfNullReturnsNullStrong() throws InterruptedException {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(false);
        testRemovalOfNullReturnsNull(clock);
    }
    
    /**
     * Tests removal of key not present returns null
     * 
     * @throws InterruptedException
     */
    private void testRemovalOfKeyNotFoundReturnsNull(WeakHashClock<String, String> clock) throws InterruptedException {
        Assert.assertNull(clock.remove(KEY));
        
        clock.put(KEY, VALUE);
        
        Assert.assertNull(clock.remove(KEY1));
        
        Assert.assertEquals(VALUE, clock.remove(KEY));
        Assert.assertNull(clock.remove(KEY));
    }
    
    @Test
    public void testRemovalOfKeyNotFoundReturnsNullWeak() throws InterruptedException {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        testRemovalOfKeyNotFoundReturnsNull(clock);
    }
    
    @Test
    public void testRemovalOfKeyNotFoundReturnsNullStrong() throws InterruptedException {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(false);
        testRemovalOfKeyNotFoundReturnsNull(clock);
    }
    
    /**
     * Tests that a weak key/value is removed after a GC only
     * using the next verb
     * 
     * @throws InterruptedException 
     */
    @Test
    public void testWeaksRemovedOnlyUsingNext() throws InterruptedException {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        
        String key = new String(KEY);
        clock.put(key, VALUE);
        
        Assert.assertEquals(1, clock.size());
        
        // Remove the hard reference
        key = null;
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            System.gc();
            
            if (clock.next() == null) {
                // Success!
                return;
            }
            
            Thread.sleep(10);
        }
        
        Assert.fail("The clock never removed the weak reference");
    }
    
    /**
     * Tests that a weak key/value is removed after a GC
     * @throws InterruptedException 
     */
    @Test
    public void testWeakKeysAllGoneAfterGCNextOnly() throws InterruptedException {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        
        // It is important to keep references to the keys
        // in a weak clock, otherwise they have the possibility
        // to get collected.  These arrays give a hard reference
        // to the keys and values
        String keys[] = new String[ITERATIONS];
        String values[] = new String[ITERATIONS];
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            keys[lcv] = KEY + lcv;
            values[lcv] = VALUE + lcv;
            
            clock.put(keys[lcv], values[lcv]);
        }
        
        Assert.assertEquals(ITERATIONS, clock.size());
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            // All keys gone
            keys[lcv] = null;
            
        }
        
        for (int lcv = 0; lcv < ITERATIONS; lcv++) {
            System.gc();
            
            if (clock.next() == null) {
                for (int lcv2 = 0; lcv2 < ITERATIONS; lcv2++) {
                    if (clock.size() == 0) {
                        // success
                        return;
                    }
                    
                    Thread.sleep(10);
                    
                    System.gc();
                }
                
                Assert.fail("Size never went to zero");
            }
            
            Thread.sleep(10);
        }
        
        Assert.fail("All keys were not removed when using only next");
    }
    
    /**
     * Tests removal of key not present returns null
     * 
     * @throws InterruptedException
     */
    private void testClearOnNonEmpty(WeakHashClock<String, String> clock) throws InterruptedException {
        clock.put(KEY, VALUE);
        
        Assert.assertEquals(1, clock.size());
        
        Map.Entry<String, String> entry = clock.next();
        Assert.assertEquals(KEY, entry.getKey());
        Assert.assertEquals(VALUE, entry.getValue());
        
        clock.clear();
        
        Assert.assertEquals(0, clock.size());
        
        entry = clock.next();
        Assert.assertNull(entry);
    }
    
    @Test
    public void testClearOnNonEmptyWeak() throws InterruptedException {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        testClearOnNonEmpty(clock);
    }
    
    @Test
    public void testClearOnNonEmptyStrong() throws InterruptedException {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(false);
        testClearOnNonEmpty(clock);
    }
    
    /**
     * Tests removal of key not present returns null
     * 
     * @throws InterruptedException
     */
    private void testEntrySetKeyThrows(WeakHashClock<String, String> clock) throws InterruptedException {
        clock.put(KEY, VALUE);
        
        Map.Entry<String, String> entry = clock.next();
        
        try {
            entry.setValue(VALUE1);
            Assert.fail("setValue should not have been implemented");
        }
        catch (AssertionError ae) {
            // correct
        }
    }
    
    @Test
    public void testEntrySetKeyThrowsWeak() throws InterruptedException {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        testEntrySetKeyThrows(clock);
    }
    
    @Test
    public void testEntrySetKeyThrowsStrong() throws InterruptedException {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(false);
        testEntrySetKeyThrows(clock);
    }
    
    /**
     * Tests that hasWeakKeys works
     * @throws InterruptedException
     */
    @Test
    public void testHasWeakKeys() throws InterruptedException {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        Assert.assertTrue(clock.hasWeakKeys());
        
        clock = GeneralUtilities.getWeakHashClock(false);
        Assert.assertFalse(clock.hasWeakKeys());
    }
    
    /**
     * Ensures that toString works
     */
    @Test
    public void testClockToString() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        String empty = clock.toString();
        
        Assert.assertTrue(empty.contains("" + System.identityHashCode(clock)));
        
        clock.put(KEY, VALUE);
        clock.put(KEY1, VALUE1);
        
        String nonEmpty = clock.toString();
        
        Assert.assertTrue(nonEmpty.contains(KEY));
        Assert.assertTrue(nonEmpty.contains(KEY1));
    }
    
    /**
     * Tests that a get that is found is found, and one not found is not found
     */
    private void testReleaseMatching(WeakHashClock<String, String> clock) {
        clock.put(KEY, VALUE);
        clock.put(KEY1, VALUE1);
        clock.put(KEY2, VALUE2);
        
        clock.releaseMatching(new CacheKeyFilter<String>() {

            @Override
            public boolean matches(String key) {
                if (key.equals(KEY)) return true;
                if (key.equals(KEY2)) return true;
                return false;
            }
            
        });
        
        Assert.assertEquals(1, clock.size());
        Assert.assertNull(clock.get(KEY));
        Assert.assertEquals(VALUE1, clock.get(KEY1));
        Assert.assertNull(clock.get(KEY2));
        
        // Make sure doesn't bomb
        clock.releaseMatching(null);
        
        Assert.assertEquals(1, clock.size());
        Assert.assertNull(clock.get(KEY));
        Assert.assertEquals(VALUE1, clock.get(KEY1));
        Assert.assertNull(clock.get(KEY2));
        
        // Make sure even if filter matches nothing we are ok
        clock.releaseMatching(new CacheKeyFilter<String>() {

            @Override
            public boolean matches(String key) {
                if (key.equals(KEY)) return true;
                if (key.equals(KEY2)) return true;
                return false;
            }
            
        });
        
        Assert.assertEquals(1, clock.size());
        Assert.assertNull(clock.get(KEY));
        Assert.assertEquals(VALUE1, clock.get(KEY1));
        Assert.assertNull(clock.get(KEY2));
        
        // Ensures this remove can take us to zero
        clock.releaseMatching(new CacheKeyFilter<String>() {

            @Override
            public boolean matches(String key) {
                return true;
            }
            
        });
        
        Assert.assertEquals(0, clock.size());
        Assert.assertNull(clock.get(KEY));
        Assert.assertNull(clock.get(KEY1));
        Assert.assertNull(clock.get(KEY2));
    }
    
    @Test
    public void testReleaseMatchingWeak() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(true);
        testReleaseMatching(clock);
    }
    
    @Test
    public void testReleaseMatchingStrong() {
        WeakHashClock<String, String> clock = GeneralUtilities.getWeakHashClock(false);
        testReleaseMatching(clock);
    }

}
