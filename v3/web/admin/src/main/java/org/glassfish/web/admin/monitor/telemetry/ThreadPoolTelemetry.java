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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Level;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.statistics.*;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.*;
import org.glassfish.flashlight.client.ProbeListener;
import org.glassfish.flashlight.provider.annotations.ProbeParam;

import java.util.logging.Logger;
import org.glassfish.flashlight.statistics.factory.CounterFactory;

/**
 * Provides the monitoring data at the Web container level
 *
 * @author Prashanth Abbagani
 */
public class ThreadPoolTelemetry{
    private TreeNode threadPoolNode;
    private Collection<ProbeClientMethodHandle> handles;
    private Logger logger;
    private boolean isEnabled = true;
    private String id;
    private String maxTPSize;

    private TreeNode maxThreadsNode;
    
    private Counter activeThreadsCount = CounterFactory.createCount();
    private Counter threadsUsed = CounterFactory.createCount();
    private Counter activeThreadsHighCount = CounterFactory.createCount();

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
    
    ThreadPoolTelemetry(TreeNode threadPoolNode, String id, String maxTPSize, Logger logger) {
        try {
            this.threadPoolNode = threadPoolNode;
            this.logger = logger;
            this.id = id;
            this.maxTPSize = maxTPSize;
            Method m1 = this.maxTPSize.getClass().getMethod("toString");
            maxThreadsNode = TreeNodeFactory.createMethodInvoker("maxthreads-count", this.maxTPSize, "thread-pool", m1);
            threadPoolNode.addChild(maxThreadsNode);

            activeThreadsHighCount.setCategory("thread-pool");
            threadPoolNode.addChild(activeThreadsHighCount);
            activeThreadsHighCount.setName("peakqueued-count");

            Method m2 = this.getClass().getMethod("getIdleCount");
            TreeNode idleThreadsNode = TreeNodeFactory.createMethodInvoker("countthreadsidle-count", this, "thread-pool", m2);
            threadPoolNode.addChild(idleThreadsNode);

            Method m3 = this.getClass().getMethod("getActiveThreadCount");
            TreeNode activeThreadsNode = TreeNodeFactory.createMethodInvoker("countthreads-count", this, "thread-pool", m3);
            threadPoolNode.addChild(activeThreadsNode);

        } catch (NoSuchMethodException ex) {
            Logger.getLogger(ThreadPoolTelemetry.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ThreadPoolTelemetry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @ProbeListener("core:threadpool::newThreadsAllocatedEvent")
    public void newThreadsAllocatedEvent(
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("increment") int increment,
        @ProbeParam("boolean") boolean startThread) {

        logger.finest("[TM]newThreadsAllocatedEvent received - : increment = " + 
                            increment + " :startThread = " + startThread + 
                            ": Thread pool name = " + threadPoolName);
        if (threadPoolName.equals(id))
            activeThreadsCount.setCount(activeThreadsCount.getCount() + increment);
    }


    @ProbeListener("core:threadpool::maxNumberOfThreadsReachedEvent")
    public void maxNumberOfThreadsReachedEvent(
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("maxNumberOfThreads") int maxNumberOfThreads) {
        

        logger.finest("[TM]maxNumberOfThreadsReachedEvent received - : maxNumberOfThreads = " + 
                            maxNumberOfThreads + ": Thread pool name = " + threadPoolName);
        //Not sure what I can do with this event, because I could tell from 
        // threadsCount if it reached maxThreadsCount
    }


    @ProbeListener("core:threadpool::threadDispatchedFromPoolEvent")
    public void threadDispatchedFromPoolEvent(
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("threadId") String threadId) {
        

        logger.finest("[TM]threadDispatchedFromPoolEvent received - : threadId = " + 
                            threadId + ": Thread pool name = " + threadPoolName);
        if (threadPoolName.equals(id)) {
            threadsUsed.increment();
            if (threadsUsed.getCount() > activeThreadsHighCount.getCount())
                activeThreadsHighCount.setCount(threadsUsed.getCount());
        }
    }


    @ProbeListener("core:threadpool::threadReturnedToPoolEvent")
    public void threadReturnedToPoolEvent(
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("threadId") String threadId) {
        

        logger.finest("[TM]threadReturnedToPoolEvent received - : threadId = " + 
                            threadId + ": Thread pool name = " + threadPoolName);
        if (threadPoolName.equals(id))
            threadsUsed.decrement();
    }

    
    public void setProbeListenerHandles(Collection<ProbeClientMethodHandle> handles) {
        this.handles = handles;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void enableMonitoring(boolean flag) {
        //loop through the handles for this node and enable/disable the listeners
        if (isEnabled == flag)
            return;
        for (ProbeClientMethodHandle handle : handles) {
            if (flag == true) 
                handle.enable();
            else
                handle.disable();
        }
        threadPoolNode.setEnabled(flag);
        if (isEnabled) {
            //It means you are turning from ON to OFF, reset the statistics
            resetStats();
        }
        isEnabled = flag;
    }

    private void resetStats() {
        //activeSessionsCurrent.setReset(true);
    }
    
    public long getIdleCount() {
        return (activeThreadsCount.getCount() - threadsUsed.getCount());
    }

    public long getActiveThreadCount() {
        return (activeThreadsCount.getCount());
    }
}
