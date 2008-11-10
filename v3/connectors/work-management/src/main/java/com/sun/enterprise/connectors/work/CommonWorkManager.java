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

package com.sun.enterprise.connectors.work;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.corba.se.spi.orbutil.threadpool.NoSuchThreadPoolException;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPool;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager;
import com.sun.enterprise.connectors.work.monitor.MonitorableWorkManager;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import javax.resource.spi.work.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * WorkManager implementation.
 *
 * @author Binod P.G
 */

public final class CommonWorkManager implements MonitorableWorkManager {

    //TODO V3 need to use ee.threadpool
    private static WorkManager wm = null;

    private ThreadPoolManager tpm;
    private ThreadPool tp;

    private static final Logger logger =
            LogDomains.getLogger(CommonWorkManager.class, LogDomains.RSR_LOGGER);

    private boolean isMonitoringEnabled = false; //default = false;

    private WorkStats workStats = null;

    private StringManager localStrings = StringManager.getManager(
            CommonWorkManager.class);

    private ConnectorRuntime runtime;

    /**
     * Private constructor.
     *
     * @param threadPoolId Id of the thread pool.
     * @throws ConnectorRuntimeException if thread pool is not accessible
     */
    public CommonWorkManager(String threadPoolId, ConnectorRuntime runtime)
            throws ConnectorRuntimeException {

        try {
            //TODO V3 need to be in sync with v2 ? (default thread-pool trial)
            this.runtime = runtime;
            tp = runtime.getThreadPool(threadPoolId);
        } catch (NoSuchThreadPoolException e) {
            String msg = localStrings.getString("workmanager.threadpool_not_found");
            logger.log(Level.SEVERE, msg, threadPoolId);
            throw new ConnectorRuntimeException(e.getMessage());
        }

        if(tp == null){
            String msg = localStrings.getString("workmanager.threadpool_not_found");
            logger.log(Level.SEVERE, msg, threadPoolId);
            throw new ConnectorRuntimeException(msg);
        }
    }

    /**
     * Using the default thread pool.
     *
     * @throws ConnectorRuntimeException if thread pool is not accessible
     */
    /**public CommonWorkManager() throws ConnectorRuntimeException {
        this(null);
    } */

    /**
     * Executes the work instance.
     *
     * @param work work instance from resource adapter
     * @throws WorkException if there is an exception while executing work.
     */
    public void doWork(Work work)
            throws WorkException {
        doWork(work, -1, null, null);
    }

    /**
     * Executes the work instance. The calling thread will wait until the
     * end of work execution.
     *
     * @param work         work instance from resource adapter
     * @param startTimeout Timeout for the work.
     * @param execContext  Execution context in which the work will be executed.
     * @param workListener Listener from RA that will listen to work events.
     * @throws WorkException if there is an exception while executing work.
     */
    public void doWork(Work work, long startTimeout,
                       ExecutionContext execContext, WorkListener workListener)
            throws WorkException {

        if (logger.isLoggable(Level.FINEST)) {
            String msg = "doWork for [" + work.toString() + "] START";
            logger.log(Level.FINEST, debugMsg(msg));
        }

        WorkCoordinator wc = new WorkCoordinator
                (work, startTimeout, execContext, tp.getAnyWorkQueue(), workListener,
                        this.workStats, runtime);
        wc.submitWork(WorkCoordinator.WAIT_UNTIL_FINISH);
        wc.lock();

        WorkException we = wc.getException();
        if (we != null) {
            throw we;
        }

        if (logger.isLoggable(Level.FINEST)) {
            String msg = "doWork for [" + work.toString() + "] END";
            msg = "doWork for [" + work.toString() + "] END";
            logger.log(Level.FINEST, debugMsg(msg));
        }
    }

    /**
     * Executes the work instance. The calling thread will wait until the
     * start of work execution.
     *
     * @param work work instance from resource adapter
     * @throws WorkException if there is an exception while executing work.
     */
    public long startWork(Work work) // startTimeout = INDEFINITE
            throws WorkException {
        //block the current application thread
        //find a thread to run work
        //notify the application thread when done

        return startWork(work, -1, null, null);
    }

    /**
     * Executes the work instance. The calling thread will wait until the
     * start of work execution.
     *
     * @param work         work instance from resource adapter
     * @param startTimeout Timeout for the work.
     * @param execContext  Execution context in which the work will be executed.
     * @param workListener Listener from RA that will listen to work events.
     * @throws WorkException if there is an exception while executing work.
     */
    public long startWork(Work work, long startTimeout,
                          ExecutionContext execContext, WorkListener workListener)
            throws WorkException {

        if (logger.isLoggable(Level.FINEST)) {
            String msg = "startWork for [" + work.toString() + "] START";
            logger.log(Level.FINEST, debugMsg(msg));
        }

        long acceptanceTime = System.currentTimeMillis();

        WorkCoordinator wc = new WorkCoordinator
                (work, startTimeout, execContext, tp.getAnyWorkQueue(), workListener,
                        this.workStats, runtime);
        wc.submitWork(WorkCoordinator.WAIT_UNTIL_START);
        wc.lock();

        WorkException we = wc.getException();
        if (we != null) {
            throw we;
        }

        if (logger.isLoggable(Level.FINEST)) {
            String msg = "startWork for [" + work.toString() + "] END";
            logger.log(Level.FINEST, debugMsg(msg));
        }
        long startTime = System.currentTimeMillis();

        return (startTime - acceptanceTime);
    }

    /**
     * Executes the work instance. Calling thread will continue after scheduling
     * the work
     *
     * @param work work instance from resource adapter
     * @throws WorkException if there is an exception while executing work.
     */
    public void scheduleWork(Work work) // startTimeout = INDEFINITE
            throws WorkException {
        scheduleWork(work, -1, null, null);
        return;
    }

    /**
     * Executes the work instance. Calling thread will continue after scheduling
     * the work
     *
     * @param work         work instance from resource adapter
     * @param startTimeout Timeout for the work.
     * @param execContext  Execution context in which the work will be executed.
     * @param workListener Listener from RA that will listen to work events.
     * @throws WorkException if there is an exception while executing work.
     */
    public void scheduleWork(Work work, long startTimeout,
                             ExecutionContext execContext, WorkListener workListener)
            throws WorkException {

        if (logger.isLoggable(Level.FINEST)) {
            String msg = "scheduleWork for [" + work.toString() + "] START";
            logger.log(Level.FINEST, debugMsg(msg));
        }

        WorkCoordinator wc = new WorkCoordinator
                (work, startTimeout, execContext, tp.getAnyWorkQueue(), workListener,
                        this.workStats, runtime);
        wc.submitWork(WorkCoordinator.NO_WAIT);
        wc.lock();

        WorkException we = wc.getException();
        if (we != null) {
            throw we;
        }

        if (logger.isLoggable(Level.FINEST)) {
            String msg = "scheduleWork for [" + work.toString() + "] END";
            logger.log(Level.FINEST, debugMsg(msg));
        }
        return;
    }

    private String debugMsg(String message) {
        String msg = "[Thread " + Thread.currentThread().getName()
                + "] -- " + message;
        return msg;
    }

    //SJSAS 8.1 Monitoring additions begins
    public boolean isMonitoringEnabled() {
        return this.isMonitoringEnabled;
    }

    public void setMonitoringEnabled(boolean isEnabled) {
        this.isMonitoringEnabled = isEnabled;
        if (this.workStats == null) {
            this.workStats = new WorkStats();
        }
        //reset WorkStats when monitoring disabled
        if (!isEnabled) {
            this.workStats.reset();
        }
    }

    public long getWaitQueueLength() {
        return this.workStats.currWaitQueueLength;
    }

    public long getMaxWaitQueueLength() {
        return this.workStats.maxWaitQueueLength;
    }

    public long getMinWaitQueueLength() {
        if (this.workStats.minWaitQueueLength != Long.MAX_VALUE) {
            return this.workStats.minWaitQueueLength;
        } else {
            return 0;
        }
    }

    public long getMaxWorkRequestWaitTime() {
        return this.workStats.maxWorkRequestWaitTime;

    }

    public long getMinWorkRequestWaitTime() {
        return this.workStats.minWorkRequestWaitTime;
    }

    public long getSubmittedWorkCount() {
        return this.workStats.submittedWorkCount;
    }

    public long getRejectedWorkCount() {
        return this.workStats.rejectedWorkCount;
    }

    public long getCompletedWorkCount() {
        return this.workStats.completedWorkCount;
    }

    public long getCurrentActiveWorkCount() {
        return this.workStats.currentActiveWorkCount;
    }

    public long getMaxActiveWorkCount() {
        return this.workStats.maxActiveWorkCount;
    }

    public long getMinActiveWorkCount() {
        if (this.workStats.minActiveWorkCount != Long.MAX_VALUE) {
            return this.workStats.minActiveWorkCount;
        } else {
            return 0;
        }
    }
    //SJSAS 8.1 Monitoring additions end

}

/**
 * A simple class that holds all statistics-related entries captured by the
 * commonworkmanager together with the work-coordinator
 * <p/>
 * An instance of workStats is passed to the Work-Coordinator, during
 * construction, so that the work-coordinator can update the stats of a
 * work-manager
 *
 * @author Sivakumar Thyagarajan
 */
class WorkStats {
    long submittedWorkCount;
    long completedWorkCount;
    long rejectedWorkCount;
    long maxWaitQueueLength;
    long minWaitQueueLength;

    long currentActiveWorkCount;
    long minActiveWorkCount;
    long maxActiveWorkCount;

    long maxWorkRequestWaitTime;
    long minWorkRequestWaitTime;
    long currWaitQueueLength;

    public void reset() {
        this.submittedWorkCount = 0L;
        this.rejectedWorkCount = 0L;
        this.completedWorkCount = 0L;

        this.currWaitQueueLength = 0L;
        this.maxWaitQueueLength = 0L;
        this.minWaitQueueLength = Long.MAX_VALUE;

        this.currentActiveWorkCount = 0L;
        this.minActiveWorkCount = Long.MAX_VALUE;
        this.maxActiveWorkCount = 0L;

        this.maxWorkRequestWaitTime = 0L;
        this.minWorkRequestWaitTime = 0L;
    }

    public synchronized void setWorkWaitTime(long waitTime) {
        //latch high
        if (waitTime > maxWorkRequestWaitTime) {
            this.maxWorkRequestWaitTime = waitTime;
        }

        //latch low
        if (waitTime < minWorkRequestWaitTime) {
            this.minWorkRequestWaitTime = waitTime;
        }
    }

    public synchronized void incrementWaitQueueLength() {
        setWaitQueueLength(++this.currWaitQueueLength);
    }

    public synchronized void decrementWaitQueueLength() {
        setWaitQueueLength(--this.currWaitQueueLength);
    }

    private void setWaitQueueLength(long waitQueueLength) {
        //latch high
        if (waitQueueLength > maxWaitQueueLength) {
            maxWaitQueueLength = waitQueueLength;
        }
        //latch low
        if (waitQueueLength < minWaitQueueLength) {
            minWaitQueueLength = waitQueueLength;
        }
    }

    public synchronized void setActiveWorkCount(long currentActiveWorkCount) {
        this.currentActiveWorkCount = currentActiveWorkCount;
        //latch high
        if (currentActiveWorkCount > maxActiveWorkCount) {
            maxActiveWorkCount = currentActiveWorkCount;
        }
        //latch low
        if (currentActiveWorkCount < minActiveWorkCount) {
            minActiveWorkCount = currentActiveWorkCount;
        }
    }
}
