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
import com.sun.enterprise.v3.admin.AdminAdapter;
import com.sun.logging.LogDomains;
import org.glassfish.api.Startup;
import org.glassfish.api.Async;
import org.glassfish.internal.api.Init;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import java.util.Calendar;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    public void run() {

        logger.fine("GlassFish v3 starting");
        logger.info("HK2 initialized in " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
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
        Collection<Init> inits = habitat.getAllByContract(Init.class);
        for (Init init : inits) {
            logger.fine("Init service : " + init);
            logger.info(init + " Init done in " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
        }
        logger.fine("Init done in " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");

        for (final Inhabitant i : habitat.getInhabitants(Startup.class)) {
            if (i.type().getAnnotation(Async.class)==null) {
                logger.fine("Startup service " + i.get());
                logger.info(i.get() + " startup done in " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
            }
        }

        logger.info("Glassfish v3 started in "
                    + (Calendar.getInstance().getTimeInMillis() - context.getCreationTime()) + " ms");

        // run the startup services
        Thread t = new Thread(new Runnable() {
            public void run() {
                for (final Inhabitant i : habitat.getInhabitants(Startup.class)) {
                    if (i.type().getAnnotation(Async.class)!=null) {
                        //logger.fine("Runs " + i.get() + "asynchronously");
                        i.get();
                    }
                }
            }
        }, "AppServerStartup");
        t.start();

        // now that we are all done with loading, I can accept administrative commands.
        AdminAdapter admin = habitat.getComponent(AdminAdapter.class);
        admin.ready();
        
    }
}
