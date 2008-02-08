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
package org.jvnet.hk2.config;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Proxy;

/**
 * Transactions is a singleton service that receives transaction notifications and dispatch these
 * notifications asynchronously to listeners.
 *
 * @author Jerome Dochez
 */
public class Transactions {

    final private static Transactions t = new Transactions();
    final List<TransactionListener> listeners = new ArrayList<TransactionListener>();
    // Use a blocking queue out of conveniene, in fact the synchornization is done with the Lock and condition below
    final private BlockingQueue<List<PropertyChangeEvent>> pendingRecords = new ArrayBlockingQueue<List<PropertyChangeEvent>>(5);
    Thread pump;

    /**
     * Lock and Condition to be able to monitor that all events have been
     * dispatched to all listeners before declaring the events backlog is empty
     */
    final Semaphore semaphore = new Semaphore(100);
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
    public synchronized void addTransactionsListener(TransactionListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an existing listener for transaction events
     * @param listener the registered listener
     * @return true if the listener unregistration was successful
     */
    public synchronized boolean removeTransactionsListener(TransactionListener listener) {
        return listeners.remove(listener); 
    }

    /**
     * Notification of a new transation completion
     *
     * @param events accumulated list of changes
     */
    void addTransaction(List<PropertyChangeEvent> events) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pendingRecords.add(events);
    }

    /**
     * Returns true if there are transaction events yet to be received by listeners
     *
     * @return true if the are pending events notifications.
     */
    public boolean pendingTransactionEvents() {
        return 100!=semaphore.availablePermits();
    }

    public void waitForDrain() {
        while (pendingTransactionEvents());
    }

    private Transactions() {
        pump = new Thread() {
            public void run() {
                while (true) {
                    try {
                        List<PropertyChangeEvent> events = pendingRecords.take();
                        for (TransactionListener listener : new ArrayList<TransactionListener>(listeners)) {
                            try {
                                listener.transactionCommited(events);
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                        for (PropertyChangeEvent evt : events) {
                            Dom dom = (Dom) ((ConfigView) Proxy.getInvocationHandler(evt.getSource())).getMasterView();
                            if (dom.getListeners() != null) {
                                for (ConfigListener listener : dom.getListeners()) {
                                    try {
                                        listener.changed(events.toArray(new PropertyChangeEvent[events.size()]));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        semaphore.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        };
        pump.start();
    }
}
