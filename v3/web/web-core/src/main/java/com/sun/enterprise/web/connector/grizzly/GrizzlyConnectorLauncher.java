/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 1999-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.enterprise.web.connector.grizzly;

import com.sun.grizzly.http.Management;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.ssl.SSLSelectorThread;
import com.sun.grizzly.tcp.http11.Constants;
import com.sun.grizzly.util.net.SSLImplementation;
import com.sun.grizzly.util.net.ServerSocketFactory;
import com.sun.logging.LogDomains;
import org.apache.tomcat.util.modeler.Registry;

import javax.management.ObjectName;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract the protocol implementation, including threading, etc.
 * Processor is single threaded and specific to stream-based protocols.
 * This is an adaptation of com.sun.grizzly.tcp.http11.Http11Processor except here
 * NIO is used.
 * 
 * @author Jean-Francois Arcand
 */
public class GrizzlyConnectorLauncher extends CoyoteConnectorLauncher {    

    private static final Logger logger =
        LogDomains.getLogger(GrizzlyConnectorLauncher.class,
            LogDomains.WEB_LOGGER);

    private static final ResourceBundle rb = logger.getResourceBundle();
    
    private int socketBuffer = 9000;
    
    /**
     * The <code>SelectorThread</code> used by this object.
     */
    private SelectorThread selectorThread;
    
    
    /**
     * The JMX management class.
     */
    private Management jmxManagement = null;

    // ------------------------------------------------------ Compression ---/
    
    
    /**
     * Compression value.
     */
    private String compression = "off";
    private String noCompressionUserAgents = null;
    private String restrictedUserAgents = null;
    private String compressableMimeTypes = "text/html,text/xml,text/plain";
    private int compressionMinSize    = 2048;
    
    
    /**
     * List of proxied protocol supported by the listener.
     */
    private String proxiedProtocols;
    
    
    /**
     * The default proxied protocol.
     */
    private final static ConcurrentLinkedQueue<String> supportedHandlers 
            = new ConcurrentLinkedQueue<String>();
    
    
    /**
     * The default proxied protocol.
     */
    private final static ConcurrentHashMap<String,String> supportedProtocols 
            = new ConcurrentHashMap<String,String>();  
    
    
    private final static String TLS = "tls";    
    private final static String HTTP = "http";
    
    static{
        supportedProtocols.put(
                HTTP,com.sun.grizzly.http.portunif.
                HttpProtocolFinder.class.getName());
        supportedProtocols.put(
                "ws/tcp","com.sun.xml.ws.transport.tcp.grizzly.WSTCPProtocolFinder");      
        supportedHandlers.add("com.sun.xml.ws.transport.tcp.grizzly.WSTCPProtocolHandler");        
    }


    // ------------------------------------------------------- Constructor --//
    
    public GrizzlyConnectorLauncher(boolean secure, boolean blocking, 
                               String selectorThreadImpl) {
        super(secure,blocking,selectorThreadImpl);    
        create();
    }   

    protected void create() {         
        if ( !secure ){
            selectorThread = new SelectorThread(); 
         } else { 
            selectorThread = new SSLSelectorThread();
        }
        setSoLinger(-1);
        setSoTimeout(30 * 1000);
        setServerSoTimeout(Constants.DEFAULT_SERVER_SOCKET_TIMEOUT);
        setTcpNoDelay(Constants.DEFAULT_TCP_NO_DELAY);
    }

    
    // ---------------------------------------------------- Public methods --//
    /** 
     * Start the protocol
     */
    @Override
    public void init() throws Exception {
        try {
            checkSocketFactory();
        } catch( Exception ex ) {
            logger.log(Level.SEVERE,
                "grizzlyHttpProtocol.socketfactory.initerror", ex);
            throw ex;
        }

        if( socketFactory!=null ) {
            Enumeration attE=attributes.keys();
            while( attE.hasMoreElements() ) {
                String key=(String)attE.nextElement();
                Object v=attributes.get( key );
                socketFactory.setAttribute( key, v );
            }
        }

        if ( secure && !blocking){
            socketFactory.init();
            ((SSLSelectorThread)selectorThread)
                    .setSSLContext(socketFactory.getSSLContext());
        }
        
        try {
             selectorThread.setAdapter(adapter);
             selectorThread.initEndpoint();

        } catch (Exception ex) {
            logger.log(Level.SEVERE, 
                "grizzlyHttpProtocol.endpoint.initerror", ex);
            throw ex;
        }
    }
    
    
    @Override
    public void start() throws Exception {
        try {            
            if ( this.oname != null ) {
                jmxManagement = new ModelerManagement();
                selectorThread.setManagement(jmxManagement);
                try {
                    ObjectName sname = new ObjectName(domain
                                   +  ":type=Selector,name=http"
                                   + selectorThread.getPort());
                    jmxManagement.registerComponent(selectorThread, sname,
                        null);
                } catch (Exception ex) {
                    String msg = rb.getString(
                        "grizzlyHttpProtocol.selectorRegistrationFailed");
                    msg = MessageFormat.format(msg, oname);
                    logger.log(Level.SEVERE, msg, ex);
                }
            }
            selectorThread.start();
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                "grizzlyHttpProtocol.endpoint.starterror", ex);
            throw ex;
        }
        
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "grizzlyHttpProtocol.start",
                String.valueOf(getPort()));
        }
    }


    @Override
    public void destroy() throws Exception {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "grizzlyHttpProtocol.stop", 
                String.valueOf(getPort()));
        }
        if ( domain != null ){
           jmxManagement.
                    unregisterComponent(new ObjectName(domain,"type", "Selector"));
        }
        selectorThread.stopEndpoint();
    }
    
    
    // -------------------- Pool setup --------------------


    public int getMaxThreads() {
        return selectorThread.getMaxThreads();
    }
    
    public void setMaxThreads( int maxThreads ) {
        selectorThread.setMaxThreads(maxThreads);
        setAttribute("maxThreads", "" + maxThreads);
    }
    
    
    @Override
    public void setMaxPostSize(int maxPostSize) {
        selectorThread.setMaxPostSize(maxPostSize);
        setAttribute("maxPostSize", maxPostSize);
    }    

    
    /*
     * API was changed. Method doesn't exist in SelectorThread 
     */
    public int getProcessorThreadsIncrement() {
//        return selectorThread.getThreadsIncrement();
        return 0;
    }

    
    /*
     * API was changed. Method doesn't exist in SelectorThread
     */
    public void setProcessorThreadsIncrement( int threadsIncrement ) {
//        selectorThread.setThreadsIncrement(threadsIncrement);
        setAttribute("threadsIncrement", "" + threadsIncrement);  
    }  
    
    
    public void setProcessorWorkerThreadsTimeout(int timeout){
//        selectorThread.setThreadsTimeout(timeout);
        setAttribute("threadsTimeout", "" + timeout);     
    }
    
    
    public int getProcessorWorkerThreadsTimeout(){
//        return selectorThread.getThreadsTimeout();
        return 0;
    }
    // -------------------- Tcp setup --------------------

    public int getBacklog() {
        return selectorThread.getSsBackLog();
    }
    
    public void setBacklog( int i ) {
        ;
    }
    
    public int getPort() {
        return selectorThread.getPort();
    }
    
    public void setPort( int port ) {
        selectorThread.setPort(port);
        setAttribute("port", "" + port);
        //this.port=port;
    }

    public InetAddress getAddress() {
        return selectorThread.getAddress();
    }
    
    public void setAddress(InetAddress ia) {
        selectorThread.setAddress( ia );
        setAttribute("address", "" + ia);
    }
    
    @SuppressWarnings("deprecation")
    public String getName() {
        String encodedAddr = "";
        if (getAddress() != null) {
            encodedAddr = "" + getAddress();
            if (encodedAddr.startsWith("/"))
                encodedAddr = encodedAddr.substring(1);
            encodedAddr = URLEncoder.encode(encodedAddr) + "-";
        }
        return ("http-" + encodedAddr + selectorThread.getPort());
    }
    
    public boolean getTcpNoDelay() {
        return selectorThread.getTcpNoDelay();
    }
    
    public void setTcpNoDelay( boolean b ) {
        selectorThread.setTcpNoDelay( b );
        setAttribute("tcpNoDelay", "" + b);
    }

    @Override
    public boolean getDisableUploadTimeout() {
        return disableUploadTimeout;
    }
    
    @Override
    public void setDisableUploadTimeout(boolean isDisabled) {
        //TO DO Implement in Grizzly
        disableUploadTimeout = isDisabled;
    }

    public int getSocketBuffer() {
        return socketBuffer;
    }
    
    public void setSocketBuffer(int valueI) {
        socketBuffer = valueI;
    }

    @Override
    public void setMaxHttpHeaderSize(int maxHttpHeaderSize) {
        super.setMaxHttpHeaderSize(maxHttpHeaderSize);
        selectorThread.setMaxHttpHeaderSize(maxHttpHeaderSize);
    }
   
    @Override
    public String getCompression() {
        return compression;
    }

    @Override
    public void setCompression(String valueS) {
        compression = valueS;
        selectorThread.setCompression(compression);
    }
    
    public String getRestrictedUserAgents() {
        return restrictedUserAgents;
    }
    
    public void setRestrictedUserAgents(String valueS) {
        restrictedUserAgents = valueS;
        selectorThread.setRestrictedUserAgents(valueS);
    }

    public String getNoCompressionUserAgents() {
        return noCompressionUserAgents;
    }
    
    public void setNoCompressionUserAgents(String valueS) {
        noCompressionUserAgents = valueS;
        selectorThread.setNoCompressionUserAgents(valueS);
    }

    public String getCompressableMimeType() {
        return compressableMimeTypes;
    }
    
    public void setCompressableMimeType(String valueS) {
        compressableMimeTypes = valueS;
        selectorThread.setCompressableMimeTypes(valueS);
    }

    public int getCompressionMinSize() {
        return compressionMinSize;
    }
    
    public void setCompressionMinSize(int valueI) {
        compressionMinSize = valueI;
        selectorThread.setCompressionMinSize(valueI);
    }

    public int getSoLinger() {
        return selectorThread.getLinger();
    }
    
    public void setSoLinger( int i ) {
        selectorThread.setLinger( i );
        setAttribute("soLinger", "" + i);
    }

    public int getSoTimeout() {
//        return selectorThread.getSoTimeout();
        return 0;
    }
    
    public void setSoTimeout( int i ) {
//        selectorThread.setSoTimeout(i);
        setAttribute("soTimeout", "" + i);
    }
    
    public int getServerSoTimeout() {
//        return selectorThread.getServerSoTimeout();
        return 0;
    }
    
    public void setServerSoTimeout( int i ) {
//        selectorThread.setServerSoTimeout(i);
        setAttribute("serverSoTimeout", "" + i);
    }
    
    @Override
    public void setSecure( boolean b ) {
        super.setSecure(b);
    }

    /** 
     * Set the maximum number of Keep-Alive requests that we will honor.
     */
    @Override
    public void setMaxKeepAliveRequests(int mkar) {
        selectorThread.setMaxKeepAliveRequests(mkar);
    }   
   

    /** 
     * Sets the number of seconds before a keep-alive connection that has
     * been idle times out and is closed.
     *
     * @param timeout Keep-alive timeout in number of seconds
     */    
    public void setKeepAliveTimeoutInSeconds(int timeout) {
        selectorThread.setKeepAliveTimeoutInSeconds(timeout);
    }


    /** 
     * Sets the number of keep-alive threads.
     *
     * @param threadCount Number of keep-alive threads
     */    
    public void setKeepAliveThreadCount(int threadCount) {
        selectorThread.setMaxKeepAliveRequests(threadCount);
    }


    /**
     * The minimun threads created at startup.
     */
    /*
     * API was changed. Method doesn't exist in SelectorThread
     */
    public void setMinThreads(int minThreads){
//        selectorThread.setMinThreads(minThreads);
    }


    /**
     * Set the request input buffer size
     */
    @Override
    public void setBufferSize(int requestBufferSize){
        super.setBufferSize(requestBufferSize);
        selectorThread.setBufferSize(requestBufferSize);
    }


    /**
     * Set the <code>Selector</code> times out value.
     */
    public void setSelectorTimeout(int selectorTimeout){
        selectorThread.setSelectorTimeout(selectorTimeout);
    }


    /**
     * Return the <code>Selector</code> times out value.
     */
    public int getSelectorTimeout(){
        return selectorThread.getSelectorTimeout();
    }


    /**
     * Set the <code>reader-thread</code> from domain.xml.
     */
    public void setMaxReadWorkerThreads(int maxReadWorkerThreads){
//        selectorThread.setMaxReadWorkerThreads(maxReadWorkerThreads);
    }


    /**
     * Return the <code>read-thread</code> used by this <code>Selector</code>
     */
    public int getMaxReadWorkerThreads(){
//        return selectorThread.getMaxReadWorkerThreads();
        return 0;
    }


    public void setDisplayConfiguration(boolean displayConfiguration){
        selectorThread.setDisplayConfiguration(displayConfiguration);
    }


    public boolean getDisplayConfiguration(){
//        return selectorThread.getDisplay();
        return false;
    }


    /**
     * Set the <code>recycle-tasks</code> by this <code>Selector</code>
     */
    public void setRecycleTasks(boolean recycleTasks){
//        selectorThread.setRecycleTasks(recycleTasks);
    }


    /**
     * Return the <code>recycle-tasks</code> used by this
     * <code>Selector</code>
     */
    public boolean getRecycleTasks(){
//        return selectorThread.isRecycleTasks();
        return false;
    }


    public void setUseByteBufferView(boolean useByteBufferView){
        selectorThread.setUseByteBufferView(useByteBufferView);
    }


    public boolean getUseByteBufferView(){
        return selectorThread.isUseByteBufferView() ;
    }


    /**
     * Set the <code>processor-thread</code> from domain.xml
     */
    public void setMaxProcessorWorkerThreads(int maxProcessorWorkerThreads){
        selectorThread.setMaxThreads(maxProcessorWorkerThreads);
    }


    /**
     * Return the <code>processor-thread</code> used by this <code>Selector</code>
     */
    public int getMaxProcessorWorkerThreads(){
        return selectorThread.getMaxThreads();
    }


    /**
     * Set the <code>reader-queue-length</code> value
     * on this <code>Selector</code>
     */
    public void setMinReadQueueLength(int minReadQueueLength){
//        selectorThread.setMinReadQueueLength(minReadQueueLength);
    }


    /**
     * Return the <code>reader-queue-length</code> value
     * on this <code>Selector</code>
     */
    public int getMinReadQueueLength(){
//        return selectorThread.getMinReadQueueLength();
        return 0;
    }


    /**
     * Set the <code>processor-queue-length</code> value
     * on this <code>Selector</code>
     */
    public void setMinProcessorQueueLength(int minProcessorQueueLength){
//        selectorThread.setMinProcessorQueueLength(minProcessorQueueLength);
    }


    /**
     * Return the <code>processor-queue-length</code> value
     * on this <code>Selector</code>
     */
    public int getMinProcessorQueueLength(){
//        return selectorThread.getMinProcessorQueueLength();
        return 0;
    }


    /**
     * Set the <code>use-nio-non-blocking</code> by this <code>Selector</code>
     */
    public void setUseDirectByteBuffer(boolean useDirectByteBuffer){
        selectorThread.setUseDirectByteBuffer(useDirectByteBuffer);
    }


    /**
     * Return the <code>use-nio-non-blocking</code> used by this
     * <code>Selector</code>
     */
    public boolean getUseDirectByteBuffer(){
        return selectorThread.isUseDirectByteBuffer();
    }


    /**
     * Set the maximum pending connection this <code>Pipeline</code>
     * can handle.
     */
    /*
     * API was changed. Method doesn't exist in SelectorThread
     */
    public void setQueueSizeInBytes(int maxQueueSizeInBytes){
//        selectorThread.setMaxQueueSizeInBytes(maxQueueSizeInBytes);
    }


    /**
     * Set the <code>SocketServer</code> backlog.
     */
    public void setSocketServerBacklog(int ssBackLog){
        selectorThread.setSsBackLog(ssBackLog);
    }

    /**
     * Set the number of <code>SelectorThread</code> Grizzly will uses.
     */
    public void setSelectorReadThreadsCount(int selectorReadThreadsCount){
        selectorThread.setSelectorReadThreadsCount(selectorReadThreadsCount);
    }


    /**
     * Set the default response type used. Specified as a semi-colon
     * delimited string consisting of content-type, encoding,
     * language, charset
     */
    public void setDefaultResponseType(String defaultResponseType){
         selectorThread.setDefaultResponseType(defaultResponseType);
    }


    /**
     * Return the default response type used
     */
    public String getDefaultResponseType(){
         return  selectorThread.getDefaultResponseType();
    }


    /**
     * Sets the forced request type, which is forced onto requests that
     * do not already specify any MIME type.
     */
    public void setForcedRequestType(String forcedResponseType){
        selectorThread.setForcedRequestType(forcedResponseType);
    }


    /**
     * Return the default request type used
     */
    public String getForcedRequestType(){
        return  selectorThread.getForcedRequestType();
    }


    //------------------------------------------------- FileCache config -----/


    /**
     * The timeout in seconds before remove a <code>FileCacheEntry</code>
     * from the <code>fileCache</code>
     */
    public void setSecondsMaxAge(int sMaxAges){
        selectorThread.setSecondsMaxAge(sMaxAges);
    }


    /**
     * Set the maximum entries this cache can contains.
     */
    public void setMaxCacheEntries(int mEntries){
        selectorThread.setMaxCacheEntries(mEntries);
    }


    /**
     * Return the maximum entries this cache can contains.
     */
    public int getMaxCacheEntries(){
        return selectorThread.getMaxCacheEntries();
    }


    /**
     * Set the maximum size a <code>FileCacheEntry</code> can have.
     */
    public void setMinEntrySize(long mSize){
        selectorThread.setMinEntrySize(mSize);
    }


    /**
     * Get the maximum size a <code>FileCacheEntry</code> can have.
     */
    public long getMinEntrySize(){
        return selectorThread.getMinEntrySize();
    }


    /**
     * Set the maximum size a <code>FileCacheEntry</code> can have.
     */
    public void setMaxEntrySize(long mEntrySize){
        selectorThread.setMaxEntrySize(mEntrySize);
    }


    /**
     * Get the maximum size a <code>FileCacheEntry</code> can have.
     */
    public long getMaxEntrySize(){
        return selectorThread.getMaxEntrySize();
    }


    /**
     * Set the maximum cache size
     */
    public void setMaxLargeCacheSize(long mCacheSize){
        selectorThread.setMaxLargeCacheSize(mCacheSize);
    }


    /**
     * Get the maximum cache size
     */
    public long getMaxLargeCacheSize(){
        return selectorThread.getMaxLargeCacheSize();
    }


    /**
     * Set the maximum cache size
     */
    public void setMaxSmallCacheSize(long mCacheSize){
        selectorThread.setMaxSmallCacheSize(mCacheSize);
    }


    /**
     * Get the maximum cache size
     */
    public long getMaxSmallCacheSize(){
        return selectorThread.getMaxSmallCacheSize();
    }


    /**
     * Is the fileCache enabled.
     */
    public boolean isFileCacheEnabled(){
        return selectorThread.isFileCacheEnabled();
    }


    /**
     * Is the file caching mechanism enabled.
     */
    public void setFileCacheEnabled(boolean isFileCacheEnabled){
        selectorThread.setFileCacheIsEnabled(isFileCacheEnabled);
    }


    /**
     * Is the large file cache support enabled.
     */
    public void setLargeFileCacheEnabled(boolean isLargeEnabled){
        selectorThread.setLargeFileCacheEnabled(isLargeEnabled);
    }


    /**
     * Is the large file cache support enabled.
     */
    public boolean getLargeFileCacheEnabled(){
        return selectorThread.getLargeFileCacheEnabled();
    }


    /**
     * Set the documenr root folder
     */
    public void setWebAppRootPath(String rootFolder){
        selectorThread.setWebAppRootPath(rootFolder);
    }


    /**
     * Return the folder's root where application are deployed.
     */
    public String getWebAppRootPath(){
        return selectorThread.getWebAppRootPath();
    }


    /**
     * Return the instance of SelectorThread used by this class.
     */
    public SelectorThread selectorThread(){
        return selectorThread;
    }


    public void setCometSupport(boolean cometSupport){
        //TODO: Should Comet has its own Container.
    }


    public void setRcmSupport(boolean rcmSupport){
        selectorThread.enableRcmSupport(rcmSupport);
    }


    // --------------------------------------------------------- Private method

    /** Sanity check and socketFactory setup.
     *  IMHO it is better to stop the show on a broken connector,
     *  then leave Tomcat running and broken.
     *  @exception Exception Unable to resolve classes
     */
    @SuppressWarnings("unchecked")
    private void checkSocketFactory() throws Exception {
        if ( !blocking && !secure) return;

        SSLSelectorThread secureSel = (SSLSelectorThread)selectorThread;
        if (secure) {
            // The SSL setup code has been moved into
            // SSLImplementation since SocketFactory doesn't
            // provide a wide enough interface
            sslImplementation =
                SSLImplementation.getInstance(sslImplementationName);

            socketFactory = sslImplementation.getServerSocketFactory();

            secureSel.setSSLImplementation(sslImplementation);
            secureSel.setEnabledCipherSuites(toStringArray(getCiphers()));
            secureSel.setEnabledProtocols(toStringArray(getProtocols()));
            String clientAuthStr = (String) getAttribute("clientauth");
            if (clientAuthStr != null){
                secureSel.setNeedClientAuth(Boolean.valueOf(clientAuthStr));
            }            
        } else if (socketFactoryName != null) {
            socketFactory = string2SocketFactory(socketFactoryName);
        }
        
        if(socketFactory==null)
            socketFactory=ServerSocketFactory.getDefault();
    }
    
    
    private static final String[] toStringArray(String list){
        
        if ( list == null ) return null;
        
        StringTokenizer st = new StringTokenizer(list,",");
        String[] array = new String[st.countTokens()];
        int i = 0;
        while(st.hasMoreTokens()){
            array[i++] = st.nextToken();
        }
        return array;
    }
       
    
    /**
     * JMX Wrapper around a JMX implementation.
     */
    static class ModelerManagement implements Management{

        @SuppressWarnings("deprecation")
        public void registerComponent(Object bean, ObjectName oname, String type) 
                throws Exception{
            Registry.getRegistry(null, null).registerComponent(bean,oname,type);
        }

        @SuppressWarnings("deprecation")
        public void unregisterComponent(ObjectName oname) throws Exception{
            Registry.getRegistry(null, null).
                    unregisterComponent(oname);
        }     
    }
 

    public void setReuseAddress(boolean reuseAddress){
        selectorThread.setReuseAddress(reuseAddress);
    }

    public boolean getReuseAddress(){
        return selectorThread.getReuseAddress();
    }

    public String getProxiedProtocols() {
        return proxiedProtocols;
    }

    
    public void setProxiedProtocols(String proxiedProtocols) {
        this.proxiedProtocols = proxiedProtocols;
    }
}
