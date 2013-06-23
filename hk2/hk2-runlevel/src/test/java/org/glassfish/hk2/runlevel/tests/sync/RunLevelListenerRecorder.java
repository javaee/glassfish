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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import junit.framework.Assert;

import org.glassfish.hk2.runlevel.ChangeableRunLevelFuture;
import org.glassfish.hk2.runlevel.ErrorInformation;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.RunLevelListener;
import org.jvnet.hk2.annotations.Service;

/**
 * @author jwells
 *
 */
@Service
public class RunLevelListenerRecorder implements RunLevelListener {
    private boolean up = true;
    private int currentLevel;
    private final HashMap<Integer, List<String>> ups = new HashMap<Integer, List<String>>();
    private final HashMap<Integer, List<String>> downs = new HashMap<Integer, List<String>>();
    
    @Inject
    private RunLevelController controller;

    @Override
    public void onCancelled(RunLevelFuture controller, int levelAchieved) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onError(RunLevelFuture currentJob, ErrorInformation info) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onProgress(ChangeableRunLevelFuture currentJob, int levelAchieved) {
        currentLevel = levelAchieved;
        up = currentJob.isUp();
    }
    
    /**
     * Tells the recorder it is going down
     */
    public void goingDown() {
        up = false;
    }
    
    /**
     * Records the given event
     * @param event The event to record
     */
    public void recordEvent(String event) {
        // no locks
        if (up) {
            Assert.assertTrue(controller.getCurrentRunLevel() == currentLevel);
            
            List<String> upList = ups.get(currentLevel);
            if (upList == null) {
                upList = new LinkedList<String>();
                
                ups.put(currentLevel, upList);
            }
            
            upList.add(event);
        }
        else {
            List<String> downList = downs.get(currentLevel);
            if (downList == null) {
                downList = new LinkedList<String>();
                
                downs.put(currentLevel, downList);
            }
            
            downList.add(event);
        }
    }
    
    /**
     * Gets the up events for the given level
     * 
     * @param level The level to get events for
     * @return The list of events
     */
    public List<String> getUpEventsForLevel(int level) {
        List<String> retVal = ups.get(level);
        if (retVal == null) return Collections.emptyList();
        return retVal;
    }
    
    /**
     * Gets the down events for the given level
     * 
     * @param level The level to get events for
     * @return The list of events
     */
    public List<String> getDownEventsForLevel(int level) {
        List<String> retVal = downs.get(level);
        if (retVal == null) return Collections.emptyList();
        return retVal;
    }

}
