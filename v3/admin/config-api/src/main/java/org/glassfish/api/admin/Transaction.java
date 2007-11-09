package org.glassfish.api.admin;

import org.glassfish.api.admin.Transactor;
import org.glassfish.api.admin.RetryableException;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple transaction mechanism for config-api objects
 */
public class Transaction {

    final LinkedList<Transactor> participants = new LinkedList<Transactor>();

    public synchronized void addParticipant(Transactor t) {
        participants.addLast(t);
    }

    public synchronized void rollback() {
        for (Transactor t : participants) {
            t.abort(this);
        }
    }

    public synchronized List<PropertyChangeEvent> commit() throws RetryableException {
        for (Transactor t : participants) {
            if (!t.canCommit(this)) {
                throw new RetryableException();
            }
        }
        List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        for (Transactor t : participants) {
            events.addAll(t.getTransactionEvents());
        }
        for (Transactor t : participants) {
            t.commit(this);            
        }
        return events;
    }
}
