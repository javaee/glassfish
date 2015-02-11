/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.runlevel.tests.listener1;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.tests.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class Listener1Test {
    /**
     * Tests that onProgress is called before going up and down
     */
    @Test // @org.junit.Ignore
    public void testOnProgressCalledBeforeAnythingElse() {
        ServiceLocator locator = Utilities.getServiceLocator(LevelThreeService.class, UpListener.class);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.proceedTo(2);
        
        UpListener upService = locator.getService(UpListener.class);
        upService.done();
        
        controller.proceedTo(3);
        
        upService.done();
        
        TrackingService ts = locator.getService(TrackingService.class);
        
        Assert.assertTrue(ts.isUp());
        Assert.assertFalse(ts.isDown());
        ts.done();
        
        Assert.assertFalse(ts.isUp());
        Assert.assertFalse(ts.isDown());
        
        controller.proceedTo(2);
        
        Assert.assertFalse(ts.isUp());
        Assert.assertTrue(ts.isDown());
    }
    
    private ServiceLocator setupChangerLocator() {
      return Utilities.getServiceLocator(ChangeLevelListener.class,
              ServiceRegistry.class,
              S1.class, S3.class, S5.class, S7.class, S10.class);
    }
    
    private static void verifyRegistry(ServiceLocator locator, boolean one, boolean three, boolean five, boolean seven, boolean ten) {
        ServiceRegistry registry = locator.getService(ServiceRegistry.class);
        
        if (one) {
            Assert.assertTrue(registry.contains(1));
        }
        else {
            Assert.assertFalse(registry.contains(1));
        }
        
        if (three) {
            Assert.assertTrue(registry.contains(3));
        }
        else {
            Assert.assertFalse(registry.contains(3));
        }
        
        if (five) {
            Assert.assertTrue(registry.contains(5));
        }
        else {
            Assert.assertFalse(registry.contains(5));
        }
        
        if (seven) {
            Assert.assertTrue(registry.contains(7));
        }
        else {
            Assert.assertFalse(registry.contains(7));
        }
        
        if (ten) {
            Assert.assertTrue(registry.contains(10));
        }
        else {
            Assert.assertFalse(registry.contains(10));
        }
        
    }
    
    private static void changerTest(ServiceLocator locator, int initial, int changedTo) {
        RunLevelController rlc = locator.getService(RunLevelController.class);
        rlc.proceedTo(5);
        
        ChangeLevelListener changer = locator.getService(ChangeLevelListener.class);
        changer.changeLevels(5, changedTo);
        
        rlc.proceedTo(initial);
        
    }
    
    /**
     * Scenario.  original target: 7, final target: 10
     */
    @Test
    public void testChange_7_10() {
        ServiceLocator locator = setupChangerLocator();
        
        changerTest(locator, 7, 10);
        
        verifyRegistry(locator,
                true, /* 1 */
                true, /* 3 */
                true, /* 5 */
                true, /* 7 */
                true  /* 10 */);
        
    }
    
    /**
     * Scenario.  original target: 3, final target: 1
     */
    @Test
    public void testChange_3_1() {
        ServiceLocator locator = setupChangerLocator();
        
        changerTest(locator, 3, 1);
        
        verifyRegistry(locator,
                true, /* 1 */
                false, /* 3 */
                false, /* 5 */
                false, /* 7 */
                false  /* 10 */);
        
    }
    
    /**
     * Scenario.  original target: 3, final target: 0
     */
    @Test
    public void testChange_3_0() {
        ServiceLocator locator = setupChangerLocator();
        
        changerTest(locator, 3, 0);
        
        verifyRegistry(locator,
                false, /* 1 */
                false, /* 3 */
                false, /* 5 */
                false, /* 7 */
                false  /* 10 */);
    }
    
    /**
     * Scenario.  original target: 10, final target: 1
     */
    @Test
    public void testChange_10_1() {
        ServiceLocator locator = setupChangerLocator();
        
        changerTest(locator, 10, 1);
        
        verifyRegistry(locator,
                true, /* 1 */
                false, /* 3 */
                false, /* 5 */
                false, /* 7 */
                false  /* 10 */);
    }
    
    /**
     * Scenario.  original target: 1, final target: 10
     */
    @Test
    public void testChange_1_10() {
        ServiceLocator locator = setupChangerLocator();
        
        changerTest(locator, 1, 10);
        
        verifyRegistry(locator,
                true, /* 1 */
                true, /* 3 */
                true, /* 5 */
                true, /* 7 */
                true  /* 10 */);
    }
    
    /**
     * Scenario.  original target: 10, final target: 7
     */
    @Test
    public void testChange_10_7() {
        ServiceLocator locator = setupChangerLocator();
        
        changerTest(locator, 10, 7);
        
        verifyRegistry(locator,
                true, /* 1 */
                true, /* 3 */
                true, /* 5 */
                true, /* 7 */
                false  /* 10 */);
    }
    
    /**
     * Scenario.  original target: 0, final target: 3
     */
    @Test
    public void testChange_0_3() {
        ServiceLocator locator = setupChangerLocator();
        
        changerTest(locator, 0, 3);
        
        verifyRegistry(locator,
                true, /* 1 */
                true, /* 3 */
                false, /* 5 */
                false, /* 7 */
                false  /* 10 */);
    }
    
    /**
     * Scenario.  original target: 0, final target: 1
     */
    @Test
    public void testChange_0_1() {
        ServiceLocator locator = setupChangerLocator();
        
        changerTest(locator, 0, 1);
        
        verifyRegistry(locator,
                true, /* 1 */
                false, /* 3 */
                false, /* 5 */
                false, /* 7 */
                false  /* 10 */);
    }
    
    /**
     * Scenario.  original target: 0, final target: 5
     */
    @Test
    public void testChange_0_5() {
        ServiceLocator locator = setupChangerLocator();
        
        changerTest(locator, 0, 5);
        
        verifyRegistry(locator,
                true, /* 1 */
                true, /* 3 */
                true, /* 5 */
                false, /* 7 */
                false  /* 10 */);
    }
    
    /**
     * Scenario.  original target: 10, final target: 5
     */
    @Test
    public void testChange_10_5() {
        ServiceLocator locator = setupChangerLocator();
        
        changerTest(locator, 10, 5);
        
        verifyRegistry(locator,
                true, /* 1 */
                true, /* 3 */
                true, /* 5 */
                false, /* 7 */
                false  /* 10 */);
    }
    
    /**
     * Cancelling from the start (upward initial job)
     */
    @Test
    public void testCancelUp() {
        ServiceLocator locator = setupChangerLocator();
        
        RunLevelController rlc = locator.getService(RunLevelController.class);
        rlc.proceedTo(5);
        
        ChangeLevelListener changer = locator.getService(ChangeLevelListener.class);
        changer.setCancelLevel(5);
        
        rlc.proceedTo(10);
        
        verifyRegistry(locator,
                true, /* 1 */
                true, /* 3 */
                true, /* 5 */
                false, /* 7 */
                false  /* 10 */);
        
        Assert.assertTrue(changer.getCancelCalled());
    }
    
    /**
     * Cancelling from the start (downward initial job)
     */
    @Test
    public void testCancelDown() {
        ServiceLocator locator = setupChangerLocator();
        
        RunLevelController rlc = locator.getService(RunLevelController.class);
        rlc.proceedTo(5);
        
        ChangeLevelListener changer = locator.getService(ChangeLevelListener.class);
        changer.setCancelLevel(5);
        
        rlc.proceedTo(0);
        
        verifyRegistry(locator,
                true, /* 1 */
                true, /* 3 */
                true, /* 5 */
                false, /* 7 */
                false  /* 10 */);
        
        Assert.assertTrue(changer.getCancelCalled());
    }

}
