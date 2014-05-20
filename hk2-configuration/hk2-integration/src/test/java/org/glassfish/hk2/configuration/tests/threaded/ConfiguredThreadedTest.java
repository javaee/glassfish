/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.configuration.tests.threaded;

import java.util.HashMap;

import javax.inject.Inject;

import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.configuration.api.ConfigurationUtilities;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * @author jwells
 *
 */
public class ConfiguredThreadedTest extends HK2Runner {
    /* package */ final static String THREADED_TYPE = "ThreadedTest1";
    /* package */ final static String ANOTHER_THREADED_TYPE = "AnotherThreadedTest1";
    
    /* package */ final static String NAME_KEY = "name";
    
    private final static int NUM_RUNS = 10000;
    private final static int NUM_THREADS = 10;
    
    @Inject
    private Hub hub;
    
    @Before
    public void before() {
        super.before();
        
        ConfigurationUtilities.enableConfigurationSystem(testLocator);
    }
    
    private void addNamedBean(String typeName, String name) {
        for (;;) {
            WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
            WriteableType wt = wbd.findOrAddWriteableType(typeName);
        
            HashMap<String, Object> namedBean = new HashMap<String, Object>();
            namedBean.put(NAME_KEY, name);
        
            wt.addInstance(name, namedBean);
        
            try {
                wbd.commit();
                return;
            }
            catch (IllegalStateException ise) {
                // lost race
            }
        }
    }
    
    private void removeType(String typeName) {
        for (;;) {
            WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
            wbd.removeType(typeName);
        
            try {
                wbd.commit();
                return;
            }
            catch (IllegalStateException ise) {
                // lost race
            }
        }
    }
    
    private void removeNamedBean(String typeName, String name) {
        for (;;) {
            WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        
            WriteableType wt = wbd.findOrAddWriteableType(typeName);
        
            wt.removeInstance(name);
        
            try {
                wbd.commit();
                return;
            }
            catch (IllegalStateException ise) {
                // Lost race
            }
        }
    }
    
    /**
     * Tests that many threads banging against the context will work
     * 
     * @throws Throwable 
     */
    @Test @Ignore
    public void testThreadedConfiguredService() throws Throwable {
        Thread threads[] = new Thread[NUM_THREADS];
        Runner runners[] = new Runner[NUM_THREADS];
        
        for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
            runners[lcv] = new Runner("Name_" + lcv);
            threads[lcv] = new Thread(runners[lcv]);
        }
        
        try {
            addNamedBean(ANOTHER_THREADED_TYPE, "");
            
            for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
                threads[lcv].start();
            }
        
        
            for (int lcv = 0; lcv < NUM_THREADS; lcv++) {
                runners[lcv].waitForCompletion(20 * 1000);
            }
        }
        finally {
            removeType(THREADED_TYPE);
            removeType(ANOTHER_THREADED_TYPE);
        }
        
    }
    
    private class Runner implements Runnable {
        private final String myName;
        private final Object lock = new Object();
        
        private boolean done = false;
        private Throwable error;
        
        private Runner(String name) {
            myName = name;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                internalRun();
            }
            catch (Throwable th) {
                synchronized (lock) {
                    error = th;
                    done = true;
                    lock.notify();
                    return;
                }
            }
            
            synchronized (lock) {
                done = true;
                lock.notify();
            }
        }
        
        private void internalRun() throws Throwable {
            for (int lcv = 0; lcv < NUM_RUNS; lcv++) {
                Assert.assertNull(testLocator.getService(ConfiguredService.class, myName));
                
                addNamedBean(THREADED_TYPE, myName);
                
                ConfiguredService cs = testLocator.getService(ConfiguredService.class, myName);
                Assert.assertNotNull(cs);
                
                Assert.assertEquals(cs.getName(), myName);
                
                Assert.assertTrue(cs.runOthersDown() > 0);
                
                removeNamedBean(THREADED_TYPE, myName);
                
                Assert.assertNull(testLocator.getService(ConfiguredService.class, myName));
                
                // Adds nastiness, this service will often need to be
                // recreated on the stack now
                ServiceHandle<AnotherConfiguredService> handle =
                        testLocator.getServiceHandle(AnotherConfiguredService.class);
                handle.destroy();
            }
        }
        
        private void waitForCompletion(long waitTime) throws Throwable {
            synchronized (lock) {
                while (waitTime > 0L && !done) {
                    long elapsedTime = System.currentTimeMillis();
                    
                    lock.wait(waitTime);
                    
                    elapsedTime = System.currentTimeMillis() - elapsedTime;
                    
                    waitTime -= elapsedTime;
                }
                
                if (!done) {
                    Assert.fail("Did not complete in the allotted time");
                }
                
                if (error != null) {
                    throw error;
                }
            }
        }
        
    }

}
