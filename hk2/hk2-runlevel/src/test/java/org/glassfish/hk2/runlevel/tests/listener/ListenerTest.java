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

import junit.framework.Assert;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelController.ThreadingPolicy;
import org.glassfish.hk2.runlevel.tests.utilities.Utilities;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This tests that proceedTo and proceedToAsync work properly
 * from listeners
 * 
 * @author jwells
 *
 */
public class ListenerTest {
    public final static int NO_LEVEL = -3;
    
    private static void setupChanger(ServiceLocator locator, int changeAt, int changeTo) {
        setupChanger(locator, changeAt, changeTo, NO_LEVEL);
    }
    
    private static void setupChanger(ServiceLocator locator, int changeAt, int changeTo, int sleepAt) {
        locator.getService(OnProgressLevelChangerListener.class).setLevels(changeAt, changeTo, sleepAt);
    }
    
    /**
     * Tests that we can change the proceeding from the proposedLevel
     * callback
     */
    @Test 
    public void testProceedToFurtherUpFromEndOfRunWillKeepGoingUp() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testProceedToFurtherUpFromEndOfRunWillKeepGoingUp",
                OnProgressLevelChangerListener.class);
        
        setupChanger(locator, 5, 10);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.proceedTo(5);
        
        // But really, it should end up being 10
        Assert.assertEquals(10, controller.getCurrentRunLevel());
        
    }
    
    /**
     * Tests that the level can be changed from the middle of
     * the proposedLevel run
     */
    @Test 
    public void testProceedToFurtherUpFromMiddleOfRunWillKeepGoingUp() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testProceedToFurtherUpFromMiddleOfRunWillKeepGoingUp",
                OnProgressLevelChangerListener.class);
        
        setupChanger(locator, 2, 10);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.proceedTo(5);
        
        // But really, it should end up being 10
        Assert.assertEquals(10, controller.getCurrentRunLevel());
        
    }
    
    /**
     * Tests that the level can be changed from the middle of
     * the proposedLevel run
     */
    @Test 
    public void testProceedToFurtherUpFromMiddleOfRunWillKeepGoingDown() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testProceedToFurtherUpFromMiddleOfRunWillKeepGoingDown",
                OnProgressLevelChangerListener.class);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.proceedTo(10);
        
        setupChanger(locator, 7, 1);
        
        controller.proceedTo(5);
        
        // But really, it should end up being 1
        Assert.assertEquals(1, controller.getCurrentRunLevel());
        
    }
    
    /**
     * Tests that the level can be changed from the end of
     * the proposedLevel run
     */
    @Test 
    public void testProceedToFurtherUpFromEndOfRunWillKeepGoingDown() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testProceedToFurtherUpFromEndOfRunWillKeepGoingDown",
                OnProgressLevelChangerListener.class);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.proceedTo(10);
        
        setupChanger(locator, 5, 1);
        
        controller.proceedTo(5);
        
        // But really, it should end up being 1
        Assert.assertEquals(1, controller.getCurrentRunLevel());
    }
    
    /**
     * Tests going from an up direction to a down direction
     */
    @Test 
    public void testGoingFromUpToDown() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testGoingFromUpToDown",
                OnProgressLevelChangerListener.class);
        
        setupChanger(locator, 7, 3, 1);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.proceedTo(10);
        
        // But really, it should end up being 1
        Assert.assertEquals(3, controller.getCurrentRunLevel());
    }
    
    /**
     * Tests going from an down direction to a up direction
     */
    @Test 
    public void testGoingFromDownToUp() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testGoingFromDwonToUp",
                OnProgressLevelChangerListener.class);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.proceedTo(10);
        
        setupChanger(locator, 2, 5, 9);
        
        controller.proceedTo(0);
        
        // But really, it should end up being 5
        Assert.assertEquals(5, controller.getCurrentRunLevel());
    }
    
    private static void noThreads(ServiceLocator locator) {
        locator.getService(RunLevelController.class).setThreadingPolicy(ThreadingPolicy.USE_NO_THREADS);
    }
    
    /**
     * Tests that we can change the proceeding from the proposedLevel
     * callback
     */
    @Test 
    public void testProceedToFurtherUpFromEndOfRunWillKeepGoingUpNoThreads() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testProceedToFurtherUpFromEndOfRunWillKeepGoingUpNoThreads",
                OnProgressLevelChangerListener.class);
        noThreads(locator);
        
        setupChanger(locator, 5, 10);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.proceedTo(5);
        
        // But really, it should end up being 10
        Assert.assertEquals(10, controller.getCurrentRunLevel());
        
    }
    
    /**
     * Tests that the level can be changed from the middle of
     * the proposedLevel run
     */
    @Test 
    public void testProceedToFurtherUpFromMiddleOfRunWillKeepGoingUpNoThreads() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testProceedToFurtherUpFromMiddleOfRunWillKeepGoingUpNoThreads",
                OnProgressLevelChangerListener.class);
        noThreads(locator);
        
        setupChanger(locator, 2, 10);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.proceedTo(5);
        
        // But really, it should end up being 10
        Assert.assertEquals(10, controller.getCurrentRunLevel());
        
    }
    
    /**
     * Tests that the level can be changed from the middle of
     * the proposedLevel run
     */
    @Test 
    public void testProceedToFurtherUpFromMiddleOfRunWillKeepGoingDownNoThreads() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testProceedToFurtherUpFromMiddleOfRunWillKeepGoingDownNoThreads",
                OnProgressLevelChangerListener.class);
        noThreads(locator);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.proceedTo(10);
        
        setupChanger(locator, 7, 1);
        
        controller.proceedTo(5);
        
        // But really, it should end up being 1
        Assert.assertEquals(1, controller.getCurrentRunLevel());
        
    }
    
    /**
     * Tests that the level can be changed from the end of
     * the proposedLevel run
     */
    @Test 
    public void testProceedToFurtherUpFromEndOfRunWillKeepGoingDownNoThreads() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testProceedToFurtherUpFromEndOfRunWillKeepGoingDownNoThreads",
                OnProgressLevelChangerListener.class);
        noThreads(locator);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.proceedTo(10);
        
        setupChanger(locator, 5, 1);
        
        controller.proceedTo(5);
        
        // But really, it should end up being 1
        Assert.assertEquals(1, controller.getCurrentRunLevel());
    }
    
    /**
     * Tests going from an up direction to a down direction
     */
    @Test
    public void testGoingFromUpToDownNoThreads() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testGoingFromUpToDownNoThreads",
                OnProgressLevelChangerListener.class);
        noThreads(locator);
        
        setupChanger(locator, 7, 3);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.proceedTo(10);
        
        // But really, it should end up being 1
        Assert.assertEquals(3, controller.getCurrentRunLevel());
    }
    
    /**
     * Tests going from an down direction to a up direction
     */
    @Test
    public void testGoingFromDownToUpNoThreads() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testGoingFromDwonToUpNoThreads",
                OnProgressLevelChangerListener.class);
        noThreads(locator);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.proceedTo(10);
        
        setupChanger(locator, 2, 5);
        
        controller.proceedTo(0);
        
        // But really, it should end up being 5
        Assert.assertEquals(5, controller.getCurrentRunLevel());
    }


}
