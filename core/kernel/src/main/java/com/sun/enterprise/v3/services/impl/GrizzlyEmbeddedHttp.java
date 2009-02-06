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

import com.sun.grizzly.DefaultProtocolChainInstanceHandler;
import com.sun.grizzly.ProtocolChain;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.UDPSelectorHandler;
import com.sun.grizzly.filter.ReadFilter;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.tcp.Adapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.glassfish.api.deployment.ApplicationContainer;

/**
 * Implementation of Grizzly embedded HTTP listener
 * 
 * @author Jeanfrancois Arcand
 * @author Alexey Stashok
 */
public class GrizzlyEmbeddedHttp extends SelectorThread 
        implements EndpointMapper<Adapter>  {
    
    
    private final static String ROOT = "/";
    
    protected volatile ProtocolFilter httpProtocolFilterWrapper;
    
    private AtomicBoolean algorithInitialized = new AtomicBoolean(false);
    
    protected volatile Collection<ProtocolFilter> defaultHttpFilters;
            
    protected boolean isHttpSecured = false;
    
    private UDPSelectorHandler udpSelectorHandler;

    protected GrizzlyService grizzlyService;


    // ---------------------------------------------------------------------/.

    /**
     * Constructor
     */    
    public GrizzlyEmbeddedHttp(GrizzlyService grizzlyService) {
        this.grizzlyService = grizzlyService;
        this.adapter = new ContainerMapper(grizzlyService, this);
        setClassLoader(getClass().getClassLoader());
    }
    
    /**
     * Load using reflection the <code>Algorithm</code> class.
     */
    @Override
    protected void initAlgorithm(){        
        if (!algorithInitialized.getAndSet(true)) {
            algorithmClass = ContainerStaticStreamAlgorithm.class;
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
                    ((GlassfishProtocolChain)protocolChain).enableRCM(rcmSupport);
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
        
        // Suport UDP only when port unification is enabled.
        if (portUnificationFilter != null) {
            controller.addSelectorHandler(createUDPSelectorHandler());
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                stopEndpoint();
            }
        });
    }
 
    
    @Override
    public void stopEndpoint(){
        try{
            super.stopEndpoint();
        } catch (Throwable t){
            logger.log(Level.SEVERE,"Unable to stop properly",t);
        } finally {
            // Force the Selector(s) to be closed in case an unexpected 
            // exception occured during shutdown.
            try{
                if (selectorHandler != null 
                        && selectorHandler.getSelector() != null){
                    selectorHandler.getSelector().close();
                }
            } catch (IOException ex){}
            
            try{
                if (udpSelectorHandler != null 
                        && udpSelectorHandler.getSelector() != null){
                    udpSelectorHandler.getSelector().close();
                }
            } catch (IOException ex){}
        }
        
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
     * Create ReadFilter
     * @return read filter
     */
    protected ProtocolFilter createReadFilter() {
        ReadFilter readFilter = new ReadFilter();
        readFilter.setContinuousExecution(GlassfishProtocolChain.CONTINUOUS_EXECUTION);
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
        if (udpSelectorHandler == null){
            udpSelectorHandler = new UDPSelectorHandler();
            udpSelectorHandler.setPort(port);
            udpSelectorHandler.setThreadPool(threadPool);
        }
        return udpSelectorHandler;
    }

    
    /**
     * Configure <code>TCPSelectorHandler</code>
     */
    protected void configureSelectorHandler(UDPSelectorHandler selectorHandler) {
        selectorHandler.setPort(port);
        selectorHandler.setReuseAddress(getReuseAddress());
        selectorHandler.setThreadPool(threadPool);
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
    public ContainerMapper getContainerMapper() {
        return (ContainerMapper)adapter;
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
        ((ContainerMapper)getAdapter()).register(contextRoot, vs, adapter, null, null);
    }   
 
    
    /**
     * Removes the context-root from our list of adapters.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) {
        ((ContainerMapper)getAdapter()).unregister(contextRoot);
    }
}
