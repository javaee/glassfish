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
 * Filename: BlockingQueue.java	
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
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/collection/BlockingQueue.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:09 $
 */
 
package com.sun.enterprise.util.collection;

import java.util.LinkedList;
import java.util.Collection;

import com.sun.enterprise.util.pool.TimedoutException;
//Bug 4677074 begin
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
//Bug 4677074 end

/**
 * A BlockingQueue is a queue where remove() blocks if the queue is empty. The thread calling
 *	remove() blocks if the queue is empty while the add() notifies any waiting thread.
 */
 
 /*
  * NOTE:- Make sure that you synchronize the entire object on critical sections. For example,
  *		DO NOT try to synchronize just the access to the lnked list, because the FastThreadPool
  *		
  */
public class BlockingQueue {
 
//Bug 4677074 begin
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
//Bug 4677074 end
	private boolean			closed = false;
	private boolean			aborted = false;
	private int			limit;
	private LinkedList		list;
        private int                     waiters=0; // added for 4682740
	
	/**
	 * Create a BlockingQueue that has an infinite queuelength with the
	 *	specified timeout.
	 * @param The maximum time remove() will block.
	 * @see remove()
	 */
	public BlockingQueue() {
		this(Integer.MAX_VALUE);
	}
	
	/**
	 * Create a BlockingQueue that has the specified queue limit with the
	 *	specified timeout.
	 * @param The maximum time remove() will block.
	 * @param The queue length after which TooManyTasksException is thrown.
	 * @see remove()
	 */
	public BlockingQueue(int queueLimit) {
		this.limit = queueLimit;
		this.list = new LinkedList();
                // START OF IASRI 4682740
                com.sun.enterprise.util.MonitorTask.addORBMonitorable(this);
                // END OF IASRI 4682740
	}
	
	
	/** 
	 * Add to the head of the queue. Probably a high priority job?
	 */
	public void addFirst(Object object)
		throws TooManyTasksException, QueueClosedException
	{
		if (closed)
			throw new QueueClosedException("Queue closed.");
		synchronized (list) {
		    if (list.size() >= limit) {
			    throw new TooManyTasksException("Too many tasks in queue...");
		    }
		    list.addFirst(object);
		    list.notify();
		}
	}
    
	/** 
	 * Add to the tail of the queue. 
	 */
	public void addLast(Object object)
		throws TooManyTasksException, QueueClosedException
	{
		if (closed)
			throw new QueueClosedException("Queue closed.");
		synchronized (list) {
		    if (list.size() >= limit) {
			    throw new TooManyTasksException("Too many tasks in queue...");
		    }
		    list.add(object);
		    list.notify();
		}
	}
    
	/** 
	 * Add the job at the specified position. Probably based on priority?
	 */
	public void add(int index, Object object)
		throws TooManyTasksException, QueueClosedException
	{
		if (closed)
			throw new QueueClosedException("Queue closed.");
		synchronized (list) {
		    if (list.size() >= limit) {
			    throw new TooManyTasksException("Too many tasks in queue...");
		    }
		    list.add(index, object);
		    list.notify();
		}
	}
	
	/**
	 * Appends all of the elements in the specified collection to the end of
	 * this list, in the order that they are returned by the specified 
	 * collection's iterator.
	 */
	public void addAll(Collection c)
		throws TooManyTasksException, QueueClosedException
	{
		if (closed)
			throw new QueueClosedException("Queue closed.");
		synchronized (list) {
		    if (list.size() >= limit) {
			    throw new TooManyTasksException("Too many tasks in queue...");
		    }
		    list.addAll(c);
		    list.notify();
		}
	}
	
    
    /**
    * 
    */
    public int size() {
        synchronized (list) {
    	    return list.size();
    	}
    }
    
    // Start 4682740 - ORB to support standalone monitoring 
    
    /**
     * Return the size of the queue, unsynchronized method.
     */
    public int getUnsyncSize () {
        return list.size();
    }
    
    /**
     * Return the number of waiting Threads on the queue.
     */
    public int getUnsyncWaitingThreads () {
        return waiters;
    }
    
    /**
     * Return a String with information about this queue. Good for monitoring.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("BlockingQueue [TW=").append(waiters);
        sb.append(", CS=").append(list.size());
        sb.append(", MS=").append(limit).append("]");
        return sb.toString();
    }
    // End 4682740 - ORB to support standalone monitoring
    
    /**
     * Remove a task from the queue. If there are no objects then the thread blocks. The thread
     *	will be notified if any object is added to the queue.
     * @return An object from the queue.
     */
	public Object remove(boolean canWait)
		throws InterruptedException, QueueClosedException
	{
		while (true) {
			if (aborted) {
			    throw new QueueClosedException("Queue closed....");
			}
			synchronized (list) {
			    if (list.size() > 0) {
			        //System.out.println(Thread.currentThread().getName() + ": GOT SOME TASK!!....");
//Bug 4677074 begin
				//_logger.log(Level.FINE,Thread.currentThread().getName() + ": GOT SOME TASK!!....");
//Bug 4677074 end
				    return list.removeFirst();
			    }
    			
			    if (closed) {
			        throw new QueueClosedException("Queue closed....");
			    } else {
			        if (! canWait) {
			            return null;
			        }
			        //System.out.println(Thread.currentThread().getName() + ": waiting....");
//Bug 4677074 begin
				//_logger.log(Level.FINE,Thread.currentThread().getName() + ": waiting....");
//Bug 4677074 end
                                waiters++; // added for 4682740
			        list.wait();
                                waiters--; // added for 4682740
			    }
			}
		}
	}

    
    /**
     * Remove a task from the queue. If there are no objects then the thread blocks. The thread
     *	will be notified if any object is added to the queue.
     * @return An object from the queue.
     */
	public Object remove(long waitFor)
		throws InterruptedException, QueueClosedException
	{
        // Fixed for Bug No. 4673949
        if (aborted) {
            throw new QueueClosedException("Queue closed....");
        }
        synchronized (list) {
            if (list.size() > 0) {
                //System.out.println(Thread.currentThread().getName() + ": GOT SOME TASK!!....");
//Bug 4677074 begin
		//_logger.log(Level.FINE,Thread.currentThread().getName() + ": GOT SOME TASK!!....");
//Bug 4677074 end
                return list.removeFirst();
            }

            if (closed) {
                throw new QueueClosedException("Queue closed....");
            } else {
                waiters++; // added for 4682740
                list.wait(waitFor);
                waiters--; // added for 4682740
                if (list.size() > 0) {
                    //System.out.println(Thread.currentThread().getName() + ": GOT SOME TASK!!....");
//Bug 4677074 begin
		    //_logger.log(Level.FINE,Thread.currentThread().getName() + ": GOT SOME TASK!!....");
//Bug 4677074 end
                    return list.removeFirst();
                } else {
                    // We timed out
                    return null;
                }
            }
        }   //Synchronized list
	}

	public void shutdown() {
		this.closed = true;
		synchronized (list) {
			list.notifyAll();
		}
	}
    
	public void abort() {
		this.closed = this.aborted = true;
		synchronized (list) {
			list.notifyAll();
		}
	}
    
    
}
