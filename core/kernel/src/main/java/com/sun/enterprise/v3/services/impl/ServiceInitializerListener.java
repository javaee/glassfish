/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2010 Oracle and/or its affiliates. All rights reserved.
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

import java.util.logging.Logger;

import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.grizzly.config.dom.Transport;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.threadpool.GrizzlyExecutorService;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.jvnet.hk2.component.Habitat;

/**
 * This class extends Grizzly's GrizzlyServiceListener class to customize it for GlassFish and enable a single listener
 * do both lazy service initialization as well as init of HTTP and admin listeners
 *
 * @author Vijay Ramachandran
 */
public class ServiceInitializerListener extends org.glassfish.grizzly.config.GenericGrizzlyListener {
    private final Logger logger;
    private final GrizzlyService grizzlyService;
//    private boolean isGenericListener = false;

//    private String name;

    public ServiceInitializerListener(final GrizzlyService grizzlyService,
            final Logger logger) {
        this.grizzlyService = grizzlyService;
        this.logger = logger;
    }

//    private NetworkListener listener;


//    public ServiceInitializerListener(GrizzlyMonitoring monitoring, NetworkListener controller) {
//        super(controller);
//    }

//    @Override
//    public void configure(final NetworkListener networkListener) throws IOException {
//        setName(networkListener.getName());
//
//        setPort(Integer.parseInt(networkListener.getPort()));
//
//        try {
//            setAddress(InetAddress.getByName(networkListener.getAddress()));
//        } catch (UnknownHostException e) {
//            logger.log(Level.WARNING, "Invalid address for {0}: {1}",
//                    new Object[]{
//                        networkListener.getName(),
//                        networkListener.getAddress()
//                    });
//            throw e;
//        }
//
//        configureDelayedExecutor();
//        configureTransport(networkListener.findTransport());
//        configureProtocol(networkListener.findProtocol(), rootFilterChain);
//        configureThreadPool(networkListener.findThreadPool());
//    }

    @Override
    protected void configureTransport(final Habitat habitat,
            Transport transportConfig) {
        
        transport = TCPNIOTransportBuilder.newInstance().build();

        rootFilterChain = FilterChainBuilder.stateless().build();
        rootFilterChain.add(new TransportFilter());

        transport.setProcessor(rootFilterChain);
    }


    @Override
    protected void configureProtocol(final Habitat habitat,
            final Protocol protocol, final FilterChain filterChain) {
        filterChain.add(new ServiceInitializerFilter(this, grizzlyService.getHabitat(), logger));
    }

    @Override
    protected void configureThreadPool(final Habitat habitat,
            final ThreadPool threadPool) {
        transport.setWorkerThreadPool(GrizzlyExecutorService.createInstance(
                ThreadPoolConfig.defaultConfig()));
    }

    /**
     * Configures the given grizzlyListener.
     */
//    @Override
//    public void configureListener(NetworkListener networkListener, Habitat habitat) {
//        this.listener = networkListener;
//        if ("light-weight-listener".equals(networkListener.getProtocol())) {
//            isGenericListener = true;
//        }
//        if (!isGenericListener) {
//            super.configureListener(networkListener);
//        } else {
//        initializeListener(networkListener, habitat);
//        }
//    }

//    private void initializeListener(NetworkListener networkListener, Habitat habitat) {
//        serviceInitializer = new ServiceInitializerThread(this, habitat);
//        serviceInitializer.setController(this.getController());
//        serviceInitializer.configure(networkListener);
//    }

//    public NetworkListener getListener() {
//        return this.listener;
//    }

//    @Override
//    public void start() throws IOException, InstantiationException {
//        serviceInitializer.initController();
//        serviceInitializer.startEndpoint();
//    }
//
//    @Override
//    public void stop() {
//        serviceInitializer.stopEndpoint();
//    }
//
//    @Override
//    public int getPort() {
//        return serviceInitializer.getPort();
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
    
    // --------------------------------------------------------- Private Methods
//    private void processDynamicCometConfiguration(PropertyChangeEvent event) {
//        final boolean enableComet = Boolean.valueOf(event.getNewValue().toString());
//        if (enableComet) {
//            enableComet();
//        } else {
//            disableComet();
//        }
//    }
//    private void enableComet() {
//        AsyncFilter cometFilter = createCometAsyncFilter();
//        if (cometFilter == null) {
//            return;
//        }
//        if (getEmbeddedHttp().getAsyncHandler() == null) {
//            AsyncHandler asyncHandler = new DefaultAsyncHandler();
//            getEmbeddedHttp().setAsyncHandler(asyncHandler);
//        }
//        getEmbeddedHttp().getAsyncHandler().addAsyncFilter(cometFilter);
//        final ProtocolChainInstanceHandler pcih =
//            getEmbeddedHttp().getController().getProtocolChainInstanceHandler();
//        if (!(pcih instanceof NonCachingInstanceHandler)) {
//            ProtocolChainInstanceHandler nonCaching =
//                new NonCachingInstanceHandler(pcih);
//            getEmbeddedHttp().getController().setProtocolChainInstanceHandler(nonCaching);
//        }
//        getEmbeddedHttp().setEnableAsyncExecution(true);
//    }
//
//    private void disableComet() {
//        getEmbeddedHttp().setAsyncHandler(null);
//        final ProtocolChainInstanceHandler pcih =
//            getEmbeddedHttp().getController().getProtocolChainInstanceHandler();
//        if (!(pcih instanceof NonCachingInstanceHandler)) {
//            ProtocolChainInstanceHandler nonCaching =
//                new NonCachingInstanceHandler(pcih);
//            getEmbeddedHttp().getController().setProtocolChainInstanceHandler(nonCaching);
//        }
//        getEmbeddedHttp().setEnableAsyncExecution(false);
//    }
//
//    @SuppressWarnings({"unchecked"})
//    private AsyncFilter createCometAsyncFilter() {
//        try {
//            Class<? extends AsyncFilter> c =
//                (Class<? extends AsyncFilter>) Class.forName("org.glassfish.grizzly.comet.CometAsyncFilter",
//                    true,
//                    Thread.currentThread().getContextClassLoader());
//            return c.newInstance();
//        } catch (Exception e) {
//            return null;
//        }
//    }
    // ---------------------------------------------------------- Nested Classes
    /**
     * This ProtocolChainInstanceHandler will be used to prevent GrizzlyEmbeddedHttp from caching the default PCIH that
     * isn't based on the async configuration change (i.e., if comet is enabled, the current PCIH will not handle async
     * execution properly, so we don't want it cached).
     */
//    private static final class NonCachingInstanceHandler implements ProtocolChainInstanceHandler {
//        private final ProtocolChainInstanceHandler wrapped;
//        // -------------------------------------------------------- Constructors
//
//        private NonCachingInstanceHandler(ProtocolChainInstanceHandler wrapped) {
//            this.wrapped = wrapped;
//        }
//        // --------------------------- Methods from ProtocolChainInstanceHandler
//
//        @Override
//        public ProtocolChain poll() {
//            return wrapped.poll();
//        }
//
//        @Override
//        public boolean offer(ProtocolChain protocolChain) {
//            return true;
//        }
//
//    } // END NonCachingInstanceHandler
}
