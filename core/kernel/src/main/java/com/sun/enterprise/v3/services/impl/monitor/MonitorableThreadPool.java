/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2010 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.v3.services.impl.monitor;

import java.util.Queue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.sun.enterprise.v3.services.impl.monitor.stats.StatsProvider;
import org.glassfish.grizzly.threadpool.SyncThreadPool;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;

/**
 * Grizzly thread pool implementation that emits probe events.
 *
 * @author jluehe
 * @author Alexey Stashok
 */
public class MonitorableThreadPool extends SyncThreadPool {
    public MonitorableThreadPool(GrizzlyMonitoring monitoring, String monitoringId, String threadPoolName,
        int corePoolSize, int maximumPoolSize, int maxTasksCount, long keepAliveTime, TimeUnit unit) {

        super(new GrizzlyIntegrationThreadPoolConfig(monitoringId, monitoring, threadPoolName, corePoolSize,
            maximumPoolSize, null, maxTasksCount, keepAliveTime, unit, null, Thread.NORM_PRIORITY));

        if (monitoring != null) {
            StatsProvider statsProvider = monitoring.getThreadPoolStatsProvider(monitoringId);
            if (statsProvider != null) {
                statsProvider.setStatsObject(this);
            }
            statsProvider = monitoring.getConnectionQueueStatsProvider(monitoringId);
            if (statsProvider != null) {
                statsProvider.setStatsObject(this);
            }
            config.setCorePoolSize(config.getCorePoolSize());
            config.setMaxPoolSize(config.getMaxPoolSize());
            config.setQueueLimit(config.getQueueLimit());
        }
    }

    /**
     * Custom {@link ThreadPoolConfig} to link V3 probe monitors into the Grizzly runtime.
     */
    private static class GrizzlyIntegrationThreadPoolConfig extends ThreadPoolConfig {
        private final String monitoringId;
        private final GrizzlyMonitoring monitoring;

        public GrizzlyIntegrationThreadPoolConfig(String monitoringId, GrizzlyMonitoring monitoring, String poolName,
            int corePoolSize, int maxPoolSize, Queue<Runnable> queue, int queueLimit, long keepAliveTime,
            TimeUnit timeUnit, ThreadFactory threadFactory, int priority) {
            super(poolName, corePoolSize, maxPoolSize, queue, queueLimit, keepAliveTime, timeUnit, threadFactory,
                priority);
            this.monitoringId = monitoringId;
            this.monitoring = monitoring;
        }

        @Override
        public ThreadPoolConfig setCorePoolSize(int corePoolSize) {
            if (monitoring != null) {
                monitoring.getThreadPoolProbeProvider().setCoreThreadsEvent(
                    monitoringId, getPoolName(), corePoolSize);
            }
            return super.setCorePoolSize(corePoolSize);
        }

        @Override
        public ThreadPoolConfig setMaxPoolSize(int maxPoolSize) {
            if (monitoring != null) {
                monitoring.getThreadPoolProbeProvider().setMaxThreadsEvent(
                    monitoringId, getPoolName(), maxPoolSize);
            }
            return super.setMaxPoolSize(maxPoolSize);
        }

        @Override
        public ThreadPoolConfig setQueueLimit(int limit) {
            if (monitoring != null) {
                monitoring.getConnectionQueueProbeProvider().setMaxTaskQueueSizeEvent(
                    monitoringId, limit);
            }
            return super.setQueueLimit(limit);
        }
    }
}
