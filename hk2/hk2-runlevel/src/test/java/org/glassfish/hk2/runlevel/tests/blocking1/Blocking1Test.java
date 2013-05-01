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
package org.glassfish.hk2.runlevel.tests.blocking1;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.tests.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class Blocking1Test {
    /**
     * This test has too many services that would block.  There
     * are three services that would block, but only two threads.
     * We want to make sure the system does not thrash around in
     * this scenario and just eventually gives up and blocks.  This
     * is tested by having a per-lookup service that all the services
     * have a dependency which counts.  It should only get created
     * a certain number of times.<OL>
     * <LI>for the service that actually blocks (BlockingService)</LI>
     * <LI>for DependingService(1 or 2) that will not block</LI>
     * <LI>for DependingService(2 or 1) that will not block</LI>
     * <LI>for DependingService(1 or 2) that WILL block this time</LI>
     * </OL>
     * @throws TimeoutException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    @Test
    public void testTooManyBlockers() throws InterruptedException, ExecutionException, TimeoutException {
        ServiceLocator locator = Utilities.getServiceLocator("Blocking1Test.testTooManyBlockers",
                BlockingService.class,
                DependingService1.class,
                DependingService2.class,
                CountingDependency.class);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setMaximumUseableThreads(2);
        
        RunLevelFuture future = controller.proceedToAsync(5);
        try {
            future.get(1, TimeUnit.SECONDS);
            Assert.fail("Should not have succeeded, the blocking service is still blocking");
        }
        catch (TimeoutException te) {
            // success
        }
        Assert.assertFalse(future.isDone());
        
        Assert.assertTrue("Expecting fewer than 4 creations, but got " + CountingDependency.getCount(),
                CountingDependency.getCount() <= 4);
        
        BlockingService.go();
        
        future.get();
        Assert.assertTrue(future.isDone());
    }

}
