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
package org.glassfish.hk2.tests.locator.multithreaded1;

import java.util.List;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class MultiThreaded1Test {
    private final static int NUM_THREADS = 20;
    private final static int NUM_ITERATIONS = 10000;
    
    private final static IndexedFilter FILTER = new IndexedFilter() {
        @Override
        public boolean matches(Descriptor d) {
            return d.getQualifiers().contains(QualifierA.class.getName());
        }

        @Override
        public String getAdvertisedContract() {
            return ContractA.class.getName();
        }

        @Override
        public String getName() {
            return null;
        }
        
    };
    
    private ServiceLocator getLocator() {
        return LocatorHelper.getServiceLocator(
                Singleton1.class,
                Singleton2.class,
                Singleton3.class,
                Singleton4.class,
                Singleton5.class,
                Singleton6.class,
                Singleton7.class,
                Singleton8.class,
                Singleton9.class,
                Singleton10.class,
                Singleton11.class,
                Singleton12.class,
                Singleton13.class,
                Singleton14.class,
                Singleton15.class,
                Singleton16.class,
                Singleton17.class,
                Singleton18.class,
                Singleton19.class,
                Singleton20.class);
        
    }
    
    /**
     * Many threads looking up all of many services
     * using a Filter with no name but an interface
     * contract and looking for a specific qualifier
     */
    @Test // @org.junit.Ignore
    public void testManyThreadsGettingALotOfServices() throws Throwable {
        ServiceLocator locator = getLocator();
        
        Thread threads[] = new Thread[NUM_THREADS];
        Runner runners[] = new Runner[NUM_THREADS];
        
        for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
            runners[lcv] = new Runner(FILTER, NUM_ITERATIONS, locator, true);
            threads[lcv] = new Thread(runners[lcv]);
        }
        
        for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
            threads[lcv].start();
        }
        
        for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
            runners[lcv].isDone(20 * 1000);
        }
        
    }
    
    /**
     * Many threads looking up all of many services
     * using a Filter with no name but an interface
     * contract and looking for a specific qualifier.
     * This version of the test dynamically adds and
     * removes services during the run
     */
    @Test // @org.junit.Ignore
    public void testManyThreadsGettingALotOfServicesWithAddsAndRemoves() throws Throwable {
        ServiceLocator locator = getLocator();
        
        Thread threads[] = new Thread[NUM_THREADS];
        Runner runners[] = new Runner[NUM_THREADS];
        
        for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
            runners[lcv] = new Runner(FILTER, NUM_ITERATIONS, locator, false);
            threads[lcv] = new Thread(runners[lcv]);
        }
        
        for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
            threads[lcv].start();
        }
        
        for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
            runners[lcv].isDone(20 * 1000);
        }
        
    }
    
    private static class Runner implements Runnable {
        private final Object lock = new Object();
        private final Filter filter;
        private final int iterations;
        private final ServiceLocator locator;
        // If true swap ranks, if false do adds/removes
        private final boolean ranks;
        
        private Throwable exception;
        private boolean done = false;
        
        private Runner(Filter filter, int iterations, ServiceLocator locator, boolean ranks) {
            this.filter = filter;
            this.iterations = iterations;
            this.locator = locator;
            this.ranks = ranks;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                internalRun();
                synchronized (lock) {
                    done = true;
                    lock.notifyAll();
                }
            }
            catch (Throwable re) {
                synchronized (lock) {
                    exception = re;
                    done = true;
                    lock.notifyAll();
                }
            }
        }
        
        private void internalRun() throws Throwable {
            boolean oneOrNegativeOne = false;
            
            ActiveDescriptor<?> added = null;
            for (int lcv = 0; lcv < iterations; lcv++) {
                List<ServiceHandle<?>> allInterceptors = locator.getAllServiceHandles(filter);
                if (!ranks) {
                    Assert.assertTrue(allInterceptors.size() >= 20);
                }
                else {
                    Assert.assertEquals(20, allInterceptors.size());
                }
                
                if (ranks) {
                    if ((lcv % 5) == 0) {
                        if (oneOrNegativeOne) {
                            allInterceptors.get(0).getActiveDescriptor().setRanking(1);
                            oneOrNegativeOne = false;
                        }
                        else {
                            allInterceptors.get(0).getActiveDescriptor().setRanking(-1);
                            oneOrNegativeOne = true;
                        }
                    
                    }
                }
                else {
                    if ((lcv % 5) == 0) {
                        if (added == null) {
                            added = ServiceLocatorUtilities.addClasses(locator, SingletonExtra.class).get(0);
                        }
                        else {
                            ServiceLocatorUtilities.removeOneDescriptor(locator, added);
                            added = null;
                        }
                    }
                    
                }
            }
            
        }
        
        private void isDone(long timeout) throws Throwable {
            synchronized (lock) {
                while (!done && timeout > 0) {
                    long elapsedTime = System.currentTimeMillis();
                    
                    lock.wait(timeout);
                    
                    elapsedTime = System.currentTimeMillis() - elapsedTime;
                    timeout -= elapsedTime;
                }
                
                Assert.assertTrue(done);
                if (exception != null) {
                    throw exception;
                }
            }
            
        }
        
    }
    
    @Singleton @QualifierA
    private static class Singleton1 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton2 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton3 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton4 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton5 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton6 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton7 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton8 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton9 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton10 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton11 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton12 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton13 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton14 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton15 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton16 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton17 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton18 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton19 implements ContractA {
    }
    @Singleton @QualifierA
    private static class Singleton20 implements ContractA {
    }
    
    @Singleton @QualifierA
    private static class SingletonExtra implements ContractA {
    }

}
