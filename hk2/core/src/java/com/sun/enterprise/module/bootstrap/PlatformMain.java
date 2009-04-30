package com.sun.enterprise.module.bootstrap;

import java.util.logging.Logger;
import java.io.File;

/**
 * Useful delegation model for starting a hk2 instance using a service based
 * lookup
 *
 * @Jerome Dochez
 */
public abstract class PlatformMain {

    Logger  logger;
    File    root;
    Object  context;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setInstallationRoot(File root) {
        this.root = root;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    public <T> T getContext(Class<T> contextType) {
        try {
            return contextType.cast(context);
        } catch(ClassCastException e) {
            return null;
        }
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
     */
    public abstract void start(String[] ags);
}
