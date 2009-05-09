package com.sun.enterprise.configapi.tests;

import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;

import com.sun.grizzly.config.dom.NetworkListeners;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * Fake container for http service configuration
 *
 * User: Jerome Dochez
 * Date: May 13, 2008
 * Time: 11:55:01 AM
 */
public class NetworkListenersContainer implements ConfigListener {

    @Inject
    NetworkListeners httpService;

    volatile boolean received=false;

    public synchronized UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        if (received) {
            // I am already happy
        }
        return ConfigSupport.sortAndDispatch(events, new Changed() {
            public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> tClass, T t) {
                if (type==TYPE.ADD) {
                    received=true;
                }

                // we did not deal with it, so it is unprocsseed
                return new NotProcessed("unimplemented by NetworkListenersContainer");
                //System.out.println("Event type : " + type + " class " + tClass +" -> " + t);
            }
        }, Logger.getAnonymousLogger());
    }
}
