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
import com.sun.grizzly.SSLConfig;
import com.sun.grizzly.arp.AsyncProtocolFilter;
import com.sun.grizzly.filter.ReadFilter;
import com.sun.grizzly.http.DefaultProcessorTask;
import com.sun.grizzly.http.DefaultProtocolFilter;
import com.sun.grizzly.http.HttpWorkerThread;
import com.sun.grizzly.http.ProcessorTask;
import com.sun.grizzly.http.SecureSelector;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.util.net.SSLImplementation;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.net.ssl.SSLContext;

/**
 * <p>The GrizzlyServiceMapper is responsible of mapping incoming requests
 * to the proper Container or Grizzly extensions. Registered Containers can be
 * notified by Grizzly using three mode:</p>
 * <ul><li>At the transport level: Containers can be notified when TCP, TLS or UDP
 *                                 requests are mapped to them.</li>
 * <li>At the protocol level: Containers can be notified when protocols
 *                            (ex: SIP, HTTP) requests are mapped to them.</li>
 * </li>At the requests level: Containers can be notified when specific patterns
 *                             requests are mapped to them.</li><ul>
 *
 * @author Jeanfrancois Arcand
 */
public class GrizzlyServiceListener extends SelectorThread implements SecureSelector<SSLImplementation> {

    /**
     * The <code>SSLImplementation</code>
     */
    private SSLImplementation sslImplementation;
    /**
     * The <code>SSLContext</code> associated with the SSL implementation
     * we are running on.
     */
    protected SSLContext sslContext;
    /**
     * The list of cipher suite
     */
    private String[] enabledCipherSuites = null;
    /**
     * the list of protocols
     */
    private String[] enabledProtocols = null;
    /**
     * Client mode when handshaking.
     */
    private boolean clientMode = false;
    /**
     * Require client Authentication.
     */
    private boolean needClientAuth = false;
    /**
     * True when requesting authentication.
     */
    private boolean wantClientAuth = false;
    
    
    private ProtocolFilter httpProtcolFilter;
    
    private boolean algorithInitialized = false;
            
    // ---------------------------------------------------------------------/.

    public GrizzlyServiceListener() {
    }
    
    public GrizzlyServiceListener(Controller controller) {
        this.controller = controller;
    }
    /**
     * Load using reflection the <code>Algorithm</code> class.
     */
    @Override
    protected void initAlgorithm(){
        if (algorithInitialized) return;
        algorithInitialized = true;
        super.initAlgorithm();
    }
    
    
    /**
     * Initialize the Grizzly Framework classes.
     */
    @Override
    protected void initController() {
        super.initController();
        if (portUnificationFilter != null){
            DefaultProtocolChainInstanceHandler instanceHandler = new DefaultProtocolChainInstanceHandler() {

                private final ConcurrentLinkedQueue<ProtocolChain> 
                        chains = new ConcurrentLinkedQueue<ProtocolChain>();

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
        }

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
            ReadFilter readFilter = new ReadFilter();
            readFilter.setContinuousExecution(true);
            protocolChain.addFilter(readFilter);
        }
        
        if (rcmSupport){
            protocolChain.addFilter(createRaFilter());
        }
        if (httpProtcolFilter == null){
            httpProtcolFilter = createHttpParserFilter();
        }
        protocolChain.addFilter(httpProtcolFilter);
    }
    
    
    /**
     * Return a <code>ProcessorTask</code> from the pool. If the pool is empty,
     * create a new instance.
     */
    @Override
    public ProcessorTask getProcessorTask(){
        if (asyncExecution){        
            HttpWorkerThread httpWorkerThread = 
                    (HttpWorkerThread)Thread.currentThread();
            DefaultProcessorTask contextPt = 
                    (DefaultProcessorTask)httpWorkerThread.getProcessorTask();

            if (contextPt == null){
                return super.getProcessorTask();
            } else {
                DefaultProcessorTask pt = 
                        (DefaultProcessorTask)super.getProcessorTask();
                // With Async, we cannot re-use the Context ProcessorTask. Since
                // The adapter has been set on it, rebind it to the current one.
                if (contextPt != null){
                    pt.setAdapter(contextPt.getAdapter());
                }
                return pt;  
            }
        } else {
            return super.getProcessorTask();
        }

    }
    
    
    /**
     * Create the HttpProtocolFilter used to map request to their Adapter
     * at runtime.
     */
    public HttpProtocolFilter createHttpProtocolFilter(){
        initAlgorithm();
        ProtocolFilter wrappedFilter;
        if (asyncExecution){
            wrappedFilter = new AsyncProtocolFilter(algorithmClass,port);
        } else {
           wrappedFilter = new DefaultProtocolFilter(algorithmClass, port);
        }
        httpProtcolFilter = new HttpProtocolFilter(wrappedFilter,this);    
        return (HttpProtocolFilter)httpProtcolFilter;
    }
    
    
    public HttpProtocolFilter getHttpProtocolFilter(){
        return (HttpProtocolFilter)httpProtcolFilter; 
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

    
    /**
     * Set the SSLContext required to support SSL over NIO.
     */
    public void setSSLConfig(SSLConfig sslConfig) {
        this.sslContext = sslConfig.createSSLContext();
    }

    /**
     * Set the SSLContext required to support SSL over NIO.
     */
    public void setSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    /**
     * Return the SSLContext required to support SSL over NIO.
     */
    public SSLContext getSSLContext() {
        return sslContext;
    }

    /**
     * Set the Coyote SSLImplementation.
     */
    public void setSSLImplementation(SSLImplementation sslImplementation) {
        this.sslImplementation = sslImplementation;
    }

    /**
     * Return the current <code>SSLImplementation</code> this Thread
     */
    public SSLImplementation getSSLImplementation() {
        return sslImplementation;
    }

    /**
     * Returns the list of cipher suites to be enabled when {@link SSLEngine}
     * is initialized.
     *
     * @return <tt>null</tt> means 'use {@link SSLEngine}'s default.'
     */
    public String[] getEnabledCipherSuites() {
        return enabledCipherSuites;
    }

    /**
     * Sets the list of cipher suites to be enabled when {@link SSLEngine}
     * is initialized.
     *
     * @param cipherSuites <tt>null</tt> means 'use {@link SSLEngine}'s default.'
     */
    public void setEnabledCipherSuites(String[] enabledCipherSuites) {
        this.enabledCipherSuites = enabledCipherSuites;
    }

    /**
     * Returns the list of protocols to be enabled when {@link SSLEngine}
     * is initialized.
     *
     * @return <tt>null</tt> means 'use {@link SSLEngine}'s default.'
     */
    public String[] getEnabledProtocols() {
        return enabledProtocols;
    }

    /**
     * Sets the list of protocols to be enabled when {@link SSLEngine}
     * is initialized.
     *
     * @param enabledProtocols <tt>null</tt> means 'use {@link SSLEngine}'s default.'
     */
    public void setEnabledProtocols(String[] enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    /**
     * Returns <tt>true</tt> if the SSlEngine is set to use client mode
     * when handshaking.
     * @return is client mode enabled
     */
    public boolean isClientMode() {
        return clientMode;
    }

    /**
     * Configures the engine to use client (or server) mode when handshaking.
     */
    public void setClientMode(boolean clientMode) {
        this.clientMode = clientMode;
    }

    /**
     * Returns <tt>true</tt> if the SSLEngine will <em>require</em>
     * client authentication.
     */
    public boolean isNeedClientAuth() {
        return needClientAuth;
    }

    /**
     * Configures the engine to <em>require</em> client authentication.
     */
    public void setNeedClientAuth(boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }

    /**
     * Returns <tt>true</tt> if the engine will <em>request</em> client
     * authentication.
     */
    public boolean isWantClientAuth() {
        return wantClientAuth;
    }

    /**
     * Configures the engine to <em>request</em> client authentication.
     */
    public void setWantClientAuth(boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
    }
}