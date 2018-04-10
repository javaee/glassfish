/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.startup;


import org.apache.catalina.*;
import org.apache.catalina.Logger;
import org.apache.catalina.core.StandardEngine;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;


/**
 * Startup event listener for a <b>Engine</b> that configures the properties
 * of that Engine, and the associated defined contexts.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:28:07 $
 */

public final class EngineConfig
    implements LifecycleListener {

    // ----------------------------------------------------- Static Variables

    private static final java.util.logging.Logger log = LogFacade.getLogger();
    private static final ResourceBundle rb = log.getResourceBundle();


    // ----------------------------------------------------- Instance Variables

    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The Engine we are associated with.
     */
    private Engine engine = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {
        return (this.debug);
    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {
        this.debug = debug;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Process the START event for an associated Engine.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        // Identify the engine we are associated with
        try {
            engine = (Engine) event.getLifecycle();
            if (engine instanceof StandardEngine) {
                int engineDebug = ((StandardEngine) engine).getDebug();
                if (engineDebug > this.debug)
                    this.debug = engineDebug;
            }
        } catch (ClassCastException e) {
            String msg = MessageFormat.format(rb.getString(LogFacade.LIFECYCLE_EVENT_DATA_IS_NOT_ENGINE_EXCEPTION),
                                              event.getLifecycle());
            log(msg, e);
            return;
        }

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT))
            start();
        else if (event.getType().equals(Lifecycle.STOP_EVENT))
            stop();

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Log a message on the Logger associated with our Engine (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {
        Logger logger = null;
        if (engine != null) {
            logger = engine.getLogger();
        }
        if (logger != null) {
            logger.log("EngineConfig: " + message);
        } else {
            if (log.isLoggable(Level.INFO)) {
                log.log(Level.INFO, LogFacade.ENGINE_CONFIG, message);
            }
        }
    }


    /**
     * Log a message on the Logger associated with our Engine (if any)
     *
     * @param message Message to be logged
     * @param t Associated exception
     */
    private void log(String message, Throwable t) {
        Logger logger = null;
        if (engine != null) {
            logger = engine.getLogger();
        }
        if (logger != null) {
            logger.log("EngineConfig: " + message, t, Logger.WARNING);
        } else {
            String msg = MessageFormat.format(rb.getString(LogFacade.ENGINE_CONFIG),
                                              message);
            log.log(Level.WARNING, msg, t);
        }
    }


    /**
     * Process a "start" event for this Engine.
     */
    private void start() {

        if (debug > 0)
            log(rb.getString(LogFacade.ENGINE_CONFIG_PROCESSING_START_INFO));

    }


    /**
     * Process a "stop" event for this Engine.
     */
    private void stop() {

        if (debug > 0)
            log(rb.getString(LogFacade.ENGINE_CONFIG_PROCESSING_STOP_INFO));

    }


}
