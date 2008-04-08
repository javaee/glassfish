package org.glassfish.config.support;

import org.glassfish.config.support.TypedChangeEvent;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Oct 29, 2007
 * Time: 11:25:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class TypedVetoableChangeSupport extends VetoableChangeSupport {

    final private Object source;

    public TypedVetoableChangeSupport(Object sourceBean) {
        super(sourceBean);
        source = sourceBean;
    }

    public void fireVetoableChange(String propertyName, Object oldValue, Object newValue)
        throws PropertyVetoException {

        super.fireVetoableChange(new TypedChangeEvent(source, propertyName, oldValue, newValue));
    }
}
