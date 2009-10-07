/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.v3.server;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.module.*;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.util.Result;
import com.sun.enterprise.v3.common.PlainTextActionReporter;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.logging.LogDomains;
import com.sun.appserv.server.util.Version;
import org.glassfish.api.Async;
import org.glassfish.api.FutureProvider;
import org.glassfish.api.Startup;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.branding.Branding;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.Init;
import org.glassfish.internal.api.PostStartup;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

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
    Version version;

    @Inject
    CommonClassLoaderServiceImpl commonCLS;

    //@Inject
    //ClassLoaderHierarchy cch;

    /**
     * A keep alive thread that keeps the server JVM from going down
     * as long as GlassFish kernel is up.
     */
    private Thread serverThread;

    public void start() {

        // See issue #5596 to know why we set context CL as common CL.
        Thread.currentThread().setContextClassLoader(
                commonCLS.getCommonClassLoader());
        run();

        final CountDownLatch latch = new CountDownLatch(1);

        // spwan a non-daemon thread that waits indefinitely for shutdown request.
        // This stops the VM process from exiting.
        serverThread = new Thread("GlassFish Kernel Main Thread"){
            @Override
            public void run() {
                logger.logp(Level.INFO, "AppServerStartup", "run",
                        "[{0}] started", new Object[]{this});

                // notify the other thread to continue now that a non-daemon
                // thread has started.
                latch.countDown();

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
        habitat.addComponent(null, this);
        habitat.addComponent(null, systemRegistry);
        habitat.addComponent(LogDomains.CORE_LOGGER, logger);
        Inhabitant<ProcessEnvironment> inh = habitat.getInhabitantByType(ProcessEnvironment.class);
        if (inh!=null) {
            habitat.remove(inh);
        }

        // remove all existing inhabitant to n
        habitat.removeAllByType(ProcessEnvironment.class);

        habitat.add(new ExistingSingletonInhabitant<ProcessEnvironment>(ProcessEnvironment.class, new ProcessEnvironment(ProcessEnvironment.ProcessType.Server)));

        Map<Class, Long> servicesTiming = new HashMap<Class, Long>();

        // run the init services
        for (Inhabitant<? extends Init> init : habitat.getInhabitants(Init.class)) {
            long start = System.currentTimeMillis();
            init.get();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(init.type() + " Init done in " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
            }
            if (logger.isLoggable(Level.FINE)) {
                servicesTiming.put(init.type(), (System.currentTimeMillis() - start));
            }
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
        for (final Inhabitant<? extends Startup> i : startups) {
            if (i.type().getAnnotation(Async.class)==null) {
                long start = System.currentTimeMillis();
                try {
                    Startup startup = i.get();
                    // the synchronous service was started successfully, let's check that it's not in fact a FutureProvider
                    if (startup instanceof FutureProvider) {
                        futures.addAll(((FutureProvider) startup).getFutures());
                    }
                } catch(RuntimeException e) {
                        e.printStackTrace();
                        logger.info("Startup service failed to start : " + e.getMessage());
                }
                if (logger.isLoggable(Level.FINE)) {
                    servicesTiming.put(i.type(), (System.currentTimeMillis() - start));
                }
            }
        }

        env.setStatus(ServerEnvironment.Status.starting);        
        events.send(new Event(EventTypes.SERVER_STARTUP), false);

        // finally let's calculate our starting times


        logger.info(version.getVersion()
                + " startup time : " + platform + "(" + (platformInitTime - context.getCreationTime()) + "ms)" +
                " startup services(" + (System.currentTimeMillis() - platformInitTime)  + "ms)" +
                " total(" + (System.currentTimeMillis() - context.getCreationTime()) + "ms)");

        printModuleStatus(Level.FINE);

        try {
			// it will only be set when called from AsadminMain and the env. variable AS_DEBUG is set to true
            long realstart = Long.parseLong(System.getProperty("WALL_CLOCK_START"));
            logger.info("TOTAL TIME INCLUDING CLI: "  + (System.currentTimeMillis() - realstart));
        }
        catch(Exception e) {
		}
        // wait for async services
        try {
            result.get(1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // do nothing, we are probably shutting down
        }
        if (logger.isLoggable(Level.FINE)) {
            for (Map.Entry<Class, Long> service : servicesTiming.entrySet()) {
                logger.info("Service : " + service.getKey() + " took " + service.getValue() + " ms");
            }
        }

        // all the synchronous and asynchronous services have started correctly, time to check
        // if a severe error happened that should trigger shutdown.
        if (shutdownRequested) {
            shutdown();
        }   else {
            for (Future<Result<Thread>> future : futures) {
                try {
                    try {
                        // wait for 3 seconds for an eventual status, otherwise ignore
                        if (future.get(3, TimeUnit.SECONDS).isFailure()) {
                            final Throwable t = future.get().exception();
                            logger.log(Level.SEVERE, "Shutting down v3 due to startup exception : " + t.getMessage());
                            logger.log(Level.FINE, future.get().exception().getMessage(), t);
                            events.send(new Event(EventTypes.SERVER_SHUTDOWN));
                            shutdown();
                            return;
                        }
                    } catch(TimeoutException e) {
                        logger.warning("Timed out, ignoring some startup service status");
                    }
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, t.getMessage(), t);    
                }
            }
        }

        env.setStatus(ServerEnvironment.Status.started);
        events.send(new Event(EventTypes.SERVER_READY), false);

        // now run the post Startup service
        for (Inhabitant<? extends PostStartup> postStartup : habitat.getInhabitants(PostStartup.class)) {
            postStartup.get();
        }

    }

    private void printModuleStatus(Level level)
    {
        if (!logger.isLoggable(level)) {
            return;
        }
        
        StringBuilder sb = new StringBuilder("Module Status Report Begins\n");
        // first started :

        for (Module m : systemRegistry.getModules()) {
            if (m.getState()== ModuleState.READY) {
                sb.append(m).append("\n");
            }
        }
        sb.append("\n");
        // then resolved
        for (Module m : systemRegistry.getModules()) {
            if (m.getState()== ModuleState.RESOLVED) {
                sb.append(m).append("\n");
            }
        }
        sb.append("\n");
        // finally installed
        for (Module m : systemRegistry.getModules()) {
            if (m.getState()!= ModuleState.READY && m.getState()!=ModuleState.RESOLVED) {
                sb.append(m).append("\n");
            }
        }
        sb.append("Module Status Report Ends");
        logger.log(level, sb.toString());
    }

    // TODO(Sahoo): Revisit this method after discussing with Jerome.
    private void shutdown() {

        CommandRunner runner = habitat.getByContract(CommandRunner.class);
        if (runner!=null) {
           final ParameterMap params = new ParameterMap();
            if (context.getArguments().containsKey("--noforcedshutdown")) {
                params.set("force", "false");    
            }
            runner.getCommandInvocation("stop-domain", new PlainTextActionReporter()).parameters(params).execute();
        }
    }

    public void stop() {

        env.setStatus(ServerEnvironment.Status.stopping);
        events.send(new Event(EventTypes.PREPARE_SHUTDOWN), false);

        try {
            for (Inhabitant<? extends Startup> svc : habitat.getInhabitants(Startup.class)) {
                if (svc.isInstantiated()) {
                    try {
                        svc.release();
                    } catch(Throwable e) {
                        e.printStackTrace();
                    }
                }
            }

            // first send the shutdown event synchronously
            env.setStatus(ServerEnvironment.Status.stopped);
            events.send(new Event(EventTypes.SERVER_SHUTDOWN), false);

            logger.info("Shutdown procedure finished");            

            // we send the shutdown events before the Init services are released since
            // evens handler can still rely on services like logging during their processing
            for (Inhabitant<? extends Init> svc : habitat.getInhabitants(Init.class)) {
                if (svc.isInstantiated()) {
                    try {
                        svc.release();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }


        } catch(ComponentException e) {
            // do nothing.
        }

        // notify the server thread that we are done, so that it can come out.
        if (serverThread!=null) {
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
}
