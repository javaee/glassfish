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
package org.glassfish.hk2.tests.locator.interception2;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Singleton;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InterceptionService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ConstructorInterceptorTest {
    /**
     * Tests that an interceptor is called on a very basic service
     */
    @Test
    public void testBasicConstructorInterception() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(SimpleService.class,
                RecordingInterceptorService.class,
                RecordingInterceptor.class);
        
        locator.getService(SimpleService.class);
        
        RecordingInterceptor recorder = locator.getService(RecordingInterceptor.class);
        
        List<Constructor<?>> cCalled = recorder.getConstructorsCalled();
        Assert.assertEquals(1, cCalled.size());
        
    }
    
    /**
     * Ensures that all of the interceptors in a chain get called
     */
    @Test
    public void testMultipleConstructorInterceptors() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(SimpleService.class,
                ThreeInterceptionService.class);
        
        ThreeInterceptionService tis = locator.getService(ThreeInterceptionService.class);
        
        locator.getService(SimpleService.class);
        
        List<ConstructorInterceptor> interceptors = tis.getConstructorInterceptors(null);
        
        InterceptorOne i1 = (InterceptorOne) interceptors.get(0);
        InterceptorTwo i2 = (InterceptorTwo) interceptors.get(1);
        InterceptorThree i3 = (InterceptorThree) interceptors.get(2);
        
        Assert.assertEquals(1, i1.wasCalled());
        Assert.assertEquals(1, i2.wasCalled());
        Assert.assertEquals(1, i3.wasCalled());
    }
    
    private static class InterceptorOne implements ConstructorInterceptor {
        private int called;

        @Override
        public Object construct(ConstructorInvocation invocation) throws Throwable {
            called++;
            return invocation.proceed();
        }
        
        private int wasCalled() { return called; }
        
    }
    
    private static class InterceptorTwo implements ConstructorInterceptor {
        private int called;

        @Override
        public Object construct(ConstructorInvocation invocation) throws Throwable {
            called++;
            return invocation.proceed();
        }
        
        private int wasCalled() { return called; }
        
    }
    
    private static class InterceptorThree implements ConstructorInterceptor {
        private int called;

        @Override
        public Object construct(ConstructorInvocation invocation) throws Throwable {
            called++;
            return invocation.proceed();
        }
        
        private int wasCalled() { return called; }
    }
    
    @Singleton
    private static class ThreeInterceptionService implements InterceptionService {
        private final LinkedList<ConstructorInterceptor> interceptors;
        
        private ThreeInterceptionService() {
            interceptors = new LinkedList<ConstructorInterceptor>();
            
            interceptors.add(new InterceptorOne());
            interceptors.add(new InterceptorTwo());
            interceptors.add(new InterceptorThree());
        }

        @Override
        public Filter getDescriptorFilter() {
            return BuilderHelper.createContractFilter(SimpleService.class.getName());
        }

        @Override
        public List<MethodInterceptor> getMethodInterceptors(Method method) {
            return null;
        }

        @Override
        public List<ConstructorInterceptor> getConstructorInterceptors(
                Constructor<?> constructor) {
            return interceptors;
        }
        
    }
}
