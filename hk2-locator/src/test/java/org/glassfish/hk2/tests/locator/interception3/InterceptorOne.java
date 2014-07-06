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
package org.glassfish.hk2.tests.locator.interception3;

import junit.framework.Assert;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.glassfish.hk2.api.HK2Invocation;

/**
 * @author jwells
 *
 */
public class InterceptorOne implements MethodInterceptor {

    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    @Override
    public Object invoke(MethodInvocation arg0) throws Throwable {
        HK2Invocation invocation = (HK2Invocation) arg0;
        
        Assert.assertNull(invocation.getUserData(HK2InvocationTest.COUNTER_1));
        Assert.assertNull(invocation.getUserData(HK2InvocationTest.COUNTER_2));
        Assert.assertNull(invocation.getUserData(HK2InvocationTest.COUNTER_3));
        Assert.assertNull(invocation.getUserData(HK2InvocationTest.COUNTER_4));
        
        invocation.setUserData(HK2InvocationTest.COUNTER_1, new CounterService());
        invocation.setUserData(HK2InvocationTest.COUNTER_4, new CounterService());
        
        Object retVal = arg0.proceed();
        
        // Removed by two
        Assert.assertNull(invocation.getUserData(HK2InvocationTest.COUNTER_4));
        
        // Added by three and incremented by two on the way down
        Assert.assertEquals(1, ((CounterService) invocation.getUserData(HK2InvocationTest.COUNTER_3)).get());
        
        // Was removed by two on the way down
        Assert.assertNull(invocation.getUserData(HK2InvocationTest.COUNTER_2));
        
        // Incremented up and down by two and three
        Assert.assertEquals(4, ((CounterService) invocation.getUserData(HK2InvocationTest.COUNTER_1)).get());
        
        return retVal;
    }

}
