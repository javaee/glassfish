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
 */
package org.jvnet.hk2.config;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Proxy;

/**
 * Transactions is a singleton service that receives transaction notifications and dispatch these
 * notifications asynchronously to listeners.
 *
 * @author Jerome Dochez
 */
public final class Transactions {
    final private static Transactions t = new Transactions();
    
    // NOTE: synchronization on the object itself
    final List<TransactionListener> listeners = new ArrayList<TransactionListener>();
    
    // Use a blocking queue out of conveniene, in fact the synchornization is done with the Lock and condition below
    final private BlockingQueue<Job> pendingJobs = new ArrayBlockingQueue<Job>(5);
    final Thread mNotififierThread;

    /**
        A job contains an optional CountdownLatch so that a caller can learn when the
        transaction has "cleared" by blocking until that time.
     */
    private static final class Job {
        final List<PropertyChangeEvent> mEvents;
        private final CountDownLatch mLatch;
        
        public Job( final List<PropertyChangeEvent> events, final CountDownLatch latch ) {
            mEvents = events;
            mLatch  = latch;
        }
        
        public void waitForLatch() throws InterruptedException {
            if ( mLatch != null ) {
                mLatch.await();
            }
        }
        
        public void releaseLatch() {
            if ( mLatch != null ) {
                mLatch.countDown();
            }
        }
    }


    /**
     * Returns the singleton service
     *
     * @return  the singleton service
     */
    public static Transactions get() {
        return t;
    }

    /**
     * add a new listener to all transaction events.
     *
     * @param listener to be added.
     */
    public void addTransactionsListener(TransactionListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes an existing listener for transaction events
     * @param listener the registered listener
     * @return true if the listener unregistration was successful
     */
    public boolean removeTransactionsListener(TransactionListener listener) {
        synchronized(listeners) {
            return listeners.remove(listener);
        }
    }
    
    /**
        To make a copy of the list, we must hold the lock while we copy it:
        what happens if add/removeTransactionsListener is called while copying?!
     */
    public List<TransactionListener> currentListeners() {
        synchronized(listeners) {
            return new ArrayList<TransactionListener>(listeners);
        }
    }


    /** maintains prior semantics of add, and return immediately */
    void addTransaction( final List<PropertyChangeEvent> events) {
        addTransaction(events, false);
    }
        
    /**
     * Notification of a new transation completion
     *
     * @param events accumulated list of changes
     * @param waitTillCleared  synchronous semantics; wait until all change events are sent
     */
    void addTransaction(
        final List<PropertyChangeEvent> events,
        final boolean waitTillCleared ) {
        
        // create a CountDownLatch to implement waiting for events to actually be sent
        final CountDownLatch latch = waitTillCleared ? new CountDownLatch(1) : null;
        
        final Job job = new Job( events, latch );
        
        // NOTE that this is put() which blocks, *not* add() which will not block and will
        // throw an IllegalStateException if the queue is full.
        try {
            pendingJobs.put(job);
            job.waitForLatch();
        } catch (InterruptedException e ) {
            throw new RuntimeException(e);
        }
    }

    
    /**
        Returns true if there are transaction events yet to be received by listeners
        <p>
        The return value is meaningless in a threaded environment
        unless  all events queued prior to this call have *actually been
        sent to all listeners*, as opposed to having been pulled off the queue and about to be sent,
        partially sent, etc.  This method should be removed, since waitForDrain() does what is needed.
     * @return true if the are pending events notifications.
    public boolean pendingTransactionEvents() {
        waitForDrain();
        return false;
    }
     */

    public void waitForDrain() {
        // insert a dummy Job and block until is has been processed.  This guarantees
        // that all prior jobs have finished
        addTransaction( new ArrayList<PropertyChangeEvent>(), true );
        // at this point all prior transactions are guaranteed to have cleared
    }

    public void shutdown() {
        mNotififierThread.interrupt();
    }

    private final class ListenerNotifierThread extends Thread {
        public ListenerNotifierThread() {}
        
        private void processJob( final Job job ) {
            try {
                final List<PropertyChangeEvent> events = job.mEvents;
                if ( events.size() != 0 ) {
                    // copy the 'listeners' list so as to avoid concurrency issues
                    final List<TransactionListener> curListeners = currentListeners();
                    
                    for ( final TransactionListener listener : curListeners ) {
                        try {
                            listener.transactionCommited(events);
                        } 
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Set<ConfigListener> notifiedListeners = new HashSet<ConfigListener>();
                    for ( final PropertyChangeEvent evt : events) {
                        final Dom dom = (Dom)((ConfigView) Proxy.getInvocationHandler(evt.getSource())).getMasterView();
                        if (dom.getListeners() != null) {
                            for ( final ConfigListener listener : dom.getListeners()) {
                                if (!notifiedListeners.contains(listener)) {
                                    try {
                                        // create a new array each time to avoid any potential array changes?
                                        listener.changed(events.toArray(new PropertyChangeEvent[events.size()]));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                notifiedListeners.add(listener);
                            }
                        }
                    }
                }
            }
            finally {
                job.releaseLatch();
            }
        }
        
        public void run() {
            while (true) {
                try {
                    final Job job  = pendingJobs.take();
                    processJob(job);
                }
                catch (InterruptedException e)
                {
                    // do anything here?
                }
            }
        }
    }
    
    private Transactions() {
        mNotififierThread = new ListenerNotifierThread(); 
        mNotififierThread.start();
    }
}






