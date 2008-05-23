package org.glassfish.kernel.event;

import org.glassfish.api.event.EventFactory;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventListener.Event;
import org.jvnet.hk2.annotations.Service;

/**
 * User: Jerome Dochez
 * Date: May 23, 2008
 * Time: 10:31:21 AM
 */
@Service
public class EventFactoryImpl implements EventFactory {

    public void register(EventListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void send(Event event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
