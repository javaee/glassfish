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
package org.glassfish.hk2.tests.locator.waiters.locator;

import javax.inject.Singleton;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class LocatorWaitersTest {
    private final static String TEST_NAME = "LocatorWaitersTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, null);
    
    private final static int DESCRIPTOR = 0;
    private final static int SERVICE = 1;
    private final static int HANDLE = 2;
    
    /**
     * Tests the basic case of waitForBestDescriptor
     * @throws InterruptedException 
     */
    @Test
    public void testBasicWaitForBestDescriptor() throws Throwable {
        Descriptor d = BuilderHelper.link(SimpleService1.class).
                in(Singleton.class.getName()).build();
        
        Filter filter = BuilderHelper.createContractFilter(SimpleService1.class.getName());
        
        WaitForBestDescriptorRunnable waitForever = new WaitForBestDescriptorRunnable(null, filter, DESCRIPTOR);
        WaitForBestDescriptorRunnable waitThirtySeconds = new WaitForBestDescriptorRunnable(new Long(30 * 1000), filter, DESCRIPTOR);
        WaitForBestDescriptorRunnable waitFiveMillis = new WaitForBestDescriptorRunnable(new Long(5), filter, DESCRIPTOR);
        
        Thread waitForeverThread = new Thread(waitForever);
        Thread waitThirtySecondsThread = new Thread(waitThirtySeconds);
        Thread waitFiveMillisThread = new Thread(waitFiveMillis);
        
        // Start all the threads.  Ensure the five millis guys fails to get an answer
        waitForeverThread.start();
        waitThirtySecondsThread.start();
        waitFiveMillisThread.start();
        
        Assert.assertNull(waitFiveMillis.waitForResult(20 * 1000));  // Should only take 5 millis
        
        Assert.assertFalse(waitForever.hasResult());
        Assert.assertFalse(waitThirtySeconds.hasResult());
        
        // OK, now we can add the descriptor
        ActiveDescriptor<?> removeMe = ServiceLocatorUtilities.addOneDescriptor(locator, d);
        try {
            ActiveDescriptor<?> foreverResult = (ActiveDescriptor<?>) waitForever.waitForResult(20 * 1000);
            ActiveDescriptor<?> thirtySecondsResult = (ActiveDescriptor<?>) waitForever.waitForResult(20 * 1000);
            
            Assert.assertNotNull(foreverResult);
            Assert.assertNotNull(thirtySecondsResult);
            
            Assert.assertSame(foreverResult, thirtySecondsResult);
            
            Assert.assertTrue(d.equals(foreverResult));
        }
        finally {
            ServiceLocatorUtilities.removeOneDescriptor(locator, removeMe);
        }
        
    }
    
    /**
     * Tests the basic case of waitForBestDescriptor
     * @throws InterruptedException 
     */
    @Test
    public void testBasicWaitForService() throws Throwable {
        Descriptor d = BuilderHelper.link(SimpleService1.class).
                in(Singleton.class.getName()).build();
        
        Filter filter = BuilderHelper.createContractFilter(SimpleService1.class.getName());
        
        WaitForBestDescriptorRunnable waitForever = new WaitForBestDescriptorRunnable(null, filter, SERVICE);
        WaitForBestDescriptorRunnable waitThirtySeconds = new WaitForBestDescriptorRunnable(new Long(30 * 1000), filter, SERVICE);
        WaitForBestDescriptorRunnable waitFiveMillis = new WaitForBestDescriptorRunnable(new Long(5), filter, SERVICE);
        
        Thread waitForeverThread = new Thread(waitForever);
        Thread waitThirtySecondsThread = new Thread(waitThirtySeconds);
        Thread waitFiveMillisThread = new Thread(waitFiveMillis);
        
        // Start all the threads.  Ensure the five millis guys fails to get an answer
        waitForeverThread.start();
        waitThirtySecondsThread.start();
        waitFiveMillisThread.start();
        
        Assert.assertNull(waitFiveMillis.waitForResult(20 * 1000));  // Should only take 5 millis
        
        Assert.assertFalse(waitForever.hasResult());
        Assert.assertFalse(waitThirtySeconds.hasResult());
        
        // OK, now we can add the descriptor
        ActiveDescriptor<?> removeMe = ServiceLocatorUtilities.addOneDescriptor(locator, d);
        try {
            SimpleService1 foreverResult = (SimpleService1) waitForever.waitForResult(20 * 1000);
            SimpleService1 thirtySecondsResult = (SimpleService1) waitForever.waitForResult(20 * 1000);
            
            Assert.assertNotNull(foreverResult);
            Assert.assertNotNull(thirtySecondsResult);
            
            Assert.assertSame(foreverResult, thirtySecondsResult);
        }
        finally {
            ServiceLocatorUtilities.removeOneDescriptor(locator, removeMe);
        }
        
    }
    
    /**
     * Tests the basic case of waitForBestDescriptor
     * @throws InterruptedException 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testBasicWaitForServiceHandle() throws Throwable {
        Descriptor d = BuilderHelper.link(SimpleService1.class).
                in(Singleton.class.getName()).build();
        
        Filter filter = BuilderHelper.createContractFilter(SimpleService1.class.getName());
        
        WaitForBestDescriptorRunnable waitForever = new WaitForBestDescriptorRunnable(null, filter, HANDLE);
        WaitForBestDescriptorRunnable waitThirtySeconds = new WaitForBestDescriptorRunnable(new Long(30 * 1000), filter, HANDLE);
        WaitForBestDescriptorRunnable waitFiveMillis = new WaitForBestDescriptorRunnable(new Long(5), filter, HANDLE);
        
        Thread waitForeverThread = new Thread(waitForever);
        Thread waitThirtySecondsThread = new Thread(waitThirtySeconds);
        Thread waitFiveMillisThread = new Thread(waitFiveMillis);
        
        // Start all the threads.  Ensure the five millis guys fails to get an answer
        waitForeverThread.start();
        waitThirtySecondsThread.start();
        waitFiveMillisThread.start();
        
        Assert.assertNull(waitFiveMillis.waitForResult(20 * 1000));  // Should only take 5 millis
        
        Assert.assertFalse(waitForever.hasResult());
        Assert.assertFalse(waitThirtySeconds.hasResult());
        
        // OK, now we can add the descriptor
        ActiveDescriptor<?> removeMe = ServiceLocatorUtilities.addOneDescriptor(locator, d);
        try {
            ServiceHandle<SimpleService1> foreverResult = (ServiceHandle<SimpleService1>) waitForever.waitForResult(20 * 1000);
            ServiceHandle<SimpleService1> thirtySecondsResult = (ServiceHandle<SimpleService1>) waitForever.waitForResult(20 * 1000);
            
            Assert.assertNotNull(foreverResult);
            Assert.assertNotNull(thirtySecondsResult);
            
            SimpleService1 foreverSs1 = foreverResult.getService();
            SimpleService1 thirtySecondSs1 = thirtySecondsResult.getService();
            
            Assert.assertNotNull(foreverSs1);
            Assert.assertNotNull(thirtySecondSs1);
            
            Assert.assertSame(foreverSs1, thirtySecondSs1);
        }
        finally {
            ServiceLocatorUtilities.removeOneDescriptor(locator, removeMe);
        }
        
    }
    
    private static class WaitForBestDescriptorRunnable implements Runnable {
        private final Object lock = new Object();
        private final Long waitTime;
        private final Filter filter;
        private final int apiType;  // 0 is BestDescriptor, 1 is Service, 2 is ServiceHandle
        
        private boolean gotResult = false;
        private Object retVal;
        private Throwable problem;
        
        private WaitForBestDescriptorRunnable(Long waitTime, Filter filter, int apiType) {
            this.waitTime = waitTime;
            this.filter = filter;
            this.apiType = apiType;
        }

        @Override
        public void run() {
            
            try {
                internalRun();
            }
            catch (Throwable me) {
                synchronized (lock) {
                    gotResult = true;
                    problem = me;
                    
                    lock.notify();
                }
            }
        }
        
        private void internalRun() throws Throwable {
            
            Object localRetVal;
            switch (apiType) {
            case DESCRIPTOR:
                if (waitTime == null) {
                    localRetVal = locator.waitForBestDescriptor(filter);
                }
                else {
                    localRetVal = locator.waitForBestDescriptor(waitTime, filter);
                }
                break;
            case SERVICE:
                if (waitTime == null) {
                    localRetVal = locator.waitForService(SimpleService1.class, null);
                }
                else {
                    localRetVal = locator.waitForService(waitTime, SimpleService1.class, null);
                }
                break;
            case HANDLE:
                if (waitTime == null) {
                    localRetVal = locator.waitForServiceHandle(SimpleService1.class, null);
                }
                else {
                    localRetVal = locator.waitForServiceHandle(waitTime, SimpleService1.class, null);
                }
                break;
            default:
                throw new AssertionError("Uknown API type: " + apiType);
            }

            synchronized (lock) {
                gotResult = true;
                retVal = localRetVal;
                
                lock.notify();
            }
        }
        
        public boolean hasResult() {
            return gotResult;
        }
        
        public Object waitForResult(long waitTime) throws Throwable {
            synchronized (lock) {
                while (!gotResult) {
                    lock.wait(waitTime);
                }
                
                if (problem != null) throw problem;
                
                return retVal;
            }
        }
        
    }

}
