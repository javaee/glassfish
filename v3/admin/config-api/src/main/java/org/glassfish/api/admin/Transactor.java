package org.glassfish.api.admin;

import org.glassfish.api.admin.Transaction;

import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 * Any config-api that want to be part of a configuration transaction should implement this interface.
 *
 * @author Jerome Dochez
 */
public interface Transactor {

    public boolean join(Transaction t);

    public boolean canCommit(Transaction t);

    public void commit(Transaction t);

    public void abort(Transaction t);

    public List<PropertyChangeEvent> getTransactionEvents();

}
