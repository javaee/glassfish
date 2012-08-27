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
package org.glassfish.hk2.tests.locator.waiters.provider;

import javax.inject.Singleton;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
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
public class ProviderWaitersTest {
    private final static String TEST_NAME = "ProviderWaitersTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new ProviderWaitersModule());
    
    private final static int FIELD = 0;
    private final static int METHOD = 1;
    private final static int CONSTRUCTOR = 2;
    
    @SuppressWarnings("unchecked")
    @Test
    public void testProviderHandleFieldWaiting() throws Throwable {
        Descriptor d = BuilderHelper.link(SimpleService1.class).
                in(Singleton.class.getName()).build();
        
        WaitForRunnable waitForever = new WaitForRunnable(null, true, FIELD);
        WaitForRunnable waitThirtySeconds = new WaitForRunnable(new Long(30 * 1000), true, FIELD);
        WaitForRunnable waitFiveMillis = new WaitForRunnable(new Long(5), true, FIELD);
        
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
    
    @Test
    public void testProviderServiceFieldWaiting() throws Throwable {
        Descriptor d = BuilderHelper.link(SimpleService1.class).
                in(Singleton.class.getName()).build();
        
        WaitForRunnable waitForever = new WaitForRunnable(null, false, FIELD);
        WaitForRunnable waitThirtySeconds = new WaitForRunnable(new Long(30 * 1000), false, FIELD);
        WaitForRunnable waitFiveMillis = new WaitForRunnable(new Long(5), false, FIELD);
        
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
    
    @SuppressWarnings("unchecked")
    @Test
    public void testProviderHandleMethodWaiting() throws Throwable {
        Descriptor d = BuilderHelper.link(SimpleService1.class).
                in(Singleton.class.getName()).build();
        
        WaitForRunnable waitForever = new WaitForRunnable(null, true, METHOD);
        WaitForRunnable waitThirtySeconds = new WaitForRunnable(new Long(30 * 1000), true, METHOD);
        WaitForRunnable waitFiveMillis = new WaitForRunnable(new Long(5), true, METHOD);
        
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
    
    @Test
    public void testProviderServiceMethodWaiting() throws Throwable {
        Descriptor d = BuilderHelper.link(SimpleService1.class).
                in(Singleton.class.getName()).build();
        
        WaitForRunnable waitForever = new WaitForRunnable(null, false, METHOD);
        WaitForRunnable waitThirtySeconds = new WaitForRunnable(new Long(30 * 1000), false, METHOD);
        WaitForRunnable waitFiveMillis = new WaitForRunnable(new Long(5), false, METHOD);
        
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
    
    @SuppressWarnings("unchecked")
    @Test
    public void testProviderHandleConstructorWaiting() throws Throwable {
        Descriptor d = BuilderHelper.link(SimpleService1.class).
                in(Singleton.class.getName()).build();
        
        WaitForRunnable waitForever = new WaitForRunnable(null, true, CONSTRUCTOR);
        WaitForRunnable waitThirtySeconds = new WaitForRunnable(new Long(30 * 1000), true, CONSTRUCTOR);
        WaitForRunnable waitFiveMillis = new WaitForRunnable(new Long(5), true, CONSTRUCTOR);
        
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
    
    @Test
    public void testProviderServiceConstructorWaiting() throws Throwable {
        Descriptor d = BuilderHelper.link(SimpleService1.class).
                in(Singleton.class.getName()).build();
        
        WaitForRunnable waitForever = new WaitForRunnable(null, false, CONSTRUCTOR);
        WaitForRunnable waitThirtySeconds = new WaitForRunnable(new Long(30 * 1000), false, CONSTRUCTOR);
        WaitForRunnable waitFiveMillis = new WaitForRunnable(new Long(5), false, CONSTRUCTOR);
        
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
    
    private static class WaitForRunnable implements Runnable {
        private final Object lock = new Object();
        private final Long waitTime;
        private final int apiType;  // 0 is BestDescriptor, 1 is Service, 2 is ServiceHandle
        private final boolean getHandle;
        private final WaiterService waiterService;
        
        private boolean gotResult = false;
        private Object retVal;
        private Throwable problem;
        
        private WaitForRunnable(Long waitTime, boolean getHandle, int apiType) {
            this.waitTime = waitTime;
            this.getHandle = getHandle;
            this.apiType = apiType;
            
            waiterService = locator.getService(WaiterService.class);
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
            case FIELD:
                if (waitTime == null) {
                    if (getHandle) {
                        localRetVal = waiterService.fieldWaitForHandle();
                    }
                    else {
                        localRetVal = waiterService.fieldWaitForService();
                    }
                }
                else {
                    if (getHandle) {
                        localRetVal = waiterService.fieldWaitForHandle(waitTime);
                    }
                    else {
                        localRetVal = waiterService.fieldWaitForService(waitTime);
                    }
                }
                break;
            case METHOD:
                if (waitTime == null) {
                    if (getHandle) {
                        localRetVal = waiterService.methodWaitForHandle();
                    }
                    else {
                        localRetVal = waiterService.methodWaitForService();
                    }
                }
                else {
                    if (getHandle) {
                        localRetVal = waiterService.methodWaitForHandle(waitTime);
                    }
                    else {
                        localRetVal = waiterService.methodWaitForService(waitTime);
                    }
                }
                break;
            case CONSTRUCTOR:
                if (waitTime == null) {
                    if (getHandle) {
                        localRetVal = waiterService.constructorWaitForHandle();
                    }
                    else {
                        localRetVal = waiterService.constructorWaitForService();
                    }
                }
                else {
                    if (getHandle) {
                        localRetVal = waiterService.constructorWaitForHandle(waitTime);
                    }
                    else {
                        localRetVal = waiterService.constructorWaitForService(waitTime);
                    }
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
