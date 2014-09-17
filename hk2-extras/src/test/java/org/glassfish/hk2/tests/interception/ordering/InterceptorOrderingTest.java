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
package org.glassfish.hk2.tests.interception.ordering;

import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.extras.internal.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class InterceptorOrderingTest {
    /**
     * Tests that we can reverse the natural ordering
     * of interceptors
     */
    @Test // @org.junit.Ignore
    public void testReverseOrdering() {
        ServiceLocator locator = Utilities.getUniqueLocator(AService.class,
                ConstructorInterceptorOne.class,
                ConstructorInterceptorTwo.class,
                ConstructorInterceptorThree.class,
                Recorder.class,
                MethodInterceptorOne.class,
                MethodInterceptorTwo.class,
                MethodInterceptorThree.class,
                Reverser.class);
        
        AService aService = locator.getService(AService.class);
        aService.callMe();
        
        Recorder recorder = locator.getService(Recorder.class);
        
        // Three constructor interceptors, three method interceptors
        List<Object> interceptors = recorder.get();
        
        Assert.assertEquals(6, interceptors.size());
        
        // The order should be REVERSED (3-2-1)
        Assert.assertEquals(ConstructorInterceptorThree.class, interceptors.get(0).getClass());
        Assert.assertEquals(ConstructorInterceptorTwo.class, interceptors.get(1).getClass());
        Assert.assertEquals(ConstructorInterceptorOne.class, interceptors.get(2).getClass());
        
        Assert.assertEquals(MethodInterceptorThree.class, interceptors.get(3).getClass());
        Assert.assertEquals(MethodInterceptorTwo.class, interceptors.get(4).getClass());
        Assert.assertEquals(MethodInterceptorOne.class, interceptors.get(5).getClass());
        
    }
    
    /**
     * Has multiple ordering services, one of which returns the original
     * list, one of which is the reverser and one of which adds services
     * to the start and end of the list (and runs last due to low rank)
     */
    @Test // @org.junit.Ignore
    public void testOrderingServiceChain() {
        ServiceLocator locator = Utilities.getUniqueLocator(AService.class,
                ConstructorInterceptorOne.class,
                ConstructorInterceptorTwo.class,
                ConstructorInterceptorThree.class,
                Recorder.class,
                MethodInterceptorOne.class,
                MethodInterceptorTwo.class,
                MethodInterceptorThree.class,
                AddToBeginningAndEndOrderer.class,
                Reverser.class,
                DoNothingOrderer.class);
        
        AService aService = locator.getService(AService.class);
        aService.callMe();
        
        Recorder recorder = locator.getService(Recorder.class);
        
        // Three constructor interceptors, three method interceptors
        List<Object> interceptors = recorder.get();
        
        Assert.assertEquals(10, interceptors.size());
        
        // Zero should be first (and NOT re-ordered)
        Assert.assertEquals(NonServiceConstructorInterceptorZero.class, interceptors.get(0).getClass());
        
        // The order should be REVERSED (3-2-1)
        Assert.assertEquals(ConstructorInterceptorThree.class, interceptors.get(1).getClass());
        Assert.assertEquals(ConstructorInterceptorTwo.class, interceptors.get(2).getClass());
        Assert.assertEquals(ConstructorInterceptorOne.class, interceptors.get(3).getClass());
        
        // Inifinity should be last (and NOT re-ordered)
        Assert.assertEquals(NonServiceConstructorInterceptorInfinity.class, interceptors.get(4).getClass());
        
        // Zero should be first (and NOT re-ordered)
        Assert.assertEquals(NonServiceMethodInterceptorZero.class, interceptors.get(5).getClass());
        
        // The order should be REVERSED (3-2-1)
        Assert.assertEquals(MethodInterceptorThree.class, interceptors.get(6).getClass());
        Assert.assertEquals(MethodInterceptorTwo.class, interceptors.get(7).getClass());
        Assert.assertEquals(MethodInterceptorOne.class, interceptors.get(8).getClass());
        
        // Infinity should be last (and NOT re-ordered)
        Assert.assertEquals(NonServiceMethodInterceptorInfinity.class, interceptors.get(9).getClass());
    }

}
