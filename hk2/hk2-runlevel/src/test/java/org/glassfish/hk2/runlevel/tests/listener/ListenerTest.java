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
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.RunLevelListener;
import org.glassfish.hk2.runlevel.tests.utilities.Utilities;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
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
    /**
     * Tests that we can change the proceeding from the proposedLevel
     * callback
     */
    @Test @Ignore
    public void testProceedToFurtherUpFromEndOfRunWillKeepGoingUp() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testProceedToFurtherUpFromEndOfRunWillKeepGoingUp");
        
        ServiceLocatorUtilities.addOneConstant(locator, new RunLevelListener() {
            @Override
            public void onCancelled(RunLevelFuture currentJob, int levelAchieved) {
                
            }

            @Override
            public void onError(RunLevelFuture currentJob, Throwable error) {
                
            }

            @Override
            public void onProgress(RunLevelFuture currentJob, int levelAchieved) {
                System.out.println("JRW(10) levelAchieved=" + levelAchieved);
                if (levelAchieved == 5) {
                    try {
                        currentJob.changeProposedLevel(10);
                    }
                    catch (Throwable th) {
                        System.out.println("JRW(30) th=" + th);
                        th.printStackTrace();
                    }
                    System.out.println("JRW(20) post async");
                }
                
            }
            
        });
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.proceedTo(5);
        
        // But really, it should end up being 10
        Assert.assertEquals(10, controller.getCurrentRunLevel());
        
    }
    
    /**
     * Tests that the level can be changed from the middle of
     * the proposedLevel run
     */
    @Test @Ignore
    public void testProceedToFurtherUpFromMiddleOfRunWillKeepGoingUp() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testProceedToFurtherUpFromMiddleOfRunWillKeepGoingUp");
        
        ServiceLocatorUtilities.addOneConstant(locator, new RunLevelListener() {
            @Override
            public void onCancelled(RunLevelFuture currentJob, int levelAchieved) {
                
            }

            @Override
            public void onError(RunLevelFuture currentJob, Throwable error) {
                
            }

            @Override
            public void onProgress(RunLevelFuture currentJob, int levelAchieved) {
                System.out.println("JRW(40) levelAchieved=" + levelAchieved);
                if (levelAchieved == 2) {
                    try {
                        currentJob.changeProposedLevel(10);
                    }
                    catch (Throwable th) {
                        System.out.println("JRW(50) th=" + th);
                        th.printStackTrace();
                    }
                    System.out.println("JRW(60) post async");
                }
                
            }
            
        });
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        controller.proceedTo(5);
        
        // But really, it should end up being 10
        Assert.assertEquals(10, controller.getCurrentRunLevel());
        
    }
    
    /**
     * Tests that the level can be changed from the middle of
     * the proposedLevel run
     */
    @Test @Ignore
    public void testProceedToFurtherUpFromMiddleOfRunWillKeepGoingDown() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testProceedToFurtherUpFromMiddleOfRunWillKeepGoingDown");
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.proceedTo(10);
        
        ServiceLocatorUtilities.addOneConstant(locator, new RunLevelListener() {
            @Override
            public void onCancelled(RunLevelFuture currentJob, int levelAchieved) {
                
            }

            @Override
            public void onError(RunLevelFuture currentJob, Throwable error) {
                
            }

            @Override
            public void onProgress(RunLevelFuture currentJob, int levelAchieved) {
                System.out.println("JRW(40) levelAchieved=" + levelAchieved);
                if (levelAchieved == 7) {
                    try {
                        currentJob.changeProposedLevel(1);
                    }
                    catch (Throwable th) {
                        System.out.println("JRW(50) th=" + th);
                        th.printStackTrace();
                    }
                    System.out.println("JRW(60) post async");
                }
                
            }
            
        });
        
        controller.proceedTo(5);
        
        // But really, it should end up being 1
        Assert.assertEquals(1, controller.getCurrentRunLevel());
        
    }
    
    /**
     * Tests that the level can be changed from the end of
     * the proposedLevel run
     */
    @Test @Ignore
    public void testProceedToFurtherUpFromEndOfRunWillKeepGoingDown() {
        ServiceLocator locator = Utilities.getServiceLocator("ListenerTest.testProceedToFurtherUpFromEndOfRunWillKeepGoingDown");
        
        RunLevelController controller = locator.getService(RunLevelController.class);
        controller.proceedTo(10);
        
        ServiceLocatorUtilities.addOneConstant(locator, new RunLevelListener() {
            @Override
            public void onCancelled(RunLevelFuture currentJob, int levelAchieved) {
                
            }

            @Override
            public void onError(RunLevelFuture currentJob, Throwable error) {
                
            }

            @Override
            public void onProgress(RunLevelFuture currentJob, int levelAchieved) {
                System.out.println("JRW(40) levelAchieved=" + levelAchieved);
                if (levelAchieved == 5) {
                    try {
                        currentJob.changeProposedLevel(1);
                    }
                    catch (Throwable th) {
                        System.out.println("JRW(50) th=" + th);
                        th.printStackTrace();
                    }
                    System.out.println("JRW(60) post async");
                }
                
            }
            
        });
        
        controller.proceedTo(5);
        
        // But really, it should end up being 1
        Assert.assertEquals(1, controller.getCurrentRunLevel());
        
    }


}
