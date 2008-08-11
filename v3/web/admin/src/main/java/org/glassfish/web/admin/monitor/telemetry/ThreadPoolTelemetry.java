/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package org.glassfish.web.admin.monitor.telemetry;

import java.util.Collection;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.statistics.*;
import org.glassfish.flashlight.statistics.factory.CounterFactory;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.*;
import org.glassfish.flashlight.client.ProbeListener;
import org.glassfish.flashlight.provider.annotations.ProbeParam;
import org.glassfish.flashlight.provider.annotations.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Provides the monitoring data at the Web container level
 *
 * @author Prashanth Abbagani
 */
public class ThreadPoolTelemetry{
    private TreeNode threadpoolNode;
    private Collection<ProbeClientMethodHandle> handles;
    private boolean threadpoolMonitoringEnabled;
    private Logger logger;    


    /* We would like to measure the following */
    /*
        server.thread-pools.orb\.threadpool\.thread-pool-1.averagetimeinqueue-description = Provides average time in milliseconds a work item waited in the work queue before getting processed.
        server.thread-pools.orb\.threadpool\.thread-pool-1.averageworkcompletiontime-description = Provides statistical information about the average completion time of a work item in milliseconds.
        server.thread-pools.orb\.threadpool\.thread-pool-1.currentnumberofthreads-description = Provides statistical information about the number of Threads in the associated ThreadPool.
        server.thread-pools.orb\.threadpool\.thread-pool-1.numberofavailablethreads-count = 0
        server.thread-pools.orb\.threadpool\.thread-pool-1.numberofavailablethreads-description = Provides the total number of available threads in the ThreadPool.
        server.thread-pools.orb\.threadpool\.thread-pool-1.numberofbusythreads-count = 2
        server.thread-pools.orb\.threadpool\.thread-pool-1.numberofbusythreads-description = Provides the total number of busy threads in the ThreadPool.
        server.thread-pools.orb\.threadpool\.thread-pool-1.numberofworkitemsinqueue-description = Provides the number of Work Items in queue.
        server.thread-pools.orb\.threadpool\.thread-pool-1.totalworkitemsadded-count = 12
        server.thread-pools.orb\.threadpool\.thread-pool-1.totalworkitemsadded-description = Provides the total number of work items added so far to the work queue associated with threadpool.
     * 
     */ 
    
    public ThreadPoolTelemetry(TreeNode parent, boolean threadpoolMonitoringEnabled, Logger logger) {
        this.logger = logger;
        this.threadpoolMonitoringEnabled = threadpoolMonitoringEnabled;
        //threadpoolNode = TreeNodeFactory.createTreeNode(threadPoolName, this, "http-service");
        //parent.addChild(threadpoolNode);
        //We can only add the child when there is a new thread pool
    }

    @ProbeListener("core:threadpool::newThreadsAllocatedEvent")
    public void newThreadsAllocatedEvent(
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("increment") int increment,
        @ProbeParam("boolean") boolean startThread) {

        logger.finest("[TM]newThreadsAllocatedEvent received - : increment = " + 
                            increment + " :startThread = " + startThread + 
                            ": Thread pool name = " + threadPoolName);
    }


    @ProbeListener("core:threadpool::maxNumberOfThreadsReachedEvent")
    public void maxNumberOfThreadsReachedEvent(
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("maxNumberOfThreads") int maxNumberOfThreads) {
        

        logger.finest("[TM]maxNumberOfThreadsReachedEvent received - : maxNumberOfThreads = " + 
                            maxNumberOfThreads + ": Thread pool name = " + threadPoolName);
    }


    @ProbeListener("core:threadpool::threadDispatchedFromPoolEvent")
    public void threadDispatchedFromPoolEvent(
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("threadId") String threadId) {
        

        logger.finest("[TM]threadDispatchedFromPoolEvent received - : threadId = " + 
                            threadId + ": Thread pool name = " + threadPoolName);
    }


    @ProbeListener("core:threadpool::threadReturnedToPoolEvent")
    public void threadReturnedToPoolEvent(
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("threadId") String threadId) {
        

        logger.finest("[TM]threadReturnedToPoolEvent received - : threadId = " + 
                            threadId + ": Thread pool name = " + threadPoolName);
    }

    
    public void setProbeListenerHandles(Collection<ProbeClientMethodHandle> handles) {
        this.handles = handles;
        if (!threadpoolMonitoringEnabled){
            //disable handles
            tuneProbeListenerHandles(threadpoolMonitoringEnabled);
        }
    }
    
    public void enableProbeListenerHandles(boolean isEnabled) {
        if (isEnabled != threadpoolMonitoringEnabled) {
            threadpoolMonitoringEnabled = isEnabled;
            tuneProbeListenerHandles(threadpoolMonitoringEnabled);
        }
    }
    
    private void tuneProbeListenerHandles(boolean shouldEnable) {
        //disable handles
        for (ProbeClientMethodHandle handle : handles) {
            if (shouldEnable)
                handle.enable();
            else
                handle.disable();
        }
        
    }

    public void enableMonitoring(boolean isEnable) {
        //loop through the handles for this node and enable/disable the listeners
        //delegate the request to the child nodes
    }
    
    public void enableMonitoringForSubElements(boolean isEnable) {
        //loop through the children and enable/disable all
    }
    
}
