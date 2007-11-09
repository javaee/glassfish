package com.sun.enterprise.config.serverbeans;

import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;

/**
 * ConstrainedBean interface is implemented by config beans that supports
 * JavaBeans constrained properties
 *
 * @author Jerome Dochez
 */
public interface ConstrainedBean {

    public VetoableChangeSupport getVetoableChangeSupport();

    public void addVetoableChangeListener(VetoableChangeListener listener);

    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener);

    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listner);

    public void removeVetoableChangeListener(VetoableChangeListener listener);

}
