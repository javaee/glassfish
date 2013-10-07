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
package org.glassfish.hk2.runlevel.tests.cancel;

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
public class CancelTest {
    /**
     * Tests that forced cancel will work even if services are blocking
     * @throws TimeoutException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    @Test 
    public void testForcedCancel() throws InterruptedException, ExecutionException, TimeoutException {
        ServiceLocator locator = Utilities.getServiceLocator(BlockingService.class);
        
        BlockingService.clear();
        
        RunLevelController rlc = locator.getService(RunLevelController.class);
        rlc.setThreadingPolicy(ThreadingPolicy.FULLY_THREADED);
        rlc.setCancelTimeoutMilliseconds(500);
        
        Assert.assertEquals(500L, rlc.getCancelTimeoutMilliseconds());
        
        try {
            rlc.proceedToAsync(5);
        
            RunLevelFuture future = rlc.getCurrentProceeding();
            Assert.assertNotNull(future);
            
            // Makes sure the postConstruct of the BlockingService has been invoked
            BlockingService.waitForPostConstruct();
            
            Assert.assertFalse(future.isDone());
        
            // This should force the issue
            future.cancel(true);
        
            future.get(10, TimeUnit.SECONDS);
        
            Assert.assertTrue(future.isDone());
        }
        finally {
            // Don't hold onto this thread forever
            BlockingService.go();
        }
        
        // Note that after the blocking service has been told to go, it should
        // now be possible to go back up to five.  This sleep gives time to
        // let the BlockingService complete
        Thread.sleep(100);
        
        Assert.assertEquals(4, rlc.getCurrentRunLevel());
        
        // A hard-cancelled service should NOT be pre-destroyed
        Assert.assertFalse(BlockingService.getPreDestroyCalled());
        
        // Now reset blocking service, but also let it go to ensure
        // that since it did clear out it CAN be started again
        
        BlockingService.clear();
        BlockingService.go();
        
        // Should work
        rlc.proceedTo(5);
        
        Assert.assertTrue(BlockingService.getPostConstructCalled());
    }
    
    /**
     * This test has one thread where the DependsOnBlockingService depends
     * on the BlockingService which will sit around until we tell it to go.
     * We do a non-force cancel and then let the BlockingService go.  The
     * DependsOnBlockingService should NOT be postConstructed because the
     * cancel should have cancelled it
     * @throws InterruptedException 
     * @throws TimeoutException 
     * @throws ExecutionException 
     */
    @Test
    public void testThingsDependingOnServicesCaughtByCancelAreNotCreated() throws InterruptedException, ExecutionException, TimeoutException {
        // NOTE: DependsOnBlockingService MUST come first so that it'll be
        // on the stack in the one thread
        ServiceLocator locator = Utilities.getServiceLocator(DependsOnBlockingService.class,
                BlockingService.class);
        
        BlockingService.clear();
        DependsOnBlockingService.clear();
        
        try {
            RunLevelController rlc = locator.getService(RunLevelController.class);
            rlc.setThreadingPolicy(ThreadingPolicy.FULLY_THREADED);
            rlc.setMaximumUseableThreads(1);
            
            rlc.proceedToAsync(5);
            
            RunLevelFuture future = rlc.getCurrentProceeding();
            Assert.assertNotNull(future);
            
            // Makes sure the postConstruct of the BlockingService has been invoked
            BlockingService.waitForPostConstruct();
            
            Assert.assertFalse(future.isDone());
        
            // Do NOT force the issue, let the thing complete
            future.cancel(false);
            
            BlockingService.go();
            
            // The DependsOnMeBlocking service should NOT be started/initialized
            
            future.get(5, TimeUnit.SECONDS);
            
            Assert.assertTrue(future.isDone());
            
            Assert.assertFalse(DependsOnBlockingService.getPostConstructCalled());
            
            Assert.assertTrue(BlockingService.getPreDestroyCalled());
            
        }
        finally {
            BlockingService.go();
            
        }
        
    }

}
