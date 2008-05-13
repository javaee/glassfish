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

package com.sun.enterprise.web.connector.coyote;

import com.sun.enterprise.web.pwc.connector.coyote.PwcCoyoteRequest;
import com.sun.enterprise.web.connector.extension.GrizzlyConfig;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.coyote.tomcat5.CoyoteConnector;
import org.apache.coyote.tomcat5.CoyoteResponse;
import org.apache.coyote.tomcat5.Constants;
import org.apache.coyote.tomcat5.MapperListener;

import org.apache.catalina.LifecycleException;

public class PECoyoteConnector extends CoyoteConnector{


    private static final String DUMMY_CONNECTOR_LAUNCHER = 
                com.sun.enterprise.web.
                    connector.grizzly.DummyConnectorLauncher.class.getName();

   
    /**
     * Are we recycling objects
     */
    protected boolean recycleObjects;
    
    
     /**
     * The number of acceptor threads.
     */   
    protected int maxAcceptWorkerThreads;
    
    
    /**
     * The number of reader threads.
     */
    protected int maxReadWorkerThreads;
    
    
    /**
     * The request timeout value used by the processor threads.
     */
    protected int processorWorkerThreadsTimeout;
    
    
    /**
     * The increment number used by the processor threads.
     */
    protected int minProcessorWorkerThreadsIncrement;
    
    
    /**
     * The size of the accept queue.
     */
    protected int minAcceptQueueLength;
    
    
    /**
     * The size of the read queue
     */
    protected int minReadQueueLength;
    
    
    /**
     * The size of the processor queue.
     */ 
    protected int minProcessorQueueLength;   
    
    
    /**
     * Use direct or non direct byte buffer.
     */
    protected boolean useDirectByteBuffer;
    
    
    // Are we using the NIO Connector or the CoyoteConnector
    private boolean coyoteOn = false;
    
    /*
     * Number of seconds before idle keep-alive connections expire
     */
    private int keepAliveTimeoutInSeconds;

    /*
     * Number of keep-alive threads
     */
    private int keepAliveThreadCount;

    /*
     * Specifies whether response chunking is enabled/disabled
     */
    private boolean chunkingDisabled;

    /**
     * Maximum pending connection before refusing requests.
     */
    private int queueSizeInBytes = 4096;
  
    /**
     * Server socket backlog.
     */
    protected int ssBackLog = 4096;    
    
    
    /**
     * Set the number of <code>Selector</code> used by Grizzly.
     */
    public int selectorReadThreadsCount = 0;
    
      
    /**
     * The default response-type
     */
    protected String defaultResponseType = "text/plain; charset=iso-8859-1";


    /**
     * The forced request-type
     */
    protected String forcedRequestType = "text/plain; charset=iso-8859-1"; 
    
    
    /**
     * The monitoring classes used to gather stats.
     */
    protected GrizzlyConfig grizzlyMonitor;

    
    /**
     * The root folder where application are deployed
     */
    private String rootFolder = "";    
    
    
    /**
     * The http-listener name
     */
    private String name;
    // ------------------------------------------------- FileCache support --//
    
    /**
     * Timeout before remove the static resource from the cache.
     */
    private int secondsMaxAge = -1;
    
    
    /**
     * The maximum entries in the <code>fileCache</code>
     */
    private int maxCacheEntries = 1024;
    
 
    /**
     * The maximum size of a cached resources.
     */
    private long minEntrySize = 2048;
            
               
    /**
     * The maximum size of a cached resources.
     */
    private long maxEntrySize = 537600;
    
    
    /**
     * The maximum cached bytes
     */
    private long maxLargeFileCacheSize = 10485760;
 
    
    /**
     * The maximum cached bytes
     */
    private long maxSmallFileCacheSize = 1048576;
    
    
    /**
     * Is the FileCache enabled.
     */
    private boolean fileCacheEnabled = true;
    
    
    /**
     * Is the large FileCache enabled.
     */
    private boolean isLargeFileCacheEnabled = true;    
    

    /**
     * Location of the CRL file
     */
    private String crlFile;    


    /**
     * The trust management algorithm
     */
    private String trustAlgorithm;    


    /**
     * The maximum number of non-self-issued intermediate
     * certificates that may exist in a certification path
     */
    private String trustMaxCertLength;

    
    /**
     * The logger used by the <code>ProtocolHandler</code>
     */
    private Logger logger;

    // ----------------------------------------------------------------------//        
   
    
    public PECoyoteConnector() {
        setProtocolHandlerClassName(DUMMY_CONNECTOR_LAUNCHER);
    }
    

    /**
     * Enables or disables chunked encoding for any responses returned by this
     * Connector.
     *
     * @param chunkingDisabled true if chunking is to be disabled, false
     * otherwise
     */
    public void setChunkingDisabled(boolean chunkingDisabled) {
        this.chunkingDisabled = chunkingDisabled;
    }


    /**
     * @return true if chunking is disabled on this Connector, and false
     * otherwise
     */
    public boolean isChunkingDisabled() {
        return this.chunkingDisabled;
    }


    /** 
     * Create (or allocate) and return a Request object suitable for
     * specifying the contents of a Request to the responsible ContractProvider.
     */
    @Override
    public Request createRequest() {
        
        PwcCoyoteRequest request = new PwcCoyoteRequest();
        request.setConnector(this);
        return (request);

    }


    /**
     * Creates and returns Response object.
     *
     * @return Response object
     */ 
    @Override
    public Response createResponse() {

        PECoyoteResponse response = new PECoyoteResponse(isChunkingDisabled());
        response.setConnector(this);
        return (response);

    }


    /**
     * Gets the number of seconds before a keep-alive connection that has
     * been idle times out and is closed.
     *
     * @return Keep-alive timeout in number of seconds
     */
    public int getKeepAliveTimeoutInSeconds() {
        return keepAliveTimeoutInSeconds;
    }


    /**
     * Sets the number of seconds before a keep-alive connection that has
     * been idle times out and is closed.
     *
     * @param timeout Keep-alive timeout in number of seconds
     */
    public void setKeepAliveTimeoutInSeconds(int timeout) {
        keepAliveTimeoutInSeconds = timeout;
        setProperty("keepAliveTimeoutInSeconds", String.valueOf(timeout));
    }


    /**
     * Gets the number of keep-alive threads.
     *
     * @return Number of keep-alive threads
     */
    public int getKeepAliveThreadCount() {
        return keepAliveThreadCount;
    }


    /**
     * Sets the number of keep-alive threads.
     *
     * @param threadCount Number of keep-alive threads
     */
    public void setKeepAliveThreadCount(int threadCount) {
        keepAliveThreadCount = threadCount;
        setProperty("keepAliveThreadCount", String.valueOf(threadCount));
    }


    /**
     * Set the maximum pending connection this <code>Connector</code>
     * can handle.
     */
    public void setQueueSizeInBytes(int queueSizeInBytes){
        this.queueSizeInBytes = queueSizeInBytes;
        setProperty("queueSizeInBytes", queueSizeInBytes);
    }


    /**
     * Return the maximum pending connection.
     */
    public int getQueueSizeInBytes(){
        return queueSizeInBytes;
    }

 
    /**
     * Set the <code>SocketServer</code> backlog.
     */
    public void setSocketServerBacklog(int ssBackLog){
        this.ssBackLog = ssBackLog;
        setProperty("socketServerBacklog", ssBackLog);
    }


    /**
     * Return the maximum pending connection.
     */
    public int getSocketServerBacklog(){
        return ssBackLog;
    }

    
    /**
     * Set the <code>recycle-tasks</code> used by this <code>Selector</code>
     */   
    public void setRecycleObjects(boolean recycleObjects){
        this.recycleObjects= recycleObjects;
        setProperty("recycleObjects", 
                    String.valueOf(recycleObjects));
    }
    
    
    /**
     * Return the <code>recycle-tasks</code> used by this 
     * <code>Selector</code>
     */      
    public boolean getRecycleObjects(){
        return recycleObjects;
    }
   
   
    /**
     * Set the <code>reader-thread</code> from domian.xml.
     */    
    public void setMaxReadWorkerThreads(int maxReadWorkerThreads){
        this.maxReadWorkerThreads = maxReadWorkerThreads;
        setProperty("maxReadWorkerThreads", 
                    String.valueOf(maxReadWorkerThreads));
    }
    
    
    /**
     * Return the <code>read-thread</code> used by this <code>Selector</code>
     */  
    public int getMaxReadWorkerThreads(){
        return maxReadWorkerThreads;
    }

    
    /**
     * Set the <code>reader-thread</code> from domian.xml.
     */    
    public void setMaxAcceptWorkerThreads(int maxAcceptWorkerThreads){
        this.maxAcceptWorkerThreads = maxAcceptWorkerThreads;
        setProperty("maxAcceptWorkerThreads", 
                    String.valueOf(maxAcceptWorkerThreads));
    }
    
    
    /**
     * Return the <code>read-thread</code> used by this <code>Selector</code>
     */  
    public int getMaxAcceptWorkerThreads(){
        return maxAcceptWorkerThreads;
    }
    
    
    /**
     * Set the <code>acceptor-queue-length</code> value 
     * on this <code>Selector</code>
     */
    public void setMinAcceptQueueLength(int minAcceptQueueLength){
        this.minAcceptQueueLength = minAcceptQueueLength;
        setProperty("minAcceptQueueLength", 
                    String.valueOf(minAcceptQueueLength));
    }
 
    
    /**
     * Return the <code>acceptor-queue-length</code> value
     * on this <code>Selector</code>
     */
    public int getMinAcceptQueueLength(){
        return minAcceptQueueLength;
    }
    
    
    /**
     * Set the <code>reader-queue-length</code> value 
     * on this <code>Selector</code>
     */
    public void setMinReadQueueLength(int minReadQueueLength){
        this.minReadQueueLength = minReadQueueLength;
        setProperty("minReadQueueLength", 
                    String.valueOf(minReadQueueLength));
    }
    
    
    /**
     * Return the <code>reader-queue-length</code> value
     * on this <code>Selector</code>
     */
    public int getMinReadQueueLength(){
        return minReadQueueLength;
    }
    
    
    /**
     * Set the <code>processor-queue-length</code> value 
     * on this <code>Selector</code>
     */
    public void setMinProcessorQueueLength(int minProcessorQueueLength){
        this.minProcessorQueueLength = minProcessorQueueLength;
        setProperty("minProcessorQueueLength", 
                    String.valueOf(minProcessorQueueLength));
    }
    
    
    /**
     * Return the <code>processor-queue-length</code> value
     * on this <code>Selector</code>
     */  
    public int getMinProcessorQueueLength(){
        return minProcessorQueueLength;
    }
    
    
    /**
     * Set the <code>use-nio-non-blocking</code> by this <code>Selector</code>
     */  
    public void setUseDirectByteBuffer(boolean useDirectByteBuffer){
        this.useDirectByteBuffer = useDirectByteBuffer;
        setProperty("useDirectByteBuffer", 
                    String.valueOf(useDirectByteBuffer));
    }
    
    
    /**
     * Return the <code>use-nio-non-blocking</code> used by this 
     * <code>Selector</code>
     */    
    public boolean getUseDirectByteBuffer(){
        return useDirectByteBuffer;
    }

    
    public void setProcessorWorkerThreadsTimeout(int timeout){
        this.processorWorkerThreadsTimeout = timeout;
        setProperty("processorWorkerThreadsTimeout", 
                    String.valueOf(timeout));        
    }
    
    
    public int getProcessorWorkerThreadsTimeout(){
        return processorWorkerThreadsTimeout;
    }
    
    
    public void setProcessorWorkerThreadsIncrement(int increment){
        this.minProcessorWorkerThreadsIncrement = increment;
        setProperty("processorThreadsIncrement", 
                    String.valueOf(increment));      
    }
    
    
    public int getMinProcessorWorkerThreadsIncrement(){
        return minProcessorWorkerThreadsIncrement;
    }
 
    public void setSelectorReadThreadsCount(int selectorReadThreadsCount){
        setProperty("selectorReadThreadsCount", 
                     String.valueOf(selectorReadThreadsCount)); 
    }
    
    
    /**
     * Set the default response type used. Specified as a semi-colon
     * delimited string consisting of content-type, encoding,
     * language, charset
     */
    public void setDefaultResponseType(String defaultResponseType){
        this.defaultResponseType = defaultResponseType;
        setProperty("defaultResponseType", defaultResponseType);             
    }


    /**
     * Return the default response type used
     */
    public String getDefaultResponseType(){
         return defaultResponseType;
    }
    
    
    /**
     * Sets the forced request type, which is forced onto requests that
     * do not already specify any MIME type.
     */
    public void setForcedRequestType(String forcedResponseType){
        this.forcedRequestType = forcedResponseType;
        setProperty("forcedRequestType", forcedResponseType);                     
    }  
    
        
    /**
     * Return the default request type used
     */
    public String getForcedRequestType(){
        return forcedRequestType;
    } 

    
    public void start() throws LifecycleException {
        super.start();  
        if ( grizzlyMonitor != null ) {
            grizzlyMonitor.initConfig();
            grizzlyMonitor.registerMonitoringLevelEvents();
        }
    }
    
    
    public void stop() throws LifecycleException {
        super.stop(); 
        if ( grizzlyMonitor != null ) {
            grizzlyMonitor.destroy();
            grizzlyMonitor=null;
        }
    }
   //------------------------------------------------- FileCache config -----/

   
    /**
     * The timeout in seconds before remove a <code>FileCacheEntry</code>
     * from the <code>fileCache</code>
     */
    public void setSecondsMaxAge(int sMaxAges){
        secondsMaxAge = sMaxAges;
        setProperty("secondsMaxAge", String.valueOf(secondsMaxAge));        
    }
    
    
    /**
     * Set the maximum entries this cache can contains.
     */
    public void setMaxCacheEntries(int mEntries){
        maxCacheEntries = mEntries;
        setProperty("maxCacheEntries", String.valueOf(maxCacheEntries));         
    }

    
    /**
     * Return the maximum entries this cache can contains.
     */    
    public int getMaxCacheEntries(){
        return maxCacheEntries;
    }
    
    
    /**
     * Set the maximum size a <code>FileCacheEntry</code> can have.
     */
    public void setMinEntrySize(long mSize){
        minEntrySize = mSize;
        setProperty("minEntrySize", String.valueOf(minEntrySize));         
    }
    
    
    /**
     * Get the maximum size a <code>FileCacheEntry</code> can have.
     */
    public long getMinEntrySize(){
        return minEntrySize;
    }
     
    
    /**
     * Set the maximum size a <code>FileCacheEntry</code> can have.
     */
    public void setMaxEntrySize(long mEntrySize){
        maxEntrySize = mEntrySize;
        setProperty("maxEntrySize", String.valueOf(maxEntrySize));        
    }
    
    
    /**
     * Get the maximum size a <code>FileCacheEntry</code> can have.
     */
    public long getMaxEntrySize(){
        return maxEntrySize;
    }
    
    
    /**
     * Set the maximum cache size
     */ 
    public void setMaxLargeCacheSize(long mCacheSize){
        maxLargeFileCacheSize = mCacheSize;
        setProperty("maxLargeFileCacheSize", 
                String.valueOf(maxLargeFileCacheSize));          
    }

    
    /**
     * Get the maximum cache size
     */ 
    public long getMaxLargeCacheSize(){
        return maxLargeFileCacheSize;
    }
    
    
    /**
     * Set the maximum cache size
     */ 
    public void setMaxSmallCacheSize(long mCacheSize){
        maxSmallFileCacheSize = mCacheSize;
        setProperty("maxSmallFileCacheSize", 
                String.valueOf(maxSmallFileCacheSize));         
    }
    
    
    /**
     * Get the maximum cache size
     */ 
    public long getMaxSmallCacheSize(){
        return maxSmallFileCacheSize;
    }    

    
    /**
     * Is the fileCache enabled.
     */
    public boolean isFileCacheEnabled(){
        return fileCacheEnabled;
    }

    
    /**
     * Is the file caching mechanism enabled.
     */
    public void setFileCacheEnabled(boolean fileCacheEnabled){
        this.fileCacheEnabled = fileCacheEnabled;
        setProperty("fileCacheEnabled",String.valueOf(fileCacheEnabled));
    }
   
    
    /**
     * Is the large file cache support enabled.
     */
    public void setLargeFileCacheEnabled(boolean isLargeEnabled){
        this.isLargeFileCacheEnabled = isLargeEnabled;
        setProperty("largeFileCacheEnabled",
                String.valueOf(isLargeFileCacheEnabled));                 
    }
   
    
    /**
     * Is the large file cache support enabled.
     */
    public boolean getLargeFileCacheEnabled(){
        return isLargeFileCacheEnabled;
    } 

    // --------------------------------------------------------------------//
         
    
    /**
     * Set the documenr root folder
     */
    public void setWebAppRootPath(String rootFolder){
        this.rootFolder = rootFolder;
        setProperty("webAppRootPath",rootFolder);     
    }
    
    
    /**
     * Return the folder's root where application are deployed.
     */
    public String getWebAppRootPath(){
        return rootFolder;
    }
    
    
    /**
     * Set the <code>Logger</code> of the <code>ProtocolHandler</code> instance 
     * used by this class.
     */
    public void setLogger(Logger logger){
        this.logger = logger;
    }
    
    
    /**
     * Initialize this connector.
     */
    @Override
    public void initialize() throws LifecycleException{
        super.initialize();
        // Set the monitoring.
        grizzlyMonitor = new GrizzlyConfig(domain,getPort());
    }


    /**
     * Set the name of this connector.
     */
    public void setName(String name){
        this.name = name;
    }
    
    
    /**
     * Get the name of this connector;
     */
    public String getName(){
        return name;
    }


    /**
     * Sets the truststore location of this connector.
     *
     * @param truststore The truststore location
     */
    public void setTruststore(String truststore) {
        setProperty("truststore", truststore);
    }


    /**
     * Gets the truststore location of this connector.
     *
     * @return The truststore location
     */
    public String getTruststore() {
        return (String) getProperty("truststore");
    }


    /**
     * Sets the truststore type of this connector.
     *
     * @param type The truststore type
     */
    public void setTruststoreType(String type) {
        setProperty("truststoreType", type);
    }


    /**
     * Gets the truststore type of this connector.
     *
     * @return The truststore type
     */
    public String getTruststoreType() {
        return (String) getProperty("truststoreType");
    }


    /**
     * Sets the keystore type of this connector.
     *
     * @param type The keystore type
     */
    public void setKeystoreType(String type) {
        setProperty("keystoreType", type);
    }


    /**
     * Gets the keystore type of this connector.
     *
     * @return The keystore type
     */
    public String getKeystoreType() {
        return (String) getProperty("keystoreType");
    }


    /**
     * Gets the location of the CRL file
     *
     * @return The location of the CRL file 
     */
    public String getCrlFile() {
         return crlFile;
    }
    
    
    /**
     * Sets the location of the CRL file.
     *
     * @param crlFile The location of the CRL file
     */
    public void setCrlFile(String crlFile) {
        this.crlFile = crlFile;
        setProperty("crlFile", crlFile);                     
    }  


    /**
     * Gets the trust management algorithm
     *
     * @return The trust management algorithm
     */
    public String getTrustAlgorithm() {
         return trustAlgorithm;
    }
    
    
    /**
     * Sets the trust management algorithm
     *
     * @param trustAlgorithm The trust management algorithm
     */
    public void setTrustAlgorithm(String trustAlgorithm) {
        this.trustAlgorithm = trustAlgorithm;
        setProperty("truststoreAlgorithm", trustAlgorithm);
    }  


    /**
     * Gets the maximum number of non-self-issued intermediate
     * certificates that may exist in a certification path
     *
     * @return The maximum number of non-self-issued intermediate
     * certificates that may exist in a certification path
     */
    public String getTrustMaxCertLength() {
         return trustMaxCertLength;
    }
    
    
    /**
     * Sets the maximum number of non-self-issued intermediate
     * certificates that may exist in a certification path
     *
     * @param trustMaxCertLength The maximum number of non-self-issued
     * intermediate certificates that may exist in a certification path
     */
    public void setTrustMaxCertLength(String trustMaxCertLength) {
        this.trustMaxCertLength = trustMaxCertLength;
        setProperty("trustMaxCertLength", trustMaxCertLength);
    }  


    /**
     * Gets the MapperListener of this connector.
     *
     * @return The MapperListener of this connector
     */
    public MapperListener getMapperListener() {
        return mapperListener;
    }
}

