/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.locator.interception1;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.glassfish.hk2.api.AOPProxyCtl;
import org.glassfish.hk2.api.ActiveDescriptor;
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
public class InterceptorTest {
    /**
     * Very simple intercepted service
     */
    @Test @org.junit.Ignore
    public void testMostBasicInterceptor() {
        ServiceLocator locator = LocatorHelper.create(null, new InterceptorModule());
        
        ServiceHandle<SimpleInterceptedService> sisHandle = locator.getServiceHandle(SimpleInterceptedService.class);
        SimpleInterceptedService sis = sisHandle.getService();
        
        sis.callMe();
        
        RecordingInterceptor ri = locator.getService(RecordingInterceptor.class);
        
        List<String> cim = ri.getCalledInMethod();
        Assert.assertEquals(1, cim.size());
        Assert.assertEquals("callMe", cim.get(0));
        
        Map<String, Object> com = ri.getCalledOutMethod();
        Assert.assertEquals(1, com.size());
        
        Object value = com.get("callMe");
        Assert.assertEquals(new Integer(0), value);
        
        Assert.assertTrue(sis instanceof AOPProxyCtl);
        
        ActiveDescriptor<?> fromHandle = sisHandle.getActiveDescriptor();
        ActiveDescriptor<?> fromProxy = ((AOPProxyCtl) sis).__getUnderlyingDescriptor();
        
        Assert.assertEquals(fromHandle, fromProxy);
    }
    
    /**
     * Tests an interceptor that does not proceed
     */
    @Test
    public void testNoProceedInterceptor() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                CountingService.class,
                NoProceedInterceptorService.class);
        
        CountingService counter = locator.getService(CountingService.class);
        
        counter.callMe();
        
        // Should be zero because the interceptor short-circuited it
        Assert.assertEquals(0, counter.gotCalled());
    }
    
    /**
     * Tests an interceptor that changes the input parameter
     */
    @Test
    public void testChangeInputInterceptor() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                RecordInputService.class,
                NegateTheInputInterceptorService.class);
        
        RecordInputService recorder = locator.getService(RecordInputService.class);
        
        recorder.recordInput(1);
        
        // Should be negative one because the interceptor negated the input
        Assert.assertEquals(-1, recorder.getLastInput());
    }
    
    /**
     * Tests an interceptor that changes the output parameter
     */
    @Test
    public void testChangeOutputInterceptor() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                EchoService.class,
                ReverseBooleanInterceptorService.class);
        
        EchoService echo = locator.getService(EchoService.class);
        
        boolean echoReturn = echo.echo(false);
        
        // Should be true because the interceptor modified the output
        Assert.assertTrue(echoReturn);
        
        echoReturn = echo.echo(true);
        
        // Should be false because the interceptor modified the output
        Assert.assertFalse(echoReturn);
    }
    
    /**
     * Tests an interceptor that changes the exception
     */
    @Test
    public void testChangeExceptionInterceptor() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                ThrowingService.class,
                ChangeExceptionInterceptorService.class);
        
        ThrowingService thrower = locator.getService(ThrowingService.class);
        
        try {
          thrower.throwy();
          Assert.fail("Should have thrown an exception");
        }
        catch (ExceptionB b) {
            // Success, because the interceptor changed from A to B
        }
    }
    
    /**
     * Tests all fields of the MethodInvocation passed to the interceptor
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     */
    @Test
    public void testAllFieldsOfMethodInvocation() throws NoSuchMethodException, SecurityException {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                RecordInputService.class,
                CheckInvocationInterceptorService.class);
        
        RecordInputService recorder = locator.getService(RecordInputService.class);
        
        recorder.recordInput(null);
        
        Object rawInput = recorder.getLastObjectInput();
        Assert.assertNotNull(rawInput);
        
        Assert.assertTrue(rawInput instanceof MethodInvocation);
        
        MethodInvocation invocation = (MethodInvocation) rawInput;
        
        Method expectedMethod = RecordInputService.class.getMethod("recordInput", Object.class);
        
        Assert.assertEquals(expectedMethod, invocation.getMethod());
        Assert.assertEquals(expectedMethod, invocation.getStaticPart());
        Assert.assertEquals(recorder, invocation.getThis());
        Assert.assertEquals(rawInput, invocation.getArguments()[0]);
    }
    
    /**
     * Ensures that multiple interceptors are called
     */
    @Test
    public void testMultipleInterceptors() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                AddingService.class,
                AddThreeInterceptorService.class);
        
        AddingService adder = locator.getService(AddingService.class);
        
        int result = adder.addOne(0);
        
        /**
         * +2 for one of the interceptors (before and after)
         * +2 for the second interceptor (before and after)
         * +2 for the third interceptor (before and after)
         * +1 for the service itself
         */
        Assert.assertEquals(7, result);
    }
    
    /**
     * Ensures that multiple interceptors can shortcut others
     * in the chain
     */
    @Test
    public void testMiddleInterceptorDoesNotProceed() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                RecordInputService.class,
                MiddleInterceptorNoProceedService.class);
        
        RecordInputService recorder = locator.getService(RecordInputService.class);
        
        recorder.recordInput(1000);
        
        // Should be zero because the middle interceptor did not proceed
        Assert.assertEquals(0, recorder.getLastInput());
        
        recorder.recordInput(new Object());
        
        // Should be null because the middle interceptor did not proceed
        Assert.assertEquals(null, recorder.getLastObjectInput());
    }
    
    /**
     * Tests an dynamically adding and removing interception
     * service works
     */
    @Test
    public void testDynamicInterceptionService() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                EchoService.class);
        
        EchoService echo = locator.getService(EchoService.class);
        
        boolean echoReturn = echo.echo(false);
        
        // Should be false because there is no interceptor yet
        Assert.assertFalse(echoReturn);
        
        List<ActiveDescriptor<?>> added = ServiceLocatorUtilities.addClasses(locator,
                ReverseBooleanInterceptorService.class);
        
        // Because EchoService is perLookup, this one should be intercepted
        echo = locator.getService(EchoService.class);
        
        echoReturn = echo.echo(false);
        
        // Should be true because of the interceptor
        Assert.assertTrue(echoReturn);
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, added.get(0));
        
        // Because EchoService is perLookup, this one should NOT be intercepted
        echo = locator.getService(EchoService.class);
        
        echoReturn = echo.echo(false);
        
        // Should be false because of there is no interceptor
        Assert.assertFalse(echoReturn);
    }
    
    /**
     * Tests that interceptors from *multiple* intercepted services are all called
     */
    @Test
    public void testInterceptorsFromMultipleInterceptionServices() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                RecordingInterceptor.class,
                InterceptorServiceImpl.class,
                InterceptorServiceImpl.class, // twice!
                SimpleInterceptedService.class);
        
        SimpleInterceptedService sis = locator.getService(SimpleInterceptedService.class);
        
        sis.callMe();
        
        RecordingInterceptor ri = locator.getService(RecordingInterceptor.class);
        
        List<String> inMethods = ri.getCalledInMethod();
        Assert.assertEquals(2,inMethods.size() );
        
        Assert.assertEquals("callMe", inMethods.get(0));
        Assert.assertEquals("callMe", inMethods.get(1));
    }
}
