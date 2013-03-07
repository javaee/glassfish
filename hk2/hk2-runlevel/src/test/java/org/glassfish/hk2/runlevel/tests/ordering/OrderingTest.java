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
package org.glassfish.hk2.runlevel.tests.ordering;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.Activator;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.tests.utilities.Utilities;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class OrderingTest {
    private final ServiceLocator locator = Utilities.getServiceLocator("OrderingTest", Music.class, Opera.class);
    
    /**
     * This ensures that we can get the proper timings for services
     * even when the RunLevelService will start the services out of order.
     * 
     * The test strategy here is to have the Music service depend on the
     * Opera service, but the RLS will get the Music service first, since
     * it has a higher ranking.  This test ensures that even though the
     * Opera service is a subordinate of the Music service that its timing
     * would still be accounted for properly
     */
    @Test @Ignore
    public void testCanGetCorrectTiming() {
        RunLevelController controller = locator.getService(RunLevelController.class);
        
        TimerActivator activator = new TimerActivator(controller.getDefaultActivator());
        
        controller.proceedTo(5, activator);
        
        LinkedList<ServiceData> records = activator.getRecords();
        
        ServiceData operaData = records.get(1);
        ServiceData musicData = records.get(0);
        
        Assert.assertEquals(operaData.descriptor.getImplementation(), Opera.class.getName());
        Assert.assertEquals(musicData.descriptor.getImplementation(), Music.class.getName());
        
        System.out.println("JRW(10) operaElapsed=" + operaData.elapsedTime + " musicElapsed=" + musicData.elapsedTime);
        
        properRange(operaData.elapsedTime, "Opera");
        properRange(musicData.elapsedTime, "Music");
    }
    
    private void properRange(long elapsedTime, String who) {
        Assert.assertTrue("elapsed time of " + who + " is less than 50ms: " + elapsedTime,
                elapsedTime > 50L);
        
        Assert.assertTrue("elapsed time of " + who + " is more than 150ms: " + elapsedTime,
                elapsedTime < 150L);
        
        
    }
    
    private static class TimerActivator implements Activator {
        private final Activator dActivator;
        private final LinkedList<ServiceData> records = new LinkedList<ServiceData>();
        
        public TimerActivator(Activator dActivator) {
            this.dActivator = dActivator;
        }

        @Override
        public void activate(ActiveDescriptor<?> activeDescriptor) {
            long startTime = System.currentTimeMillis();
            
            dActivator.activate(activeDescriptor);
            
            long finishTime = System.currentTimeMillis();
            
            records.add(new ServiceData(activeDescriptor, finishTime - startTime));
            
        }
        
        private LinkedList<ServiceData> getRecords() {
            return records;
        }

        @Override
        public void deactivate(ActiveDescriptor<?> activeDescriptor) {
            // Do nothing
            
        }

        @Override
        public void awaitCompletion() throws ExecutionException,
                InterruptedException, TimeoutException {
            // Do nothing
            
        }

        @Override
        public void awaitCompletion(long timeout, TimeUnit unit)
                throws ExecutionException, InterruptedException,
                TimeoutException {
            // Do nothing
            
        }
        
    }
    
    private static class ServiceData {
        private final ActiveDescriptor<?> descriptor;
        private final long elapsedTime;
        
        private ServiceData(ActiveDescriptor<?> descriptor, long elapsedTime) {
            this.descriptor = descriptor;
            this.elapsedTime = elapsedTime;
        }
    }

}
