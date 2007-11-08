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

//NOTE: Tabs are used instead of spaces for indentation.
//  Make sure that your editor does not replace tabs with spaces.
//  Set the tab length using your favourite editor to your
//  visual preference.

/*
 * Filename: FastThreadPool.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */

/**
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/threadpool/FastThreadPool.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:32 $
 */

package com.sun.enterprise.util.threadpool;

import java.util.Properties;

import com.sun.enterprise.util.threadpool.Servicable;
import com.sun.enterprise.util.collection.BlockingQueue;
import com.sun.enterprise.util.collection.QueueClosedException;
import com.sun.enterprise.util.collection.TooManyTasksException;
import com.sun.enterprise.util.pool.Pool;
import com.sun.enterprise.util.pool.AbstractPool;
import com.sun.enterprise.util.pool.BoundedPool;
import com.sun.enterprise.util.pool.ObjectFactory;

import com.sun.enterprise.util.collection.DListNode;
//Bug 4677074 begin
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
//Bug 4677074 end

/**
 * This implementation runs a thread and does the following:
 *	a) Picks a task from the task queue
 *      b) Removes one thread from the (thread)Pool.
 *      c) notifies the threadPoolThread.
 */
public class FastThreadPool {
    
//Bug 4677074 begin
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
//Bug 4677074 end
    protected boolean          bDebug = false;
    private PoolProperties     poolProps;
    private TaskQueue          taskQueue;
    private int                waitCount = 0;
    private int                totalThreadCreatedCount = 0; // 4682740
    private int                totalThreadDestroyedCount = 0; // 4682740
    private int                numMessages = 0;
    
    public FastThreadPool(String threadGroupName, int minThreadCount, int maxThreadCount,
            long maxIdleTime, int queueLimit, TaskFactory factory) {
        this(new ThreadGroup(threadGroupName), minThreadCount, maxThreadCount,
                maxIdleTime, new TaskQueue(queueLimit, factory));
    }
    
    public FastThreadPool(ThreadGroup threadGroup, int minThreadCount, int maxThreadCount,
            long maxIdleTime, int queueLimit, TaskFactory factory) {
        this(threadGroup, minThreadCount, maxThreadCount, maxIdleTime, 
                new TaskQueue(queueLimit, factory));
    }
    
    public FastThreadPool(ThreadGroup threadGroup, int minThreadCount, int maxThreadCount,
            long maxIdleTime, TaskQueue queue) {
        this.taskQueue = queue;
        
        poolProps = new PoolProperties(minThreadCount, maxThreadCount,
                maxIdleTime, taskQueue, threadGroup);
        
    }
    
    /**
     * Start the threadpool. Needed for scenarios where the queue gets
     * created and set in the threadpool from some other object.
     */
    public void start() {
        // We set createdCount to be the number of threads we are creating
        poolProps.createdCount = poolProps.minThreadCount;
        for (int i=0; i < poolProps.minThreadCount; i++) {
            // if (bDebug) System.out.println("FastThreadPool creating thread: " 
            //         + i + "/" + poolProps.minThreadCount);
//Bug 4677074 begin
	    // if (com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,"FastThreadPool creating thread: "+ i + "/" + poolProps.minThreadCount);
//Bug 4677074 end
            new ThreadPoolThread(poolProps);
        }
        // START OF IASRI 4682740
        com.sun.enterprise.util.MonitorTask.addORBMonitorable(this);
        // END OF IASRI 4682740
    }
    
    /**
     * returns the task queue
     */
    public TaskQueue getTaskQueue() {
        return taskQueue;
    }
    
    /**
     * sets the task queue. Returns true if successful
     */
    public boolean setTaskQueue(TaskQueue bq) {
        if (taskQueue != null)
            return false;
        taskQueue = bq;
        return true;
    }
    
    /**
     * Add to the head of the queue. Probably a high priority job?
     */
    public void addFirst(Servicable servicable)
            throws TooManyTasksException, QueueClosedException {
        taskQueue.addFirst(servicable);
    }
    
    /**
     * Add to the tail of the queue.
     */
    public void addLast(Servicable servicable)
            throws TooManyTasksException, QueueClosedException {
        taskQueue.addLast(servicable);
    }
    
    /**
     * Add the job at the specified position. Probably based on priority?
     */
    public void add(int index, Servicable servicable)
            throws TooManyTasksException, QueueClosedException {
        taskQueue.add(index, servicable);
    }
    
    public void shutdown() {
        taskQueue.shutdown();
    }
    
    public void abort() {
        taskQueue.abort();
    }
    
    public int getPoolSize() {
        return (poolProps != null) ? (poolProps.createdCount) : -1;
    }
    
    public int getWaitCount() {
        return waitCount;
    }
    
    public int[] getMonitoredValues() {
        synchronized(poolProps) {
            // Return the two integer values as an array.
            int [] ret = {(poolProps != null) ? (poolProps.createdCount) : -1,
                    waitCount};
            return ret;
        }
    }
    
    // Start 4682740 - ORB to support standalone monitoring 
    
    /**
     * Great for monitoring. All methods used here are unsynchronized.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FastThreadPool [CS=").append(poolProps.createdCount);
        sb.append(", TC=").append(totalThreadCreatedCount);
        sb.append(", TD=").append(totalThreadDestroyedCount);
        sb.append(", Min=").append(poolProps.minThreadCount);
        sb.append(", Max=").append(poolProps.maxThreadCount);
        sb.append(", MaxIdle=").append(poolProps.maxIdleTime);
        sb.append(", Msgs=").append(numMessages);
        sb.append("]");
        return sb.toString();
    }
    
    // End 4682740 - ORB to support standalone monitoring 
    
    private class PoolProperties {
        int             minThreadCount;
        int             maxThreadCount;
        long            maxIdleTime;
        TaskQueue       taskQueue;
        ThreadGroup     threadGroup;
        
        int             createdCount;
        
        PoolProperties(int minThreadCount, int maxThreadCount, long maxIdleTime,
                TaskQueue taskQueue, ThreadGroup threadGroup) {
            this.minThreadCount = minThreadCount;
            this.maxThreadCount = maxThreadCount;
            this.maxIdleTime = maxIdleTime;
            this.taskQueue = taskQueue;
            this.threadGroup = threadGroup;
            
            this.createdCount = 0;
        }
    }
    
    private class ThreadPoolThread implements Runnable {
        PoolProperties poolProps;
        
        ThreadPoolThread(PoolProperties poolProps) {
            this.poolProps = poolProps;
            Thread thread = new Thread(poolProps.threadGroup, this);
            if (poolProps.threadGroup != null) {
                if (poolProps.threadGroup.isDaemon()) {
                    thread.setDaemon(true);
                }
            }
            thread.start();
            totalThreadCreatedCount++; // 4682740
        }
        
        public void run() {
            Servicable task = null;
            try {
                while (true) {
                    boolean canCreateBuddy = false;
                    
                    do {
                        synchronized (poolProps) {
                            waitCount++;
                        }
                        
                        task = null; // Bug 4700462 - Intermittent objects hang on to threads blocked on queue
                        
                        task = (Servicable) taskQueue.remove(poolProps.maxIdleTime);
                        synchronized (poolProps) {
                            waitCount--;
                            if (task == null) {
                                // We timedout!!
                                if (poolProps.createdCount > poolProps.minThreadCount) {
                                    //there are too many threads and the system is idle.
                                    // if (bDebug) System.out.println(Thread.currentThread().getName() 
                                    //         + " Timedout. (quitting)....");
//Bug 4677074 begin
				    // if (com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,Thread.currentThread().getName()+ " Timedout. (quitting)....");
//Bug 4677074 end
                                    // DIE!!!!
                                    poolProps.createdCount--;
                                    totalThreadDestroyedCount++; // 4682740
                                    return;
                                }
                                // We get to live a little longer!
                                continue;
                            }
                            canCreateBuddy = (waitCount == 0) &&
                                    (poolProps.createdCount < poolProps.maxThreadCount);
                            // Increment createdCount in anticipation of buddy creation
                            if (canCreateBuddy) poolProps.createdCount++;
                            numMessages++;
                        }
                        if (canCreateBuddy) {
                            // if (bDebug) System.out.println(Thread.currentThread().getName() 
                            //         + " creating buddy...");
//Bug 4677074 begin
			    // if (com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,Thread.currentThread().getName()+ " creating buddy...");
//Bug 4677074 end
                            new ThreadPoolThread(poolProps);
                        }                 

                        // if (bDebug) System.out.println(Thread.currentThread().getName() 
                        //         + " got a task: " + task);
//Bug 4677074 begin
			// if (com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,Thread.currentThread().getName() + " got a task: " + task);
//Bug 4677074 end
                        try {
                            task.prolog();
                            task.service();
                            task.epilog();
                        } catch (Throwable th) {
//Bug 4677074                            th.printStackTrace();
//Bug 4677074 begin
				_logger.log(Level.SEVERE,"iplanet_util.generic_exception",th);
//Bug 4677074 end
                        }
                    } while (task != null);                    
                }                
            } catch (com.sun.enterprise.util.collection.QueueClosedException qcEx) {
//Bug 4677074                System.out.println("Queue closed. Exitting....");
//Bug 4677074 begin
			_logger.log(Level.FINE,"Queue closed. Exitting....");
//Bug 4677074 end
                synchronized (poolProps) {
                    poolProps.createdCount--;
                }
                totalThreadDestroyedCount++; // 4682740
                return;
            } catch (InterruptedException inEx) {
//Bug 4677074                System.out.println("Interrupted. Exitting....");
//Bug 4677074 begin
			_logger.log(Level.SEVERE,"iplanet_util.generic_exception",inEx);
//Bug 4677074 end
                synchronized (poolProps) {
                    poolProps.createdCount--;
                }
                totalThreadDestroyedCount++; // 4682740
                return;
            }                
        }
        
    }
    
    
}
