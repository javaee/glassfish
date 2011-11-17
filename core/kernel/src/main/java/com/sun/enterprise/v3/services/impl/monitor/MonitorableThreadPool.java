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

import com.sun.enterprise.v3.services.impl.monitor.stats.StatsProvider;
import com.sun.grizzly.http.StatsThreadPool;
import com.sun.grizzly.util.ThreadPoolConfig;
import com.sun.grizzly.util.ThreadPoolMonitoringProbe;

import java.util.Queue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Grizzly thread pool implementation that emits probe events.
 *
 * @author jluehe
 * @author Alexey Stashok
 */
public class MonitorableThreadPool extends StatsThreadPool {


    // ------------------------------------------------------------ Constructors


    public MonitorableThreadPool(
            GrizzlyMonitoring monitoring, String monitoringId,
            String threadPoolName, int corePoolSize, int maximumPoolSize,
            int maxTasksCount, long keepAliveTime, TimeUnit unit) {

        super(new GrizzlyIntegrationThreadPoolConfig(
              monitoringId,
              monitoring,
              threadPoolName,
              corePoolSize,
              maximumPoolSize,
              null,
              maxTasksCount,
              keepAliveTime,
              unit,
              null,
              Thread.NORM_PRIORITY));


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

            ThreadPoolConfig config = getConfiguration();

            // force related events to be fired though the values aren't
            // changing.
            config.setCorePoolSize(config.getCorePoolSize());
            config.setMaxPoolSize(config.getMaxPoolSize());
            config.setQueueLimit(config.getQueueLimit());

        }

    }


    // ---------------------------------------------------------- Nested Classes


    /**
     * Custom {@link ThreadPoolConfig} to link V3 probe monitors into the
     * Grizzly runtime.
     */
    private static class GrizzlyIntegrationThreadPoolConfig extends ThreadPoolConfig {

        private final String monitoringId;
        private final GrizzlyMonitoring monitoring;


        // -------------------------------------------------------- Constructors


        public GrizzlyIntegrationThreadPoolConfig(String monitoringId,
                                                  GrizzlyMonitoring monitoring,
                                                  String poolName,
                                                  int corePoolSize,
                                                  int maxPoolSize,
                                                  Queue<Runnable> queue,
                                                  int queueLimit,
                                                  long keepAliveTime,
                                                  TimeUnit timeUnit,
                                                  ThreadFactory threadFactory,
                                                  int priority) {

            super(poolName,
                  corePoolSize,
                  maxPoolSize,
                  queue,
                  queueLimit,
                  keepAliveTime,
                  timeUnit,
                  threadFactory,
                  priority,
                  null);
            super.setMonitoringProbe(new GrizzlyIntegrationThreadPoolMonitoringProbe());
            this.monitoringId = monitoringId;
            this.monitoring = monitoring;

        }

        public GrizzlyIntegrationThreadPoolConfig(final ThreadPoolConfig cfg) {
            super(cfg);
            
            if (cfg instanceof GrizzlyIntegrationThreadPoolConfig) {
                GrizzlyIntegrationThreadPoolConfig thatCasted = (GrizzlyIntegrationThreadPoolConfig) cfg;
                monitoringId = thatCasted.monitoringId;
                monitoring = thatCasted.monitoring;
            } else {
                monitoringId = null;
                monitoring = null;
            }
        }

        

        // --------------------------------------- Methods from ThreadPoolConfig


        /**
         * @see ThreadPoolConfig#setCorePoolSize(int)
         */
        @Override public ThreadPoolConfig setCorePoolSize(int corePoolSize) {

            if (monitoring != null) {
                monitoring.getThreadPoolProbeProvider().setCoreThreadsEvent(
                      monitoringId, getPoolName(), corePoolSize);
            }
            return super.setCorePoolSize(corePoolSize);

        }


        /**
         * @see ThreadPoolConfig#setMaxPoolSize(int)
         */
        @Override public ThreadPoolConfig setMaxPoolSize(int maxPoolSize) {

            if (monitoring != null) {
                monitoring.getThreadPoolProbeProvider().setMaxThreadsEvent(
                      monitoringId, getPoolName(), maxPoolSize);
            }
            return super.setMaxPoolSize(maxPoolSize);

        }


        /**
         * @see ThreadPoolConfig#setQueueLimit(int)
         */
        @Override public ThreadPoolConfig setQueueLimit(int limit) {

            if (monitoring != null) {
                monitoring.getConnectionQueueProbeProvider().setMaxTaskQueueSizeEvent(
                      monitoringId, limit);
            }
            return super.setQueueLimit(limit);

        }

        @Override
        public ThreadPoolConfig copy() {
            return new GrizzlyIntegrationThreadPoolConfig(this);
        }


        // ------------------------------------------------------- Inner Classes

        /**
         * An implementation of {@link ThreadPoolMonitoringProbe} which delegates
         * probe calls from the {@link com.sun.grizzly.util.GrizzlyExecutorService}
         * to the provided {@link GrizzlyMonitoring} instance.
         */
        private class GrizzlyIntegrationThreadPoolMonitoringProbe
              implements ThreadPoolMonitoringProbe {


            // ------------------------------ Methods from ThreadPoolMonitoringProbe


            /**
             * @see com.sun.grizzly.util.ThreadPoolMonitoringProbe#threadAllocatedEvent(String, Thread)
             */
            @Override public void threadAllocatedEvent(String threadPoolName,
                                                       Thread thread) {

                if (monitoring != null) {
                    monitoring.getThreadPoolProbeProvider()
                          .threadAllocatedEvent(monitoringId,
                                                threadPoolName,
                                                thread.getId());
                }

            }


            /**
             * @see com.sun.grizzly.util.ThreadPoolMonitoringProbe#threadAllocatedEvent(String, Thread)
             */
            @Override public void threadReleasedEvent(String threadPoolName,
                                                      Thread thread) {

                if (monitoring != null) {
                    monitoring.getThreadPoolProbeProvider()
                          .threadReleasedEvent(monitoringId,
                                               threadPoolName,
                                               thread.getId());
                }

            }


            /**
             * @see com.sun.grizzly.util.ThreadPoolMonitoringProbe#threadAllocatedEvent(String, Thread)
             */
            @Override
            public void maxNumberOfThreadsReachedEvent(String threadPoolName,
                                                       int maxNumberOfThreads) {

                if (monitoring != null) {
                    monitoring.getThreadPoolProbeProvider()
                          .maxNumberOfThreadsReachedEvent(monitoringId,
                                                          threadPoolName,
                                                          maxNumberOfThreads);
                }

            }


            /**
             * @see com.sun.grizzly.util.ThreadPoolMonitoringProbe#threadAllocatedEvent(String, Thread)
             */
            @Override public void onTaskQueuedEvent(Runnable runnable) {

                if (monitoring != null) {
                    monitoring.getConnectionQueueProbeProvider()
                          .onTaskQueuedEvent(monitoringId,
                                             String.valueOf(runnable.hashCode()));
                }

            }


            /**
             * @see com.sun.grizzly.util.ThreadPoolMonitoringProbe#threadAllocatedEvent(String, Thread)
             */
            @Override public void onTaskDequeuedEvent(Runnable runnable) {

                if (monitoring != null) {
                    monitoring.getConnectionQueueProbeProvider()
                          .onTaskDequeuedEvent(monitoringId,
                                               String.valueOf(runnable.hashCode()));
                    monitoring.getThreadPoolProbeProvider()
                          .threadDispatchedFromPoolEvent(monitoringId,
                                                         getPoolName(),
                                                         Thread.currentThread().getId());
                }

            }


            /**
             * @see com.sun.grizzly.util.ThreadPoolMonitoringProbe#threadAllocatedEvent(String, Thread)
             */
            @Override
            public void onTaskQueueOverflowEvent(String threadPoolName) {

                if (monitoring != null) {
                    monitoring.getConnectionQueueProbeProvider()
                          .onTaskQueueOverflowEvent(monitoringId);
                }

            }


            /**
             * @see ThreadPoolMonitoringProbe#onTaskCompletedEvent(Runnable)
             */
            @Override public void onTaskCompletedEvent(Runnable runnable) {

                if (monitoring != null) {
                    monitoring.getThreadPoolProbeProvider()
                          .threadReturnedToPoolEvent(monitoringId,
                                                     getPoolName(),
                                                     Thread.currentThread().getId());
                }

            }

        } // END GrizzlyIntegrationThreadPoolMonitoringProbe

    } // END GrizzlyIntegrationThreadPoolConfig

}
