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
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/sync/Semaphore.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:31 $
 */
 

package com.sun.enterprise.util.sync;

import java.lang.Thread;
import java.lang.InterruptedException;
import java.lang.IllegalMonitorStateException;

/**
 * A <i>Semaphore</i> (counting Semaphore) is a concurrency control construct. It conforms 
 * to the standard acquire-release protocol. Counting Semaphores are widely used in 
 * implementation of multi-threaded bounded resource pools, collections etc.
 * <p>
 * A Semaphore maintains a set of totalPermits initialized in a constructor. Method acquire()
 * blocks in wait till a permit becomes available and then takes it. The release() method needs 
 * to be invoked to add the permit back to the Semaphore and notify a waiting
 * thread that a permit has become available to use. Method attemptAcquire() is
 * the same as acquire() except it fails on time-out. It returns true or false
 * based on whether it succeeded or failed. 
 * <p>
 * This implementation of Semaphore is non-reentrant i.e. if the a thread that has a
 * permit re-enters to acquire one more permit, it would be given another permit if
 * available.
 * <p>
 * @author Dhiru Pandey 8/1/2000 
 */

public class Semaphore {

  /**
   * Denotes the total number permits available for the Semaphore object
   */
  protected long totalPermits; // available permits

/**
 * Constructs a new Semaphore Object and sets the total number of permits for the
 * Semaphore.
 *
 * @param initialPermits Sets the total number of permits for the Semaphore
 */
  public Semaphore(long initialPermits) {
    totalPermits = initialPermits;  // set the total number of permits
  }

/**
 * This method tries to acquire a permit from the Semaphore. If all the permits are
 * taken then the calling thread will wait till it is notified that a permit has
 * become available
 * <p>
 * @throws InterruptedException thrown if the calling thread is interrupted during
 * the execution of acquire()
 */
  public void acquire() throws InterruptedException {
    if (Thread.interrupted()) 
      throw new InterruptedException();
    synchronized (this) {
      try {
        while (totalPermits <= 0)
          wait();
        --totalPermits;
      } catch (InterruptedException ie) {
        notify();
        throw ie;
      }
    }
  }

/**
 * This method is variation on the acquire() method, wherein it takes a waitTime
 * parameter in milliseconds to decide how long to wait to get a permit. It also 
 * returns a <code>true</code> or <code>false</code> based on whether it was able
 * to get the permit or not.
 * <p>
 * @param waitTime time to wait in milliseconds for the permit to become available
 * @throws InterruptedException thrown if the calling thread is 
 *                              interrupted during the execution of attemptAcquire()
 * @return <code>true</code> if acquire succeeded <code>false</code> otherwise
 */
  public boolean attemptAcquire(long waitTime) throws InterruptedException {
    if (Thread.interrupted()) 
      throw new InterruptedException();
    synchronized (this) {
      if (totalPermits > 0) {  // just like acquire
        --totalPermits;
        return true;
      } else if (waitTime <= 0) {   // avoided timed wait
        return false;
      } else {
        try {
          long startTime = System.currentTimeMillis();
          long timeToWait = waitTime;
          
          while (true) {
            wait(timeToWait);
            if (totalPermits > 0) {
              --totalPermits;
              return true;
            } else {    // now check for timeout
              long now = System.currentTimeMillis();
              timeToWait = waitTime - (now - startTime);
              if (timeToWait <= 0)
                return false;
            }
          }
        } catch (InterruptedException ie) {
          notify();
          throw ie;
        }
      }
    }
  }

 /**
 * This method returns a permit to the Semaphore and will notify a waiting thread to
 * go ahead and try to acquire the permit.
 * <p>
 * @return void
 */
 public synchronized void release() {
    ++totalPermits;
    notify();
  }

}

