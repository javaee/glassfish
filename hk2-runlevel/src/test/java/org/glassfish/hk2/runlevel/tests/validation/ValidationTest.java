/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.runlevel.tests.validation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.glassfish.hk2.api.MultiException;
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
public class ValidationTest {
    public final static int ZERO = 0;
    public final static int THREE = 3;
    public final static int FIVE = 5;
    
    /**
     * This test ensures a validating service will
     * fail if it created (with no outstanding work)
     */
    @Test
    public void testValidatingService() {
        ServiceLocator locator = Utilities.getServiceLocator(
                DependsOnValidatingLevelFiveService.class,
                LevelFiveService.class);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.proceedTo(4);  // Not five
        
        try {
            locator.getService(DependsOnValidatingLevelFiveService.class);
            Assert.fail("Should have failed as we are not at level 5");
        }
        catch (MultiException me) {
            Throwable topException = me.getErrors().get(0);
            Assert.assertTrue(topException instanceof IllegalStateException);
            
            Assert.assertTrue(topException.getMessage().contains(" but it has a run level of "));
        }
        
    }
    
    /**
     * This test ensures a non-validating service will
     * get properly created even if it is not the proper
     * level yet
     */
    @Test
    public void testNonValidatingService() {
        ServiceLocator locator = Utilities.getServiceLocator(
                DependsOnNonValidatingLevelFiveService.class,
                NonValidatingLevelFiveService.class);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.proceedTo(4);  // Not five
        
        locator.getService(DependsOnNonValidatingLevelFiveService.class);
    }
    
    /**
     * This test ensures a validating service will
     * fail if it is created (when going up)
     * @throws TimeoutException 
     * @throws InterruptedException 
     */
    @Test
    public void testValidatingServiceInProgress() throws InterruptedException, TimeoutException {
        ServiceLocator locator = Utilities.getServiceLocator(
                LevelThreeService.class,
                LevelFiveService.class);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        RunLevelFuture future = controller.proceedToAsync(FIVE);
        
        try {
            future.get(20, TimeUnit.SECONDS);
            Assert.fail("Should have failed as a service at level three depends on a service at level 5");
        }
        catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            Assert.assertTrue (cause instanceof MultiException);
            
            MultiException me = (MultiException) cause;
            Throwable th1 = me.getErrors().get(0);
            
            Assert.assertTrue(th1 instanceof MultiException);
            MultiException me1 = (MultiException) th1;
            Throwable th2 = me1.getErrors().get(0);
            
            Assert.assertTrue(th2 instanceof IllegalStateException);
            Assert.assertTrue(th2.getMessage().contains(" but it has a run level of "));
        }
        
    }
    
    /**
     * This test ensures a validating service will
     * fail if it is created (when going up)
     * @throws TimeoutException 
     * @throws InterruptedException 
     */
    @Test
    public void testNonValidatingServiceInProgress() throws InterruptedException, TimeoutException {
        ServiceLocator locator = Utilities.getServiceLocator(
                LevelThreeDependsOnLevelFiveNonValidating.class,
                NonValidatingLevelFiveService.class);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.proceedTo(FIVE);
        
        LevelThreeDependsOnLevelFiveNonValidating l3 = locator.getService(LevelThreeDependsOnLevelFiveNonValidating.class);
        NonValidatingLevelFiveService l5 = locator.getService(NonValidatingLevelFiveService.class);
        
        Assert.assertTrue(l3.isUp());
        Assert.assertTrue(l5.isUp());
        
        controller.proceedTo(THREE);
        
        // Even though we have gone below level 5, this level 5 service should STILL be up because
        // a level 3 service depended on it...
        Assert.assertTrue(l3.isUp());
        Assert.assertTrue(l5.isUp());
        
        controller.proceedTo(ZERO);
        
        // But now they should both come down
        Assert.assertFalse(l3.isUp());
        Assert.assertFalse(l5.isUp());
    }
    
    @Test
    public void testDirectlyGettingInvalidService() {
        ServiceLocator locator = Utilities.getServiceLocator(
                LevelFiveService.class);
        
        try {
            locator.getService(LevelFiveService.class);
        }
        catch (MultiException me) {
            Throwable th1 = me.getErrors().get(0);
            
            Assert.assertTrue(th1 instanceof IllegalStateException);
            Assert.assertTrue(th1.getMessage().contains(" but it has a run level of "));
        }
        
    }

}
