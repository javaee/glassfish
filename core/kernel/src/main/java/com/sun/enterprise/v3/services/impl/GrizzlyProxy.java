/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.v3.services.impl;

import com.sun.enterprise.util.Result;
import com.sun.enterprise.v3.services.impl.monitor.GrizzlyMonitoring;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import org.jvnet.hk2.config.types.Property;
import com.sun.grizzly.Controller;
import com.sun.grizzly.ControllerStateListener;
import com.sun.grizzly.config.GrizzlyEmbeddedHttp;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.StaticResourcesAdapter;
import com.sun.grizzly.util.http.mapper.Mapper;
import com.sun.grizzly.util.Grizzly;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.internal.grizzly.V3Mapper;
import org.jvnet.hk2.component.Inhabitant;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for configuring Grizzly {@link SelectorThread}.
 *
 * @author Jerome Dochez
 * @author Jeanfrancois Arcand
 */
public class GrizzlyProxy implements NetworkProxy {
    protected GrizzlyListener grizzlyListener;
    final Logger logger;
    final NetworkListener networkListener;
    private int portNumber;

    public final static String LEADER_FOLLOWER
            = "com.sun.grizzly.useLeaderFollower";

    public final static String AUTO_CONFIGURE
            = "com.sun.grizzly.autoConfigure";

    // <http-listener> 'address' attribute
    private InetAddress address;

    private GrizzlyService grizzlyService;

    private VirtualServer vs;


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
        if(!("light-weight-listener".equals(networkListener.getProtocol()))) {
            registerMonitoringStatsProviders();
        }

        grizzlyListener = new GrizzlyListener(grizzlyService.getMonitoring(), new Controller(){
            public void logVersion(){}   
        }, networkListener.getName());
        final Protocol httpProtocol = networkListener.findHttpProtocol();
        V3Mapper mapper = null;
        // mapper initialization now happens *before* the GrizzlyListener
        // is configured.  This is necessary for WebSocket support (if enabled).
        ContainerMapper adapter = null;
        if (httpProtocol != null) {
            
            // A bit dirty fix for issue GLASSFISH-18211
            // We have to initialize Mapper before adding it to Habitat
            // @TODO rework the fix.
            mapper = new V3Mapper(logger);
            mapper.setPort(portNumber);
            mapper.setId(networkListener.getName());
            
            adapter = new ContainerMapper(
                    grizzlyService, networkListener);
            adapter.setMapper(mapper);
            adapter.setDefaultHost(httpProtocol.getHttp().getDefaultVirtualServer() /*grizzlyListener.getDefaultVirtualServer()*/);
            adapter.configureMapper();
            
            Inhabitant<Mapper> onePortMapper = new ExistingSingletonInhabitant<Mapper>(mapper);
            grizzlyService.getHabitat().addIndex(onePortMapper,
                                                 Mapper.class.getName(),
                                                 (networkListener.getAddress() + networkListener.getPort()));
        }

        grizzlyListener.configure(networkListener, grizzlyService.habitat);

        if(!grizzlyListener.isGenericListener()) {
            final GrizzlyEmbeddedHttp embeddedHttp = grizzlyListener.getEmbeddedHttp();

            if (httpProtocol != null) {
                adapter.setEmbeddedHttp(embeddedHttp);
                embeddedHttp.setAdapter(adapter);

                String ct = httpProtocol.getHttp().getDefaultResponseType();
                adapter.setDefaultContentType(ct);
                final Collection<VirtualServer> list = grizzlyService.getHabitat().getAllByContract(VirtualServer.class);
                final String vsName = httpProtocol.getHttp().getDefaultVirtualServer();
                for (VirtualServer virtualServer : list) {
                    if (virtualServer.getId().equals(vsName)) {
                        vs = virtualServer;
                        embeddedHttp.setWebAppRootPath(vs.getDocroot());

                        if (!grizzlyService.hasMapperUpdateListener() &&
                                vs.getProperty() != null && !vs.getProperty().isEmpty()) {
                            for (Property p: vs.getProperty()){
                                String name = p.getName();
                                if (name.startsWith("alternatedocroot")){
                                    String value = p.getValue();
                                    String[] mapping = value.split(" ");

                                    if (mapping.length != 2){
                                        logger.log(Level.WARNING, "Invalid alternate_docroot " + value);
                                        continue;
                                    }

                                    String docBase = mapping[1].substring("dir=".length());
                                    String urlPattern = mapping[0].substring("from=".length());
                                    try {
                                        StaticResourcesAdapter a = new StaticResourcesAdapter();
                                        a.addRootFolder(docBase);
                                        ArrayList<String> al = toArray(vs.getHosts(),";");
                                        al.add(grizzlyListener.getDefaultVirtualServer());
                                        registerEndpoint(urlPattern,al , a, null);
                                    } catch (EndpointRegistrationException ex) {
                                        logger.log(Level.SEVERE, "Unable to set alternate_docroot", ex);
                                    }

                                }
                            }
                        }
                        break;
                    }
                }

                adapter.addRootFolder(embeddedHttp.getWebAppRootPath());
                grizzlyService.notifyMapperUpdateListeners(networkListener, mapper);
            }
            
            boolean autoConfigure = false;
            // Avoid overriding the default with false
            if (System.getProperty(AUTO_CONFIGURE) != null){
                autoConfigure = true;
            }
            embeddedHttp.getController().setAutoConfigure(autoConfigure);

            boolean leaderFollower = false;
            // Avoid overriding the default with false
            if (System.getProperty(LEADER_FOLLOWER) != null){
                leaderFollower = true;
            }
            embeddedHttp.getController().useLeaderFollowerStrategy(leaderFollower);
        }
    }

    static ArrayList<String> toArray(String list, String token){
        return new ArrayList<String>(Arrays.asList(list.split(token)));
    }

    /**
     * Stops the Grizzly service.
     */
    @Override
    public void stop() {
        grizzlyListener.stop();
        grizzlyService.removeMapperLock(networkListener.getName());
    }

    @Override
    public void destroy() {
        if(!grizzlyListener.isGenericListener()) {
            grizzlyService.getHabitat().removeIndex(Mapper.class.getName(),
                        (networkListener.getAddress() + networkListener.getPort()));
            unregisterMonitoringStatsProviders();
        }
    }

    @Override
    public String toString() {
        return "GrizzlyProxy{" +
                "virtual server=" + vs +
                ", address=" + address +
                ", portNumber=" + portNumber +
                '}';
    }


    /*
    * Registers a new endpoint (adapter implementation) for a particular
    * context-root. All request coming with the context root will be dispatched
    * to the adapter instance passed in.
    * @param contextRoot for the adapter
    * @param endpointAdapter servicing requests.
    */
    @Override
    public void registerEndpoint(String contextRoot, Collection<String> vsServers, Adapter endpointAdapter,
        ApplicationContainer container) throws EndpointRegistrationException {
        
        if(grizzlyListener.isGenericListener()) {
            return;
        }

        // e.g., there is no admin service in an instance
        if (contextRoot == null) {
            return;
        }

        if (endpointAdapter == null) {
            throw new EndpointRegistrationException(
                "The endpoint adapter is null");
        }
        ((ContainerMapper)grizzlyListener.getEmbeddedHttp().getAdapter())
            .register(contextRoot, vsServers, endpointAdapter, container);
    }

    /**
     * Removes the contex-root from our list of endpoints.
     */
    @Override
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) throws EndpointRegistrationException {
        if(grizzlyListener.isGenericListener()) {
            return;
        }
        ((ContainerMapper) grizzlyListener.getEmbeddedHttp().getAdapter())
            .unregister(contextRoot);
    }

    @Override
    public Future<Result<Thread>> start() {
        final GrizzlyFuture future = new GrizzlyFuture();
        final long t1 = System.currentTimeMillis();
        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    final Thread t = Thread.currentThread();                    
                    grizzlyListener.initEndpoint();
                    grizzlyListener.getController().addStateListener(new ControllerStateListener() {
                        @Override
                        public void onStarted() {
                        }

                        @Override
                        public void onReady() {
                            if (logger.isLoggable(Level.INFO)){
                                logger.info("Grizzly Framework " + Grizzly.getRawVersion() + " started in: "
                                        + (System.currentTimeMillis() - t1)
                                        + "ms - bound to [" 
                                        + grizzlyListener.getListener().getAddress()
                                        + ':' + grizzlyListener.getPort() + ']');
                            }

                            future.setResult(new Result<Thread>(t));
                        }

                        @Override
                        public void onStopped() {
                        }

                        @Override
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
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        return future;
    }

    @Override
    public int getPort() {
        return portNumber;
    }

    @Override
    public InetAddress getAddress() {
        return address;
    }

    public GrizzlyListener getUnderlyingListener() {
        return grizzlyListener;
    }

    protected void registerMonitoringStatsProviders() {
        final String name = networkListener.getName();
        final GrizzlyMonitoring monitoring = grizzlyService.getMonitoring();

        monitoring.registerThreadPoolStatsProvider(name);
        monitoring.registerKeepAliveStatsProvider(name);
        monitoring.registerFileCacheStatsProvider(name);
        monitoring.registerConnectionQueueStatsProvider(name);
    }

    protected void unregisterMonitoringStatsProviders() {
        final String name = networkListener.getName();
        final GrizzlyMonitoring monitoring = grizzlyService.getMonitoring();

        monitoring.unregisterThreadPoolStatsProvider(name);
        monitoring.unregisterKeepAliveStatsProvider(name);
        monitoring.unregisterFileCacheStatsProvider(name);
        monitoring.unregisterConnectionQueueStatsProvider(name);
    }
    

    public static final class GrizzlyFuture implements Future<Result<Thread>> {
        Result<Thread> result;
        CountDownLatch latch = new CountDownLatch(1);

        public void setResult(Result<Thread> result) {
            this.result = result;
            latch.countDown();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return latch.getCount() == 0;
        }

        @Override
        public Result<Thread> get() throws InterruptedException {
            latch.await();
            return result;
        }

        @Override
        public Result<Thread> get(long timeout, TimeUnit unit) throws InterruptedException {
            latch.await(timeout, unit);
            return result;
        }
    }
}
