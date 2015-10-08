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
package org.glassfish.hk2.utilities.cache.test;

import java.util.LinkedList;
import java.util.Random;

import org.glassfish.hk2.utilities.cache.CacheKeyFilter;
import org.glassfish.hk2.utilities.cache.CacheUtilities;
import org.glassfish.hk2.utilities.cache.Computable;
import org.glassfish.hk2.utilities.cache.WeakCARCache;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class WeakCARCacheTest {
    private final static ToIntegerComputable TO_INTEGER = new ToIntegerComputable();
    private final static ReflectiveComputable<Integer> INT_TO_INT = new ReflectiveComputable<Integer>();
    
    private final static String ZERO = "0";
    private final static String ONE = "1";
    private final static String TWO = "2";
    private final static String THREE = "3";
    private final static String FOUR = "4";
    private final static String FIVE = "5";
    private final static String SIX = "6";
    private final static String SEVEN = "7";
    private final static String EIGHT = "8";
    private final static String NINE = "9";
    private final static String TEN = "10";
    
    private final static int SMALL_CACHE_SIZE = 10;
    
    private final static int[] TAKE_OFF_OF_B2 = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
        1
    };
    
    private final static int[] ACCESS_T2 = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
        1, 1, 5
    };
    
    private final static int[] TAKE_OFF_OF_B1 = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
        1, 0
    };
    
    private final static int[] EQUAL_T1_T2 = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
        1, 0, 11, 12, 11, 12, 15, 16, 17, 18, 19
    };
    
    private final static int[] MAX_OUT_B2_KEYS_PLUS_ONE = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
        1, 0, 11, 12, 11, 12, 15, 16, 17, 18, 19,
        15, 20, 16, 21, 17, 22
    };
    
    private final static int[] CYCLE_ACCESSED_T2_TO_FIND_DEMOTE = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1,
        11
    };
    
    private final static int[] ADD_TO_T1_WITH_VALUE_INB2 = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
        11
    };
    
    private final static int[] PUSH_P_TO_MAX = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
        1, 0, 11, 12, 11, 12, 15, 16, 17, 18, 19,
        15, 20, 16, 21, 17, 22, 23, 18, 19
    };
    
    private final static int[] P_TO_5_BACK_TO_2 = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
        1, 0, 11, 12, 11, 12, 15, 16, 17, 18, 19,
        9, 8, 7
    };
    
    private static Integer[] getIntArray(int[] fromScalar) {
        Integer[] retVal = new Integer[fromScalar.length];
        
        for (int lcv = 0; lcv < fromScalar.length; lcv++) {
            retVal[lcv] = new Integer(fromScalar[lcv]);
        }
        
        return retVal;
    }
    
    /**
     * Tests that we can add eleven things to a cache of size 10, twice!
     */
    private void testAddElevenToCacheSizeTen(WeakCARCache<String, Integer> car) {
        for (int lcv = 0; lcv < 2; lcv++) {
            Assert.assertEquals(0, car.compute(ZERO).intValue());
            Assert.assertEquals(1, car.compute(ONE).intValue());
            Assert.assertEquals(2, car.compute(TWO).intValue());
            Assert.assertEquals(3, car.compute(THREE).intValue());
            Assert.assertEquals(4, car.compute(FOUR).intValue());
            Assert.assertEquals(5, car.compute(FIVE).intValue());
            Assert.assertEquals(6, car.compute(SIX).intValue());
            Assert.assertEquals(7, car.compute(SEVEN).intValue());
            Assert.assertEquals(8, car.compute(EIGHT).intValue());
            Assert.assertEquals(9, car.compute(NINE).intValue());
            Assert.assertEquals(10, car.compute(TEN).intValue());
        }
        
        Assert.assertEquals(10, car.getValueSize());
        Assert.assertEquals(10, car.getKeySize());
        
        Assert.assertEquals(0, car.getP());
    }
    
    @Test // @org.junit.Ignore
    public void testAddElevenToCacheSizeTenWeak() {
        WeakCARCache<String, Integer> car = CacheUtilities.createWeakCARCache(TO_INTEGER, SMALL_CACHE_SIZE, true);
        testAddElevenToCacheSizeTen(car);
    }
    
    @Test // @org.junit.Ignore
    public void testAddElevenToCacheSizeTenStrong() {
        WeakCARCache<String, Integer> car = CacheUtilities.createWeakCARCache(TO_INTEGER, SMALL_CACHE_SIZE, false);
        testAddElevenToCacheSizeTen(car);
    }
    
    /**
     * Tests moving completely from T1 to T2 (and one B1)
     */
    private void testAddElevenToCacheSizeTenForwardThenBackward(WeakCARCache<String, Integer> car) {
        Assert.assertEquals(0, car.compute(ZERO).intValue());
        Assert.assertEquals(1, car.compute(ONE).intValue());
        Assert.assertEquals(2, car.compute(TWO).intValue());
        Assert.assertEquals(3, car.compute(THREE).intValue());
        Assert.assertEquals(4, car.compute(FOUR).intValue());
        Assert.assertEquals(5, car.compute(FIVE).intValue());
        Assert.assertEquals(6, car.compute(SIX).intValue());
        Assert.assertEquals(7, car.compute(SEVEN).intValue());
        Assert.assertEquals(8, car.compute(EIGHT).intValue());
        Assert.assertEquals(9, car.compute(NINE).intValue());
        Assert.assertEquals(10, car.compute(TEN).intValue());
        
        Assert.assertEquals(10, car.compute(TEN).intValue());
        Assert.assertEquals(9, car.compute(NINE).intValue());
        Assert.assertEquals(8, car.compute(EIGHT).intValue());
        Assert.assertEquals(7, car.compute(SEVEN).intValue());
        Assert.assertEquals(6, car.compute(SIX).intValue());
        Assert.assertEquals(5, car.compute(FIVE).intValue());
        Assert.assertEquals(4, car.compute(FOUR).intValue());
        Assert.assertEquals(3, car.compute(THREE).intValue());
        Assert.assertEquals(2, car.compute(TWO).intValue());
        Assert.assertEquals(1, car.compute(ONE).intValue());
        Assert.assertEquals(0, car.compute(ZERO).intValue());
        
        Assert.assertEquals(10, car.getValueSize());
        Assert.assertEquals(11, car.getKeySize());
        
        Assert.assertEquals(1, car.getT1Size());
        Assert.assertEquals(9, car.getT2Size());
        Assert.assertEquals(0, car.getB1Size());
        Assert.assertEquals(1, car.getB2Size());
        
        Assert.assertEquals(0, car.getP());
    }
    
    @Test // @org.junit.Ignore
    public void testAddElevenToCacheSizeTenForwardThenBackwardWeak() {
        WeakCARCache<String, Integer> car = CacheUtilities.createWeakCARCache(TO_INTEGER, SMALL_CACHE_SIZE, true);
        testAddElevenToCacheSizeTenForwardThenBackward(car);
    }
    
    @Test // @org.junit.Ignore
    public void testAddElevenToCacheSizeTenForwardThenBackwardStrong() {
        WeakCARCache<String, Integer> car = CacheUtilities.createWeakCARCache(TO_INTEGER, SMALL_CACHE_SIZE, false);
        testAddElevenToCacheSizeTenForwardThenBackward(car);
    }
    
    /**
     * Takes a value off of B2
     */
    private void testTakingOffOfB2(WeakCARCache<Integer, Integer> car) {
        Integer[] keys = getIntArray(TAKE_OFF_OF_B2);
        
        for (int lcv = 0; lcv < keys.length; lcv++) {
            Assert.assertEquals(keys[lcv], car.compute(keys[lcv]));
        }
        
        Assert.assertEquals(10, car.getValueSize());
        Assert.assertEquals(11, car.getKeySize());
        
        Assert.assertEquals(0, car.getT1Size());
        Assert.assertEquals(10, car.getT2Size());
        Assert.assertEquals(1, car.getB1Size());
        Assert.assertEquals(0, car.getB2Size());
        
        Assert.assertEquals(0, car.getP());
    }
    
    @Test // @org.junit.Ignore
    public void testTakingOffOfB2Weak() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, true);
        testTakingOffOfB2(car);
        
    }
    
    @Test // @org.junit.Ignore
    public void testTakingOffOfB2Strong() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, false);
        testTakingOffOfB2(car);
        
    }
    
    /**
     * Takes a value off of B1
     */
    private void testTakingOffOfB1(WeakCARCache<Integer, Integer> car) {
        Integer[] keys = getIntArray(TAKE_OFF_OF_B1);
        
        for (int lcv = 0; lcv < keys.length; lcv++) {
            Assert.assertEquals(keys[lcv], car.compute(keys[lcv]));
        }
        
        Assert.assertEquals(10, car.getValueSize());
        Assert.assertEquals(11, car.getKeySize());
        
        Assert.assertEquals(0, car.getT1Size());
        Assert.assertEquals(10, car.getT2Size());
        Assert.assertEquals(0, car.getB1Size());
        Assert.assertEquals(1, car.getB2Size());
        
        Assert.assertEquals(1, car.getP());
    }
    
    @Test // @org.junit.Ignore
    public void testTakingOffOfB1Weak() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, true);
        testTakingOffOfB1(car);
    }
    
    @Test // @org.junit.Ignore
    public void testTakingOffOfB1Strong() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, false);
        testTakingOffOfB1(car);
    }
    
    /**
     * Gets T1 and T2 to equal size
     */
    private void testEqualT1T2(WeakCARCache<Integer, Integer> car) {
        Integer[] keys = getIntArray(EQUAL_T1_T2);
        
        for (int lcv = 0; lcv < keys.length; lcv++) {
            Assert.assertEquals(keys[lcv], car.compute(keys[lcv]));
        }
        
        Assert.assertEquals(10, car.getValueSize());
        Assert.assertEquals(18, car.getKeySize());
        
        Assert.assertEquals(5, car.getT1Size());
        Assert.assertEquals(5, car.getT2Size());
        Assert.assertEquals(0, car.getB1Size());
        Assert.assertEquals(8, car.getB2Size());
        
        Assert.assertEquals(5, car.getP());
    }
    
    @Test // @org.junit.Ignore
    public void testEqualT1T2Weak() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, true);
        testEqualT1T2(car);
    }
    
    @Test // @org.junit.Ignore
    public void testEqualT1T2Strong() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, false);
        testEqualT1T2(car);
    }
    
    /**
     * Maxes keys plus one off of B2, makes sure B2 does not grow without bound
     */
    private void testMaxOutKeysPlusOne(WeakCARCache<Integer, Integer> car) {
        Integer[] keys = getIntArray(MAX_OUT_B2_KEYS_PLUS_ONE);
        
        for (int lcv = 0; lcv < keys.length; lcv++) {
            Assert.assertEquals(keys[lcv], car.compute(keys[lcv]));
        }
        
        Assert.assertEquals(10, car.getValueSize());
        Assert.assertEquals(20, car.getKeySize());
        
        Assert.assertEquals(5, car.getT1Size());
        Assert.assertEquals(5, car.getT2Size());
        Assert.assertEquals(0, car.getB1Size());
        Assert.assertEquals(10, car.getB2Size());
        
        Assert.assertEquals(5, car.getP());
    }
    
    @Test // @org.junit.Ignore
    public void testMaxOutKeysPlusOneWeak() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, true);
        testMaxOutKeysPlusOne(car);
    }
    
    @Test // @org.junit.Ignore
    public void testMaxOutKeysPlusOneStrong() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, false);
        testMaxOutKeysPlusOne(car);
    }
    
    /**
     * Maxes keys plus one off of B2, makes sure B2 does not grow without bound
     */
    private void testWeCanAccessAMemberOfT2(WeakCARCache<Integer, Integer> car) {
        Integer[] keys = getIntArray(ACCESS_T2);
        
        for (int lcv = 0; lcv < keys.length; lcv++) {
            Assert.assertEquals(keys[lcv], car.compute(keys[lcv]));
        }
        
        Assert.assertEquals(10, car.getValueSize());
        Assert.assertEquals(11, car.getKeySize());
        
        Assert.assertEquals(0, car.getT1Size());
        Assert.assertEquals(10, car.getT2Size());
        Assert.assertEquals(1, car.getB1Size());
        Assert.assertEquals(0, car.getB2Size());
        
        Assert.assertEquals(0, car.getP());
    }
    
    @Test // @org.junit.Ignore
    public void testWeCanAccessAMemberOfT2Weak() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, true);
        testWeCanAccessAMemberOfT2(car);
    }
    
    @Test // @org.junit.Ignore
    public void testWeCanAccessAMemberOfT2Strong() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, false);
        testWeCanAccessAMemberOfT2(car);
    }
    
    /**
     * Sets all T2 to true bit forces cycle when looking for demotion candidate
     */
    private void testForceDemotionT2ToCycle(WeakCARCache<Integer, Integer> car) {
        Integer[] keys = getIntArray(CYCLE_ACCESSED_T2_TO_FIND_DEMOTE);
        
        for (int lcv = 0; lcv < keys.length; lcv++) {
            Assert.assertEquals(keys[lcv], car.compute(keys[lcv]));
        }
        
        Assert.assertEquals(10, car.getValueSize());
        Assert.assertEquals(12, car.getKeySize());
        
        Assert.assertEquals(1, car.getT1Size());
        Assert.assertEquals(9, car.getT2Size());
        Assert.assertEquals(1, car.getB1Size());
        Assert.assertEquals(1, car.getB2Size());
        
        Assert.assertEquals(0, car.getP());
    }
    
    @Test // @org.junit.Ignore
    public void testForceDemotionT2ToCycleWeak() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, true);
        testForceDemotionT2ToCycle(car);
    }
    
    @Test // @org.junit.Ignore
    public void testForceDemotionT2ToCycleStrong() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, false);
        testForceDemotionT2ToCycle(car);
    }
    
    /**
     * Sets all T2 to true bit forces cycle when looking for demotion candidate
     */
    private void testRemoveB2ToZeroAndGetSomethingFromB1(WeakCARCache<Integer, Integer> car) {
        
        
        Integer[] keys = getIntArray(TAKE_OFF_OF_B2);
        
        for (int lcv = 0; lcv < keys.length; lcv++) {
            Assert.assertEquals(keys[lcv], car.compute(keys[lcv]));
        }
        
        // At this point 0 is in B1 and 2 is in T2.  Removing 2
        // gives us space in the cache and doing a compute of
        // 0 has us move 0 from B1 onto T2, where the size of
        // B2 is zero
        Assert.assertTrue(car.remove(new Integer(2)));
        Assert.assertEquals(0, car.compute(new Integer(0)).intValue());
        
        Assert.assertEquals(10, car.getValueSize());
        Assert.assertEquals(10, car.getKeySize());
        
        Assert.assertEquals(0, car.getT1Size());
        Assert.assertEquals(10, car.getT2Size());
        Assert.assertEquals(0, car.getB1Size());
        Assert.assertEquals(0, car.getB2Size());
        
        Assert.assertEquals(1, car.getP());
    }
    
    @Test // @org.junit.Ignore
    public void testRemoveB2ToZeroAndGetSomethingFromB1Weak () {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, true);
        testRemoveB2ToZeroAndGetSomethingFromB1(car);
    }
    
    @Test // @org.junit.Ignore
    public void testRemoveB2ToZeroAndGetSomethingFromB1Strong () {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, false);
        testRemoveB2ToZeroAndGetSomethingFromB1(car);
    }
    
    /**
     * Sets all T2 to true bit forces cycle when looking for demotion candidate
     */
    private void testMakeB1SizeBeLessThanB2SizeDuringCacheMiss(WeakCARCache<Integer, Integer> car) {
        Integer[] keys = getIntArray(ADD_TO_T1_WITH_VALUE_INB2);
        
        for (int lcv = 0; lcv < keys.length; lcv++) {
            Assert.assertEquals(keys[lcv], car.compute(keys[lcv]));
        }
        
        // At this point 0 is in B1, 1 is in B2, 7 is in T2
        // We remove 0 to make the size of B1 less than B2,
        // and remove a value from T2 to allow the cache to
        // just take a new one, then take it from B2
        Assert.assertTrue(car.remove(new Integer(0)));
        Assert.assertTrue(car.remove(new Integer(7)));
        Assert.assertEquals(1, car.compute(new Integer(1)).intValue());
        
        Assert.assertEquals(10, car.getValueSize());
        Assert.assertEquals(10, car.getKeySize());
        
        Assert.assertEquals(1, car.getT1Size());
        Assert.assertEquals(9, car.getT2Size());
        Assert.assertEquals(0, car.getB1Size());
        Assert.assertEquals(0, car.getB2Size());
        
        Assert.assertEquals(0, car.getP());
    }
    
    @Test // @org.junit.Ignore
    public void testMakeB1SizeBeLessThanB2SizeDuringCacheMissWeak() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, true);
        testMakeB1SizeBeLessThanB2SizeDuringCacheMiss(car);
    }
    
    @Test // @org.junit.Ignore
    public void testMakeB1SizeBeLessThanB2SizeDuringCacheMissStrong() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, false);
        testMakeB1SizeBeLessThanB2SizeDuringCacheMiss(car);
    }
    
    /**
     * Pushes P to maxSize, makes sure it cannot go over
     */
    private void testPushPToMaxSize(WeakCARCache<Integer, Integer> car) {
        Integer[] keys = getIntArray(PUSH_P_TO_MAX);
        
        for (int lcv = 0; lcv < keys.length; lcv++) {
            Assert.assertEquals(keys[lcv], car.compute(keys[lcv]));
        }
        
        Assert.assertEquals(10, car.getValueSize());
        Assert.assertEquals(20, car.getKeySize());
        
        Assert.assertEquals(4, car.getT1Size());
        Assert.assertEquals(6, car.getT2Size());
        Assert.assertEquals(0, car.getB1Size());
        Assert.assertEquals(10, car.getB2Size());
        
        Assert.assertEquals(10, car.getP());
    }
    
    @Test // @org.junit.Ignore
    public void testPushPToMaxSizeWeak() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, true);
        testPushPToMaxSize(car);
    }
    
    @Test // @org.junit.Ignore
    public void testPushPToMaxSizeStrong() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, false);
        testPushPToMaxSize(car);
    }
    
    /**
     * Pushes P to 5 and then back down to zero
     */
    private void testPushPToFiveThenBackToZero(WeakCARCache<Integer, Integer> car) {
        Integer[] keys = getIntArray(P_TO_5_BACK_TO_2);
        
        for (int lcv = 0; lcv < keys.length; lcv++) {
            Assert.assertEquals(keys[lcv], car.compute(keys[lcv]));
        }
        
        Assert.assertEquals(10, car.getValueSize());
        Assert.assertEquals(18, car.getKeySize());
        
        Assert.assertEquals(2, car.getT1Size());
        Assert.assertEquals(8, car.getT2Size());
        Assert.assertEquals(3, car.getB1Size());
        Assert.assertEquals(5, car.getB2Size());
        
        Assert.assertEquals(2, car.getP());
    }
    
    @Test // @org.junit.Ignore
    public void testPushPToFiveThenBackToZeroWeak() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, true);
        testPushPToFiveThenBackToZero(car);
    }
    
    @Test // @org.junit.Ignore
    public void testPushPToFiveThenBackToZeroStrong() {
        WeakCARCache<Integer, Integer> car = CacheUtilities.createWeakCARCache(INT_TO_INT, SMALL_CACHE_SIZE, false);
        testPushPToFiveThenBackToZero(car);
    }
    
    private final static int NUM_THREADS = 20;
    
    /**
     * Tests the concurrency of the system
     * 
     * @param clock
     * @throws InterruptedException
     */
    private void testConcurrency(WeakCARCache<Integer, Integer> cache) throws InterruptedException {
        Thread threads[] = new Thread[NUM_THREADS];
        Runner runners[] = new Runner[NUM_THREADS];
        
        for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
            runners[lcv] = new Runner(lcv, cache);
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
    
    @Test @org.junit.Ignore
    public void testConcurrencyWeak() throws InterruptedException {
        // Key space is 100 keys, so we will make the cache size 50
        WeakCARCache<Integer, Integer> cache = CacheUtilities.createWeakCARCache(INT_TO_INT, 50, true);
        testConcurrency(cache);
    }
    
    @Test @org.junit.Ignore
    public void testConcurrencyStrong() throws InterruptedException {
        // Key space is 100 keys, so we will make the cache size 50
        WeakCARCache<Integer, Integer> cache = CacheUtilities.createWeakCARCache(INT_TO_INT, 50, false);
        testConcurrency(cache);
    }
    
    private final static int CONCURRENT_ITERATIONS = 100000;
    
    private static class Runner implements Runnable {
        private final Random RANDOM;
        private final WeakCARCache<Integer, Integer> cache;
        private final LinkedList<Throwable> errors = new LinkedList<Throwable>();
        private final LinkedList<Integer> hardenedKeys = new LinkedList<Integer>();
        private final Object lock = new Object();
        private boolean finished = false;
        
        private Runner(int randomizer, WeakCARCache<Integer, Integer> cache) {
            RANDOM = new Random(10000L + randomizer);
            this.cache = cache;
        }

        private void runInternal() {
            for (int i = 0; i < CONCURRENT_ITERATIONS; i++) {
                int operation = RANDOM.nextInt(100);
                if (operation < 70) {
                    // compute operation, 70% of the time
                    int getMe = RANDOM.nextInt(100);
                    
                    try {
                        cache.compute(getMe);
                    }
                    catch (Throwable th) {
                        System.err.println("contains failure: " + th.getMessage());
                        th.printStackTrace();
                        
                        errors.add(th);
                    }
                    
                    if (RANDOM.nextInt(2) == 0) {
                        // Half the keys added are hardened
                        hardenedKeys.add(getMe);
                    }
                }
                else if (operation < 80) {
                    // remove operation, 10% of the time
                    Integer putMe = RANDOM.nextInt(100);
                    
                    try {
                        cache.remove(putMe);
                    }
                    catch (Throwable th) {
                        System.err.println("remove failure: " + th.getMessage());
                        th.printStackTrace();
                        
                        errors.add(th);
                    }
                }
                else if (operation < 90) {
                    // remove releaseMatching, 10% of the time
                    
                    try {
                        cache.releaseMatching(new CacheKeyFilter<Integer>() {

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
                else if (operation < 95) {
                    // size, 5% of the time
                    try {
                        cache.getValueSize();
                        cache.getKeySize();
                        cache.getT1Size();
                        cache.getT2Size();
                        cache.getB1Size();
                        cache.getB2Size();
                    }
                    catch (Throwable th) {
                        System.err.println("size failure: " + th.getMessage());
                        th.printStackTrace();
                        
                        errors.add(th);
                    }
                }
                else if (operation < 98) {
                    // getP, 2% of the time
                    try {
                        cache.getP();
                    }
                    catch (Throwable th) {
                        System.err.println("getP failure: " + th.getMessage());
                        th.printStackTrace();
                        
                        errors.add(th);
                    }
                }
                else if (operation < 99) {
                    // clear, 1% of the time
                    try {
                        cache.clear();
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
                        cache.clearStaleReferences();
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
    
    private static class ToIntegerComputable implements Computable<String, Integer> {

        /* (non-Javadoc)
         * @see org.glassfish.hk2.utilities.cache.Computable#compute(java.lang.Object)
         */
        @Override
        public Integer compute(String key) {
            return Integer.parseInt(key);
        }
        
    }
    
    private static class ReflectiveComputable<I> implements Computable<I, I> {

        /* (non-Javadoc)
         * @see org.glassfish.hk2.utilities.cache.Computable#compute(java.lang.Object)
         */
        @Override
        public I compute(I key) {
            return key;
        }
        
    }

}
