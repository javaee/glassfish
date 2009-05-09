package com.sun.enterprise.v3.server;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.component.Factory;
import org.jvnet.hk2.component.ComponentException;
import java.util.concurrent.ThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;

/**
 * Factory to create the scheduled executor service
 * 
 * @author Jerome Dochez
 */
@Service
@FactoryFor(ScheduledExecutorService.class)
public class ScheduledExecutorServiceFactory implements Factory {
    
    public Object getObject() throws ComponentException {
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                }
            }
            );

    }
}
