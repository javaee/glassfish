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
import com.sun.enterprise.util.Result;
import com.sun.enterprise.v3.common.PlainTextActionReporter;
import com.sun.logging.LogDomains;
import org.glassfish.api.Startup;
import org.glassfish.api.Async;
import org.glassfish.api.FutureProvider;
import org.glassfish.api.admin.*;
import org.glassfish.api.branding.Branding;
import org.glassfish.api.event.EventListener.Event;

import org.glassfish.internal.api.Init;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.ComponentException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.event.*;
import org.glassfish.server.ServerEnvironmentImpl;

/**
 * Main class for Glassfish v3 startup
 * This class spawns a non-daemon Thread when the start() is called.
 * Having a non-daemon thread allows us to control lifecycle of server JVM.
 * The thead is stopped when stop() is called.
 *
 * @author Jerome Dochez, sahoo@sun.com
 */
@Service
public class AppServerStartup implements ModuleStartup {
    
    StartupContext context;

    final static Logger logger = LogDomains.getLogger(AppServerStartup.class, LogDomains.CORE_LOGGER);

    @Inject
    ServerEnvironmentImpl env;

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

    @Inject
    Branding branding;

    @Inject
    ClassLoaderHierarchy cch;

    /**
     * A keep alive thread that keeps the server JVM from going down
     * as long as GlassFish kernel is up.
     */
    private Thread serverThread;

    public void start() {
        final CountDownLatch latch = new CountDownLatch(1);

        // wait indefinitely for shutdown to be called by starting
        // a non-daemon thread.
        serverThread = new Thread("GlassFish Kernel Main Thread"){
            @Override
            public void run() {
                logger.logp(Level.INFO, "AppServerStartup", "run",
                        "[{0}] started", new Object[]{this});

                // notify the other thread to continue now that a non-daemon
                // thread has started.
                latch.countDown();

                // See issue #5596 to know why we set context CL as common CL.
                Thread.currentThread().setContextClassLoader(
                        cch.getCommonClassLoader());
                AppServerStartup.this.run();
                try {
                    synchronized (this) {
                        wait(); // Wait indefinitely until shutdown is requested
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.logp(Level.INFO, "AppServerStartup", "run",
                        "[{0}] exiting", new Object[]{this});
            }
        };

        // by default a thread inherits daemon status of parent thread.
        // Since this method can be called by non-daemon threads (e.g.,
        // PackageAdmin service in case of an update of bundles), we
        // have to explicitly set the daemon status to false.
        serverThread.setDaemon(false);
        serverThread.start();

        // wait until we have spwaned a non-daemon thread
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        
        String platform = System.getProperty("GlassFish_Platform");
        if (platform==null) {
            platform = "Embedded";
        }
        if (context==null) {
            System.err.println("Startup context not provided, cannot continue");
            return;
        }
        final long platformInitTime = System.currentTimeMillis();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Startup class : " + this.getClass().getName());
        }

        // prepare the global variables
        habitat.addComponent(null, systemRegistry);
        habitat.addComponent(LogDomains.CORE_LOGGER, logger);
        habitat.addComponent(null, new ProcessEnvironment(ProcessEnvironment.ProcessType.Server));

        // run the init services
        for (Inhabitant<? extends Init> init : habitat.getInhabitants(Init.class)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(init.type() + " Init started in " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
            }
            init.get();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(init.type() + " Init done in " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
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

        boolean shutdownRequested=false;
        ArrayList<Future<Result<Thread>>> futures = new ArrayList<Future<Result<Thread>>>();
        final List<Inhabitant<? extends Startup>> executedStartups = new ArrayList<Inhabitant<? extends Startup>>();
        for (final Inhabitant<? extends Startup> i : startups) {
            if (i.type().getAnnotation(Async.class)==null) {
                try {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.info(i.type() + " startup started at " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
                    }
                    Startup startup = i.get();
                    // the synchronous service was started successfully, let's check that it's not in fact a FutureProvider
                    if (startup instanceof FutureProvider) {
                        futures.addAll(((FutureProvider) startup).getFutures());
                    }
                } catch(RuntimeException e) {
                        logger.info("Startup service failed to start : " + e.getMessage());
                }
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(i.type() + " startup done at " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
                }
                executedStartups.add(i);
            }
        }

        // finally let's calculate our starting times


        logger.info(branding.getVersion()
                + " startup time : " + platform + "(" + (platformInitTime - context.getCreationTime()) + "ms)" +
                " startup services(" + (System.currentTimeMillis() - platformInitTime)  + "ms)" +
                " total(" + (System.currentTimeMillis() - context.getCreationTime()) + "ms)");

        // wait for async services
        try {
            result.get(1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // do nothing, we are probably shutting down
        }

        // all the synchronous and asynchronous services have started correctly, time to check
        // if a severe error happened that should trigger shutdown.
        if (shutdownRequested) {
            shutdown(startups, executedStartups);
        }   else {
            for (Future<Result<Thread>> future : futures) {
                try {
                    if (future.get().isFailure()) {
                        final Throwable t = future.get().exception();
                        logger.log(Level.SEVERE, "Shutting down v3 due to startup exception : " + t.getMessage());
                        logger.log(Level.FINE, future.get().exception().getMessage(), t);
                        events.send(new Event(EventTypes.SERVER_SHUTDOWN));
                        shutdown(startups, executedStartups);
                        return;
                    }
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, t.getMessage(), t);    
                }
            }
        }

        events.send(new Event(EventTypes.SERVER_READY));

    }

    // TODO(Sahoo): Revisit this method after discussing with Jerome.
    private final void shutdown(Collection<Inhabitant<? extends Startup>> startups, Collection<Inhabitant<? extends Startup>> executedServices) {

        CommandRunner runner = habitat.getByType(CommandRunner.class);
        if (runner!=null) {
           final Properties params = new Properties();
            if (context.getArguments().containsKey("--noforcedshutdown")) {
                params.put("force", "false");    
            }
            runner.doCommand("stop-domain", params, new PlainTextActionReporter());
            return;
        }
    }

    public void stop() {

        events.send(new Event(EventTypes.PREPARE_SHUTDOWN), false);

        try {
            for (Inhabitant<? extends Startup> svc : habitat.getInhabitants(Startup.class)) {
                try {
                    svc.release();
                } catch(Throwable e) {
                    e.printStackTrace();
                }
            }

            for (Inhabitant<? extends Init> svc : habitat.getInhabitants(Init.class)) {
                try {
                    svc.release();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            // first send the shutdown event synchronously
            events.send(new Event(EventTypes.SERVER_SHUTDOWN), false);

        } catch(ComponentException e) {
            // do nothing.
        }

        // notify the server thread that we are done, so that it can come out.
        synchronized (serverThread) {
            serverThread.notify();
        }
        try {
            serverThread.join(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
