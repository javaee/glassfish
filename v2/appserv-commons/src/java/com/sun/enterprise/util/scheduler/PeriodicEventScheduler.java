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

//
// Copyright 2001 iPlanet/ Sun Microsystems, Inc. All Rights Reserved.
//
// Author : darpan
// Module : Utils
//
package com.sun.enterprise.util.scheduler;

import com.sun.enterprise.util.ApproximateClock;
//Bug 4677074 begin
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
//Bug 4677074 end

/**
* PeriodicEventScheduler manages tasks of the type PeriodicallyServicable. Such 
* objects can be added to the manager and the time interval in these objects
* defines at what frequency the service() method of these tasks gets invoked by 
* the manager.<br>
* It is critical that the service() operation be as brisk as possible for the 
* manager to work effectively.<br>
* PeriodicEventScheduler is internally set as daemon.
* <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/scheduler/PeriodicEventScheduler.java,v $</I>
* @author     $Author: tcfujii $
* @version    1.0 $Revision: 1.3 $ $Date: 2005/12/25 04:12:29 $
* @see PeriodicallyServicable
*/
public class PeriodicEventScheduler
    implements Runnable
{
//Bug 4677074 begin
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
//Bug 4677074 end
    // TODO: What the scheduler can also do, is to put these tasks at the top of 
    // the worker thread pool task queue, instead of executing the task on itself.
    
    /** Internal Thread holder */
    protected Thread _thread;    
    /** Scheduler instance holder */
    private static PeriodicEventScheduler _instance = null;
    /** Instance lock */
    private static Object instanceLock = new Object();
    /** Sorted task list */
    protected TimedTaskList sortedList;
    
    /** Thread runs while bRun is true (unless shutdown) */
    protected boolean bRun;
    /** Debug flag */
    protected boolean bDebug=false;
    /** Current time holder */
    protected transient long counter=0;
    /** Delay for time approximation */
    protected long delay_time_approx = 100;
    /** Approximate clock holder */
    protected ApproximateClock clock = null;

    static
    {
        _instance = new PeriodicEventScheduler();
    }
    
    /**
    * Method to access the PeriodicEventScheduler singleton. 
    * @return singleton object.
    */
    public static PeriodicEventScheduler getInstance()
    {
        return _instance;   
    }

    
    /**
    * Constructor invoked once for the singleton.
    */
    private PeriodicEventScheduler()
    {
        super();
        bRun = true;
        sortedList = new TimedTaskList();
        clock = new ApproximateClock (delay_time_approx);
        counter = getTime();
        _thread = new Thread (this, "PeriodicEventScheduler");
		_thread.setDaemon(true);
		_thread.start();
    }
    
    /**
    * Get approximate or actual time.
    * @return long current time.
    */
    private long getTime()
    {
        return clock.getActualTime(); // TODO : change actual time to perhaps approx time
    }

    /**
    * Add a PeriodicallyServicable object to the 'timed task execution queue'.
    * @param seconds time in seconds after which the task will be invoked for 
    * the first time. <I>Do not confuse with the frequency period which is given
    * by the getTimeIntervalForService() method on this object.</I>
    * @param obj object that has to be serviced in timed interval fashion.
    * @param startingTime start calling service() method in startingTime + frequency time.
    * @return boolean true on success, false otherwise
    */
    public synchronized boolean addTimeRepeatableTask(PeriodicallyServicable obj, int startingTime)
    {
        if(startingTime < 0 || obj.getFrequency() < 1)
        {
//Bug 4677074            if(bDebug) System.out.println("PeriodicEventScheduler::addTimeRepeatableTask() rejected task" + obj.toString());
//Bug 4677074 begin
	    if(com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,"PeriodicEventScheduler::addTimeRepeatableTask() rejected task" + obj.toString());
//Bug 4677074 end

            return false;
        }
        boolean bool = sortedList.addTask(obj, startingTime, counter);
        synchronized(instanceLock)
        {
            instanceLock.notify();
        }
        return bool;
    }
    
    /**
    * Remove the servicable object from the task list.
    * @param obj PeriodicallyServicable object to be removed from the task list.
    * @return boolean true on success, false otherwise
    */
    public synchronized boolean removeTimeRepeatableTask(PeriodicallyServicable obj)
    {
        if(executingTask.equals(obj))
        {
            removeExecutingTask=true;
            return true;
        }
        else
            return sortedList.removeTask(obj);            
    }
    
    /**
    * Insert the task object into the task list.
    * @param taskObj task object to insert.
    * @return boolean true on success, false otherwise
    */
    protected synchronized boolean insertSorted(TaskData taskObj)
    {
        return sortedList.insertTask(taskObj);
    }
    
    /** Maintained by the run() method */
    private PeriodicallyServicable executingTask=null;
    /** On remove, a check is made if the executingTask is the one to be removed. This is the flag set. */
    private boolean removeExecutingTask=false;
    
    /**
    * Start running the thread.
    */
    public void run()
    {
        TaskData task=null;

        while(bRun)
        {
            try
            {
                //if(bDebug) System.out.println("---run()" + sortedList.toString() + "---");
//Bug 4677074 begin
		//if(com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,"---run()" + sortedList.toString() + "---");
//Bug 4677074 end
                task = sortedList.getFirstTask();
                //if(bDebug) System.out.println("---run()" + sortedList.toString() + "+++");
//Bug 4677074 begin
                //if(com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,"---run()" + sortedList.toString() + "+++");
//Bug 4677074 end
                if(null==task)
                {
                    synchronized(instanceLock)
                    {
                        instanceLock.wait();
                        continue; // fetches a task
                    }
                }
                
                executingTask=task.obj;
                
                // got the first task
                counter = getTime();
                long sleepingTime = task.abs_execute_time - counter;
                if(sleepingTime > 0L)
                {
                    try
                    {
//Bug 4677074                        if(bDebug) System.out.println("Current time=" + (int)(counter/1000) + ", Sleeping for " + sleepingTime + " msec.");
//Bug 4677074 begin
			if(com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,"Current time=" + (int)(counter/1000) + ", Sleeping for " + sleepingTime + " msec.");
//Bug 4677074 end
                        Thread.sleep( sleepingTime );
                    }
                    catch(InterruptedException ieInner)
                    {
//Bug 4677074                        if(bDebug) System.out.println("PeriodicEventScheduler::run() > " + ieInner);
//Bug 4677074 begin
			if(com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,"PeriodicEventScheduler::run() > " + ieInner);
//Bug 4677074 end
                    }
                }else
                {
                    // a decision has to be made immediately if we want to execute 
                    // the task even if we missed the right time to execute it
                    if (!task.obj.getExecutionTolerance(Math.abs(sleepingTime)) )
                    {
//Bug 4677074                         if(bDebug) System.out.println("Missed scheduling for " + task.obj.toString());
//Bug 4677074 begin
			if(com.sun.enterprise.util.logging.Debug.enabled)  _logger.log(Level.FINE,"Missed scheduling for " + task.obj.toString());
//Bug 4677074 end
                        continue;
                    } else
                    {
//Bug 4677074                        if(bDebug) System.out.println("Executing after missing scheduling for " + task.obj.toString());
//Bug 4677074 begin
			if(com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,"Executing after missing scheduling for " + task.obj.toString());
//Bug 4677074 end
                    }
                }
              
                // now we can execute this task in multiple ways, one is on this thread,
                // other is on other task queues (by putting this task on the front of the Q)
                task.obj.service();
                
            }catch(InterruptedException ieOuter)
            {
//Bug 4677074                if(bDebug) System.out.println("PeriodicEventScheduler::run() > " + ieOuter);
//Bug 4677074 begin
			if(com.sun.enterprise.util.logging.Debug.enabled)  _logger.log(Level.FINE,"PeriodicEventScheduler::run() > " + ieOuter);
//Bug 4677074 end
            }
            catch(Exception e)
            {
                System.out.println("PeriodicEventScheduler::run() > " + e);
//Bug 4677074 begin
		_logger.log(Level.WARNING,"iplanet_util.generic_exception",e);
//Bug 4677074 end
            }
            finally
            {
                if (null!=task)
                {
                    counter = getTime();
                    // now put this task back into the Q
                    task.abs_execute_time = counter;
                    //if(bDebug) System.out.println("Adding in list " + task.obj.toString());
//Bug 4677074 begin
		    //if(com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,"Adding in list " + task.obj.toString());
//Bug 4677074 end
                    if(!removeExecutingTask)
                        insertSorted(task);
                    else
                        removeExecutingTask=false;
                    executingTask=null;
                }
            }
        } // while
    }
    
    public String toString()
    {
        return "[PeriodicEventScheduler: " + sortedList.toString() + "]";
    }
}

