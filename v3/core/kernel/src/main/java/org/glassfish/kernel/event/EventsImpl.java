package org.glassfish.kernel.event;

import com.sun.logging.LogDomains;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventListener.Event;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.deployment.common.DeploymentException;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.RestrictTo;

/**
 * Simple implementation of the events dispatching facility.
 * 
 * @author Jerome Dochez
 */
@Service
public class EventsImpl implements Events {

    @Inject
    ExecutorService executor;
    
    final static Logger logger = LogDomains.getLogger(EventsImpl.class, LogDomains.CORE_LOGGER);

    List<EventListener> listeners = Collections.synchronizedList(new ArrayList<EventListener>());

    public synchronized void register(EventListener listener) {
        listeners.add(listener);
    }

    public void send(final Event event) {
        send(event, true);
    }

    public void send(final Event event, boolean asynchronously) {
        
        List<EventListener> l = new ArrayList<EventListener>();
        l.addAll(listeners);
        for (final EventListener listener : l) {
            
            Method m =null;
            try {
                // check if the listener is interested with his event.
                m = listener.getClass().getMethod("event", Event.class);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(EventsImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(EventsImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (m!=null) {
                RestrictTo fooBar = m.getParameterTypes()[0].getAnnotation(RestrictTo.class);
                if (fooBar!=null) {
                    EventTypes interested = EventTypes.create(fooBar.value());
                    if (!event.is(interested)) {
                        continue;
                    }
                }
            }

            if (asynchronously) {
                executor.submit(new Runnable() {
                    public void run() {
                        try {
                            listener.event(event);
                        } catch(Exception e) {
                            logger.log(Level.WARNING, "Exception while dispatching an event", e);
                        }
                    }
                });
            } else {
                try {
                    listener.event(event);
                } catch (DeploymentException de) {
                    // when synchronous listener throws DeploymentException
                    // we re-throw the exception to abort the deployment
                    throw de;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Exception while dispatching an event", e);
                }
            }
        }
    }

    public synchronized boolean unregister(EventListener listener) {
        return listeners.remove(listener);
    }
}
