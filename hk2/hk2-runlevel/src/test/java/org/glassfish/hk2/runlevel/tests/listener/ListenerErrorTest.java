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
package org.glassfish.hk2.runlevel.tests.listener;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.ErrorInformation;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelController.ThreadingPolicy;
import org.glassfish.hk2.runlevel.tests.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ListenerErrorTest {
    private static void setupErrorChanger(ServiceLocator locator, ErrorInformation.ErrorAction action) {
        locator.getService(OnProgressLevelChangerListener.class).setErrorAction(action);
    }
    
    /**
     * Ensures we can ignore failures when going up
     */
    @Test
    public void testKeepGoingUpWithIgnoreAction() {
        ServiceLocator locator = Utilities.getServiceLocator(LevelFiveErrorService.class,
                LevelFiveUpService.class,
                OnProgressLevelChangerListener.class);
        
        LevelFiveUpService.postConstructCalled = false;
        
        setupErrorChanger(locator, ErrorInformation.ErrorAction.IGNORE);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.proceedTo(10);
        
        Assert.assertTrue(LevelFiveUpService.postConstructCalled);
        
        // Should go all the way up because we ignored the error
        Assert.assertEquals(10, controller.getCurrentRunLevel());
    }
    
    /**
     * Ensures we can ignore failures when going up
     */
    @Test
    public void testKeepGoingUpWithIgnoreActionSingleThread() {
        ServiceLocator locator = Utilities.getServiceLocator(LevelFiveErrorService.class,
                LevelFiveUpService.class,
                OnProgressLevelChangerListener.class);
        
        LevelFiveUpService.postConstructCalled = false;
        
        setupErrorChanger(locator, ErrorInformation.ErrorAction.IGNORE);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setMaximumUseableThreads(1);
        
        controller.proceedTo(10);
        
        Assert.assertTrue(LevelFiveUpService.postConstructCalled);
        
        // Should go all the way up because we ignored the error
        Assert.assertEquals(10, controller.getCurrentRunLevel());
    }
    
    /**
     * Ensures we can ignore failures when going up
     */
    @Test
    public void testComingDownDoesNotCallOtherServices() {
        ServiceLocator locator = Utilities.getServiceLocator(LevelFiveErrorService.class,
                LevelFiveUpService.class,
                OnProgressLevelChangerListener.class);
        
        LevelFiveUpService.postConstructCalled = false;
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setMaximumUseableThreads(1);
        
        try {
            controller.proceedTo(10);
            Assert.fail("Should have failed at level 5");
        }
        catch (MultiException me) {
            // Expected exception
        }
        
        Assert.assertFalse(LevelFiveUpService.postConstructCalled);
        
        // Should go all the way up because we ignored the error
        Assert.assertEquals(4, controller.getCurrentRunLevel());
    }
    
    /**
     * Ensures we can ignore failures when going up
     */
    @Test
    public void testKeepGoingUpWithIgnoreActionNoThreads() {
        ServiceLocator locator = Utilities.getServiceLocator(LevelFiveErrorService.class,
                LevelFiveUpService.class,
                OnProgressLevelChangerListener.class);
        
        LevelFiveUpService.postConstructCalled = false;
        
        setupErrorChanger(locator, ErrorInformation.ErrorAction.IGNORE);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setThreadingPolicy(ThreadingPolicy.USE_NO_THREADS);
        
        controller.proceedTo(10);
        
        Assert.assertTrue(LevelFiveUpService.postConstructCalled);
        
        // Should go all the way up because we ignored the error
        Assert.assertEquals(10, controller.getCurrentRunLevel());
    }
    
    /**
     * Ensures we can ignore failures when going up
     */
    @Test
    public void testComingDownDoesNotCallOtherServicesNoThreads() {
        ServiceLocator locator = Utilities.getServiceLocator(LevelFiveErrorService.class,
                LevelFiveUpService.class,
                OnProgressLevelChangerListener.class);
        
        LevelFiveUpService.postConstructCalled = false;
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setThreadingPolicy(ThreadingPolicy.USE_NO_THREADS);
        
        try {
            controller.proceedTo(10);
            Assert.fail("Should have failed at level 5");
        }
        catch (MultiException me) {
            // Expected exception
        }
        
        Assert.assertFalse(LevelFiveUpService.postConstructCalled);
        
        // Should go all the way up because we ignored the error
        Assert.assertEquals(4, controller.getCurrentRunLevel());
    }
    
    /**
     * Ensures the user can halt the downward level progression if a service
     * failed when going down
     */
    @Test
    public void testHaltLevelRegressionOnError() {
        ServiceLocator locator = Utilities.getServiceLocator(LevelFiveDownErrorService.class,
                LevelFiveService.class,
                OnProgressLevelChangerListener.class);
        
        setupErrorChanger(locator, ErrorInformation.ErrorAction.GO_TO_NEXT_LOWER_LEVEL_AND_STOP);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.proceedTo(10);
        
        // Should go all the way up because we ignored the error
        Assert.assertEquals(10, controller.getCurrentRunLevel());
        
        LevelFiveService levelFiveService = locator.getService(LevelFiveService.class);
        Assert.assertFalse(levelFiveService.isPreDestroyCalled());
        
        controller.proceedTo(0);
        
        // Should get halted
        Assert.assertEquals(4, controller.getCurrentRunLevel());
        
        OnProgressLevelChangerListener listener = locator.getService(OnProgressLevelChangerListener.class);
        
        Assert.assertEquals(4, listener.getLatestOnProgress());
        
        Assert.assertTrue(levelFiveService.isPreDestroyCalled());
    }
    
    /**
     * Ensures the user can halt the downward level progression if a service
     * failed when going down
     */
    @Test
    public void testHaltLevelRegressionOnErrorNoThreads() {
        ServiceLocator locator = Utilities.getServiceLocator(LevelFiveDownErrorService.class,
                LevelFiveService.class,
                OnProgressLevelChangerListener.class);
        
        setupErrorChanger(locator, ErrorInformation.ErrorAction.GO_TO_NEXT_LOWER_LEVEL_AND_STOP);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setThreadingPolicy(ThreadingPolicy.USE_NO_THREADS);
        
        controller.proceedTo(10);
        
        // Should go all the way up because we ignored the error
        Assert.assertEquals(10, controller.getCurrentRunLevel());
        
        LevelFiveService levelFiveService = locator.getService(LevelFiveService.class);
        Assert.assertFalse(levelFiveService.isPreDestroyCalled());
        
        controller.proceedTo(0);
        
        // Should get halted
        Assert.assertEquals(4, controller.getCurrentRunLevel());
        
        OnProgressLevelChangerListener listener = locator.getService(OnProgressLevelChangerListener.class);
        
        Assert.assertEquals(4, listener.getLatestOnProgress());
        
        Assert.assertTrue(levelFiveService.isPreDestroyCalled());
    }
    
    /**
     * Ensures that the expected exception was given to the error handler
     */
    @Test
    public void testCheckDescriptorFromErrorInformationOnWayUp() {
        ServiceLocator locator = Utilities.getServiceLocator(
                LevelFiveService.class,
                LevelFiveErrorService.class,
                OnProgressLevelChangerListener.class);
        
        OnProgressLevelChangerListener listener = locator.getService(OnProgressLevelChangerListener.class);
        listener.reset();
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setThreadingPolicy(ThreadingPolicy.USE_NO_THREADS);
        
        try {
          controller.proceedTo(5);
          Assert.fail("Should have failed at level 5");
        }
        catch (MultiException me) {
            
        }
        
        // Sanity check
        Assert.assertEquals(4, controller.getCurrentRunLevel());
        
        ErrorInformation errorInfo = listener.getLastErrorInformation();
        Assert.assertNotNull(errorInfo);
        
        Descriptor failedDescriptor = errorInfo.getFailedDescriptor();
        Assert.assertNotNull(failedDescriptor);
        
        Assert.assertEquals(failedDescriptor.getImplementation(), LevelFiveErrorService.class.getName());
    }
    
    /**
     * Tests that the error handler is only called once when going up
     */
    @Test
    public void testErrorHandlerOnlyCalledOnceUp() {
        ServiceLocator locator = Utilities.getServiceLocator(
                LevelFiveErrorService.class,
                OnProgressLevelChangerListener.class);
        
        OnProgressLevelChangerListener listener = locator.getService(OnProgressLevelChangerListener.class);
        listener.reset();
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setThreadingPolicy(ThreadingPolicy.USE_NO_THREADS);
        
        try {
          controller.proceedTo(5);
          Assert.fail("Should have failed at level 5");
        }
        catch (MultiException me) {
            
        }
        
        // Sanity check
        Assert.assertEquals(4, controller.getCurrentRunLevel());
        
        Assert.assertEquals(1, listener.getNumOnErrorCalled());
    }
    
    /**
     * Tests that the error handler is only called once when going down
     */
    @Test
    public void testErrorHandlerOnlyCalledOnceDown() {
        ServiceLocator locator = Utilities.getServiceLocator(
                LevelFiveDownErrorService.class,
                LevelFiveService.class,
                OnProgressLevelChangerListener.class);
        
        OnProgressLevelChangerListener listener = locator.getService(OnProgressLevelChangerListener.class);
        listener.reset();
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setThreadingPolicy(ThreadingPolicy.USE_NO_THREADS);
        
        controller.proceedTo(6);
        
        Assert.assertEquals(0, listener.getNumOnErrorCalled());
        
        controller.proceedTo(4);
        
        // Sanity check
        Assert.assertEquals(4, controller.getCurrentRunLevel());
        
        Assert.assertEquals(1, listener.getNumOnErrorCalled());
    }
    
    /**
     * Ensures that the expected exception was given to the error handler
     * on the way down
     */
    @Test
    public void testCheckDescriptorFromErrorInformationOnWayDown() {
        ServiceLocator locator = Utilities.getServiceLocator(
                LevelFiveService.class,
                LevelFiveDownErrorService.class,
                OnProgressLevelChangerListener.class);
        
        OnProgressLevelChangerListener listener = locator.getService(OnProgressLevelChangerListener.class);
        listener.reset();
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setThreadingPolicy(ThreadingPolicy.USE_NO_THREADS);
        
        controller.proceedTo(6);
        controller.proceedTo(4);
        
        // Sanity check
        Assert.assertEquals(4, controller.getCurrentRunLevel());
        
        ErrorInformation errorInfo = listener.getLastErrorInformation();
        Assert.assertNotNull(errorInfo);
        
        Descriptor failedDescriptor = errorInfo.getFailedDescriptor();
        Assert.assertNotNull(failedDescriptor);
        
        Assert.assertEquals(failedDescriptor.getImplementation(), LevelFiveDownErrorService.class.getName());
    }

}
