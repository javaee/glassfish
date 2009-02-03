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

package com.sun.enterprise.v3.services.impl;

import com.sun.grizzly.http.StatsThreadPool;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.glassfish.kernel.admin.monitor.ThreadPoolProbeProvider;

/**
 * Grizzly thread pool implementation that emits probe events.
 *
 * @author jluehe
 */
public class GrizzlyProbeThreadPool extends StatsThreadPool {
        
    // The ThreadPoolProbeProvider to which to emit any probe events
    private ThreadPoolProbeProvider threadPoolProbeProvider =
        GrizzlyService.NO_OP_THREADPOOL_PROBE_PROVIDER;


    public void setThreadPoolProbeProvider(
            ThreadPoolProbeProvider threadPoolProbeProvider) {
        this.threadPoolProbeProvider = threadPoolProbeProvider;
    }

    public GrizzlyProbeThreadPool() {
        super();
        setThreadFactory(new ProbeWorkerThreadFactory());
    }

    public GrizzlyProbeThreadPool(int corePoolSize, int maximumPoolSize,
            int maxTasksCount, long keepAliveTime, TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, maxTasksCount, keepAliveTime, unit);
        setThreadFactory(new ProbeWorkerThreadFactory());
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        super.beforeExecute(thread, runnable);
        threadPoolProbeProvider.threadDispatchedFromPoolEvent(name,
                thread.getName());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        threadPoolProbeProvider.threadReturnedToPoolEvent(name,
                Thread.currentThread().getName());
        super.afterExecute(r, t);
    }

    public class ProbeWorkerThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread thread = new GrizzlyProbeWorkerThread(
                    GrizzlyProbeThreadPool.this, r, name,
                    initialByteBufferSize, threadPoolProbeProvider);
            threadPoolProbeProvider.newThreadsAllocatedEvent(name, 1, true);

            return thread;
        }
    }
}
