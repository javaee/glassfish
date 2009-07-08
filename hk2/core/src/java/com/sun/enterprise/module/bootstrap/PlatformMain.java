package com.sun.enterprise.module.bootstrap;

import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
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

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setInstallationRoot(File root) {
        this.root = root;
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
     * @param ags arguments obtained from the command line for instance.
     * @throws Exception if anything goes wrong
     */
    public abstract void start(String[] ags) throws Exception;

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
