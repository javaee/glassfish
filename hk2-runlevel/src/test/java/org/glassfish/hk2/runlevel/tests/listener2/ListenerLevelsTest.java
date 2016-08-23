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
import org.glassfish.hk2.runlevel.OnProgressCallbackType;
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
     * Tests that the OnProgressCallbackType works properly
     * (including in the case of failures going up and down)
     */
    @Test
    public void testOnProgressCallbackType() {
        ServiceLocator locator = Utilities.getServiceLocator(TestListener.class,
                ServiceAtTen.class, SometimesFailsAtFiveService.class);
        
        TestListener listener = locator.getService(TestListener.class);
        listener.clear();
        
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
        
        // Check we got the expected events
        List<LevelAndType> events = listener.getEvents();
        Assert.assertEquals(7, events.size());
        
        Assert.assertEquals(new LevelAndType(-2, OnProgressCallbackType.INITIAL), events.get(0));
        Assert.assertEquals(new LevelAndType(-1, OnProgressCallbackType.PROGRESSION), events.get(1));
        Assert.assertEquals(new LevelAndType(0, OnProgressCallbackType.PROGRESSION), events.get(2));
        Assert.assertEquals(new LevelAndType(1, OnProgressCallbackType.PROGRESSION), events.get(3));
        Assert.assertEquals(new LevelAndType(2, OnProgressCallbackType.PROGRESSION), events.get(4));
        Assert.assertEquals(new LevelAndType(3, OnProgressCallbackType.PROGRESSION), events.get(5));
        Assert.assertEquals(new LevelAndType(4, OnProgressCallbackType.PROGRESSION), events.get(6));
        
        // Now let it proceed
        listener.clear();
        SometimesFailsAtFiveService.setBombAtFive(false);
        
        controller.proceedTo(10);
        
        Assert.assertTrue(ServiceAtTen.isActive());
        
        events = listener.getEvents();
        Assert.assertEquals(7, events.size());
        
        Assert.assertEquals(new LevelAndType(4, OnProgressCallbackType.INITIAL), events.get(0));
        Assert.assertEquals(new LevelAndType(5, OnProgressCallbackType.PROGRESSION), events.get(1));
        Assert.assertEquals(new LevelAndType(6, OnProgressCallbackType.PROGRESSION), events.get(2));
        Assert.assertEquals(new LevelAndType(7, OnProgressCallbackType.PROGRESSION), events.get(3));
        Assert.assertEquals(new LevelAndType(8, OnProgressCallbackType.PROGRESSION), events.get(4));
        Assert.assertEquals(new LevelAndType(9, OnProgressCallbackType.PROGRESSION), events.get(5));
        Assert.assertEquals(new LevelAndType(10, OnProgressCallbackType.PROGRESSION), events.get(6));
        
        // Now the downward direction
        listener.clear();
        SometimesFailsAtFiveService.setBombAtFive(true);
        
        controller.proceedTo(0);
        
        Assert.assertFalse(ServiceAtTen.isActive());
        
        events = listener.getEvents();
        Assert.assertEquals(7, events.size());
        
        Assert.assertEquals(new LevelAndType(10, OnProgressCallbackType.INITIAL), events.get(0));
        Assert.assertEquals(new LevelAndType(9, OnProgressCallbackType.PROGRESSION), events.get(1));
        Assert.assertEquals(new LevelAndType(8, OnProgressCallbackType.PROGRESSION), events.get(2));
        Assert.assertEquals(new LevelAndType(7, OnProgressCallbackType.PROGRESSION), events.get(3));
        Assert.assertEquals(new LevelAndType(6, OnProgressCallbackType.PROGRESSION), events.get(4));
        Assert.assertEquals(new LevelAndType(5, OnProgressCallbackType.PROGRESSION), events.get(5));
        Assert.assertEquals(new LevelAndType(4, OnProgressCallbackType.PROGRESSION), events.get(6));
        
        listener.clear();
        controller.proceedTo(0);
        
        events = listener.getEvents();
        Assert.assertEquals(5, events.size());
        
        Assert.assertEquals(new LevelAndType(4, OnProgressCallbackType.INITIAL), events.get(0));
        Assert.assertEquals(new LevelAndType(3, OnProgressCallbackType.PROGRESSION), events.get(1));
        Assert.assertEquals(new LevelAndType(2, OnProgressCallbackType.PROGRESSION), events.get(2));
        Assert.assertEquals(new LevelAndType(1, OnProgressCallbackType.PROGRESSION), events.get(3));
        Assert.assertEquals(new LevelAndType(0, OnProgressCallbackType.PROGRESSION), events.get(4));
    }
    
    @Singleton
    private static class TestListener implements RunLevelListener {
        private final List<LevelAndType> onProgressEvents = new LinkedList<LevelAndType>();
        
        private void clear() {
            onProgressEvents.clear();
        }
        
        private List<LevelAndType> getEvents() {
            return onProgressEvents;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.runlevel.RunLevelListener#onProgress(org.glassfish.hk2.runlevel.ChangeableRunLevelFuture, int)
         */
        @Override
        public void onProgress(ChangeableRunLevelFuture currentJob,
                int levelAchieved) {
            onProgressEvents.add(new LevelAndType(levelAchieved, currentJob.getCallbackType()));
            
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
            errorInformation.setAction(ErrorAction.GO_TO_NEXT_LOWER_LEVEL_AND_STOP);
        }
        
    }
    
    private static class LevelAndType {
        private final int level;
        private final OnProgressCallbackType type;
        
        private LevelAndType(int level, OnProgressCallbackType type) {
            this.level = level;
            this.type = type;
        }
        
        @Override
        public int hashCode() {
            return level ^ type.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof LevelAndType)) return false;
            
            LevelAndType lat = (LevelAndType) o;
            
            return (lat.level == level) && (lat.type.equals(type));
        }
        
        @Override
        public String toString() {
            return "LevelAndType(" + level + "," + type + "," + System.identityHashCode(this) + ")";
        }
    }

}
