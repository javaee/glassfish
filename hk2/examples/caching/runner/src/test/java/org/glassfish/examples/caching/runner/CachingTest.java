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
package org.glassfish.examples.caching.runner;

import junit.framework.Assert;

import org.glassfish.examples.caching.services.ExpensiveConstructor;
import org.glassfish.examples.caching.services.ExpensiveMethods;
import org.glassfish.examples.caching.services.InputFactory;
import org.junit.Test;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * These are junit tests that ensure that the caching
 * interceptors are working properly
 * 
 * @author jwells
 *
 */
public class CachingTest extends HK2Runner {
    /**
     * Tests that the expensive method on the ExpensiveMethods class is
     * properly cached
     */
    @Test
    public void testMethodsAreIntercepted() {
        ExpensiveMethods expensiveMethods = testLocator.getService(ExpensiveMethods.class);
        
        // Ensure that we are at zero calls to the expensive method
        expensiveMethods.clear();
        Assert.assertEquals(0, expensiveMethods.getNumTimesCalled());
        
        // Now call the expensive method
        int result = expensiveMethods.veryExpensiveCalculation(1);
        Assert.assertEquals(2, result);
        
        // The expensive method should have been called
        Assert.assertEquals(1, expensiveMethods.getNumTimesCalled());
        
        // Now call the expensive method ten more times
        for (int i = 0; i < 10; i++) {
            result = expensiveMethods.veryExpensiveCalculation(1);
            Assert.assertEquals(2, result);
        }
        
        // But the expensive call was never made again, since the result was cached!
        Assert.assertEquals(1, expensiveMethods.getNumTimesCalled());
        
        // Now call it again, with a different input
        result = expensiveMethods.veryExpensiveCalculation(2);
        Assert.assertEquals(3, result);
        
        // The expensive method was called again since it had not seen 2 before
        Assert.assertEquals(2, expensiveMethods.getNumTimesCalled());
        
        // Now call the expensive method with both 1 and 2 several times
        for (int i = 0; i < 10; i++) {
            result = expensiveMethods.veryExpensiveCalculation(1);
            Assert.assertEquals(2, result);
            
            result = expensiveMethods.veryExpensiveCalculation(2);
            Assert.assertEquals(3, result);
        }
        
        // But the expensive method was not called again, as the results were cached
        Assert.assertEquals(2, expensiveMethods.getNumTimesCalled());
        
        
    }
    
    @Test
    public void testConstructorsAreIntercepted() {
        // Clears out the class so we can see how many times it is created
        ExpensiveConstructor.clear();
        
        // Gets the factory that changes the input to the ExpensiveConstructor constructor
        InputFactory inputFactory = testLocator.getService(InputFactory.class);
        
        inputFactory.setInput(2);
        
        // ExpensiveConstructor is PerLookup and is therefor nominally created for every lookup
        ExpensiveConstructor instanceOne = testLocator.getService(ExpensiveConstructor.class);
        
        // The real calculation is to multiply by two, ensure we got a good service
        int computation = instanceOne.getComputation();
        Assert.assertEquals(4, computation);
        
        // Also see that the service was created once
        Assert.assertEquals(1, ExpensiveConstructor.getNumTimesConstructed());
        
        // Now look it up again, only this time it will NOT be created (since it'll use the one from the intercepted cache)
        ExpensiveConstructor instanceTwo = testLocator.getService(ExpensiveConstructor.class);
        
        // Same object should have same computation
        computation = instanceTwo.getComputation();
        Assert.assertEquals(4, computation);
        
        // But amazingly, the object was NOT recreated:
        Assert.assertEquals(1, ExpensiveConstructor.getNumTimesConstructed());
        
        // Further proof that it was not recreated:
        Assert.assertTrue(instanceOne == instanceTwo);
        
        // Now change the input paramter
        inputFactory.setInput(8);
        
        // Lookup the service again
        ExpensiveConstructor instanceThree = testLocator.getService(ExpensiveConstructor.class);
        
        // This time the calculation should be different
        computation = instanceThree.getComputation();
        Assert.assertEquals(16, computation);
        
        // And this time the objects are NOT the same
        Assert.assertFalse(instanceOne.equals(instanceThree));
    }
} 
