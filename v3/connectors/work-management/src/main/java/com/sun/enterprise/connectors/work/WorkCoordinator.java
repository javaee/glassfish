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


import com.sun.corba.se.spi.orbutil.threadpool.WorkQueue;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.logging.LogDomains;

import javax.resource.spi.work.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * WorkCoordinator : Coordinates one work's execution. Handles all
 * exception conditions and does JTS coordination.
 *
 * @author Binod P.G
 */
public final class WorkCoordinator {

    static final int WAIT_UNTIL_START = 1;
    static final int WAIT_UNTIL_FINISH = 2;
    static final int NO_WAIT = 3;

    static final int CREATED = 1;
    static final int STARTED = 2;
    static final int COMPLETED = 3;
    static final int TIMEDOUT = 4;

    private volatile int waitMode;
    private volatile int state = CREATED;

    private final boolean workIsBad = false;

    private final javax.resource.spi.work.Work work;
    private final long timeout;
    private long startTime;
    private final ExecutionContext ec;
    private final WorkQueue queue;
    private final WorkListener listener;
    private volatile WorkException exception;
    private final Object lock;
    private static int seed;
    private final int id;

    private static final Logger logger =
            LogDomains.getLogger(LogDomains.RSR_LOGGER);

    /**
     * <code>workStats</code> is responsible for holding all monitorable
     * properties of a work-manager. Workstats would be null, if monitoring is
     * *not* enabled.
     */
    private WorkStats workStats = null;

    private ConnectorRuntime runtime;

    /**
     * Constructs a coordinator
     *
     * @param work     A work object as submitted by the resource adapter
     * @param timeout  timeout for the work instance
     * @param ec       ExecutionContext object.
     * @param queue    WorkQueue of the threadpool, to which the work
     *                 will be submitted
     * @param listener WorkListener object from the resource adapter.
     */
    public WorkCoordinator(javax.resource.spi.work.Work work,
                           long timeout,
                           ExecutionContext ec,
                           WorkQueue queue,
                           WorkListener listener, WorkStats workStats) {

        this.work = work;
        this.timeout = timeout;
        this.ec = ec;
        this.queue = queue;
        this.listener = listener;
        synchronized (WorkCoordinator.class) {
            this.id = ++seed;
        }
        this.lock = new Object();
        this.workStats = workStats;
    }

    /**
     * Submits the work to the queue and generates a work accepted event.
     */
    public void submitWork(int waitMode) {
        this.waitMode = waitMode;
        this.startTime = System.currentTimeMillis();
        if (listener != null) {
            listener.workAccepted(
                    new WorkEvent(this, WorkEvent.WORK_ACCEPTED, work, null));
        }
        if (workStats != null) {
            workStats.submittedWorkCount++;
            workStats.incrementWaitQueueLength();
        }
        queue.addWork(new OneWork(work, this));
    }

    /**
     * Pre-invoke operation. This does the following
     * <pre>
     * 1. Notifies the <code> WorkManager.startWork </code> method.
     * 2. Checks whether the wok has already been timed out.
     * 3. Recreates the transaction with JTS.
     * </pre>
     */
    public void preInvoke() {

        // If the work is just scheduled, check whether it has timed out or not. 
        if (waitMode == NO_WAIT && timeout > -1) {
            long elapsedTime = System.currentTimeMillis() - startTime;

            if (workStats != null) {
                workStats.setWorkWaitTime(elapsedTime);
            }

            if (elapsedTime > timeout) {
                workTimedOut();
            }
        }

        // Change the status to started.
        setState(STARTED);

        if (waitMode == WAIT_UNTIL_START) {
            unLock();
        }

        // If the work is timed out then return.
        if (!proceed()) {
            if (workStats != null) {
                workStats.decrementWaitQueueLength();
            }
            return;
        }

        // All set to do start the work. So send the event.
        if (listener != null) {
            listener.workStarted(
                    new WorkEvent(this, WorkEvent.WORK_STARTED, work, null));
        }


        try {
            JavaEETransactionManager tm = getTransactionManager();
            if (ec != null && ec.getXid() != null) {
                tm.recreate(ec.getXid(), ec.getTransactionTimeout());
            }
        } catch (WorkException we) {
            this.exception = we;
        } catch (Exception e) {
            setException(e);
        }

        if (workStats != null) {
            workStats.setActiveWorkCount(++workStats.currentActiveWorkCount);
            workStats.decrementWaitQueueLength();
        }

    }

    /**
     * Post-invoke operation. This does the following after the work is executed.
     * <pre>
     * 1. Releases the transaction with JTS.
     * 2. Generates work completed event.
     * 3. Clear the thread context.
     * </pre>
     */
    public void postInvoke() {
        boolean txImported = (ec != null && ec.getXid() != null);
        try {
            JavaEETransactionManager tm = getTransactionManager();
            /* TODO V3
            if (txImported) {
                tm.release(ec.getXid());
            }
            */
        } catch (Exception e) {
            setException(e);
        }
        /*catch (WorkException ex) {
            setException(ex);
        } */ finally {
            try {
                if (workStats != null) {
                    workStats.setActiveWorkCount
                            (--workStats.currentActiveWorkCount);
                    workStats.completedWorkCount++;
                }

                //If exception is not null, the work has already been rejected.
                if (listener != null) {
                    if ((!isTimedOut()) && (exception == null)) {
                        listener.workCompleted(
                                new WorkEvent(this, WorkEvent.WORK_COMPLETED, work,
                                        getException()));
                    }
                }

                //Also release the TX from the record of TX Optimizer
                /* TODO V3
                if (txImported) {
                    JavaEETransactionManager tm = getTransactionManager();
                    //TODO V3 check whether J2EETxMgrOpt is available 
                    if (tm instanceof J2EETransactionManagerOpt) {
                        ((J2EETransactionManagerOpt) tm).clearThreadTx();
                    }
                }
                */
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage());
            }
        }

        setState(COMPLETED);
        if (waitMode == WAIT_UNTIL_FINISH) {
            unLock();
        }
    }

    /**
     * Times out the thread
     */
    private void workTimedOut() {
        setState(TIMEDOUT);
        exception = new WorkRejectedException();
        exception.setErrorCode(WorkException.START_TIMED_OUT);
        if (listener != null) {
            listener.workRejected(
                    new WorkEvent(this, WorkEvent.WORK_REJECTED, work, exception));
        }
        if (workStats != null) {
            workStats.rejectedWorkCount++;
            workStats.setActiveWorkCount(--workStats.currentActiveWorkCount);
        }
    }

    /**
     * Checks the work is good to proceed with further processing.
     *
     * @return true if the work is good and false if it is bad.
     */
    public boolean proceed() {
        return !isTimedOut() && exception == null;
    }

    private boolean isTimedOut() {
        return getState() == TIMEDOUT;
    }

    /**
     * Retrieves the exception created during the work's execution.
     *
     * @return a <code>WorkException</code> object.
     */
    public WorkException getException() {
        return exception;
    }

    /**
     * Accepts an exception object and converts to a
     * <code>WorkException</code> object.
     *
     * @param e Throwable object.
     */
    public void setException(Throwable e) {
        if (getState() < STARTED) {
            if (e instanceof WorkRejectedException) {
                exception = (WorkException) e;
            } else if (e instanceof WorkException) {
                WorkException we = (WorkException) e;
                exception = new WorkRejectedException(we);
                exception.setErrorCode(we.getErrorCode());
            } else {
                exception = new WorkRejectedException(e);
                exception.setErrorCode(WorkException.UNDEFINED);
            }
        } else {
            if (e instanceof WorkCompletedException) {
                exception = (WorkException) e;
            } else if (e instanceof WorkException) {
                WorkException we = (WorkException) e;
                exception = new WorkCompletedException(we);
                exception.setErrorCode(we.getErrorCode());
            } else {
                exception = new WorkCompletedException(e);
                exception.setErrorCode(WorkException.UNDEFINED);
            }
        }
    }

    /**
     * Lock the thread upto the end of execution or start of work
     * execution.
     *
     * @param waitMode either WAIT_UNTIL_START or WAIT_UNTIL_FINISH
     */
    public void lock() {

        if (!lockRequired()) {
            return;
        }

        try {
            synchronized (lock) {
                if (checkStateBeforeLocking()) {
                    if (timeout != -1) {
                        lock.wait(timeout);
                    } else {
                        lock.wait();
                    }
                }
            }

            if (getState() < STARTED) {
                workTimedOut();
            }
            if (lockRequired()) {
                synchronized (lock) {
                    if (checkStateBeforeLocking()) {
                        lock.wait();
                    }
                }
            }

        } catch (Exception e) {
            setException(e);
        }
    }

    /**
     * Unlocks the thread.
     */
    private void unLock() {
        try {
            synchronized (lock) {
                lock.notify();
            }
        } catch (Exception e) {
            setException(e);
        }
    }

    /**
     * Returns the string representation of WorkCoordinator.
     *
     * @return Unique identification concatenated by work object.
     */
    public String toString() {
        return id + ":" + work;
    }

    /**
     * Sets the state of the work  coordinator object
     *
     * @param state CREATED or Either STARTED or COMPLETED or TIMEDOUT
     */
    public synchronized void setState(int state) {
        this.state = state;
    }

    /**
     * Retrieves the state of the work coordinator object.
     *
     * @return Integer represnting the state.
     */
    public synchronized int getState() {
        return state;
    }

    private boolean lockRequired() {
        if (!proceed()) {
            return false;
        }
        if (waitMode == NO_WAIT) {
            return false;
        }
        if (waitMode == WAIT_UNTIL_FINISH) {
            return getState() < COMPLETED;
        }
        if (waitMode == WAIT_UNTIL_START) {
            return getState() < STARTED;
        }
        return false;
    }

    /**
     * It is possible that state is modified just before
     * the lock is obtained. So check it again.
     * Access the variable directly to avoid nested locking.
     */
    private boolean checkStateBeforeLocking() {
        if (waitMode == WAIT_UNTIL_FINISH) {
            return state < COMPLETED;
        }
        if (waitMode == WAIT_UNTIL_START) {
            return state < STARTED;
        }
        return false;
    }

    private JavaEETransactionManager getTransactionManager() {
        //TODO V3 accessing connectorRuntime impll ?
        if (runtime == null) {
            runtime = ConnectorRuntime.getRuntime();
        }
        return runtime.getTransactionManager();
    }
}
