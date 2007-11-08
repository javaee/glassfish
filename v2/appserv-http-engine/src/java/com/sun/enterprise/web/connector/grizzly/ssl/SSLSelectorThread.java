/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.web.connector.grizzly.ssl;

import com.sun.enterprise.web.connector.grizzly.Pipeline;
import com.sun.enterprise.web.connector.grizzly.ReadTask;
import com.sun.enterprise.web.connector.grizzly.DefaultReadTask;
import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.SecureSelector;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.StreamAlgorithm;
import com.sun.enterprise.web.connector.grizzly.algorithms.NoParsingAlgorithm;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.tomcat.util.net.ServerSocketFactory;

/**
 * SSL over NIO <code>Selector</code> implementation. Mainly, this class
 * replace the clear text implementation by defining the SSL tasks counterpart:
 * SSLReadTask, SSLProcessorTask and SSLByteBufferInputStream.
 *
 * The SSLPipeline is the default and must not be replace unless all its
 * attribute properly implemented.
 *
 * @author Jean-Francois Arcand
 */
public class SSLSelectorThread extends SelectorThread 
        implements SecureSelector<SSLImplementation>{
    
    
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
     * Session keep-alive flag.
     */
    private final static String EXPIRE_TIME = "expireTime";

    
    
    /**
     * Has the enabled protocol configured.
     */
    private boolean isProtocolConfigured = false;
    
    
    /**
     * Has the enabled Cipher configured.
     */
    private boolean isCipherConfigured = false;
    
    // ---------------------------------------------------------------------/.
    
    
    public SSLSelectorThread(){
        super();
        setPipelineClassName(com.sun.enterprise.web.connector.grizzly.ssl.
                SSLPipeline.class.getName());
    }

    
    /**
     * Initialize <code>SSLSelectorReadThread</code> used to process
     * OP_READ operations.
     */
    protected void initMultiSelectors() throws IOException, 
                                                 InstantiationException {
        for (int i = 0; i < readThreads.length; i++) {
            readThreads[i] = new SSLSelectorReadThread(){
                public ReadTask getReadTask(SelectionKey key) 
                    throws IOException{
                    
                    return SSLSelectorThread.this.getReadTask(key);
                }
            };
            ((SSLSelectorReadThread)readThreads[i]).countName = i;
            configureReadThread((SSLSelectorReadThread)readThreads[i]);
        }
    }
 
    
    /**
     * Enable all registered interestOps. Due a a NIO bug, all interestOps
     * invokation needs to occurs on the same thread as the selector thread.
     */
    public void enableSelectionKeys(){
        SelectionKey selectionKey;
        int size = getKeysToEnable().size();
        long currentTime = System.currentTimeMillis();
        for (int i=0; i < size; i++) {
            selectionKey = (SelectionKey)getKeysToEnable().poll();

            selectionKey.interestOps(
                    selectionKey.interestOps() | SelectionKey.OP_READ);

            if ( selectionKey.attachment() != null){
                ((SSLEngine)selectionKey
                        .attachment()).getSession().putValue
                            (EXPIRE_TIME, System.currentTimeMillis());
            }
            keepAlivePipeline.trap(selectionKey);   
        } 
    } 
    
    
    /**
     * Handle OP_READ
     */ 
    protected ReadTask handleRead(SelectionKey key) throws IOException{                   
        // disable OP_READ on key before doing anything else 
        key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
        
        if (enableNioLogging){
            logger.log(Level.INFO,"Handling OP_READ on SocketChannel " + 
                    key.channel());
        }      
        
        return getReadTask(key);
    }    
    
    
    /**
     * Cancel keep-alive connections.
     */
    protected void expireIdleKeys() {
        if (keepAliveTimeoutInSeconds <= 0 || !selector.isOpen()) return;
        long current = System.currentTimeMillis();

        if (current < getNextKeysExpiration()) {
            return;
        }
        setNextKeysExpiration(current + getKaTimeout());
        
        Set<SelectionKey> readyKeys = selector.keys();
        if (readyKeys.isEmpty()){
            return;
        }
        Iterator<SelectionKey> iterator = readyKeys.iterator();
        SelectionKey key;
        while (iterator.hasNext()) {
            key = (SelectionKey)iterator.next();
            if ( !key.isValid() ) {
                keepAlivePipeline.untrap(key);
                continue;
            }  
                        
            // Keep-alive expired
            if (key.attachment() != null) {                
                SSLSession sslSession = null;
                Object attachment = key.attachment(); 
                long expire = -1L;
                if (attachment instanceof SSLEngine){
                    sslSession = ((SSLEngine)key.attachment()).getSession();
                    if (sslSession != null 
                            && sslSession.getValue(EXPIRE_TIME) != null){
                        expire = (Long)sslSession.getValue(EXPIRE_TIME); 
                    }
                } else if (attachment instanceof Long){
                    expire = (Long)attachment;
                }
                
                if (expire != -1L){
                    if (current - expire >= getKaTimeout()) {                   
                        cancelKey(key);
                    } else if (expire + getKaTimeout() < getNextKeysExpiration()){
                        setNextKeysExpiration(expire + getKaTimeout());
                    }
                }
            }
        }                    
    }
    
    
    /**
     * Register a <code>SelectionKey</code> to this <code>Selector</code>
     * running of this thread.
     */
    public void registerKey(SelectionKey key){
        if (key == null) return;
        
        if (keepAlivePipeline.dropConnection()) {
            cancelKey(key);
            return;
        }

        if (enableNioLogging){
            logger.log(Level.INFO,
                    "Registering SocketChannel for keep alive " +  
                    key.channel());
        }         
        getKeysToEnable().add(key);
        selector.wakeup();
    } 
    
    /**
     * Create a new <code>Pipeline</code> instance using the 
     * <code>pipelineClassName</code> value.
     */
    protected Pipeline newPipeline(int maxThreads,
                                   int minThreads,
                                   String name, 
                                   int port,
                                   int priority){
        
        Class className = null;                               
        Pipeline pipeline = null;                               
        try{                              
            className = Class.forName(getPipelineClassName());
            pipeline = (Pipeline)className.newInstance();
        } catch (ClassNotFoundException ex){
            getLogger().log(Level.WARNING,
                       "Unable to load Pipeline: " + getPipelineClassName());
            pipeline = new SSLPipeline();
        } catch (InstantiationException ex){
            getLogger().log(Level.WARNING,
                       "Unable to instantiate Pipeline: "
                       + getPipelineClassName());
            pipeline = new SSLPipeline();
        } catch (IllegalAccessException ex){
            getLogger().log(Level.WARNING,
                       "Unable to instantiate Pipeline: "
                       + getPipelineClassName());
            pipeline = new SSLPipeline();
        }
        
        if (getLogger().isLoggable(Level.FINE)){
            getLogger().log(Level.FINE,
                       "http-listener " + port + " uses pipeline: "
                       + pipeline.getClass().getName());
        }
        
        pipeline.setMaxThreads(maxThreads);
        pipeline.setMinThreads(minThreads);    
        pipeline.setName(name);
        pipeline.setPort(port);
        pipeline.setPriority(priority);
        pipeline.setQueueSizeInBytes(getMaxQueueSizeInBytes());
        pipeline.setThreadsIncrement(getThreadsIncrement());
        pipeline.setThreadsTimeout(getThreadsTimeout());
        
        return pipeline;
    
    }

    
    /**
     * Return a <code>SSLReadTask</code> from the pool. If the pool is empty,
     * create a new instance. Make sure the SSLEngine is reused when the 
     * SelectionKey is part of a keep-alive transaction.
     */
    public ReadTask getReadTask(SelectionKey key) throws IOException{
        ReadTask task = super.getReadTask(key);
        
        SSLEngine sslEngine = null;
        Object attachment = key.attachment();
        if (attachment != null && attachment instanceof SSLEngine){
            sslEngine = (SSLEngine)attachment;
        } else {
            key.attach(null);
        }
        
        if (sslEngine != null) {
            ((SSLReadTask)task).setHandshake(false);
        } else {
            sslEngine = sslContext.createSSLEngine(); 
            if (enabledCipherSuites != null){                
                if (!isCipherConfigured) {
                    enabledCipherSuites = configureEnabledCiphers(sslEngine,
                            enabledCipherSuites);
                    isCipherConfigured = true;
                }                
                sslEngine.setEnabledCipherSuites(enabledCipherSuites);
            } 

            if (enabledProtocols != null){                
                if (!isProtocolConfigured) {
                    enabledProtocols = configureEnabledProtocols(sslEngine,
                                                                 enabledProtocols);
                    isProtocolConfigured = true;
                }                
                sslEngine.setEnabledProtocols(enabledProtocols);
            }       
            sslEngine.setUseClientMode(isClientMode()); 
        }
        sslEngine.setWantClientAuth(isWantClientAuth());     
        sslEngine.getSession().removeValue(EXPIRE_TIME);
        sslEngine.setNeedClientAuth(isNeedClientAuth());
        ((SSLReadTask)task).setSSLEngine(sslEngine);           
        return task;
    }           
    
    
    /**
     * Return a new <code>SSLReadTask</code> instance
     */
    protected DefaultReadTask newReadTask(){
        StreamAlgorithm streamAlgorithm = new NoParsingAlgorithm();    
        streamAlgorithm.setPort(getPort());
               
        SSLReadTask task;
        if (getMaxReadWorkerThreads() > 0 || asyncExecution){
            task =  new SSLAsyncReadTask();  
        } else {
            task = new SSLReadTask();
        }
                     
        task.initialize(streamAlgorithm, isUseDirectByteBuffer(), isUseByteBufferView());
        task.setPipeline(getReadPipeline());  
        task.setSelectorThread(this);
        task.setRecycle(isRecycleTasks());
        task.setSSLImplementation(sslImplementation);
        
        return task;
    }
    
    
    /**
     * Create <code>SSLProcessorTask</code> objects and configure it to be ready
     * to proceed request.
     */
    protected ProcessorTask newProcessorTask(boolean initialize){                                                      
        SSLProcessorTask task = null;
        if (!asyncExecution) {
            task = new SSLProcessorTask(initialize, isBufferResponse());
        } else {
            task = new SSLAsyncProcessorTask(initialize, isBufferResponse());
        }      
        return configureProcessorTask(task);        
    }
    
    
    /**
     * Set the SSLContext required to support SSL over NIO.
     */
    public void setSSLContext(SSLContext sslContext){
        this.sslContext = sslContext;
    }
 
    
    /**
     * Return the SSLContext required to support SSL over NIO.
     */    
    public SSLContext getSSLContext(){
        return sslContext;
    }
    
    
    /**
     * Set the Coyote SSLImplementation.
     */
    public void setSSLImplementation(SSLImplementation sslImplementation){
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
     * @param protocols <tt>null</tt> means 'use {@link SSLEngine}'s default.'
     */    
    public void setEnabledProtocols(String[] enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    
    /**
     * Returns <tt>true</tt> if the SSlEngine is set to use client mode
     * when handshaking.
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
     * Return the list of allowed protocol.
     * @return String[] an array of supported protocols.
     */
    private final static String[] configureEnabledProtocols(
            SSLEngine sslEngine, String[] requestedProtocols){
        
        String[] supportedProtocols = sslEngine.getSupportedProtocols();
        String[] protocols = null;
        ArrayList<String> list = null;
        for(String supportedProtocol: supportedProtocols){        
            /*
             * Check to see if the requested protocol is among the
             * supported protocols, i.e., may be enabled
             */
            for(String protocol: requestedProtocols) {
                protocol = protocol.trim();
                if (supportedProtocol.equals(protocol)) {
                    if (list == null) {
                        list = new ArrayList<String>();
                    }
                    list.add(protocol);
                    break;
                }
            }
        } 

        if (list != null) {
            protocols = list.toArray(new String[list.size()]);                
        }
 
        return protocols;
    }
    
    
    /*
     * Determines the SSL cipher suites to be enabled.
     *
     * @return Array of SSL cipher suites to be enabled, or null if none of the
     * requested ciphers are supported
     */
    private final static String[] configureEnabledCiphers(SSLEngine sslEngine,
            String[] requestedCiphers) {

        String[] supportedCiphers = sslEngine.getSupportedCipherSuites();
        String[] ciphers = null;
        ArrayList<String> list = null;
        for(String supportedCipher: supportedCiphers){        
            /*
             * Check to see if the requested protocol is among the
             * supported protocols, i.e., may be enabled
             */
            for(String cipher: requestedCiphers) {
                cipher = cipher.trim();
                if (supportedCipher.equals(cipher)) {
                    if (list == null) {
                        list = new ArrayList<String>();
                    }
                    list.add(cipher);
                    break;
                }
            }
        } 

        if (list != null) {
            ciphers = list.toArray(new String[list.size()]);                
        }
 
        return ciphers;
    }
 
    
    /**
     * Initialize the fileCacheFactory associated with this instance
     */
    protected void initFileCacheFactory(){
        fileCacheFactory = SSLFileCacheFactory.getFactory(port);
        fileCacheFactory.setIsEnabled(isFileCacheEnabled);
        fileCacheFactory.setLargeFileCacheEnabled(isLargeFileCacheEnabled);
        fileCacheFactory.setSecondsMaxAge(secondsMaxAge);
        fileCacheFactory.setMaxCacheEntries(maxCacheEntries);
        fileCacheFactory.setMinEntrySize(minEntrySize);
        fileCacheFactory.setMaxEntrySize(maxEntrySize);
        fileCacheFactory.setMaxLargeCacheSize(maxLargeFileCacheSize);
        fileCacheFactory.setMaxSmallCacheSize(maxSmallFileCacheSize);         
        fileCacheFactory.setIsMonitoringEnabled(isMonitoringEnabled);
    }
    
    // --------------------------------------------------- Not used/
    
    /**
     * Return the <code>ServerSocketFactory</code> used when a blocking IO
     * is enabled.
     */
    public ServerSocketFactory getServerSocketFactory(){
       return null;
    }

    
    /**
     * Set the <code>ServerSocketFactory</code> used when a blocking IO
     * is enabled.
     */   
    public void setServerSocketFactory(ServerSocketFactory factory){
       ;
    }
    
    
    /**
     * Enable gathering of monitoring datas.
     */
    public void enableMonitoring(){
        isMonitoringEnabled = true;
        enablePipelineStats();      
        if (readThreads != null) {
            for (int i = 0; i < readThreads.length; i++) {
                ((SSLSelectorReadThread)readThreads[i]).isMonitoringEnabled = true;
            }
        }
        fileCacheFactory.setIsMonitoringEnabled(isMonitoringEnabled);
    }
    
    
    /**
     * Disable gathering of monitoring datas. 
     */
    public void disableMonitoring(){
        disablePipelineStats();  
        if (readThreads != null) {
            for (int i = 0; i < readThreads.length; i++) {
                ((SSLSelectorReadThread)readThreads[i]).isMonitoringEnabled = false;
            }
        }
        fileCacheFactory.setIsMonitoringEnabled(isMonitoringEnabled);        
    }

    
}
