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
package org.glassfish.hk2.runlevel.tests.sync;

import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.tests.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class SyncTest {
    public final static String SERVICE_ONE = "One";
    public final static String SERVICE_TEN = "Ten";
    public final static String SERVICE_TWENTY = "Twenty";
    
    /**
     * This tests that things truly happen on the thread passed in
     */
    @Test
    public void testUseNoThreadsPolicy() {
        ServiceLocator locator = Utilities.getServiceLocator(
                ServiceWithThreadLocal.class,
                ThreadSensitiveService.class,
                ListenerService.class);
        
        ServiceWithThreadLocal threadLocal = locator.getService(ServiceWithThreadLocal.class);
        Assert.assertFalse(threadLocal.wasUpToggled());
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setThreadingPolicy(RunLevelController.ThreadingPolicy.USE_NO_THREADS);
        
        controller.proceedTo(1);
        
        // If no other thread was used then the thread local variable will have been toggled
        Assert.assertTrue(threadLocal.wasUpToggled());
        
        Assert.assertFalse(threadLocal.wasDownToggled());
        
        controller.proceedTo(0);
        
        Assert.assertTrue(threadLocal.wasDownToggled());
    }
    
    private void checkList(List<String> list, String onlyEvent) {
        Assert.assertEquals(1, list.size());
        
        Assert.assertEquals(list.get(0), onlyEvent);
    }
    
    /**
     * Makes sure the proper events are fired for multiple levels
     */
    @Test
    public void testMultipleServicesUpAndDown() {
        ServiceLocator locator = Utilities.getServiceLocator(
                LevelOneService.class,
                LevelTenService.class,
                LevelTwentyService.class,
                RunLevelListenerRecorder.class);
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.setThreadingPolicy(RunLevelController.ThreadingPolicy.USE_NO_THREADS);
        
        RunLevelListenerRecorder recorder = locator.getService(RunLevelListenerRecorder.class);
        
        controller.proceedTo(20);
        recorder.goingDown();
        controller.proceedTo(0);
        
        for (int lcv = 0; lcv < 21; lcv++) {
            if (lcv == 0) {
                checkList(recorder.getUpEventsForLevel(lcv), SERVICE_ONE);
                
                Assert.assertTrue(recorder.getDownEventsForLevel(lcv).isEmpty());
            }
            else if (lcv == 1) {
                Assert.assertTrue(recorder.getUpEventsForLevel(lcv).isEmpty());
                
                checkList(recorder.getDownEventsForLevel(lcv), SERVICE_ONE);
            }
            else if (lcv == 9) {
                checkList(recorder.getUpEventsForLevel(lcv), SERVICE_TEN);
                
                Assert.assertTrue(recorder.getDownEventsForLevel(lcv).isEmpty());
            }
            else if (lcv == 10) {
                Assert.assertTrue(recorder.getUpEventsForLevel(lcv).isEmpty());
                
                checkList(recorder.getDownEventsForLevel(lcv), SERVICE_TEN);
            }
            else if (lcv == 19) {
                checkList(recorder.getUpEventsForLevel(lcv), SERVICE_TWENTY);
                
                Assert.assertTrue(recorder.getDownEventsForLevel(lcv).isEmpty());
            }
            else if (lcv == 20) {
                Assert.assertTrue(recorder.getUpEventsForLevel(lcv).isEmpty());
                
                checkList(recorder.getDownEventsForLevel(lcv), SERVICE_TWENTY);
            }
            else {
                Assert.assertTrue(recorder.getUpEventsForLevel(lcv).isEmpty());
                
                Assert.assertTrue(recorder.getDownEventsForLevel(lcv).isEmpty());
            }
        }
     }

}
