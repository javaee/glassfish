package org.glassfish.api.admin;

import org.glassfish.api.admin.TypedChangeEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Oct 29, 2007
 * Time: 1:44:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigEventsDispatcher implements VetoableChangeListener {

    Map<Class, ArrayList<ConfigListener>> configListeners = new HashMap<Class, ArrayList<ConfigListener>>();

    public synchronized boolean addListener(Class configBeanType, ConfigListener listener) {
        ArrayList<ConfigListener> listeners = configListeners.get(configBeanType);
        if (listeners==null) {
            listeners = new ArrayList<ConfigListener>();
            configListeners.put(configBeanType, listeners);
        }
        return listeners.add(listener);
    }

    public synchronized boolean removeListener(Class configBeanType, ConfigListener listener) {
        ArrayList<ConfigListener> listeners = configListeners.get(configBeanType);
        if (listeners!=null) {
            return listeners.remove(listener);
        }
        return false;

    }


    /**
     * This method gets called when a constrained property is changed.
     *
     * @param evt a <code>PropertyChangeEvent</code> object describing the
     *            event source and the property that has changed.
     * @throws java.beans.PropertyVetoException
     *          if the recipient wishes the property
     *          change to be rolled back.
     */
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {

        ArrayList<ConfigListener> listeners = configListeners.get(evt.getSource().getClass());
        if (listeners==null) {
            return;
        }
        ArrayList<ConfigListener> targets;
        synchronized(listeners) {
            targets = (ArrayList) listeners.clone();    
        }
        try {
            for (ConfigListener listener : targets) {
                if (evt instanceof TypedChangeEvent) {
                    TypedChangeEvent typedEvent = TypedChangeEvent.class.cast(evt);
                    switch(typedEvent.getType()) {
                        case ADD:
                            listener.configAdded(evt.getSource(), evt.getPropertyName(), evt.getNewValue());
                            break;
                        case REMOVE:
                            listener.configRemoved(evt.getSource(), evt.getPropertyName(), evt.getOldValue());
                            break;
                        default:
                            listener.changed(evt);
                    }
                } else {
                    listener.changed(evt);
                }
            }
        } catch(PropertyVetoException pve) {
            for (ConfigListener listener : targets) {
                // revert everyone to the old value
                try {
                    PropertyChangeEvent newEvent = new PropertyChangeEvent(evt.getSource(), evt.getPropertyName(), evt.getNewValue(), evt.getOldValue());
                    if (evt instanceof TypedChangeEvent) {
                        TypedChangeEvent typedEvent = TypedChangeEvent.class.cast(evt);
                        switch(typedEvent.getType()) {
                            case ADD:
                                listener.configRemoved(evt.getSource(), evt.getPropertyName(), evt.getNewValue());
                                break;
                            case REMOVE:
                                listener.configAdded(evt.getSource(), evt.getPropertyName(), evt.getOldValue());
                                break;
                            default:
                                listener.changed(newEvent);
                        }
                    } else {
                        listener.changed(newEvent);
                    }
                } catch(PropertyVetoException ignore) {
                    
                }
            }
            // rethrow orignial event
            throw pve;
        }
    }

    
}
