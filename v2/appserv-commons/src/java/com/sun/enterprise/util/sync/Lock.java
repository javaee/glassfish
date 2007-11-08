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
 * Filename: Lock.java	
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
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/sync/Lock.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:31 $
 */
 

package com.sun.enterprise.util.sync;

import java.lang.Thread;
import java.lang.InterruptedException;
import java.lang.IllegalMonitorStateException;

import com.sun.enterprise.util.pool.TimedoutException;

/**
 * A <i>Lock</i> is a concurrency control class to support mutual exclusion. Some of
 * the additional functionality that is provided by this class w.r.t. synchronized
 * are :
 * <ul>
 *       <li> time out based lock acquiring semantics
 *       <li> supports try lock semantics (isLocked())
 *       <li> lock ownership can be determined (getOwner())
 * </ul>
 * The usage pattern is the standard acquire/release protocol.
 * <p>
 * The usage of Lock can be see as under:
 *  <p><hr><blockquote><pre>
 *    public class MyHashTable {
 *      private Lock lock = new Lock();
 *      .....
 *      .....
 *      public Object get(Key k) {
 *        try {
 *          lock.acquire()
 *          ....perform search to get the Object ...
 *        } finally {
 *          lock.release()
 *        }
 *      }
 *
 *      public Object set(Key k, Object o) {
 *        try {
 *          lock.acquire()
 *          ....perform operation to insert object ...
 *        } finally {
 *          lock.release()
 *        }
 *      }
 *    }
 * </pre></blockquote><hr>
 * <p>
 * This version of Lock also supports time out semantics when trying to acquire a
 * lock. If the lock is not acquired in the timeout parameter specified, a
 * TimedOutException is thrown. This would be extremely useful in avoiding deadlock
 * situations especially in I/O related multi-threaded access patterns.
 * <p>
 * This implementation of Lock is reentrant i.e. if the a thread that has a Lock
 * and tries to acquire the same lock, it would be given the lock
 * <p>
 * @author Dhiru Pandey 8/1/2000 
 */

public class Lock {

	private Thread owner = null;  // Current owner of the Lock
	private int lockCount = 0;    // number of times the lock is acquired by the owner
	
/**
 * This method tries to acquire the lock and waits forever till the lock becomes
 * available. If the lock is available, the invoking
 * thread proceeds else it ends up waiting till the lock becomes available. If the
 * invoking thread has already acquired this lock and tries to acquire it again the
 * lock object allows it to acquire it again.
 * <p>
 * @throws InterruptedException thrown if the calling thread is interrupted during
 * the execution of acquire()
 */
	public synchronized void acquireLock() throws InterruptedException {
		while(! tryGetLock())
			wait();
	}

/**
 * This method tries to acquire the lock but waits for waitTime in milliseconds for
 * the lock to be acquired. If the lock is available, the invoking
 * thread proceeds else it ends up waiting for the duration of waitTime. After that
 * if the lock is still no available TimedOutException is thrown. If the
 * invoking thread has already acquired this lock and tries to acquire it again the
 * lock object allows it to acquire it again.
 * <p>
 * @param waitTime time to wait in milliseconds to acquire the lock
 * @throws TimedOutException thrown if the lock is not acquired within the timeout
 * parameter specified.
 * @throws InterruptedException thrown if the calling thread is interrupted during
 * the execution of acquire()
 */
	public synchronized void acquireLock( long waitTime) throws 
							InterruptedException, TimedoutException
	{
		if (tryGetLock() == false && waitTime != 0) {
			if( waitTime == -1)
				while(! tryGetLock())
					wait();
			else {
				boolean isWaiting = true;
				long startTime = System.currentTimeMillis();
				long timeRemaining = waitTime;
				while( isWaiting) {
					wait( timeRemaining);
					if( tryGetLock())
						return;
					timeRemaining = startTime + waitTime - System.currentTimeMillis();
					isWaiting = (timeRemaining > 0);
				}
				throw new TimedoutException();
			}
		}
	}

// synchronized not needed since this method is only called
// from other methods which are synchronized

//	private synchronized boolean tryGetLock() {
	private boolean tryGetLock() {
		Thread t = Thread.currentThread();
		if( owner == null) {
			owner = t;
			lockCount = 1;
			return true;
		}
		if( owner == t) {
			++lockCount;
			return true;
		}
		return false;
	}

/**
 * This method tries to release the lock and notify other waiting threads that the
 * lock is available to be acquired.
 * If the invoking thread has already acquired the lock multiple times then this
 * method will not notify any waiting threads. In this scenario the invoking thread
 * should call release() as many times on the lock object as acquire() was invoked,
 * only after that other threads waiting for this lock will be notified.
 * <p>
 */
	public synchronized void releaseLock() {
		if(!owner.equals( Thread.currentThread()))
			throw new IllegalMonitorStateException();
		if(--lockCount == 0) {
			owner = null;
		  notify();
    }
	}

/**
 * This method can be used to test if a lock has been acquired/in use.
 * <p>
 * @return <code>true</code> if lock is still available else <code>false</code>
 */
	public synchronized boolean isLocked() { return (owner != null); }

/**
 * This method can be used to get the thread object that is the owner of the lock.
 * <p>
 * @return the owner of the lock
 */
	public Thread getOwner() { return this.owner; }
}

