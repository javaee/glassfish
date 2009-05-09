package com.sun.enterprise.v3.server;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.component.Factory;
import org.jvnet.hk2.component.ComponentException;
import java.util.concurrent.ThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: Jerome Dochez
 * Date: May 22, 2008
 * Time: 3:27:59 PM
 */
@Service
@FactoryFor(ExecutorService.class)
public class ExecutorServiceFactory implements Factory {

    public Object getObject() throws ComponentException {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        }
        );
    }
}
