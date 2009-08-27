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
package com.sun.enterprise.v3.services.impl;

import com.sun.enterprise.util.Result;
import com.sun.enterprise.v3.admin.AdminAdapter;
import com.sun.enterprise.v3.admin.adapter.AdminConsoleAdapter;
import com.sun.enterprise.v3.services.impl.monitor.GrizzlyMonitoring;
import com.sun.grizzly.Controller;
import com.sun.grizzly.ControllerStateListener;
import com.sun.grizzly.config.GrizzlyEmbeddedHttp;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.util.http.mapper.Mapper;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.internal.grizzly.V3Mapper;
import org.jvnet.hk2.component.Inhabitant;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Grizzly Service is responsible for starting Grizzly Port Unification
 * mechanism. It is also providing a runtime service where other services (like
 * admin for instance) can register endpoints adapter to particular context
 * root.
 *
 * @author Jerome Dochez
 * @author Jeanfrancois Arcand
 */
public class GrizzlyProxy implements NetworkProxy {
    protected GrizzlyListener grizzlyListener;
    final Logger logger;
    final NetworkListener networkListener;
    private int portNumber;


    // <http-listener> 'address' attribute
    private InetAddress address;


    private Inhabitant<Mapper> onePortMapper;


    private GrizzlyService grizzlyService;

    private static final List<String> nvVsMapper = new ArrayList<String>();

    // Those Adapter MUST not be mapped through a VirtualHostMapper, as our
    // WebContainer already supports it.
    static {
        nvVsMapper.add("org.apache.catalina.connector.CoyoteAdapter");
        nvVsMapper.add(AdminAdapter.class.getName());
        nvVsMapper.add(AdminConsoleAdapter.class.getName());
    }

    public GrizzlyProxy(GrizzlyService service, NetworkListener listener) {
        grizzlyService = service;
        logger = service.getLogger();
        networkListener = listener;
        String port = networkListener.getPort();
        portNumber = 8080;
        if (port == null) {
            logger.severe("Cannot find port information from domain.xml");
            throw new RuntimeException("Cannot find port information from domain configuration");
        }
        try {
            portNumber = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            logger.severe("Cannot parse port value : " + port + ", using port 8080");
        }
        try {
            address = InetAddress.getByName(networkListener.getAddress());
        } catch (UnknownHostException ex) {
            logger.log(Level.SEVERE, "Unknown address " + address, ex);
        }
        configureGrizzly();
    }

    /**
     * Create a <code>GrizzlyServiceListener</code> based on a NetworkListener
     * configuration object.
     */
    private void configureGrizzly() {
        grizzlyListener = new GrizzlyListener(grizzlyService.getMonitoring(), new Controller(), networkListener.getName());
        grizzlyListener.configure(networkListener, grizzlyService.habitat);

        if(!grizzlyListener.isGenericListener()) {
            final V3Mapper mapper = new V3Mapper(logger);
            mapper.setPort(portNumber);
            mapper.setId(networkListener.getName());

            final GrizzlyEmbeddedHttp embeddedHttp = grizzlyListener.getEmbeddedHttp();
            // Issue 9284
            GrizzlyEmbeddedHttp.setWebAppRootPath(
                    System.getProperty("com.sun.aas.instanceRoot") + "/docroot");

            final ContainerMapper adapter = new ContainerMapper(grizzlyService, embeddedHttp);
            adapter.setMapper(mapper);
            adapter.setDefaultHost(grizzlyListener.getDefaultVirtualServer());
            adapter.configureMapper();

            embeddedHttp.setAdapter(adapter);

            onePortMapper = new ExistingSingletonInhabitant<Mapper>(mapper);
            grizzlyService.getHabitat().addIndex(onePortMapper,
                Mapper.class.getName(), networkListener.getPort());
            grizzlyService.notifyMapperUpdateListeners(networkListener, mapper);
        }

        registerMonitoringStatsProviders();
    }

    /**
     * Stops the Grizzly service.
     */
    public void stop() {
        grizzlyListener.stop();
    }

    public void destroy() {
        if(!grizzlyListener.isGenericListener()) {
            grizzlyService.getHabitat().removeIndex(Mapper.class.getName(),
                        String.valueOf(portNumber));
        }

        unregisterMonitoringStatsProviders();
    }

    @Override
    public String toString() {
        return "Grizzly on port " + networkListener.getPort();
    }

    /*
    * Registers a new endpoint (adapter implementation) for a particular
    * context-root. All request coming with the context root will be dispatched
    * to the adapter instance passed in.
    * @param contextRoot for the adapter
    * @param endpointAdapter servicing requests.
    */
    public void registerEndpoint(String contextRoot, Collection<String> vsServers, Adapter endpointAdapter,
        ApplicationContainer container) throws EndpointRegistrationException {
        if(grizzlyListener.isGenericListener()) {
            return;
        }
        if (endpointAdapter == null) {
            throw new EndpointRegistrationException(
                "The endpoint adapter is null");
        }
        ((ContainerMapper)grizzlyListener.getEmbeddedHttp().getAdapter())
            .register(contextRoot, vsServers, endpointAdapter, null, null);
    }

    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) throws EndpointRegistrationException {
        if(grizzlyListener.isGenericListener()) {
            return;
        }
        ((ContainerMapper) grizzlyListener.getEmbeddedHttp().getAdapter())
            .unregister(contextRoot);
    }

    public Future<Result<Thread>> start() {
        final GrizzlyFuture future = new GrizzlyFuture();
        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    final Thread t = Thread.currentThread();                    
                    grizzlyListener.initEndpoint();
                    grizzlyListener.getController().addStateListener(new ControllerStateListener() {
                        public void onStarted() {
                        }

                        public void onReady() {
                            future.setResult(new Result<Thread>(t));
                        }

                        public void onStopped() {
                        }

                        public void onException(Throwable throwable) {
                            future.setResult(new Result<Thread>(throwable));
                        }
                    });
                    grizzlyListener.startEndpoint();
                } catch (InstantiationException e) {
                    logger.log(Level.SEVERE, "Cannot start grizzly listener", e);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Cannot start grizzly listener", e);
                } catch (RuntimeException e) {
                    logger.log(Level.INFO, "Exception in grizzly thread", e);
                } catch (Throwable e) {
                    logger.log(Level.INFO, e.getMessage(), e);
                }
            }
        };
        thread.start();
        logger.info("Listening on port " + grizzlyListener.getPort());
        return future;
    }

    public int getPort() {
        return portNumber;
    }

    protected void registerMonitoringStatsProviders() {
        final String name = networkListener.getName();
        final GrizzlyMonitoring monitoring = grizzlyService.getMonitoring();

        monitoring.registerThreadPoolStatsProvider(name);
        monitoring.registerKeepAliveStatsProvider(name);
        monitoring.registerFileCacheStatsProvider(name);
        monitoring.registerConnectionsStatsProvider(name);
    }

    protected void unregisterMonitoringStatsProviders() {
        final String name = networkListener.getName();
        final GrizzlyMonitoring monitoring = grizzlyService.getMonitoring();

        monitoring.unregisterThreadPoolStatsProvider(name);
        monitoring.unregisterKeepAliveStatsProvider(name);
        monitoring.unregisterFileCacheStatsProvider(name);
        monitoring.unregisterConnectionsStatsProvider(name);
    }
    

    public final class GrizzlyFuture implements Future<Result<Thread>> {
        Result<Thread> result;
        CountDownLatch latch = new CountDownLatch(1);

        public void setResult(Result<Thread> result) {
            this.result = result;
            latch.countDown();
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return latch.getCount() == 0;
        }

        public Result<Thread> get() throws InterruptedException {
            latch.await();
            return result;
        }

        public Result<Thread> get(long timeout, TimeUnit unit) throws InterruptedException {
            latch.await(timeout, unit);
            return result;
        }
    }
}
