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

import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.util.Result;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.util.http.mapper.Mapper;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.container.EndpointRegistrationException;

import java.util.logging.Logger;
import org.jvnet.hk2.component.Inhabitant;

/**
 * The Grizzly Service is responsible for starting Grizzly Port Unification
 * mechanism. It is also providing a runtime service where other
 * services (like admin for instance) can register endpoints adapter to
 * particular context root.
 *
 * @author Jerome Dochez
 * @author Jeanfrancois Arcand
 */
public class GrizzlyProxy implements NetworkProxy {


    protected GrizzlyServiceListener grizzlyListener;


    final Logger logger;


    final HttpListener httpListener;


    final HttpService httpService;


    private VirtualHostMapper vsMapper;


    private int portNumber;


    private Inhabitant<Mapper> onePortMapper;


    private GrizzlyService grizzlyService;


    //TODO: This must be configurable.
    private final static boolean isWebProfile =
            Boolean.parseBoolean(System.getProperty("v3.grizzly.webProfile", "true"));


    private static List<String> nvVsMapper = new ArrayList<String>();


    // Those Adapter MUST not be mapped through a VirtualHostMapper, as our
    // WebContainer already supports it.
    static{
        nvVsMapper.add("org.apache.catalina.connector.CoyoteAdapter");
        nvVsMapper.add(com.sun.enterprise.v3.admin.AdminAdapter.class.getName());
    }


    /**
     * TODO: We must configure Grizzly using the HttpService element,
     * <strong>not HttpListener only</strong>.
     */
    public GrizzlyProxy(GrizzlyService grizzlyService,
                        HttpListener httpListener,
                        HttpService httpService) {
        this.grizzlyService = grizzlyService;
        this.logger = grizzlyService.getLogger();
        this.httpListener = httpListener;
        this.httpService = httpService;

        String port = httpListener.getPort();
        portNumber = 8080;

        if (port==null) {
            logger.severe("Cannot find port information from domain.xml");
            throw new RuntimeException("Cannot find port information from domain configuration");
        }

        try {
            portNumber = Integer.parseInt(port);
        } catch(java.lang.NumberFormatException e) {
            logger.severe("Cannot parse port value : " + port + ", using port 8080");
        }

        configureGrizzly();
    }


    /**
     * Create a <code>GrizzlyServiceListener</code> based on a HttpService
     * configuration object.
     * @param port the port on which we need to listen.
     */
    private void configureGrizzly() {
        grizzlyListener = new GrizzlyServiceListener(grizzlyService);

        GrizzlyListenerConfigurator.configure(
                grizzlyListener, httpService, httpListener, portNumber,
                grizzlyService.getController(), isWebProfile);
        
        GrizzlyEmbeddedHttp geh = grizzlyListener.getEmbeddedHttp();
        Mapper mapper = new V3Mapper(logger);
        mapper.setPort(portNumber);
        geh.getContainerMapper().setMapper(mapper);
        geh.getContainerMapper().configureMapper();

        onePortMapper = new ExistingSingletonInhabitant<Mapper>(mapper);

        grizzlyService.getHabitat().addIndex(
            onePortMapper, "com.sun.grizzly.util.http.mapper.Mapper",
            String.valueOf(portNumber));
    }


    /**
     * Stops the Grizzly service.
     */
    public void stop() {
        grizzlyListener.stop();
    }


    public void destroy() {
        /*
         * FIXME: Uncomment as soon as Habitat.removeIndex has been fixed
         * grizzlyService.getHabitat().removeIndex("com.sun.grizzly.util.http.mapper.Mapper", String.valueOf(portNumber));
         */
    }


    @Override
    public String toString() {
        return "Grizzly on port " + httpListener.getPort();
    }


    /*
     * Registers a new endpoint (adapter implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the adapter instance passed in.
     * @param contextRoot for the adapter
     * @param endpointAdapter servicing requests.
     */
    public void registerEndpoint(String contextRoot, Collection<String> vsServers,
                                 Adapter endpointAdapter,
                                 ApplicationContainer container) throws EndpointRegistrationException {

        if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }
        // THis is a hack, but we don't want to add virtual server support
        // for the Web Container as it already supports it.
        if (!nvVsMapper.contains(endpointAdapter.getClass().getName())) {
            vsMapper.registerEndpoint(contextRoot, vsServers, endpointAdapter, container);
            endpointAdapter = vsMapper;
        }

        grizzlyListener.getEmbeddedHttp().registerEndpoint(contextRoot,
                vsServers, endpointAdapter, container);
    }


    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) throws EndpointRegistrationException {
        grizzlyListener.getEmbeddedHttp().unregisterEndpoint(contextRoot, app);
        vsMapper.unregisterEndpoint(contextRoot, app);
    }


    /**
     * Returns the context root for this adapter.
     * @return the context root
     */
    public String getContextRoot() {
        return "/";
    }


    public Future<Result<Thread>> start() {
        final GrizzlyFuture future = new GrizzlyFuture();
        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    grizzlyListener.start(future);
                } catch(InstantiationException e) {
                    logger.log(Level.SEVERE, "Cannot start grizzly listener", e);
                } catch(IOException e) {
                    logger.log(Level.SEVERE, "Cannot start grizzly listener", e);
                } catch (RuntimeException e) {
                    logger.log(Level.INFO, "Exception in grizzly thread", e);
                }  catch(Throwable e) {
                    logger.log(Level.INFO, e.getMessage(), e);
                }
            }
        };
        thread.start();
        logger.info("Listening on port " + grizzlyListener.getPort());
        return future;
    }


    public void setVsMapper(VirtualHostMapper vsMapper) {
        this.vsMapper = vsMapper;
    }


    public VirtualHostMapper getVsMapper() {
        return vsMapper;
    }


    public int getPort() {
        return portNumber;
    }

    public  final class  GrizzlyFuture  implements Future<Result<Thread>> {
            Result<Thread> result;
            CountDownLatch latch = new CountDownLatch(1);
            public void setResult(Result<Thread>result) {
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
                return latch.getCount()==0;
            }

            public Result<Thread> get() throws InterruptedException, ExecutionException {
                latch.await();
                return result;
            }

            public Result<Thread> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                latch.await(timeout, unit);
                return result;
            }
        }
}
