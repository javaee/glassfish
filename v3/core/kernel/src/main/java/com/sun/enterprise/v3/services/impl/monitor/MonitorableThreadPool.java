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

import com.sun.grizzly.http.StatsThreadPool;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Grizzly thread pool implementation that emits probe events.
 *
 * @author jluehe
 */
public class MonitorableThreadPool extends StatsThreadPool {

    // The GrizzlyMonitoring objects, which encapsulates Grizzly probe emitters
    private final GrizzlyMonitoring monitoring;
    private final String threadPoolMonitoringName;

    public MonitorableThreadPool(GrizzlyMonitoring monitoring,
            String threadPoolMonitoringName) {
        this.monitoring = monitoring;
        this.threadPoolMonitoringName = threadPoolMonitoringName;
        setThreadFactory(new ProbeWorkerThreadFactory());
    }

    public MonitorableThreadPool(
            GrizzlyMonitoring monitoring, String threadPoolMonitoringName,
            String threadPoolName, int corePoolSize, int maximumPoolSize,
            int maxTasksCount, long keepAliveTime, TimeUnit unit) {
        super(threadPoolName, corePoolSize, maximumPoolSize, maxTasksCount,
                keepAliveTime, unit);
        this.monitoring = monitoring;
        this.threadPoolMonitoringName = threadPoolMonitoringName;
        setThreadFactory(new ProbeWorkerThreadFactory());
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        super.beforeExecute(thread, runnable);
        monitoring.getThreadPoolProbeProvider().threadDispatchedFromPoolEvent(
                threadPoolMonitoringName, thread.getName());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        monitoring.getThreadPoolProbeProvider().threadReturnedToPoolEvent(
                threadPoolMonitoringName, Thread.currentThread().getName());
        super.afterExecute(r, t);
    }

    public class ProbeWorkerThreadFactory implements ThreadFactory {

        public Thread newThread(Runnable r) {
            Thread thread = new MonitorableWorkerThread(
                    MonitorableThreadPool.this, r, name,
                    initialByteBufferSize, monitoring);
            monitoring.getThreadPoolProbeProvider().newThreadsAllocatedEvent(
                    threadPoolMonitoringName, 1, true);
            return thread;
        }
    }
}
