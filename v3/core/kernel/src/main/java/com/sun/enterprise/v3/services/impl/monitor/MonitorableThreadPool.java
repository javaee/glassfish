/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 */
package com.sun.enterprise.v3.services.impl.monitor;

import com.sun.enterprise.v3.services.impl.monitor.probes.ThreadPoolProbeProvider;
import com.sun.grizzly.http.StatsThreadPool;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Grizzly thread pool implementation that emits probe events.
 *
 * @author jluehe
 * @author Alexey Stashok
 */
public class MonitorableThreadPool extends StatsThreadPool {

    // The GrizzlyMonitoring objects, which encapsulates Grizzly probe emitters
    private final GrizzlyMonitoring monitoring;
    private final String monitoringName;

    public MonitorableThreadPool(GrizzlyMonitoring monitoring,
            String monitoringName) {
        this.monitoring = monitoring;
        this.monitoringName = monitoringName;
        setThreadFactory(new ProbeWorkerThreadFactory());

        final ThreadPoolProbeProvider threadPoolProbeProvider =
                monitoring.getThreadPoolProbeProvider();

        threadPoolProbeProvider.setCoreThreadsEvent(
                monitoringName, corePoolSize);
        threadPoolProbeProvider.setMaxThreadsEvent(
                monitoringName, maxPoolSize);

        monitoring.getConnectionQueueProbeProvider().setMaxTaskQueueSizeEvent(
                monitoringName, workQueue.remainingCapacity());
    }

    public MonitorableThreadPool(
            GrizzlyMonitoring monitoring, String threadPoolMonitoringName,
            String threadPoolName, int corePoolSize, int maximumPoolSize,
            int maxTasksCount, long keepAliveTime, TimeUnit unit) {
        super(threadPoolName, corePoolSize, maximumPoolSize, maxTasksCount,
                keepAliveTime, unit);
        this.monitoring = monitoring;
        this.monitoringName = threadPoolMonitoringName;
        setThreadFactory(new ProbeWorkerThreadFactory());

        final ThreadPoolProbeProvider threadPoolProbeProvider =
                monitoring.getThreadPoolProbeProvider();
        
        threadPoolProbeProvider.setCoreThreadsEvent(
                threadPoolMonitoringName, super.corePoolSize);
        threadPoolProbeProvider.setMaxThreadsEvent(
                threadPoolMonitoringName, super.maxPoolSize);

        monitoring.getConnectionQueueProbeProvider().setMaxTaskQueueSizeEvent(
                monitoringName, workQueue.remainingCapacity());
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        synchronized (statelock) {
            super.setCorePoolSize(corePoolSize);

            if (monitoring == null) return;

            monitoring.getThreadPoolProbeProvider().setCoreThreadsEvent(
                    monitoringName, corePoolSize);
        }
    }

    @Override
    public void setMaximumPoolSize(int maxPoolSize) {
        synchronized (statelock) {
            super.setMaximumPoolSize(maxPoolSize);
            
            if (monitoring == null) return;

            monitoring.getThreadPoolProbeProvider().setMaxThreadsEvent(
                    monitoringName, maxPoolSize);
        }
    }

    @Override
    protected void setPoolSizes(int corePoolSize, int maxPoolSize) {
        synchronized (statelock) {
            super.setPoolSizes(corePoolSize, maxPoolSize);

            if (monitoring == null) return;

            final ThreadPoolProbeProvider threadPoolProbeProvider =
                    monitoring.getThreadPoolProbeProvider();

            threadPoolProbeProvider.setCoreThreadsEvent(
                    monitoringName, corePoolSize);
            threadPoolProbeProvider.setMaxThreadsEvent(
                    monitoringName, maxPoolSize);
        }
    }


    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        super.beforeExecute(thread, runnable);
        monitoring.getThreadPoolProbeProvider().threadDispatchedFromPoolEvent(
                monitoringName, thread.getName());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        monitoring.getThreadPoolProbeProvider().threadReturnedToPoolEvent(
                monitoringName, Thread.currentThread().getName());
        super.afterExecute(r, t);
    }

    @Override
    protected void onWorkerExit(BasicWorker worker) {
        monitoring.getThreadPoolProbeProvider().threadReleasedEvent(
                monitoringName, Thread.currentThread().getName());
        super.onWorkerExit(worker);
    }

    @Override
    protected void onTaskQueued(Runnable task) {
        monitoring.getConnectionQueueProbeProvider().onTaskQueuedEvent(
                monitoringName, task);
        super.onTaskQueued(task);
    }

    @Override
    protected void onTaskDequeued(Runnable task) {
        monitoring.getConnectionQueueProbeProvider().onTaskDequeuedEvent(
                monitoringName, task);
        super.onTaskDequeued(task);
    }

    @Override
    protected void onTaskQueueOverflow() {
        monitoring.getConnectionQueueProbeProvider().onTaskQueueOverflowEvent(
                monitoringName);
        super.onTaskQueueOverflow();
    }

    public class ProbeWorkerThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new MonitorableWorkerThread(
                    MonitorableThreadPool.this, r, name +
                    "-(" + workerThreadCounter.getAndIncrement() + ")",
                    initialByteBufferSize, monitoring);
            monitoring.getThreadPoolProbeProvider().threadAllocatedEvent(
                    monitoringName, thread.getName());
            return thread;
        }
    }
}
