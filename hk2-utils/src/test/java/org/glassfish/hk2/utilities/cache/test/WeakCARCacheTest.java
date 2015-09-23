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
    
    /**
     * Tests that we can add eleven things to a cache of size 10, twice!
     */
    @Test @org.junit.Ignore
    public void testAddElevenToCacheSizeTen() {
        WeakCARCache<String, Integer> car = CacheUtilities.createWeakCARCache(TO_INTEGER, SMALL_CACHE_SIZE);
        
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

}
