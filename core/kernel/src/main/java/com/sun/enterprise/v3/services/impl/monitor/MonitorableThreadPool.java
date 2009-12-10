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
import com.sun.enterprise.v3.services.impl.monitor.stats.StatsProvider;
import com.sun.grizzly.http.StatsThreadPool;
import com.sun.grizzly.util.AbstractThreadPool.Worker;
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
    private final String monitoringId;

    public MonitorableThreadPool(
            GrizzlyMonitoring monitoring, String monitoringId,
            String threadPoolName, int corePoolSize, int maximumPoolSize,
            int maxTasksCount, long keepAliveTime, TimeUnit unit) {
        super(threadPoolName, corePoolSize, maximumPoolSize, maxTasksCount,
                keepAliveTime, unit);
        this.monitoring = monitoring;
        this.monitoringId = monitoringId;

        if (monitoring != null) {
            StatsProvider statsProvider =
                    monitoring.getThreadPoolStatsProvider(monitoringId);
            if (statsProvider != null) {
                statsProvider.setStatsObject(this);
            }

            statsProvider =
                    monitoring.getConnectionQueueStatsProvider(monitoringId);
            if (statsProvider != null) {
                statsProvider.setStatsObject(this);
            }

        }

        setThreadFactory(new ProbeWorkerThreadFactory());

        final ThreadPoolProbeProvider threadPoolProbeProvider =
                monitoring.getThreadPoolProbeProvider();
        
        threadPoolProbeProvider.setCoreThreadsEvent(
                monitoringId, threadPoolName, super.corePoolSize);
        threadPoolProbeProvider.setMaxThreadsEvent(
                monitoringId, threadPoolName, super.maxPoolSize);

        monitoring.getConnectionQueueProbeProvider().setMaxTaskQueueSizeEvent(
                monitoringId, getMaxQueuedTasksCount());
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        synchronized (statelock) {
            super.setCorePoolSize(corePoolSize);

            if (monitoring == null) return;

            monitoring.getThreadPoolProbeProvider().setCoreThreadsEvent(
                    monitoringId, getName(), corePoolSize);
        }
    }

    @Override
    public void setMaximumPoolSize(int maxPoolSize) {
        synchronized (statelock) {
            super.setMaximumPoolSize(maxPoolSize);
            
            if (monitoring == null) return;

            monitoring.getThreadPoolProbeProvider().setMaxThreadsEvent(
                    monitoringId, getName(), maxPoolSize);
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
                    monitoringId, getName(), corePoolSize);
            threadPoolProbeProvider.setMaxThreadsEvent(
                    monitoringId, getName(), maxPoolSize);
        }
    }


    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        super.beforeExecute(thread, runnable);
        monitoring.getThreadPoolProbeProvider().threadDispatchedFromPoolEvent(
                monitoringId, getName(), thread.getName());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        monitoring.getThreadPoolProbeProvider().threadReturnedToPoolEvent(
                monitoringId, getName(), Thread.currentThread().getName());
        super.afterExecute(r, t);
    }

    @Override
    protected void onWorkerStarted(Worker worker) {
        super.onWorkerStarted(worker);
        monitoring.getThreadPoolProbeProvider().threadAllocatedEvent(
                monitoringId, getName(), Thread.currentThread().getName());
    }

    @Override
    protected void onWorkerExit(Worker worker) {
        monitoring.getThreadPoolProbeProvider().threadReleasedEvent(
                monitoringId, getName(), Thread.currentThread().getName());
        super.onWorkerExit(worker);
    }

    @Override
    protected void onMaxNumberOfThreadsReached() {
        monitoring.getThreadPoolProbeProvider().maxNumberOfThreadsReachedEvent(
                monitoringId, getName(), getMaximumPoolSize());
        super.onMaxNumberOfThreadsReached();
    }

    @Override
    protected void onTaskQueued(Runnable task) {
        monitoring.getConnectionQueueProbeProvider().onTaskQueuedEvent(
                monitoringId, String.valueOf(task.hashCode()));
        super.onTaskQueued(task);
    }

    @Override
    protected void onTaskDequeued(Runnable task) {
        monitoring.getConnectionQueueProbeProvider().onTaskDequeuedEvent(
                monitoringId, String.valueOf(task.hashCode()));
        super.onTaskDequeued(task);
    }

    @Override
    protected void onTaskQueueOverflow() {
        monitoring.getConnectionQueueProbeProvider().onTaskQueueOverflowEvent(
                monitoringId);
        super.onTaskQueueOverflow();
    }

    public class ProbeWorkerThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new MonitorableWorkerThread(
                    MonitorableThreadPool.this, r, name +
                    "-(" + nextThreadId() + ")",
                    initialByteBufferSize, monitoring);
            return thread;
        }
    }
}
