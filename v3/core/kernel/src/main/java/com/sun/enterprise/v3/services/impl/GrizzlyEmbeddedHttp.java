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

import com.sun.grizzly.Controller;
import com.sun.grizzly.DefaultProtocolChainInstanceHandler;
import com.sun.grizzly.ProtocolChain;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.UDPSelectorHandler;
import com.sun.grizzly.filter.ReadFilter;
import com.sun.grizzly.http.DefaultProcessorTask;
import com.sun.grizzly.http.HttpWorkerThread;
import com.sun.grizzly.http.ProcessorTask;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import com.sun.grizzly.tcp.Adapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.glassfish.api.deployment.ApplicationContainer;

/**
 * Implementation of Grizzly embedded HTTP listener
 * 
 * @author Jeanfrancois Arcand
 * @author Alexey Stashok
 */
public class GrizzlyEmbeddedHttp extends SelectorThread implements EndpointMapper<Adapter> {
    
    
    private final static String ROOT = "/";

    /**
     * The Mapper used to find and configure the endpoint.
     */
    private ContextRootMapper mapper;
    
    protected volatile ProtocolFilter httpProtocolFilterWrapper;
    
    private AtomicBoolean algorithInitialized = new AtomicBoolean(false);
    
    protected volatile Collection<ProtocolFilter> defaultHttpFilters;
            
    protected boolean isHttpSecured = false;
    // ---------------------------------------------------------------------/.

    public GrizzlyEmbeddedHttp() {
        this(null);
    }
    
    public GrizzlyEmbeddedHttp(Controller controller) {
        this.controller = controller;
        this.mapper = new ContextRootMapper(this);  
    }
    /**
     * Load using reflection the <code>Algorithm</code> class.
     */
    @Override
    protected void initAlgorithm(){
        
        if (!algorithInitialized.getAndSet(true)) {
            algorithmClass = StaticStreamAlgorithm.class;
            defaultAlgorithmInstalled = true;
        }
    }

    /**
     * Initialize the Grizzly Framework classes.
     */
    @Override
    protected void initController() {
        super.initController();
        // Re-start problem when set to true as of 04/18.
        //selectorHandler.setReuseAddress(false);
        DefaultProtocolChainInstanceHandler instanceHandler = new DefaultProtocolChainInstanceHandler() {

            private final ConcurrentLinkedQueue<ProtocolChain> chains = 
                    new ConcurrentLinkedQueue<ProtocolChain>();

            /**
             * Always return instance of ProtocolChain.
             */
            @Override
            public ProtocolChain poll() {
                ProtocolChain protocolChain = chains.poll();
                if (protocolChain == null) {
                    protocolChain = new GlassfishProtocolChain();
                    configureFilters(protocolChain);
                }
                return protocolChain;
            }

            /**
             * Pool an instance of ProtocolChain.
             */
            @Override
            public boolean offer(ProtocolChain instance) {
                return chains.offer(instance);
            }
            };
        controller.setProtocolChainInstanceHandler(instanceHandler);

        controller.setReadThreadsCount(readThreadsCount);
        // TODO: Do we want to support UDP all the time?
        controller.addSelectorHandler(createUDPSelectorHandler());
    }
 
    
    /**
     * Adds and configures <code>ProtocolChain</code>'s filters
     * @param <code>ProtocolChain</code> to configure
     */
    @Override
    protected void configureFilters(ProtocolChain protocolChain) {
        if (portUnificationFilter != null) {
            protocolChain.addFilter(portUnificationFilter);
            // ProtocolFilter are added on the fly by their respective
            // ProtocolHandler, so here we just add a single ProtocolFilter.
            return;
        } else {
            ProtocolFilter readFilter = createReadFilter();
            protocolChain.addFilter(readFilter);
        }
        
        if (rcmSupport) {
            protocolChain.addFilter(createRaFilter());
        }
        
        protocolChain.addFilter(getHttpProtocolFilter());
    }
    
    
    protected Collection<ProtocolFilter> getDefaultHttpProtocolFilters() {
        if (defaultHttpFilters == null) {
            synchronized(this) {
                if (defaultHttpFilters == null) {
                    Collection<ProtocolFilter> tmpList = new ArrayList<ProtocolFilter>(4);
                    if (rcmSupport) {
                        tmpList.add(createRaFilter());
                    }

                    tmpList.add(createHttpParserFilter());
                    defaultHttpFilters = tmpList;
                }
            }
        }
        
        return defaultHttpFilters;
    }
    
    /**
     * Return a <code>ProcessorTask</code> from the pool. If the pool is empty,
     * create a new instance.
     */
    @Override
    public ProcessorTask getProcessorTask() {
        if (asyncExecution) {        
            HttpWorkerThread httpWorkerThread = 
                    (HttpWorkerThread)Thread.currentThread();
            DefaultProcessorTask contextPt = 
                    (DefaultProcessorTask) httpWorkerThread.getProcessorTask();

            if (contextPt == null){
                return super.getProcessorTask();
            } else {
                DefaultProcessorTask pt = 
                        (DefaultProcessorTask)super.getProcessorTask();
                // With Async, we cannot re-use the Context ProcessorTask. Since
                // The adapter has been set on it, rebind it to the current one.
                if (contextPt != null) {
                    pt.setAdapter(contextPt.getAdapter());
                }
                return pt;  
            }
        } else {
            return super.getProcessorTask();
        }

    }
    
    
    /**
     * Create ReadFilter
     * @return read filter
     */
    protected ProtocolFilter createReadFilter() {
        ReadFilter readFilter = new ReadFilter();
        readFilter.setContinuousExecution(true);
        return readFilter;
    }
    

    /**
     * Create the HttpProtocolFilter, 
     * which is aware of EmbeddedHttp's context-root<->adapter map.
     */
    protected HttpProtocolFilter getHttpProtocolFilter() {
        if (httpProtocolFilterWrapper == null) {
            synchronized (this) {
                if (httpProtocolFilterWrapper == null) {
                    initAlgorithm();
                    ProtocolFilter wrappedFilter = createHttpParserFilter();
                    
                    httpProtocolFilterWrapper = new HttpProtocolFilter(wrappedFilter, this);
                }
            }
        }

        return (HttpProtocolFilter) httpProtocolFilterWrapper;
    }
    
    
    /**
     * Create <code>TCPSelectorHandler</code>
     */
    protected UDPSelectorHandler createUDPSelectorHandler() {
        UDPSelectorHandler udpSelectorHandler = new UDPSelectorHandler();
        udpSelectorHandler.setPort(port);
        udpSelectorHandler.setPipeline(processorPipeline);
        return udpSelectorHandler;
    }

    
    /**
     * Configure <code>TCPSelectorHandler</code>
     */
    protected void configureSelectorHandler(UDPSelectorHandler selectorHandler) {
        selectorHandler.setPort(port);
        selectorHandler.setReuseAddress(getReuseAddress());
        selectorHandler.setPipeline(processorPipeline);
    }
    // ---------------------------------------------- Public get/set ----- //

    public boolean isHttpSecured() {
        return isHttpSecured;
    }


    /**
     * Gets context-root mapper
     * 
     * @return context-root mapper
     */
    public ContextRootMapper getContextRootMapper() {
        return mapper;
    }

    /*
     * Registers a new endpoint (adapter implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the adapter instance passed in.
     * @param contextRoot for the adapter
     * @param adapter servicing requests.
     */
    public void registerEndpoint(String contextRoot, Collection<String> vs, Adapter adapter,
                                 ApplicationContainer container) {
        mapper.register(ensureStartsWithSlash(contextRoot), adapter, null, null);
    }

    
    /**
     * Removes the context-root from our list of adapters.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) {
        mapper.unregister(ensureStartsWithSlash(contextRoot));
    }
    
    private String ensureStartsWithSlash(String path) {
        if (!path.startsWith(ROOT)) {
            return ROOT + path;
        }
        
        return path;
    }
}
