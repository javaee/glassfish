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
package org.glassfish.hk2.tests.interception;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.extras.internal.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the default interception service
 * @author jwells
 */
public class DefaultInterceptionTest {
    /**
     * Tests that a non-intercepted method is not intercepted,
     * while an intercepted method is intercepted
     */
    @Test // @org.junit.Ignore
    public void testMethodInterception() {
        ServiceLocator locator = Utilities.getUniqueLocator(BasicRecordingInterceptor.class,
                InterceptedService.class);
        
        BasicRecordingInterceptor interceptor = locator.getService(BasicRecordingInterceptor.class);
        Assert.assertNotNull(interceptor);
        Assert.assertNull(interceptor.getLastInvocation());
        
        InterceptedService interceptedService = locator.getService(InterceptedService.class);
        Assert.assertNotNull(interceptedService);
        
        interceptedService.isIntercepted();
        
        MethodInvocation invocation = interceptor.getLastInvocation();
        Assert.assertNotNull(invocation);
        
        Assert.assertEquals("isIntercepted", invocation.getMethod().getName());
        
        interceptor.clear();
        
        interceptedService.isNotIntercepted();
        
        invocation = interceptor.getLastInvocation();
        Assert.assertNull(invocation);
    }
    
    private static void clearInterceptors(BasicRecordingInterceptor a,
            BasicRecordingInterceptor2 b,
            BasicRecordingInterceptor3 c) {
        a.clear();
        b.clear();
        c.clear();
    }
    
    /**
     * Has a class interceptor and services that have some interceptors and not others etc
     */
    @SuppressWarnings("deprecation")
    @Test // @org.junit.Ignore
    public void testComplexMethodInterception() {
        ServiceLocator locator = Utilities.getUniqueLocator(BasicRecordingInterceptor.class,
                BasicRecordingInterceptor2.class,
                BasicRecordingInterceptor3.class,
                ComplexInterceptedService.class);
        
        BasicRecordingInterceptor interceptor1 = locator.getService(BasicRecordingInterceptor.class);
        BasicRecordingInterceptor2 interceptor2 = locator.getService(BasicRecordingInterceptor2.class);
        BasicRecordingInterceptor3 interceptor3 = locator.getService(BasicRecordingInterceptor3.class);
        
        ComplexInterceptedService interceptedService = locator.getService(ComplexInterceptedService.class);
        Assert.assertNotNull(interceptedService);
        
        {
            interceptedService.interceptedByTwo();
        
            Assert.assertEquals("interceptedByTwo", interceptor1.getLastInvocation().getMethod().getName());
            Assert.assertEquals("interceptedByTwo", interceptor2.getLastInvocation().getMethod().getName());
            Assert.assertNull(interceptor3.getLastInvocation());
        }
        
        clearInterceptors(interceptor1, interceptor2, interceptor3);
        
        {
            interceptedService.interceptedByThree();
        
            Assert.assertEquals("interceptedByThree", interceptor1.getLastInvocation().getMethod().getName());
            Assert.assertNull(interceptor2.getLastInvocation());
            Assert.assertEquals("interceptedByThree", interceptor3.getLastInvocation().getMethod().getName());
        }
        
        clearInterceptors(interceptor1, interceptor2, interceptor3);
        
        {
            interceptedService.interceptedByClass();
        
            Assert.assertEquals("interceptedByClass", interceptor1.getLastInvocation().getMethod().getName());
            Assert.assertNull(interceptor2.getLastInvocation());
            Assert.assertNull(interceptor3.getLastInvocation());
        }
        
        {
            interceptedService.interceptedByAll();
        
            Assert.assertEquals("interceptedByAll", interceptor1.getLastInvocation().getMethod().getName());
            Assert.assertEquals("interceptedByAll", interceptor2.getLastInvocation().getMethod().getName());
            Assert.assertEquals("interceptedByAll", interceptor3.getLastInvocation().getMethod().getName());
        }
        
        {
            interceptedService.interceptedViaStereotype();
        
            Assert.assertEquals("interceptedViaStereotype", interceptor1.getLastInvocation().getMethod().getName());
            Assert.assertEquals("interceptedViaStereotype", interceptor2.getLastInvocation().getMethod().getName());
            Assert.assertEquals("interceptedViaStereotype", interceptor3.getLastInvocation().getMethod().getName());
        }
    }
    
    /**
     * Tests basic constructor interception
     */
    @Test // @org.junit.Ignore
    public void testConstructorInterception() {
        ServiceLocator locator = Utilities.getUniqueLocator(ConstructorRecordingInterceptor.class,
                ConstructorInterceptedService.class);
        
        ConstructorRecordingInterceptor interceptor = locator.getService(ConstructorRecordingInterceptor.class);
        Assert.assertNotNull(interceptor);
        Assert.assertNull(interceptor.getLastInvocation());
        
        ConstructorInterceptedService interceptedService = locator.getService(ConstructorInterceptedService.class);
        Assert.assertNotNull(interceptedService);
        
        ConstructorInvocation invocation = interceptor.getLastInvocation();
        Assert.assertNotNull(invocation);
        
        Assert.assertNotNull(invocation.getConstructor());
        
        Assert.assertEquals(ConstructorInterceptedService.class, invocation.getConstructor().getDeclaringClass());
    }
}
