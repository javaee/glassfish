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

import com.sun.grizzly.ProtocolChain;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.SSLConfig;
import com.sun.grizzly.TCPSelectorHandler;
import com.sun.grizzly.filter.SSLReadFilter;
import com.sun.grizzly.http.ProcessorTask;
import com.sun.grizzly.ssl.SSLAsyncProcessorTask;
import com.sun.grizzly.ssl.SSLAsyncProtocolFilter;
import com.sun.grizzly.ssl.SSLProcessorTask;
import com.sun.grizzly.ssl.SSLDefaultProtocolFilter;
import com.sun.grizzly.ssl.SSLSelectorThreadHandler;
import com.sun.grizzly.util.net.SSLImplementation;
import com.sun.grizzly.util.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;

/**
 * Implementation of Grizzly embedded HTTPS listener
 *
 * @author Jeanfrancois Arcand
 * @author Alexey Stashok
 */
public class GrizzlyEmbeddedHttps extends GrizzlyEmbeddedHttp {

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
    
    /**
     * Cert nick name
     */
    private String certNickname = null;
    

    // ---------------------------------------------------------------------/.

    /**
     * Constructor
     */    
    public GrizzlyEmbeddedHttps(GrizzlyService grizzlyService) {
        super(grizzlyService);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected TCPSelectorHandler createSelectorHandler() {
        return new SSLSelectorThreadHandler(this);
    }

    /**
     * Create HTTP parser <code>ProtocolFilter</code>
     * @return HTTP parser <code>ProtocolFilter</code>
     */
    @Override
    protected ProtocolFilter createHttpParserFilter() {
        if (asyncExecution){
            return new SSLAsyncProtocolFilter(algorithmClass, port, sslImplementation);
        } else {
            return new SSLDefaultProtocolFilter(algorithmClass, port, sslImplementation);
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureFilters(ProtocolChain protocolChain) {
        if (portUnificationFilter != null) {
            portUnificationFilter.setContinuousExecution(false);
            protocolChain.addFilter(portUnificationFilter);
        }
        
        protocolChain.addFilter(createReadFilter());
        protocolChain.addFilter(createHttpParserFilter());
    }
    
    
    /**
     * Create and configure <code>SSLReadFilter</code>
     * @return <code>SSLReadFilter</code>
     */
    @Override
    protected ProtocolFilter createReadFilter() {
        SSLReadFilter readFilter = new SSLReadFilter();
        readFilter.setSSLContext(sslContext);
        readFilter.setClientMode(clientMode);
        readFilter.setEnabledCipherSuites(enabledCipherSuites);
        readFilter.setEnabledProtocols(enabledProtocols);
        readFilter.setNeedClientAuth(needClientAuth);
        readFilter.setWantClientAuth(wantClientAuth);
        return readFilter;
    }


    /**
     * Create <code>SSLProcessorTask</code> objects and configure it to be ready
     * to proceed request.
     */
    @Override
    protected ProcessorTask newProcessorTask(boolean initialize){                                                      
        SSLProcessorTask task = null;
        if (!asyncExecution) {
            task = new SSLProcessorTask(initialize, getBufferResponse());
        } else {
            task = new SSLAsyncProcessorTask(initialize, getBufferResponse());
        }      
        return configureProcessorTask(task);        
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

    /**
     * Initializes SSL
     * @throws java.lang.Exception
     */
    public void initializeSSL() throws Exception {
        SSLImplementation sslHelper = SSLImplementation.getInstance();
        ServerSocketFactory serverSF = sslHelper.getServerSocketFactory();

        serverSF.setAttribute("keystoreType", "JKS");
        serverSF.setAttribute("keystore",
                System.getProperty("javax.net.ssl.keyStore"));

        serverSF.setAttribute("truststoreType", "JKS");
        serverSF.setAttribute("truststore",
                System.getProperty("javax.net.ssl.trustStore"));

        if (certNickname != null) {
            serverSF.setAttribute("keyAlias", certNickname);
        }
        
        serverSF.init();

        this.sslImplementation = sslHelper;
        this.sslContext = serverSF.getSSLContext();
        this.isHttpSecured = true;
    }

    public String getCertNickname() {
        return certNickname;
    }

    public void setCertNickname(String certNickname) {
        this.certNickname = certNickname;
    }
}
