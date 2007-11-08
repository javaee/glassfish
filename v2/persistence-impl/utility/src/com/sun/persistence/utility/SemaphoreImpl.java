/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.utility;

import com.sun.persistence.utility.logging.Logger;
import com.sun.org.apache.jdo.util.I18NHelper;

import java.util.ResourceBundle;

/**
 * Implements a simple semaphore.
 * @author Dave Bristor
 * @author Marina Vatkina
 */
// db13166: I would rather we use Doug Lea's stuff, but don't want to
// introduce that magnitude of change at this point in time.
public class SemaphoreImpl implements Semaphore {
    /**
     * Where to log messages about locking operations
     */
    private static final Logger _logger = LogHelperUtility.getLogger();

    /**
     * For logging, indicates on whose behalf locking is done.
     */
    private final String _owner;

    /**
     * Synchronizes the lock.
     */
    private final Object _lock = new Object();

    /**
     * Thread which holds the lock.
     */
    private Thread _holder = null;

    /**
     * Semaphore counter.
     */
    private int _counter = 0;

    /**
     * I18N message handler
     */
    private final static I18NHelper messages = I18NHelper.getInstance(
            SemaphoreImpl.class);

    public SemaphoreImpl(String owner) {
        _owner = owner;
    }

    /**
     * Acquire a lock.
     */
    public void acquire() {
        boolean debug = _logger.isLoggable(Logger.FINEST);

        if (debug) {
            Object[] items = new Object[]{_owner, Thread.currentThread(),
                                          new Integer(_counter)};
            _logger.finest("utility.semaphoreimpl.acquire", items); // NOI18N
        }

        synchronized (_lock) {
            //
            // If the current thread already holds this lock, we simply
            // update the count and return.
            //
            if (Thread.currentThread() == _holder) {
                _counter++;

            } else {
                while (_counter > 0) {
                    try {
                        // wait for the lock to be released
                        _lock.wait();
                    } catch (InterruptedException e) {
                    }
                }
                _holder = Thread.currentThread();
                _counter++;

                if (debug) {
                    Object[] items = new Object[]{_owner,
                                                  Thread.currentThread(),
                                                  new Integer(_counter)};
                    _logger.finest("utility.semaphoreimpl.gotlock", items); // NOI18N
                }
            }
        }
    }

    /**
     * Release a lock.
     */
    public void release() {
        boolean debug = _logger.isLoggable(Logger.FINEST);

        if (debug) {
            Object[] items = new Object[]{_owner, Thread.currentThread(),
                                          new Integer(_counter)};
            _logger.finest("utility.semaphoreimpl.release", items); // NOI18N
        }

        synchronized (_lock) {
            //
            // If the current thread already holds this lock, we simply
            // update the count and return.
            //
            if (Thread.currentThread() == _holder) {
                if (--_counter == 0) {
                    _holder = null;
                    _lock.notify();
                }
            } else {
                throw new IllegalMonitorStateException(
                        messages.msg(
                                "utility.semaphoreimpl.wrongthread", // NOI18N
                                new Object[]{_owner, Thread.currentThread()}));
            }
        }
    }
}
