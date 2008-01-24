package org.jvnet.hk2.config;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.LogRecord;
import java.beans.PropertyChangeEvent;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 23, 2008
 * Time: 10:31:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Transactions {

    final static Transactions t = new Transactions();
    final List<TransactionListener> listeners = new ArrayList<TransactionListener>();
    final private BlockingQueue<List<PropertyChangeEvent>> pendingRecords = new ArrayBlockingQueue<List<PropertyChangeEvent>>(5);
    Thread pump;

    public static Transactions get() {
        return t;
    }

    public synchronized void listenToAllTransactions(TransactionListener listener) {
        if (listeners.size()==0) {
            start();
        }
        listeners.add(listener);
    }

    public void addTransaction(List<PropertyChangeEvent> events) {
        pendingRecords.add(events);
    }

    public boolean pendingTransactionEvents() {
        return !pendingRecords.isEmpty();
    }

    private void start() {
        pump = new Thread() {
            public void run() {
                while (true) {
                    try {
                        List<PropertyChangeEvent> events = pendingRecords.take();
                        for (TransactionListener listener : listeners) {
                            listener.transactionCommited(events);
                        }
                    } catch (InterruptedException e) {

                    }
                }
            }
        };
        pump.start();
    }
}
