package com.sun.enterprise.module.bootstrap;

import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.io.File;

/**
 * Useful delegation model for starting a hk2 instance using a service based
 * lookup
 *
 * @author Jerome Dochez
 */
public abstract class PlatformMain {

    protected Logger  logger;
    protected File    root;
    List<Object> contexts = new ArrayList<Object>();

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setContext(Object context) {
        contexts.add(context);
    }

    public <T> T getContext(Class<T> contextType) {
        // first one is returned.
        for (Object context : contexts) {
            try {
                return contextType.cast(context);
            } catch(ClassCastException e) {
            }
        }
        return null;
    }

    public List<Object> getContexts() {
        List<Object> copy = new ArrayList<Object>();
        copy.addAll(contexts);
        return copy;
    }

    /**
     * Returns the platform name associated with this main.
     * @return ther plaform name
     */
    public abstract String getName();

    /**
     * Starts the main entry point using this platform implementation.
     *
     * @param ctx Context in which this object executes. It contains arguments passed to the program as well.
     * @throws Exception if anything goes wrong
     */
    public abstract void start(Properties ctx) throws Exception;

    /**
     * Optional method.
     * Returns singleton services after the start method was executed successfully.
     *
     * @param serviceType type of the requested service
     * @param <T> service type
     * @return service instance
     */
    public <T> T getStartedService(Class<T> serviceType) {
        return null;
    }
}
