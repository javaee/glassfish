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
package org.glassfish.hk2.utilities.test;

import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;

import org.glassfish.hk2.utilities.cache.CacheKeyFilter;
import org.glassfish.hk2.utilities.cache.Computable;
import org.glassfish.hk2.utilities.cache.HybridCacheEntry;
import org.glassfish.hk2.utilities.cache.LRUHybridCache;
import org.junit.Test;

/**
 * Test LRU Hybrid cache based on the original test, {@link CacheTest}, for {@link LRUCache}.
 *
 * @author jwells
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class LRUHybridCacheTest {

    class Counter {

        Map<Integer, Integer> computationalMap = new HashMap<Integer, Integer>();

        synchronized void computedKey(Integer key) {
            if (!computationalMap.containsKey(key)) {
                computationalMap.put(key, 0);
            }
            final int newVal = computationalMap.get(key) + 1;
            computationalMap.put(key, newVal);
        }

        synchronized void clear() {
            computationalMap.clear();
        }

        synchronized int numberOfComputations(Integer key) {
            return computationalMap.containsKey(key) ? computationalMap.get(key) : 0;
        }
    }

    final Counter counter = new Counter();
    class MyComputable implements Computable<Integer, HybridCacheEntry<Integer>> {

        LRUHybridCache cache;

        @Override
        public HybridCacheEntry<Integer> compute(Integer key) {
            counter.computedKey(key);
            return cache.createCacheEntry(key, key, false);
        }
    };

    MyComputable c3 = new MyComputable();
    final LRUHybridCache<Integer, Integer> cache3 = new LRUHybridCache(3, c3);
    MyComputable c5 = new MyComputable();
    final LRUHybridCache<Integer, Integer> cache5 = new LRUHybridCache(5, c5);
    MyComputable c20 = new MyComputable();
    final LRUHybridCache<Integer, Integer> cache20 = new LRUHybridCache(20, c20);

    {
        c3.cache = cache3;
        c5.cache = cache5;
        c20.cache = cache20;
    }


    void reset() {
        counter.clear();
        cache3.clear();
        cache5.clear();
        cache20.clear();
    }

    private void _testGet123Etc(LRUHybridCache cache, int... a) {
        for (int i : a) {
            Assert.assertSame(i, cache.compute(i).getValue());
        }
    }

    /**
     * The most basic test, add to and get from cache3
     */
    @Test
    public void testCanGetFromCache() {
        reset();
        _testGet123Etc(cache3, 1,2,3);
    }

    /**
     * If cache3 is size three, only three values should be stored.  And the one removed
     * should be the last one
     */
    @Test
    public void testLastIsRemoved() {
        reset();

        _testGet123Etc(cache3, 1,2,3); //123
        Assert.assertSame(4, cache3.compute(4).getValue()); // 234

        Assert.assertSame(1, counter.numberOfComputations(1));
        // 1 is least recently used, so it should be gone after computing 4,
        // thus computation repeated, after the next line, 2 will be gone
        Assert.assertSame(1, cache3.compute(1).getValue()); // 341
        Assert.assertSame(2, counter.numberOfComputations(1));

        // Also check that the others ARE still there without a need to re-compute
        Assert.assertSame(4, cache3.compute(4).getValue()); // 314
        Assert.assertSame(1, counter.numberOfComputations(4));
        Assert.assertSame(3, cache3.compute(3).getValue()); // 143
        Assert.assertSame(1, counter.numberOfComputations(3));

        // same test as with 1 above, 2 is gone, so far computed once
        Assert.assertSame(1, counter.numberOfComputations(2)); // 143
        // after the next line, another computation for 2 is needed
        Assert.assertSame(2, cache3.compute(2).getValue()); //432
        Assert.assertSame(2, counter.numberOfComputations(2));

        Assert.assertSame(1, counter.numberOfComputations(3));
        Assert.assertSame(3, cache3.compute(3).getValue()); //423
        Assert.assertSame(1, counter.numberOfComputations(3));

        Assert.assertSame(1, cache3.compute(1).getValue()); //231
        Assert.assertSame(3, counter.numberOfComputations(1));
    }

    /**
     * Ensure that get changes the LRU order of the list
     */
    @Test
    public void testGetChangesOrder() {

        reset();

        _testGet123Etc(cache3, 1,2,3); // 123

        // 1 is last now, but if I get it, it should move to the front, and
        // 2 would be put out on the next add
        Assert.assertSame(1, cache3.compute(1).getValue()); // 231

        Assert.assertSame(4, cache3.compute(4).getValue()); // 314

        // 2 was least recently used, so it should be gone
        Assert.assertSame(1, counter.numberOfComputations(2));
        Assert.assertSame(2, cache3.compute(2).getValue()); // 142
        Assert.assertSame(2, counter.numberOfComputations(2));
    }

    /**
     * Tests that an entry in the middle of a larger cache3 can be properly moved
     */
    @Test
    public void testMakeChangesToLargerCache() {

        reset();

        _testGet123Etc(cache5, 1,2,3,4,5); // 12345


        _testGet123Etc(cache5, 6); // 23456

        // 1 was least recently used, should be gone
        _testGet123Etc(cache5, 1); // 34561
        Assert.assertSame(2, counter.numberOfComputations(1));

        // Also check that the others ARE there
        Assert.assertSame(4, cache5.compute(4).getValue()); //35614
        Assert.assertSame(3, cache5.compute(3).getValue()); //56143
        Assert.assertSame(5, cache5.compute(5).getValue()); //61435

        // 2 should be gone
        Assert.assertSame(1, counter.numberOfComputations(2));
        Assert.assertSame(2, cache5.compute(2).getValue()); //14352
        Assert.assertSame(2, counter.numberOfComputations(2));
    }

    /**
     * The most basic test, add to and get from cache3
     */
    @Test
    public void testReleaseCache() {

        reset();

        _testGet123Etc(cache3, 1,2,3);
        _testGet123Etc(cache3, 1,2,3);

        Assert.assertSame(1, counter.numberOfComputations(1));
        Assert.assertSame(1, counter.numberOfComputations(2));
        Assert.assertSame(1, counter.numberOfComputations(3));

        cache3.clear();

        _testGet123Etc(cache3, 1,2,3);

        Assert.assertSame(2, counter.numberOfComputations(1));
        Assert.assertSame(2, counter.numberOfComputations(2));
        Assert.assertSame(2, counter.numberOfComputations(3));
    }

    /**
     * Tests that removing directly from the cache3 using the CacheEntry works
     */
    @Test
    public void testRemoveFirstFromCache() {

        reset();

        HybridCacheEntry entry = cache3.compute(1); // 1
        cache3.compute(2); // 12
        cache3.compute(3); // 123

        entry.removeFromCache(); //23

        Assert.assertSame(1, counter.numberOfComputations(1));
        Assert.assertSame(1, cache3.compute(1).getValue());
        Assert.assertSame(2, counter.numberOfComputations(1));
    }

    /**
     * Tests that removing directly from the cache3 using the CacheEntry works
     */
    @Test
    public void testRemoveMiddleFromCache() {
        reset();

        cache3.compute(1); // 1
        HybridCacheEntry entry = cache3.compute(2); // 12
        cache3.compute(3); // 123

        entry.removeFromCache(); //13

        Assert.assertSame(1, counter.numberOfComputations(2));
        Assert.assertSame(2, cache3.compute(2).getValue());
        Assert.assertSame(2, counter.numberOfComputations(2));
    }

    /**
     * Tests that removing directly from the cache3 using the CacheEntry works
     */
    @Test
    public void testRemoveLastFromCache() {
        reset();

        cache3.compute(1); // 1
        cache3.compute(2); // 12
        HybridCacheEntry entry = cache3.compute(3); // 123

        entry.removeFromCache(); //12

        Assert.assertSame(1, counter.numberOfComputations(3));
        Assert.assertSame(3, cache3.compute(3).getValue());
        Assert.assertSame(2, counter.numberOfComputations(3));
    }


    /**
     * This was originally an error case found in ServiceLocatorImpl for LRUCache
     */
    @Test
    public void testRemoveMovedEntry() {
        reset();

        cache3.compute(1); // 1
        HybridCacheEntry entry = cache3.compute(2); // 12
        cache3.compute(3); // 123

        Assert.assertEquals(new Integer(2), cache3.compute(2).getValue());  // Moves 2: 132

        entry.removeFromCache(); // 13

        Assert.assertSame(1, counter.numberOfComputations(2));
        Assert.assertSame(2, cache3.compute(2).getValue());
        Assert.assertSame(2, counter.numberOfComputations(2));
    }

    /**
     * This removes multiple entries from the cache20 based
     * on a filter
     */
    @Test
    public void testRemoveEvenEntries() {

        reset();

        for (int i = 0; i < 10; i++) {
            cache20.compute(i);
        }

        cache20.releaseMatching(new CacheKeyFilter<Integer>() {

            @Override
            public boolean matches(Integer key) {
                return key % 2 == 0;
            }

        });

        for (int i = 0; i < 10; i++) {
            Assert.assertSame(i, cache20.compute(i).getValue());
            Assert.assertSame((i % 2) == 0 ? 2 : 1, counter.numberOfComputations(i));
        }
    }
}
