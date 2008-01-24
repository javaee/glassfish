package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Contract;

import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 * Listener interface for objects interested in transaction events on the config beans.
 *
 * @author Jerome Dochez
 */
@Contract
public interface TransactionListener {

    public void transactionCommited(List<PropertyChangeEvent> changes);
}
