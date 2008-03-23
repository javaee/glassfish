package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.EjbContainer;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

import java.beans.PropertyChangeEvent;
import static org.junit.Assert.*;

/**
 * Simple container code that is interested in getting notification of injected model changes
 *
 * @author Jerome Dochez
 */
@Service
public class HttpListenerContainer implements ConfigListener {

    @Inject(name="http-listener-1")
    HttpListener httpListener;

    volatile boolean received=false;
    
    public synchronized void changed(PropertyChangeEvent[] events) {
        if (received) {
            // I am alredy happy
            return;
        }
        assertTrue(events.length==1);
        String listenerName = ((HttpListener) events[0].getSource()).getId();
        assertEquals("http-listener-1",listenerName);
        assertEquals("8989",events[0].getNewValue().toString());
        received = true;
    }

}
