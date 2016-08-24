/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.runlevel.tests.listener2;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Singleton;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.ChangeableRunLevelFuture;
import org.glassfish.hk2.runlevel.ErrorInformation;
import org.glassfish.hk2.runlevel.ErrorInformation.ErrorAction;
import org.glassfish.hk2.runlevel.ProgressStartedListener;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.RunLevelListener;
import org.glassfish.hk2.runlevel.tests.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ListenerLevelsTest {
    /**
     * Ensures the different listener types get the proper set of events
     * even when there is a failure going up or down
     */
    @Test
    public void testDifferentRunLevelListenerTypes() {
        ServiceLocator locator = Utilities.getServiceLocator(TestStartingListener.class,
                TestProgressListener.class,
                ServiceAtTen.class, SometimesFailsAtFiveService.class);
        
        TestStartingListener listener = locator.getService(TestStartingListener.class);
        TestProgressListener progressListener = locator.getService(TestProgressListener.class);
        progressListener.clear();
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        SometimesFailsAtFiveService.setBombAtFive(true);
        
        try {
            controller.proceedTo(10);
            Assert.fail("Should fail at five");
        }
        catch (MultiException me) {
            // Expected
        }
        
        Assert.assertFalse(ServiceAtTen.isActive());
        
        Assert.assertEquals(-2, listener.getLastLevel());
        
        List<Integer> levels = progressListener.getLevels();
        
        Assert.assertEquals(6, levels.size());
        
        Assert.assertEquals(-1, levels.get(0).intValue());
        Assert.assertEquals(0, levels.get(1).intValue());
        Assert.assertEquals(1, levels.get(2).intValue());
        Assert.assertEquals(2, levels.get(3).intValue());
        Assert.assertEquals(3, levels.get(4).intValue());
        Assert.assertEquals(4, levels.get(5).intValue());
        
        // Now let it proceed
        SometimesFailsAtFiveService.setBombAtFive(false);
        
        progressListener.clear();
        controller.proceedTo(10);
        
        Assert.assertTrue(ServiceAtTen.isActive());
        
        Assert.assertEquals(4, listener.getLastLevel());
        
        levels = progressListener.getLevels();
        
        Assert.assertEquals(6, levels.size());
        
        Assert.assertEquals(5, levels.get(0).intValue());
        Assert.assertEquals(6, levels.get(1).intValue());
        Assert.assertEquals(7, levels.get(2).intValue());
        Assert.assertEquals(8, levels.get(3).intValue());
        Assert.assertEquals(9, levels.get(4).intValue());
        Assert.assertEquals(10, levels.get(5).intValue());
        
        // Now the downward direction
        SometimesFailsAtFiveService.setBombAtFive(true);
        
        progressListener.clear();
        
        // Will halt at four due to value returned from progressListener
        controller.proceedTo(0);
        
        Assert.assertFalse(ServiceAtTen.isActive());
        
        Assert.assertEquals(10, listener.getLastLevel());
        Assert.assertEquals(4, controller.getCurrentRunLevel());
        
        levels = progressListener.getLevels();
        
        Assert.assertEquals(6, levels.size());
        
        Assert.assertEquals(9, levels.get(0).intValue());
        Assert.assertEquals(8, levels.get(1).intValue());
        Assert.assertEquals(7, levels.get(2).intValue());
        Assert.assertEquals(6, levels.get(3).intValue());
        Assert.assertEquals(5, levels.get(4).intValue());
        Assert.assertEquals(4, levels.get(5).intValue());
        
        // And all the way down now
        progressListener.clear();
        controller.proceedTo(0);
        
        Assert.assertFalse(ServiceAtTen.isActive());
        
        Assert.assertEquals(4, listener.getLastLevel());
        Assert.assertEquals(0, controller.getCurrentRunLevel());
        
        levels = progressListener.getLevels();
        
        Assert.assertEquals(4, levels.size());
        
        Assert.assertEquals(3, levels.get(0).intValue());
        Assert.assertEquals(2, levels.get(1).intValue());
        Assert.assertEquals(1, levels.get(2).intValue());
        Assert.assertEquals(0, levels.get(3).intValue());
    }
    
    @Singleton
    private static class TestStartingListener implements ProgressStartedListener {
        private int lastLevel = -3;

        @Override
        public void onProgressStarting(ChangeableRunLevelFuture currentJob,
                int currentLevel) {
            lastLevel = currentLevel;
        }
        
        public int getLastLevel() {
            return lastLevel;
        }
    }
    
    @Singleton
    private static class TestProgressListener implements RunLevelListener {
        private final List<Integer> levels = new LinkedList<Integer>();
        
        public void clear() {
            levels.clear();
        }
        
        public List<Integer> getLevels() {
            return levels;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.runlevel.RunLevelListener#onProgress(org.glassfish.hk2.runlevel.ChangeableRunLevelFuture, int)
         */
        @Override
        public void onProgress(ChangeableRunLevelFuture currentJob,
                int levelAchieved) {
            levels.add(levelAchieved);
            
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.runlevel.RunLevelListener#onCancelled(org.glassfish.hk2.runlevel.RunLevelFuture, int)
         */
        @Override
        public void onCancelled(RunLevelFuture currentJob, int levelAchieved) {
            
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.runlevel.RunLevelListener#onError(org.glassfish.hk2.runlevel.RunLevelFuture, org.glassfish.hk2.runlevel.ErrorInformation)
         */
        @Override
        public void onError(RunLevelFuture currentJob,
                ErrorInformation errorInformation) {
            // Make it stop when going down
            errorInformation.setAction(ErrorAction.GO_TO_NEXT_LOWER_LEVEL_AND_STOP);
        }

        
    }
    
    

}
