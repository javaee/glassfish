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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.jvnet.hk2.annotations.Service;

/**
 * Transactions is a singleton service that receives transaction notifications and dispatch these
 * notifications asynchronously to listeners.
 *
 * @author Jerome Dochez
 */

@Service
public final class Transactions {
    
    private static final Transactions singleton = new Transactions();
    
    // NOTE: synchronization on the object itself
    final List<ListenerInfo<TransactionListener>> listeners = new ArrayList<ListenerInfo<TransactionListener>>();

    final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {

        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
        
    });
        
    final private ListenerInfo<Object> configListeners = new ListenerInfo<Object>(null);
    
    private final class ListenerInfo<T> {
        
        private final T listener;
        
        private final BlockingQueue<Job> pendingJobs = new ArrayBlockingQueue<Job>(50);
        private CountDownLatch latch = new CountDownLatch(1);
        
        public ListenerInfo(T listener) {
            this.listener = listener;
            start();
        }
        
        public void addTransaction(Job job) {
                
            // NOTE that this is put() which blocks, *not* add() which will not block and will
            // throw an IllegalStateException if the queue is full.
            try {
                pendingJobs.put(job);
            } catch (InterruptedException e ) {
                throw new RuntimeException(e);
            }            
            
        }
        
        private void start() {

            executor.submit(new Runnable() {

                public void run() {
                    while (latch.getCount()>0) {
                        try {
                            final Job job  = pendingJobs.take();
                            try {
                                if ( job.mEvents.size() != 0 ) {
                                    job.process(listener);
                                }
                            } finally {
                                job.releaseLatch();
                            }
                        }
                        catch (InterruptedException e) {
                            // do anything here?
                        }
                    }
                }
                
            });
        }

        void stop() {
            latch.countDown();
            // last event to force the close
            pendingJobs.add(new TransactionListenerJob(new ArrayList<PropertyChangeEvent>(), null));
        }
    }
    /**
        A job contains an optional CountdownLatch so that a caller can learn when the
        transaction has "cleared" by blocking until that time.
     */
    private abstract static class Job<T> {
        protected final List<PropertyChangeEvent> mEvents;
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
        
        public abstract void process(T target);
    }
    
    private static class TransactionListenerJob extends Job<TransactionListener> {

        public TransactionListenerJob(List<PropertyChangeEvent> events, CountDownLatch latch) {
            super(events, latch);
        }
        
        @Override
        public void process(TransactionListener listener) {
            try {
                listener.transactionCommited(mEvents);
            } catch(Exception e) {
                e.printStackTrace();
            }            
        }
    }
    
    private static class ConfigListenerJob extends Job<Object> {

        public ConfigListenerJob(List events, CountDownLatch latch) {
            super(events, latch);
        }

        @Override
        public void process(Object target) {
            Set<ConfigListener> notifiedListeners = new HashSet<ConfigListener>();
            for (final PropertyChangeEvent evt : mEvents) {
                final Dom dom = (Dom) ((ConfigView) Proxy.getInvocationHandler(evt.getSource())).getMasterView();
                if (dom.getListeners() != null) {
                    for (final ConfigListener listener : dom.getListeners()) {
                        if (!notifiedListeners.contains(listener)) {
                            try {
                                // create a new array each time to avoid any potential array changes?
                                listener.changed(mEvents.toArray(new PropertyChangeEvent[mEvents.size()]));
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

    /**
     * add a new listener to all transaction events.
     *
     * @param listener to be added.
     */
    public void addTransactionsListener(TransactionListener listener) {
        synchronized(listeners) {
            listeners.add(new ListenerInfo(listener));
        }
    }

    /**
     * Removes an existing listener for transaction events
     * @param listener the registered listener
     * @return true if the listener unregistration was successful
     */
    public boolean removeTransactionsListener(TransactionListener listener) {
        synchronized(listeners) {
            for (ListenerInfo info : listeners) {
                if (info.listener==listener) {
                    info.stop();
                    return listeners.remove(info);
                }
            }
        }
        return false;
    }
    
    public List<TransactionListener> currentListeners() {
        synchronized(listeners) {            
            List<TransactionListener> l = new ArrayList<TransactionListener>();
            for (ListenerInfo<TransactionListener> info : listeners) {
                l.add(info.listener);
            }
            return l;
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
        
        final List<ListenerInfo> listInfos = new ArrayList<ListenerInfo>();
        listInfos.addAll(listeners);
        
        // create a CountDownLatch to implement waiting for events to actually be sent
        final CountDownLatch latch = waitTillCleared ? new CountDownLatch(listInfos.size()+1) : null;
        
        final Job job = new TransactionListenerJob( events, latch );
        
        // NOTE that this is put() which blocks, *not* add() which will not block and will
        // throw an IllegalStateException if the queue is full.
        try {
            for (ListenerInfo listener : listInfos) {
                listener.addTransaction(job);
            }
            // the config listener job
            configListeners.addTransaction(new ConfigListenerJob(events, latch));
            
            job.waitForLatch();
        } catch (InterruptedException e ) {
            throw new RuntimeException(e);
        }
    }

    public void waitForDrain() {
        // insert a dummy Job and block until is has been processed.  This guarantees
        // that all prior jobs have finished
        addTransaction( new ArrayList<PropertyChangeEvent>(), true );
        // at this point all prior transactions are guaranteed to have cleared
    }
    
    private Transactions() {        
    }
    
    public static final Transactions get() {
        return singleton;
    }
}






