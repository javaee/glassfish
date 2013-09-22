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
package org.glassfish.hk2.runlevel.tests.thrusingleton;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelController.ThreadingPolicy;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.tests.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author jwells
 *
 */
public class ThruSingletonTest {
    /**
     * Tests that run-level services may get started through other contexts, such
     * as singleton context (even if there is a blocking condition)
     * @throws InterruptedException
     * @throws TimeoutException 
     * @throws ExecutionException 
     */
    @Test
    public void testRunLevelServiceStartedThroughSingletonAndBlocking() throws InterruptedException, ExecutionException, TimeoutException {
        ServiceLocator locator = Utilities.getServiceLocator(
                BlockingService.class,
                HighPriorityServiceOne.class,
                HighPriorityServiceTwo.class,
                LowPriorityForceService.class,
                SingletonServiceA.class);
        
        BlockingService.reset();
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.setThreadingPolicy(ThreadingPolicy.FULLY_THREADED);
        controller.setMaximumUseableThreads(2);
        
        RunLevelFuture future = controller.proceedToAsync(5);
        
        Thread.sleep(100);
        
        Assert.assertFalse(future.isDone());
        
        BlockingService.go();
        
        future.get(1, TimeUnit.HOURS);
        
        Assert.assertTrue(future.isDone());
        
        Assert.assertEquals(5, controller.getCurrentRunLevel());   
    }
    
    /**
     * Tests that run-level services may get started through other contexts, such
     * as singleton context (even if there is a blocking condition)
     * @throws InterruptedException
     * @throws TimeoutException 
     * @throws ExecutionException 
     */
    @Test
    public void testRunLevelServiceStartedThroughPerLookupAndBlocking() throws InterruptedException, ExecutionException, TimeoutException {
        ServiceLocator locator = Utilities.getServiceLocator(
                BlockingService.class,
                HighPriorityServiceOne.class,
                HighPriorityServiceTwo.class,
                LowPriorityForceService.class,
                PerLookupService.class);
        BlockingService.reset();
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.setThreadingPolicy(ThreadingPolicy.FULLY_THREADED);
        controller.setMaximumUseableThreads(2);
        
        RunLevelFuture future = controller.proceedToAsync(5);
        
        Thread.sleep(100);
        
        Assert.assertFalse(future.isDone());
        
        BlockingService.go();
        
        future.get(1, TimeUnit.HOURS);
        
        Assert.assertTrue(future.isDone());
        
        Assert.assertEquals(5, controller.getCurrentRunLevel());   
    }

}
