/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.server;

import com.sun.enterprise.module.*;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.v3.admin.adapter.AdminConsoleAdapter;
import com.sun.logging.LogDomains;
import org.glassfish.api.Startup;
import org.glassfish.api.Async;
import org.glassfish.api.event.EventListener.Event;

import org.glassfish.internal.api.Init;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import java.util.Calendar;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;

/**
 * Main class for Glassfish v3 startup
 *
 * @author dochez
 */
@Service
public class AppServerStartup implements ModuleStartup {
    
    StartupContext context;

    final static Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    @Inject
    ServerEnvironment env;

    @Inject
    Habitat habitat;

    @Inject
    ModulesRegistry systemRegistry;

    @Inject
    public void setStartupContext(StartupContext context) {
        this.context = context;
    }

    @Inject
    ExecutorService executor;

    @Inject
    Events events;
    
    public void run() {

        logger.fine("Module subsystem initialized in " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
        if (context==null) {
            System.err.println("Startup context not provided, cannot continue");
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Startup class : " + this.getClass().getName());
        }

        // prepare the global variables
        habitat.addComponent(null, systemRegistry);
        habitat.addComponent(LogDomains.CORE_LOGGER, logger);

        // run the init services
        for (Inhabitant<? extends Init> init : habitat.getInhabitants(Init.class)) {
            init.get();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(init + " Init done in " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Init done in " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
        }

        // run the startup services
        final Collection<Inhabitant<? extends Startup>> startups = habitat.getInhabitants(Startup.class);
        Future<?> result = executor.submit(new Runnable() {
            public void run() {
                for (final Inhabitant<? extends Startup> i : startups) {
                    if (i.type().getAnnotation(Async.class)!=null) {
                        //logger.fine("Runs " + i.get() + "asynchronously");
                        i.get();
                    }
                }
            }
        });

        for (final Inhabitant<? extends Startup> i : startups) {
            if (i.type().getAnnotation(Async.class)==null) {
                i.get();
                if (logger.isLoggable(Level.FINE)) {
                    logger.info(i.get() + " startup done in " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
                }

            }
        }

        logger.info("Glassfish v3 started in "
                    + (Calendar.getInstance().getTimeInMillis() - context.getCreationTime()) + " ms");

        // wait for async services
        try {
            result.get(1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // do nothing, we are probably shutting down
        }

        events.send(new Event(EventTypes.SERVER_READY));

    }
}
