package org.glassfish.api.admin;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Oct 29, 2007
 * Time: 1:48:02 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ConfigListener {

    public void configAdded(Object to, String propertyName, Object added) throws PropertyVetoException;

    public void changed(PropertyChangeEvent evt) throws PropertyVetoException;

    public void configRemoved(Object from, String propertyName, Object removed) throws PropertyVetoException;
    
}
