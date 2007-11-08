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
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/sync/RWLock.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:31 $
 */
 

package com.sun.enterprise.util.sync;

import java.lang.InterruptedException;
import java.util.LinkedList;
//Bug 4677074 begin
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
//Bug 4677074 end

/**
 * A <i>RWLock</i> provides concurrency control for multiple readers single writer
 * access patterns. This lock can provide access to multiple reader threads simultaneously
 * as long as there are no writer threads. Once a writer thread gains access to the
 * instance locked by a RWLock, all the reader threads wait till the writer completes
 * accessing the instance in question.
 * <p>
 * A RWLock is extremely useful in scenarios where there are lots more readers and
 * very few writers to a data structure. Also if the read operation by the reader
 * thread could take significant amount of time (binary search etc.)
 * <p>
 * The usage of Lock can be see as under:
 *  <p><hr><blockquote><pre>
 *    public class MyBTree {
 *      private RWLock lock = new Lock();
 *      .....
 *      .....
 *      public Object find(Object o) {
 *        try {
 *          lock.acquireReadLock();
 *          ....perform complex search to get the Object ...
 *          return result;
 *        } finally {
 *          lock.releaseReadLock();
 *        }
 *      }
 *
 *      public void insert(Object o) {
 *        try {
 *          lock.acquireWriteLock();
 *          ....perform complex operation to insert object ...
 *        } finally {
 *          lock.releaseWriteLock();
 *        }
 *      }
 *    }
 * </pre></blockquote><hr>
 * <p>
 * @author Dhiru Pandey 8/7/2000 
 */


public class RWLock {

//Bug 4677074 begin
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
//Bug 4677074 end
  int currentReaders;
  int pendingReaders;
  int currentWriters;
  Queue writerQueue = new Queue();

  /**
   * This method is used to acquire a read lock. If there is already a writer thread
   * accessing the object using the RWLock then the reader thread will wait until
   * the writer completes its operation
   */
  public synchronized void acquireReadLock() {
    if (currentWriters == 0 && writerQueue.size() == 0) {
      ++currentReaders;
    } else {
      ++pendingReaders;
      try {
        wait();
      } catch(InterruptedException ie) {
      }
    }
  }

  /**
   * This method is used to acquire a write lock. If there are already reader threads
   * accessing the object using the RWLock, then the writer thread will wait till all
   * the reader threads are finished with their operations.
   */
  public void acquireWriteLock() {
    Object lock = new Object();

    synchronized(lock) {
      synchronized(this) {
        if (writerQueue.size() == 0 && currentReaders == 0 && 
                                          currentWriters == 0) {
          ++currentWriters;
          // Use logging facility if you need to log this
//          System.out.println(" RW: incremented WriterLock count");
//Bug 4677074 begin
            _logger.log(Level.FINE," RW: incremented WriterLock count");
//Bug 4677074 end
          return;
        }
          writerQueue.enQueue(lock);
          // Use logging facility if you need to log this
//          System.out.println(" RW: Added WriterLock to queue");
//Bug 4677074 begin
            _logger.log(Level.FINE," RW: Added WriterLock to queue");
//Bug 4677074 end
      }
      try {
        lock.wait();
      } catch(InterruptedException ie) {
      }
    }
  }

  /**
   * This method is used to release a read lock. It also notifies any waiting writer thread
   * that it could now acquire a write lock.
   */
  public synchronized void releaseReadLock() {
    if (--currentReaders == 0) 
      notifyWriters();
  }

  /**
   * This method is used to release a write lock. It also notifies any pending
   * readers that they could now acquire the read lock. If there are no reader
   * threads then it will try to notify any waiting writer thread that it could now
   * acquire a write lock.
   */
  public synchronized void releaseWriteLock() {
    --currentWriters;
    if (pendingReaders > 0) 
      notifyReaders();
    else 
      notifyWriters();
  }

  private void notifyReaders() {
    currentReaders += pendingReaders;
    pendingReaders = 0;
    notifyAll();
  }
  
  private void notifyWriters() {
    if (writerQueue.size() > 0) {
      Object lock = writerQueue.deQueueFirst();
      ++currentWriters;
      synchronized(lock) { 
        lock.notify();
      }
    }
  }

  class Queue extends LinkedList {

    public Queue() {
      super();
    }

    public void enQueue(Object o) {
      super.addLast(o);
    }

    public Object deQueueFirst() {
      return super.removeFirst();
    }

  }

}

