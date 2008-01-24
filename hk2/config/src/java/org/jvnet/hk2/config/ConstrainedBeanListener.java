package org.jvnet.hk2.config;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

/**
 * Interface allowing to register a listener for vetoable events.
 *
 * @author Jerome Dochez
 */
public interface ConstrainedBeanListener {

    public void addVetoableChangeListener(VetoableChangeListener listener);

    public void removeVetoableChangeListener(VetoableChangeListener listener);
}
