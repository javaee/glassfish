/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2016 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.general.Hk2ThreadLocal;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class GeneralUtilitiesTest {
    /**
     * Tests that the exact same objects are equal
     */
    @Test
    public void testEqualObjectsAreEqual() {
        IntegerObject zero = new IntegerObject(0);
        
        Assert.assertTrue(GeneralUtilities.safeEquals(zero, zero));
    }
    
    /**
     * Tests both null are equals
     */
    @Test
    public void testBothNullEquals() {
        Assert.assertTrue(GeneralUtilities.safeEquals(null, null));
    }
    
    /**
     * Tests first parameter is null, second not returns false
     */
    @Test
    public void testFirstOperatorNullNotEquals() {
        IntegerObject zero = new IntegerObject(0);
        
        Assert.assertFalse(GeneralUtilities.safeEquals(null, zero));
    }
    
    /**
     * Tests second parameter is null, first not returns false
     */
    @Test
    public void testSecondOperatorNullNotEquals() {
        IntegerObject zero = new IntegerObject(0);
        
        Assert.assertFalse(GeneralUtilities.safeEquals(zero, null));
    }
    
    /**
     * Tests two different object that are equal returns true
     */
    @Test
    public void testTwoDifferentObjectWhoAreEqualReturnsTrue() {
        IntegerObject zero0 = new IntegerObject(0);
        IntegerObject zero1 = new IntegerObject(0);
        
        Assert.assertTrue(GeneralUtilities.safeEquals(zero0, zero1));
    }
    
    /**
     * Tests two different object that are equal returns true
     */
    @Test
    public void testTwoDifferentObjectWhichAreNotEqualReturnsFalse() {
        IntegerObject zero = new IntegerObject(0);
        IntegerObject one = new IntegerObject(1);
        
        Assert.assertFalse(GeneralUtilities.safeEquals(zero, one));
    }
    
    /**
     * Tests two different object that are equal returns true
     */
    @Test
    public void testObjectsOfDifferentTypesReturnFalse() {
        IntegerObject zero = new IntegerObject(0);
        
        Assert.assertFalse(GeneralUtilities.safeEquals(zero, new Integer(0)));
        Assert.assertFalse(GeneralUtilities.safeEquals(new Integer(0), zero));
    }
    
    /**
     * Tests two different object that are equal returns true
     */
    @Test
    public void testVariousArrayTypes() {
        {
            Class<?> bArray = GeneralUtilities.loadClass(getClass().getClassLoader(), "[B");
            Assert.assertEquals("[B", bArray.getName());
        }
        
        {
            Class<?> iArray = GeneralUtilities.loadClass(getClass().getClassLoader(), "[[I");
            Assert.assertEquals("[[I", iArray.getName());
        }
        
        {
            Class<?> jArray = GeneralUtilities.loadClass(getClass().getClassLoader(), "[[[J");
            Assert.assertEquals("[[[J", jArray.getName());
        }
        
        {
            Class<?> zArray = GeneralUtilities.loadClass(getClass().getClassLoader(), "[Z");
            Assert.assertEquals("[Z", zArray.getName());
        }
        
        {
            Class<?> sArray = GeneralUtilities.loadClass(getClass().getClassLoader(), "[S");
            Assert.assertEquals("[S", sArray.getName());
        }
        
        {
            Class<?> cArray = GeneralUtilities.loadClass(getClass().getClassLoader(), "[C");
            Assert.assertEquals("[C", cArray.getName());
        }
        
        {
            Class<?> dArray = GeneralUtilities.loadClass(getClass().getClassLoader(), "[D");
            Assert.assertEquals("[D", dArray.getName());
        }
        
        {
            Class<?> fArray = GeneralUtilities.loadClass(getClass().getClassLoader(), "[F");
            Assert.assertEquals("[F", fArray.getName());
        }
        
        {
            Class<?> lArray = GeneralUtilities.loadClass(getClass().getClassLoader(), "[[[[[Ljava.lang.String;");
            Assert.assertEquals("[[[[[Ljava.lang.String;", lArray.getName());
        }
    }
    
    /**
     * Tests the gets in Hk2ThreadLocal
     */
    @Test
    public void testBasicHk2ThreadLocalOperation() throws InterruptedException {
        ThreadService ts = new ThreadService();
        
        ThreadGetter g1 = new ThreadGetter(ts);
        ThreadGetter g2 = new ThreadGetter(ts);
        ThreadGetter g3 = new ThreadGetter(ts);
        
        Thread t1 = new Thread(g1);
        Thread t2 = new Thread(g2);
        Thread t3 = new Thread(g3);
        
        t1.start();
        t2.start();
        t3.start();
        
        Assert.assertEquals(t1.getId(), g1.getThreadIdFromService());
        Assert.assertEquals(t2.getId(), g2.getThreadIdFromService());
        Assert.assertEquals(t3.getId(), g3.getThreadIdFromService());
        Assert.assertEquals(Thread.currentThread().getId(), ts.getThreadIdFromLocal());
    }
    
    /**
     * Tests that sets override the initial value, in various combinations such as
     * initialSet, non-initialSet and then also after a removal
     */
    @Test
    public void testSetsOverrideInitialValue() throws InterruptedException {
        ThreadService ts = new ThreadService();
        
        ThreadSpecificSetOverridesInitialValue g1 = new ThreadSpecificSetOverridesInitialValue(ts, -1, false, false);
        ThreadSpecificSetOverridesInitialValue g2 = new ThreadSpecificSetOverridesInitialValue(ts, -2, true, false);
        ThreadSpecificSetOverridesInitialValue g3 = new ThreadSpecificSetOverridesInitialValue(ts, -3, true, true);
        ThreadSpecificSetOverridesInitialValue g4 = new ThreadSpecificSetOverridesInitialValue(ts, -3, false, true);
        
        Thread t1 = new Thread(g1);
        Thread t2 = new Thread(g2);
        Thread t3 = new Thread(g3);
        Thread t4 = new Thread(g4);
        
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        
        Assert.assertEquals(-1, g1.getThreadIdFromService());
        Assert.assertEquals(-2, g2.getThreadIdFromService());
        Assert.assertEquals(t3.getId(), g3.getThreadIdFromService());
        Assert.assertEquals(t4.getId(), g4.getThreadIdFromService());
        Assert.assertEquals(Thread.currentThread().getId(), ts.getThreadIdFromLocal());
        
        // One for each thread we still have a reference to t1, t2, t3, t4 and the current thread
        Assert.assertEquals(5, ts.getSize());
        
        // The below code is here to make sure an optimizer doesn't get
        // smart on us and notice that our references are stale in the
        // check above
        Assert.assertTrue(t1.getId() > 0);
        Assert.assertTrue(t2.getId() > 0);
        Assert.assertTrue(t3.getId() > 0);
        Assert.assertTrue(t4.getId() > 0);
    }
    
    /**
     * Tests that ephemeral threads do not cause a large memory leak in the
     * Hk2ThreadLocal
     */
    @Test
    public void testMemoryLeakWithEphemeralThreads() throws InterruptedException {
        ThreadService ts = new ThreadService();
        
        for (int lcv = 0; lcv < 1000; lcv++) {
            ThreadSpecificSetOverridesInitialValue g1 = new ThreadSpecificSetOverridesInitialValue(ts, -1, true, false);
            
            Thread t1 = new Thread(g1);
            
            t1.start();
            
            Assert.assertEquals(-1, g1.getThreadIdFromService());
        }
        
        for (int lcv = 0; lcv < 10; lcv++) {
            System.gc();
            
            if (ts.getSize() < 10) break;
            
            Thread.sleep(1000);
        }
        
        Assert.assertTrue("Size of heap still too large: " + ts.getSize(), ts.getSize() < 10);
        
    }
    
    /**
     * Tests that removeAll works
     */
    @Test
    public void testRemoveAllRemovesAll() throws InterruptedException {
        ThreadService ts = new ThreadService();
        
        ThreadSpecificReuppingGetter g1 = new ThreadSpecificReuppingGetter(ts, -1);
        ThreadSpecificReuppingGetter g2 = new ThreadSpecificReuppingGetter(ts, -2);
        
        Thread t1 = new Thread(g1);
        Thread t2 = new Thread(g2);
        
        t1.start();
        t2.start();
        
        Assert.assertEquals(-1, g1.getThreadIdFromService());
        Assert.assertEquals(-2, g2.getThreadIdFromService());
        
        ts.doRemoveAll();
        
        g1.reup();
        g2.reup();
        
        Assert.assertEquals(t1.getId(), g1.getThreadIdFromService());
        Assert.assertEquals(t2.getId(), g2.getThreadIdFromService());
        
        g1.shutdown();
        g2.shutdown();
    }
    
    /**
     * Tests that a ThreadLocal can have a null value
     */
    @Test
    public void testHk2ThreadLocalCanHaveNullValue() throws InterruptedException {
        Hk2ThreadLocal<Object> threadLocal = new Hk2ThreadLocal<Object>();
        
        // First time uses default impl of initialValue to set it
        Assert.assertNull(threadLocal.get());
        
        // Second time just gets it
        Assert.assertNull(threadLocal.get());
    }
    
    private static class ThreadGetter implements Runnable {
        private final ThreadService threadService;
        private Long tid;
        
        private ThreadGetter(ThreadService ts) {
            threadService = ts;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            synchronized (this) {
                tid = threadService.getThreadIdFromLocal();
                notifyAll();
            }
            
        }
        
        public long getThreadIdFromService() throws InterruptedException {
            synchronized (this) {
                while (tid == null) {
                    wait();
                }
                
                return tid;
            }
        }
    }
    
    private static class ThreadSpecificSetOverridesInitialValue implements Runnable {
        private final ThreadService threadService;
        private final long customInitial;
        private final boolean doInitialGet;
        private final boolean doPostRemove;
        private Long tid;
        
        private ThreadSpecificSetOverridesInitialValue(ThreadService ts,
                long customInitial,
                boolean doInitialGet,
                boolean doPostRemove) {
            threadService = ts;
            this.customInitial = customInitial;
            this.doInitialGet = doInitialGet;
            this.doPostRemove = doPostRemove;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            if (doInitialGet) {
                if (threadService.getThreadIdFromLocal() != Thread.currentThread().getId()) {
                    throw new AssertionError("Should have gotten an initial value equal to thread id");
                }
            }
            
            threadService.doSet(customInitial);
            
            if (doPostRemove) {
                threadService.doRemove();
            }
            
            synchronized (this) {
                
                tid = threadService.getThreadIdFromLocal();
                notifyAll();
            }
            
        }
        
        public long getThreadIdFromService() throws InterruptedException {
            synchronized (this) {
                while (tid == null) {
                    wait();
                }
                
                return tid;
            }
        }
    }
    
    private static class ThreadSpecificReuppingGetter implements Runnable {
        private final ThreadService threadService;
        private Long tid;
        private boolean done = false;
        private final long altSet;
        
        private ThreadSpecificReuppingGetter(ThreadService ts, long altSet) {
            threadService = ts;
            this.altSet = altSet;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            synchronized (this) {
                threadService.doSet(altSet);
                
                while (!done) {
                    if (tid == null) {
                        tid = threadService.getThreadIdFromLocal();
                        notifyAll();
                    }
                    
                    try {
                        wait();
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    
                }
            }
            
        }
        
        public long getThreadIdFromService() throws InterruptedException {
            synchronized (this) {
                while (tid == null) {
                    wait();
                }
                
                return tid;
            }
        }
        
        public void reup() {
            synchronized (this) {
                tid = null;
                notifyAll();
            }
        }
        
        public void shutdown() {
            synchronized (this) {
                done = true;
                notifyAll();
            }
        }
    }
    
    private static class IntegerObject {
        private final int value;
        
        private IntegerObject(int value) {
            this.value = value;
        }
        
        public int hashCode() {
            return value;
        }
        
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof IntegerObject)) return false;
            IntegerObject other = (IntegerObject) o;
            
            return value == other.value;
        }
    }

}
