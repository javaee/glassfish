package com.sun.enterprise.configapi.tests;

import org.jvnet.hk2.config.*;
import org.jvnet.hk2.annotations.Inject;

import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;

import com.sun.enterprise.config.serverbeans.HttpService;

/**
 * Fake container for http service configuration
 *
 * User: Jerome Dochez
 * Date: May 13, 2008
 * Time: 11:55:01 AM
 */
public class HttpServiceContainer implements ConfigListener {

    @Inject
    HttpService httpService;

    volatile boolean received=false;

    public synchronized UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        if (received) {
            // I am alredy happy
        }
        ConfigSupport.sortAndDispatch(events, new Changed() {
            public <T extends ConfigBeanProxy> void changed(TYPE type, Class<T> tClass, T t) {
                if (type==TYPE.ADD) {
                    received=true;
                }
                //System.out.println("Event type : " + type + " class " + tClass +" -> " + t);
            }
        }
        , Logger.getAnonymousLogger());
        return null;
    }
}
