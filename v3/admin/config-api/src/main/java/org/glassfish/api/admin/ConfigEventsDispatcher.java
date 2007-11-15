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
 package org.glassfish.api.admin;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * ConfigEvent dispatcher, this is the main facility to register interest in config events for a 
 * particular config type or config instance. 
 *
 * TODO: this is half baked .
 *
 * @author Jerome Dochez
 */
public class ConfigEventsDispatcher implements VetoableChangeListener {

    Map<Class, ArrayList<ConfigListener>> configListeners = new HashMap<Class, ArrayList<ConfigListener>>();

	// TODO : add addListener/removeListener for config bean instances.

    /**
     * Add a new listener for a particular type of configuration.
     *
     * @param configBeanType the configuration type
     * @param listener the listener
     * @return true if the registration was successful
     */
    public synchronized boolean addListener(Class configBeanType, ConfigListener listener) {
        ArrayList<ConfigListener> listeners = configListeners.get(configBeanType);
        if (listeners==null) {
            listeners = new ArrayList<ConfigListener>();
            configListeners.put(configBeanType, listeners);
        }
        return listeners.add(listener);
    }

    /**
     * Removes a previously registered listener
     *
     * @param configBeanType  the configuration type the listener is registered for
     * @param listener the iistener
     * @return true if the unregistration was successful
     */

    public synchronized boolean removeListener(Class configBeanType, ConfigListener listener) {
        ArrayList<ConfigListener> listeners = configListeners.get(configBeanType);
        return  (listeners!=null && listeners.remove(listener));
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
            targets = (ArrayList<ConfigListener>) listeners.clone();    
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
